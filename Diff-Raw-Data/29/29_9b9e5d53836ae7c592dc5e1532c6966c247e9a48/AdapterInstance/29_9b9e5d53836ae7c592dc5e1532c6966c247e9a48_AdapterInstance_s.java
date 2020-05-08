 /**
  *
  * Software written by Remi Emonet.
  *
  */
 package com.heeere.eaa.adaptertools;
 
import com.heeere.eaa.adaptertools.*;
 import fr.prima.omiscid.user.connector.ConnectorListener;
 import fr.prima.omiscid.user.connector.ConnectorType;
 import fr.prima.omiscid.user.connector.Message;
 import fr.prima.omiscid.user.service.Service;
 import fr.prima.omiscid.user.service.ServiceFilters;
 import fr.prima.omiscid.user.service.ServiceProxy;
 import fr.prima.omiscid.user.util.Utility;
 import fr.prima.omiscid.user.variable.LocalVariableListener;
 import fr.prima.omiscid.user.variable.VariableAccessType;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 abstract class ConnectorListenerAdapter implements ConnectorListener {
 
     public void connected(Service arg0, String arg1, int arg2) {
     }
 
     public void messageReceived(Service arg0, String arg1, Message arg2) {
     }
 
     public void disconnected(Service arg0, String arg1, int arg2) {
     }
 }
 
 abstract class VariableListenerAdapter implements LocalVariableListener {
 
     public boolean isValid(Service arg0, String arg1, String arg2) {
         return true;
     }
 
     public void variableChanged(Service arg0, String arg1, String arg2) {
     }
 }
 
 public class AdapterInstance {
 
     public static final String killServiceVariable = "killService";
     public static final String providesVariable = "provides";
     public static final String factoryIdVariable = "factoryId";
     public static final String factoryParametersVariable = "factoryParameters";
     public final Set<String> variables = new HashSet<String>();
     private Service service;
     private Timer timer = new Timer(); // used for sending delayed messages
 
     public void start() {
         service.start();
     }
 
     public void stop() {
         service.stop();
     }
 
     private String trunc(String s, int limit) {
         if (s.length() <= limit) {
             return s;
         }
         return s.substring(0, limit - 1) + "…";
     }
 
     private boolean isJavascript(String code) {
         return code.startsWith("js:");
     }
 
     private boolean isXslt(String code) {
         return code.startsWith("xslt:") || code.startsWith("xsl:");
     }
 
     private String code(String code) {
         return code.replaceFirst("^\\s*\\w+[:]", "");
     }
 
     private ServiceProxy findService(Service s, String idAsString) {
         return s.findService(ServiceFilters.peerIdIs(Utility.hexStringToInt(idAsString)), 8000);
     }
     Transformer transformer;
 
     private void initTransformer(Map<String, String> parameters, String adaptationCode) {
         try {
             for (String it : variables) {
                 parameters.put(it, service.getVariableValue(it));
             }
             transformer = createTransformerFromTemplate(code(adaptationCode), parameters);
         } catch (TransformerConfigurationException ex) {
             Logger.getLogger(AdapterInstance.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public AdapterInstance(Service factoryService, final Service adapter, String to, String rawParameters, final Map<String, String> parameters, boolean killable, String startCodeOrNull, final String adaptationCodeOrNull) {
         this.service = adapter;
 
         adapter.addVariable(providesVariable, "...", "provided functionalities", VariableAccessType.CONSTANT);
         adapter.setVariableValue(providesVariable, replaceVariables(to, parameters));
         adapter.addVariable(factoryIdVariable, "PeerIdAsString", "Id of the creator of this service", VariableAccessType.CONSTANT);
         adapter.setVariableValue(factoryIdVariable, factoryService.getPeerIdAsString());
         adapter.addVariable(factoryParametersVariable, "line-list", "Parameters for creator of this service", VariableAccessType.CONSTANT);
         adapter.setVariableValue(factoryParametersVariable, rawParameters);
 
         if (killable) {
             adapter.addVariable(killServiceVariable, "string", "change to anything else to kill the service", VariableAccessType.READ_WRITE);
             adapter.setVariableValue(killServiceVariable, "alive");
             adapter.addLocalVariableListener(killServiceVariable, new VariableListenerAdapter() {
                 @Override
                 public void variableChanged(Service s, String name, String value) {
                     s.stop();
                 }
             });
         }
 
         if (startCodeOrNull != null) {
             if (!isJavascript(startCodeOrNull)) {
                 throw new UnsupportedOperationException("Unhandled start language for '" + trunc(startCodeOrNull, 10) + "'");
             }
             String dependency = rawParameters.split("\\n")[0];
             final ServiceProxy sourceService = findService(adapter, dependency);
             String startCode = code(startCodeOrNull);
             ConnectorListener listener = new ConnectorListenerAdapter() {
             };
             if (adaptationCodeOrNull != null) {
                 if (!isXslt(adaptationCodeOrNull)) {
                     throw new UnsupportedOperationException("Unhandled adapter language for '" + trunc(adaptationCodeOrNull, 10) + "'");
                 }
                 listener = new ConnectorListenerAdapter() {
                     @Override
                     public void messageReceived(final Service s, String drainConnectorName, Message m) {
                         if (transformer == null) {
                             initTransformer(parameters, adaptationCodeOrNull);
                         }
                         String connectorName = sourceService.findConnector(m.getPeerId());
                         try {
                             Source input;
                             {
                                 Document doc = Utility.Xml.createDocument(connectorName);
                                 Element mAsXML = m.getBufferAsXMLUnchecked();
                                 doc.adoptNode(mAsXML);
                                 doc.getDocumentElement().appendChild(mAsXML);
                                 input = new DOMSource(doc.getDocumentElement());
                             }
                             DOMResult output = new DOMResult();
                             for (String it : variables) {
                                 transformer.setParameter(it, adapter.getVariableValue(it));
                             }
                             transformer.transform(input, output);
                             if (output.getNode().hasChildNodes()) {
                                 Node root = output.getNode().getChildNodes().item(0);
                                 List<Element> toProcess = new ArrayList<Element>();
                                 if ("message".equals(root.getNodeName())
                                         || "setvariable".equals(root.getNodeName())) { // single message
                                     toProcess.add((Element) root);
                                 } else {
                                     NodeList children = root.getChildNodes();
                                     for (int i = 0; i < children.getLength(); i++) {
                                         if (children.item(i) instanceof Element) {
                                             toProcess.add((Element) children.item(i));
                                         }
                                     }
                                 }
                                 for (Element element : toProcess) {
                                     if ("setvariable".equals(element.getNodeName())) {
                                         String var = element.getAttribute("on");
                                         String content = element.getTextContent();
                                         adapter.setVariableValue(var, content);
                                         //System.out.println("setvariable:"+var+" "+content);
                                     } else if ("message".equals(element.getNodeName())) {
                                         String delay = element.getAttribute("delay");
                                         String destination = element.getAttribute("on");
                                         String type = element.hasAttribute("type") ? element.getAttribute("type") : "xml";
                                         if (destination.isEmpty()) {
                                             throw new IllegalArgumentException("Missing “" + "on" + "” attribute in output of xslt stylesheet");
                                         }
                                         byte[] toSend = null;
                                         if ("xml".equals(type)) {
                                             Element messageContent = null;
                                             {
                                                 NodeList children = element.getChildNodes();
                                                 for (int i = 0; i < children.getLength(); i++) {
                                                     if (children.item(i) instanceof Element) {
                                                         if (messageContent != null) {
                                                             throw new IllegalArgumentException("Multiple elements in a “" + "message" + "” in output of xslt stylesheet");
                                                         }
                                                         messageContent = (Element) children.item(i);
                                                     }
                                                 }
                                             }
                                             toSend = Utility.Xml.elementToByteArray(messageContent);
                                         } else if ("text".equals(type)) {
                                             if (element.getChildNodes().getLength() > 1) {
                                                 throw new IllegalArgumentException("Multiple children in a “" + "message" + "” of type “" + "text" + "”");
                                             }
                                             toSend = Utility.message(element.getTextContent());
                                         } else {
                                             throw new IllegalArgumentException("Unsupported type “" + type + "” as output message type of xslt stylesheet");
                                         }
                                         if (toSend != null) {
                                             if (delay.isEmpty()) {
                                                 s.sendToAllClients(destination, toSend);
                                             } else {
                                                 try {
                                                     long waitTime = Long.parseLong(delay);
                                                     final String fD = destination;
                                                     final byte[] fM = toSend;
                                                     timer.schedule(new TimerTask() {
                                                         @Override
                                                         public void run() {
                                                             s.sendToAllClients(fD, fM);
                                                         }
                                                     }, waitTime);
                                                 } catch (NumberFormatException ex) {
                                                     throw new IllegalArgumentException("wrong delay “" + delay + "”");
                                                 }
                                             }
                                         }
                                     } else {
                                         throw new IllegalArgumentException("Unsupported tag “" + element.getNodeName() + "” as output of xslt stylesheet");
                                     }
                                 }
                             }
                         } catch (TransformerException ex) {
                             Logger.getLogger(AdapterInstance.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
 
                     @Override
                     public void disconnected(Service service, String connectorName, int peerId) {
                         if (service.getConnectorClientCount(connectorName) == 0) {
                             service.stop();
                         }
                     }
                 };
             }
             ScriptEngine eng = new ScriptEngineManager().getEngineByExtension("js");
             eng.put("__dep__", sourceService);
             eng.put("__lis__", listener);
             eng.put("__adaptinstance__", this);
             eng.put("s", adapter);
             eng.put("OUTPUT", ConnectorType.OUTPUT);
             eng.put("INPUT", ConnectorType.INPUT);
             eng.put("READ", VariableAccessType.READ);
             eng.put("READWRITE", VariableAccessType.READ_WRITE);
             try {
                 eng.eval("var addOutput = function(o) {s.addConnector(o, 'no description',  OUTPUT)}");
                 eng.eval("var addVariable = function(o) {s.addVariable(o, 'string', 'no description', READWRITE);s.setVariableValue(o,'-1'); __adaptinstance__.variables.add(o);}");
                 eng.eval("var tryAddInput = function(i) {try {s.addConnector(i, 'no description',  INPUT)} catch(err) {return;} s.addConnectorListener(i, __lis__);}");
                 eng.eval("var listenTo = function(o) {tryAddInput('drain'); s.connectTo('drain', __dep__,  o)}");
                 eng.eval(startCode);
             } catch (ScriptException ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     public static String replaceVariables(String to, final Map<String, String> parameters) {
         return ReplaceAll.replaceAll(to, "[$][{]([^}]+)[}]", new Replacement() {
             public String replacement(Matcher m) {
                 return parameters.get(m.group(1));
             }
         });
     }
 
     public static String replaceALaConfigure(String to, final Map<String, String> parameters) {
         return ReplaceAll.replaceAll(to, "[@][@][@]([^@]+)[@][@][@]", new Replacement() {
             public String replacement(Matcher m) {
                 return parameters.get(m.group(1));
             }
         });
     }
     private static final String xsltTemplate =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
             + "  <xsl:output method=\"xml\"/>\n"
             + "@@@parameters@@@\n"
             + "  \n"
             + "  <xsl:template match=\"/\">\n"
             + "    <xsl:apply-templates/>\n"
             + "  </xsl:template>\n"
             + "  \n"
             + "@@@custom@@@\n"
             + "  \n"
             + "</xsl:stylesheet>\n";
 
     private Transformer createTransformerFromTemplate(String innerXsltCode, Map<String, String> parameters) throws TransformerConfigurationException {
         Map<String, String> replacements = new HashMap<String, String>();
         {
             replacements.put("custom", innerXsltCode);
             String params = "";
             for (Map.Entry<String, String> e : parameters.entrySet()) {
                 params += "  <xsl:param name='" + e.getKey() + "' />";
             }
             replacements.put("parameters", params);
         }
         String xslStylesheet = replaceALaConfigure(xsltTemplate, replacements);
         Transformer res = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslStylesheet)));
         for (Map.Entry<String, String> e : parameters.entrySet()) {
             res.setParameter(e.getKey(), e.getValue());
         }
         res.setOutputProperty(OutputKeys.INDENT, "yes");
         res.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         return res;
     }
 }

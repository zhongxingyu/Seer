 package com.activiti.bpmn;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.activiti.cycle.impl.connector.signavio.transform.pattern.RemedyTemporarySignavioIncompatibility;
 import org.json.JSONException;
 import org.oryxeditor.server.diagram.Diagram;
 import org.oryxeditor.server.diagram.DiagramBuilder;
 import org.xml.sax.SAXException;
 
 import com.activiti.bpmn.factories.ActivitiStartEventFactory;
 import com.activiti.bpmn.factories.ActivitiTaskFactory;
 
 import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
 import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
 import de.hpi.bpmn2_0.factory.node.StartEventFactory;
 import de.hpi.bpmn2_0.factory.node.TaskFactory;
 import de.hpi.bpmn2_0.model.Definitions;
 import de.hpi.bpmn2_0.transformation.Bpmn2XmlConverter;
 import de.hpi.bpmn2_0.transformation.Diagram2BpmnConverter;
 
 public class Converter {
 
     public static void main(String[] obj) throws Exception {
         for (String file: obj) {
            Matcher m = Pattern.compile(".*\\"+System.getProperty("file.separator")+"(.*)\\.signavio.xml").matcher(file);
             m.matches();
             String output = file.replace(".signavio.xml", ".bpmn20.xml");
             writeXml(convJSon(readJson(file),m.group(1)),output);
         }
     }
     
     public static String convJSon(String json, String procName) throws BpmnConverterException,
             JAXBException, SAXException, ParserConfigurationException, TransformerException, JSONException {
         
         int regexpFlags = Pattern.MULTILINE | Pattern.UNIX_LINES | Pattern.UNICODE_CASE | Pattern.DOTALL;
 
         // get proc description
         String description = null;
         Matcher m = Pattern.compile(".*<description>(.*)</description>.*", regexpFlags).matcher(json);
         if (m.matches()) {
             description = m.group(1);
         }
 
         // get json representation
         m = Pattern.compile(".*<json-representation><!\\[CDATA\\[(.*)]]></json-representation>.*", regexpFlags)
                 .matcher(json);
         m.matches();
         json = m.group(1);
         
         //build own factory list
         List<Class<? extends AbstractBpmnFactory>> classes = AbstractBpmnFactory.getFactoryClasses();
         classes.remove(TaskFactory.class);
         classes.remove(StartEventFactory.class);
         classes.add(ActivitiTaskFactory.class);
         classes.add(ActivitiStartEventFactory.class);
         
         Diagram diagram = DiagramBuilder.parseJson(json);
         Diagram2BpmnConverter converter = new Diagram2BpmnConverter(diagram, classes);
         Definitions bpmnDefinitions = converter.getDefinitionsFromDiagram();
         Bpmn2XmlConverter xmlConverter = new Bpmn2XmlConverter(bpmnDefinitions, "classpath:/META-INF/validation/xsd/BPMN20.xsd");
         String xml = xmlConverter.getXml().toString();
 
         // clear xml
         String result = new RemedyTemporarySignavioIncompatibility().transformBpmn20Xml(xml, procName);
         if (description!=null) {
             result = setAttributeText(result, "process", "name", description);
         }
         
         return result;
     }
     
     private static void writeXml(String result, String outPath) throws IOException {
         OutputStream out = new FileOutputStream(outPath);
         out.write(result.getBytes());
     }
 
     private static String readJson(String inPath) throws IOException {
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(new FileReader(inPath));
         char[] buf = new char[1024];
         int numRead = 0;
         while ((numRead = reader.read(buf)) != -1) {
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         
         return fileData.toString();
     }
 
     // copypaste from activity-cycle
     private static String setAttributeText(String xml, String element, String attributeName, String newValue) {
         int elementStartIndex = xml.indexOf("<" + element);
         if (elementStartIndex == -1)
             return xml;
         int startIndex = xml.indexOf(attributeName + "=\"", elementStartIndex) + attributeName.length() + 2;
         if (startIndex == -1) {
             return xml;
         }
         int endIndex = xml.indexOf("\"", startIndex);
 
         return xml.substring(0, startIndex) + newValue + xml.substring(endIndex);
     }
 
     
 }

 package org.theider.plugin.templates;
 
 import java.io.IOException;
 import java.io.InputStream;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  *
  * @author Tim
  */
 public class DeploymentSaxHandler extends DefaultHandler {
 
     private Deployment deployment = null;
     
     private TemplateMapping templateMapping;
 
     public Deployment getDeployment() {
         return deployment;    
     }
     
     public static Deployment getDeployment(InputStream in) throws IOException {
         try {
             DeploymentSaxHandler handler = new DeploymentSaxHandler();
             // Obtain a new instance of a SAXParserFactory.
             SAXParserFactory factory = SAXParserFactory.newInstance();
             // Specifies that the parser produced by this code will provide support for XML namespaces.
             factory.setNamespaceAware(false);
             // Specifies that the parser produced by this code will validate documents as they are parsed.
             factory.setValidating(false);
             // Creates a new instance of a SAXParser using the currently configured factory parameters.
             SAXParser saxParser = factory.newSAXParser();            
             InputSource ins = new InputSource(in);
             saxParser.parse(ins, handler);
             return handler.getDeployment();
         } catch (ParserConfigurationException ex) {
             throw new IOException("error loading deployment descriptor",ex);
         } catch (SAXException ex) {
             throw new IOException("error loading deployment descriptor",ex);
         }
         
     }
     
     protected enum ParserState {
         DEPLOYMENT,
         BODY,
         TEMPLATE_BODY,
         TEMPLATE_SOURCE,
         FOLDER_BODY,
         DESTINATION_PATH;
     };
     
     private ParserState parserState = ParserState.DEPLOYMENT;
     
     private boolean destFileExecutable;
 
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
         String data = new String(ch,start,length);
         switch(parserState) {
             case TEMPLATE_SOURCE:
                 templateMapping.setTemplateFilename(data);                
                 break;
             case DESTINATION_PATH:
                 templateMapping.setDestinationFilename(data);
                 break;
             case FOLDER_BODY:
                 deployment.getFolderNames().add(data);
                 break;
         }
     }
     
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         switch(parserState) {
             case DEPLOYMENT:
                 if(!qName.equals("deployment")) {
                     throw new SAXException("expecting root node to be deployment");
                 }
                 deployment = new Deployment();
                 parserState = ParserState.BODY;
                 break;
             case BODY:
                 if(qName.equals("template")) {
                     templateMapping = new TemplateMapping();
                     parserState = ParserState.TEMPLATE_BODY;                                    
                 } else if(qName.equals("folder")) {
                     parserState = ParserState.FOLDER_BODY;      
                 } else {
                     throw new SAXException("expecting folder or template node and got " + qName);
                 }
                 break;
             case TEMPLATE_BODY:
                 if(qName.equals("template-filename")) {
                     // template source
                     parserState = ParserState.TEMPLATE_SOURCE;
                 } else
                 if(qName.equals("destination-filename")) {
                     parserState = ParserState.DESTINATION_PATH;
                     destFileExecutable = false;
                     String execFile = attributes.getValue("executable");
                     if(execFile != null) {
                         destFileExecutable = execFile.equalsIgnoreCase("true");
                     }
                 } else {
                    throw new SAXException("expecting root node to be deployment");   
                 }                                    
                 break;                
         }
     }
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         switch(parserState) {
             case TEMPLATE_SOURCE:
                 parserState = ParserState.TEMPLATE_BODY;
                 break;
             case DESTINATION_PATH:
                 parserState = ParserState.TEMPLATE_BODY;
                 break;
             case FOLDER_BODY:
                 if(qName.equals("folder")) {
                     parserState = ParserState.BODY;
                 } else {
                     throw new SAXException("missing end folder tag");
                 }
             case TEMPLATE_BODY:
                 if(qName.equals("template")) {
                     if(templateMapping.getDestinationFilename() == null) {
                         throw new SAXException("template mapping is missing destination path");
                     }
                     if(templateMapping.getTemplateFilename() == null) {
                         throw new SAXException("template mapping is missing template source");
                     }
                     templateMapping.setExecutable(destFileExecutable);
                     deployment.getTemplateMappings().add(templateMapping);
                     parserState = ParserState.BODY;
                 }
                 break;                
         }
     }
 
     
     
 }

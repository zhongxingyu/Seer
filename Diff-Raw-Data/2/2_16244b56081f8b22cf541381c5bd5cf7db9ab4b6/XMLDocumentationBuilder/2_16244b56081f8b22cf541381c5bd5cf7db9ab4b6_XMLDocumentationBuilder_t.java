 package org.codehaus.xfire.service.binding.documentation;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.ServiceInfo;
 import org.codehaus.xfire.util.jdom.StaxBuilder;
 import org.jdom.Document;
 import org.jdom.Element;
 
 /**
  * Builds DocumentationProvider based on XML files.
  * 
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  * 
  */
 public class XMLDocumentationBuilder
 {
     
     protected static Log log = LogFactory.getLog(XMLDocumentationBuilder.class.getName());
     
     private static final String DOCUMENTATION_TAG = "documentation";
 
     private static final String METHOD_TAG = "method";
     
     private static final String PARAMTER_TAG = "parameter";
     
     private static final String NAME_ATTR = "name";
     
     private static final String INDEX_ATTR = "index";
     
     
    private static final String ARGUMENTS_NUMBER_ATTR = "parametersNumber";
 
     private static final String CONFIG_SUFIX = ".doc.xml";
 
     /**
      * @param service
      * @return
      */
     public DocumentationProvider build(ServiceInfo service)
     {
 
         Document doc = loadDocument(service);
         
         return (doc == null ? null : parseDocument(doc));
     }
 
     /**
      * @param doc
      * @return
      */
     protected DocumentationProvider parseDocument(Document doc)
     {
 
         DocumentationProvider docProvider = new DocumentationProvider();
         Element service = doc.getRootElement();
 
         String documentation = readDocumentations(service);
         if (documentation != null)
         {
             docProvider.setServiceDocumentation(documentation);
         }
 
         List operationsList = service.getChildren(METHOD_TAG);
         for (int i = 0; i < operationsList.size(); i++)
         {
             Element element = (Element) operationsList.get(i);
             String name = element.getAttribute(NAME_ATTR).getValue();
             String argNrStr = element.getAttribute(ARGUMENTS_NUMBER_ATTR).getValue();
             String opDocumentation = readDocumentations(element);
             int argNr = Integer.parseInt(argNrStr);
             List params = new ArrayList(argNr);
             for (int p = 0; p < argNr; p++)
             {
                 params.add(null);
             }
             List parameters = element.getChildren(PARAMTER_TAG);
             for( int p=0;p<parameters.size();p++){
                 Element param = (Element) parameters.get(p);
                 String indexStr = param.getAttribute(INDEX_ATTR).getValue();
                 int index = Integer.parseInt(indexStr);
                 String paramDoc = readDocumentations(param);
                 params.set(index, paramDoc);
             }
             docProvider.addOperation(name, opDocumentation, params);
         }
 
         return docProvider;
     }
 
     /**
      * @param elem
      * @return
      */
     private String readDocumentations(Element elem)
     {
 
         List docList = elem.getChildren(DOCUMENTATION_TAG);
         if (docList.size() == 0)
         {
             return null;
         }
         StringBuffer buffer = new StringBuffer();
         for (int i = 0; i < docList.size(); i++)
         {
             Element element = (Element) docList.get(i);
             buffer.append(element.getTextTrim());
         }
 
         return buffer.toString();
 
     }
 
     /**
      * @param service
      * @return
      */
     protected Document loadDocument(ServiceInfo service)
     {
 
         Class clazz = service.getServiceClass();
         int idx = clazz.getName().lastIndexOf(".");
         String className = clazz.getName().substring(idx + 1);
         String fileName = className + CONFIG_SUFIX;
         log.debug("Searching for " + fileName + " config..");
         InputStream inStr = clazz.getResourceAsStream(fileName);
         if (inStr == null)
         {
             log.debug("Config " + fileName + " NOT found.");
             return null;
         }
 
         StaxBuilder builder = new StaxBuilder();
         try
         {
             log.debug("Config " + fileName + " found.");
             return builder.build(inStr);
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException(e.getMessage());
         }
 
     }
 
 }

 package com.kn.groupBuilder.FileOperations.Writer;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.kn.groupBuilder.Exceptions.WriteOperationException;
 import com.kn.groupBuilder.FileOperations.Output.EmailJobHelper;
 import com.kn.groupBuilder.FileOperations.Output.GroupFileArchiver;
 import com.kn.groupBuilder.FileOperations.Output.PrintJobHelper;
 import com.kn.groupBuilder.Storage.Pojo;
 
 /**
  * Helper class for all kind of file writing operations. Initializes the different actions and has some general used methods for
  * the different writers.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_21
  * @version 1.0
  */
 
 public class FileWriteHelper {
 
     public final void createXMLFiles(final Pojo pojo) {
 
         new MemberListWriter().createXmlFile(pojo);
         new GroupListWriter().createXmlFile(pojo);
         new GroupFileWriter().initializeXMLPrint(pojo);
        new GroupFileArchiver().archivGroupFiles(pojo);
         new SettingsFileWriter().createXmlFile(pojo);
         new FileCleaner().updateArchive(pojo);
 
         if (pojo.getSettings().isPrintAutomatically()) {
             new PrintJobHelper(pojo).printAllGroups();
         }
         if (pojo.getSettings().isSendMailAutomatically()) {
             new EmailJobHelper().sendMailsToAll(pojo);
         }
     }
 
     final Element createElement(final Document doc, final Element superiorElement, final String name, final String value) {
         final Element element = doc.createElement(name);
         if (value != null) {
             element.appendChild(doc.createTextNode(value));
         }
         superiorElement.appendChild(element);
         return element;
     }
 
     final Attr createAttribute(final Document doc, final Element superiorElement, final String name, final String value) {
         final Attr attr = doc.createAttribute(name);
         attr.setValue(value);
         return superiorElement.setAttributeNode(attr);
     }
 
     final Element createMainElement(final Document doc, final String name) {
         final Element element = doc.createElement(name);
         doc.appendChild(element);
         return element;
     }
 
     final Document createDocument() {
         try {
             return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         } catch (final ParserConfigurationException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     // write the content into xml file
     final void writeFile(final String path, final String fileName, final Document doc) {
 
         new File(path).mkdirs();
         final File file = new File(path + fileName + ".xml");
 
         try {
             TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(file));
         } catch (final TransformerException e) {
             new WriteOperationException(fileName, e.getStackTrace()).showDialog();
         }
 
     }
 
 }

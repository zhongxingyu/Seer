 package com.socialcomputing.wps.server.web;
 
 import java.io.ByteArrayInputStream;
 import java.io.CharArrayWriter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.jar.JarInputStream;
 import java.util.zip.ZipEntry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 
 import com.socialcomputing.utils.database.HibernateUtil;
 import com.socialcomputing.utils.servlet.ExtendedRequest;
 import com.socialcomputing.utils.servlet.UploadedFile;
 import com.socialcomputing.wps.server.persistence.Dictionary;
 import com.socialcomputing.wps.server.persistence.DictionaryManager;
 import com.socialcomputing.wps.server.persistence.Swatch;
 import com.socialcomputing.wps.server.persistence.SwatchManager;
 import com.socialcomputing.wps.server.persistence.hibernate.DictionaryImpl;
 import com.socialcomputing.wps.server.persistence.hibernate.DictionaryManagerImpl;
 import com.socialcomputing.wps.server.persistence.hibernate.SwatchManagerImpl;
 import com.socialcomputing.wps.server.persistence.hibernate.SwatchPk;
 
 /**
  * Title: Users Description: Copyright: Copyright (c) 2001 Company: VOYEZ VOUS
  * 
  * @author
  * @version 1.0
  */
 
 public class WPSUploadServlet extends HttpServlet {
     /**
 	 * 
 	 */
     private static final long serialVersionUID = 1362617337569904439L;
 
     public long getLastModified(HttpServletRequest request) {
         return System.currentTimeMillis();
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String action = request.getParameter("action");
         if (action != null) {}
         response.sendError(HttpServletResponse.SC_BAD_REQUEST);
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HibernateUtil.currentSession();
         ExtendedRequest exrequest = new ExtendedRequest(request);
         String action = exrequest.getParameter("action");
         String dictionaryName = exrequest.getParameter("dictionary");
         if (action != null) {
             InternalReport report = null;
             if (action.equalsIgnoreCase("updateDictionary") || action.equalsIgnoreCase("updateSwatch")) {
                 report = updateDefinition(exrequest.getParameter("definition"), action, dictionaryName);
             }
             else {
                 report = uploadDefinitionFile(exrequest.getFileParameter("definitionFile"), action, dictionaryName);
             }
             HttpSession session = request.getSession();
             session.setAttribute("UploadDefinitionFileResults", report);
 
             response.setContentType("text/html");
            /*PrintWriter out = response.getWriter();
             out.print("<html><head><meta http-equiv=\"Refresh\" content=\"0; URL=");
             out.print(exrequest.getParameter("redirect"));
             out.print("\"></head></html>");
            out.close();*/
            response.sendRedirect(exrequest.getParameter("redirect"));
             HibernateUtil.closeSession();
             response.setStatus(HttpServletResponse.SC_OK);
             return;
         }
         HibernateUtil.closeSession();
         response.sendError(HttpServletResponse.SC_BAD_REQUEST);
     }
 
     private InternalReport uploadDefinitionFile(UploadedFile file, String action, String dictionaryName)
             throws ServletException, IOException {
         InternalReport report = new InternalReport();
         if (file == null)
             return report;
         try {
             if (file.getContentFilename().endsWith(".xml")) {
                 SAXBuilder saxBuilder = new SAXBuilder(true);
                 saxBuilder.setEntityResolver(new WPSResolver());
                 uploadFile(saxBuilder, report, file.getContentFilename(), new String(file.getBytes()), action,
                            dictionaryName);
             } else
                 uploadZipFile(report, file.getBytes(), action, dictionaryName);
         } catch (Exception e) {
             e.printStackTrace();
             report.setLastActionResult("Error uploadDefinitionFile : " + e.getMessage());
         }
         return report;
     }
 
     private InternalReport updateDefinition(String definition, String action, String dictionaryName)
             throws ServletException, IOException {
         InternalReport report = new InternalReport();
         if (definition == null)
             return report;
         try {
             SAXBuilder saxBuilder = new SAXBuilder(true);
             saxBuilder.setEntityResolver(new WPSResolver());
             if (action.equalsIgnoreCase("updateDictionary")) {
                 definition = "<!DOCTYPE dictionary SYSTEM \"WPS-dictionary.dtd\" >" + definition;
             } else {
                 definition = "<!DOCTYPE swatch SYSTEM \"swatch.dtd\">" + definition;
             }
             Document doc = saxBuilder.build(new StringReader(definition), ".");
             Element root = doc.getRootElement();
 
             CharArrayWriter bout = new CharArrayWriter();
             XMLOutputter op = new XMLOutputter();
             if (action.equalsIgnoreCase("updateDictionary")) {
                 // Update dictionary definition
                 if (root.getName().equalsIgnoreCase("dictionaries")) {  
                     List lst = root.getChildren("dictionary");
                     for (int i = 0; i < lst.size(); ++i) {
                         Element subelem = (Element) lst.get(i);
                         op.output(subelem, bout);
                         uploadDictionary(subelem.getAttributeValue("name"), bout.toString(), report);
                         bout.reset();
                     }
                 } else if (root.getName().equalsIgnoreCase("dictionary")) {
                     op.output(root, bout);
                     uploadDictionary(root.getAttributeValue("name"), bout.toString(), report);
                 }
             } else if (action.equalsIgnoreCase("updateSwatch")) {
                 // Update swatch definition
                 if (root.getName().equalsIgnoreCase("swatch")) {
                     op.output(root, bout);
                     uploadSwatch(root.getAttributeValue("name"), bout.toString(), report, dictionaryName);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             report.setLastActionResult("Error updateDefinition : " + e.getMessage());
         }
         return report;
     }
 
     private class WPSResolver implements EntityResolver {
         Hashtable<String, String> m_dtds = null;
 
         public WPSResolver() {
             m_dtds = new Hashtable<String, String>();
         }
 
         public WPSResolver(Hashtable<String, String> dtds) {
             m_dtds = dtds;
         }
 
         public InputSource resolveEntity(String publicId, String systemId) throws java.io.FileNotFoundException {
             InputSource iSource = null;
             String file = m_dtds.get(extractPath(systemId));
             if (file != null) {
                 iSource = new InputSource(new StringReader(file));
                 iSource.setSystemId(".");
             }
             else {
                 try {
                 //FileReader fr = new java.io.FileReader(getServletContext().getRealPath("dtd/" + extractPath(systemId)));
                 //iSource = new InputSource(fr);// new java.io.FileReader( "/" +
                                               // extractPath( systemId)));
                     iSource = new InputSource(getServletContext().getResourceAsStream("/dtd/" + extractPath(systemId)));
                     iSource.setSystemId(".");
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             return iSource;
         }
     }
 
     static public String extractPath(String filePath) {
         int pos = filePath.lastIndexOf('\\');
         String file = (pos > -1) ? filePath.substring(pos + 1) : filePath;
         pos = file.lastIndexOf('/');
         return (pos > -1) ? file.substring(pos + 1) : file;
     }
 
     static public String extractFile(JarInputStream is) throws IOException {
         StringBuffer sb = new StringBuffer();
         byte[] b = new byte[512];
         while (is.available() > 0) {
             int n = is.read(b);
             if (n > 0)
                 sb.append(new String(b, 0, n));
         }
         return sb.toString();
     }
 
     private void uploadZipFile(InternalReport output, byte[] definitionFile, String action, String dictionaryName)
             throws Exception {
         Hashtable<String, String> dtds = new Hashtable<String, String>();
         Hashtable<String, String> files = new Hashtable<String, String>();
         String name = null;
         output.addAction(0, "Reading definition file");
         JarInputStream input = new JarInputStream(new ByteArrayInputStream(definitionFile));
         // La parserXML ferme le stream : donc lecture et découpage du fichier
         // maintenant
         ZipEntry entry = input.getNextEntry();
         if (entry == null) { // Not a jar file
             output.setLastActionResult("bad file format or corrupted file!");
             return;
         }
         while (input.available() > 0) {
             name = entry.getName();
             if (name.endsWith(".dtd"))
                 dtds.put(extractPath(name), extractFile(input));
             else
                 files.put(extractPath(name), extractFile(input));
             input.closeEntry();
             entry = input.getNextEntry();
         }
         SAXBuilder saxBuilder = new SAXBuilder(true);
         saxBuilder.setEntityResolver(new WPSResolver(dtds));
         output.setLastActionResult("done.");
         output.skipLine();
 
         for (String name1 : files.keySet()) {
             if (name1.endsWith(".xml")) {
                 uploadFile(saxBuilder, output, name1, files.get(name1), action, dictionaryName);
             }
         }
     }
 
     private void uploadFile(SAXBuilder saxBuilder, InternalReport output, String name, String definition,
                             String action, String dictionaryName) throws Exception {
         output.addAction(0, "Reading definition file '" + name + "'");
         Document doc = saxBuilder.build(new StringReader(definition), ".");
         Element root = doc.getRootElement();
 
         CharArrayWriter bout = new CharArrayWriter();
         XMLOutputter op = new XMLOutputter();
 
         if (action.equalsIgnoreCase("uploadDictionaryFile")) {
             // Upload dictionary
             if (root.getName().equalsIgnoreCase("dictionaries")) {
                 List lst = root.getChildren("dictionary");
                 for (int i = 0; i < lst.size(); ++i) {
                     org.jdom.Element subelem = (org.jdom.Element) lst.get(i);
                     op.output(subelem, bout);
                     uploadDictionary(subelem.getAttributeValue("name"), bout.toString(), output);
                     bout.reset();
                 }
             }
             else if (root.getName().equalsIgnoreCase("dictionary")) {
                 op.output(root, bout);
                 uploadDictionary(root.getAttributeValue("name"), bout.toString(), output);
             }
         }
         else if (action.equalsIgnoreCase("uploadSwatchFile")) {
             // Upload swatch
             if (root.getName().equalsIgnoreCase("swatch")) {
                 op.output(root, bout);
                 uploadSwatch(root.getAttributeValue("name"), bout.toString(), output, dictionaryName);
             }
         }
     }
 
     private void uploadDictionary(String name, String definition, InternalReport output) {
         output.addAction(1, "Dictionary '" + name + "'");
         try {
             DictionaryManager manager = new DictionaryManagerImpl();
             Dictionary dictionary = manager.findByName(name);
             if (dictionary == null) {
                 dictionary = manager.create(name, definition);
                 output.setLastActionResult("created.");
             }
             else {
                 dictionary.setDefinition(definition);
                 manager.update(dictionary);
                 output.setLastActionResult("updated.");
             }
         }
         catch (Exception e) {
             output.setLastActionResult(e.getMessage());
             e.printStackTrace();
         }
     }
 
     private void uploadSwatch(String name, String definition, InternalReport output, String dictionaryName) {
         output.addAction(1, "Swatch '" + name + "'");
         try {
             SwatchManager swatchManager = new SwatchManagerImpl();
             DictionaryManager dictionaryManager = new DictionaryManagerImpl();
             Dictionary dictionary = dictionaryManager.findByName(dictionaryName);
             Swatch swatch = swatchManager.findByName(name, dictionaryName);
 
             if (swatch == null) {
                 swatch = swatchManager.create(name, definition, dictionaryName);
                 output.setLastActionResult("created.");
             }
             else {
                 swatch.setDefinition(definition);
                 swatch.setSwatchPk(new SwatchPk(name, dictionaryName));
                 swatchManager.update(swatch);
                 output.setLastActionResult("updated.");
             }
         }
         catch (Exception e) {
             output.setLastActionResult(e.getMessage());
             e.printStackTrace();
         }
     }
 }

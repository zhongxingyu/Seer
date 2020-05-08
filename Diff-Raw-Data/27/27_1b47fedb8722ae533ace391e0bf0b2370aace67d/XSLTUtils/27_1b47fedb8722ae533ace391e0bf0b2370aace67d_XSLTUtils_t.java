 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmlExercises.xslt;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import nu.xom.*;
 import nu.xom.xslt.XSLException;
 import nu.xom.xslt.XSLTransform;
 import xmlExercises.Constants;
 import xmlExercises.SyntaxErorException;
 import xmlExercises.Utils;
 
 /**
  *
 * @author Matous Jobanek
  */
 public class XSLTUtils {
 
     private static final Logger LOGGER = Logger.getLogger(XSLTUtils.class.getPackage().toString());
 
     /**
      * @param newXSL
      * @param name
      */
     public static XSLTResult evaluate(String newXSL, String name) throws SyntaxErorException, IOException, XSLException {
         Assignment assignment = scanDirectory(Utils.getPathTo("xslt", name));
 
         try {
             Document transformed = transform(assignment.getXmlDocument(), newXSL, false);
 
             boolean equal =
                     testForEquality(assignment.getHtmlOutputAsString(),
                     Utils.formatOutputHtml(transformed.toXML()));
 
             String userHtml = Utils.formatOutputHtml(transformed.toXML()).replace("\\n", "\n");
             String correctHtml = assignment.getHtmlOutputAsString().replace("\\n", "\n");
 
             return new XSLTResult(equal, transformed.toXML(), assignment.getHtmlOutput(), Utils.replaceTagsAndIndent(userHtml, true), Utils.replaceTagsAndIndent(correctHtml, true));
 
         } catch (XSLException e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             e.printStackTrace();
 
         } catch (ParsingException e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             e.printStackTrace();
 
         } catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             e.printStackTrace();
 
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * @param xml
      * @param xml2
      */
     public static boolean testForEquality(String originalXml, String userXml) {
 
         String[] originalSplited = originalXml.split(Constants.MY_LINE_SEPARATOR);
         String[] userSplited = userXml.split(Constants.MY_LINE_SEPARATOR);
 
         if (originalSplited.length != userSplited.length) {
             return false;
         }
         for (int i = 0; i < userSplited.length; i++) {
             String regex = "[" + Constants.MY_TAB + "," + Constants.MY_LINE_SEPARATOR + "]";
             String originalLine = originalSplited[i].replaceAll(regex, "");
             String userLine = userSplited[i].replaceAll(regex, "");
             if (!originalLine.equals(userLine)) {
 //                System.err.println("original: " + originalSplited[i]);
 //                System.err.println("user: " + userSplited[i]);
                 return false;
             }
         }
         return true;
     }
 
     public static String getMyTestXslt(String path) {
 
         Builder builder = new Builder();
         try {
             FileFilter xslFilter = new FileFilter() {
 
                 @Override
                 public boolean accept(File pathname) {
                     return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xsl");
                 }
             };
             File[] listFiles = new File(path).listFiles(xslFilter);
 
             return builder.build(listFiles[0]).toXML();
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * @param string
      */
     public static Assignment getAssignment(String name) throws SyntaxErorException, XSLException, IOException {
         List<Assignment> assignments = null;
         Assignment assignment = null;
 
         if (name != null && !"".equals(name)) {
             assignment = scanDirectory(Utils.getPathTo("xslt") + File.separator + name);
 
         } else {
             assignments = scanDirectoryStructure(Utils.getPathTo("xslt"));
             if (assignments.size() > 0) {
                 Random randomGenerator = new Random();
                 assignment = assignments.get(randomGenerator.nextInt(assignments.size()));
             }
         }
 
 
         if (assignment == null) {
             assignment = new Assignment();
             assignment.setAssignmentText("No excercise found.");
         }
         String pathToInitSol = System.getProperty("user.home")
                 + File.separator + Constants.ASSIGNMENTS_FOLDER_NAME + File.separator
                 + "xslt" + File.separator + "initSolution";
         if ((new File(pathToInitSol)).exists()) {
             assignment.setInitSolution(Utils.readFile(pathToInitSol).replace("\"", "\\\"").replace("\n", "\\n"));
         } else {
             assignment.setInitSolution("");
         }
 
         return assignment;
 
 
     }
 
     public static List<Assignment> scanDirectoryStructure(String path) throws SyntaxErorException, IOException, XSLException {
 
         File dir = new File(path);
         FileFilter filter = new FileFilter() {
 
             @Override
             public boolean accept(File pathname) {
                 return !pathname.isFile();
             }
         };
         File[] dirs = dir.listFiles(filter);
         if (dirs == null || dirs.length == 0) {
             LOGGER.log(Level.SEVERE, "There is no assignments on this path: " + path);
             return null;
         }
 
         ArrayList<Assignment> assignments = new ArrayList<Assignment>(dirs.length);
         for (int i = 0; i < dirs.length; i++) {
             Assignment assignment = scanDirectory(dirs[i].getPath());
             if (assignment != null) {
                 assignments.add(assignment);
             }
         }
 
         return assignments;
     }
 
     public static Assignment scanDirectory(String dirPath) throws SyntaxErorException, IOException, XSLException {
 //System.out.println(dirPath);
         File dir = new File(dirPath);
         FileFilter filterFiles = new FileFilter() {
 
             @Override
             public boolean accept(File pathname) {
                 return pathname.isFile();
             }
         };
         File[] files = dir.listFiles(filterFiles);
 
         if (files == null || files.length == 0) {
             LOGGER.log(Level.INFO, "There is no assignments on this path: " + dirPath);
             return null;
         }
 
         String xmlDoc = null;
         String assignmentText = null;
         String xsl = null;
         for (int i = 0; i < files.length; i++) {
             if (files[i].getPath().toLowerCase().endsWith(Constants.XML_FILE_SUFFIX)) {
                 xmlDoc = files[i].getPath();
             } else if (files[i].getPath().toLowerCase().endsWith(Constants.ASSIGNMENT_FILE_SUFFIX)) {
                 assignmentText = files[i].getPath();
             } else if (files[i].getPath().toLowerCase().endsWith(Constants.XSL_FILE_SUFFIX)) {
                 xsl = files[i].getPath();
             }
         }
 
         if (xmlDoc == null || assignmentText == null || xsl == null) {
             return null;
         }
 
 
         return createAssignment(dirPath, xmlDoc, assignmentText, xsl);
 
     }
 
     /**
      * @param dirPath
      * @param xmlDocPath
      * @param assignmentTextPath
      * @param xslPath
      * @return
      */
     public static Assignment createAssignment(String dirPath,
             String xmlDocPath,
             String assignmentTextPath,
             String xslPath) throws SyntaxErorException, IOException, XSLException {
         try {
             String name = dirPath.substring(dirPath.lastIndexOf(File.separator) + 1);
 
             Builder builder = new Builder();
             Document xmlDocument = builder.build(new File(xmlDocPath));
 
             BufferedReader reader = new BufferedReader(new FileReader(assignmentTextPath));
 
             StringBuffer assignmentBuffer = new StringBuffer("");
             while (reader.ready()) {
                 assignmentBuffer.append(reader.readLine()).append("\n");
             }
 
             Document htmlOutput = transform(xmlDocument, xslPath, true);
 
             if (name != null && xmlDocument != null && !"".equals(xmlDocument.toXML())
                     && assignmentBuffer.toString() != null && !"".equals(assignmentBuffer.toString())
                     && htmlOutput != null && !"".equals(htmlOutput.toXML())) {
                 String htmlOuputAsString = Utils.formatOutputHtml(htmlOutput.toXML().trim());
                 return new Assignment(name,
                         dirPath.substring(dirPath.lastIndexOf(File.separator) + 1),
                         xmlDocument,
                         xmlDocument.toXML().replace("\n", "\\n"),
                         assignmentBuffer.toString().replace("\n", "\\n"),
                         getBodyContent(htmlOutput.toXML()).replace("\\n", ""),
                         htmlOuputAsString);
             }
 
         } catch (ParsingException e) {
             LOGGER.log(Level.SEVERE, "Problem", e);
             throw new SyntaxErorException(e.getMessage());
 
         }
         return null;
     }
 
     public static String getBodyContent(String html) {
         int indexOfBody = html.toLowerCase().indexOf("<body>");
         return html.substring(html.indexOf(">", indexOfBody) + 1, html.toLowerCase().indexOf("</body>")).replace("\n", "\\n").replace("\"", "\\\"");
     }
 
     public static Document transform(Document xmlDocument, String XSL, boolean isFile) throws XSLException,
             ParsingException, IOException, SyntaxErorException {
 
         Builder builder = new Builder();
         Document stylesheet = isFile ? builder.build(XSL) : builder.build(new ByteArrayInputStream(XSL.getBytes()));
         XSLTransform transform = new XSLTransform(stylesheet);
 
         return XSLTransform.toDocument(transform.transform(xmlDocument));
 
     }
 }

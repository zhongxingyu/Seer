 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2013, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.japit.reporting;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import org.jboss.japit.Application;
 import org.jboss.japit.core.Archive;
 import org.jboss.japit.core.ClassDetails;
 import org.jboss.japit.core.JarArchive;
 
 /**
  *
  * @author Rostislav Svoboda
  */
 public class HtmlFactory {
 
     private static final String NEW_LINE = System.getProperty("line.separator");
     private static boolean failCalled;
     private static boolean failCalledForArchive;
     private static Map<String, Boolean> failDiffMap = new HashMap<String, Boolean>();
     
     private HtmlFactory() {
     }
 
     public static void generateIndex(Collection<Archive> archives, File outputDir,
             boolean includeDiff, boolean suppressArchiveReport) {
         try {
             File targetFile = new File(outputDir, "index.html");
             OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8");
             BufferedWriter bw = new BufferedWriter(osWriter, 8192);
 
             generateHeader(bw, "Index");
 
             bw.write("<h1>" + Application.FULL_VERSION + "</h1>" + NEW_LINE);
             if (!suppressArchiveReport) {
                 bw.write("<h2>Archives: </h2>" + NEW_LINE);
                 bw.write("<ul>" + NEW_LINE);
                 TreeSet<Archive> sortedArchives = new TreeSet<Archive>(archives);
                 for (Archive archive : sortedArchives) {
                     String targetFileName = convertPathForFileName(archive.getFilePath()) + ".html";
                     bw.write("<li><a href=\"" + targetFileName + "\">"
                             + archive.getFileName() + "</a> - " + archive.getFilePath());
                     bw.write(" (" + getArchiveReportSize(outputDir, targetFileName) + ")</li>" + NEW_LINE);
                 }
                 bw.write("</ul>" + NEW_LINE);
             }
 
             if (includeDiff) {
                 bw.write("<h2>Diffs: </h2>" + NEW_LINE);
                 bw.write("<ul>" + NEW_LINE);
                 Iterator<Archive> iter = archives.iterator();
                 int counter = 0;
                 while (iter.hasNext()) {
                     JarArchive first = (JarArchive) iter.next();
                     JarArchive second = (JarArchive) iter.next();
                     counter++;
                     String targetFileName = convertPathForFileName(first.getFilePath()) + "-diff-" + counter + ".html";
                     Boolean diffFailed = failDiffMap.get(targetFileName);
                     String statusHtmlString = "<span class=\"ok\">OK</span>";
                     if (diffFailed == null || diffFailed.equals(Boolean.TRUE)) {
                          statusHtmlString = "<span class=\"fail\">FAIL</span>";
                     }
                     bw.write("<li>" + statusHtmlString + " - "
                             + "<a href=\"" + targetFileName + "\">"
                             + first.getFileName() + "</a> - " + first.getFilePath() + " vs. " + second.getFilePath());
                     bw.write(" (" + getArchiveReportSize(outputDir, targetFileName) + ")</li>" + NEW_LINE);
                 }
                 bw.write("</ul>" + NEW_LINE);
             }
 
             generateFooter(bw);
 
             bw.flush();
             bw.close();
         } catch (Exception e) {
             System.err.println("GenerateIndex: " + e.getMessage());
             e.printStackTrace(System.err);
         }
     }
 
     public static void generateDiffReportFile(Collection<Archive> archives, File outputDir, boolean ignoreClassVersion, 
             boolean suppressArchiveReport, boolean enableDeclaredItems) {
         Iterator<Archive> iter = archives.iterator();
         int counter = 0;
         while (iter.hasNext()) {
             JarArchive first = (JarArchive) iter.next();
             JarArchive second = (JarArchive) iter.next();
             counter++;
             String targetFileName = convertPathForFileName(first.getFilePath()) + "-diff-" + counter + ".html";
 
             try {                
                 File targetFile = new File(outputDir, targetFileName);
                 OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osWriter, 8192);
 
                 generateHeader(bw, first.getFileName());
 
                 generateDiffReportBody(bw, first, second, ignoreClassVersion, suppressArchiveReport, enableDeclaredItems, targetFileName);
 
                 bw.write("</table>" + NEW_LINE);
                 bw.write("<br/>" + NEW_LINE);
 
                 generateFooter(bw);
 
                 bw.flush();
                 bw.close();
             } catch (Exception e) {
                 System.err.println("generateArchiveReport: " + e.getMessage());
                 e.printStackTrace(System.err);
             }
         }
     }
 
     private static void generateDiffReportBody(BufferedWriter bw, JarArchive first, JarArchive second,
             boolean ignoreClassVersion, boolean suppressArchiveReport, boolean enableDeclaredItems, String targetFileName) throws IOException {
         bw.write("<h1>" + first.getFileName() + " vs. " + second.getFileName() + "</h1>" + NEW_LINE);
         bw.write("<h2>" + first.getFilePath() + " vs. " + second.getFilePath() + "</h2>" + NEW_LINE);
         bw.write("<a href=\"index.html\">Main</a>" + NEW_LINE);
         bw.write("<br/><br/>" + NEW_LINE);
 
         bw.write("First jar: " + first.getFilePath() + "<br/>" + NEW_LINE);
         bw.write("Second jar: " + second.getFilePath() + "<br/>" + NEW_LINE);
         bw.write("<br/>" + NEW_LINE);
         bw.write("Compared classes count: " + first.getClasses().size() + " vs. " + second.getClasses().size() + "<br/>" + NEW_LINE);
         if (ignoreClassVersion) {
             bw.write("Class Version ignored in comparison<br/>" + NEW_LINE);
         }
         bw.write("<br/>" + NEW_LINE);
 
         TreeMap<String, ClassDetails> firstJarClassesMap = new TreeMap<String, ClassDetails>();
         for (ClassDetails firstJarClass : first.getClasses()) {
             firstJarClassesMap.put(firstJarClass.getClassName(), firstJarClass);
         }
         failCalledForArchive = false;
         for (ClassDetails secondJarClass : second.getClasses()) {
             bw.write("<a name=\"" + secondJarClass.getClassName() + "\"></a> " + NEW_LINE);
             if (suppressArchiveReport) {
                 bw.write("<div class=\"class-name\">" + secondJarClass.getClassName() + "</div>" + NEW_LINE);
             } else {
                 String firstArchiveReportFileName = convertPathForFileName(first.getFilePath()) + ".html";
                 String secondArchiveReportFileName = convertPathForFileName(second.getFilePath()) + ".html";
                 bw.write("<div class=\"class-name\">" + secondJarClass.getClassName()
                         + "&nbsp;&nbsp;"
                         + "<a href=\"" + firstArchiveReportFileName + "#" + secondJarClass.getClassName() + "\">First jar</a>"
                         + "&nbsp;&nbsp;"
                         + "<a href=\"" + secondArchiveReportFileName + "#" + secondJarClass.getClassName() + "\">Second jar</a>"
                         + "</div>" + NEW_LINE);
             }
 
             failCalled = false;
 
             ClassDetails firstJarClass;
             try {
                 firstJarClass = firstJarClassesMap.remove(secondJarClass.getClassName());
                 if (firstJarClass == null) {
                     fail(bw, "class doesn't exist in first jar");
                     continue;
                 }
             } catch (Exception e) {
                 fail(bw, "class doesn't exist in first jar");
                 continue;
             }
 
             if (!ignoreClassVersion && firstJarClass.getClassVersion() != secondJarClass.getClassVersion()) {
                 fail(bw, "class version doesn't match: " + firstJarClass.getClassVersion() + " vs. " + secondJarClass.getClassVersion());
             }
 
             if (firstJarClass.getMethodsCount() != secondJarClass.getMethodsCount()) {
                 fail(bw, "methods count doesn't match: " + firstJarClass.getMethodsCount() + " vs. " + secondJarClass.getMethodsCount());
             }
 
             if (enableDeclaredItems && firstJarClass.getDeclaredMethodsCount() != secondJarClass.getDeclaredMethodsCount()) {
                 fail(bw, "declared methods count doesn't match: " + firstJarClass.getDeclaredMethodsCount() + " vs. " + secondJarClass.getDeclaredMethodsCount());
             }
 
             if (firstJarClass.getFieldsCount() != secondJarClass.getFieldsCount()) {
                 fail(bw, "fields count doesn't match: " + firstJarClass.getFieldsCount() + " vs. " + secondJarClass.getFieldsCount());
             }
 
             if (enableDeclaredItems && firstJarClass.getDeclaredFieldsCount() != secondJarClass.getDeclaredFieldsCount()) {
                 fail(bw, "declared fields count doesn't match: " + firstJarClass.getDeclaredFieldsCount() + " vs. " + secondJarClass.getDeclaredFieldsCount());
             }
 
             if (!firstJarClass.getSuperclassName().equals(secondJarClass.getSuperclassName())) {
                 fail(bw, "super class name doesn't match: " + firstJarClass.getSuperclassName() + " vs. " + secondJarClass.getSuperclassName());
             }
             if (firstJarClass.getOriginalJavaFile() != null && secondJarClass.getOriginalJavaFile() != null &&
                     !firstJarClass.getOriginalJavaFile().equals(secondJarClass.getOriginalJavaFile())) {
                 fail(bw, "original java file doesn't match: " + firstJarClass.getOriginalJavaFile() + " vs. " + secondJarClass.getOriginalJavaFile());
             }
 
             for (String firstJarClassMethod : firstJarClass.getMethods()) {
                 if (!secondJarClass.getMethods().contains(firstJarClassMethod)) {
                     fail(bw, "second jar doesn't contain method " + firstJarClassMethod);
                 }
             }
             for (String secondJarClassMethod : secondJarClass.getMethods()) {
                 if (!firstJarClass.getMethods().contains(secondJarClassMethod)) {
                     fail(bw, "first jar doesn't contain method " + secondJarClassMethod);
                 }
             }
 
             for (String firstJarClassField : firstJarClass.getFields()) {
                 if (!secondJarClass.getFields().contains(firstJarClassField)) {
                     fail(bw, "second jar doesn't contain field " + firstJarClassField);
                 }
             }
             for (String secondJarClassField : secondJarClass.getFields()) {
                 if (!firstJarClass.getFields().contains(secondJarClassField)) {
                     fail(bw, "first jar doesn't contain field " + secondJarClassField);
                 }
             }
 
             if (!failCalled) {
                 ok(bw);
             }
         }
         if (firstJarClassesMap.size() > 0) {
             for (ClassDetails firstJarClass : firstJarClassesMap.values()) {
                 bw.write("<div class=\"class-name\">" + firstJarClass.getClassName() + "</div>" + NEW_LINE);
                 fail(bw, "class doesn't exist in second jar");
             }
         }
         failDiffMap.put(targetFileName, failCalledForArchive);
     }
 
     private static void fail(BufferedWriter bw, String details) throws IOException {
         bw.write("<div> <span class=\"fail\">FAIL</span> -- " + details
                 + " </div>" + NEW_LINE);
         failCalled = true;
         failCalledForArchive = true;
     }
 
     private static void ok(BufferedWriter bw) throws IOException {
         bw.write("<div> <span class=\"ok\">OK</span></div>" + NEW_LINE);
     }
 
     public static void generateArchiveReport(Collection<Archive> archives, File outputDir) {
         for (Archive archive : archives) {
             try {
                 File targetFile =new File(outputDir, convertPathForFileName(archive.getFilePath()) + ".html");
                 OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8");
                 BufferedWriter bw = new BufferedWriter(osWriter, 8192);
 
                 generateHeader(bw, archive.getFileName());
 
                 bw.write("<h1>" + archive.getFileName() + " - " + archive.getFilePath() + "</h1>" + NEW_LINE);
                 bw.write("<a href=\"index.html\">Main</a>" + NEW_LINE);
                 bw.write("<br/><br/>" + NEW_LINE);
 
                 generateArchiveReportBody(bw, (JarArchive) archive);   // type check done in HtmlFileReportGenerator
                 generateFooter(bw);
 
                 bw.flush();
                 bw.close();
             } catch (Exception e) {
                 System.err.println("generateArchiveReport: " + e.getMessage());
                 e.printStackTrace(System.err);
             }
         }
     }
 
     private static String convertPathForFileName(String path) {
         return path.replaceFirst("/", "").replace("/", "-").replace(":\\", "-").replace("\\", "-").replace(" ", "-");
     }
 
     private static String getArchiveReportSize(File outputDir, String fileName) {
         File file = new File(outputDir, fileName);
         return ((file.length() / 1024) + 1) + "KB";
     }
 
     private static void generateHeader(BufferedWriter bw, String title) throws IOException {
         bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
                 + "\"http://www.w3.org/TR/html4/loose.dtd\">" + NEW_LINE);
         bw.write("<html>" + NEW_LINE);
         bw.write("<head>" + NEW_LINE);
         bw.write("  <title>" + Application.FULL_VERSION + ": " + title + "</title>" + NEW_LINE);
         bw.write("  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">" + NEW_LINE);
         bw.write("  <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" + NEW_LINE);
         bw.write("</head>" + NEW_LINE);
         bw.write("<body>" + NEW_LINE);
     }
 
     private static void generateFooter(BufferedWriter bw) throws IOException {
         bw.write("<hr>" + NEW_LINE);
         bw.write("Generated by: <a href=\"" + Application.HOMEPAGE + "\">"
                 + Application.FULL_VERSION + "</a>" + NEW_LINE);
         bw.write("</body>" + NEW_LINE);
         bw.write("</html>" + NEW_LINE);
     }
 
     private static void generateArchiveReportBody(BufferedWriter bw, JarArchive jarArchive) throws IOException {
         bw.write("<h2>" + jarArchive.getClasses().size() + " classes</h2>" + NEW_LINE);
         String delim = "";
         for (ClassDetails classDetails : jarArchive.getClasses()) {
             bw.write(delim + " <a href=\"#" + classDetails.getClassName() + "\">"
                     + classDetails.getClassName() + "</a> ");
             delim = "||";
         }
 
         bw.write("<br/><br/>" + NEW_LINE);
 
         //TODO split into methods for each archive type
         for (ClassDetails classDetails : jarArchive.getClasses()) {
             bw.write("<a name=\"" + classDetails.getClassName() + "\"></a> " + NEW_LINE);
             bw.write("<table>" + NEW_LINE);
             bw.write("  <tr>" + NEW_LINE);
             bw.write("     <th>Class " + classDetails.getClassName() + "</th>" + NEW_LINE);
             bw.write("     <th>&nbsp;</th>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
 
             bw.write("  <tr class=\"rowodd\">" + NEW_LINE);
             bw.write("     <td class=\"row-heading\">Class version</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getClassVersion() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
             bw.write("  <tr class=\"rowodd\">" + NEW_LINE);
             bw.write("     <td class=\"row-heading\">Original Java file</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getOriginalJavaFile() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
             bw.write("  <tr class=\"rowodd\">" + NEW_LINE);
            bw.write("     <td class=\"row-heading\">Superclass</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getSuperclassName() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
             bw.write("  <tr class=\"rowodd\">" + NEW_LINE);
             bw.write("     <td class=\"row-heading\">Referenced classes count</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getReferencedClasses() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
 
             bw.write("  <tr class=\"roweven\">" + NEW_LINE);
             bw.write("     <td class=\"row-heading\">Methods / Declared Methods count</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getMethodsCount() + " / " + classDetails.getDeclaredMethodsCount() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
             bw.write("  <tr class=\"roweven\">" + NEW_LINE);
             bw.write("     <td class=\"row-heading\">Fields / Declared Fields count</td>" + NEW_LINE);
             bw.write("     <td>" + classDetails.getFieldsCount() + " / " + classDetails.getDeclaredFieldsCount() + "</td>" + NEW_LINE);
             bw.write("  </tr>" + NEW_LINE);
 
             if (classDetails.getMethodsCount() > 0) {
                 bw.write("  <tr class=\"rowodd\">" + NEW_LINE);
                 bw.write("     <td class=\"row-heading\">Methods</td>" + NEW_LINE);
                 bw.write("     <td>");
                 for (String method : classDetails.getMethods()) {
                     bw.write( method + "<br/>" + NEW_LINE);
                 }
                 bw.write("     </td>" + NEW_LINE);
                 bw.write("  </tr>" + NEW_LINE);
             }
             if (classDetails.getFieldsCount() > 0) {
                 bw.write("  <tr class=\"roweven\">" + NEW_LINE);
                 bw.write("     <td class=\"row-heading\">Fields</td>" + NEW_LINE);
                 bw.write("     <td>");
                 for (String field : classDetails.getFields()) {
                     bw.write( field + "<br/>" + NEW_LINE);
                 }
                 bw.write("     </td>" + NEW_LINE);
                 bw.write("  </tr>" + NEW_LINE);
             }
             bw.write("</table>" + NEW_LINE);
             bw.write("<br/>" + NEW_LINE);
         }
     }
 
     public static void generateCSS(File outputDir) {
         byte buffer[] = new byte[8192];
         int bytesRead;
 
         InputStream is = null;
         OutputStream os = null;
 
 
         try {
             is = HtmlFactory.class
                     .getClassLoader().getResourceAsStream("style.css");
             os = new FileOutputStream(new File(outputDir, "style.css"));
             while ((bytesRead = is.read(buffer)) != -1) {
                 os.write(buffer, 0, bytesRead);
             }
 
             os.flush();
         } catch (Exception e) {
             System.err.println("GenerateCSS: " + e.getMessage());
             e.printStackTrace(System.err);
         } finally {
             try {
                 if (is != null) {
                     is.close();
                 }
             } catch (IOException ioe) {
             } // Ignore
 
             try {
                 if (os != null) {
                     os.close();
                 }
             } catch (IOException ioe) {
             } // Ignore
         }
     }
 }

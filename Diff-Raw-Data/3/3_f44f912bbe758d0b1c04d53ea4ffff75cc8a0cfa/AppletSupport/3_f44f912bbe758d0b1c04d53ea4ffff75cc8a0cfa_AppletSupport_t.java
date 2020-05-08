 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.project.applet;
 
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.Tree;
 import com.sun.source.util.TreePath;
 import com.sun.source.util.Trees;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import org.netbeans.api.java.source.CancellableTask;
 import org.netbeans.api.java.source.CompilationController;
 import org.netbeans.api.java.source.JavaSource;
 import org.netbeans.api.javafx.platform.JavaFXPlatform;
 import org.netbeans.api.javafx.source.JavaFXSourceUtils;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectManager;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.modules.javafx.project.JavaFXProjectUtil;
 import org.openide.*;
 import org.openide.modules.SpecificationVersion;
 import org.openide.filesystems.*;
 import org.openide.util.*;
 import org.netbeans.api.java.classpath.*;
 import org.netbeans.api.java.platform.*;
 import org.netbeans.modules.javafx.project.ui.customizer.JavaFXProjectProperties;
 import org.netbeans.spi.project.support.ant.EditableProperties;
 
 /** Support for execution of applets.
  *
  * @author Ales Novak, Martin Grebac
  */
 public class AppletSupport {
 
     // JDK issue #6193279: Appletviewer does not accept encoded URLs
     private static final SpecificationVersion JDK_15 = new SpecificationVersion("1.5"); // NOI18N
 
     /** constant for html extension */
     private static final String HTML_EXT = "html"; // NOI18N
 
     /** constant for class extension */
     private static final String CLASS_EXT = "class"; // NOI18N
 
     /** constank for jnlp extension */
     private static final String JNLP_EXT = "jnlp"; // NOI18N
 
     private final static String POLICY_FILE_NAME = "applet";
     private final static String POLICY_FILE_EXT = "policy";
     private final static String APPLET_MAIN_CLASS = "javafx.application.Applet";
 
     private AppletSupport() {
     }
 
     // Used only from unit tests to suppress detection of applet. If value
     // is different from null it will be returned instead.
     public static Boolean unitTestingSupport_isApplet = null;
 
     public static boolean isApplet(final FileObject file) {
         if (file == null) {
             return false;
         }
         // support for unit testing
         if (unitTestingSupport_isApplet != null) {
             return unitTestingSupport_isApplet.booleanValue();
         }
 
         JavaSource js = JavaSource.forFileObject(file);
         if (js == null) {
             return false;
         }
         final boolean[] result = new boolean[]{false};
         try {
             js.runUserActionTask(new CancellableTask<CompilationController>() {
 
                 public void run(CompilationController control) throws Exception {
                     if (JavaSource.Phase.ELEMENTS_RESOLVED.compareTo(control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED)) <= 0) {
                         Elements elements = control.getElements();
                         Trees trees = control.getTrees();
                         Types types = control.getTypes();
                         TypeElement applet = elements.getTypeElement("java.applet.Applet");     //NOI18N
 
                         TypeElement japplet = elements.getTypeElement("javax.swing.JApplet");   //NOI18N
 
                         CompilationUnitTree cu = control.getCompilationUnit();
                         List<? extends Tree> topLevels = cu.getTypeDecls();
                         for (Tree topLevel : topLevels) {
                             if (topLevel.getKind() == Tree.Kind.CLASS) {
                                 TypeElement type = (TypeElement) trees.getElement(TreePath.getPath(cu, topLevel));
                                 if (type != null) {
                                     Set<Modifier> modifiers = type.getModifiers();
                                     if (modifiers.contains(Modifier.PUBLIC) &&
                                             ((applet != null && types.isSubtype(type.asType(), applet.asType())) || (japplet != null && types.isSubtype(type.asType(), japplet.asType())))) {
                                         result[0] = true;
                                         break;
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 public void cancel() {
                 }
             }, true);
         } catch (IOException ioe) {
             Exceptions.printStackTrace(ioe);
         }
         return result[0];
     }
 
     public static boolean isJavaFXApplet(final FileObject file) {
         // this code was moved to the JavaFX Source module because in this module the wrong classes (from javac lib) are loaded
         return JavaFXSourceUtils.isJavaFXApplet(file);
     }
 
     /**
      * @return html file with the same name as applet
      */
     private static FileObject generateHtml(FileObject appletFile, FileObject buildDir, FileObject classesDir, FileObject distDir, String activePlatform, EditableProperties ep) throws IOException {
         FileObject htmlFile = distDir.getFileObject(appletFile.getName(), HTML_EXT);
 
         if (htmlFile == null) {
 //            htmlFile = buildDir.createData(appletFile.getName(), HTML_EXT);
             htmlFile = distDir.createData(appletFile.getName(), HTML_EXT);
         }
 
         FileLock lock = htmlFile.lock();
         PrintWriter writer = null;
         try {
             writer = new PrintWriter(htmlFile.getOutputStream(lock));
 //            ClassPath cp = ClassPath.getClassPath(appletFile, ClassPath.EXECUTE);
             ClassPath sp = ClassPath.getClassPath(appletFile, ClassPath.SOURCE);
             String path = FileUtil.getRelativePath(sp.findOwnerRoot(appletFile), appletFile);
             String codebase = FileUtil.getRelativePath(buildDir, classesDir);
 
             if (codebase == null) {
                 codebase = classesDir.getURL().toString();
             }
             String appletJavaScript = ep.getProperty(JavaFXProjectProperties.APPLET_JAVASCRIPT);
             boolean isJavaScript = "true".equals(appletJavaScript);
             boolean isInBrowser = "true".equals(ep.getProperty(JavaFXProjectProperties.APPLET_RUN_IN_BROWSER));
             String jnlpFileName = "true".equals(ep.getProperty(JavaFXProjectProperties.APPLET_JNLP)) ? htmlFile.getName() + "." + JNLP_EXT : null;
             String draggable = ep.getProperty(JavaFXProjectProperties.APPLET_DRAGGABLE);
             String java_args = ep.getProperty(JavaFXProjectProperties.APPLET_ARGUMENTS);
             if (appletFile.getExt().equals("fx")) {
                 JavaFXProject project = (JavaFXProject) getProject(appletFile);
		if (project == null) {
		    project = (JavaFXProject)getProject(buildDir);
		}
 
                 String distJAR = project.evaluator().getProperty("dist.jar");
                 distJAR = distJAR.substring(distJAR.indexOf('/') + 1);
                 String libs = distJAR;
                 try {
                     File fxFolder = new File(((JavaFXPlatform) JavaFXProjectUtil.getActivePlatform(activePlatform)).getJavaFXFolder().toURI());
                     String[] list = fxFolder.list(new FilenameFilter() {
 
                         public boolean accept(File dir, String name) {
                             if (name.endsWith(".jar")) {
                                 return true;
                             } else {
                                 return false;
                             }
                         }
                     });
                     for (int i = 0; i < list.length; i++) {
                         libs += ",lib/" + list[i];
                     }
                 } catch (URISyntaxException e) {
                 }
 
 
 //                String libs = distJAR + ",lib/javafxrt.jar,lib/Scenario.jar,lib/Reprise.jar";// REWRITE runtime jars
                 path = path.substring(0, path.length() - 3);
                 if (isJavaScript && isInBrowser) {
                     fillInFileJavaScript(writer, path.replaceAll("/", "."), " archive=\"" + libs + "\"", true, draggable, java_args, jnlpFileName); // NOI18N
 
                 } else {
                     fillInFile(writer, path.replaceAll("/", "."), " archive=\"" + libs + "\"", true, draggable, java_args, jnlpFileName); // NOI18N
 
                 }
             } else {
                 path = path.substring(0, path.length() - 5);
                 if (isJavaScript && isInBrowser) {
                     fillInFileJavaScript(writer, path + "." + CLASS_EXT, "codebase=\"" + codebase + "\"", false, draggable, java_args, jnlpFileName); // NOI18N
 
                 } else {
                     fillInFile(writer, path + "." + CLASS_EXT, "codebase=\"" + codebase + "\"", false, draggable, java_args, jnlpFileName); // NOI18N
 
                 }
             }
         } finally {
             lock.releaseLock();
             if (writer != null) {
                 writer.close();
             }
         }
         return htmlFile;
     }
 
     /**
      * @return JNLP file with the same name as applet
      */
     public static FileObject generateJNLP(FileObject appletFile, FileObject buildDir, FileObject classesDir, FileObject distDir, String activePlatform, EditableProperties ep) throws IOException {
         FileObject jnlpFile = distDir.getFileObject(appletFile.getName(), JNLP_EXT);
 
         if (jnlpFile == null) {
             jnlpFile = distDir.createData(appletFile.getName(), JNLP_EXT);
         }
 
         FileLock lock = jnlpFile.lock();
         PrintWriter writer = null;
         try {
             writer = new PrintWriter(jnlpFile.getOutputStream(lock));
             ClassPath sp = ClassPath.getClassPath(appletFile, ClassPath.SOURCE);
             String path = FileUtil.getRelativePath(sp.findOwnerRoot(appletFile), appletFile);
             String codebase = FileUtil.getRelativePath(buildDir, classesDir);
 
             if (codebase == null) {
                 codebase = classesDir.getURL().toString();
             }
             String appletJavaScript = ep.getProperty(JavaFXProjectProperties.APPLET_JAVASCRIPT);
             if (appletFile.getExt().equals("fx")) {
                 JavaFXProject project = (JavaFXProject) getProject(appletFile);
 
                 String distJAR = project.evaluator().getProperty("dist.jar");
                 distJAR = distJAR.substring(distJAR.indexOf('/') + 1);
                 String libs = distJAR;
                 String[] list = {""};
                 try {
                     File fxFolder = new File(((JavaFXPlatform) JavaFXProjectUtil.getActivePlatform(activePlatform)).getJavaFXFolder().toURI());
                     list = fxFolder.list(new FilenameFilter() {
 
                         public boolean accept(File dir, String name) {
                             if (name.endsWith(".jar")) {
                                 return true;
                             } else {
                                 return false;
                             }
                         }
                     });
                 } catch (URISyntaxException e) {
                 }
 
                 path = path.substring(0, path.length() - 3);
                 fillInJNLPFile(writer, path.replaceAll("/", "."), codebase, true, list, distJAR);
             } else {
                 path = path.substring(0, path.length() - 5);
                 fillInJNLPFile(writer, path.replaceAll("/", "."), codebase, false, null, null);
             }
         } finally {
             lock.releaseLock();
             if (writer != null) {
                 writer.close();
             }
         }
         return jnlpFile;
     }
 
     /**
      * @return html file with the same name as applet
      */
     public static FileObject generateSecurityPolicy(FileObject projectDir) {
 
         FileObject policyFile = projectDir.getFileObject(POLICY_FILE_NAME, POLICY_FILE_EXT);
 
         try {
             if (policyFile == null) {
                 policyFile = projectDir.createData(POLICY_FILE_NAME, POLICY_FILE_EXT);
             }
             FileLock lock = policyFile.lock();
             PrintWriter writer = null;
             try {
                 writer = new PrintWriter(policyFile.getOutputStream(lock));
                 fillInPolicyFile(writer);
             } finally {
                 lock.releaseLock();
                 if (writer != null) {
                     writer.close();
                 }
             }
         } catch (IOException ioe) {
             ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Problem when generating applet policy file: " + ioe); //NOI18N
 
         }
         return policyFile;
     }
 
     /**
      * @return URL of the html file with the same name as sibling
      */
     public static URL generateHtmlFileURL(FileObject appletFile, FileObject buildDir, FileObject classesDir, FileObject distDir, String activePlatform, EditableProperties ep) throws FileStateInvalidException {
         FileObject html = null;
         FileObject jnlp = null;
         IOException ex = null;
         if ((appletFile == null) || (buildDir == null) || (classesDir == null)) {
             return null;
         }
         try {
             if ("true".equals(ep.getProperty(JavaFXProjectProperties.APPLET_JNLP))) {
                 jnlp = generateJNLP(appletFile, buildDir, classesDir, distDir, activePlatform, ep);
             }
             html = generateHtml(appletFile, buildDir, classesDir, distDir, activePlatform, ep);
             if (html != null) {
                 return getHTMLPageURL(html, activePlatform);
             } else {
                 return null;
             }
         } catch (IOException iex) {
             return null;
         }
     }
 
     /**
      * Creates an URL of html page passed to the appletviewer. It workarounds a JDK 1.5 appletviewer
      * bug. The appletviewer is not able to handle escaped URLs. 
      * @param htmlFile html page
      * @param activePlatform identifier of the platform used in the project
      * @return URL of the html page or null
      */
     public static URL getHTMLPageURL(FileObject htmlFile, String activePlatform) {
         assert htmlFile != null : "htmlFile cannot be null";    //NOI18N
         // JDK issue #6193279: Appletviewer does not accept encoded URLs
 
         JavaPlatform platform = JavaFXProjectUtil.getActivePlatform(activePlatform);
         boolean workAround6193279 = platform != null //In case of nonexisting platform don't use the workaround
                 && platform.getSpecification().getVersion().compareTo(JDK_15) >= 0; //JDK1.5 and higher
 
         URL url = null;
         if (workAround6193279) {
             File f = FileUtil.toFile(htmlFile);
             try {
                 String path = f.getAbsolutePath();
                 if (File.separatorChar != '/') {    //NOI18N
 
                     path = path.replace(File.separatorChar, '/');   //NOI18N
 
                 }
                 //Workaround to get it work in windows
                 path = path.replaceAll(" ", "%20");
                 url = new URL("file", null, path);
             } catch (MalformedURLException e) {
                 ErrorManager.getDefault().notify(e);
             }
         } else {
             try {
                 url = htmlFile.getURL();
             } catch (FileStateInvalidException f) {
                 ErrorManager.getDefault().notify(f);
             }
         }
         return url;
     }
 
     /** fills in file with html source so it is html file with applet
      * @param file is a file to be filled
      * @param name is name of the applet                                     
      */
     private static void fillInFile(PrintWriter writer, String name, String codebase, boolean isFX, String draggable, String java_args, String jnlpFileName) {
         ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);
 
         writer.println("<HTML>"); // NOI18N
 
         writer.println("<HEAD>"); // NOI18N
 
         writer.print("   <TITLE>"); // NOI18N
 
         writer.print(bundle.getString("GEN_title"));
         writer.println("</TITLE>"); // NOI18N
 
         writer.println("</HEAD>"); // NOI18N
 
         writer.println("<BODY>\n"); // NOI18N
 
         writer.print(bundle.getString("GEN_warning"));
 
         writer.print("<H3><HR WIDTH=\"100%\">"); // NOI18N
 
         writer.print(bundle.getString("GEN_header"));
         writer.println("<HR WIDTH=\"100%\"></H3>\n"); // NOI18N
 
         writer.println("<P>"); // NOI18N
 
         if (jnlpFileName != null) {
             writer.println("<APPLET width=350 height=200>");
             writer.println("    <param name=\"jnlp_href\" value=\"" + jnlpFileName + "\">");
         } else {
             if (codebase == null) {
                 writer.print("<APPLET code="); // NOI18N
 
             } else {
                 writer.print("<APPLET " + codebase + " code="); // NOI18N
 
             }
             if (isFX) {
                 writer.print("\"" + APPLET_MAIN_CLASS + "\""); // NOI18N
 
                 writer.println(" width=350 height=200>"); // NOI18N
 
                 writer.println("    <param name=\"ApplicationClass\" value=\"" + name + "\">"); // NOI18N
 
             } else {
                 writer.print("\"" + name + "\""); // NOI18N
 
                 writer.println(" width=350 height=200>"); // NOI18N
 
             }
         }
         if (draggable != null) {
             if ("true".equals(draggable)) {
                 writer.println("    <param name=\"draggable\" value=\"true\">");
             }
         }
         if (java_args != null) {
             writer.println("    <param name=\"java_arguments\" value=\"" + java_args + "\">");
         }
         writer.println("</APPLET>"); // NOI18N
 
         writer.println("</P>\n"); // NOI18N
 
         writer.print("<HR WIDTH=\"100%\"><FONT SIZE=-1><I>"); // NOI18N
 
         writer.print(bundle.getString("GEN_copy"));
         writer.println("</I></FONT>"); // NOI18N
 
         writer.println("</BODY>"); // NOI18N
 
         writer.println("</HTML>"); // NOI18N
 
         writer.flush();
     }
 
     /** fills in file with html source so it is html file with applet
      * @param file is a file to be filled
      * @param name is name of the applet                                     
      */
     private static void fillInFileJavaScript(PrintWriter writer, String name, String codebase, boolean isFX, String draggable, String java_args, String jnlpFileName) {
         ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);
 
         writer.println("<HTML>"); // NOI18N
 
         writer.println("<HEAD>"); // NOI18N
 
         writer.print("   <TITLE>"); // NOI18N
 
         writer.print(bundle.getString("GEN_title"));
         writer.println("</TITLE>"); // NOI18N
 
         writer.println("</HEAD>"); // NOI18N
 
         writer.println("<BODY>\n"); // NOI18N
 
         writer.print(bundle.getString("GEN_warning"));
 
         writer.print("<H3><HR WIDTH=\"100%\">"); // NOI18N
 
         writer.print(bundle.getString("GEN_header"));
         writer.println("<HR WIDTH=\"100%\"></H3>\n"); // NOI18N
 
         writer.println("<P>"); // NOI18N
 
         writer.println("<script src=\"http://java.com/js/deployJava.js\"></script><br>");
         writer.println("<script>");
         writer.println("    var attributes = {");
         if (jnlpFileName != null) {
             writer.println("    };");
             writer.println("    var parameters = {");
             writer.println("            jnlp_href:'" + jnlpFileName + "',");
 
         } else {
             if (codebase == null) {
                 writer.print("            code: "); // NOI18N
 
             } else {
                 writer.print("           " + codebase.replaceAll("=", ":") + ",\n             code: "); // NOI18N
 
             }
             if (isFX) {
                 writer.println("'" + APPLET_MAIN_CLASS + "',");
                 writer.println("            width: 375,");
                 writer.println("            height: 375");
                 writer.println("    };");
                 writer.println("    var parameters = {");
                 writer.println("        ApplicationClass:" + "'" + name + "',");
             } else {
                 writer.println("'" + name + "'");
                 writer.println("    };");
                 writer.print("    var parameters = {");
             }
         } 
         if (draggable != null) {
             if ("true".equals(draggable)) {
                 writer.println("            draggable: 'true',");
             }
         }
         if (java_args != null) {
             writer.println("        java_arguments: '" + java_args + "'");
         }
         writer.println("    };");
         //XXX TODO Java Hardcoded here
         writer.println("    deployJava.runApplet( attributes, parameters, \"1.5\" );");
         writer.println("</script>");
         writer.println("</P>\n"); // NOI18N
 
         writer.print("<HR WIDTH=\"100%\"><FONT SIZE=-1><I>"); // NOI18N
 
         writer.print(bundle.getString("GEN_copy"));
         writer.println("</I></FONT>"); // NOI18N
 
         writer.println("</BODY>"); // NOI18N
 
         writer.println("</HTML>"); // NOI18N
 
         writer.flush();
     }
 
     /** fills in file with html source so it is html file with applet
      * @param file is a file to be filled
      * @param name is name of the applet                                     
      */
     private static void fillInJNLPFile(PrintWriter writer, String name, String codebase, boolean isFX, String[] libs, String distJar) {
         ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);
 
         writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
 
         writer.println("    <jnlp href=\"" + name + "\">"); // NOI18N
 
         writer.println("        <information>"); // NOI18N
 
         writer.println("            <title>" + bundle.getString("GEN_title") + "</title>"); // NOI18N
 
         writer.println("            <vendor>My Company, Inc</vendor>"); // NOI18N
 
         writer.println("            <offline-allowed/>"); // NOI18N
 
         writer.println("        </information>"); // NOI18N
 
         writer.println("        <resources>"); // NOI18N
         //TODO XXX hardcoded Java version
 
         writer.println("            <j2se version=\"1.6+\" href=\"http://java.sun.com/products/autodl/j2se\">"); // NOI18N
 
         writer.println("            <jar href=\"" + distJar + "\">"); // NOI18N
 
         String mainJar = "";
         for (int i = 0; i < libs.length; i++) {
             if (libs[i].equals("javafxgui.jar")) {
                 mainJar = " main=\"true\"";
             } else {
                 mainJar = "";
             }
             writer.println("            <jar href=\"lib/" + libs[i] + "\"" + mainJar + ">"); // NOI18N
         }
         writer.println("        </resources>"); // NOI18N
         writer.println("        <applet-desc"); // NOI18N
         writer.println("            name=\"" + name + "\""); // NOI18N
         writer.println("            main-class=\"" + APPLET_MAIN_CLASS + "\""); // NOI18N
         writer.println("            width=\"300\""); // NOI18N
         writer.println("            height=\"300\">"); // NOI18N
         writer.println("            <param name=\"ApplicationClass\" value=\"" + name + "\">"); // NOI18N
         writer.println("        </applet-desc>"); // NOI18N
         writer.println("    </jnlp>"); // NOI18N
         writer.flush();
     }
 
     /** fills in policy file with all permissions granted
      * @param writer is a file to be filled
      */
     private static void fillInPolicyFile(PrintWriter writer) {
         writer.println("grant {"); // NOI18N
 
         writer.println("permission java.security.AllPermission;"); // NOI18N
 
         writer.println("};"); // NOI18N
 
         writer.flush();
     }
 
     private static Project getProject(FileObject fileObject) {
         Project result = null;
         try {
             ProjectManager pm = ProjectManager.getDefault();
             FileObject projDir = fileObject.getParent();
             while (!pm.isProject(projDir)) {
                 projDir = projDir.getParent();
             }
             result = pm.findProject(projDir);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (IllegalArgumentException iae) {
         }
         return result;
     }
 }

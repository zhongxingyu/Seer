 /**************************************************************
  * Philippe VIENNE, Copyright (C) 2011.  All rights reserved. *
  **************************************************************/
 package org.javascool.jvs;
 
 // used for the translation
 import java.io.File;
 
 // Used to register the proglets
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 // Used to interface with the j5/j6 compiler
 
 // Used to load class
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 import org.javascool.gui.JVSMainPanel;
 import org.javascool.tools.Console;
 
 /** This factory defines how a Jvs code is translated into a Java code and compiled.
  * The goal of the Jvs syntax is to ease the syntax when starting to program in an imperative language, like Java.
  * <p>- This factory calls the java compiler as in the jdk5 (and earlier) case, but still works with jdk6. It is designed to be used in standalone mode.</p>
  * @see <a href="Jvs2Java.java.html">source code</a>
  * @serial exclude
  */
 public class Jvs2Java {
 
     public static boolean reportError = true;
 
     /**
      * This class can not be invoked
      * @deprecated
      */
     private Jvs2Java() {
     }
 
     /** Translates a Jvs code source.
      * @param The jvs source code
      */
     public static String translate(String jvsCode) {
         String text = jvsCode;
         // Here we check and correct a missing "void main()"
         if (!text.replaceAll("[ \n\r\t]+", " ").matches(".*void[ ]+main[ ]*\\([ ]*\\).*") && Jvs2Java.reportError) {
             if (text.replaceAll("[ \n\r\t]+", " ").matches(".*main[ ]*\\([ ]*\\).*")) {
                 System.out.println("Attention: il faut mettre \"void\" devant \"main()\" pour que le programme puisque se compiler");
                 text = text.replaceFirst("main[ ]*\\([ ]*\\)", "void main()");
             } else {
                 System.out.println("Attention: il faut un block \"void main()\" pour que le programme puisque se compiler");
                 if (Jvs2Java.reportError) {
                     text = "\nvoid main() {\n" + text + "\n}\n";
                 }
             }
         }
         String[] lines = text.split("\n");
         StringBuilder head = new StringBuilder();
         StringBuilder body = new StringBuilder();
         // Here is the translation loop
         {
             int i = 1;
             // Copies the user's code
             for (String line : lines) {
                 if (line.matches("^\\s*(import|package)[^;]*;\\s*$")) {
                     head.append(line);
                     body.append("//").append(line).append("\n");
                     if (line.matches("^\\s*package[^;]*;\\s*$")) {
                         System.out.println("Attention: on ne peut normallement pas définir de package Java en JavaScool\n le programme risque de ne pas s'exécuter correctement");
                     }
                 } else {
                     body.append(translateOnce(line)).append("\n");
                 }
                 i++;
             }
             // Imports proglet's static methods
             head.append("import static java.lang.Math.*;");
             head.append("import java.util.List;");
             head.append("import java.util.ArrayList;");
             head.append("import java.util.Map;");
             head.append("import java.io.*;");
             head.append("import java.net.*;");
             head.append("import java.util.*;");
             head.append("import org.javascool.*;");
             head.append("import org.javascool.gui.*;");
             head.append("import java.util.logging.*;");
             head.append("import static org.javascool.tools.Macros.*;");
             if (JVSMainPanel.getCurrentProglet().getJvsFunctionsToInclude()) {
                 head.append("import static ").append(JVSMainPanel.getCurrentProglet().getFullPackageName()).append(".Functions.*;");
             }
             if (!JVSMainPanel.getCurrentProglet().getJavaDependance().equals(new ArrayList<String>())) {
                 for (String dep : JVSMainPanel.getCurrentProglet().getJavaDependance()) {
                     head.append("import ").append(dep).append(";");
                 }
             }
             //head.append("import proglet.paintbrush.*;");
             // Declares the proglet's core as a Runnable in the Applet
             head.append("public class JvsToJavaTranslated").append(Jvs2Java.uid).append(" implements Runnable{");
             head.append("  private static final long serialVersionUID = ").append(uid).append("L;");
             head.append("  public void run() {");
             head.append(JVSMainPanel.getCurrentProglet().getJavaCodeToIncludeBefore());
             head.append(" main(); new File(System.getProperty(\"java.io.tmpdir\")+\"");
             head.append(File.separator.equals("\\") ? "\\\\" : "/");
             head.append("JvsToJavaTranslated");
             head.append(Jvs2Java.uid);
             head.append(".class\").delete(); ");
             head.append(JVSMainPanel.getCurrentProglet().getJavaCodeToIncludeAfter());
             head.append("}");
         }
        String finalBody = body.toString().replaceAll("((^|\n)([ \t]*)(?!((public|private|protected)([ \t]+)))([A-Za-z0-9_]+)([ ]+)([A-Za-z0-9_]+)\\(([^()]*)\\)([ ]*)\\{([ \t]*)(\n|$))", "public $1").replaceAll("(^|[\n\t ])for[\n\t ]*\\(([A-Za-z0-9_.]+)[\n\t ]+([A-Za-z0-9_.]+)[\n\t ]+in[\n\t ]+([A-Za-z0-9_.]+)[\n\t ]*\\)[\n\t ]*\\{","for (int tmpsystemi=0; tmpsystemi<$4.size(); tmpsystemi++) {$2 $3=($2)($4.get(tmpsystemi));");
         System.err.println("** Java Final Code **");
         System.err.println(head.toString() + finalBody + "}");
         System.err.println("*********************");
         return (head.toString() + finalBody + "}");
     }
 
     /** Translate a jvs line to a java line 
      * Translate with replace
      * @param line
      * @return 
      */
     private static String translateOnce(String line) {
         // Translates the while statement with sleep
         line = line.replaceAll("(while.*\\{)", "$1 sleep(20);");
       /*  line = line.replaceAll("int ([a-zA-Z0-9_]+)( |\t)","Integer $1 ");
         line = line.replaceAll("double ([a-zA-Z0-9_]+)( |\t|=)","Double $1 ");*/
         line = line.replaceAll("(.*[^a-zA-Z0-9_])([a-zA-Z0-9_]+[ \t=]*\\.getProperty[ \t=]*\\()[ \t=]*([a-zA-Z0-9_]+)[ \t=]*,([^)]*\\))(.*)","$1(($3)$2$4)$5");
 //       line = line.replaceAll("\\(int\\)","(Integer)");
 //       line = line.replaceAll("\\(double\\)","(Double)");
         line = line.replaceAll("([A-Za-z0-9_\\-]+)::([A-Za-z0-9_\\-]+)", "org.javascool.proglets.$1.Functions.$2");
         //line = line.replaceAll("(while\\(true\\)\\{)", "$1 sleep(50);");
         // Translates the Synthe proglet @tone macro
         line = line.replaceFirst("@tone:(.*)\\s*;",
                 "proglet.synthesons.SoundDisplay.tone = new org.javascool.SoundBit() { public double get(char c, double t) { return $1; } }; proglet.synthesons.SoundDisplay.syntheSet(\"16 a\");");
         return "    " + line;
     }
     // Counter used to increment the serialVersionUID in order to reload the different versions of the class
     private static int uid = 1;
 
     public static DiagnosticCollector<JavaFileObject> javaCompile(String pathToJavaFile) {
 
         // We init tools
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler(); // The compiler tool
         DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>(); // The diagnostic colector
         StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.FRENCH, null); // The file manager
 
         // Setup the file
         List<File> sourceFileList = new ArrayList<File>();
         sourceFileList.add(new File(pathToJavaFile));
         Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
 
         // Create the task
         JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
                 null, compilationUnits);
 
         // Now we javaCompile
         task.call();
 
         // Close file manager
         try {
             fileManager.close();
         } catch (IOException ex) {
             Logger.getLogger(Jvs2Java.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return diagnostics;
     }
 
     /** Compiles a Jvs code source.
      * <div>The jdk <tt>tool.jar</tt> must be in the path.</div>
      * @param path The file path to javaCompile.
      * @return The compilation success
      */
     public static Boolean jvsCompile(String jvsCode) {
 
         // Create an temp file
         File tmpJavaFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "JvsToJavaTranslated" + Jvs2Java.uid + ".java");
         String tmpPath = new File(tmpJavaFile.getParent()).getAbsolutePath();
         String tmpJFilePath = tmpJavaFile.getAbsolutePath();
 
         // We convert the JVS file and write the Java code ito the temp file
         try {
             Utils.saveString(Jvs2Java.translate(jvsCode), tmpJFilePath);
         } catch (IOException ex) {
             Logger.getLogger(Jvs2Java.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         /////////////////////////
         // Compile tmpJavaFile //
         /////////////////////////
 
         DiagnosticCollector<JavaFileObject> diagnostics = Jvs2Java.javaCompile(tmpJFilePath);
 
         // Delete the tmp Java source file
         tmpJavaFile.delete();
 
         // Show errors
         for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
             String javaDiagnostic = diagnostic.getMessage(Locale.FRENCH);
             String jvsDiagnostic = javaDiagnostic.split(" ", 2)[1];
             if (jvsDiagnostic.equals("not a statement")) {
                 jvsDiagnostic = "La variable indiqué n'existe pas";
             } else if (jvsDiagnostic.equals("';' expected")) {
                 jvsDiagnostic = "Il manque un point virgule à la fin de la ligne";
                 /* 
                  */
             } else if (jvsDiagnostic.matches(".*\\W*found\\W*:\\W([A-Za-z\\.]*)\\Wrequired:\\W([A-Za-z\\.]*)")) {
                 jvsDiagnostic = jvsDiagnostic.replaceAll("incompatible\\Wtypes\\W*found\\W*:\\W([A-Za-z\\.]*)\\Wrequired:\\W([A-Za-z\\.]*)", "Vous avez mis une valeur de type $1 alors qu'il faut une valeur de type $2");
             } else if (jvsDiagnostic.matches("package org\\.javascool\\.proglets\\.[A-Za-z0-9_]+ does not exist")) {
                 jvsDiagnostic = jvsDiagnostic.replaceAll("package org\\.javascool\\.proglets\\.([A-Za-z0-9_]+) does not exist", "La proglet $1 n'existe pas");
             } else {
                 jvsDiagnostic = "Erreur Java inconnue : \n" + jvsDiagnostic;
             }
             if (Jvs2Java.reportError) {
                 JVSMainPanel.reportCompileError((int) diagnostic.getLineNumber(), jvsDiagnostic);
             }
             if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
                 System.err.println("Erreur fatal lors de la compilation, arrêt de la compilation.");
                 uid++;
                 return false;
             }
 
         }
 
         ///////////////
         // SetUp Run //
         ///////////////
 
         try {
             Runnable program = Jvs2Java.load(tmpJavaFile.getAbsolutePath());
             Jvs2Java.runnable = program;
         } catch (Throwable e) {
             // If any error during the execution time
             System.err.println("Erreur fatal lors de l'éxecution, arrêt du programme.");
             uid++;
             return false;
         }
         uid++;
         return true;
     }
 
     /** Dynamically loads a Java runnable class to be used during this session.
      * @param path The path to the java class to load. The java class is supposed to belong to the "default" package, i.e. not to belong to a package.
      * @return An instantiation of this Java class. If the object is a runnable, the current runnable is set.
      *
      * @throws RuntimeException if an I/O exception occurs during command execution.
      * @throws IllegalArgumentException If the Java class name is not valid.
      */
     public static Runnable load(String path) throws Throwable {
         try {
             File javaClass = new File(path);
             URL[] urls = new URL[]{new URL("file:" + javaClass.getParent() + File.separator)};
             Class< ?> j_class = new URLClassLoader(urls).loadClass(javaClass.getName().replaceAll("\\.java", ""));
             Object o = j_class.newInstance();
             if (!(o instanceof Runnable)) {
                 throw new Exception("La class à charger n'est pas un runnable");
             }
             return (Runnable) o;
         } catch (Throwable e) {
             throw new RuntimeException("Erreur: impossible de charger la class");
         }
     }
     /** The java runnable wich has been compiled */
     public static Runnable runnable = null;
     public static String lastClass = "";
 }

 package com.martinleopold.mode.python;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import processing.app.Library;
 import processing.app.Sketch;
 import processing.app.SketchException;
 import processing.mode.java.JavaBuild;
 
 /**
  *
  * @author mlg
  */
 public class PythonBuild extends JavaBuild {
 
     public PythonBuild(Sketch sketch) {
         super(sketch);
     }
 
     /*
      * better?: write sketch code to a .py file and have the .java read it
      *
      *
      */
     @Override
     public String build(File srcFolder, File binFolder, boolean sizeWarning) throws SketchException {
         this.srcFolder = srcFolder;
         this.binFolder = binFolder;
 
         // escape all newlines to make single line string
        String pyCode = sketch.getMainProgram().replaceAll("\n", "\\\\n"); // \\n (will be inserted into a string literal)
         //System.out.println(pyCode);
 
         String code = "import org.python.util.PythonInterpreter; public class " + sketch.getName() + " { public static void main(String[] args) { PythonInterpreter py = new PythonInterpreter(); py.exec(\"" + pyCode + "\"); } }";
 
         //System.out.println(srcFolder);
         //System.out.println(getClassPath());
 
         // create .java file
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(srcFolder + File.separator + sketch.getName() + ".java"));
             writer.write(code);
             writer.close();
         } catch (Exception e) {
             throw new SketchException(e.getMessage());
         }
 
         String classNameFound = sketch.getName();
         //if (true) return null;
 
         // compile the program. errors will happen as a RunnerException
         // that will bubble up to whomever called build().
 //    Compiler compiler = new Compiler(this);
 //    String bootClasses = System.getProperty("sun.boot.class.path");
 //    if (compiler.compile(this, srcFolder, binFolder, primaryClassName, getClassPath(), bootClasses)) {
         if (processing.mode.java.Compiler.compile(this)) {
             sketchClassName = classNameFound;
             return classNameFound;
         }
         return null;
     }
 
     @Override
     public String getClassPath() {
         String classPath = binFolder.getAbsolutePath();
 
         // jython.jar
         classPath += File.pathSeparator + sketch.getMode().getContentFile("mode/jython.jar").getAbsolutePath();
 
         // regular classpath
         String javaClassPath = System.getProperty("java.class.path");
 
         // Remove quotes if any.. A messy (and frequent) Windows problem
         if (javaClassPath.startsWith("\"") && javaClassPath.endsWith("\"")) {
             javaClassPath = javaClassPath.substring(1, javaClassPath.length() - 1);
         }
         classPath += File.pathSeparator + javaClassPath;
 
         return classPath;
     }
 
     @Override
     public ArrayList<Library> getImportedLibraries() {
         return new ArrayList<Library>();
     }
 
     @Override
     public String getJavaLibraryPath() {
         return "";
     }
 
     @Override
     public boolean getFoundMain() {
         return true;
     }
 }

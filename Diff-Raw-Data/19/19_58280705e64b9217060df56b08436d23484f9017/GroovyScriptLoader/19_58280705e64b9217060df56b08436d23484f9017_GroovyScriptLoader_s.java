 package com.mulesoft.jockey;
 
 import groovy.lang.GroovyClassLoader;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.codehaus.groovy.control.CompilationUnit;
 import org.codehaus.groovy.control.CompilerConfiguration;
 import org.codehaus.groovy.tools.GroovyClass;
 
 public class GroovyScriptLoader {
     private String scriptDirectory;
     private String outputDirectory;
     
     public GroovyScriptLoader(String scriptDirectory, 
     						  String outputDirectory) {
         super();
         this.scriptDirectory = scriptDirectory;
 		this.outputDirectory = outputDirectory;
     }
     
     public List<AbstractCommand> loadScripts() throws Exception {
         File scriptsFile = new File(scriptDirectory);
         File classesFile;
         if (outputDirectory == null) {
             classesFile = File.createTempFile("mule-jockey-output", "");
             classesFile.delete();
             classesFile.mkdir();
         } else {
             classesFile = new File(outputDirectory);
         }
         
         if (!scriptsFile.exists()) {
             throw new IOException("Scripts directory does not exist!");
         }
         
         // get the last modified time of the scripts
         long lastModifiedScript = 0;
         for (File file : scriptsFile.listFiles()) {
             if (!file.isDirectory() && file.getName().endsWith(".groovy") && file.lastModified() > lastModifiedScript) {
                 lastModifiedScript = file.lastModified();
             }
         }
 
         // get the last modified time of the compiled files
         long lastModifiedClass = getLastModifiedRecursive(classesFile);
         
         List<AbstractCommand> commands;
//        if (lastModifiedScript > lastModifiedClass || outputDirectory == null) {
             commands = recompileScripts(scriptsFile, classesFile);
//        } else {
//            commands = loadClasses(classesFile);
//        }
         
         if (outputDirectory == null) {
             classesFile.deleteOnExit();
         }
         
         return commands;
     }
 
     /**
      * Traverse sub directories for last modified times because groovy will compile things into package directories,
      * so not everything is at the top level.
      * @param directory
      * @return
      */
     protected long getLastModifiedRecursive(File directory) {
         long lastModified = 0;
         if (directory.exists() && directory.listFiles() != null) {
             for (File file : directory.listFiles()) {
                 if (!file.isDirectory() && file.getName().endsWith(".class") && file.lastModified() > lastModified) {
                     lastModified = file.lastModified();
                 } else if (file.isDirectory()) {
                     long dirLastModified = getLastModifiedRecursive(file);
                     
                     if (dirLastModified > lastModified) {
                         lastModified = dirLastModified;
                     }
                 }
             }
         }
         return lastModified;
     }
 
     private List<AbstractCommand> loadClasses(File classesFile) throws IOException
     {
         URLClassLoader sysloader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
 
         GroovyClassLoader cl = new GroovyClassLoader(sysloader);
         cl.addClasspath(classesFile.getAbsolutePath());
         
         List<Class> classes = new ArrayList<Class>();
         List<AbstractCommand> commands = new ArrayList<AbstractCommand>();
         addClasses(classesFile, "", cl, classes);
         
         for (Class cls : classes) {
             // See if the new class is a Command!
             if (GroovyCommand.class.isAssignableFrom(cls) && (cls.getModifiers() & Modifier.ABSTRACT) == 0) {
                 GroovyCommandWrapper command = new GroovyCommandWrapper(cls);
                
                 commands.add(command);
             }
         }
         
         return commands;
     }
     
     protected void addClasses(File directory, String packageName, ClassLoader cl, List<Class> classes) {
         for (File file : directory.listFiles()) {
             if (!file.isDirectory() && file.getName().endsWith(".class")) {
                 String shortName = file.getName().substring(0, file.getName().length()-6);
                 String clsName = packageName.length() > 0 ? packageName + "." + shortName : shortName;
                 
                 try {
                     classes.add(cl.loadClass(clsName));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else if (file.isDirectory()) {
                 String subPkg = packageName.length() > 0 ? packageName + "." + file.getName() : file.getName();
                 
                 addClasses(file, subPkg, cl, classes);
             }
         }
     }
     
     private List<AbstractCommand> recompileScripts(File scriptsFile, File classesFile) {
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         GroovyClassLoader loader = new GroovyClassLoader(parent);
         
         CompilerConfiguration config = new CompilerConfiguration();
         config.setTargetDirectory(classesFile.getAbsolutePath());
         
         CompilationUnit unit = new CompilationUnit(config, null, loader);
         
         // load up all the .groovy files
         for (File file : scriptsFile.listFiles()) {
             if (!file.isDirectory() && file.getName().endsWith(".groovy")) {
                 unit.addSource(file);
             }
         }
         
         // compile them
         unit.compile();
         
         // Create commands for the compiled classes
         List<AbstractCommand> commands = new ArrayList<AbstractCommand>();
         for (Object o : unit.getClasses()) {
             GroovyClass gcls = (GroovyClass)o;
             Class<? extends GroovyCommand> cls = loader.defineClass(gcls.getName(), gcls.getBytes());
             
             // See if the new class is a Command!
             if (GroovyCommand.class.isAssignableFrom(cls) && (cls.getModifiers() & Modifier.ABSTRACT) == 0) {
                 GroovyCommandWrapper command = new GroovyCommandWrapper(cls);
                 commands.add(command);
             }
         }
         return commands;
     }
 }

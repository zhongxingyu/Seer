 package coldjavaserver;
 
 
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Properties;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 public class Compiler {
 
     private static DiagnosticCollector<JavaFileObject> diagnostics;
 
 
     public static CompilationTask makeCompilerTask(String fileName) {
   Properties prop = new Properties();
 	 try {
   		prop.load(new FileInputStream(MyWebServer.configPath));
  		System.setProperty("java.home", prop.getProperty("jdk"));
          } catch (IOException ex) {
      		ex.printStackTrace();
          }
 	
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         if (compiler == null) {
             System.out.println("Compiler not found");
             return null;
         }
 
         diagnostics = new DiagnosticCollector <>();
         StandardJavaFileManager fileMan = compiler.getStandardFileManager(diagnostics, null, null);
         Iterable<? extends JavaFileObject> fileObject = fileMan.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
         Iterable <String> options = Arrays.asList(new String[]{"-Xlint:deprecation"});
         
         return compiler.getTask(null, fileMan, diagnostics, options, null, fileObject);
     }
 }

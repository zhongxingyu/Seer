 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.player.bot;
 
 import capstone.player.Bot;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.CopyOption;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.Arrays;
 import java.util.Locale;
 import javax.tools.*;
 import org.objectweb.asm.ClassReader;
 
 /**
  *
  * @author Max
  */
 public class BotCompiler {
 
     private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 
     public static Bot createBot(String methodBody, String id, String path) throws BotCompilationException, IOException {
         StringWriter writer = new StringWriter();
         PrintWriter out = new PrintWriter(writer);
         out.println("public class " + id + " extends Bot {");
         out.println(methodBody);
         out.println("  public String getName(){");
         out.println("     return \"" + id + "\"");
         out.println("  }");
         out.println("}");
         out.close();
 
         JavaFileObject so = null;
         try {
             so = new InMemoryJavaFileObject(id, writer.toString());
         } catch (Exception exception) {
             exception.printStackTrace();
         }
 
         Bot bot = compile(so, id, path, true);
         //Success - now store the bot
         Path FROM = Paths.get(path+"/temp/"+id+".class");
         Path TO = Paths.get(path+"/"+id+".class");
         CopyOption[] options = new CopyOption[]{
             StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES
         };
         Files.copy(FROM, TO, options);
         return bot;
     }
     
     public static Bot load(String id, String path) throws BotCompilationException, IOException{
         //TODO load bot
        return null;
     }
 
     private static Bot compile(JavaFileObject source, String id, String path, boolean test) throws BotCompilationException {
         if (compiler
                 == null) {
             throw new BotCompilationException("No compiler found");
         } else {
 
             MyDiagnosticListener c = new MyDiagnosticListener();
             StandardJavaFileManager fileManager = compiler.getStandardFileManager(c,
                     Locale.ENGLISH,
                     null);
             Iterable options = Arrays.asList("-d", path+"/temp");
             JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
                     c, options, null,
                     Arrays.asList(source));
 
             boolean result = task.call();
             if (!result) {
                 throw new BotCompilationException("There was a problem compiling the code");
             }
             //TODO load bot
 
             try {
                 File file = new File(path+"/temp");
                 URL url = file.toURL();
                 URL[] urls = new URL[]{url};
 
                 
                 ClassLoader loader = new URLClassLoader(urls);
 
                 Class thisClass = loader.loadClass(id);
                 if(test){
                 securityCheck(thisClass, path+"/temp");
 
                 Bot bot = (Bot)thisClass.newInstance();
                 
             String msg = TestExcecutor.testBot(bot);
                 if (msg.equals("Pass")) {
                     return bot;
                 } else {
                     throw new BotCompilationException(msg);
                 }
                 }else{
                     return (Bot)thisClass.newInstance();
                 }
                 
             } catch (MalformedURLException ex) {
                 throw new BotCompilationException("There was an error loading the bytecode");
             } catch (ClassNotFoundException ex) {
                 throw new BotCompilationException("There was an error loading the bytecode");
             } catch (InstantiationException ex) {
                 throw new BotCompilationException("There was an error loading the bytecode");
             } catch (IllegalAccessException ex) {
                 throw new BotCompilationException("There was an error loading the bytecode");
             } catch (SecurityException ex) {
                 throw new BotCompilationException("There was an error loading the bytecode");
             }
 
         }
     }
     
     private static void securityCheck(Class clazz, String path) throws BotCompilationException{
         InputStream in = null;
         try {
             DependencyCollector  collector = new DependencyCollector();
             File classFile = new File(path + clazz.getName().replace('.','/')+".class");
             in = new FileInputStream(classFile);
             new ClassReader(in).accept(collector, 0);
             
             for (String ref:collector.getReferenced()) {
                 if(ref.startsWith("java.util")||ref.startsWith("capstone")){
                     continue;
                 }
                 if(ref.startsWith("java.lang")&&!ref.contains("System")){
                     continue;
                 }
                 throw new BotCompilationException("Error: Access to classes outside of bot-specific classes, java.util and java.lang (except System) not permitted.");
             }
             
         } catch (FileNotFoundException ex) {
             throw new BotCompilationException("There was an error accessing the bytecode");
         } catch (IOException ex) {
             throw new BotCompilationException("There was an error accessing the bytecode");
         } finally {
             try {
                 in.close();
             } catch (IOException ex) {
                 throw new BotCompilationException("There was an error accessing the bytecode");
             }
         }
 
     }
 }

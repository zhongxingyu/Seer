 package org.sterling.runtime;
 
 import static java.io.File.pathSeparator;
 import static java.lang.System.out;
 import static java.lang.Thread.currentThread;
 import static java.util.Arrays.asList;
 import static java.util.regex.Pattern.quote;
 import static org.sterling.runtime.expression.ExpressionFactory.constant;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.sterling.SterlingException;
 import org.sterling.cli.CommandLineRunner;
 import org.sterling.cli.RunnerException;
 import org.sterling.runtime.expression.*;
 
 public class SterlingRunner implements CommandLineRunner {
 
     private final ClassLoader delegateClassLoader;
 
     public SterlingRunner() {
         delegateClassLoader = currentThread().getContextClassLoader();
     }
 
     @Override
     public String getCommand() {
         return "run";
     }
 
     @Override
     public String getHelpText() {
         return "Runs the 'main' expression from a specified module";
     }
 
     @Override
     public void run(List<String> args) {
         try {
             URL[] urls = getModulePath(args);
             ExpressionLoader loader = createLoader(urls);
             String moduleName = getModuleName(args);
             Expression main = loader.load(getMainPath(moduleName));
             if (main instanceof Module) {
                 mainNotFound(moduleName, urls);
             } else {
                 main.apply(convertArgs(args)).evaluate();
             }
         } catch (SterlingException | IOException exception) {
             throw new RunnerException(exception);
         }
     }
 
     private ExpressionLoader createLoader(URL[] urls) {
         return new GlobalModule(new ClasspathResolver(new URLClassLoader(urls, delegateClassLoader)), new Compiler());
     }
 
     private Expression convertArgs(List<String> args) {
         List<Expression> elements = new ArrayList<>();
         for (String arg : args) {
             elements.add(constant(arg));
         }
         return new TupleExpression(elements);
     }
 
     private String getMainPath(String moduleName) {
         return moduleName.replace('.', '/') + "/main";
     }
 
     private String getModuleName(List<String> args) {
         Iterator<String> iterator = args.iterator();
         if (iterator.hasNext()) {
             String moduleName = iterator.next();
             iterator.remove();
             return moduleName;
         } else {
             throw new RunnerException("No module specified");
         }
     }
 
     private URL[] getModulePath(List<String> args) throws IOException {
         Iterator<String> iterator = args.iterator();
         while (iterator.hasNext()) {
             if ("-mp".equals(iterator.next())) {
                 iterator.remove();
                 URL[] urls = listPaths(iterator.next());
                 iterator.remove();
                 return urls;
             }
         }
         return listPaths(".");
     }
 
     private URL[] listPaths(String paths) throws IOException {
         List<String> pathList = new ArrayList<>(asList(paths.split(quote(pathSeparator))));
         List<URL> urls = new ArrayList<>();
         if (!pathList.contains(".")) {
             pathList.add(".");
         }
         for (String path : pathList) {
             urls.add(toUrl(path));
         }
         return urls.toArray(new URL[urls.size()]);
     }
 
     private void mainNotFound(String moduleName, URL[] urls) {
         out.println("ERROR! Could not find main expression in module '" + moduleName + "'.");
         out.println("Verify that module '" + moduleName + "' exists and defines an expression in the form of:");
         out.println();
        out.println("main = EXPRESSION");
         out.println();
         out.println("Module path: [");
         for (URL url : urls) {
             out.println("\t" + url.toString());
         }
         out.println("]");
         out.println();
     }
 
     private URL toUrl(String path) throws IOException {
         return new File(path).getCanonicalFile().toURI().toURL();
     }
 }

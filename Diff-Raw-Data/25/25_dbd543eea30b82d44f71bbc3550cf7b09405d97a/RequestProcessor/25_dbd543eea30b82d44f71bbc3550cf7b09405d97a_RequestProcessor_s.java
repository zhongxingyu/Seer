 import gleam.CompilationError;
 import gleam.CompilationResult;
 import gleam.Node;
 import gleam.View;
 import gleam.compiler.CompilationUnit;
 import gleam.compiler.FileCompilationUnit;
 import gleam.compiler.GleamCompiler;
 import gleam.util.HtmlCreator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Acme.Serve.Serve;
 
 @SuppressWarnings("serial")
 public class RequestProcessor extends HttpServlet {
   
   private static boolean developerMode = false;
   
   /**
    * Compiles the views.
    * 
    * @return A string containing the errors ready to be output.
    */
   private String compile() {
     String result = "";
     final File viewsFolder = new File(this.getClass().getResource("views/").getFile());
     final List<CompilationUnit> units = new LinkedList<CompilationUnit>();
     for (final File viewFile : viewsFolder.listFiles()) {
       if (viewFile.getAbsolutePath().endsWith(".gleam")) {
         final String viewName = viewFile.getName().substring(0, viewFile.getName().indexOf(".gleam"));
         final String sourceName = viewFile.toString().substring(0, viewsFolder.toString().length());
         final CompilationUnit unit = 
           new FileCompilationUnit(viewName, sourceName, viewFile.getAbsolutePath());
         System.out.println("Saved: " + viewName + " on " + new Date(viewFile.lastModified()));
         units.add(unit);
       }
     }
   
     // Compile all the units
     List<String> classPaths = new LinkedList<String>();
     classPaths.add("../gleam/bin");
     
     List<CompilationResult> compilationResults = new GleamCompiler().compile(units, classPaths);
     
     boolean hasErrors = false;
     for (CompilationResult compilationResult : compilationResults) {
       for (CompilationError error : compilationResult.getErrors()) {
         hasErrors = true;
         System.out.println(error.toString());
         result += compilationResult.getFilename() + ": " + error.toString() + "<br />";
       }
     }
     
     if (hasErrors) {
       result = "<pre>" + result + "</pre>";
     }
     
     return result;
   }
   
   public static Properties loadConfiguration() {
     Properties propertiesFile = new Properties();
     try {
       propertiesFile.load(new FileInputStream("config.properties"));
     } catch (Exception e) {
       throw new RuntimeException("Failed to load config.properties", e);
     }
     return propertiesFile;
   }
   
   public static void main(String[] args) {
     Properties config = loadConfiguration();
     
     // Set developer mode
     developerMode = Boolean.parseBoolean(config.getProperty("developermode"));
     
     // setting properties for the server, and exchangable Acceptors
     Properties properties = new java.util.Properties();
     properties.put("port", Integer.parseInt(config.getProperty("port")));
     properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
 
     final Serve srv = new Serve();
     srv.arguments = properties;
     srv.addDefaultServlets(null); // optional file servlet
     srv.addServlet("/", new RequestProcessor()); // optional
     
     Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
       public void run() {
         try {
           srv.notifyStop();
         } catch (java.io.IOException ioe) {
 
         }
         srv.destroyAllServlets();
       }
     }));
     srv.serve();
   }
   
   RequestProcessor() {
     compile();
   }
   
   private byte[] readFile(String filename) throws FileNotFoundException {
     FileInputStream fis = null;
     try {
       fis = new FileInputStream(filename);
       int numberBytes = fis.available();
       byte bytearray[] = new byte[numberBytes];
   
       fis.read(bytearray);
       return bytearray;
     } catch (IOException e) {
       if (e instanceof FileNotFoundException) {
         throw (FileNotFoundException)e;
       } else {
         throw new RuntimeException(e);
       }
     } finally {
       if (fis != null) {
         try {
           fis.close();
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
     }
   }
   
   final Map<String, View> viewCache = new HashMap<String, View>();
   @SuppressWarnings("deprecation")
   private View getView(String viewName) {
     if (!developerMode && viewCache.containsKey(viewName)) {
       return viewCache.get(viewName);
     }
     
     try {
       final ClassLoader classLoader = new URLClassLoader(new URL[] { new File("temp/").toURL() });
       final Class<?> viewClazz;
       viewClazz = classLoader.loadClass(viewName);
       View view = (View)viewClazz.newInstance();
       viewCache.put(viewName, view);
       return view;
     } catch (Exception e) {
       throw new RuntimeException("Failed to instantiate view", e);
     }
   }
   
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     long startTime = System.currentTimeMillis();
     
     response.addHeader("Expires", "Fri, 30 Oct 1998 14:19:41 GMT");
     response.addHeader("Content-Type", "text/html");
     
     // Locate all the gleam files
     String requestUri = request.getRequestURI().substring(1);
     if (requestUri.indexOf(".") != -1) {
       // Get from the resources folder
       try {
         final byte[] contents = readFile("resource/" + requestUri);
         response.getOutputStream().write(contents);
       } catch (FileNotFoundException e) {
         System.err.println("Requested missing file [" + e.getMessage() + "]");
       }
       return;
     }
     
     String result = "";
     
     
     try {
       if (developerMode) {
         result = compile();
       }
       
       if (result.length() == 0) {
         
         for (int i = 0; i < 1; i++) {
           final Object controller = new DummyController();
           String viewname = request.getRequestURI().substring(1);
           if (viewname.equals("")) {
             viewname = "index";
           }
           final View view = getView(viewname);
           final List<Node> nodes = view.view(controller);
           result = new HtmlCreator().generate(nodes);
         }
       }
     } catch (Throwable e) {
       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter(sw);
       e.printStackTrace(pw);
       result = sw.toString();
     }
 
     final PrintWriter writer = response.getWriter();
     response.addIntHeader("Content-Length", result.length());
     writer.println(result);
 //    writer.flush();
 //    writer.close();
     
     long endTime = System.currentTimeMillis();
     long totalTime = endTime - startTime;
     System.out.println("Execution performed in " + totalTime + "ms");
   }
  
  private String outputNode(final Node node) {
    StringBuffer attributes = new StringBuffer();
    StringBuffer result = new StringBuffer();
    for (Entry<String, Object> attribute : node.getAttributes().entrySet()) {
      attributes.append(" " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
    }

    if (node.getValue() != null || node.getNodes() != null) {
      result.append("<" + node.getTagName() + attributes + ">");
      if (node.getValue() != null) {
        result.append(node.getValue().toString());
      } else if (node.getNodes() != null) {
        for (final Node childNode : node.getNodes()) {
          result.append(outputNode(childNode));
        }
      }
      result.append("</" + node.getTagName() + ">");
    } else {
      result.append("<" + node.getTagName() + attributes + "/>");
    }
    return result.toString();
  }
 }

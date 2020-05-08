 package ru.spbau.bioinf.mgra;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XsltCompiler;
 import net.sf.saxon.s9api.XsltTransformer;
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.eclipse.jetty.servlets.MultiPartFilter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.stream.StreamSource;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class JettyServer {
 
     private static final Logger log = Logger.getLogger(JettyServer.class);
 
     private static final ServletContext servletContext = new UploadServleContext();
     private static MultiPartFilter uploadFilter;
 
     private static File uploadDir = new File("upload");
 
     private static String mgraExec = "exec";
     private static File xslDir = new File("xsl");
     private static int port = 8080;
     private static File execDir;
 
     private static final String CFG_FILE_NAME = "mgra.cfg";
     private static final Processor processor = new Processor(false);
     private static final XsltCompiler comp = processor.newXsltCompiler();
     private static XsltTransformer xslt;
     private static final String GENOME_FILE = "genome.txt";
 
     private static File dateDir;
 
     private static AtomicInteger requestId;
 
     private static int currentDay = -1;
 
     static {
         try {
             xslt = comp.compile(new StreamSource(
                     new InputStreamReader(new FileInputStream(new File(xslDir, "tree.xsl")), "UTF-8"))).load();
         } catch (Throwable e) {
             log.error("Error initializing xslt", e);
         }
         updateDateDir();
     }
 
     private static synchronized void updateDateDir() {
         Calendar calendar = Calendar.getInstance();
         int day = calendar.get(Calendar.DAY_OF_MONTH);
         if (day != currentDay) {
             dateDir = new File(new File(new File(uploadDir, Integer.toString(calendar.get(Calendar.YEAR))),
                        Integer.toString(calendar.get(Calendar.MONTH) + 1)),
                         Integer.toString(day));
             currentDay = day;
             if (dateDir.exists()) {
                requestId = new AtomicInteger(dateDir.list().length);
            } else {
                dateDir.mkdirs();
                requestId = new AtomicInteger(0);
            }
         }
     }
 
     public static void main(String[] args) throws Exception {
 
         if (args.length > 0)
             mgraExec = args[0];
 
         if (args.length > 1)
             port = Integer.parseInt(args[1]);
 
         uploadDir.mkdirs();
 
         uploadFilter = new MultiPartFilter();
         FilterConfig config = new FilterConfig() {
             public String getFilterName() {
                 return null;
             }
 
             public ServletContext getServletContext() {
                 return servletContext;
             }
 
             public String getInitParameter(String s) {
                 // _deleteFiles="true".equals(filterConfig.getInitParameter("deleteFiles"));
                 //String fileOutputBuffer = filterConfig.getInitParameter("fileOutputBuffer");
                 if ("deleteFiles".equalsIgnoreCase(s))
                     return "false";
                 return Integer.toString(32 * 1024);
             }
 
             public Enumeration getInitParameterNames() {
                 return null;
             }
         };
         uploadFilter.init(config);
 
         execDir = new File(mgraExec);
 
         Handler handler = new AbstractHandler() {
             public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                     throws IOException, ServletException {
                 String path = request.getPathInfo();
                 if (path.startsWith("/file/")) {
                     path = path.substring("/file".length());
                     File file = new File(uploadDir, path);
                     if (file.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
                         ServletOutputStream out = response.getOutputStream();
                         FileInputStream in = new FileInputStream(file);
 
                         byte[] buf = new byte[4048];
                         int count = 0;
                         while ((count = in.read(buf)) >= 0) {
                             out.write(buf, 0, count);
                         }
                         in.close();
                         out.close();
                     }
                     return;
                 }
                 final Properties properties = new Properties();
                 final File[] files = new File[1];
                 try {
                     uploadFilter.doFilter(request, response, new FilterChain() {
                         public void doFilter(ServletRequest wrapper, ServletResponse servletResponse) throws IOException, ServletException {
                             Enumeration uploads = wrapper.getAttributeNames();
                             while (uploads.hasMoreElements()) {
                                 String fileField = (String) uploads.nextElement();
                                 if ("genome".equals(fileField)) {
                                     files[0] = (File)wrapper.getAttribute(fileField);
                                 }
                             }
                             Enumeration parameters = wrapper.getParameterNames();
                             while (parameters.hasMoreElements()) {
                                 String name = (String) parameters.nextElement();
                                 String[] values = wrapper.getParameterValues(name);
                                 String concatValues = "";
                                 for (String value : values) {
                                     concatValues += value + " ";
                                 }
                                 properties.put(name, concatValues.trim());
                             }
                         }
                     });
                     //response.setContentType("text/html");
                     String fileUrl = processRequest(properties, files[0]);
                     response.sendRedirect("file/" + fileUrl);
 
                     //response.setStatus(HttpServletResponse.SC_OK);
                     //((Request) request).setHandled(true);
                 } catch (Throwable e) {
                     PrintWriter writer = response.getWriter();
                     log.error("Error processing request", e);
                     e.printStackTrace(writer);
                 } finally {
                     for (int i = 0; i < files.length; i++) {
                         File file = files[i];
                         if (file != null && file.exists())
                             file.delete();
                     }
                 }
             }
         };
         Server server = new Server(port);
         server.setHandler(handler);
         server.start();
     }
 
     private static String processRequest(Properties properties, File genomeFileUpload) throws Exception {
         File datasetDir;
         updateDateDir();
         do {
             String dir = "request" + requestId.getAndIncrement();
             datasetDir = new File(dateDir, dir);
         } while (datasetDir.exists());
         datasetDir.mkdirs();
 
         String key;
 
         if (genomeFileUpload != null) {
             genomeFileUpload.renameTo(new File(datasetDir, GENOME_FILE));
         } else {
             PrintWriter genomeFile = createOutput(datasetDir, GENOME_FILE);
             int genomeId = 1;
             key = "genome" + genomeId;
             do {
                 genomeFile.println(properties.get(key));
                 genomeId++;
                 genomeFile.println();
                 key = "genome" + genomeId;
             } while (properties.containsKey(key));
             genomeFile.close();
         }
         PrintWriter cfgFile = createOutput(datasetDir, CFG_FILE_NAME);
         cfgFile.println("[Genomes]");
 
         int aliasId = 1;
         key = "alias" + aliasId;
         do {
             cfgFile.println(properties.get("name" + aliasId) + " " +  properties.get(key));
             aliasId++;
             key = "alias" + aliasId;
         } while (properties.containsKey(key));
 
         cfgFile.println("[Blocks]");
         cfgFile.println("format " + getFormat(new File(datasetDir, GENOME_FILE)));
         cfgFile.println("file genome.txt");
 
         cfgFile.println();
 
         cfgFile.println("[Trees]");
         cfgFile.println(properties.getProperty("trees"));
         cfgFile.println();
 
         cfgFile.println("[Algorithm]");
         cfgFile.println();
 
         cfgFile.println("stages " + properties.getProperty("stages"));
         cfgFile.println();
 
         boolean useTarget = "1".equals(properties.getProperty("useTarget"));
 
         if (useTarget) {
             cfgFile.println("target " + properties.getProperty("target"));
             cfgFile.println();
         }
 
         cfgFile.println("[Graphs]");
         cfgFile.println();
 
         cfgFile.println("filename stage");
         cfgFile.println();
 
         cfgFile.println("colorscheme set19");
         cfgFile.println();
 
         if (useTarget) {
             cfgFile.println("[Completion]");
             cfgFile.println(properties.getProperty("completion"));
             cfgFile.println();
         }
 
         cfgFile.close();
 
         String[] command = new String[]{new File(execDir, "mgra.exe").getAbsolutePath(), CFG_FILE_NAME};
 
         Process process = Runtime.getRuntime().exec(
                 command,
                 new String[]{}, datasetDir);
         Thread outputThread = listenOutput(process.getInputStream(), "output");
         Thread errorThread = listenOutput(process.getErrorStream(), "error output");
 
         do {
             try {
                 int value = process.waitFor();
                 log.debug("MGRA process return value : " + value);
                 break;
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         } while (true);
 
         outputThread.interrupt();
         errorThread.interrupt();
 
         new TreeReader(new File(datasetDir, CFG_FILE_NAME));
 
         XdmNode source = getSource(processor, new File(datasetDir, "tree.xml"));
         Serializer out = new Serializer();
         out.setOutputProperty(Serializer.Property.METHOD, "html");
         out.setOutputProperty(Serializer.Property.INDENT, "yes");
         out.setOutputFile(new File(datasetDir, "tree.html"));
 
         xslt.setInitialContextNode(source);
         xslt.setDestination(out);
         xslt.transform();
 
         String path = datasetDir.getCanonicalPath();
         path = path.replaceAll("\\\\","/");
         int cur = path.length();
         for (int i = 0; i < 4; i++) {
             cur = path.lastIndexOf("/", cur - 1);
         }
         return path.substring(cur + 1) + "/tree.html";
 
     }
 
 
     private static String getFormat(File genome) throws  IOException {
         BufferedReader reader = TreeReader.getBufferedInputReader(genome);
         String s;
         int infercars = 0;
         int grimm = 0;
         while ((s = reader.readLine())!=null) {
              s = s.trim();
              if (!s.startsWith("#") && s.length() > 0) {
                  if (s.endsWith("+") || s.endsWith("-")) {
                      infercars++;
                  } else {
                      if (s.endsWith("$")) {
                          grimm++;
                      }
                  }
             }
         }
 
         String ans = infercars > grimm ? "infercars" : "grimm";
         log.debug("Genome format detection: " + infercars + " for infercars " + grimm + " for grimm, answer - " + ans);
         return ans;
     }
 
 
     private static Thread listenOutput(InputStream inputStream, final String type) {
         final BufferedReader input =
                 new BufferedReader
                         (new InputStreamReader(inputStream));
 
 
         Thread outputThread = new Thread(new Runnable() {
             public void run() {
                 String line;
                 try {
                     while ((line = input.readLine()) != null) {
                         log.debug("MGRA " + type + " : " + line);
                     }
                 } catch (IOException e) {
                     log.error("Error reading MGRA " + type, e);
                 } finally {
                     try {
                         input.close();
                     } catch (IOException e) {
                         log.error("Error closing MGRA " + type, e);
                     }
                 }
             }
         });
         outputThread.start();
         return outputThread;
     }
 
     private static PrintWriter createOutput(File datasetDir, String file) throws UnsupportedEncodingException, FileNotFoundException {
         return new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(datasetDir, file)), "UTF-8"));
     }
 
     private static XdmNode getSource(Processor processor, File xmlFile) throws SaxonApiException {
         return processor.newDocumentBuilder().build(new StreamSource(
                 xmlFile));
     }
 
     public static class UploadServleContext implements ServletContext {
         public String getContextPath() {
             return null;
         }
 
         public ServletContext getContext(String s) {
             return null;
         }
 
         public int getMajorVersion() {
             return 0;
         }
 
         public int getMinorVersion() {
             return 0;
         }
 
         public String getMimeType(String s) {
             return null;
         }
 
         public Set getResourcePaths(String s) {
             return null;
         }
 
         public URL getResource(String s) throws MalformedURLException {
             return null;
         }
 
         public InputStream getResourceAsStream(String s) {
             return null;
         }
 
         public RequestDispatcher getRequestDispatcher(String s) {
             return null;
         }
 
         public RequestDispatcher getNamedDispatcher(String s) {
             return null;
         }
 
         public Servlet getServlet(String s) throws ServletException {
             return null;
         }
 
         public Enumeration getServlets() {
             return null;
         }
 
         public Enumeration getServletNames() {
             return null;
         }
 
         public void log(String s) {
         }
 
         public void log(Exception e, String s) {
 
         }
 
         public void log(String s, Throwable throwable) {
 
         }
 
         public String getRealPath(String s) {
             return null;
         }
 
         public String getServerInfo() {
             return null;
         }
 
         public String getInitParameter(String s) {
             return null;
         }
 
         public Enumeration getInitParameterNames() {
             return null;
         }
 
         public Object getAttribute(String s) {
             //tempdir=(File)filterConfig.getServletContext().getAttribute("javax.servlet.context.tempdir");
             return uploadDir;
         }
 
         public Enumeration getAttributeNames() {
             return null;
         }
 
         public void setAttribute(String s, Object o) {
         }
 
         public void removeAttribute(String s) {
         }
 
         public String getServletContextName() {
             return null;
         }
     }
 
 }

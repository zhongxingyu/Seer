 package kkckkc.jsourcepad.http;
 
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import com.sun.net.httpserver.*;
 import kkckkc.jsourcepad.model.*;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.utils.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import sun.net.www.MimeEntry;
 import sun.net.www.MimeTable;
 
 import javax.annotation.PostConstruct;
 import java.io.*;
 import java.net.URLDecoder;
 import java.util.Iterator;
 import java.util.Map;
 
 public class PreviewServer {
 
     private HttpServer httpServer;
 
     @Autowired
     public void setHttpServer(HttpServer httpServer) {
         this.httpServer = httpServer;
     }
 
     @PostConstruct
     public void init() {
         initPreview();
         initFile();
         initCmd();
     }
 
     private void initPreview() {
         final String path = "/preview";
 
         final HttpContext context = httpServer.createContext(path);
         context.setHandler(new HttpHandler() {
             public void handle(HttpExchange exchange) throws IOException {
                 String requestMethod = exchange.getRequestMethod();
                 if (requestMethod.equalsIgnoreCase("GET")) {
                     String httpPath = exchange.getRequestURI().toString();
                     httpPath = httpPath.substring(path.length() + 1);
 
                     int windowId = getWindowId(httpPath);
                     String path = getFilePath(httpPath);
 
                     Window window = Application.get().getWindowManager().getWindow(windowId);
                     Project project = window.getProject();
 
                     // Look through open files, these should serve the current state, not the saved
                     // state
                     int tabIdx = 0;
                     Doc docToServe = null;
                     for (Doc doc : window.getDocList().getDocs()) {
                         if (doc.isBackedByFile() && project.getProjectRelativePath(doc.getFile().getPath()).equals(path)) {
                             docToServe = doc;
                         } else if (path.equals("/tab-" + tabIdx)) {
                             docToServe = doc;
                         }
                         tabIdx++;
                     }
 
                     OutputStream responseBody = exchange.getResponseBody();
                     Headers responseHeaders = exchange.getResponseHeaders();
 
                     if (docToServe != null) {
                         String mimeEncoding = "text/html";
                         if (docToServe.isBackedByFile()) {
                             mimeEncoding = getMimeEncoding(docToServe.getFile());
                         }
 
                         responseHeaders.set("Content-Type", mimeEncoding);
                         exchange.sendResponseHeaders(200, 0);
 
                         Buffer buffer = docToServe.getActiveBuffer();
                         String content = buffer.getCompleteDocument().getText();
 
                         Writer writer = new OutputStreamWriter(responseBody);
                         writer.append(content);
                         writer.flush();
                         writer.close();
                     } else {
                         File f = new File(project.getProjectDir(), path);
 
                         responseHeaders.set("Content-Type", getMimeEncoding(f));
                         exchange.sendResponseHeaders(200, 0);
 
                         Files.copy(f, responseBody);
                         responseBody.flush();
                         responseBody.close();
                     }
                 }
             }
         });
     }
 
     private void initCmd() {
         final String path = "/cmd";
 
         final HttpContext context = httpServer.createContext(path);
         context.setHandler(new HttpHandler() {
             public void handle(HttpExchange exchange) throws IOException {
                 String requestMethod = exchange.getRequestMethod();
                 if (requestMethod.equalsIgnoreCase("GET")) {
 
                     String httpPath = exchange.getRequestURI().toString();
                     httpPath = httpPath.substring(path.length() + 1);
 
                     Map<String, String> params = parseQueryString(httpPath);
 
                     String cmd = httpPath.substring(0, httpPath.indexOf("?", 1));
 
                     exchange.sendResponseHeaders(204, 0);
 
                     System.out.println("httpPath = " + httpPath);
                     System.out.println("cmd = " + cmd);
 
                     if ("open".equals(cmd)) {
                         String url = params.get("url");
                         url = StringUtils.removePrefix(url, "http://localhost:" + Config.getHttpPort() + "/files");
 
                         System.out.println("url = " + url);
 
                         Window window;
                         if (params.containsKey("windowId")) {
                             int windowId = Integer.parseInt(params.get("windowId"));
 
                             window = Application.get().getWindowManager().getWindow(windowId);
                             window.getDocList().open(new File(url));
                         } else {
                             window = Application.get().open(new File(url));
                         }
 
 
                         String line = params.get("line");
                         if (line != null) {
                             int lineIdx = Integer.parseInt(line);
 
                             Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
                             LineManager lm = buffer.getLineManager();
 
                             Iterator<LineManager.Line> it = lm.iterator();
                             while (it.hasNext()) {
                                 LineManager.Line l = it.next();
                                 if (l.getIdx() == (lineIdx - 1)) {
                                     buffer.setSelection(Interval.createEmpty(l.getStart()));
                                     break;
                                 }
                             }
                         }
                     } else {
                         throw new RuntimeException("Unsupport cmd");
                     }
 
                 }
             }
         });
     }
 
     private void initFile() {
         final String path = "/files";
 
         final HttpContext context = httpServer.createContext(path);
         context.setHandler(new HttpHandler() {
             public void handle(HttpExchange exchange) throws IOException {
                 String requestMethod = exchange.getRequestMethod();
                 if (requestMethod.equalsIgnoreCase("GET")) {
                     String httpPath = exchange.getRequestURI().toString();
                     httpPath = httpPath.substring(path.length());
                    httpPath = URLDecoder.decode(httpPath, "utf-8");
 
                     OutputStream responseBody = exchange.getResponseBody();
                     Headers responseHeaders = exchange.getResponseHeaders();
 
                     File f = new File(httpPath);
 
                     responseHeaders.set("Content-Type", getMimeEncoding(f));
                     exchange.sendResponseHeaders(200, 0);
 
                     Files.copy(f, responseBody);
                     responseBody.flush();
                     responseBody.close();
                 }
             }
         });
     }
 
     private String getFilePath(String httpPath) {
         return httpPath.substring(httpPath.indexOf('/'));
     }
 
     private int getWindowId(String httpPath) {
         return Integer.parseInt(httpPath.substring(0, httpPath.indexOf('/')));
     }
 
     private String getMimeEncoding(File f) {
         MimeEntry me = MimeTable.getDefaultTable().findByFileName(f.getName());
         if (me == null) {
             if (f.getName().endsWith(".css")) return "text/css";
             if (f.getName().endsWith(".js")) return "text/javascript";
             return "text/html";
         }
         return me.getType();
     }
 
     public Map<String, String> parseQueryString(String s) throws UnsupportedEncodingException {
         Map<String, String> params = Maps.newHashMap();
         String[] urlParts = s.split("\\?");
         if (urlParts.length > 1) {
             String query = urlParts[1];
             for (String param : query.split("&")) {
                 String[] pair = param.split("=");
                 String key = URLDecoder.decode(pair[0], "UTF-8");
                 String value = URLDecoder.decode(pair[1], "UTF-8");
                 params.put(key, value);
             }
         }
         return params;
     }
 }

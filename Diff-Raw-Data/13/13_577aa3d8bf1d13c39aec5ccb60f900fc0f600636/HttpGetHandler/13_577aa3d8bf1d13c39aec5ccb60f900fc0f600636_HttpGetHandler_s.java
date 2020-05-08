 package com.interview.web.http;
 
 import com.interview.web.utils.Constants;
 
 import java.io.*;
 
 public class HttpGetHandler extends HttpFileHandler implements HttpHandler {
 
     @Override
     public void handle(String method, HttpRequest request, PrintStream out) throws IOException {
         if (method.equalsIgnoreCase(GET)) {
             doGet(request, out);
         } else {
             getSuccessor().handle(method, request, out);
         }
     }
 
     private void doGet(HttpRequest request, PrintStream out) throws IOException {
         String path = request.getHeader(Constants.PATH);
         getLogger().info("GET for " + path);
 
        File file = new File(getRoot(), path);
         if (file.exists()) {
             if (file.isDirectory()) {
                 sendFolder(out, file);
             } else {
                 sendFile(out, file);
             }
         }
         else {
             sendForm(out, path);
         }
     }
 
     private void sendForm(PrintStream out, String path) throws IOException {
         printHeaders(out, HTTP_OK, "text/html; charset=utf-8");
         StringBuilder html = new StringBuilder();
 
         html.append("<html>\r\n");
         html.append("<head>\r\n");
         html.append("    <title>\r\n");
         html.append(path);
         html.append("</title>\r\n");
         html.append("</head>\r\n");
         html.append("<body>\r\n");
         html.append("        <span>Enter file contents for: </span>\r\n");
         html.append("<span>\r\n");
         html.append(path);
         html.append("</span>\r\n");
         html.append("        <br/>\r\n");
         html.append("    <form name=\"");
         html.append(Constants.CONTENT_PARAM_NAME);
         html.append("\" id=\"fileContent\" action=\"");
         html.append(path);
         html.append("\" method=\"POST\">\r\n");
         html.append("        <textarea name=\"content\" id=\"content\" rows=\"30\" cols=\"160\"></textarea>\r\n");
         html.append("        <input type=\"submit\" value=\"Submit\">\r\n");
         html.append("    </form>\r\n");
         html.append("</body>\r\n");
         html.append("</html>\r\n");
 
         out.print(html.toString());
     }
 
     /**
      * Send file content.
      *
      * @param out
      * @param file
      * @throws IOException
      */
     private void sendFile(PrintStream out, File file) throws IOException {
         printHeaders(out, HTTP_OK, TEXT_HTML);
         out.println("<a href=\"..\">..</a><BR>");
         BufferedReader is = null;
         try {
             is = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath())));
             String line;
             while ((line = is.readLine()) != null) {
                 out.println(line);
                 out.println("<br/>");
             }
         } finally {
             if (is != null) {
                 is.close();
             }
         }
     }
 
     /**
      * Send the folder listing.
      */
     private void sendFolder(PrintStream out, File file) throws IOException {
         String html = listFolder(file);
         byte[] data = html.getBytes("UTF-8");
         printHeaders(out, HTTP_OK, TEXT_HTML);
         out.write(data);
     }
 
     /**
      * Generate a string with the folder listing in HTML format.
      *
      * @param folder Folder to list.
      * @return An HTML representation of the folder listing.
      * @throws IOException
      */
     private String listFolder(File folder) throws IOException {
         StringBuilder html = new StringBuilder();
         html.append("<html><head><title>");
         html.append(folder.getName());
         html.append("</title></head><body>");
         html.append("<a href=\"..\">..</a><BR>");
         String[] list = folder.list();
         if (list != null) {
             for (int i = 0; i < list.length; i++) {
                 File file = new File(folder, list[i]);
                 if (file.isDirectory()) {
                     html.append("<a href=\"");
                     html.append(list[i]);
                     html.append("/\">");
                     html.append(list[i]);
                     html.append("</a><BR>");
                 } else {
                     html.append("<a href=\"");
                     html.append(list[i]);
                     html.append("\">");
                     html.append(list[i]);
                     html.append("</a><BR>");
                 }
             }
         }
 
         html.append("</body></html>");
 
         return html.toString();
     }
 }

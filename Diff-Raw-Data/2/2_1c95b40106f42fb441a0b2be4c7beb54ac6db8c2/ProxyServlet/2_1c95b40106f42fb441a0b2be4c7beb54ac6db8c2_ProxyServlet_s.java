 package de.saumya.gwt.persistence.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class ProxyServlet extends HttpServlet {
 
     @SuppressWarnings("unchecked")
     @Override
     protected void service(final HttpServletRequest req,
             final HttpServletResponse resp) throws ServletException,
             IOException {
         final URL url = new URL("http://localhost:3000"
                + req.getRequestURI().replaceFirst("/[a-z.]*/", "/"));
         final HttpURLConnection con = ((HttpURLConnection) url.openConnection());
         con.setRequestMethod(req.getMethod());
         con.setDoInput(true);
         con.addRequestProperty("Content-type", req.getContentType());
         log(req.getMethod());
         InputStream in = null;
         OutputStream out = null;
         final Enumeration<String> headers = req.getHeaderNames();
         while (headers.hasMoreElements()) {
             final String key = headers.nextElement();
             if (!key.startsWith("Content-") && !key.equals("Host")) {
                 con.addRequestProperty(key, req.getHeader(key));
             }
         }
         if (req.getContentLength() > 0) {
             con.setDoOutput(true);
             con.addRequestProperty("Content-length", ""
                     + req.getContentLength());
             try {
                 in = req.getInputStream();
                 out = con.getOutputStream();
                 int i = in.read();
                 while (i != -1) {
                     out.write(i);
                     i = in.read();
                 }
             }
             finally {
                 if (out != null) {
                     out.close();
                 }
             }
         }
         try {
 
             resp.setContentType(con.getContentType());
             try {
                 in = con.getInputStream();
             }
             catch (final IOException e) {
                 in = con.getErrorStream();
                 resp.setStatus(con.getResponseCode());
             }
             for (final Map.Entry<String, List<String>> prop : con.getHeaderFields()
                     .entrySet()) {
                 // assume only one value for each key !!!
                 if (prop.getValue().get(0) != null && prop.getKey() != null) {
                     resp.addHeader(prop.getKey(), prop.getValue().get(0));
                 }
             }
             out = resp.getOutputStream();
             int i = in.read();
             while (i != -1) {
                 out.write(i);
                 i = in.read();
             }
         }
         finally {
             if (in != null) {
                 in.close();
             }
         }
     }
 }

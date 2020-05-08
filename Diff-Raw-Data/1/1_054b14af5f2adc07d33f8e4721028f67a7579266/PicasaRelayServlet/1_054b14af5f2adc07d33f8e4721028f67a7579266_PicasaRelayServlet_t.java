 package com.cantstopthesignals;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.regex.Pattern;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class PicasaRelayServlet extends HttpServlet {
   private static final Pattern ALLOWED_URLS = Pattern.compile(
       "^https://(picasaweb.google.com|lh[0-9]+.googleusercontent.com)/.*$");
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     String urlParam = req.getParameter("url");
     URL url = new URL(urlParam);
 
     if (!ALLOWED_URLS.matcher(url.toString()).matches()) {
       throw new RuntimeException("Url not in whitelist");
     }
 
     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
     String method = req.getParameter("method");
     if (method != null) {
       connection.setRequestMethod(method);
     }
     String[] headers = req.getParameterValues("header");
     if (headers != null) {
       for (String header : headers) {
         String[] headerPieces = header.split("=", 2);
         connection.addRequestProperty(headerPieces[0], headerPieces[1]);
       }
     }
     connection.setConnectTimeout(60000);
     connection.setReadTimeout(60000);
 
     resp.setContentType(connection.getContentType());
    resp.setStatus(connection.getResponseCode());
 
     InputStream inputStream = connection.getInputStream();
 
     byte[] buffer = new byte[1024];
     int bytesRead;
     while ((bytesRead = inputStream.read(buffer, 0, 1024)) > 0) {
       resp.getOutputStream().write(buffer, 0, bytesRead);
     }
   }
 }

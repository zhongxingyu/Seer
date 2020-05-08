 package com.fear_airsoft.json;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.logging.*;
 import java.io.*;
 import java.net.*;
 import java.nio.charset.Charset;
 
 public class JsonServletPublishedData extends HttpServlet {
   private static final long serialVersionUID = 1L;
   private static final Logger logger = Logger.getLogger(JsonServletPublishedData.class.getName());
   private static final String publishedDataUrl="https://www.googledrive.com/host/0B4Nj2G61OMg-OGgtdXdGei1zR2M";
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
     resp.setHeader("Access-Control-Allow-Origin", "*");
     resp.setContentType("application/json");
     resp.setCharacterEncoding("UTF-8");
     String service=req.getPathInfo();
     String result=executeGet(publishedDataUrl+service);
     PrintWriter out = resp.getWriter();
     out.write(result);
     out.close();
   }
  
   @Override
   protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{ 
     resp.setHeader("Access-Control-Allow-Origin", "*");
     resp.setHeader("Access-Control-Allow-Methods", "GET");
   }
   
   public static String executeGet(String targetURL)
   {
     URL url;
     HttpURLConnection connection = null;  
     try {
       url = new URL(targetURL);
       connection = (HttpURLConnection)url.openConnection();
       connection.setRequestMethod("GET");
       connection.setUseCaches(true);
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("ISO-8859-1")));
       StringBuffer response = new StringBuffer();
       String inputLine;
       while((inputLine = in.readLine())!=null) 
         response.append(inputLine);
       in.close();
       return response.toString();
     } catch (Exception e) {
       logger.throwing(JsonServletPublishedData.class.getName(),"executeGet",e);
       return null;
     } finally {
       if(connection != null) {
         connection.disconnect(); 
       }
     }
   }
 }

 package httpgame;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import static utils.ConsoleUtils.*;
 import static utils.FileUtils.*;
 
 public class HttpRequest
 {
     private HashMap<String,List<String>> headers = new HashMap<String,List<String>>();
     private HashMap<String,String> cookies = new HashMap<String,String>();
     private HashMap<String,List<String>> parameters = new HashMap<String,List<String>>();
     private byte[] content;
     
     private String method, url, httpVersion;
     
     private BufferedInputStream in;
     
     private void parseHeader(String line)
     {
         int colon = line.indexOf(":");
         String name = line.substring(0, colon).trim().toLowerCase();
         String value = line.substring(colon+1).trim().toLowerCase();
         List<String> list = headers.get(name);
         if (list == null)
         {
             list = new ArrayList<String>();
             headers.put(name,list);
         }
         list.add(value);
     }
     
     private void extractCookies()
     {
         List<String> cookieData = headers.get("cookie");
         if (cookieData == null) return;
         for (String str :cookieData)
         {
             String[] splits = str.split(";");
             for (String nvp :splits)
             {
                 String[] namevalue = nvp.split("=");
                 cookies.put(namevalue[0], namevalue[1]);
             }
         }
     }
     
     private void extractUrlParameters()
     {
         int qmark = url.indexOf('?');
         if (qmark == -1) return;
         String paramString = url.substring(qmark+1);
         url = url.substring(0,qmark);
         
         extractParameters(paramString);
     }
     
     private void extractParameters(String paramString)
     {
         println("Parameters! " + paramString);
         String[] params = paramString.split("&");
         for (String param :params)
         {
             param = param.trim();
             if (param.isEmpty()) continue;
             println("PARAMETER! " + param);
             String[] namevalue = param.split("=");
             List<String> list = parameters.get(namevalue[0]);
             if (list == null)
             {
                 list = new ArrayList<String>();
                 parameters.put(namevalue[0],list);
             }
             list.add(namevalue[1]);
         }
     }
     
     public List<String> getParameterValues(String name)
     {
         return parameters.get(name);
     }
     
     public String getParameterValue(String name)
     {
         List<String> list = getParameterValues(name);
         if (list == null || list.isEmpty()) return null;
         return list.get(0);
     }
 
     public String getMethod()
     {
         return method;
     }
 
     public String getUrl()
     {
         return url;
     }
 
     public String getHttpVersion()
     {
         return httpVersion;
     }
     
     public String getCookie(String name)
     {
         return cookies.get(name);
     }
     
     public List<String> getHeaders(String name)
     {
         return headers.get(name);
     }
     
     public int getContentLength()
     {
         try
         {
             List<String> list = headers.get("content-length");
             if (list == null) return 0;
             String contentLength = list.get(0);
             if (contentLength == null) return 0;
             return Integer.parseInt(contentLength);
         }
         catch (NumberFormatException nfe)
         {
             return 0;
         }
     }
     
     public HttpRequest(InputStream in)
             throws IOException
     {
         this.in = new BufferedInputStream(in);
         
         String Tn = Thread.currentThread().getName();
         
         println(Tn + "+=====~~~~------");
         
         String line = readLine(this.in);
         println(Tn + "| <- " + line);
         
         String[] splits = line.split(" ");
         method = splits[0];
         url = splits[1];
         httpVersion = splits[2];
 
         while ((line = readLine(this.in)) != null && !line.isEmpty())
         {
             parseHeader(line);
             println(Tn + "| <- " + line);
         }
         
         extractCookies();
         extractUrlParameters();
         
         if (getContentLength() > 0)
         {
             println("before reading content");
             content = new byte[getContentLength()];
            in.read(content);
             println("after reading content");
         }
         
         if ("POST".equalsIgnoreCase(method))
         {
             println("getting POST data");
             extractParameters(new String(content));
         }
 
         println(Tn + "+=====~~~~------");
         
     }
     
 }

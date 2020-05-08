 package com.zynick.comparison;
 
 import java.io.IOException;
 import java.io.PrintWriter;
import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.zynick.comparison.sites.Website;
 
 public class Search extends HttpServlet {
 
     private static final long serialVersionUID = -6785660763248229508L;
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
         
         // get parameters
         String query = request.getParameter("query");
         String sizeStr = request.getParameter("size");
         String source = request.getParameter("source");
         String callback = request.getParameter("callback");
         
         // prepare list
         List<Item> result = new ArrayList<Item>();
         
         // get website
         Website site = getSite(source);
         if (site != null) {
             try {
                query = URLEncoder.encode(query, "UTF-8");
                 int size = Integer.parseInt(sizeStr);
                 
                 // get result
                 result = site.parse(query, size);
                 
             } catch (Exception ex) {
                 // doesn't matter.
                 System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                System.out.println("\t" + ex.getStackTrace()[0]);
             }
         }
         
         // response back
        PrintWriter out = response.getWriter();
         response.setContentType("application/json");
         String resp = callback + "(" + result.toString() + ")";
         out.println(resp);
         out.flush();
     }
     
     private static final Website getSite(String source) {
         try {
             return (Website) Class.forName("com.zynick.comparison.sites." + source).newInstance();
         } catch (Exception ex) {
             return null;
         }
     }
 }
 
 

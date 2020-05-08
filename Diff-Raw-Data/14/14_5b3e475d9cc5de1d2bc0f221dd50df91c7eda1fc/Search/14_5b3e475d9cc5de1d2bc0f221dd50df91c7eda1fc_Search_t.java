 package com.zynick.comparison;
 
 import java.io.IOException;
 import java.io.PrintWriter;
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
                 int size = Integer.parseInt(sizeStr);
                 
                 // get result
                 result = site.parse(query, size);
                 
             } catch (Exception ex) {
                 // doesn't matter.
                 System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                StackTraceElement[] st = ex.getStackTrace();
                System.out.println("\t" + st[0]);
                if (st.length > 1) System.out.println("\t" + st[1]);
                if (st.length > 2) System.out.println("\t" + st[2]);
             }
         }
         
         // response back
         response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
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
 
 

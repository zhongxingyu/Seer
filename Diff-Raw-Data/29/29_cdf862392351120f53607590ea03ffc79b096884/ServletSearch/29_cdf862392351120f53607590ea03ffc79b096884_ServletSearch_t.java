 package by.siteload.servlet;
 
 import by.siteload.solr.SearchHelper;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.List;
 
 public class ServletSearch extends HttpServlet {
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
         this.process(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
         this.process(request, response);
     }
 
     private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
         SearchHelper search = new SearchHelper();
 
         response.setStatus(200);
 
         PrintWriter out = response.getWriter();
         response.setContentType("text/plain");
 
         // Get the values of all request parameters
         Enumeration en = request.getParameterNames();
         while (en.hasMoreElements()) {
             // Get the name of the request parameter
             String name = (String) en.nextElement();
 
             // If the request parameter can appear more than once in the query string, get all values
             String[] values = request.getParameterValues(name);
 
             try {
                 if (values.length > 0) {
                     String str = values[0];
                     str = new String(str.getBytes("ISO-8859-1"), "UTF8");
                     out.println(str);
                     List<String> links = search.getResults(str);
                     for (String s : links) {
                         out.println(s);
                     }
                 } else {
                     out.println("No results");
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         out.close();
     }
}

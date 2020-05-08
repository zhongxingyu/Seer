 package com.pcwerk.seck.servlet;
 
 import java.io.*;
 import java.util.List;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import com.pcwerk.seck.indexer.Indexer;
 import com.pcwerk.seck.store.WebDocument;
 
 public class HelloWorld extends HttpServlet {
 
   private static final long serialVersionUID = 1L;
 
   public void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
     
     response.setContentType("text/html");
     
     PrintWriter out = response.getWriter();
         
     String query = request.getParameter("query");
     System.out.println("Getting indexer");
     Indexer indexSearch = new Indexer("indexing");
     System.out.println("Got indexer");
     
     System.out.println("Query is: " + query);
     
     List<WebDocument> resultList =  indexSearch.indexQuerySearch( query );
     
     out.println("<table><tr><th>URL</th><th>score</th></tr>");
     
     for( WebDocument result : resultList) {
         out.println("<tr><td>");
        
         out.println("<a href='" + result.getUrl() + "'>'" + result.getTitle() + "' </a>");
                 
         out.println("</td><td>");
         
         out.println(result.getScore());
         
         out.println("</td></tr>");
     }  
     
     out.println("</table>");
     
    out.println("<a href=seck-web-0.0.6-dev/index.jsp>back to search</a>");
     
    
   }
 }

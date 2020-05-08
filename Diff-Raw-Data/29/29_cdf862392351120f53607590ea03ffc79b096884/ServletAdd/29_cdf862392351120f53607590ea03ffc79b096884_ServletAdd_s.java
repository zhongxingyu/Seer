 package by.siteload.servlet;
 
import by.siteload.solr.SearchHelper;

 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.util.Enumeration;
import java.util.List;
 
 public class ServletAdd extends HttpServlet {
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
         this.process(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
         this.process(request, response);
     }
 
    /*
        generate the page showing all the request parameters
    */
     private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
         File seed = new File("/opt/nutch/urls/seed.txt");
         FileOutputStream outSeed = new FileOutputStream(seed, true);
 
         File filter = new File("/opt/nutch/conf/regex-urlfilter.txt");
         FileOutputStream outFilter = new FileOutputStream(filter, true);
 
         response.setStatus(200);
         response.setContentType("text/plain");
 
         // Get the values of all request parameters
         Enumeration en = request.getParameterNames();
         while (en.hasMoreElements()) {
             String name = (String) en.nextElement();// Get the name of the request parameter
             String[] values = request.getParameterValues(name);// If the request parameter can appear more than once in the query string, get all values
             if (values.length > 0) {
                 outSeed.write(values[0].getBytes());
                 outFilter.write(new String("+^http://([a-z0-9]*\\.)*" + values[0].split("http://")[1]).getBytes());
             }
         }
         outSeed.close();
         outFilter.close();
     }
 }

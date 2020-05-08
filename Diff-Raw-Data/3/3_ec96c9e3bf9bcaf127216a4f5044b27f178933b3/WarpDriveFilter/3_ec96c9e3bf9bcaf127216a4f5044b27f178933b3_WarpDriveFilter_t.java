 /*
    Copyright 2010 Kristian Andersen
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package net.kristianandersen.warpdrive.filter;
 
 import net.kristianandersen.warpdrive.Runtime;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 
 /**
  * Created by IntelliJ IDEA.
  * User: kriand
  * Date: Mar 3, 2010
  * Time: 9:27:22 PM
  * Time: 9:27:22 PM
  */
 public class WarpDriveFilter implements Filter {
 
     private final static String EXPIRES_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
 
     private final static long ONE_YEAR_IN_SECONDS =  31536000L;
 
     public void init(FilterConfig filterConfig) throws ServletException {
 
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         HttpServletRequest req = (HttpServletRequest)request;
         HttpServletResponse resp = (HttpServletResponse)response;
         String oneYearFromNow = getOneYearFromNow();
        //TODO Improve:
        if(req.getRequestURI().substring(0, req.getRequestURI().lastIndexOf('.')).endsWith(Runtime.GZIP_EXTENSION)) {
             resp.setHeader("Content-Encoding", "gzip");
         }
         resp.setHeader("Expires", oneYearFromNow);
         resp.setHeader("Cache-Control", "max-age=" + ONE_YEAR_IN_SECONDS + ";public;must-revalidate;");
         chain.doFilter(req, resp);
     }
 
     private String getOneYearFromNow() {
         Calendar cal = GregorianCalendar.getInstance();
         cal.add(1, Calendar.YEAR);
         SimpleDateFormat sdf = new SimpleDateFormat(EXPIRES_HEADER_FORMAT);
         return sdf.format(cal.getTime());
     }
 
     public void destroy() {
 
     }
 
 }

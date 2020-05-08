 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.web.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.util.StringUtils;
 
 /**
  * Fitler that checks if the Accept header wants RDF content, the request will be forwarded to RDF output page.
  *
  * @author Juhan Voolaid
  */
 public class ContentNegotiationFilter implements Filter {
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(ContentNegotiationFilter.class);
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("utf-8");

         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
         String requestUrl = httpRequest.getRequestURL().toString();
 
         if (!StringUtils.endsWithIgnoreCase(requestUrl, "/rdf")) {
             if (isRdfXmlPreferred(httpRequest)) {
                 String redirectUrl = requestUrl + "/rdf";
                 httpResponse.sendRedirect(redirectUrl);
                 return;
             }
         }
 
         chain.doFilter(request, response);
     }
 
     @Override
     public void destroy() {
     }
 
     /**
      * Returns true, if request wants RDF content.
      *
      * @param httpRequest
      * @return
      */
     private boolean isRdfXmlPreferred(HttpServletRequest httpRequest) {
         String acceptHeader = httpRequest.getHeader("Accept");
         return acceptHeader != null && acceptHeader.trim().toLowerCase().startsWith("application/rdf+xml");
     }
 }

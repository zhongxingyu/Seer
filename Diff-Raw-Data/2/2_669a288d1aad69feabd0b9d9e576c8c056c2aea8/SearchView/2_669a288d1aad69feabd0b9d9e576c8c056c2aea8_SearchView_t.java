 package org.paxle.gui.impl;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 
 public class SearchView extends AServlet {
 
     protected Log logger = LogFactory.getLog(SearchView.class);
     
     public SearchView(ServiceManager manager) {
         super(manager);
     }
     
     public Template handleRequest( HttpServletRequest request,
                                    HttpServletResponse response,
                                    Context context ) {
         
         Template template = null;
 
         try {
            if (request.getParameter("query") != null && !request.getParameter("query").equals("")) {
                 context.put("searchQuery", request.getParameter("query"));
             }
             context.put("manager", this.manager);
             template = this.getTemplate("/resources/templates/SearchView.vm");
             
         } catch (Exception e) {
             // TODO Auto-generated catch block
             this.logger.error("Error",e);
         }
         return template;
     }
 }

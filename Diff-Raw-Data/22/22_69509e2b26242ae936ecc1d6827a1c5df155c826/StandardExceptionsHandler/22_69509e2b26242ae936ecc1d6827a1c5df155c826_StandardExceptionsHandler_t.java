 package com.redshape.servlet.actions.exceptions.handling;
 
 import com.redshape.servlet.actions.exceptions.PageAccessDeniedException;
 import com.redshape.servlet.actions.exceptions.PageAuthRequiredException;
 import com.redshape.servlet.actions.exceptions.PageNotFoundException;
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.core.IHttpResponse;
 import com.redshape.servlet.core.controllers.ProcessingException;
import com.redshape.servlet.views.ViewHelper;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 
 /**
  * @package com.redshape.servlet.actions.exceptions.handling
  * @user cyril
  * @date 6/20/11 10:57 PM
  */
 public class StandardExceptionsHandler extends AbstractPageExceptionHandler {
     private static final Logger log = Logger.getLogger(StandardExceptionsHandler.class);
 
     private String page404 = "/errors/404.html";
     private String page403 = "/errors/403.html";
     private String page401 = "/errors/401.html";
     private String page500 = "/errors/500.html";
 
     public StandardExceptionsHandler() {
         super();
     }
 
     public String getPage500() {
         return page500;
     }
 
     public void setPage500(String page500) {
         this.page500 = page500;
     }
 
     public String getPage404() {
         return page404;
     }
 
     public void setPage404(String page404) {
         this.page404 = page404;
     }
 
     public String getPage403() {
         return page403;
     }
 
     public void setPage403(String page403) {
         this.page403 = page403;
     }
 
     public String getPage401() {
         return page401;
     }
 
     public void setPage401(String page401) {
         this.page401 = page401;
     }
 
     @Override
     public void handleException(PageNotFoundException e, IHttpRequest request, IHttpResponse response) {
         try {
             log.info("Error message (404):");
             log.info( e.getMessage(), e );
 
             this.sendRedirect( request, response, this.getPage404() );
         } catch ( IOException ex ) {
             log.error( ex.getMessage(), ex );
         }
     }
 
     @Override
     public void handleException(PageAuthRequiredException e, IHttpRequest request, IHttpResponse response) {
         try {
             log.info("Error message (401):");
             log.info( e.getMessage(), e );
 
             this.sendRedirect( request, response, this.getPage401() );
         } catch ( IOException ex ) {
             log.error( ex.getMessage(), ex );
         }
     }
 
     @Override
     public void handleException(PageAccessDeniedException e, IHttpRequest request, IHttpResponse response) {
         try {
             log.info("Error message (403):");
             log.info( e.getMessage(), e );
 
             this.sendRedirect( request, response, this.getPage403() );
         } catch ( IOException ex ) {
             log.error( ex.getMessage(), ex );
         }
     }
 
     @Override
     protected void unknownExceptionHandler(ProcessingException e, IHttpRequest request, IHttpResponse response) {
         try {
             log.error("Error message (500):");
             log.error( e.getMessage(), e );
 
             this.sendRedirect( request, response, this.getPage500() );
         } catch ( IOException ex ) {
             log.error( ex.getMessage(), ex );
         }
     }
 
 	protected void sendRedirect( IHttpRequest request, IHttpResponse response, String path )
 		throws IOException {
		response.sendRedirect( ViewHelper.url(path) );
 	}
 }

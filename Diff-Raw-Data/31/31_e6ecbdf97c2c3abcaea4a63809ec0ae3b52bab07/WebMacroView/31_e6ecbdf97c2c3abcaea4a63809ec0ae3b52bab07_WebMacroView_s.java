 package org.webmacro.adapter.spring;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.context.ApplicationContextException;
 import org.springframework.web.servlet.view.AbstractTemplateView;
 import org.webmacro.ResourceException;
 import org.webmacro.Template;
 import org.webmacro.WebMacro;
 import org.webmacro.servlet.WebContext;
 
 /**
  * Author: mpalmer
  */
 public class WebMacroView extends AbstractTemplateView
 {
 
     private WebMacro webMacro;
 
     /**
      * Verify that the template specified exists
      */
     void checkTemplate()
     {
         try
         {
             // This is arguably redundant and non-performant as the template could disappear
             // during execution
             webMacro.getTemplate(getUrl());
         }
         catch (ResourceException re)
         {
             throw new ApplicationContextException("Cannot find WebMacro template for URL [" + getUrl() + "]", re);
         }
     }
 
     public WebMacro getWebMacro()
     {
         return webMacro;
     }
 
     public void setWebMacro(WebMacro webMacro)
     {
         this.webMacro = webMacro;
     }
 
     /**
      * Render the template.
      *
      * TODO Add code here to put in standard context variables used for Spring form stuff
      * 
      * @param model
      * @param request
      * @param response
      * @throws Exception
      */
     protected void renderMergedTemplateModel(Map model,
                                              HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         WebContext context = webMacro.getWebContext(request, response);
 
         context.putAll(model);
 
         Template template = webMacro.getTemplate(getUrl());
 
        // @todo Need handling for template encodings here
         template.write(response.getOutputStream(), context);
     }
 
 
 }

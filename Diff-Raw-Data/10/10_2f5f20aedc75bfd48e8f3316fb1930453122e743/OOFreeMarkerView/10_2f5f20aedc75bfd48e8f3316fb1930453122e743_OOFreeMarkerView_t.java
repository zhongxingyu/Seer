 package org.otherobjects.cms.views;
 
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.binding.OORequestContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.support.RequestContextUtils;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
 
 import freemarker.template.TemplateException;
 
 /**
  * Adds error handling to the standard FreeMarkerView.
  * 
  * @author rich
  */
 public class OOFreeMarkerView extends FreeMarkerView
 {
     private final Logger logger = LoggerFactory.getLogger(OOFreeMarkerView.class);
     
    private static final String DEFAULT_ERROR_TEMPLATE_PATH = "/site/templates/pages/500.ftl";
     private static final String DEFAULT_EXCEPTION_ATTRIBUTE = "exception";
 
     @SuppressWarnings("unchecked")
     @Override
     protected void doRender(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         try
         {
             model.put(SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE, new OORequestContext(request, getServletContext(), model));
             super.doRender(model, request, response);
         }
         catch (Exception e)
         {
             // Render error page on exception
             logger.error("Error whilst rendering view.", e);
             if(e instanceof TemplateException)
             {
                 TemplateException te = (TemplateException)e;
                 System.out.println(te.getFTLInstructionStack());
             }
             response.reset();
             model.put(DEFAULT_EXCEPTION_ATTRIBUTE, e);
             Locale locale = RequestContextUtils.getLocale(request);
             processTemplate(getTemplate(DEFAULT_ERROR_TEMPLATE_PATH, locale), model, response);
         }
     }
     
 //  @SuppressWarnings("unchecked")
 //  //@Override
 //  protected void xprocessTemplate(Template template, Map model, HttpServletResponse response) throws IOException, TemplateException
 //  {
 //      /* Write via StringWriter so that if an exception
 //       * is thrown during rendering we have not already
 //       * sent a response to the browser. This allows us
 //       * to reliably send an error page.
 //       */
 //      StringWriter writer = new StringWriter();
 //      template.process(model, writer);
 //      //FIXME Test performance of StringWriter in this example
 //      response.getWriter().write(writer.toString());
 //  }  
 }

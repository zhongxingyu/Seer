 package org.otherobjects.cms.views;
 
 import java.io.IOException;
 
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
 
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateExceptionHandler;
 
 /**
  * Custom FreeMarker Configuration Bean.
  * 
  * @author rich
  *
  */
 public class OOFreeMarkerConfigurationFactoryBean extends FreeMarkerConfigurationFactoryBean
 {
     private final Logger logger = LoggerFactory.getLogger(OOFreeMarkerConfigurationFactoryBean.class);
 
     
     private OtherObjectsConfigurator otherObjectsConfigurator;
     
     @Override
     protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException
     {
         if(otherObjectsConfigurator.getEnvironmentName().equals("dev"))
         {
             logger.info("Configuring Freemarker for development.");
             config.setSetting("template_update_delay", "0"); // Don't cache templates            
             config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER); // Print error to page
         }
         else
         {
             logger.info("Configuring Freemarker for production.");
             config.setSetting("template_update_delay", "600"); // Cache templates for 5 mins            
             config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER); // TODO Throw error 
             
         }
     }
 
     public void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 }

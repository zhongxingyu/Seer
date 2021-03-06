 package com.qcadoo.mes;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.springframework.core.io.Resource;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.qcadoo.mes.core.data.internal.DataAccessServiceImpl;
 
 public class PluginResourcesResolver implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
 
     private static final Logger LOG = LoggerFactory.getLogger(DataAccessServiceImpl.class);
 
     private ApplicationContext applicationContext;
 
     private ServletContext servletContext;
 
     private String webappPath;
 
     @Override
     public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
         servletContext = ((WebApplicationContext) applicationContext).getServletContext();
         webappPath = servletContext.getRealPath("/");
     }
 
     @Override
     public void onApplicationEvent(final ContextRefreshedEvent event) {
         copyResources("js", "js");
         copyResources("css", "css");
         copyResources("img", "img");
         copyResources("jsp", "WEB-INF/jsp");
     }
 
     private void copyResources(final String type, final String targetPath) {
         LOG.info("Copying resources " + type + " ...");
 
         try {
             Resource[] resources = applicationContext.getResources("classpath*:META-INF/" + type + "/**/*");
 
             for (Resource resource : resources) {
                 copyResource(resource, type, targetPath);
             }
 
         } catch (IOException e) {
             throw new IllegalStateException("cannot copy resources: " + type, e);
         }
     }
 
     private void copyResource(final Resource resource, final String type, final String targetPath) throws IOException {
         if (!resource.isReadable()) {
             return;
         }
 
         String path = resource.getURI().toString().split("META-INF/" + type)[1];
 
         File file = new File(webappPath + "/" + targetPath + path);
 
         if (resource.getInputStream().available() == 0) {
             file.mkdirs();
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Copying " + path + " to " + file.getAbsolutePath());
             }
 
            OutputStream output = null;
 
            try {
                output = new BufferedOutputStream(new FileOutputStream(file));
                IOUtils.copy(resource.getInputStream(), output);
            } finally {
                IOUtils.closeQuietly(output);
            }
         }
     }
 }

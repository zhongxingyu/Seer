 /**
  * @author <a href="mailto:kisg@mailbox.hu">Gergely Kis</a>
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: PortalConfigServiceImpl.java 5032 2006-08-17 18:15:06Z novotny $
  */
 package org.gridsphere.services.core.portal.impl;
 
 import org.gridsphere.portlet.PortletLog;
 import org.gridsphere.portlet.impl.SportletLog;
 import org.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridsphere.services.core.portal.PortalConfigService;
 
 import java.util.Properties;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 
 /**
  * Portal configuration service is used to manage portal administrative settings
  */
 public class PortalConfigServiceImpl implements PortletServiceProvider, PortalConfigService {
 
     private PortletLog log = SportletLog.getInstance(PortalConfigServiceImpl.class);
 
 
     private String GRIDSPHERE_PROPERTIES = "/WEB-INF/CustomPortal/portal/gridsphere.properties";
 
    private FileOutputStream propertiesOutputStream = null;
     protected Properties props = null;
 
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         InputStream propsStream = config.getServletContext().getResourceAsStream(GRIDSPHERE_PROPERTIES);
         try {
            propertiesOutputStream = new FileOutputStream(config.getServletContext().getRealPath(GRIDSPHERE_PROPERTIES));
             props = new Properties();
             props.load(propsStream);
             props.store(propertiesOutputStream, "GridSphere Portal Properties");
         } catch (FileNotFoundException e) {
             log.error("Unable to find gridsphere.properties", e);
         } catch (IOException e) {
             log.error("Unable to load gridsphere.properties", e);
         }
     }
 
     public void destroy() {
 
     }
 
     public String getProperty(String key) {
         if (key == null) throw new IllegalArgumentException("property key cannot be null!");
         return props.getProperty(key);
     }
 
     public void setProperty(String key, String value) {
         if (key == null) throw new IllegalArgumentException("property key cannot be null!");
         if (value == null) throw new IllegalArgumentException("property value cannot be null!");
         props.setProperty(key, value);
     }
 
     public void storeProperties() throws IOException {
         props.store(propertiesOutputStream, "GridSphere Portal Properties");
     }
 
 }

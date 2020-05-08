 package gov.usgs.service;
 
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author isuftin
  */
 public class PropertiesServlet extends HttpServlet {
     private static final org.slf4j.Logger log = LoggerFactory.getLogger(PropertiesServlet.class);
     private static final long serialVersionUID = 1L;
     private String configPath = "";
     private String configFilePath = "";
     private Properties defaultProperties = null;
 
     @Override
     public void init(ServletConfig config) throws ServletException
     {
         super.init(config);
         Properties sysProps = System.getProperties();
         String fileSeperator = sysProps.getProperty("file.separator");
         String  catalinaBase = sysProps.getProperty("catalina.base");
         String contextPath = config.getServletContext().getContextPath();
         String webappName = contextPath.substring(contextPath.lastIndexOf("/") + 1);
 
         // First let's get a handle to the config dir we're going to use
         configPath = new StringBuilder(catalinaBase)
                 .append(fileSeperator)
                 .append("content")
                 .append(fileSeperator)
                 .append(webappName)
                 .toString();
 
         // Make sure the directory exists (this won't recreate it if it does exist)
         FileHelper.createDir(new File(configPath));
 
         // Let's see if we can find the configuration file
         configFilePath = new StringBuilder(configPath)
                 .append(fileSeperator)
                 .append("config.xml")
                 .toString();
 
         defaultProperties = new Properties();
         {   // Check http://privusgs2.er.usgs.gov/display/GDP/Configuring+front-end+through+config.jsp for definitions of these properties
             defaultProperties.setProperty("config.csw.cache.getcaps", "1");
             
             // Endpoint configuration
             defaultProperties.setProperty("endpoint.csw", "http://igsarm-cida-gdp2.er.usgs.gov:8081/geonetwork/srv/en/csw");
             defaultProperties.setProperty("endpoint.wms", "http://localhost:8081/geoserver/wms");
             defaultProperties.setProperty("endpoint.utilitywps", "http://localhost:8080/gdp-utility-wps/WebProcessingService");
             defaultProperties.setProperty("endpoint.processwps", "http://localhost:8080/gdp-process-wps/WebProcessingService");
             defaultProperties.setProperty("endpoint.statuswps", "http://localhost:8080/gdp-process-wps/RetrieveResultServlet");
             defaultProperties.setProperty("endpoint.geoserver", "http://localhost:8081/geoserver");
             defaultProperties.setProperty("endpoint.proxy", "proxy/");
             defaultProperties.setProperty("endpoint.retrieveprocinfo", "getprocinfo");
 
             // OpenLayers Mapping Properties
             defaultProperties.setProperty("map.default_layer", "street_map");
             defaultProperties.setProperty("map.layers.tile.street_map",
                     "Street Map,http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer/tile?layers=0&isBaseLayer=true");
             defaultProperties.setProperty("map.layers.tile.shaded_relief",
                     "Shaded Relief,http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_ShadedRelief_World_2D/MapServer/tile?layers=0&isBaseLayer=true");
             defaultProperties.setProperty("map.layers.wms.blue_marble",
                     "Blue Marble,http://maps.opengeo.org/geowebcache/service/wms?layers=bluemarble&isBaseLayer=true");
             defaultProperties.setProperty("map.layers.wms.naip",
                     "NAIP,http://isse.cr.usgs.gov/ArcGIS/services/Combined/SDDS_Imagery/MapServer/WMSServer?layers=0&isBaseLayer=true");
 
             // GUI configuration
             defaultProperties.setProperty("ui.default_csw_search", "");
             defaultProperties.setProperty("ui.default_dataset_url", "");
             defaultProperties.setProperty("ui.max_upload_size", "16777216");
             defaultProperties.setProperty("ui.shapefile_downloading_allow", "1");
             defaultProperties.setProperty("ui.shapefile_downloading_maxfeatures", "");
             defaultProperties.setProperty("ui.tip.fadeout.timeout", "2000");
             defaultProperties.setProperty("ui.view_algorithm_list", "");
             defaultProperties.setProperty("ui.view_max_polygons_shown", "1000");
             defaultProperties.setProperty("ui.view_popup_info", "0");
             defaultProperties.setProperty("ui.view_popup_info_txt", "<h1>Info Popup</h1><br /><h3>Some Text Here</h3>");
             defaultProperties.setProperty("ui.view_show_beyond_max_polygons", "0");
             defaultProperties.setProperty("ui.view_show_csw_chosen_dataset_title", "1");
             defaultProperties.setProperty("ui.view_show_csw_dataset_url", "1");
             defaultProperties.setProperty("ui.view_show_csw_dialog", "1");
             defaultProperties.setProperty("ui.view_show_csw_url_input", "1");
             defaultProperties.setProperty("ui.view_show_service_draw_feature", "1");
             defaultProperties.setProperty("ui.view_show_service_upload", "1");
             defaultProperties.setProperty("ui.view_show_service_waters", "1");
             defaultProperties.setProperty("ui.view_simple_csw_client", "0");
             defaultProperties.setProperty("ui.view_submit_dialog_info", "ui.view_submit_dialog_info needs to be configured");
         }
         writeDefaultProperties();
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      * @param request
      * @param response
      * @requestParam request servlet request
      * @requestParam response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws FileNotFoundException
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, FileNotFoundException, IOException {
         String req = request.getParameter("command");
         if (req == null || "".equals(req) || "getprops".equals(req)) {
             sendPropertiesXML(response);
         } else if ("setprops".equals(req)) {
             setPropertiesXML(request);
         }
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         doGet(request, response);
     }
 
     private void sendPropertiesXML(HttpServletResponse response) {
         FileInputStream input = null;
         BufferedInputStream buf = null;
         try {
             File xml = new File(configFilePath);
             if (!xml.exists()) {
                 writeDefaultProperties();
             }
             input = new FileInputStream(xml);
             buf = new BufferedInputStream(input);
             response.setContentType("text/xml");
             response.setContentLength((int) xml.length());
             ServletOutputStream stream = response.getOutputStream();
             int readBytes = 0;
             while ((readBytes = buf.read()) != -1) {
                 stream.write(readBytes);
             }
         } catch (IOException ex) {
             Logger.getLogger(PropertiesServlet.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if (input != null) { input.close(); }
                 if (buf != null) { buf.close(); }
             } catch (IOException ignore) {}
         }
     }
 
     private void writePropertiesXML(Properties props) {
         Properties localProperties = (props == null) ? defaultProperties : props;
         FileOutputStream file = null;
         try {
             file = new FileOutputStream(new File(configFilePath));
             localProperties.storeToXML(file, null);
             log.debug("Properties written to: " + configFilePath);
         } catch (IOException ex) {
             Logger.getLogger(PropertiesServlet.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try { if (file != null) { file.close(); }} catch (IOException ignore) {}
         }
     }
 
     private void setPropertiesXML(HttpServletRequest request) {
         Enumeration<String> params = request.getParameterNames();
         Properties props = loadPropertiesXML();
         
         while(params.hasMoreElements()) {
             String requestParam = params.nextElement();
             if ("command".equals(requestParam)) {
                 continue;
             }
 
             String key = requestParam;
             String val = request.getParameter(requestParam).split(";")[0];
             boolean delete = (request.getParameter(requestParam).contains(";")) ? Boolean.parseBoolean(request.getParameter(requestParam).split(";")[1]) : false;
 
             if (delete) {
                 props.remove(key);
             }
             else {
                 props.setProperty(key, val);
             }
         }
         writePropertiesXML(props);
     }
 
     private Properties loadPropertiesXML() {
         FileInputStream in = null;
         Properties props = null;
         try {
             in = new FileInputStream(new File(configFilePath));
             props = new Properties();
             props.loadFromXML(in);
         } catch (IOException ex) {
             Logger.getLogger(PropertiesServlet.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             IOUtils.closeQuietly(in);
         }
         return props;
     }
 
     private void writeDefaultProperties() {
         // First we get the current properties, if any
         Properties currentProperties = loadPropertiesXML();
 
         // Copy the current properties into the default properties map.  This will
         // overwrite any of the default properties with the actual property if the
         // key exists in the current properties
         if (currentProperties != null) {
             defaultProperties.putAll(currentProperties);
         }
 
         // Write out the copied list
         writePropertiesXML(defaultProperties);
     }
 }

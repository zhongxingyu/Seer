 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.openlayers;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 
 
 public class OpenLayersMapProducer implements GetMapProducer {
     /**
      * static freemaker configuration
      */
     static Configuration cfg;
 
     static {
         cfg = new Configuration();
         cfg.setClassForTemplateLoading(OpenLayersMapProducer.class, "");
     }
 
     /**
      * wms configuration
      */
     WMS wms;
 
     /**
      * The current template
      */
     Template template;
 
     /**
      * The current map context
      */
     WMSMapContext mapContext;
 
     public OpenLayersMapProducer(WMS wms) {
         this.wms = wms;
     }
 
     public void abort() {
         mapContext = null;
         template = null;
     }
 
     public String getContentDisposition() {
         return null;
     }
 
     public String getContentType() throws IllegalStateException {
         return "text/html";
     }
 
     public void produceMap(WMSMapContext map) throws WmsException {
         mapContext = map;
     }
 
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         try {
             //create the template
             Template template = cfg.getTemplate("OpenLayersMapTemplate.ftl");
             HashMap map = new HashMap();
             map.put("context", mapContext);
             map.put("request", mapContext.getRequest());
             map.put("maxResolution", new Double(getMaxResolution(mapContext.getAreaOfInterest())));
             template.process(map, new OutputStreamWriter(out));
         } catch (TemplateException e) {
             throw new WmsException(e);
         }
 
         mapContext = null;
         template = null;
     }
 
     private double getMaxResolution(ReferencedEnvelope areaOfInterest) {
         double w = areaOfInterest.getWidth();
         double h = areaOfInterest.getHeight();
 
         return ((w > h) ? w : h) / 256;
     }
 }

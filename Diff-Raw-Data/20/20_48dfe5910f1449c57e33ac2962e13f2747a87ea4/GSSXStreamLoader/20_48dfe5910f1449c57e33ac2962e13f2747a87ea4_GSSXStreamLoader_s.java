 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.gss;
 
 import java.util.ArrayList;
 
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.util.XStreamServiceLoader;
 import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.wfs.WFSInfoImpl;
 import org.geotools.util.Version;
 
 /**
  * Loads GSS configuration from the disk
  * 
  * @author aaime
  */
 public class GSSXStreamLoader extends XStreamServiceLoader<GSSInfo> {
 
     public GSSXStreamLoader(GeoServerResourceLoader resourceLoader) {
         super(resourceLoader, "gss");
     }
 
     @Override
     protected GSSInfo createServiceFromScratch(GeoServer gs) {
         return new GSSInfoImpl();
     }
 
     public Class<GSSInfo> getServiceClass() {
         return GSSInfo.class;
     }
 
     @Override
     protected GSSInfo initialize(GSSInfo service) {
         if (service.getVersions() == null) {
            ((WFSInfoImpl) service).setVersions(new ArrayList());
         }
         if (service.getVersions().isEmpty()) {
             service.getVersions().add(new Version("1.0.0"));
         }
         return service;
     }
 }

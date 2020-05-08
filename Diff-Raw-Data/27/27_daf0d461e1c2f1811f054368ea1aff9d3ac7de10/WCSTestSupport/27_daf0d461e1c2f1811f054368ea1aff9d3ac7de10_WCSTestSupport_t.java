 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wcs.test;
 
 import javax.xml.namespace.QName;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.test.ows.KvpRequestReaderTestSupport;
 import org.vfny.geoserver.global.WCS;
 
 /**
  * Base support class for wcs tests.
  * 
  * @author Andrea Aime, TOPP
  * 
  */
 public class WCSTestSupport extends KvpRequestReaderTestSupport {
     protected static final String BASEPATH = "wcs";
 
     public static String WCS_PREFIX = "wcs";
 
     public static String WCS_URI = "http://www.opengis.net/wcs/1.1.1";
 
     public static String TIFF = "tiff";
 
     public static QName TASMANIA_DEM = new QName(WCS_URI, "DEM", WCS_PREFIX);
 
     public static QName TASMANIA_BM = new QName(WCS_URI, "BlueMarble", WCS_PREFIX);
     
     public static QName ROTATED_CAD = new QName(WCS_URI, "RotatedCad", WCS_PREFIX);
 
     /**
      * @return The global wfs instance from the application context.
      */
     protected WCS getWCS() {
         return (WCS) applicationContext.getBean("wcs");
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         org.geoserver.ows.util.RequestUtils.setForcedBaseUrl("");
     }
 
     @Override
     protected void populateDataDirectory(MockData dataDirectory) throws Exception {
         dataDirectory.addCoverage(TASMANIA_DEM, WCSTestSupport.class.getResource("tazdem.tiff"),
                 TIFF, null);
         dataDirectory.addCoverage(TASMANIA_BM, WCSTestSupport.class.getResource("tazbm.tiff"),
                 TIFF, null);
         // hum, the tiff reader does not seem to be able to load this one
         dataDirectory.addCoverage(ROTATED_CAD, WCSTestSupport.class.getResource("rotated.tiff"),
                 TIFF, null);
     }
 }

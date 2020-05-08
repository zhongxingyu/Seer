 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
import org.vfny.geoserver.config.WMSConfig;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.GetMapProducerFactorySpi;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * KMZMapProducerFactory
  * This class is used as part of the SPI auto discovery process which enables
  * new format producers to be plugged in.
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $
  * @version $Id$
  */
 public class KMZMapProducerFactory implements GetMapProducerFactorySpi {
     /**
      * this is just to check the requested mime type starts with this string,
      * since the most common error when performing the HTTP request is not to
      * escape the '+' sign in "kml+xml", which is decoded as a space
      * character at server side.
      */
     private static final String PRODUCE_TYPE = "kmz";
 
     /**
      * Official KMZ mime type
      */
     static final String MIME_TYPE = "application/vnd.google-earth.kmz";
 
     /** Set of supported mime types for the producers made by this Factory
      */
     private static final Set SUPPORTED_FORMATS = Collections.singleton(MIME_TYPE);
 
     /**
      * Creates a new KMZMapProducerFactory object.
      */
     public KMZMapProducerFactory() {
         super();
     }
 
     /**
      * Human readable description of output format.
      */
     public String getName() {
         return "Keyhole markup language compressed producer";
     }
 
     /**
      * Discover what output formats are supported by the producers made by this factory.
      *
      * @return Set of supported mime types
      */
     public Set getSupportedFormats() {
         return SUPPORTED_FORMATS;
     }
 
     /**
      * Reports on the availability of this factory.  As no external libraries are
      * required for KMZ this should always be true.
      *
      * @return <code>true</code>
      */
     public boolean isAvailable() {
         return true;
     }
 
     /**
      * evaluates if this Map producer can generate the map format specified by
      * <code>mapFormat</code>
      *
      * @param mapFormat the mime type of the output map format requiered
      *
      * @return true if class can produce a map in the passed format.
      */
     public boolean canProduce(String mapFormat) {
         return (mapFormat != null)
         && (mapFormat.startsWith(PRODUCE_TYPE) // "KMZ"
         || mapFormat.startsWith("application/vnd.google-earth.kmz"));
     }
 
     /**
      * Create an actual instance of a KMZMapProducer.
      *
      * @param mapFormat String which MUST match the supported formats.  Call
      * canProcess fisrt if you are unsure.
      *
      * @return GetMapProducer instance.
      *
      * @throws IllegalArgumentException DOCUMENT ME!
      */
     public GetMapProducer createMapProducer(String mapFormat, WMS wms)
         throws IllegalArgumentException {
         return new KMZMapProducer(wms);
     }
 
     /* (non-Javadoc)
      * @see org.geotools.factory.Factory#getImplementationHints()
      * This just returns java.util.Collections.EMPTY_MAP
      */
     public Map getImplementationHints() {
         return java.util.Collections.EMPTY_MAP;
     }
 }

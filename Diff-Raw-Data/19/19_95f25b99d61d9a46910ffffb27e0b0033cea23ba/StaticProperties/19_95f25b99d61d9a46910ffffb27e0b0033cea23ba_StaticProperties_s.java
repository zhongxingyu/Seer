 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.wunda_blau.res;
 
 import de.cismet.tools.PropertyReader;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public final class StaticProperties {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final PropertyReader propReader;
     private static final String PROPERTY_FILE_URL = "/de/cismet/cids/custom/wunda_blau/res/urlconfig.properties";
     // --------------
     public static final String ARCHIVAR_URL_PREFIX;
     public static final String ARCHIVAR_URL_SUFFIX;
     public static final String POI_SIGNATUR_URL_PREFIX;
     public static final String POI_SIGNATUR_URL_SUFFIX;
     public static final String POI_SIGNATUR_DEFAULT_ICON;
     public static final String POI_LOCATIONTYPE_URL_PREFIX;
     public static final String POI_LOCATIONTYPE_URL_SUFFIX;
     public static final String ALB_PLAN_URL_PREFIX;
     public static final String ALB_TEXTBLATT_URL_PREFIX;
     public static final String FORTFUEHRUNGSNACHWEISE_URL_PREFIX;
 
     // --------------
     static {
         propReader = new PropertyReader(PROPERTY_FILE_URL);
         ARCHIVAR_URL_PREFIX = propReader.getProperty("archivar_url_prefix");
         ARCHIVAR_URL_SUFFIX = propReader.getProperty("archivar_url_suffix");
         POI_SIGNATUR_URL_PREFIX = propReader.getProperty("poi_signatur_url_prefix");
         POI_SIGNATUR_URL_SUFFIX = propReader.getProperty("poi_signatur_url_suffix");
         POI_SIGNATUR_DEFAULT_ICON = propReader.getProperty("poi_signatur_default_icon");
         POI_LOCATIONTYPE_URL_PREFIX = propReader.getProperty("poi_locationtype_url_prefix");
         POI_LOCATIONTYPE_URL_SUFFIX = propReader.getProperty("poi_locationtype_url_suffix");
         ALB_PLAN_URL_PREFIX = propReader.getProperty("alb_plan_url_prefix");
         ALB_TEXTBLATT_URL_PREFIX = propReader.getProperty("alb_textblatt_url_prefix");
         FORTFUEHRUNGSNACHWEISE_URL_PREFIX = propReader.getProperty("fortfuehrungsnachweise_url_prefix");
     }
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new StaticProperties object.
      *
      * @throws  AssertionError  DOCUMENT ME!
      */
     private StaticProperties() {
         throw new AssertionError("Epic fail.");
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   in  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getProperty(final String in) {
         return propReader.getProperty(in);
     }
 }

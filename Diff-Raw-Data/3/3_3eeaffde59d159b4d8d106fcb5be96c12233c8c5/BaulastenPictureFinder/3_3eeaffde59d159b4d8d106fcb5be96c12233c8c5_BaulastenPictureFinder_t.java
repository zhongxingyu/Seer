 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.utils;
 
 import java.io.File;
 
 import java.net.URL;
 
 import java.util.List;
 
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public final class BaulastenPictureFinder {
 
     //~ Static fields/initializers ---------------------------------------------
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaulastenPictureFinder.class);
     private static final String[] SUFFIXE = new String[] { "tif", "jpg", "tiff", "jpeg" };
 //    "TIF", "JPG", "TIFF", "JPEG"};
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   picture  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static List<File> findPlanPicture(final String picture) {
         final String picturePath = StaticProperties.ALB_PLAN_URL_PREFIX + picture + ".";
        log.debug("findPlanPicture: "+picturePath);
         return probeForRightSuffix(picturePath);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   picture  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static List<File> findTextblattPicture(final String picture) {
         final String picturePath = StaticProperties.ALB_TEXTBLATT_URL_PREFIX + picture + ".";
        log.debug("findTextblattPicture: "+picturePath);
         return probeForRightSuffix(picturePath);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fileWithoutSuffix  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static List<File> probeForRightSuffix(final String fileWithoutSuffix) {
         if (log.isDebugEnabled()) {
             log.debug("Searching for picture: " + fileWithoutSuffix + "xxx");
         }
         final List<File> results = TypeSafeCollections.newArrayList();
         for (final String suffix : SUFFIXE) {
             try {
                 final URL fileURL = new URL(fileWithoutSuffix + suffix);
                 final File testFile = new File(fileURL.toURI());
                 if (testFile.isFile()) {
                     if (log.isDebugEnabled()) {
                         log.debug("Found picture in file: " + testFile.getAbsolutePath());
                     }
                     results.add(testFile);
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
         if (log.isDebugEnabled()) {
             log.debug("No picture file found.");
         }
         return results;
     }
 }

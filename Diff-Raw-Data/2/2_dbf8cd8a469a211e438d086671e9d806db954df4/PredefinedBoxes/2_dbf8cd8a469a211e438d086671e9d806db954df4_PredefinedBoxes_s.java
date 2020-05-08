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
 package de.cismet.cids.custom.butler;
 
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.Properties;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class PredefinedBoxes {
 
     //~ Static fields/initializers ---------------------------------------------
 
     protected static final ArrayList<PredefinedBoxes> butler1Boxes = new ArrayList<PredefinedBoxes>();
     protected static final ArrayList<PredefinedBoxes> butler2Boxes = new ArrayList<PredefinedBoxes>();
     private static final Logger LOG = Logger.getLogger(PredefinedBoxes.class);
 
     static {
         Properties prop = new Properties();
         try {
             prop.load(PredefinedBoxes.class.getResourceAsStream("butler1Boxes.properties"));
             loadPropertiesIntoList(butler1Boxes, prop);
 
             prop = new Properties();
             prop.load(PredefinedBoxes.class.getResourceAsStream("butler2Boxes.properties"));
             loadPropertiesIntoList(butler2Boxes, prop);
         } catch (IOException ex) {
             LOG.error("Could not read property file with defined boxes for butler 1", ex);
         }
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private final String displayName;
     private double eSize;
     private final double nSize;
     private String key;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new PredefinedBoxes object.
      *
      * @param  displayName  DOCUMENT ME!
      * @param  eSize        DOCUMENT ME!
      * @param  nSize        DOCUMENT ME!
      */
     private PredefinedBoxes(final String displayName, final double eSize, final double nSize) {
         this.displayName = displayName;
         this.eSize = eSize;
         this.nSize = nSize;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDisplayName() {
         return displayName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public double getEastSize() {
         return eSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public double getNorthSize() {
         return nSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getKey() {
         return key;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  key  DOCUMENT ME!
      */
     public void setKey(final String key) {
         this.key = key;
     }
 
     @Override
     public String toString() {
         return displayName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  list  DOCUMENT ME!
      * @param  prop  DOCUMENT ME!
      */
     private static void loadPropertiesIntoList(final ArrayList list, final Properties prop) {
         final Enumeration keys = prop.propertyNames();
         final ArrayList<String> keyList = new ArrayList<String>();
         while (keys.hasMoreElements()) {
             final String key = (String)keys.nextElement();
             keyList.add(key);
         }
         final Comparator<String> keyComp = new Comparator<String>() {
 
                 @Override
                 public int compare(final String o1, final String o2) {
                     final int number1 = Integer.parseInt(o1.replaceAll("box", ""));
                     final int number2 = Integer.parseInt(o2.replaceAll("box", ""));
                    return Integer.compare(number1, number2);
                 }
             };
         Collections.sort(keyList, keyComp);
         for (final String key : keyList) {
             final String[] splittedVal = ((String)prop.getProperty(key)).split(";");
             final double width = Double.parseDouble(splittedVal[1]);
             final double height = Double.parseDouble(splittedVal[2]);
             final PredefinedBoxes box = new PredefinedBoxes(
                     splittedVal[0],
                     width,
                     height);
             if (splittedVal.length == 4) {
                 box.setKey(splittedVal[3]);
             }
             list.add(box);
         }
     }
 }

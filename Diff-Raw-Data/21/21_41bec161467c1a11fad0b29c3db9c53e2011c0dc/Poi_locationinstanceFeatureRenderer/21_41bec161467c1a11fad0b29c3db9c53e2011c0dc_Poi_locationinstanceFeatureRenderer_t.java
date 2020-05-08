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
 package de.cismet.cids.custom.featurerenderer.wunda_blau;
 
 import java.net.URL;
 
 import javax.swing.JComponent;
 
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class Poi_locationinstanceFeatureRenderer extends CustomCidsFeatureRenderer {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             Poi_locationinstanceFeatureRenderer.class);
    private static final FeatureAnnotationSymbol DEFAULT_SYMBOL;
 
    static {
         DEFAULT_SYMBOL = getSymbolFromResourceString(StaticProperties.POI_SIGNATUR_DEFAULT_ICON);
         if (DEFAULT_SYMBOL != null) {
             DEFAULT_SYMBOL.setSweetSpotX(0.5d);
             DEFAULT_SYMBOL.setSweetSpotY(0.5d);
         }
     }
 
    //~ Instance fields --------------------------------------------------------

    private FeatureAnnotationSymbol symbol;
    private boolean assigned = false;

     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   in  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static FeatureAnnotationSymbol getSymbolFromResourceString(final String in) {
         if ((in != null) && (in.length() > 0)) {
             try {
                 // first try to load from jar
                 URL symbolURL = Object.class.getResource(in);
 
                 if (symbolURL == null) {
                     // otherwise try to resolve directly
                     symbolURL = new URL(in);
                 }
                 if (symbolURL != null) {
                     final FeatureAnnotationSymbol ret = new FeatureAnnotationSymbol(symbolURL);
                     if (ret == null) {
                         log.error("konnte kein FAS aus:" + symbolURL + "erzeugen");
                     }
                     return ret;
                 } else {
                     log.error("FAS: konnte URL nicht erzeugen:" + symbolURL);
                 }
             } catch (Exception ex) {
                 log.warn("Fehler in getSymbolFromResourceString(" + in + ")", ex);
             }
         }
         return null;
     }
 
     @Override
     public void assign() {
         if (metaObject != null) {
             cidsBean = metaObject.getBean();
             if (cidsBean != null) {
                 assigned = true;
                 String iconUrl;
                 symbol = null;
                 Object o = cidsBean.getProperty("signatur");
                 if (o instanceof CidsBean) {
                     iconUrl = getUrlStringFromSignature(o);
                     if (iconUrl != null) {
                         symbol = getSymbolFromResourceString(iconUrl);
                     }
                 }
                 if (symbol == null) {
                     o = cidsBean.getProperty("mainlocationtype");
                     if (o instanceof CidsBean) {
                         iconUrl = getUrlStringFromSignature(((CidsBean)o).getProperty("signatur"));
                         if (iconUrl != null) {
                             symbol = getSymbolFromResourceString(iconUrl);
                         }
                     }
                     if (symbol == null) {
                         o = cidsBean.getProperty("mainlocationtype");
                         if (o instanceof CidsBean) {
                             final CidsBean mainLocationType = (CidsBean)o;
                             o = mainLocationType.getProperty("signatur");
                             iconUrl = getUrlStringFromSignature(o);
                             if (iconUrl != null) {
                                 symbol = getSymbolFromResourceString(iconUrl);
                             }
                         }
                     }
                     if (symbol == null) {
                         symbol = DEFAULT_SYMBOL;
                     }
                 }
                 if (symbol == null) {
                     symbol = DEFAULT_SYMBOL;
                 } else {
                     symbol.setSweetSpotX(0.5d);
                     symbol.setSweetSpotY(0.5d);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   signature  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getUrlStringFromSignature(final Object signature) {
         if (signature instanceof CidsBean) {
             final CidsBean signatur = (CidsBean)signature;
             try {
                 final Object fileName = signatur.getProperty("filename");
                 if (fileName != null) {
                     final String ret = StaticProperties.POI_SIGNATUR_URL_PREFIX + fileName
                                 + StaticProperties.POI_SIGNATUR_URL_SUFFIX;
                     log.info("Signatur Url: " + ret);
                     return ret;
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
         return null;
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol() {
         // BUGFIX - ugly but necessary
         if (!assigned) {
             assign();
         }
         final FeatureAnnotationSymbol ret = symbol;
         if (ret != null) {
             return ret;
         } else if (DEFAULT_SYMBOL != null) {
             return DEFAULT_SYMBOL;
         } else {
             return new FeatureAnnotationSymbol();
         }
     }
 
     @Override
     public float getTransparency() {
         return 1.0f;
     }
 
     @Override
     public JComponent getInfoComponent(final Refreshable refresh) {
         return null;
     }
 }

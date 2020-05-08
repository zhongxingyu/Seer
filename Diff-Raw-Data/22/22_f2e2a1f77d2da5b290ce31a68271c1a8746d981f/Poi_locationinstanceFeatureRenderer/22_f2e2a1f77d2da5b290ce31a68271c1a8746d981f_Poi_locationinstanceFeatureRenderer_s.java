 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.featurerenderer.wunda_blau;
 
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import java.net.URL;
 import javax.swing.JComponent;
 
 /**
  *
  * @author srichter
  */
 public class Poi_locationinstanceFeatureRenderer extends CustomCidsFeatureRenderer {
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Poi_locationinstanceFeatureRenderer.class);
     private static final FeatureAnnotationSymbol DEFAULT_SYMBOL;
     private FeatureAnnotationSymbol symbol;
     private boolean assigned = false;
 
     static {
         DEFAULT_SYMBOL = getSymbolFromResourceString(StaticProperties.POI_SIGNATUR_DEFAULT_ICON);
         if (DEFAULT_SYMBOL != null) {
             DEFAULT_SYMBOL.setSweetSpotX(0.5d);
             DEFAULT_SYMBOL.setSweetSpotY(0.5d);
         }
     }
 
     private static final FeatureAnnotationSymbol getSymbolFromResourceString(String in) {
         if (in != null && in.length() > 0) {
             try {
                 //first try to load from jar
                 URL symbolURL = Object.class.getResource(in);
                 if (symbolURL == null) {
                     //otherwise try to resolve directly
                     symbolURL = new URL(in);
                 }
                 if (symbolURL != null) {
                    return new FeatureAnnotationSymbol(symbolURL);
                 }
             } catch (Exception ex) {
                log.warn(ex, ex);
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
                             final CidsBean mainLocationType = (CidsBean) o;
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
 
     private final String getUrlStringFromSignature(Object signature) {
         if (signature instanceof CidsBean) {
             final CidsBean signatur = (CidsBean) signature;
             try {
                 final Object fileName = signatur.getProperty("filename");
                 if (fileName != null) {
                    return StaticProperties.POI_SIGNATUR_URL_PREFIX + fileName + StaticProperties.POI_SIGNATUR_URL_SUFFIX;
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
         return null;
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol() {
         //BUGFIX - ugly but necessary
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
     public JComponent getInfoComponent(Refreshable refresh) {
         return null;
     }
 }

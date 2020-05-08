 /*
  * CidsFeature.java
  * Copyright (C) 2005 by:
  *
  *----------------------------
  * cismet GmbH
  * Goebenstrasse 40
  * 66117 Saarbruecken
  * http://www.cismet.de
  *----------------------------
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *----------------------------
  * Author:
  * thorsten.hell@cismet.de
  *----------------------------
  *
  * Created on 5. Mai 2006, 12:02Ø
  *
  */
 package de.cismet.cismap.navigatorplugin;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.server.localserver.attribute.ClassAttribute;
 import Sirius.server.localserver.attribute.ObjectAttribute;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import com.vividsolutions.jts.geom.Geometry;
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 import de.cismet.cids.tools.StaticCidsUtilities;
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.features.Bufferable;
 import de.cismet.cismap.commons.features.FeatureRenderer;
 import de.cismet.cismap.commons.features.Highlightable;
 import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
 import de.cismet.cismap.commons.features.XStyledFeature;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
 import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;
 import de.cismet.tools.BlacklistClassloading;
 import de.cismet.tools.collections.TypeSafeCollections;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.Stroke;
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 
 import java.util.HashMap;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 /**
  *
  * @author thorsten.hell@cismet.de
  */
 public class CidsFeature implements XStyledFeature, Highlightable, Bufferable, RasterLayerSupportedFeature {
 
     private Paint featureFG = Color.black;
     private Paint featureBG = Color.gray;
     private Paint featureHighFG = Color.blue;
     private Paint featureHighBG = Color.darkGray;
     private float featureTranslucency = 0.5f;
     private float featureBorder = 10.0f;
     private String renderFeature = null;
     private String renderMultipleFeatures = null;
     private int renderAllFeatures = 1;
     private boolean hiding = false;
     private Geometry geom;
     private MetaObject mo;
     private MetaClass mc;
     private MetaObjectNode mon;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private boolean editable = false;
     private String namenszusatz = "";
     private FeatureRenderer featureRenderer = null;
     private FeatureAwareRasterService featureAwareRasterService = null;
     private String supportingRasterServiceRasterLayerName = null;
     private String supportingRasterServiceIdAttributeName = null;
     private String supportingRasterServiceLayerStyleName = "default";
     private Image pointSymbol = null;
     private double pointSymbolSweetSpotX = 0d;
     private double pointSymbolSweetSpotY = 0d;
 
     /**
      * Creates a new instance of CidsFeature
      * @param mon
      * @throws java.lang.IllegalArgumentException 
      */
     public CidsFeature(MetaObjectNode mon) throws IllegalArgumentException {
         log.debug("New CIDSFEATURE");
         try {
             this.mon = mon;
             mo = mon.getObject();
             this.mc = SessionManager.getProxy().getMetaClass(mo.getClassKey());
             initFeatureSettings();
 
             //renderFeature auswerten
             try {
                 if (renderFeature != null && renderFeature.trim().equals("")) {
                     geom = (Geometry) StaticCidsUtilities.getValueOfAttributeByString(renderFeature, mo);
                 }
             } catch (Exception e) {
                 log.debug("RENDER_FEATURE war fehlerhaft gesetzt. Geometrieattribut mit dem Namen: " + renderFeature + " konnte nicht gefunden werden", e);
                 geom = null;
             }
 
             if (geom == null) {
                 //Defaultfall: Es ist kein Geometriefeld angegeben
                 Collection c = mo.getAttributesByType(Geometry.class, 1);
                 for (Object elem : c) {
                     ObjectAttribute oa = (ObjectAttribute) elem;
                     geom = (Geometry) oa.getValue();
                 }
             }
 
         } catch (Throwable t) {
             log.error("Error CidsFeature(MetaObjectNode mon)", t);
             throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.Fehler_beim_Erstellen_eines_CidsFeatures"), t);
         }
     }
 
     public CidsFeature(MetaObjectNode mon, ObjectAttribute oAttr) throws IllegalArgumentException {
         log.debug("New CIDSFEATURE");
         try {
             this.mon = mon;
             mo = mon.getObject();
             this.mc = SessionManager.getProxy().getMetaClass(mo.getClassKey());
             initFeatureSettings();
 
             //TODO noch irgendwie sinnvoll den namenszusatz f\u00FCllen
 
             if (oAttr.getValue() instanceof MetaObject) {
                 Collection c = ((MetaObject) oAttr.getValue()).getAttributesByType(Geometry.class, 1);
                 if (c.size() == 0) {
                     throw new IllegalArgumentException("Geometry==null");
                 }
                 for (Object elem : c) {
                     ObjectAttribute oa = (ObjectAttribute) elem;
                     geom = (Geometry) oa.getValue();
                     if (geom == null) {
                         throw new IllegalArgumentException("Geometry==null");
                     }
                 }
             } else {
                 throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.Keine_Geometrie_im_�bergebenen_ObjectAttribute."));
             }
         } catch (Throwable t) {
             throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.Fehler_beim_Erstellen_eines_CidsFeatures"), t);
         }
     }
 
     private void initFeatureSettings() throws Throwable {
 
         try {
             renderFeature = getAttribValue("RENDER_FEATURE", mo, mc).toString();
             log.debug("RENDER_FEATURE=" + renderFeature);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.RENDER_FEATURE_nicht_vorhanden"), t);
         }
         try {
             renderMultipleFeatures = getAttribValue("RENDER_MULTIPLE_FEATURES", mo, mc).toString();
             log.debug("RENDER_MULTIPLE_FEATURES=" + renderMultipleFeatures);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.RENDER_MULTIPLE_FEATURES_nicht_vorhanden"), t);
         }
         try {
             renderAllFeatures = new Integer(getAttribValue("RENDER_ALL_FEATURES", mo, mc).toString()).intValue();
             log.debug("renderAllFeatures=" + renderAllFeatures);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.RENDER_AKK_FEATURES_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             hiding = new Boolean(getAttribValue("HIDE_FEATURE", mo, mc).toString()).booleanValue();
             log.debug("HIDE_FEATURE=" + hiding);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.HIDE_FEATURE_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             log.debug("VERSUCHE FEATURERENDERER ZU SETZEN");
             String overrideFeatureRendererClassName=System.getProperty(mo.getDomain().toLowerCase()+"."+mo.getMetaClass().getTableName().toLowerCase()+".featurerenderer");
 
             String featureRendererClass = overrideFeatureRendererClassName;
             if (featureRendererClass==null){
                String mcName=mo.getMetaClass().getTableName();
                 featureRendererClass="de.cismet.cids.custom.featurerenderer."+mo.getDomain().toLowerCase()+"."+mcName.substring(0,1).toUpperCase()+mcName.substring(1).toLowerCase()+"FeatureRenderer";
             }
             log.debug("FEATURE_RENDERER=" + featureRendererClass);
             Class c = BlacklistClassloading.forName(featureRendererClass);
             if (c==null){
                 c=BlacklistClassloading.forName((String)getAttribValue("FEATURE_RENDERER", mo, mc));
             }
             Constructor constructor = c.getConstructor();
             featureRenderer = (FeatureRenderer) constructor.newInstance();
             ((CustomCidsFeatureRenderer) featureRenderer).setMetaObject(mo);
             //Method assignM=assigner.getMethod("assign", new Class[] {Connection.class,String[].class, UniversalContainer.class});
             log.debug("HAT GEKLAPPT:" + featureRendererClass);
         } catch (Throwable t) {
             log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_RENDERER_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             float featureTranslucencyValue = new Float(getAttribValue("FEATURE_TRANSLUCENCY", mo, mc).toString()).floatValue();
             log.debug("FEATURE_TRANSLUCENCY=" + featureTranslucencyValue);
             featureTranslucency = featureTranslucencyValue;
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_TRANSLUCENCY_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             setFeatureBorder(new Float(getAttribValue("FEATURE_BORDER", mo, mc).toString()).floatValue());
             log.debug("featureBorder=" + featureBorder);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_BORDER_nicht_vorhanden_oder_nicht_korrekt_gefuellt._Probiere_jetzt_noch_UMGEBUNG"), t);
             try {
                 setFeatureBorder(new Float(getAttribValue("UMGEBUNG", mo, mc).toString()).floatValue());
                 log.debug("featureBorder=" + featureBorder);
             } catch (Throwable tt) {
                 log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.UMGEBUNG_nicht_vorhanden_oder_nicht_korrekt_gefuellt."), tt);
             }
         }
         try {
             String fg = getAttribValue("FEATURE_FG", mo, mc).toString();
             String[] t = fg.split(",");
             int r = new Integer(t[0]).intValue();
             int g = new Integer(t[1]).intValue();
             int b = new Integer(t[2]).intValue();
             featureFG = new Color(r, g, b);
             log.debug("FEATURE_FG=Color(" + r + "," + g + "," + b + ")");
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_FG_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             String s = getAttribValue("FEATURE_BG", mo, mc).toString();
             String[] t = s.split(",");
             int r = new Integer(t[0]).intValue();
             int g = new Integer(t[1]).intValue();
             int b = new Integer(t[2]).intValue();
             featureBG = new Color(r, g, b);
             log.debug("FEATURE_BG=Color(" + r + "," + g + "," + b + ")");
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_BG_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             String s = getAttribValue("FEATURE_HIGH_FG", mo, mc).toString();
             String[] t = s.split(",");
             int r = new Integer(t[0]).intValue();
             int g = new Integer(t[1]).intValue();
             int b = new Integer(t[2]).intValue();
             log.debug("FEATURE_HIGH_FG=Color(" + r + "," + g + "," + b + ")");
             featureHighFG = new Color(r, g, b);
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_HIGH_FG_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             String s = getAttribValue("FEATURE_HIGH_BG", mo, mc).toString();
             String[] t = s.split(",");
             int r = new Integer(t[0]).intValue();
             int g = new Integer(t[1]).intValue();
             int b = new Integer(t[2]).intValue();
             featureHighFG = new Color(r, g, b);
             log.debug("FEATURE_HIGH_BG=Color(" + r + "," + g + "," + b + ")");
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.FEATURE_HIGH_BG_nicht_vorhanden_oder_nicht_korrekt_gefuellt"), t);
         }
         try {
             String path = getAttribValue("FEATURE_POINT_SYMBOL", mo, mc).toString();
             pointSymbol = new javax.swing.ImageIcon(getClass().getResource(path)).getImage();
             log.debug("FEATURE_POINT_SYMBOL=" + path);
         } catch (Throwable t) {
             log.info("FEATURE_POINT_SYMBOL Error", t);
         }
         try {
             String x = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_X", mo, mc).toString();
             pointSymbolSweetSpotX = new Double(x).doubleValue();
             log.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_X=" + x);
         } catch (Throwable t) {
             log.info("FEATURE_POINT_SYMBOL_SWEETSPOT_X Error", t);
         }
         try {
             String y = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_Y", mo, mc).toString();
             pointSymbolSweetSpotY = new Double(y).doubleValue();
             log.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_Y=" + y);
         } catch (Throwable t) {
             log.info("FEATURE_POINT_SYMBOL_SWEETSPOT_Y Error", t);
         }
         try {
             String supportingRasterService = getAttribValue("FEATURESUPPORTINGRASTERSERVICE_TYPE", mo, mc).toString();
             String supportingRasterServiceUrl = getAttribValue("FEATURESUPPORTINGRASTERSERVICE_SIMPLEURL", mo, mc).toString();
             supportingRasterServiceRasterLayerName = getAttribValue("FEATURESUPPORTINGRASTERSERVICE_RASTERLAYER", mo, mc).toString();
             supportingRasterServiceIdAttributeName = getAttribValue("FEATURESUPPORTINGRASTERSERVICE_ID_ATTRIBUTE", mo, mc).toString();
             String serviceName = getAttribValue("FEATURESUPPORTINGRASTERSERVICE_NAME", mo, mc).toString();
             log.debug("FEATURESUPPORTINGRASTERSERVICE_TYPE=" + supportingRasterService);
             Class c = BlacklistClassloading.forName(supportingRasterService);
             SimpleFeatureSupporterRasterServiceUrl url = new SimpleFeatureSupporterRasterServiceUrl(supportingRasterServiceUrl);
             Constructor constructor = c.getConstructor(SimpleFeatureSupporterRasterServiceUrl.class);
             this.featureAwareRasterService = (FeatureAwareRasterService) constructor.newInstance(url);
             featureAwareRasterService.setName(serviceName);
         } catch (Throwable t) {
             log.info("Fehler beim Erzeugen des FeaureSupportingRasterService, oder nicht vorhanden.", t);
         }
     }
 
     private Object getAttribValue(String name, MetaObject mo, MetaClass mc) {
         Collection coa = mo.getAttributeByName(name, 1);
         Collection cca = mc.getAttributeByName(name);
         log.debug("mc.getAttributeByName(" + name + ")=" + cca);
         if (coa.size() == 1) {
             ObjectAttribute oa = (ObjectAttribute) (coa.toArray()[0]);
             return oa.getValue();
         } else if (cca.size() > 0) {
             ClassAttribute ca = (ClassAttribute) (cca.toArray()[0]);
             return ca.getValue();
         } else {
             return null;
         }
     }
 
     public void setGeometry(Geometry geom) {
         this.geom = geom;
     }
 
     public float getTransparency() {
         if (featureRenderer != null && featureRenderer.getTransparency() > 0) {
             return featureRenderer.getTransparency();
         } else {
             return featureTranslucency;
         }
     }
 
     public Stroke getLineStyle() {
         if (featureRenderer != null && featureRenderer.getLineStyle() != null) {
             return featureRenderer.getLineStyle();
         } else {
             return null;
         }
     }
 
     public Paint getLinePaint() {
         if (featureRenderer != null && featureRenderer.getLinePaint() != null) {
             return featureRenderer.getLinePaint();
         } else {
             return featureFG;
         }
     }
 
     public Geometry getGeometry() {
         return geom;
     }
 
     public Paint getFillingPaint() {
         if (featureRenderer != null && featureRenderer.getFillingStyle() != null) {
             return featureRenderer.getFillingStyle();
         } else {
             return featureBG;
         }
     }
 
     public boolean canBeSelected() {
         return true;
     }
 
     public void setHighlighting(boolean highlighting) {
     }
 
     public boolean getHighlighting() {
         return false;
     }
 
     public String getName() {
         log.debug("getName() von " + mo);
         try {
             if (featureRenderer instanceof CustomCidsFeatureRenderer && ((CustomCidsFeatureRenderer) featureRenderer).getAlternativeName() != null) {
                 return ((CustomCidsFeatureRenderer) featureRenderer).getAlternativeName();
             } else {
                 return mon.getName() + namenszusatz;
             }
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.Fehler_beim_Ermitteln_des_Namens"), t);
             return null;
         }
     }
 
     public JComponent getInfoComponent(Refreshable refresh) {
         log.debug("getInfoComponent");
         if (featureRenderer != null) {
             return featureRenderer.getInfoComponent(refresh);
         } else {
             return null;
         }
     }
 
     public ImageIcon getIconImage() {
         ImageIcon ii = null;
         try {
             ii = new ImageIcon(mc.getObjectIconData());
         } catch (Throwable t) {
             log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.Fehler_beim_Lesen_der_Icon_Daten._Versuche_dann_das_Klassenicon_zu_laden"), t);
             try {
                 ii = new ImageIcon(mc.getIconData());
             } catch (Throwable tt) {
                 log.info(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.log.Fehler_beim_Lesen_der_Icon_Daten"), tt);
 
                 ii = null;
             }
             ii = null;
         }
         log.debug("getIconImage:" + ii);
         return ii;
     }
 
     public float getFeatureBorder() {
         return featureBorder;
     }
 
     public double getBuffer() {
         return featureBorder;
     }
 
     public void setFeatureBorder(float featureBorder) {
         this.featureBorder = featureBorder;
     }
 
     public String getType() {
         return mc.getName();
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof CidsFeature)) {
             return false;
         } else {
             try {
                 String thisString = mon.getObjectId() + "@" + mon.getClassId();
                 String thatString = ((CidsFeature) o).mon.getObjectId() + "@" + ((CidsFeature) o).mon.getClassId();
                 return thisString.equals(thatString);
             } catch (Exception e) {
                 return false;
             }
         }
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
 
     /**
      * Returns a hash code value for the object. This method is
      * supported for the benefit of hashtables such as those provided by
      * <code>java.util.Hashtable</code>.
      * <p>
      * The general contract of <code>hashCode</code> is:
      * <ul>
      * <li>Whenever it is invoked on the same object more than once during
      *     an execution of a Java application, the <tt>hashCode</tt> method
      *     must consistently return the same integer, provided no information
      *     used in <tt>equals</tt> comparisons on the object is modified.
      *     This integer need not remain consistent from one execution of an
      *     application to another execution of the same application.
      * <li>If two objects are equal according to the <tt>equals(Object)</tt>
      *     method, then calling the <code>hashCode</code> method on each of
      *     the two objects must produce the same integer result.
      * <li>It is <em>not</em> required that if two objects are unequal
      *     according to the {@link java.lang.Object#equals(java.lang.Object)}
      *     method, then calling the <tt>hashCode</tt> method on each of the
      *     two objects must produce distinct integer results.  However, the
      *     programmer should be aware that producing distinct integer results
      *     for unequal objects may improve the performance of hashtables.
      * </ul>
      * <p>
      * As much as is reasonably practical, the hashCode method defined by
      * class <tt>Object</tt> does return distinct integers for distinct
      * objects. (This is typically implemented by converting the internal
      * address of the object into an integer, but this implementation
      * technique is not required by the
      * Java<font size="-2"><sup>TM</sup></font> programming language.)
      *
      *
      * @return a hash code value for this object.
      * @see java.lang.Object#equals(java.lang.Object)
      * @see java.util.Hashtable
      */
     @Override
     public int hashCode() {
         int retValue;
         if (mon != null) {
             retValue = mon.hashCode();
         } else {
             retValue = super.hashCode();
         }
         return retValue;
     }
 
     public void hide(boolean hiding) {
         this.hiding = hiding;
     }
 
     public boolean isHidden() {
         return hiding;
     }
 
     public void setSupportingRasterService(FeatureAwareRasterService featureAwareRasterService) {
         this.featureAwareRasterService = featureAwareRasterService;
     }
 
     public FeatureAwareRasterService getSupportingRasterService() {
         return featureAwareRasterService;
     }
 
     public MetaObject getMetaObject() {
         return mo;
     }
 
     public MetaClass getMetaClass() {
         return mc;
     }
 
     public String getFilterPart() {
         ObjectAttribute oa = (ObjectAttribute) mo.getAttributeByName(supportingRasterServiceIdAttributeName, 1).toArray()[0];
         String id = oa.getValue().toString();
         return supportingRasterServiceRasterLayerName + "@" + id + "@" + supportingRasterServiceLayerStyleName + ",";
     }
 
     public String getSpecialLayerName() {
         if (supportingRasterServiceRasterLayerName != null && supportingRasterServiceRasterLayerName.startsWith("cidsAttribute::")) {
             try {
                 String attrField = supportingRasterServiceRasterLayerName.substring("cidsAttribute::".length());
                 log.fatal("attrField" + attrField);
                 String ret = getMetaObject().getBean().getProperty(attrField).toString();
                 return ret;
             } catch (Exception e) {
                 log.error("AttrFieldProblem",e);
             }
 
         }
         return supportingRasterServiceRasterLayerName;
 
     }
 
     @Override
     public Object clone() {
         log.debug("CLONE");
         try {
             return super.clone();
         } catch (Exception e) {
             return null;
         }
     }
 
     public FeatureAnnotationSymbol getPointAnnotationSymbol() {
         if (featureRenderer != null &&
                 featureRenderer.getPointSymbol() != null) {
             return featureRenderer.getPointSymbol();
         } else if (pointSymbol != null) {
             FeatureAnnotationSymbol ret = new FeatureAnnotationSymbol(pointSymbol);
             ret.setSweetSpotX(pointSymbolSweetSpotX);
             ret.setSweetSpotY(pointSymbolSweetSpotY);
             return ret;
         } else {
             return null;
         }
     }
 
     public int getLineWidth() {
         if (featureRenderer != null && featureRenderer.getLineStyle() != null && featureRenderer.getLineStyle() instanceof BasicStroke) {
             return (int) ((BasicStroke) featureRenderer.getLineStyle()).getLineWidth();
         }
         return 1;
     }
 
     public void setFillingPaint(Paint fillingStyle) {
     }
 
     public void setLineWidth(int width) {
     }
 
     public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
     }
 
     public void setTransparency(float transparrency) {
         this.featureTranslucency = transparrency;
     }
 
     public boolean isHighlightingEnabled() {
         return true;
     }
 
     public void setHighlightingEnabled(boolean enabled) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void setLinePaint(Paint linePaint) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }

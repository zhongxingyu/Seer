 /*
  *  Copyright (C) 2010 srichter
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.custom.featurerenderer.wunda_blau;
 
 import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
 import de.cismet.cismap.commons.Refreshable;
 import de.cismet.cismap.commons.features.SubFeature;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 import java.awt.Color;
 import java.awt.Paint;
 import java.awt.Stroke;
 import javax.swing.JComponent;
 
 /**
  *
  * @author srichter
  */
 public class Alb_baulastFeatureRenderer extends  CustomCidsFeatureRenderer {
 
     private static final Color BELASTET = new Color(0, 255, 0);
     private static final Color BEGUENSTIGT = new Color(255, 255, 0);
 
     @Override
     public Paint getFillingStyle(CidsFeature subFeature) {
         String attrString=subFeature.getMyAttributeStringInParentFeature();
         if(attrString!=null){
             if (attrString.contains("belastet")){
                 return BELASTET;
             }
             else if (attrString.contains("beguenstigt")){
                 return BEGUENSTIGT;
             }
             else {
                 return Color.red;
             }
         }
         else {
             return new Color(0,0,0,0);
         }
 
     }
 
     @Override
     public JComponent getInfoComponent(Refreshable refresh, CidsFeature subFeature) {
         return null;
     }
 
     @Override
     public Paint getLinePaint(CidsFeature subFeature) {
         return Color.BLACK;
     }
 
     @Override
     public Stroke getLineStyle(CidsFeature subFeature) {
         return new FixedWidthStroke();
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol(CidsFeature subFeature) {
         return null;
     }
 
     @Override
     public float getTransparency(CidsFeature subFeature) {
         return 0.8f;
     }
 
     @Override
     public Paint getFillingStyle() {
         return getFillingStyle(null);
     }
 
     @Override
     public JComponent getInfoComponent(Refreshable refresh) {
         return getInfoComponent(refresh,null);
     }
 
     @Override
     public Paint getLinePaint() {
         return getLinePaint(null);
     }
 
     @Override
     public Stroke getLineStyle() {
         return getLineStyle(null);
     }
 
     @Override
     public FeatureAnnotationSymbol getPointSymbol() {
         return getPointSymbol(null);
     }
 
     @Override
     public float getTransparency() {
         return getTransparency(null);
     }
 
     @Override
     public void assign() {
 
     }
    
 }

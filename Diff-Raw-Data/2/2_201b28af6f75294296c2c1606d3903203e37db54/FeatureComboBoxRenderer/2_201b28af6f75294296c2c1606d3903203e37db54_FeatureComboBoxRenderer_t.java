 /*
  * FeatureComboBoxRenderer.java
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
  * Created on 31. Mai 2006, 16:32
  *
  */
 
 package de.cismet.cismap.cids.geometryeditor;
 
 import com.vividsolutions.jts.geom.Point;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.XStyledFeature;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 
 /**
  *
  * @author thorsten.hell@cismet.de
  */
 public class FeatureComboBoxRenderer extends DefaultListCellRenderer implements ListCellRenderer{
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     
     Color background=UIManager.getDefaults().getColor("ComboBox.background");
     Color selectedBackground=UIManager.getDefaults().getColor("ComboBox.selectionBackground");
     
     /** Creates a new instance of FeatureComboBoxRenderer */
     public FeatureComboBoxRenderer() {
     }
     
     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
         try {
             if (value!=null) {
                 if (value instanceof CidsFeature&&((CidsFeature)value).getGeometry()!=null) {
                     setText("momentan zugeordnete Geometrie ("+((CidsFeature)value).getGeometry().getGeometryType()+")");
                 }
                 else if (value instanceof XStyledFeature&&((XStyledFeature)value).getGeometry()!=null) {
                     setText(((XStyledFeature)value).getName());
                     setIcon(((XStyledFeature)value).getIconImage());
                    //log.fatal("xxxlala "+CismapBroker.getInstance().getMappingComponent().getPFeatureHM());
                     PFeature pf=(PFeature)CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(value);
                     
                     PFeature clonePf=(PFeature)pf.clone();
                     
                     if (clonePf.getFeature().getGeometry() instanceof Point) {
                         clonePf.getChild(0).removeAllChildren();
                     } else {
                         clonePf.removeAllChildren();
                     }
                     
                     setToolTipText("@@@@"+getText());
                     //i=clonePf.toImage(100,55,null);
                     
                 } else {
                     setText(value.getClass()+":"+value.toString());
                     setIcon(null);
                 }
             } else {
                 setText("keine Geometrie zugeordnet");
             }
         } catch (Throwable t) {
             log.error("Fehler im Renderer der ComboBox",t);
             
             setText("--->value:"+((Feature)value).getGeometry());
         }
         return this;
         
     }
     
 }

 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.tree;
 
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SIcon;
 import org.wings.SLabel;
 import org.wings.STree;
 import org.wings.util.SStringBuilder;
 import org.wings.resource.ResourceManager;
 
 /**
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class SDefaultTreeCellRenderer
         extends SLabel
         implements STreeCellRenderer
 {
     private SStringBuilder nameBuffer = new SStringBuilder();
 
     // Icons
     /**
      * Icon used to show non-leaf nodes that aren't expanded.
      */
     transient protected SIcon closedIcon;
 
     /**
      * Icon used to show leaf nodes.
      */
     transient protected SIcon leafIcon;
 
     /**
      * Icon used to show non-leaf nodes that are expanded.
      */
     transient protected SIcon openIcon;
 
     /**
      * Create a SDefaultTreeCellRenderer with default properties.
      */
     public SDefaultTreeCellRenderer() {
         setHorizontalAlignment(SConstants.LEFT);
         setLeafIcon(getDefaultLeafIcon());
         setClosedIcon(getDefaultClosedIcon());
         setOpenIcon(getDefaultOpenIcon());
     }
 
     /**
      * Returns the default icon, for the current laf, that is used to
      * represent non-leaf nodes that are expanded.
      */
     public SIcon getDefaultOpenIcon() {
         return (SIcon) ResourceManager.getObject("TreeCG.openIcon", SIcon.class);
     }
 
     /**
      * Returns the default icon, for the current laf, that is used to
      * represent non-leaf nodes that are not expanded.
      */
     public SIcon getDefaultClosedIcon() {
         return (SIcon) ResourceManager.getObject("TreeCG.closedIcon", SIcon.class);
     }
 
     /**
      * Returns the default icon, for the current laf, that is used to
      * represent leaf nodes.
      */
     public SIcon getDefaultLeafIcon() {
         return (SIcon) ResourceManager.getObject("TreeCG.leafIcon", SIcon.class);
     }
 
     /**
      * Sets the icon used to represent non-leaf nodes that are expanded.
      */
     public void setOpenIcon(SIcon newIcon) {
         openIcon = newIcon;
     }
 
     /**
      * Returns the icon used to represent non-leaf nodes that are expanded.
      */
     public SIcon getOpenIcon() {
         return openIcon;
     }
 
     /**
      * Sets the icon used to represent non-leaf nodes that are not expanded.
      */
     public void setClosedIcon(SIcon newIcon) {
         closedIcon = newIcon;
     }
 
     /**
      * Returns the icon used to represent non-leaf nodes that are not
      * expanded.
      */
     public SIcon getClosedIcon() {
         return closedIcon;
     }
 
     /**
      * Sets the icon used to represent leaf nodes.
      */
     public void setLeafIcon(SIcon newIcon) {
         leafIcon = newIcon;
     }
 
     /**
      * Returns the icon used to represent leaf nodes.
      */
     public SIcon getLeafIcon() {
         return leafIcon;
     }
 
     /**
      * Style to use for the foreground for selected nodes.
      */
     protected String selectionStyle = null;
 
     /**
      * Style to use for the foreground for non-selected nodes.
      */
     protected String nonSelectionStyle = null;
 
     public SComponent getTreeCellRendererComponent(STree tree,
                                                    Object value,
                                                    boolean selected,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {
         setNameRaw(name(tree, row));
 
         if (value == null || value.toString() == null ||
                 value.toString().length() == 0) {
 
            setText("&nbsp;");
         } else {
             setText(value.toString());
             setToolTipText(value.toString());
         }
 
         if (!tree.isEnabled()) {
             setEnabled(false);
             if (leaf) {
                 setDisabledIcon(getLeafIcon());
             } else if (expanded) {
                 setDisabledIcon(getOpenIcon());
             } else {
                 setDisabledIcon(getClosedIcon());
             }
         } else {
             setEnabled(true);
             if (leaf) {
                 setIcon(getLeafIcon());
             } else if (expanded) {
                 setIcon(getOpenIcon());
             } else {
                 setIcon(getClosedIcon());
             }
         }
 
         if (getStyle() == null)
             setStyle(tree.getStyle());
         if (getDynamicStyles() == null)
             setDynamicStyles(tree.getDynamicStyles());
 
         return this;
     }
 
     protected String name(SComponent component, int row) {
         nameBuffer.setLength(0);
         nameBuffer.append(component.getName()).append("_").append(row);
         return nameBuffer.toString();
     }
 
     /**
      * Sets the style the cell is drawn with when the cell is selected.
      */
     public void setSelectionStyle(String newStyle) {
         selectionStyle = newStyle;
     }
 
     /**
      * Returns the style the cell is drawn with when the cell is selected.
      */
     public String getSelectionStyle() {
         return selectionStyle;
     }
 
     /**
      * Sets the style the cell is drawn with when the cell isn't selected.
      */
     public void setNonSelectionStyle(String newStyle) {
         nonSelectionStyle = newStyle;
     }
 
     /**
      * Returns the style the cell is drawn with when the cell isn't selected.
      */
     public String getNonSelectionStyle() {
         return nonSelectionStyle;
     }
 
 }
 
 

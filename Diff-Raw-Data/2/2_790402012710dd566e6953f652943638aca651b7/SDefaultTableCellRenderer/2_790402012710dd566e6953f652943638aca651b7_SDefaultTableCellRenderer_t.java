 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.table;
 
 import org.wings.SComponent;
 import org.wings.STable;
 import org.wings.SLabel;
 import org.wings.SIcon;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:holger.engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SDefaultTableCellRenderer
     extends SLabel
     implements STableCellRenderer
 {
     /** Style to use for the foreground for selected nodes. */
     protected String selectionStyle = null;
 
     /** Style to use for the foreground for non-selected nodes. */
     protected String nonSelectionStyle = null;
 
     /**
      * TODO: documentation
      *
      */
     public SDefaultTableCellRenderer() {
     }
 
     public SComponent getTableCellRendererComponent(STable table,
                                                     Object value,
                                                     boolean selected,
                                                     int row,
                                                     int col)
     {
         setText(null);
         setIcon(null);
 
         if (value == null)
            setText("<html>&nbsp;");
         else if (value instanceof SIcon)
             setIcon((SIcon)value);
         else if ( value instanceof SComponent ) {
             SComponent result = (SComponent)value;
 
             if ( selected && selectionStyle!=null ) {
                 result.setStyle(selectionStyle);
             } else {
                 result.setStyle(nonSelectionStyle);
             }
             
             return result;
         } 
         else {
             setText(value.toString());
         }
 
         if ( selected && selectionStyle!=null ) {
             setStyle(selectionStyle);
         } else {
             setStyle(nonSelectionStyle);
         }
 
         return this;
     }
 
     /**
      * Sets the style the cell is drawn with when the cell is selected.
      *
      * @param newStyle
      */
     public void setSelectionStyle(String newStyle) {
         selectionStyle = newStyle;
     }
 
     /**
      * Returns the style the cell is drawn with when the cell is selected.
      *
      * @return
      */
     public String getSelectionStyle() {
         return selectionStyle;
     }
 
     /**
      * Sets the style the cell is drawn with when the cell isn't selected.
      *
      * @param newStyle
      */
     public void setNonSelectionStyle(String newStyle) {
         nonSelectionStyle = newStyle;
     }
 
     /**
      * Returns the style the cell is drawn with when the cell isn't selected.
      *
      * @return
      */
     public String getNonSelectionStyle() {
         return nonSelectionStyle;
     }
 
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */

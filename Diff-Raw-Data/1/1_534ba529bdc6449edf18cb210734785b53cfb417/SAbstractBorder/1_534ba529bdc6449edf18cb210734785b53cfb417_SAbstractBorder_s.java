 /*
  * $Id$
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
 package org.wings.border;
 
 import org.wings.style.CSSAttributeSet;
 import org.wings.style.CSSStyleSheet;
 import org.wings.style.CSSProperty;
 import org.wings.SConstants;
 
 import java.awt.*;
 
 /**
  * This is a an abstract implementation of the <code>SBorder</code>
  * interface.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public abstract class SAbstractBorder
     implements SBorder
 {
     protected BorderSpec[] specs = new BorderSpec[] {
         new BorderSpec(),
         new BorderSpec(),
         null,
         null,
         new BorderSpec(),
         new BorderSpec(),
     };
 
     protected Insets insets;
 
     private CSSAttributeSet attributes = new CSSAttributeSet();
 
     public SAbstractBorder() {
         this(null, -1, null);
     }
 
     public SAbstractBorder(Color c, int thickness, Insets insets) {
         setInsets(insets);
         setColor(c);
         setThickness(thickness);
     }
 
     public SAbstractBorder(Insets insets) {
         this(null, -1, insets);
     }
 
     public SAbstractBorder(Color c) {
         this(c, 1, null);
     }
 
     public SAbstractBorder(int thickness) {
         this(null, thickness, null);
     }
 
     /**
      * set the insets of the border
      */
     public void setInsets(Insets insets) {
         this.insets = insets;
         attributes = null;
     }
 
     /**
      * @return the insets of the border
      */
     public final Insets getInsets() {
         return insets;
     }
 
     /**
      * sets the foreground color of the border
      */
     public Color getColor() {
         return getColor(SConstants.TOP);
     }
 
     public Color getColor(int position) {
         return specs[position].color;
     }
 
     /**
      * sets the foreground color of the border
      */
     public void setColor(Color color) {
         setColor(color, SConstants.TOP);
         setColor(color, SConstants.LEFT);
         setColor(color, SConstants.RIGHT);
         setColor(color, SConstants.BOTTOM);
     }
 
     public void setColor(Color color, int position) {
         specs[position].color = color;
         attributes = null;
     }
 
     /**
      * set the thickness of the border
      * thickness must be > 0
      */
     public void setThickness(int thickness) {
         setThickness(thickness, SConstants.TOP);
         setThickness(thickness, SConstants.LEFT);
         setThickness(thickness, SConstants.RIGHT);
         setThickness(thickness, SConstants.BOTTOM);
     }
 
     public void setThickness(int thickness, int position) {
         specs[position].thickness = thickness;
         attributes = null;
     }
 
     /**
      * @return thickness in pixels
      */
     public final int getThickness() {
         return getThickness(SConstants.TOP);
     }
 
     public int getThickness(int position) {
         return specs[position].thickness;
     }
 
     /**
      * set the style of the border
      * style must be > 0
      */
     public void setStyle(String style) {
         setStyle(style, SConstants.TOP);
         setStyle(style, SConstants.LEFT);
         setStyle(style, SConstants.RIGHT);
         setStyle(style, SConstants.BOTTOM);
     }
 
     public void setStyle(String style, int position) {
         specs[position].style = style;
         attributes = null;
     }
 
     /**
      * @return style in pixels
      */
     public final String getStyle() {
         return getStyle(SConstants.TOP);
     }
 
     public String getStyle(int position) {
         return specs[position].style;
     }
 
     public CSSAttributeSet getAttributes() {
         if (attributes == null) {
             if (insets != null) {
                 attributes.put(CSSProperty.PADDING_TOP, insets.top + "px");
                 attributes.put(CSSProperty.PADDING_LEFT, insets.left + "px");
                 attributes.put(CSSProperty.PADDING_RIGHT, insets.right + "px");
                 attributes.put(CSSProperty.PADDING_BOTTOM, insets.bottom + "px");
             }
 
             BorderSpec top = specs[SConstants.TOP];
             attributes.put(CSSProperty.BORDER_TOP_WIDTH, top.thickness + "px");
             attributes.put(CSSProperty.BORDER_TOP_STYLE, top.style);
             attributes.put(CSSProperty.BORDER_TOP_COLOR, CSSStyleSheet.getAttribute(top.color));
             BorderSpec left = specs[SConstants.LEFT];
             attributes.put(CSSProperty.BORDER_LEFT_WIDTH, left.thickness + "px");
             attributes.put(CSSProperty.BORDER_LEFT_STYLE, left.style);
             attributes.put(CSSProperty.BORDER_LEFT_COLOR, CSSStyleSheet.getAttribute(left.color));
             BorderSpec right = specs[SConstants.RIGHT];
             attributes.put(CSSProperty.BORDER_RIGHT_WIDTH, right.thickness + "px");
             attributes.put(CSSProperty.BORDER_RIGHT_STYLE, right.style);
             attributes.put(CSSProperty.BORDER_RIGHT_COLOR, CSSStyleSheet.getAttribute(right.color));
             BorderSpec bottom = specs[SConstants.BOTTOM];
             attributes.put(CSSProperty.BORDER_BOTTOM_WIDTH, bottom.thickness + "px");
             attributes.put(CSSProperty.BORDER_BOTTOM_STYLE, bottom.style);
             attributes.put(CSSProperty.BORDER_BOTTOM_COLOR, CSSStyleSheet.getAttribute(bottom.color));
         }
         return attributes;
     }
 
 
     static class BorderSpec {
         public int thickness;
         public String style;
         public Color color;
     }
 }

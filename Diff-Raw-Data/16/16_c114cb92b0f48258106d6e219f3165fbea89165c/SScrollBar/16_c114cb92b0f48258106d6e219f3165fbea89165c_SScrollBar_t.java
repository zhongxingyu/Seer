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
 
 package org.wings;
 
 import java.awt.Adjustable;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.AdjustmentEvent;
 import java.io.Serializable;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.DefaultBoundedRangeModel;
 import javax.swing.Icon;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 import org.wings.plaf.*;
 import org.wings.io.Device;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SScrollBar
     extends SAbstractAdjustable
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "ScrollBarCG";
 
 
     /**
      * Access to the default Icons for buttons
      */
     public static final int FORWARD = 0;
     public static final int BACKWARD = 1;
     public static final int FORWARD_BLOCK = 2;
     public static final int BACKWARD_BLOCK = 3;
     public static final int FIRST = 4;
     public static final int LAST = 5;
 
     /** contains the clickables forward, backward, blockforward, blockbackward, 
         first, last */
     protected SClickable[] clickables = new SClickable[6];
 
     // 2 orientations, 6 directions (FORWARD, BACKWARD,...) 
     // and the Icons
     private final static SIcon[][][] DEFAULT_ICONS =
         new SIcon[2][6][SClickable.ICON_COUNT];
 
     // Initialisiert (laedt) die Default Images
     static {
         String[] postfixes = new String[6];
         String[] prefixes = new String[6];
         for ( int orientation=0; orientation<2; orientation++ ) {
             prefixes[BACKWARD] = "";
             prefixes[FORWARD] = "";
             prefixes[FIRST] = "Margin";
             prefixes[LAST] = "Margin";
             prefixes[FORWARD_BLOCK] = "Block";
             prefixes[BACKWARD_BLOCK] = "Block";
             if ( orientation==SConstants.VERTICAL ) {
                 postfixes[BACKWARD] = "Up";
                 postfixes[FORWARD] = "Down";
                 postfixes[FIRST] = "Up";
                 postfixes[LAST] = "Down";
                 postfixes[BACKWARD_BLOCK] = "Up";
                 postfixes[FORWARD_BLOCK] = "Down";
             } else {
                 postfixes[BACKWARD] = "Left";
                 postfixes[FORWARD] = "Right";
                 postfixes[FIRST] = "Left";
                 postfixes[LAST] = "Right";
                 postfixes[BACKWARD_BLOCK] = "Left";
                 postfixes[FORWARD_BLOCK] = "Right";
             }
 
             for ( int direction=0; direction<postfixes.length; direction++ ) {
                 DEFAULT_ICONS[orientation][direction][SClickable.ENABLED_ICON] =
                     new ResourceImageIcon("org/wings/icons/"
                                           + prefixes[direction]
                                           + "Scroll"
                                           + postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][SClickable.DISABLED_ICON] =
                     new ResourceImageIcon("org/wings/icons/Disabled" 
                                           + prefixes[direction]
                                           + "Scroll"
                                           + postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][SClickable.PRESSED_ICON] =
                     new ResourceImageIcon("org/wings/icons/Pressed"
                                           + prefixes[direction]
                                           + "Scroll"
                                           + postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][SClickable.ROLLOVER_ICON] =
                     new ResourceImageIcon("org/wings/icons/Rollover"
                                           + prefixes[direction]
                                           + "Scroll"
                                           + postfixes[direction] + ".gif");
             }
         }
     }
 
     /**
      * Creates a scrollbar with the specified orientation,
      * value, extent, mimimum, and maximum.
      * The "extent" is the size of the viewable area. It is also known
      * as the "visible amount".
      * <p>
      * Note: Use <code>setBlockIncrement</code> to set the block
      * increment to a size slightly smaller than the view's extent.
      * That way, when the user jumps the knob to an adjacent position,
      * one or two lines of the original contents remain in view.
      *
      * @exception IllegalArgumentException if orientation is not one of VERTICAL, HORIZONTAL
      *
      * @see #setOrientation
      * @see #setValue
      * @see #setVisibleAmount
      * @see #setMinimum
      * @see #setMaximum
      */
     public SScrollBar(int orientation, int value, int extent, int min, int max) {
         super(value, extent, min, max);
 
         for ( int i=0; i<clickables.length; i++ ) {
             clickables[i] = new SClickable();
             clickables[i].setEventTarget(this);
         }
 
         setOrientation(orientation);
     }
 
     /**
      * Creates a scrollbar with the specified orientation
      * and the following initial values:
      * <pre>
      * minimum = 0
      * maximum = 100
      * value = 0
      * extent = 10
      * </pre>
      */
     public SScrollBar(int orientation) {
         this(orientation, 0, 10, 0, 100);
     }
 
 
     /**
      * Creates a vertical scrollbar with the following initial values:
      * <pre>
      * minimum = 0
      * maximum = 100
      * value = 0
      * extent = 10
      * </pre>
      */
     public SScrollBar() {
         this(SConstants.VERTICAL);
     }
 
     /**
      * TODO: documentation
      *
      */
     public void resetIcons() {
         for ( int i=0; i<clickables.length; i++ ) {
             clickables[i].setIcons(DEFAULT_ICONS[orientation][i]);
         } 
     }
 
     /**
      * to set your favorite icons and text of the clickable. Icons will be
      * reset to default, if you change the orientation {@link #setOrientation}
      * @see #FORWARD
      * @see #BACKWARD
      * @see #FORWARD_BLOCK
      * @see #BACKWARD_BLOCK
      * @see #FIRST
      * @see #LAST
      **/
     public SClickable getClickable(int clickable) {
         return clickables[clickable];
     }
 
     /**
      * Are margin buttons visible
      * @see #FIRST
      * @see #LAST
      **/
     public final boolean isMarginVisible() {
         return clickables[FIRST].isVisible()  ||
             clickables[LAST].isVisible();
     }
 
     /**
      * Are margin buttons visible
      * @see #FIRST
      * @see #LAST
      **/
     public final void setMarginVisible(boolean b) {
         clickables[FIRST].setVisible(b);
         clickables[LAST].setVisible(b);
     }
 
 
     /**
      * Are step buttons visible
      * @see #FORWARD
      * @see #BACKWARD
      **/
     public final boolean isStepVisible() {
         return clickables[BACKWARD].isVisible()  ||
             clickables[FORWARD].isVisible();
     }
 
 
     /**
      * Are step buttons visible
      * @see #FORWARD
      * @see #BACKWARD
      **/
     public final void setStepVisible(boolean b) {
         clickables[FORWARD].setVisible(b);
         clickables[BACKWARD].setVisible(b);
     }
 
     /**
      * Are block buttons visible
      * @see #FORWARD_BLOCK
      * @see #BACKWARD_BLOCK
      **/
     public final void setBlockVisible(boolean b) {
         clickables[FORWARD_BLOCK].setVisible(b);
         clickables[BACKWARD_BLOCK].setVisible(b);
     }
 
     /**
      * Are block buttons visible
      * @see #FORWARD_BLOCK
      * @see #BACKWARD_BLOCK
      **/
     public final boolean isBlockVisible() {
         return clickables[BACKWARD_BLOCK].isVisible()  ||
             clickables[FORWARD_BLOCK].isVisible();
     }
 
     public void setOrientation(int orientation) {
         super.setOrientation(orientation);
         resetIcons();
         initLayout();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initLayout()
     {
         removeAllComponents();
 
 
         SPanel backward = null;
         SPanel forward = null;
         if ( orientation == SConstants.VERTICAL) {
             backward = new SPanel(new SFlowDownLayout() );
             add( backward );
             
             forward = new SPanel(new SFlowDownLayout() );
             add( forward );
         } else {
             backward = new SPanel(new SFlowLayout() );
             add( backward );
             
             forward = new SPanel(new SFlowLayout() );
             add( forward );
         }
 
         backward.add(clickables[FIRST]);
         backward.add(clickables[BACKWARD_BLOCK]);
         backward.add(clickables[BACKWARD]);
 
         forward.add(clickables[FORWARD]);
         forward.add(clickables[FORWARD_BLOCK]);
         forward.add(clickables[LAST]);
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void refreshComponents() {
         // lower bound
         clickables[BACKWARD].setEnabled(getValue() > getMinimum());
         clickables[FIRST].setEnabled(clickables[BACKWARD].isEnabled());
         clickables[BACKWARD_BLOCK].setEnabled(clickables[BACKWARD].isEnabled());
 
         if ( clickables[BACKWARD].isEnabled() ) {
             clickables[BACKWARD].setEvent(getEventParameter(getValue()-1));
             int first = getMinimum();
             clickables[FIRST].setEvent(getEventParameter(first));
            int blockValue = Math.max(first,
                                       getValue()-getBlockIncrement());
             clickables[BACKWARD_BLOCK].setEvent(getEventParameter(blockValue));
         }
 
         // upper bound: maximum - extent
         clickables[FORWARD].setEnabled(getValue() < getMaximum()-getExtent());
         clickables[LAST].setEnabled(clickables[FORWARD].isEnabled());
         clickables[FORWARD_BLOCK].setEnabled(clickables[FORWARD].isEnabled());
 
         if ( clickables[FORWARD].isEnabled() ) {
             clickables[FORWARD].setEvent(getEventParameter(getValue()+1));
             int last = getMaximum()-getExtent();
             clickables[LAST].setEvent(getEventParameter(last));
             int blockValue = Math.min(last,
                                       getValue()+getBlockIncrement());
             clickables[FORWARD_BLOCK].setEvent(getEventParameter(blockValue));
         }
     }
 
     /**
      * Enables the component so that the knob position can be changed.
      * When the disabled, the knob position cannot be changed.
      *
      * @param b a boolean value, where true enables the component and
      *          false disables it
      */
     public void setEnabled(boolean x)  {
         super.setEnabled(x);
         SComponent[] children = getComponents();
         for(int i = 0; i < children.length; i++) {
             children[i].setEnabled(x);
         }
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(ScrollBarCG cg) {
         super.setCG(cg);
     }
 
     public String toString()
     {
         return "SScrollBar[orientation=" +
             ((orientation == SComponent.HORIZONTAL)?"horizontal":"vertical") + "]";
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */

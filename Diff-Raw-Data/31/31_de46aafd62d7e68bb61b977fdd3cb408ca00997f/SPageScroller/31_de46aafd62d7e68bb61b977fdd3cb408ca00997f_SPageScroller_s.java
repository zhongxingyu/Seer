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
 import java.util.ArrayList;
 
 import javax.swing.Icon;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 import org.wings.border.SEmptyBorder;
 import org.wings.util.AnchorProperties;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SPageScroller
     extends SAbstractAdjustable
 {
 
     /**
      * Default maximum amount of page clickables.
      */
     private static final int DEFAULT_DIRECT_PAGES = 10;
 
     /**
      *
      */
     private static final SEmptyBorder DEFAULT_DIRECT_CLICKABLE_BORDER = 
         new SEmptyBorder(0,5,0,5);
 
     public static final int FORWARD = 0;
     public static final int BACKWARD = 1;
     public static final int FIRST = 2;
     public static final int LAST = 3;
 
     /**
      * Icons for both layout modes, both directions and enabled resp. disabled
      **/
     private final static SIcon[][][] DEFAULT_ICONS =
         new SIcon[2][4][SClickable.ICON_COUNT];
 
     // Initialisiert (laedt) die Default Images
     static {
         String[] postfixes = new String[4];
         String[] prefixes = new String[4];
         for ( int orientation=0; orientation<2; orientation++ ) {
             prefixes[BACKWARD] = "";
             prefixes[FORWARD] = "";
             prefixes[FIRST] = "Margin";
             prefixes[LAST] = "Margin";
             if ( orientation==SConstants.VERTICAL ) {
                 postfixes[BACKWARD] = "Up";
                 postfixes[FORWARD] = "Down";
                 postfixes[FIRST] = "Up";
                 postfixes[LAST] = "Down";
             }
             else {
                 postfixes[BACKWARD] = "Left";
                 postfixes[FORWARD] = "Right";
                 postfixes[FIRST] = "Left";
                 postfixes[LAST] = "Right";
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
      * Actual amount of page clickables; depends on the number of elemnts in the
      * model and on the models extent.
      * @see #setDirectPages
      */
     protected int directPages = DEFAULT_DIRECT_PAGES;
 
     /*
      * how to layout the scroller, vertical or horizontal
      * @see #setLayoutMode
      */
     protected int layoutMode;
     
     /** contains the clickables forward, backward, first, last */
     protected SClickable[] clickables = new SClickable[4];
 
     /** contains the direct page clickables. Size of this array is extend */
     protected SClickable[] directPageClickables;
 
     /**  */
     protected SLabel pageCountLabel = new SLabel();
 
     /**
      * Creates a scrollbar with the specified orientation,
      * value, extent, mimimum, and maximum.
      * The "extent" is the size of the viewable area. It is also known
      * as the "visible amount".
      *
      * @exception IllegalArgumentException if orientation is not one of VERTICAL, HORIZONTAL
      *
      * @see #setOrientation
      * @see #setValue
      * @see #setVisibleAmount
      * @see #setMinimum
      * @see #setMaximum
      */
     public SPageScroller(int orientation, int value, int extent, int min, int max) {
         super(new SPagingBoundedRangeModel(value, extent, min, max));
 
         for ( int i=0; i<clickables.length; i++ ) {
             clickables[i] = new SClickable();
             clickables[i].setEventTarget(this);
         }
 
         setOrientation(orientation);
         setMarginVisible(false);
         setHorizontalAlignment(CENTER);
         setVerticalAlignment(CENTER);
 
         setLayout(new SBorderLayout());
 
         initScrollers();
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
     public SPageScroller(int orientation) {
         this(orientation, 0, 1, 0, 100);
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
     public SPageScroller() {
         this(SConstants.VERTICAL);
     }
 
     /**
      * 
      **/
     public SLabel getPageCountLabel() {
         return pageCountLabel;
     }
 
     /**
      * 
      **/
     protected void setPageCountText(int pages) {
         pageCountLabel.setText("/" + pages);
     }
 
     /**
      * to set your favorite icons and text of the clickable. Icons will be
      * reset to default, if you change the layout mode {@link #setLayoutMode}
      * @see #FORWARD
      * @see #BACKWARD
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
      * set how to layout components
      * {@link #VERTICAL} or {@link #HORIZONTAL}
      **/
     public void setLayoutMode(int orientation) {
         switch (orientation) {
         case SConstants.VERTICAL:
         case SConstants.HORIZONTAL:
             layoutMode = orientation;
             resetIcons();
             initLayout();
             break;
         default:
             throw new IllegalArgumentException("layout mode must be one of: VERTICAL, HORIZONTAL");
         }
     }
 
     /**
      * Sets the default icons 
      */
     public void resetIcons() {
         for ( int i=0; i<clickables.length; i++ ) {
             clickables[i].setIcons(DEFAULT_ICONS[layoutMode][i]);
         } 
     }
 
     /**
      * Sets the amount of page clickables to <code>count</code>.
      */
     public final int getDirectPages() {
         return directPages;
     }
 
     /**
      * Sets the amount of page clickables to <code>count</code>.
      * @param count : New amount of page clickables.
      */
     public void setDirectPages(int count) {
         if ( directPages!=count ) {
             directPages = count;
             initScrollers();
         }
     }
 
     public final int getPageCount() {
         // avoid division by zero
         if ( getExtent()==0 )
             return 0;

        return ((getMaximum()+1)+(getExtent()-1)-getMinimum())/getExtent();
     }
 
     public final int getActualPage() {
         // avoid division by zero
         if ( getExtent()==0 )
             return 0;
 
         return (getValue()-getMinimum()+getExtent()-1)/getExtent();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected SClickable createDirectPageClickable() {
         SClickable result = new SClickable();
         result.setBorder(DEFAULT_DIRECT_CLICKABLE_BORDER);
         result.setHorizontalAlignment(CENTER);
         return result;
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initScrollers() {
         directPageClickables = new SClickable[directPages];
         for (int i = 0; i<directPageClickables.length; i++) {
             directPageClickables[i] = createDirectPageClickable();
             directPageClickables[i].setEventTarget(this);
         }
 
         initLayout();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initLayout() {
         removeAllComponents();
 
         SPanel forwardPanel = null;
         SPanel backwardPanel = null;
         SPanel middlePanel = null;
         if ( layoutMode==SConstants.VERTICAL) {
             middlePanel = new SPanel(new SFlowDownLayout());
 
             backwardPanel = new SPanel(new SFlowDownLayout());
             add(backwardPanel, SBorderLayout.NORTH);
 
             forwardPanel = new SPanel(new SFlowDownLayout());
             add(forwardPanel, SBorderLayout.SOUTH);
         }
         else {
             middlePanel = new SPanel(new SFlowLayout());
 
             backwardPanel = new SPanel(new SFlowLayout());
             add(backwardPanel, SBorderLayout.WEST);
 
             forwardPanel = new SPanel(new SFlowLayout());
             add(forwardPanel, SBorderLayout.EAST);
 
         }
 
         backwardPanel.add(clickables[FIRST]);
         backwardPanel.add(clickables[BACKWARD]);
         forwardPanel.add(clickables[FORWARD]);
         forwardPanel.add(clickables[LAST]);
 
         add(middlePanel, SBorderLayout.CENTER);
 
         // Clickables fuer die Seiten einfuegen
         for (int i = 0; i<directPageClickables.length; i++) {
             middlePanel.add(directPageClickables[i]);
         }
 
 
         refreshComponents();
     }
 
     protected String formatDirectPageLabel(int page) {
         return Integer.toString(page+1);
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void refreshComponents() {
         // lower bound
         clickables[BACKWARD].setEnabled(getValue() > getMinimum());
         clickables[FIRST].setEnabled(clickables[BACKWARD].isEnabled());
 
         if ( clickables[BACKWARD].isEnabled() ) {
             clickables[BACKWARD].setEvent(getEventParameter(getValue()-getExtent()));
             clickables[FIRST].setEvent(getEventParameter(getMinimum()));
         }
 
         // upper bound: maximum - extent
         clickables[FORWARD].setEnabled(getValue() < getMaximum()-getExtent());
         clickables[LAST].setEnabled(clickables[FORWARD].isEnabled());
 
         if ( clickables[FORWARD].isEnabled() ) {
             clickables[FORWARD].setEvent(getEventParameter(getValue()+getExtent()));
             clickables[LAST].setEvent(getEventParameter(getMaximum()-getExtent()));
         }
 
 
         // overall pages 
         int pages = getPageCount();
 
         setPageCountText(pages);
 
        int actualPage = getActualPage();
 
         // prefer forward
        int firstDirectPage = actualPage - (directPages-1)/2;
         firstDirectPage = Math.min(firstDirectPage, pages-directPages);
         firstDirectPage = Math.max(firstDirectPage, 0);
 
         // reset alle page clickables
         for ( int i=0; i<directPageClickables.length; i++ ) {
             directPageClickables[i].setVisible(false);
         }
 
         for ( int i=0; 
               i<Math.min(directPageClickables.length, pages-firstDirectPage);
               i++ ) {
 
             int page = firstDirectPage+i;
 
             directPageClickables[i].setText(formatDirectPageLabel(page));
             directPageClickables[i].setVisible(true);
 
             directPageClickables[i].setEvent(getEventParameter(page*getExtent()));
 
            if ( page==actualPage ) {
                 directPageClickables[i].setEnabled(false);
                 directPageClickables[i].setSelected(true);
            } else {
                 directPageClickables[i].setEnabled(true);
                 directPageClickables[i].setSelected(false);
             }
             
         }
     }
 
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
 
 
 

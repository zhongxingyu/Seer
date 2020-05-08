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
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.Icon;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SPageScroller
     extends AbstractAdjustable
 {
     /**
      * Default maximum amount of page buttons.
      * @see setPageButtonCount()
      */
     private static final int DEFAULT_DIRECT_PAGES = 5;
 
     /**
      * Access to the default Icons for button status.
      */
     private static final int DISABLED = 0;
     private static final int ENABLED = 1;
 
     /**
      * Access to the default Icons for buttons
      */
     private static final int FORWARD = 0;
     private static final int BACKWARD = 1;
 
     /**
      * Icons for both orientations, both directions and enabled resp. disabled
      **/
     private final static ResourceImageIcon[][][] DEFAULT_ICONS =
         new ResourceImageIcon[2][2][2];
 
     // Initialisiert (laedt) die Default Images
     static {
         String[] postfixes = new String[2];
         for ( int orientation=0; orientation<2; orientation++ ) {
             if ( orientation==SConstants.VERTICAL ) {
                 postfixes[BACKWARD] = "Up";
                 postfixes[FORWARD] = "Down";
             }
             else {
                 postfixes[BACKWARD] = "Left";
                 postfixes[FORWARD] = "Right";
             }
 
             for ( int direction=0; direction<postfixes.length; direction++ ) {
                 DEFAULT_ICONS[orientation][direction][ENABLED] =
                    new ResourceImageIcon("org/wings/icons/BlockScroll" +
                                          postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][DISABLED] =
                    new ResourceImageIcon("org/wings/icons/DisabledBlockScroll"
                                          + postfixes[direction] + ".gif");
             }
         }
     }
 
 
     /**
      * Actual amount of page buttons; depends on the number of elemnts in the
      * model and on the models extent.
      * @see setDirectPages()
      */
     protected int directPages = DEFAULT_DIRECT_PAGES;
 
     /*
      * @see #setOrientation
      */
     protected int orientation;
 
     private final SButton forwardPage = new SButton("");
     private final SButton backwardPage = new SButton("");
 
     // immer so viele directPageButtons, wie extent angegeben
     protected SButton[] directPageButtons = null;
 
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
     public SPageScroller(int orientation, int value, int extent, int min, int max) {
         super(value, extent, min, max);
 
         setOrientation(orientation);
 
         setLayout(new SBorderLayout());
 
         forwardPage.addActionListener(scrollerAction);
         backwardPage.addActionListener(scrollerAction);
 
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
      *<P>Sets the visible amount to new value.</P>
      * @param amount : New value of visible amount.
      */
     public void setExtent(int amount) {
         getModel().setExtent(amount);
     }
 
     /**
      * Returns the component's orientation (horizontal or vertical).
      *
      * @return VERTICAL or HORIZONTAL
      * @see #setOrientation
      * @see java.awt.Adjustable#getOrientation
      */
     public int getOrientation() {
         return orientation;
     }
 
 
     /**
      * Set the scrollbar's orientation to either VERTICAL or
      * HORIZONTAL.
      *
      * @exception IllegalArgumentException if orientation is not one of VERTICAL, HORIZONTAL
      * @see #getOrientation
      * @beaninfo
      *    preferred: true
      *        bound: true
      *    attribute: visualUpdate true
      *  description: The scrollbar's orientation.
      *         enum: VERTICAL JScrollBar.VERTICAL
      *               HORIZONTAL JScrollBar.HORIZONTAL
      */
     public void setOrientation(int orientation) {
         this.orientation = orientation;
         checkOrientation();
     }
 
     /**
      * Checks if given orientation is valid.
      * @param orientation : Given orientation.
      **/
     private void checkOrientation() {
         switch (orientation) {
         case SConstants.VERTICAL:
         case SConstants.HORIZONTAL:
             resetIcons();
             break;
         default:
             throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
         }
     }
 
     /**
      * Sets the proper icons for buttonstatus enabled resp. disabled.
      */
     public void resetIcons() {
         forwardPage.setIcon(DEFAULT_ICONS[orientation][FORWARD][ENABLED]);
         forwardPage.setDisabledIcon(DEFAULT_ICONS[orientation][FORWARD][DISABLED]);
 
         backwardPage.setIcon(DEFAULT_ICONS[orientation][BACKWARD][ENABLED]);
         backwardPage.setDisabledIcon(DEFAULT_ICONS[orientation][BACKWARD][DISABLED]);
     }
 
     /**
      * TODO: documentation
      */
     protected final ActionListener scrollerAction = new ActionListener() {
         /**
          * TODO: documentation
          *
          * @param e
          */
         public void actionPerformed(ActionEvent e) {
             BoundedRangeModel model = getModel();
 
             if ( e.getSource()==forwardPage ) {
                 model.setValue(model.getValue()+model.getExtent());
             }
             else if ( e.getSource()==backwardPage ) {
                 model.setValue(model.getValue()-model.getExtent());
             }
             else { // check all Page Buttons
                 for (int i=0; i<directPageButtons.length; i++) {
                     if ( e.getSource()==directPageButtons[i] ) {
 
                         // die Anzahl der maximalen Seiten anpassen auf Model Werte
                         int directPages = Math.min(SPageScroller.this.directPages,
                                                    model.getMaximum()/model.getExtent());
 
                         int directStart = model.getValue() - (directPages/2)*model.getExtent();
                         directStart = Math.max(directStart, model.getMinimum());
                         directStart = Math.min(directStart, model.getMaximum()-
                                                (directPages*model.getExtent()));
 
                         model.setValue(i*model.getExtent()+directStart);
 
                         break;
                     }
                 }
             }
         }
     };
 
     /**
      * Sets the amount of page buttons to <code>count</code>.
      * @param count : New amount of page buttons.
      */
     public void setDirectPages(int count) {
         directPages = count;
         initScrollers();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initScrollers() {
         // Alte Buttons entfernen
         if ( directPageButtons!=null )
             for (int i = 0; i<directPageButtons.length; i++)
                 // alte Knoepfe entfernen, so vorhanden
                 if (directPageButtons[i] != null) {
                     directPageButtons[i].removeActionListener(scrollerAction);
                 }
 
         directPageButtons = new SButton[directPages];
         for (int i = 0; i<directPageButtons.length; i++) {
             directPageButtons[i] = new SButton();
             directPageButtons[i].addActionListener(scrollerAction);
         }
 
         initLayout();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initLayout() {
         removeAllComponents();
 
         if ( orientation==SConstants.VERTICAL) {
             add(backwardPage, SBorderLayout.NORTH);
             add(forwardPage, SBorderLayout.SOUTH);
         }
         else {
             add(backwardPage, SBorderLayout.WEST);
             add(forwardPage, SBorderLayout.EAST);
         }
 
         SPanel middlePanel = new SPanel(new SFlowLayout());
         add(middlePanel, SBorderLayout.CENTER);
 
 
         // Buttons fuer die Seiten einfuegen
         for (int i = 0; i<directPageButtons.length; i++) {
             middlePanel.add(directPageButtons[i]);
         }
 
         refreshComponents();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void refreshComponents() {
         BoundedRangeModel model = getModel();
 
         // linker Rand
         if (model.getValue() <= model.getMinimum()) {
             backwardPage.setEnabled(false);
         }
         else {
             backwardPage.setEnabled(true);
         }
 
         // upper bound: maximum - extent
         if (model.getValue() >= (model.getMaximum() - model.getExtent())) {
             forwardPage.setEnabled(false);
         }
         else {
             forwardPage.setEnabled(true);
         }
 
         // die Anzahl der maximalen Seiten anpassen auf Model Werte
         int directPages = Math.min(this.directPages, model.getMaximum()/model.getExtent());
 
         int directStart = model.getValue() - (directPages/2)*model.getExtent();
         directStart = Math.max(directStart, model.getMinimum());
         directStart = Math.min(directStart, model.getMaximum()-(directPages*model.getExtent()));
 
         System.out.println(model);
         System.out.println(directStart);
 
         for ( int i=0; i<directPageButtons.length; i++ ) {
             directPageButtons[i].setText(""+(directStart/model.getExtent()+i+1));
 
             if ( directStart+((i+1)*model.getExtent())>model.getMaximum() ) {
                 directPageButtons[i].setVisible(false);
             }
             else {
                 directPageButtons[i].setVisible(true);
             }
 
             if ( model.getValue()>=directStart+(i*model.getExtent()) &&
                  model.getValue()<directStart+((i+1)*model.getExtent()) ) {
                 directPageButtons[i].setEnabled(false);
             }
             else {
                 directPageButtons[i].setEnabled(true);
             }
         }
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */

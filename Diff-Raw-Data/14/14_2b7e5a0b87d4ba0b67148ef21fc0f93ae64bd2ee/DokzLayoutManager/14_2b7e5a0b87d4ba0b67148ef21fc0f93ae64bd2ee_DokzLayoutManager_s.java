 /*******************************************************************************
  * Dokz
  * Copyright (c) 2011-2 Siniatech Ltd  http://www.siniatech.com/products/dokz
  *
  * All rights reserved. This project and the accompanying materials are made 
  * available under the terms of the MIT License which can be found in the root  
  * of the project, and at http://www.opensource.org/licenses/mit-license.php
  *
  ******************************************************************************/
 package com.siniatech.dokz.layout;
 
 import static com.siniatech.siniautils.swing.BoundsHelper.*;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.siniatech.dokz.DokzContainer;
 import com.siniatech.dokz.DokzPanel;
 import com.siniatech.dokz.DokzPanelState;
 import com.siniatech.dokz.context.DokzContext;
 import com.siniatech.dokz.docking.DockingGlassPanel;
 import com.siniatech.siniautils.collection.CollectionHelper;
 import com.siniatech.siniautils.fn.IConditional;
 import com.siniatech.siniautils.fn.Tuple2;
 
 public class DokzLayoutManager implements LayoutManager {
 
     private static final int MaxedZOrder = 0;
     private static final int DockingZOrder = 0;
 
     static private final ILayouter scalingLayouter = new ScalingLayouter();
     static private final ILayouter tilingLayouter = new TilingLayouter();
     static private final ILayouter removingLayouter = new RemovingLayouter();
 
     private final DokzContext dokzContext;
 
     private Collection<DokzPanel> lastLaidOutComponents;
     private Tuple2<DokzPanel, Rectangle> lastMaxedPanel;
     private final DokzContainer dokzContainer;
 
     public DokzLayoutManager( DokzContainer dokzContainer, DokzContext dokzContext ) {
         this.dokzContainer = dokzContainer;
         this.dokzContext = dokzContext;
         this.lastLaidOutComponents = new HashSet<>();
     }
 
     @Override
     public void addLayoutComponent( String name, Component comp ) {
         dokzContainer.setComponentZOrder( comp, standardZOrder() );
     }
 
     private int standardZOrder() {
         return dokzContainer.getComponentCount() - 1;
     }
 
     @Override
     public void removeLayoutComponent( Component comp ) {
     }
 
     @Override
     public Dimension preferredLayoutSize( Container parent ) {
         return getExtentOfComponents( lastLaidOutComponents );
     }
 
     @Override
     public Dimension minimumLayoutSize( Container parent ) {
         return new Dimension( 600, 600 );
     }
 
     @Override
     public void layoutContainer( Container parent ) {
         assert dokzContainer == parent;
         if ( hasMaximizedChild() ) {
             layoutWithMaximizedChild();
         } else {
             restoreMaxedChildIfPresent();
             layoutDockingPanel();
             layoutWithoutMaximizedChild();
         }
     }
 
     private void layoutDockingPanel() {
         DockingGlassPanel glassPanel = dokzContainer.getDockingManager().getDockingGlassPanel();
         glassPanel.setBounds( 0, 0, dokzContainer.getWidth(), dokzContainer.getHeight() );
         dokzContainer.setComponentZOrder( glassPanel, DockingZOrder );
     }
 
     private void restoreMaxedChildIfPresent() {
         if ( lastMaxedPanel != null ) {
             lastMaxedPanel._1().setBounds( lastMaxedPanel._2().getBounds() );
             dokzContainer.setComponentZOrder( lastMaxedPanel._1(), standardZOrder() );
             lastMaxedPanel = null;
         }
     }
 
     private boolean hasMaximizedChild() {
         return CollectionHelper.exists( getPanels(), isMaximized() );
     }
 
     private IConditional<DokzPanel> isMaximized() {
         return new IConditional<DokzPanel>() {
             @Override
             public Boolean apply( DokzPanel t ) {
                 return dokzContext.getPanelContext( t ).getState() == DokzPanelState.maxed;
             }
         };
     }
 
     private void layoutWithMaximizedChild() {
         DokzPanel maxedPanel = CollectionHelper.find( getPanels(), isMaximized() );
         lastMaxedPanel = new Tuple2<>( maxedPanel, maxedPanel.getBounds() );
         maxedPanel.setBounds( 0, 0, dokzContainer.getWidth(), dokzContainer.getHeight() );
         dokzContainer.setComponentZOrder( maxedPanel, MaxedZOrder );
     }
 
     private void layoutWithoutMaximizedChild() {
         Set<DokzPanel> currentComponents = getPanels();
         if ( lastLaidOutComponents.equals( currentComponents ) ) {
             layoutSameComponents();
         } else {
             layoutRemainingComponents( currentComponents );
         }
         lastLaidOutComponents = currentComponents;
     }
 
     private void layoutRemainingComponents( Set<DokzPanel> currentComponents ) {
         if ( currentComponents.size() > lastLaidOutComponents.size() ) {
             assert lastLaidOutComponents.isEmpty();
             tilingLayouter.doLayout( currentComponents, dokzContainer.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
         } else {
             removingLayouter.doLayout( currentComponents, dokzContainer.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
         }
     }
 
     private void layoutSameComponents() {
        if ( getExtentOfComponents( lastLaidOutComponents ) != dokzContainer.getSize() ) {
             scalingLayouter.doLayout( lastLaidOutComponents, dokzContainer.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
         }
         // else do nothing
     }
 
     private Set<DokzPanel> getPanels() {
         Set<DokzPanel> panels = new HashSet<>();
         for ( DokzPanel panel : dokzContext.getPanels() ) {
             if ( dokzContext.getPanelContext( panel ).isVisibleIn( dokzContainer ) ) {
                 panels.add( panel );
             }
         }
         return panels;
     }
 
     /**
      * If the layout of the container is manipulated externally, call this
      * method to let the layout manager know about the current layout.
      */
     public void applyCurrentLayout() {
         lastLaidOutComponents = getPanels();
     }
 
 }

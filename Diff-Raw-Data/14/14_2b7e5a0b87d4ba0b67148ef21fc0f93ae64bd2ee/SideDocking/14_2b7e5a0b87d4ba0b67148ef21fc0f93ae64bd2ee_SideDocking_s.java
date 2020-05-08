 /*******************************************************************************
  * Dokz
  * Copyright (c) 2011-2 Siniatech Ltd  http://www.siniatech.com/products/dokz
  *
  * All rights reserved. This project and the accompanying materials are made 
  * available under the terms of the MIT License which can be found in the root  
  * of the project, and at http://www.opensource.org/licenses/mit-license.php
  *
  ******************************************************************************/
 package com.siniatech.dokz.docking;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.siniatech.dokz.DokzContainer;
 import com.siniatech.dokz.DokzPanel;
 import com.siniatech.dokz.context.DokzContext;
 import com.siniatech.dokz.layout.DokzLayoutManager;
 import com.siniatech.dokz.layout.ILayouter;
 import com.siniatech.dokz.layout.RemovingLayouter;
 import com.siniatech.dokz.layout.ScalingLayouter;
 import com.siniatech.dokz.layout.TranslatingLayoutContext;
 import com.siniatech.dokz.layout.TranslatingLayouter;
 
 abstract public class SideDocking implements IDocking {
 
     static protected ILayouter removingLayouter = new RemovingLayouter();
     static protected ILayouter scalingLayouter = new ScalingLayouter();
     static protected ILayouter translatingLayouter = new TranslatingLayouter();
 
     @Override
     public void showPotentialPositioning( DokzContainer dokzContainer, DockingGlassPanel glassPanel, DokzPanel dockingPanel ) {
         glassPanel.setPotentialDockingZone( getDockingZone( dokzContainer, dockingPanel ) );
     }
 
     abstract protected Rectangle getDockingZone( DokzContainer dokzContainer, DokzPanel dockingPanel );
 
     @Override
     public void applyDocking( DokzContainer dokzContainer, DokzPanel dockingPanel ) {
         Rectangle oldDockingPanelBounds = dockingPanel.getBounds();
         Rectangle containerBounds = dokzContainer.getBounds();
         dokzContainer.remove( dockingPanel );
         DokzContext dokzContext = dokzContainer.getDokzContext();
         Set<DokzPanel> panels = getPanels( dokzContainer );
         removingLayouter.doLayout( panels, dokzContainer.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
        scalingLayouter.doLayout( panels, getScalingBounds( oldDockingPanelBounds, containerBounds ) );
         translatingLayouter.doLayout( panels, dokzContainer.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap(), createTranslatingLayoutContext( oldDockingPanelBounds ) );
         dokzContainer.add( dockingPanel );
         dockingPanel.setBounds( createDockedBounds( oldDockingPanelBounds, containerBounds ) );
         applyNewLayout( dokzContainer );
     }
 
     abstract protected Rectangle createDockedBounds( Rectangle oldDockingPanelBounds, Rectangle containerBounds );
 
     abstract protected TranslatingLayoutContext createTranslatingLayoutContext( Rectangle oldDockingPanelBounds );
 
     abstract protected Dimension getScalingBounds( Rectangle oldDockingPanelBounds, Rectangle containerBounds );
 
     static protected void applyNewLayout( DokzContainer dokzContainer ) {
         DokzLayoutManager layoutManager = (DokzLayoutManager) dokzContainer.getLayout();
         layoutManager.applyCurrentLayout();
     }
 
     static protected Set<DokzPanel> getPanels( DokzContainer dokzContainer ) {
         Set<DokzPanel> panels = new HashSet<>();
         DokzContext dokzContext = dokzContainer.getDokzContext();
         for ( DokzPanel panel : dokzContext.getPanels() ) {
             if ( dokzContext.getPanelContext( panel ).isVisibleIn( dokzContainer ) ) {
                 panels.add( panel );
             }
         }
         return panels;
     }
 
 }

 package com.siniatech.dokz.layout;
 
 import static com.siniatech.siniautils.swing.BoundsHelper.*;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.siniatech.dokz.DokzContainer;
 import com.siniatech.dokz.DokzPanel;
 import com.siniatech.dokz.context.DokzContext;
 
 public class DokzLayoutManager implements LayoutManager {
 
     static private final ILayouter scalingLayouter = new ScalingLayouter();
     static private final ILayouter tilingLayouter = new TilingLayouter();
 
     private final DokzContext dokzContext;
 
     private Collection<DokzPanel> lastLaidOutComponents;
 
     public DokzLayoutManager( DokzContext dokzContainerContext ) {
         this.dokzContext = dokzContainerContext;
         this.lastLaidOutComponents = new HashSet<>();
     }
 
     @Override
     public void addLayoutComponent( String name, Component comp ) {
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
         Set<DokzPanel> currentComponents = getPanels( (DokzContainer) parent );
        if ( lastLaidOutComponents.equals( currentComponents ) ) {
             layoutSameComponents( parent );
         } else {
             layoutNewComponents( parent, currentComponents );
         }
         lastLaidOutComponents = currentComponents;
     }
 
     private void layoutNewComponents( Container parent, Set<DokzPanel> currentComponents ) {
         tilingLayouter.doLayout( currentComponents, parent.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
     }
 
     private void layoutSameComponents( Container parent ) {
         if ( getExtentOfComponents( lastLaidOutComponents ) != parent.getSize() ) {
             scalingLayouter.doLayout( lastLaidOutComponents, parent.getSize(), dokzContext.getPanelGap(), dokzContext.getPanelGap() );
         }
         // else do nothing
     }
 
     private Set<DokzPanel> getPanels( DokzContainer parent ) {
         Set<DokzPanel> panels = new HashSet<>();
         for ( DokzPanel panel : dokzContext.getPanels() ) {
             if ( dokzContext.getPanelContext( panel ).isVisibleIn( parent ) ) {
                 panels.add( panel );
             }
         }
         return panels;
     }
 
 }

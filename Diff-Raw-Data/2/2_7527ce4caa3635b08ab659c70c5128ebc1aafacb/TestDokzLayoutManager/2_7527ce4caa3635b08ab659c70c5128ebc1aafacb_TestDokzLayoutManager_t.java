 package com.siniatech.dokz.layout;
 
 import static com.siniatech.siniautils.collection.SetHelper.*;
 import static junit.framework.Assert.*;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.siniatech.dokz.DokzConstants;
 import com.siniatech.dokz.DokzContainer;
 import com.siniatech.dokz.DokzManager;
 import com.siniatech.dokz.DokzPanel;
 import com.siniatech.dokz.context.DokzContext;
 
 public class TestDokzLayoutManager {
 
     private DokzContainer mainContainer;
     private DokzLayoutManager layoutManager;
 
     @Before
     public void setUp() {
         DokzManager dokz = new DokzManager();
         dokz.add( new JPanel() );
         dokz.add( new JPanel() );
         dokz.add( new JPanel() );
         DokzContext dokzContext = (DokzContext) dokz.getDokzContext();
         mainContainer = dokzContext.getMainContainer();
        layoutManager = new DokzLayoutManager( mainContainer, dokzContext );
     }
 
     @Test
     public void canAddLayoutComponent() {
         // smoke test
         layoutManager.addLayoutComponent( "test", new JPanel() );
     }
 
     @Test
     public void canRemoveLayoutComponent() {
         // smoke test - note it doesn't matter that it's not been added
         layoutManager.removeLayoutComponent( new JPanel() );
     }
 
     @Test
     public void preferredLayoutSizeShouldFillContainer() {
         assertEquals( new Dimension( 0, 0 ), layoutManager.preferredLayoutSize( mainContainer ) );
         checkPreferredLayoutSize( new Dimension( 1000, 800 ) );
         checkPreferredLayoutSize( new Dimension( 400, 600 ) );
         checkPreferredLayoutSize( new Dimension( 90, 120 ) );
     }
 
     private void checkPreferredLayoutSize( Dimension d ) {
         mainContainer.setSize( d );
         layoutManager.layoutContainer( mainContainer );
         assertEquals( mainContainer.getSize(), layoutManager.preferredLayoutSize( mainContainer ) );
         assertEquals( d, mainContainer.getSize() );
     }
 
     @Test
     public void checkLayoutOfMainContainer() {
         mainContainer.setSize( new Dimension( 1000 + DokzConstants.defaultPanelGap, 600 + DokzConstants.defaultPanelGap ) );
         layoutManager.layoutContainer( mainContainer );
         DokzContext dokzContext = mainContainer.getDokzContext();
         Set<Rectangle> expectedBounds = asSet( //
             new Rectangle( 0, 0, 500, 300 ), //
             new Rectangle( 500 + DokzConstants.defaultPanelGap, 0, 500, 300 ), //
             new Rectangle( 0, 300 + DokzConstants.defaultPanelGap, 1000 + DokzConstants.defaultPanelGap, 300 ) //
         );
         Set<Rectangle> actualBounds = new HashSet<>();
         for ( DokzPanel panel : dokzContext.getPanels() ) {
             actualBounds.add( panel.getBounds() );
         }
         assertEquals( expectedBounds, actualBounds );
     }
     // TODO - test popped out panels are not laid out
     // TODO - test resizing layout
 }

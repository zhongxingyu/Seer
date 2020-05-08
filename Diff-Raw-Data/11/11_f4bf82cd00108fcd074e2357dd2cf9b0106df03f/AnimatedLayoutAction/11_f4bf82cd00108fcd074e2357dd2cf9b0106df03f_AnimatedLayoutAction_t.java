 package cytoscape.actions;
 
 import java.awt.event.ActionEvent;
 import javax.swing.*;
 
 import cytoscape.view.NetworkView;
 import cytoscape.dialogs.GraphObjectSelection;
 
 import phoebe.*;
 import phoebe.util.*;
 import phoebe.event.*;
 import java.util.*;
 
 import giny.model.*;
 import giny.view.*;
 import giny.util.*;
 
 public class AnimatedLayoutAction extends AbstractAction {
 
   NetworkView networkView;
   boolean bool = false;
   public AnimatedLayoutAction ( NetworkView networkView ) {
     super("Animate Layout");
     this.networkView = networkView;
   }
 
   public void actionPerformed (ActionEvent e) {
 
     JDialog dialog = new JDialog();
     JPanel main = new JPanel();
 
     main.add(  new JButton (new AbstractAction( "Grand Tour" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   PGrandTour tour = new PGrandTour( gv );
                   tour.takeTour();
                 }
               } ); } } ) );
 
     main.add(  new JButton (new AbstractAction( "Force" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   ModelBasedSpringLayout mbsl = new ModelBasedSpringLayout( gv );
                   mbsl.doLayout();
                 }
               } ); } } ) );
 
     main.add(  new JButton (new AbstractAction( "FR" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   FRLayout fr = new FRLayout( gv );
                   fr.doLayout();
                 }
               } ); } } ) );
 
     main.add(  new JButton (new AbstractAction( "ISOM" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   ISOMLayout isom = new ISOMLayout( gv );
                   isom.doLayout();
                 }
               } ); } } ) );
 
     main.add(  new JButton (new AbstractAction( "JUNG" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   JUNGSpringLayout jung = new JUNGSpringLayout( gv );
                   jung.doLayout();
                 }
               } ); } } ) );
 
     main.add(  new JButton (new AbstractAction( "ForceDirected" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  PGraphView gv = ( PGraphView )networkView.getView();
                  ForceDirectedLayout fdl = new ForceDirectedLayout( gv );
                  fdl.doLayout();
                }
              } ); } } ) );

 
     main.add(  new JButton (new AbstractAction( "Sugiyama" ) {
         public void actionPerformed ( ActionEvent e ) {
           // Do this in the GUI Event Dispatch thread...
           SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                 PGraphView gv = ( PGraphView )networkView.getView();
                 String file = null;
                   JFileChooser chooser = new JFileChooser( "/users/xmas/CSBI/cytoscape/testData");
                 chooser.setDialogTitle( "Load Tier Data" );
                 if( chooser.showOpenDialog( gv.getComponent() ) == chooser.APPROVE_OPTION ) {
                   file = chooser.getSelectedFile().toString();
     }
                 TieredInputReader reader = new TieredInputReader( gv );
                 List fTierList = reader.readGraphFromInteractionsFile(file);
                 Sugiyama sugiyama = new Sugiyama( gv );
                 sugiyama.layout( fTierList, true );
 
               }
             } ); } } ) );
   
 
      main.add(  new JButton (new AbstractAction( "Tree Select" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   PGraphView gv = ( PGraphView )networkView.getView();
                   int[] sel = gv.getSelectedNodeIndices();
                   TreeLayout tl = new TreeLayout();
                   GraphPerspective p = tl.doLayout( gv ) ;
                   int[] edges = p.getEdgeIndicesArray();
                   for ( int i = 0; i < p.getEdgeCount(); ++i ) {
                     //System.out.println( "Index of Edge is: "+edges[i] );
                     EdgeView ev = gv.getEdgeView( edges[i] );
                     ev.setSelected( true );
                   }
 
 
                 }
               } ); } } ) );
 
 
     main.add(  new JButton (new AbstractAction( "Tree" ) {
         public void actionPerformed ( ActionEvent e ) {
           // Do this in the GUI Event Dispatch thread...
           SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                 PGraphView gv = ( PGraphView )networkView.getView();
                 int[] sel = gv.getSelectedNodeIndices();
                  TreeLayout tl = new TreeLayout();
                  tl.doLayout( gv ) ;
               }
             } ); } } ) );
     
 
     main.add( new JLabel( "  ") );
 
 
     main.add(  new JButton (new AbstractAction( "Update" ) {
         public void actionPerformed ( ActionEvent e ) {
           // Do this in the GUI Event Dispatch thread...
           SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                 PGraphView gv = ( PGraphView )networkView.getView();
                 Iterator nodes = gv.getNodeViewsIterator();
                 while ( nodes.hasNext() ) {
                   ( ( PNodeView )nodes.next() ).setNodePosition( false );
                 }
               }
             } ); } } ) );
      main.add(  new JButton (new AbstractAction( "Animate" ) {
         public void actionPerformed ( ActionEvent e ) {
           // Do this in the GUI Event Dispatch thread...
           SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                 PGraphView gv = ( PGraphView )networkView.getView();
                 Iterator nodes = gv.getNodeViewsIterator();
                 while ( nodes.hasNext() ) {
                   ( ( PNodeView )nodes.next() ).setNodePosition( true );
                 }
               }
             } ); } } ) );
 
 
      
     
  
 
     dialog.getContentPane().add( main );
     dialog.pack();
     dialog.setVisible( true );
 
   }
 }

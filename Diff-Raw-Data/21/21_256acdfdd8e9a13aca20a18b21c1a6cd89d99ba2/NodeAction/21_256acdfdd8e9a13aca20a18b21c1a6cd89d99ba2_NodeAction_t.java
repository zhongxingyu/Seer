 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 
 
 package cytoscape.graphutil;
 
 import cytoscape.view.*;
 import cytoscape.browsers.*;
 
 import java.util.*;
 import giny.model.*;
 import giny.view.*;
 import phoebe.*;
 import edu.umd.cs.piccolo.*;
 import edu.umd.cs.piccolo.activities.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class NodeAction {
     final static String SHAPEMENUCAPTION[] = {
         "Diamond","Ellipse","Hexagon","Octagon","Triangle","Parallelogram","Rectangle"};
     final static int SHAPEMENUVALUE[] = {
         PNodeView.DIAMOND,PNodeView.ELLIPSE,PNodeView.HEXAGON,PNodeView.OCTAGON,
         PNodeView.TRIANGLE,PNodeView.PARALELLOGRAM,PNodeView.RECTANGLE};
     private static class ShapeMenuAction extends AbstractAction {
         private int i;
         private PNodeView nv;
         public ShapeMenuAction(int index, PNodeView nodeview) {
             super(SHAPEMENUCAPTION[index]);
             i = index;
             nv = nodeview;
         }
         public void actionPerformed(ActionEvent e) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     nv.setShape(SHAPEMENUVALUE[i]);
                 }
             });
         }
     }
 
   public NodeAction () {
   }
 
   /**
   * get the Title (label) for a PNodeView
    */
  public static String getTitle (Object unused[], PNode node ) {
      if (node instanceof PNodeView) {
          return ((PNodeView)node).getLabel().getText();
      } else {
          return "";
      }
   }
 
   /**
    * This will open a Node Attribute browser
    */
   public static JMenuItem viewNodeAttributeBrowser ( Object[] args, PNode node ) {
     final NetworkView network = ( NetworkView )args[0];
     final PNodeView view = ( PNodeView )node;
     return new JMenuItem( new AbstractAction( "Attribute Browser" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                   List nodes = network.getView().getSelectedNodes();
                   Object[] objects;
                   if ( !nodes.isEmpty() ) {
                     if ( !view.isSelected() ) {
                       nodes.add( view );
                     }
                     objects = new Object[ nodes.size() ];
                     for ( int i = 0; i < nodes.size(); ++i ) {
                       objects[i] = ( ( NodeView )nodes.get(i) ).getNode();
                     }
                   } else {
                     objects = new Object[] { view.getNode() };
                   }
 
     TabbedBrowser nodeBrowser = new TabbedBrowser ( objects,
                                                     network.getNetwork().getNodeAttributes(),
                                                     new Vector(),
                                                     network.getCytoscapeObj().
                                                     getConfiguration().getProperties().
                                                     getProperty("webBrowserScript",
                                                                  "noScriptDefined") ,
                                                     TabbedBrowser.BROWSING_NODES );
      } } ); } } );
   }
 
 
   /**
    * Instant Node Editing
    */
   public static JMenuItem editNode ( Object[] args, PNode node ) {
     final NetworkView network = ( NetworkView )args[0];
     final GraphView v = network.getView();
     final PNodeView nv = ( PNodeView )node;
 
     JMenu editMenu = new JMenu( "<html>Node Editing <I><small>(short-term)</I></small></html>");
     editMenu.add( new JMenuItem( new AbstractAction( "Color" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  JColorChooser color = new JColorChooser();
                  nv.setUnselectedPaint( color.showDialog( v.getComponent() , "Choose a Node Color", (java.awt.Color)nv.getUnselectedPaint() ) );
                } } ); } } ) );
 
     int i;
     JCheckBoxMenuItem jmi;
     JMenu shapeMenu = new JMenu( "Shape" );
     for (i = 0; i < SHAPEMENUCAPTION.length; i++) {
         jmi = new JCheckBoxMenuItem( new ShapeMenuAction(i, nv));
         jmi.setSelected(nv.getShape() == SHAPEMENUVALUE[i]);
         shapeMenu.add(jmi);
     }
     editMenu.add(shapeMenu);
     return editMenu;
 }
 
 
   /**
    * This will open an web page that will give you more info.
    */
   public static JMenuItem openWebInfo ( Object[] args, PNode node ) {
 
     final PNode nv = node;
 
     JMenu web_menu = new JMenu( "Web Info" );
 
     web_menu.add(  new JMenuItem( new AbstractAction( "<html>SGD <small><i>yeast only</i></small></html>" ) {
         public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL( "http://db.yeastgenome.org/cgi-bin/SGD/locus.pl?locus="+gene );
 
                 } } ); } } ) );
 
     web_menu.add(  new JMenuItem( new AbstractAction( "Google" ) {
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL( "http://www.google.com/search?q="+gene);
 
                 } } ); } } ) );
 
 
     JMenu gn_menu = new JMenu( "GenomeNet" );
     web_menu.add( gn_menu );
     gn_menu.add(  new JMenuItem( new AbstractAction( "Pathway" ) {
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=pathway&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "KO" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=ko&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Genes" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genes&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Genome" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genome&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Ligand" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=ligand&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Compound" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=compound&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Glycan" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                       //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=glycan&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Reaction" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=reaction&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Enzyme" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=enzyme&keywords="+gene );
 
                 } } ); } }) );
 
            gn_menu.addSeparator();
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Swiss-Prot" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=swissprot&keywords="+gene );
 
                 } } ); } }) );
 
 
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Ref-Seq" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=refseq&keywords="+gene );
 
                 } } ); } } ));
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "GenBank" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genbank&keywords="+gene );
 
                 } } ); } } ));
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Embl" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PNodeView ) {
                     gene = ( ( PNodeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=embl&keywords="+gene );
 
                 } } ); } } ));
 
            return web_menu;
 
    }
 
 
 
   public static JMenuItem showData ( Object[] args, PNode node ) {
 
     final PNodeView nv = ( PNodeView )node;
     final PGraphView v = ( PGraphView )args[0];
     final GraphPerspective graphPerspective = v.getGraphPerspective();
 
     JMenu datashow_menu = new JMenu( "Show the Node" );
     datashow_menu.add( new JMenuItem( new AbstractAction( "As a Star Node" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                  PNode node = (PNode)v.addNodeView( "cytoscape.graphutil.StarNode",nv.getIndex());
 
                 } } ); } } ) );
 
 
     datashow_menu.add( new JMenuItem( new AbstractAction( "As a Grid Node" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                   PNode node = (PNode) v.addNodeView( "cytoscape.graphutil.GridNode",nv.getIndex());
                 } } ); } } ) );
 
 
     datashow_menu.add( new JMenuItem( new AbstractAction( "As a Petal Node" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                   PNode node = (PNode) v.addNodeView( "cytoscape.graphutil.PetalNode",nv.getIndex());
                 } } ); } } ) );
 
     datashow_menu.add( new JMenuItem( new AbstractAction( "As a Radar Node" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                   PNode node = (PNode) v.addNodeView( "cytoscape.graphutil.RadarNode",nv.getIndex());
                 } } ); } } ) );
 
      datashow_menu.add( new JMenuItem( new AbstractAction( "As a Clip Radar Node" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   //set nv to Clip Radar Node
 
                   PNode node = (PNode) v.addNodeView( "cytoscape.graphutil.ClipRadarNode",nv.getIndex());
                 } } ); } } ) );
 
 
      return datashow_menu;
    }//end of datashow menu
 
    public static JMenuItem changeFirstNeighbors (Object[] args, PNode node)
    {
 	  //final PNode thenode = node;
 	  final PNodeView nv = ( PNodeView )node;
     final PGraphView v = ( PGraphView )args[0];
 	  final GraphPerspective graphPerspective = v.getGraphPerspective();
 
     JMenu firstn_menu = new JMenu( "<html>Set First Neighbors <I><small>(short-term)</I></small></html>");
 
 
     firstn_menu.add( new JMenuItem( new AbstractAction( "Color" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  JColorChooser color = new JColorChooser();
 		 java.awt.Color custom = color.showDialog( v.getComponent() , "Choose a Node Color", (java.awt.Color)nv.getUnselectedPaint() );
 		 Node n = graphPerspective.getNode(nv.getIndex());
 
 		 List list = graphPerspective.neighborsList(n);
 		 Iterator i = list.iterator();
 		 while (i.hasNext())
 		{
 			Node neib =(Node) i.next();
 			NodeView neibview = (NodeView)v.getNodeView(neib.getRootGraphIndex());
 			neibview.setUnselectedPaint( custom);
 		}
 	 } } ); } } ) );
 
 
     JMenu shape_menu = new JMenu( "Shape" );
     firstn_menu.add( shape_menu );
 	 shape_menu.add( new JMenuItem( new AbstractAction( "To parallelogram" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
 		 Node n = graphPerspective.getNode(nv.getIndex());
 
 		 List list = graphPerspective.neighborsList(n);
 		 Iterator i = list.iterator();
 		 while (i.hasNext())
 		{
 			Node neib =(Node) i.next();
 			NodeView neibview = (NodeView)v.getNodeView(neib.getRootGraphIndex());
 			neibview.setShape( NodeView.PARALELLOGRAM );
 		}
 
                } } ); } } ) );
 
 	 shape_menu.add( new JMenuItem( new AbstractAction( "To circles" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
 		 Node n = graphPerspective.getNode(nv.getIndex());
 
 		 List list = graphPerspective.neighborsList(n);
 		 Iterator i = list.iterator();
 		 while (i.hasNext())
 		{
 			Node neib =(Node) i.next();
 			NodeView neibview = (NodeView)v.getNodeView(neib.getRootGraphIndex());
 			neibview.setShape( PNodeView.ELLIPSE );
 		}
 
                } } ); } } ) );
 
 	 shape_menu.add( new JMenuItem( new AbstractAction( "To triangles" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
 		 Node n = graphPerspective.getNode(nv.getIndex());
 
 		 List list = graphPerspective.neighborsList(n);
 		 Iterator i = list.iterator();
 		 while (i.hasNext())
 		{
 			Node neib =(Node) i.next();
 			NodeView neibview = (NodeView)v.getNodeView(neib.getRootGraphIndex());
 			neibview.setShape( PNodeView.TRIANGLE );
 		}
 
                } } ); } } ) );
 
 	  shape_menu.add( new JMenuItem( new AbstractAction( "To diamonds" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
 		 Node n = graphPerspective.getNode(nv.getIndex());
 
 		 List list = graphPerspective.neighborsList(n);
 		 Iterator i = list.iterator();
 		 while (i.hasNext())
 		{
 			Node neib =(Node) i.next();
 			NodeView neibview = (NodeView)v.getNodeView(neib.getRootGraphIndex());
 			neibview.setShape( PNodeView.DIAMOND );
 		}
 
                } } ); } } ) );
 
      return firstn_menu;
    }//end of changeFirstNeighbors
 
     public static JMenuItem zoomToNode ( Object[] args, PNode node ) {
     final PNode n = ( PNode )node;
 
      final PGraphView v = ( PGraphView )args[0];
     return new  JMenuItem( new AbstractAction( "Zoom To" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
 		       PTransformActivity activity =  v.getCanvas().getCamera().animateViewToCenterBounds( n.getGlobalFullBounds(), true, 500 );
                } } ); } } );
   }
   public static JMenuItem zoomToNeighbors ( Object[] args, PNode node ) {
     final PNode n = ( PNode )node;
 
      final PGraphView v = ( PGraphView )args[0];
     return new  JMenuItem( new AbstractAction( "Zoom To Neghbors" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  PTransformActivity activity =  v.getCanvas().getCamera().animateViewToCenterBounds( n.getUnionOfChildrenBounds(n.getFullBounds()), true, 500 );
 
 		       //PTransformActivity activity =  v.getCanvas().getCamera().animateViewToCenterBounds( n.getGlobalFullBounds(), true, 500 );
                } } ); } } );
   }
 }

 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.geom.Rectangle2D;
 import javax.swing.*;
 
 import org.w3c.dom.NodeList;
 
 import prefuse.Display;
 import prefuse.Visualization;
 import prefuse.action.ActionList;
 import prefuse.action.RepaintAction;
 import prefuse.action.assignment.ColorAction;
 import prefuse.action.assignment.DataSizeAction;
 import prefuse.action.layout.SpecifiedLayout;
 import prefuse.controls.PanControl;
 import prefuse.data.Graph;
 import prefuse.data.Table;
 import prefuse.data.io.CSVTableReader;
 import prefuse.render.DefaultRendererFactory;
 import prefuse.render.LabelRenderer;
 import prefuse.util.ColorLib;
 import prefuse.util.collections.IntIterator;
 import prefuse.util.ui.UILib;
 import prefuse.visual.VisualItem;
 import prefuse.visual.VisualTable;
 
 public class Vis extends JPanel {
 
 	/* Displays */
 	private static TagCloud tagcloud;
 	private static radialview feelingsgraph;
 	private static Box feelingsbox;
 	
 	private static JFrame frame;
 	
 	/* Constants */
 	private static final int vis_width = 600;
 	private static final int vis_height = 690;
 	
 	public static void main(String[] args) {
         Database.init();
         setupTagVis();
 		setupGraphVis();
         setFrame();
     }
 	
 	
 	/* Set up the graph visualization */
 	private static void setupGraphVis() {
 	    UILib.setPlatformLookAndFeel();
 	    
 		NodeList nl = feelingsgraph.getAssociatedFeelingsFromXMLFile("feel.xml");
         Graph g2 = feelingsgraph.buildGraph(nl);
         
         feelingsgraph = new radialview(g2,"name");
         feelingsgraph.setMyTC(tagcloud);
         feelingsbox = radialview.buildBottomBox(feelingsgraph, feelingsgraph.getVisualization(), "name");
 	}
 	
 	
 	/* Set up the tag visualization */
 	private static void setupTagVis() {
		TagCloud tagcloud = new TagCloud("blessed", vis_width, vis_height-130, 8, 3);
 		Vis.tagcloud = tagcloud;
 	}
 	
 	/* Combine the graph and tagcloud togethers */
 	private static void setFrame() {
 		frame = new JFrame("M I X E D F E E L I N G S");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(feelingsgraph);
 		frame.add(tagcloud.display);
 		frame.add(feelingsbox,BorderLayout.SOUTH);
 		frame.pack(); 
 		frame.setSize(vis_width, vis_height);
 		frame.setVisible(true); 
 	}
 	
 }

 /*
  * Copyright (c) 2003, the JUNG Project and the Regents of the University of
  * California All rights reserved.
  * 
  * This software is open-source under the BSD license; see either "license.txt"
  * or http://jung.sourceforge.net/license.txt for a description.
  * 
  */
 package samples.graph;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import edu.uci.ics.graph.Graph;
 import edu.uci.ics.jung.graph.TestGraphs;
 import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationModel;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
 import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.ViewScalingControl;
 import edu.uci.ics.jung.visualization.decorators.DefaultToolTipFunction;
 import edu.uci.ics.jung.visualization.decorators.EdgeShape;
 import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
 import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintFunction;
 import edu.uci.ics.jung.visualization.decorators.VertexShapeFunction;
 import edu.uci.ics.jung.visualization.layout.FRLayout;
 import edu.uci.ics.jung.visualization.picking.MultiPickedState;
 import edu.uci.ics.jung.visualization.picking.PickSupport;
 import edu.uci.ics.jung.visualization.picking.PickedState;
 import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
 import edu.uci.ics.jung.visualization.transform.MutableTransformer;
 
 /**
  * Demonstrates 3 views of one graph in one model with one layout.
  * Each view uses a different scaling graph mouse.
  * 
  * @author Tom Nelson - RABA Technologies
  * 
  */
 public class MultiViewDemo extends JApplet {
 
     /**
      * the graph
      */
     Graph graph;
 
     /**
      * the visual components and renderers for the graph
      */
     VisualizationViewer vv1;
     VisualizationViewer vv2;
     VisualizationViewer vv3;
     
     /**
      * the normal transformer
      */
     MutableTransformer transformer;
     
     Dimension preferredSize = new Dimension(300,300);
     
     final String messageOne = "The mouse wheel will scale the model's layout when activated"+
     " in View 1. Since all three views share the same layout transformer, all three views will"+
     " show the same scaling of the layout.";
     
     final String messageTwo = "The mouse wheel will scale the view when activated in"+
     " View 2. Since all three views share the same view transformer, all three views will be affected.";
     
     final String messageThree = "   The mouse wheel uses a 'crossover' feature in View 3."+
     " When the combined layout and view scale is greater than '1', the model's layout will be scaled."+
     " Since all three views share the same layout transformer, all three views will show the same "+
     " scaling of the layout.\n   When the combined scale is less than '1', the scaling function"+
     " crosses over to the view, and then, since all three views share the same view transformer,"+
     " all three views will show the same scaling.";
     
     JTextArea textArea;
     JScrollPane scrollPane;
     
     /**
      * create an instance of a simple graph in two views with controls to
      * demo the zoom features.
      * 
      */
     public MultiViewDemo() {
         
         // create a simple graph for the demo
         graph = TestGraphs.getOneComponentGraph();
         
         // create one layout for the graph
         FRLayout layout = new FRLayout(graph);
         layout.setMaxIterations(1000);
         
         // create one model that all 3 views will share
         VisualizationModel visualizationModel =
             new DefaultVisualizationModel(layout, preferredSize);
  
         // create 3 views that share the same model
         vv1 = new VisualizationViewer(visualizationModel, preferredSize);
         vv2 = new VisualizationViewer(visualizationModel, preferredSize);
         vv3 = new VisualizationViewer(visualizationModel, preferredSize);
         
         vv1.getRenderContext().setEdgeShapeFunction(new EdgeShape.Line());
         vv2.getRenderContext().setVertexShapeFunction(new VertexShapeFunction() {
             public Shape getShape(Object v) {
                 return new Rectangle2D.Float(-6, -6, 12, 12);
             }
         });
         vv2.getRenderContext().setEdgeShapeFunction(new EdgeShape.QuadCurve());
         
         vv3.getRenderContext().setEdgeShapeFunction(new EdgeShape.CubicCurve());
 
         transformer = vv1.getLayoutTransformer();
         vv2.setLayoutTransformer(transformer);
         vv3.setLayoutTransformer(transformer);
         
         vv2.setViewTransformer(vv1.getViewTransformer());
         vv3.setViewTransformer(vv1.getViewTransformer());
         
         vv1.setBackground(Color.white);
         vv2.setBackground(Color.white);
         vv3.setBackground(Color.white);
         
         // create one pick support for all 3 views to share
         PickSupport pickSupport = new ShapePickSupport(vv1);
         vv1.setPickSupport(pickSupport);
         vv2.setPickSupport(pickSupport);
         vv3.setPickSupport(pickSupport);
 
         // create one picked state for all 3 views to share
         PickedState pes = new MultiPickedState();
         PickedState pvs = new MultiPickedState();
         vv1.setPickedVertexState(pvs);
         vv2.setPickedVertexState(pvs);
         vv3.setPickedVertexState(pvs);
         vv1.setPickedEdgeState(pes);
         vv2.setPickedEdgeState(pes);
         vv3.setPickedEdgeState(pes);
         
         // set an edge paint function that shows picked edges
         vv1.getRenderContext().setEdgePaintFunction(new PickableEdgePaintFunction(pes, Color.black, Color.red));
         vv2.getRenderContext().setEdgePaintFunction(new PickableEdgePaintFunction(pes, Color.black, Color.red));
         vv3.getRenderContext().setEdgePaintFunction(new PickableEdgePaintFunction(pes, Color.black, Color.red));
         vv1.getRenderContext().setVertexPaintFunction(new PickableVertexPaintFunction(pvs, Color.black, Color.red, Color.yellow));
         vv2.getRenderContext().setVertexPaintFunction(new PickableVertexPaintFunction(pvs, Color.black, Color.red, Color.yellow));
         vv3.getRenderContext().setVertexPaintFunction(new PickableVertexPaintFunction(pvs, Color.black, Color.red, Color.yellow));
         
         // add default listener for ToolTips
         vv1.setToolTipFunction(new DefaultToolTipFunction());
         vv2.setToolTipFunction(new DefaultToolTipFunction());
         vv3.setToolTipFunction(new DefaultToolTipFunction());
         
         Container content = getContentPane();
         JPanel panel = new JPanel(new GridLayout(1,0));
         
         final JPanel p1 = new JPanel(new BorderLayout());
         final JPanel p2 = new JPanel(new BorderLayout());
         final JPanel p3 = new JPanel(new BorderLayout());
         
         p1.add(new GraphZoomScrollPane(vv1));
         p2.add(new GraphZoomScrollPane(vv2));
         p3.add(new GraphZoomScrollPane(vv3));
         
         JButton h1 = new JButton("?");
         h1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 textArea.setText(messageOne);
                 JOptionPane.showMessageDialog(p1, scrollPane, 
                         "View 1", JOptionPane.PLAIN_MESSAGE);
             }});
         JButton h2 = new JButton("?");
         h2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 textArea.setText(messageTwo);
                 JOptionPane.showMessageDialog(p2, scrollPane, 
                         "View 2", JOptionPane.PLAIN_MESSAGE);
             }});
         JButton h3 = new JButton("?");
         h3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 textArea.setText(messageThree);
                 textArea.setCaretPosition(0);
                 JOptionPane.showMessageDialog(p3, scrollPane, 
                         "View 3", JOptionPane.PLAIN_MESSAGE);
            }});
         
         // create a GraphMouse for each view
         // each one has a different scaling plugin
         DefaultModalGraphMouse gm1 = new DefaultModalGraphMouse() {
             protected void loadPlugins() {
                 pickingPlugin = new PickingGraphMousePlugin();
                 animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
                 translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
                 scalingPlugin = new ScalingGraphMousePlugin(new LayoutScalingControl(), 0);
                 rotatingPlugin = new RotatingGraphMousePlugin();
                 shearingPlugin = new ShearingGraphMousePlugin();
 
                 add(scalingPlugin);
                setMode(Mode.TRANSFORMING);
             }
         };
 
         DefaultModalGraphMouse gm2 = new DefaultModalGraphMouse() {
             protected void loadPlugins() {
                 pickingPlugin = new PickingGraphMousePlugin();
                 animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
                 translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
                scalingPlugin = new ScalingGraphMousePlugin(new ViewScalingControl(), 0);
                 rotatingPlugin = new RotatingGraphMousePlugin();
                 shearingPlugin = new ShearingGraphMousePlugin();
 
                 add(scalingPlugin);
                setMode(Mode.TRANSFORMING);
             }
        	
         };
 
         DefaultModalGraphMouse gm3 = new DefaultModalGraphMouse() {};
         
         vv1.setGraphMouse(gm1);
         vv2.setGraphMouse(gm2);
         vv3.setGraphMouse(gm3);
 
         vv1.setToolTipText("<html><center>MouseWheel Scales Layout</center></html>");
         vv2.setToolTipText("<html><center>MouseWheel Scales View</center></html>");
         vv3.setToolTipText("<html><center>MouseWheel Scales Layout and<p>crosses over to view<p>ctrl+MouseWheel scales view</center></html>");
  
         vv1.addPostRenderPaintable(new BannerLabel(vv1, "View 1"));
         vv2.addPostRenderPaintable(new BannerLabel(vv2, "View 2"));
         vv3.addPostRenderPaintable(new BannerLabel(vv3, "View 3"));
         
         textArea = new JTextArea(6,30);
         scrollPane = new JScrollPane(textArea, 
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);
         textArea.setEditable(false);
         
         JPanel flow = new JPanel();
         flow.add(h1);
         flow.add(gm1.getModeComboBox());
         p1.add(flow, BorderLayout.SOUTH);
         flow = new JPanel();
         flow.add(h2);
         flow.add(gm2.getModeComboBox());
         p2.add(flow, BorderLayout.SOUTH);
         flow = new JPanel();
         flow.add(h3);
         flow.add(gm3.getModeComboBox());
         p3.add(flow, BorderLayout.SOUTH);
         
         panel.add(p1);
         panel.add(p2);
         panel.add(p3);
         content.add(panel);
         
 
     }
     
     class BannerLabel implements VisualizationViewer.Paintable {
         int x;
         int y;
         Font font;
         FontMetrics metrics;
         int swidth;
         int sheight;
         String str;
         VisualizationViewer vv;
         
         public BannerLabel(VisualizationViewer vv, String label) {
             this.vv = vv;
             this.str = label;
         }
         
         public void paint(Graphics g) {
             Dimension d = vv.getSize();
             if(font == null) {
                 font = new Font(g.getFont().getName(), Font.BOLD, 30);
                 metrics = g.getFontMetrics(font);
                 swidth = metrics.stringWidth(str);
                 sheight = metrics.getMaxAscent()+metrics.getMaxDescent();
                 x = (3*d.width/2-swidth)/2;
                 y = d.height-sheight;
             }
             g.setFont(font);
             Color oldColor = g.getColor();
             g.setColor(Color.gray);
             g.drawString(str, x, y);
             g.setColor(oldColor);
         }
         public boolean useTransform() {
             return false;
         }
     }
 
 
     /**
      * a driver for this demo
      */
     public static void main(String[] args) {
         JFrame f = new JFrame();
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.getContentPane().add(new MultiViewDemo());
         f.pack();
         f.setVisible(true);
     }
 }

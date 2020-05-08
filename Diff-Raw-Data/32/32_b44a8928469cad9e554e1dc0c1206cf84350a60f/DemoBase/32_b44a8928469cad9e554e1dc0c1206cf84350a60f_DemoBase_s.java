 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import y.anim.AnimationFactory;
 import y.anim.AnimationPlayer;
 import y.base.EdgeCursor;
 import y.base.Node;
 import y.base.NodeCursor;
 import y.io.GMLIOHandler;
 import y.io.YGFIOHandler;
 import y.layout.BufferedLayouter;
 import y.layout.GraphLayout;
 import y.util.D;
 import y.view.Graph2D;
 import y.view.Graph2DView;
 import y.view.Graph2DViewMouseWheelZoomListener;
 import y.view.LayoutMorpher;
 import y.view.LineType;
 import y.view.NavigationMode;
 //import yext.svg.io.SVGIOHandler;
 
 public class DemoBase extends Thread {
   /**
    * Initializes to a "nice" look and feel.
    */
   public static void initLnF() {
     try {
       if ( !"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(
               UIManager.getSystemLookAndFeelClassName()) &&
            !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(
               UIManager.getSystemLookAndFeelClassName()) &&
            !UIManager.getSystemLookAndFeelClassName().equals(
               UIManager.getLookAndFeel().getClass().getName() ) ) {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
       }
     }
     catch ( Exception e ) {
       e.printStackTrace();
     }
   }
 
 
   /**
    * The view component of this demo.
    */
   protected Graph2DView view;
     protected Graph2DView typeview;
   protected final JPanel contentPane;
   protected IncrementalHierarchicLayout incrementallayouter;
   protected IncrementalHierarchicLayout typelayouter;
   private String name;
   private LayouterClient client;
   protected JComboBox project_chooser;
   protected JComboBox graph_chooser;
   protected JSlider slider = new JSlider(JSlider.VERTICAL);
   public boolean updatingslider = false;
   protected HashMap<String, String> string_source_map = new HashMap<String, String>();
   protected JTextArea text;
   private JPanel graphpanel;
   private JSlider alphaslider;
   private boolean forcelayout = false;
   private JToggleButton compb;
   private JToggleButton debug;
   
   final JToolBar jtb;
   
   /**
    * This constructor creates the {@link #view}
    * and calls,
    * {@link #createToolBar()}
    * {@link #registerViewModes()}, {@link #registerViewActions()},
    * and {@link #registerViewListeners()}
    */
   protected DemoBase(String nam, LayouterClient cl) {
 	name = nam;
 	client = cl;
 
     view = new Graph2DView();
     view.setAntialiasedPainting( true );
 
     typeview = new Graph2DView();
     typeview.setAntialiasedPainting( true );
 
     contentPane = new JPanel();
     contentPane.setLayout( new BorderLayout() );
 
     JPanel left = new JPanel();
     left.setLayout( new BorderLayout() );
 
     registerViewModes();
     
     graphpanel = new JPanel();
     graphpanel.setLayout( new BorderLayout(3, 3) );
     left.add(graphpanel, BorderLayout.CENTER);
     
     graphpanel.add(view, BorderLayout.CENTER);
     graphpanel.add(typeview, BorderLayout.EAST);
 
     graph_chooser = new JComboBox(new SortedListComboBoxModel());
     graph_chooser.addItem(new ListElement("new..."));
     graph_chooser.setSelectedIndex(0);
     graph_chooser.setMaximumRowCount(50);
     graph_chooser.addActionListener(new ChangeGraphAction());
 
     project_chooser = new JComboBox(new SortedListComboBoxModel());
     project_chooser.setMaximumRowCount(50);
     project_chooser.addActionListener(new ChangeProjectAction());
     
     
     jtb = createToolBar();
     if ( jtb != null ) {
       left.add( jtb, BorderLayout.NORTH );
     }
 
     contentPane.add(left, BorderLayout.CENTER);
     
     JPanel right = new JPanel();
     right.setLayout( new BorderLayout() );
     
     JPanel textok = new JPanel();
     textok.setLayout( new BorderLayout() );
     
     JPanel choosers = new JPanel();
     choosers.setLayout( new BorderLayout() );
     
     //choosers.add(project_chooser, BorderLayout.NORTH);
     choosers.add(graph_chooser, BorderLayout.SOUTH);
     
     textok.add(choosers, BorderLayout.NORTH);
     
 
     text = new JTextArea("Choose code example or type code!", 15, 22);
     //text.setFont(new Font( "dialog", Font.PLAIN, 20));
     string_source_map.put("new...", text.getText());
     text.setEditable(true);
     
     JScrollPane textscroll = new JScrollPane(text);
     textscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
     textscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
     textok.add(textscroll, BorderLayout.CENTER );
 
     JButton send = new JButton("send");
     textok.add(send, BorderLayout.SOUTH );
     send.addActionListener(new SendAction());
 
     right.add(textok, BorderLayout.NORTH );
     
     slider.setPaintLabels(true);
     slider.setSnapToTicks(true);
     slider.setMinimum(0);
     slider.setMaximum(0);
     slider.addChangeListener(new ChangeSlider());
     slider.setEnabled(false);
     right.add(slider, BorderLayout.CENTER );
     contentPane.add( right, BorderLayout.EAST );
     
     alphaslider = new JSlider(JSlider.HORIZONTAL);
     right.add(alphaslider, BorderLayout.SOUTH);
     alphaslider.setMinimum(0);
     alphaslider.setMaximum(3);
     Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
     labels.put(0, new JLabel("CFG"));
     labels.put(3, new JLabel("Type"));
     alphaslider.setLabelTable(labels);
     alphaslider.setPaintLabels(true);
     alphaslider.setSnapToTicks(true);
     alphaslider.setValue(1);
     alphaslider.addChangeListener(new ChangeAlphaSlider());
   }
 
   private void switchViews (int newl) {
 	  if (newl == 0) { //cfg only
 		  graphpanel.remove(typeview);
 		  graphpanel.remove(view);
 		  graphpanel.add(view, BorderLayout.CENTER);
 	  } else if (newl == 1) {
 		  graphpanel.remove(typeview);
 		  graphpanel.remove(view);
 		  graphpanel.add(view, BorderLayout.CENTER);
 		  graphpanel.add(typeview, BorderLayout.EAST);		  
 	  } else if (newl == 2) {
 		  graphpanel.remove(typeview);
 		  graphpanel.remove(view);
 		  graphpanel.add(view, BorderLayout.CENTER);
 		  graphpanel.add(typeview, BorderLayout.SOUTH);		  
 	  } else if (newl == 3) { //type only
 		  graphpanel.remove(typeview);
 		  graphpanel.remove(view);
 		  graphpanel.add(typeview, BorderLayout.CENTER);
 	  }
 	  graphpanel.revalidate();
       view.fitContent();
       view.updateView();
       typeview.fitContent();
       typeview.updateView();
   }
   
   public String methodName () {
 	  String mname = text.getText();
 	  String def = "define method ";
 	  if (mname.startsWith(def))
 		  mname = mname.substring(def.length(), mname.indexOf(')', def.length() + 1) + 1).trim();
 	  return mname;
   }
   
   public boolean updatingguimanually = false;
   
   public void waitforstep (IncrementalHierarchicLayout i) {
 	  i.waiting = true;
 	  jtb.setBackground(Color.green);
 	  if (! i.graphfinished)
 		  slider.setEnabled(false);
 	  while(i.waiting && ! i.playpressed)
 		  try {
 			  Thread.sleep(300);
 		  } catch (InterruptedException e) {
 			  e.printStackTrace();
 		  }
 	  i.waiting = false;
 	  jtb.setBackground(Color.LIGHT_GRAY);
   }
   
   public boolean containsMethodHeader () {
 	  String mname = text.getText();
 	  String def = "define method ";
 	  if (mname.startsWith(def))
 		  return true;
 	  return false;
   }
   
   public void graphChanged (IncrementalHierarchicLayout ihl) {
 	  //compb.setSelected(false);
 	  incrementallayouter = ihl;
 	  updatingslider = true;
 	  //happens from time to time (some propertychanged event
 	  //which shouldn't be sent in the first place)...
 	  try { slider.setValue(0); } catch (NullPointerException e) { }
 	  try { slider.setMaximum(0); } catch (NullPointerException e) { }
 	  try { slider.setLabelTable(ihl.sliderLabels); } catch (NullPointerException e) { }
 	  try { slider.setMaximum(ihl.lastEntry); } catch (NullPointerException e) { }
 	  try { slider.setValue(ihl.lastslidervalue); } catch (NullPointerException e) { }
 	  if (incrementallayouter.graphfinished)
 		  slider.setEnabled(true);
 	  else
 		  slider.setEnabled(false);
 	  updatingslider = false;
 	  Color bgc = Color.LIGHT_GRAY;
 	  if (incrementallayouter.waiting)
 		  bgc = Color.GREEN;
 	  jtb.setBackground(bgc);
 	  calcLayout();
   }
   
   public void dispose() {
   }
 
   /**
    * Creates an application  frame for this demo
    * and displays it. The class name is the title of
    * the displayed frame.
    */
   public final void run() {
 	  JFrame frame = new JFrame( name );
 	  //frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	  frame.getRootPane().setContentPane( contentPane );
 	  frame.pack();
 	  frame.setSize(1400, 1000);
 	  frame.setLocationRelativeTo( null );
 	  frame.setVisible( true );
   }
 
   protected void registerViewModes() {
 	  view.getCanvasComponent().addMouseListener(new MyMouseListener(view));
 	  view.addViewMode(new NavigationMode());
 	  view.getCanvasComponent().addMouseWheelListener( new Graph2DViewMouseWheelZoomListener() );
 	  typeview.getCanvasComponent().addMouseListener(new MyMouseListener(typeview));
 	  typeview.addViewMode(new NavigationMode());
 	  typeview.getCanvasComponent().addMouseWheelListener( new Graph2DViewMouseWheelZoomListener() );
   }
 
   protected JToolBar createToolBar() {
     JToolBar toolBar = new JToolBar();
     toolBar.add( new Zoom( 1.2 ) );
     toolBar.add( new Zoom( 0.8 ) );
     toolBar.add( new FitContent( ) );
 	toolBar.add( new LayoutAction() );
 //	toolBar.add( new ForceLayoutAction() );
 //	compb = new JToggleButton(new CompareAction());
 //	toolBar.add( compb );
 	toolBar.add( new Play() );
 	toolBar.add( new Step() );
 //	toolBar.add( new SaveAction(view, "Flow Graph") );
 //	toolBar.add( new SaveAction(typeview, "Type Graph") );
 	debug = new JToggleButton(new DebugAction());
 	toolBar.add( debug );
     return toolBar;
   }
 
   public void activate (IncrementalHierarchicLayout ih) {
 	  if (ih != incrementallayouter) {
 		  String mname = ih.graph_id;
 		  text.setText(string_source_map.get(mname));
 		  updatingguimanually = true;
 		  for (int i = 0; i < graph_chooser.getItemCount(); i++)
 			  if (((ListElement)graph_chooser.getItemAt(i)).toString().equals(mname)) {
 				  graph_chooser.setSelectedIndex(i);
 				  break;
 			  }
 		  updatingguimanually = false;
 		  ih.activateLayouter();
 	  }
   }
 
   final class MyMouseListener implements MouseListener {
 	  private Graph2DView graph;
 	  public MyMouseListener (Graph2DView gr) {
 		  this.graph = gr;
 	  }
 	  
 	public void mouseClicked(MouseEvent arg0) {
 		Node selected = checkClick(graph, graph.getGraph2D(), arg0.getX(), arg0.getY());
 		if (selected == null)
 			unselect();
 		else
 			select(selected);
 	}
 	
 	public Node checkClick (Graph2DView graph, Graph2D g, int x, int y) {
 		double xv = graph.toWorldCoordX(x);
 		double yv = graph.toWorldCoordY(y);
 		for (NodeCursor nc = g.nodes(); nc.ok(); nc.next())
 			if (g.getRectangle(nc.node()).contains(xv, yv)) {
 				return nc.node();
 			}
 		return null;
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	public void mouseReleased(MouseEvent arg0) {
 	}
 	  
   }
   final class SendAction extends AbstractAction {
 	public void actionPerformed(ActionEvent ev) {
 		if (string_source_map.get(methodName()) == null)
 			if (string_source_map.get(methodName().substring(0, methodName().indexOf(' ')).trim()) == null) {
 			System.out.println("new method :" + methodName() + ":");
 			string_source_map.put(methodName(), text.getText());
 			ListElement newLE = new ListElement(methodName());
 			graph_chooser.addItem(newLE);
 			graph_chooser.setSelectedItem(newLE);
 		} 
 		if (client.getGraph(methodName()) == null) {
 			ArrayList data = new ArrayList();
 			data.add(new Symbol("compile"));
 			//data.add(new Symbol(methodName()));
 			//if (containsMethodHeader())
 				data.add(text.getText());
 			//else
 			//	data.add("define function test" + client.getGraphSize() + " () " + text.getText() + " end;");
 			client.printMessage(data);
 		}
 	}
   }
   
   final class ChangeProjectAction extends AbstractAction {
 		public void actionPerformed(ActionEvent ev) {
 			ArrayList data = new ArrayList();
 			data.add(new Symbol("open-project"));
 			data.add((String)project_chooser.getSelectedItem());
 			client.printMessage(data);					
 		} 
   }
   
   final class ChangeGraphAction extends AbstractAction {
 		public ChangeGraphAction() {
 			super("Change Graph");
 			this.putValue(Action.SHORT_DESCRIPTION, "Change Graph");
 		}
 		
 		public void actionPerformed(ActionEvent ev) {
 			if (! updatingguimanually ) {
 				String mname = ((ListElement)graph_chooser.getSelectedItem()).toString();
 				text.setText(string_source_map.get(mname));
 				if (client.getGraph(mname) != null) {
 					IncrementalHierarchicLayout ih = client.getGraph(mname);
 					ih.activateLayouter();
 				} else {
 					incrementallayouter = null;
 					updatingslider = true;
 					slider.setLabelTable(null);
 					slider.setMaximum(0);
 					slider.setEnabled(false);
 					updatingslider = false;
 					view.setGraph2D(new Graph2D());
 					view.repaint();
 					typeview.setGraph2D(new Graph2D());
 					typeview.repaint();
 					System.out.println("no graph yet, please wait");
 				}
 			}				
 		}
 	}
   
   final class ChangeSlider implements ChangeListener {
 	public void stateChanged(ChangeEvent arg0) {
 		if (!updatingslider && !slider.getValueIsAdjusting() && incrementallayouter.graphfinished) {
 			int step = slider.getValue();
 			incrementallayouter.resetGraph(step);
 		}
 	}
   }
  
   final class ChangeAlphaSlider implements ChangeListener {
 	public void stateChanged(ChangeEvent arg0) {
 		if (!alphaslider.getValueIsAdjusting()) {
 			int step = alphaslider.getValue();
 			switchViews(step);
 		}
 	}
   }
   /**
    * Action that applies a specified zoom level to the view.
    */
   protected class Zoom extends AbstractAction {
     double factor;
 
     public Zoom( double factor ) {
       super( "Zoom " + ( factor > 1.0 ? "In" : "Out" ) );
       this.putValue( Action.SHORT_DESCRIPTION, "Zoom " + ( factor > 1.0 ? "In" : "Out" ) );
       this.factor = factor;
     }
 
     public void actionPerformed( ActionEvent e ) {
       view.setZoom( view.getZoom() * factor );
       Rectangle box = view.getGraph2D().getBoundingBox();
       view.setWorldRect( box.x - 20, box.y - 20, box.width + 40, box.height + 40 );
       typeview.setZoom( typeview.getZoom() * factor );
       Rectangle box1 = typeview.getGraph2D().getBoundingBox();
       typeview.setWorldRect( box1.x - 20, box1.y - 20, box1.width + 40, box1.height + 40 );
 
       typeview.updateView();
       view.updateView();
     }
   }
 
   /**
    * Action that fits the content nicely inside the view.
    */
   protected class FitContent extends AbstractAction {
     public FitContent( ) {
       super( "Fit Content" );
       this.putValue( Action.SHORT_DESCRIPTION, "Fit Content" );
     }
 
     public void actionPerformed( ActionEvent e ) {
       view.fitContent();
       view.updateView();
       typeview.fitContent();
       typeview.updateView();
     }
   }
 
 	/**
 	 * Simple Layout action (incremental)
 	 */
 	final class LayoutAction extends AbstractAction	{
 		LayoutAction()
 		{
 			super("Layout");
 			this.putValue( Action.SHORT_DESCRIPTION, "Layout");
 		}
 		public void actionPerformed(ActionEvent ev)
 		{
 			//incrementallayouter.changed = true;
 			//incrementallayouter.typechanged = true;
 			forcelayout = true;
 			calcLayout();
 			forcelayout = false;
 		}
 	}
 	
 	final class ForceLayoutAction extends AbstractAction {
 		ForceLayoutAction ()
 		{
 			super("Force Layout");
 		}
 		public void actionPerformed(ActionEvent ev)
 		{
 			incrementallayouter.changed = true;
 			incrementallayouter.typechanged = true;
 			forcelayout = true;
 			calcLayout();
 			forcelayout = false;
 		}
 	}
 	
 	  final class SaveAction extends AbstractAction {
 		  JFileChooser chooser;
 		  Graph2DView view;
 		  
 		  public SaveAction(Graph2DView v, String t) {
 			  super( "Save " + t );
 			  chooser = null;
 			  view = v;
 		  }
 
 		  public void actionPerformed( ActionEvent e ) {
 			  if ( chooser == null ) {
 				  chooser = new JFileChooser();
 			  }
 			  if ( chooser.showSaveDialog( contentPane ) == JFileChooser.APPROVE_OPTION ) {
 				  String name = chooser.getSelectedFile().toString();
 				  if ( name.endsWith( ".gml" ) ) {
 					  GMLIOHandler ioh = new GMLIOHandler();
 					  try {
 						  ioh.write( view.getGraph2D(), name );
 					  } catch ( IOException ioe ) {
 						  D.show( ioe );
 					  }
 //				  } else if ( name.endsWith(".svg")) {
 //					  SVGIOHandler svg = new SVGIOHandler();
 //					  try {
 //						  svg.write( view.getGraph2D(), name );
 //					  } catch ( IOException ioe ) {
 //						  D.show( ioe );
 //					  }
 				  } else {
 					  if ( !name.endsWith( ".ygf" ) )
 						  name = name + ".ygf";
 					  YGFIOHandler ioh = new YGFIOHandler();
 					  try {
 						  ioh.write( view.getGraph2D(), name );
 					  } catch ( IOException ioe ) {
 						  D.show( ioe );
 					  }
 				  }
 			  }
 		  }
 	  }
 
 	  
 	final class CompareAction extends AbstractAction {
 		private IncrementalHierarchicLayout other = null;
 		CompareAction () {
 			super("Compare Graph");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			if (compb.isSelected()) {
 				IncrementalHierarchicLayout ihl2 = client.findComparableGraph(incrementallayouter.graph_id, incrementallayouter.graphfinished);
 				if (ihl2 != null) {
 					other = ihl2;
 					CompareGraphs.resetComparison(incrementallayouter);
 					CompareGraphs.resetComparison(ihl2);
 					CompareGraphs.compareColorize(incrementallayouter, ihl2);
 				} else
 					System.out.println("no suitable graph found for comparison");
 			} else {
 				CompareGraphs.resetComparison(incrementallayouter);
 				CompareGraphs.resetComparison(other);
 				other = null;
 			}
 		}
 	}
 	
 	final class DebugAction extends AbstractAction {
 		public DebugAction() {
 			super("Debug");
 			this.putValue( Action.SHORT_DESCRIPTION, "Debug");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			if (incrementallayouter != null) {
 				boolean d = debug.isSelected();
 				GraphNodeRealizer.setDebug(d);
 				for (NodeCursor nc = incrementallayouter.graph.nodes(); nc.ok(); nc.next())
 					((GraphNodeRealizer)incrementallayouter.graph.getRealizer(nc.node())).updateText();
 				for (NodeCursor nc = incrementallayouter.typegraph.nodes(); nc.ok(); nc.next())
 					((GraphNodeRealizer)incrementallayouter.typegraph.getRealizer(nc.node())).updateText();
 			}
 		}
 	}
 	
 	final class Play extends AbstractAction	{
 		Play() {
 			super("Play");
 			this.putValue( Action.SHORT_DESCRIPTION, "Play");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			incrementallayouter.playpressed = true;
 			if (incrementallayouter != null)
 				while (true)
 					if (! incrementallayouter.nextStep())
 						break;
 		}
 	}
 	
 	final class Step extends AbstractAction	{
 		Step() {
 			super("Step");
 			this.putValue( Action.SHORT_DESCRIPTION, "Step");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			incrementallayouter.waiting = false;
 			incrementallayouter.nextStep();
 		}
 		
 	}
 	
 	protected void unselect () {
 		if (incrementallayouter != null) {
 			Node old = incrementallayouter.selection;
 			if  (old != null) {
 				Graph2D gr = (Graph2D)old.getGraph();
 				gr.setSelected(old, false);
 				((GraphNodeRealizer)gr.getRealizer(old)).updateColor();
 				for (EdgeCursor ec = old.edges(); ec.ok(); ec.next()) {
 					if (gr == view.getGraph2D())
 						if (gr.getRealizer(ec.edge()).getLineColor() == Color.blue)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DASHED_2);
 						else
 							gr.getRealizer(ec.edge()).setLineType(LineType.LINE_2);
 					else //typegraph
 						if (gr.getRealizer(ec.edge()).getLineColor() == Color.red)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DOTTED_2);
 						else if (gr.getRealizer(ec.edge()).getLineColor() == Color.GREEN)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DASHED_2);
 						else
 							gr.getRealizer(ec.edge()).setLineType(LineType.LINE_2);
 					gr.getRealizer(ec.edge()).repaint();
 					((GraphNodeRealizer)(gr.getRealizer(ec.edge().opposite(old)))).setNeighbourSelected(false);
 				}
 				Node tt = findTNode(old);
 				if (tt != null) {
 					Graph2D gr2 = (Graph2D)tt.getGraph();
 					if (gr2 != null)
 						((GraphNodeRealizer)gr2.getRealizer(tt)).setReferenceSelected(false);
 				}
 				incrementallayouter.selection = null;
 			}
 		}
 	}
 	
 	private Node findTNode (Node a) {
 		if (incrementallayouter.tv_temp_map.containsKey(a))
 			return incrementallayouter.tv_temp_map.get(a);
 		if (incrementallayouter.tv_temp_map.containsValue(a))
 			for (Node s : incrementallayouter.tv_temp_map.keySet())
 				if (incrementallayouter.tv_temp_map.get(s) == a)
 					return s;
 		return null;
 	}
 	
 	protected void select (Node s) {
 		if (s != null) {
 			if (s != incrementallayouter.selection) {
 				unselect();
 				//System.out.println("selection now " + incrementallayouter.graph.getLabelText(s));
 				Graph2D gr = (Graph2D)s.getGraph();
 				gr.setSelected(s, true);
 				gr.getRealizer(s).repaint();
 				for (EdgeCursor ec = s.edges(); ec.ok(); ec.next()) {
 					if (gr == view.getGraph2D())
 						if (gr.getRealizer(ec.edge()).getLineColor() == Color.blue)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DASHED_4);
 						else
 							gr.getRealizer(ec.edge()).setLineType(LineType.LINE_4);
 					else //typegraph
 						if (gr.getRealizer(ec.edge()).getLineColor() == Color.red)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DOTTED_4);
 						else if (gr.getRealizer(ec.edge()).getLineColor() == Color.green)
 							gr.getRealizer(ec.edge()).setLineType(LineType.DASHED_4);
 						else
 							gr.getRealizer(ec.edge()).setLineType(LineType.LINE_4);
 					gr.getRealizer(ec.edge()).repaint();
 					((GraphNodeRealizer)gr.getRealizer(ec.edge().opposite(s))).setNeighbourSelected(true); 
 				}
 				Node tt = findTNode(s);
 				if (tt != null) {
 					Graph2D gr2 = (Graph2D)tt.getGraph();
 					if (gr2 != null)
 						((GraphNodeRealizer)gr2.getRealizer(tt)).setReferenceSelected(true); 
 				}
 				incrementallayouter.selection = s;
 			}
 		}
 	}
 
 	/**
 	 * Animated layout assignment
 	 */
 	public void calcLayout(){
 		//if (forcelayout) {
 		//System.out.println("calculating layout! (graph changed? " + incrementallayouter.changed + ", typegraph changed? " + incrementallayouter.typechanged + ")");
 		if (!view.getGraph2D().isEmpty() && incrementallayouter.changed){
 		    //System.out.println("calculating layout");
 			//if (alphaslider.getValue() == 3)
 			//	alphaslider.setValue(1);
 			//switchViews(typeview);
 			incrementallayouter.changed = false;
 			Cursor oldCursor = view.getCanvasComponent().getCursor();
 
 /*			for (NodeCursor nc = incrementallayouter.graph.nodes(); nc.ok(); nc.next()) {
 				Object hint = incrementallayouter.hintMap.get(nc.node()); 
 				if ((hint != null) && (hint instanceof Integer) && ((Integer)hint == 42))
 					incrementallayouter.hintMap.set(nc.node(), null);
 				else {
 					boolean data = true;
 					for (EdgeCursor ec = nc.node().edges(); ec.ok(); ec.next())
 						if (incrementallayouter.graph.getRealizer(ec.edge()).getLineColor() != Color.pink) {
 							data = false;
 							break;
 						}
 					if (data) {
 						if (nc.node().inDegree() == 1) {
 							System.out.println("found generator " + incrementallayouter.graph.getLabelText(nc.node()));
 							incrementallayouter.hintMap.set(nc.node(), incrementallayouter.hintsFactory.createLayerIncrementallyHint(nc.node().firstInEdge().source()));
 						} else if (nc.node().outDegree() == 1) {
 							System.out.println("found single user " + incrementallayouter.graph.getLabelText(nc.node()));
 							incrementallayouter.hintMap.set(nc.node(), incrementallayouter.hintsFactory.createLayerIncrementallyHint(nc.node().firstOutEdge().target()));
 						} else {
 							System.out.println("don't know what to do");
 							incrementallayouter.hintMap.set(nc.node(), incrementallayouter.hintsFactory.createLayerIncrementallyHint(nc.node()));
 						}
 					} else { 
 						Object newhint = incrementallayouter.hintsFactory.createLayerIncrementallyHint(nc.node());
 						incrementallayouter.hintMap.set(nc.node(), newhint);
 						for (EdgeCursor ec = nc.node().outEdges(); ec.ok(); ec.next())
 							if (incrementallayouter.graph.getRealizer(ec.edge()).getLineColor() == Color.pink) {
 								System.out.println("setting hint of " + incrementallayouter.graph.getLabelText(ec.edge().target()));
 								incrementallayouter.hintMap.set(ec.edge().target(), newhint);
 							}
 					}
 				}
 			} */
 			try {
 				view.getCanvasComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				incrementallayouter.calcSwimLanes();
 				GraphLayout layout = new BufferedLayouter(incrementallayouter.hierarchicLayouter).calcLayout(view.getGraph2D());
 				LayoutMorpher morpher = new LayoutMorpher(view, layout);
 				final AnimationPlayer player = new AnimationPlayer();
 				player.addAnimationListener(view);
 				if (alphaslider.getValue() == 3) {
 					morpher.setPreferredDuration(0);
 					player.setFps(0);
 				} else {
 					morpher.setSmoothViewTransform(true);
 					//morpher.setKeepZoomFactor(true);
 					morpher.setPreferredDuration(1500);
 					player.setFps(30);
 					//player.setBlocking(true);
 				}
 				player.animate(AnimationFactory.createEasedAnimation(morpher));
 			} catch (Exception e) {
 				System.out.println("got exception during layouting");
 				e.printStackTrace();
 			} finally {
 				view.getCanvasComponent().setCursor(oldCursor);
 				//incrementallayouter.hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
 			}
 			//for (NodeCursor nc = incrementallayouter.graph.nodes(); nc.ok(); nc.next())
 			//	incrementallayouter.hintMap.set(nc.node(), 42);
 		}
 		if (!typeview.getGraph2D().isEmpty() && incrementallayouter.typechanged){
 			incrementallayouter.typechanged = false;
 			//if (alphaslider.getValue() == 0)
 			//	alphaslider.setValue(1);
 			//switchViews(view);
 			Cursor oldCursor = typeview.getCanvasComponent().getCursor();
 			for (NodeCursor nc = incrementallayouter.typegraph.nodes(); nc.ok(); nc.next())
 				if (incrementallayouter.typeHintMap.get(nc.node()) == null)
 					incrementallayouter.typeHintMap.set(nc.node(), incrementallayouter.typeHintsFactory.createLayerIncrementallyHint(nc.node()));
 				else
 					incrementallayouter.typeHintMap.set(nc.node(), incrementallayouter.typeHintsFactory.createUseExactLayerCoordinatesHint(nc.node()));
 			try {
 				typeview.getCanvasComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				GraphLayout layout = new BufferedLayouter(incrementallayouter.typeLayouter).calcLayout(typeview.getGraph2D());
 				LayoutMorpher morpher = new LayoutMorpher(typeview, layout);
 				final AnimationPlayer player = new AnimationPlayer();
 				player.addAnimationListener(typeview);
 				if (alphaslider.getValue() == 0) {
 					morpher.setPreferredDuration(0);
 					player.setFps(0);
 				} else {
 					morpher.setSmoothViewTransform(true);
 					//morpher.setKeepZoomFactor(true);
 					morpher.setPreferredDuration(1500);
 					player.setFps(30);
 					//player.setBlocking(true);
 				}
 				player.animate(AnimationFactory.createEasedAnimation(morpher));
 			} catch (Exception e) {
 				System.out.println("got exception during layouting");
 				e.printStackTrace();
 			} finally {
 				typeview.getCanvasComponent().setCursor(oldCursor);
 			}
 		}
 		typeview.updateView();
 		view.updateView();
 		//}
 	}
 
 }

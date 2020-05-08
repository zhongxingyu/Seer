 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import y.anim.AnimationFactory;
 import y.anim.AnimationPlayer;
 import y.base.EdgeCursor;
 import y.base.Node;
 import y.base.NodeCursor;
 import y.layout.BufferedLayouter;
 import y.layout.GraphLayout;
 import y.view.DefaultBackgroundRenderer;
 import y.view.Graph2D;
 import y.view.Graph2DView;
 import y.view.Graph2DViewMouseWheelZoomListener;
 import y.view.LayoutMorpher;
 import y.view.LineType;
 import y.view.NavigationMode;
 import y.view.NodeRealizer;
 
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
   private JPanel left;
   private boolean typesForeground = false;
   private JSlider alphaslider;
   
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
 
     left = new JPanel();
     left.setLayout( new BorderLayout() );
 
     registerViewModes();
     //view.setOpaque(true);
     view.setPreferredSize(new Dimension(1100, 1000));
     ((DefaultBackgroundRenderer)view.getBackgroundRenderer()).setColor(new Color(0xff, 0xff, 0xff, 0x33));
 
     //typeview.setOpaque(true);
     typeview.setPreferredSize(new Dimension(1100, 1000));
     //((DefaultBackgroundRenderer)typeview.getBackgroundRenderer()).setColor(new Color(0xff, 0xff, 0xff, 0x33));
     typeview.getGlassPane().add(view);
 
     left.add( typeview, BorderLayout.CENTER );
 
     graph_chooser = new JComboBox(new SortedListComboBoxModel());
     graph_chooser.addItem(new ListElement(-1, "new..."));
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
     
 
     text = new JTextArea("Choose code example or type code!", 8, 20);
     text.setFont(new Font( "dialog", Font.PLAIN, 20));
     string_source_map.put("new...", text.getText());
     text.setEditable(true);
     textok.add(text, BorderLayout.CENTER );
 
     JButton send = new JButton("send");
     textok.add(send, BorderLayout.SOUTH );
     send.addActionListener(new SendAction());
 
     right.add(textok, BorderLayout.NORTH );
     
     slider.setPaintLabels(true);
     slider.setSnapToTicks(true);
     slider.setMinimum(0);
     slider.setMaximum(0);
     slider.addChangeListener(new ChangeSlider());
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
     alphaslider.addChangeListener(new ChangeAlphaSlider());
   }
 
   private void switchViews (Graph2DView fg) {
 	  if (fg == typeview && !typesForeground) {
 		  reallySwitch(typeview, view);
 		  typesForeground = true;
 	  } else if (fg == view && typesForeground) {
 		  reallySwitch(view, typeview);
 		  typesForeground = false;
 	  }
   }
   
   private void reallySwitch (Graph2DView fg, Graph2DView bg) {
 	  left.remove(fg);
 	  typeview.getGlassPane().remove(bg);
 	  ((DefaultBackgroundRenderer)bg.getBackgroundRenderer()).setColor(Color.white);
 	  left.add(bg, BorderLayout.CENTER);
 	  ((DefaultBackgroundRenderer)fg.getBackgroundRenderer()).setColor(new Color(0xff, 0xff, 0xff, 0x33));
 	  bg.getGlassPane().add(fg);
   }
   
   public String methodName () {
 	  String mname = text.getText();
 	  String def = "define method ";
 	  if (mname.startsWith(def))
 		  mname = mname.substring(def.length(), mname.indexOf(' ', def.length() + 1)).trim();
 	  return mname;
   }
   
   private boolean steppressed = false;
   
   public void waitforstep () {
 	  steppressed = false;
 	  jtb.setBackground(Color.green);
 	  while(! steppressed)
 		try {
 			Thread.sleep(300);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
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
 	  incrementallayouter = ihl;
 	  updatingslider = true;
 	  slider.setLabelTable(ihl.sliderLabels);
 	  slider.setMaximum(ihl.lastEntry);
 	  slider.setValue(ihl.lastslidervalue);
 	  updatingslider = false;
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
 	  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	  frame.getRootPane().setContentPane( contentPane );
 	  frame.pack();
 	  frame.setSize(1400, 1000);
 	  frame.setLocationRelativeTo( null );
 	  frame.setVisible( true );
   }
 
   protected void registerViewModes() {
 	  view.getCanvasComponent().addMouseListener(new MyMouseListener());
 	  view.addViewMode(new NavigationMode());
 	  view.getCanvasComponent().addMouseWheelListener( new Graph2DViewMouseWheelZoomListener() );
 	  typeview.getCanvasComponent().addMouseListener(new MyMouseListener());
 	  typeview.addViewMode(new NavigationMode());
 	  typeview.getCanvasComponent().addMouseWheelListener( new Graph2DViewMouseWheelZoomListener() );
   }
 
   protected JToolBar createToolBar() {
     JToolBar toolBar = new JToolBar();
     toolBar.add( new Zoom( 1.2 ) );
     toolBar.add( new Zoom( 0.8 ) );
     toolBar.add( new FitContent( ) );
 	toolBar.add( new LayoutAction() );
 	toolBar.add( new Play() );
 	toolBar.add( new Step() );
 
     return toolBar;
   }
 
   public void activate (String methodname) {
 	text.setText(string_source_map.get(methodname));
 	for (int i = 0; i < graph_chooser.getItemCount(); i++)
 		if (((ListElement)graph_chooser.getItemAt(i)).toString().equals(methodname)) {
 			graph_chooser.setSelectedIndex(i);
 			break;
 		}
 	int realindex = ((ListElement)graph_chooser.getSelectedItem()).index;
 	if (realindex >= 0) {
 		IncrementalHierarchicLayout ih = client.getGraph(realindex);
 		ih.activateLayouter();
 	} else {
 		updatingslider = true;
 		slider.setLabelTable(null);
 		slider.setMaximum(0);
 		updatingslider = false;
 		view.setGraph2D(new Graph2D());
 		view.repaint();
 		System.out.println("no graph yet, please wait");
 	}
   }
 
   final class MyMouseListener implements MouseListener {
 	public void mouseClicked(MouseEvent arg0) {
 		Node selected = null;
 		if (alphaslider.getValue() < 2)
 			selected = checkClick(view, incrementallayouter.graph, arg0.getX(), arg0.getY());
 		else
 			selected = checkClick(typeview, incrementallayouter.typegraph, arg0.getX(), arg0.getY());
 		if (selected == null)
 			unselect();
 		else
 			select(selected);
 		view.repaint();
 		typeview.repaint();
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
   final class SendAction extends AbstractAction
   {
 	public void actionPerformed(ActionEvent ev) {
 		if (string_source_map.get(methodName()) == null) {
 			System.out.println("new method :" + methodName() + ":");
 			string_source_map.put(methodName(), text.getText());
 			ListElement newLE = new ListElement(-1, methodName());
 			graph_chooser.addItem(newLE);
 			graph_chooser.setSelectedItem(newLE);
 		} 
 		int realindex = ((ListElement)graph_chooser.getSelectedItem()).index;
 		if (realindex == -1) {
 			ArrayList data = new ArrayList();
 			data.add(new Symbol("compile"));
 			//data.add(new Symbol(methodName()));
 			if (containsMethodHeader())
 				data.add(text.getText());
 			else
 				data.add("define function test" + client.getGraphSize() + " () " + text.getText() + " end;");
 			client.printMessage(data);
 		}
 	}
   }
   
   final class ChangeProjectAction extends AbstractAction
   {
 		public void actionPerformed(ActionEvent ev) {
 			ArrayList data = new ArrayList();
 			data.add(new Symbol("open-project"));
 			data.add((String)project_chooser.getSelectedItem());
 			client.printMessage(data);					
 		} 
   }
   
   final class ChangeGraphAction extends AbstractAction
 	{
 		public ChangeGraphAction() {
 			super("Change Graph");
 			this.putValue(Action.SHORT_DESCRIPTION, "Change Graph");
 		}
 		
 		public void actionPerformed(ActionEvent ev) {
 			text.setText(string_source_map.get(((ListElement)graph_chooser.getSelectedItem()).toString()));
 			int realindex = ((ListElement)graph_chooser.getSelectedItem()).index;
 			if (realindex >= 0) {
 				IncrementalHierarchicLayout ih = client.getGraph(realindex);
 				ih.activateLayouter();
 			} else {
 				updatingslider = true;
 				slider.setLabelTable(null);
 				slider.setMaximum(0);
 				updatingslider = false;
 				view.setGraph2D(new Graph2D());
 				view.repaint();
 				typeview.setGraph2D(new Graph2D());
 				typeview.repaint();
 				System.out.println("no graph yet, please wait");
 			}
 				
 		}
 	}
   
   final class ChangeSlider implements ChangeListener
   {
 	public void stateChanged(ChangeEvent arg0) {
 		if (!updatingslider && !slider.getValueIsAdjusting() && incrementallayouter.graphfinished) {
 			int step = slider.getValue();
 			incrementallayouter.resetGraph(step);
 		}
 	}
   }
  
   final class ChangeAlphaSlider implements ChangeListener
   {
 	public void stateChanged(ChangeEvent arg0) {
 		if (!alphaslider.getValueIsAdjusting()) {
 			int step = alphaslider.getValue();
 			if (step == 1)
 				switchViews(view);
 			else if (step == 2)
 				switchViews(typeview);
 			else if (step == 0) {
 				switchViews(typeview);
 				view.getGlassPane().remove(typeview);
 			} else if (step == 3) {
 				switchViews(view);
 				typeview.getGlassPane().remove(view);			
 			}
 			//view.updateView();
 			//typeview.updateView();
 			left.repaint();
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
       URL imageURL;
       if ( factor > 1.0d ) {
         imageURL = ClassLoader.getSystemResource( "demo/view/resource/ZoomIn16.gif" );
       } else {
         imageURL = ClassLoader.getSystemResource( "demo/view/resource/ZoomOut16.gif" );
       }
       if ( imageURL != null ) {
         this.putValue( Action.SMALL_ICON, new ImageIcon( imageURL ) );
       }
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
       URL imageURL = ClassLoader.getSystemResource( "demo/view/resource/FitContent16.gif" );
       if ( imageURL != null ) {
         this.putValue( Action.SMALL_ICON, new ImageIcon( imageURL ) );
       }
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
 	final class LayoutAction extends AbstractAction
 	{
 		LayoutAction()
 		{
 			super("Layout");
 			URL imageURL = ClassLoader.getSystemResource("demo/view/resource/Layout16.gif");
 			if (imageURL != null){
 				this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
 			}
 			this.putValue( Action.SHORT_DESCRIPTION, "Layout");
 		}
 		public void actionPerformed(ActionEvent ev)
 		{
 			incrementallayouter.changed = true;
 			incrementallayouter.typechanged = true;
 			calcLayout();
 		}
 	}
 	
 	final class Play extends AbstractAction
 	{
 		Play() {
 			super("Play");
 			this.putValue( Action.SHORT_DESCRIPTION, "Play");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			while (true)
 				if (! incrementallayouter.nextStep())
 					break;
 		}
 		
 	}
 	
 	final class Step extends AbstractAction
 	{
 		Step() {
 			super("Step");
 			this.putValue( Action.SHORT_DESCRIPTION, "Step");
 		}
 		
 		public void actionPerformed (ActionEvent ev) {
 			steppressed = true;
 			incrementallayouter.nextStep();
 		}
 		
 	}
 	
 	private Color highlightColor (Color c) {
 		Color b = c.darker().darker();
 		return new Color(b.getRed(), b.getGreen(), b.getBlue(), c.getAlpha());
 	}
 	
 	private Color unhighlightColor (Color c) {
 		Color b = c.brighter().brighter();
 		return new Color(b.getRed(), b.getGreen(), b.getBlue(), c.getAlpha());
 	}
 	
 	protected void unselect () {
 		if (incrementallayouter != null) {
 			Node old = incrementallayouter.selection;
 			if  (old != null) {
 				Graph2D gr = (Graph2D)old.getGraph();
 				gr.setSelected(old, false);
 				for (EdgeCursor ec = old.edges(); ec.ok(); ec.next())
 					if (gr.getRealizer(ec.edge()).getLineColor() != Color.black) {
 						gr.getRealizer(ec.edge()).setLineType(LineType.LINE_1);
 						NodeRealizer o = gr.getRealizer(ec.edge().opposite(old)); 
 						o.setFillColor(unhighlightColor(o.getFillColor()));
 					}
 				Node tt = findTNode(old);
 				if (tt != null) {
 					Graph2D gr2 = (Graph2D)tt.getGraph();
 					gr2.getRealizer(tt).setFillColor(unhighlightColor(gr2.getRealizer(tt).getFillColor()));
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
 				for (EdgeCursor ec = s.edges(); ec.ok(); ec.next())
 					if (gr.getRealizer(ec.edge()).getLineColor() != Color.black) {
 						gr.getRealizer(ec.edge()).setLineType(LineType.LINE_3);
 						NodeRealizer n = gr.getRealizer(ec.edge().opposite(s)); 
 						n.setFillColor(highlightColor(n.getFillColor()));
 					}
 				Node tt = findTNode(s);
 				if (tt != null) {
 					Graph2D gr2 = (Graph2D)tt.getGraph();
 					gr2.getRealizer(tt).setFillColor(highlightColor(gr2.getRealizer(tt).getFillColor()));
 				}
 				incrementallayouter.selection = s;
 			}
 		}
 	}
 
 	/**
 	 * Animated layout assignment
 	 */
 	public void calcLayout(){
 		if (!view.getGraph2D().isEmpty() && incrementallayouter.changed){
 		    //System.out.println("calculating layout");
 			if (alphaslider.getValue() != 0 && alphaslider.getValue() != 2)
 				alphaslider.setValue(2);
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
 				morpher.setSmoothViewTransform(true);
 				//morpher.setKeepZoomFactor(true);
 				morpher.setPreferredDuration(1500);
 				final AnimationPlayer player = new AnimationPlayer();
 				player.addAnimationListener(view);
 				player.setFps(30);
 				//player.setBlocking(true);
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
			if (alphaslider.getValue() != 3 && alphaslider.getValue() == 1)
 				alphaslider.setValue(1);
 			//switchViews(view);
 			Cursor oldCursor = typeview.getCanvasComponent().getCursor();
 			//for (NodeCursor nc = incrementallayouter.typegraph.nodes(); nc.ok(); nc.next())
 			//	incrementallayouter.action_nodes.set(nc.node(), true);
 			//Point2D vc = view.getCenter();
 			//Point2D tc = typeview.getCenter();
 			//double xoff = vc.getX() - tc.getX();
 			//double yoff = vc.getY() - tc.getY();
 			/*
 			for (Node n : incrementallayouter.tv_temp_map.keySet()) {
 				Node m = incrementallayouter.tv_temp_map.get(n);
 				NodeRealizer mr = incrementallayouter.graph.getRealizer(m);
 				double xv = typeview.toWorldCoordX(view.toViewCoordX(mr.getCenterX()));
 				double yv = typeview.toWorldCoordY(view.toViewCoordY(mr.getCenterY()));
 				//incrementallayouter.typegraph.getRealizer(n).setCenter(xv, yv);
 				//incrementallayouter.typeHintMap.set(n, incrementallayouter.typeHintsFactory.createUseExactCoordinatesHint(n));
 				//incrementallayouter.action_nodes.set(n, false);
 			} */
 			try {
 				typeview.getCanvasComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				GraphLayout layout = new BufferedLayouter(incrementallayouter.typeLayouter).calcLayout(typeview.getGraph2D());
 				LayoutMorpher morpher = new LayoutMorpher(typeview, layout);
 				morpher.setSmoothViewTransform(true);
 				//morpher.setKeepZoomFactor(true);
 				morpher.setPreferredDuration(1500);
 				final AnimationPlayer player = new AnimationPlayer();
 				player.addAnimationListener(typeview);
 				player.setFps(30);
 				//player.setBlocking(true);
 				player.animate(AnimationFactory.createEasedAnimation(morpher));
 			} catch (Exception e) {
 				System.out.println("got exception during layouting");
 				e.printStackTrace();
 			} finally {
 				typeview.getCanvasComponent().setCursor(oldCursor);
 			}
 			/* for (Node n : incrementallayouter.tv_temp_map.keySet()) {
 				Node m = incrementallayouter.tv_temp_map.get(n);
 				NodeRealizer mr = incrementallayouter.graph.getRealizer(m);
 				double xv = typeview.toWorldCoordX(view.toViewCoordX(mr.getCenterX()));
 				double yv = typeview.toWorldCoordY(view.toViewCoordY(mr.getCenterY()));
 				incrementallayouter.typegraph.getRealizer(n).setCenter(xv, yv);
 			} */
 		}
 		typeview.updateView();
 		view.updateView();
 	}
 
 }

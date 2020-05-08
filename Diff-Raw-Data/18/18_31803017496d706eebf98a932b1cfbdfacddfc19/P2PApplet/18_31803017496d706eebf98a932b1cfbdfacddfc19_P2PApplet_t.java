 package spiderweb;
 
 //[start] Imports
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.collections15.functors.ConstantTransformer;
 
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
 import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
 import edu.uci.ics.jung.algorithms.util.IterativeContext;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 //[end] Imports
 
 /**
  * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
  * @author Alan
  * @author Matt
  */
 public class P2PApplet extends JApplet implements EventPlayerListener {
 	//[start] Attributes
 	
 	//[start] Static Final Attributes
 	// for the length of the edges in the graph layout
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);
 
 	//the log file (default value)
 	private static final String DEF_LOG_FILE = "ProcessedLog.txt";
 	private static final String DEF_LOG_URL = "http://www.sce.carleton.ca/~adavoust/simuldemo/ProcessedLog.txt";
 	//[end] Static Final Attributes
 	
 	//[start] Private Variables
 	private static final long serialVersionUID = 2L;
 	
 	//default size for the swing graphic components
 	public static final int DEFWIDTH = 800;
 	public static final int DEFHEIGHT = 600;
 
 	private static final boolean ONWEB = true;
 	
 	// this boolean indicates, as ONWEB above, that we are running on the web.
 	//it can be changed through the main()
 	private boolean ontheweb=ONWEB;
 	
 	//private String logfilename=null;
 	private File mylogfile =null;
 	
 	private VisualizationViewer<P2PVertex,P2PConnection> fullViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedDocumentViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerAndDocumentViewViewer = null;
 
 	private AbstractLayout<P2PVertex,P2PConnection> layout = null;
 	
 	private LinkedList<LogEvent> myGraphEvolution;
 
 	//a hidden graph that contains all the nodes that will ever be added... 
 	//in order to calculate the positions of all the nodes
 	private P2PNetworkGraph hiddenGraph; 
 	private P2PNetworkGraph visibleGraph = null;
 	
 	private List<LoadingListener> loadingListeners;
 	
 	//[end] Private Variables
 	
 	//[start] Protected Variables
 	protected JTable logList;
 	protected JTabbedPane tabsPane;
 	protected JButton relaxerButton;
 	protected JButton fastForwardButton;
 	protected JButton forwardButton;
 	protected JButton pauseButton;
 	protected JButton reverseButton;
 	protected JButton fastReverseButton;
 	protected JButton stopButton;
 	protected JButton saveButton;
 	protected JButton loadButton;
 	protected JSlider fastSpeedSlider;
 	protected JSlider playbackSlider;
 
 	protected EventPlayer eventThread;
 	//[end] Protected Variables
 	
 	//[end] Attributes
 	
 	//[start] Constructor
 	public P2PApplet(boolean onWeb) {
 		ontheweb = onWeb;
 		if(!onWeb) {
 			LogPanelWithFileChooser mygui = new LogPanelWithFileChooser(this);
 	
 			mygui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			//mygui.validate();
 			//mygui.setBounds(200,200,500,200);
 			mygui.pack();
 			mygui.setVisible(true);
 			loadingListeners = new LinkedList<LoadingListener>();
 			loadingListeners.add(mygui);
 		}
 	}
 	//[end] Constructor
 
 	//[start] Initialization
 	
 	//[start] setters for when on the web
 	/** to use a particular log file (can be chosen from GUI)*/
 	public void setLogFile(File lf){
 		mylogfile = lf;
 	}
 	
 	/**
 	 * to set if the applet is running on the web 
 	 * (in which case we need to access the log file through an http connection)
 	 * @param b true if the applet is running on the web (default). If not, it must be set from the main()
 	 */
 	public void setOnWeb(boolean b){
 		ontheweb = b;	
 	}
 	//[end] setters for when on the web
 	
 	//[start] Open the log file for reading
 	/**
 	 * Helper Method for opening the Log File.
 	 * @return The Log File for reading.
 	 */
 	private BufferedReader openLogFile() {
 		BufferedReader in;
 		try {
 			if (ontheweb){ // hack : when running on SCE server I can't read the log file without opening it through this URL reader ...
 				URL yahoo = new URL(DEF_LOG_URL);
 				in = new BufferedReader(new InputStreamReader(yahoo.openStream()));
 			} else {
 				if (mylogfile == null) {
 					in = new BufferedReader(new FileReader(DEF_LOG_FILE));
 				} else{
 					in = new BufferedReader(new FileReader(mylogfile));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return in;
 	}
 	//[end] Open the log file for reading
 	
 	//[start] Calculate the Spring Layout
 	/**
 	 * Helper Method for setting up the spring layout.
 	 * @return The initialized spring layout.
 	 */
 	private SpringLayout<P2PVertex,P2PConnection> springLayoutBuilder(List<LogEvent> graphEvents, int width, int height, P2PNetworkGraph graph) {
 		final int numSteps = 100;
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(numSteps, "Spring Layout");
 		}
 		SpringLayout<P2PVertex,P2PConnection> sp_layout=null;
 		sp_layout = new SpringLayout<P2PVertex,P2PConnection>(graph, new P2PNetEdgeLengthFunction()); // here is my length calculation
 		
 		sp_layout.setSize(new Dimension(width,height));
 		sp_layout.setForceMultiplier(0.6); //testing this value
 
 		sp_layout.setInitializer(new P2PVertexPlacer(sp_layout, new Dimension(width,height)));
 		
 		sp_layout.initialize();
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		
 		for(int j =1;j<=numSteps;j++) {
 			sp_layout.step();
 			for(LoadingListener l : loadingListeners) {
 				l.loadingProgress(j);
 			}
 		}
 		return sp_layout;
 	}
 	//[end] Calculate the Spring Layout
 	
 	//[start] Create the visualization viewer
 	/**
 	 * 
 	 * @return The Initialized Visualization Viewer
 	 */
 	private VisualizationViewer<P2PVertex,P2PConnection> visualizationViewerBuilder(final Layout<P2PVertex, P2PConnection> layout, int width, int height, GraphMouse gm) {
 		VisualizationViewer<P2PVertex,P2PConnection> viewer = new VisualizationViewer<P2PVertex,P2PConnection>(layout, new Dimension(width,height));
 		JRootPane rp = this.getRootPane();
 		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
 
 		
 
 		// the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
 		viewer.setGraphMouse(gm);
 
 		//set graph rendering parameters & functions
 		viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
 		//the vertex labeler will use the tostring method which is fine, the P2PVertex class has an appropriate toString() method implementation
 		viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());
 
 		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
 		viewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(viewer.getPickedVertexState()));
 		viewer.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
 		viewer.setForeground(Color.white);
 		viewer.setBounds(0,0,width,height);
 		
 		viewer.addComponentListener(new ComponentAdapter() {
 			
 			/**
 			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
 			 */
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 				super.componentResized(arg0);
 				//viewer.getGraphLayout().setSize(arg0.getComponent().getSize());
 			}
 		});
 		
 		return viewer;
 	}
 	
 	private void initSpecialTransformers(VisualizationViewer<P2PVertex,P2PConnection> viewer,
 			VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
 			EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {
 		
 		//add my own vertex shape & color fill transformers
 		viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape,documentShape, peerDocumentShape));
 		// note :the color depends on being picked.
 		
 		//make the p2p edges different from the peer to doc edges 
 		viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
 		viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(P2PEdgeShape,P2DocEdgeShape,Doc2PDocEdgeShape,P2PDocEdgeShape)); //stroke width
 		
 	}
 	//[end] Create the visualization viewer
 	
 	//[start] Create Components
 	
 	//[start] East Panel
 	private JPanel initializeEastPanel() {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(myGraphEvolution.size()+2, "East Panel");
 		}
 		
 		Object[][] table = new Object[myGraphEvolution.size()][4];
 		int i=0;
 		for(LogEvent evt : myGraphEvolution) {
 			table[i] = evt.toArray();
 			i++;
 			for(LoadingListener l : loadingListeners) {
 				l.loadingProgress(i);
 			}
 		}
 		Object[] titles = { "Time", "Type", "Param 1", "Param 2" };
 		
 		
 		logList = new JTable(table, titles);
 		
 		logList.setBackground(Color.LIGHT_GRAY);
 		logList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		logList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		logList.setEnabled(false);
 		logList.setColumnSelectionAllowed(false);
 		
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(myGraphEvolution.size()+1);
 		}
 		
 		JScrollPane listScroller = new JScrollPane(logList);
 		listScroller.setWheelScrollingEnabled(true);
 		listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
 		listScroller.setSize(logList.getWidth(),logList.getHeight());
 		
 		JPanel eastPanel = new JPanel(new GridLayout(1,1));
 		eastPanel.add(listScroller);
 		eastPanel.setBackground(Color.GRAY);
 		eastPanel.setBorder(BorderFactory.createTitledBorder("Log Events"));
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(myGraphEvolution.size()+2);
 		}
 		
 		return eastPanel;
 	}
 	//[end] EastPanel
 	
 	//[start] South Panel
 	/**
 	 * Helper Method for initializing the Buttons and slider for the South Panel.
 	 * @return The South Panel, laid out properly, to be displayed.
 	 */
 	private JPanel initializeSouthPanel() {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(9, "South Panel");
 		}
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		
 		fastReverseButton = new JButton("<|<|");
 		fastReverseButton.addActionListener(new FastReverseButtonListener()); 
 		fastReverseButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(2);
 		}
 		
 		reverseButton = new JButton("<|");
 		reverseButton.addActionListener(new ReverseButtonListener());
 		reverseButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(3);
 		}
 		
 		pauseButton = new JButton("||");
 		pauseButton.addActionListener(new PauseButtonListener()); 
 		pauseButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(4);
 		}
 		
 		forwardButton = new JButton("|>");
 		forwardButton.addActionListener(new ForwardButtonListener());
 		forwardButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(5);
 		}
 		
 		fastForwardButton = new JButton("|>|>");
 		fastForwardButton.addActionListener(new FastForwardButtonListener());
 		fastForwardButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(6);
 		}
 		
 		playbackSlider = new JSlider(JSlider.HORIZONTAL,0,(int)myGraphEvolution.getLast().getTime(),0);
 		SliderListener s = new SliderListener();
 		playbackSlider.addChangeListener(s);
 		playbackSlider.addMouseListener(s);
 		playbackSlider.setMajorTickSpacing(playbackSlider.getExtent()/4);
 		playbackSlider.setMajorTickSpacing(playbackSlider.getExtent()/8);
 		playbackSlider.setPaintTicks(true);
 		playbackSlider.setPaintLabels(false);
 		playbackSlider.setBackground(Color.GRAY);
 		playbackSlider.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(7);
 		}
 		
 		GridBagLayout southLayout = new GridBagLayout();
 		GridBagConstraints southConstraints = new GridBagConstraints();
 		
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBackground(Color.GRAY);
 		buttonPanel.setLayout(southLayout);
 		
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(8);
 		}
 		
 		buttonPanel.add(fastReverseButton);
 		buttonPanel.add(reverseButton);
 		buttonPanel.add(pauseButton);
 		buttonPanel.add(forwardButton);
 		southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
 		southLayout.setConstraints(fastForwardButton, southConstraints);
 		buttonPanel.add(fastForwardButton);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(9);
 		}
 		JPanel south = new JPanel();
 		south.setBackground(Color.GRAY);
 		south.setLayout(new GridLayout(2,1));
 		south.setBorder(BorderFactory.createTitledBorder("Playback Options"));
 		south.add(buttonPanel);
 		south.add(playbackSlider);
 		
 		return south;
 	}
 	//[end] South Panel
 	
 	//[start] West Panel
 	/**
 	 * Helper Method for initializing the Buttons and drop down menu for the West Panel.
 	 * @return The west Panel, laid out properly, to be displayed.
 	 */
 	private JPanel initializeWestPanel(DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(8, "West Panel");
 		}
 		
 		fastSpeedSlider = new JSlider(JSlider.HORIZONTAL,0,100,25);
 		fastSpeedSlider.addChangeListener(new SpeedSliderListener());
 		fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum()-fastSpeedSlider.getMinimum())/4);
 		fastSpeedSlider.setPaintTicks(true);
 		fastSpeedSlider.setPaintLabels(true);
 		fastSpeedSlider.setBackground(Color.GRAY);
 		fastSpeedSlider.setForeground(Color.BLACK);
 		fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Fast Playback Speed"));
 		fastSpeedSlider.setEnabled(false);
 		
 		//[start] Relaxer creation
 		Relaxer relaxer = new VisRunner((IterativeContext)layout);
 		relaxer.stop();
 		relaxer.setSleepTime(80L);
 		relaxer.relax();
 		//[end] Relaxer creation
 		relaxerButton = new JButton("Finalize Layout");
 		relaxerButton.addActionListener(new relaxerButtonListener(relaxer)); 
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		
 
 		stopButton = new JButton("Stop Playback");
 		stopButton.addActionListener(new StopButtonListener());
 		stopButton.setEnabled(false);
 		
 		saveButton = new JButton("Save");
 		saveButton.addActionListener(new SaveButtonListener());
 		saveButton.setEnabled(false);
 		
 		loadButton = new JButton("Load");
 		loadButton.addActionListener(new LoadButtonListener());
 		loadButton.setEnabled(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(2);
 		}
 		
 		GridBagLayout westLayout = new GridBagLayout();
 		GridBagConstraints westConstraints = new GridBagConstraints();
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(3);
 		}
 		
 		JPanel west = new JPanel();
 		west.setBorder(BorderFactory.createTitledBorder("Options"));
 		west.setBackground(Color.GRAY);
 		west.setLayout(westLayout);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(4);
 		}
 		
 		JPanel p = new JPanel();
 		//add little combo box to choose between the mouse picking and the mouse transforming the layout
 		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
 		p.setBackground(Color.GRAY);
 		p.add(gm.getModeComboBox());
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(5);
 		}
 		
 		westConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
 		westLayout.setConstraints(p, westConstraints);
 		west.add(p);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(6);
 		}
 		
 		westLayout.setConstraints(relaxerButton, westConstraints);
 		west.add(relaxerButton);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(7);
 		}
 		
 		westLayout.setConstraints(stopButton, westConstraints);
 		west.add(stopButton);
 		
 		westLayout.setConstraints(saveButton, westConstraints);
 		west.add(saveButton);
 		
 		westLayout.setConstraints(loadButton, westConstraints);
 		west.add(loadButton);
 		
 		westLayout.setConstraints(fastSpeedSlider, westConstraints);
 		west.add(fastSpeedSlider);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(8);
 		}
 		
 		return west;
 	}
 	//[end] West Panel
 	
 	//[end] Create Components
 	
 	//[start] init method
 	/**
 	 * applet initialization
 	 */
 	public void init() {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingStarted(1, "Graphs");
 		}
 		hiddenGraph = new P2PNetworkGraph();
 		visibleGraph = new P2PNetworkGraph();
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		
 		LogEventListBuilder logBuilder = new LogEventListBuilder(hiddenGraph);
 		for(LoadingListener l : loadingListeners) {
 			logBuilder.addLoadingListener(l);
 		}
 		
 		
 		myGraphEvolution = logBuilder.createLinkedList(openLogFile());
 		
 		layout = springLayoutBuilder(myGraphEvolution,DEFWIDTH,DEFHEIGHT,hiddenGraph);
 		
 		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm = new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
 		
 		fullViewViewer = visualizationViewerBuilder(layout, DEFWIDTH,DEFHEIGHT, gm);
 		//add my own vertex shape & color fill transformers
 		initSpecialTransformers(fullViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		collapsedDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		initSpecialTransformers(collapsedDocumentViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.ELLIPSE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		collapsedPeerViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		initSpecialTransformers(collapsedPeerViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		collapsedPeerAndDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		initSpecialTransformers(collapsedPeerAndDocumentViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		tabsPane = new JTabbedPane(JTabbedPane.TOP);
 		
 		tabsPane.addTab("Full View",fullViewViewer);
 		tabsPane.addTab("Collapsed Document View", collapsedDocumentViewViewer);
 		tabsPane.addTab("Collapsed Peer View", collapsedPeerViewViewer);
 		tabsPane.addTab("Collapsed Peer and Document View", collapsedPeerAndDocumentViewViewer);
 		
 		tabsPane.setEnabled(false);
 		
 		
 		//tabsPane.addTab("All Views",allViews);
 		
 		JPanel graphsPanel = new JPanel(new GridLayout(1,1));
 		graphsPanel.setBackground(Color.GRAY);
 		graphsPanel.add(tabsPane);
 				
 		JSplitPane p3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		p3.setResizeWeight(0);
 		p3.add(initializeWestPanel(gm));
 		p3.add(graphsPanel);
 		
 		JSplitPane p2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		p2.setResizeWeight(0);
 		p2.add(p3);
 		p2.add(initializeEastPanel());
 		
 		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		p.setResizeWeight(1);
 		p.add(p2);
 		p.add(initializeSouthPanel());
 		
 		getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));
 		//try set the size
 		getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
 		getContentPane().add(p);
 		
 		/// create the event player
 		eventThread = new EventPlayer(myGraphEvolution,hiddenGraph,visibleGraph, playbackSlider);
 		eventThread.addEventPlayerListener(this);
 		
 		setBounds(0, 0, 1024, 768);
 		for(LoadingListener l : loadingListeners) {
 			l.loadingComplete();
 		}
 	}
 	//[end] init method
 
 	//[start] overridden start method
 	@Override
 	public void start() {
 		validate();
 
 		fullViewViewer.repaint();
 		collapsedDocumentViewViewer.repaint();
 		collapsedPeerViewViewer.repaint();
 		collapsedPeerAndDocumentViewViewer.repaint();
 		///----------run the spring layout algorithm with the full hidden graph for a bit -------
 	}
 	//[end] overridden start method
 	
 	//[end] Initialization
 	
 	//[start] Main
 	/**
 	 * 
 	 * to run this applet as a java application
 	 * @param args optional argument : the log file to process
 	 */
 	public static void main(String[] args) {
 
 		P2PApplet myapp = new P2PApplet(false);
 	}
 	//[end] Main
 
 	//[start] EventPlayer Handlers
 	
 	@Override
 	public void playbackFastReverse() {
 		fastReverseButton.setEnabled(false);
 		reverseButton.setEnabled(true);
 		pauseButton.setEnabled(true);
 		forwardButton.setEnabled(true);
 		fastForwardButton.setEnabled(true);
 	}
 
 	@Override
 	public void playbackReverse() {
 		fastReverseButton.setEnabled(true);
 		reverseButton.setEnabled(false);
 		pauseButton.setEnabled(true);
 		forwardButton.setEnabled(true);
 		fastForwardButton.setEnabled(true);
 	}
 
 	@Override
 	public void playbackPause() {
 		if(eventThread.atFront()) {
 			fastReverseButton.setEnabled(false);
 			reverseButton.setEnabled(false);
 		} else {
 			fastReverseButton.setEnabled(true);
 			reverseButton.setEnabled(true);
 		}
 		pauseButton.setEnabled(false);
 		if(eventThread.atBack()) {
 			forwardButton.setEnabled(false);
 			fastForwardButton.setEnabled(false);
 		} else {
 			forwardButton.setEnabled(true);
 			fastForwardButton.setEnabled(true);
 		}
 	}
 
 	@Override
 	public void playbackForward() {
 		fastReverseButton.setEnabled(true);
 		reverseButton.setEnabled(true);
 		pauseButton.setEnabled(true);
 		forwardButton.setEnabled(false);
 		fastForwardButton.setEnabled(true);
 	}
 
 	@Override
 	public void playbackFastForward() {
 		fastReverseButton.setEnabled(true);
 		reverseButton.setEnabled(true);
 		pauseButton.setEnabled(true);
 		forwardButton.setEnabled(true);
 		fastForwardButton.setEnabled(false);
 	}
 	
 	@Override
 	public void doRepaint() {
 		fullViewViewer.repaint();
 		collapsedDocumentViewViewer.repaint();
 		collapsedPeerViewViewer.repaint();
 		collapsedPeerAndDocumentViewViewer.repaint();
 	}
 	
 	//[end] EventPlayer Handlers
 
 	//[start] Swing Event Listeners
 	
 	//[start] Relaxer Button
 	 /**
 	 * an actionlistener that defines the use of a button to stop the spring-layout processing
 	 * @author adavoust
 	 *
 	 */
 	class relaxerButtonListener implements ActionListener {
 		
 		//this is the SpringLayout relaxer for the beginning
 		private Relaxer relaxer;
 		boolean started;
 		
 		public relaxerButtonListener (Relaxer rr){ // the relaxer is the thread that's doing the dynamic layout.
 										  // we need to stop it then switch to a static layout,
 										  // then show the full graph layout for one sec then make it hidden
 			this.relaxer = rr;
 			started = false; // says if we've passed the initial problem of freezing the layout and getting started
 			
 		}
 		/**
 		 * handles the button : first to freeze the layout, then to toggle between fast-forward and normal speed
 		 */
 		public void actionPerformed(ActionEvent ae) {
 
 			if(!started){ // this will be the first button task : freeze layout and start the simulation
 				try {
 					relaxer.stop();
 					relaxerButton.setText("Layout Complete");
 					relaxerButton.setEnabled(false);
 					
 					layout = new StaticLayout<P2PVertex,P2PConnection>(hiddenGraph, layout);
 	
 					//change the layout we're viewing
 					fullViewViewer.getModel().setGraphLayout(layout);
 					collapsedPeerAndDocumentViewViewer.getModel().setGraphLayout(layout);
 					collapsedPeerViewViewer.getModel().setGraphLayout(layout);
 					collapsedDocumentViewViewer.getModel().setGraphLayout(layout);
 					//fullViewViewer.repaint();
 					//	tie the "include functions" of the viewer to the visible graph
 					
 					
 					started = true;
 	
 					Thread.sleep(1000);
 					
 					stopButton.setEnabled(true);
 					fastReverseButton.setEnabled(true);
 					reverseButton.setEnabled(true);
 					pauseButton.setEnabled(true);
 					forwardButton.setEnabled(false);
 					fastForwardButton.setEnabled(true);
 					playbackSlider.setEnabled(true);
 					saveButton.setEnabled(true);
 					loadButton.setEnabled(true);
 					tabsPane.setEnabled(true);
 					fastSpeedSlider.setEnabled(true);
 	
 	
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} //wait 5 seconds
 				//these predicates say : if the considered node /edge (which will be evaluated in the context of the hiddengraph) is found in the visible graph, then show it !
 				fullViewViewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
 				fullViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
 				
 				collapsedPeerAndDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph,PeerDocumentVertex.class));
 				collapsedPeerAndDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
 				
 				collapsedPeerViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph, DocumentVertex.class));
 				collapsedPeerViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
 				
 				collapsedDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(visibleGraph,PeerVertex.class));
 				collapsedDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
 				
 				eventThread.run();
 			}		
 		}
 	}
 	//[end] Relaxer Button
 
 	//[start] Stop Button
 	class StopButtonListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			fastReverseButton.setEnabled(false);
 			reverseButton.setEnabled(false);
 			pauseButton.setEnabled(false);
 			forwardButton.setEnabled(false);
 			fastForwardButton.setEnabled(false);
 			playbackSlider.setEnabled(false);
 			eventThread.stopPlayback();
 		}
 	}
 	//[end] Stop Button
 	
 	//[start] Save Button
 	class SaveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			pauseButton.doClick();
 			JFileChooser fileNamer = new JFileChooser();
 			fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
 			int returnVal = fileNamer.showSaveDialog(null);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				eventThread.save(fileNamer.getSelectedFile());
 			}
 		}	
 	}
 	//[end] Save Button
 	
 	//[start] Load Button
 	class LoadButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			pauseButton.doClick();
 			JFileChooser fileNamer = new JFileChooser();
 			fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
 			int returnVal = fileNamer.showOpenDialog(null);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				//eventThread.save(fileNamer.getSelectedFile());
 			}
 		}	
 	}
 	//[end] Load Button
 	
 	//[start] Fast Reverse Button
 	class FastReverseButtonListener implements ActionListener {
 		
 		public void actionPerformed(ActionEvent ae) {
 			eventThread.fastReverse();
 		}
 	}
 	//[end] Fast Reverse Button
 	
 	//[start] Reverse Button
 	/**
 	 * An ActionListener that defines the action of the reverse button for the applet
 	 * @author Matthew
 	 * @version May 2011
 	 */
 	class ReverseButtonListener implements ActionListener {
 		
 		/**
 		 * Method called when the reverse button has an action performed(clicked)
 		 * Tells eventThread to traverse the graph placement in reverse.
 		 * @param ae	The ActionEvent that triggered the listener
 		 */
 		public void actionPerformed(ActionEvent ae) {
 			eventThread.reverse();
 		}
 	
 	}
 	//[end] Reverse Button
 	
 	//[start] Pause Button
 	class PauseButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			eventThread.pause();
 		}
 	}
 	//[end] Pause Button
 	
 	//[start] Forward Button
 	class ForwardButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			eventThread.forward();
 		}
 	}
 	//[end] Forward Button
 	
 	//[start] Fast Forward Button
 	class FastForwardButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {			
 			eventThread.fastForward();
 		}
 	}
 	//[end] Fast Forward Button
 	
 	//[start] Speed Slider
 	class SpeedSliderListener implements ChangeListener {
 
 		@Override
 		public void stateChanged(ChangeEvent arg0) {
 			eventThread.setFastSpeed(((JSlider)arg0.getSource()).getValue());
 		}
 		
 	}
 	//[end] Speed Slider
 	
 	//[start] Playback Slider
 	class SliderListener implements ChangeListener, MouseListener {
 
 		PlayState prevState = PlayState.PAUSE;
 		
 		@Override
 		public void stateChanged(ChangeEvent ce) {
 			JSlider source = (JSlider)ce.getSource();
 			eventThread.goToTime(source.getValue());
 			logList.clearSelection();
 			logList.addRowSelectionInterval(0, eventThread.getCurrentIndex()-1);
 			logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentIndex()-1, 0, true));
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			if(((JSlider)(arg0.getSource())).isEnabled()){
 				prevState = eventThread.getPlayState();
 				eventThread.pause();
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			if(((JSlider)(arg0.getSource())).isEnabled()){
 				if(prevState == PlayState.FASTREVERSE) {
 					eventThread.fastReverse();
 				}
 				else if (prevState == PlayState.REVERSE) {
 					eventThread.reverse();
 				}
 				else if (prevState == PlayState.FORWARD) {
 					eventThread.forward();
 				}
 				else if (prevState == PlayState.FASTFORWARD) {
 					eventThread.fastForward();
 				}
 				else if (prevState == PlayState.PAUSE) {
 					eventThread.pause();
 				}
 			}
 		}
 	}
 	//[end] Playback Slider
 
 	//[end] Swing Event Listeners
 }

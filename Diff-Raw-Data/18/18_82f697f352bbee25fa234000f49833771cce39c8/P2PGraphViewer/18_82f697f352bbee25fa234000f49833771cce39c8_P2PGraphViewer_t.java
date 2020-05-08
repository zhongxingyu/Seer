 /*
  * File:         P2PGraphVeiwer.java
  * Project:		 Spiderweb Network Graph Visualizer
  * Created:      01/06/2011
  * Last Changed: Date: 20/07/2011 
  * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
  * 
  * This code was produced at Carleton University 2011
  */
 package spiderweb;
 
 //[start] Imports
 import spiderweb.graph.*;
 import spiderweb.visualizer.*;
 import spiderweb.graph.savingandloading.*;
 import spiderweb.visualizer.eventplayer.*;
 
 import spiderweb.networking.*;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
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
 import org.jdom.JDOMException;
 
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
 import edu.uci.ics.jung.algorithms.layout.CircleLayout;
 import edu.uci.ics.jung.algorithms.layout.FRLayout;
 import edu.uci.ics.jung.algorithms.layout.FRLayout2;
 import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
 import edu.uci.ics.jung.algorithms.layout.KKLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout;
 import edu.uci.ics.jung.algorithms.layout.TreeLayout;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 //[end] Imports
 
 /**
  * P2PGraphViewer is the main program which contains the swing application 
  * and maintains all the visualizers and event players.
  * 
  * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
  * @author Alan Davoust
  * @version Date: 20/07/2011 
  */
 public class P2PGraphViewer extends JApplet implements EventPlayerListener, NetworkGraphListener {
 	//[start] Attributes
 	
 	//[start] Static Final Attributes
 	// for the length of the edges in the graph layout
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static final Transformer<P2PConnection,Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);
 
 	//default size for the swing graphic components
 	public static final int DEFWIDTH = 1360;
 	public static final int DEFHEIGHT = 768;
 	//[end] Static Final Attributes
 	
 	//[start] Private Variables
 	private static final long serialVersionUID = 2L;
 	
 	private VisualizationViewer<P2PVertex,P2PConnection> fullViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedDocumentViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerViewViewer = null;
 	private VisualizationViewer<P2PVertex,P2PConnection> collapsedPeerAndDocumentViewViewer = null;
 
 	private AbstractLayout<P2PVertex,P2PConnection> layout = null;
 	
 	private LinkedList<LogEvent> myGraphEvolution;
 
 	//a hidden graph that contains all the nodes that will ever be added... 
 	//in order to calculate the positions of all the nodes
 	private P2PNetworkGraph fullGraph; 
 	private P2PNetworkGraph dynamicGraph = null;
 	
 	private List<LoadingListener> loadingListeners;
 	
 	private HTTPClient networkClient;
 	
 	//[end] Private Variables
 	
 	//[start] Protected Variables
 	protected JTable logList;
 	protected JTabbedPane tabsPane;
 	protected JButton fastForwardButton;
 	protected JButton forwardButton;
 	protected JButton pauseButton;
 	protected JButton reverseButton;
 	protected JButton fastReverseButton;
 	protected JSlider fastSpeedSlider;
 	protected JSlider playbackSlider;
 	protected JPopupMenu mouseContext;
 
 	protected EventPlayer eventThread;
 	//[end] Protected Variables
 	
 	//[end] Attributes
 	
 	//[start] Constructor
 	public P2PGraphViewer() {
 		
 		networkClient = new HTTPClient(this);
 		
 		loadingListeners = new LinkedList<LoadingListener>();
 		init();
 		start();
 		JFrame frame = new JFrame();
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().add(this);
 		
 		frame.pack();
 		frame.setVisible(true);
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 	}
 	//[end] Constructor
 
 	//[start] Initialization
 	
 	//[start] Calculate the Spring Layout
 	/**
 	 * Helper Method for setting up the spring layout.
 	 * @return The initialized spring layout.
 	 */
 	/*private SpringLayout<P2PVertex,P2PConnection> springLayoutBuilder(int width, int height, P2PNetworkGraph graph) {
 		final int numSteps = 100;
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(numSteps, "Spring Layout");
 		}
 		SpringLayout<P2PVertex,P2PConnection> sp_layout;
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
 	}*/
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
 		//viewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<P2PConnection>());
 		viewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(viewer.getPickedVertexState()));
 		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
 		
 		viewer.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
 		viewer.setForeground(Color.white);
 		viewer.setBackground(Color.GRAY);
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
 	
 	//[start] File Menu
 	private JMenuBar createFileMenu() {
 		//[start] File Menu
 		JMenu file = new JMenu("File");
 		//[start] Connect Entry
 		JMenuItem connect = new JMenuItem("Connect to..");
 		connect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				pauseButton.doClick();
 				
 				String option = (String)JOptionPane.showInputDialog(null, "Enter a URL:", "http://134.117.60.66:8080/graphServer");
 				
 				if(option != null ) {
 					if(option.startsWith("http://")){
 						//networkClient.closeNetwork();
 						networkClient.startNetwork(option);
 						//client.addNetworkListener();
 					}
 					else {
 						JOptionPane.showMessageDialog(null, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 				//else cancel option, don't do anything
 			}	
 		});
 		//[end] Connect Entry
 		
 		//[start] Save Entry
 		JMenuItem save = new JMenuItem("Save");
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				pauseButton.doClick();
 				int option = JOptionPane.showConfirmDialog(null, "Would you like to save the first 500 log events after this graph snapshot", 
 						"Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 				
 				if(option == JOptionPane.YES_OPTION) {
 					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(dynamicGraph,eventThread.getSaveEvents(), eventThread.getCurrentTime());
 					saver.addProgressListener(new LoadingBar());
 					saver.doSave();
 				}
 				else if(option == JOptionPane.NO_OPTION) {
 					P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(dynamicGraph);
 					saver.addProgressListener(new LoadingBar());
 					saver.doSave();
 				}
 				//else cancel option, don't do anything
 			}	
 		});
 		//[end] Save Entry
 		
 		//[start] Load Entry
 		JMenuItem load = new JMenuItem("Load");
 		load.addActionListener(new LoadListener());
 		//[end] Load Entry
 		
 		//[start] Exit Entry
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener() {	
 			public void actionPerformed(ActionEvent arg0) {
 				pauseButton.doClick();
 				int option = JOptionPane.showConfirmDialog(null, "Would you like to save before quitting?", "Save", 
 						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 				
 				if(option == JOptionPane.OK_OPTION) {
 					//GraphSaverAndLoader.save(dynamicGraph);
 					System.exit(0);
 				}
 				else if(option == JOptionPane.NO_OPTION) {
 					System.exit(0);
 				}
 				//else if(option == JOptionPane.CANCEL_OPTION) {
 					//do nothing
 				//}
 			}	
 		});
 		//[end] Exit Entry
 		
 		file.add(connect);
 		file.addSeparator();
 		file.add(save);
 		file.add(load);
 		file.addSeparator();
 		file.add(exit);
 		//[end] File Menu
 		
 		//[start] Window Menu
 		JMenu window = new JMenu("Window");
 		JMenuItem logTable = new JMenuItem("Show Log Table");
 		logTable.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(myGraphEvolution != null) { //graph has been initialized
 					JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 					p.setResizeWeight(1);
 					p.add(getContentPane().getComponent(0));
 					p.add(initializeLogList(myGraphEvolution));
 					
 					getContentPane().add(p);
 					validate();
 				}
 			}	
 		});
 		window.add(logTable);
 		//[end] Window Menu
 		
 		JMenuBar bar = new JMenuBar();
 		bar.add(file);
 		bar.add(window);
 		bar.setVisible(true);
 		return bar;
 	}
 	//[end] File Menu
 	
 	//[start] Table Panel
 	private JPanel initializeLogList(List<LogEvent> logEvents) {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingStarted(logEvents.size(), "Log List");
 		}
 		
 		Object[][] table = new Object[logEvents.size()][4];
 		int i=0;
 		for(LogEvent evt : logEvents) {
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
 		logList.setVisible(true);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(logEvents.size()+1);
 		}
 		
 		JScrollPane listScroller = new JScrollPane(logList);
 		listScroller.setWheelScrollingEnabled(true);
 		listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
 		listScroller.setSize(logList.getWidth(),logList.getHeight());
 		
 		JPanel tablePanel = new JPanel(new GridLayout(1,1));
 		tablePanel.add(listScroller);
 		//tablePanel.setBackground(Color.GRAY);
 		tablePanel.setBorder(BorderFactory.createTitledBorder("Log Events"));
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingComplete();
 		}
 		
 		return tablePanel;
 	}
 	//[end] Table Panel
 	
 	//[start] South Panel
 	/**
 	 * Helper Method for initializing the Buttons and slider for the South Panel.
 	 * @return The South Panel, laid out properly, to be displayed.
 	 */
 	private JPanel initializeSouthPanel() {
 		
 		fastSpeedSlider = new JSlider(JSlider.HORIZONTAL,0,100,25);
 		fastSpeedSlider.addChangeListener(new SpeedSliderListener());
 		fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum()-fastSpeedSlider.getMinimum())/4);
 		fastSpeedSlider.setFont(new Font("Arial",Font.PLAIN,8));
 		fastSpeedSlider.setPaintTicks(false);
 		fastSpeedSlider.setPaintLabels(true);
 		//fastSpeedSlider.setBackground(Color.DARK_GRAY);
 		fastSpeedSlider.setForeground(Color.BLACK);
 		fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Quick Playback Speed"));
 		fastSpeedSlider.setEnabled(false);
 		
 		fastReverseButton = new JButton("<|<|");
 		fastReverseButton.addActionListener(new FastReverseButtonListener()); 
 		fastReverseButton.setEnabled(false);
 		
 		reverseButton = new JButton("<|");
 		reverseButton.addActionListener(new ReverseButtonListener());
 		reverseButton.setEnabled(false);
 		
 		pauseButton = new JButton("||");
 		pauseButton.addActionListener(new PauseButtonListener()); 
 		pauseButton.setEnabled(false);
 		
 		forwardButton = new JButton("|>");
 		forwardButton.addActionListener(new ForwardButtonListener());
 		forwardButton.setEnabled(false);
 		
 		fastForwardButton = new JButton("|>|>");
 		fastForwardButton.addActionListener(new FastForwardButtonListener());
 		fastForwardButton.setEnabled(false);
 		
 		playbackSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
 		
 		//playbackSlider.setBackground(Color.LIGHT_GRAY);
 		playbackSlider.setEnabled(false);
 		
 		GridBagLayout southLayout = new GridBagLayout();
 		GridBagConstraints southConstraints = new GridBagConstraints();
 		
 		
 		JPanel buttonPanel = new JPanel();
 		//buttonPanel.setBackground(Color.LIGHT_GRAY);
 		buttonPanel.setLayout(southLayout);
 		
 		buttonPanel.add(fastReverseButton);
 		buttonPanel.add(reverseButton);
 		buttonPanel.add(pauseButton);
 		buttonPanel.add(forwardButton);
 		buttonPanel.add(fastForwardButton);
 		southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
 		southLayout.setConstraints(fastSpeedSlider, southConstraints);
 		buttonPanel.add(fastSpeedSlider);
 		
 		
 		JPanel south = new JPanel();
 		//south.setBackground(Color.LIGHT_GRAY);
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
 	/*private JPanel initializeWestPanel(DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(5, "West Panel");
 		}
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		
 		
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
 		westConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
 		//westLayout.setConstraints(relaxerButton, westConstraints);
 		//west.add(relaxerButton);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(5);
 		}
 		return west;
 	}*/
 	//[end] West Panel
 	
 	//[start] Mouse Context Menu
 	private void initializeMouseContext(final DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
 		mouseContext = new JPopupMenu("Mouse Mode");
 		JMenuItem picking = new JMenuItem("Picking");
 		picking.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gm.setMode(Mode.PICKING);
 				int size = mouseContext.getComponentCount();
 				for(int i = size-1;i>4;i--) {
 					mouseContext.remove(i);
 				}
 				mouseContext.setVisible(false);
 				mouseContext.setEnabled(false);
 			}
 		});
 		
 		JMenuItem transforming = new JMenuItem("Transforming");
 		transforming.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gm.setMode(Mode.TRANSFORMING);
 				int size = mouseContext.getComponentCount();
 				for(int i = size-1;i>4;i--) {
 					mouseContext.remove(i);
 				}
 				mouseContext.setVisible(false);
 				mouseContext.setEnabled(false);
 			}
 		});
 		mouseContext.add("Mouse Mode:").setEnabled(false);
 		mouseContext.add(picking);
 		mouseContext.add(transforming);
 		mouseContext.addSeparator();
 		mouseContext.add("Set Layout:").setEnabled(false);
 	}
 	
 	//[end] Mouse Context Menu
 	
 	//[end] Create Components
 	
 	//[start] init method
 	/**
 	 * applet initialization
 	 */
 	public void init() {
 		
 		//[start] Tabs Pane
 		tabsPane = new JTabbedPane(JTabbedPane.TOP) {
 			
 			private static final long serialVersionUID = -4075340829665484983L;
 			//private int i=0;
 			@Override
 			public void paint(Graphics g) {
 				//i++;
 				//System.out.println("tabsPane paint "+i);
 				try {
 					super.paint(g);
 				} catch(Exception e) {
 					
 				}
 			}
 			
 		};
 		JPanel initialTab = new JPanel();
 		tabsPane.addTab("Welcome", initialTab);
 		
 		tabsPane.setEnabled(false);
 		//[end] Tabs Pane
 		
 		JPanel graphsPanel = new JPanel(new GridLayout(1,1));
 		graphsPanel.add(tabsPane);
 				
 		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		p.setResizeWeight(1);
 		p.add(graphsPanel);
 		p.add(initializeSouthPanel());
 		
 		
 		getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
 		//try set the size
 		getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
 		setJMenuBar(createFileMenu());
 		getContentPane().add(p);
 		setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
 		
 		//startGraph();
 		loadingListeners.add(new LoadingBar());
 	}
 	//[end] init method
 
 	//[start] Init Graph
 	public void startGraph() {
 		for(LoadingListener l : loadingListeners) {
 			l.loadingStarted(7,"Building Visualizer");
 		}
 		tabsPane.removeAll();
 		
 		//layout = springLayoutBuilder(DEFWIDTH,DEFHEIGHT,fullGraph);
 		layout = new FRLayout2<P2PVertex, P2PConnection>(fullGraph);
 		layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingChanged(5, "Building Visualizer");
 		}
 		
 		DefaultModalGraphMouse<P2PVertex,P2PConnection> gm = new DefaultModalGraphMouse<P2PVertex,P2PConnection>(); 
 		GraphMouseListener graphListener = new GraphMouseListener();
 		
 		//[start] Full Visualization Viewer Init
 		fullViewViewer = visualizationViewerBuilder(layout, DEFWIDTH,DEFHEIGHT, gm);
 		fullViewViewer.addMouseListener(graphListener);
 		fullViewViewer.setName("Full View");
 		//add my own vertex shape & color fill transformers
 		initSpecialTransformers(fullViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		fullViewViewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(dynamicGraph));
 		fullViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(dynamicGraph));
 		//[end] Full Visualization Viewer Init
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(1);
 		}
 		//[start] Collapsed Document Visualization Viewer Init
 		collapsedDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		collapsedDocumentViewViewer.addMouseListener(graphListener);
 		collapsedDocumentViewViewer.setName("Collapsed Document View");		
 		collapsedDocumentViewViewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(
 				collapsedDocumentViewViewer.getPickedVertexState(),Color.RED, Color.YELLOW, Color.MAGENTA, Color.RED, Color.RED, Color.BLUE));
 		collapsedDocumentViewViewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(
 				VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.ELLIPSE, 
 				P2PVertexShapeTransformer.PEER_SIZE, P2PVertexShapeTransformer.DOC_SIZE, P2PVertexShapeTransformer.PEER_SIZE));
 		collapsedDocumentViewViewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
 		collapsedDocumentViewViewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(EdgeShapeType.QUAD_CURVE,
 				EdgeShapeType.CUBIC_CURVE,EdgeShapeType.LINE,EdgeShapeType.LINE)); //stroke width
 		
 		collapsedDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(dynamicGraph,PeerVertex.class));
 		collapsedDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(dynamicGraph));
 		//[end] Collapsed Document Visualization Viewer Init
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(2);
 		}
 		//[start] Collapsed Peer Visualization Viewer Init
 		collapsedPeerViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		collapsedPeerViewViewer.addMouseListener(graphListener);
 		collapsedPeerViewViewer.setName("Collapsed Peer View");
 		initSpecialTransformers(collapsedPeerViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		collapsedPeerViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(dynamicGraph, DocumentVertex.class));
 		collapsedPeerViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(dynamicGraph));
 		//[end] Collapsed Peer Visualization Viewer Init
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(3);
 		}
 		//[start] Collapsed Peer AndDocument Visualization Viewer Init
 		collapsedPeerAndDocumentViewViewer = visualizationViewerBuilder(fullViewViewer.getGraphLayout(),DEFWIDTH,DEFHEIGHT, fullViewViewer.getGraphMouse());
 		collapsedPeerAndDocumentViewViewer.addMouseListener(graphListener);
 		collapsedPeerAndDocumentViewViewer.setName("Collapsed Peer and Document View");
 		initSpecialTransformers(collapsedPeerAndDocumentViewViewer,VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
 								EdgeShapeType.QUAD_CURVE,
 								EdgeShapeType.CUBIC_CURVE,
 								EdgeShapeType.LINE,
 								EdgeShapeType.LINE);
 		
 		collapsedPeerAndDocumentViewViewer.getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(dynamicGraph,PeerDocumentVertex.class));
 		collapsedPeerAndDocumentViewViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(dynamicGraph));
 		//[end] Collapsed Peer AndDocument Visualization Viewer Init
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(4);
 		}
 		
 		initializeMouseContext(gm);
 		
 		if(myGraphEvolution.isEmpty()) {
 			SliderListener s = new SliderListener();
 			
 			playbackSlider.setMaximum(0);
 			playbackSlider.addChangeListener(s);
 			playbackSlider.addMouseListener(s);
 			
 			/// create the event player
 			eventThread = new EventPlayer(fullGraph, dynamicGraph);
 			eventThread.addEventPlayerListener(this);
 		}
 		else {
 			SliderListener s = new SliderListener();
 			//playbackSlider.setMinimum((int)myGraphEvolution.getFirst().getTime());
 			playbackSlider.setMinimum(0);
 			playbackSlider.setMaximum((int)myGraphEvolution.getLast().getTime());
 			playbackSlider.addChangeListener(s);
 			playbackSlider.addMouseListener(s);
 			
 			
 			/// create the event player
 			eventThread = new EventPlayer(fullGraph, dynamicGraph, myGraphEvolution, playbackSlider);
 			eventThread.addEventPlayerListener(this);
 		}
 		for(LoadingListener l : loadingListeners) {
 			l.loadingProgress(5);
 		}
 		
 		
 		tabsPane.addTab(fullViewViewer.getName(),fullViewViewer);
 		tabsPane.addTab(collapsedDocumentViewViewer.getName(), collapsedDocumentViewViewer);
 		tabsPane.addTab(collapsedPeerViewViewer.getName(), collapsedPeerViewViewer);
 		tabsPane.addTab(collapsedPeerAndDocumentViewViewer.getName(), collapsedPeerAndDocumentViewViewer);
 		tabsPane.setEnabled(true);
 		tabsPane.setIgnoreRepaint(false);
 		
 		for(LoadingListener l : loadingListeners) {
 			l.loadingComplete();
 		}
 		
 		fastReverseButton.setEnabled(true);
 		reverseButton.setEnabled(true);
 		pauseButton.setEnabled(true);
 		forwardButton.setEnabled(false);
 		fastForwardButton.setEnabled(true);
 		playbackSlider.setEnabled(true);
 		fastSpeedSlider.setEnabled(true);
 		
 		layout.lock(true);
 		doRepaint();
 		eventThread.run();
 	}
 	//[end] Init Graph
 	
 	
 	//[end] Initialization
 	
 	//[start] Main
 	/**
 	 * 
 	 * to run this applet as a java application
 	 * @param args optional argument : the log file to process
 	 */
 	public static void main(String[] args) {
 
 		@SuppressWarnings("unused")
 		P2PGraphViewer myapp = new P2PGraphViewer();
 	}
 	//[end] Main
 
 	//[start] Helper Methods
 	
 	//[start] Layout Items for context menu
 	public List<JMenuItem> getLayoutItems() {
 		List<JMenuItem> menuItems = new LinkedList<JMenuItem>();
 		
 		if(tabsPane.getSelectedComponent().getName().equals("Full View")) {
 			JMenuItem frLayout = new JMenuItem("FR Layout");
 			frLayout.addActionListener(new FRLayoutListener(fullViewViewer));
 			
 			JMenuItem kkLayout = new JMenuItem("KK Layout");
 			kkLayout.addActionListener(new KKLayoutListener(fullViewViewer));
 			
 			JMenuItem isomLayout = new JMenuItem("ISOM Layout");
 			isomLayout.addActionListener(new ISOMLayoutListener(fullViewViewer));
 			
 			JMenuItem circleLayout = new JMenuItem("Circle Layout");
 			circleLayout.addActionListener(new CircleLayoutListener(fullViewViewer));
 			
 			JMenuItem springLayout = new JMenuItem("Spring Layout");
 			springLayout.addActionListener(new SpringLayoutListener(fullViewViewer));
 			
 			menuItems.add(circleLayout);
 			menuItems.add(frLayout);
 			menuItems.add(isomLayout);
 			menuItems.add(kkLayout);
 			menuItems.add(springLayout);
 		}
 		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Document View")) {
 			JMenuItem frLayout = new JMenuItem("FR Layout");
 			frLayout.addActionListener(new FRLayoutListener(collapsedDocumentViewViewer));
 			
 			JMenuItem kkLayout = new JMenuItem("KK Layout");
 			kkLayout.addActionListener(new KKLayoutListener(collapsedDocumentViewViewer));
 			
 			JMenuItem isomLayout = new JMenuItem("ISOM Layout");
 			isomLayout.addActionListener(new ISOMLayoutListener(collapsedDocumentViewViewer));
 			
 			JMenuItem circleLayout = new JMenuItem("Circle Layout");
 			circleLayout.addActionListener(new CircleLayoutListener(collapsedDocumentViewViewer));
 			
 			JMenuItem balloonLayout = new JMenuItem("Balloon Layout");
 			balloonLayout.addActionListener(new BalloonLayoutListener(collapsedDocumentViewViewer));
 			
 			JMenuItem treeLayout = new JMenuItem("Tree Layout");
 			treeLayout.addActionListener(new TreeLayoutListener(collapsedDocumentViewViewer));
 			
 			menuItems.add(balloonLayout);
 			menuItems.add(circleLayout);
 			menuItems.add(frLayout);
 			menuItems.add(isomLayout);
 			menuItems.add(kkLayout);
 			menuItems.add(treeLayout);
 		}
 		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Peer View")) {
 			JMenuItem frLayout = new JMenuItem("FR Layout");
 			frLayout.addActionListener(new FRLayoutListener(collapsedPeerViewViewer));
 			
 			
 			JMenuItem kkLayout = new JMenuItem("KK Layout");
 			kkLayout.addActionListener(new KKLayoutListener(collapsedPeerViewViewer));
 			
 			JMenuItem isomLayout = new JMenuItem("ISOM Layout");
 			isomLayout.addActionListener(new ISOMLayoutListener(collapsedPeerViewViewer));
 			
 			JMenuItem circleLayout = new JMenuItem("Circle Layout");
 			circleLayout.addActionListener(new CircleLayoutListener(collapsedPeerViewViewer));
 			
 			menuItems.add(circleLayout);
 			menuItems.add(frLayout);
 			menuItems.add(isomLayout);
 			menuItems.add(kkLayout);
 		}
 		else if(tabsPane.getSelectedComponent().getName().equals("Collapsed Peer and Document View")) {
 			JMenuItem frLayout = new JMenuItem("FR Layout");
 			frLayout.addActionListener(new FRLayoutListener(collapsedPeerAndDocumentViewViewer));
 			
 			JMenuItem kkLayout = new JMenuItem("KK Layout");
 			kkLayout.addActionListener(new KKLayoutListener(collapsedPeerAndDocumentViewViewer));
 			
 			JMenuItem isomLayout = new JMenuItem("ISOM Layout");
 			isomLayout.addActionListener(new ISOMLayoutListener(collapsedPeerAndDocumentViewViewer));
 			
 			JMenuItem circleLayout = new JMenuItem("Circle Layout");
 			circleLayout.addActionListener(new CircleLayoutListener(collapsedPeerAndDocumentViewViewer));
 			
 			menuItems.add(circleLayout);
 			menuItems.add(frLayout);
 			menuItems.add(isomLayout);
 			menuItems.add(kkLayout);
 		}
 		
 		return menuItems;
 	}
 	//[end] Layout Items for context menu
 	
 	//[end] Helper Methods
 	
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
 	
 	//[start] Relaxer and mouse context listener
 	 /**
 	 * an actionlistener that defines the use of a button to stop the spring-layout processing
 	 * @author adavoust
 	 *
 	 */
 	private class GraphMouseListener extends MouseAdapter {
 				
 		@Override 
 		public void mousePressed(MouseEvent e) {
 			doPop(e);
 		}
 		
 		@Override 
 		public void mouseReleased(MouseEvent e) {
 			doPop(e);
 		}
 		
 		private void doPop(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				for(JMenuItem item : getLayoutItems()) {
 					mouseContext.add(item);
 				}
 				mouseContext.setEnabled(true);
 	    		mouseContext.show(null, e.getXOnScreen(), e.getYOnScreen());	    		
 	    	}
 			else if(mouseContext.isVisible()) {
 				int size = mouseContext.getComponentCount();
 				for(int i = size-1;i>4;i--) {
 					mouseContext.remove(i);
 				}
 				mouseContext.setVisible(false);
 				mouseContext.setEnabled(false);
 			}
 		}
 	}
 	//[end] Relaxer and mouse context listener
 
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
 	/**
 	 * @author  Matty
 	 */
 	class SliderListener extends MouseAdapter implements ChangeListener {
 
 		PlayState prevState = PlayState.PAUSE;
 		
 		@Override
 		public void stateChanged(ChangeEvent ce) {
 			JSlider source = (JSlider)ce.getSource();
 			eventThread.goToTime(source.getValue());
 			if(logList != null) { //if log list is initialized and showing
 				if(logList.isVisible()) {
 					logList.clearSelection();
 					logList.addRowSelectionInterval(0, eventThread.getCurrentIndex()-1);
 					logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentIndex()-1, 0, true));
 				}
 			}
 		}
 
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if(((JSlider)(e.getSource())).isEnabled()){
 				prevState = eventThread.getPlayState();
 				eventThread.pause();
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if(((JSlider)(e.getSource())).isEnabled()){
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
 	
 	//[start] Layout Context Listeners
 	
 	//fullViewViewer.getModel().setGraphLayout(new AggregateLayout<P2PVertex, P2PConnection>(
 	//		new AggregateLayout<P2PVertex, P2PConnection>(fullViewViewer.getGraphLayout())));
 	
 	class  FRLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public FRLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AbstractLayout<P2PVertex, P2PConnection> graphLayout = new FRLayout<P2PVertex, P2PConnection>(fullGraph, vv.getSize());
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			graphLayout.lock(true);
 		}
 	}
 	class  ISOMLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public ISOMLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AbstractLayout<P2PVertex, P2PConnection> graphLayout = new ISOMLayout<P2PVertex, P2PConnection>(fullGraph);
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			graphLayout.lock(true);
 		}
 	}
 	class  KKLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public KKLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AbstractLayout<P2PVertex, P2PConnection> graphLayout = new KKLayout<P2PVertex, P2PConnection>(fullGraph);
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			graphLayout.lock(true);
 		}
 	}
 	class  CircleLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public CircleLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			CircleLayout<P2PVertex, P2PConnection> graphLayout = new CircleLayout<P2PVertex, P2PConnection>(fullGraph);
 			graphLayout.setRadius(fullGraph.getVertexCount()*10);
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			graphLayout.lock(true);
 		}
 	}
 	class  SpringLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public SpringLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AbstractLayout<P2PVertex, P2PConnection> graphLayout = new SpringLayout<P2PVertex, P2PConnection>(fullGraph);
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			graphLayout.lock(true);
 		}
 	}
 	class  TreeLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public TreeLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		} 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			TreeLayout<P2PVertex, P2PConnection> graphLayout = 
 				new TreeLayout<P2PVertex, P2PConnection>(P2PNetworkGraph.makeTreeGraph(fullGraph)) {
 				
 				@Override
 				public void setSize(Dimension size) {
 					// The set size method was being called, and it raised an exception every time
 				}
 			};
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 			
 		}
 	}
 	class  BalloonLayoutListener implements ActionListener {
 		VisualizationViewer<P2PVertex,P2PConnection> vv;
 		public BalloonLayoutListener(VisualizationViewer<P2PVertex,P2PConnection> vv) {
 			this.vv = vv;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			BalloonLayout<P2PVertex, P2PConnection> graphLayout = new BalloonLayout<P2PVertex, P2PConnection>(P2PNetworkGraph.makeTreeGraph(fullGraph));
 			graphLayout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
 			
 			vv.getModel().setGraphLayout(graphLayout);
 			mouseContext.setVisible(false);
 			mouseContext.setEnabled(false);
 			int size = mouseContext.getComponentCount();
 			for(int i = size-1;i>4;i--) {
 				mouseContext.remove(i);
 			}
 		}
 	}
 	
 	//[end] Layout Context Listeners
 	
 	//[start] Load Listener
 	class LoadListener implements ActionListener {
 	
 		public void actionPerformed(ActionEvent arg0) {
 			
 			pauseButton.doClick();
 			Thread loadingThread = new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					
 					P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
 					loader.addProgressListener(new LoadingBar());
 					if(loader.doLoad()) {
 						if(myGraphEvolution != null) {
 							myGraphEvolution.clear();
 							eventThread.stopPlayback();
 							fastReverseButton.setEnabled(false);
 							reverseButton.setEnabled(false);
 							pauseButton.setEnabled(false);
 							forwardButton.setEnabled(false);
 							fastForwardButton.setEnabled(false);
 							playbackSlider.setEnabled(false);
 							playbackSlider.setValue(0);
 							tabsPane.setEnabled(false);
 							fastSpeedSlider.setEnabled(false);
 							
 							//When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
 							AbstractLayout<P2PVertex, P2PConnection> graphLayout = new CircleLayout<P2PVertex, P2PConnection>(fullGraph);
 							collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
 						}
 						myGraphEvolution = loader.getLogList();
 						fullGraph = loader.getFullP2PNetworkGraph();
 						dynamicGraph = loader.getDynamicP2PNetworkGraph();
 						startGraph();
 					}
 				}
 			});
 			loadingThread.start();
 		}
 	}
 	//[end] Load Listener
 	
 	//[end] Swing Event Listeners
 	
 	//[start] Network Listeners
 	@Override
 	public synchronized void incomingLogEvents(InputStream inStream) {
 		try {
 			eventThread.pause();
 			LinkedList<LogEvent> events;
 			synchronized(fullGraph) {
 				events = P2PNetworkGraphLoader.buildLogs(inStream, networkClient, fullGraph);
 			}
 			if(!events.isEmpty()) {
 				networkClient.setLatestTime(events.getLast().getTime());
 				events.addLast(LogEvent.getEndEvent(events.getLast()));
 				eventThread.addEvents(events);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void incomingGraph(InputStream inStream) {
 		
 		try {
 			P2PNetworkGraphLoader loader = P2PNetworkGraphLoader.buildGraph(inStream, networkClient);
 			
 			if(myGraphEvolution != null) {
 				myGraphEvolution.clear();
 				eventThread.stopPlayback();
 				fastReverseButton.setEnabled(false);
 				reverseButton.setEnabled(false);
 				pauseButton.setEnabled(false);
 				forwardButton.setEnabled(false);
 				fastForwardButton.setEnabled(false);
 				playbackSlider.setEnabled(false);
 				playbackSlider.setValue(0);
 				tabsPane.setEnabled(false);
 				fastSpeedSlider.setEnabled(false);
 				
 				//When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
 				AbstractLayout<P2PVertex, P2PConnection> graphLayout = new CircleLayout<P2PVertex, P2PConnection>(fullGraph);
 				collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
 			}
 			
 			myGraphEvolution = loader.getLogList();
 			fullGraph = loader.getFullP2PNetworkGraph();
 			dynamicGraph = loader.getDynamicP2PNetworkGraph();
 			startGraph();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 	}
 	//[end] Network Listeners
 }

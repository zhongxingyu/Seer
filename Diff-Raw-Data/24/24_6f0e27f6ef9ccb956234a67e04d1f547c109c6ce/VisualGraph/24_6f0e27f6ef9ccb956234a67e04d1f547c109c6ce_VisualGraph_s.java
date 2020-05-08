 package de.bwvaachen.graph.gui.input;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.Rectangle;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.BorderFactory;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JViewport;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.Scrollable;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.bwvaachen.graph.gui.input.controller.IGraphChangedListener;
 import de.bwvaachen.graph.gui.input.controller.IGraphComponentChangedListener;
 import de.bwvaachen.graph.gui.input.visualgraph.AddConnection;
 import de.bwvaachen.graph.gui.input.visualgraph.AddNodeDialog;
 import de.bwvaachen.graph.gui.input.visualgraph.NodeDisplayProvider;
 import de.bwvaachen.graph.gui.input.visualgraph.VisualGraphContainer;
 import de.bwvaachen.graph.gui.input.visualgraph.VisualGraphProperties;
 import de.bwvaachen.graph.gui.input.visualgraph.VisualGraphPropertiesDialog;
 import de.bwvaachen.graph.gui.input.visualgraph.VisualNode;
 import de.bwvaachen.graph.logic.Connection;
 import de.bwvaachen.graph.logic.Graph;
 import de.bwvaachen.graph.logic.Node;
 import de.bwvaachen.graph.logic.Path;
 
 public class VisualGraph extends JPanel implements IGraphChangedListener {
 
 	private HashMap<Node, VisualNode> nodes = new HashMap<Node, VisualNode>();
 	private Graph graph;
 	private JPanel panel;
 	private HashSet<IGraphComponentChangedListener> graphComponentListener = new HashSet<IGraphComponentChangedListener>();
 	private NodeDisplayProvider nodeDisplayProvider;
 	private boolean editMode;
 	private double scaleFactor=1;
 	
 	private VisualGraphProperties properties;
 	private JScrollPane scrollPane;
 	private JSlider slider;
 
 	/**
 	 * Create the panel.
 	 */
 	public VisualGraph(boolean editMode) {
 		this.editMode = editMode;
 		setLayout(new BorderLayout());
 		graph = new Graph();
 		panel = new MyPanel();
 		slider = new JSlider(0,100);
 		slider.setValue(10);
 		slider.setOrientation(JSlider.VERTICAL);
 		slider.setMajorTickSpacing( 10 );
 		slider.setMinorTickSpacing( 1 );
 		slider.setBorder( BorderFactory.createTitledBorder("Scale0.1"));
 		slider.setPaintTicks( true );
 	    slider.setPaintLabels( true );
 	    slider.setSnapToTicks( true );
 	    slider.addChangeListener( new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				VisualGraph.this.setScaleFactor(slider.getValue()/10.0);
 				VisualGraph.this.update();
 			}
 		});
 
 		add(slider,BorderLayout.WEST);
 		if (editMode) {
 			JPopupMenu popupMenu = new JPopupMenu();
 			addPopup(panel, popupMenu);
 
 			JMenuItem mntmAddNode = new JMenuItem("Add Node");
 			mntmAddNode.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					PointerInfo info = MouseInfo.getPointerInfo();
 					Point location = info.getLocation();
 					Point locationOfInsert = VisualGraph.this
 							.getMousePosition();
 					AddNodeDialog addNodeDialog = new AddNodeDialog(graph,
 							location);
 					addNodeDialog.setVisible(true);
 					if (addNodeDialog.newNodeWasCreated()) {
 						Node node = addNodeDialog.getNode();
 						if (locationOfInsert == null)
 							locationOfInsert = new Point(0, 0);
 						addNodeAtPosition(node, locationOfInsert);
 					}
 				}
 
 			});
 			popupMenu.add(mntmAddNode);
 
 			JMenuItem mntmAddConnection = new JMenuItem("Add Connection");
 			mntmAddConnection.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					AddConnection addConnection = new AddConnection(graph);
 					if(addConnection.isConnectionAdded())
 					{
 						repaint(0, 0, getSize().width, getSize().height);
 						commitChange();
 					}
 				}
 			});
 			popupMenu.add(mntmAddConnection);
 			
 			JMenuItem mntmProperties=new JMenuItem("Properties");
 			mntmProperties.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					VisualGraphPropertiesDialog visualGraphPropertiesDialog=new  VisualGraphPropertiesDialog(properties);
 					visualGraphPropertiesDialog.setVisible(true);
 					panel.setPreferredSize(properties.getSize());
 					scrollPane.revalidate();
 					recalculate();
 					repaint(0, 0, getSize().width, getSize().height);
 				}
 			});
 			popupMenu.add(mntmProperties);
 		}
 		panel.setLayout(null);
 		panel.setSize(new Dimension(430, 430));
 		panel.setPreferredSize(new Dimension(430, 430));
 //		panel.setMinimumSize(new Dimension(430, 430));
 		panel.setAutoscrolls(true);
 		scrollPane = new JScrollPane();
 		scrollPane.setViewportView(panel);
 		add(scrollPane,BorderLayout.CENTER);
 		properties=new VisualGraphProperties(1,1,3,Color.BLACK,Color.BLACK,Color.GREEN,true,false,false,null,panel.getSize());
 	}
 
 
 	public VisualGraph(Graph graph, boolean editMode) {
 		this(editMode);
 		initGraph(graph);
 		repaint(0, 0, getSize().width, getSize().height);
 	}
 
 	public VisualGraph(VisualGraphContainer container, boolean editMode) {
 		this(container.getGraph(), editMode);
 		this.properties=container.getProperties();
 		setPositionOfNodes(container);
 		repaint(0, 0, getSize().width, getSize().height);
 	}
 
 	public void setPositionOfNodes(VisualGraphContainer container) {
 		for (Entry<Node, Point> entry : container.getPointMap().entrySet()) {
 			VisualNode visualNode = this.nodes.get(entry.getKey());
 			if (visualNode != null)
 				visualNode.setLocation(entry.getValue());
 		}
 		properties=container.getProperties();
 		panel.setPreferredSize(properties.getSize());
 	}
 
 	public VisualGraphContainer getVisualGraphContainer() {
 		Graph graph = new Graph(this.graph);
		return new VisualGraphContainer(graph, this.nodes, properties);
 	}
 
 	private void addNodeAtPosition(Node node, Point position) {
 		VisualNode visualNode = new VisualNode(this, node, null);
 		visualNode.setLocation(position);
 		nodes.put(node, visualNode);
 		graph.addNode(node);
 		commitChange();
 	}
 
 	public void commitChange() {
 		for (IGraphComponentChangedListener listener : graphComponentListener) {
 			listener.graphChanged(graph);
 		}
 	}
 
 	@Override
 	public void graphChanged(Graph graph) {
 		panel.removeAll();
 		HashMap<Node, VisualNode> equalElements = equalElements(graph);
 		if (equalElements.size() == 0) {
 			initGraph(graph);
 		} else {
 			nodes = new HashMap<Node, VisualNode>();
 			Point position = new Point(0, 0);
 			for (Node node : graph.getNodes()) {
 				Number weight = calculateWeight(graph, node);
 				VisualNode visualNode = null;
 				if ((visualNode = equalElements.get(node)) != null) {
 					visualNode.setWeight(weight);
 
 				} else {
 					visualNode = new VisualNode(this, node, weight);
 					createPopupForVisualNode(visualNode);
 					visualNode.setLocation(position);
 					position.x += visualNode.getWidth();
 					if (position.x + 30 > this.getWidth()) {
 						position.x = 0;
 						position.y += 30;
 					}
 				}
 				panel.add(visualNode);
 				nodes.put(node, visualNode);
 			}
 			this.graph = graph;
 		}
 update();
 	}
 
 	public double getScaleFactor()
 	{
 		return this.scaleFactor;
 	}
 	public void setScaleFactor(double scaleFactor)
 	{
 		this.scaleFactor=scaleFactor;
		properties.setScaleFactor((int) (properties.getScaleFactor()*1/scaleFactor));
 		for(Entry<Node,VisualNode>entry:nodes.entrySet())
 		{
 			entry.getValue().updateLocation();
 		}
 	}
 	private void createPopupForVisualNode(final VisualNode visualNode) {
 		if (editMode) {
 			final JPopupMenu popupMenu = new JPopupMenu("Popup");
 			visualNode.addMouseListener(new MouseAdapter() {
 				public void mousePressed(MouseEvent ev) {
 					if (ev.getButton() == 3) {
 						Point mousePosition = VisualGraph.this
 								.getMousePosition();
 						if (mousePosition != null) {
 							popupMenu.show(VisualGraph.this, mousePosition.x,
 									mousePosition.y);
 						}
 					}
 				}
 			});
 			JMenuItem mntmRemoveNode = new JMenuItem("Remove Node");
 			mntmRemoveNode.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					VisualGraph.this.removeNode(visualNode.getNode());
 				}
 			});
 			popupMenu.add(mntmRemoveNode);
 		}
 	}
 
 	private HashMap<Node, VisualNode> equalElements(Graph newGraph) {
 		HashMap<Node, VisualNode> result = new HashMap<Node, VisualNode>();
 		for (Node node : newGraph.getNodes()) {
 
 			if (nodes.containsKey(node)) {
 				result.put(node, nodes.get(node));
 			}
 		}
 		return result;
 	}
 
 	public void setNodeDisplayProvider(NodeDisplayProvider provider) {
 		if (this.nodeDisplayProvider != provider) {
 			this.nodeDisplayProvider = provider;
 
 			for (VisualNode node : nodes.values()) {
 				node.setNodeDisplayProvider(provider);
 			}
 		}
 	}
 
 	public void initGraph(Graph graph) {
 		this.graph = graph;
 		panel.setBackground(Color.WHITE);
 		nodes = new HashMap<Node, VisualNode>();
 		LinkedList<Node> nodeList = new LinkedList<Node>(graph.getNodes());
 		LinkedList<Connection> connections = new LinkedList<Connection>(
 				graph.getSortedConnections());
 
 		if (nodeList.isEmpty())
 			return;
 		Node currentNode = nodeList.removeFirst();
 		LinkedList<VisualNode> visualSortedNodesList = new LinkedList<VisualNode>();
 
 		while (!nodeList.isEmpty()) {
 			Number weight = calculateWeight(graph, currentNode);
 			VisualNode visualNode = new VisualNode(this, currentNode, weight);
 			createPopupForVisualNode(visualNode);
 			visualSortedNodesList.addLast(visualNode);
 			panel.add(visualNode);
 			nodes.put(currentNode, visualNode);
 			nodeList.remove(currentNode);
 			if (!connections.isEmpty()) {
 				LinkedList<Connection> deleteList = new LinkedList<Connection>();
 				for (Connection connection : connections) {
 					if (connection.containsNode(currentNode)) {
 						deleteList.add(connection);
 					}
 				}
 				if (!deleteList.isEmpty()) {
 					connections.removeAll(deleteList);
 					Connection connection = deleteList.getFirst();
 					currentNode = (Node) connection
 							.getTheOtherNode(currentNode);
 					continue;
 				}
 			}
 			if (!nodeList.isEmpty())
 				currentNode = nodeList.getFirst();
 		}// end while
 
 		calculatePositionsAnWeights(visualSortedNodesList);
 		for (VisualNode node : nodes.values()) {
 			node.setNodeDisplayProvider(this.nodeDisplayProvider);
 		}
 		update();
 	}
 
 	private Number calculateWeight(Graph graph, Node currentNode) {
 		Number weight = null;
 		for (Path path : graph.getPaths()) {
 			if (path.endsWith(currentNode)) {
 				weight = path.getWeight();
 			}
 		}
 		return weight;
 	}
 
 	public void addGraphComponentChangedListener(
 			IGraphComponentChangedListener listener) {
 		graphComponentListener.add(listener);
 	}
 
 	public void removeGraphComponentChangedListener(
 			IGraphComponentChangedListener listener) {
 		graphComponentListener.remove(listener);
 	}
 
 	private void drawPathsAndConnection(Graphics2D g2d) {
 		if (graph != null) {
 			Stroke stroke = g2d.getStroke();
 			g2d.setStroke(new BasicStroke(properties.getConnectionWeight()));
 			Color color = g2d.getColor();
 			g2d.setColor(properties.getConnectionColor());
 			List<Connection> sortedConnections = graph.getSortedConnections();
 			for (Connection connection : sortedConnections) {
 				VisualNode startNode = nodes.get(connection.getStartNode());
 				VisualNode endNode = nodes.get(connection.getEndNode());
 
 				Point startPoint = startNode.getOutputAnchor(endNode);
 				Point endPoint = endNode.getOutputAnchor(startNode);
 
 				g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
 				// g2d.drawString(connection.getEdge().getWeight().toString(),
 				// (startPoint.x-endPoint.x)/2+endPoint.x,(startPoint.y-endPoint.y)/2+endPoint.y);
 			}
 			List<Path> paths = graph.getPaths();
 			HashSet<Connection> connections = new HashSet<Connection>();
 			for (Path path : paths) {
 				connections.addAll(path.getConnections());
 			}
 			g2d.setColor(properties.getPathColor());
 			g2d.setStroke(new BasicStroke(properties.getPathWeight()));
 			for (Connection connection : connections) {
 				VisualNode startNode = nodes.get(connection.getStartNode());
 				VisualNode endNode = nodes.get(connection.getEndNode());
 
 				Point startPoint = startNode.getOutputAnchor(endNode);
 				Point endPoint = endNode.getOutputAnchor(startNode);
 
 				g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
 
 			}
 			g2d.setStroke(stroke);
 			g2d.setColor(properties.getConnectionColor());
 			for (Connection connection : sortedConnections) {
 				VisualNode startNode = nodes.get(connection.getStartNode());
 				VisualNode endNode = nodes.get(connection.getEndNode());
 
 				Point startPoint = startNode.getOutputAnchor(endNode);
 				Point endPoint = endNode.getOutputAnchor(startNode);
 
 				double weight=connection.getEdge().getWeight().doubleValue();
 				String str_Weight=String.format("%.2f", weight);
 				if(weight==(int)weight)
 					str_Weight=""+(int)weight;
 				g2d.drawString(str_Weight,
 						(startPoint.x - endPoint.x) / 2 + endPoint.x,
 						(startPoint.y - endPoint.y) / 2 + endPoint.y);
 			}
 			g2d.setColor(color);
 		}
 	}
 
 	private void calculatePositionsAnWeights(
 			LinkedList<VisualNode> visualSortedNodesList) {
 		System.out.println("Calculate Position");
 		Rectangle rec2 = getBounds();
 		int width = panel.getWidth();
 		int height = panel.getHeight();
 
 		int counter = 0;
 		int size = visualSortedNodesList.size();
 		int radius = 100;// TODO relativer Radius
 		for (VisualNode visualNode : visualSortedNodesList) {
 
 			double angle = calculateAngle(counter, size);
 			Point point = calculateAnchor(radius, angle);
 			Rectangle rec = visualNode.getBounds();
 			if (point.x <= width / 2)
 				point.x -= rec.width;
 			else
 				point.x += rec.width;
 
 			if (point.y <= height / 2)
 				point.y -= rec.height;
 			else
 				point.y += rec.height;
 			visualNode.setLocation(point);
 			counter++;
 		}
 	}
 
 	private Point calculateAnchor(int radius, double angle) {
 		Point p = new Point();
 		int width = panel.getWidth();
 		int height = panel.getHeight();
 		p.y = (int) (Math.cos(angle) * radius) + height / 2;
 		p.x = (int) (Math.sin(angle) * radius) + width / 2;
 		return p;
 	}
 
 	private double calculateAngle(int counter, int size) {
 		return Math.PI * 2.0 * counter / size;
 	}
 
 	private static void addPopup(Component component, final JPopupMenu popup) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 
 			private void showMenu(MouseEvent e) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 
 	public Graph getGraph() {
 		return graph;
 	}
 
 	public void addConnection(Connection connection) {
 		graph.addConnection(connection);
 		repaint(0, 0, getSize().width, getSize().height);
 		commitChange();
 	}
 
 	protected void removeNode(Node node) {
 		graph.removeNode(node);
 		repaint(0, 0, getSize().width, getSize().height);
 		commitChange();
 	}
 	
 	private void calculateConnectionWeight()
 	{
 		List<Connection> sortedConnections = graph.getSortedConnections();
 		for(Connection c: sortedConnections)
 		{
 			Node startNode = c.getStartNode();
 			Node endNode = c.getEndNode();
 			
 			VisualNode visualNode1 = nodes.get(startNode);
 			VisualNode visualNode2 = nodes.get(endNode);
 			double weight=0;
 			
 			Point p1=visualNode1.getLocation();
 			Point p2=visualNode2.getLocation();
 			
 			int x=p1.x-p2.x;
 			int y=p1.y-p2.y;
			weight=Math.sqrt(x*x+y*y)*properties.getScaleFactor();
 			
 			c.getEdge().setWeight(weight);
 		}
 		commitChange();
 	}
 
 
 	public void recalculate() {
 		if(properties.isScaleFactorIsUsed())
 			calculateConnectionWeight();		
 	}
 
 	class MyPanel extends JPanel{
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			if(properties.isBackgroundImageIsShown()&& properties.getBackgroundImage()!=null)
 			{
 			Graphics2D g2d=(Graphics2D) g;
 			if(scaleFactor==0)
 				scaleFactor=0.1;
 //			g2d.scale(scaleFactor, scaleFactor);
 			g.drawImage(properties.getBackgroundImage(), 0, 0,(int)(properties.getSize().getWidth()*scaleFactor),(int)(properties.getSize().getHeight()*scaleFactor), VisualGraph.this);
 //			g2d.scale(1.0,1.0);
 			}
 			drawPathsAndConnection((Graphics2D) g);
 		}	
 
 	}
 
 	public VisualGraphProperties getProperties() {
 		return properties;
 	}
 
 
 	public void update() {
 //		setPreferredSize(new Dimension((int)(properties.getSize().width*scaleFactor),(int)( properties.getSize().height*scaleFactor)));
 //		setSize(new Dimension((int)(properties.getSize().width*scaleFactor),(int)( properties.getSize().height*scaleFactor)));
 		panel.setPreferredSize(new Dimension((int)(properties.getSize().width*scaleFactor),(int)( properties.getSize().height*scaleFactor)));
 		//panel.setSize(new Dimension((int)(properties.getSize().width*scaleFactor),(int)( properties.getSize().height*scaleFactor)));
 //		scrollPane.setPreferredSize(new Dimension((int)(properties.getSize().width*scaleFactor),(int)( properties.getSize().height*scaleFactor)));
 //		JViewport viewport = scrollPane.getViewport();
 //(0, 0, properties.getSize().width, properties.getSize().height);
 //		scrollPane.setViewport(viewport);
 		scrollPane.revalidate();
 		repaint();
 	};
 }

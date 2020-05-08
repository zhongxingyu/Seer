 package algorithm;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import model.DirectedEdge;
 import model.Edge;
 import model.Graph;
 import model.Vertex;
 
 /**
  * dijkstra's algorithm
  * find shortest path between two vertices
  */
 public class Dijkstra extends DefaultGraphAlgorithm {
 	/**
 	 * the starting point vertex
 	 */
 	private Vertex origin;
 	
 	/**
 	 * the ending point vertex
 	 */
 	private Vertex target;
 	
 	/**
 	 * list of boxed vertexes
 	 * a vertex is boxed, when all its edges were traversed
 	 */
 	private HashSet<Vertex> boxed = new HashSet<Vertex>();
 	
 	/**
 	 * default constructor
 	 * 
 	 * @param graph graph
 	 * @param origin origin
 	 * @param target target
 	 */
 	public Dijkstra(Graph graph, Vertex origin, Vertex target) {
 		super(graph);
 		this.origin = origin;
 		this.target = target;
 	}
 	
 	/**
 	 * run the algorithm
 	 */
 	public void execute() {
 		
 		// prequisites
 		if (origin == null || target == null) {
 			System.out.println("origin or target is null, cannot run algorithm");
 			return;
 		}
 		
 		// prepare edges for display
 		for (Edge e : graph.getEdges()) {
 			e.setColor(Color.gray);
 		}
 		
 		// set initial label
 		origin.setLabel(0);
 		
 		// box initial vertex
 		boxVertex(origin);
 
 		// loop through all vertices
 		Vertex boxedVertex;
 		while (null != (boxedVertex = getLowestVertex())) {
 			if (boxVertex(boxedVertex)) {
 				// found shortest path
 				break;
 			}
 		}
 		
 		// vertex not found, some error
 		if (boxedVertex == null) {
 			System.out.println("Error - no vertex found for boxing");
 			return;
 		}
 		
 		System.out.println("-----");
 		
 		// we're done
 		
 		// trace way back
 		Vertex vertex = target;
 		System.out.println(vertex);
 		
 		// viz
 		vertex.setColor(Color.green);
 		breakPoint();
 		Edge shortest = Graph.getShortestEdge(graph.getVerticesEdges(vertex, vertex.getOrigin()));
 		if (shortest != null) {
 			shortest.setColor(Color.green);
 			breakPoint();
 		}
 		
 		while (vertex != origin) {
 			vertex = vertex.getOrigin();
 			System.out.println(vertex);
 			
 			// viz
 			vertex.setColor(Color.green);
 			breakPoint();
 			shortest = Graph.getShortestEdge(graph.getVerticesEdges(vertex, vertex.getOrigin()));
 			if (shortest != null) {
 				shortest.setColor(Color.green);
 				breakPoint();
 			}
 		}
 	}
 	
 	/**
 	 * box a vertex
 	 * 
 	 * @param v vertex to be boxed
 	 * @return is boxed vertex the target
 	 */
 	private boolean boxVertex(Vertex v) {
 		System.out.println(v);
 		
 		// paint vertex red
 		v.setColor(Color.red);
 		breakPoint();
 		
 		// add newly boxed vertex to boxed array
 		boxed.add(v);
 		
 		// we found our target
 		if (v == target) {
 			return true;
 		}
 		
 		// label all vertices
 		for (DirectedEdge de : graph.getVertexEdges(v)) {
 			
 			// targeted vertex is boxed
 			// we can skip it
 			if (boxed.contains(de.getTarget())) {
 				continue;
 			}
 			
 			// viz
 			de.getEdge().setColor(Color.red);
 			breakPoint();
 			
 			// targeted vertex is already touched
			// unless we can get a better deal, we skip labeling
 			// this also catches going back to parent vertices
 			if (de.getTarget().isLabeled() && de.getEdge().getFullWeight(v) >= de.getTarget().getLabel()) {
 				System.out.println(v + " " + de.getTarget() + " " + de.getTarget().getLabel() + " unprofitable");
 				continue;
 			}
 			
 			// set label
 			de.getTarget().setLabel(de.getFullWeight());
 			de.getTarget().setOrigin(v);
 
 			breakPoint();
 			
 			System.out.println(v + " " + de.getTarget() + " " + de.getTarget().getLabel());
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * find the vertex with the lowest weight and isn't boxed
 	 * first sort vertices asc, then check conditions
 	 * 
 	 * @return lowest weighted vertex
 	 */
 	private Vertex getLowestVertex() {
 		graph.sortVertices();
 		for (Vertex v : graph.getVertices()) {
 			if (!boxed.contains(v) && v.isLabeled()) {
 				return v;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * window displaying settings of the algorithm
 	 * 
 	 * @param parent parent window
 	 */
 	public void settingsFrame(JFrame parent) {
 		// create new dialog window
 		final JDialog dialog = new JDialog(parent, "Settings");
 		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		dialog.setModal(true);
 		dialog.setSize(300, 150);
 		
 		// layout
 		dialog.getContentPane().setLayout(new GridLayout(0, 2));
 		
 		// origin selector
 		dialog.getContentPane().add(new JLabel("Start Vertex"));
 		final JComboBox originVertexBox = new JComboBox(graph.getVertices());
 		dialog.getContentPane().add(originVertexBox);
 		
 		if (origin != null) {
 			originVertexBox.setSelectedItem(origin);
 		}
 		
 		// target selector
 		dialog.getContentPane().add(new JLabel("Target Vertex"));
 		final JComboBox targetVertexBox = new JComboBox(graph.getVertices());
 		dialog.getContentPane().add(targetVertexBox);
 		
 		if (target != null) {
 			targetVertexBox.setSelectedItem(target);
 		}
 		
 		// okay button
 		dialog.getContentPane().add(new JLabel(""));
 		JButton saveButton = new JButton("Okay");
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				origin = (Vertex) originVertexBox.getSelectedItem();
 				target = (Vertex) targetVertexBox.getSelectedItem();
 				dialog.dispose();
 				System.out.println("options saved");
 			}
 		});
 		dialog.getContentPane().add(saveButton);
 		dialog.getRootPane().setDefaultButton(saveButton);
 		
 		dialog.setVisible(true);
 	}
 	
 	/**
 	 * reset algorithm to a neutral state
 	 */
 	public void reset() {
 		super.reset();
 		
 		boxed.clear();
 		
 		gui.repaint();
 	}
 }

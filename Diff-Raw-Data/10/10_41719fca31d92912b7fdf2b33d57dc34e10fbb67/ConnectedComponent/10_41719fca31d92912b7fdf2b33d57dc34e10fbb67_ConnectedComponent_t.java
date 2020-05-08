 package dna.metrics.connectedComponents;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import dna.graph.Graph;
 import dna.graph.IElement;
 import dna.graph.edges.Edge;
 import dna.graph.nodes.Node;
 import dna.metrics.Metric;
 import dna.series.data.Distribution;
 import dna.series.data.NodeNodeValueList;
 import dna.series.data.NodeValueList;
 import dna.series.data.Value;
 import dna.updates.batch.Batch;
 
 public abstract class ConnectedComponent extends Metric {
 
 	protected HashMap<Node, Integer> nodeComponentMembership;
 	protected boolean[] visited;
 	protected HashMap<Node, Node> parents;
 	protected HashMap<Integer, Component> componentList;
 	protected HashMap<Integer, Integer> componentConnection;
 	protected int counter;
 
 	public ConnectedComponent(String name, ApplicationType type) {
 		super(name, type, MetricType.exact);
 	}
 
 	@Override
 	public void init_() {
 		this.nodeComponentMembership = new HashMap<Node, Integer>();
 		this.parents = new HashMap<Node, Node>();
 		this.visited = new boolean[this.g.getMaxNodeIndex() + 1];
 		this.componentList = new HashMap<Integer, Component>();
 		this.componentConnection = new HashMap<>();
 		this.counter = 0;
 	}
 
 	@Override
 	public void reset_() {
 		this.nodeComponentMembership = new HashMap<Node, Integer>();
 		this.visited = new boolean[this.g.getMaxNodeIndex() + 1];
 		this.parents = new HashMap<Node, Node>();
 		this.componentList = new HashMap<Integer, Component>();
 		this.componentConnection = new HashMap<>();
 		this.counter = 0;
 	}
 
 	@Override
 	public boolean compute() {
 		for (IElement ie : g.getNodes()) {
 			Node n = (Node) ie;
 			if (!this.visited[n.getIndex()]) {
 				bfs(n);
 			}
 		}
 
 		return true;
 	}
 
 	protected void bfs(Node node) {
 		int comp = counter++;
 		Queue<Node> q = new LinkedList<Node>();
 		Component root = new Component(comp);
 		int size = 0;
 		this.componentList.put(comp, root);
 		q.add(node);
 		visited[node.getIndex()] = true;
 		while (!q.isEmpty()) {
 			size++;
 			Node temp = q.poll();
 			this.nodeComponentMembership.put(temp, comp);
 			for (IElement ie : temp.getEdges()) {
 				Edge n = (Edge) ie;
 				Node dst = n.getDifferingNode(temp);
 				if (!visited[dst.getIndex()]) {
 					visited[dst.getIndex()] = true;
 					parents.put(dst, temp);
 					q.add(dst);
 				}
 			}
 		}
 		root.setSize(size);
 	}
 
 	@Override
 	public boolean equals(Metric m) {
 		if (!(m instanceof ConnectedComponent)) {
 			return false;
 		}
 		ConnectedComponent cc = (ConnectedComponent) m;
 
 		boolean success = true;
 		if (this.componentList.size() != cc.componentList.size()) {
 			System.out.println("diff @ number of components expected "
 					+ this.componentList.size() + " is "
 					+ cc.componentList.size());
 
 			success = false;
 		}
 
 		HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer>();
 		for (Component cV : this.componentList.values()) {
 			if (sizes.containsKey(cV.getSize())) {
 				sizes.put(cV.getSize(), sizes.get(cV.getSize()) + 1);
 			} else {
 				sizes.put(cV.getSize(), +1);
 			}
 		}
 		for (Component cV : cc.componentList.values()) {
 			if (!sizes.containsKey(cV.getSize())) {
 				System.out.println("no existing size " + cV.getSize()
 						+ " Index " + cV.getIndex());
 				success = false;
 			} else if (sizes.get(cV.getSize()) == 0) {
 				System.out.println("to much of this size " + cV.getSize()
 						+ " Index " + cV.getIndex());
 				success = false;
 			} else {
 				sizes.put(cV.getSize(), sizes.get(cV.getSize()) - 1);
 			}
 
 		}
 
 		HashMap<Integer, Integer> check = new HashMap<>();
 		for (IElement node : g.getNodes()) {
 			Node n = (Node) node;
 			int id1 = this.nodeComponentMembership.get(n);
 			int id2 = cc.lookUp(n);
 			int size1 = this.componentList.get(id1).getSize();
 			int size2 = cc.componentList.get(id2).getSize();
 
 			if (size1 != size2) {
 				System.out.println("component with wrong size for node " + n
 						+ " expected " + size1 + " is " + size2);
 				success = false;
 			}
 			if (check.containsKey(id1)) {
 				if (!check.get(id1).equals(id2)) {
 					System.out.println("component with wrong index for node "
 							+ n + " expected " + check.get(id1) + " is " + id2);
 					success = false;
 				}
 			} else {
 				check.put(id1, id2);
 			}
 		}
 
 		return success;
 	}
 
 	public int lookUp(Node n) {
 		int result = this.nodeComponentMembership.get(n);
 		while (componentConnection.containsKey(result)) {
 			result = componentConnection.get(result);
 		}
 		return result;
 	}
 
 	// /compcounter
 	@Override
 	public Value[] getValues() {
 		Value v1 = new Value("NumberofComponents", countComponents());
 		Value v2 = new Value("AverageComponentSize",
 				calculateAverageComponentSize());
 
 		return new Value[] { v1, v2 };
 	}
 
 	private double countComponents() {
 		return componentList.size();
 	}
 
 	private double calculateAverageComponentSize() {
 		if (this.componentList.isEmpty()) {
 			return 0;
 		} else {
			return (double) this.g.getNodes().size()
					/ (double) componentList.size();
 		}
 	}
 
 	@Override
 	public Distribution[] getDistributions() {
 		Distribution d1 = new Distribution("Components", calculateComponents());
 		return new Distribution[] { d1 };
 	}
 
 	@Override
 	public NodeValueList[] getNodeValueLists() {
 		return new NodeValueList[] {};
 	}
 
 	@Override
 	public NodeNodeValueList[] getNodeNodeValueLists() {
 		return new NodeNodeValueList[] {};
 	}
 
 	private double[] calculateComponents() {
 		double[] sumComp = new double[componentList.size()];
 		int counter = 0;
 		for (Component n : componentList.values()) {
 			sumComp[counter] = n.getSize();
 			counter++;
 		}
 		return sumComp;
 	}
 
 	@Override
 	public boolean isComparableTo(Metric m) {
 		return m != null && m instanceof ConnectedComponent;
 	}
 
 	@Override
 	public boolean isApplicable(Graph g) {
 		return Node.class.isAssignableFrom(g.getGraphDatastructures()
 				.getNodeType());
 	}
 
 	@Override
 	public boolean isApplicable(Batch b) {
 		return Node.class.isAssignableFrom(b.getGraphDatastructures()
 				.getNodeType());
 	}
 
 }

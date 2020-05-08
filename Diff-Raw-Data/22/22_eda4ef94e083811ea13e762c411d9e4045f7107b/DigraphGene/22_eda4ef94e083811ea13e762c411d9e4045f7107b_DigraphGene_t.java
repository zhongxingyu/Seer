 package bio.genetics;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import physics.Joint;
 import physics.PointMass;
 import physics.Rod;
 
 import environment.Environment;
 import structure.Muscle;
 import structure.Organism;
 
 import static applet.Util.clamp_radians;
 
 /**
  * A gene for creating organisms by traversing a directed graph. Nodes in the graph correspond to joints in structure,
  * and edges correspond to limbs. Edges may be recursively followed to make substructures.
  * 
  * TODO tests (serialization, mutation, f(g) properties)
  */
 public class DigraphGene extends Gene<Organism> {
 	
 	/**
 	 * graph traversal always starts from the same root node (the first node in serialization)
 	 */
 	private GraphNode root;
 	
 	private ArrayList<Integer> ACTIVE_IDS = new ArrayList<Integer>();
 	private int max_id = -1;
 	
 	// mutation rates for single structures
 	private static final String MUT_MASS = "MASS";
 	private static final String MUT_LEN = "LENGTH";
 	private static final String MUT_ANGLE = "ANGLE";
 	// mutation rates for creating structures
 	private static final String MUT_ADD_JOINT = "+JOINT";
 	private static final String MUT_ADD_JOINT_MUSCLE = "+JMUSCLE";
 	private static final String MUT_ADD_ROD_MUSCLE = "+RMUSCLE";
 	// mutation rates for removing structures
 	private static final String MUT_REM_JOINT = "-JOINT";
 	private static final String MUT_REM_JOINT_MUSCLE = "-JMUSCLE";
 	private static final String MUT_REM_ROD_MUSCLE = "-RMUSCLE";
 	// mutation rates for altering graph objects
 	private static final String MUT_EDGE_REC = "RECURSE";
 	private static final String MUT_ADD_CYC = "+CYCLE";
 	private static final String MUT_REM_CYC = "-CYCLE";
 	private static final String MUT_ADD_TERMINAL = "+TERM";
 	private static final String MUT_REM_TERMINAL = "-TERM";
 	// mutation rates for creating graph objects
 	private static final String MUT_ADD_NODE = "+NODE";
 	private static final String MUT_ADD_EDGE = "+EDGE";
 	// mutation rates for removing graph objects
 	private static final String MUT_REM_NODE = "-NODE";
 	private static final String MUT_REM_EDGE = "-EDGE";
 	
 	// note that if we could guarantee that this graph is connected, then only the root would be needed
 	// to track the full structure (by traversing it). But here I assume that there may be 'latent' nodes
 	// or edges with the gene that will be mutated with everything else. Having a list of nodes and edges
 	// also makes serialization and deserialization easier
 	private HashMap<Integer, GraphNode> nodes;
 	private ArrayList<GraphEdge> edges;
 	
 	// lists used during construction
 	private ArrayList<PointMass> all_points;
 	private ArrayList<Rod> all_rods;
 	private ArrayList<Joint> all_joints;
 	private ArrayList<Muscle> all_muscles;
 	
 	public DigraphGene(){
 		nodes = new HashMap<Integer, GraphNode>();
 		edges = new ArrayList<GraphEdge>();
 		this.initMutables(
 				new String[] {MUT_MASS, MUT_LEN, MUT_ANGLE, 
 				MUT_ADD_JOINT, MUT_REM_JOINT, 
 				MUT_ADD_JOINT_MUSCLE, MUT_REM_JOINT_MUSCLE,
 				MUT_ADD_ROD_MUSCLE, MUT_REM_ROD_MUSCLE, 
 				MUT_ADD_NODE, MUT_REM_NODE, 
 				MUT_ADD_EDGE, MUT_REM_EDGE});
 	}
 
 	/**
 	 * This class acts as a node in the directed graph and is responsible for instantiating PointMasses during traversal
 	 * @author wrongu
 	 */
 	private class GraphNode{
 		
 		/** edges in which this node is 'from' */
 		private ArrayList<GraphEdge> edges_out;
 		/** just for logistics, each node gets a unique id. this is unchanged by mutation. */
 		private int local_uid;
 		/** traversal is made easier by keeping track of the last _instances_ that this node created */
 		public PointMass last_instance;
 		public Rod last_incident_rod;
 		/** mass of the resultant pointmass(es) */
 		public double mass;
 		/** whether or not this pointmass is a joint (i.e. it has limits on rotation) */
 		public boolean is_joint;
 		/** low and high limits - only useful if it is a joint */
 		public double rest_low, rest_high;
 		/** if it is a joint, it may be either passive or driven (non-muscle or muscle) */
 		public boolean is_muscle;
 		/** strength of the muscle - only used if 'is_muscle' is true */
 		public double muscle_strength;
 		
 		/**
 		 * Constructor for unjointed nodes
 		 * @param id unique id
 		 * @param m mass of corresponding PointMasses
 		 */
 		public GraphNode(int id, double m){
 			this(id, m, 0.0, 0.0, 0.0);
 			this.is_joint = false;
 			this.is_muscle = false;
 		}
 		
 		/**
 		 * Constructor for jointed but non-muscle nodes
 		 * @param id unique id
 		 * @param m mass of corresponding PointMasses
 		 * @param rl low angle limit
 		 * @param rh high angle limit
 		 */
 		public GraphNode(int id, double m, double rl, double rh){
 			this(id, m, rl, rh, 0.0);
 			this.is_muscle = false;
 		}
 		
 		/**
 		 * Constructor for jointed, muscle-driven nodes
 		 * @param id unique id
 		 * @param m mass of corresponding PointMasses
 		 * @param rl low angle limit
 		 * @param rh high angle limit
 		 * @param strength muscle strength
 		 */
 		public GraphNode(int id, double m, double rl, double rh, double strength){
 			this.local_uid = id;
 			this.mass = m;
 			this.rest_low = rl;
 			this.rest_high = rh;
 			this.muscle_strength = strength;
 			this.is_joint = true;
 			this.is_muscle = true;
 			this.edges_out = new ArrayList<GraphEdge>();
 			DigraphGene.this.nodes.put(id, this);
 			DigraphGene.this.ACTIVE_IDS.add(id);
 			if(id > DigraphGene.this.max_id){
 				DigraphGene.this.max_id = id;
 			}
 			if(DigraphGene.this.root == null){
 				DigraphGene.this.root = this;
 			}
 		}
 		
 		public void addEdge(GraphEdge e){
 			assert(e.from.getId() == this.local_uid);
 			this.edges_out.add(e);
 		}
 		
 		public int getId(){
 			return this.local_uid;
 		}
 		
 		/**
 		 * used for instantiating a pointmass from this node's properties
 		 * @param incident the rod of which the new mass will be the 2nd point (null only when instantiating the root)
 		 * @return the new PointMass instance
 		 */
 		public PointMass createPointMass(Rod incident){
 			this.last_instance = new PointMass(this.mass);
 			this.last_incident_rod = incident;
 			return this.last_instance;
 		}
 	}
 	
 	/**
 	 * This class acts as an edge in the directed graph and is responsible for instantiating Rods during traversal
 	 * @author wrongu
 	 */
 	private class GraphEdge{
 		/** in terms of the directed graph, the 'source' and 'destination' nodes */
 		private GraphNode from, to;
 		/** maximum number of times to follow this edge in a given branch of DFS traversal */
 		public int max_recurse;
 		/** in the current traversal, the number of times this edge has been traversed */
 		public int current_recurse;
 		/** whether or not this edge should create a closed loop (to the last created instance of 'this.to') */
 		public boolean is_cyclic;
 		/** terminal edges are only followed when a node's sibling edges' recursion is maxed out */
 		public boolean is_terminal;
 		// TODO scale (or other affine transform) for recursive steps
 		/** rods have a minimum and maximum rest length */
 		public double rest_low, rest_high;
 		/** rods may or may not be driven */
 		public boolean is_muscle;
 		/** if rods of this edge are driven, this is their strength multiplier */
 		public double muscle_strength;
 		
 		public GraphEdge(GraphNode from, GraphNode to, int rec, boolean cyc, boolean term, double rl, double rh, boolean muscle, double strength){
 			this.from = from;
 			this.to = to;
 			this.max_recurse = rec;
 			this.is_cyclic = cyc;
 			this.is_terminal = term;
 			this.rest_low = rl;
 			this.rest_high = rh;
 			this.is_muscle = muscle;
 			this.muscle_strength = strength;
 			this.recursionReset();
 			this.from.addEdge(this);
 			DigraphGene.this.edges.add(this);
 		}
 		
 		public void recursionReset(){
 			this.current_recurse = 0;
 		}
 	}
 	
 	@Override
 	public DigraphGene mutate(Random r) {
 		super.metaMutate(r);
 		// (maybe) alter existing graph elements
 		for(GraphNode n: this.nodes.values()){
 			if(r.nextDouble() < mutationRate(MUT_MASS)){
 				n.mass *= (r.nextDouble() + 0.5);
 				if(n.mass < 1.0) n.mass = 1.0;
 			}
 			if(n.is_joint){
 				if(r.nextDouble() < mutationRate(MUT_REM_JOINT)){
 					n.is_joint = false;
 				} else{
 					if(r.nextDouble() < mutationRate(MUT_ANGLE)){
 						n.rest_low = clamp_radians(r.nextDouble() * 2.0 * Math.PI);
 						n.rest_high = clamp_radians(n.rest_low + r.nextDouble() * 2.0 * Math.PI);
 					}
 					if(n.is_muscle){
 						if(r.nextDouble() < mutationRate(MUT_REM_JOINT_MUSCLE)){
 							n.is_muscle = false;
 						}
 					} else{
 						if(r.nextDouble() < mutationRate(MUT_ADD_JOINT_MUSCLE)){
 							n.is_muscle = true;
 							n.muscle_strength = 1.0;
 						}
 					}
 				}
 			} else{
 				if(r.nextDouble() < mutationRate(MUT_ADD_JOINT)){
 					n.is_joint = true;
 				}
 			}
 		}
 		for(GraphEdge e: this.edges){
 			// mutate structural properties
 			if(r.nextDouble() < mutationRate(MUT_LEN)){
 				e.rest_low = r.nextDouble() * 50;
 				e.rest_high = e.rest_low + r.nextDouble() * 50;
 			}
 			if(e.is_muscle){
 				if(r.nextDouble() < mutationRate(MUT_REM_ROD_MUSCLE)){
 					e.is_muscle = false;
 				}
 			} else{
 				if(r.nextDouble() < mutationRate(MUT_ADD_ROD_MUSCLE)){
 					e.is_muscle = true;
 					e.muscle_strength = 1.0;
 				}
 			}
 			// mutate graph traversal properties
 			if(e.is_cyclic){
 				if(r.nextDouble() < mutationRate(MUT_REM_CYC)){
 					e.is_cyclic = false;
 				}
 			} else{
 				if(r.nextDouble() < mutationRate(MUT_ADD_CYC)){
 					e.is_cyclic = true;
 				}
 			}
 			if(e.is_terminal){
 				if(r.nextDouble() < mutationRate(MUT_REM_TERMINAL)){
 					e.is_terminal = false;
 				}
 			} else{
 				if(r.nextDouble() < mutationRate(MUT_ADD_TERMINAL)){
 					e.is_terminal = true;
 				}
 			}
 			if(r.nextDouble() < mutationRate(MUT_EDGE_REC)){
 				// add to limit
 				if(r.nextDouble() >= 0.5){
 					e.max_recurse += 1;
 				}
 				// subtract from limit
 				else{
 					e.max_recurse -= 1;
 					if(e.max_recurse < 1) e.max_recurse = 1;
 				}
 			}
 		}
 		
 		// (maybe) add new graph elements
 		if(r.nextDouble() < mutationRate(MUT_ADD_NODE)){
 			new GraphNode(++max_id, 1.0 + r.nextDouble()*4.0);
 		}
 		if(nodes.size() > 0 && r.nextDouble() < mutationRate(MUT_ADD_EDGE)){
 			int n_ids = ACTIVE_IDS.size();
 			int id_f = ACTIVE_IDS.get(r.nextInt(n_ids));
 			int id_t = ACTIVE_IDS.get(r.nextInt(n_ids));
 			boolean cyc = r.nextDouble() < mutationRate(MUT_ADD_CYC);
 			boolean term = r.nextDouble() < mutationRate(MUT_ADD_CYC);
 			int rec = 1;
 			double rest_low = r.nextDouble() * 50;
 			double rest_high = rest_low + r.nextDouble() * 50;
 			new GraphEdge(nodes.get(id_f), nodes.get(id_t), rec, cyc, term, rest_low, rest_high, false, 0.0);
 		}
 		
 		// (maybe) remove existing graph elements
 		if(nodes.size() > 1){
 			if(r.nextDouble() < mutationRate(MUT_REM_NODE)){
 				int rand_index = r.nextInt(ACTIVE_IDS.size());
 				this.nodes.remove(ACTIVE_IDS.get(rand_index));
 				ACTIVE_IDS.remove(rand_index);
 			}
 		}
 		if(edges.size() > 0){
 			if(r.nextDouble() < mutationRate(MUT_REM_EDGE)){
 				this.edges.remove(r.nextInt(this.edges.size()));
 			}
 		}
 		return this; // TODO clone
 	}
 	
 	/**
 	 * Express this gene as an organism using graph traversal
 	 * @see traverse_helper
 	 * 
 	 * @param posx the x position of the root (first-created point)
 	 * @param posy the y position of the root (first-created point)
 	 * @param e the environment into which this organism should be added
 	 */
 	@Override
 	public Organism create(double posx, double posy, Environment e) {
 		// initialize arrays (effectively clearing them)
 		this.all_points = new ArrayList<PointMass>();
 		this.all_rods = new ArrayList<Rod>();
 		this.all_joints = new ArrayList<Joint>();
 		this.all_muscles = new ArrayList<Muscle>();
		if(this.root != null){
			// ensure that all edges are reset in terms of recursion depth
			for(GraphEdge ed : this.edges) ed.recursionReset();
			// start creating structure from the root
			PointMass init_pm = this.root.createPointMass(null);
			init_pm.initPosition(posx, posy);
			this.all_points.add(init_pm);
			// crazy traversal algorithm
			traverse_helper(this.root);
		} else{
			System.err.println("Attempting to create an empty organism.. creating a single pointmass instead");
			this.all_points.add(new PointMass(1.0));
		}
 		Organism o = new Organism(e);
 		o.addAllPointMasses(all_points);
 		o.addAllRods(all_rods);
 		o.addAllJoints(all_joints);
 		o.addAllMuscles(all_muscles);
 		return o;
 	}
 	
 	/**
 	 * Create a rod and the destination's pointmass. add in all joints and muscles as specified.
 	 * @param e
 	 * @param parent
 	 */
 	private void instantiateGraphEdge(GraphEdge e, GraphNode parent){
 		// create a rod for this edge with it's first point equal to
 		// where we are coming from
 		Rod r = new Rod(e.rest_low, e.rest_high);
 		this.all_rods.add(r);
 		r.addPoint(parent.last_instance);
 
 		PointMass next;
 		// note that "cycle" only works if destination already exists - otherwise it acts
 		// as non-cyclic and creates the destination
 		if(e.is_cyclic && e.to != e.from && e.to.last_instance != null){
 			next = e.to.last_instance;
 			e.to.last_incident_rod = r;
 		} else {
 			// instantiate destination pointmass
 			next = e.to.createPointMass(r);
 			this.all_points.add(next);
 			// decide where in space it goes using some trig
 			double l = (r.getRestValue1() + r.getRestValue2()) / 2.0;
 			// angle of the rod based on joint and previous rod
 			double angle = parent.last_incident_rod.getAngleOffHorizontal();
 			// add in mean joint angle
 			if(parent.is_joint)
 				angle += (clamp_radians(root.rest_low) + clamp_radians(root.rest_high)) / 2.0;
 			// compute x and y positions based on parent's position
 			double x = parent.last_instance.getX() + l * Math.cos(angle);
 			double y = parent.last_instance.getY() + l * Math.sin(angle);
 			// set the position
 			next.initPosition(x, y);
 		}
 		// add this pointmass as the other end of the rod
 		r.addPoint(next);
 			
 		// if 'parent' is a joint, set it's parameters here, now that both affected rods are created
 		if(parent.is_joint && parent.last_incident_rod != null){
 			Joint j = new Joint(
 					clamp_radians(parent.rest_low), clamp_radians(parent.rest_high),
 					parent.last_instance, parent.last_incident_rod, r);
 			this.all_joints.add(j);
 			// if parent is a driven joint, add the muscle
 			if(parent.is_muscle){
 				Muscle m = new Muscle(j, parent.muscle_strength);
 				this.all_muscles.add(m);
 			}
 		}
 		
 		// create muscle for this rod if specified
 		if(e.is_muscle){
 			Muscle m = new Muscle(r, e.muscle_strength);
 			this.all_muscles.add(m);
 		}
 	}
 	
 	private void traverse_helper(GraphNode root){
 		boolean recursion_taken = false;
 		// handle non-terminal edges
 		for(GraphEdge e : root.edges_out){
 			if(!e.is_terminal && e.current_recurse < e.max_recurse){
 				recursion_taken = true;
 				// mark another step of dfs traversal
 				e.current_recurse += 1;
 				// create structures
 				this.instantiateGraphEdge(e, root);
 				// recurse (DFS)
 				this.traverse_helper(e.to);
 				// when traversal is done (i.e. when the above line is finished),
 				// set recursive depth back to 0
 				e.recursionReset();
 			}
 		}
 		if(!recursion_taken){
 			// handle terminal edges
 			for(GraphEdge e : root.edges_out){
 				if(e.is_terminal && e.current_recurse <= e.max_recurse){
 					// mark another step of dfs traversal
 					e.current_recurse += 1;
 					// create structures
 					this.instantiateGraphEdge(e, root);
 					// recurse (DFS)
 					this.traverse_helper(e.to);
 					// when traversal is done (i.e. when the above line is finished),
 					// set recursive depth back to 0
 					e.recursionReset();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void serialize(OutputStream s) throws IOException {
 		super.serialize(s);
 		DataOutputStream dest = new DataOutputStream(s);
 		// NOTE that local_uid is never mutated but only used for convenience when
 		// serializing and deserializing.
 		//
 		// write all nodes
 		dest.writeInt(nodes.size());
 		for(GraphNode node : this.nodes.values()){
 			// write node id first
 			dest.writeInt(node.getId());
 			// next write three doubles for mass and rest angles
 			dest.writeDouble(node.mass);
 			dest.writeBoolean(node.is_joint);
 			if(node.is_joint){
 				dest.writeDouble(node.rest_low);
 				dest.writeDouble(node.rest_high);
 				// next write muscle info: byte for T/F plus strength
 				dest.writeBoolean(node.is_muscle);
 				if(node.is_muscle) dest.writeDouble(node.muscle_strength);
 			}
 		}
 		// write all edges
 		dest.writeInt(edges.size());
 		for(GraphEdge edge : this.edges){
 			// write graph porperties
 			dest.writeInt(edge.from.getId());
 			dest.writeInt(edge.to.getId());
 			dest.writeInt(edge.max_recurse);
 			dest.writeBoolean(edge.is_cyclic);
 			dest.writeBoolean(edge.is_terminal);
 			// write rod structure properties
 			dest.writeDouble(edge.rest_low);
 			dest.writeDouble(edge.rest_high);
 			// write muscle properties
 			dest.writeBoolean(edge.is_muscle);
 			if(edge.is_muscle) dest.writeDouble(edge.muscle_strength);
 		}
 	}
 	
 	@Override
 	public void deserialize(InputStream in) throws IOException {
 		super.deserialize(in);
 		DataInputStream source = new DataInputStream(in);
 		// parse nodes
 		GraphNode n;
 		int n_nodes = source.readInt();
 		for(int i=0; i<n_nodes; i++){
 			int id = source.readInt();
 			double mass = source.readDouble();
 			// read joint properties
 			boolean joint = source.readBoolean();
 			if(joint){
 				double rl = source.readDouble();
 				double rh = source.readDouble();
 				// read muscle properties
 				boolean muscle = source.readBoolean();
 				if(muscle){
 					double strength = source.readDouble();
 					n = new GraphNode(id, mass, rl, rh, strength);
 				} else{
 					n = new GraphNode(id, mass, rl, rh);
 				}
 			} else{
 				n = new GraphNode(id, mass);
 			}
 			// set root to the first node
 			if(this.root == null) this.root = n;
 		}
 		// parse edges
 		GraphEdge e;
 		int n_edges = source.readInt();
 		for(int i=0; i<n_edges; i++){
 			// read IDs of nodes this edge connects to
 			int from_id = source.readInt();
 			int to_id = source.readInt();
 			// read recursive depth
 			int rec = source.readInt();
 			boolean cyc = source.readBoolean();
 			boolean term = source.readBoolean();
 			// read structure properties
 			double rl = source.readDouble();
 			double rh = source.readDouble();
 			// read muscle properties
 			boolean muscle = source.readBoolean();
 			double strength = muscle ? source.readDouble() : 0.0;
 			// create edge
 			e = new GraphEdge(this.nodes.get(from_id), this.nodes.get(to_id), rec, cyc, term, rl, rh, muscle, strength);
 			this.edges.add(e);
 		}
 	}
 	
 	@Override
 	public String toString(){
 		StringBuilder str = new StringBuilder();
 		for(GraphNode n : this.nodes.values()){
 			str.append(n.local_uid);
 			str.append(":\n");
 			for(GraphEdge e : n.edges_out){
 				str.append("\t");
 				str.append(e.to.local_uid);
 				str.append("\trec ");
 				str.append(e.max_recurse);
 				str.append("\tlen ");
 				str.append(e.rest_low);
 				if(e.is_cyclic) str.append("\tcyc");
 				if(e.is_terminal) str.append("\tterm");
 				str.append("\n");
 			}
 		}
 		return str.toString();
 	}
 	
 	// TESTING: output empty gene to file
 	public static void main(String[] args){
 		Random r = new Random(12345);
 		System.out.println("-creating empty gene-");
 		DigraphGene g = new DigraphGene();
 		for(int i=0; i<100; i++) g.metaMutate(r);
 		for(int i=0; i<100; i++) g.mutate(r);
 		File dest = new File(System.getProperty("user.home") + File.separator + ".evolutionapp"  + File.separator + "digraphtest.gene");
 		try {
 			dest.createNewFile();
 			FileOutputStream out = new FileOutputStream(dest);
 			System.out.println("-writing gene-");
 			g.serialize(out);
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println("-done-");
 	}
 }

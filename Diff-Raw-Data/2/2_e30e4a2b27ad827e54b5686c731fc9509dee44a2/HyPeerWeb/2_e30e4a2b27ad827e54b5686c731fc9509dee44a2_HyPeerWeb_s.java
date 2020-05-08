 
 import java.util.Random;
 import java.util.TreeSet;
 
 /**
  * The Great HyPeerWeb Singleton
  * @author isaac
  */
 public class HyPeerWeb implements HyPeerWebInterface {
 	
 	private static HyPeerWeb instance;
 	TreeSet<Node> nodes;
 	Database db;
 	//Random number generator for getting random nodes
 	Random rand;
 	
 	/**
 	 * Private constructor for initializing the HyPeerWeb
 	 * @author isaac
 	 */
 	private HyPeerWeb() throws Exception{
 		nodes = new TreeSet<>();
 		db = Database.getInstance();
 		rand = new Random();
 	}
 	/**
 	 * Retrieve the HyPeerWeb singleton
 	 * @return the singleton
 	 * @author isaac
 	 */
 	public static HyPeerWeb getInstance() throws Exception{
 		if (instance != null)
 			return instance;
 		instance = new HyPeerWeb();
 		return instance;
 	}
 	
 	/**
 	 * Adds a new node to the HyPeerWeb
 	 * @return the new node
 	 * @author guy, brian, isaac
 	 */
 	public Node addNode(){
 		//Handle hypeerweb with no nodes
 		if (nodes.isEmpty()){
 			Node first = new Node(0, 0);
 			nodes.add(first);
 			db.addNode(first);
 			return first;
 		}
 		//Hypeerweb with one node
 		if (nodes.size() == 1){
 			Node sec = new Node(1, 1),
 				first = nodes.first();
 			first.setHeight(1);
 			first.setFold(sec);
 			sec.setFold(first);
 			nodes.add(sec);
 			return sec;
 		}
 		
 		//Find parent node
 		Node parent = findRandomNode().findInsertionNode();
 		
 		//Create child node and set its height
 		int height = parent.getHeight()+1,
 			webid = (int) (Math.pow(10, height-1) + parent.getWebId());
 		Node child = new Node(webid, height);
 		db.addNode(child);
 		parent.setHeight(height);
 		db.setHeight(parent.getWebId(), height);
 		nodes.add(child);
 		
 		//Set neighbours (Guy)
 		parent.hasChild(true);//sets parents hadChild value to true
 				Node[] list;
 				//makes the parent's ISNs the child's neighbors
 				list = parent.getInverseSurrogateNeighbors();
 				for (Node n:list){
 					child.addNeighbor(n);
 					db.addNeighbor(child.getWebId(), n.getWebId());
 				}
 				//adds a neighbor of parent as a surrogate neighbor of child if nieghbor is childless
 				//and makes child an isn of neighbor
 				list = parent.getNeighbors();
 				for(Node n:list){
 					if(!n.hasChild()){ 
 						child.addSurrogateNeighbor(n);
 						db.addSurrogateNeighbor(child.getWebId(), n.getWebId());
 						n.addInverseSurrogateNeighbor(child);
 					}
 				}
 				parent.addNeighbor(child);
 				child.addNeighbor(parent);
 				db.addNeighbor(parent.getWebId(), child.getWebId());
 		
 		//Set folds (Brian/Isaac)
 		
 		return child;
 	}
 	
 	/**
 	 * Picks a random node in the HyPeerWeb
 	 * @return the random node
 	 * @author john
 	 */
 	private Node findRandomNode(){
 		if (nodes.isEmpty())
 			return null;
 		
 		long index = rand.nextInt(Integer.MAX_VALUE);
 		index *= Integer.MAX_VALUE;
 		index += rand.nextInt(Integer.MAX_VALUE);
		return ((Node) nodes.toArray()[0]).findInsertionNode();
 	}
 
 	@Override
 	public Node[] getOrderedListOfNodes() {
 		return nodes.toArray(new Node[nodes.size()]);
 	}
 
 	@Override
 	public Node getNode(int webId){
 		Node n = nodes.floor(new Node(webId, 0));
 		if (n.getWebId() != webId)
 			return null;
 		return n;
 	}
 }

import java.net.InetAddress;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Random;
 
 class View {
 	private final ArrayList<Node> nodes;
 	private final Random rand = new Random();
 
 	public static void main(String[] args) {
 		ArrayList<Node> test = new ArrayList<Node>();
 		for (int i = 0; i < 10; i++) {
 			String a = "" + i;
 			test.add(i, new Node("test" + a, 10 - i));
 		}
 
 		View v1 = new View(test);
 		System.out.println("Alex");
 		ArrayList<Node> res = new ArrayList<Node>();
 		for (int i = 0; i < 10; i++) {
 			String a = "" + i;
 			res.add(i, new Node("test" + i, i));
 		}
 
 		View v2 = new View(res);
 
 		v1.printView();
 		System.out.println("***********");
 		v2.printView();
 
 		v1.mergeViews(v2, 5, 6, 7);
 
 		v1.printView();
 		// v1.moveDownOldest(2);
 		// v1.printView();
 	}
 
 	public View() {
 		this.nodes = new ArrayList<Node>();
 	}
 
 	public View(ArrayList<Node> nodes) {
 		this.nodes = nodes;
 	}
 
 	// return nodes array
 	public ArrayList<Node> getNodes() {
 		return this.nodes;
 	}
 
 	// get the buffer to be sent to other peers
 	public View getBuffer() {
 		View buf = new View();
 		try {
 			// add your own IP-address
 			buf.getNodes().add(
 					new Node(InetAddress.getLocalHost().getHostAddress(), 0));
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		this.permute();
 		this.moveDownOldest(Application.H);
 		buf.append(this.head(this.getNodes().size() / 2));
 		return buf;
 	}
 
 	// print the current list of nodes
 	public void printView() {
 		Iterator<Node> itr = this.nodes.iterator();
 		System.out.println("----------------------------");
 		while (itr.hasNext()) {
 			Node element = itr.next();
 			System.out.println(element.getAddress() + ' ' + element.getAge());
 		}
 		System.out.println("----------------------------");
 	}
 
 	// get a random node from the array
 	public Node selectNode() {
 		int position = rand.nextInt(this.nodes.size());
 		return this.nodes.get(position);
 	}
 
 	// randomize the nodes array
 	public void permute() {
 		Collections.shuffle(this.nodes);
 	}
 
 	// move down the oldest 'count' elements in the nodes array
 	public void moveDownOldest(int count) {
 		ArrayList<Node> oldest_nodes = new ArrayList<Node>();
 
 		// as long as there are still nodes in the nodes array and the
 		// oldest_nodes array is not yet filled
 		while ((oldest_nodes.size() < count) && (this.nodes.size() > 0)) {
 			int oldest_index = 0;
 			int oldest_age = 0;
 
 			// get index and age of the oldest node in the nodes array
 			for (int i = 0; i < this.nodes.size(); i++) {
 				if (this.nodes.get(i).getAge() > oldest_age) {
 					oldest_index = i;
 					oldest_age = this.nodes.get(i).getAge();
 				}
 			}
 
 			// put oldest node from nodes array at beginning of
 			// oldest_nodes array
 			oldest_nodes.add(0, this.nodes.get(oldest_index));
 			// remove oldest node from nodes array afterwards
 			this.nodes.remove(oldest_index);
 		}
 
 		// in the end, append the oldest nodes to the nodes array again
 		this.nodes.addAll(oldest_nodes);
 	}
 
 	// return a new View containing the first count elements of the
 	// nodes array of the current view
 	public View head(int count) {
 		ArrayList<Node> retNodes = new ArrayList<Node>();
 		Iterator<Node> itr = this.nodes.iterator();
 		int i = 0;
 		while (itr.hasNext() && (i < count)) {
 			Node element = itr.next();
 			retNodes.add(element);
 			i++;
 		}
 		View retView = new View(retNodes);
 
 		return retView;
 	}
 
 	// remove items with index from 0 to count
 	public synchronized void removeHead(int count) {
 
 		for (int i = 0; i < count; i++) {
 			if (i < this.nodes.size()) {
 				this.nodes.remove(i);
 			}
 		}
 	}
 
 	// append the nodes from another view to this view
 	public void append(View appendix) {
 		this.nodes.addAll(appendix.nodes);
 	}
 
 	// let all nodes age by one
 	public void age() {
 		Iterator<Node> itr = this.nodes.iterator();
 		while (itr.hasNext()) {
 			Node element = itr.next();
 			element.age();
 		}
 	}
 
 	// TODO: documentation
 	public synchronized void removeDuplicates() {
 		for (int i = 0; i < this.nodes.size(); i++) {
 			for (int j = i + 1; j < this.nodes.size(); j++) {
 				if (this.nodes.get(i).compareAddress(this.nodes.get(j))) {
 					/* remove the older node */
 					if (this.nodes.get(i).compareAge(this.nodes.get(j))) {
 						this.nodes.remove(i);
 
 						if (j > 0) {
 							j--;
 						}
 					} else {
 						this.nodes.remove(j);
 						if (i > 0) {
 							i--;
 						}
 
 					}
 				}
 			}
 		}
 	}
 
 	public synchronized void removeRandom(int count) {
 
 		while (count > 0) {
 			this.nodes.remove(rand.nextInt(this.nodes.size()));
 			count--;
 		}
 	}
 
 	public synchronized void mergeViews(View view, int h, int s, int c) {
 
 		this.append(view);
 		this.printView();
 		this.removeDuplicates();
 		this.printView();
 
 		this.moveDownOldest(Math.min(h, this.nodes.size() - c));
 		this.printView();
 		this.removeHead(Math.min(s, this.nodes.size() - c));
 		this.printView();
 		this.removeRandom(this.nodes.size() - c);
 
 	}
 }
 

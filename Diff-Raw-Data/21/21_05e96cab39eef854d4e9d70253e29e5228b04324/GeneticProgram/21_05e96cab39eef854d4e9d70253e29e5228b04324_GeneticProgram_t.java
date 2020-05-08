 package library;
 
 import java.util.Scanner;
 
 /**
  * This is a GeneticProgram. It is a forest, where the number of trees is set in the GPConfig.
  * 
  * @author Roma
  * 
  */
 
 public class GeneticProgram {
 
 	/**
 	 * The fitness of this program
 	 */
 	private double fitness;
 
 	/**
 	 * Depth of this program
 	 */
 	private final int[] depth;
 
 	/**
 	 * Size of this program
 	 */
 	private final int[] size;
 
 	/**
 	 * Return type of this program
 	 */
 	private final int[] returnType;
 
 	/**
 	 * The root node of the program tree
 	 */
 	private final Node[] root;
 
 	/** Number of parts **/
 	private final int numRoots;
 
 	/**
 	 * Create a new genetic program based on conf
 	 * 
 	 * @param conf
 	 *            the config to use
 	 */
 	public GeneticProgram(GPConfig conf) {
 		this(conf.getNumRoots());
 	}
 
 	/**
 	 * Create a new genetic program with numParts roots
 	 * 
 	 * @param numRoots
 	 *            number of root nodes
 	 */
 	public GeneticProgram(int numRoots) {
 		this.numRoots = numRoots;
 		fitness = 0.0;
 		returnType = new int[numRoots];
 		root = new Node[numRoots];
 		depth = new int[numRoots];
 		size = new int[numRoots];
 
 	}
 
 	/**
 	 * The evaluate method evaluates (executes) this program
 	 * 
 	 * @param out
 	 *            a pointer to the return value
 	 */
 	public void evaluate(ReturnData out[]) {
 		for (int i = 0; i < numRoots; i++) {
 			root[i].evaluate(out[i]);
 		}
 	}
 
 	/**
 	 * Sets the root of the tree to point to the new tree value. This does not delete the nodes
 	 * 
 	 * @param value
 	 *            the new root node
 	 * @param place
 	 *            the number root it is
 	 */
 	public void setRoot(Node value, int place) {
 		root[place] = value;
		value.setParent(null);
 		computeSizeAndDepth(place);
 	}
 
 	/**
 	 * Get the place root node
 	 * 
 	 * @param place
 	 *            index of root to get
 	 * @return the root at place
 	 */
 	public Node getRoot(int place) {
 		return root[place];
 	}
 
 	/**
 	 * Delete the current tree. Returns it to the node factory and sets the root to null
 	 * 
 	 * @param place
 	 *            which root to delete
 	 */
 	public void deleteTree(int place) {
 		deleteTree(root[place]);
 		root[place] = null;
 	}
 
 	/**
 	 * Recursively deletes the tree whose root is pointed to by theRoot. This returns all nodes in the subtree to the
 	 * NodeFactory
 	 * 
 	 * @param theRoot
 	 *            root node to delete from
 	 */
 	public void deleteTree(Node theRoot) {
 		NodeFactory.delete(theRoot);
 	}
 
 	/**
 	 * Get the depth of a tree
 	 * 
 	 * @param place
 	 *            which tree to get the depth of
 	 * @return the depth of the place tree
 	 */
 	public int getDepth(int place) {
 		return depth[place];
 	}
 
 	/**
 	 * Gets the size of the place tree
 	 * 
 	 * @param place
 	 *            which tree to get the size of
 	 * @return the size of the tree
 	 */
 	public int getSize(int place) {
 		return size[place];
 	}
 
 	/**
 	 * Computes the size and depth of each of the trees
 	 */
 	public void computeSizeAndDepth() {
 		for (int i = 0; i < numRoots; i++) {
 			size[i] = root[i].computeSize();
 			depth[i] = root[i].computeDepth(0);
 			root[i].computePositions(-1);
 		}
 	}
 
 	/**
 	 * Computes the size and depth of a given tree
 	 * 
 	 * @param place
 	 *            which tree to compute
 	 */
 	public void computeSizeAndDepth(int place) {
 		size[place] = root[place].computeSize();
 		depth[place] = root[place].computeDepth(0);
 		root[place].computePositions(-1);
 	}
 
 	/**
 	 * Gets a random node from the program tree
 	 * 
 	 * @param p
 	 *            which subtree to select from
 	 * @param config
 	 *            the config to use for random numbers
 	 * @return a random node in the subtree
 	 */
 	public Node getRandomNode(int p, GPConfig config) {
 		int node = Math.abs(config.randomNumGenerator.nextInt() % size[p]);
 		Node n= root[p].getNode(node);
 		if(n == null){
 			System.out.println("LAME");
 		}
 		return n;
 	}
 
 	/**
 	 * Get a random node from the tree that has type typeNum
 	 * 
 	 * @param typeNum
 	 *            The return type of the node
 	 * @param p
 	 *            the subtree to select from
 	 * @param config
 	 *            the config to use
 	 * @return a random node from the tree p with type typeNum or null if there is none
 	 * 
 	 */
 	public Node getRandomNode(int typeNum, int p, GPConfig config) {
 		int node = Math.abs(config.randomNumGenerator.nextInt() % size[p]);
 		Node n = root[p].getNode(node, typeNum);
 		return n;
 	}
 
 	/**
 	 * Prints the program to a string buffer. The program can be reconstructed from this string
 	 * 
 	 * @param s
 	 *            the StringBuffer to print to
 	 */
 	public void print(StringBuffer s) {
 		s.append(numRoots);
 		s.append(" ");
 		for (int i = 0; i < numRoots; i++) {
 			s.append("Program");
 			s.append(i);
 			s.append(" ");
 			root[i].print(s);
 			s.append(" | ");
 		}
 
 	}
 
 	/**
 	 * Set the return type for a particular root node
 	 * 
 	 * @param root
 	 *            the root whose return type will be se
 	 * @param type
 	 *            the return type of the root
 	 */
 	public void setReturnType(int root, int type) {
 		returnType[root] = type;
 	}
 
 	/**
 	 * Get the return type of a given root
 	 * 
 	 * @param root
 	 *            which root to get the return type for
 	 * @return the return type of the root
 	 */
 	public int getReturnType(int root) {
 		return returnType[root];
 	}
 
 	/**
 	 * Set the fitness. Assumes the fitness should be positive
 	 * 
 	 * @param f
 	 *            the fitness of the node
 	 */
 	public void setFitness(double f) {
 		fitness = f;
 	}
 
 	/**
 	 * Get the fitness of this program
 	 * 
 	 * @return this program's fitness
 	 */
 	public double getFitness() {
 		return fitness;
 	}
 
 	/**
 	 * The copy method will create a new genetic program which is a copy of the current program
 	 * 
 	 * @param config
 	 *            the config to create with
 	 * @return a clone of the current GeneticProgram
 	 */
 	public GeneticProgram copy(GPConfig config) {
 		GeneticProgram tmp = new GeneticProgram(numRoots);
 		tmp.setFitness(fitness);
 
 		for (int i = 0; i < numRoots; i++) {
 			tmp.setReturnType(i, returnType[i]);
 		}
 
 		for (int i = 0; i < numRoots; i++) {
 			Node rootTmp = root[i].copy(config);
 			tmp.setRoot(rootTmp, i);
 		}
 
 		tmp.computeSizeAndDepth();
 
 		return tmp;
 	}
 
 	/**
 	 * Read in a program from a string and make this that program
 	 * 
 	 * @param programString
 	 *            The string specifying the program
 	 * @param config
 	 *            the config used to build the program
 	 */
 	public void parseProgram(String programString, GPConfig config) {
 
 		try {
 			Scanner scan = new Scanner(programString);
 			Node[] tmpRoot = buildTree(scan, config);
 			for (int i = 0; i < numRoots; i++) {
 				deleteTree(i);
 				setRoot(tmpRoot[i], i);
 			}
 		} catch (Exception e) {
 			System.err.println("GeneticProgram::parseProgram\nError program: ");
 			System.err.println(programString);
 			e.printStackTrace();
 			throw new RuntimeException("Parsing failed");
 		}
 
 	}
 
 	private Node[] buildTree(Scanner scan, GPConfig config) {
 		Node[] tmpRoot = null;
 		Node arg = null;
 
 		String token = scan.next();
 
 		// token should be a number
 		int numParts = Integer.parseInt(token);
 		tmpRoot = new Node[numParts];
 
 		token = scan.next();
 		for (int i = 0; i < numParts; i++) {
 			int count = 0;
 
 			if (token.startsWith("Program")) {
 				token = scan.next();
 			}
 
 			if (token.equalsIgnoreCase("|")) {
 				// End of Program
 			} else if (token.equalsIgnoreCase("(")) // Start of function
 			{
 				token = scan.next();
 				tmpRoot[i] = config.funcSet.generateNodeByName(token, config);
 				token = scan.next();
 				while (!token.equals(")")) {
 					arg = buildTreeNode(scan, token, config);
 					((Function) (tmpRoot[i])).setArgN(count, arg);
 					count++;
 					token = scan.next();
 				}
 				token = scan.next();
 			} else // Must be terminal
 			{
 				tmpRoot[i] = config.termSet.generateNodeByName(token, config);
 				token = scan.next(); // read extra |
 			}
 		}
 
 		// If we get an error it will usually be because of a terminal
 		// or function being missing from the nodesets.
 
 		return tmpRoot;
 	}
 
 	private Node buildTreeNode(Scanner scan, String token, GPConfig config) {
 		Node tmpRoot = null;
 		Node arg = null;
 
 		int count = 0;
 
 		if (token.equalsIgnoreCase("|")) {
 			/* End of program */
 		} else if (token.equalsIgnoreCase("(")) // Start of function
 		{
 			token = scan.next();
 			tmpRoot = config.funcSet.generateNodeByName(token, config);
 			token = scan.next();
 			while (!token.equals(")")) {
 				arg = buildTreeNode(scan, token, config);
 				((Function) (tmpRoot)).setArgN(count, arg);
 				count++;
 				token = scan.next();
 			}
 		} else // Must be terminal
 		{
 			tmpRoot = config.termSet.generateNodeByName(token, config);
 		}
 
 		// If we get an error it will usually be because of a terminal
 		// or function being missing from the nodesets.
 
 		return tmpRoot;
 	}
 
 	public String toString() {
 		StringBuffer s = new StringBuffer();
 		this.print(s);
 		return s.toString();
 	}
 
 }

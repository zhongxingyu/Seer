 package ch.uzh.agglorecommender.clusterer.treesearch;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 import ch.uzh.agglorecommender.clusterer.TreeBuilder;
 import ch.uzh.agglorecommender.clusterer.treecomponent.ENodeType;
 import ch.uzh.agglorecommender.clusterer.treecomponent.IAttribute;
 import ch.uzh.agglorecommender.clusterer.treecomponent.INode;
 import ch.uzh.agglorecommender.clusterer.treecomponent.Node;
 import ch.uzh.agglorecommender.clusterer.treecomponent.TreeComponentFactory;
 
 import com.google.common.collect.ImmutableCollection;
 import com.google.common.collect.ImmutableSet;
 
 
 public class ClassitMaxCategoryUtilitySearcherTest {
 
 
 	//Node1, A1 = 4, A2 = 3
 	//Node2, A2 = 5, A3 = 5
 	@Test
 	public void classitTestI() throws InstantiationException, IllegalAccessException {
 
 		System.out.println(" ");
 		System.out
 				.println("----------------------Starting test 1..----------------------");
 
 		// Based on example in Google Docs
 		// https://docs.google.com/spreadsheet/ccc?key=0AnvRo1G6q1ffdEJLWjJiX2QtX2hza1l4WG5Sclp4WEE#gid=0
 
 		// Creating the attributes
 
 		IAttribute N1A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(4.0);
 		IAttribute N1A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(3.0);
 		IAttribute N2A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(5.0);
 		IAttribute N2A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(5.0);
 
 		// ClassitAttribute map of node 1
 		Map<INode, IAttribute> attMap1 = new HashMap<INode, IAttribute>();
 
 		// this node is an attribute of node 1 and node 2
 		INode sharedAttribute = new Node(ENodeType.Content, null, null);
 		
 		//public Node(ENodeType nodeType, String dataSetId, ImmutableMap<String, Boolean> useForClustering)
 
 		// add the corresponding attributes to the attribute map of node 1
 		attMap1.put(new Node(ENodeType.Content, null, null), N1A1);
 		attMap1.put(sharedAttribute, N1A2);
 
 		// create node 1
 		INode node1 = new Node(ENodeType.User, null, null);
 		node1.setRatingAttributes(attMap1);
 
 		// attribute map of node 2
 		Map<INode, IAttribute> attMap2 = new HashMap<INode, IAttribute>();
 
 		// add the corresponding attributes to the attribute map of node 2
 		attMap2.put(sharedAttribute, N2A2);
 		attMap2.put(new Node(ENodeType.Content, null, null), N2A3);
 
 		// create node 2
 		INode node2 = new Node(ENodeType.User, null, null);
 		node2.setRatingAttributes(attMap2);
 
 		
 
 		// add the two created user nodes to a set (set of open nodes)
 		Set<INode> openNodes = new IndexAwareSet<INode>();
 		openNodes.add(node1);
 		openNodes.add(node2);
 
 		// Print standard deviations
 		System.out.println("Node 1: " + node1.getNodeType()+ ", num atts: " + node1.getNumericalAttributesString());
 		System.out.println("Node 2: " + node2.getNodeType()+ ", num atts: " + node2.getNumericalAttributesString());
 
 		ArrayList<INode> nodesToUpdate = new ArrayList<INode>();
 		nodesToUpdate.add(node1);
 		nodesToUpdate.add(node2);
 
 		// Merge
 		IMaxCategoryUtilitySearcher utilityCalc = new ClassitMaxCategoryUtilitySearcher();
 		
 		IMergeResult merge = null;
 		TreeBuilder tr = null;
 		ImmutableCollection<INode> nodeSet = ImmutableSet.copyOf(nodesToUpdate);
 		IClusterSetIndexed<INode> leafNodes = new ClusterSetIndexed<INode>(nodeSet);
 		
 
 		//Instantiate TreeBuilder
 		try {
             //Class[] parameterTypes = {};
             Constructor cons[] = TreeBuilder.class.getDeclaredConstructors();
             cons[0].setAccessible(true);
             tr = (TreeBuilder)cons[0].newInstance(null);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
 		
 		
 		//Search best merge
 		try {
             Class[] parameterTypes = {IClusterSetIndexed.class, IMaxCategoryUtilitySearcher.class};
             
           //This causes an Exception.. Why?
             //private IMergeResult searchBestMergeResultIndexed(IClusterSetIndexed<INode> nodes, IMaxCategoryUtilitySearcher mcus)
             Method method = TreeBuilder.class.getDeclaredMethod("searchBestMergeResultIndexed", parameterTypes);
             
             method.setAccessible(true);
             merge = (IMergeResult) method.invoke(tr,leafNodes,utilityCalc);
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
         
 				
 		Double utility = merge.getCategoryUtility();
 		
 		System.out.println("Merge result: "+merge.toString());
 
 		// evaluate the category utility result
 		assertEquals("category utility", 1/(Math.sqrt(2))/3, utility, 0.0001);
 
 	}
 	
 	// Node 1: A1 = 1, A2 = 8
 	// Node 2: A1 = 4, A2 = 5
 	
 	
 	@Test
 	public void classitTestII() throws InstantiationException, IllegalAccessException {
 
 		System.out.println(" ");
 		System.out
 				.println("----------------------Starting test 2..----------------------");
 
 		// Creating the attributes
 
 		IAttribute N1A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(1.0);
 		IAttribute N1A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(8.0);
 		IAttribute N2A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(4.0);
 		IAttribute N2A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(5.0);
 
 		// ClassitAttribute map of node 1
 		Map<INode, IAttribute> attMap1 = new HashMap<INode, IAttribute>();
 
 		// this node is an attribute of node 1 and node 2
 		INode sharedAttribute1 = new Node(ENodeType.Content, null, null);
 		INode sharedAttribute2 = new Node(ENodeType.Content, null, null);
 		
 		//public Node(ENodeType nodeType, String dataSetId, ImmutableMap<String, Boolean> useForClustering)
 
 		// add the corresponding attributes to the attribute map of node 1
 		attMap1.put(sharedAttribute1, N1A1);
 		attMap1.put(sharedAttribute2, N1A2);
 
 		// create node 1
 		INode node1 = new Node(ENodeType.User, null, null);
 		node1.setRatingAttributes(attMap1);
 
 		// attribute map of node 2
 		Map<INode, IAttribute> attMap2 = new HashMap<INode, IAttribute>();
 
 		// add the corresponding attributes to the attribute map of node 2
 		attMap2.put(sharedAttribute1, N2A1);
 		attMap2.put(sharedAttribute2,N2A2);
 
 		// create node 2
 		INode node2 = new Node(ENodeType.User, null, null);
 		node2.setRatingAttributes(attMap2);
 
 		
 
 		// add the two created user nodes to a set (set of open nodes)
 		Set<INode> openNodes = new IndexAwareSet<INode>();
 		openNodes.add(node1);
 		openNodes.add(node2);
 
 		// Print standard deviations
 		System.out.println("Node 1: " + node1.getNodeType()+ ", num atts: " + node1.getNumericalAttributesString());
 		System.out.println("Node 2: " + node2.getNodeType()+ ", num atts: " + node2.getNumericalAttributesString());
 
 		ArrayList<INode> nodesToUpdate = new ArrayList<INode>();
 		nodesToUpdate.add(node1);
 		nodesToUpdate.add(node2);
 
 		// Merge
 		IMaxCategoryUtilitySearcher utilityCalc = new ClassitMaxCategoryUtilitySearcher();
 		
 		IMergeResult merge = null;
 		TreeBuilder tr = null;
 		ImmutableCollection<INode> nodeSet = ImmutableSet.copyOf(nodesToUpdate);
 		IClusterSetIndexed<INode> leafNodes = new ClusterSetIndexed<INode>(nodeSet);
 		
 
 		//Instantiate TreeBuilder
 		try {
             //Class[] parameterTypes = {};
             Constructor cons[] = TreeBuilder.class.getDeclaredConstructors();
             cons[0].setAccessible(true);
             tr = (TreeBuilder)cons[0].newInstance(null);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
 		
 		
 		//Search best merge
 		try {
             Class[] parameterTypes = {IClusterSetIndexed.class, IMaxCategoryUtilitySearcher.class};
             
           //This causes an Exception.. Why?
             //private IMergeResult searchBestMergeResultIndexed(IClusterSetIndexed<INode> nodes, IMaxCategoryUtilitySearcher mcus)
             Method method = TreeBuilder.class.getDeclaredMethod("searchBestMergeResultIndexed", parameterTypes);
             
             method.setAccessible(true);
             merge = (IMergeResult) method.invoke(tr,leafNodes,utilityCalc);
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
         
 				
 		Double utility = merge.getCategoryUtility();
 		
 		System.out.println("Merge result: "+merge.toString());
 
 		// evaluate the category utility result
 		assertEquals("category utility", ((1/(Math.sqrt(4.5)))+(1/(Math.sqrt(4.5))))/2, utility, 0.0001);
 
 	}
 
 //	// Node 1: A1 = 14, A2 = 7, A3 = 8
 //	// Node 2: A1 = 12, A2 = 7, A3 = 20
 	// (Example from J.H.Gennari et al."Models of incremental concept formation", p. 32, figure 6)
 	
 	@Test
 	public void classitTestIII() throws InstantiationException, IllegalAccessException {
 
 		System.out.println(" ");
 		System.out
 				.println("----------------------Starting test 3..----------------------");
 
 		// Creating the attributes
 
 		IAttribute N1A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(14.0);
 		IAttribute N1A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(7.0);
 		IAttribute N1A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(8.0);
 		IAttribute N2A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(2.0);
 		IAttribute N2A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(7.0);
 		IAttribute N2A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(20.0);
 
 		// ClassitAttribute map of node 1
 		Map<INode, IAttribute> attMap1 = new HashMap<INode, IAttribute>();
 
 		// this node is an attribute of node 1 and node 2
 		INode sharedAttribute1 = new Node(ENodeType.Content, null, null);
 		INode sharedAttribute2 = new Node(ENodeType.Content, null, null);
 		INode sharedAttribute3 = new Node(ENodeType.Content, null, null);
 		
 		//public Node(ENodeType nodeType, String dataSetId, ImmutableMap<String, Boolean> useForClustering)
 
 		// add the corresponding attributes to the attribute map of node 1
 		attMap1.put(sharedAttribute1, N1A1);
 		attMap1.put(sharedAttribute2, N1A2);
 		attMap1.put(sharedAttribute3, N1A3);
 
 		// create node 1
 		INode node1 = new Node(ENodeType.User, null, null);
 		node1.setRatingAttributes(attMap1);
 
 		// attribute map of node 2
 		Map<INode, IAttribute> attMap2 = new HashMap<INode, IAttribute>();
 
 		// add the corresponding attributes to the attribute map of node 2
 		attMap2.put(sharedAttribute1, N2A1);
 		attMap2.put(sharedAttribute2,N2A2);
 		attMap2.put(sharedAttribute3, N2A3);
 
 		// create node 2
 		INode node2 = new Node(ENodeType.User, null, null);
 		node2.setRatingAttributes(attMap2);
 
 		
 
 		// add the two created user nodes to a set (set of open nodes)
 		Set<INode> openNodes = new IndexAwareSet<INode>();
 		openNodes.add(node1);
 		openNodes.add(node2);
 
 		// Print standard deviations
 		System.out.println("Node 1: " + node1.getNodeType()+ ", num atts: " + node1.getNumericalAttributesString());
 		System.out.println("Node 2: " + node2.getNodeType()+ ", num atts: " + node2.getNumericalAttributesString());
 
 		ArrayList<INode> nodesToUpdate = new ArrayList<INode>();
 		nodesToUpdate.add(node1);
 		nodesToUpdate.add(node2);
 
 		// Merge
 		IMaxCategoryUtilitySearcher utilityCalc = new ClassitMaxCategoryUtilitySearcher();
 		
 		IMergeResult merge = null;
 		TreeBuilder tr = null;
 		ImmutableCollection<INode> nodeSet = ImmutableSet.copyOf(nodesToUpdate);
 		IClusterSetIndexed<INode> leafNodes = new ClusterSetIndexed<INode>(nodeSet);
 		
 
 		//Instantiate TreeBuilder
 		try {
             //Class[] parameterTypes = {};
             Constructor cons[] = TreeBuilder.class.getDeclaredConstructors();
             cons[0].setAccessible(true);
             tr = (TreeBuilder)cons[0].newInstance(null);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
 		
 		
 		//Search best merge
 		try {
             Class[] parameterTypes = {IClusterSetIndexed.class, IMaxCategoryUtilitySearcher.class};
             
           //This causes an Exception.. Why?
             //private IMergeResult searchBestMergeResultIndexed(IClusterSetIndexed<INode> nodes, IMaxCategoryUtilitySearcher mcus)
             Method method = TreeBuilder.class.getDeclaredMethod("searchBestMergeResultIndexed", parameterTypes);
             
             method.setAccessible(true);
             merge = (IMergeResult) method.invoke(tr,leafNodes,utilityCalc);
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
         
 				
 		Double utility = merge.getCategoryUtility();
 		
 		System.out.println("Merge result: "+merge.toString());
 
 		// evaluate the category utility result
 		assertEquals("category utility", ((1/(Math.sqrt(2)))+(1/(Math.sqrt(72))))/2, utility, 0.001);
 
 	}
 	
 
 //	@Test
 //	public void testSimpleClassitTree() {
 //
 //		System.out.println(" ");
 //		System.out
 //				.println("----------------------Starting test 2..----------------------");
 //		// Ratings are doubles
 
 		/*
 		 * Based on example in J.H.Gennari et al.
 		 * "Models of incremental concept formation", page 35, Figure 10
 		 */
 
 		@Test
 	public void classitTestIV() throws InstantiationException, IllegalAccessException {
 
 		System.out.println(" ");
 		System.out
 				.println("----------------------Starting test 4..----------------------");
 
 		// Creating the attributes
 		
 		//Node 1
 		IAttribute N1A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(13.0);
 		IAttribute N1A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(6.5);
 		IAttribute N1A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(7.5);
 		//Node2
 		IAttribute N2A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(12);
 		IAttribute N2A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(7);
 		IAttribute N2A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(20.0);
 		//Node 3
 		IAttribute N3A1= TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(28.0);
 		IAttribute N3A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(13.0);
 		IAttribute N3A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(19.0);
 		//Node 4
 		IAttribute N4A1 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(25.0);
 		IAttribute N4A2 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(15.0);
 		IAttribute N4A3 = TreeComponentFactory.getInstance()
 				.createNumericalLeafAttribute(24.0);
 
 		// ClassitAttribute map of node 1
 		Map<INode, IAttribute> attMap1 = new HashMap<INode, IAttribute>();
 
 		// this node is an attribute of node 1 and node 2
 		INode sharedAttribute1 = new Node(ENodeType.Content, null, null);
 		INode sharedAttribute2 = new Node(ENodeType.Content, null, null);
 		INode sharedAttribute3 = new Node(ENodeType.Content, null, null);
 		
 		//public Node(ENodeType nodeType, String dataSetId, ImmutableMap<String, Boolean> useForClustering)
 
 		// add the corresponding attributes to the attribute map of node 1
 		attMap1.put(sharedAttribute1, N1A1);
 		attMap1.put(sharedAttribute2, N1A2);
 		attMap1.put(sharedAttribute3, N1A3);
 
 		// create node 1
 		INode node1 = new Node(ENodeType.User, null, null);
 		node1.setRatingAttributes(attMap1);
 
 		// attribute map of node 2
 		Map<INode, IAttribute> attMap2 = new HashMap<INode, IAttribute>();
 
 		// add the corresponding attributes to the attribute map of node 2
 		attMap2.put(sharedAttribute1, N2A1);
 		attMap2.put(sharedAttribute2,N2A2);
 		attMap2.put(sharedAttribute3, N2A3);
 
 		// create node 2
 		INode node2 = new Node(ENodeType.User, null, null);
 		node2.setRatingAttributes(attMap2);
 		
 		// attribute map of node 3
 		Map<INode, IAttribute> attMap3 = new HashMap<INode, IAttribute>();
 
 		// add the corresponding attributes to the attribute map of node 3
 		attMap3.put(sharedAttribute1, N3A1);
 		attMap3.put(sharedAttribute2,N3A2);
 		attMap3.put(sharedAttribute3, N3A3);
 
 		// create node 3
 		INode node3 = new Node(ENodeType.User, null, null);
 		node3.setRatingAttributes(attMap2);
 		
 		// attribute map of node 4
        Map<INode, IAttribute> attMap4 = new HashMap<INode, IAttribute>();
 
        // add the corresponding attributes to the attribute map of node 3
        attMap4.put(sharedAttribute1, N4A1);
        attMap4.put(sharedAttribute2,N4A2);
        attMap4.put(sharedAttribute3, N4A3);
 
        // create node 4
        INode node4 = new Node(ENodeType.User, null, null);
        node4.setRatingAttributes(attMap4);
 
 		// add the two created user nodes to a set (set of open nodes)
 		Set<INode> openNodes = new IndexAwareSet<INode>();
 		openNodes.add(node1);
 		openNodes.add(node2);
 		openNodes.add(node3);
 		openNodes.add(node4);
 
 		ArrayList<INode> nodesToUpdate = new ArrayList<INode>();
 		nodesToUpdate.add(node1);
 		nodesToUpdate.add(node2);
 
 		// Merge
 		IMaxCategoryUtilitySearcher utilityCalc = new ClassitMaxCategoryUtilitySearcher();
 		
 		IMergeResult merge = null;
 		TreeBuilder tr = null;
 		ImmutableCollection<INode> nodeSet = ImmutableSet.copyOf(nodesToUpdate);
 		IClusterSetIndexed<INode> leafNodes = new ClusterSetIndexed<INode>(nodeSet);
		
 
 		//Instantiate TreeBuilder
 		try {
             //Class[] parameterTypes = {};
             Constructor cons[] = TreeBuilder.class.getDeclaredConstructors();
             cons[0].setAccessible(true);
             tr = (TreeBuilder)cons[0].newInstance(null);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
 		
 		//Search best merge
 		try {
             Class[] parameterTypes = {IClusterSetIndexed.class, IMaxCategoryUtilitySearcher.class};
             
           //This causes an Exception.. Why?
             //private IMergeResult searchBestMergeResultIndexed(IClusterSetIndexed<INode> nodes, IMaxCategoryUtilitySearcher mcus)
             Method method = TreeBuilder.class.getDeclaredMethod("searchBestMergeResultIndexed", parameterTypes);
             
             method.setAccessible(true);
             merge = (IMergeResult) method.invoke(tr,leafNodes,utilityCalc);
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             e.printStackTrace();
         }
         
				
 		Double utility = merge.getCategoryUtility();
		System.out.println("Merge Node 1: "+node1.getId()+", "+node1.getNumericalAttributesString());
		System.out.println("Merge Node 2: "+node2.getId()+", "+node2.getNumericalAttributesString());
 
 		System.out.println("Merge result: "+merge.toString());
 
 		double stDev1 = Math.sqrt(0.5);
 		double stDev2 = Math.sqrt(0.125);
		double stDev3 = Math.sqrt(78.125);
		
 		// evaluate the category utility result
 		assertEquals("category utility", ((1/stDev1)+(1/stDev2)+(1/stDev3))/3, utility, 0.000001);
 
 	}
 
 
 }

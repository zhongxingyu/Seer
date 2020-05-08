 package ch.uzh.agglorecommender.recommender;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.junit.Test;
 
 import ch.uzh.agglorecommender.client.ClusterResult;
 import ch.uzh.agglorecommender.clusterer.treecomponent.ClassitAttribute;
 import ch.uzh.agglorecommender.clusterer.treecomponent.ENodeType;
 import ch.uzh.agglorecommender.clusterer.treecomponent.IAttribute;
 import ch.uzh.agglorecommender.clusterer.treecomponent.INode;
 import ch.uzh.agglorecommender.clusterer.treecomponent.Node;
 
 /*
  * TODO: 
  * 	1. Sorted list is empty right now
  *  2. getSumOfRatings() returns sum of squared ratings
  */
 
 public class RankRecommendationTest {
  //public ArrayList<IAttribute> rankRecommendation(Map<INode, IAttribute> unsortedRecommendation,int direction, int limit)
 	@Test
 	public void rankRecommendationsTesI(){
 		System.out.println("Start recommendations ranking test I..");
 		// Create nodes
 		INode node1 = new Node(ENodeType.Content, null, null);
 		INode node2 = new Node(ENodeType.Content, null, null);
 		INode node3 = new Node(ENodeType.Content, null, null);
 		INode node4 = new Node(ENodeType.Content, null, null);
 		
 		// Create recommendations
 		IAttribute att1 = new ClassitAttribute(1, 5, 1);
 		IAttribute att2 = new ClassitAttribute(1, 2, 4);
 		IAttribute att3 = new ClassitAttribute(1, 1, 1);
 		IAttribute att4 = new ClassitAttribute(1, 4, 16);
 		
 		// Create map
 		Map<INode,IAttribute> unsortedRecommendations = new HashMap<INode, IAttribute>();
 		unsortedRecommendations.put(node1, att1);
 		unsortedRecommendations.put(node2, att2);
 		unsortedRecommendations.put(node3, att3);
 		unsortedRecommendations.put(node4,att4);
 		
 		// Sort recommendations
 		ClusterResult cr = new ClusterResult(null,null,null, null, UUID.randomUUID());
		Recommender ranker = new Recommender(cr, null);
 		SortedMap<INode,IAttribute> sortedRecommendations = new TreeMap<INode, IAttribute>();
 		sortedRecommendations = ranker.rankRecommendation(unsortedRecommendations, 1, 4);
 		ranker.rankRecommendation(unsortedRecommendations, 1, 4);
 		
 		// Check results
 		assertEquals("All nodes in map",unsortedRecommendations.size(),sortedRecommendations.size(),0.01);
 		
 		Iterator i = sortedRecommendations.entrySet().iterator();
 		double a = 0;
 		double b = 0;
 		Map.Entry pair1 = (Map.Entry) i.next();
 		a = ((IAttribute)pair1.getValue()).getSumOfRatings();
 		int count = 0;
 		while(i.hasNext()){
 			Map.Entry pair2 = (Map.Entry) i.next();
 			b = ((IAttribute)pair2.getValue()).getSumOfRatings();
 			assertTrue("Pair combiantion",a>b);
 			pair1 = pair2;
 			count++;
 		}
 		//Make sure all comparisons have been calculated
 		assertEquals("No of comparisons",3,count,0.01);
 	}
 }

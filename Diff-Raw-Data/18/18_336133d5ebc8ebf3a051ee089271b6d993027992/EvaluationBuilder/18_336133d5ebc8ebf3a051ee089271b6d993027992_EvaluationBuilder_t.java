 package ch.uzh.agglorecommender.recommender.evaluator;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import ch.uzh.agglorecommender.client.InitialNodesCreator;
 import ch.uzh.agglorecommender.clusterer.treecomponent.ClassitTreeComponentFactory;
 import ch.uzh.agglorecommender.clusterer.treecomponent.ENodeType;
 import ch.uzh.agglorecommender.clusterer.treecomponent.IAttribute;
 import ch.uzh.agglorecommender.clusterer.treecomponent.INode;
 import ch.uzh.agglorecommender.clusterer.treecomponent.Node;
 import ch.uzh.agglorecommender.recommender.RecommendationBuilder;
 
 import com.google.common.collect.ImmutableMap;
 
 /**
  * Instantiates a new evaluation builder
  * Evaluation builder offers evaluation methods (single,multiple) to calculate rmse
  * It also offers helper methods to pick test users randomly from test set
  * or create random users as input nodes for recommendations
  * 
  */
 public class EvaluationBuilder {
 
 	Random randomGenerator = new Random();
 	
 	/**
 	 * Evaluates a given training-set based recommendation with a test node from a test set
 	 * 
 	 * @param testnode this node is from the test set
 	 * @param rb this recommendation builder was initialized with the tree of the training set
 	 */
 	public Map<String,Double> evaluate(INode testNode, int testNodeID, RecommendationBuilder rb) throws NullPointerException {
 		
 		if(testNode != null){
 			// Get Predicitions & Real Values
 			Map<INode, IAttribute> predictedRatings = rb.runTestRecommendation(testNode,testNodeID);
 			
 			if(predictedRatings != null) {
 				
 				Map<String,Double> eval = new HashMap<String,Double>();
 				Set<INode> realRatingsKeys = testNode.getAttributeKeys();
 				
 				eval.put("RMSE",calculateRMSE(realRatingsKeys, testNode, predictedRatings));
 				eval.put("AME",calculateAME(realRatingsKeys, testNode, predictedRatings));
 				
 				return eval;
 				
 			}
 			else {
 				System.out.println("Recommendation Data is null");
 				return null;
 			}
 		}
 		else {
 			System.out.println("Input Node is null");
 			return null;
 		}
 	}
 	
 	/**
 	 * Runs multiple evaluations with different test nodes to eliminate variances
 	 * 
 	 * @param testNodes Set of nodes from test set
 	 * @param rb this recommendation builder was initialized with the tree of the training set
 	 * 
 	 */
 	public Map<String,Double> kFoldEvaluation(Map<INode,Integer> testNodes, RecommendationBuilder rb) throws NullPointerException{
 		
 		try {
 			
 			// Establish Maps
 			Map<String,Double> eval = null;
 			Map<String,Double> sumOfEval = new HashMap<String,Double>();
 			
 			// Calculate evaluation for all test nodes
 			for(INode testNode : testNodes.keySet()) {
 				eval = evaluate(testNode,testNodes.get(testNode),rb);
 				
 				// Add up results
 				for(String eKey : eval.keySet()){
 					Double eValue = eval.get(eKey);
 					Double sumOfEValue = sumOfEval.get(eKey);
 					sumOfEValue += eValue;
 					sumOfEval.put(eKey, sumOfEValue);
 				}
 			}
 			
 			// Take Mean of every value
 			for(String eKey : sumOfEval.keySet()){
 				Double meanEvalValue = sumOfEval.get(eKey) / testNodes.size();
 				sumOfEval.put(eKey, meanEvalValue);
 			}
 			
 			return eval;
 		}
 		catch (NullPointerException e) {
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * Calculate Evalution Value with Root Mean Squared Error (RMSE) Method
 	 * 
 	 * @param realRatingsKeys set of the real ratings
 	 * @param testNode the node that needs to be compared
 	 * @param predictedRatings set of the predicted ratings
 	 * 
 	 */
 	public double calculateRMSE (Set<INode> realRatingsKeys, INode testNode, Map<INode, IAttribute> predictedRatings){
 		
 		// Calculate Difference of predicted values to real values
 		double sumOfSquaredDifferences = 0;
 		for(INode ratingKey: realRatingsKeys) {
 			
 			// Calculate predicted rating - value could be null
 			double pRating = 0;
 			IAttribute pRatingAtt = predictedRatings.get(ratingKey);
 			if(pRatingAtt != null){
 				pRating = pRatingAtt.getSumOfRatings() / pRatingAtt.getSupport();
 			}
 			
 			// Calculate real rating
 			IAttribute rRatingAtt = testNode.getAttributeValue(ratingKey);
 			double rRating = rRatingAtt.getSumOfRatings() / rRatingAtt.getSupport();
 			
 			// Real Difference Squared
 			sumOfSquaredDifferences += Math.pow(rRating - pRating,2);
 		}
 		
 		// Division through number of Content Items
 		double mse = sumOfSquaredDifferences / realRatingsKeys.size();
 		
 		// Take root
 		double rmse = Math.sqrt(mse);
 	
 		return rmse;
 	}
 	
 	/**
 	 * Calculate Evalution Value as Absolute Min Error (AME)
 	 * 
 	 * @param realRatingsKeys set of the real ratings
 	 * @param testNode the node that needs to be compared
 	 * @param predictedRatings set of the predicted ratings
 	 * 
 	 */
 	private double calculateAME(Set<INode> realRatingsKeys, INode testNode,
 			Map<INode, IAttribute> predictedRatings) {
 		
 		// Calculate Difference of predicted values to real values
 		double sumOfSquaredDifferences = 0;
 		for(INode ratingKey: realRatingsKeys) {
 			
 			// Calculate predicted rating - value could be null
 			double pRating = 0;
 			IAttribute pRatingAtt = predictedRatings.get(ratingKey);
 			if(pRatingAtt != null){
 				pRating = pRatingAtt.getSumOfRatings() / pRatingAtt.getSupport();
 			}
 			
 			// Calculate real rating
 			IAttribute rRatingAtt = testNode.getAttributeValue(ratingKey);
 			double rRating = rRatingAtt.getSumOfRatings() / rRatingAtt.getSupport();
 			
 			// Absolute Difference Squared
 			sumOfSquaredDifferences += Math.pow(Math.abs(rRating - pRating),2);
 		}
 		
 		// Division through number of Content Items
 		double mse = sumOfSquaredDifferences / realRatingsKeys.size();
 		
 		// Take root
 		double ame = Math.sqrt(mse);
 	
 		return ame;
 	}
 
 	/**
 	 * Creates usable Map of test users for evaluation
 	 * Helper method for recommendations of type 1 (rmse test)
 	 * 
 	 * @param testSet reference on the test set
 	 */
 	public Map<INode, Integer> getTestUsers(InitialNodesCreator testSet){
 		
 		Map<INode,Integer> testUsers = new HashMap<INode,Integer>();
 		
 		ImmutableMap<Integer, INode> userLeaves = testSet.getUserLeaves();
 		for(Integer userLeaf : userLeaves.keySet()){
 			testUsers.put(userLeaves.get(userLeaf), userLeaf);
 		}
 		
 		return testUsers;
 	}
 	
 	/**
 	 * Creates a random user with random ratings 
 	 * Helper method for recommendations of type 2 (recommend unknown content)
 	 * 
 	 * @param testset reference on the test set
 	 */
 	public INode createRandomUser() {
 		
 		// Creating the content nodes
		INode A1n = new Node(ENodeType.Content, null, null,0);
		INode A2n = new Node(ENodeType.Content, null, null,0);
		INode A3n = new Node(ENodeType.Content, null, null,0);
		INode A4n = new Node(ENodeType.Content, null, null,0);
 		
 		A1n.setId(3);
 		A2n.setId(47);
 		A3n.setId(5);
 		A4n.setId(6);
 		
 		// Creating the attributes
 		IAttribute A1a = ClassitTreeComponentFactory.getInstance().createAttribute(randomGenerator.nextInt(10));
 		IAttribute A2a = ClassitTreeComponentFactory.getInstance().createAttribute(randomGenerator.nextInt(10));
 		IAttribute A3a = ClassitTreeComponentFactory.getInstance().createAttribute(randomGenerator.nextInt(10));
 		IAttribute A4a = ClassitTreeComponentFactory.getInstance().createAttribute(randomGenerator.nextInt(10));
 
 		// ClassitAttribute map
 		Map<INode, IAttribute> attMap = new HashMap<INode, IAttribute>();
 		attMap.put(A1n, A1a);
 		attMap.put(A2n, A2a);
 		attMap.put(A3n, A3a);
 		attMap.put(A4n, A4a);
 
 		// Create the user node
		INode randomUser = new Node(ENodeType.User, null, null,0);// FIXME: Wie/auf welchem Weg muss die CU berechnet werden?
 		randomUser.setAttributes(attMap);
 		randomUser.setId(1);
 		
 		return randomUser;
 	}
 }

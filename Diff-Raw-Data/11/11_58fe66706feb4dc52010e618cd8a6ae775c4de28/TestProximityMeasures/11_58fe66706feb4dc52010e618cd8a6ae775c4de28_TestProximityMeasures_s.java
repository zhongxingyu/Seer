 package nardiff.ordered;
 
import org.junit.Test;
 
import nardiff.MersenneTwisterFast;
 
 public class TestProximityMeasures {
 
 	@Test
 	public void test() {
		MersenneTwisterFast random = new MersenneTwisterFast(1);
 
 		int replicates = 1;
 		int totalPopulation = 300;
 		int stepsLimit = 3;
 		int flipsToCheck = 15;
 		int numInitialInfoPieces = 3;
 		int numTotalInfoPieces = 15;
 		int numAttachments = 5;
 		float probKnowledgeXmit = 0.3f;
 		float opinionLeaderChance = 0.1f;
 		float reorderProb = 1f;
 		float similarityProbMin = 0.2f;
 		float similarityProbMax = 0.8f;
 		float sigmoidCenter = 5f;
 
 		int modelsCreated = 0;
 
 		for (float opLeadBack = 0.1f; opLeadBack <= 0.9f; opLeadBack += 0.1f) {
 			for (float opFollowBack = 0.1f; opFollowBack <= 0.9f; opFollowBack += 0.1f) {
 				for (int rep = 0; rep < replicates; rep++) {
 					OrderedDiffusion model = new OrderedDiffusion();
 					model.totalPopulation = totalPopulation;
 					model.stepsLimit = stepsLimit;
 					model.flipsToCheck = flipsToCheck;
 					model.numInitialInfoPieces = numInitialInfoPieces;
 					model.numTotalInfoPieces = numTotalInfoPieces;
 					model.numAttachments = numAttachments;
 					model.probKnowledgeXmit = probKnowledgeXmit;
 					model.opinionLeaderChance = opinionLeaderChance;
 					model.opinionLeaderChanceBackward = opLeadBack;
 					model.opinionFollowerChanceBackward = opFollowBack;
 					model.reorderProb = reorderProb;
 					model.similarityProbMax = similarityProbMax;
 					model.similarityProbMin = similarityProbMin;
 					model.sigmoidCenter = sigmoidCenter;
 
 					model.modelID = modelsCreated++;
 					model.randomSeed = random.nextLong();
					model.random = new MersenneTwisterFast(model.randomSeed);
 
 					model.opinionLeaderAlpha = 0.8f;
 					model.opinionFollowerAlpha = 0.0f;
 					
 					model.populateTotalTruthVectors();
 					model.createGraph();
 					testMeasures(model);
 					System.out.println(modelsCreated);
 					
 				}
 			}
 		}
 	}
 	
 	private void testMeasures(OrderedDiffusion model) {
 		// Allow differences due to rounding and precision errors
 		float acceptableDifference = 0.00001f;
 		float acceptableNeighborDifference = 0.02f; // Accumulated rounding errors?
 
 		/*
 		 * proximityToLatentSubset
 		 */
 		org.junit.Assert.assertEquals(
 				model.proximityToLatentSubset(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 		
 		org.junit.Assert.assertEquals(
 				model.proximityToLatentSubset(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				model.proximityToLatentSubset(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		/*
 		 * proximityToLatent
 		 */
 		org.junit.Assert.assertEquals(
 				model.proximityToLatent(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 		
 		org.junit.Assert.assertEquals(
 				model.proximityToLatent(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				model.proximityToLatent(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.LATENT, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 		
 		/*
 		 * proximityToASubset
 		 */
 		
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToASubset(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToASubset(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToASubset(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 
 		/*
 		 * proximityToA
 		 */
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToA(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToA(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToA(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.A_FORWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		/*
 		 * proximityToBSubset
 		 */
 		
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToBSubset(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToBSubset(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToBSubset(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.INTERSECTION_WITH_SEEN), 
 				acceptableDifference);		
 
 
 		/*
 		 * proximityToB
 		 */
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToB(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToB(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.proximityToB(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.B_BACKWARD, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableDifference);		
 		
 		/*
 		 * proximityToNeighbors
 		 */
 		org.junit.Assert.assertEquals(
 				(Float) model.avgSimilarityToNeighbors(false), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.FOLLOWERS, 
 						OrderedDiffusion.OpinionType.NEIGHBORS, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 				acceptableNeighborDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.avgSimilarityToNeighbors(true), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.LEADERS, 
 						OrderedDiffusion.OpinionType.NEIGHBORS, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 						acceptableNeighborDifference);		
 
 		org.junit.Assert.assertEquals(
 				(Float) model.avgSimilarityToNeighbors(null), 
 				model.meanProximityBetween(OrderedDiffusion.PersonScope.ALL, 
 						OrderedDiffusion.OpinionType.NEIGHBORS, 
 						OrderedDiffusion.InfoScope.ALL_PIECES), 
 						acceptableNeighborDifference);		
 		
 
 		/*
 		 * proximityToNeighborsSubset was never implemented, IIRC.
 		 */
 
 		
 		
 		
 		
 		
 		
 	}
 	
 
 }

 package sdcl.ics.uci.edu.lda.modelAggregator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.ChangeSet;
 import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.LightweightTopicModel;
 
 /**
  * Associates each ChangeSet with the topics that best characterize all classes
  * impacted by the ChangeSet
  * 
  * @author nlopezgi
  * 
  */
 public class ChangeSetToTopicMapper {
 
 	/**
 	 * Associates each ChangeSet with the topics that best characterize all
 	 * classes impacted by the ChangeSet. The model must already have the
 	 * ChangeSets loaded as well as the aggregated document matrices. This method
 	 * actually creates two small vectors for each change set. The vectors store
 	 * the topics (ChangeSet.topics) and the probabilities
 	 * (ChangeSet.topicProbabilities) for all topics according to the classes
 	 * included in the change. The topic vector has indexes corresponding to the
 	 * topics in the lightweight model. The probability vector contains the sum of
 	 * all probabilities for each topic for all classes impacted by the change.
 	 * 
 	 * 
 	 * @param model
 	 */
 	public static void associateChangeSetsToTopics(LightweightTopicModel model) {
 		int numTopics = model.topicToClasses.length;
 		for (int i = 0; i < model.changeSets.length; i++) {
 			ChangeSet oneChange = model.changeSets[i];
 
 			double[] topicProbabilities = new double[numTopics];
 			for (int j = 0; j < oneChange.changes.length; j++) {
				for (int z = 0; z < numTopics; z++) {
					topicProbabilities[z] += model.topicToClasses[z][oneChange.changes[j].id];
 				}
 			}
 			int topicsOrderedByProbability[] = new int[numTopics];
 			double probabilitiesOrdered[] = new double[numTopics];
 			int resultIndex = 0;
 			int orderedTopics = 0;
 			double maxProb = 0;
 			int maxProbIndex = -1;
 			while (orderedTopics < numTopics) {
 				for (int z = 0; z < numTopics; z++) {
 					if (maxProb < topicProbabilities[z]) {
 						maxProb = topicProbabilities[z];
 						maxProbIndex = z;
 					}
 				}
 				if (maxProbIndex != -1) {
 					topicsOrderedByProbability[resultIndex] = maxProbIndex;
 					probabilitiesOrdered[resultIndex] = maxProb;
 					topicProbabilities[maxProbIndex] = 0;
 					resultIndex++;
 					orderedTopics++;
 				}
 			}
 			oneChange.topics = topicsOrderedByProbability;
 			oneChange.topicProbabilities = probabilitiesOrdered;
 		}
 	}
 }

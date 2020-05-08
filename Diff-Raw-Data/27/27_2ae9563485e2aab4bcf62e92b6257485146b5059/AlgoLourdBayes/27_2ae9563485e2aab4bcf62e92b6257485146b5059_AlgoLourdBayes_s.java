 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.bson.types.ObjectId;
 
 
 
 public class AlgoLourdBayes extends AlgoLourd {
 
 	
 	public void maj() {
 		//on met a jour les compteurs de feedback positif et negatifs
 		System.out.print("maj des compteurs de feedback...");
 		ArrayList<DataFeedBack> toBeUpdated = Interprete.getFeedback();
 		System.out.print("("+ toBeUpdated.size() +")");
 		HashSet<ObjectId> feedbackers = new HashSet<ObjectId>();
 		
 		for (DataFeedBack feedback : toBeUpdated) {
 			ObjectId recoGiver = feedback.recoGiver();
 			ObjectId recoReceiver = feedback.recoReceiver();
 			feedbackers.add(recoReceiver);
 			Interprete.modifyFeedback(recoGiver, recoReceiver, feedback.clicked()); //true means that the feedback was positive
 			}
 		System.out.println("[done]");
 		//on va ensuite supprimer des listes damis les recommandeurs trop mauvais que l'on va remplacer par un user au hasard
 		System.out.print("maj des listes d'ami...");
 		ArrayList<ObjectId> userList = Interprete.getUserList();
 		for (ObjectId guy : feedbackers) {
 			DataUserNode user = Interprete.db2DataUserNodeHard(guy);
 			
 			boolean hasChanged= false;
 			ArrayList<DataUserRelation> newFriends = new ArrayList<DataUserRelation>(user.getFriends().size());
 			for (DataUserRelation relation: user.getFriends()) {
 				if (relation.posFeedback - relation.negFeedback < -3) {
 					hasChanged = true;
 
 					//We add a new friend relation created from a random user.
 					//We don't need to calculate the proba as it will be done later.
 					int var = (int) Math.floor(Math.random()* userList.size());
 					newFriends.add(new DataUserRelation(Interprete.db2DataUserNodeSimple(userList.get(var))));
 				} else {
 					newFriends.add(relation);
 				}
 			}
 			user.setFriends(newFriends);
 			if (hasChanged) {Interprete.DataUserNode2db(user);}
 		}
 		System.out.println("[done]");
 		
 		//on va ensuite recalculer les crossProbabiltes
 		System.out.print("maj des crossProba...");
 		for (ObjectId userId: userList) {
 			DataUserNode user = Interprete.db2DataUserNodeHard(userId);
 			user.updateProbabilities();
 			for (DataUserRelation relation : user.getFriends()) {
 				Interprete.setCrossProbability(userId, relation.friend.getId(), relation.crossProbability);
 			}
 		}
 		System.out.println("[done]");
 		
 		
 	}
 	
 }

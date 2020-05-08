 package com.warriorwebpros.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.warriorwebpros.model.Actor;
 
 public class ActorSortingService {
 	
 	private Random random;
 
 	/**
 	 * Set the Order on all Actor objects in the list based on their initiative scores.
 	 * @param actorList
 	 * @return
 	 */
 	public List<Actor> setOrderByInitiative(List<Actor> actorList) {
 		//Iterator starts with last in order.
 		int i_order = actorList.size() - 1;
 		while(true){
 			Actor sortedActor = actorList.get(i_order);
 			actorList.remove(i_order);
 			for(Actor actor : actorList){
 				if(sortedActor.getInitiative() < actor.getInitiative()){
 					sortedActor.setOrder(sortedActor.getOrder() + 1);
 				}
 			}
 			//Set the actor back in the same place in the list we got it.
 			actorList.add(i_order, sortedActor);
 			i_order -= 1;//update iterator
 			if(i_order == -1){
 				break;
 			}
 		}
 		return actorList;
 	}
 
 	/**
 	 * This class iterates through all of the Actors and mutates the initiatives
 	 * to be unique.  
 	 * This is a required step before calling setOrderByInitiative
 	 * to receive their order.
 	 * @param List<Actor> actorList
 	 * @return List<Actor> actorList
 	 */
 	public List<Actor> breakTiedInitiatives(List<Actor> actorList) {
 		for(Actor actor1 : actorList) {
 			int index1 = actorList.indexOf(actor1);
			//actorList.remove(actor1);//Don't test against the same object
 			for(Actor actor2 : actorList){
 				int index2 = actorList.indexOf(actor2);
 				if(index1 == index2){
 					continue;
 				}
				//actorList.remove(actor2);
 				List<Actor> uniqueInitiatives;
 				if(isSameInitiative(actor1, actor2)) {
 					uniqueInitiatives = breakInitiativeTie(actor1, actor2);
 					actor1 = uniqueInitiatives.get(0);
 					actor2 = uniqueInitiatives.get(1);
 				} else {
 					continue;
 				}
				//Add them back in reverse order we removed them.
 				actorList.set(index1,actor1);
 				actorList.set(index2,actor2);
 			}
 		}
 		return actorList;
 	}
 	
 	public List<Actor> breakInitiativeTie(Actor a1, Actor a2){
 		List<Actor> returnList = new ArrayList<Actor>();
 		if(random.nextInt(2)+1 == 1){
 			a1.setInitiative(a1.getInitiative()+1);
 		} else {
 			a2.setInitiative(a2.getInitiative()+1);
 		}
 		returnList.add(0,a1);
 		returnList.add(1,a2);
 		return returnList;
 	}
 	
 	public Boolean isSameInitiative(Actor a1, Actor a2){
 		Boolean b = false;
 		if(a1.getInitiative() == a2.getInitiative()){
 			b = true;
 		}
 		return b;
 	}
 	
 	/**
 	 * Returns the higher of two passed in actor's initiative scores.
 	 * @return
 	 */
 	public int getHigherInitiative(int a, int b){
 		int returnValue = 0;
 		if(a > b) {
 			returnValue = a;
 		} else {
 			returnValue = b;
 		}
 		return returnValue;
 	}
 	
 
 	public void setRandom(Random random) {
 		this.random = random;
 	}
 }

 package edu.umw.cpsc.collegesim;
 import java.util.ArrayList;
 
 import sim.engine.*;
 import sim.util.*;
 import ec.util.*;
 import sim.field.network.*;
 
 public class Decay implements Steppable{
 	
 	private int numTimes = 1;
 	private static final int MAX_ITER = 3;
 	public static final int NUM_STEPS_TO_DECAY = 3;
 	
 	Decay( ){
         
 	}
 	
 	//Problem: Sometimes the code checks the same person twice
 	//Ex: It will print "Person 1 last met person 2 a number of 2 steps ago." Immediately followed by
 	//"Person 1 last met person 2 a number of 3 steps ago." So it goes through the loop checking other
 	//connections twice with the same person. Note that this is not related to the fix where we made
 	//it so that when Person 0 checks the relationship with person 1, person 1 does not also check
 	//that relationship.
 	
 	//Problem: The number of steps since meeting does not reset after two people meet if the decay
 	//class is stepped before those students are stepped.
 	//Ex: Person 2 met person 1 a number of 2 steps ago. Decay is stepped, then in the same iteration
 	//person 2 meets person 1. The next time decay is stepped, it still says person 2 met person 1 a
 	//number of 3 steps ago, even though it was now only 1 step ago.
 	
 	public void step(SimState state){
 		//People is a bag of all people
 		Bag people = Sim.instance( ).people.getAllNodes( );
 		//We're going to print out the people for sanity check
 		System.out.println("The contents of the bag: ");
 		for(int m=0; m<people.size( ); m++){
 			Person mP = (Person) people.get(m);
 			System.out.println(mP.getID( ));
 		}
 		//for each of the people
 		for(int i=0; i<people.size( ); i++){
 			//pick one
 			Person person = (Person) people.get(i);
 System.out.println("Looking at person " + person.getID( ));
 			//get a bag containing all of the edges between others and this person
 			Bag b = Sim.instance( ).lastMet.getEdgesIn(person);
 			//for each of the edges
 			for(int j=0; j<b.size( ); j++){
 				//pick one
 				Edge edge = (Edge)b.get(j);
 				//determine who the other person on that edge is
 				Person otherPerson = (Person) edge.getOtherNode(person);
 				if(otherPerson.getID( ) > person.getID( )){
 					//if the other person has a higher index, meaning we have not yet looked at them
 System.out.println("Looking at when they last met person " + otherPerson.getID( ));
 					//obtain the number of steps since the two last met
					int steps = (int) edge.getInfo( );
 					//increment this number
 					steps++;
 System.out.println("This was " + steps + " steps ago.");
 					//if the steps is past the decay point
 					if(steps > NUM_STEPS_TO_DECAY){
 System.out.println("Steps causes decay");
 						//remove the edge saying when they last met
 						Sim.instance( ).lastMet.removeEdge(edge);
 						//get a bag of all the friendships of this person
 						Bag friendships = Sim.instance( ).people.getEdgesIn(person);
 						//for each friendship
 						for(int m=0; m<friendships.size( ); m++){
 							//pick one
 							Edge edgeTest = (Edge)friendships.get(m);
 							//obtain the person on the other end
 							Person test = (Person) edgeTest.getOtherNode(person);
 							//when this person is the other friend in question
 							if(test.equals(otherPerson)){
 System.out.println("We're removing the friendship between " + person.getID( ) + " and " + test.getID( ));
 								//remove this edge
 								Sim.instance( ).people.removeEdge(edgeTest);
 							}
 						}
 						//if we're not past the decay point
 					}else{
 System.out.println("Steps does not cause decay.");
 						//just make the edge hold the new number of steps
 						Sim.instance( ).lastMet.updateEdge(edge, person, otherPerson, steps);
 					}
 				}else{
 					System.out.println("Skipping looking at person " + otherPerson.getID( ));
 				}
 			}
 System.out.println("The friends for " + person.getID( ) + " are:");
 Bag testBag = Sim.instance( ).people.getEdgesIn(person);
 for(int k=0; k<testBag.size( ); k++){
 	Edge friendEdge = (Edge)testBag.get(k);
 	Person friend = (Person)friendEdge.getOtherNode(person);
 	System.out.println(friend.getID( ));
 }
 		}
 		if(numTimes >= MAX_ITER){
 			System.out.println(this);
 		}else{
 			Sim.instance( ).schedule.scheduleOnceIn(1, this);
 		}
 		numTimes++;
 	}
 	
     
     public boolean met(Person other){
     	Bag b = Sim.instance( ).lastMet.getEdgesIn(this);
     	for(int i=0; i<b.size( ); i++){
     		Person otherSideOfThisEdge = (Person) ((Edge)b.get(i)).getOtherNode(this);
     		if(other == otherSideOfThisEdge){
     			return true;
     		}
     	}
     	return false;
     }
 }
 

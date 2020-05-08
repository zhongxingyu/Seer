 package edu.umw.cpsc.collegesim;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import sim.engine.*;
 import sim.util.*;
 import ec.util.*;
 import sim.field.network.*;
 
 public class Person implements Steppable{
 
     public enum Race { WHITE, MINORITY };
     public enum Gender { MALE, FEMALE };
 	public static final int PROBABILITY_WHITE = 80;
 	public static final int PROBABILITY_FEMALE = 50;
 	
 	public static final double RACE_WEIGHT = 3;
 	public static final double GEN_WEIGHT = 1;
 	public static final double CONST_WEIGHT = 1;
 	public static final double INDEP_WEIGHT = 1.5;
 	public static final double DEP_WEIGHT = 2.5;
 	
 	public static final double FRIENDSHIP_COEFFICIENT = .7;
 	public static final double FRIENDSHIP_INTERCEPT = .2;
 	
 	//The number of people to meet from groups
 	public static final int NUM_TO_MEET_GROUP = 2;
 	public static final int NUM_TO_MEET_POP = 2;
 
 	private int ID;
 	private MersenneTwisterFast generator = Sim.instance( ).random;
 	private int numTimes = 1;
 	private static final int MAX_ITER = 3;
 	private int decayThreshold = 10;
 	
 	private Race race;
 	private Gender gender;
     
     //from maddie's code
     private int willingnessToMakeFriends;
     private ArrayList<Group> groups;
 	
     int NUM_CONSTANT_ATTRIBUTES = 10;
 	//constant attributes, like place of birth, etc.
	private ArrayList<Boolean> attributesK1
 	private ArrayList<Boolean> attributesK1			//Constant attributes
 		= new ArrayList<Boolean>(Collections.nCopies(NUM_CONSTANT_ATTRIBUTES, false));
 	
     int NUM_INDEPENDENT_ATTRIBUTES = 20;
     int INDEPENDENT_ATTRIBUTE_POOL = 100;
 	//independent attributes, which can change but do not affect each other
 	private ArrayList<Double> attributesK2			//Independent attributes
 		= new ArrayList<Double>(Collections.nCopies(INDEPENDENT_ATTRIBUTE_POOL, 0.0));
 	//the following is the interval inside which two attributes are considered "the same"
 	//so for attribute 14, if this has 0.5 and other has 0.3, they have this attribute in
 	//common, but if other had 0.2, they would not have this attribute in common
 	double INDEPENDENT_INTERVAL = 0.2;
 	
     int NUM_DEPENDENT_ATTRIBUTES = 20;
     int DEPENDENT_ATTRIBUTE_POOL = 100;
 	//dependent attributes, which can change but you only have 1 unit to split among them
 	//in other words, if one increases, then another decreases
     private ArrayList<Double> attributesK3			//Dependent attributes
 		= new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
     //the following is the interval inside which two attributes are considered "the same"
   	//so for attribute 14, if this has 0.5 and other has 0.2, they have this attribute in
   	//common, but if other had 0.1, they would not have this attribute in common
   	double DEPENDENT_INTERVAL = 0.3;
 
   	//A list that will house when this person last met all other people
   	private ArrayList<Integer> lastMet
   		= new ArrayList<Integer>(Collections.nCopies(Sim.instance( ).getNumPeople( ), -1));
 
   	public void setMet(int index, int val){
   		lastMet.set(index, val);
   	}
   	public int getMet(int index){
   		return lastMet.get(index);
   	}
   	public void resetLastMet(int index){
   		lastMet.set(index, 0);
   	}
   	private void incMet( ){
   		//for the entire last met array
   		for(int i=0; i<lastMet.size( ); i++){
   			//if the value is -1, the two have never met
   			//so we only do something if it's different from -1
   			int prev = lastMet.get(i);
   			if(prev != -1){
   				lastMet.set(i, prev+1);
   			}
   		}
   	}
   	
   	private void decay( ){
   		for(int i=0; i<lastMet.size( ); i++){
   			Edge toRemoveIn = null;
   			Edge toRemoveOut = null;
   			int val = lastMet.get(i);
   			//if the people last met longer than the threshold ago
   			if(val > decayThreshold){
   				//Get a bag of all the edges into this person
   				Bag bIn = Sim.instance( ).people.getEdgesIn(ID);
   				//for each of these edges
   				for(int j=0; j<bIn.size( ); j++){
   					//look for the person whose ID matches the ID of the person we want to decay
   					Edge edgeIn = (Edge)bIn.get(j);
   					Person otherPerson = (Person) edgeIn.getOtherNode(this);
   					int otherID = otherPerson.getID( );
   					if(otherID == i){
   						//when we find the person, make their edge the one we want to remove
   						toRemoveIn = edgeIn;
   						j = bIn.size( );
   					}
   				}
   				//Do the same with the other edges
   				Bag bOut = Sim.instance( ).people.getEdgesOut(ID);
   				//for each of these edges
   				for(int j=0; j<bOut.size( ); j++){
   					//look for the person whose ID matches the ID of the person we want to decay
   					Edge edgeOut = (Edge)bOut.get(j);
   					Person otherPerson = (Person) edgeOut.getOtherNode(this);
   					int otherID = otherPerson.getID( );
   					if(otherID == i){
   						//when we find the person, make their edge the one we want to remove
   						toRemoveOut = edgeOut;
   						j = bOut.size( );
   					}
   				}
   				Sim.instance( ).people.removeEdge(toRemoveIn);
   				Sim.instance( ).people.removeEdge(toRemoveOut);
   				//reset the value to -1, as though they've never met
   				lastMet.set(i,-1);
   			}
   		}
   	}
   	
     private void assignAttribute(int numAttr, int poolSize, ArrayList<Double> attr){
     	boolean okay;
     	for(int i=0; i<numAttr; i++){
     		//pick an attribute to change
     		int index = generator.nextInt(poolSize);
     		okay = false;
     		//while we have not chosen an appropriate index
     		while(!okay){
     			//if the attribute is zero, it has not already been changed, so we use it
     			if(attr.get(index) == 0.0){
     				okay = true;
     			//otherwise, we have to pick a new attribute
     			}else{
     				index = generator.nextInt(poolSize);
     			}
     		}
     		//pick a degree to which the person will have this attribute
    			//we generate a number between 0 and 1, including 1 but not including 0
    			double degree = generator.nextDouble(false, true);
    			//then we set the attribute at the chosen index to be the generated degree
    			attr.set(index, degree);
     	}
     }
     
     private boolean assignRaceGender(int probability){
     	int gen = generator.nextInt(100);
     	if(gen <= probability){
     		return true;
     	}else{
     		return false;
     	}
     }
     
 	Person(int ID){
         this.ID = ID;
 		groups = new ArrayList<Group>( );
 		
 		//Assigning constant attributes
 		for(int i=0; i<NUM_CONSTANT_ATTRIBUTES; i++){
 			boolean rand = generator.nextBoolean( );
 			attributesK1.set(i, rand);
 		}
 		//Assigning independent attributes
 		assignAttribute(NUM_INDEPENDENT_ATTRIBUTES, INDEPENDENT_ATTRIBUTE_POOL, attributesK2);
 		//Assigning dependent attributes
 		assignAttribute(NUM_DEPENDENT_ATTRIBUTES, DEPENDENT_ATTRIBUTE_POOL, attributesK3);
 		
 		//Assign a race		
 		boolean white = assignRaceGender(PROBABILITY_WHITE);
 		if(white){
 			race = Race.WHITE;
 		}else{
 			race = Race.MINORITY;
 		}
 		//Assign a gender
 		boolean female = assignRaceGender(PROBABILITY_FEMALE);
 		if(female){
 			gender = Gender.FEMALE;
 		}else{
 			gender = Gender.MALE;
 		}
 		willingnessToMakeFriends = generator.nextInt(10)+1;
 	}
 	
 	//What to do when meeting a new person
 	public void meet(Person personToMeet){
 		double similar;
 		boolean friends = false;
 		int personToMeetID = personToMeet.getID( );
 System.out.println("Person " + ID + " is meeting person " + personToMeetID);
 		//Calculate their similarity rating, and then see if they should become friends
 		similar = similarityTo(personToMeet);
 		friends = areFriends(similar);
 		//if they become friends, add their edge to the network
 		//and reset when they met
 		if(friends){
 				Sim.instance( ).people.addEdge(this, personToMeet, 1);
 				resetLastMet(personToMeetID);
 				personToMeet.resetLastMet(ID);
 		}
 	}
 	
 	//A function which "tickles" the relationship between "this" and the person whose ID is tickleID
 	public void tickle(Person person){
 		//reset when the two last encountered each other
 		int tickleID = person.getID( );
 		resetLastMet(tickleID);
 		person.resetLastMet(ID);
 	}
 	
 	public void encounter(int number, Bag pool){
 		for(int i=0; i<number; i++){
 			Person personToMeet;
 			do{
 				personToMeet = (Person) pool.get(generator.nextInt(Sim.instance( ).getNumPeople( )));
 			}while(personToMeet == this);
 			if(friendsWith(personToMeet)){
 				tickle(personToMeet);
 			}else{
 				meet(personToMeet);
 			}
 		}
 	}
 	
 	public void step(SimState state){
 		//Need to somehow get a bag of all the people in the group we want to use for encountering
 		Bag bag = null;
 		//encounter(NUM_TO_MEET_GROUP, bag);
 		//Get a bag of all the people and then encounter some number of those people
 		Bag peopleBag = Sim.instance( ).people.getAllNodes( );
 		encounter(NUM_TO_MEET_POP, peopleBag);
 
 
 //NOTE: Decay only matters if the people are friends- you can't decay a friendship that
 //doesn't exist.
 //So, the time they last met only matters if they are friends already or if they
 //become friends this turn
 //If they aren't already friends and if they don't become friends this turn, then -1 for last met is
 //fine
 //(unless we implement something where if two people meet enough times, they become friends by brute
 //force)
 		
 		//Now, we want to increment the steps since this person has met everyone they are friends with
 		incMet( );
 		//Note that this could cause problems if, for instance, person 1 steps before person 2
 		//and on person 2's turn they meet person 1
 		//so person 2 and person 1 met each other 0 steps ago
 		//but then person 2 finishes and person 2 met person 1 1 step ago
 		//while person 1 met person 2 0 steps ago
 		//I think this may be inconsequential really, and if person 2 surpasses the decay threshold
 		//we'll just remove the friendship anyway even though person 1 hasn't technically surpassed that
 		
 		//Now we want to see if any of the friendships have decayed
 		decay( );
 		
 		//If we've done the maximum number of iterations, then stop; otherwise, keep stepping
 		if(numTimes >= MAX_ITER){
 			System.out.println(this);
 		}else{
 			Sim.instance( ).schedule.scheduleOnceIn(1, this);
 		}
 		numTimes++;
 	}
 
     public boolean friendsWith(Person other) {
     	Bag b = Sim.instance( ).people.getEdgesIn(this);
         for (int i=0; i<b.size(); i++) {
             Person otherSideOfThisEdge = 
                 (Person) ((Edge)b.get(i)).getOtherNode(this);
             if (other == otherSideOfThisEdge) {
                 return true;
             }
         }
         return false;
     }
     
     public boolean met(Person other){
     	int otherID = other.getID( );
     	if(lastMet.get(otherID) == -1){
     		return false;
     	}else{
     		return true;
     	}
     }
 
     public String toString() {
         String retval = "Person " + ID + " (friends with ";
         Bag b = Sim.instance().people.getEdgesIn(this);
         for (int i=0; i<b.size(); i++) {
             retval += ((Person)(((Edge)b.get(i)).getOtherNode(this))).ID;
             if (i == b.size()-1) {
                 retval += ")";
             } else {
                 retval += ",";
             }
         }
         return retval;
     }
     
     public int getID( ){
     	return ID;
     }
  
 	int getWillingnessToMakeFriends( ){
 		return willingnessToMakeFriends;
 	}
 	
 	void joinGroup(Group group){
 		groups.add(group);
 	}
 	
 	boolean isStudentInGroup(Group group){
 		for(int x = 0; x<groups.size( ); x++){
 			if(groups.get(x).equals(group)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void printStatement( ){
 		System.out.println("Willingness: "+willingnessToMakeFriends+" Number of Groups: " + groups.size( ));
 	}
     
 	/**
      * Based on the possible presence of popular attributes possessed by
      * the Group's members, possibly absorb one or more of these attributes
      * into this Person, if he/she does not already have them.
      */
     public void possiblyAbsorbAttributesFrom(Group g) {
         // for now, if you absorb an attribute, just add it, don't try to
         // even it out by removing one.
     }
 
     /**
      * Return a number from 0 to 1 based on how similar the passed Person
      * is to this Person.
      */
     
     private int attrCounter(int num, ArrayList<Boolean> attr1, ArrayList<Boolean> attr2){
     	int count = 0;
     	for(int i=0; i<num; i++){
     		//if they have the same boolean value for an attribute
     		if(attr1.get(i) == attr2.get(i)){
     			//increment constant count
     			count++;
     		}
     	}
     	return count;
     }
     
     private int attrCounter(int num, ArrayList<Double> attr1, ArrayList<Double> attr2, double interval){
     	int count = 0;
     	for(int i=0; i<num; i++){
     		double difference = attr1.get(i) - attr2.get(i);
     		difference = Math.abs(difference);
     		//if the difference is within the accept interval
     		if(difference <= interval){
     			//increment constant count
     			count++;
     		}
     	}
     	return count;
     }
     
     public double similarityTo(Person other) {
     	double similar = 0.0;
     	
     	//Kind 1: Constant
     	int constantCount = attrCounter(NUM_CONSTANT_ATTRIBUTES, attributesK1, other.attributesK1);
     	
     	//Kind 2: Independent
     	int indepCount = attrCounter(INDEPENDENT_ATTRIBUTE_POOL, attributesK2, other.attributesK2, INDEPENDENT_INTERVAL);
     	
     	//Kind 3: Dependent
     	ArrayList<Double> normalK3This = normalize(attributesK3);
     	ArrayList<Double> normalK3Other = normalize(other.attributesK3);
     	int depCount = attrCounter(DEPENDENT_ATTRIBUTE_POOL, normalK3This, normalK3Other, DEPENDENT_INTERVAL);
     	
        	//Do they have the same race?
        	int raceCount = 0;
        	if(race == other.race){
        		raceCount = 1;
        	}
        	//Do they have the same gender?
        	int genCount = 0;
        	if(gender == other.gender){
        		genCount = 1;
        	}
        	//Calculate their similarity rating, taking importance of each category (the weight) into account
     	similar = (constantCount * CONST_WEIGHT) + (indepCount * INDEP_WEIGHT)
     			+ (depCount * DEP_WEIGHT) + (raceCount * RACE_WEIGHT) + (genCount * GEN_WEIGHT);
 		return similar;
     }
     
 	public boolean areFriends(double similarities){
 		double maxRating = (NUM_CONSTANT_ATTRIBUTES * CONST_WEIGHT) + (INDEPENDENT_ATTRIBUTE_POOL * INDEP_WEIGHT)
 				+ (DEPENDENT_ATTRIBUTE_POOL * DEP_WEIGHT) + RACE_WEIGHT + GEN_WEIGHT;
 		double acceptProb = FRIENDSHIP_COEFFICIENT * (similarities / maxRating) + FRIENDSHIP_INTERCEPT;
 		double friendProb = generator.nextDouble( );
 		if(friendProb <= acceptProb){
 System.out.println("They became friends.");
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private ArrayList<Double> normalize(ArrayList<Double> attr){
 		ArrayList<Double> normal
 			= new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
 		double sum = 0.0;
 		for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
     		sum = sum + attr.get(i);
     	}
 		for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
     		double valThis = attr.get(i)/sum;
     		normal.set(i,valThis);
     	}
 		return normal;
 	}
 	
 	public ArrayList<Double> getDependentAttributes(){
 		return normalize(attributesK3);
 	}
 	
 	public ArrayList<Double> getIndependentAttributes(){
 		return attributesK2;
 	}
 	
 	public void setAttrValue(int index, double val){
 		//this functions says I want the normalized value of attribute index to be val
 		double sum = 0.0;
 		//Take the sum of all of the other non-normalized values
 		for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
 			if(index != i){
 				sum = sum + attributesK3.get(i);
 			}
 		}
 		double newNonNormalVal = (val * sum)/(1-val);
 		attributesK3.set(index, newNonNormalVal);
 	}
 }
 
 
 

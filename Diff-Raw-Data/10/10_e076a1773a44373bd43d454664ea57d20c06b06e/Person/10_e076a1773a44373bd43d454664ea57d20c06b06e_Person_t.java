 package edu.umw.cpsc.collegesim;
 import java.io.IOException;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import sim.util.distribution.Normal;
 import java.lang.Math;
 
 import sim.engine.*;
 import sim.util.*;
 import ec.util.*;
 import sim.field.network.*;
 
 /**
  * A student in the CollegeSim model.
  */
 public class Person implements Steppable{
 
     public enum Race { WHITE, MINORITY };
     public enum Gender { MALE, FEMALE };
 
     /**
      * Baseline prior probability that a newly generated student will be of
      * race "WHITE". */
     public static double PROBABILITY_WHITE;
 
     /**
      * Baseline prior probability that a newly generated student will be of
      * gender "FEMALE". */
     public static final double PROBABILITY_FEMALE = .5;
     
     /** A number reflecting the relative importance that race has in
      * determining perceived similarity. The "units" of this constant are
      * in "equivalent number of attributes"; <i>i.e.</i>, if the
      * RACE_WEIGHT is 4, this means that if another person is the same race
      * as you, this will impact your perceived similarity to them (and
      * theirs to you) to the same degree that four of your individual
      * attributes being the same would. */
     public static final double RACE_WEIGHT = 3;
 
     /** A number reflecting the relative importance that gender has in
      * determining perceived similarity. The "units" of this constant are
      * in "equivalent number of attributes"; <i>i.e.</i>, if the
      * GENDER_WEIGHT is 4, this means that if another person is the same
      * gender as you, this will impact your perceived similarity to them
      * (and theirs to you) to the same degree that four of your individual
      * attributes being the same would. */   
     public static final double GEN_WEIGHT = 1;
 
     /** The relative importance of "constant" attributes, with respect to
      * other types of attributes. ("Constant" attributes are those that are
      * unchangeable; <i>e.g.</i>, "where are you from?") */
     public static final double CONST_WEIGHT = 1;
 
     /** The relative importance of "independent" attributes, with respect to
      * other types of attributes. ("Independent" attributes are those that 
      * can vary independently with respect to each other. Having more of
      * one indep attribute does not impact your value of another indep
      * attribute. (<i>e.g.</i>, the degree to which you like purple does
      * not depend on the degree to which you like basketball.) */
     public static final double INDEP_WEIGHT = 1.5;
 
     /** The relative importance of "dependent" attributes, with respect to
      * other types of attributes. ("Dependent" attributes are those that
      * affect one other. Having more of one dep attribute invariably means
      * having relatively less of others. (<i>e.g.</i>, the degree to which
      * you spend time mountain biking has an effect on the amount of time
      * you spend reading graphic novels, because time is constant. */
     public static final double DEP_WEIGHT = 2.5;
     
     /** The coefficient (see also {@link #FRIENDSHIP_INTERCEPT}) of a linear
      * equation to transform perceived similarity to probability of
      * friendship. If x is the perceived similarity, then y=mx+b, where m
      * is the FRIENDSHIP_COEFFICIENT and b the FRIENDSHIP_INTERCEPT gives
      * the probability of becoming friends. */
     public static final double FRIENDSHIP_COEFFICIENT = .22;
 
     /** See {@link #FRIENDSHIP_COEFFICIENT}. */
     public static final double FRIENDSHIP_INTERCEPT = .05;
   
     /** Each time step (= 1 month), how many other people from a person's 
      * groups that person will encounter. Note that this number is only
      * unidirectional; <i>i.e.</i>, this person may well "be met by" 
      * numerous other people when their step() methods run. */
     public static final int NUM_TO_MEET_GROUP = 10;
 
     /** Each time step (= 1 month), how many other people from the overall
      * student body a person will encounter. Note that this number is only
      * unidirectional; <i>i.e.</i>, this person may well "be met by" 
      * numerous other people when their step() methods run. */
     public static final int NUM_TO_MEET_POP = 5;
 
     
 
     private int ID;
     private int year;
     private static MersenneTwisterFast generator = Sim.instance( ).random;
     private Normal normal = new Normal(.5, .15, generator);
     private static final int DECAY_THRESHOLD = 2;
     
     private Race race;
     private Gender gender;
     
     private double extroversion;
     private ArrayList<Group> groups;
   
     /** The total number of "constant" attributes in the system. (See {@link
      * #CONST_WEIGHT}.) Each person will have a boolean value for each,
      * indicating whether they do (or do not) possess the attribute. */
     public static int CONSTANT_ATTRIBUTE_POOL = 100;
     private ArrayList<Boolean> attributesK1     //Constant attributes
         = new ArrayList<Boolean>(
             Collections.nCopies(CONSTANT_ATTRIBUTE_POOL, false));
   
     /** The number of "independent" attributes each person has. (See {@link
      * #INDEP_WEIGHT}.) */
     public static int NUM_INDEPENDENT_ATTRIBUTES = 20;
 
     /** The total number of "independent" attributes in the system. (See 
      * {@link #INDEP_WEIGHT}.) Each person will either have the attribute or
      * not; and if they do, they will have a double value assigned
      * indicating its strength. */
     public static int INDEPENDENT_ATTRIBUTE_POOL = 50;
     //independent attributes, which can change but do not affect each other
     private ArrayList<Double> attributesK2      //Independent attributes
       = new ArrayList<Double>(Collections.nCopies(
             INDEPENDENT_ATTRIBUTE_POOL, 0.0));
 
     /** The interval inside which two indep attributes are considered "the
      * same" so for attribute 14, if this has 0.5 and other has 0.2, they
      * have this attribute in common, but if other had 0.1, they would
      * not have this attribute in common */
     public static double INDEPENDENT_INTERVAL = 0.2;
   
     /** The number of "dependent" attributes each person has. (See {@link
      * #DEP_WEIGHT}.) */
     public static int NUM_DEPENDENT_ATTRIBUTES = 20;
     /** The total number of "dependent" attributes in the system. (See 
      * {@link #DEP_WEIGHT}.) Each person will either have the attribute or
      * not; and if they do, they will have a double value assigned
      * indicating its strength. */
     public static int DEPENDENT_ATTRIBUTE_POOL = 50;
 
     //dependent attributes, which can change but you only have 1 unit to 
     //split among them
     //in other words, if one increases, then another decreases
     private ArrayList<Double> attributesK3      //Dependent attributes
       = new ArrayList<Double>(Collections.nCopies(
             DEPENDENT_ATTRIBUTE_POOL, 0.0));
 
     /** The interval inside which two dep attributes are considered "the
      * same" so for attribute 14, if this has 0.5 and other has 0.2, they
      * have this attribute in common, but if other had 0.1, they would
      * not have this attribute in common */
     public static double DEPENDENT_INTERVAL = 0.3;
 
     /**
     * The following ArrayLists are used to store each student's influencible
     * preferences at the beginning and end of each year. The end of the 
     * student's last year will be the attributesK2 and attributesK3 
     * variables
     */
     private ArrayList<Double> attributesK2Year0;
     private ArrayList<Double> attributesK3Year0;
     private ArrayList<Double> attributesK2Year1;
     private ArrayList<Double> attributesK3Year1; 
     private ArrayList<Double> attributesK2Year2;
     private ArrayList<Double> attributesK3Year2; 
     private ArrayList<Double> attributesK2Year3;
     private ArrayList<Double> attributesK3Year3;
 
     //A list that will house the absolute sim time that this person first met,
     //or last tickled, each other person
     private Hashtable<Integer,Double> lastTickleTime
       = new Hashtable<Integer,Double>();
 
     
 
 
 
     /** Removes this student from the university, forcing them to leave all groups. */
     public void leaveUniversity( ){
     	//This removes this person from all of their groups
     	for(int i=0; i<groups.size( ); i++){
     		Group group = groups.get(i);
     		group.removeStudent(this);
     	}
     }
     
     
     /**
      * The person whose ID (index) is passed has now been tickled, so this
      * sets the last tickle time for this person to the current time.
      */
     public void refreshLastTickleTime(int index){
         lastTickleTime.put(index,Sim.instance().schedule.getTime());
     }
     
     /**
      * Blah, I'm no longer friends with this person, so this completely
      * removes them from my hashtable (and life.)
      */
     public void resetLastTickleTime(int index){
         lastTickleTime.remove(index);
     }
     
     private void decay( ){
         Enumeration<Integer> friendIDs = lastTickleTime.keys();
         while (friendIDs.hasMoreElements()) {
             int friendID = friendIDs.nextElement();
             Edge toRemoveIn = null;
             Edge toRemoveOut = null;
             double val = lastTickleTime.get(friendID);
             //if the people last met longer than the threshold ago
             if(Sim.instance().schedule.getTime() - val >= DECAY_THRESHOLD){
               //Get a bag of all the edges into this person
               Bag bIn = Sim.peopleGraph.getEdgesIn(this);
               //for each of these edges
               for(int j=0; j<bIn.size( ); j++){
                 //look for the person whose ID matches the ID of the 
                 //person we want to decay
                 Edge edgeIn = (Edge)bIn.get(j);
                 Person otherPerson = (Person) edgeIn.getOtherNode(this);
                 int otherID = otherPerson.getID( );
                 if(otherID == friendID){
                   //when we find the person, make their edge the one we 
                   //want to remove
                   toRemoveIn = edgeIn;
                   j = bIn.size( );
                 }
               }
               //Do the same with the other edges
               Bag bOut = Sim.peopleGraph.getEdgesOut(this);
               Person otherPerson = null;
               //for each of these edges
               for(int j=0; j<bOut.size( ); j++){
                 //look for the person whose ID matches the ID of the person 
                 //we want to decay
                 Edge edgeOut = (Edge)bOut.get(j);
                 otherPerson = (Person) edgeOut.getOtherNode(this);
                 int otherID = otherPerson.getID( );
                 if(otherID == friendID){
                   //when we find the person, make their edge the one we 
                   //want to remove
                   toRemoveOut = edgeOut;
                   otherPerson.resetLastTickleTime(ID);
                   j = bOut.size( );
                 }
               }
               //Platypus
               //Do we have to do this? Remove the edge in and the edge out?
               Sim.peopleGraph.removeEdge(toRemoveIn);
               Sim.peopleGraph.removeEdge(toRemoveOut);
               resetLastTickleTime(friendID);
             }
           }
     }
     
     private void assignAttribute(int numAttr, int poolSize, 
         ArrayList<Double> attr){
       boolean okay;
       for(int i=0; i<numAttr; i++){
         //pick an attribute to change
         int index = generator.nextInt(poolSize);
         okay = false;
         //while we have not chosen an appropriate index
         while(!okay){
           //if the attribute is zero, it has not already been changed, so 
           //we use it
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
         //then we set the attribute at the chosen index to be the generated 
         //degree
         attr.set(index, degree);
       }
     }
     
     private boolean assignRaceGender(double probability){
       double gen = generator.nextDouble();
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
         for(int i=0; i<CONSTANT_ATTRIBUTE_POOL; i++){
             boolean rand = generator.nextBoolean( );
             attributesK1.set(i, rand);
         }
         //Assigning independent attributes
         assignAttribute(NUM_INDEPENDENT_ATTRIBUTES, 
             INDEPENDENT_ATTRIBUTE_POOL, attributesK2);
         //Assigning dependent attributes
         assignAttribute(NUM_DEPENDENT_ATTRIBUTES, 
             DEPENDENT_ATTRIBUTE_POOL, attributesK3);
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
         extroversion = normal.nextDouble();
     }
   
   /**
    * Meet the person passed as an argument, who is expected to <i>not</i>
    * already be friends with that person. Determine whether these two will
    * become friends, and if so, make them so. */
   public void meet(Person personToMeet){
     double similar;
     boolean friends = false;
     int personToMeetID = personToMeet.getID( );
     //Calculate their similarity rating, and then see if they should become 
     //friends
     similar = similarityTo(personToMeet);
     friends = areFriends(similar);
     //if they become friends, add their edge to the network
     //and reset when they met
     if(friends){
         Sim.peopleGraph.addEdge(this, personToMeet, 1);
         refreshLastTickleTime(personToMeetID);
         personToMeet.refreshLastTickleTime(ID);
     }
   }
   
   /**
    * Make this person "tickle" the person passed as an argument, who is
    * presumed to <i>already</i> be friends with the person. ("Tickle"
    * essentially means "refresh their friendship.") */
   public void tickle(Person person){
     //reset when the two last encountered each other
     int tickleID = person.getID( );
     refreshLastTickleTime(tickleID);
     person.refreshLastTickleTime(ID);
   }
   
   /**
  * THIS COMMENT IS INSANELY OUT OF DATE
    * Make this person encounter the person passed as an argument, who may
    * or may not already be friends with them. If they are not already
    * friends, they have a chance to become so, and may be by the time this
    * method returns. If they <i>are</i> already friends, their friendship
    * will be "tickled" (refreshed). */
   private void encounter(int number, Bag pool){
     if(pool.size( ) < number){
       number = pool.size( );
     }
     for(int i=0; i<number; i++){
       Person personToMeet;
       do{
         personToMeet = (Person) pool.get(generator.nextInt(pool.size( )));
       }while(personToMeet.ID == ID);
       if(friendsWith(personToMeet)){
         tickle(personToMeet);
       }else{
         meet(personToMeet);
       }
     }
   }
   
   /**
    * Make this person perform one month's actions. These include:
    * <ol>
    * <li>Encounter {@link #NUM_TO_MEET_GROUP} other people who are members
    * of one or more of their current groups.</li>
    * <li>Encounter {@link #NUM_TO_MEET_POP} other people from the student
    * body at large (who may or may not be members of their current
    * groups.)</li>
    * <li>Decay this user's existing friendships to reflect the passage of
    * time.</li>
    * </ol>
    * After this, the Person reschedules itself for the next month (or
    * August, if it's coming up on summertime.)
    * <p>Note that Persons only step during academic months.</p>
    */
   public void step(SimState state){
 	    Bag peopleBag = Sim.peopleGraph.getAllNodes( );
 	    if(!peopleBag.contains(this)){
 	        return;
 	    }
     //Get a bag of all the people in the groups
     Bag groupBag = getPeopleInGroups( );
     if(groupBag.size( ) > 1){
     	encounter(NUM_TO_MEET_GROUP, groupBag);
     }
     //Get a bag of all the people and then encounter some number of those people
     if(peopleBag.size( ) > 1){
     	encounter(NUM_TO_MEET_POP, peopleBag);
     }
 
 
     //NOTE: Decay only matters if the people are friends- you can't decay a
     //friendship that doesn't exist. So, the time they last met only
     //matters if they are friends already or if they become friends this
     //turn If they aren't already friends and if they don't become
     //friends this turn, then -1 for last met is fine (unless we
     //implement something where if two people meet enough times, they
     //become friends by brute force)
     //Now we want to see if any of the friendships have decayed
     decay( );
     
             if (Sim.instance().nextMonthInAcademicYear()) {
                 // It's not the end of the academic year yet. Run again
                 // next month.
                 Sim.instance( ).schedule.scheduleOnceIn(1, this);
             } else {
                 // It's summer break! Sleep for the summer.
                 Sim.instance( ).schedule.scheduleOnceIn(
                     Sim.NUM_MONTHS_IN_SUMMER + 1, this);
             }
   }
 
     /**
      * Output diagnostic and statistical information about this Person to
      * the writer passed.
      */
     public void printToFile(BufferedWriter writer) {
         String message = Integer.toString(ID) + ",";
         Bag b = Sim.peopleGraph.getEdgesIn(this);
         int numFriends = b.size( );
         message = message + Integer.toString(numFriends) + ","
             + Integer.toString(groups.size( )) + "," + race + "," + gender + ","
             + extroversion +  "," + year + "\n";
         try {
             writer.write(message);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Output friendship information.
      */
     public void printFriendsToFile(BufferedWriter writer) {
         String message = "";
         Bag b = Sim.peopleGraph.getEdgesIn(this);
         for (int i=0; i<b.size( ); i++) {
         	Person friend = (Person) ((Edge)b.get(i)).getOtherNode(this);
         	//We only document the friendship if the other person's ID is greater
         	//otherwise, the friendship edge was already documented
         	message = message + this.getID( ) + "," + friend.getID( ) + "\n";
         }
         //We'll only try to write if there are actually friends
         if(b.size( ) > 0){
         	try {
         		writer.write(message);
         	} catch (Exception e) {
         		e.printStackTrace();
         	}
         }
     }
 
     private boolean friendsWith(Person other) {
       Bag b = Sim.peopleGraph.getEdgesIn(this);
         for (int i=0; i<b.size(); i++) {
             Person otherSideOfThisEdge = 
                 (Person) ((Edge)b.get(i)).getOtherNode(this);
             if (other.ID == otherSideOfThisEdge.ID) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean met(Person other){
       int otherID = other.getID( );
       if(lastTickleTime.get(otherID) == -1){
         return false;
       }else{
         return true;
       }
     }
 
     public void printPreferencesToFile(BufferedWriter writer) {
         String message = this.getID( ) + ",";
         Bag b = Sim.peopleGraph.getEdgesIn(this);
         int numFriends = b.size( );
         message = message + numFriends + "," + race + "," + this.getAlienation() + "," + year + "\n";
         try {
           writer.write(message);
         } catch (Exception e) {
           e.printStackTrace();
         }
     }
 
     public void printChangeToFile(BufferedWriter writer) {
         double indAverage=0;
         double depAverage=0;
         String message = "";
         for(int x = 0; x < NUM_INDEPENDENT_ATTRIBUTES; x++){
           indAverage += Math.abs(attributesK2.get(x) - attributesK2Year0.get(x));
         }
         indAverage=indAverage/NUM_INDEPENDENT_ATTRIBUTES;
         for(int x = 0; x < NUM_DEPENDENT_ATTRIBUTES; x++){
           depAverage += Math.abs(attributesK3.get(x) - attributesK3Year0.get(x));
         }
         depAverage=depAverage/NUM_DEPENDENT_ATTRIBUTES;
         message = message + getID() + " " + extroversion + " " + Sim.peopleGraph.getEdgesIn(this).size() + " " + groups.size() + " " + depAverage + " " + indAverage + "\n";
         try {
           writer.write(message);
         } catch (Exception e) {
           e.printStackTrace();
         }
     }
 
     public String toString() {
         Bag b = Sim.peopleGraph.getEdgesIn(this);
         if (b.size() == 0) {
             return "Person " + ID + " (lonely with no friends)";
         }
         String retval = "Person " + ID + " (friends with ";
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
   
   public Race getRace( ){
 	  return race;
   }
   
   public Gender getGender( ){
 	  return gender;
   }
  
   double getExtroversion( ){
     return extroversion;
   }
   
   void joinGroup(Group group){
     groups.add(group);
   }
   
   boolean isStudentInGroup(Group group){
     for(int x = 0; x<groups.size( ); x++){
       if(groups.get(x).getID( ) == group.getID( )){
         return true;
       }
     }
     return false;
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
 
     private int attrCounter(int num, ArrayList<Boolean> attr1, 
         ArrayList<Boolean> attr2){
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
     
     private int attrCounter(int num, ArrayList<Double> attr1, 
         ArrayList<Double> attr2, double interval){
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
     
     /**
      * Returns a number between 0 and 1 indicating how similar this person
      * is perceived to be to the person passed. (1 = perfect similarity.) */
     public double similarityTo(Person other) {
       double similar = 0.0;
       
       //Kind 1: Constant
       int constantCount = attrCounter(CONSTANT_ATTRIBUTE_POOL, attributesK1, 
         other.attributesK1);
       
       //Kind 2: Independent
       int indepCount = attrCounter(INDEPENDENT_ATTRIBUTE_POOL, attributesK2, 
         other.attributesK2, INDEPENDENT_INTERVAL);
       
       //Kind 3: Dependent
       ArrayList<Double> normalK3This = normalize(attributesK3);
       ArrayList<Double> normalK3Other = normalize(other.attributesK3);
       int depCount = attrCounter(DEPENDENT_ATTRIBUTE_POOL, normalK3This, 
         normalK3Other, DEPENDENT_INTERVAL);
       
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
         //Calculate their similarity rating, taking importance of each 
         //category (the weight) into account
       similar = (constantCount * CONST_WEIGHT) + (indepCount * INDEP_WEIGHT)
           + (depCount * DEP_WEIGHT) + (raceCount * RACE_WEIGHT) 
           + (genCount * GEN_WEIGHT);
       double maxRating = (CONSTANT_ATTRIBUTE_POOL * CONST_WEIGHT) 
           + (INDEPENDENT_ATTRIBUTE_POOL * INDEP_WEIGHT)
           + (DEPENDENT_ATTRIBUTE_POOL * DEP_WEIGHT) + RACE_WEIGHT + GEN_WEIGHT;
       double similarity = similar / maxRating;
     return similarity;
     }
     
   private boolean areFriends(double similarity){
     double acceptProb = 
         FRIENDSHIP_COEFFICIENT * similarity + FRIENDSHIP_INTERCEPT;
     double friendProb = generator.nextDouble( );
     if(friendProb <= acceptProb){
       return true;
     }else{
       return false;
     }
   }
   
   private ArrayList<Double> normalize(ArrayList<Double> attr){
     ArrayList<Double> normal = new ArrayList<Double>(
         Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
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
   
   
   public double getAlienation( ){
 	  //Get the number of friends this person has
 	  Bag bIn = Sim.peopleGraph.getEdgesIn(this);
 	  int numFriends = bIn.size( );
 	  //Find the percent of the population with which this person is friends
 	  //int totalPeople = Sim.getNumPeople( );
 	  //Platypus
 	  double requiredNumFriends = 3.0;
 	  double percFriends = numFriends / requiredNumFriends;
 	  //As extroversion increases, the likelihood to feel alienated increases
 	  //As the percent of friends you have in the population increases, the likelihood
 	  //to feel alienated decreases
 	  double alienationFactor = extroversion / percFriends;
 	  if(alienationFactor > 1){
 		  alienationFactor = 1;
 	  }
 	  return alienationFactor;
   }
   
   
   /** Returns a list of doubles, one for each of the {@link
    * #DEPENDENT_ATTRIBUTE_POOL} possible dep attributes. This will indicate
    * the degree to which the person possesses each of those attributes (0.0
    * = does not have that attribute at all.) */
   public ArrayList<Double> getDependentAttributes(){
     return normalize(attributesK3);
   }
   
   /** Returns a list of doubles, one for each of the {@link
    * #INDEPENDENT_ATTRIBUTE_POOL} possible indep attributes. This will *
    * indicate the degree to which the person possesses each of those
    * attributes (0 = does not have that attribute at all.) */
   public ArrayList<Double> getIndependentAttributes(){
     return attributesK2;
   }
 
   /** Sets the value of the independent attribute whose index is passed to
    * the value passed. */
   public void setIndAttrValue(int index, double val){
     attributesK2.set(index, val);
   }
 
   /** Sets the value of the dependent attribute whose index is passed to
    * the value passed. Internally, this may have the side effect of
    * adjusting the values of the other dependent attributes so that their
    * normalized sum continues to equal 1. */
   public void setDepAttrValue(int index, double val){
     //this functions says I want the normalized value of attribute index 
     // to be val
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
 
 //  public ArrayList<Person> getPeopleInGroups( ){
 //    ArrayList<Person> groupmates = new ArrayList<Person>();
 //    boolean addPerson;
 //    for(int x = 0; x < groups.size(); x++){
 //      for(int y = 0; y < groups.get(x).getSize(); y++){
 //        addPerson = true;
 //        for(int z = 0; z < groupmates.size(); z++){
 //          if (groups.get(x).getPersonAtIndex(y).equals(groupmates.get(z))){
 //            addPerson = false;
 //          }
 //        }
 //        if(addPerson&&!(groups.get(x).getPersonAtIndex(y).equals(this))){
 //          groupmates.add(groups.get(x).getPersonAtIndex(y));
 //        }
 //      }
 //    }
 //    return groupmates;
 //  }
   
   /**
    * FIX: Is this essentially just "get a bag of all the people who are in
    * one or more of this person's groups"? */
 
   public Bag getPeopleInGroups( ){
     boolean repeat = false;
     Bag groupmates = new Bag();
     for(int x = 0; x < groups.size( ); x++){
       for(int y = 0; y < groups.get(x).getSize(); y++){
         for(int z = 0; z < groupmates.size(); z++){
           if(groups.get(x).getPersonAtIndex(y).ID == ((Person) groupmates.get(z)).ID){
             repeat = true;    //student is already in this bag, don't add again
           }
         }
         if(!repeat){
           groupmates.add(groups.get(x).getPersonAtIndex(y));
         }
         repeat = false;
       }
     }
     return groupmates;
   }
 
   /*
   public Bag getPeopleInGroups( ){
     Bag groupmates = new Bag();
     boolean addPerson;
     boolean first = true;
     for(int x = 0; x < groups.size( ); x++){
       for(int y = 0; y < groups.get(x).getSize(); y++){
         addPerson = true;
         Person personToAdd = groups.get(x).getPersonAtIndex(y);
         if(first){
           if(!personToAdd.equals(this)){
             groupmates.add(personToAdd);
           }
         }else{
           for(int z = 0; z < groupmates.size(); z++){
             if(personToAdd.equals(groupmates.get(z))){
               addPerson = false;
             }
           }
           if(addPerson && !(groups.get(x).getPersonAtIndex(y).equals(this))){
             groupmates.add(groups.get(x).getPersonAtIndex(y));
           }
         }
       }
     }
     return groupmates;
   }
 */
     /** Marks this Person as no longer being a member of the Group passed.
      * Should <i>not</i> be called in isolation, else the Group object will
      * still think the Person is a member! See {@link
      * edu.umw.cpsc.collegesim.Group#removeEveryoneFromGroup()}.
      */
     public void leaveGroup(Group g){
         for(int x = 0; x<groups.size(); x++){
           if(groups.get(x).getID( ) == g.getID( )){
               //MONTY PYTHON'S HOLY GRAIL RIGHT HERE
               groups.get(x).removeStudent(this);
             groups.remove(x);
           }
         }
       }
 
 //   public boolean equals(Person p){
 //     return(ID==p.getID());
 //   }
 
     /** Sets the school year (1=freshman, 2=sophomore, etc.) of this
      * Person. No validation checking is performed. */
   public void setYear(int x){
     year = x;
     //store initial attributes
     if(year==1){
       attributesK2Year0=new ArrayList<Double>(attributesK2);
       attributesK3Year0=new ArrayList<Double>(attributesK3);
     }else if(year==2){
       attributesK2Year1=new ArrayList<Double>(attributesK2);
       attributesK3Year1=new ArrayList<Double>(attributesK3);
     }else if(year==3){
       attributesK2Year2=new ArrayList<Double>(attributesK2);
       attributesK3Year2=new ArrayList<Double>(attributesK3);
     }else if(year==4){
       attributesK2Year3=new ArrayList<Double>(attributesK2);
       attributesK3Year3=new ArrayList<Double>(attributesK3);
     }
   }
 
     /** Gets the school year (1=freshman, 2=sophomore, etc.) of this
      * Person.  */
   public int getYear(){
     return year;
   }
 
     /** Increments the school year (1=freshman, 2=sophomore, etc.) of this
      * Person, possibly to 5 or higher (no validation checking is
      * performed). */
   public void incrementYear(){
     if(year==1){
       attributesK2Year1=new ArrayList<Double>(attributesK2);
       attributesK3Year1=new ArrayList<Double>(attributesK3);
     }else if(year==2){
       attributesK2Year2=new ArrayList<Double>(attributesK2);
       attributesK3Year2=new ArrayList<Double>(attributesK3);
     }else if(year==3){
       attributesK2Year3=new ArrayList<Double>(attributesK2);
       attributesK3Year3=new ArrayList<Double>(attributesK3);
     }
     year++;
   }
 
   public boolean hasFullData(){
     if(attributesK2Year0!=null&&attributesK2Year1!=null&&attributesK2Year3!=null&&attributesK2!=null&&attributesK3Year0!=null&&attributesK3Year1!=null&&attributesK3Year3!=null&&attributesK3!=null){
       return true;
     }else{
       return false;
     }
   }
 
 }

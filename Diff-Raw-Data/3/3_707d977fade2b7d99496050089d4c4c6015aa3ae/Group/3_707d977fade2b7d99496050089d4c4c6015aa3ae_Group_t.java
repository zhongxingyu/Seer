 
 package edu.umw.cpsc.collegesim;
 
 import java.util.ArrayList;
 import ec.util.*;
 import sim.engine.*;
 
 /*TODO: clean up size stuff, maybe get rid of the variable all together and just keep the setter and use students.size() in the class. 
 * Should I change students to people? That way it would be consistant throughout the program
 * maybe change factors to 0-1 rather than 0-10?
 * What are we doing with tightness? Does it help determine recruitement? Or should it deal with leaving a group?
 * maybe have a max/min num students per group factor?
 */
 
 
 public class Group implements Steppable{
 	//all hard coded rands are subject to change
 	private final int MINIMUM_START_GROUP_SIZE = 3;
 	private final int MAXIMUM_START_GROUP_SIZE = 8; 
 	private final int RECRUITMENT_REQUIRED = 8;			//lower this to accept more students in group per step
 	private final double LIKELYHOOD_OF_RANDOMLY_LEAVING_GROUP = .1;		//increase this to remove more students in group per step
 	private final double LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE = .1;
 	private int id;
 	private int size = 0;//based on how many people join-- affects it for now by decreasing the recruitment factor when increased-- gotta think of a way to scale it though to effect the closeness appropriately 
 	private int tightness=0;//based on individual students' willingness to make friends in the group
 	private int frequency;//random 1-10
 	private int recruitmentFactor;//random 1-10
 	static MersenneTwisterFast rand;
 
 	private ArrayList<Person> students;
 	
 	public Group(int x){
 		id = x;
 		rand = Sim.instance( ).random;
 		frequency=rand.nextInt(10)+1; 
 		recruitmentFactor=rand.nextInt(10)+1; 
 		students = new ArrayList<Person>();
 	}
 
 	void selectStartingStudents(ArrayList<Person> people){
 		int initialGroupSize = rand.nextInt(MAXIMUM_START_GROUP_SIZE-MINIMUM_START_GROUP_SIZE)+MINIMUM_START_GROUP_SIZE+1;
 		Person randStudent;
 		if(initialGroupSize>Sim.getNumPeople()){
 			initialGroupSize=Sim.getNumPeople(); 		//to insure the initial group size is never greater than the number of total people
 		}
 		for(int x = 0; x < initialGroupSize; x++){
 			randStudent = people.get(rand.nextInt(people.size()));
 			while(doesGroupContainStudent(randStudent)){
 				randStudent = people.get(rand.nextInt(people.size()));
 			}
 			students.add(randStudent);
 			randStudent.joinGroup(this);
 		}
 		size = students.size();
 	}
 
 	void recruitStudent(Person s){
 		if(!doesGroupContainStudent(s)){
 			/*System.out.println("A: " + affinityTo(s));
 			System.out.println("RF: " +recruitmentFactor);
 			System.out.println("Willing: " + s.getWillingnessToMakeFriends());
 			System.out.println("Rand: " + rand.nextInt(10));
 			*/
      		double r = (affinityTo(s) + recruitmentFactor + s.getWillingnessToMakeFriends()*2 + (rand.nextInt(10)+1)*2)/6.0; //want to mess with balence here
      		System.out.println("\nFinal Recruitment: " + r);
      		System.out.println("Person " + s.getID() + " looks at group " + id);
      		if(r>RECRUITMENT_REQUIRED){
      	  		students.add(s);
      	 		s.joinGroup(this);
      	 		System.out.println("Person " + s.getID() + " joined group " + id);
      		}
      		size = students.size();
      		int t=0;
      		for(int x = 0; x<size; x++){
      			t += students.get(x).getWillingnessToMakeFriends();
      		}
      		if(size>0){
     			tightness = t/size;
  	 		}
    		}
     }
 	
 	private boolean doesGroupContainStudent(Person p){
 		for (int x = 0; x<students.size(); x++){
 			if (p.getID()==students.get(x).getID()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	boolean equals(Group a){
 		return (id==a.getID());
 	}
 
 	 /*
      * Return a number from 0 to 1 indicating the degree of affinity the
      *   Person passed has to the existing members of this group.
      */
 	double affinityTo(Person p) {
     	if(size>0){
     		double temp=0;
     		for(int x = 0; x<students.size(); x++){
     			temp = p.similarityTo(students.get(x));
     		}
     		return (temp/students.size()*10);
     	}else{
     		return 5;
     	}
 
         // write this maddie
         // ideas:
         //    for each of the person's attributes, find the avg number of
         //    group members (with that attribute, and then take the avg of
         //    those averages.
         //  Ex: The group has persons F, T, Q. The Person in question is
         //  person A. Person A has three attributes: 1, 4, and 5. Attribute
         //  1 is owned by F and T. Attribute 4 is owned by F, T, and Q.
         //  Attribute 5 is owned by no one in the group. So, the affinity
         //  for Person A to this group is (2/3 + 3/3 + 0/3)/3 = 5/3/3
         //
         // question: what to return from this method if the group is empty?
         // .5?
     }
 
    	void influenceMembers(){
      if(students.size()>0){
    		System.out.println("**Influence members**");
     	ArrayList<Double> independentAverage = new ArrayList<Double>();
     	ArrayList<Double> dependentAverage = new ArrayList<Double>();
       	double tempTotal;
       	for (int x = 0; x<students.get(0).getIndependentAttributes().size(); x++){    
         	tempTotal=0;
         	for (int y = 0; y<students.size(); y++){
           		tempTotal+=students.get(y).getIndependentAttributes().get(x);
         	}
         	independentAverage.add(tempTotal/students.size());
       	}
       	for (int x = 0; x<students.get(0).getDependentAttributes().size(); x++){
         	tempTotal=0;
         	for (int y = 0; y<students.size(); y++){
           		tempTotal+=students.get(y).getDependentAttributes().get(x);
         	}
         	dependentAverage.add(tempTotal/students.size());
       	}
 
       	//At this point, both independentAverage and dependentAverage are filled
       	//the following should use two rands-- one to see if the attribute should in fact change, and another to be used to multiply by the distance to calculate how much it would increment by
       	//note that a group's influence will never directly decrement any attributes-- dependent attributes may only decrement by indirect normalization
       	//We have to keep our numbers pretty low here-- this will be called at every step
       	double distanceI;  //distance between current person's current independent attribute and the group's average attribute
       	double distanceD;  //distance between current person's current dependent attribute and group's average attribute
       	double increment; //how much each attribute will increment by
       	for(int x = 0; x<students.size(); x++){
         	for (int y = 0; y<independentAverage.size(); y++){
           		distanceI = independentAverage.get(y) - students.get(x).getIndependentAttributes().get(y);
           		distanceD = dependentAverage.get(y) - students.get(x).getDependentAttributes().get(y);
           		if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceI>0){  //rand subject to change 
             		increment = (rand.nextDouble(true,true)/52)*distanceI; //random number inclusively from 0-1, then divide by 5, then multiply by the distance that attribute is from the group's average
             		students.get(x).setIndAttrValue(y, (students.get(x).getIndependentAttributes().get(y))+increment);
             		System.out.println("Person " + students.get(x).getID() + "has changed an independent attribute");
           		}  
 
           		if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceD>0){  
             		increment = (rand.nextDouble(true, true)/5)*distanceD;
             		students.get(x).setDepAttrValue(y, (students.get(x).getDependentAttributes().get(y))+increment);  //Morgan's method
           			System.out.println("Person " + students.get(x).getID() + " has changed a dependent attribute");
           		}
         	}
       	}
      }
     }
 
     public void possiblyLeaveGroup(Person p){
     	if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_LEAVING_GROUP){
     		p.leaveGroup(this);
     		removeStudent(p);
     		System.out.println("Removing Student "+p.getID()+" from group " + id);
     	}
     }
       
     public void step(SimState state){
     	ArrayList<Person> allPeople = Sim.getPeople();
     	influenceMembers();
     	for(int x = 0; x<allPeople.size(); x++){ 		//do we want to narrow down the list of people who could possibly join?
     		recruitStudent(allPeople.get(x));
     	}
     	for(int x = 0; x<students.size(); x++){
     		possiblyLeaveGroup(students.get(x));
     	}
         if (Sim.instance().nextMonthInAcademicYear()) {
             // It's not the end of the academic year yet. Run again
             // next month.
             Sim.instance( ).schedule.scheduleOnceIn(1, this);
         } else {
             // It's summer break! Sleep for the summer.
             Sim.instance( ).schedule.scheduleOnceIn(
                 Sim.NUM_MONTHS_IN_SUMMER, this);
         }
  	}
 
 	
 	public void setSize(int s){
 		size=s;
 	}
 	
 	public void setTightness(int t){
 		tightness=t;
 	}
 	
 	public void setFrequency(int f){
 		frequency=f;
 	}
 	
 	public void setRecruitmentFactor(int r){
 		recruitmentFactor=r;
 	}
 	
 	public int getSize(){
 		return students.size();
 	}
 	
 	public int getTightness(){
 		return tightness;
 	}
 	
 	public int getFrequency(){
 		return frequency;
 	}
 	
 	public double getRecruitmentFactor(){
 		return recruitmentFactor;
 	}
 	
 	public int getCloseness(){
 		return (tightness+frequency+recruitmentFactor)/3; //maybe this could be used for leaving the group
 	}
 
 	public String toString(){
 		return "Closeness: "+ getCloseness() + " (Size: " + size + " Tightness: " + tightness + " Frequency: " + frequency + " Recruitment Factor: "+ recruitmentFactor + ")";
 	}
 
 	public void listMembers(){
 		System.out.println("The following students are in group " + id + ":");
 		for(int x = 0; x < students.size(); x++){
 			System.out.println("\t" + students.get(x));
 		}
 	}
 	
 	public int getID(){
 		return id;
 	}
 	
 	public void setID(int i){
 		id=i;
 	}
 
 	public Person getPersonAtIndex(int x){
 		return students.get(x);
 	}
 
 	public void removeStudent(Person p){
     	for(int x = 0; x<students.size(); x++){
       		if(students.get(x).equals(p)){
         		students.remove(x);
       		}
     	}
   	}
 
 }
 

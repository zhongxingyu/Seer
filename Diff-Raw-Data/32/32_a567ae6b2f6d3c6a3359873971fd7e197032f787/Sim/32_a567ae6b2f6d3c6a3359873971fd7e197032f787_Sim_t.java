 package edu.umw.cpsc.collegesim;
 import sim.engine.*;
 import sim.util.*;
 import sim.field.network.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 /** The top-level singleton simulation class, with main(). */
 public class Sim extends SimState implements Steppable{
 	
     /**
      * A graph where each node is a student and each edge is a friendship between
      * those students. It is undirected. */
 	public static Network peopleGraph = new Network(false);
 
 //	public static Network lastMet = new Network(false);
 
     /**
      * The number of people, of random year-in-college (fresh, soph, etc.)
      * that the simulation will begin with. */
 	public static final int INIT_NUM_PEOPLE = 100;
 
 
     /**
      * The number of groups, with random initial membership, that the
      * simulation will begin with. */
 	public static final int INIT_NUM_GROUPS = 30;
 
 
     /**
      * The number of newly enrolling freshmen each year.     */
 	public static final int NUM_PEOPLE_ENROLLING_EACH_YEAR = 25;
 
 
     /**
      * The number of groups, with random initial membership, that the
      * simulation will begin with. */
 	public static final int NUM_GROUPS_ADDED_EACH_YEAR = 10;
 
 
     /**
      * The per-year probability of each student dropping out of college. */
 	public static final double DROPOUT_RATE = .02;
 
 	private static ArrayList<Group> groups = new ArrayList<Group>();
 	
 	//Platypus Do we need this now? It looks like it because the network doesn't
 	//have a method to determine the size - you could get a bag of the people from the network
 	//and then get the size of the bag
 	private static ArrayList<Person> peopleList = new ArrayList<Person>();
 	
     private static long SEED = 0;
     private static Sim theInstance;
 
     public static final int NUM_MONTHS_IN_ACADEMIC_YEAR = 9;
     public static final int NUM_MONTHS_IN_SUMMER = 3;
     public static final int NUM_MONTHS_IN_YEAR = NUM_MONTHS_IN_ACADEMIC_YEAR +
         NUM_MONTHS_IN_SUMMER;
     
     //Platypus do we use both of these?
     public static final int MAX_ITER = 1000;
     public static final int NUM_SIMULATION_YEARS=  8;
 
     private static File outF;
 	private static BufferedWriter outWriter;
 	
 	//Platypus do we need these?
 	private static int currentStudentID = 0;
 	private static int currentGroupID = 0; 
 
     
     // Here is the schedule!
     // Persons run at clock time 0.5, 1.5, 2.5, ..., 8.5.
     // Groups run at clock time 1, 2, 3, ..., 9.
     boolean nextMonthInAcademicYear() {
         double curTime = Sim.instance().schedule.getTime();
         int curTimeInt = (int) Math.ceil(curTime);
         // if curTimeInt is 1, that means we are at the first month.
         int monthsWithinYear = curTimeInt % NUM_MONTHS_IN_YEAR;
         return (monthsWithinYear < NUM_MONTHS_IN_ACADEMIC_YEAR);
     }
 
     //Platypus
     public static int getNumPeople( ){
     	return peopleList.size();
     }
 
     public int getNumGroups(){
     	return groups.size();
     }
 
     public static synchronized Sim instance( ){
         if (theInstance == null){
             theInstance = new Sim(SEED);
         }
         return theInstance;
     }
 
     //Platypus
     public static ArrayList<Person> getPeople(){
     	return peopleList;
     }
     
 	public Sim(long seed){
 		super(seed);
 	}
 	
 	public void start( ){
 		super.start( );
 		//create people, put them in the network, and add them to the
 		//schedule
 		for(int i=0; i<INIT_NUM_PEOPLE; i++){
 			//Create a new student with the desired student ID.
 			//Here, currentStudentID and i will be identical. But we want to increment
 			//currentStudentID so we can use it later as the years go on, when the
 			//number of students and the assignment of IDs will no longer match the iterator (i)
 			Person person = new Person(currentStudentID);
 			currentStudentID++;
 			//Give them a random year
 			person.setYear(random.nextInt(4)+1);
 			//Add them to the list of students
 			//Platypus
 			peopleList.add(person);
 			//Add the student to the Network
 			peopleGraph.addNode(person);
 //			lastMet.addNode(person);
 			//Schedule the student to step
 			schedule.scheduleOnceIn(1.5, person);
 		}
 
 		for(int x = 0; x<INIT_NUM_GROUPS; x++){
 			//Create a new group with a group ID and give it the list of people
 			Group group = new Group(currentGroupID, peopleList);
 			currentGroupID++;
 			//Add it to the list of groups
 			groups.add(group);
 			//Schedule the group to step
 			schedule.scheduleOnceIn(2.0, group);
 		}
 
 		//Schedule this (why?) Platypus
 		schedule.scheduleOnceIn(1.1, this);
 
 	}
 	
     /**
      * Run the simulation from the command line (no arguments necessary). */
 	public static void main(String[] args) throws IOException {
         doLoop(new MakesSimState() { 
             public SimState newInstance(long seed, String[] args) {
                 Sim.SEED = seed;
                 return instance();
             }
             public Class simulationClass() {
                 return Sim.class;
             }
         }, args);
 	}
 
     private boolean isEndOfSim() {
         return (schedule.getTime()/NUM_MONTHS_IN_YEAR) > NUM_SIMULATION_YEARS;
     }
 
     private int getCurrYearNum() {
         return (int) schedule.getTime()/NUM_MONTHS_IN_YEAR;
     }
 
     private void dumpToFiles() {
 
         if(outWriter!=null){
             try{
                 outWriter.close();
             }catch(IOException e){
                 System.out.println("Could not close file");
             }
         }
         //if((int)(schedule.getTime()/NUM_MONTHS_IN_YEAR)!=NUM_SIMULATION_YEARS){
         if(!isEndOfSim()) {
            String f="year"+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR)+".txt";
             try{
                 outF = new File(f);
                 outF.createNewFile( );
                 outWriter = new BufferedWriter(new FileWriter(outF));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             //Platypus
             //We can probably leave this but just rewrite the print file for person
             for(int x = 0; x<peopleList.size(); x++){
                 peopleList.get(x).printToFile(outWriter);
             }
         }
     }
 
 	public void step(SimState state){
 
 		if(!isEndOfSim()) {
 
 			if(nextMonthInAcademicYear()){
                 /*
                  * August.
                  * Year-start activities. Increment everyone's year, enroll
                  * the new freshman class, create new groups.
                  */
                 System.out.println("---------------");
                 System.out.println("Starting year: "+getCurrYearNum());
 				for(int x = 0; x<peopleList.size(); x++){
 					//Platypus
 					//Is this something we need to track in the graph?
 					peopleList.get(x).incrementYear();
 				}
 				for(int x = 0; x<NUM_PEOPLE_ENROLLING_EACH_YEAR; x++){
 					//Create a new student
 					Person person = new Person(currentStudentID);
 					currentStudentID++;
 					//Make them a freshman
 					person.setYear(1);
 					//Add the student to the list and the graph
 					peopleList.add(person);
 					peopleGraph.addNode(person);
 					//Schedule the person
 					schedule.scheduleOnceIn(1.5, person);
 				}
 				for(int x = 0; x<NUM_GROUPS_ADDED_EACH_YEAR; x++){
 					//Create a new group with the list of people
 					Group group = new Group(currentGroupID, peopleList);
 					currentGroupID++;
 					//Add the group
 					groups.add(group);
 					//Schedule the group
 					schedule.scheduleOnceIn(2.0,group);
 				}
                 /*
                  * The new academic year is now ready to begin! Schedule
                  * myself to wake up in May.
                  */
 				schedule.scheduleOnceIn(NUM_MONTHS_IN_ACADEMIC_YEAR, this);
 
 			}else{
 
                 /*
                  * May.
                  * Year-end activities. Dump output files, graduate and
                  * dropout students, remove some groups.
                  */
 				System.out.println("End of year: "+getCurrYearNum());
 				ArrayList<Person> toRemove = new ArrayList<Person>();
 				ArrayList<Group> toRemoveGroups = new ArrayList<Group>();
 
                 dumpToFiles();
 				if(!isEndOfSim()) {
					//For all of the people
 					for(int x = 0; x<peopleList.size(); x++){
						Person student = peopleList.get(x);
						//If they have more than four years, they graduate
						if(student.getYear( ) >= 4){
 							System.out.println("Person " + 
                                student.getID() +
                                 " has graduated! Congrats!");
							toRemove.add(student);
						//Otherwise
						}else{
							boolean isAlienated = student.getAlienation( );
							//If they feel alienated, they have a chance to drop out
							if(isAlienated){
								double dropChance = random.nextDouble( );
								if(dropChance <= DROPOUT_RATE){
									System.out.println("Person " + student.getID( ) +
											" has dropped out of school.");
									toRemove.add(student);
								}
							}
 						}
 					}
 					for(int x = 0; x<groups.size(); x++){
 						if(random.nextDouble(true, true)>.75){
 							System.out.println("Removing group " +
                                 groups.get(x).getID());
 							toRemoveGroups.add(groups.get(x));
 						}
 					}
 					for(int x = 0; x<toRemoveGroups.size(); x++){
 						toRemoveGroups.get(x).removeEveryoneFromGroup();
 						groups.remove(toRemoveGroups.get(x));
 					}
 					for(int x = 0; x<toRemove.size(); x++){
 						//Let the person leave their groups
 						toRemove.get(x).leaveUniversity();
 						//remove the person from the list of people
 						peopleList.remove(toRemove.get(x));
 						//remove the person from the graph of people and friendships
 						peopleGraph.removeNode(toRemove.get(x));
 					}
 					toRemoveGroups.clear();
 					toRemove.clear();
 				}
                 /*
                  * The academic year is now complete -- have a great summer!
                  * Schedule myself to wake up in August.
                  */
 				schedule.scheduleOnceIn(NUM_MONTHS_IN_SUMMER, this);
 			}
 		}else{
 			schedule.seal();
 		}
 
 	}
 
 }

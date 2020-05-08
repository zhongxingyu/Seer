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
 
 	public static int currentTrial;
 	public static double param;
 
     /**
      * The number of people, of random year-in-college (fresh, soph, etc.)
      * that the simulation will begin with. */
 	public static final int INIT_NUM_PEOPLE = 4000;
 
 
     /**
      * The number of groups, with random initial membership, that the
      * simulation will begin with. */
 	public static final int INIT_NUM_GROUPS = 200;
 
 
     /**
      * The number of newly enrolling freshmen each year.     */
 	public static final int NUM_PEOPLE_ENROLLING_EACH_YEAR = 1000;
 
 
     /**
      * The number of groups, with random initial membership, that the
      * simulation will begin with. */
 	public static final int NUM_GROUPS_ADDED_EACH_YEAR = 100;
 	
     /** The coefficient (see also {@link #DROPOUT_INTERCEPT}) of a linear
      * equation to transform alienation to probability of
      * dropping out. If x is based on the alienation level,
      * then y=mx+b, where m is the DROPOUT_RATE and b the
      * DROPOUT_INTERCEPT, gives the probability of dropping out. */
     public static final double DROPOUT_RATE = 0.01;
     
     /** See {@link #DROPOUT_RATE}. */
     public static final double DROPOUT_INTERCEPT = 0.05;
 
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
 	private static File FoutF;
 	private static BufferedWriter FoutWriter;
     private static File PrefoutF;
     private static BufferedWriter PrefoutWriter;
 	
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
 
     //Giraffe
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
         /*PrefoutF= new File("preferencesDropout.csv");
         FoutF.delete();
         PrefoutF= new File("preferencesGraduate.csv");
         FoutF.delete();
         */
 
         /*try{
             File file = new File("preferencesGraduate.csv");
             file.delete();
             file = new File("preferencesDropout.csv");
             file.delete();
             file = new File("averageChange.csv");
             file.delete();
             }catch(Exception e){
             e.printStackTrace();
  
         }*/
 
 		for(int i=0; i<INIT_NUM_PEOPLE; i++){
             //Create a new student with the desired student ID. Here,
             //currentStudentID and i will be identical. But we want to
             //increment currentStudentID so we can use it later as the
             //years go on, when the number of students and the assignment
             //of IDs will no longer match the iterator (i)
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
 		currentTrial = Integer.parseInt(args[0]);
 		param = Double.parseDouble(args[1]);
		Person.PROBABILITY_WHITE = Double.parseDouble(args[1]);
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
 
     int getCurrYearNum() {
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
         if(FoutWriter!=null){
             try{
                 FoutWriter.close();
             }catch(IOException e){
                 System.out.println("Could not close file");
             }
         }
         //if((int)(schedule.getTime()/NUM_MONTHS_IN_YEAR)!=NUM_SIMULATION_YEARS){
         if(!isEndOfSim()){
             String f="P"+param+"T"+currentTrial+"year"+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR)+".csv";
             try{
                 outF = new File(f);
                 outF.createNewFile( );
                 outWriter = new BufferedWriter(new FileWriter(outF));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             //Platypus. Indeed.
             //We can probably leave this but just rewrite the print file for person
             for(int x = 0; x<peopleList.size(); x++){
                 peopleList.get(x).printToFile(outWriter);
                 peopleList.get(x).toString();
             }
             
             //FILE OF FRIENDSHIPS
             String ff="P"+param+"T"+currentTrial+"edges"+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR)+".csv";
             try{
                 FoutF = new File(ff);
                 FoutF.createNewFile();
                 FoutWriter = new BufferedWriter(new FileWriter(FoutF));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             for(int x = 0; x<peopleList.size(); x++){
                 peopleList.get(x).printFriendsToFile(FoutWriter);
             }
         }
     }
 
     public void dumpPreferencesOfGraduatedStudent(Person x){
         if(PrefoutWriter!=null){
             try{
                 PrefoutWriter.close();
             }catch(IOException e){
                 System.out.println("Could not close file");
             }
         }
         /*try{
                 PrefoutF = new File("preferencesGraduate.csv");
                 if(!PrefoutF.exists()){
                     PrefoutF.createNewFile();
                 }
                 PrefoutWriter = new BufferedWriter(new FileWriter(PrefoutF, true));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             x.printPreferencesToFile(PrefoutWriter);*/
 
 
             if(PrefoutWriter!=null){
             try{
                 PrefoutWriter.close();
             }catch(IOException e){
                 System.out.println("Could not close file");
             }
         }
         /*try{
                 PrefoutF = new File("averageChange.csv");
                 if(!PrefoutF.exists()){
                     PrefoutF.createNewFile();
                     try {
                         PrefoutWriter = new BufferedWriter(new FileWriter(PrefoutF, true));
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
                 PrefoutWriter = new BufferedWriter(new FileWriter(PrefoutF, true));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             if(x.hasFullData()){
                 x.printChangeToFile(PrefoutWriter);
             }*/
     }
 
     public void dumpPreferencesOfDropoutStudent(Person x){
         if(PrefoutWriter!=null){
             try{
                 PrefoutWriter.close();
             }catch(IOException e){
                 System.out.println("Could not close file");
             }
         }
         try{
 		String string = "P"+param+"T"+currentTrial+"dropout.csv";
                 PrefoutF = new File(string);
                 if(!PrefoutF.exists()){
                     PrefoutF.createNewFile();
                 }
                 PrefoutWriter = new BufferedWriter(new FileWriter(PrefoutF, true));
             }catch(IOException e){
                 System.out.println("Couldn't create file");
                 e.printStackTrace();
                 System.exit(1);
             }
             x.printPreferencesToFile(PrefoutWriter);
     }
 
 	public void step(SimState state){
 
 //System.out.println("Sim::step(). The clock is now " + schedule.getTime());
 		if(!isEndOfSim()) {
 
 			if(nextMonthInAcademicYear()){
                 /*
                  * August.
                  * Year-start activities. Increment everyone's year, enroll
                  * the new freshman class, create new groups.
                  */
                 System.out.println("---------------");
                 System.out.println("Starting year: "+getCurrYearNum());
 //Bag b = peopleGraph.getAllNodes();
 //boolean itIsInThere = false;
 //for (int i=0; i<b.size(); i++) {
 //    if (((Person)b.get(i)).getID() == personInQuestion) {
 //        itIsInThere = true;
 //    }
 //}
 //if (itIsInThere) {
 //System.out.println("At start of year, " + personInQuestion + " is IN the graph.");
 //} else {
 //System.out.println("At start of year, " + personInQuestion + " is NOT in the graph.");
 //}
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
 					//Why 1.4? Because (1) we the Sim are running at int.1,
 					//and (2) (Morgan and Stephen are too tired to figure
 					//out the second part.)
 					schedule.scheduleOnceIn(1.4, person);
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
 //							System.out.println("Person " + 
   //                              student.getID() +
     //                            " has graduated! Congrats!");
                             dumpPreferencesOfGraduatedStudent(student);
 							toRemove.add(student);
 						//Otherwise
 						}else{
 							double alienationLevel = student.getAlienation( );
 							double alienation = DROPOUT_RATE * alienationLevel + DROPOUT_INTERCEPT; 
 							//If they feel alienated, they have a chance to drop out
 							double dropChance = random.nextDouble( );
 							if(dropChance <= alienation){
 //								System.out.println("Person " + student.getID( ) +
 //										" has dropped out of school.");
                                 dumpPreferencesOfDropoutStudent(student);
 								toRemove.add(student);
 							}
 						}
 					}
 					for(int x = 0; x<groups.size(); x++){
 						if(random.nextDouble(true, true)>.75){
 //							System.out.println("Removing group " +
 //                                groups.get(x).getID());
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
 //Bag b = peopleGraph.getAllNodes();
 //boolean itIsInThere = false;
 //for (int i=0; i<b.size(); i++) {
 //    if (((Person)b.get(i)).getID() == personInQuestion) {
 //        itIsInThere = true;
 //    }
 //}
 //if (itIsInThere) {
 //System.out.println(personInQuestion + " is IN the graph.");
 //} else {
 //System.out.println(personInQuestion + " is NOT in the graph.");
 //}
 			}
 		}else{
 			schedule.seal();
 		}
 
 	}
 
 }

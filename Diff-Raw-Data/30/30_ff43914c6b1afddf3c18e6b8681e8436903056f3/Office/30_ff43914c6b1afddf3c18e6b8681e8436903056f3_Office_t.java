 
 package edu.se.se441;
 
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.se.se441.threads.*;
 
 public class Office {
     private Clock clock;
 
     // Barriers
     private CyclicBarrier standupMeeting = new CyclicBarrier(4);
     private CyclicBarrier[] teamMeetings = new CyclicBarrier[3];
     private CyclicBarrier endOfDayMeeting = new CyclicBarrier(13);
 
     // Manager and Employees
     private Manager manager;
     private Employee[] leads = new Employee[3];
 
     // Capacity of conference room.
    private volatile int confRoom = 0;
    private volatile int confRoomUsedBy = -1;
 
     // Locks.
     private Object workingLock = new Object();
     private Object confRoomLock = new Object();
     private Object leadQLock = new Object();
 
     /**
      * Instantiates the office with a given clock.
      * Sets up the meeting barriers for the maximum number of employees
      * @param clock the Clock that the office uses to keep track of time
      */
     public Office(Clock clock){
 	this.clock = clock;
 
 	for(int i=0; i<teamMeetings.length; i++){
 	    teamMeetings[i] = new CyclicBarrier(4);
 	}
     }
 
     /**
      * Call when employee is doing nothing.
      */
     public void startWorking(){
 	synchronized(workingLock){
 	    try {
 		workingLock.wait();
 	    } catch (InterruptedException e) {
 		e.printStackTrace();
 	    }
 	}
     }
 
     /**
      * Notifies any working employees.
      */
     public void notifyWorking(){
 	synchronized(workingLock){
 	    workingLock.notifyAll();
 	}
     }
 
     /**
      * Tells if the lead is locked on a question
      * @return the current Lead Question Lock
      */
     public Object getLeadQLock(){
 	return leadQLock;
     }
 
     /**
      * Returns the current time in the office simulation
      * @return the current time in milliseconds
      */
     public long getTime(){
 	// system milliseconds
 	long time = clock.getTime();
 	if(clock.getTime() > 6000) time = 0;	// fix minor thread bug
 
 	// simulation total minutes
 	time = time/10;
 
 	// get simulation hours
 	int hours = (int) (time/60);
 
 	// get just minutes (0<=minutes<60)
 	int minutes = (int) time%60;
 
 	// put hours and minutes together
 	time = minutes + (hours * 100) + 800;
 	return time;
     }
 
     /**
      * Converts the office time to a clock time
      * @param officeTime 800 to 1700
      * @return 0 to 5400
      */
     private long officeTimeToClockTime(long officeTime){
 	officeTime -= 800;
 	int hours = (int) (officeTime/100);		// 0 to 9
 	int minutes = (int) (officeTime%100);	// 0 to 60
 
 	// 0 to 540 minutes in the day
 	int totalMinutes = (hours * 60) + minutes;
 
 	// Return milliseconds (0 to 5400 ms)
 	return totalMinutes * 10;
     }
 
     /**
      * Converts the current time to a formatted string
      * @return the time as a string in HH:MM form
      */
     public String getStringTime(){
 	long time = getTime();
 
 	int hours = (int) (time/100);
 	hours = (hours>12) ? hours - 12 : hours;
 
 	String minutes = Integer.toString((int) (time%100));
 	minutes = (Integer.parseInt(minutes) < 10) ? "0" + minutes : minutes;
 
 	return hours + ":" + minutes;
     }
 
 
     // Barrier functions (for meetings)
 
     /**
      * The Barrier waits for all the leads to check in for the meetin
      */
     public void waitForStandupMeeting(){
 	try {
 	    standupMeeting.await();
 	    System.out.println(getStringTime() + " The Standup Meeting Starts");
 	} catch (InterruptedException e1) {
 	    e1.printStackTrace();
 	} catch (BrokenBarrierException e2) {
 	    e2.printStackTrace();
 	}
     }
 
     /**
      * The team meeting Barrier for the chosen team waits for employees
      * @param teamNumber the team holding the meeting
      */
     public void waitForTeamMeeting(int teamNumber){
 	try {
 	    teamMeetings[teamNumber].await();
 	} catch (InterruptedException e1) {
 	    e1.printStackTrace();
 	} catch (BrokenBarrierException e2) {
 	    e2.printStackTrace();
 	}
     }
 
     /**
      * Runs a team meeting for the given team number and employee
      * @param teamNumber the team holding the meeting
      * @param employee the employee participating in the meeting
      */
     public void haveTeamMeeting(int teamNumber, Employee employee) {
 	try {
 	    //We wait for the conference room to be open or contain our team
 	    synchronized(confRoomLock){
		System.out.println("Employee " + employee.getName() + "wants the room");
		System.out.println("The room is used by " + getConfRoomUsedBy());
 		while(getConfRoomUsedBy() != teamNumber){
 		    if(confRoomOpen()){
 			// Fill the room.
 			teamMeetings[teamNumber].reset();
			//fillConfRoom(teamNumber);
			confRoomUsedBy = teamNumber;
			System.out.println(getStringTime() + " conference room accquired by team " 
				+ (int)(teamNumber+1));
			
 		    } else {
 			// Wait until the room is open.
 			confRoomLock.wait();
 		    }
 		}
 
 	    }
 	    
 	    //Increase the used capacity of the conference room
 	    //to show that an employee entered it
 	    confRoom++;
 	    System.out.println(getStringTime() + " " + employee.getEmployeeName() + 
 		    " has entered the conference room.");
 	    
 	    // Synchronize other team members to start the meeting at the same time.
 	    teamMeetings[teamNumber].await();
 	    if(employee.isLead()){
 		System.out.println(getStringTime() +" Team " + (int)(teamNumber + 1) + " starts their team meeting.");
 	    }
 	    // Meeting starts.
 	    Thread.sleep(150);
 	    // Meeting ends.
 	    System.out.println(getStringTime() +" Team " + (int)(teamNumber + 1) + " meeting has ended.");
 	    //Empty the room
 	    emptyConfRoom();		
 	} catch (InterruptedException e1) {
 	    e1.printStackTrace();
 	} catch (BrokenBarrierException e2) {
 	    e2.printStackTrace();
 	}
     }
 
     /**
      * Waits for the end of day meeting to start
      */
     public void waitForEndOfDayMeeting() {
 	try {
 	    endOfDayMeeting.await();
 	} catch (InterruptedException e1) {
 	    e1.printStackTrace();
 	} catch (BrokenBarrierException e2) {
 	    e2.printStackTrace();
 	}
     }
 
     // Getters and setters for Manager and Leads
     
     /**
      * Returns the team's Manager
      * @return the Manager
      */
     public Manager getManager() {
 	return manager;
     }
 
     /**
      * Sets the Manager of the team
      * @param manager the new Manager
      */
     public void setManager(Manager manager) {
 	this.manager = manager;
     }
 
     /**
      * Returns the lead of the given team
      * @param teamNumber the team to look up the lead for
      * @return the lead of that team
      */
     public Employee getLead(int teamNumber) {
 	return leads[teamNumber];
     }
 
     /**
      * Sets the lead of the chosen team
      * @param teamNumber the team to set the lead for
      * @param lead the Employee to lead the team
      */
     public void setLead(int teamNumber, Employee lead) {
 	this.leads[teamNumber] = lead;
     }
 
     /**
      * Returns the different barriers for all the meetings
      * @return the array of all the team meetings
      */
     public CyclicBarrier[] getTeamMeetings(){
 	return teamMeetings;		
     }
 
     /**
      * Adds a new time event to the clock
      * @param time OfficeTime
      */
     public synchronized void addTimeEvent(long time) {
 	clock.addTimeEvent(officeTimeToClockTime(time));
     }
 
     /**
      * Tells if the conference room is empty
      * @return the current open state of the conference room
      */
     public boolean confRoomOpen() {
 	synchronized(confRoomLock){
	    return confRoomUsedBy==-1;
 	}
     }
 
     /**
      * Acquires the lock on the conference room for the given team
      * @param teamNumber the team acquiring the lock
      */
     public void fillConfRoom(int teamNumber) {
	//synchronized(confRoomLock){
 	    confRoomUsedBy = teamNumber;
 	    System.out.println(getStringTime() + " conference room accquired by team " + (int)(teamNumber+1));
	//}
     }
 
     /**
      * Empties the conference room of an employee.
      * If that was the last employee in the conference room, notifies
      * everyone that the conference room is now open.
      */
     public void emptyConfRoom() {
 	synchronized(confRoomLock){
 	    confRoom--;
 
 	    // Only let the last one out notifyAll.
 	    if(confRoom == 0){
 		confRoomUsedBy = -1;
 		confRoomLock.notifyAll();
 	    }
 	}
     }
     
     /**
      * Gets the team using the conference room
      * @return the number of the team using the conference room
      */
     public int getConfRoomUsedBy() {
 	return confRoomUsedBy;
     }
 
     /**
      * Sets the team using the conference room
      * @param confRoomUsedBy the team to use the conference room.
      */
     public void setConfRoomUsedBy(int confRoomUsedBy) {
 	this.confRoomUsedBy = confRoomUsedBy;
     }
 
 }
 

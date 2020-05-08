 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * Threaded employee class 
  * Goes to work, goes to meetings, eats lunch, etc.
  * During work can ask questions of the team lead 
  * @author Ian Salitrynski
  *
  */
 public class Employee extends Thread {
 	
 	/**
 	 * Chance of an employee asking a question during work. 
 	 * (0.0-1.0)
 	 * Is incredibly low as this is checked every simulation minute of work
 	 * Recommend level is between 0.00125 and 0.005
 	 */
 	protected final double chance = 0.0025;
 	
 	/**
 	 * ID in the team.
 	 * 0 is TeamLead
 	 */
 	protected final int ID;
 	
 	/**
 	 * ID of team employee belongs to
 	 */
 	protected final int teamID;
 	
 	/**
 	 * Time employee starts work in simulation minutes
 	 * 0-30 (8:00 - 8:30)
 	 */
 	protected int startTime;
 	
 	/**
 	 * Time employee starts to eat lunch in simulation minutes
 	 * 180-270 (11:00 - 12:30)
 	 */
 	protected int lunchTime;
 	
 	/**
 	 * Random object to get random numbers
 	 */
 	protected final static Random r = new Random();
 	
 	/**
 	 * CDL for starting to run.
 	 */
 	protected final CountDownLatch startcdl;
 	
 	/**
 	 * CDL for the afternoon meeting
 	 */
 	protected final CountDownLatch afternoonMeeting;
 
 	/**
 	 * CDL for when the meeting is over
 	 */
 	protected final CountDownLatch afternoonMeetingOver;
 	
 	/**
 	 * Makes a new employee and sets all given fields. 
 	 * @param id number within the team
 	 * @param teamID team the employee is in
 	 * @param startcdl latch to start all functionality
 	 * @param lastMeeting latch for the end of day meeting
 	 */
 	public Employee(int id, int teamID, CountDownLatch startcdl, 
 			CountDownLatch lastMeeting, CountDownLatch lastMeetingOver) {
 		this.ID = id;
 		this.teamID = teamID;
 		this.startcdl = startcdl;
 		this.afternoonMeeting = lastMeeting;
 		this.afternoonMeetingOver = lastMeetingOver;
 	}
 	
 	/**
 	 * Gets the employee's ID
 	 * Not to be confused with Thread.getId()
 	 * @return ID specific within a team
 	 */
 	public int getID() {
 		return ID;
 	}
 	
 	/**
 	 * Gets the employee's Team ID
 	 * @return ID of their team
 	 */
 	public int getTeamID() {
 		return teamID;
 	}
 	
 	
 	/**
 	 * Runs the employee. Shouldn't be called, use employee.start() instead.
 	 * Employee arrives, goes to team-specific meeting, does work, 
 	 * goes to lunch, does more work, goes to a final meeting, 
 	 * and works a bit more before heading home
 	 */
 	@Override
 	public void run() {	
 		// Employees start from 0-30 minutes in the workday (8:00-8:30)
 		startTime = r.nextInt(30);
 		
 		// Employees start lunch from 11:00 - 12:30
 		lunchTime = 180 + r.nextInt(90);
 		
 		// Wait until time to arrive for work
 		startDay(startTime);
 		
 		// Start and wait at the team-specific meeting for 15 minutes 
 		// using the team meeting latch
 		busyWait(Firm.getLead(teamID).getTeamLatch(), 15);
 		
 		// Work until lunch
 		doWork(lunchTime, true);
 		
 		// Announce lunchtime and wait for lunch
 		busyWait(new CountDownLatch(1), 30, "started eating lunch", 
 				"finished eating lunch");
 		
 		// Work until 4:00
 		doWork(480, true);
 		
 		// Wait for the meeting at the end of the day and wait in that meeting
 		finalMeeting();
 		
 		// Work until the end of the day
 		doWork(startTime + 510, false);
 		
 		// Quit working and go home
 		say("ended work");
 	}
 	
 	/**
 	 * Start the day at a random time
 	 * Blocks until that time on the startcdl
 	 * @param startOffset simulation minutes of time to wait for
 	 */
 	public synchronized void startDay(int startOffset) {
 		startcdl.countDown();
 		try {
 			startcdl.await();
 		} catch (InterruptedException e) {}
 		waitFor(startOffset);
		say("arrived at work");
 		Thread.yield();
 	}
 	
 	/**
 	 * Do some work, could possibly ask a question
 	 * Waits until the nextScheduled event
 	 * @param nextScheduledEvent time of the next thing to do in sim minutes
 	 * @param can questions be asked in this time?
 	 */
 	public void doWork(int nextScheduledEvent, boolean asksQuestion) {
 		while (Firm.getFirmTime().getTimeElapsed() < nextScheduledEvent) {
 			if (hasQuestion(chance)&& asksQuestion) {
 				say("would like to ask a question");
 				askQuestion();
 			} else {
 				try {
 					sleep(5);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Wait on the cdl, announce the message, wait some time, 
 	 * then announce the second message
 	 * Used for lunch/meetings. 
 	 * for example: busyWait(meetinglatch, 15, "starts meeting", "ends meeting")
 	 * @param cdl latch to wait before meeting
 	 * @param time offset waiting for cdl to wait in simulation minutes
 	 * @param message1 to say before waiting (for example "starts eating lunch")
 	 * @param message2 to say after completion of event
 	 */
 	public synchronized void busyWait(CountDownLatch cdl, int time, 
 			String message1, String message2) {
 		cdl.countDown();
 		try {
 			cdl.await();
 			say(message1);
 			waitFor(time);
 			say(message2);
 		} catch (InterruptedException e) {}
 	}
 	
 	/**
 	 * Wait on the cdl, then wait some time. Does not announce anything
 	 * Used for meetins where announcements are elsewhere 
 	 * (like team meetings announced by the team lead)
 	 * @param cdl latch to wait before meeting
 	 * @param time offset waiting for cdl to wait in simulation minutes
 	 */
 	public synchronized void busyWait(CountDownLatch cdl, int time) {
 		cdl.countDown();
 		try {
 			cdl.await();
 			waitFor(time);
 		} catch (InterruptedException e) {}
 	}
 	
 	/**
 	 * Waits for a certain amount of time to pass. 
 	 * @param time offset from current time to wait in simulation minutes
 	 */
 	protected void waitFor(int time) {
 		long initTime = Firm.getFirmTime().getTimeElapsed();
 		while (Firm.getFirmTime().getTimeElapsed() < initTime + time) {
 			yield();
 		}
 	}
 	
 	/**
 	 * Asks a question of the team lead
 	 * locks the employee until the question is answered. 
 	 */
 	protected synchronized void askQuestion() {
 		Firm.getLead(teamID).answerQuestion();
 	}
 	
 	/**
 	 * Finds out if the employee has a question to ask
 	 * @param chance (0.0-1.0) percent chance of question
 	 * @return if employee wants to ask
 	 */
 	protected boolean hasQuestion(double c) {
 		return (r.nextDouble() < c);		
 	}
 	
 	/**
 	 * Prints out a statement with information about the Thread stating it
 	 * @param s action to state without spaces on ends ("started eating lunch")
 	 */
 	protected void say(String s) {
 		if (ID == 0) {
 			System.out.println(Firm.getFirmTime().formatTime() + " Team Lead " 
 					+ (teamID + 1) + " " + s + ".");
 		} else if(ID == -1) {
 			System.out.println(Firm.getFirmTime().formatTime() + 
 					" Project Manager " + s + ".");
 		} else {
 			System.out.println(Firm.getFirmTime().formatTime() + " Developer " 
 					+ (ID + 1) + (teamID + 1) + " " + s + ".");
 		}
 	}
 	
 	/**
 	 * Waits on the afternoon meeting cdl, then sleeps for the meeting
 	 */
 	protected void finalMeeting() {
 		afternoonMeeting.countDown();
 		try {
 			afternoonMeeting.await();
 			sleep(150);
 		} catch (InterruptedException e) {
 		}
 
 		afternoonMeetingOver.countDown();
 		try {
 			afternoonMeetingOver.await();
 
 		} catch (InterruptedException e) {}
 	}
 	
 }

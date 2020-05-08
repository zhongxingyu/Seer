 import java.util.concurrent.CountDownLatch;
 
 /**
  * Threaded Project Manager
  * Goes to work like employees, but also has an extra morning meeting, 
  * executive meetings and can answer questions
  * Cannot ask questions
  * @author Ian Salitrynski
  *
  */
 public class ProjectManager extends Employee {
 	
 	/**
 	 * CDL for the morning meeting with the PM
 	 */
 	private final CountDownLatch morningMeeting;
 	
 	/**
 	 * Makes a new Project Manager and sets all given fields.
 	 * @param startcdl latch to start all functionality
 	 * @param lastMeeting latch for the end of day meeting
 	 * @param firstMeeting latch for the project manager's meeting
 	 */
 	public ProjectManager(CountDownLatch startcdl, CountDownLatch lastMeeting, 
 			CountDownLatch firstMeeting) {
 		super(-1, -1, startcdl, lastMeeting);
 		morningMeeting = firstMeeting;
 	}
 	
 	/**
 	 * Runs the PM. Shouldn't be called, use employee.start() instead.
 	 * PM arrives at 8:00, goes to meeting with Team Leads, does work, 
 	 * goes to an exec meeting, goes to lunch, does more work, goes to a 
 	 * second exec meeting, goes to a final meeting, works more, then goes home.
 	 */
 	@Override
 	public void run() {
 		startTime = 0;		// PM starts at 8:00
 		lunchTime = 240;	// PM eats lunch at 12:00
 		startDayManager();
 		
 		// Start and wait at the Team Lead meeting with the PM for 15 minutes 
 		// using the morning meeting latch
 		busyWait(morningMeeting, 15);
 		
 		// Work until first exec meeting
 		doWork(120, false);
 		
 		// Go to and wait at the first exec meeting
 		busyWait(new CountDownLatch(1), 60, 
 				"went to the first executive meeting", 
 				"finished the first executive meeting");
 		
 		// Work until lunch
 		doWork(lunchTime, false);
 		
 		// Announce lunchtime and wait for extended lunch
 		busyWait(new CountDownLatch(1), 60, "started eating lunch", 
 				"finished eating lunch");
 		
 		// Work until second exec meeting
 		doWork(360, false);
 		
 		// Go to and wait at second exec meeting
 		busyWait(new CountDownLatch(1), 60, 
 				"went to the second executive meeting", 
 				"finished the second executive meeting");
 		
 		// Work until 4:00
 		doWork(480, false);
 		
 		// Wait for the meeting at the end of the day and wait in that meeting
 		finalMeeting();
 		
 		// Work until the end of the day
		doWork(539, false);
 		
 		// Quit working and go home
 		say("ended work");
 	}
 	
 	/**
 	 * Answers a question for a Team Lead
 	 * Causes the current thread to wait for 10 simulation minutes
 	 */
 	public synchronized void answerQuestion() {
 		try {
 			sleep(100);
 			System.out.println("\t\tProject Manager finished answering " +
 					"question for team " + 
 					((Employee) Thread.currentThread()).getTeamID());
 			System.out.println("\t\t" + Firm.getFirmTime().formatTime() + 
 					" Project Manager finished answering question for team " +
 					((Employee) Thread.currentThread()).getTeamID() + 
 					".");
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	/**
 	 * Start the day at a 8:00 after waiting on the startcdl
 	 */
 	private synchronized void startDayManager() {
 		startcdl.countDown();
 		try {
 			startcdl.await();
 		} catch (InterruptedException e) {}
 		say("started work");
 		Thread.yield();
 	}
 	
 	/**
 	 * Waits on the afternoon meeting cdl, then sleeps for the meeting
 	 * Acquires the conference room for the meeting first
 	 */
 	@Override
 	protected synchronized void finalMeeting() {
 		synchronized(Firm.getConferenceRoom()) {
 			afternoonMeeting.countDown();
 			try {
 				afternoonMeeting.await();
 				say("gathers all employees for the final meeting in the " +
 						"conference room");
 				sleep(150);
 				say("lets all employees leave the final meeting");
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 	
 }

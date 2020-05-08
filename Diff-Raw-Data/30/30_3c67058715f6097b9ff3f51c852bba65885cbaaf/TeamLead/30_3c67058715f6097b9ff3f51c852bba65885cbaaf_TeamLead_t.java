 import java.util.concurrent.CountDownLatch;
 
 /**
  * Threaded Team Lead
  * Goes to work like employees
  * Has an extra morning meeting and can answer questions
  * May ask PM questions
  * @author Ian Salitrynski
  *
  */
 public class TeamLead extends Employee {
 	
 	/**
 	 * CDL for the morning meeting with the PM
 	 */
 	private final CountDownLatch morningMeeting;
 	
 	/**
 	 * Team-specific CDL for the team morning meeting
 	 */
 	private final CountDownLatch teamMeeting;
 	
 	/**
 	 * Makes a new Team Lead and sets all given fields. 
 	 * Creates the CDL for the initial team meeting
 	 * @param id number within the team (should be 0)
 	 * @param teamID team the employee is in
 	 * @param startcdl latch to start all functionality
 	 * @param lastMeeting latch for the end of day meeting
 	 * @param firstMeeting latch for the project manager's meeting
 	 */
 	public TeamLead(int id, int teamID, CountDownLatch startcdl, 
 			CountDownLatch lastMeeting, CountDownLatch firstMeeting) {
 		super(id, teamID, startcdl, lastMeeting);
 		morningMeeting = firstMeeting;
 		teamMeeting = new CountDownLatch(Firm.MEMBERS_PER_TEAM);
 	}
 	
 	/**
 	 * gets the CDL for the team-specific morning meeting
 	 * @return CDL for the team morning meeting
 	 */
 	public CountDownLatch getTeamLatch() {
 		return teamMeeting;
 	}
 	
 	/**
 	 * Runs the Team Lead. Shouldn't be called, use employee.start() instead.
 	 * Team Lead arrives, goes to meeting with PM and other leads,
 	 * goes to team-specific meeting, does work, goes to lunch, does more work,
 	 * goes to a final meeting, and works a bit more before heading home.
 	 * Can require the lock on the PM to answer questions, 
 	 * sleeping for several simulation minutes
 	 */
 	@Override
 	public void run() {
 		// Team Leads start from 0-30 minutes in the workday (8:00-8:30)
 		startTime = r.nextInt(30);
 		
 		// Team Leads start lunch from 11:00 - 12:30
 		lunchTime = 180 + r.nextInt(90);
 		
 		// Wait until time to arrive for work
 		startDay(startTime);
 		
 		// Start and wait at the Team Lead meeting with the PM for 15 minutes 
 		// using the morning meeting latch
 		busyWait(morningMeeting, 15);
 		
 		// Start and wait at the team-specific meeting for 15 minutes 
 		// using the team meeting latch, after getting the conference room
 		meeting(teamMeeting, 15, "started the team meeting for team " + 
				teamID, "finished the team meeting for team " + (teamID + 1));
 		
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
 	 * Acquire the conference room, wait on the cdl, then wait that time. 
 	 * Used for meetings.
 	 * @param cdl latch to wait on
 	 * @param time time to wait for in simulation minutes
 	 * @param msg1 to say before waiting (for example "starts team meeting")
 	 * @param msg2 to say after completion of event
 	 */
 	public synchronized void meeting(CountDownLatch cdl, int time, String msg1,
 			String msg2) {
 		synchronized(Firm.getConferenceRoom()) {
 			cdl.countDown();
 			try {
 				cdl.await();
 				say(msg1);
 				waitFor(time);
 				say(msg2);
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	/**
 	 * Answers a question for an employee. 
 	 * If the employee is this Team Lead or by chance the question cannot be 
 	 * answered, the employee(s) head to the PM's office where they might wait
 	 */
 	public synchronized void answerQuestion() {
 		if ((r.nextDouble() < 0.5) && Thread.currentThread() != this) { 
 			// Lead can answer the question
 			System.out.println("\t" + Firm.getFirmTime().formatTime() + 
 					" Team Lead " + (teamID + 1) + 
 					" answered a question for Developer " + 
 					((Employee) Thread.currentThread()).getID() + 
					(((Employee) Thread.currentThread()).getTeamID() + 1) + ".");
 			return;
 		} else { 
 			// Lead cannot answer or is self
 			if (Thread.currentThread() != this) {
 				System.out.println("\t" + Firm.getFirmTime().formatTime() + 
 						" Team Lead " + (teamID + 1) + 
 						" and Developer " + 
 						((Employee) Thread.currentThread()).getID() + 
						(((Employee) Thread.currentThread()).getTeamID() + 1) + 
 						" head to the PM's office.");
 			} else {
 				System.out.println("\t" + Firm.getFirmTime().formatTime() + 
 						" Team Lead " + (teamID + 1) + 
 						" heads to the PM's office.");
 			}
 			Firm.getProjectManager().answerQuestion();
 		}
 	}
 }

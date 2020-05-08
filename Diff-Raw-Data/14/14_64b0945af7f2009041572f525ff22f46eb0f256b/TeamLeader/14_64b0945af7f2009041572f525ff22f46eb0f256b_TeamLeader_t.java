 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * This model describes a Team Leader. It extends Programmer.
  * 
  * @author Yin
  * @author Shawn
  * @author Peter
  */
 public class TeamLeader extends Employee {
 
     /**
      * Availability of the Leader.
      */
     private final AtomicBoolean available;
 
     /**
      * The length of the team leader meetings.
      */
     private final long meetingDuration = 150;
 
     /**
      * The Team Leader's project manager.
      */
     private Manager manager;
 
     /**
      * The team leader team of developers.
      */
     private final HashMap<Developer, Boolean> team = new HashMap<Developer, Boolean>();
 
     /**
      * Default Constructor.
      * 
      * @param manager
      *            - The assigned project manager
      */
     public TeamLeader(Calendar time, List<Developer> developers,
             ConferenceRoom confRoom, String id) {
         super.startTime = time;
         super.name = id;
         this.conferenceRoom = confRoom;
         this.arrived = false;
         for (int i = 0; i < developers.size(); i++) {
             this.team.put(developers.get(i), false);
         }
         this.available = new AtomicBoolean(true);
         this.workingTime = 0l;
         this.lunchTime = 0l;
         this.waitingTime = 0l;
         this.meetingTime = 0l;
     }
 
     /**
      * Sets the manager for this team leader.
      * 
      * @param man
      *            the one project manager for the company
      */
     public void setManager(Manager manager) {
         this.manager = manager;
     }
 
     /**
      * Answers Developer's question.
      */
     public synchronized void answerQuestion() {
         if (available.compareAndSet(true, false)) {
             Random gen = new Random();
             int askMan = gen.nextInt(10);
             if (askMan % 2 == 0) {
                 System.out.println(getTimeInString() + " " + name
                         + " can't answer the question");
                 System.out.println(getTimeInString() + " " + name + " asks "
                         + manager.getEmployeeName() + " the question");
                 long startTime = getTime();
                 manager.answerQuestion();
                 waitingTime += getTime() - startTime;
                 available.set(true);
             } else {
                 System.out.println(getTimeInString() + ":" + name
                         + " answers the question");
                 available.set(true);
             }
         } else {
             System.out.println(getTimeInString() + " " + name
                     + " is not available");
             manager.answerQuestion();
         }
     }
 
     /**
      * Checks to see of all developers in the team have arrived.
      * 
      * @return true if the team has arrived false if the team has not arrived
      */
     private boolean hasTeamArrived() {
         Collection<Boolean> temp = team.values();
         return !temp.contains(false);
     }
 
     /**
      * Registers the arrival of a team member.
      * 
      * @param dev
      *            the developer that has arrived
      * 
      * @throws InterruptedException
      */
     public synchronized void notifyArrival(Developer dev)
             throws InterruptedException {
         team.put(dev, true);
         this.wait();
     }
 
     /**
      * Notifies everyone to proceed.
      */
     public synchronized void notifyEveryone() {
         this.notifyAll();
     }
 
     /**
      * Override for the run method in the Thread class.
      */
     @Override
     public void run() {
         // Each event's starting time
         long eventStartTime;
 
         // Random Number Generator
         Random rand = new Random();
 
         // Whether the employee has had lunch or not
         boolean ateLunch = false;
 
         // Random arrive at Work
         try {
             sleep(rand.nextInt(300));
         } catch (InterruptedException e) {
             System.err.println(e.getMessage());
         }
         arrivalTime = getTime();
         arrived();
         System.out.println(getTimeInString() + " " + name
                 + " arrives at the company");
 
         // Daily 15 minutes morning meeting with manager
         System.out.println(getTimeInString() + " " + name
                 + " knocks on manager's door");
         eventStartTime = getTime();
         manager.notifyArrival(this);
         waitingTime += (getTime() - eventStartTime) - meetingDuration;
         meetingTime += meetingDuration;
 
         // Daily 15 minutes morning meeting with team
         System.out.println(getTimeInString() + " " + name
                 + " waits for team members to arrive");
         while (!hasTeamArrived()) {
             try {
                 sleep(10);
                 waitingTime += 10;
             } catch (InterruptedException e) {
                 System.err.print(e.getMessage());
             }
         }
 
         // Team has arrived and have daily 15 minutes morning meeting
         System.out.println(getTimeInString() + " " + name
                 + " brings team to the conference room");
         eventStartTime = getTime();
         try {
             conferenceRoom.lockRoom();
         } catch (InterruptedException e) {
             System.err.print(e.getMessage());
         } finally {
             notifyEveryone();
         }
         waitingTime += (getTime() - eventStartTime) - meetingDuration;
         meetingTime += meetingDuration;
         System.out.println(getTimeInString() + " " + name
                 + " returns from the standup meeting");
 
         for (Developer developer : team.keySet()) {
             team.put(developer, false);
         }
         boolean hadStatusMeeting = false;
         while (hasArrived()) {
             int task = rand.nextInt(400000);
 
             // Goes to Lunch
             if (!ateLunch) {
                if ((task > 3 && task < 10 && available.compareAndSet(true,
                        false)) || getTime() >= 4200) {
                     System.out.println(getTimeInString() + " " + name
                             + " goes to lunch");
                     try {
                         this.lunchTime = 300 + rand.nextInt(310);
                         sleep(this.lunchTime);
                     } catch (InterruptedException e) {
                         System.err.print(e.getMessage());
                     }
                     available.set(true);
                     ateLunch = true;
                     System.out.println(getTimeInString() + " " + name
                             + " returns from lunch");
                 }
             }
 
             // Ask Questions
            if (task < 1 && available.compareAndSet(true, false)) {
                 System.out.println(getTimeInString() + " " + name + " askes "
                         + manager.getEmployeeName() + " the question");
                 eventStartTime = getTime();
                 manager.answerQuestion();
                 waitingTime += getTime() - eventStartTime;
                 available.set(true);
             }
 
             // Project Status meeting
            if (!hadStatusMeeting && getTime() >= 4800
                    && available.compareAndSet(true, false)) {
                 while (!hasTeamArrived()) {
                     try {
                         sleep(10);
                         waitingTime += 10;
                     } catch (InterruptedException e) {
                         System.err.print(e.getMessage());
                     }
                 }
                 notifyEveryone();
                 System.out.println(getTimeInString() + " " + name
                         + " goes to the project status meeting");
                 try {
                     eventStartTime = getTime();
                     manager.notifyArrival(this);
                     conferenceRoom.projectStatusMeeting();
                 } catch (InterruptedException e) {
                     System.err.print(e.toString());
                 }
                 waitingTime += (getTime() - eventStartTime) - meetingDuration;
                 meetingTime += meetingDuration;
                 hadStatusMeeting = true;
                 System.out.println(getTimeInString() + " " + name
                         + " returns from the status meeting");
                 available.set(true);
             }
 
             // Leave work after 8 hours of work
             if (hadStatusMeeting && getTime() - arrivalTime >= 4800) {
                 if (getTime() < 5400) {
                     try {
                         sleep(rand.nextInt((int) (5400 - getTime())));
                     } catch (InterruptedException e) {
                         System.err.print(e.getMessage());
                     }
                 }
                 officeTime = getTime() - arrivalTime;
                 System.out.println(getTimeInString() + " " + name
                         + " leaves work");
                 left();
             }
 
         } // End While Loop
 
     } // End Run
 }

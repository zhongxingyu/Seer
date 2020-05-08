 import java.util.Calendar;
 import java.util.Random;
 
 /**
  * This model describes the Developer. It extends Programmer.
  * 
  * @author Yin
  * @author Shawn
  * @author Peter
  */
 public class Developer extends Employee {
 
     /**
      * The Developer's Team Leader.
      */
     private TeamLeader leader;
 
     /**
      * Default Constructor.
      * 
      * @param leader
      *            - Assigned Team Leader
      */
     public Developer(Calendar time, String name, ConferenceRoom room) {
         this.lunchTime = 0l;
         this.waitingTime = 0l;
         this.meetingTime = 0l;
         this.workingTime = 0l;
         this.officeTime = 0l;
         this.arrived = false;
         this.name = name;
         this.startTime = time;
         this.conferenceRoom = room;
     }
 
     /**
      * Ask Team Leader or Project Manager question. Question will first go to
      * Team Leader if he/she is available; otherwise, it will go to the Project
      * Manager.
      */
     public void askQuestion() {
         leader.answerQuestion();
     }
 
     /**
      * Set the leader.
      */
     public void setLeader(TeamLeader leader) {
         this.leader = leader;
     }
 
     /**
      * Override for the run method in the Thread class.
      */
     @Override
     public void run() {
         // Random number generator
         Random rand = new Random();
 
         // Each event's starting time
         long eventStartTime;
 
         // Whether the developer has had lunch or not
         Boolean ateLunch = false;
 
         // Arrive work
         try {
             sleep(rand.nextInt(300));
         } catch (InterruptedException e) {
            System.err.println(e.toString());
         }
         arrivalTime = getTime();
         arrived();
         System.out.println(getTimeInString() + " " + name
                 + " arrives at the company");
 
         // Daily 15 minutes morning meeting
         eventStartTime = getTime();
         try {
             leader.notifyArrival(this);
         } catch (InterruptedException e) {
            System.err.println(e.toString());
         }
        meetingTime += getTime() - eventStartTime;
 
         while (hasArrived()) {
 
             // Ask team leader a question.
             int askQuestion = rand.nextInt(300000);
             if (askQuestion == 1) {
                 System.out.println(getTimeInString() + " " + name + " askes "
                         + leader.getEmployeeName() + " a question");
                 eventStartTime = getTime();
                 leader.answerQuestion();
                 waitingTime += getTime() - eventStartTime;
 
             }
 
             // Randomly Goes to Lunch
             if (!ateLunch) {
                 int goToLunch = rand.nextInt(300000);
                 // The developer needs to go to lunch by 3PM=4200ms
                 if (goToLunch == 1 || getTime() >= 4200) {
                     System.out.println(getTimeInString() + " " + name
                             + " goes to lunch");
                     lunchTime = rand.nextInt(300) + 300;
                     try {
                         sleep(lunchTime);
                     } catch (InterruptedException e) {
                        System.err.println(e.toString());
                     }
                     ateLunch = true;
                 }
             }
 
             // Project Status meeting
             if (getTime() >= 4800 && getTime() < 5100) {
                 System.out.println(getTimeInString() + " " + name
                         + " goes to the project status meeting");
                 eventStartTime = getTime();
                 try {
                     leader.notifyArrival(this);
                     conferenceRoom.projectStatusMeeting();
                 } catch (InterruptedException e) {
                    System.err.println(e.toString());
                 }
                meetingTime += getTime() - eventStartTime;
             }
 
             // Leave work after 8 hours of work
             if (getTime() - arrivalTime >= 4800) {
                 if (getTime() < 5400) {
                     try {
                         sleep(rand.nextInt((int) (5400 - getTime())));
                     } catch (InterruptedException e) {
                        System.err.print(e.toString());
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

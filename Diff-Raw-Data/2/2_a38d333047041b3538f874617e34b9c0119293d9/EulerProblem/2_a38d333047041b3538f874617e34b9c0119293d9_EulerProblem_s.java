 package utils.euler;
 
 public abstract class EulerProblem {
     long                 answer;
     int                  runningTime;
 
     protected EulerTimer eulerTimer;
 
     {
         eulerTimer = new EulerTimer();
     }
 
     public static void run(EulerProblem instance) {
         // Actually do the problem
         instance.setAnswer(instance.doProblemSpecificStuff());
         System.out.println("Answer: " + instance.getAnswer());
 
        // See if the problem was completed in one minute
         instance.setRunningTime(instance.getEulerTime());
         System.out.println("--------------------------");
         System.out.println("Running time: " + instance.getRunningTime() + " seconds");
     }
 
     protected abstract long doProblemSpecificStuff();
 
     protected int getEulerTime() {
         return eulerTimer.getRunningTime();
     }
 
     public long getAnswer() {
         return answer;
     }
 
     protected void setAnswer(long answerIn) {
         answer = answerIn;
     }
 
     public int getRunningTime() {
         return runningTime;
     }
 
     protected void setRunningTime(int runningTimeIn) {
         runningTime = runningTimeIn;
     }
 }

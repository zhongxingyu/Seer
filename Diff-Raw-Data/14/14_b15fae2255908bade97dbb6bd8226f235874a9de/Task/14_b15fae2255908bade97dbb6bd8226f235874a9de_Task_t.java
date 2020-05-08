 package vysichart;
 
 import java.util.Calendar;
 import java.util.ArrayList;
 
 /**
  *
  * @author UP619902, OtherID1, OtherID2
  */
 public class Task {
 
     private String taskName, taskNumber;
     //The level of the task in the tree (the parent's level plus one).
     private int taskLevel;
     private Task taskParent;
     //An array of all nodes that need to be done before this task.
     private ArrayList<Task> dependentNodes;
     private ArrayList<Task> children; // tasks children
     private Calendar startCalendar, endCalendar, lateStart, lateEnd;
     private long taskDuration, taskSlack;
     private boolean taskIsComplete;
 
     public Task() {
         dependentNodes = new ArrayList<>(); // init
         children = new ArrayList<>(); // init
         taskIsComplete = false; // defaults
     } //Default constructor.
 
     public Task(String taskName) {
         this.taskName = taskName;
         dependentNodes = new ArrayList<>();
         children = new ArrayList<>();
         taskIsComplete = false; // defaults
         this.taskParent = null;
     }
     /*
      * FOR TEST PURPOSES ONLY
      */
 
     public Task(String taskName, Calendar startCalendar, Calendar endCalendar) {
         this.taskName = taskName;
         dependentNodes = new ArrayList<>();
         children = new ArrayList<>();
         this.startCalendar = startCalendar;
         this.endCalendar = endCalendar;
         taskDuration = calculateDuration();
         this.taskParent = null;
         addToTasks();
     }
 
     public Task(String taskName, Task taskParent) {
         this.taskName = taskName;
 
         this.taskParent = taskParent;
         initParent(); // adds THIS as child to parent
 
         dependentNodes = new ArrayList<>();
         children = new ArrayList<>();
 
         taskIsComplete = false; // defaults
         initParent();
         addToTasks();
     }
 
     public Task(String taskName, Task taskParent,
             Calendar startCalendar, Calendar endCalendar) {
 
         this.taskName = taskName;
         this.taskParent = taskParent;
         this.dependentNodes = new ArrayList<>(); // init
         this.children = new ArrayList<>(); // init
         this.startCalendar = startCalendar;
         this.endCalendar = endCalendar;
         taskDuration = calculateDuration();
 
         taskIsComplete = false; // defaults
 
         initParent(); // adds THIS as child to parent
         addToTasks(); //adds THIS to the main Task list in Project.
     }
 
     public Task(String taskName, Task taskParent, ArrayList<Task> dependentNodes,
             Calendar startCalendar, Calendar endCalendar) {
 
         this.taskName = taskName;
         this.taskParent = taskParent;
         this.dependentNodes = dependentNodes; // no need to init
         this.startCalendar = startCalendar;
         this.endCalendar = endCalendar;
         taskDuration = calculateDuration();
         this.children = new ArrayList<>();
 
         initParent(); // adds THIS as child to parent
         addToTasks();
     }
 
     private void initParent() {
         this.taskParent.addChild(this); // fixes leakage
     }
     /*
      * Adds to project task list upon construction.
      */
 
     private void addToTasks() {
         if (Project.getTasks().isEmpty()){
             Project.setRoot(this);
         }
         Project.addTask(this);
     }
 
     //Get methods for retrieving variable data
     public String getName() {
         return taskName;
     }
 
     public String getTaskNumber() {
         return taskNumber;
     }
 
     public int getTaskLevel() {
         return taskLevel;
     }
 
     public Task getTaskParent() {
         return taskParent;
     }
 
     public ArrayList<Task> getDependentNodes() {
         return dependentNodes;
     }
 
     public ArrayList<Task> getChildren() {
         return children;
     }
 
     public Calendar getStartCalendar() {
         return startCalendar;
     }
 
     public Calendar getEndCalendar() {
         return endCalendar;
     }
 
     public Calendar getLateStart() {
         return lateStart;
     }
 
     public Calendar getLateEnd() {
         return lateEnd;
     }
 
     public long getTaskDuration() {
        return (this.getEndCalendar().getTimeInMillis()) - (this.getStartCalendar().getTimeInMillis());
     }
     
     public long getTaskStartCalendarToMillisecond(){
         return calendarToMillisecond(true);
     }
     
     public long getTaskEndCalendarToMillisecond(){
         return calendarToMillisecond(false);
     }
 
     //set methods for modifying class variables.
     public void setTaskName(String taskName) {
         this.taskName = taskName;
     }
 
     public void setTaskNumber(String taskNumber) {
         this.taskNumber = taskNumber;
     }
 
     /*Remove if not needed*/
     public void setTaskParent(Task taskParent) {
         this.taskParent = taskParent;
         this.taskParent.addChild(this); // adds THIS as child to parent
     }
 
     public void setTaskDuration(int taskDuration) {
         this.taskDuration = taskDuration;
     }
 
     public void setTaskSlack(int taskSlack) {
         this.taskSlack = taskSlack;
     }
 
     public void setTaskIsComplete(boolean taskIsComplete) {
         this.taskIsComplete = taskIsComplete;
     }
 
     public void setStartCalendar(Calendar startCalendar) {
         this.startCalendar = startCalendar;
     }
 
     public void setEndCalendar(Calendar endCalendar) {
         this.endCalendar = endCalendar;
     }
 
     //--- 'Utility' methods ---
     public void printOut() { // just a console printout for debugging
         System.out.println("Task Name: " + taskName);
         System.out.println("Duration(ms): " + calculateDuration());
         if (taskParent != null) {
             System.out.println("Parent: " + taskParent.getName());
         } else {
             System.out.println("No Parent");
         }
         System.out.println("Depedent Nodes:");
         if (dependentNodes.isEmpty()) {
             System.out.println(" None");
         } else {
             for (Task currentTask : dependentNodes) {
                 System.out.println(" " + currentTask.getName());
             }
         }
         System.out.println("Children:");
         if (children.isEmpty()) {
             System.out.println(" None");
         } else {
             for (Task currentTask : children) {
                 System.out.println(" " + currentTask.getName());
             }
         }
     }
 
     public void addDependantNode(Task task) {
         dependentNodes.add(task); // adds a node to the dependant node array
     }
 
     public void removeDependentNode(Task taskToRemove) {
         if (isDependentNode(taskToRemove)) {
             dependentNodes.remove(taskToRemove); // removes a node from the dependant node aray
         }
     }
 
     public boolean isDependentNode(Task taskToCheck) {
         return dependentNodes.contains(taskToCheck);
     }
     
     public void addChild(Task task) {
         children.add(task); // adds a node to the child array
     }
 
     public void removeChild(Task taskToRemove) {
         children.remove(taskToRemove); // removes a child from the child array
     }
 
     public void changeParent(Task newParent) {
         // 'cut ties' with old parent
         this.taskParent.removeChild(this);
         // set new parent
         setTaskParent(newParent);
         //setTaskParent automatically adds THIS as child
     }
 
     public boolean isSibling(Task taskToCheck) {
         try {
             Task parent = this.getTaskParent();
             ArrayList<Task> siblings = parent.getChildren();
             if (siblings.contains(taskToCheck)) {
                 return true;
             }
             return false;
         } catch (Exception e) {
             return false; // for root node, has no parent
         }
     }
     /*
      * Returns a calendar's millisecond value, starting at the Unix Epoch
      * (01/01/1970).
      */
     public long calendarToMillisecond(boolean isStart) {
         Calendar date;
         if(isStart){
             date = startCalendar;
         }
         else{
             date = endCalendar;
         }
         
         long year = date.get(Calendar.YEAR) * 31556952000L;
         long day = date.get(Calendar.DAY_OF_YEAR) * 86400000;
         long hour = date.get(Calendar.HOUR_OF_DAY) * 3600000;
         long minute = date.get(Calendar.MINUTE) * 60000;
         long second = date.get(Calendar.SECOND) * 1000;
         //System.out.println("cTM: " + (year + day + hour + minute + second));
         return (year + day + hour + minute + second);
     }
     /* This method converts back to other date formats.
      * Useful for pipelining conversions to other methods which display
      * timeframes to users.
      */
 
     private int millisecondToOtherFormat(long duration, String conversionSwitch) {
         switch (conversionSwitch) {
             case ("year"):
                 return (int) (duration / 31556952000L);
             case ("month"):
                 return (int) (duration / 2629746000L);
             case ("week"):
                 return (int) (duration / 604800000);
             case ("day"):
                 return (int) (duration / 86400000);
             case ("hour"):
                 return (int) (duration / 3600000);
             case ("minute"):
                 return (int) (duration / 60000);
             default:
                 return (int) (duration / 1000); //returns in seconds
         }
     }
     //Everything works flawlessly, except the months value - it's miscalculating!
 
     public int[] getTotalTime() {
         int[] totalTime = new int[7];
         long currentDuration = taskDuration;
         String[] timeFrameNames = {"year", "month", "week", "day", "hour",
             "minute", "second"};
         long[] timeFrames = {31556952000L, 2629746000L, 604800000, 86400000,
             3600000, 60000, 1000};
         for (int i = 0; i < timeFrameNames.length; i++) {
             totalTime[i] = millisecondToOtherFormat(currentDuration, timeFrameNames[i]);
             if (totalTime[i] != 0) {
                 currentDuration -= (totalTime[i] * timeFrames[i]);
             }
         }
         return totalTime;
     }
 
     private long calculateDuration() {
         return calendarToMillisecond(false)
                 - calendarToMillisecond(true);
     }
 
     public String getString() {
         // string interpretation of task
         //taskname, taskNumber already string
         //String idStr = String.valueOf(taskId);
         //String levelStr = String.valueOf(taskLevel);
         // store parent ID
         //String parentStr = String.valueOf(taskParent.getTaskId());
         //String dependentStr = "";
         //for (Task currentTask : dependentNodes){
         // dependentStr += String.valueOf(currentTask.getTaskId()); // stores IDs
         // dependentStr += " "; // seperated by spaces
         //}
 
         //String childStr = "";
         //for (Task currentTask : children){
         // childStr += String.valueOf(currentTask.getTaskId()); // stores IDs
         // childStr += " "; // seperated by spaces
         //}
 
         //String strtStr = String.valueOf(startCalendar);
         //String endStr = String.valueOf(endCalendar);
         //String lateStartStr = String.valueOf(lateStart);
         //String lateEndStr = String.valueOf(lateEnd);
 
         //String durationStr = String.valueOf(taskDuration);
         //String slackStr = String.valueOf(taskSlack);
 
         //String completeStr = String.valueOf(taskIsComplete);
 
 
         //String str = taskName + "\n" + taskNumber + "\n" + idStr + "\n" + levelStr +
         // "\n" + parentStr + "\n" + dependentStr + "\n" + childStr + "\n" + strtStr +
         // "\n" + endStr + "\n" + lateStartStr + "\n" + lateEndStr + "\n" +
         // durationStr + "\n" + slackStr + "\n" + completeStr;
 
         String str = taskName;
         return str;
     }
 }

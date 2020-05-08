 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vysichart;
 
 /**
  *
  * @author Harry, Todd
  */
 import java.util.ArrayList;
 
 public class Project {
 
     private static ArrayList<Task> tasks; //ALL the tasks, they're fed through to Chart
     private static Chart gantt;//    \
     private static Chart pert;//     |  ALL the charts
     private static Chart wbt;//      /
     private String name; //name of project
     private String filePath; // file path for save/load
     private float timeFrame; //total timeframe (in hrs), calculated from tasks (used to be in Chart)
     private long startProject, endProject; //start and end times of project
     private static Task root;
 
     public Project() {
         // default constructor
     }
 
     public Project(String name, String filePath) {
         tasks = new ArrayList<>(); // init
         gantt = new Gantt(tasks, timeFrame); // init  \
         pert = new PERT(tasks); // init    | all have no tasks, they all start empty
         wbt = new WBT(tasks); // init  /
 
 
         this.name = name; // read in
         this.filePath = filePath; // read in
 
         timeFrame = 0; // 0 tasks = 0 timeFrame
 
     }
     
     public static void setRoot(Task task){
         root = task;
     }
     
     public static Task getRoot(){
         return root;
     }
 
     // --- accessors ---
     public static ArrayList<Task> getTasks() {
         return tasks;
     }
 
     public Task[] getTasksAsArray() {
         // returns an Array of tasks
         Task[] t = {};
         t = tasks.toArray(t);
         return t;
     }
 
     public String[] taskListToArray(ArrayList<Task> taskList) {
         // generic, converts any arrayList into string[]
         ArrayList<String> taskStringAL = new ArrayList<String>();
         for (Task current : taskList) {
             taskStringAL.add(current.getName());
         }
         String[] taskStringA = {};
         taskStringA = taskStringAL.toArray(taskStringA);
         return taskStringA;
     }
 
     public ArrayList<Task> getDependantTasks(Task task) {
         return task.getDependentNodes();
     }
 
     public ArrayList<Task> getPlausableDependantTasks(Task task) {
         ArrayList<Task> tasksToReturn = new ArrayList<Task>();
         ArrayList<Task> dependantNodes = task.getDependentNodes();
         for (Task currentTask : tasks) {
             if (task.isSibling(currentTask)) {
                 // task is subling
                 if (dependantNodes.contains(currentTask) == false) {
                     // not currently a dependant node
                     if ((task == currentTask) == false) {
                         // if not self
                         tasksToReturn.add(currentTask);
                     }
                 }
 
             }
         }
         return tasksToReturn;
     }
 
     public String[] getTasksAsStringArray() {
         // retuns tasks as an array of strings
         ArrayList<String> taskStringAL = new ArrayList<String>();
         for (Task current : tasks) {
             taskStringAL.add(current.getName());
         }
         String[] taskStringA = {};
         taskStringA = taskStringAL.toArray(taskStringA);
         return taskStringA;
     }
 
     public String[] getTasksAsStringArray(String message) {
         // same name, different method
         // appends the message to the beggininng
 
         ArrayList<String> finalAL = new ArrayList<String>();
         finalAL.add(message); // first element
 
         ArrayList<String> taskStringAL = new ArrayList<String>();
         for (Task current : tasks) {
             taskStringAL.add(current.getName());
         }
 
         finalAL.addAll(taskStringAL); // add new
 
         String[] taskStringA = {};
         taskStringA = finalAL.toArray(taskStringA);
 
         return taskStringA;
 
     }
     
     public boolean isTaskFromString(String taskName){
         for (Task current : tasks){
             if (current.getName().equals(taskName)){
                 return true;
             }
         }
         return false;
     }
     
     public Task getTaskFromString(String taskName){
         for (Task current : tasks){
             if (current.getName().equals(taskName)){
                 return current;
             }
         }
         return new Task("NULL"); // return NULL (invalid) task
     }
 
     public Chart getGantt() {
         return gantt;
     }
 
     public Chart getPERT() {
         return pert;
     }
 
     public Chart getWBT() {
         return wbt;
     }
 
     public String getName() {
         return name;
     }
 
     public String getFilePath() {
         return filePath;
     }
 
     public float getTimeFrame() {
         return timeFrame; // no need for mutator, is calculated from tasks
     }
 
     // --- mutators ---
     public void setTasks(ArrayList<Task> tasks) {
         this.tasks = tasks;
     }
 
     public void setGantt(Gantt gantt) {
         this.gantt = gantt; // may be useful for templating
     }
 
     public void setPERT(PERT pert) {
         this.pert = pert;
     }
 
     public void setWBT(WBT wbt) {
         this.wbt = wbt;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setFilePath(String filePath) {
         this.filePath = filePath;
     }
 
     //--- 'Utility' methods ---
     public static void addTask(Task newTask) {
         // not only adds newTask, but also all it's children
         tasks.add(newTask);
         if (newTask.getChildren() != null) {
             for (Task currentTask : newTask.getChildren()) {
                 addTask(currentTask);
             }
         }
         wbt.setTasks(tasks); // updates wbt
         gantt.setTasks(tasks); // updates pert
         pert.setTasks(tasks); // updates gantt
     }
 
     public void printOut() { // print out function
         System.out.println("Project Name: " + name);
         System.out.println("File Path: " + filePath);
         System.out.println("-------------------");
         if (tasks.isEmpty()) {
             System.out.println("Currently No Tasks");
         } else {
             System.out.println("---Task Overview---");
             for (int i = 0; i < tasks.size(); i++) {
                 tasks.get(i).printOut();
                 System.out.println("-------------");
             }
         }
 
         System.out.println("-------------------");
     }
 
     public void calculateTimeFrame() {
         // get earliest start date
         // get latest end date
         // timeFrame = earliestStart - latestEnd
         // update class 
     }
 
     public String getString() {
         // String representation of the project
         String tasksStr = "";
 
         for (Task current : tasks) {
             System.out.println(current.getName());
 
             tasksStr += current.getString();
             tasksStr += "\n"; // net line between each
         }
 
 
         //String ganttStr = gantt.getString();
         //String pertStr = pert.getString();
         //String wbtStr = wbt.getString();
 
         // name is already a string
         // filePath is alreadt a string
 
         String timeFrameStr = String.valueOf(timeFrame);
 
 
 
         String str = tasksStr + "\n" + name + "\n" + filePath + "\n" + timeFrameStr;
 
         return str;
     }
 
     public void saveToFile() {
         // save everything to the filePath
         // tasks saves as full tasks
     }
 
     //Needs fixing - inaccurate due to counting children wrong
     public static float getTaskPercentage(Task task) {
         return ((float) task.getTaskDuration() / (float) getProjectDuration()) * 100;
     }
     
     //retutns the percentage of given whitespace time in the project.
     public static float getWhitespacePercentage(long duration){
         return ((float)duration / (float) getProjectDuration()) * 100;
     }
     /*
      * Overloaded method - takes tasks and returns a long value.
      */
     public static long getTimeDifference(Task firstTask, Task secondTask){
         return secondTask.calendarToMillisecond(true) -
                 firstTask.calendarToMillisecond(false);
     }
     /*
      * Overloaded method - takes long values instead of tasks.
      */
     public static long getTimeDifference(long firstTask, long secondTask){
         return secondTask - firstTask;
     }
     /*
      * Returns the total duration of the project.
      *
      */
     public static long getProjectDuration(){
         Task task = tasks.get(0);
         long taskDuration = (task.getEndCalendar().getTimeInMillis()) - (task.getStartCalendar().getTimeInMillis());
         return taskDuration;
     }
 
     /*
      * Finds the project start/time.
      * Used when rendering gantt charts.
      * Needs redoing.
      */
     public static Task findProjectStart() {
         Task currentTask = null;
         ArrayList<Task> projectTasks = tasks;
         projectTasks.remove(0);
         for(Task t : projectTasks){
            if(currentTask == null){
                currentTask = t;
            }
            if(t.getTaskStartCalendarToMillisecond() < currentTask.getTaskStartCalendarToMillisecond()){
                currentTask = t;
            }
         }
         return currentTask;
     }
     /*
      * Finds the project end time.
      */
     public static Task findProjectEnd(){
         Task currentTask = null;
         ArrayList<Task> projectTasks = tasks;
         projectTasks.remove(0);
         for(Task t : projectTasks){
             if(currentTask == null || t.getTaskEndCalendarToMillisecond() > currentTask.getTaskEndCalendarToMillisecond()){
                 currentTask = t;
             }
         }
         return currentTask;
     }
     
     public static ArrayList<Task> getParents(){
         ArrayList<Task> parents = new ArrayList<>();
         for(Task t : tasks){
             if(t.getTaskParent() == null){
                 parents.add(t);
             }
         }
         return parents;
     }
 
     /*
      * Counts the number of parents of a specific task - 
      * is this redundant?
      */
     public static int countParents(Task task) {
         int parentCount = 0;
         if (task.getTaskParent() != null) {
             parentCount++;
             parentCount += countParents(task.getTaskParent());
         }
         return parentCount;
     }
     /*
      * Calculates the number of levels in the project.
      */
 
     public static int calculateLevels() {
         int previousLevelCount = 0;
         for (int i = 0; i < tasks.size(); i++) {
             Task currentTask = tasks.get(i);
             if (currentTask.getChildren() != null) { //skips those without children 
                 int currentLevelCount = countParents(tasks.get(i));
                 if (currentLevelCount > previousLevelCount) {
                     previousLevelCount = currentLevelCount;
                 }
             }
         }
         return previousLevelCount + 1; //previous levels + current level
     }
 
     public int getNumberOfTasks() {
         return tasks.size();
     }
 
     private void removeFromTasks(Task taskToRemove) {
         // delete a task, recurse through it's children and delete them
 
 
         for (Task currentTask : taskToRemove.getChildren()) {
             // recursivley delete children
             removeFromTasks(currentTask);
         }
 
 
         // possibly go through all nodes that depend, and delete this task from their dependantNodes arrays
 
         tasks.remove(taskToRemove); // do the deleting
 
     }
 
     public void removeFromParent(Task taskToRemove) {
         // cut ties with parent
 
         // this method throws errors! (sometimes)
 
         Task taskParent = taskToRemove.getTaskParent();
 
         for (Task currentTask : taskParent.getChildren()) { // error happens here
             if (currentTask == taskToRemove) {
                 taskParent.getChildren().remove(taskToRemove);
                 break; // break out of loop, if don't break loop then concurrency error
                 // *loops round tasks*, *deletes task*, 'wait, my loop material has changed, better throw a concurrency error'
             }
         }
     }
 
     public void deleteTask(Task taskToRemove) {
         removeFromParent(taskToRemove);
         removeFromTasks(taskToRemove);
     }
     
     public ArrayList<Task> getNodesThatDepend(Task t){
         ArrayList<Task> nodesThatDepend = new ArrayList<Task>();
         for (Task current : tasks){
             if (current.getDependentNodes().contains(t)){
                 nodesThatDepend.add(current);
             }
         }
         return nodesThatDepend;
     }
 }

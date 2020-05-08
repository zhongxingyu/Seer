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
 
     private ArrayList<Task> tasks; //ALL the tasks, they're fed through to Chart
     private Chart gantt;//    \
     private Chart pert;//     |  ALL the charts
     private Chart wbt;//      /
     private String name; //name of project
     private String filePath; // file path for save/load
     private float timeFrame; //total timeframe (in hrs), calculated from tasks (used to be in Chart)
 
     public Project() {
         // default constructor
     }
 
     public Project(String name, String filePath) {
         tasks = new ArrayList<>(); // init
         gantt = new Gantt(tasks, timeFrame); // init  \
         pert = new PERT(tasks); // init    | all have no tasks, they all start empty
         wbt = new WBT(tasks); // init      /
 
         this.name = name; // read in
         this.filePath = filePath; // read in
 
         timeFrame = 0; // 0 tasks = 0 timeFrame
 
     }
 
     // --- accessors ---
     public ArrayList<Task> getTasks() {
         return tasks;
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
     public void addTask(Task newTask) {
         // not only adds newTask, but also all it's children
         tasks.add(newTask);
         for (Task currentTask : newTask.getChildren()) {
             addTask(currentTask);
         }
     }
 
     public void printOut() { // print out function
         System.out.println("Project Name: " + name);
         System.out.println("File Path: " + filePath);
         System.out.println("-------------------");
         if (tasks.isEmpty()) {
             System.out.println("Currently No Tasks");
         } else {
             System.out.println("---Task Overview---");
            for (Task currentTask : tasks) {
                currentTask.printOut();
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
     
     public float getTaskPercentage(Task task){
         long allTaskDuration = 0;
        for(Task t : tasks){
            allTaskDuration =+ t.getTaskDuration();
         }
         return (float)((task.getTaskDuration() / allTaskDuration) * 100);
     }
 }

 package itu.dk.smds.e2012.common;
 import java.util.ArrayList;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 /**
  * The class responsible for the task manager data
  */
 @XmlRootElement(name = "cal")
 public class Cal {
     @XmlElementWrapper(name = "users")
     @XmlElement(name = "user")
     public ArrayList<User> users;
     
     @XmlElementWrapper(name = "tasks")
     @XmlElement(name = "task")
     public ArrayList<Task> tasks;
     
     static Cal currentCal;
     
     /**
      * Constructor creating the lists of users and tasks.
      */
     private Cal(){
         //users = new ArrayList<User>();
         //tasks = new ArrayList<Task>();
     }
     
     /**
      * Method for adding a new user
      * @param u, the user to be added to the task manager system
      */
     public void addUser(User u){
         users.add(u);
     }
     /**
      * Method for adding a new task
      * @param t, the task to be added to the task manager system 
      */
     public void addTask(Task t){
         tasks.add(t);
     }
     
     /**
      * @return A list of the tasks in the current Cal object
      */
     public ArrayList<Task> GET(){
         return tasks;
     }
     
     /**
      * 
      * @param id the String of the task to be returned
      * @return a Task object if found. Returns null otherwise.
      */
     public Task GET(String id){
         for(int i = 0; i < tasks.size(); i++)
         {
             if(tasks.get(i).id == id)
             {
                 return tasks.get(i);
             }
         }
         return null;
     }
     
     /**
      * Adds a new task to the task list.
      * @param t, the task to be added.
      */
     public void POST(Task t){
         addTask(t); //In case we ever change anything
         writeToXml();
     }
     
     /**
      * Updates the task with the specific id given.
      * @param t, the task to be changed.
      */
     public void PUT(Task t){
         for(int i = 0; i < tasks.size(); i++)
         {
             if(tasks.get(i).id == t.id)
             {
                synchronized(tasks.get(i)) {
                    tasks.remove(i);
                    tasks.add(t);
                    break;
                }
             }
         }
         writeToXml();
     }
     
     /**
      * Deletes the specific task.
      * @param t, the task to be deleted.
      */
     public void DELETE(Task t) {
         tasks.remove(t);
         writeToXml();
     }
     
     public void DELETE(String id){
         for(int i = 0; i < tasks.size(); i++)
         {
             if(tasks.get(i).id == id)
             {
                 tasks.remove(i);
                 break;
             }
         }
         writeToXml();
     }
     
     /**
      * Writes the current Cal objekt to the XML file.
      */
     private void writeToXml() {
         try {
             CalSerializer.makeXmlFile(this);
         }
         catch (Exception e) {
             System.out.println("Error - Could not write to XML: " + e);
         }
     }
     
     /**
      * Deletes all current task
      */
     public void deleteAllTask(){
         tasks = new ArrayList<Task>();
     }
 }

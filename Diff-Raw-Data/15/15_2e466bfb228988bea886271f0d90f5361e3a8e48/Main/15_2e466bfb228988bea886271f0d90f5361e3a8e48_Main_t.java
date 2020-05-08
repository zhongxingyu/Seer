 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package dealwithcalendar;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  *
  * @author Deal With It Productions
  */
 public class Main {
     private dwiCalendar currentCalendar;
     private HashMap<Integer, Course> courses;
     private String address;
     private String calendarName = "mycalendar.cld";
     private String courseName = "mycourses.crs";
     /**
      * @param args the command line arguments
      */
 
     public Main () throws IOException {
         // this constructor is TIMANTTIA
         File f = new File(calendarName);
         if (f.exists()) {
             //FileOperations.startRead(calendarName);
             currentCalendar = FileOperations.readDWICalendar(calendarName);
             //courses = new HashMap<Integer, Course>();
         }
         else {
             currentCalendar = new dwiCalendar(2011);
         }
 
         f = new File(courseName);
         if (f.exists()) {
             
             courses = FileOperations.readCourses(courseName);
             //courses = new HashMap<Integer, Course>();
         }
         else {
             courses = new HashMap<Integer, Course>();
         }
 
 	address = "http://www.cs.helsinki.fi/u/tkairi/rajapinta/courses.json";
 	network.Fetcher fetcher = new network.Fetcher(address);
         ArrayList<Course> remoteCourses = fetcher.read();
         addCourses(remoteCourses);
 
 
     }
 
 
     public static void main(String[] args) throws IOException {
         Main main = new Main();
         GUI gui = new GUI(main);
 
         
     }
 
 
 
     public void writeData() throws IOException{
         FileOperations.writeCalendar(currentCalendar, calendarName);
         FileOperations.writeCourseEvents(courses, courseName);
     }
 
     public Course getCourse(int courseID){
         return courses.get(courseID);
     }
     public boolean createEventsToCalendar(int id){
         Course source = courses.get(id);
         ArrayList<Event> generated = source.generateEvents();
         if(generated == null)
             return false;
         Iterator<Event> eventIterator = generated.iterator();
         while(eventIterator.hasNext()){
             Event current = eventIterator.next();
             currentCalendar.addEvent(current);
         }
         return true;
     }
 
     /**
      * Tools to handle single events and to create one
      */
      public void addEvent(Calendar starttime, Calendar endtime, int courseID,
                           String location, String name, boolean attending){
         Event e = new Event(starttime, endtime, location, name, courseID);
         currentCalendar.addEvent(e);
     }
 
     public void addEvent(Event e) {
         currentCalendar.addEvent(e);
     }
 
     public void removeEvent(Event e) {
         currentCalendar.removeEvents(e);
     }
 
     public void changeEventStarttime(Event e, Calendar starttime){
         e.setStarttime(starttime);
     }
 
 
     public void changeEventEndtime(Event e, Calendar endtime){
         e.setEndtime(endtime);
     }
 
     public void changeEventLocation(Event e, String location){
         e.setLocation(location);
     }
 
     public void changeEventName(Event e, String name){
         e.setLocation(name);
     }
 
     public void changeEventAttending(Event e, boolean attending){
         e.setAttending(attending);
     }
 
     public void changeEventOwnMarkings(Event e, String om) {
         e.setOwnMarkings(om);
     }
 
 
     /**
      * Tools to handle single course or create one
      */
     public int addCourse(Calendar starttime, Calendar endtime, String name,
                           int credits){
         int ID = getNextFreeID();
         Course c = new Course(starttime, endtime, name, ID, credits);
         courses.put(ID, c);
         return ID;
     }
 
     public void addCourses(ArrayList<Course> courseArray){
         Iterator<Course> courseIterator = courseArray.iterator();
         while(courseIterator.hasNext()){
             Course current = courseIterator.next();
             int ID = getNextFreeID();
             current.setId(ID);
             courses.put(ID, current);
         }
     }
 
     public void addCourseEvent(Course id, int type, Calendar time, int weekday, int duration,
                                 String location){
         id.addCourseEvent(type, time, weekday, duration, location);
 
     }
 
      public void addCourseExam(Course id, Calendar _testDate, String _location, int duration){
         id.addExam(_testDate, _location, duration);
     }
 
     public void changeCourseStart(int id, Calendar starttime){
         courses.get(id).setStart(starttime);
     }
 
     public void changeCourseEnd(int id, Calendar endtime){
         courses.get(id).setStart(endtime);
     }
 
     public void changeCourseName(int id, String name){
         courses.get(id).setName(name);
     }
 
     public void changeCourseCreditpoints(int id, int cr){
         courses.get(id).setCr(cr);
     }
 
     public HashMap<Integer, Course> getCourses(){
         return courses;
     }
 
     public Course getACourse(int id){
         return courses.get(id);
     }
 //
 //     Likely useless, thus commented out ATM
 //
 //     public void changeCourseId(int id, int ident){
 //         courses.get(id).setCr(ident);
 //     }
 
     public void deleteCourseEvents(int courseID){
         currentCalendar.removeEvents(courseID);
         courses.get(courseID).deleteAllCourseEvents();
     }
 
     public ArrayList<Event> getWeek(int year, int week){
         return currentCalendar.getEventsOfWeek(year, week);
     }
 
     public ArrayList<Event> getCalendar(){
         return currentCalendar.getEvents();
     }
 
     public ArrayList<Event> getWeek(Calendar date){
         return currentCalendar.getEventsOfWeek(date);
     }
 
     private int getNextFreeID(){
         if (courses == null) return 0;
 
         int ID = 0;
         while(true){
             if(!courses.containsKey(ID)){
                 break;
             }
             ID++;
         }
         return ID;
     }
 
     public void writeWeekEventList (int year, int week, String filename){
         FileOperations.writeWeek(getWeek(year, week), filename);
     }
 
 
 }
 
 
 
 /*
  * TODO: GUI wants atleast these dummies
  *
  *
  *
  *
  void deleteCourseEvents(int courseID)
  * delete all courseEvents from course and created events
  * from calendar
 
 
  getWeek(int weekN, int yearN)
 return ArrayList<Event>
 
 getWeek(Calendar date) // palauttaa viikon jolle pvm sijoittuu
 return ArrayList<Event>
 
  */

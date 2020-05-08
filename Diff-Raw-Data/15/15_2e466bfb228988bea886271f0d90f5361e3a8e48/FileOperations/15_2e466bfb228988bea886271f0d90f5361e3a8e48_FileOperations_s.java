 package dealwithcalendar;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /**
  * Functions to read and write the calendar in use to a file
  *
  */
 public class FileOperations {
 
     static OutputStream file, buffer;
     static ObjectOutput output;
     static InputStream fileIn, bufferIn;
     static ObjectInput input;
 
     /**
      * Closes the file that the writing operations used
      * @param output
      * @throws IOException
      */
     public static void closeOutputFile() throws IOException {
         output.close();
     }
 
     /**
      * Writes HashMap containing Courses to file
      * @param courses
      * @throws IOException
      */
     public static void writeCourseEvents(HashMap<Integer, Course> courses, String name) throws IOException {
         try {
             file = new FileOutputStream(name);
             buffer = new BufferedOutputStream(file);
             output = new ObjectOutputStream(buffer);
 
         } catch (IOException ex) {
             System.out.println("Could not save the calendar and events to file");
         }
         try {
             output.writeObject(courses);
             output.close();
         } catch (IOException ex) {
             System.out.println("Could not save the calendar and events to file");
         }
     }
 
     /**
      * Writes the calendar given as parameter to a file name 'mycalendar.cld'
      * @param calendar dwiCalendar that will be written
      */
     public static void writeCalendar(dwiCalendar calendar, String name) {
         try {
             file = new FileOutputStream(name);
             buffer = new BufferedOutputStream(file);
             output = new ObjectOutputStream(buffer);
 
         } catch (IOException ex) {
             System.out.println("Could not save the calendar and events to file");
         }
         try {
             output.writeObject(calendar);
             output.close();
 
         } catch (IOException ex) {
             System.out.println("Could not save the calendar and events to file");
         }
 
     }
 
     /**
      * Closes the file thats been read
      * @throws IOException
      */
     public static void closeInputFile() throws IOException {
         input.close();
     }
 
     /**
      * Reads save information of the courses from HashMap<Integer, Course>
      * @return the saved HashMap
      * @throws IOException
      */
     public static HashMap<Integer, Course> readCourses(String name) throws IOException {
         try {
             //use buffering
             fileIn = new FileInputStream(name);
             bufferIn = new BufferedInputStream(fileIn);
             input = new ObjectInputStream(bufferIn);
         } catch (IOException ex) {
             System.out.println("Error. Could not open file");
         }
         try {
             //HashMap<Integer, Course> courses = null;
             HashMap<Integer, Course> savedCourses = null;
 
             if ((savedCourses = (HashMap<Integer, Course>) input.readObject()) != null) {
                 HashMap<Integer, Course> courses = savedCourses;
                 return courses;
             }
 
         } catch (ClassNotFoundException ex) {
             System.out.println("Error. Could not find class");
         }
         return null;
     }
 
     /**
      * Reads saved information and events of dwiCalendar from file
      * @return dwiCalendar object read from file.
      */
     public static dwiCalendar readDWICalendar(String name) throws IOException {
         try {
             //use buffering
             fileIn = new FileInputStream(name);
             bufferIn = new BufferedInputStream(fileIn);
             input = new ObjectInputStream(bufferIn);
         } catch (IOException ex) {
             System.out.println("Error. Could not open file");
         }
         try {
             dwiCalendar calendar = null;
 
             if ((calendar = (dwiCalendar) input.readObject()) != null) {
                 dwiCalendar dwi = calendar;
                 return dwi;
             }
 
         } catch (ClassNotFoundException ex) {
             System.out.println("Error. Could not find class");
         }
         return null;
     }
 
     /**
      * Static method for writing a certain week's events to a text file
      * @param events ArrayList containing one week's events
      * @param filename Name of the text file.
      */
     public static void writeWeek(ArrayList<Event> events, String filename) {
         
         try {
             PrintWriter out = new PrintWriter(new FileWriter(filename));
             ArrayList<String> dailyEvents;
             out.print("VIIKKO: " + events.get(0).getStarttime().get(Calendar.WEEK_OF_YEAR) + "\n\n********\n");
 
             dailyEvents = getWeekDayEvents(events, Calendar.MONDAY);
             printDayEvents(dailyEvents, out, "MAANANTAI");
 
             dailyEvents = getWeekDayEvents(events, Calendar.TUESDAY);
             printDayEvents(dailyEvents, out, "TIISTAI");
 
             dailyEvents = getWeekDayEvents(events, Calendar.WEDNESDAY);
             printDayEvents(dailyEvents, out, "KESKIVIIKKO");
 
             dailyEvents = getWeekDayEvents(events, Calendar.THURSDAY);
             printDayEvents(dailyEvents, out, "TORSTAI");
 
             dailyEvents = getWeekDayEvents(events, Calendar.FRIDAY);
             printDayEvents(dailyEvents, out, "PERJANTAI");
 
             dailyEvents = getWeekDayEvents(events, Calendar.SATURDAY);
             printDayEvents(dailyEvents, out, "LAUANTAI");
 
             dailyEvents = getWeekDayEvents(events, Calendar.SUNDAY);
             printDayEvents(dailyEvents, out, "SUNNUNTAI");
 
             out.close();
 
 
 
 
         } catch (IOException e) { // *I step back from the mic to WHY
             e.printStackTrace();
         }
 
 
     }
 
     private static void printDayEvents(ArrayList<String> dailyEvents, PrintWriter out, String dayName) {
         if (!dailyEvents.isEmpty()) {
             out.print("---" + dayName +"---\n");
             for (String event : dailyEvents) {
                 out.print(event + "\n\n");
             }
             out.print("\n");
         }
     }
 
     private static ArrayList<String> getWeekDayEvents(ArrayList<Event> events, int weekDayID) {
         ArrayList<String> dayEvents = new ArrayList<String>();
         for (Event e : events) {
             if (e.getStarttime().get(Calendar.DAY_OF_WEEK) == weekDayID) {
                 dayEvents.add(e.toString());
             }
         }
         return dayEvents;
     }
 }

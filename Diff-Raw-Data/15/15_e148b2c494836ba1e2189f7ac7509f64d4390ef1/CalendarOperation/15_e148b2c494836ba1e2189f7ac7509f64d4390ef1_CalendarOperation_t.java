 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Iterator;
 import java.util.Scanner;
 
 /**
  * Operations like save and load file.
  * This is accessed by the calendar model.
  *
  * @author VictorPantazi
  * @author cmcl
  * @version 1.1
  */
public class CalendarOperation {
 
     private CalendarModel events;
 
    public CalendarOperation(CalendarModel m) {
         this.events = m;
     }
 
     // load the events into the calendar from a given filename
     // boolean to be used by CalendarModel to check if a file
     // is already loaded
    public boolean loadCalendar(String filename) {
         BufferedReader bis = null;
         try {
             try {
                 // open file
                 bis = new BufferedReader(new FileReader(filename));
                 String s=null;
                 s = bis.readLine();
                 //while we still have events to read
                 while (s != null) {
                     Event ev;
                     String name=null,loc=null,desc=null;
                     Date start=null,end=null;
                     Repetition rep=null;
                     Reminder rem=null;
                     //String categories=null;
                     //Time startTime=null,endTime=null;
                     //repetition,reminder
 
                     name=bis.readLine();
                     loc = bis.readLine();
                     desc = bis.readLine();
                     //date has '/' as a delimiter
                     Scanner sc = new Scanner(bis.readLine());
                     sc = sc.useDelimiter("/");
                     start = new Date(sc.nextInt(),
                                      sc.nextInt(),sc.nextInt());
                     sc = new Scanner(bis.readLine());
                     sc = sc.useDelimiter("/");
                     end = new Date(sc.nextInt(),
                                    sc.nextInt(),sc.nextInt());
                     sc = new Scanner(bis.readLine());
                     if(sc.hasNextInt())
                         rep = new Repetition("",sc.nextInt());
                     else rep = new Repetition(sc.next(),sc.nextInt());
                     sc = new Scanner(bis.readLine());
                     sc = sc.useDelimiter("[/|:]");
                     rem = new Reminder(new Date(sc.nextInt(),
                                                 sc.nextInt(),
                                                 sc.nextInt()),
                                        new Time(sc.nextInt(),
                                                 sc.nextInt()));
                     //push event
                     ev = new Event(name,loc,desc,start,end,rep,rem);
                     this.events.push(ev);
                     
                     //read delimiters
                     bis.readLine();
                     s = bis.readLine();
                 
                 }
                 bis.close();
                 return true;
 
                 // in case the file does not exist, create it
             } catch (FileNotFoundException e) {
                 System.err.println("File not found, "+
                                    "creating file ...");
                 FileOutputStream out;
                 PrintStream p;
                 try {
                     out = new FileOutputStream(filename);
                     p = new PrintStream(out);
                     p.close();
                     out.close();
                     return true;
                 } catch (Exception e2) {
                     System.err.println ("Error "+
                                         "writing file: "+
                                         e2.getStackTrace());
                     return false;
                 }
             }
         } catch(IOException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     // saves the calendar to the given file
    public boolean saveCalendar(String filename) {
         FileOutputStream out;
         PrintStream p;
         try {
             out = new FileOutputStream(filename);
             p = new PrintStream(out);
             Event ev;
             Iterator<Event> it = events.iterator();
             while(it.hasNext()) {
                 ev = (Event) it.next();
                 p.println("^^^^^^^^^^");
                 p.println(ev.getName());
                 p.println(ev.getLocation());
                 p.println(ev.getDescription());
                 p.println(ev.getStartDate());
                 p.println(ev.getEndDate());
                 Repetition rep = ev.getRepetition();
                 if(rep==null)p.println(" 0");
                 else
                     p.println(ev.getRepetition());
                 p.println(ev.getReminder());
                 // more stuff to be added
                 p.println("vvvvvvvvvv");
             }
             p.close();
             out.close();
             return true;
         } catch (Exception e) {
             System.err.println ("Error writing to file: "+
                                 e.getStackTrace());
             return false;
         }
     }
 
     // test method
     public static void main(String[] args) {
         if (args.length != 2) {
             System.out.println("Usage: java "
                     + "CalendarOperations inputfilename outputfilename");
             return;
         }
         boolean loaded = false;
         Event ev = new Event("d", "e", "f", new Date(), new Date(),
                 new Repetition("weekly", 0), new Reminder(
                         new Date(23, 2, 2012), new Time(13, 21)));
         Event ev2 = new Event("a", "b", "c", new Date(), new Date(),
                 new Repetition(), new Reminder());
         System.out.println(loaded);
         CalendarOperation test = new CalendarOperation(new CalendarModel());
         loaded = test.loadCalendar(args[0]);
         test.events.push(ev);
         test.events.push(ev2);
         System.out.println(loaded);
         System.out.println(test.events.peek());
         test.saveCalendar(args[1]);
     }
 }

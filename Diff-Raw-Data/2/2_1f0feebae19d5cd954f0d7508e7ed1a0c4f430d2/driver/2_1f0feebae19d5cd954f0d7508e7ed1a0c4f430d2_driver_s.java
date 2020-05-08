 /**
  * Name: Christina Black, Kelly Hutchison, Nicole Hart
  * Date: 10/5/2012
  * Section: 002 
  * Project:
  */
 
 package calendar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 public class driver {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException {
 
         Scanner input = new Scanner(System.in);
         int hrPart = 0;
         int minPart = 0;
         int endHr = 0;
         int endMin = 0;
         String name;
         BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
         String location;
 
         //New popup reminder
         Reminder remindme = new Reminder(1, 2, "2", "HELLO!");
         remindme.reminderPopUp();
 
         Event newEvent = null;
 
         System.out.println("When do you want your event to start? (Use military time and seperate min and hour digits)");
         while (true) {
             try {
                 if (!input.hasNextInt()) {
                     throw new InputMismatchException();
                 } 
                 
                 else {
                     hrPart = input.nextInt();
                     minPart = input.nextInt();
                     break;
                 }
             } 
             
             catch (InputMismatchException ime) {
                 input.nextLine();
                 System.out.println("You must enter integers!");
                 System.out.println("When do you want your event to start? (Use military time and seperate min and hour digits)");
             }
         }
 
             System.out.println("What is the event?");
             name = consoleIn.readLine();
 
 
             System.out.println("Where is it?");
             location = consoleIn.readLine();
 
             System.out.println("What time does it end? (Military time, digits separated by spaces");
             while (true) {
                 try {
                     if (!input.hasNextInt()) {
                         throw new InputMismatchException();
                     } 
                     
                     
                     else {
                         endHr = input.nextInt();
                         endMin = input.nextInt();
                         break;
                     }
                 } 
                 
                 catch (InputMismatchException ime) {
                     input.nextLine();
                    System.out.println("Youø must enter integers!");
                     System.out.println("When do you want your event to start? (Use military time and seperate min and hour digits)");
                 }
             }
 
             while (true) {
                 try {
                     newEvent = new Event(hrPart, minPart, name, location, endHr, endMin);
                     if ((hrPart > endHr) || (hrPart == endHr && minPart < endMin)) {
                         throw new EventException();
                     }
                     break;
                 } 
                 
                 catch (EventException ee) {
                     input.nextLine();
                     System.out.println("Your event time ends before it starts!");
                     System.out.println("Enter a time after your start time:");
                     endHr = input.nextInt();
                     endMin = input.nextInt();
                 }
             }
 
             Alert alertMe = new Alert(15, newEvent);
             alertMe.alertPopUp();
 
             Alarm alarm1 = new Alarm(8, 30, 7);
             alarm1.alarmPopUp();
 
         }
 
     }

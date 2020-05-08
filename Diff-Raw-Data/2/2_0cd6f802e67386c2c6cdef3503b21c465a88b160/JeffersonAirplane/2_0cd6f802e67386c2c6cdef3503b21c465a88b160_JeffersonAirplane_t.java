 package whiterabbit;
 import java.util.Scanner;
 
 import whiterabbit.JeffersonAirplane.Printer;
 
 public class JeffersonAirplane {
     private static final String ANSWER1C = "C) Team Room Orange - located on the 2nd floor in building\n" +
     		"D (same building as Charlie) and is the nicest team space.  However, it will take the\n" +
     		"developers three days to move, Tom will need hour every morning and evening for travel,\n" +
     		"and Bob will need 30 min notice to get there and can spend 3-4 hours a day.\n";
 
 	private static final String ANSWER1B = "B) Conference Room A - located in building K.  This room is an\n" +
 			"old conference room \"with potential\".  Charles can be there three times a week.  Developers\n" +
 			"need two days to set up.  and Tom needs 30 min of travel time every morning and evening.  There\n" +
 			"is an old conference table and chairs and it will take two weeks for new furniture.\n";
 
 	private static final String ANSWER1A = "A) Team Room 231 - located on the 3rd floor in building F (your building).\n" +
 			"The developers can move in right away because it's already wired.  However, Charlie the\n" +
 			"Customer can only be in the building on Tuesdays from 9:00-11:30 and Tom will need 45 min\n" +
 			"every morning and evening for travel.\n";
 
 	public static final String WELCOME = "\n\n" +
             "Welcome.  My name is Hume.  I am an educational computer simulation. \n" +
             "I'm your guide to the Agile world.\n\n" +
             "In order the determine if Agile is right for you, I need to know how you'll \n" +
             "deal with the following scenarios. These are real-life experiences from Agile \n" +
             "projects. How you choose to deal with these will tell me if you're truly \n" +
             "ready to be on an Agile team.\n\n";
 
     String test1 = "You’ve just been name project manager on the new \n" +
 "ASTRO project that is slated to start next week.\n"+
             "On your team is Tom the Tester, \n" +
 "David, Daniel,and Debbie the Developers,\n"+
             "Bob the Business Analyst, and Uma the UX Designer.\n"+
 "A great group of highly experienced people\n"+
             "and some of the best in the company.\n"+
 
 "However...turns out Tom is currently located in building\n"+
 "C down the road, David and Daniel and Debbie are together \n"+
 "but one floor down, Bob’s cube is right next to you in \n"+
 "building F, and Uma has a floating desk and care move anywhere.\n"+
 "And Charlie the Customer is located on the 6th floor of \n"+
 "building D.  Below are the available space/configuration options.\n"+
 "It’s up to you to choose the best working set up for your team.";
   
 private final Printer printer;
     private ReadoDude reado;
 
 
     public JeffersonAirplane() {
         printer = new Printer();
         reado = new ReadoDude(printer);
     }
 
     public JeffersonAirplane(Printer printer, ReadoDude reado) {
         this.printer = printer;
         this.reado = reado;
     }
 
     public static void main(String[] args) {
         new JeffersonAirplane().run();
 	}
 
     void run() {
         printer.print(WELCOME);
         Question question = new Question(test1);
         question.addAnswer(ANSWER1A, 3);
         question.addAnswer(ANSWER1B, 5);
         question.addAnswer(ANSWER1C, 10);
         printer.print(question.toString());
 
         String answer = reado.get();
         
         int response = question.score(answer);
         printer.print("You got a " + response + "out of 10!");
     }
 
 
     static class Printer {
         public void print(String toPrint){
             System.out.println(toPrint);
         }
     }
 
     static class ReadoDude {
         private Printer printer;
 
         public ReadoDude(Printer printer) {
             this.printer = printer;
         }
 
         public String get() {
            printer.print("Please enter your answer below:");
             String s = new Scanner(System.in).nextLine();
             printer.print("Thank you!");
             return s;
         }
     }
 }

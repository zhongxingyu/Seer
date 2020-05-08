 package RedPointMaven;
 
 /**
  * Main class for RedpointMaven.
  */
 
 import java.util.Scanner;
 
 public class Redpoint {
     // main method
     public static void main(String[] args) {
         // initialize variables
         Scanner scanner;
         scanner = new Scanner(System.in);
         int year = 0;
         int doNextYear;
         Roster roster;
         roster = new Roster();
         Hat giverHat;
         Hat giveeHat;
         String giver;
         String givee;
 
         System.out.println("Year " + year + " Gifts:");
        roster.printGivingRoster(0);
         System.out.println();
         System.out.print("Continue? (1 = yes, 0 = no): ");
         doNextYear = scanner.nextInt();
 
         while (doNextYear == 1) {
             year++;
 
             // add a new, empty year to history
             roster.addNewYear();
 
             // make a giver Hat
             giverHat = new Hat(roster);
 
             // make a givee Hat
             giveeHat = new Hat(roster);
 
             // draw a giver
             giver = giverHat.drawPuck();
 
             // draw a givee
             givee = giveeHat.drawPuck();
 
             // keep drawing givers until Hat empty
             while (!giver.equals("none")) {
                 /*
                 keep drawing a givee for giver until all 3 tests are true.
                 discard givee if test fails.
                 set givee for giver when full success.
                 remove givee from Hat.
                 remover giver from Hat.
                 replace discard givees.
                 draw new giver and repeat all.
                 */
                 while (!givee.equals("none")) {
                     if (Rules.giveeNotSelf(giver, givee)) {
                         if (Rules.giveeNotRecip(giver, givee, roster, year)) {
                             if (Rules.giveeNotRepeat(giver, givee, roster, year)) {
                                 roster.setGivee(giver, givee, year);
                                 giveeHat.removePuck(givee);
                                 givee = "none";
                             } else {
                                 giveeHat.discardPuck(givee);
                                 givee = giveeHat.drawPuck();
                             }
                         } else {
                             giveeHat.discardPuck(givee);
                             givee = giveeHat.drawPuck();
                         }
                     } else {
                         giveeHat.discardPuck(givee);
                         givee = giveeHat.drawPuck();
                     }
                 }
                 giverHat.removePuck(giver);
                 giveeHat.returnDiscards();
                 giver = giverHat.drawPuck();
                 givee = giveeHat.drawPuck();
             }
             System.out.println("Year " + year + " Gifts:");
             roster.printGivingRoster(year);
             System.out.println();
 
             // another year?
             System.out.print("Continue? (1 = yes, 0 = no): ");
             doNextYear = scanner.nextInt();
             System.out.println();
         }
 
         // hope to hear from you!!
         System.out.println();
         System.out.println("This was fun!");
         System.out.println("Talk about a position with Redpoint?");
         System.out.println("Please call: Eric Tobin 773-325-1516");
         System.out.println("Thanks! Bye...");
     }
 }

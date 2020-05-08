 package RandomStuff;
 import java.util.Scanner;
 public class GameCompanyWage {
 
     public static void main(String[] args) {
         Scanner numScan = new Scanner(System.in);
         int hoursWorked;
         int totalPay;
         System.out.println("How many hours did you work this week?");
         hoursWorked = numScan.nextInt();
         if (hoursWorked < 20){
             totalPay = 30*hoursWorked;
        } else {
             totalPay = 40*hoursWorked;
         }
        System.out.println("You earned $" + totalPay + " this week.");
     }
 }

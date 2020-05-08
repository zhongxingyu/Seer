 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * Name:    Sean Hermna
  * Date:    09/17/2013 9:23 PM
  * Class:   CPAC I (2013)
  * Summary: This calculates the difference between 2 input times values.
  * The input time values should be 6-digit integer, in HHMMSS format.
  * The output time value is in this same HHMMSS format.
  */
 public class homework1 {
     public static void main(String[] args) {
         // Introduce the program.
         System.out.println("This program will calculate the difference between" +
                 " 2 input time values.");
 
         // Receive input from the user.
         Scanner input = new Scanner(System.in);
         System.out.print("Enter the first time (HHMMSS): ");
         int inputTime1 = input.nextInt();
         System.out.print("Enter the second time (HHMMSS): ");
         int inputTime2 = input.nextInt();
             // 230000, 210001 -- > 15959
             // i.e., 11:00:00 PM and 9:00:01 PM [difference is 1:59:59, or 1 hr 59 mins 59 secs]
 
         // Pull the HH (hours), MM (minutes), and SS (seconds) pairs from each input.
         // Simultaneously convert these value pairs to seconds.
         int timeInSeconds1 = (((inputTime1 / 10000) * 3600) + // hours in seconds
                 (((inputTime1 / 100 ) % 100) * 60) + // minutes in seconds
                 (inputTime1 % 100)); // just plain seconds
         int timeInSeconds2 = (((inputTime2 / 10000) * 3600) +
                 (((inputTime2 / 100 ) % 100) * 60) +
                 (inputTime2 % 100));
 
         // Calculate the difference in seconds.
         boolean negativeDifference = (timeInSeconds1 < timeInSeconds2);
         int secondsDifference = Math.abs(timeInSeconds1 - timeInSeconds2);
        int timeDifference = 0;
 
         // Convert difference in seconds back to HHMMSS
         int hoursDifference = secondsDifference / 3600; // hours in difference
         secondsDifference = Math.abs(secondsDifference % 3600);
        timeDifference = timeDifference + (hoursDifference * 10000);
         int minutesDifference = secondsDifference / 60; // minutes in remaining difference
        timeDifference = timeDifference + (minutesDifference * 100);
         secondsDifference = secondsDifference % 60; // seconds in remaining difference
        timeDifference = timeDifference + secondsDifference;
 
         // Display the results
        System.out.printf("Time difference (1): ");
         if (negativeDifference)
             System.out.printf("-");
         System.out.printf("%d%02d%02d", hoursDifference, minutesDifference, secondsDifference);
 
        System.out.printf("\nTime difference (2): ");
        if (negativeDifference)
            System.out.printf("-");
        System.out.print(timeDifference);

     }
 }

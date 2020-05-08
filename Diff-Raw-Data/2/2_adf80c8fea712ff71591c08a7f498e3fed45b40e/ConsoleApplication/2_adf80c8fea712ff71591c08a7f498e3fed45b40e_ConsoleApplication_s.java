 package de.menzerath.imwd;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class ConsoleApplication {
     private String url;
     private int interval;
 
     /**
      * Validate the values, save them and run the checker
      *
      * @param url      URL to check
      * @param interval Interval to check
      */
     public ConsoleApplication(String url, String interval) {
         if (Helper.validateInput(url, interval, false)) {
             int myInterval = 0;
             try {
                 myInterval = Integer.parseInt(interval);
             } catch (NumberFormatException ignored) {
             }
 
             this.url = url;
             this.interval = myInterval;
             run();
         } else {
             System.exit(1);
         }
     }
 
     /**
      * This is the main-method which will take the values from the preferences and start directly.
      */
     private void run() {
         // Run the update-check
         runUpdateCheck();
 
         // Display the used values
         System.out.println("\nStarting with the following settings:");
         System.out.println("URL: " + url);
         System.out.println("Interval: " + interval + " seconds");
         System.out.println("Log-File: Yes");
         System.out.println("Log valid checks: Yes\n");
 
         // Create the Checker and go!
         Checker checker = new Checker(1, url, interval, true, true, true, true, false);
         checker.startTesting();
 
         // Add option to exit "Is My Website Down?" by typing "stop"
         BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
 
         boolean working = true;
         while (working) {
             try {
                 if (buf.readLine().equalsIgnoreCase("stop")) {
                     checker.stopTesting();
                     working = false;
 
                     try {
                         buf.close();
                     } catch (IOException ignored) {
                     }
                 }
             } catch (IOException ignored) {
             }
         }
 
         System.exit(0);
     }
 
     /**
      * An update-check for the "ConsoleApplication": If there is an update available, it will stop and show an url to get the update.
      */
     private void runUpdateCheck() {
         System.out.println("Checking for updates, please wait...");
         Updater myUpdater = new Updater();
         if (myUpdater.getServerVersion().equalsIgnoreCase("Error")) {
             System.out.println("Unable to search for Updates. Please visit \"https://github.com/MarvinMenzerath/IsMyWebsiteDown/releases/\"." + "\n");
         } else if (myUpdater.isUpdateAvailable()) {
             System.out.println("There is an update to version " + myUpdater.getServerVersion() + " available.");
             System.out.println("Changes: " + myUpdater.getServerChangelog());
            System.out.println("Please download it now by using \"wget https://github.com/MarvinMenzerath/IsMyWebsiteDown/releases/download/v" + myUpdater.getServerVersion() + "/IMWD.jar\"." + "\n");
             System.exit(0);
         } else {
             System.out.println("Congrats, no update found!");
         }
     }
 }

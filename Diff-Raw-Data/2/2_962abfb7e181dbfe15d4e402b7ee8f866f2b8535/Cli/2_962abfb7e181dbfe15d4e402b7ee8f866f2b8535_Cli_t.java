 /**
  * Cli.java
  * Sep 18, 2007
  */
 package com.ericsson.wst.ui;
 
 import gnu.getopt.Getopt;
 
 import java.util.List;
 
 import com.ericsson.wst.core.facade.Coordinator;
 
 /**
  * @author ehonlia
  * 
  */
 public class Cli
 {
     private Coordinator coordinator;
 
     /**
      * @param args
      */
     public static void main(String[] args)
     {
         Cli cli = new Cli();
         Getopt g = new Getopt("wst", args, "hlf:");
         String fileName = "";
         boolean toShowIndicators = false;
 
         int c = -1;
         while ((c = g.getopt()) != -1)
         {
             switch (c)
             {
             case 'h':
             case '?':
                 cli.usage();
                 break;
             case 'l':
                 toShowIndicators = true;
                 break;
             case 'f':
                 fileName = g.getOptarg();
                 break;
             default:
                 System.err.println("Unsupported arguments detected!");
                 cli.usage();
                 break;
             }
         }
 
         cli.coordinator = new Coordinator();
 
         try
         {
             cli.coordinator.setUp();
 
             if (toShowIndicators)
             {
                 cli.showAllIndicators();
             }
             else
             {
                 cli.coordinator.testWorstationStatus(fileName);
                 cli.coordinator.outputWorkstationStatus();
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
 
             System.out.println("wst quit abnormally with error code: "
                    + cli.coordinator.getErrorCode().getValue());
 
             cli.coordinator.tearDown();
 
             System.exit(cli.coordinator.getErrorCode().getValue());
         }
 
         cli.coordinator.tearDown();
     }
 
     private void showAllIndicators()
     {
         List<String> indicatorList = coordinator.getAllIndicators();
 
         for (String indicator : indicatorList)
         {
             System.out.println(indicator + "    "
                     + coordinator.getCommand(indicator));
         }
     }
 
     private void usage()
     {
         System.out.println("Use: wst -f file");
         System.out.println("     or, no argument to use "
                 + "default workstaion list file \"$HOME/.wst\".");
         System.out.println("Use: wst -l");
         System.out.println("     to show all available commands.");
 
         System.exit(0);
     }
 
 }

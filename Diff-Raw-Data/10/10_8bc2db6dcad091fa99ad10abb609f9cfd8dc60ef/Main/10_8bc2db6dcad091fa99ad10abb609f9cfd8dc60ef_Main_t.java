 /*
  * Copyright (c) 2007 Zauber S.A.  -- All rights reserved
  */
 package ar.com.zauber.commons.launcher;
 
 /**
  * Creates the launcher
  * 
  * @author Christian Nardi
  * @since Nov 14, 2007
  */
 public final class Main {
     /**
      * Creates the Main.
      *
      */
     private Main() { 
     }
     
     /**
      * @param args 1 - port
      *             2 - application directory
      *             3 - context path
      * @throws Exception if the server could not be run
      */
     public static void main(final String[] args) throws Exception {
         if (args.length < 3) {
             showUssage();
             System.exit(1);
         }
         int port = 0;
         String contextpath = "";
         String path = "";
         try {
             port = Integer.parseInt(args[0]);
             if (port <= 0) {
                 showUssage();
                 System.exit(1);
             }
         } catch (final NumberFormatException e) {
             showUssage();
             System.exit(1);
         }
        contextpath = args[2];
        path = args[1];
         new Launcher(path, contextpath, port).run();
     }
 
     /**
      * 
      */
     private static void showUssage() {
         System.out.println("You need three arguments:");
         System.out.println("\t port");
         System.out.println("\t application directory or war path");
         System.out.println("\t context path");
     }
 }
 

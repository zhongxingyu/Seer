 package de.metalab.namnam.twitterer;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * @author fake
  */
 public class Main {
 
     public static final Logger logger = Logger.getLogger(Main.class.getName());
     protected static SimpleDateFormat df;
 
 
     public static void main(String args[]) {
 
         if(args.length < 3) {
 			usage();
 			return;
 		}
 
         boolean doTwitter = true;
         String theDate = null;
         String twitterUser = null;
         String twitterPass = null;
         String file = null;
 
         String arg = null;
         for(int i = 0; i < args.length; i++) {
             arg = args[i];
             if(arg.equals("--help")) {
 				usage();
 				return;
             } else if (arg.startsWith("--date=")) {
                 theDate = arg.substring(arg.indexOf('=')+1,arg.length());
             } else if (arg.equals("--no-twitter")) {
                 doTwitter = false;
             } else if (arg.startsWith("--tu=")) {
                 twitterUser = arg.substring(arg.indexOf('=')+1,arg.length());
             } else if (arg.startsWith("--tp=")) {
                 twitterPass = arg.substring(arg.indexOf('=')+1,arg.length());
             } else if (arg.startsWith("--file=")) {
                 file = arg.substring(arg.indexOf('=')+1,arg.length());
             }
         }
 
         if(file == null || twitterUser == null || twitterPass == null ) {
             usage();
             return;
         }
 
         // set the calendar to today
         Calendar date = Calendar.getInstance();
 
         if(theDate != null) {
            df = new SimpleDateFormat("yyyy-MM-dd");
             try {
                 date.setTime(df.parse(theDate));
             } catch (ParseException pe) {
                 logger.log(Level.SEVERE, "Invalid date speciefied!", pe);
             }
         }
 
         date.set(Calendar.HOUR_OF_DAY, 0);
         date.set(Calendar.MINUTE, 0);
         date.set(Calendar.SECOND, 0);
         date.set(Calendar.MILLISECOND, 0);
 
         if(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
             logger.log(Level.WARNING,"Wochenende! Ich hab' frei!");
             return;
         }
 
 
         try {
             logger.log(Level.INFO, (!doTwitter?"not ":"")+"twittering:");
             NamNamTwitterer nnt = new NamNamTwitterer(file,twitterUser,twitterPass);
             nnt.setDoTwitter(doTwitter);
             nnt.sendMenue(date.getTime());
         } catch (IOException ioex) {
             logger.log(Level.SEVERE,"IOException while loading mensa",ioex);
         }
     }
 
     public static void usage() {
         System.out.println( "NamNamTwitterer - (C) 2009 Thomas 'fake' Jakobi");
 		System.out.println();
 		System.out.println("Usage:");
 		System.out.println();
 		System.out.println("java -jar namnamtwitterer.jar --tu=<twitter user> --tp=<twitter pw> --file=<filename> ");
 		System.out.println();
 		System.out.println("Options:");
 		System.out.println(" --help         This help text");
 		System.out.println(" --no-twitter   do not actually twitter, just ouput what would be sent");
         System.out.println(" --date=<date>  twitter info for date, format: YYYY-MM-DD");
 		System.out.println();
 		System.out.println("Examples:");
 		System.out.println(" java -jar namnamtwitterer.jar --tu=hsin_mensa --tp=pass --file=Mensa-IN.xml");
 		System.out.println();
     }
 
 }

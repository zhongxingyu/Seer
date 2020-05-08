 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package com.team20.sik;
 
 import com.team20.sik.controllers.Serializer;
 import com.team20.sik.views.SiKFrame;
 import org.json.JSONArray;
import org.json.JSONObject;
 import com.team20.sik.controllers.data.BlueAlliance;
 import java.io.File;
 import javax.swing.JFileChooser;
 
 /**
 *
 * @author Driver
 */
 public class SiK {
 
     /**
 * @param args the command line arguments
 */
     public static void main(String[] args) {
          //output string for all search commands: printed after every printable command is executed String output=null;
         String output="";    
             
         //forces an output on every command
         boolean force = false;
 
         //option is true only if -v or --verbose is specified: System prints all query results
         boolean verbose = false;
 
         //version number (can be anything really, just thought it'd be good for the help screen)
         final String version = "v0.0.0.1";
 
         //option is true only if command line args were passed at invocation
         boolean commandline=false;
 
         //option is true only if flag -s or --store is set: allows program to make output file.
         boolean store=false;
 
         //option is true only if the last flag was printable: it assures that the output string is only stored in a file when appropriate.
         boolean flag=false;
 
         //output filename: defaults to frc.o: modified with -o or --output
         String file = "frc.o";
 
         //included for versatility over windows and linux to make sure the directories are printedd correctly. Defaults to linux/mac styling.
         String dirseparator="/";
 
         //gets the OS and makes sure the dirseparator is the correct string.
         String OS=System.getProperty("os.name").toLowerCase();
         if (OS.indexOf("win")>=0)
                 dirseparator = "\\";
         //output directory: defaults to present working directory: modified with -d or --directory
         String dir = System.getProperty("user.dir")+dirseparator + "frcdata";
 
         //declaration for File Type used for storage of data
         File fi;
 //test for command line arguments
         if(args.length>0)
             commandline=true;
         //iterates through command line args
         for(int i=0;i<args.length;++i)
         {
             if (args[i].equals("-v")||args[i].equals("--verbose"))
                 verbose=true;
             //checks for flag to store output
             if(args[i].equals("-s")||args[i].equals("--store"))
                 store=true;
             //checks for flag to force output
             if(args[i].equals("-f")||args[i].equals("--force"))
                 force=true;
             //set output file or set file to deserialize
             if(args[i].equals("-o")||args[i].equals("--output"))
             {
                 try
                 {
                         file=args[i+1];
                 }
                 catch (Exception e)
                 {
                         System.err.println("-o flag requires a directory name");
                 }
             }
             //set output directory or set directory to search for file
             if(args[i].equals("-d")||args[i].equals("--directory"))
             {
                 dir=args[i+1];
                 if(dir.endsWith(dirseparator))
                 {
                     dir=dir.substring(0,dir.length()-1);
                 }
             }
             //search for team data
             if(args[i].equals("-t")||args[i].equals("--team"))
             {
                                        try
                 {
                     //tells system to store after this flag
                     flag=true;
                     if (args[i+1].equals("-o")||args[i+1].equals("-output"))
                         args[i+1]=output;
                     output = BlueAlliance.requestTeam(args[i+1]).toString();
                     if(output.startsWith("{\"Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
                     System.err.println("Error fetching team:");
                     System.exit(0);
                 }
             }
             //search for year data
             if(args[i].equals("-y")||args[i].equals("--year"))
             {
                 try
                 {
                     //see above
                     flag=true;
                     output = BlueAlliance.requestYear(args[i+1]).toString();
                     if(output.equals("Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
 
                     System.err.println("Error fetching year:");
                     System.exit(0);
                 }
             }
             //search for event data
             if(args[i].equals("-e")||args[i].equals("--event"))
             {
                 try
                 {
                     //see above
                     flag=true;
                     if (args[i+1].equals("-o")||args[i+1].equals("-output"))
                         args[i+1]=output;
                     output = BlueAlliance.requestEvent(args[i+1]).toString();
                     if(output.equals("Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
                     System.err.println("Error fetching event:");
                     System.exit(0);
                 }
             }
             if(args[i].equals("-m")||args[i].equals("--match"))
             {
                 try
                 {
                     //see above
                     flag=true;
                     if(output.equals("Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
                     System.err.println("Error fetching match:");
                     ex.printStackTrace();
                     System.exit(0);
                 }
             }
             //deserialize from file
             //search for event data
             if(args[i].equals("-e")||args[i].equals("--event"))
             {
                 try
                 {
                     //see above
                     flag=true;
                     if (args[i+1].equals("-o")||args[i+1].equals("-output"))
                         args[i+1]=output;
                     output = BlueAlliance.requestEvent(args[i+1]).toString();
                     if(output.equals("Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
                     System.err.println("Error fetching event:");
                     System.exit(0);
                 }
             }
             if(args[i].equals("-m")||args[i].equals("--match"))
             {
                 try
                 {
                     //see above
                     flag=true;
                     if(output.equals("Property Error"))
                         throw new Exception();
                     if(verbose)
                         System.out.println(output);
                 }
                 catch(Exception ex)
                 {
                     System.err.println("Error fetching match:");
                     ex.printStackTrace();
                     System.exit(0);
                 }
             }
             //deserialize from file
              if(args[i].equals("-g")||args[i].equals("--get"))
             {
                 try
                 {
                     if(args[i+1]=="-o"||args[i+1]=="--output")
                          args[i+1]=file;
                     System.out.println("Retrieving from:" +dir+dirseparator+args[i+1]);
                     output=Serializer.deserializeData(dir+dirseparator+args[i+1]).toString();
                     System.out.println(output);
                     flag=true;
                 }
                 catch(Exception e)
                 {
 
                     System.err.println("File not found:");
                     System.exit(0);
                 }
             }
             /*search JSON output for key
 ex:
 SiK -t frc20 : nickname
 output= "the rocketeers"
 */
             if(args[i].equals(":"))
             {
                 if(output==null)
                         System.exit(-1);
                 System.out.println("");
                 //Object to reutrn
                 JSONObject temp;
                 //Array to return
                 JSONArray temp2;
                 //checking to see if output represents a JSON Object or a JSONArray
                 if (output.startsWith("{"))
                 {
                     temp = new JSONObject(output);
                     //all retrievals are using .get() and not .getString() on the off chance that there is an array when an object was expected.
                     try
          {
                         output = temp.get(args[i+1]).toString();
                     }
                     catch (Exception e)
                     {
                         System.err.println(": operator requires a VALID keyword");
                         System.exit(0);
                     }
                 }
                 //output is an array
                 if (output.startsWith("["))
                 {
                     temp2 = new JSONArray(output);
                     try
                     {
                         //check if the user specified an index- if not, handle the exception.
                         int input = Integer.parseInt(args[i+1]);
                         output = temp2.get(input).toString();
                         //assure that the output of the search is not stored in a file
                         flag = false;
                     }
                     catch(Exception ex)
                     {
                         //iterate through array values- display them with corresponding index
                         for(int s=0; s<temp2.length();++s)
                         {
                             System.out.println(s + " - " + temp2.get(s));
                         }
                         try
                         {
                             //I hate Scanners, so I made a manual, 2 byte input
                             byte [] b = new byte [2];
                             Integer i1,i2;
                             System.out.println("What index do you want?: ");
                             int input = System.in.read(b);
                             //Bytes return ASCII values, so I had to subtract to get the actual int
                             i1=((int)b[0])-48;
                             i2=((int)b[1])-48;
                   if(i2<0)
                             {
                                 i2=i1;
                                 i1=0;
                             }
                             //concatenate the numbers together (I don't think efficiency is neccessary here)
                             Integer total = Integer.parseInt(i1.toString() + i2.toString());
                             output = temp2.get(total).toString();
                         }
                         catch (Exception e)
                         {
                             System.err.println("Error in read: Are you sure that's in there?");
                         }
                         //Note that in this search the file can be stored, as flags is not set to false.
                     }
                 }
                 System.out.println(output);
             }
             if (force==true)
             {
                 if (output!=null)
                 {
                         store=true;
                         flag=true;
                         try
                         {
                                 ++i;
                                 file=args[i+1];
                                 ++i;
                         }
                         catch(ArrayIndexOutOfBoundsException e)
                         {
                                 System.err.println("Force behavior requires filename after every command");
                         }
                 }
             }
                               //check to make sure that the user specified to store the data and that the last operation resulted in an object that should be stored
             if (store==true && flag==true)
             {
                 //file constructed to specified output directory
                 fi = new File(dir);
                 if(!fi.exists())
                 //creates any directory that was specified by the user to store the file in.
                     fi.mkdirs();
                 Serializer.serializeObject(output, dir+dirseparator+file);
                 //reports file storage success
                 System.out.println("File created@" + dir+dirseparator+file);
             }
             if (args[i].equals("-h")||args[i].equals("--help"))
             {
                 //note:
                 String [] help = {"",
                                   "Sik: Scouting instant Knowledge " + version,
                                   "",
                                   "Usage: SiK [-s][-t|-y|-m|-e] : <key>","SiK [flag(s)] <action>",
                                   "(If <action> is null, interactive mode will start)",
                                   "",
                                   "",
                                   "-s --store -specifies whether to store the output or not",
                                   "-g --get -deserialize and output the specified file",
                                   "-d --directory -specify the output directory for data (-s must be checked)",
                                   "-o --output -specify the name of the output file (-s must be checked)",
                                   "-t --team -query frc team names",
                                   "-e --event -query frc events",
                                   "-m --match -query frc matches",
                                   "-y --year -query year data",
                                   "-f --force -force output file on every command: requires a filename after every output command",
                                   "-v --verbose -force console print of all queries, note as well that it nullifies the automatic help display",
                                   ": <key> -look for key in output data",
                                   "",
                                   "NOTE: For your searching purposes, the : operator is modified to display an interactive year query.",
                                   "To search within that output, use : :",
                                   "ex: to get the end date for an event in year 2013, you can use : <event_index> : end_date OR : : end_date to choose the event.",
                                   "-h --help -display help text"
                                  };
                 for (String s:help)
                 {
                     System.out.println(s);
                 }
                 System.exit(1);
 
 
             }
             if (output==null && i==args.length-1)
             {
                 System.err.println("No neccessary task given!");
                 i=-1;
                 args[0]="-h";
             }
             flag=false;
         }
         if(!commandline)                                            
             SiKFrame.start();
         }
 }

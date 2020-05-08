 package com.globalsight.tools;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Formatter;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.MissingOptionException;
 
 public class CLI {
 
     private SortedMap<String, Class<? extends Command>> commands =
         new TreeMap<String, Class<? extends Command>>();
     
     public void run(String[] args) {
         registerDefaultCommands(commands);
         if (args.length == 0) {
             help();
         }
         String cmd = args[0].toLowerCase();
         if (cmd.equals("help") && args.length == 2) {
             help(args[1]);
         }
         Command command = getCommand(commands.get(cmd));
        command.setName(cmd);
         if (command == null) {
             help();
         }
         if (args.length > 1) {
             args = Arrays.asList(args).subList(1, args.length)
                                 .toArray(new String[args.length - 1]);
         }
         else {
             args = new String[0];
         }
         CommandLineParser parser = new GnuParser();
         try {
             CommandLine cl = parser.parse(command.getOptions(), args);
             UserData userData = new UserData();
             File dataFile = getUserDataFile();
             if (dataFile != null) {
                 if (!dataFile.exists()) {
                     System.out.println("Creating " + dataFile);
                     dataFile.createNewFile();
                 }
                 userData = UserData.load(dataFile);
             }
             else {
                 throw new RuntimeException("Unimplemented");
             }
             command.handle(cl, userData);
             if (dataFile != null && userData.isDirty()) {
                 System.out.println("Saving to " + dataFile + "...");
                 userData.store(dataFile);
             }
         }
         catch (MissingOptionException e) {
             System.err.println(e.getMessage());
             command.usage();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Return data file location in the user's home directory.  Note that
      * this file may not exist.
      * @return file location, or null if the file can not be created 
      *     (no home directory set, or directory does not exist)
      */
     File getUserDataFile() {
         String homeDirPath = System.getProperty("user.home");
         if (homeDirPath == null) {
             return null;
         }
         File homeDir = new File(homeDirPath);
         if (!homeDir.exists()) {
             return null;
         }
         return new File(homeDir, ".globalsight");
     }
     
     void registerDefaultCommands(Map<String, Class<? extends Command>> commands) {
         commands.put("fileprofiles", FileProfilesCommand.class);
         commands.put("add-profile", AddProfileCommand.class);
         commands.put("create-job", CreateJobCommand.class);
         commands.put("jobs", ShowJobsCommand.class);
         commands.put("workflow", ShowWorkflowCommand.class);
         commands.put("set-default-profile", SetDefaultProfileCommand.class);
     }
     
     private static Command getCommand(Class<? extends Command> clazz) {
         if (clazz == null) {
             return null;
         }
         try {
             return clazz.newInstance();
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     void help() {
         Formatter f = new Formatter(System.err);
         f.format("Available commands:\n");
         for (Map.Entry<String, Class<? extends Command>> e : 
                                             commands.entrySet()) {
             Command c = getCommand(e.getValue());
             f.format("%-20s%s\n", e.getKey(), c.getDescription());
         }
         f.flush();
         f.close();
     }
     
     private void help(String cmd) {
         Command command = getCommand(commands.get(cmd));
         if (command == null) {
             help();
         }
         command.usage();
     }
     
     public static void main(String[] args) {
         new CLI().run(args);
     }
 }

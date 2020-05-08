 package com.soebes.cli.cli_test;
 
 import com.beust.jcommander.JCommander;
 
 public class SupoSECLI {
     public static final String PROGRAMM_NAME = "supose";
 
     public static final String SCAN_COMMAND = "scan";
     public static final String SEARCH_COMMAND = "search";
     public static final String MERGE_COMMAND = "merge";
 
     private MainCommand mainCommand;
     private ScanCommand scanCommand;
     private SearchCommand searchCommand;
     private MergeCommand mergeCommand;
 
     private JCommander commander;
 
     public SupoSECLI(String[] args) {
         mainCommand = new MainCommand();
 
         scanCommand = new ScanCommand();
         searchCommand = new SearchCommand();
         mergeCommand = new MergeCommand();
 
         commander = new JCommander(mainCommand);
 
         getCommander().addCommand(SCAN_COMMAND, scanCommand);
         getCommander().addCommand(SEARCH_COMMAND, searchCommand);
         getCommander().addCommand(MERGE_COMMAND, mergeCommand);
 
         getCommander().setProgramName(PROGRAMM_NAME);
         getCommander().parse(args);
     }
 
     public boolean isScanCommand() {
         if (SCAN_COMMAND.equals(getCommander().getParsedCommand())) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isSearchCommand() {
         if (SEARCH_COMMAND.equals(getCommander().getParsedCommand())) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isMergeCommand() {
         if (MERGE_COMMAND.equals(getCommander().getParsedCommand())) {
             return true;
         } else {
             return false;
         }
     }
 
     public JCommander getCommander() {
         return this.commander;
     }
 
     public MainCommand getMainCommand() {
         return this.mainCommand;
     }
 
     public ScanCommand getScanCommand() {
         return this.scanCommand;
     }
 
     public SearchCommand getSearchCommand() {
         return this.searchCommand;
     }
 
     public MergeCommand getMergeCommand() {
         return mergeCommand;
     }
 
 
 }

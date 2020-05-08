 package com.jclarity.had_one_dismissal;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class Main {
     private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
     public static void main(String[] args) throws ParseException, IOException {
         Options options = new Options();
         options.addOption("f", true, "csv file to use for exercises");
         options.addOption("c", true, "single class to run");
         options.addOption("t", true, "time limit to run for");
 
        CommandLine cmd = new GnuParser().parse(options, args);
 
         if (cmd.hasOption("f")) {
             runFromCsv(cmd.getOptionValue("f"));
         } else if (cmd.hasOption("c")) {
             String exercise = cmd.getOptionValue("c");
             long timeLimit = Long.parseLong(cmd.getOptionValue("t"));
             Exercise.runExercise(exercise, timeLimit);
         } else {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("Main", options);
         }
     }
 
     public static void runFromCsv(String csvFile) throws IOException {

         List<String[]> exercises;
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
             exercises = reader.readAll();
         }
 
         while (true) {
             for (String[] exercise : exercises) {
                 if (exercise.length >= 2) {
                     LOGGER.info("Running {} for {}", exercise[0], exercise[1]);
                     Exercise.runExercise(exercise[0], Long.parseLong(exercise[1]));
                 }
             }
         }
     }
 
 }

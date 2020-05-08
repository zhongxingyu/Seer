 package com.inescid.cnm;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 public class ReaderCliOptions extends Options
 {
     private static final String DEFAULT_INPUT_MSEED_PATH = "../Sesimbra_07_Jan_2012/theirs/IP.PSES..BHN.D.2013.038";
 
     private static final long serialVersionUID = 1L;
 
     public String inputFilePath = DEFAULT_INPUT_MSEED_PATH;
     public String outputDataFilePath = "out.data";
     public Boolean softLineLimit = false;
     public int softLineLimitValue = -1;
     public boolean debug = false;
     public boolean inputWithSequenceNumber = false;
     public boolean outputWithTime = false;
     public boolean onlyCheck = false;
     public boolean isInputJson = false;
 
 
     public ReaderCliOptions()
     {
         super();
 
         OptionBuilder.withLongOpt("soft-line-limit");
         OptionBuilder.withDescription("Limits the number of mseed lines to process (only a soft limit, number of lines limit can be higher)");
         OptionBuilder.withType(Number.class);
         OptionBuilder.hasArg();
         OptionBuilder.withArgName("number");
         this.addOption(OptionBuilder.create());
 
        OptionBuilder.withLongOpt("inputFilePath");
         OptionBuilder.withDescription("Input filepath");
         OptionBuilder.withType(String.class);
         OptionBuilder.hasArg();
         OptionBuilder.withArgName("input-file-path");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("data-output");
         OptionBuilder.withDescription("Filepath to write the data output");
         OptionBuilder.withType(String.class);
         OptionBuilder.hasArg();
         OptionBuilder.withArgName("output-path");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("help");
         OptionBuilder.withDescription("print this message and exit");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("debug");
         OptionBuilder.withDescription("Prints mseed info as it reads the input file");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("only-check");
         OptionBuilder.withDescription("Only checks input file and does not write output file");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("outputWithTime");
         OptionBuilder.withDescription("Is the output to be written with time values");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("inputWithSequenceNumber");
         OptionBuilder.withDescription("Input has sequence number instead of timestamp");
         this.addOption(OptionBuilder.create());
 
         OptionBuilder.withLongOpt("isInputJson");
         OptionBuilder.withDescription("Is Input in json format");
         this.addOption(OptionBuilder.create());
     }
 
     void parse(String[] args)
     {
         CommandLineParser cmdLineParser = new PosixParser();
 
         try {
             CommandLine cmdLine = cmdLineParser.parse(this, args);
 
             if (cmdLine.hasOption("soft-line-limit")) {
                 softLineLimit = true;
                 softLineLimitValue = ((Number)cmdLine.getParsedOptionValue("soft-line-limit")).intValue();
             }
 
             if (cmdLine.hasOption("data-output")) {
                 outputDataFilePath = (String) cmdLine.getParsedOptionValue("data-output");
             }
 
             if (cmdLine.hasOption("input-file-path")) {
                 inputFilePath = (String) cmdLine.getParsedOptionValue("input-file-path");
             }
 
             if (cmdLine.hasOption("inputWithSequenceNumber")) {
                 inputWithSequenceNumber = true;
             }
 
             if (cmdLine.hasOption("outputWithTime")) {
                 outputWithTime = true;
             }
 
             if (cmdLine.hasOption("isInputJson")) {
                 isInputJson = true;
             }
 
             if (cmdLine.hasOption("debug")) {
                 debug = true;
             }
 
             if (cmdLine.hasOption("only-check")) {
                 onlyCheck = true;
             }
 
             if (cmdLine.hasOption("help")) {
                 System.out.println("Help");
                 HelpFormatter h = new HelpFormatter();
                 h.printHelp("help", this);
                 System.exit(-1);
             }
 
         } catch (ParseException e) {
             HelpFormatter h = new HelpFormatter();
             h.printHelp("help", this);
             System.exit(-1);
         }
     }
 
     @Override
     public String toString() {
         return String.format("Options:%n\thas line Limit:\t%s%n\tsoftLineLimit:\t%d%n\tdata-output:\t%s%n\tinput filepath:\t%s" + 
                 "%n\tdebug:\t%s%n\toutputWithTime:\t%s%n\tinputWithSequenceNumber\t%s%n\tIsInputWithJson: %s", 
                 softLineLimit, softLineLimitValue, outputDataFilePath, inputFilePath, 
                 debug, outputWithTime, inputWithSequenceNumber, isInputJson);
     };
 }

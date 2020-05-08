 package com.cs3240.parsergenerator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import com.cs3240.parsergenerator.Domain.Grammar;
 import com.cs3240.parsergenerator.Domain.ParseTable;
 import com.cs3240.parsergenerator.exceptions.InvalidSyntaxException;
 import com.cs3240.parsergenerator.utils.Driver;
 import com.cs3240.parsergenerator.utils.ParseTableGenerator;
 
 public class Main {
 
     @Option(name="-g",
             usage="File containing grammar specification",
             aliases={"--grammar"})
     private File grammarFile;
 
     @Option(name="-i",
             usage="File containing input to check against the supplied grammar." +
                     "If this is not supplied, then the parse table is written out to " +
                     "the specified filename.",
             aliases={"--input"})
     private File inputFile;
 
     @Option(name="-o",
             usage="When a test input is not supplied, the parse table is written to this file.",
             aliases={"--output"},
             metaVar="FILE")
     private String outputFilename;
     
     @Option(name="-pt",
     		usage="File containing a parseTable to check input against, cannot be using with grammar file input",
     		aliases="--parseTable")
     private File parseTableFile;
     
     public static void main(String[] args)
     throws IOException, InvalidSyntaxException {
 
 
         Main m = new Main();
         CmdLineParser parser = new CmdLineParser(m);
 
         try {
            parser.parseArgument(args);
 
             if (m.inputFile == null && m.outputFilename == null && m.parseTableFile == null) {
                 String message = "The -o parameter must be used when the -i parameter is absent.";
                 throw new CmdLineException(parser, message);
             }
             
             if (m.grammarFile != null && m.parseTableFile != null) {
             	String message = "The -g parameter and the -pt cannot be used at the same time.";
                 throw new CmdLineException(parser, message);
             }
             
             if (m.grammarFile == null && m.parseTableFile == null) {
             	String message = "You must use either the -g parameter or the -pt parameter.";
                 throw new CmdLineException(parser, message); 
             }
             
             if (m.parseTableFile != null && m.inputFile == null) {
             	String message = "You must use the -i parameter to use the -pt parameter.";
                 throw new CmdLineException(parser, message);
             }
 
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             parser.printUsage(System.out);
             System.exit(1);
         }
 
         ParseTable pt = null;
         if (m.grammarFile != null) {
         	Grammar grammar = GrammarFileParser.parse(m.grammarFile);
         	grammar.removeLeftRecursion();
         	pt = ParseTableGenerator.generateTable(grammar);        	
         } else {
         	pt = Driver.getTableFromFile(m.parseTableFile);
         }
 
         if (m.inputFile == null) {
             Driver.outputTableToFile(pt, m.outputFilename);
         } else {
             StringBuffer sb = new StringBuffer();
             BufferedReader reader = new BufferedReader(new FileReader(m.inputFile));
             String temp;
             while ((temp = reader.readLine()) != null) {
                 sb.append(temp);
             }
 
             String input = sb.toString();
             if (Driver.parse(pt, input)) {
             	System.out.println("Your input is valid with the grammar");
             }
         }
     }
 }

 package org.uli.util;
 
 import org.junit.Test;

 import static org.junit.Assert.*;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 // import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 // import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 // import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 public class MyOptionBuilderTest
 {
 
     @Test
     public void testNew() {
        @SuppressWarnings("unused")
         Option option = MyOptionBuilder.init().create("x");
     }
 
     @Test
     public void testSimpleOption() throws Exception {
         String[] args = new String[] { "-x" };
         Option option = MyOptionBuilder.init().create("x");
         Options options = new Options();
         options.addOption(option);
         CommandLineParser commandLineParser = new PosixParser();
         CommandLine commandLine = commandLineParser.parse(options, args);
         assertTrue(commandLine.hasOption("x"));
         assertFalse(commandLine.hasOption("X"));
     }
 }

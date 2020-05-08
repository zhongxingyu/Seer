 package org.meeuw.json.grep;
 
 import com.fasterxml.jackson.core.JsonParser;
 import org.apache.commons.cli.*;
 import org.meeuw.json.Util;
 
 import java.io.*;
 import java.util.Arrays;
import java.util.List;
 
 /**
  * @author Michiel Meeuwissen
  * @since 0.4
  */
 public class GrepMain {
 
 
     public static enum Output {
         PATHANDVALUE,
         KEYANDVALUE,
		PATH,
		KEY,
         VALUE
     }
 
     private Output outputFormat = Output.PATHANDVALUE;
 
     private final PrintStream output;
 
     private String sep = "\n";
     private String recordsep = "\n";
 
     private final Grep.PathMatcher pathMatcher;
 
     public GrepMain(Grep.PathMatcher pathMatcher, OutputStream output) {
         this.pathMatcher = pathMatcher;
         this.output = new PrintStream(output);
     }
 
     public Output getOutputFormat() {
         return outputFormat;
     }
 
     public void setOutputFormat(Output outputFormat) {
         this.outputFormat = outputFormat;
     }
 
 
     public String getSep() {
         return sep;
     }
 
     public void setSep(String sep) {
         this.sep = sep;
     }
 
     public String getRecordsep() {
         return recordsep;
     }
 
     public void setRecordsep(String recordsep) {
         this.recordsep = recordsep;
     }
 
     public void read(JsonParser in) throws IOException {
         Grep grep = new Grep(pathMatcher, in);
         boolean needsSeperator = false;
         while (grep.hasNext()) {
             GrepEvent match = grep.next();
             if (needsSeperator) {
                 output.print(sep);
             }
             switch (outputFormat) {
				case PATHANDVALUE:
					output.print(match.getPath().toString());
                     output.print('=');
                     output.print(match.getValue());
                     break;
                 case KEYANDVALUE:
                     output.print(match.getPath().peekLast());
                     output.print('=');
                     output.print(match.getValue());
                     break;
				case PATH:
					output.print(match.getPath().toString());
					break;
				case KEY:
					output.print(match.getPath().peekLast());
					break;
                 case VALUE:
                     output.print(match.getValue());
                     break;
             }
             needsSeperator = true;
         }
         if (needsSeperator) {
             output.print('\n');
         }
         output.close();
     }
 
     public void read(Reader in) throws IOException {
         read(Util.getJsonParser(in));
     }
     public void read(InputStream in) throws IOException {
         read(Util.getJsonParser(in));
     }
 
 
 
     public static void main(String[] argv) throws IOException, ParseException {
         CommandLineParser parser = new BasicParser();
         Options options = new Options().addOption(new Option("help", "print this message"));
         options.addOption(new Option("output", true, "Output format, one of " + Arrays.asList(Output.values())));
         options.addOption(new Option("sep", true, "Separator (defaults to newline)"));
        //options.addOption(new Option("record", true, "Record pattern (default to no matching at all)"));
         options.addOption(new Option("recordsep", true, "Record separator"));
         CommandLine cl = parser.parse(options, argv, true);
         String[] args = cl.getArgs();
         if (cl.hasOption("help")) {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(
                     "jsongrep [OPTIONS] <pathMatcher expression> [<INPUT FILE>|-]",
                     options
             );
             System.exit(1);
         }
         if (args.length < 1) throw new MissingArgumentException("No pathMatcher expression given");
         GrepMain grep = new GrepMain(Parser.parsePathMatcherChain(args[0], false), System.out);
         if (cl.hasOption("output")) {
             grep.setOutputFormat(Output.valueOf(cl.getOptionValue("output").toUpperCase()));
         }
         if (cl.hasOption("sep")) {
             grep.setSep(cl.getOptionValue("sep"));
         }
         if (cl.hasOption("recordsep")) {
             grep.setRecordsep(cl.getOptionValue("recordsep"));
         }
         if (cl.hasOption("record")) {
             //pathMatcher.setRecordMatcher(parsePathMatcherChain(cl.getOptionValue("record")));
         }
 
		List<String> argList = cl.getArgList();
		InputStream in = Util.getInput(argList.toArray(new String[argList.size()]), 1);
         grep.read(in);
         in.close();
     }
 }

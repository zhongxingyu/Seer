 package org.uli.wikitext;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
 import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.core.parser.builder.DocBookDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.builder.XslfoDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
 import org.eclipse.mylyn.wikitext.markdown.core.MarkdownLanguage;
 import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
 import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
 import org.eclipse.mylyn.wikitext.tracwiki.core.TracWikiLanguage;
 import org.eclipse.mylyn.wikitext.twiki.core.TWikiLanguage;
 import org.uli.util.MyOptionBuilder;
 
 public class Main {
 
     static private final String NAME = "wikitext";
 
     public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
         int exitCode = run(args);
         if (exitCode != 0) {
             System.exit(exitCode);
         }
     }
 
     private final static void printHelp(PrintStream out, Options options, ParseException e) {
         if (e != null) {
             out.println(NAME + ": Command line error - " + e.getMessage());
         }
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp(NAME, options);
     }
 
     static public int run(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
         Main main = new Main();
         Options options = new Options();
         Option[] add = new Option[] {
            MyOptionBuilder.init().withLongOpt("help").withDescription("print help").create("h"),
            MyOptionBuilder.init().withLongOpt("from").hasArg().withDescription("input file").create("f"),
            MyOptionBuilder.init().withLongOpt("to").hasArg().withDescription("output file").create("t"),
            MyOptionBuilder.init().withLongOpt("input-format").hasArg().withDescription("input format").create("i"),
            MyOptionBuilder.init().withLongOpt("output-format").hasArg().withDescription("output format").create("o")
         };
         for (Option o : add) {
             options.addOption(o);
         }
         int exitCode = 0;
         boolean fHelp = false;
         inputType it = inputType.UNKNOWN;
         outputType ot = outputType.UNKNOWN;
         Reader inputReader = null;
         Writer outputWriter = null;
         for (;;) {
             CommandLineParser commandLineParser = new PosixParser();
             try {
                 CommandLine commandLine = commandLineParser.parse(options, args);
                 fHelp = commandLine.hasOption("h");
                 if (fHelp) {
                     printHelp(System.out, options, null);
                     break;
                 }
                 if (commandLine.hasOption("f")) {
                     String fname = commandLine.getOptionValue("f");
                    System.err.println(fname);
                     inputReader = new FileReader(fname);
                     it = inputType.fromName(getExtension(fname));
                 }
                 if (commandLine.hasOption("i")) {
                     String fmtname = commandLine.getOptionValue("i");
                     it = inputType.fromName(fmtname);
                 }
                 if (commandLine.hasOption("t")) {
                     String fname = commandLine.getOptionValue("t");
                     outputWriter = new FileWriter(fname);
                     ot = outputType.fromName(getExtension(fname));
                 }
                 if (commandLine.hasOption("o")) {
                     String fmtname = commandLine.getOptionValue("o");
                     ot = outputType.fromName(fmtname);
                 }
                 String[] remainingArgs = commandLine.getArgs();
                 if (remainingArgs.length != 0) {
                     printHelp(System.err, options, null);
                     System.err.println("Expecting no command line arguments - got " + remainingArgs.length);
                     exitCode = 11;
                     break;
                 }
                 if (inputReader == null) {
                     inputReader = new InputStreamReader(System.in);
                 }
                 if (outputWriter == null) {
                     outputWriter = new OutputStreamWriter(System.out);
                 }
                 if (it == inputType.UNKNOWN) {
                     System.err.println("Unkown input type");
                     exitCode = 12;
                     break;
                 }
                 if (ot == outputType.UNKNOWN) {
                     System.err.println("Unkown output type");
                     exitCode = 13;
                     break;
                 }
                 main.parse(it, inputReader, ot, outputWriter);
                 break;
             } catch (ParseException e) {
                 System.err.println(NAME + ": Command line error - " + e.getMessage());
                 HelpFormatter helpFormatter = new HelpFormatter();
                 helpFormatter.printHelp(NAME, options);
                 exitCode = 10;
                 break;
             }
         }
         return exitCode;
     }
     
     void parse(inputType inputFormat, Reader inputReader, outputType outputType, Writer outputWriter) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
         DocumentBuilder builder = outputType.getBuilder(outputWriter);
         MarkupLanguage language = inputFormat.getMarkupLanguage();
         MarkupParser parser = new MarkupParser(language, builder);
         parser.parse(inputReader);
     }
     
     private static final String getExtension(String filename) {
         String[] tokens = filename.split("\\.(?=[^\\.]+$)");
         String result = "";
         if (tokens.length > 1) {
             result = tokens[1];
         }
         return result;
     }
     
     enum inputType {
         UNKNOWN("", null),
         TEXTILE("textile", new TextileLanguage()),
         TRAC("trac", new TracWikiLanguage()),
         MEDIAWIKI("mediawiki", new MediaWikiLanguage()),
         CONFLUENCE("confluence", new ConfluenceLanguage()),
         TWIKI("twiki", new TWikiLanguage()),
         MARKDOWN("md", new MarkdownLanguage());
         
         String inputTypeName;
         MarkupLanguage language;
         
         inputType(String name, MarkupLanguage language) {
             this.inputTypeName = name;
             this.language = language;
         }
         
         public MarkupLanguage getMarkupLanguage() {
             return language;
         }
 
         private final static inputType fromName(String name) {
             inputType it = inputType.UNKNOWN;
             for (inputType thisInputType : inputType.values()) {
                 if (thisInputType != inputType.UNKNOWN) {
                     if (name.equals(thisInputType.inputTypeName)) {
                         it = thisInputType;
                         break;
                     }
                 }
             }
             return it;
         }
     } // inputType
 
     enum outputType {
         UNKNOWN("", null),
         HTML("html", HtmlDocumentBuilder.class),
         DOCBOOK("docbook", DocBookDocumentBuilder.class),
         XSLFO("xslfo", XslfoDocumentBuilder.class);
         
         String outputTypeName;
         Class<?> builderClass;
         
         outputType(String name, Class<?> builderClass) {
             this.outputTypeName = name;
             this.builderClass = builderClass;
         }
         
         public DocumentBuilder getBuilder(Writer out) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
             Constructor<?> ctor = builderClass.getConstructor(Writer.class);
             DocumentBuilder db = (DocumentBuilder) ctor.newInstance(new Object[] { out });
             return db;
         }
         
         private final static outputType fromName(String name) {
             outputType ot = outputType.UNKNOWN;
             for (outputType thisOutputType : outputType.values()) {
                 if (thisOutputType != outputType.UNKNOWN) {
                     if (name.equals(thisOutputType.outputTypeName)) {
                         ot = thisOutputType;
                         break;
                     }
                 }
             }
             return ot;
         }
     } // outputType
 }

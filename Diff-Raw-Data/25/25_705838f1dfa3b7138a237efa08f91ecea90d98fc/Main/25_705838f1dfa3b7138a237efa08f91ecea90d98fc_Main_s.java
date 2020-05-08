 package org.uli.wikitext;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
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
 import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;
 import org.eclipse.mylyn.wikitext.markdown.core.MarkdownLanguage;
 import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
 import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
 import org.eclipse.mylyn.wikitext.tracwiki.core.TracWikiLanguage;
 import org.eclipse.mylyn.wikitext.twiki.core.TWikiLanguage;
 import org.uli.util.MyOptionBuilder;
 
 public class Main {
 
     static private final String NAME = "wikitext";
 
     // private boolean fSymbolicLink = false;
     // Taken from http://www.java-only.com/LoadTutorial.javaonly?id=115
     // Please note: The ordering of the parameters is similar to the
     // unix command "ln"
     public void ln(String to, String from, boolean fSymbolicLink) throws IOException {
         Path toPath = FileSystems.getDefault().getPath(to);
         Path fromPath = FileSystems.getDefault().getPath(from);
         if (fSymbolicLink) {
             Files.createSymbolicLink(fromPath, toPath);
         } else {
             Files.createLink(fromPath, toPath);
         }
     }
 
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
         Reader r = new FileReader(args[0]);
         Main main = new Main();
        System.out.println(main.parse(extensionEnum.MARKDOWN, builderEnum.HTML, r));
         if (1 == 1) {
             return 0;
         }
         Options options = new Options();
         Option h = MyOptionBuilder.init().withLongOpt("help").withDescription("print help").create("h");
         Option s = MyOptionBuilder.init().withLongOpt("symbolic").withDescription("create a symbolic link instead of a hard link").create("s");
         options.addOption(h);
         options.addOption(s);
         int exitCode = 0;
         boolean fHelp = false;
         boolean fSymbolicLink = false;
         Main ln = new Main();
         for (;;) {
             CommandLineParser commandLineParser = new PosixParser();
             try {
                 CommandLine commandLine = commandLineParser.parse(options, args);
                 fHelp = commandLine.hasOption("h");
                 if (fHelp) {
                     printHelp(System.out, options, null);
                     break;
                 }
                 fSymbolicLink = commandLine.hasOption("s");
                 String[] remainingArgs = commandLine.getArgs();
                 if (remainingArgs.length != 2) {
                     printHelp(System.err, options, null);
                     System.err.println("Expecting 2 command line arguments - got " + remainingArgs.length);
                     exitCode = 11;
                     break;
                 }
                 int idx = -1;
                 String to = remainingArgs[++idx];
                 String from = remainingArgs[++idx];
                 try {
                     ln.ln(to, from, fSymbolicLink);
                 } catch (IOException ioe) {
                     System.err.println(ioe);
                     exitCode = 1;
                 }
                 return exitCode;
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
     
    String parse(extensionEnum inputFormat, builderEnum outputFormat, Reader text) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
         StringWriter writer = new StringWriter();
        DocumentBuilder builder = outputFormat.getBuilder(writer);
         MarkupLanguage language = inputFormat.getMarkupLanguage();
         MarkupParser parser = new MarkupParser(language, builder);
         parser.parse(text);
         return writer.toString();
     }
     
    enum extensionEnum {
         TEXTILE("textile", new TextileLanguage()),
         TRAC("trac", new TracWikiLanguage()),
         MEDIAWIKI("mediawiki", new MediaWikiLanguage()),
         CONFLUENCE("confluence", new ConfluenceLanguage()),
         TWIKI("twiki", new TWikiLanguage()),
         MARKDOWN("md", new MarkdownLanguage());
         
        String extension;
         MarkupLanguage language;
         
        extensionEnum(String extension, MarkupLanguage language) {
            this.extension = extension;
             this.language = language;
         }
         
         public MarkupLanguage getMarkupLanguage() {
             return language;
         }
     }
     
    enum builderEnum {
         HTML("html", HtmlDocumentBuilder.class),
         DOCBOOK("docbook", DocBookDocumentBuilder.class),
         XSLFO("xslfo", XslfoDocumentBuilder.class);
         
         String name;
         Class<?> builderClass;
         
        builderEnum(String name, Class<?> builderClass) {
             this.name = name;
             this.builderClass = builderClass;
         }
         
         public DocumentBuilder getBuilder(Writer out) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
             Constructor<?> ctor = builderClass.getConstructor(Writer.class);
             DocumentBuilder db = (DocumentBuilder) ctor.newInstance(new Object[] { out });
             return db;
         }
     }
 }

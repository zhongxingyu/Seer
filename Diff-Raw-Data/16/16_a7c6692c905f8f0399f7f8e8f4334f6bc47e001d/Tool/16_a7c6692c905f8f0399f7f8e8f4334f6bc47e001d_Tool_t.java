 package name.vysoky.epub.cli;
 
 import name.vysoky.epub.SmartQuoter;
 import name.vysoky.xhtml.XhtmlTool;
 import nl.siegmann.epublib.domain.Book;
 import nl.siegmann.epublib.domain.Resource;
 import nl.siegmann.epublib.domain.Spine;
 import nl.siegmann.epublib.domain.SpineReference;
 import nl.siegmann.epublib.epub.EpubReader;
 import org.apache.commons.cli.*;
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 /**
  * Command line tool.
  * @author Jiri Vysoky
  */
 public class Tool {
 
     public static void main(String[] args) {
         CommandLineParser parser = new PosixParser();
         Options options = new Options();
         options.addOption(new Option("t", "text", false, "extractPlainText ePUB content as plain text"));
         options.addOption(new Option("h", "help", false, "print this message"));
         options.addOption(new Option("s", "simplify", false, "simplify text by converting special chars"));
         HelpFormatter formatter = new HelpFormatter();
         try {
             if (args.length == 0) throw new ParseException("Missing options!");
             CommandLine line = parser.parse(options, args);
             if (line.getArgList().size() == 0) throw new ParseException("Missing arguments!");
             if (line.hasOption("text")) {
                 String input = line.getArgList().get(0).toString();
                 String output = (line.getArgList().size() == 2) ? line.getArgList().get(1).toString() : null;
                 boolean simplify = line.hasOption("simplify");
                 extractText(input, output, simplify);
             }
         } catch (ParseException e) {
             System.err.println("Command line parsing failed. Reason: " + e.getMessage());
             formatter.printHelp("epubtool", options );
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static void extractText(String input, String output, boolean simplify) throws IOException {
         File epubFile = new File(input);
         File textFile = (output == null) ? null : new File(output);
         EpubReader reader = new EpubReader();
         StringBuilder stringBuilder = new StringBuilder();
         Book book = reader.readEpub(new FileInputStream(epubFile));
         Spine spine = book.getSpine();
         String string; // extracted text
         for (SpineReference spineReference : spine.getSpineReferences())  {
             Resource resource = spineReference.getResource();
             try {
                 XhtmlTool.extractPlainText(resource.getInputStream(), stringBuilder);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         string = stringBuilder.toString();
         if (simplify) {
             string = SmartQuoter.convertToDefaultQuotes(string);
             string = string.replaceAll("\u2013", "-"); // replace dash with minus
            string = string.replaceAll("\u00A0", " ");  // replace non breaking space with space
         }
         if (textFile == null) System.out.println(string);
         else FileUtils.writeStringToFile(textFile, string);
     }
 }

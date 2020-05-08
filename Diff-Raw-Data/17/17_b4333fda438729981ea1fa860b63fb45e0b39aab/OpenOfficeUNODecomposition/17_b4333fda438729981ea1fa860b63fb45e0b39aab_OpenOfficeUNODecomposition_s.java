 /*
  * OpenOfficeUNODecomposition.java
  *
  * Created on 2009.08.17 - 10:37:43
  *
  */
 
 package edu.umich.med.umms;
 
 import java.io.IOException;
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 
 import com.sun.star.frame.XDesktop;
 import com.sun.star.uno.XComponentContext;
 
 /**
  *
  * @author kwc@umich.edu
  */
 public class OpenOfficeUNODecomposition {
 
     public static final OOoFormat[] SUPPORTED_FORMATS = {
         OOoFormat.OpenDocument_Text,
         OOoFormat.Microsoft_Word_97_2000_XP,
         OOoFormat.Microsoft_Word_95,
         OOoFormat.Microsoft_Word_60,
         OOoFormat.Rich_Text_Format,
         //OOoFormat.Microsoft_Excel_97_2000_XP,
         //OOoFormat.Microsoft_Excel_95,
         //OOoFormat.Microsoft_Excel_50,
         //OOoFormat.OpenDocument_Spreadsheet,
         //OOoFormat.OpenOfficeorg_10_Spreadsheet,
         //OOoFormat.Text_CSV,
         OOoFormat.OpenDocument_Presentation,
         OOoFormat.OpenOfficeorg_10_Presentation,
         OOoFormat.Microsoft_PowerPoint_97_2000_XP,
         OOoFormat.Microsoft_PowerPoint_2007_XML
     };
 
     private static final String Usage =
             "[ --extract <options> | --replace <options> | --cite <options> | --copy <options> | --json <file>]\n" +
             "Global options: [--verbose] [--help] " +
             "--json <file> --json-result <file>\n" +
             "--extract [ --include-custom-shapes ] --input <file> --output-dir <dir>\n" +
             "--replace <options> --input <file> --newimage <file> --pagenum <pagenum> --imagenum <imagenum>\n" +
             "--cite <options> --input <file> --citeimage <file> --pagenum <pagenum> --imagenum <imagenum>\n" +
             "--copy <options> --input <file> --output <file> --boilerplateFile <file>";
 
     private com.spinn3r.log5j.Logger mylog = com.spinn3r.log5j.Logger.getLogger();
     private org.apache.log4j.Level myLogLevel = org.apache.log4j.Level.WARN;
 
     /** Creates a new instance of OpenOfficeUNODecomposition */
     public OpenOfficeUNODecomposition() {
     }
 
     public void setLoggingLevel(org.apache.log4j.Level lvl)
     {
         myLogLevel = lvl;
         mylog.setLevel(myLogLevel);
     }
 
     private DecompFileProcessor getFileProcessor(XComponentContext xContext, XDesktop xDesktop, String fileName)
     {
         DecompFileProcessor fp = null;
         try {
             fp = new DecompFileProcessor(xContext, xDesktop, fileName);
         } catch (Exception e) {
             mylog.error("Error instantiating input file '%s' for processing: %s", fileName, e.getMessage());
             return null;
             // System.exit(5);
         }
         fp.setLoggingLevel(myLogLevel);
         return fp;
     }
 
     private void saveFile(DecompParameters dp, DecompFileProcessor fp)
     {
         String outFile = dp.getOutputFile();
         try {
             if (outFile != null) {
                 fp.saveTo(outFile, dp.getBoilerPlateFile());
             }
         } catch (java.lang.Exception e) {
             mylog.error("Caught Exception while saving file");
         }
     }
 
 
     private void disposeFile(DecompFileProcessor fp)
     {
         try {
             fp.dispose();
         } catch (java.lang.Exception e) {
             mylog.error("Caught Exception while disposing file");
         }
     }
 
     private String processSingleFileOperation(DecompParameters dp, DecompFileProcessor fp)
     {
         return fp.doOperation(dp);
     }
 
     public String processSingleFile(XComponentContext xContext, XDesktop xDesktop, DecompParameters dp) {
         String ret = null;
         DecompFileProcessor fp = getFileProcessor(xContext, xDesktop, dp.getInputFile());
         if (fp == null)
             return new String("Error getting FileProcessor for file '" + dp.getInputFile() + "'");
 
         ret = processSingleFileOperation(dp, fp);
         saveFile(dp, fp);
         disposeFile(fp);
        
         return ret;
     }
 
     public String processJsonFile(XComponentContext xContext, XDesktop xDesktop, DecompParameters dp)
     {
         DecompJson dj = null;
         int ret = 1;
         String retstring = null;
         String resultJsonFileName = dp.getJsonResultFile();
         try {
             dj = new DecompJson(dp.getJsonCommandFile());
         } catch (java.lang.Exception e) {
             String msg = new String ("Exception while processing input JSON file " + dp.getJsonCommandFile() + ": " + e.getMessage());
             if (resultJsonFileName != null) {
                 dj = new DecompJson();
                 dj.setMainResult(1, msg);
                 dj.writeResults(resultJsonFileName);
             }
             return msg;
         }
 
         // Now process the commands...
         switch (dj.getVersion()) {
             case 1:
                 for (int i = 0; i < dj.getNumFiles(); i++) {
                     DecompFileProcessor fp = getFileProcessor(xContext, xDesktop, dj.getFile(i).getInputFile());
                     if (fp != null) {
                         for (int op = 0; op < dj.getFile(i).getNumOps(); op++) {
                             DecompParameters ldp = new DecompParameters(dj.getFile(i), op);
                             retstring = processSingleFileOperation(ldp, fp);
                             dj.getFile(i).getFileOp(op).setOperationResult((retstring == null) ? 0 : 1, retstring);
                         }
                         disposeFile(fp);
                     } else {
                         dj.getFile(i).setFileResult(1, "Error opening file" + dj.getFile(i).getInputFile());
                     }
                 }
                 break;
             default:
                 retstring = new String("Recieved unsupported JSON version number: " + dj.getVersion());
                 dj.setMainResult(1, retstring);
         }
         if (resultJsonFileName != null)
             dj.writeResults(resultJsonFileName);
         return retstring;
     }
 
     public void defineOptions(Options opt)
     {
         OptionGroup mainopts = new OptionGroup();
         mainopts.addOption(OptionBuilder.hasArg(false).withLongOpt("extract").create("e"));
         mainopts.addOption(OptionBuilder.hasArg(false).withLongOpt("replace").create("r"));
         mainopts.addOption(OptionBuilder.hasArg(false).withLongOpt("cite").create("t"));
         mainopts.addOption(OptionBuilder.hasArg(true).withLongOpt("json").create("j"));
         mainopts.addOption(OptionBuilder.hasArg(false).withLongOpt("copy").create("c"));
         opt.addOptionGroup(mainopts);
 
         opt.addOption("h", "help", false, "Print this usage information");
         opt.addOption("l", "loglevel", true, "Set logging level (off, fatal, error, warn, info, debug, all) default is warn");
         opt.addOption("nc", "include-custom-shapes", false, "Include Custom shapes while extracting images");
 
         opt.addOption("i", "input", true, "Input file name (full path)");
         opt.addOption("o", "output", true, "Output file name (full path)");
         opt.addOption("bp", "boilerplateFile", true, "File containing boilerplate slides (full path)");
         opt.addOption("n", "newimage", true, "Replacement (new) image file (full path)");
         opt.addOption("ti", "citeimage", true, "Image file containing citation information (full path)");
         opt.addOption("te", "citetext", true, "Full citation text");
         opt.addOption("d", "output-dir", true, "Name of directory to receive output");
         opt.addOption("m", "commandlist", true, "List of commands");
         opt.addOption("jr", "json-result", true, "Output JSON result file (ignored except when using json input)");
 
         opt.addOption("p", "pagenum", true, "Page number");
         opt.addOption("g", "imagenum", true, "Image number");
     }
 
     public int processArguments(Options opt, String[] args, DecompParameters dp)
     {
         CommandLine cl;
         try {
             BasicParser parser = new BasicParser();
             cl = parser.parse(opt, args);
         } catch (ParseException ex) {
             mylog.error("Error processing arguments: '%s'", ex.getMessage());
             return 1;
         }
 
         if (cl.hasOption("l")) {
             String levelDesired = cl.getOptionValue("l").toString();
 
             if (!dp.setLoggingLevel(levelDesired)) {
                 mylog.warn("Loglevel " + levelDesired + " is not a valid choice.  Ignored.");
             }
             myLogLevel = dp.getLoggingLevel();
         }
 
         if (cl.hasOption("h")) {
             printUsage(opt);
             return 8;
         }
 
         if (!cl.hasOption("e") && !cl.hasOption("r") && !cl.hasOption("c")
                 && !cl.hasOption("t") && !cl.hasOption("j")) {
             mylog.error("You must choose a main function");
             printUsage(opt);
             return 2;
         }
 
         /* JSON Commands */
         if (cl.hasOption("j")) {
             dp.setOperation(DecompOperation.JSON);
             dp.setJsonCommandFile(cl.getOptionValue("j"));
             if (cl.hasOption("jr")) {
                 dp.setJsonResultFile(cl.getOptionValue("jr"));
             }
         }
 
         /* extract */
         if (cl.hasOption("e")) {
             dp.setOperation(DecompOperation.EXTRACT);
             dp.setInputFile(cl.getOptionValue("i"));
             dp.setOutputDir(cl.getOptionValue("d"));
 
             if (cl.hasOption("nc")) {
                 dp.setIncludeCustomShapes(true);
             }
         }
 
         /* replace */
         if (cl.hasOption("r")) {
             dp.setOperation(DecompOperation.REPLACE);
             dp.setInputFile(cl.getOptionValue("i"));
             dp.setOutputFile(cl.getOptionValue("o"));
             dp.setRepImageFile(cl.getOptionValue("n"));
             if (cl.hasOption("p"))
                 dp.setPageNum(Integer.parseInt(cl.getOptionValue("p")));
             if (cl.hasOption("g"))
                 dp.setImageNum(Integer.parseInt(cl.getOptionValue("g")));
 
             dp.adjustIndexes();
         }
 
         /* citation */
         if (cl.hasOption("t")) {
             dp.setOperation(DecompOperation.CITE);
             dp.setInputFile(cl.getOptionValue("i"));
             dp.setOutputFile(cl.getOptionValue("o"));
             dp.setCitationImageFile(cl.getOptionValue("ti"));
             dp.setCitationText(cl.getOptionValue("te"));
             if (cl.hasOption("p"))
                 dp.setPageNum(Integer.parseInt(cl.getOptionValue("p")));
             if (cl.hasOption("g"))
                 dp.setImageNum(Integer.parseInt(cl.getOptionValue("g")));
 
             dp.adjustIndexes();
         }
 
         /* copy */
         if (cl.hasOption("c")) {
             dp.setOperation(DecompOperation.SAVE);
             dp.setInputFile(cl.getOptionValue("i"));
             dp.setOutputFile(cl.getOptionValue("o"));
         }
 
         /* verify things are valid for the given function */
         if (!dp.Valid()) {
             System.out.println(dp.getValidationErrorMessage());
             printUsage(opt);
             return 7;
         }
 
         return 0;
     }
 
     private void printUsage(Options opt)
     {
         HelpFormatter hf = new HelpFormatter();
         hf.setWidth(80);
         hf.printHelp(Usage, opt);
     }
 
 }

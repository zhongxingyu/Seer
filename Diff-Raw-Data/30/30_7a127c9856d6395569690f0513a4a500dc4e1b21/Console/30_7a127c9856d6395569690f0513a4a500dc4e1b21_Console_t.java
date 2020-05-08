 package mcmapconverter.cli;
 
 import java.io.File;
 import java.io.PrintStream;
 import mcmapconverter.Converter;
 import mcmapconverter.ConverterUI;
 import mcmapconverter.MapFormat;
 
 public class Console implements ConverterUI {
     private Converter converter;
     
     public Console(Converter converter, String[] args) {
         this.converter = converter;
         parseArgs(args);
         converter.convert(this);
     }
 
     private void parseArgs(String[] args) {
         String inFormatName = null, outFormatName = null, inFileName = null, outFileName = null;
         File outFile, inFile;
        MapFormat inFormat = null, outFormat = null;
         
         try {
             try {
                 for(int i = 0;i < args.length;i++) {
                     String arg;
 
                     arg = args[i];
                     if(arg.equals("-in")) {
                         inFormatName = args[++i];
                     } else if(arg.equals("-out")) {
                         outFormatName = args[++i];
                     } else if(arg.equals("-help")) {
                         printHelp(System.out);
                         System.exit(0);
                     } else if(arg.equals("-formats")) {
                         listFormats(System.out);
                         System.exit(0);
                     } else if(inFileName == null) {
                         inFileName = arg;
                     } else if(outFileName == null) {
                         outFileName = arg;
                     } else {
                         throw new BadArgumentsException("Unknown argument.");
                     }
                 }
                 if(inFileName == null)
                     throw new BadArgumentsException("No input file specified");
                 if(outFileName == null)
                     throw new BadArgumentsException("no output file specified");
             } catch(ArrayIndexOutOfBoundsException e) {
                 throw new BadArgumentsException("Missing argument", e);
             }
         } catch(BadArgumentsException e) {
             System.err.println(e.getMessage());
             printHelp(System.err);
             System.exit(0);
         }
 
         inFile = new File(inFileName);
         outFile = new File(outFileName);
        if(inFormatName != null)
            inFormat = converter.getMapFormat(inFormatName);
        if(outFormatName != null)
            outFormat = converter.getMapFormat(outFormatName);
 
         converter.setInputFile(inFile);
         converter.setOutputFile(outFile);
        if(inFormat != null)
            converter.setInputFormat(inFormat);
        if(outFormat != null)
            converter.setOutputFormat(outFormat);
     }
 
     private void printHelp(PrintStream out) {
        out.println("Usage:\n"
                  + "  inputFile outputFile\n"
                  + "  -in <format> -out <format> inputFile outputFile\n"
                  + "      Convert inputFile to outputFile\n"
                  + "  -formats\n"
                  + "      List file formats\n"
                  + "  -help\n"
                  + "      Command help");
     }
 
     private void listFormats(PrintStream out) {
         Iterable<MapFormat> formats;
 
         formats = converter.getMapFormats();
         out.println("Map formats:");
         for(MapFormat format : formats) {
             out.println(format.getName() + " - " + format.getDescription());
         }
     }
     
     public void displayInputFileError(String message) {
         System.err.println(message);
         printHelp(System.err);
     }
 
     public void displayOutputFileError(String message) {
         System.err.println(message);
         printHelp(System.err);
     }
 
     public void displayInputFormatError(String message) {
         System.err.println(message);
         System.err.println("Please specify an input file format with -in <format>");
         printHelp(System.err);
     }
 
     public void displayOutputFormatError(String message) {
         System.err.println(message);
         System.err.println("Please specify an output file format with -out <format>");
         printHelp(System.err);
     }
 
     public void displayError(String message) {
         System.err.println(message);
         printHelp(System.err);
     }
 
     public void displaySuccess() {
         System.out.println("Map successfully converted");
     }
 }

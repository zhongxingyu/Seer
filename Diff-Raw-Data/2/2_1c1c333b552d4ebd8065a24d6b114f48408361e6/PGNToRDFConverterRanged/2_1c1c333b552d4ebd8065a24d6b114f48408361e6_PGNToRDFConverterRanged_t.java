 /**
  * PGNToRDFConverterRanged.java
  */
 package de.uni_leipzig.informatik.swp13_sc.converter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.IllegalFormatException;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipOutputStream;
 
 import de.uni_leipzig.informatik.swp13_sc.converter.ChessDataModelToRDFConverter.OutputFormats;
 import de.uni_leipzig.informatik.swp13_sc.util.FileUtils;
 
 /**
  * A PGN converter implementation.<br />
  * It takes a number which determines the split rate while outputting the
  * games into a archive.
  *
  * @author Erik
  *
  */
 public class PGNToRDFConverterRanged
 {
     // Commandline arguments
     private final static String PARAM_SPLIT_NUMBER = "--split";
     //private final static String PARAM_PROCESSING_NUMBER = "--processsplit";
     private final static String PARAM_COMPRESS_LEVEL = "--compresslevel";
     private final static String PARAM_DO_NOT_COMPRESS = "--dontcompress";
     private final static String PARAM_OUTPUT_FORMAT = "--format";
     private final static String PARAM_HELP = "--help";
     
     // Converter standard parameters
     private int count = 1000; // default split rate for outputting
     //private int procCount = count; // default split rate while parsing
     private int compressLevel = 9; // highest compression
     private boolean compressToZip = true;
     private OutputFormats outFormat = OutputFormats.TURTLE;
     private List<String> files = new ArrayList<String>();
     
     // ------------------------------------------------------------------------
     
     /**
      * Base constructor
      */
     public PGNToRDFConverterRanged()
     {
         // see above
     }
     
     // ------------------------------------------------------------------------
     // Converter properties
     
     /**
      * Sets the split rate for outputting the rdf chess game data.
      * 
      * @param   count   rate for splitting, if less or equal to 0 it will
      *                  output all together.
      */
     public void setSplitRate(int count)
     {
         this.count = count;
         
         // 0 makes no sense
         // less means all
         if (this.count <= 0)
         {
             this.count = PGNToChessDataModelConverter.ALL_GAMES;
         }
     }
     
     /**
      * Sets the compression level for zip output files. The level can be
      * an integer value between 0 and 9. 0 meaning no compression at all
      * and 9 the highest level of compression. If a value less than 0 is
      * given it will result in no zip file at all - it will be the normal
      * uncompressed content.
      * 
      * @param   level   Value between 0 and 9. Less 0 equals --dontcompress!
      */
     public void setCompressionLevel(int level)
     {
         this.compressLevel = level;
         
         if (this.compressLevel > 9)
         {
             this.compressLevel = 9;
         }
         if (this.compressLevel <= -1)
         {
             this.compressLevel = -1;
             this.compressToZip = false;
         }
     }
     
     /**
      * Enables or Disables the compression of the output file(s) into a
      * Zip-Archive.
      * 
      * @param   enabled Enable Zip or not.
      */
     public void setCompressionEnabled(boolean enabled)
     {
         this.compressToZip = enabled;
     }
     
     /**
      * Parses the parameter String and tries to set the output format. If an
      * error occurs it will take the last value set.
      * 
      * @param   format  output format or if wrong it will be set to default
      */
     public void setOutputFormat(String format)
     {
         for (OutputFormats fo : OutputFormats.values())
         {
             if (fo.getFormat().equalsIgnoreCase(format))
             {
                 this.setOutputFormat(fo);
                 break;
             }
         }
     }
     
     /**
      * Sets the output format or the default format if incorrect.
      * 
      * @param   format  {@link ChessDataModelToRDFConverter.OutputFormats}
      */
     public void setOutputFormat(OutputFormats format)
     {
         if (format == null)
         {
             this.outFormat = OutputFormats.TURTLE;                    
         }
         else
         {
             this.outFormat = format;
         }
     }
     
     // ------------------------------------------------------------------------
     // Processing files and arguments ...
     
     /**
      * Entry point of this converter. Spins everything off.
      * 
      * @param   args    arguments from command line
      */
     public void start(String[] args)
     {
         this.parseArguments(args);
         this.processFiles();
     }
     
     /**
      * Parses the input arguments.
      * 
      * @param   args    arguments from command line
      */
     private void parseArguments(String[] args)
     {
         System.out.print("Parsing arguments ...");
         for (int i = 0; i < args.length; i ++)
         {
             String arg = args[i];
             if (arg.startsWith(PARAM_SPLIT_NUMBER))
             {
                 int j = arg.indexOf('=');
                 if (j != -1)
                 {
                     String number = arg.substring(j + 1);
                     try
                     {
                         this.setSplitRate(Integer.parseInt(number));
                     }
                     catch (NumberFormatException e)
                     {
                         // ignore
                     }
                 }
             }
             else if (arg.startsWith(PARAM_COMPRESS_LEVEL))
             {
                 int j = arg.indexOf('=');
                 if (j != -1)
                 {
                     String number = arg.substring(j + 1);
                     try
                     {
                         this.setCompressionLevel(Integer.parseInt(number));
                     }
                     catch (NumberFormatException e)
                     {
                         // ignore
                     }
                 }
             }
             else if (arg.startsWith(PARAM_DO_NOT_COMPRESS))
             {
                 this.setCompressionEnabled(false);
             }
             else if (arg.startsWith(PARAM_OUTPUT_FORMAT))
             {
                 int j = arg.indexOf('=');
                 if (j != -1)
                 {
                     this.setOutputFormat(arg.substring(j + 1));
                 }
             }
             else if (arg.startsWith(PARAM_HELP))
             {
                 printHelp(); 
             }
             else
             {
                 // possible files starting with --XxX..
                 this.files.add(arg);
             }
         }        
         System.out.println(" finished.");
         
         // Print the parameters of this parser after parsing (commandline) args        
         System.out.println("  Using split number of:      " + count +
                 ((count == PGNToChessDataModelConverter.ALL_GAMES)?"ALL_GAMES":""));
         System.out.println("  Using compression level of: " + compressLevel);
         System.out.println("  Using compression:          " + compressToZip);
         System.out.println("  Using output format:        " + outFormat.getFormat()
                 + "(" + outFormat.getExtension() + ")");
         System.out.println();
     }
     
     /**
      * Prints the help string and exits the program after that.
      */
     private void printHelp()
     {
         System.out.println();
         System.out.println("Help to PGNToRDFConverter[Ranged]");
         System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         System.out.println("Usage:");
         System.out.println("\tjava -jar <archiv>.jar [options] <file> [<files> ...]");
         System.out.println();
         System.out.println("\t" + PARAM_HELP + "\t\t\tPrints this message.");
         System.out.println("\t" + PARAM_SPLIT_NUMBER + "=<number>\tSplitts the output"
                 + " file / zip entries after\n\t\t\t\t<number> processed chess games."
                 + "\n\t\t\t\tIf the <number> is -1 it will output all.\n\t\t\t\t"
                 + "level:\t0 == STORE ... 9 == HIGHEST");
         System.out.println("\t" + PARAM_COMPRESS_LEVEL + "=<0-9>\tSets the compression"
                 + " level of the zip output\n\t\t\t\tarchive. If it is -1 it won't compress.");
         System.out.println("\t" + PARAM_DO_NOT_COMPRESS + "\t\tForces the programm to"
                 + " output all data into a\n\t\t\t\tuncompressed state.\n\t\t\t\t"
                 + PARAM_COMPRESS_LEVEL + " == -1!");
         /*System.out.println("\t" + PARAM_OUTPUT_FILE + "=<file>\t\tUses the specified"
                 + " output file if only a single\n\t\t\t\tinput file exists. If there"
                 + " are more input\n\t\t\t\tfiles this parameter will be ignored.");*/
         System.out.print("\t" + PARAM_OUTPUT_FORMAT + "=<format>\tUses the specified"
                 + "output format for the RDF\n\t\t\t\tdata. The standard format is TURTLE."
                 + "\n\t\t\t\tAvailable output formats are:\n\t\t\t\t");
         System.out.println("{TURTLE, N-TRIPLE, RDF/XML, RDF/XML-ABBREV}");
         System.out.println();
         System.out.println("Examples:");
         System.out.println("- If the archive name is PGNConverter.jar and the file separator '/' ...");
         System.out.println("#> java -jar PGNConverter.jar dir1/dir2/file.pgn");
         System.out.println("#> java -jar PGNConverter.jar dir1/dir2/*.pgn");
         System.out.println("#> java -jar PGNConverter.jar file.pgn file2.pgn");
         System.out.println("#> java -jar PGNConverter.jar --split=250 file.pgn");
         System.out.println("#> java -jar PGNConverter.jar --format=RDF/XML file.pgn");
         
         // try exiting
         try
         {
             System.exit(0);
         }
         catch (SecurityException e)
         {
             e.printStackTrace();
         }
     }
     
     /**
      * Processes the input files. Parse & Convert.
      */
     private void processFiles()
     {
         if (files.size() == 0)
         {
             System.out.println("No input files! ... Exiting!");
             printHelp();
         }
         
         long startProcessing = System.currentTimeMillis();
         
         for (int i = 0; i < files.size(); i ++)
         {
             System.out.println("Processing input file " + (i+1) + "/" + files.size() + ".");
             
             // ----------------------------------------------------------------
             // get file names
             String input = files.get(i);
             String ext = null;
             if (input.indexOf('.') != -1)
             {
                 ext = input.substring(input.lastIndexOf('.') + 1);
             }            
             String output;
             // multiple input files -> multiple output files ...
             if (ext == null)
             {
                 output = input;
             }
             else
             {
                 output = input.substring(0, input.lastIndexOf('.'));
             }
             output = output + "." + outFormat.getExtension();
             
             
             System.out.println("Converting PGN-File <" + input
                     + "> to RDF (" + outFormat.getFormat() + ") <" +
                     output + "> ...");
             
             long startFile = System.currentTimeMillis();
             
             // ----------------------------------------------------------------
                         
             if (this.compressToZip)
             {
                 this.processToZipStream(input, output);
             }
             else
             {
                 this.processToStream(input, output);
             }
             
             // ----------------------------------------------------------------
             
             System.out.println("Finished input file. Took " + 
                     ((System.currentTimeMillis() - startFile) / 1000.0) +
                     " seconds.");
             
             
             // ----------------------------------------------------------------            
             
         }
         
         System.out.println("Took " + ((System.currentTimeMillis() - startProcessing) / 1000.0) + " seconds total.");
     }
     
     // ------------------------------------------------------------------------
     // Processing single files
     
     /**
      * "OutputHandler" for using Zip files.
      * 
      * @param   input   input filename
      * @param   output  output filename
      * @return  true if successful else false
      */
     protected boolean processToZipStream(String input, String output)
     {
         String outputZip = output + ".zip";
         
         // get relative path
         String relPath = output;
         try
         {
             File inputFile = new File(input);
             if (! inputFile.exists())
             {
                 System.out.println("Input file <" + input + "> doesn't exist!");
                 System.out.println();
                 return false;
             }
             File parentFile = inputFile.getParentFile();
             
             if (parentFile != null)
             {
                 relPath = parentFile.toURI().relativize(inputFile.toURI()).getPath();
             }
         }
         catch (NullPointerException e)
         {
             e.printStackTrace();
         }
         // without extension
         relPath = (relPath.indexOf('.') != -1) ?
                 relPath.substring(0, relPath.lastIndexOf('.')) : relPath;
         
         // --------------------------------------------------------------------
         
         // open outputfile
         FileOutputStream fos = FileUtils.openOutputStream(outputZip);
         if (fos == null)
         {
             return false;
         }
         ZipOutputStream zos = new ZipOutputStream(fos);
         zos.setLevel(compressLevel);
         
         // --------------------------------------------------------------------
         
         // parse & convert
         long startFile = System.currentTimeMillis();
         PGNToChessDataModelConverter pgn2cdm = new PGNToChessDataModelConverter();
         ChessDataModelToRDFConverter cdm2rdf = new ChessDataModelToRDFConverter();
         
         pgn2cdm.setInputFilename(input);
         
         int nr = 0;
         while (! pgn2cdm.finishedInputFile())
         {
             nr ++;
             System.out.println("  Working on Part: " + nr);
             
             long start = System.currentTimeMillis();
             // Parse only count games.
             System.out.print("    Parsing data ...");
             pgn2cdm.parse(count);
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
             
             start = System.currentTimeMillis();
             // Convert all the parsed games in memory.
             System.out.print("    Converting data ...");
             cdm2rdf.convert(pgn2cdm.getGames());
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
             
             // Output all the converted games.
             // compute entry name
             String entryName = relPath;
             if ((nr != 1) || (! pgn2cdm.finishedInputFile()))
             {
                 // two or more entries
                 try
                 {
                     entryName = String.format("%s.part_%03d", entryName, nr);
                 }
                 catch (IllegalFormatException e)
                 {
                     e.printStackTrace();
                 }
                 catch (NullPointerException e)
                 {
                     e.printStackTrace();
                 }
             }
             entryName = entryName + "." + outFormat.getExtension();
             
             start = System.currentTimeMillis();
             System.out.print("    Writing Zip-Archive-Entry: " + entryName + " ...");
             
             // generate entry
             try
             {
                 ZipEntry ze = new ZipEntry(entryName);
                 zos.putNextEntry(ze);
                 
                 // write to stream
                 cdm2rdf.write(zos, outFormat);
                 
                 zos.closeEntry();
                 zos.flush();
             }
             catch (Exception e)
             {
                 // NullPointerException
                 // IllegalArgumentException
                 // ZipException
                 // IOException
                 e.printStackTrace();
             }
             
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
         }
         
         // write the generate chess game names into a file
         try
         {
             ZipEntry ze = new ZipEntry("generateChessGameName.txt");
             zos.putNextEntry(ze);
             cdm2rdf.writeConvertedGameNames(zos);
             zos.closeEntry();
             zos.flush();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         
         System.out.println("  Processed " + pgn2cdm.numberOfParsedGames() +
                 " games in " + ((System.currentTimeMillis() - startFile) / 1000.0) +
                 " seconds. Wrote " + nr + " part(s) to Zip-Archive " + outputZip + ".");
         
         // --------------------------------------------------------------------
         // close output file
         try
         {
             zos.close();
         }
         catch (ZipException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return true;
     }
     
     /**
      * "OutputHandler" for normal FileOutputStreams. Processes the input file
      * and writes it into output.
      * 
      * @param   input   input filename
      * @param   output  output filenam
      * @return  true if successful else false
      */
     protected boolean processToStream(String input, String output)
     {
         // parse & convert
         long startFile = System.currentTimeMillis();
         PGNToChessDataModelConverter pgn2cdm = new PGNToChessDataModelConverter();
         ChessDataModelToRDFConverter cdm2rdf = new ChessDataModelToRDFConverter();
         pgn2cdm.setInputFilename(input);
         
         int nr = 0;
         while (! pgn2cdm.finishedInputFile())
         {
             nr ++;
             System.out.println("  Working on Part: " + nr);
             
             long start = System.currentTimeMillis();
             System.out.print("    Parsing data ...");
             pgn2cdm.parse(count);
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
             
             start = System.currentTimeMillis();
             System.out.print("    Converting data ...");
             cdm2rdf.convert(pgn2cdm.getGames());
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
             
             // compute file part name
             // without extension
             String filePartName = (output.indexOf('.') != -1) ?
                     output.substring(0, output.lastIndexOf('.')) : output;
             if ((nr != 1) || (! pgn2cdm.finishedInputFile()))
             {
                 // two or more entries
                 try
                 {
                     filePartName = String.format("%s.part_%03d", filePartName, nr);
                 }
                 catch (IllegalFormatException e)
                 {
                     e.printStackTrace();
                 }
                 catch (NullPointerException e)
                 {
                     e.printStackTrace();
                 }
             }
             filePartName = filePartName + "." + outFormat.getExtension();
             
             start = System.currentTimeMillis();
             System.out.print("    Writing File-Part: " + filePartName + " ...");
             
             // generate new OutputStream
             try
             {
                 // open outputfile
                FileOutputStream fos = FileUtils.openOutputStream(filePartName);
                 if (fos == null)
                 {
                     System.out.println("    Couldn't open output file <" +
                             filePartName + "> ! Skipping outputting data!");
                     continue;
                     //return false;
                 }
                 
                 // write to stream
                 cdm2rdf.write(fos, outFormat);
                 
                 fos.flush();
                 fos.close();
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
             
             System.out.println(" finished. (" +
                     ((System.currentTimeMillis() - start) / 1000.0) + " s)");
         }
         
         System.out.println("  Processed " + pgn2cdm.numberOfParsedGames() +
                 " games in " + ((System.currentTimeMillis() - startFile) / 1000.0) +
                 " seconds. Wrote " + nr + " File(-Parts).");
         
         return true;
     }
 }

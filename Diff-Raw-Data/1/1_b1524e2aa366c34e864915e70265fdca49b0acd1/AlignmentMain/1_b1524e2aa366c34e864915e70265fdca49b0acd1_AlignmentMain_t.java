 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.common.distance.QUASARDistanceMatrixFactory;
 import de.bioinformatikmuenchen.pg4.common.sequencesource.ISequenceSource;
 import de.bioinformatikmuenchen.pg4.common.sequencesource.SequenceLibrarySequenceSource;
 import de.bioinformatikmuenchen.pg4.alignment.gap.AffineGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.AlignmentOutputFormatFactory;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.alignment.pairfile.PairfileEntry;
 import de.bioinformatikmuenchen.pg4.alignment.pairfile.PairfileParser;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 /**
  * Hello world!
  *
  */
 public class AlignmentMain {
 
     private AlignmentAlgorithm algorithm = null;
     private AlignmentMode mode = null;
     private AlignmentOutputFormat outputFormat = null;
 
     public AlignmentMain(String[] args) throws IOException {
         //Which opts are available
         final Options opts = new Options();
         opts.addOption("g", "go", true, "Gap open")
                 .addOption("h", "help", false, "Print help")
                 .addOption("e", "ge", true, "gapextend")
                 .addOption("d", "dpmatrices", true, "Output dynamic programming matrices to directory")
                 .addOption("p", "pairs", true, "Path to pairs file")
                 .addOption("s", "seqlib", true, "seqlibfile")
                 .addOption("m", "matrixname", true, "matrixname")
                 .addOption("s", "mode", true, "mode")
                 .addOption("u", "nw", false, "Use Needleman-Wunsch")
                 .addOption("c", "check", true, "Calculate checkscores")
                 .addOption("f", "format", true, "format");
         //Parse the opts
         final CommandLineParser cmdLinePosixParser = new PosixParser();
         CommandLine commandLine = null;
         try {
             commandLine = cmdLinePosixParser.parse(opts, args);
         } catch (ParseException parseException) {    // checked exception  
             System.err.println(
                     "Encountered exception while parsing using PosixParser:\n"
                     + parseException.getMessage());
            System.exit(1);
         }
         //
         //Check if the options are valid
         //
         //Check help
         if (commandLine.hasOption("help")) {
             HelpFormatter formatter = new HelpFormatter();
 //            formatter.printHelp(new PrintWriter(System.err), 100, "gnu", "alignment.jar", opts, 4, 4, "Exiting...");
             formatter.printHelp("alignment.jar", opts);
             System.out.println("--help supplied, exiting...");
             System.exit(1);
         }
         //gapopen
         double gapOpen = Double.NaN;
         try {
             if (commandLine.hasOption("go")) {
                 gapOpen = Double.parseDouble(commandLine.getOptionValue("go"));
             } else { //Default
                 gapOpen = -12;
             }
         } catch (NumberFormatException ex) {
             System.err.println("--go takes a number, not " + commandLine.getOptionValue("go") + "!");
             System.exit(1);
         }
         //ge
         double gapExtend = Double.NaN;
         try {
             if (commandLine.hasOption("ge")) {
                 gapExtend = Double.parseDouble(commandLine.getOptionValue("ge"));
             } else { //Default
                 gapExtend = -1;
             }
         } catch (NumberFormatException ex) {
             System.err.println("--ge takes a number, not " + commandLine.getOptionValue("ge") + "!");
             System.exit(1);
         }
         //check --dpmatrices
         File dpMatrixDir = null;
         if (commandLine.hasOption("dpmatrices")) {
             dpMatrixDir = new File(commandLine.getOptionValue("dpmatrices"));
             if (dpMatrixDir.exists() && !dpMatrixDir.isDirectory()) {
                 System.err.println("Error: --dpmatrices argument exists and is not a directory");
                 System.exit(1);
             }
             if (!dpMatrixDir.exists()) {
                 dpMatrixDir.mkdirs();
             }
         }
         //check --pairs
         File pairsFile = null;
         if (commandLine.hasOption("pairs")) {
             pairsFile = new File(commandLine.getOptionValue("pairs"));
             if (!pairsFile.exists() || pairsFile.isDirectory()) {
                 System.err.println("Error: --pairs argument is not a file or does not exist!");
                 System.exit(1);
             }
         } else {
             System.err.println("--pairs is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //check --seqlib
         File seqLibFile = null;
         if (commandLine.hasOption("seqlib")) {
             seqLibFile = new File(commandLine.getOptionValue("seqlib"));
             if (!seqLibFile.exists() || !seqLibFile.isFile()) {
                 System.err.println("Error: --seqlib argument is not a file or does not exist!");
                 System.exit(1);
             }
         } else {
             System.err.println("--seqlib is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //matrixname
         File substitutionMatrixFile = null;
         if (commandLine.hasOption("matrixname")) {
             substitutionMatrixFile = new File(commandLine.getOptionValue("matrixname"));
             if (!substitutionMatrixFile.exists()) {
                 System.err.println("Error: --matrixname argument '" + substitutionMatrixFile + "' does not exist!");
                 System.exit(1);
             } else if (!substitutionMatrixFile.isFile()) {
 
                 System.err.println("Error: --matrixname argument is not a file!");
                 System.exit(1);
             }
         } else {
             System.err.println("--matrixname is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //mode
         String modeString = commandLine.getOptionValue("mode").toLowerCase();
         if (modeString != null) {
             if (modeString.equalsIgnoreCase("local")) {
                 mode = AlignmentMode.LOCAL;
             } else if (modeString.equalsIgnoreCase("global")) {
                 mode = AlignmentMode.GLOBAL;
             } else if (modeString.equalsIgnoreCase("freeshift")) {
                 mode = AlignmentMode.FREESHIFT;
             } else {
                 System.err.println("Error: --mode argument " + modeString + " is invalid!");
                 System.exit(1);
             }
         } else {
             System.err.println("--matrixname is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //mode
         if (!commandLine.hasOption("format")) {
             System.err.println("No --format option given (it's mandatory)");
             System.exit(1);
         } else { //We have a format option
             String outputFormatString = commandLine.getOptionValue("format").toLowerCase();
             if (outputFormatString != null) {
                 if (outputFormatString.equalsIgnoreCase("ali")) {
                     outputFormat = AlignmentOutputFormat.ALI;
                 } else if (outputFormatString.equalsIgnoreCase("html")) {
                     outputFormat = AlignmentOutputFormat.HTML;
                 } else if (outputFormatString.equalsIgnoreCase("scores")) {
                     outputFormat = AlignmentOutputFormat.SCORES;
                 } else {
                     System.err.println("Error: --format argument " + outputFormatString + " is invalid!");
                     System.exit(1);
                 }
             } else {
                 System.err.println("Error: --format is mandatory");
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("alignment.jar", opts);
                 System.exit(1);
             }
         }
         //algorithm
         if (commandLine.hasOption("nw")) {
             algorithm = (mode == AlignmentMode.LOCAL ? AlignmentAlgorithm.SMITH_WATERMAN : AlignmentAlgorithm.NEEDLEMAN_WUNSCH);
         } else {
             algorithm = AlignmentAlgorithm.GOTOH;
         }
         //
         //Inter-argument cheks
         //
         boolean haveAffineGapCost = (gapOpen - gapExtend) > 0.00000001;
         //TODO TEMPORARY assertion until it's impl
         assert !haveAffineGapCost;
         //
         // Read & Collect helper objects
         //
         //
         IAlignmentOutputFormatter formatter = AlignmentOutputFormatFactory.factorize(outputFormat);
         ISequenceSource sequenceSource = new SequenceLibrarySequenceSource(seqLibFile.getAbsolutePath());
         Collection<PairfileEntry> pairfileEntries = PairfileParser.parsePairfile(pairsFile.getAbsolutePath());
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(substitutionMatrixFile.getAbsolutePath());
         IGapCost gapCost = (haveAffineGapCost ? new AffineGapCost(gapOpen, gapExtend) : new ConstantGapCost(gapOpen));
         //Create the processor
         AlignmentProcessor proc = AlignmentProcessorFactory.factorize(mode, algorithm, matrix, gapCost);
         for (PairfileEntry entry : pairfileEntries) {
             //Get the sequences
             Sequence seq1 = sequenceSource.getSequence(entry.first);
             Sequence seq2 = sequenceSource.getSequence(entry.second);
             AlignmentResult result = proc.align(seq1, seq2);
             //Print all alignments (usually one)
             for (SequencePairAlignment alignment : result.getAlignments()) {
                 formatter.formatAndPrint(result);
             }
         }
     }
 
     public static void main(String[] args) throws IOException {
         new AlignmentMain(args);
     }
 }

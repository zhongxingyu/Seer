 package org.broadinstitute.sting.gatk;
 
 import edu.mit.broad.picard.reference.ReferenceSequence;
 import edu.mit.broad.picard.reference.ReferenceSequenceFile;
 import edu.mit.broad.picard.reference.ReferenceSequenceFileFactory;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.samtools.SAMSequenceRecord;
 import net.sf.samtools.util.RuntimeIOException;
 import org.apache.log4j.Logger;
 import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedData;
 import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedDatum;
 import org.broadinstitute.sting.gatk.refdata.rodDbSNP;
 import org.broadinstitute.sting.gatk.refdata.rodGFF;
 import org.broadinstitute.sting.gatk.refdata.HapMapAlleleFrequenciesROD;
 import org.broadinstitute.sting.gatk.walkers.LocusWalker;
 import org.broadinstitute.sting.gatk.walkers.ReadWalker;
 import org.broadinstitute.sting.gatk.walkers.Walker;
 import org.broadinstitute.sting.gatk.traversals.*;
 import org.broadinstitute.sting.utils.FastaSequenceFile2;
 import org.broadinstitute.sting.utils.GenomeLoc;
 import org.broadinstitute.sting.utils.Utils;
 import org.broadinstitute.sting.utils.cmdLine.CommandLineProgram;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class GenomeAnalysisTK extends CommandLineProgram {
     public static GenomeAnalysisTK Instance = null;
 
     // parameters and their defaults
     public File INPUT_FILE = null;
     public String MAX_READS_ARG = "-1";
     public String STRICTNESS_ARG = "strict";
     public File REF_FILE_ARG = null;
     public String DEBUGGING_STR = null;
     public String REGION_STR = null;
     public String Analysis_Name = null;
     public String DBSNP_FILE = null;
     public String HAPMAP_FILE = null;
     public String HAPMAP_CHIP_FILE = null;
     public Boolean ENABLED_THREADED_IO = false;
     public Boolean UNSAFE = false;
     public String MAX_ON_FLY_SORTS = null;
     public String DOWNSAMPLE_FRACTION = null;
     public String DOWNSAMPLE_COVERAGE = null;
     public String INTERVALS_FILE = null;
 
     // our walker manager
     private WalkerManager walkerManager = null;
 
     public String pluginPathName = null;
     private TraversalEngine engine = null;
     public boolean DEBUGGING = false;
     public Boolean WALK_ALL_LOCI = false;
 
     /**
      * An output file presented to the walker.
      */
     public String outFileName = null;
 
     /**
      * An error output file presented to the walker.
      */
     public String errFileName = null;
 
     /**
      * A joint file for both 'normal' and error output presented to the walker.
      */
     public String outErrFileName = null;
 
     /**
      * The output stream, initialized from OUTFILENAME / OUTERRFILENAME.
      * Used by the walker.
      */
     public PrintStream out = System.out;
 
     /**
      * The output stream, initialized from ERRFILENAME / OUTERRFILENAME.
      * Used by the walker.
      */
     public PrintStream err = System.err;
 
     /**
      * our log, which we want to capture anything from this class
      */
     private static Logger logger = Logger.getLogger(GenomeAnalysisTK.class);
 
 
     /**
      * setup our arguments, both required and optional
      * <p/>
      * Flags don't take an argument, the associated Boolean gets set to true if the flag appears on the command line.
      */
     protected void setupArgs() {
         m_parser.addOptionalArg("input_file", "I", "SAM or BAM file", "INPUT_FILE");
         //m_parser.addRequiredArg("input_file", "I", "SAM or BAM file", "INPUT_FILE");
         m_parser.addOptionalArg("maximum_reads", "M", "Maximum number of reads to process before exiting", "MAX_READS_ARG");
         m_parser.addOptionalArg("validation_strictness", "S", "How strict should we be with validation (lenient|silent|strict)", "STRICTNESS_ARG");
         m_parser.addOptionalArg("reference_sequence", "R", "Reference sequence file", "REF_FILE_ARG");
         m_parser.addOptionalArg("genome_region", "L", "Genome region to operation on: from chr:start-end", "REGION_STR");
         m_parser.addRequiredArg("analysis_type", "T", "Type of analysis to run", "Analysis_Name");
         m_parser.addOptionalArg("DBSNP", "D", "DBSNP file", "DBSNP_FILE");
         m_parser.addOptionalArg("hapmap", "H", "Hapmap file", "HAPMAP_FILE");
         m_parser.addOptionalArg("hapmap_chip", "hc", "Hapmap chip file", "HAPMAP_CHIP_FILE");
         m_parser.addOptionalFlag("threaded_IO", "P", "If set, enables threaded I/O operations", "ENABLED_THREADED_IO");
         m_parser.addOptionalFlag("unsafe", "U", "If set, enables unsafe operations, nothing will be checked at runtime.", "UNSAFE");
         m_parser.addOptionalArg("sort_on_the_fly", "sort", "Maximum number of reads to sort on the fly", "MAX_ON_FLY_SORTS");
         m_parser.addOptionalArg("downsample_to_fraction", "dfrac", "Fraction [0.0-1.0] of reads to downsample to", "DOWNSAMPLE_FRACTION");
         m_parser.addOptionalArg("downsample_to_coverage", "dcov", "Coverage [integer] to downsample to", "DOWNSAMPLE_COVERAGE");
         m_parser.addOptionalArg("intervals_file", "V", "File containing list of genomic intervals to operate on. line := <contig> <start> <end>", "INTERVALS_FILE");
         m_parser.addOptionalFlag("all_loci", "A", "Should we process all loci, not just those covered by reads", "WALK_ALL_LOCI");
         m_parser.addOptionalArg("out", "o", "An output file presented to the walker.  Will overwrite contents if file exists.", "outFileName" );
         m_parser.addOptionalArg("err", "e", "An error output file presented to the walker.  Will overwrite contents if file exists.", "errFileName" );
         m_parser.addOptionalArg("outerr", "oe", "A joint file for 'normal' and error output presented to the walker.  Will overwrite contents if file exists.", "outErrFileName");
     }
 
     /**
      * GATK can add arguments dynamically based on analysis type.
      * @return true
      */
     @Override
     protected boolean canAddArgumentsDynamically() { return true; }
 
     /**
      * GATK provides the walker as an argument source.  As a side-effect, initializes the walker variable.
      * @return List of walkers to load dynamically.
      */
     @Override
     protected Class[] getArgumentSources() {
         if( Analysis_Name == null )
             throw new IllegalArgumentException("Must provide analysis name");
 
         walkerManager = new WalkerManager( pluginPathName );
 
         if( !walkerManager.doesWalkerExist(Analysis_Name) )
             throw new IllegalArgumentException("Invalid analysis name");
 
         return new Class[] { walkerManager.getWalkerClassByName(Analysis_Name) };
     }
 
     @Override
     protected String getArgumentSourceName( Class argumentSource ) {
         return WalkerManager.getWalkerName( (Class<Walker>)argumentSource );
     }
 
     /**
      * Required main method implementation.
      */
     public static void main(String[] argv) {
         Instance = new GenomeAnalysisTK();
         start(Instance, argv);
     }
 
     protected int execute() {
         final boolean TEST_ROD = false;
         List<ReferenceOrderedData<? extends ReferenceOrderedDatum> > rods = new ArrayList<ReferenceOrderedData<? extends ReferenceOrderedDatum> >();
 
         if ( TEST_ROD ) {
             ReferenceOrderedData<rodGFF> gff = new ReferenceOrderedData<rodGFF>("test", new File("trunk/data/gFFTest.gff"), rodGFF.class );
             gff.testMe();
 
             //ReferenceOrderedData dbsnp = new ReferenceOrderedData(new File("trunk/data/dbSNP_head.txt"), rodDbSNP.class );
             ReferenceOrderedData<rodDbSNP> dbsnp = new ReferenceOrderedData<rodDbSNP>("dbSNP", new File("/Volumes/Users/mdepristo/broad/ATK/exampleSAMs/dbSNP_chr20.txt"), rodDbSNP.class );
             //dbsnp.testMe();
             rods.add(dbsnp); // { gff, dbsnp };
         } else {
             if ( DBSNP_FILE != null ) {
                 ReferenceOrderedData<rodDbSNP> dbsnp = new ReferenceOrderedData<rodDbSNP>("dbSNP", new File(DBSNP_FILE), rodDbSNP.class );
                 //dbsnp.testMe();
                 rods.add(dbsnp); // { gff, dbsnp };
             }
             if ( HAPMAP_FILE != null ) {
                 ReferenceOrderedData<HapMapAlleleFrequenciesROD> hapmap = new ReferenceOrderedData<HapMapAlleleFrequenciesROD>("hapmap", new File(HAPMAP_FILE), HapMapAlleleFrequenciesROD.class );
                 //dbsnp.testMe();
                 rods.add(hapmap); // { gff, dbsnp };
             }
             if ( HAPMAP_CHIP_FILE != null ) {
                ReferenceOrderedData<rodGFF> hapmapChip = new ReferenceOrderedData<rodGFF>("hapmap-chip", new File(HAPMAP_CHIP_FILE), rodGFF.class );
                 rods.add(hapmapChip);
             }
         }
 
         initializeOutputStreams();
 
         Walker<?,?> my_walker = null;
         try {
             my_walker = walkerManager.createWalkerByName( Analysis_Name );
         }
         catch( InstantiationException ex ) {
             throw new RuntimeException( "Unable to instantiate walker.", ex );
         }
         catch( IllegalAccessException ex ) {
             throw new RuntimeException( "Unable to access walker", ex );
         }
 
         // Try to get the walker specified
         try {
             LocusWalker<?, ?> walker = (LocusWalker<?, ?>) my_walker;
 
             if ( INPUT_FILE == null ) {
                 if ( walker.requiresReads() )
                     Utils.scareUser(String.format("Analysis %s requires reads, but none were given", Analysis_Name));
                 this.engine = new TraverseByReference(null, REF_FILE_ARG, rods);
             } else {
                 if ( walker.cannotHandleReads() )
                     Utils.scareUser(String.format("Analysis %s doesn't support SAM/BAM reads, but a read file %s was provided", Analysis_Name, INPUT_FILE));
 
 
                 if ( WALK_ALL_LOCI )
                     this.engine = new TraverseByLociByReference(INPUT_FILE, REF_FILE_ARG, rods);
                 else
                     this.engine = new TraverseByLoci(INPUT_FILE, REF_FILE_ARG, rods);
             }
         }
         catch (java.lang.ClassCastException e) {
             // I guess we're a read walker LOL
             ReadWalker<?, ?> walker = (ReadWalker<?, ?>) my_walker;
             this.engine = new TraverseByReads(INPUT_FILE, REF_FILE_ARG, rods);
         }
 
         // Prepare the sort ordering w.r.t. the sequence dictionary
         if (REF_FILE_ARG != null) {
             final ReferenceSequenceFile refFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(REF_FILE_ARG);
             GenomeLoc.setupRefContigOrdering(refFile);
         }
         ValidationStringency strictness;
         if (STRICTNESS_ARG == null) {
             strictness = ValidationStringency.STRICT;
         } else if (STRICTNESS_ARG.toLowerCase().equals("lenient")) {
             strictness = ValidationStringency.LENIENT;
         } else if (STRICTNESS_ARG.toLowerCase().equals("silent")) {
             strictness = ValidationStringency.SILENT;
         } else {
             strictness = ValidationStringency.STRICT;
         }
         logger.info("Strictness is " + strictness);
         engine.setStrictness(strictness);
 
         engine.setDebugging(!(DEBUGGING_STR == null || DEBUGGING_STR.toLowerCase().equals("true")));
         engine.setMaxReads(Integer.parseInt(MAX_READS_ARG));
 
         if (REGION_STR != null) {
             engine.setLocation(REGION_STR);
         }
 
         if (INTERVALS_FILE != null) {
             engine.setLocationFromFile(INTERVALS_FILE);
         }
 
         if (MAX_ON_FLY_SORTS != null) {
             engine.setSortOnFly(Integer.parseInt(MAX_ON_FLY_SORTS));
         }
 
         if (DOWNSAMPLE_FRACTION != null) {
             engine.setDownsampleByFraction(Double.parseDouble(DOWNSAMPLE_FRACTION));
         }
 
         if (DOWNSAMPLE_COVERAGE != null) {
             engine.setDownsampleByCoverage(Integer.parseInt(DOWNSAMPLE_COVERAGE));
         }
 
         engine.setSafetyChecking(!UNSAFE);
         engine.setThreadedIO(ENABLED_THREADED_IO);
         engine.setWalkOverAllSites(WALK_ALL_LOCI);
         engine.initialize();
 
         engine.traverse(my_walker);
 
         return 0;
     }
 
     /**
      * Initialize the output streams as specified by the user.
      */
     private void initializeOutputStreams() {
         if( outErrFileName != null && (outFileName != null || errFileName != null) )
             throw new IllegalArgumentException("Can't set output/error output file with either out file name or err file name");
 
         try {
             if( outErrFileName != null ) {
                 PrintStream outErrStream = new PrintStream( outErrFileName );
                 out = outErrStream;
                 err = outErrStream;
             }
 
             if ( outFileName != null ) {
                 out = new PrintStream( outFileName );
             }
 
             if( errFileName != null ) {
                 err = new PrintStream( errFileName );
             }
         }
         catch( FileNotFoundException ex ) {
             throw new RuntimeException("Unable to open a walker output file.", ex);
         }
     }
 
     /**
      * An inappropriately placed validation and performance testing routine for jumping
      * around in the fasta sequence file.
      * @param refFileName
      */
     private static void testNewReferenceFeatures(final File refFileName) {
         final FastaSequenceFile2 refFile = new FastaSequenceFile2(refFileName);
         GenomeLoc.setupRefContigOrdering(refFile);
 
         List<SAMSequenceRecord> refContigs = refFile.getSequenceDictionary().getSequences();
 
         /*
         for ( SAMSequenceRecord refContig: refContigs ) {
             System.out.printf("  Traversing from chr1 to %s would require jumping %d bytes%n",
                     refContig.getSequenceName(), refFile.getDistanceBetweenContigs("chr1", refContig.getSequenceName()));
         }
         */
 
         String lastContig = null;
         List<Double> timings = new ArrayList<Double>();
         for ( SAMSequenceRecord startContig : refFile.getSequenceDictionary().getSequences() ) {
             final String startContigName = startContig.getSequenceName();
             for ( SAMSequenceRecord targetContig : refFile.getSequenceDictionary().getSequences() ) {
                 refFile.seekToContig(startContigName, true);
                 logger.info(String.format("Seeking: current=%s, target=%s%n", startContigName, targetContig.getSequenceName()));
                 long lastTime = System.currentTimeMillis();
                 final boolean success = refFile.seekToContig(targetContig.getSequenceName(), true);
                 long curTime = System.currentTimeMillis();
                 final double elapsed = (curTime - lastTime) / 1000.0;
                 timings.add(elapsed);
                 logger.info(String.format("  -> Elapsed time %.2f, averaging %.2f sec / seek for %d seeks%n",
                         elapsed, Utils.averageDouble(timings), timings.size()));
 
                 if ( ! success ) {
                     logger.error(String.format("Failured to seek to %s from %s%n", targetContig.getSequenceName(), lastContig ));
                 }
                 //System.exit(1);
             }
         }
         System.exit(1);
 
         // code for randomly sampling the seeks
 //        Random rnd = new Random();
 //        String lastContig = null;
 //        List<Double> timings = new ArrayList<Double>();
 //        final int N_SAMPLES = 1000;
 //        //try { refFile.seekToContig("chr3"); } catch ( IOException e ) {}
 //        for ( int i = 0; i < N_SAMPLES; i++ ) {
 //            final int nextIndex = rnd.nextInt(refContigs.size());
 //            String nextContig = refFile.getSequenceDictionary().getSequence(nextIndex).getSequenceName();
 //            //nextContig = "chr2";
 //            try {
 //                System.out.printf("Seeking: current=%s, target=%s%n", refFile.getContigName(), nextContig);
 //                long lastTime = System.currentTimeMillis();
 //                final boolean success = refFile.seekToContig(nextContig, true);
 //                long curTime = System.currentTimeMillis();
 //                final double elapsed = (curTime - lastTime) / 1000.0;
 //                timings.add(elapsed);
 //                System.out.printf("  -> Elapsed time %.2f, averaging %.2f sec / seek for %d seeks%n",
 //                        elapsed, Utils.averageDouble(timings), timings.size());
 //
 //                if ( ! success ) {
 //                    System.out.printf("Failured to seek to %s from %s%n", nextContig, lastContig );
 //                }
 //                //System.exit(1);
 //            } catch ( IOException e ) {
 //                System.out.printf("Failured to seek to %s from %s%n", nextContig, lastContig );
 //                e.printStackTrace();
 //            }
 //
 //            lastContig = nextContig;
 //        }
 //        System.exit(1);
 
 /*
         final String targetChr = "chr10";
         try {
             refFile.seekToContig(targetChr);
         } catch ( IOException e ){
             System.out.printf("Failured to seek to %s%n", targetChr);
             e.printStackTrace();
         }
         System.exit(1);
 */
 
         //List<Double> timings = new ArrayList<Double>();
         final long startTime = System.currentTimeMillis();
         long lastTime = System.currentTimeMillis();
 
         int i = 0;
         String prevNextContigName = null;
         logger.info(String.format("Walking reference sequence:%n"));
         for ( SAMSequenceRecord refContig: refContigs ) {
             long curTime = System.currentTimeMillis();
             ReferenceSequence contig = refFile.nextSequence();
             final double elapsed = (curTime - lastTime) / 1000.0;
             timings.add(elapsed);
             logger.info(String.format("%2d : expected %s contig, found %s with next of %s after %.2f seconds, average is %.2f%n", i,
                     refContig.getSequenceName(), contig.getName(), refFile.getNextContigName(), elapsed, Utils.averageDouble(timings)));
             if ( prevNextContigName != null && contig.getName() != null && ! prevNextContigName.equals(contig.getName()) )
                 throw new RuntimeIOException(String.format("Unexpected contig ordering %s was expected next, but I found %s?",
                         prevNextContigName, contig.getName()));
 
             prevNextContigName = refFile.getNextContigName();
             lastTime = curTime;
             i++;
 
             logger.info(String.format("  Traversing from chr1 to %s would require jumping %d bytes%n",
                     contig.getName(), refFile.getDistanceBetweenContigs("chr1", contig.getName())));
         }
     }
 
     public SAMFileReader getSamReader() { return this.engine.getSamReader(); }
 }

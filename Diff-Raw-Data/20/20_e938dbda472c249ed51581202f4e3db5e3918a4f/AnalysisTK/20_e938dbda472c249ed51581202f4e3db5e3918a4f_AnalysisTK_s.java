 package edu.mit.broad.sting.atk;
 
 import edu.mit.broad.sam.SAMFileReader.ValidationStringency;
 import edu.mit.broad.picard.cmdline.CommandLineProgram;
 import edu.mit.broad.picard.cmdline.Usage;
 import edu.mit.broad.picard.cmdline.Option;
 
 import edu.mit.broad.sting.atk.modules.*;
 import edu.mit.broad.sting.utils.ReferenceOrderedData;
 import edu.mit.broad.sting.utils.rodGFF;
 import edu.mit.broad.sting.utils.rodDbSNP;
 
 import java.io.*;
 import java.util.HashMap;
 
 public class AnalysisTK extends CommandLineProgram {
     // Usage and parameters
     @Usage(programVersion="0.1") public String USAGE = "SAM Validator\n";
     @Option(shortName="I", doc="SAM or BAM file for validation") public File INPUT_FILE;
     @Option(shortName="M", doc="Maximum number of reads to process before exiting", optional=true) public String MAX_READS_ARG = "-1";
     @Option(shortName="S", doc="How strict should we be with validation", optional=true) public String STRICTNESS_ARG = "strict";
     @Option(shortName="R", doc="Reference sequence file", optional=true) public File REF_FILE_ARG = null;
     @Option(shortName="B", doc="Debugging output", optional=true) public String DEBUGGING_STR = null;
     @Option(shortName="L", doc="Genome region to operation on: from chr:start-end", optional=true) public String REGION_STR = null;
     @Option(shortName="T", doc="Type of analysis to run") public String Analysis_Name = null;
     @Option(shortName="DBSNP", doc="DBSNP file", optional=true) public String DBSNP_FILE = null;
     
     public static HashMap<String, Object> MODULES = new HashMap<String,Object>();
     public static void addModule(final String name, final Object walker) {
         System.out.printf("* Adding module %s%n", name);
         MODULES.put(name, walker);
     }
 
     static {
         addModule("CountLoci", new CountLociWalker());
         addModule("Pileup", new PileupWalker());
         addModule("CountReads", new CountReadsWalker());
         addModule("PrintReads", new PrintReadsWalker());
         addModule("Base_Quality_Histogram", new BaseQualityHistoWalker());
     }
 
     private TraversalEngine engine = null;
     public boolean DEBUGGING = false;
 
     /** Required main method implementation. */
     public static void main(String[] argv) {
         System.exit(new AnalysisTK().instanceMain(argv));
     }
 
     protected int doWork() {
         final boolean TEST_ROD = false;
         ReferenceOrderedData[] rods = null;
 
         if ( TEST_ROD ) {
             ReferenceOrderedData gff = new ReferenceOrderedData(new File("trunk/data/gFFTest.gff"), rodGFF.class );
             gff.testMe();
 
             //ReferenceOrderedData dbsnp = new ReferenceOrderedData(new File("trunk/data/dbSNP_head.txt"), rodDbSNP.class );
             ReferenceOrderedData dbsnp = new ReferenceOrderedData(new File("/Volumes/Users/mdepristo/broad/ATK/exampleSAMs/dbSNP_chr20.txt"), rodDbSNP.class );
             //dbsnp.testMe();
             rods = new ReferenceOrderedData[] { dbsnp }; // { gff, dbsnp };
         }
         else if ( DBSNP_FILE != null ) {
             ReferenceOrderedData dbsnp = new ReferenceOrderedData(new File(DBSNP_FILE), rodDbSNP.class );
             //dbsnp.testMe();
             rods = new ReferenceOrderedData[] { dbsnp }; // { gff, dbsnp };
         }
         else {
             rods = new ReferenceOrderedData[] {}; // { gff, dbsnp };
         }
 
         this.engine = new TraversalEngine(INPUT_FILE, REF_FILE_ARG, rods);
         engine.initialize();
 
         ValidationStringency strictness;
     	if ( STRICTNESS_ARG == null ) {
             strictness = ValidationStringency.STRICT;
     	}
     	else if ( STRICTNESS_ARG.toLowerCase().equals("lenient") ) {
     		strictness = ValidationStringency.LENIENT;
     	}
     	else if ( STRICTNESS_ARG.toLowerCase().equals("silent") ) {
     		strictness = ValidationStringency.SILENT;
     	}
     	else {
             strictness = ValidationStringency.STRICT;
     	}
         System.err.println("Strictness is " + strictness);
         engine.setStrictness(strictness);
 
         engine.setDebugging(! ( DEBUGGING_STR == null || DEBUGGING_STR.toLowerCase().equals("true")));
         engine.setMaxReads(Integer.parseInt(MAX_READS_ARG));
 
         if ( REGION_STR != null ) {
             engine.setLocation(REGION_STR);
         }
 
         //LocusWalker<Integer,Integer> walker = new PileupWalker();
         try {
            LocusWalker<?, ?> walker = (LocusWalker<?, ?>)MODULES.get(Analysis_Name);
             engine.traverseByLoci(walker);
         }
         catch ( java.lang.ClassCastException e ) {
             // I guess we're a read walker LOL
            ReadWalker<?, ?> walker = (ReadWalker<?, ?>)MODULES.get(Analysis_Name);
             engine.traverseByRead(walker);
         }
 
         return 0;
     }
}

 package edu.mit.wi.haploview;
 
 
 public interface Constants {
 
     //main jframe setup stuff & labels.
     public final boolean DEBUG = false;
     public static final double VERSION = 4.1;
     public static final int BETA_VERSION = 0;
    public static final String EMAIL_STRING = "haploview@broad.mit.edu";
     public static final String TITLE_STRING = "Haploview "+VERSION;
     public static final String JAVA_VERSION = System.getProperty("java.version");
     public static final String USER_AGENT = "Haploview/" + VERSION + " Java/" + JAVA_VERSION;
     public static final String RELEASE_DATE = "29 April, 2008";
     public static final String WEBSITE_STRING = "http://www.broad.mit.edu/mpg/haploview/";
     public static final String BETA_WEBSITE_STRING = "http://www.broad.mit.edu/mpg/haploview/beta.php";
     public static final String CITATION_STRING = "Citation: Barrett JC, Fry B, Maller J, Daly MJ.\nHaploview: analysis and visualization of LD and haplotype maps.\nBioinformatics. 2005";
     public static final String ABOUT_STRING = TITLE_STRING + "\n" + RELEASE_DATE + "\n" +
             WEBSITE_STRING + "\n\n" + CITATION_STRING + "\n\n" +
             "Daly Lab at the Broad Institute\n" +
             "Cambridge, MA 02141, USA\n\n"+
             "Jeffrey Barrett\n" +
             "Julian B. Maller\n" +
             "David J. Bender\n" +
             "Jesse C. Whitworth\n" +
             EMAIL_STRING;
 
     public static final String READ_GENOTYPES = "Open new data";
     public static final String READ_MARKERS = "Load marker data";
     public static final String READ_ANALYSIS_TRACK = "Load analysis track";
     public static final String READ_BLOCKS_FILE = "Load block definitions";
     public static final String DOWNLOAD_GBROWSE = "Download HapMap info track";
     public static final String GBROWSE_OPTS = "HapMap Info Track Options";
 
     public static final String EXPORT_TEXT = "Export current tab to text";
     public static final String EXPORT_PNG = "Export current tab to PNG";
     public static final String EXPORT_OPTIONS = "Export options";
 
     public static final String CLEAR_BLOCKS = "Clear all blocks";
     public static final String CUST_BLOCKS = "Customize Block Definitions";
 
     public static final String VIEW_DPRIME = "LD Plot";
     public static final String VIEW_HAPLOTYPES = "Haplotypes";
     public static final String VIEW_CHECK_PANEL = "Check Markers";
     public static final String VIEW_ASSOC = "Association";
     public static final String VIEW_PLINK = "PLINK";
     public static final String VIEW_TAGGER = "Tagger";
 
     //main frame tab numbers
     public static final int VIEW_D_NUM = 0;
     public static final int VIEW_HAP_NUM = 1;
     public static final int VIEW_CHECK_NUM = 2;
     public static final int VIEW_TAGGER_NUM = 3;
     public static final int VIEW_PLINK_NUM = 4;
     public static final int VIEW_ASSOC_NUM = 5;
 
     //association tab subtab indices
     public static final int VIEW_SINGLE_ASSOC = 0;
     public static final int VIEW_HAPLO_ASSOC = 1;
 
     //export modes
     public static final int PNG_MODE = 0;
     public static final int TXT_MODE = 1;
     public static final int COMPRESSED_PNG_MODE = 2;
     public static final int SVG_MODE = 3;
     public static final int TABLE_TYPE = 0;
     public static final int LIVE_TYPE = 1;
 
     //block defs
     public static final int BLOX_GABRIEL = 0;
     public static final int BLOX_4GAM = 1;
     public static final int BLOX_SPINE = 2;
     public static final int BLOX_CUSTOM = 3;
     public static final int BLOX_ALL = 4;
     public static final int BLOX_NONE = 5;
 
     //filetypes
     static final int GENO_FILE = 0;
     static final int INFO_FILE = 1;
     static final int HAPS_FILE = 2;
     static final int PED_FILE = 3;
     static final int HMP_FILE = 4;
     static final int ASSOC_FILE = 5;
     static final int PHASEHMP_FILE = 6;
     static final int HMPDL_FILE = 7;
     static final int SAMPLEHMP_FILE = 8;
     static final int LEGENDHMP_FILE = 9;
     static final int PLINK_FILE = 10;
     static final int MAP_FILE = 11;
     //static final int FASTPHASE_FILE = 12;
     static final int HMPDL_FILE3 = 13;
 
     //color modes
     static final int STD_SCHEME = 0;
     static final int RSQ_SCHEME = 1;
     static final int WMF_SCHEME = 2;
     static final int GAB_SCHEME = 3;
     static final int GAM_SCHEME = 4;
     static final int GOLD_SCHEME = 5;
 
     //what LD stat to print
     static final int D_PRIME = 0;
     static final int R_SQ = 1;
     static final int LD_NONE = 2;
 
     //association test modes
     static final int ASSOC_NONE = 0;
     static final int ASSOC_TRIO = 1;
     static final int ASSOC_CC = 2;
 
     //tdt types
     static final int TDT_STD = 0;
     static final int TDT_PAREN = 1;
 
     //single marker association display stuff
     static final int SHOW_SINGLE_COUNTS = 0;
     static final int SHOW_SINGLE_FREQS = 1;
 
     //haplotype association display stuff
     static final int SHOW_HAP_COUNTS = 0;
     static final int SHOW_HAP_RATIOS = 1;
 
     //default LD comparison distance (in kb)
     static final int MAXDIST_DEFAULT = 500;
 
     //phased data stuff
     static final String[] CHROM_NAMES = {"1","2","3","4","5","6","7","8","9","10",
             "11","12","13","14","15","16","17","18","19","20","21","22","X","Y"};
     static final String[] PANEL_NAMES = {"CEU", "YRI", "CHB+JPT"};
     static final String[] VERSION_HAPMAP = {"HapMap 2", "HapMap 3"};
     static final String[] PANEL_NAMES_HAPMAP3 = {"ASW", "CEU", "CHB", "CHD", "GIH", "JPT", "LWK", "MEX", "MKK", "TSI", "YRI"};
     static final String[] RELEASE_NAMES = {"16a","21","22"};
     //static final String[] PHASE_FORMATS = {"HapMap PHASE","fastPHASE"};
     static final String[] DOWNLOAD_FORMATS = {"Region","Gene ID"};
 
     //GeneCruiser stuff
     static final String[] GENE_DATABASES = {"Ensembl","HUGO","SNP"};
     static final int ENSEMBL = 0;
     static final int HUGO = 1;
     static final int SNP = 2;
 
     //plot types
     static final String[] PLOT_TYPES = {"Untransformed", "-log10", "ln"};
     static final int UNTRANSFORMED_PLOT = 0;
     static final int LOG10_PLOT = 1;
     static final int LN_PLOT = 2;
 
     //GBrowse options
     static final String[] GB_TYPES = {"gtsh", "mRNA", "recomb", "NT", "DNA"};
     static final String[] GB_OPTS = {"gtsh%201", "mRNA%203", "", "", ""};
     static final String[] GB_OPTS_NAMES = {"HapMap SNPs", "Entrez Genes", "Recombination Rate", "NT Contigs", "DNA/GC Content"};
     static final String GB_DEFAULT_OPTS = GB_OPTS[0] + "+" + GB_OPTS[1];
     static final String GB_DEFAULT_TYPES = GB_TYPES[0] + "+" + GB_TYPES[1];
 
 
     static final String HELP_OUTPUT = TITLE_STRING + " Command line options\n" +
             "-h, -help                       Print this message\n" +
             "-memory <memsize>               allocates <memsize> megabytes of memory (default 512)\n"+
             "-n, -nogui                      Command line output only\n" +
             "-q, -quiet                      Quiet mode- doesnt print any warnings or information to screen\n" +
             "-log <filename>                 Specify a logfile name (defaults to haploview.log if no name specified)\n" +
             "-out <fileroot>                 Specify a fileroot to be used for all output files\n" +
             "-pedfile <pedfile>              Specify an input file (or http:// location) in pedigree file format\n" +
             "-hapmap <hapmapfile>            Specify an input file (or http:// location) in HapMap format\n" +
             "-phasedhmpdata <phasedfile>     Specify a HapMap PHASE data file (or http:// location)\n" +
             "-phasedhmpsample <samplefile>   Specify a HapMap PHASE sample file (or http:// location)\n" +
             "-phasedhmplegend <legendfile>   Specify a HapMap PHASE legend file (or http:// location)\n" +
             //"-fastphase <fastphasefile>      Specify an input file (or http:// location) in fastPHASE format\n" +
             "-gzip                           Indicates that phased input files use GZIP compression\n" +
             "-hapmapDownload                 Specify a phased HapMap download\n" +
             "-haps <hapsfile>                Specify an input file (or http:// location) in .haps format\n" +
             "-info <infofile>                Specify a marker info file (or http:// location)\n" +
             "-plink <plinkfile>              Specify a PLINK or other results file (or http:// location)\n" +
             "-map <mapfile>                  Specify a map file or binary map file (or http:// location)\n" +
             "-nonSNP                         Specify that the accompanying PLINK file is non-SNP based output\n" +
             "-selectCols                     Activate the preloading column filter for PLINK loads\n" +
             "-batch <batchfile>              Batch mode. Each line in batch file should contain a genotype file \n"+
             "                                followed by an optional info file, separated by a space.\n" +
             "-blocks <blockfile>             Blocks file (or http:// location), one block per line, will force output for these blocks\n" +
             "-track <trackfile>              Specify an input analysis track file (or http:// location)\n"+
             "-excludeMarkers <markers>       Specify markers (in range 1-N where N is total number of markers) to be\n"+
             "                                skipped for all analyses. Format: 1,2,5..12\n"+
             "-skipcheck                      Skips the various genotype file checks\n" +
             "-chromosome <1-22,X,Y>          Specifies the chromosome for this file or download\n" +
             "-panel <CEU,YRI,CHB+JPT>        Specifies the analysis panel for this HapMap download\n" +
             "-startpos <integer>             Specifies the start position in kb for this HapMap download\n" +
             "-endpos <integer>               Specifies the end position in kb for this HapMap download\n" +
             "-release <16a,21,22>            Specifies the HapMap phase for this HapMap download (defaults to 21)\n" +
             "-dprime                         Outputs LD text to <fileroot>.LD\n" +
             "-png                            Outputs LD display to <fileroot>.LD.PNG\n"+
             "-compressedpng                  Outputs compressed LD display to <fileroot>.LD.PNG\n"+
             "-svg                            Outputs svg format LD display to <fileroot>.LD.SVG\n"+
             "-infoTrack                      Downloads and displays HapMap info track on PNG image output\n"+
             "-ldcolorscheme <argument>       Specify an LD color scheme. <argument> should be one of:\n" +
             "                                DEFAULT, RSQ, DPALT, GAB, GAM\n" +
             "-ldvalues <DPRIME,RSQ,NONE>     Specify what to print in LD image output. default is DPrime\n" +
             "-check                          Outputs marker checks to <fileroot>.CHECK\n" +
             "                                note: -dprime  and -check default to no blocks output. \n" +
             "                                Use -blockoutput to also output blocks\n" +
             "-indcheck                       Outputs genotype percent per individual to <fileroot>.INDCHECK\n" +
             "-mendel                         Outputs Mendel error information to <fileroot>.MENDEL\n" +
             "-malehets                       Outputs male heterozygote information to <fileroot>.MALEHETS\n" +
             "-blockoutput <GAB,GAM,SPI,ALL>  Output type. Gabriel, 4 gamete, spine output or all 3. default is Gabriel.\n" +
             "-blockCutHighCI <thresh>        Gabriel 'Strong LD' high confidence interval D' cutoff.\n" +
             "-blockCutLowCI <thresh>         Gabriel 'Strong LD' low confidence interval D' cutoff.\n" +
             "-blockMAFThresh <thresh>        Gabriel MAF threshold.\n" +
             "-blockRecHighCI <thresh>        Gabriel recombination high confidence interval D' cutoff.\n" +
             "-blockInformFrac <thresh>       Gabriel fraction of informative markers required to be in LD.\n" +
             "-block4GamCut <thresh>          4 Gamete block cutoff for frequency of 4th pairwise haplotype.\n" +
             "-blockSpineDP <thresh>          Solid Spine blocks D' cutoff for 'Strong LD\n"+
             "-maxdistance <distance>         Maximum comparison distance in kilobases (integer). Default is 500\n" +
             "-hapthresh <frequency>          Only output haps with at least this frequency\n" +
             "-spacing <threshold>            Proportional spacing of markers in LD display. <threshold> is a value\n" +
             "                                between 0 (no spacing) and 1 (max spacing). Default is 0\n"  +
             "-minMAF <threshold>             Minimum minor allele frequency to include a marker. <threshold> is a value\n" +
             "                                between 0 and 0.5. Default is .001\n" +
             "-maxMendel <integer>            Markers with more than <integer> Mendel errors will be excluded. Default is 1.\n" +
             "-minGeno <threshold>            Exclude markers with less than <threshold> valid data. <threshold> is a value\n" +
             "                                between 0 and 1. Default is .75\n" +
             "-hwcutoff <threshold>           Exclude markers with a HW p-value smaller than <threshold>. <threshold> is a value\n" +
             "                                between 0 and 1. Default is .001\n" +
             "-missingCutoff <threshold>      Exclude individuals with more than <threshold> fraction missing data.\n" +
             "                                <threshold> is a value between 0 and 1. Default is .5 \n" +
             "-assocCC                        Outputs case control association results to <fileroot>.ASSOC and <fileroot>.HAPASSOC\n" +
             "-assocTDT                       Outputs trio association results to <fileroot>.ASSOC and <fileroot>.HAPASSOC\n" +
             "-customAssoc <file>             Loads a set of custom tests for association.\n" +
             "-permtests <numtests>           Performs <numtests> permutations on default association tests (or custom tests\n" +
             "                                if a custom association file is specified) and writes to <fileroot>.PERMUT\n" +
             "-pairwiseTagging                Generates pairwise tagging information in <fileroot>.TAGS and .TESTS\n" +
             "-aggressiveTagging              As above but generates 2-marker haplotype tags unless specified otherwise by -aggressiveNumMarkers\n" +
             //"-tagrsqcounts                   Generates conditional haplotype probabilities from tagger in <fileroot>.CHAPS\n" +
             "-aggressiveNumMarkers <2,3>     Specifies whether to use 2-marker haplotype tags or 2 and 3-marker haplotype tags.\n" +
             "-maxNumTags <n>                 Only selects <n> best tags.\n" +
             "-dontaddtags                    Only uses forced in tags.\n" +
             "-includeTags <markers>          Forces in a comma separated list of marker names as tags.\n" +
             "-includeTagsFile <file>         Forces in a file (or http:// location) of one marker name per line as tags.\n" +
             "-excludeTags <markers>          Excludes a comma separated list of marker names from being used as tags.\n" +
             "-excludeTagsFile <file>         Excludes a file (or http:// location) of one marker name per line from being used as tags.\n" +
             "-captureAlleles <file>          Capture only the alleles contained in a file (or http:// location) of one marker name per line.\n" +
             "-designScores <file>            Specify design scores in a file (or http:// location) of one marker name and one score per line\n" +
             "-mindesignscore <threshold>     Specify a minimum design score threshold.\n" +
             "-mintagdistance <distance>      Specify a Minimum distance in bases between picked tags.\n" +
             "-taglodcutoff <thresh>          Tagger LOD cutoff for creating multimarker tag haplotypes.\n" +
             "-tagrsqcutoff <thresh>          Tagger r^2 cutoff.\n"
             ;
 
 }
 

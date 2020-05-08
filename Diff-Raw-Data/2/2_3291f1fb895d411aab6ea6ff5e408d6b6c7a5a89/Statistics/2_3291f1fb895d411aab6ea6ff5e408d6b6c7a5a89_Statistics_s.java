 /**
  * 
  */
 package BAMStats;
 
 import java.text.*;
 import net.sf.samtools.*;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author niravs
  * The class that computes the statistics for the specified BAM/SAM file
  */
 public class Statistics
 {
   private long totalReads = 0;         // Total reads in file
   private long numReads = 0;           // Total reads of specified read type
   private long unmappedReads = 0;      // Unmapped reads
   private long mappedReads = 0;        // Total mapped reads
   private long bothEndsMapped = 0;     // Number of reads where itself and mate 
                                        // are both mapped - Valid only for 
                                        // read1 or read2
   
   private long unpairedReads = 0;      // Number of reads without any pair information
                                        // Valid only for read1 or read2
   
   private long uniqMappedReads = 0;    // Uniquely mapped reads
   private long dupReads = 0;           // Duplicate reads
   private ReadType rType;              // Type of read
   private double mappedTput = 0;       // Throughput of mapped reads
   private double uniqTput = 0;         // Throughput of uniquely mapped reads
   private long mapQuality[];           // To hold mapping quality for mapped reads
                                        // of specified type
   private long nonUniqMapQual[];       // To hold mapping qualities for non-uniquely
                                        //mapped reads of specified type
   private final int MAPQUALSIZE = 256; // Levels of mapping quality available
   
   // Number of mapped reads having unique candidate alignment, i.e., reads having
   // NH tag value equal to one
   private long numReadsUniqCandidateAlignments = 0;
   private boolean foundNHTag = false;  // Whether reads have NH tag
 
   private String sTag; // Holds a F3 or R3 depending of rType
   
   /**
    * Class constructor that defaults to ReadType as read1. 
    */
   public Statistics()
   {
 	constructorHelper(ReadType.Read1);
   }
   
   /**
    * Class constructor that allows the caller to specify read type
    * @param rType - ReadType enum representing whether read1 or read2
    */
   public Statistics(ReadType rType)
   {
     constructorHelper(rType);
   }
   
   /**
    * Private constructor helper method
    * @param rType - ReadType enum representing whether read1 or read2
    */
   private void constructorHelper(ReadType rType)
   {
     this.rType = rType;
     if (rType == ReadType.Read1) { // read1 of a pair
       this.sTag = "F3";
     }
     else if (rType == ReadType.Read2) { // read 2 of a pair
       this.sTag = "R3";
     }
     else { // fragment
       this.sTag = "F3";
     }
     mapQuality = new long[MAPQUALSIZE];
     nonUniqMapQual = new long[MAPQUALSIZE];
   }
   
   /**
    * Public method to process a read. It calculates the total number of reads,
    * number of reads of a specific type (i.e. fragment, read1 or read2),
    * duplicate reads, unmapped reads.
    * @param rec - Instance of SAMRecord to be processed
    */
   public void processRead(SAMRecord rec)
   {
     if(totalReads > 0 && totalReads % 1000000 == 0)
     {
       System.err.print("\r" + totalReads);
     }
     ++totalReads;
     
     /**
      * If we are looking for read1 or read2, (i.e. paired read), and the 
      * current read is unpaired, increment the counter for unpaired reads.
      */
     if(rType != ReadType.Fragment && !rec.getReadPairedFlag())
       unpairedReads++;
     
     /*
      * Process this read if and only if at least one of these conditions are 
      * satisfied:
      * 1) We want to inspect fragment reads and current record is fragment 
      *    i.e. flag (0x01 is not set)
      * 2) We want to inspect read 1 and current record is paired read, and it
      *    is first read in pair, i.e. flags 0x01 and 0x40 are both set.
      * 3) We want to inspect read 2 and current record is paired read, and it 
      *    is second read in pair, i.e. flags 0x01 and 0x80 are both set.
      * we skip this read in all other cases.
      */
     if((rType == ReadType.Fragment && !rec.getReadPairedFlag()) ||
        (rType == ReadType.Read1 && rec.getReadPairedFlag() && rec.getFirstOfPairFlag()) ||
        (rType == ReadType.Read2 && rec.getReadPairedFlag() && rec.getSecondOfPairFlag()))
     {
       numReads++;
       if(rec.getDuplicateReadFlag())
         dupReads++;
       if(rec.getReadUnmappedFlag())
         unmappedReads++;
       else
       {
         /**
          * For a mapped read, we check if it is a paired end, and if it's mate
          * is also mapped, we increment the corresponding counter
          */
     	if(rec.getReadPairedFlag() && !rec.getMateUnmappedFlag())
     	  bothEndsMapped++;
     	
         // This is a mapped read - increment mapped read throughput
         mappedTput += rec.getReadLength();
      	
     	// This is a uniquely mapped read - increment unique mapped read throughput
         if(!rec.getDuplicateReadFlag())
         {
           uniqTput += rec.getReadLength();
         }
         
         // For a mapped read, remember its mapping quality
         mapQuality[rec.getMappingQuality()]++;
         
         /**
          * NH attribute represents the number of candidate alignments the current read had. The aligner
          * selects the alignment with the best mapping score. However, in this field, it stores the 
          * total number of other possible alignments it saw for this read.
          */
         if(rec.getAttribute("NH") != null)
         {
           foundNHTag = true;
           if(rec.getAttribute("NH").toString().compareTo("1") == 0)
           {
             numReadsUniqCandidateAlignments++;
           }
           else
           {
             // Read had more than one alignments - we remember its mapping quality
         	  nonUniqMapQual[rec.getMappingQuality()]++;
           }
         }
       }
     }
   }
   
   /**
    * Method to display the read statistics of the BAM/SAM file
    */
   public void showStats()
   {
     mappedReads = numReads - unmappedReads;
     uniqMappedReads = mappedReads - dupReads;
     System.out.println("");
     System.out.println("Total Reads in file        : " + formatNumber(totalReads));
     System.out.println("Number of Reads considered : " + formatNumber(numReads));
     System.out.println("");
     System.out.println("Mapped Reads               : " + formatNumber(mappedReads));
 
    
     if(numReads > 0)
     {
       System.out.format("Mapped Reads Percentage    : %.2f%% %n", (1.0 * mappedReads / numReads * 100.0));
     }
     else
     {
       System.out.println("Mapped Reads Percentage    : 0%");
     }
     
     System.out.println("Throughput                 : " + formatNumber(mappedTput) + " bp");
 
         
     if(foundNHTag == true)
     {
       System.out.println();
       System.out.println("Num Reads With exactly 1 Alignment : " + formatNumber(numReadsUniqCandidateAlignments));
       
       if(numReads > 0)
       {
         System.out.format("%% Reads With exactly 1 Alignment   : %.2f%% %n", (numReadsUniqCandidateAlignments * 1.0 / numReads * 100));
       }
       else
       {
         System.out.println("% Reads With 1 Alignment  : 0%");
       }
     }
     
     if(rType != ReadType.Fragment)
     {
       System.out.println("");
       System.out.println("Read Pairs With Both Ends Mapped    : " + formatNumber(bothEndsMapped));
       
       if(mappedReads > 0)
         System.out.format("%% Read Pairs With Both Ends Mapped : %.2f%% %n", (1.0 * bothEndsMapped / mappedReads * 100.0));
       else
         System.out.println("% Read Pairs With Both Ends Mapped : 0%");
       
       System.out.println("");
       System.out.println("Num Reads Without a Mate   : " + formatNumber(unpairedReads));
     }
     
     System.out.println("");
     System.out.println("Duplicate Reads            : " + formatNumber(dupReads));
     System.out.println("Uniquely Mapped Reads      : " + formatNumber(uniqMappedReads));
     
     if(mappedReads > 0)
     {
       System.out.format("Uniqueness Percentage      : %.2f%% %n", (uniqMappedReads * 1.0 /mappedReads * 100));
     }
     else
     {
       System.out.println("Uniqueness Percentage      : 0%");
     }
     System.out.println("Effective Throughput       : " + formatNumber(uniqTput) + " bp");
 
     /* 4 lims */
     // Dump here the key values for LIMS
     //HashMap<String, String> h_for_lims = new HashMap<String, String>();
 
     System.out.println("\nBEGIN 4 LIMS");
     System.out.println(sTag + "_total_reads_considered: " + formatNumber(numReads));
     System.out.println(sTag + "_total_reads_mapped: " + formatNumber(mappedReads));
     System.out.println(sTag + "_throughput: " + formatNumber(mappedTput));
    System.out.println(sTag + "_effective_throughput: " + formatNumber(mappedTput));
     System.out.println("END 4 LIMS\n");
   }
   
   /**
    * Returns reference to array containing quality distribution for 
    * all mapped reads
    * @return
    */
   public long[] getMapQualityDistribution()
   {
 	  return mapQuality;
   }
   
   /**
    * Returns reference to array containing quality distribution for all 
    * non-uniquely mapped reads
    * @return
    */
   public long[] getNonUniqueMapQualityDistribution()
   {
     return nonUniqMapQual;
   }
   
   /**
    * Private method to format doubles to integral format
    * @param double to be shown in integer format
    * @return Formatted string equivalent of the double
    */
   private String formatNumber(double d)
   {
     DecimalFormat df = new DecimalFormat();
     StringBuffer output = new StringBuffer();
     output = df.format(d, output, new FieldPosition((NumberFormat.INTEGER_FIELD)));
     return output.toString();
   }
   
   /**
    * Private overloaded helper method to format long
    * @param l - Long type to be shows as a string
    * @return Formatted string equivalent of input
    */
   private String formatNumber(long l)
   {
 	return formatNumber(l * 1.0); 
   }
 }

 package analyzer.SequenceAnalyzer;
 
 import analyzer.Common.*;
 import java.util.*;
 import java.io.*;
 import net.sf.picard.fastq.FastqRecord;
 
 /**
  * Class to calculate percentage of unique reads based on comparing k-mer
  * sequences of specified of read sequences.
  * @author Nirav Shah niravs@bcm.edu
  *
  */
 public class UniquenessCalculator extends MetricsCalculator
 {
   private int prefixLength       = 5;    // Length of read prefix used to split
                                          // read sequences in different buckets
   private int seedLength         = 30;   // K-mer length of sequence
   
   private long totalReads        = 0;    // Total number of reads
   private long uniqueReads       = 0;    // Number of unique reads
   
   private File tempDir           = null; // Directory where to write temp files
   
   //Table holding references to intermediate files
   private Hashtable<String, BufferedWriter>prefixTable;
   
   /**
    * Class constructor.
    */
   public UniquenessCalculator(File tempDir)
   {
     super();
     prefixTable  = new Hashtable<String, BufferedWriter>();
     this.tempDir = tempDir;
   }
   
   /**
    * Process the next set of reads. Calculate the k-mer string for these reads
    * and store it in the correct file bucket.
    */
   @Override
   void processRead(FastqRecord read1, FastqRecord read2) throws Exception
   {
     if(read1 == null)
     {
       throw new Exception("Encountered null/empty record for read 1");
     }
     String sequenceRead1 = read1.getReadString();
     String sequenceRead2 = null;
 	    
     if(read2 != null)
     {
       sequenceRead2 = read2.getReadString();
     }
 
     StringBuffer kmerString = new StringBuffer(sequenceRead1.substring(0, seedLength));
     if(sequenceRead2 != null && !sequenceRead2.isEmpty())
       kmerString.append(sequenceRead2.substring(0, seedLength));
 
     writeToFileBucket(kmerString.toString());
     kmerString = null;
 
     sequenceRead1 = null;
     sequenceRead2 = null;
   }
 
   /* 
    * Create a result object to enable logging the results.
    */
   @Override
   void buildResultMetrics()
   {
     resultMetric = new ResultMetric();
     resultMetric.setMetricName("Uniqueness");
     resultMetric.addKeyValue("TotalReads", Long.toString(totalReads));
     resultMetric.addKeyValue("UniqueReads", Long.toString(uniqueReads));
    double percentUnique = uniqueReads * 1.0 / totalReads * 100.0;
    resultMetric.addKeyValue("PercentUnique", getFormattedNumber(percentUnique));
   }
 
   /** 
    * Calculate the final result.
    */
   @Override
   void calculateResult()
   {
     BufferedReader fileToRead = null;
     String fileName           = null;
     UniquenessHelper helper   = null;
     
     try
     {
       for(BufferedWriter wr: prefixTable.values())
       {
         wr.close();
       }
       System.err.println("Finished generating intermediate files");
       Enumeration<String> e = prefixTable.keys();
       
       while(e.hasMoreElements())
       {
         fileName    = e.nextElement() + ".seq";
         fileToRead  = new BufferedReader(new FileReader(new File(tempDir, fileName)));
         helper      = new UniquenessHelper(fileToRead, tempDir);
         totalReads  += helper.getTotalReads();
         uniqueReads += helper.getUniqueReads();
         fileToRead.close();
       }
     }
     catch(Exception e)
     {
       // do something here.
     }
   }
 
   /**
    * Helper method to write the k-mer sequence to a file bucket such that
    * all these sequences have the same prefix of length prefixLength.
    * @param kmerSequence
    */
   private void writeToFileBucket(String kmerSequence) throws IOException
   {
     BufferedWriter writer   = null;
     String key = kmerSequence.substring(0, prefixLength);
     
     if(prefixTable.containsKey(key))
     {
       writer = prefixTable.get(key);
       writer.write(kmerSequence);
       writer.newLine();
     }
     else
     {
       File file = new File(tempDir, key + ".seq");
   
       // Delete the temp files when Java VM exits"
       file.deleteOnExit();
       writer    = new BufferedWriter(new FileWriter(file));
       writer.write(kmerSequence);
       writer.newLine();
       prefixTable.put(key, writer);
     }
   }
 }

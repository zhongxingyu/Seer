 /*
  * The MIT License
  *
  * Copyright (c) 2010 The Broad Institute
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 /**
  * Class to modify CIGAR and mapping quality of reads. For mapped reads, if
  * CIGAR
  * extends beyond the chromosome end, clip CIGAR. For unmapped reads, reset
  * mapping
  * quality to zero and reset CIGAR to *.
  */
 import net.sf.picard.cmdline.CommandLineProgram;
 import net.sf.picard.cmdline.Option;
 import net.sf.picard.cmdline.StandardOptionDefinitions;
 import net.sf.picard.cmdline.Usage;
 import net.sf.picard.io.IoUtil;
 import net.sf.samtools.*;
 import net.sf.samtools.util.RuntimeIOException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Nirav Shah niravs@bcm.edu
  *
  */
 public class FixCIGAR extends CommandLineProgram 
 {
     @Usage
     public String USAGE = getStandardUsagePreamble() +
     "Read SAM and perform modify CIGAR and mapping quality. \r\n" +
 //    "For mapped reads where CIGAR extends beyond end of chromosome, clip the CIGAR. \r\n" +
     "For unmapped reads, reset CIGAR to * and mapping quality to zero.";
 
     @Option(shortName = StandardOptionDefinitions.INPUT_SHORT_NAME, doc = "Input SAM/BAM to be cleaned.")
     public File INPUT;
 
     @Option(shortName = StandardOptionDefinitions.OUTPUT_SHORT_NAME, optional=true,
             doc = "Where to write cleaned SAM/BAM. If not specified, replaces original input file.")
     public File OUTPUT;
     
 	/**
 	 * @param args
 	 */
   public static void main(String[] args)
   {
     new FixCIGAR().instanceMainWithExit(args);
   }
   
   /**
    * Do the work after command line has been parsed.
    * RuntimeException may be thrown by this method, and are reported
    * appropriately.
    *
    * @return program exit status.
    */
   @Override
   protected int doWork()
   {
 	  IoUtil.assertFileIsReadable(INPUT);
     long numReadsProcessed = 0;
     
 	  if(OUTPUT != null) OUTPUT = OUTPUT.getAbsoluteFile();
     
     final boolean differentOutputFile = OUTPUT != null;
     
     if(differentOutputFile) IoUtil.assertFileIsWritable(OUTPUT);
     else
     {
       createTempFile();
     }
     
     SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);
     SAMFileReader reader = new SAMFileReader(INPUT);
     SAMFileWriter writer = new
     SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), true, OUTPUT);
     
     CigarFixingIterator it =new CigarFixingIterator(reader.iterator());
     while(it.hasNext())
     {
       if(numReadsProcessed % 1000000 == 0)
       {
         System.err.print("Processed : " + numReadsProcessed + " reads\r");
       }
       writer.addAlignment(it.next());
     }
     writer.close();
     reader.close();
     it.close();
     
     if(differentOutputFile) return 0;
     else return replaceInputFile();
   }
   
   protected void createTempFile()
   {
     final File inputFile = INPUT.getAbsoluteFile();
     final File inputDir  = inputFile.getParentFile().getAbsoluteFile();
     
     try
     {
       IoUtil.assertFileIsWritable(inputFile);
       IoUtil.assertDirectoryIsWritable(inputDir);
       OUTPUT = File.createTempFile(inputFile.getName()+ "_being_fixed", ".bam", inputDir);
     }
     catch(IOException ioe)
     {
       throw new RuntimeIOException("Could not create tmp file in " + inputDir.getAbsolutePath());
     }
   }
   
   protected int replaceInputFile()
   {
     final File inputFile = INPUT.getAbsoluteFile();
     final File oldFile = new File(inputFile.getParentFile(), inputFile.getName() + ".old");
     
     if(!oldFile.exists() && inputFile.renameTo(oldFile))
     {
       if(OUTPUT.renameTo(inputFile))
       {
         if(!oldFile.delete())
         {
           System.err.println("Could not delete old file : " + oldFile.getAbsolutePath());
           return 1;
         }
       }
       else
       {
         System.err.println("Could not move temp file to : " + inputFile.getAbsolutePath());
         System.err.println("Input file preserved as : " + oldFile.getAbsolutePath());
         System.err.println("New file preserved as : " + OUTPUT.getAbsolutePath());
         return 1;
       }
     }
     else
     {
       System.err.println("Could not move input file : " + inputFile.getAbsolutePath());
       System.err.println("New file preserved as : " + OUTPUT.getAbsolutePath());
       return 1;
     }
     return 0;
   }
 }

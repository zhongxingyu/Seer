 package com.inescid.cnm;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 public class App
 {
     public static float SPS;
     public static int numberOverlaps = 0;
     public static int numberGaps = 0;
 
     public static void main(String[] args)
     {
         ReaderCliOptions opt = new ReaderCliOptions();
         opt.parse(args);
         System.out.println(opt + "\n");
 
         //Lets read the mseed file, order it, transform it to a collection of samples, and then write the samples in a file
         try
         {
            System.out.println(String.format("Reading mseed file: %s", opt.inputFilePath));
 
             IDataReader reader;
 
             if(opt.isInputJson)
                reader = new ReadJson(opt.inputFilePath);
             else
                reader = new ReadMSeed(opt.inputFilePath, opt.debug, opt.inputWithSequenceNumber);
 
             SPS = reader.getSPS();
 
             List<Sample> sampleList = reader.getSamples();
             if(validSampleList(sampleList))
             {
                 System.out.println("All samples are valid");
             }
 
             printStatistics(sampleList.size(), numberOverlaps, numberGaps);
 
             if (!opt.onlyCheck)
             {
                 System.out.println("Writting to output file treated data");
                 writeSampleList(sampleList, opt.outputDataFilePath, opt.softLineLimit, opt.softLineLimitValue, opt.outputWithTime);
             }
         }
         catch (FileNotFoundException e1)
         {
            System.out.println("Unable to find input mseed file: " + opt.inputFilePath);
             System.exit(1);
         }
     }
 
     private static boolean validSampleList(List<Sample> sampleList)
     {
         long delta = 0;
         Sample lastSample = sampleList.get(0);
         long last_sample_time = sampleList.get(0).getTs().getTime();
         boolean valid = true;
         boolean first = true;
 
         // Check for gaps in time collection
         for (Sample s : sampleList)
         {
             long this_sample_time = s.getTs().getTime();
             delta = last_sample_time - this_sample_time;
 
             // If delta is higher than the SPS
             if (delta > (1000 / SPS) && !first)
             {
                 System.out.println("Expected SPS/Delta: " + SPS + " / " + delta + "\t\tGap in data records at time: " + s.toStringJustTS() + " / " + lastSample.toStringJustTS());
                 numberGaps += 1;
                 valid = false;                  
             }
 
             // Check if they are repeated values
             if (this_sample_time == last_sample_time && !first)
             {
                 System.out.println("Repeated sample with time: " + s.toStringDate() + "\t\t" + lastSample.toStringDate());
                 numberOverlaps += 1;
                 valid = false;                  
             }
             lastSample = s;
             last_sample_time = this_sample_time;
             first = false;
         }
 
         return valid;
     }
 
 
 
     private static void writeSampleList(Collection<Sample> sampleList, String dataOutFilepath, Boolean softLineLimit, int softLineLimitValue, Boolean outputWithTime){
         BufferedWriter out;
         int lines = 0;
         try
         {
             out = new BufferedWriter(new FileWriter(dataOutFilepath));
             for(Sample sample : sampleList){
                 out.write(sample.toString(outputWithTime) + "\n");
 
                 lines +=1;
                 if(softLineLimit && lines > softLineLimitValue)
                 {
                     break;   
                 }
             }
 
             out.close();
         }
 
         catch (IOException e)
         {
             System.out.println("Could not write data file");
             e.printStackTrace();
         }
     }
 
     private static void printStatistics(int number_samples, int numberOverlaps, int numberGaps)
     {
             float n_samp = (float) number_samples;
             String message = String.format("Analysed %f samples. Overlaps: %d (%f%%)\tGaps: %d (%f%%)", 
                     n_samp, numberOverlaps, (numberOverlaps / n_samp) * 100, numberGaps, (numberGaps / n_samp) * 100);
             System.out.println(message);
     }
 }

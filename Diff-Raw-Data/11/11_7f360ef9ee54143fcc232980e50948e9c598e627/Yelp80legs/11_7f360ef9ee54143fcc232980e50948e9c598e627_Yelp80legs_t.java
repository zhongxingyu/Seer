 package com.where.atlas.feed.yelp;
 
 import java.io.File;
 import java.util.Enumeration;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import com.where.utils.XMLfixer;
 
 
 
 /**
  * @author fliuzzi
  * 
  */
 
 public class Yelp80legs {
 
 	public static void main(String[] args) {
         if(args ==  null || args.length != 4)
         {
             System.err.println("USAGE: program will go through a directory of yelp 80legs raw data zips"+
             						" and parse them to a neat JSON");
             System.err.println("ARG0: directory of raw data .zips");
             System.err.println("ARG1: Output flatfile target path");
             System.err.println("ARG2: Thread count (int)");
            System.err.println("ARG3: if you want text reviews in the output, type in true; otherwise false");
             return;
         }
         
         try{
         	
             File zipDirectory = new File(args[0]);
             final File[] zipFiles = zipDirectory.listFiles();
             
             if(zipFiles == null){
                 System.err.println("Directory Error");
                 return;
             }
             else{
                 final Yelp80legsParser parser = new Yelp80legsParser(args[1]);
                 parser.setFlag(args[3]);
                 ExecutorService thePool = Executors.newFixedThreadPool(Integer.parseInt(args[2]));
                 
                 final Yelp80legsCollector collector = new Yelp80legsCollector();
                 
                 for(int x = 0; x < zipFiles.length; x++)
                 {
                     final int lcv = x;
                 	@SuppressWarnings("unused")
 					Future<?> fut = thePool.submit(
                             new Runnable(){ public void run(){
                             	try{
                             	final ZipFile zipFile = new ZipFile(zipFiles[lcv]);
                             	
                             	for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
                             		
                             		final ZipEntry entry = (ZipEntry) e.nextElement();
                         
                         
                                 	System.out.println("Zip File #"+(lcv+1)+" : " + zipFiles[lcv].getName());
                                 	
                                     parser.parse(collector, XMLfixer.repairXML(zipFile.getInputStream(entry)));
                                     
 									
                                     System.out.println("\nFinished zip" + zipFile.getName());
                             	}
                             	zipFile.close();
                             }
                             catch(Exception e)
                             {e.printStackTrace();}
                             
                             }});
                         
                     }
                     
                     thePool.shutdown();
                     thePool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
                     
                     //close writer
                     System.out.println("Done!");
                 }
                 
                 
                 
             }
         catch(Throwable e){
             e.printStackTrace();
             }
 
 	}
 
 }

 /** 
  * Author:  anthony.fodor@gmail.com    
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 
 package scripts.sequenceScripts;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import parsers.FastQ;
 
 import utils.ConfigReader;
 import coPhylog.ContextCount;
 import coPhylog.ContextHash;
 
 public class CoPhylogOnBurk
 {
 	public static void main(String[] args) throws Exception
 	{
 		  
 		File sequenceDir = new File(ConfigReader.getBurkholderiaDir());
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File( 
 				sequenceDir + File.separator + "results" + File.separator + "log.txt")));
 		
 		String[] files = sequenceDir.list();
 		
 		for(String s : files)
 		{
 			if( s.endsWith("gz"))
 			{
 				File outFile = new File(sequenceDir + File.separator + "results" + File.separator + 
 						s + "_CO_PhylogBin.gz");
 				
 				if( ! outFile.exists())
 				{
 					try
 					{
 						System.out.println("RUNNING " + outFile.getAbsolutePath());
 						runAFile(new File(sequenceDir.getAbsolutePath() + File.separator + s), outFile,writer);
 					}
 					catch(Exception ex)
 					{
 						log(ex,writer);
 					}
 				}
 				else
 				{
 					log(outFile.getAbsolutePath() + "exists.  Skipping",writer);
 				}
 				
 			}
 		}
 		
 		writer.flush();  writer.close();
 			
 	}
 	
 	private static void log(Exception ex, BufferedWriter writer) throws Exception
 	{
 		ex.printStackTrace();
 		
 		StringWriter tempWriter = new StringWriter();
 		PrintWriter printWriter = new PrintWriter( tempWriter );
 		ex.printStackTrace( printWriter );
 		printWriter.flush();
 
 		String stackTrace = tempWriter.toString();
 		
 		writer.write(stackTrace + "\n");
 		
 		printWriter.close();  tempWriter.close();
 	}
 	
 	private static void log(String message, BufferedWriter writer ) throws Exception
 	{
 		System.out.println(message);
 		writer.write(message + "\n");
 		writer.flush();
 	}
 	
 	public static void runAFile(File inFile, File outFile, BufferedWriter log) throws Exception
 	{	
 		log("Starting " + inFile.getPath(),log);
 		HashMap<Long, ContextCount> map = new HashMap<>();
 		
 		int contextSize = 13;
 		
 		BufferedReader reader = 
 				inFile.getName().toLowerCase().endsWith("gz") ? 
 						new BufferedReader(new InputStreamReader( 
 								new GZIPInputStream( new FileInputStream( inFile)))) :  
 						new BufferedReader(new FileReader(inFile)) ;
 		
 		int numDone =0;
 		int numRemoved =0;
 		
 		for(FastQ fq = FastQ.readOneOrNull(reader); 
				fq != null && numDone < 2000000; 
 				fq = FastQ.readOneOrNull(reader))
 		{
 			ContextHash.addToHash(fq.getSequence(), map, contextSize);
 			numDone++;
 			
 			if(numDone % 10000 == 0 )
 			{
 				log( numRemoved + " " +  numDone + " " + map.size() + " " + (((float)map.size())/numDone) + " "+ 
 				Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().totalMemory() +  " " + Runtime.getRuntime().maxMemory() 
 				+ " " + ((double)Runtime.getRuntime().freeMemory())/Runtime.getRuntime().maxMemory(),log );
 				
 				
 				double fractionFree= 1- (Runtime.getRuntime().totalMemory()- ((double)Runtime.getRuntime().freeMemory() ))
 								/Runtime.getRuntime().totalMemory();
 				
 				log("fraction Free= " + fractionFree,log);
 				
 				double fractionAllocated = 1-  (Runtime.getRuntime().maxMemory()- ((double)Runtime.getRuntime().totalMemory() ))
 						/Runtime.getRuntime().maxMemory();
 				
 				log("fraction allocated = " + fractionAllocated,log);
 				
 				if( fractionFree <= 0.10 && fractionAllocated >= 0.90 )
 					removeSingletons(map,log);
 				
 				System.out.println("\n\n");
 			}
 				
 		}
 		
 		reader.close();
 		
 		log("Finished reading with " + map.size() + " having removed " + numRemoved + " singletons ",log);
 		
 		removeSingletons(map,log);
 		
 		log("Removed singletons " + map.size() ,log);
 		log("Writing text file",log);
 		
 		
 		writeBinaryFile(outFile, map);
 		
 		System.out.println("Finished " + inFile.getAbsolutePath());
 	}
 	
 	private static int removeSingletons(HashMap<Long, ContextCount> map, BufferedWriter log) 
 		throws Exception
 	{
 		log("Removing singletons",log);
 		int num =0;
 		
 		for( Iterator<Long> i = map.keySet().iterator(); i.hasNext(); )
 		{
 			if( map.get(i.next()).isSingleton() )
 			{
 				i.remove();
 				num++;
 			}
 				
 		}
 		
 		return num;
 	}
 	
 	private static void writeBinaryFile(File outFile, HashMap<Long, ContextCount> map ) throws Exception
 	{
 		DataOutputStream out =new DataOutputStream( new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFile))));
 		
 		out.writeInt(map.size());
 		
 		for( Long l : map.keySet() )
 		{
 			out.writeLong(l);
 			
 			ContextCount cc = map.get(l);
 			
 			out.writeByte( cc.getAAsByte() );
 			out.writeByte( cc.getCAsByte());
 			out.writeByte(cc.getGAsByte());
 			out.writeByte( cc.getTAsByte());
 		}
 		
 		out.flush();  out.close();
 		
 	}
 	
 	/*
 	private static void writeTextFile( File outFile, HashMap<Long, ContextCount> map ) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 		
 		writer.write("bits\tnumA\tnumC\tnumG\tnumT\n");
 		
 		for( Long l : map.keySet() )
 		{
 			writer.write(l + "\t");
 			
 			ContextCount cc = map.get(l);
 			
 			writer.write(cc.getNumA() + "\t");
 			writer.write(cc.getNumC() + "\t");
 			writer.write(cc.getNumG() + "\t");
 			writer.write(cc.getNumT() + "\n");
 		}
 		
 		writer.flush();  writer.close();
 	}
 	*/
 }

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
 
 
 package scripts.clusterManipulations;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import parsers.FastaSequence;
 import parsers.FastaSequenceOneAtATime;
 
 public class SplitFasta
 {
 	/*
 	 * This assumes a 200 basepair read which will be split into each paired end
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		if( args.length != 2)
 		{
 			System.out.println("Usage SplitFasta fileToSplit numSequencesPerSplit");
 			System.exit(1);
 		}
 		
 		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(args[0]);
 		int splitSize = Integer.parseInt(args[1]);
 		
 		int count=0;
 		int file =1;
 		long seqNum =0;
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[0] + "_FILE_" + file)));
 		
 		for( FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence() )
 		{
 			count++;
 			
 			if( fs.getSequence().length() != 200 )
 				throw new Exception("Expecting a 200 basepair length");
 			
 			if( count == splitSize)
 			{
 				writer.flush();  writer.close();
 				count =0;
 				writer = new BufferedWriter(new FileWriter(new File(args[0] + "_FILE_" + file)));
 				System.out.println("Finished " + args[0] + "_FILE_" + file);
 				file++;
 			}
 			
 			seqNum++;
			writer.write(">A" + seqNum + "\n");
 			writer.write(fs.getSequence().substring(0, 100) + "\n");
 			
 			seqNum++;
 			writer.write(">A" + seqNum + "\n");
 			writer.write(fs.getSequence().substring(100, fs.getSequence().length()) + "\n");
 			
 		}
 		
 		System.out.println("Finished");
 		writer.flush();  writer.close();
 	}
 }

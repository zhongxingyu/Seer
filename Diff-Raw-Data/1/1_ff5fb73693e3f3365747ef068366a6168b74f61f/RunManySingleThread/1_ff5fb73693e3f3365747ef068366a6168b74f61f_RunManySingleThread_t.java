 /** 
  * Author:  anthony.fodor@gmail.com
  * 
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package eTree;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import parsers.FastaSequence;
 import parsers.FastaSequenceOneAtATime;
 
 import dereplicate.DereplicateBySample;
 
 import utils.ConfigReader;
 
 public class RunManySingleThread
 {
 	/*
 	 * First run dereplicate.DereplicateBySample
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		File dir = new File(ConfigReader.getETreeTestDir() + File.separator + 
 				"gastro454DataSet" + File.separator );
 		
 		ETree eTree = new ETree();
 		
 		int numDone =0;
 		
 		List<String> fileNames = new ArrayList<String>();
 		for( String s : dir.list())
 			fileNames.add(s);
 		
 		Collections.shuffle(fileNames);
 		
 		for(String s : fileNames)
 			if( s.startsWith(DereplicateBySample.DEREP_PREFIX))
 			{
 				numDone++;
 				File file = new File(dir.getAbsolutePath() + File.separator + s);
 				System.out.println(file.getAbsolutePath());
 				FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(file);
 				
 				int x=0;
 				for( FastaSequence fs = fsoat.getNextSequence(); 
 						fs != null; 
 							fs = fsoat.getNextSequence())
 				{
 					eTree.addSequence(fs.getSequence(), 
 						ETree.getNumberOfDereplicatedSequences(fs), 
 						s.replace(DereplicateBySample.DEREP_PREFIX, ""));
 					System.out.print(" " + ++x);
 				}
 				System.out.println("\nFinished " + numDone);
 			}
 		
 		eTree.writeAsSerializedObject(ConfigReader.getETreeTestDir() + File.separator + "mel74tree.etree");
 		eTree.writeAsXML(ConfigReader.getETreeTestDir() + File.separator + "mel74phyloXML.xml");
 
 	}
	
 }

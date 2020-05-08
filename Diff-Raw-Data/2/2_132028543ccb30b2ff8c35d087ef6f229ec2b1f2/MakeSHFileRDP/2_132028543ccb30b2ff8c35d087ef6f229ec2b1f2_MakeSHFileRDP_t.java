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
 
 package bottomUpTree.rdp;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.ConfigReader;
 
 public class MakeSHFileRDP
 {
 	/*
 	 * For the ISME colorectal adenomas dataset
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		File dir = new File(ConfigReader.getNinaWithDuplicatesDir() + File.separator + "rdp");
 		
 		List<String> fileNames = new ArrayList<String>();
 		for( String s : dir.list())
 			fileNames.add(s);
 		
 		BufferedWriter mainBatFile = new BufferedWriter(new FileWriter(new File( 
 				dir.getAbsolutePath()+ File.separator +  "runAll.sh")));
 		
 		for(String s : fileNames)
 			if( s.endsWith("fas"))
 			{
 				File shFile = 
						new File( dir.getAbsolutePath()+ File.separator + "run" + s  + ".sh");
 				
 				BufferedWriter aSHWriter = new BufferedWriter(new FileWriter(shFile));
 				
 				aSHWriter.write("java -jar /users/afodor/rdp/rdp_classifier_2.6/dist/classifier.jar -q " + 
 				dir.getAbsolutePath() + File.separator + s + " -o " + dir.getAbsolutePath() + File.separator +  s +"_rdpOut.txt");
 				
 				mainBatFile.write("qsub -N \"RunClust" + s+ "\"  -q \"viper\" " + shFile.getAbsolutePath() + "\n");
 				
 				aSHWriter.flush();  aSHWriter.close();
 			}
 		
 		mainBatFile.flush();  mainBatFile.close();
 	}
 	
 }

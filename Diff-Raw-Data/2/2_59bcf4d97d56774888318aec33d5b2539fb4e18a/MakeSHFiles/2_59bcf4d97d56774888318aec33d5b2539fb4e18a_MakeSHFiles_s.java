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
 
 
 package bottomUpTree;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.ConfigReader;
 import dereplicate.DereplicateBySample;
 
 public class MakeSHFiles
 {
 	public static void main(String[] args) throws Exception
 	{
 		File dir = new File(ConfigReader.getETreeTestDir() + File.separator + 
 				"gastro454DataSet" + File.separator );
 		
 		List<String> fileNames = new ArrayList<String>();
 		for( String s : dir.list())
 			fileNames.add(s);
 		
 		BufferedWriter mainBatFile = new BufferedWriter(new FileWriter(new File( 
 				ConfigReader.getETreeTestDir() + File.separator + 
 				"gastro454DataSet" + File.separator + "runAll.sh")));
 		
 		for(String s : fileNames)
 			if( s.startsWith(DereplicateBySample.DEREP_PREFIX))
 			{
 				File shFile = 
 						new File( 
 							ConfigReader.getETreeTestDir() + File.separator + 
 							"gastro454DataSet" + File.separator + "run" + s.replace(DereplicateBySample.DEREP_PREFIX, "") + ".sh");
 				
 				BufferedWriter aSHWriter = new BufferedWriter(new FileWriter(shFile));
 				
 				aSHWriter.write("java -mx3000m /users/afodor/metagenomicsTools/bin/bottomUpTree/RunOne " + 
 				dir.getAbsolutePath() + File.separator + s + " " + s +"_CLUST.clust");
 				
 				mainBatFile.write("qsub -N \"" + s.replace(DereplicateBySample.DEREP_PREFIX, "")
								+ "\"  -q \"viper\" " + shFile.getAbsolutePath());
 				
 				aSHWriter.flush();  aSHWriter.close();
 			}
 		
 		mainBatFile.flush();  mainBatFile.close();
 	}
 }

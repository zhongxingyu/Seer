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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.ConfigReader;
 import eTree.ENode;
 import eTree.ETree;
 
 public class ETreeFromBottom
 {
 	/*
 	 * First run RunOne for all 74 samples
 	 * Then run MergeMultipSamplesAtLevelSingleThread.
 	 * The file bottomUpMelMergedStartingCleanedOnce is created the first time this code is run 
 	 * and can be re-used to save time...
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		ETree etree = new ETree();
 		
 		List<ENode> list= ReadCluster.readFromFile(
		ConfigReader.getETreeTestDir() + File.separator + "Merged74At03.merged",false,false);
 		PivotOut.pivotOut(list, ConfigReader.getETreeTestDir() + File.separator +  "bottomUpMelMerged"+ 
 				File.separator + "bottomUpMel740.03.txt");
 		
 		if( etree.LEVELS[etree.LEVELS.length-1] != 0.03f)
 			throw new Exception("Expecting bottom of 0.03 " + etree.LEVELS[etree.LEVELS.length-1]);
 		
 		for( int x=etree.LEVELS.length-2; x >=0; x--)
 		{
 			System.out.println(etree.LEVELS[x]);
 			List<ENode> newList = new ArrayList<ENode>();
			ClusterAtLevel.clusterAtLevel(newList, list, etree.LEVELS[x], etree.LEVELS[x] + 0.05f, 
 					"mel74" + etree.LEVELS[x] + "_round1",  ClusterAtLevel.MODE.SISTER_MERGE);
 			List<ENode> newList2 = new ArrayList<ENode>();
 			System.out.println("First round " + newList.size());
 			ClusterAtLevel.clusterAtLevel(newList2, newList, etree.LEVELS[x], 
 					etree.LEVELS[x] + 0.05f,"mel74" + etree.LEVELS[x] + "_round2", ClusterAtLevel.MODE.PARENT_MERGE);
 			System.out.println("Writing " + newList2.size() );
 			PivotOut.writeBinaryFile(ConfigReader.getETreeTestDir() + File.separator + "bottomUpMelMerged"+ 
 					etree.LEVELS[x] + ".merged", newList2);
 			PivotOut.pivotOut(newList2, ConfigReader.getETreeTestDir() + File.separator + "bottomUpMel740" + 
 					etree.LEVELS[x] + ".txt");
 			list = newList2;
 		}
		
		ENode rootNode = new ENode(null, "root", 0, null);
		for(ENode enode : list)
			enode.setParent(rootNode);
		
		PivotOut.writeBinaryFile(ConfigReader.getETreeTestDir() + File.separator + "bottomUpMelMerged"+ 
				"finalTree.merged", list);		
 	}
 }

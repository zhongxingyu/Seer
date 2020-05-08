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
 
 package bottomUpTree;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import bottomUpTree.pvalues.GetOneWayAnovas;
 
 import parsers.NewRDPNode;
 import parsers.NewRDPParserFileLine;
 import probabilisticNW.ProbSequence;
 
 import utils.ConfigReader;
 
 import eTree.ENode;
 import eTree.ETree;
 
 public class BuildJSONDataStructure
 {
 	public static void main(String[] args) throws Exception
 	{
 		List<ENode> list= ReadCluster.readFromFile(
 				ConfigReader.getETreeTestDir() + File.separator + "bottomUpMelMerged0.2.merged",false, false);
 		
 		ENode rootNode = new ENode(new ProbSequence("ACGT", ETree.ROOT_NAME), ETree.ROOT_NAME, 0, null) ;
 		
 		int numNodes=0;
 		for( ENode node : list)
 		{
 			node.setParent(rootNode);
 			rootNode.getDaughters().add(node);
 			numNodes++;
 			rootNode.getProbSequence().setMapCount(rootNode.getProbSequence(), node.getProbSequence());
 		}
 		
 		System.out.println("Proceeding with " + numNodes);
 		
 		rootNode.validateNodeAndDaughters(true);
 		HashMap<String, Double> pValueSubject = GetOneWayAnovas.getOneWayAnovaPValues(rootNode);
 		System.out.println(pValueSubject);
 		
 		ETree etree = new ETree();
 		etree.setTopNode(rootNode);
 		
 		HashMap<String, NewRDPParserFileLine> rdpMap = etree.tryForRDPMap(new File(ConfigReader.getETreeTestDir() + File.separator 
 				+  "mel04RDPFfile.txt"));
 		
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File( 
 			ConfigReader.getD3Dir() + File.separator + "aTree2.json"	)));
 		
		writeNodeAndChildren(writer, rootNode,10, rdpMap,pValueSubject);
 		 
 		writer.flush();  writer.close();
 		
 	}
 	
 	private static String getRDPString(String rdpLevel, String eNodeName,
 			HashMap<String, NewRDPParserFileLine> rdpMap )
 		throws Exception
 		{
 			String returnString = "unclassified";
 			NewRDPParserFileLine fileLine = rdpMap.get(eNodeName);
 			
 			if( fileLine != null)
 			{
 				NewRDPNode node = fileLine.getTaxaMap().get(rdpLevel);
 				if ( node != null)
 					returnString = node.getTaxaName();
 			}
 			
 			return returnString;
 			
 		}
 	
 	private static void writeNodeAndChildren( BufferedWriter writer,
 					ENode enode, double cutoff, HashMap<String, NewRDPParserFileLine> rdpMap,
 					HashMap<String, Double> pValuesSubject) throws Exception
 	{
 		NewRDPParserFileLine fileLine = rdpMap.get(enode.getNodeName());
 		
 		String rdpString = "NotClassified";
 		
 		if( fileLine != null)
 			rdpString = fileLine.getSummaryString().replaceAll(";", " ");
 		
 		writer.write("{\n");
 		
 		writer.write("\"numSeqs\": " +  enode.getNumOfSequencesAtTips() + ",\n");		
 		writer.write("\"otuLevel\": " +  enode.getLevel()+ ",\n");
 		writer.write("\"rdpString\": \"" +  rdpString+ "\",\n");
 		writer.write("\"pvalue_Subject\": \"" +  pValuesSubject.get(enode.getNodeName())+ "\",\n");
 		
 		for( int x=1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++)
 		{
 			String valString = getRDPString(NewRDPParserFileLine.TAXA_ARRAY[x], enode.getNodeName(), rdpMap);
 			writer.write("\"" + NewRDPParserFileLine.TAXA_ARRAY[x]+ "\": \"" +  valString+ "\",\n");
 		}
 		
 		writer.write("\"nodeName\": \"" +  enode.getNodeName()+ "\"\n");
 		
 		List<ENode> toAdd = new ArrayList<ENode>();
 		
 		for( ENode d : enode.getDaughters())
 			if(d.getNumOfSequencesAtTips() >= cutoff)
 				toAdd.add(d);
 		
 		if ( toAdd.size() > 0) 
 		{
 			writer.write(",\"children\": [\n");
 				
 			for( Iterator<ENode> i = toAdd.iterator(); i.hasNext();)
 			{
 				writeNodeAndChildren(writer,i.next(), cutoff, rdpMap, pValuesSubject);
 				if( i.hasNext())
 					writer.write(",");
 			}
 					writer.write("]\n");
 		}
 		
 		
 		writer.write("}\n");
 	}
 }

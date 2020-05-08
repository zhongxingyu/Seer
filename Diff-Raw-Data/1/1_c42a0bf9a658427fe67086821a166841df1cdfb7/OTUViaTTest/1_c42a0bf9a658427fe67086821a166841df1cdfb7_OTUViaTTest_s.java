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
 
 package ratSaccharine;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import parsers.OtuWrapper;
 import utils.Avevar;
 import utils.ConfigReader;
 import utils.TTest;
 
 public class OTUViaTTest
 {
 
 	public static void main(String[] args) throws Exception
 	{
 		HashMap<String, MetadataFileLine> map = MetadataFileLine.getMap();
 		
 		OtuWrapper wrapper = new OtuWrapper(ConfigReader.getSaccharineRatDir() 
 				+ File.separator 
 				+ "otuCounts.txt");
 		
 		String[] tissues = {"CECUM", "STOOL", "COLON"};
 		
 		for( String s : tissues)
 			writeTTestFile(s, map, wrapper);
 		
 		System.out.println(map);
 	}
 	
 	private static void addToMap( HashMap<String, List<List<Double>>> map, OtuWrapper wrapper,
 			String cage, int sampleIndex) throws Exception
 	{
 		List<List<Double>> innerList = map.get(cage);
 		
 		if(innerList == null)
 		{
 			innerList =new ArrayList<List<Double>>();
 			
 			for(int x=0; x < wrapper.getOtuNames().size(); x++)
 				innerList.add(new ArrayList<Double>());
 			
 			map.put(cage,innerList);
 		}
 		
 		for( int x=0; x < wrapper.getOtuNames().size(); x++)
 			innerList.get(x).add(wrapper.getDataPointsNormalizedThenLogged().get(sampleIndex).get(x));
 	}
 	
 	private static class Holder implements Comparable<Holder>
 	{
 		String taxa;
 		double pValue=1;
 		double averageLowSach;
 		double avergeHighSach;
 		
 		@Override
 		public int compareTo(Holder arg0)
 		{
 			return Double.compare(this.pValue, arg0.pValue);
 		}
 	}
 	
 	private static void writeTTestFile(String tissue, 
 			HashMap<String, MetadataFileLine> metaMap, OtuWrapper wrapper)
 		throws Exception
 	{
 		System.out.println(tissue);
 		// the key is the cage
 		// the outer list has one entry for each OTU
 		// the inner list has one entry for each cage
 		HashMap<String, List<List<Double>>> lowSachData = new HashMap<String, List<List<Double>>>();
 		HashMap<String, List<List<Double>>> highSachData = new HashMap<String, List<List<Double>>>();
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
 				ConfigReader.getSaccharineRatDir() + 
 					File.separator + "otuTTests"  + tissue + ".txt")));
 		
 		for( int x=0; x < wrapper.getSampleNames().size(); x++)
 		{
 			MetadataFileLine mfl = metaMap.get(wrapper.getSampleNames().get(x));
 			
 			if( mfl.getTissue().equals(tissue))
 			{
 				if( mfl.getPhenotype().equals("Low"))
 				{
 					addToMap(lowSachData, wrapper, mfl.getCage(), x);
 				}
 				else if( mfl.getPhenotype().equals("High"))
 				{
 
 					addToMap(highSachData, wrapper, mfl.getCage(), x);
 				}
 				else throw new Exception("No");
 			}
 			
 		}
 		
 		List<Holder> list = new ArrayList<Holder>();
 		
 		for( int x=0; x < wrapper.getOtuNames().size(); x++)
 		{
 			List<Number> mediansHi = new ArrayList<Number>();
 			List<Number> mediansLow = new ArrayList<Number>();
 			
 			for(List<List<Double>> aList : lowSachData.values())
 			{
 				mediansLow.add( MedianOfCage.getMedian(aList.get(x)));
 			}
 			
 			for(List<List<Double>> aList : highSachData.values())
 			{
 				mediansHi.add( MedianOfCage.getMedian(aList.get(x)));
 			}
 			
 			Holder h = new Holder();
 			h.taxa = wrapper.getOtuNames().get(x);
 			h.averageLowSach = new Avevar(mediansLow).getAve();
 			h.avergeHighSach = new Avevar(mediansHi).getAve();
 			
 			try
 			{
 				h.pValue = TTest.ttestFromNumberUnequalVariance(mediansHi, mediansLow).getPValue();
 			}
 			catch(Exception ex)
 			{
 				
 			}
 		}
 		
 		writer.write("taxa\tlowAverage\thiAverage\tpValue\tfdrPValue\n");
 		
 		Collections.sort(list);
 		
 		int rank=1;
 		for( Holder h : list)
 		{
 			writer.write(h.taxa + "\t");
 			writer.write(h.averageLowSach + "\t");
 			writer.write(h.avergeHighSach + "\t");
 			writer.write(h.pValue + "\t");
 			writer.write(list.size() * h.pValue / rank + "\n");
 			rank++;
 		}
 		
 		writer.flush();  writer.close();
 	}
 	
 }

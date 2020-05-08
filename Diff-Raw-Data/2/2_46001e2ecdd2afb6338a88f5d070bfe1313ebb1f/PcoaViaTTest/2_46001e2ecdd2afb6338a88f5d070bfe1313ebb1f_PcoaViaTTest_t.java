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
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import utils.ConfigReader;
 import utils.TTest;
 
 public class PcoaViaTTest
 {
 
 	public static void main(String[] args) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ConfigReader.getSaccharineRatDir() + File.separator + 
 				"pcoaAxes.txt")));
 		
 		writer.write("axis\tcecum\tcolon\tstool\n");
 		
 		String[] tissues = { "CECUM", "COLON" ,"STOOL"};
 		
 		for( int x=1; x <=20; x++)
 		{
 			writer.write( "" + x );
 			
 			for( String s : tissues)
 			{
 				writer.write("\t" + getPValue(s, x) );
 			}
 			
 			writer.write("\n");
 		}
 		
 		writer.flush();  writer.close();
 	}
 	
 	private static double getPValue( String tissue, int axisNum ) throws Exception
 	{
 		BufferedReader reader = new BufferedReader(new FileReader(new File( 
 			ConfigReader.getSaccharineRatDir() + File.separator + "mergedMapBrayCurtisPCOA.txt"	)));
 		
 		HashMap<String, List<Double>> highCages = new HashMap<String, List<Double>>();
 		HashMap<String, List<Double>> lowCages = new HashMap<String, List<Double>>();
 		
 		reader.readLine();
 		
 		for( String s= reader.readLine(); s != null ; s= reader.readLine())
 		{
 			String[] splits = s.split("\t");
 			
 			if(splits[3].equals(tissue))
 			{
				double val = Double.parseDouble(splits[23+axisNum]);
 				
 				String cage = splits[8];
 				
 				if( splits[7].equals("Low"))
 					addToMap(val, cage, lowCages);
 				else if( splits[7].equals("High"))
 					addToMap(val, cage, highCages);
 				else throw new Exception("No");
 			}
 		}
 		
 		List<Number> lowVals = new ArrayList<Number>();
 		List<Number> highVals = new ArrayList<Number>();
 		
 		for( String s : lowCages.keySet() )
 			lowVals.add(MedianOfCage.getMedian(lowCages.get(s)));
 		
 		for( String s : highCages.keySet())
 			highVals.add(MedianOfCage.getMedian(highCages.get(s)));
 		
 		double pValue = 1;
 		
 		try
 		{
 			pValue = TTest.ttestFromNumber(lowVals, highVals).getPValue();
 		}
 		catch(Exception ex)
 		{
 			
 		}
 		
 		return pValue;
 	}
 	
 	private static void addToMap( double val, String cage, HashMap<String, List<Double>> map )
 	{
 		List<Double> list = map.get(cage);
 		
 		if( list == null)
 		{
 			list = new ArrayList<Double>();
 			map.put(cage,list);
 		}
 		
 		list.add(val);
 	}
 }

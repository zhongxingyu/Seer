 /*
  * #%L
  * Cyni Implementation (cyni-impl)
  * $Id:$
  * $HeadURL:$
  * %%
  * Copyright (C) 2006 - 2013 The Cytoscape Consortium
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.cytoscape.cyni.internal.metrics;
 
 
 import java.util.*;
 
 import org.cytoscape.cyni.*;
 
 
 /**
  * The BasicInduction provides a very simple Induction, suitable as
  * the default Induction for Cytoscape data readers.
  */
 public class EntropyMetric extends AbstractCyniMetric {
 	
 	private static Map<String,Integer> mapStringValues;
 	/**
 	 * Creates a new  object.
 	 */
 	public EntropyMetric() {
 		super("Entropy.cyni","Entropy Metric");
 		addType(CyniMetricTypes.INPUT_STRINGS.toString());
 		addType(CyniMetricTypes.LOCAL_METRIC_SCORE.toString());
 		mapStringValues =  new HashMap<String,Integer>();
 		
 	}
 	
 	public void resetParameters()
 	{
 		if(!mapStringValues.isEmpty())
 			mapStringValues.clear();
 	}
 
 	
 	public Double getMetric(CyniTable table1, CyniTable table2, int indexBase,List<Integer> indexToCompare) { 
 		double result = 0.0;
 		int i = 0;
 		int ncols,col;
 		int count = 0;
 		int numValues ;
 
 		
 		if(mapStringValues.size() != table1.getAttributeStringValues().size())
 		{
 			i=0;
 			mapStringValues.clear();
 			for(String name : table1.getAttributeStringValues())
 			{
 				mapStringValues.put(name, i);
 				i++;
 				System.out.println("attribute: " + name);
 			}
 		}
 		
 		if(indexToCompare.size() == 0)
 			return result;
 		
 		numValues =  mapStringValues.size();
 		
 		int[] nCounts = new int[ (int)Math.pow((double)numValues, (double)(indexToCompare.size()+1))];
 		int[] nodes ;
 			
 		
 		ncols = table1.nColumns();
 		if(indexToCompare.size() == 1)
 		{
 			if(indexToCompare.get(0) == indexBase)
 				nodes = new int[indexToCompare.size()];
 			else
 				nodes = new int[indexToCompare.size()+1];
 		}
 		else
 			nodes = new int[indexToCompare.size()+1];
 
 		i=0;
 		for(int ele : indexToCompare)
 		{
 			nodes[i] = ele;
 			i++;
 		}
 		if(indexToCompare.size() < nodes.length)
 			nodes[i] = indexBase;
 		
 		for(col = 0; col<ncols;col++ )
 		{
 			count = 0;
 			for(i=0;i<nodes.length;i++)
 			{
 				if(table1.hasValue(nodes[i], col))
 					count = numValues*count + mapStringValues.get(table1.stringValue(nodes[i], col));
 			}
 			nCounts[count]++;
 		}
 		
 		result = getScoreWithCounts(nodes,nCounts );
 		
 		
 		return  result;
 	}
 	
 	private double getScoreWithCounts(int[] nodes,int[] nCounts )
 	{
 		double result = 0.0;
 		int combinations;
 		int i,j;
 		int numValues =  mapStringValues.size();
 		int numTimes;
 		
 		combinations = (int)Math.pow((double)mapStringValues.size(),(double)(nodes.length-1));
 		for(i=0;i<combinations;i++)
 		{
 			numTimes = 0;
 			for(j=0;j<numValues;j++)
 			{
 				numTimes += nCounts[i*numValues+j];
 			}
 			for(j=0;j<numValues;j++)
 			{
 				if(nCounts[i*numValues+j] > 0)
 				{
					result += (double)nCounts[i*numValues+j]* Math.log(nCounts[i*numValues+j] / numTimes);
 				}
 			}
			
 		}
 		
		return result;
 	}
 	
 
 	
 }

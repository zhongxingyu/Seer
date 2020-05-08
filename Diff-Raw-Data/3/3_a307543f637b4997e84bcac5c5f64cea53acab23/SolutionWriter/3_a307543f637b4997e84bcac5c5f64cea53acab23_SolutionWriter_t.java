 package com.mattdrees.dropboxcalories;
 
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
import com.google.common.collect.Lists;

 
 public class SolutionWriter {
 
 	public void writeSolution(Solution solution, PrintWriter writer)
 	{
 		if (solution == Solution.NO_SOLUTION)
 		{
 			writeNoSolution(writer);
 		}
 		else
 		{
 			writeExistingSolution(solution, writer);
 		}
 		
 	}
 
 	private void writeNoSolution(PrintWriter writer) {
 		writer.println("no solution");
 	}
 	
 
 	private void writeExistingSolution(Solution solution, PrintWriter writer) {
 		List<Item> items = sortItems(solution);
 		
 		for (Item item : items)
 		{
 			writer.println(item.name);
 		}
 	}
 
 	private List<Item> sortItems(Solution solution) {
 		List<Item> items = Lists.newArrayList(solution.items);
 		Collections.sort(items, new Comparator<Item>() {
 			public int compare(Item o1, Item o2) {
 				return o1.name.compareTo(o2.name);
 			}
 		});
 		return items;
 	}
 }

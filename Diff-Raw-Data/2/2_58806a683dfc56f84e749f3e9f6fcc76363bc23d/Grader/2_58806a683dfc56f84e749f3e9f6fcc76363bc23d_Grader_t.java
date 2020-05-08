 package com.evllabs.grader;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 import com.evllabs.grader.structures.Histogram;
 
 public class Grader {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Scanner input = new Scanner(System.in);
 		int counter = 0;
 		Histogram authors = new Histogram();
 		Map<String, List<Histogram>> ranks = new HashMap<String, List<Histogram>>();
 		List<Histogram> currentRanks = new ArrayList<Histogram>();
 		while(input.hasNext()){
 			String line = input.nextLine();
 			if(counter == 0){
 				authors.add(line);
 				currentRanks = ranks.get(line);
 				if(currentRanks == null){
 					currentRanks = new ArrayList<Histogram>();
 					ranks.put(line, currentRanks);
 				}
 			}else if(counter > 5){
 				if(line.isEmpty()){
 					continue;
 				}else if(line.equalsIgnoreCase("------------------------")){
 					counter = 0;
 					continue;
 				}
				int rank = counter - 6;
 				Histogram currentRank;
 				try{
 					currentRank = currentRanks.get(rank);
 				} catch(IndexOutOfBoundsException e){
 					currentRank = new Histogram();
 					currentRanks.add(currentRank);
 				}
 				currentRank.add(line.split(" ")[0]);
 			}
 			counter++;
 		}
 		Set<Entry<String,List<Histogram>>> rankEntries = ranks.entrySet();
 		for(Entry<String, List<Histogram>> entry : rankEntries){
 			List<Histogram> current = entry.getValue();
 			System.out.println(entry.getKey()+" "+authors.get(entry.getKey()));
 			for(int i =0; i<current.size();i++){
 				StringBuilder resultBuilder = new StringBuilder();
 				resultBuilder.append(i+1);
 				Set<Entry<String, Integer>> rankSet = current.get(i).entrySet();
 				for(Entry<String, Integer> rankEntry : rankSet){
 					resultBuilder.append(" ").append(rankEntry.getKey()).append(":").append(rankEntry.getValue());
 				}
 				System.out.println(resultBuilder.toString());
 			}
 			System.out.println();
 		}
 
 	}
 
 }

 package edu.mwdb.project;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Map;
 
 public class Task3a {
 
 	public static void main(String[] args) {
 		try {
 			Task2 task2 = new Task2();
 			ArrayList<Map.Entry<String, Double>>[] authorGroups = getGroupPartitions(task2.getTop3LatSemBySVD_AuthorAuthor());
 			
 			for (int i=0; i<authorGroups.length; i++) {
 				System.out.println("AUTHOR GROUP" + (i+1));
 				for (int j=0; j<authorGroups[i].size(); j++) {
 					System.out.println(authorGroups[i].get(j).getKey() + " : " + authorGroups[i].get(j).getValue());
 				}
 				System.out.println();
 			}
 			
 			ArrayList<Map.Entry<String, Double>>[] coauthorGroups = getGroupPartitions(task2.getTop3LatSemBySVD_CoAuthorCoAuthor());
 			
 			for (int i=0; i<coauthorGroups.length; i++) {
 				System.out.println("COAUTHOR GROUP" + (i+1));
 				for (int j=0; j<coauthorGroups[i].size(); j++) {
 					System.out.println(coauthorGroups[i].get(j).getKey() + " : " + coauthorGroups[i].get(j).getValue());
 				}
 				System.out.println();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Partitions the latent semantics into groups depending on where the weight is the highest in the semantics
 	 * @param latentSemanticsAuthorOrCoauthor - the ones obtained from task2 (author/coauthor)
 	 * @return
 	 */
 	public static ArrayList<Map.Entry<String, Double>>[] getGroupPartitions(Map.Entry<String, Double>[][] latentSemanticsAuthorOrCoauthor) {
 		ArrayList<Map.Entry<String, Double>>[] retVal = new ArrayList[latentSemanticsAuthorOrCoauthor.length];
 		for (int i=0; i<latentSemanticsAuthorOrCoauthor[0].length; i++) {
			double max = Double.NEGATIVE_INFINITY;
 			int maxIndex = 0;
 
 			// Find the max value for that column/key/authorId
 			for (int j=0; j<latentSemanticsAuthorOrCoauthor.length; j++) {
 				if (latentSemanticsAuthorOrCoauthor[j][i].getValue() > max) {
 					max = latentSemanticsAuthorOrCoauthor[j][i].getValue();
 					maxIndex = j;
 				}
 			}
 			
 			// Store the max
 			if (retVal[maxIndex] == null)
 				retVal[maxIndex] = new ArrayList<Map.Entry<String, Double>>();
 			retVal[maxIndex].add(new AbstractMap.SimpleEntry<String,Double>(latentSemanticsAuthorOrCoauthor[0][i].getKey(), max));
 		}
 		
 		return retVal;
 	}
 }

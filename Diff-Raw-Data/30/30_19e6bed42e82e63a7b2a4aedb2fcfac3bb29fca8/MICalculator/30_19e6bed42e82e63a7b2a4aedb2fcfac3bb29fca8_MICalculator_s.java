 package utils.mutualinformation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import seqManipulation.AlignmentSequenceEditor;
 import seqManipulation.GapToolbox;
 import fileformats.fastaIO.Pair;
 
 public class MICalculator {
 	
 	public static Map<Pair<Integer, Integer>, Double> calculateProteinMIMatrix(List<Pair<String,String>> alignment) {
 		
 		GapToolbox gtb = new GapToolbox();
 		AlignmentSequenceEditor ase = new AlignmentSequenceEditor(alignment);
 		
 		
 		// Look for columns with gaps in the alignment
 		boolean[] keepers = gtb.getKeepers(alignment);
 		
 
 		// Remove gaps
 		
 		List<Pair<String,String>> nAlign = gtb.removeGapedColumns(alignment, keepers);
 		
 		List<Character[]> columns = new ArrayList<Character[]>();
 		
 		// Calculate Entropy for each column
 		
 		double[] columnEntropies = new double[nAlign.get(0).getSecond().length()];
 		
 		for (int i=0; i<columnEntropies.length; i++) {
 			
 			// Get the characters for the current column
 			
 			Character[] currentColumn = ase.getColumnAt(i);
 			
 			columns.add(currentColumn);
 			
 			columnEntropies[i] = EntropyCalculator.calculateEntropy(currentColumn, 20, false);
 			
 		}
 		
 		
		Map<Pair<Integer,Integer>,Double> diColumnEntropies = new HashMap<Pair<Integer,Integer>, Double>();
 		
 		
 		// Iterate Entropy for each pair of columns
 		
 		for(int i = 0 ; i < columnEntropies.length-1;i++) {
 			
 			for (int j = i+1 ; j<columnEntropies.length;j++) {
 				
 				
 				try {
 					
					diColumnEntropies.put(new Pair<Integer,Integer>(i,j) , EntropyCalculator.calculateEntropy(columns.get(i), columns.get(j), 2, false));
 					
 				} catch (CharGroupSizeException e) {
 					e.printStackTrace();
 				}
 				
 			}
 			
 		}
 		
 		
		return diColumnEntropies;
 		
 	}
 	
 	
 	public static void main(String[] args) {
 		
 		
 		
 	}
 
 }

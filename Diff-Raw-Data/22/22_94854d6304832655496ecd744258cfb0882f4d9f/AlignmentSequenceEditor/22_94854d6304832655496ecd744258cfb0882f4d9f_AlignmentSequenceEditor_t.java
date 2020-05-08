 package seqManipulation;
 
 import java.util.List;
 
 import fileformats.fastaIO.Pair;
 
 public class AlignmentSequenceEditor {
 	
 	private List<Pair<String,String>> alignment;
 
 	public AlignmentSequenceEditor(List<Pair<String,String>> alignment){
 		
 	this.alignment = alignment;	
 		
 	}
 	
 	public Character[] getColumnAt(int position) {
 		
 		int numberOfRows = this.getColumnsSize();
 		
 		Character[] result = new Character[numberOfRows];
 		
 		for (int i=0; i<numberOfRows; i++) {
 			
 			result[i] = this.getCharAt(i,position);
 			
 		}
 		
 		return result;
 		
 	}
 	
 	
 	
 	/**
 	 * Retrieves the corresponding char from a sequence given the its row and column
 	 * 
 	 * @param row
 	 * @param position
 	 * @return
 	 */
 	private Character getCharAt(int row, int position) {
 		
 		return this.getAlignment().get(row).getSecond().charAt(position);
 		
 	}
 
 	
 	
 
 	/**
 	 * returns the number of sequences in the alignment
 	 * 
 	 */
 	
 	public int getColumnsSize() {
 		
 		return this.alignment.size();
 		
 	}	
 	/**
 	 * returns the length of the sequence of the alignment.
 	 * its assumed that all sequences have the seme length 
 	 * 
 	 * @return
 	 */
 	public int getRowsSize() {
 		
 		if (!this.alignment.isEmpty()) {
 		
			return this.alignment.get(0).getSecond().length();
 		
 		}
 		
 		return 0;
 		
 	}
 	
 	
 	// getters and setters
 	private List<Pair<String,String>> getAlignment() {
 		
 		return this.alignment;
 		
 	}
 
 }

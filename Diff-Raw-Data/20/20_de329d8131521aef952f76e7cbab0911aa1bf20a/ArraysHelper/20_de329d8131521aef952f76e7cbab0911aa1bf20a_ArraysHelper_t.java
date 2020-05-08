 import java.util.ArrayList;
 
 public class ArraysHelper {
	public static ArrayList<Integer> asArrayList(int[] array) {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 
		for (int item : array) {
 			result.add(item);
 		}
 
 		return result;
 	}
 	
	public static ArrayList<int[]> asArrayList(int[][] array) {
		ArrayList<int[]> result = new ArrayList<int[]>();
 
		for (int[] item : array) {
			result.add(new int[item.length]);
 			for (int i = 0; i<item.length; i++) {
 				result.get(result.size()-1)[i] = item[i];
 			}
 		}
 
 		return result;
 	}
 
 	public static ArrayList<Piece> asArrayList(Piece[] array) {
 		ArrayList<Piece> result = new ArrayList<Piece>();
 
 		for (Piece item : array) {
 			result.add(item);
 		}
 
 		return result;
 	}
 
	public static int[][] copyOfRange(int[][] original, int startIndex, int endIndex) {
		int[][] result = new int[endIndex-startIndex][];
 
 		for (int i = 0; i<endIndex-startIndex; i++) {
 			result[i] = original[startIndex+i];
 		}
 		return result;
 	}
 
 	
 }

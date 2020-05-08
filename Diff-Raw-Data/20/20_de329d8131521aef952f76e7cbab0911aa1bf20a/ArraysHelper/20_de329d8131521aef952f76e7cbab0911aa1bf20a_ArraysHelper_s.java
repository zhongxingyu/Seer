 import java.util.ArrayList;
 
 public class ArraysHelper {
	public static ArrayList<Integer> asArrayList(Integer[] array) {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 
		for (Integer item : array) {
 			result.add(item);
 		}
 
 		return result;
 	}
 	
	public static ArrayList<Integer[]> asArrayList(Integer[][] array) {
		ArrayList<Integer[]> result = new ArrayList<Integer[]>();
 
		for (Integer[] item : array) {
			result.add(new Integer[item.length]);
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
 
	public static Integer[][] copyOfRange(Integer[][] original, int startIndex, int endIndex) {
		Integer[][] result = new Integer[endIndex-startIndex][];
 
 		for (int i = 0; i<endIndex-startIndex; i++) {
 			result[i] = original[startIndex+i];
 		}
 		return result;
 	}
 
 	
 }

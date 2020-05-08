 package assign3;
 
 import java.util.Comparator;
 import java.util.Scanner;
 /**
  * 
  * @author Alonzo Rose & Michael Anderson
  * @param <T>
  *
  */
 
 public class AnagramUtil<T> {
 	//Variables
 	public enum 	SortMethod { INSERTION, SELECTION };
	private static 	SortMethod method = SortMethod.INSERTION;
	private Comparator<T> byNaturalSort = new NaturalSort();
 	
 	
 	/**
 	 * This method returns the sorted version of the input string.
 	 * The sorting must be accomplished using either an insertion sort or a selection sort,
 	 * depending on the method state variable. This method should preserve case.
 	 * So the result of calling AnagramUtil.sort("Stephen") should be "Seehnpt".
 	 * @param s
 	 * @return
 	 */
 	public static String sort(String s){
		
 		if(method == SortMethod.INSERTION){
			insertionSort(s.toCharArray(), byNaturalSort);
 		}
 		
 		else{
			selectionSort(s.toCharArray(), byNaturalSort);
 		}
 		//Return sorted String
 		return s;
 	}
 	
 	/**
 	 * This generic method sorts the input array using an insertion sort and the input Comparator object.
 	 * @param arr
 	 * @param c
 	 */
 	public static <T> void insertionSort(T[] arr, Comparator<? super T> c){
 		int j;
 		T focusObject;
 		
 		for(int i = 1; i < arr.length; i ++){
 			focusObject = arr[i];
 			
 			for(j = i - 1; (j >= 0) && (c.compare(arr[i], focusObject) < 0); i --){
 				arr[j + 1] = arr [j];
 			}
 			
 			arr[j+1] = focusObject;
 		}
 	}
 	
 	/**
 	 * This generic method sorts the input array using a selection sort and the input Comparator object.
 	 * @param arr
 	 * @param c
 	 */
 	public static <T> void selectionSort(T[] arr, Comparator<? super T> c){
 		int first;
 		T temp;
 		
 		for(int i = arr.length -1; i > 0; i --){
 			first = 0;
 			
 			for(int j = 1; j <= i; j ++){
 				if(c.compare(arr[j], arr[first]) < 0)
 					first = j;
 			}
 			
 			temp		= arr[first];
 			arr[first]	= arr[i];
 			arr[i]		= temp;
 		}          
 	}
 	
 	/**
 	 * This method returns true if the two input strings (ignoring case) are anagrams of each other, otherwise returns false.
 	 * @param <T>
 	 * @param s1
 	 * @param s2
 	 * @return
 	 */
 	public static boolean areAnagrams(String s1, String s2){
 		
 		if(s1.length() != s2.length())
 			return false;
 		
 		if(s1.compareToIgnoreCase(s2) != 0)
 			return false;
 		else
 			return true;
 	}
 	
 	/**
 	 * This method returns the largest group of anagrams in the input array of words,
 	 * in no particular order. It returns an empty array if there are no anagrams in the input array.
 	 * @param arr
 	 * @return
 	 */
 	public static String[] getLargestAnagramGroup(String[] arr){
 		String[] anagramList = new String[0];
 		for(int i = 1; i < arr.length; i ++){
 			for(int j = 0; j < i; j ++){
 				if(areAnagrams(arr[i], arr[j])){
 					addToArray(arr[i], arr[j], anagramList);
 				}
 			}
 		}
 		
 		return anagramList;
 	}
 	
 	/**
 	 * Behaves the same as the previous method, but reads the list of words from the input filename.
 	 * It is assumed that the file contains one word per line. If the file does not exist or is empty,
 	 * the method returns an empty array because there are no anagrams.
 	 * @param filename
 	 * @return
 	 */
 	public static String[] getLargestAnagramGroup(String filename){
 		//Create scanner from text in file
 		Scanner sc = new Scanner(filename);
 		
 		//Create new String array to return
 		String[] sarr = new String[0];
 		
 		//Copy info from scanner to String array
 		while(sc.hasNext()){
 			addToArray(sc.next(),sarr);
 		}
 		
		
 		//Return the anagram list
 		return getLargestAnagramGroup(sarr);
 	}
 	
 	private static void addToArray(String s1, String s2, String[] arr){
 		int i;
 		//Increase array size by 2
 		String[] tempList = arr.clone();
 		arr = new String[arr.length + 2];
 		
 		//Add in original elements
 		for(i = 0; i < tempList.length; i ++)
 			arr[i] = tempList[i];
 		
 		//Add new anagram pair
 		arr[i + 1] = s1;
 		arr[i + 2] = s2;
 	}
 	
 	private static void addToArray(String s1, String[] arr){
 		int i;
 		//Increase array size by 1
 		String[] tempList = arr.clone();
 		arr = new String[arr.length + 1];
 		
 		//Add in original elements
 		for(i = 0; i < tempList.length; i ++)
 			arr[i] = tempList[i];
 		
 		//Add new anagram pair
 		arr[i + 1] = s1;
 	}
 	
	private class NaturalSort implements Comparator<T>, Comparable<T> {
 
 		@Override
 		public int compare(T o1, T o2) {
 			Comparable<T> element = (Comparable<T>) o1;
 			return element.compareTo(o2);
 		}
 		
 		@Override
 		public int compareTo(T o) {
 			return this.compareTo(o);
 		}
 	}
 }

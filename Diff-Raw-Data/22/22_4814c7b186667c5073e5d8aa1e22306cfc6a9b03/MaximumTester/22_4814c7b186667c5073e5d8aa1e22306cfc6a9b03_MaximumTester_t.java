 package cs151.hw2.ex4_06;
 
 import java.util.*;
 
/**
 * Class to find the String with Maximum length
 * @author Jiajie Wu
 *
 */
 public class MaximumTester implements Comparator<String> {
 	
 	/**
 	 * Get the longest String in a ArrayList
 	 * @param a a String ArrayList
 	 * @return the longest String in the ArrayList
 	 */
 	public static String maximum(ArrayList<String> a, Comparator<String> c) {
 		String longest = "";
 		if (a.size() == 0)
 			return longest;
 		else {
 			Collections.sort(a, c);
 			return a.get(a.size()-1);
 		}
 	}
 	
 	/**
 	 * Compare the length of 2 Strings
 	 * @param s1 a String to compare
 	 * @param s2 another String to compare
 	 * @return -1, 0, or 1 if s1 is shorter, as long as, or longer than s2
 	 */
 	@Override
 	public int compare(String s1, String s2) {
 		if (s1.length() < s2.length())
 			return -1;
 		else if (s1.length() > s2.length())
 			return 1;
 		return 0;
 	}
 	
 	public static void main(String args[]) {
 		ArrayList<String> al = new ArrayList<String>();
 		Comparator<String> c = new MaximumTester();
 		
 		al.add("1");
 		al.add("12345");
 		al.add("123");
 		
 		System.out.println("Strings in the ArrayLists are:");
 		for (String s : al) {
 			System.out.println(s);
 		}
 		System.out.println();
 		
 		System.out.println("The longest String is " + maximum(al, c));
 		
 		
 	}
 }

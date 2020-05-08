 import java.util.ArrayList;
 
 public class StringSearches {
 
 	/**
 	 * Return a table for use with Boyer-Moore.
 	 * 
 	 * map[c] = the length - 1 - last index of c in the needle map[c] = the
 	 * length if c doesn't appear in the needle
 	 * 
 	 * the map should have an entry for every character, 0 to
 	 * Character.MAX_VALUE
 	 */
 	public static int[] buildCharTable(String needle) {
 		int[] map = new int[Character.MAX_VALUE + 1];
 		for (int i = 0; i < needle.length(); i++) {
 			char c = needle.charAt(i);
 			int index = i, temp = 0;
			if (map[(int) c] != 0)
 				continue;
 			while (temp != -1 && needle.indexOf(c) + 1 < needle.length()) {
 				temp = needle.substring(needle.indexOf(c) + 1 + temp,
 						needle.length()).indexOf(c);
 				if (temp != -1) {
 					index += temp + 1;
 					temp = index;
 				}
 			}
			map[(int) c] = (needle.length() - 1 - index > 0) ? needle
 					.length() - 1 - index
 					: 1;
 		}
 
 		for (int i = 0; i < map.length; i++)
 			if (map[i] == 0)
 				map[i] = needle.length();
 		return map;
 	}
 
 	/**
 	 * Run Boyer-Moore on the given strings, looking for needle in haystack.
 	 * Return an array of the indices of the occurrence of the needle in the
 	 * haystack.
 	 * 
 	 * If there are matches that start at index 4, 7, and 9 in the haystack,
 	 * return an array containing only 4, 7, and 9. If there are no matches
 	 * return an empty array, new int[0]
 	 * 
 	 * Running time matters, you will not get full credit if it is not
 	 * implemented correctly
 	 * 
 	 * 
 	 */
 	public static int[] boyerMoore(String needle, String haystack) {
 
 		int map[] = buildCharTable(needle), haystackIndex = 0, needleIndex = needle
 				.length() - 1;
 		ArrayList<Integer> array = new ArrayList<Integer>();
 
 		while (haystackIndex < haystack.length() - (needle.length() - 1))
 			if (needle.charAt(needleIndex) != haystack.charAt(haystackIndex
 					+ needleIndex)) {
				haystackIndex += map[((int) haystack
                        .charAt(haystackIndex + needleIndex))];
 				needleIndex = needle.length() - 1;
 			} else if (needleIndex == 0) {
 				array.add(haystackIndex);
 				needleIndex = needle.length() - 1;
 				haystackIndex++;
 			} else
 				needleIndex--;
 		return intArrayListToArray(array);
 	}
 
 	/**
 	 * Return a table for use with KMP. In this table, table[i] is the length of
 	 * the longest possible prefix that matches a proper suffix in the string
 	 * needle.substring(0, i)
 	 */
 	public static int[] buildTable(String needle) {
 
 		int prefix[] = new int[needle.length()];
 		char array[] = needle.toCharArray();
 		prefix[0] = -1;
 		prefix[1] = 0;
 		int position = 2, currentCandidate = 0;
 		while (position < array.length)
 			if (array[position - 1] == array[currentCandidate]) {
 				currentCandidate++;
 				prefix[position] = currentCandidate;
 				position++;
 			} else if (currentCandidate > 0)
 				currentCandidate = prefix[currentCandidate];
 			else {
 				prefix[position] = 0;
 				position++;
 			}
 
 		return prefix;
 	}
 
 	/**
 	 * Run Knuth-Morris-Pratt on the given strings, looking for needle in
 	 * haystack. Return an array of the indices of the occurrence of the needle
 	 * in the haystack.
 	 * 
 	 * If there are matches that start at index 4, 7, and 9 in the haystack,
 	 * return an array containing only 4, 7, and 9. If there are no matches
 	 * return an empty array, new int[0]
 	 */
 	public static int[] kmp(String needle, String haystack) {
 
 		char text[] = haystack.toCharArray(), word[] = needle.toCharArray();
 		int prefix[] = buildTable(needle);
 		ArrayList<Integer> arrayList = new ArrayList<Integer>();
 
 		int i = 0, j = 0;
 		while (i < text.length)
 			if (text[i] != word[j])
 				if (j != 0) {
 					j = prefix[j];
 				} else
 					i++;
 			else if (j == word.length - 1) {
 				arrayList.add(i - (word.length - 1));
 				j = 0;
 			} else {
 				i++;
 				j++;
 			}
 
 		return intArrayListToArray(arrayList);
 	}
 
 	/**
 	 * @param arrayList
 	 * @return
 	 */
 	private static int[] intArrayListToArray(ArrayList<Integer> arrayList) {
 		int matches[] = new int[arrayList.size()];
 		for (int i = 0; i < arrayList.size(); i++)
			matches[i] = arrayList.get(i);
 		return matches;
 	}
 
 	// This is the base you should use, don't change it
 	public static final int BASE = 1332;
 
 	/**
 	 * Given the hash for a string, return the hash for that string removing
 	 * oldChar from the front and adding newChar to the end.
 	 * 
 	 * Power is BASE raised to the power of the length of the needle
 	 */
 	public static int updateHash(int oldHash, int power, char newChar,
 			char oldChar) {
 		return oldHash * BASE - oldChar * power + newChar;
 	}
 
 	/**
 	 * Hash the given string, using the formula given in the homework
 	 */
 	public static int hash(String s) {
 
 		char array[] = s.toCharArray();
 		int total = 0;
 		for (int i = 0; i < array.length; i++) {
 			total += array[i] * power(BASE, array.length - i - 1);
 		}
 		return total;
 	}
 
 	/**
 	 * Run Rabin-Karp on the given strings, looking for needle in haystack.
 	 * Return an array of the indices of the occurrence of the needle in the
 	 * haystack.
 	 * 
 	 * If there are matches that start at index 4, 7, and 9 in the haystack,
 	 * return an array containing only 4, 7, and 9. If there are no matches
 	 * return an empty array, new int[0]
 	 */
 	public static int[] rabinKarp(String needle, String haystack) {
 
 		ArrayList<Integer> arrayList = new ArrayList<Integer>();
 		int hash = hash(needle);
 		int otherHash = hash(haystack.substring(0, needle.length()));
 		for (int i = 0; i < haystack.length() - (needle.length() - 1); i++) {
 			if (hash == otherHash)
 				if (needle.equals(haystack.substring(i, i + needle.length())))
 					arrayList.add(i);
 			if (i + 1 < haystack.length() - needle.length() + 1)
 				otherHash = updateHash(otherHash,
 						power(BASE, needle.length()),
 						haystack.charAt(i + needle.length()),
 						haystack.charAt(i));
 		}
 
 		return intArrayListToArray(arrayList);
 	}
 
 	public static int power(int base, int exponent) {
 		int temp = base;
 		if (exponent == 0)
 			return 1;
 		for (int i = 1; i < exponent; i++)
 			temp *= base;
 		return temp;
 	}
 }

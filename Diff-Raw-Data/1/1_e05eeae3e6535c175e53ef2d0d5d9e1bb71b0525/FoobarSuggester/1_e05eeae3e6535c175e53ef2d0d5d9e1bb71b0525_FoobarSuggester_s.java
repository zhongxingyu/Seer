 package foobar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * FoobarSuggester provides a static getSuggestions method, which returns
  * suggestions based on a ranking algorithm. The ranking algorithm ranks
  * Fooables by their keyword with the smallest weighted Levenshtein Distance. If
  * there is a tie, then the ranking algorithm reverts to String comparison.
  * 
  * @author Frank Goodman
  * 
  */
 public abstract class FoobarSuggester {
 	/**
 	 * Generate a list of suggested Fooables for 'query' based on the Fooables
 	 * in 'fooables'.
 	 * 
 	 * @param query
 	 *            The search query
 	 * @param fooables
 	 *            The list of Fooables through which to search
 	 * @return A ranked list of suggested Fooables based on the search term
 	 */
 	public static final List<Fooable> getSuggestions(String query,
 			Collection<Fooable> fooables) {
 
 		Map<Fooable, Integer> distances = new HashMap<>();
 
 		int min, dist;
 		for (Fooable fooable : fooables) {
 			min = Integer.MAX_VALUE;
 
 			for (String keyword : fooable.getKeywords()) {
 				dist = FoobarSuggester.weightedLevenshteinDistance(
 						keyword.toLowerCase(), query.toLowerCase());
 				min = dist < min ? dist : min;
 			}
 
 			distances.put(fooable, min);
 		}
 
 		return FoobarSuggester.sortFooables(distances);
 	}
 
 	/**
 	 * Sort a list of Fooables based on Levenshtein Distance, as computed with
 	 * levenshteinDistance(String, String). If there is a tie in Levenshtein
 	 * Distance for any two or more Fooables, rank the Fooables by String
 	 * comparison using the value returned by the getName() method.
 	 * 
 	 * @param fooPairs
 	 *            A list of Fooable Map.Entries, which contain the Fooable and
 	 *            there corresponding minimum Levenshtein Distance for its
 	 *            keywords.
 	 * @return An ordered list of Fooables sorted according to the description
 	 */
 	private static final List<Fooable> sortFooables(
 			Map<Fooable, Integer> fooPairs) {
 		// Create a list of Map.Entries based on the input Map
 		List<Map.Entry<Fooable, Integer>> pairList = new ArrayList<>();
 		pairList.addAll(fooPairs.entrySet());
 
 		// Sort the list of Map.Entries containing Fooables and Levenshtein
 		// Distances
 		Collections.sort(pairList,
 				new Comparator<Map.Entry<Fooable, Integer>>() {
 					@Override
 					public int compare(Entry<Fooable, Integer> o1,
 							Entry<Fooable, Integer> o2) {
 						// Initially sort by Levenshtein Distance
 						if (o1.getValue() < o2.getValue())
 							return -1;
 
 						if (o1.getValue() > o2.getValue())
 							return 1;
 
 						// Fallback sorting on String comparison
 						return o1.getKey().getName()
 								.compareToIgnoreCase(o2.getKey().getName());
 					}
 				});
 
 		// Construct a sorted list of Fooables
 		List<Fooable> sortedList = new ArrayList<>();
 		for (Map.Entry<Fooable, Integer> pair : pairList) {
 			sortedList.add(pair.getKey());
 		}
 
 		return sortedList;
 	}
 
 	/**
 	 * Computes the Levenshtein distance between str1 and str2, while
 	 * additionally weighting distances based on string length and key-to-key
 	 * distances on a QWERTY keyboard.
 	 * 
 	 * @param str1
 	 *            A string to compare with str2
 	 * @param str2
 	 *            A string to compare with str1
 	 * @return The weighted Levenshtein distance of str1 and str2
 	 */
 	private static final int weightedLevenshteinDistance(String str1,
 			String str2) {
 		int[][] distances = new int[str1.length() + 1][str2.length() + 1];
 
 		for (int i = 0; i < str1.length() + 1; i++) {
 			distances[i][0] = i;
 		}
 
 		for (int j = 1; j < str2.length() + 1; j++) {
 			distances[0][j] = j;
 		}
 
 		for (int i = 1; i < str1.length() + 1; i++) {
 			for (int j = 1; j < str2.length() + 1; j++) {
 				distances[i][j] = getKeyDistance(str1.charAt(0), str2.charAt(0))
						/ 3 // magic
 						+ Math.min(
 								distances[i - 1][j - 1]
 										+ (str1.charAt(i - 1) == str2
 												.charAt(j - 1) ? 0 : 1), Math
 										.min(distances[i - 1][j] + 1,
 												distances[i][j - 1] + 1));
 			}
 		}
 
 		return distances[str1.length()][str2.length()];
 	}
 
 	// Dropped in favor of weighted Levenshtein Distance
 	// /**
 	// * Computes the Levenshtein Distances between str1 and str2 using dynamic
 	// * programming to save on computation.
 	// *
 	// * @param str1
 	// * A string to compare with str2
 	// * @param str2
 	// * A string to compare with str1
 	// * @return The Levenshtein Distance between str1 and str2
 	// */
 	// private static final int levenshteinDistance(String str1, String str2) {
 	// int[][] distances = new int[str1.length() + 1][str2.length() + 1];
 	//
 	// for (int i = 0; i < str1.length() + 1; i++) {
 	// distances[i][0] = i;
 	// }
 	//
 	// for (int j = 1; j < str2.length() + 1; j++) {
 	// distances[0][j] = j;
 	// }
 	//
 	// for (int i = 1; i < str1.length() + 1; i++) {
 	// for (int j = 1; j < str2.length() + 1; j++) {
 	// distances[i][j] = Math.min(
 	// distances[i - 1][j - 1]
 	// + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0
 	// : 1), Math.min(distances[i - 1][j] + 1,
 	// distances[i][j - 1] + 1));
 	// }
 	// }
 	//
 	// return distances[str1.length()][str2.length()];
 	// }
 
 	// Recursion is slow as fuck in Java..
 	// /**
 	// * Computes the Levenshtein distance between str1 and str2, while
 	// * additionally weighting distances based on string length and key-to-key
 	// * distances on a QWERTY keyboard.
 	// *
 	// * @param str1
 	// * A string to compare with str2
 	// * @param str2
 	// * A string to compare with str1
 	// * @return The weighted Levenshtein distance of str1 and str2
 	// */
 	// private static int weightedLevenshteinDistance(String str1, String str2)
 	// {
 	// if (str1.length() == 0)
 	// return str2.length();
 	// if (str2.length() == 0)
 	// return str1.length();
 	//
 	// if (str1.charAt(0) == str2.charAt(0)) {
 	// return weightedLevenshteinDistance(str1.substring(1),
 	// str2.substring(1));
 	// }
 	//
 	// int a = weightedLevenshteinDistance(str1.substring(1),
 	// str2.substring(1));
 	// int b = weightedLevenshteinDistance(str1, str2.substring(1));
 	// int c = weightedLevenshteinDistance(str1.substring(1), str2);
 	//
 	// if (a > b)
 	// a = b;
 	// if (a > c)
 	// a = c;
 	//
 	// return a + 1 + Math.min(str1.length(), str2.length())
 	// + getKeyDistance(str1.charAt(0), str2.charAt(0));
 	// }
 
 	private static int getKeyDistance(char chr1, char chr2) {
 		return (int) Math.sqrt(Math.pow(getX(chr2) - getX(chr1), 2)
 				+ Math.pow(getY(chr2) - getY(chr1), 2));
 	}
 
 	/**
 	 * Get the row on a QWERTY keyboard containing chr. The first, 0-th, row is
 	 * the zxc... row. The last, 2-nd, row is the qwe... row. Characters lying
 	 * outside the alphabetic area of the keyboard are assigned to the "10-th"
 	 * row.
 	 * 
 	 * @param chr
 	 *            A character
 	 * @return The keyboard row containing chr
 	 */
 	private static int getY(char chr) {
 		switch (chr) {
 		case 'z':
 		case 'x':
 		case 'c':
 		case 'v':
 		case 'b':
 		case 'n':
 		case 'm':
 		case ',':
 		case '.':
 		case '/':
 			return 0;
 		case 'a':
 		case 's':
 		case 'd':
 		case 'f':
 		case 'g':
 		case 'h':
 		case 'j':
 		case 'k':
 		case 'l':
 			return 1;
 		case 'q':
 		case 'w':
 		case 'e':
 		case 'r':
 		case 't':
 		case 'y':
 		case 'u':
 		case 'i':
 		case 'o':
 		case 'p':
 			return 2;
 		case '1':
 		case '2':
 		case '3':
 		case '4':
 		case '5':
 		case '6':
 		case '7':
 		case '8':
 		case '9':
 		case '0':
 		case '-':
 		case '=':
 		case '~':
 		case '!':
 		case '#':
 		case '_':
 		case '+':
 			return 3;
 		default:
 			return 4;
 		}
 	}
 
 	/**
 	 * Get the column on a QWERTY keyboard containing chr. The first, 0-th,
 	 * column is the qaz column. The last, 9-th, column is the p column.
 	 * Characters lying outside the alphabetic area of the keyboard are assigned
 	 * to the "10-th" column.
 	 * 
 	 * @param chr
 	 *            A character
 	 * @return The keyboard column containing chr
 	 */
 	private static int getX(char chr) {
 		switch (chr) {
 		case '1':
 		case '!':
 		case '~':
 		case '`':
 		case 'q':
 		case 'a':
 		case 'z':
 			return 0;
 		case '2':
 		case 'w':
 		case 's':
 		case 'x':
 			return 1;
 		case '#':
 		case '3':
 		case 'e':
 		case 'd':
 		case 'c':
 			return 3;
 		case '4':
 		case 'r':
 		case 'f':
 		case 'v':
 			return 4;
 		case '5':
 		case 't':
 		case 'g':
 		case 'b':
 			return 5;
 		case '6':
 		case 'y':
 		case 'h':
 		case 'n':
 			return 6;
 		case '7':
 		case 'u':
 		case 'j':
 		case 'm':
 			return 7;
 		case '8':
 		case 'i':
 		case 'k':
 		case ',':
 			return 8;
 		case '9':
 		case 'o':
 		case 'l':
 		case '.':
 			return 9;
 		case '-':
 		case '_':
 		case '+':
 		case '=':
 		case 'p':
 		case '/':
 			return 10;
 		default:
 			return 11;
 		}
 	}
 }

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
  * Fooables by their keyword with the smallest Levenshtein Distance. If there is
  * a tie, then the ranking algorithm reverts to String comparison.
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
 				dist = FoobarSuggester.levenshteinDistance(keyword, query);
 				min = dist < min ? dist : min;
 			}
 
 			distances.put(fooable, min);
 		}
 
		System.out.println(distances);

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
 	 * Computes the Levenshtein Distances between str1 and str2 using dynamic
 	 * programming to save on computation.
 	 * 
 	 * @param str1
 	 *            A string to compare with str2
 	 * @param str2
 	 *            A string to compare with str1
 	 * @return The Levenshtein Distance between str1 and str2
 	 */
 	private static final int levenshteinDistance(String str1, String str2) {
 		int[][] distances = new int[str1.length() + 1][str2.length() + 1];
 
 		for (int i = 0; i < str1.length() + 1; i++) {
 			distances[i][0] = i;
 		}
 
 		for (int j = 1; j < str2.length() + 1; j++) {
 			distances[0][j] = j;
 		}
 
 		for (int i = 1; i < str1.length() + 1; i++) {
 			for (int j = 1; j < str2.length() + 1; j++) {
 				distances[i][j] = Math.min(
 						distances[i - 1][j - 1]
 								+ (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0
 										: 1), Math.min(distances[i - 1][j] + 1,
 								distances[i][j - 1] + 1));
 			}
 		}
 
		System.out.println(str1 + ", " + str2 + " = "
				+ distances[str1.length()][str2.length()]);
 		return distances[str1.length()][str2.length()];
 	}
 }

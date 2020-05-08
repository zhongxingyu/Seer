 package org.obda.sql.benchmark;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class WisconsinBenchmarkGenerator {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new WisconsinBenchmarkGenerator().generate(1000);
 	}
 
 	public void generate(int maxTuples) {
 		List<Integer> unique1List = new ArrayList<Integer>(maxTuples);
 
 		for (int i = 0; i < maxTuples; i++) {
 			unique1List.add(i);
 		}
 
 		Collections.shuffle(unique1List);
 
 		String[] string4s = //
 		{ 		"AAAAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", //
 				"HHHHxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", //
 				"OOOOxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", //
 				"VVVVxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" //
 		};
 
 		for (int i = 0; i < maxTuples; i++) {
 			int unique1 = unique1List.get(i);
 			int unique2 = i;
 			int two = unique1 % 2;
 			int four = unique1 % 4;
 			int ten = unique1 % 10;
 			int twenty = unique1 % 20;
 			int onePercent = unique1 % 100;
 			int tenPercent = unique1 % 10;
 			int twentyPercent = unique1 % 5;
 			int fiftyPercent = unique1 % 2;
 			int unique3 = unique1;
 			int evenOnePercent = onePercent * 2;
 			int oddOnePercent = onePercent * 2 + 1;
 			String stringu1 = convert(unique1);
 			String stringu2 = convert(unique2);
 			String string4 = string4s[i % 4];
 
 			System.out.format(//
					"%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,\"%s\",\"%s\",\"%s\"\n", //
 							unique1, unique2, two, four, ten, twenty,
 							onePercent, tenPercent, twentyPercent,
 							fiftyPercent, unique3, evenOnePercent,
 							oddOnePercent, stringu1, stringu2, string4);
 
 		}
 
 	}
 
 	String convert(int unique) {
 
 		char[] tmp = new char[7];
 		char[] result = new char[7];
 
 		int i, j, rem, cnt;
 
 		/* first set result string to "AAAAAAA" */
 		for (i = 0; i < 7; i++)
 			result[i] = 'A';
 		i = 6;
 		cnt = 0;
 		/*
 		 * convert unique value from right to left into an alphabetic string in
 		 * tmp
 		 */
 		/* tmp digits are right justified in tmp */
 		while (unique > 0) {
 			rem = unique % 26; /* '%' is the mod operator in C */
 			tmp[i] = (char) ('A' + rem);
 			unique = unique / 26;
 			i--;
 			cnt++;
 		}
 		/* finally move tmp into result, left justifying it */
 		for (j = 0; j <= cnt; j++, i++)
 			result[j] = tmp[i];
 
 		return new String(result);
 
 	}
 }

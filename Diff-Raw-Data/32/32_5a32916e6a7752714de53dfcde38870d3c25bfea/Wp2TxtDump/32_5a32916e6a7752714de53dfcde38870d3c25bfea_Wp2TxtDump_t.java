 package org.jovislab.tools.hadoop.hadoopgrams.filters;
 
 import org.jovislab.tools.hadoop.hadoopgrams.Filter;
 
 /* This filter is to process Wikipedia dumps preprocessed with WP2TXT software */
 /* Not just raw dumps in XML format */
 public class Wp2TxtDump implements Filter {
 	public String filter(String input) {
 		if (input.startsWith("[[")) {
 			// Remove title
			if (input.length() - 4 < 1)
				return "";
 			return input.substring(2, input.length() - 2);
 		}
 		if (input.startsWith("= ")) {
 			// Remove header
			if (input.length() - 4 < 1)
				return "";
 			return input.substring(2, input.length() - 2);
 		}
 		if (input.startsWith("== ")) {
 			// Remove header
			if (input.length() - 6 < 1)
				return "";
 			return input.substring(3, input.length() - 3);
 		}
 		if (input.startsWith("=== ")) {
 			// Remove header
			if (input.length() - 8 < 1)
				return "";
 			return input.substring(4, input.length() - 4);
 		}
 		if (input.startsWith("==== ")) {
 			// Remove header
			if (input.length() - 10 < 1)
				return "";
 			return input.substring(5, input.length() - 5);
 		}
 		if (input.startsWith("===== ")) {
 			// Remove header
			if (input.length() - 12 < 1)
				return "";
 			return input.substring(6, input.length() - 6);
 		}
 		if (input.startsWith("====== ")) {
 			// Remove header
			if (input.length() - 14 < 1)
				return "";
 			return input.substring(7, input.length() - 7);
 		}
 		if (input.startsWith("======= ")) {
 			// Remove header
			if (input.length() - 16 < 1)
				return "";
 			return input.substring(8, input.length() - 8);
 		}
 		if (input.startsWith("======== ")) {
 			// Remove header
			if (input.length() - 18 < 1)
				return "";
 			return input.substring(9, input.length() - 9);
 		}
 		if (input.startsWith("- ")) {
 			// Remove list indicator
			if (input.length() - 2 < 1)
				return "";
 			return input.substring(2, input.length());
 		}
 		if (input.startsWith("* ")) {
 			// Remove list indicator
			if (input.length() - 2 < 1)
				return "";
 			return input.substring(2, input.length());
 		}
 		return input;
 	}
 }

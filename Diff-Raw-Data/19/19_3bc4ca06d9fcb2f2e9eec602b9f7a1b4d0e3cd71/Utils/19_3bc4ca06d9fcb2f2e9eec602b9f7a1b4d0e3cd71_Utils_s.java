 package ru.inventos.yum;
 
 public class Utils {
 	
 	public static String getFormatText(String str, int charsInStr, boolean needTabs) {
 		int len = str.length();
 		int[] ind = new int[len];
 		int chars = 0;
 		int qty = 0;
 		for(int i = 0; i < len; i++) {
 			chars++;
 			if ((str.charAt(i) == ' ') && 
 					(str.indexOf(' ', i + 1) - i + chars - 1 > charsInStr)) {
 				ind[qty] = i;
 				chars = 0; 
 				qty++; 
 			}
 			else if ((str.charAt(i) == '(') && 
 					(str.indexOf(')', i + 1) - i + chars > charsInStr)) {
 				ind[qty] = i - 1;
 				chars = 0;
 				qty++;
 				
 			}
 			 
 		}
 		String res = ""; 
 		int start = 0;
 		for (int i = 0; i < qty; i++) {
 			res = res + str.substring(start, ind[i]) +"\n";
 			if (needTabs) {
 				res = res + "    ";
 			}
 			start = ind[i] + 1;
 		}
 		res = res + str.substring(start, len);
 		return res;		
 	}
 	
 	public static String parseDate(String str) {
 		return str.substring(0, 10);
 	}
 	
 	public static String parseTime(String str) {
 		return str.substring(11, 19);
 	}
 
 }

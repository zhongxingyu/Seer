 package util;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class RLE {
 	
 	static public int[][] read(String file) {
 		Scanner in;
 		try {
 			in = new Scanner(new File(file));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		String line = in.nextLine();
 		for(; line.charAt(0) == '#'; line = in.nextLine());
 		
 		Pattern pattern = Pattern.compile("^x = ([0-9]+), y = ([0-9]+), rule = (.+)$");
 		Matcher matcher = pattern.matcher(line);
 		
 		if(!matcher.matches()) {
 			return null;
 		}
 		
 		int n = Integer.parseInt(matcher.group(1));
 		int m = Integer.parseInt(matcher.group(2));
 		String rule = matcher.group(3);
 		
 		int[][] t = new int[m][n];
 		int buffer = 0;
 		int x=0, y=0;
 		
 		do {
 			line = in.nextLine();
 			for(int j=0; j<line.length(); j++) {
 				char c = line.charAt(j);
 				if(Character.isDigit(c)) {
 					buffer = 10*buffer + (int) (c - '0');
 				}
 				else if(c == 'o') {
 					Arrays.fill(t[y], x, Math.min(n, x+Math.max(1, buffer)), 1);
					x += buffer;
 					buffer = 0;
 				}
 				else if(c == 'b') {
 					x += Math.max(1, buffer);
 					buffer = 0;
 				}
 				else if(c == '$') {
 					y += Math.max(1, buffer);
 					x = 0;
 					buffer = 0;
 				}
 				else if(c == '!')
 					return t;
 			}
 		} while(in.hasNext());
 		
 		return null;
 		
 	}
 	
 }

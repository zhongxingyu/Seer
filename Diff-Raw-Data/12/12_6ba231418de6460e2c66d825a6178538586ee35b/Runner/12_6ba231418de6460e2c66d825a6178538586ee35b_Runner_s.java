 package org.araqne.regex.tester;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Runner {
 	public static void main(String[] args) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 		System.out.print("line? ");
 		String line = reader.readLine();
 
 		System.out.println("press ctrl-c to break");
 		while (true) {
 			try {
 				System.out.print("regex? ");
 				String regex = reader.readLine();
 				Pattern p = Pattern.compile(regex);
 				Matcher m = p.matcher(line);
 				if (m.find()) {
					for (int i = 0; i < m.groupCount(); i++)
 						System.out.println("#" + i + ": " + m.group(i));
 				} else {
 					System.out.println("no pattern match");
 				}
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 	}
 }

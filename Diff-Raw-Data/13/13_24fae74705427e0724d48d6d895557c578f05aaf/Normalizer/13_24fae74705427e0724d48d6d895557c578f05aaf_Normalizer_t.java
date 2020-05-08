 package uk.ac.ucl.cs.clonedetector;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Normalizer {
 	String lang;
 	String keywords = "";
 	
 	public Normalizer(String lang) {
 		this.lang = lang;
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new FileReader("lang/" + lang + ".keywords"));
 			String line;
 			while ((line = in.readLine()) != null) {
 				keywords += line + "|";
 			}
 			keywords = keywords.substring(0, keywords.length()-1);
 		} catch (IOException e) {
 			// Language does not exist, do nothing.
 		}
 	}
 	
 	public String normalise(String line) {
 		if (!lang.equals("")) {
			String regexp = "((?!(" + keywords + ")\\b)\\b[A-Za-z][A-Za-z0-9]*)";
 			line = line.replaceAll(regexp, "%VAR%");	// normalise variables
 		}
 		line = line.replaceAll("\\s*", ""); 		// strip whitespace
 		return line;
 	}
 	
 }

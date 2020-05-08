 package ch22.ex11;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class CSVManager {
 	
 	public static List<String[]> readCSVTable(Readable source, int commaCount) throws IOException{
 		List<String[]> vals = new ArrayList<String[]>();
 		BufferedReader bufferedreader = new BufferedReader((Reader) source);
 		String line;
 		while ((line = bufferedreader.readLine()) != null) {
 			vals.add(splitLineWithComma(line, commaCount));
 		}
 		return vals;
 	}
 	
 	private static String[] splitLineWithComma(String line, int commaCount) throws IOException{  
 	    if (line == null) {  
 	        throw new IOException("invalid input form");
 	    }  
 	  
 	    List<String> vals = new ArrayList<String>();  
 	    
 	    StringTokenizer tokenizer = new StringTokenizer(line, ",");  
 	    int count = 0;
 	    while (tokenizer.hasMoreTokens()) { 
 	    	if (++count > commaCount) 
 	    		throw new IOException("invalid input form");
 	        vals.add(tokenizer.nextToken());
 	    }  
 	  
 	    return (String[]) vals.toArray(new String[vals.size()]);  
 	}  
 }
 
 

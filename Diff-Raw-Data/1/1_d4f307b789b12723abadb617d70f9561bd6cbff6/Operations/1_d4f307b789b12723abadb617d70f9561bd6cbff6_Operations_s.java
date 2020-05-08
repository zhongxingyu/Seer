 package Source;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Operations {
 	
 	public static boolean replace(String regex,String ascii_str,String infile,String outfile){
 		File in = new File(infile);
 		File out = new File(outfile);
 		FileInputStream inFile;
 		FileOutputStream outFile;
 		try {
 			inFile = new FileInputStream(in);
 			outFile = new FileOutputStream(out);
 		} catch (FileNotFoundException e) {
 			System.out.println("Couldn't find files "+infile +" &/or "+outfile);
 			return false;
 		}
 		StringBuffer inputFileText = new StringBuffer();
 		char c;
 		try {
 			c = (char) inFile.read();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 
 		while(c != (char)65535){
 			inputFileText.append(c);	
 			try {
 				c = (char) inFile.read();
 			} catch (IOException e) {
 				System.out.println("Failure while reading inFile "+infile);
 				return false;
 			}
 		}
 				
 		Pattern regexPattern = Pattern.compile(regex);
 	    Matcher matcher = regexPattern.matcher(inputFileText.toString());
 	    String outFileText = matcher.replaceAll(ascii_str);	
 		for(int i=0;i<outFileText.length();i++){
 			try {
 				outFile.write(outFileText.charAt(i));
 			} catch (IOException e) {
 				System.out.println("Failure while writing outFile "+outfile);
 				return false;			
 			}
 		}		
 		try {
 			inFile.close();
 			outFile.close();
 		} catch (IOException e) {
 			System.out.println("Failure while closing files");
 			return false;		
 		}
 		return true;
 	}
 	
 	public static String[] find(String regex, String infile){
 		File in = new File(infile);
 		FileInputStream inFile;
 		try {
 			inFile = new FileInputStream(in);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 		StringBuffer inputFileText = new StringBuffer();
 		char c;
 		try {
 			c = (char) inFile.read();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return null;
 		}
 
 		while(c != (char)65535){
 			inputFileText.append(c);	
 			try {
 				c = (char) inFile.read();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		Pattern regexPattern = Pattern.compile(regex);
 	    Matcher matcher = regexPattern.matcher(inputFileText.toString());
 	    ArrayList<String> a = new ArrayList<String>();
 	    while(matcher.find()){
 	    	a.add(matcher.group());
 	    }
 		String[] results = new String[a.size()];
 		return   a.toArray(results);
 	}
 	
 	public static String[] intersec(String[] a, String[] b){
 
 		HashSet<String> hs = new HashSet<String>();
 		for(int i=0;i<a.length;i++){
 			hs.add(a[i]);
 		}
 		HashSet<String> rs = new HashSet<String>();
 		for(int i=0;i<b.length;i++){
 			if(hs.contains(b[i])){
 				rs.add(b[i]);
 			}
 		}
 		
 		String[] results = new String[rs.size()];
 		return rs.toArray(results);
 	}
 	
 	public static int hash(String[] a){
 		return a.length;
 	}
 	public static void print(int a){
 		System.out.println(a);
 	}
 	
 	//This needs to be tested:
 	public static String maxfreqstring(String[] a){
 		HashMap<String,Integer> hm = new HashMap<String,Integer>();
 		String maxString = null;
 		int maxNum=0;
 		for(int i=0;i<a.length;i++){
 			if(hm.containsKey(a[i])){
 				int prevNum = hm.get(a[i]);
 				prevNum++;
 				if(prevNum>maxNum){
 					maxNum = prevNum;
 					maxString = a[i];
 				}
 				hm.put(a[i],prevNum);
 			}
 			else{
 				hm.put(a[i], 1);
 			}
 		}		
 		return maxString;
 	}
 	//This needs to be tested
 	public static String[] union(String[] a, String[] b){
 
 		HashSet<String> hs = new HashSet<String>();
 		for(int i=0;i<a.length;i++){
 			hs.add(a[i]);
 		}
 		for(int i=0;i<b.length;i++){
 				hs.add(b[i]);
 			
 		}
 		
 		String[] results = new String[hs.size()];
 		return hs.toArray(results);
 	}
 	//This needs to be tested
 	public static String[] diff(String[] a, String[] b){
 
 		HashSet<String> hs = new HashSet<String>();
 		for(int i=0;i<a.length;i++){
 			hs.add(a[i]);
 		}
 		
 		for(int i=0;i<b.length;i++){
 				if(hs.contains(b[i])){
 					hs.remove(b[i]);
 				}
 			
 		}
 		
 		String[] results = new String[hs.size()];
 		return hs.toArray(results);
 	}
 
 }

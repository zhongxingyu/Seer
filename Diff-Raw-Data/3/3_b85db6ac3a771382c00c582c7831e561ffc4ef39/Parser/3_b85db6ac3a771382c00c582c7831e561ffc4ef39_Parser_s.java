 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import java.util.HashMap;
 
 public class Parser {
 
 	public static void main(String[] args) {
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("foo"));
 
 			String line;
 			String[] node;
 			StringBuilder s = new StringBuilder();
 			String current = "";
 
 			while ((line = in.readLine()) != null) {
 				node = line.split("\t");
 				if (s.length() == 0) {
 				    current = node[0];
 				    s.append("["+current+",0,[");
 				}
 				if (node[0].equals(current)) {
 					s.append("["+node[1]+",1],");
 				} else {
 					System.out.println(s.deleteCharAt(s.length()-1).toString()+"]]");
 					s = new StringBuilder();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }

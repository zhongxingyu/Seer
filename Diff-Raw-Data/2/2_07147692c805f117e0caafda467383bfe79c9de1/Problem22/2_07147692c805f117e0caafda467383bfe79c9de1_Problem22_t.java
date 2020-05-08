 package Problem21to30;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 
 public class Problem22 {
 	private String[] list;
 	
 	public Problem22(String[] list) {
 		this.list = list;
		System.out.println(solve());
 	}
 	
 	
 	private long solve() {
 		Arrays.sort(list);
 		
 		String line;
 		long sum = 0;
 		
 		for (int i = 0; i < list.length; i++) {
 			line = list[i];
 			for (int j = 0; j < line.length(); j++) {
 				sum += (line.charAt(i) - 64) * (i +1);
 			}
 		}
 		
 		return sum;
 	}
 
 
 	public static void main(String[] args) {
 		BufferedReader br = null;
 		String line = "";
 		
 		try {
 			br = new BufferedReader(new FileReader(new File("names.txt")));
 			line = br.readLine();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		line = line.replaceAll("\"", "");
 		String[] split = line.split(",");
 		new Problem22(split);
 		
 		
 	}
 
 }

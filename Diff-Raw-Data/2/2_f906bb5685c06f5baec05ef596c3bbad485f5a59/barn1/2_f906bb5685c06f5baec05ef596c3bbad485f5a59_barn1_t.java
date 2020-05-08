 import java.io.*;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.StringTokenizer;
 
 
 
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: barn1
 */
 
 public class barn1 {
 	
 	public static int calculateMinLength(int[] occupied, int m, int c){
 		if (m >= c){
 			return c;
 		}
 		Integer[] diff = new Integer[c-1];
 		for (int i = 0; i < c-1; i++) {
 			diff[i] = occupied[i+1] - occupied[i]-1;
 		}
 		Arrays.sort(diff, Collections.reverseOrder());
 			
 		int length = 0;
 		for (int j = 0; j < m-1; j++) {
 			length += diff[j];
 		}
 		
 		return occupied[c-1] - occupied[0] + 1 - length;
 	}
 
 	public static void main(String[] args) throws IOException {
 		// TODO Auto-generated method stub
 		BufferedReader br = new BufferedReader(new FileReader("barn1.in"));
 		StringTokenizer st = new StringTokenizer(br.readLine());
 		int m = Integer.parseInt(st.nextToken());
		st.nextToken();
 		int c = Integer.parseInt(st.nextToken());
 		
 		int[] occupied = new int[c];
 		for (int i = 0; i<c; i++) {
 			occupied[i] = Integer.parseInt(br.readLine());
 		}
 		Arrays.sort(occupied);
 		int minLength = calculateMinLength(occupied, m, c);
 		
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("barn1.out")));
 		out.println(minLength);
 		out.close();
 		br.close();
 		System.exit(0);
 	}
 
 }

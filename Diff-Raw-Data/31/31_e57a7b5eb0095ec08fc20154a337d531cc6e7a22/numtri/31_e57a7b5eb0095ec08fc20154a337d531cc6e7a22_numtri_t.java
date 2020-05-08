 import java.io.*;
 import java.util.StringTokenizer;
 
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: numtri
 */
 public class numtri {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException{
 		// TODO Auto-generated method stub
 		BufferedReader br = new BufferedReader(new FileReader("numtri.in"));
 		int n = Integer.parseInt(br.readLine());
 		int[] prevLine = null;
 		int[] curLine = new int[n+2];
 		for(int i = 1; i<=n; i++){
 			prevLine = curLine;
 			curLine = new int[n+2];
 			StringTokenizer st = new StringTokenizer(br.readLine());
 			for(int j = 1; j<=i; j++){
 				curLine[j] = Integer.parseInt(st.nextToken()) + Math.max(prevLine[j], prevLine[j-1]);
 			}
 		}
 		
 		int maxVal = 0;
 		for(int i = 0; i<n+2; i++){
 			if(curLine[i]>maxVal){
 				maxVal = curLine[i];
 			}
 		}
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("numtri.out")));
 		out.println(maxVal);
 		out.close();
		br.close();
 		System.exit(0);
 	}
 
 }

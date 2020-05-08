 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: pprime
 */
 public class pprime {
 
 	private static ArrayList<Integer> pprime;
 	private static int a;
 	private static int b;
 	private static int maxLen;
 	
 	public static boolean isPrime(int val){
 		if(val%2==0){
 			return false;
 		}
 		int limit = (int)(Math.sqrt(val));
 		for(int i = 3; i<=limit; i+=2){
 			if(val%i==0){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	
 	public static void dfs(StringBuffer valBuf, int length){
 		if(length>maxLen){
 			return;
 		}
 		int val = Integer.parseInt(valBuf.toString());		
 		if(val > b){
 			return;
 		}
 		if(val >=a && valBuf.charAt(0)!=0 && isPrime(val)){
 			pprime.add(val);
 		}
 		for(int i = 0; i<=9; i++){
 			StringBuffer newSB = new StringBuffer().append(i).append(valBuf).append(i);
 			dfs(newSB, length+2);
 		}
 	}
 	
 	public static List<Integer> getPPrime(int aIn, int bIn){
 		a = aIn;
 		b = bIn;
 		maxLen = Integer.toString(b).length();
 		pprime = new ArrayList<Integer>();
 		for(int i = 0; i<=9; i++){
 			dfs(new StringBuffer().append(i), 1);
 			dfs(new StringBuffer().append(i).append(i), 2);
 		}
 		Collections.sort(pprime);
 		return pprime;
 	}
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException{
 		// TODO Auto-generated method stub
 		BufferedReader br = new BufferedReader(new FileReader("pprime.in"));
 		StringTokenizer st = new StringTokenizer(br.readLine());
 		int a = Integer.parseInt(st.nextToken());
 		int b = Integer.parseInt(st.nextToken());
 		List<Integer> pprime = getPPrime(a, b);
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("pprime.out")));
 		for(Integer i:pprime){
 			out.println(i.intValue());
 		}
 		out.close();
 		System.exit(0);
 	}
 
 }

 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: sprime
 */
 
 public class sprime {
 	private static ArrayList<Integer> sprimes;
 	private static int n;
 	public static boolean isPrime(int val){
 		if(val==2){
 			return true;
 		}
 		if(val%2==0){
 			return false;
 		}
 		for(int i = 3; i*i<=val; i+=2){
 			if(val%i==0){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private static void dfs(int val, int len){
 		if(!isPrime(val)){
 			return;
 		}
 		if(len==n){
 			if(isPrime(val)){
 				sprimes.add(val);
 				return;
 			}
 		}	
 		for(int i = 1; i<=9; i+=2){
 			dfs(val*10+i, len+1);
 		}
 	}
 	
 	
 	public static List<Integer> getSPrime(int length){
 		sprimes = new ArrayList<Integer>();
 		n = length;
 		dfs(2, 1);
 		dfs(3, 1);
 		dfs(5, 1);
 		dfs(7, 1);
 		
 		return sprimes;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException{
 		// TODO Auto-generated method stub
 		BufferedReader br = new BufferedReader(new FileReader("sprime.in"));
 		int n = Integer.parseInt(br.readLine());
 		List<Integer> sprimes = getSPrime(n);
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("sprime.out")));
 		for(Integer i:sprimes){
 			out.println(i);
 		}
 		out.close();
		br.close();
 		System.exit(0);
 	}
 
 }

 import java.io.*;
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: checker
 */
 public class checker {
 	private static final int maxCount = 3;
 	private static int[][] first3Sols;
 	private static int max;	
 	private static int[] row;
 	private static int count = 0;
 	private static boolean[] colUsed;
 	private static boolean[] diagUsed;
 	private static boolean[] offDiagUsed;
 	
 	private static void addQueen(int i, int j){
 		colUsed[j] = true;
 		diagUsed[i+j] = true;
 		offDiagUsed[i-j+max] = true;
 	}
 	
 	private static void removeQueen(int i, int j){
 		colUsed[j] = false;
 		diagUsed[i+j] = false;
 		offDiagUsed[i-j+max] = false;
 	}
 	
 	
 	private static void findSolutions(int i){
 		if(i==max){
 			if(count<maxCount){
 				System.arraycopy(row, 0, first3Sols[count], 0, max);
 			}
 			count++;
 		}else{
 			for(int j = 0; j<max; j++){
 				if(!(colUsed[j] || diagUsed[i+j] || offDiagUsed[i-j+max])){
 					row[i] = j;
 					addQueen(i, j);
 					findSolutions(i+1);
 					removeQueen(i, j);
 				}
 			}
 		}
 	}
 	
 	private static void findSolGreaterThan6(){
 		for(int j = 0; j<max/2; j++){
 			row[0] = j;
 			addQueen(0, j);
 			findSolutions(1);
 			removeQueen(0, j);
 		}
 		count*=2;
 		if(max%2!=0){
 			row[0] = max/2;
 			addQueen(0, max/2);
 			findSolutions(1);
 			removeQueen(0,max/2);
 		}
 	}
 	
 	
 	public static int[][] getSolutions(int n){
 		max = n;
 		row = new int[max];
 		colUsed = new boolean[max];
 		diagUsed = new boolean[max*2];
 		offDiagUsed = new boolean[max*2];
 		first3Sols = new int[3][max];
 		
 		if(max==6){
 			findSolutions(0);
 		}else{
 			findSolGreaterThan6();
 		}
 		return first3Sols;
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException{
 		// TODO Auto-generated method stub
 		BufferedReader br = new BufferedReader(new FileReader("checker.in"));
 		int n = Integer.parseInt(br.readLine());		
 		int[][] first3Sols = getSolutions(n);
 		
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("checker.out")));
 		for(int i = 0; i<3; i++){
 			StringBuffer sb = new StringBuffer();
 			for(int j = 0; j<n; j++){
 				sb.append(first3Sols[i][j]+1).append(" ");
 			}
 			out.println(sb.deleteCharAt(sb.length()-1).toString());
 		}
 		out.println(count);
 		out.close();
 		System.exit(0);
 	}
 
 }

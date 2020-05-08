 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 /*
 ID: kennyk61
 LANG: JAVA
 PROG: ariprog
 */
 public class ariprog {
 
 	class Sequence implements Comparable<Sequence>{
 		int first;
 		int diff;
 		Sequence(int first, int diff){
 			this.first = first;
 			this.diff = diff;
 		}
 		@Override
 		public int compareTo(Sequence other) {
 			// TODO Auto-generated method stub
 			if(this.diff!=other.diff){
 				return this.diff - other.diff;
 			}
 			return this.first - other.first;
 		}
 	}
 	
 	boolean[] isBiSquare;
 	int mm2;
 	public ariprog(int m){
 		mm2 = m*m*2;
 		isBiSquare = new boolean[mm2+1];
 		for(int i = 0; i<=m; i++){
 			for(int j = 0; j<=m; j++){
 				isBiSquare[i*i+j*j] = true;
 			}
 		}
 	}
 
 	public Sequence[] getSequences(int n){
 		ArrayList<Sequence> sequence = new ArrayList<Sequence>();
 	
 		for(int a = 0; a<mm2; a++){
 			if(isBiSquare[a]){
 				label:
 				for(int b = 1; b<=(mm2-a)/(n-1); b++){
 					for(int i = 1; i<n; i++){
 						if(!isBiSquare[a+b*i]){
 							continue label;
 						}
 					}
 					sequence.add(new Sequence(a, b));
 				}
 			}
 		}
 		Collections.sort(sequence);
 		return sequence.toArray(new Sequence[sequence.size()]);
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException{
 		// TODO Auto-generated method stub
 
 		BufferedReader br = new BufferedReader(new FileReader("ariprog.in"));
 		int n = Integer.parseInt(br.readLine());
 		int m = Integer.parseInt(br.readLine());
 		Sequence[] sequences = new ariprog(m).getSequences(n);
 		
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("ariprog.out")));
 		if(sequences.length==0){
 			out.println("NONE");
 		}else{
 			for(int i = 0; i<sequences.length; i++){
 				out.println(sequences[i].first + " " + sequences[i].diff);
 			}
 		}
 		out.close();
 		System.exit(0);
 		
 	}
 
 }

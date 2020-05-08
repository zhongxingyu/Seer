 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 
 public class Main {
 
 	public Main(BufferedReader in) throws Exception {
 		
 		int[] created = new int[12];
 		
 		for (int x = 1;; x++) {
 			int S = Integer.parseInt(in.readLine());
 			if (S < 0)
 				break;
 			
 			System.out.format("Case %d:\n", x);
 			
 			StringTokenizer st = new StringTokenizer(in.readLine());
 			for (int i = 0; i < 12; i++)
 				created[i] = Integer.parseInt(st.nextToken());
 			
 			st = new StringTokenizer(in.readLine());
 			for (int i = 0; i < 12; i++) {
 				
 				int needed = Integer.parseInt(st.nextToken());
 				if (S - needed >= 0) {
 					S -= needed;
 					System.out.println("No problem! :D");
 				}
 				else
 					System.out.println("No problem. :(");
 				
 				S += created[i];
 			}
 		}
 	}
 	public static void main(String[] args) throws Exception {
 		InputStreamReader isr = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(isr);
 
 		new Main(in);
 	}
 
 }

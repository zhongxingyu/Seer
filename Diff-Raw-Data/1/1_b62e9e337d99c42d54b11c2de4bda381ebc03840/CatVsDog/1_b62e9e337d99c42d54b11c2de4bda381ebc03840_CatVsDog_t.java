 import java.util.Scanner;
 
 public class CatVsDog {
 	public static void main(String[] args) {
 		Scanner sc = new Scanner(System.in);
 		int voters = 0;
 		int nbrTests = Integer.parseInt(sc.nextLine());
 		String[] info;
 
 		VoteBooth booth;
 		for(int i = 0; i < nbrTests; i++) {
 			booth = new VoteBooth();
 			info = sc.nextLine().split(" "); 
 			voters = Integer.parseInt(info[2]);
 			for(int j=0; j < voters; j++) {
 				info = sc.nextLine().split(" ");	
 				booth.add(new Vote(info[0], info[1]));
 			}
 			System.out.println(booth.getMax());
 		}
 	}
 }

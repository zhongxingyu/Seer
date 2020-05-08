 import java.util.ArrayList;
 
 import players.BasePlayer;
 import players.ai.NoviceAI;
 import players.ai.RandomAI;
 import players.ai.RecursiveAI;
 import players.ai.minmax.MinMaxAI;
 import players.ai.minmax.MinMaxAI2;
 
 
 public class StatisticsRunner {
 	BasePlayer p1, p2;
 	int numMatches;
 	public StatisticsRunner(BasePlayer p1, BasePlayer p2, int numMatches){
 		this.p1 = p1;
 		this.p2 = p2;
 		this.numMatches = numMatches;
 	}
 	public int [] simulate(boolean reverse){
 		ArrayList<GM> gameMasters = new ArrayList<GM>();
 		ArrayList<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < numMatches; i++){
 			GM g;
 			if (!reverse)
 				g = new GM(false,false,0,p1,p2);
 			else 
 				g = new GM(false,false,0,p2,p1);
 			Thread t = new Thread(g, "Quarto " + i);
 			t.start();
 			gameMasters.add(g);
 			threads.add(t);
 		}
 
 		int results[] = new int[3];
 		for (int i = 0; i < 3; i++) results[i] = 0;
 		for (int i = 0; i < numMatches; i++){
 			//System.out.println("Complete " + ((double)i / (double)numMatches) + "%");
 			try {
 				threads.get(i).join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			int index = gameMasters.get(i).winner;
 			results[index]++;
 		}
 		System.out.println(numMatches + " games was played, the results are:");
 		if (!reverse){
 			System.out.println(p1.getName() + " won: " + results[1]);
 			System.out.println(p2.getName() + " won: " + results[2]);
 		}
 		else{
 			System.out.println(p1.getName() + " won: " + results[2]);
 			System.out.println(p2.getName() + " won: " + results[1]);
 			
 		}
 		System.out.println("Draws: " + results[0] + "\n" );
 		return results;
 
 	}
 	public int[] doGames(BasePlayer p1, BasePlayer p2, int numMatches){
 		ArrayList<GM> gameMasters = new ArrayList<GM>();
 		ArrayList<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < numMatches; i++){
 			GM g = new GM(false,false,0,p1,p2);
 			Thread t = new Thread(g, "Quarto " + i);
 			t.start();
 			gameMasters.add(g);
 			threads.add(t);
 		}
 
 		int results[] = new int[3];
 		for (int i = 0; i < 3; i++) results[i] = 0;
 		for (int i = 0; i < numMatches; i++){
 			try {
 				threads.get(i).join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			int index = gameMasters.get(i).winner;
 			results[index]++;
 		}
 		return results;
 
 	}
 	public void run(){
 		int batchesOfMatches = numMatches/4;
 		int []totalScore = new int[3];
 		for (int j = 0; j < 3; j++){
 			totalScore[j] += 0;
 		}
 		for (int i = 0; i < batchesOfMatches; i++){
 			System.out.println("Complete " + (((double)i*100)/((double)batchesOfMatches)) + "%");
 			int []r;
 			if (i%2 == 0){
 				r = doGames(p1, p2, 10);
 
 				for (int j = 0; j < 3; j++){
 					totalScore[j] += r[j];
 					//System.out.println(totalScore[j] + " += " + r[j] );
 				}
 			}
 			else{
 				r = doGames(p2, p1, 10);
 				totalScore[0] += r[0];
 				totalScore[2] += r[1];
 				totalScore[1] += r[2];
 			}
 		}
		System.out.println(numMatches + " games was played, the results are:");
 
 		System.out.println(p1.getName() + " won: " + totalScore[1]);
 		System.out.println(p2.getName() + " won: " + totalScore[2]);
 		System.out.println("Draws: " + totalScore[0] + "\n" );
 	}
 	public static void main(String[] args)
     {
		StatisticsRunner s2 = new StatisticsRunner( new MinMaxAI(false,2),new NoviceAI(false), 100);
 		s2.run();
 		
 		
 	}
 }

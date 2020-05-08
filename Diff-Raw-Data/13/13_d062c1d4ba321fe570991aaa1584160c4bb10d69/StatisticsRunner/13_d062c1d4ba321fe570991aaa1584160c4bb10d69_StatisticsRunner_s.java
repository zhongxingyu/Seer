 import java.util.ArrayList;
 import java.util.HashMap;
 
 import players.BasePlayer;
 import players.ai.NoviceAI;
 import players.ai.RandomAI;
 import players.ai.RecursiveAI;
 import players.ai.minmax.MemoMinMaxAI;
 import players.ai.minmax.MinMaxAI;
 import players.ai.minmax.MinMaxAI2;
 
 
 public class StatisticsRunner {
 	BasePlayer p1, p2;
 	int numMatches;
 	int batchSize = 4;
 	public StatisticsRunner(BasePlayer p1, BasePlayer p2, int numMatches){
 		this.p1 = p1;
 		this.p2 = p2;
 		this.numMatches = numMatches;
 	}
 	public int[] doGames(BasePlayer p1, BasePlayer p2, int matchesToPlay){
 		ArrayList<GM> gameMasters = new ArrayList<GM>();
 		ArrayList<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < matchesToPlay; i++){
 			GM g = new GM(false,false,0,p1,p2);
 			Thread t = new Thread(g, "Quarto " + i);
 			t.start();
 			gameMasters.add(g);
 			threads.add(t);
 		}
 
 		int results[] = new int[3];
 		for (int i = 0; i < 3; i++) results[i] = 0;
 		for (int i = 0; i < matchesToPlay; i++){
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
 		int batchesOfMatches = numMatches/batchSize;
 		int matchCount = 0;
 		int []totalScore = new int[3];
 		for (int j = 0; j < 3; j++){
 			totalScore[j] += 0;
 		}
 		for (int i = 0; i < batchesOfMatches+1; i++){
 			System.out.println("Complete " + Math.round((((double)matchCount*100)/((double)numMatches))) + "%");
 			int []r;
 			int matchesToPlay = min(batchSize,numMatches-matchCount);
 			if (i%2 == 0){
 				r = doGames(p1, p2, matchesToPlay);
 
 				for (int j = 0; j < 3; j++){
 					totalScore[j] += r[j];
 				}
 			}
 			else{
 				r = doGames(p2, p1, matchesToPlay);
 				totalScore[0] += r[0];
 				totalScore[2] += r[1];
 				totalScore[1] += r[2];
 			}
 			matchCount += min(batchSize,numMatches-matchCount);
 		}
 		System.out.println(matchCount + " games was played, the results are:");
 
 		System.out.println(p1.getName() + " won: " + totalScore[1] + " (" + Math.round((double)(totalScore[1]*100)/matchCount) + "%)");
 		System.out.println(p2.getName() + " won: " + totalScore[2] + " (" + Math.round((double)(totalScore[2]*100)/matchCount) + "%)");
 		System.out.println("Draws: " + totalScore[0] + " (" + Math.round((double)(totalScore[0]*100)/matchCount) + "%)");
 	}
 	private int min(int x, int y) {
 		// TODO Auto-generated method stub
 		if (x < y) return x;
 		else return y;
 	}
 	public static void main(String[] args)
     {
		StatisticsRunner s2 = new StatisticsRunner( new MinMaxAI(false,3),new NoviceAI(false), 20);
 		s2.run();
 	}
 }

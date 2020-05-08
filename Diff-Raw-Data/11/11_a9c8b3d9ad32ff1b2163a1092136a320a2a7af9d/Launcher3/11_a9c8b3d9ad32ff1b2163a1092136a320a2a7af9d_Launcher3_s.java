 package tests;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import search.Search3;
 import search.SearchS4V29;
 import util.OldPositions;
 import util.board2.State2;
 import util.board4.Debug;
 import util.board4.State4;
 import ai.modularAI2.Evaluator2;
 import customAI.evaluators.board4.SuperEvalS4V8;
 
 /**
  * Simple launcher for playing two AIs. Prints board state after each move
  * and stops when a king has died. Uses {@link State2} for board representation
  * @author jdc2172
  *
  */
 public class Launcher3 {
 	public static void main(String[] args){
 		/*State4 state = new State4();
 		state.initialize();*/
 		State4 state = Debug.loadConfig(OldPositions.queenKingMate);
 		
 		Evaluator2<State4> e1 =
 				new SuperEvalS4V8();
 		
 		Evaluator2<State4> e2 = 
 				new SuperEvalS4V8();
 		
 		List<Search3> l = new ArrayList<Search3>();
 		Search3 search1 = new SearchS4V29(20, state, e1, 20, false);
 		Search3 search2 = new SearchS4V29(20, state, e2, 20, false);
 		l.add(search1);
 		l.add(search2);
 		
 		final long maxTime = 3*1000;
 		final int maxDepth = 20;
 		
 		int[] move = new int[2];
 		int turn = State4.WHITE;
 		
 		System.out.println(state);
 		
 		while(state.kings[1-turn] > 0 && (state.queens[0] > 0)){
 			System.out.println("moving player "+turn+"...");
 			search(l.get(turn), turn, maxDepth, maxTime, move);
 			if(move[0] == move[1]){
 				break;
 			}
 			state.executeMove(turn, 1L<<move[0], 1L<<move[1]);
 			
 			System.out.println(state);
 			turn = 1-turn;
 		}
 		
 		System.out.println("player "+turn+" wins");
 		System.exit(0);
 	}
 	
 	private static void search(final Search3 s, final int player,
 			final int maxPly, final long searchTime, final int[] move){
 		Thread t = new Thread(){
 			public void run(){
 				s.search(player, move, maxPly);
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		long start = System.currentTimeMillis();
 		while(t.isAlive() && System.currentTimeMillis()-start < searchTime){
 			try{
 				Thread.sleep(30);
 			} catch(InterruptedException e){}
 		}
 		
 		s.cutoffSearch();
 		while(t.isAlive()){
 			try{
 				t.join();
 			} catch(InterruptedException e){}
 		}
 	}
 	
 	private static void printBoard(State2 b){
 		System.out.println(b);
 		System.out.println("=======================");
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 * @param i offset
 	 * @return
 	 */
 	private static String getMoveString(int[] l, int i){
 		return ""+(char)('A'+l[i])+(l[i+1]+1);
 	}
 }

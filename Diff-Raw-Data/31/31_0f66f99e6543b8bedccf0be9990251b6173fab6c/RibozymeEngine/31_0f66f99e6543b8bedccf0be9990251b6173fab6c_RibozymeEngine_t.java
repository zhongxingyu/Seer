 package uci;
 
 import search.Search4;
 import search.search32k.SearchS4V32k;
 import state4.State4;
 import time.TimerThread3;
 import eval.Evaluator2;
 import eval.evalV8.SuperEvalS4V8;
 
 public class RibozymeEngine implements UCIEngine{
 
 	private final Search4 s;
 	private Thread t;
 	private final int[] moveStore = new int[2];
 	private Position p;
 	private TimerThread3.Controller c;
 	
 	public RibozymeEngine(){
 		
 		Evaluator2<State4> e = 
 				new SuperEvalS4V8();
 				//new ExpEvalV2();
 		
 		s = new SearchS4V32k(e, 21, false);
 	}
 	
 	@Override
 	public void go(final GoParams params, final Position p) {
 		final int player = p.sideToMove;
 		if(!params.infinite && params.moveTime == -1){ //allocate time
 			t = new Thread(){
 				public void run(){
 					final int inc = 0;
 					TimerThread3.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
 					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
 					String move = posString(moveStore[0])+posString(moveStore[1]);
 					System.out.println("bestmove "+move+promotion);
 				}
 			};
 			t.setDaemon(true);
 			t.start();
 		} else if(!params.infinite && params.moveTime != -1){ //fixed time per move
			assert false;
 			t = new Thread(){
 				public void run(){
 					s.search(player, p.s, moveStore);
 					
 					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
 					String move = posString(moveStore[0])+posString(moveStore[1]);
 					System.out.println("bestmove "+move+promotion);
 				}
 			};
 			t.setDaemon(true);
 			t.start();
 			final Thread timer = new Thread(){
 				public void run(){
 					final long start = System.currentTimeMillis();
 					final long targetTime = params.time[player];
 					long time;
 					while((time = System.currentTimeMillis()-start) < targetTime){
 						try{
 							Thread.sleep(time/2);
 						} catch(InterruptedException e){}
 					}
 					s.cutoffSearch();
 				}
 			};
 			timer.setDaemon(true);
 			timer.start();
 		} else if(params.infinite){
			assert false;
 			t = new Thread(){
 				public void run(){
 					s.search(player, p.s, moveStore);
 					String promotion = (p.s.pawns[player] & 1L<<moveStore[0]) != 0 && (moveStore[1]/8==7 || moveStore[1]/8==0)? "q": "";
 					String move = posString(moveStore[0])+posString(moveStore[1]);
 					System.out.println("bestmove "+move+promotion);
 				}
 			};
 			t.setDaemon(true);
 			t.start();
 		}
 	}
 	
 	private static String posString(int pos){
 		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
 	}
 
 	@Override
 	public void stop() {
 		s.cutoffSearch();
		final Thread t = this.t;
 		if(t != null){
 			while(t.isAlive()){
 				try{
 					t.join();
 				} catch(InterruptedException e){}
 			}
 		}
 	}
 
 	@Override
 	public void resetEngine() {
 		stop();
 		s.resetSearch();
 	}
 
 }

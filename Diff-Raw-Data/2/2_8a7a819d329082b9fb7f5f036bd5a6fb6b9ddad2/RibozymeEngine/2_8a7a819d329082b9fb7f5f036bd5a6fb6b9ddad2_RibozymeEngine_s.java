 package uci;
 
 import search.MoveSet;
 import search.Search4;
 import search.search34.Search34v3;
 import state4.BitUtil;
 import state4.Masks;
 import state4.State4;
 import time.TimerThread5;
 import eval.Evaluator3;
 import eval.e9.E9;
 
 public final class RibozymeEngine implements UCIEngine{
 
 	// major-version.search-version.eval-version
 	private final static String name = "ribozyme 0.9v1.34v3";
 	
 	private final Search4 s;
 	private Thread t;
 	private final MoveSet moveStore = new MoveSet();
 	
 	public RibozymeEngine(final int hashSize){
 		
 		final Evaluator3 e = new E9();
 		
		s = new Search34v3(e, 20, true);
 	}
 	
 	@Override
 	public String getName(){
 		return name;
 	}
 	
 	private static String buildMoveString(final int player, final State4 s, final MoveSet moveStore){
 		final char promotionChar;
 		switch(moveStore.promotionType){
 		case State4.PROMOTE_QUEEN:
 			promotionChar = 'q';
 			break;
 		case State4.PROMOTE_ROOK:
 			promotionChar = 'r';
 			break;
 		case State4.PROMOTE_BISHOP:
 			promotionChar = 'b';
 			break;
 		case State4.PROMOTE_KNIGHT:
 			promotionChar = 'n';
 			break;
 		default:
 			promotionChar = 'x';
 			assert false;
 			break;
 		}
 		final boolean isPromoting = (s.pawns[player] & moveStore.piece) != 0 && (moveStore.moves & Masks.pawnPromotionMask[player]) != 0;
 		final String move = posString(BitUtil.lsbIndex(moveStore.piece))+posString(BitUtil.lsbIndex(moveStore.moves));
 		return move+(isPromoting? promotionChar: "");
 	}
 	
 	@Override
 	public void go(final GoParams params, final Position p) {
 		final int player = p.sideToMove;
 		if(!params.infinite && params.moveTime == -1){ //allocate time
 			t = new Thread(){
 				public void run(){
 					final int inc = params.increment[player];
 					TimerThread5.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
 					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
 				}
 			};
 			t.setDaemon(true);
 			t.start();
 		} else if(!params.infinite && params.moveTime != -1){ //fixed time per move
 			assert false;
 			t = new Thread(){
 				public void run(){
 					s.search(player, p.s, moveStore);
 					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
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
 					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
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

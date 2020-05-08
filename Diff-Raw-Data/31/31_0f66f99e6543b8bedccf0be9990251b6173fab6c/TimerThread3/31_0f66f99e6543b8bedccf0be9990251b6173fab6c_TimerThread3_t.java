 package time;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import search.Search4;
 import search.SearchListener;
 import state4.State4;
 
 public final class TimerThread3 extends Thread{
 	public static interface Controller{
 		public void stopSearch();
 		public boolean isFinished();
 	}
 	
 	private final static int failLow = -1;
 	private final static int failHigh = 1;
 	private final AtomicBoolean stopSearch = new AtomicBoolean(false);
 	private final AtomicBoolean isFinished = new AtomicBoolean(false);
 	
 	private final Search4 search;
 	private final State4 s;
 	private long time;
 	private long inc;
 	private int player;
 	private int[] moveStore;
 	
 	private static final boolean debug = false;
 	
 	/** stores extra time from aspiration window failures*/
 	private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();
 	private final static class PlySearchResult{
 		int ply;
 		long move;
 	}
 	private final LinkedBlockingQueue<PlySearchResult> plyq = new LinkedBlockingQueue<PlySearchResult>();
 	
 	private final SearchListener l = new SearchListener() {
 		@Override
 		public void plySearched(long move, int ply) {
 			PlySearchResult temp = new PlySearchResult();
 			temp.move = move;
 			temp.ply = ply;
 			interrupt();
 			plyq.add(temp);
 		}
 		@Override
 		public void failLow(int ply) {
 			q.add(failLow);
 			interrupt();
 		}
 		@Override
 		public void failHigh(int ply) {
 			q.add(failHigh);
 			interrupt();
 		}
 	};
 	
 	private TimerThread3(Search4 search, State4 s, int player, long time, long inc, int[] moveStore) {
 		setDaemon(true);
 		this.search = search;
 		this.s = s;
 		this.player = player;
 		this.time = time;
 		this.inc = inc;
 		this.moveStore = moveStore;
 	}
 	
 	@Override
 	public void run(){
 		long start = System.currentTimeMillis();
 		final int material = getMaterial(s);
 		long target = time / (getHalfMovesRemaining(material)/2);
 		search.setListener(l);
 		//final long maxTime = (long)(time*(material >= 60? .04: .06));
 		final long maxTime = (long)(target * 1.3);
 		
 		if(debug){
 			System.out.println("moves remaining = "+(getHalfMovesRemaining(material)/2));
 			System.out.println("target time = "+target);
 			System.out.println("max time = "+maxTime);
 		}
 		
 		final Thread t = new Thread(){
 			public void run(){
				search.search(player, s, moveStore);
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		
 		long move = 0;
 		int currentPly = 0;
 		int lastpvChange = 1;
 		while(System.currentTimeMillis()-start < target &&
 				System.currentTimeMillis()-start < maxTime && 
 				!stopSearch.get()){ //so we dont keep extending forever
 			
 			while(!plyq.isEmpty()){
 				PlySearchResult r = plyq.poll();
 				if(r.ply != 1 && r.move != move){// && currentPly < r.ply){
 					lastpvChange = r.ply;
 					target += target*.15;
 				}
 				move = r.move;
 				currentPly = r.ply > currentPly? r.ply: currentPly;
 			}
 			
 			if(currentPly-lastpvChange+1 > 6 && currentPly > 8){ //perhaps increase difficulty with fail lows
 				break;
 				//target -= target*.2;
 			}
 			
 			//handle adjustments from search failures
 			while(!q.isEmpty()){
 				int failType = q.poll();
 				//double scale = failType == failHigh? .005: .01;
 				double scale = 0;
 				target += target*scale;
 			}
 
 			final long remainingTime = (target-(System.currentTimeMillis()-start))/2;
 			if(remainingTime > 10){
 				try{
 					Thread.sleep(remainingTime);
 				} catch(InterruptedException e){}
 			}
 		}
 		
 		search.cutoffSearch();
 		while(t.isAlive()){
 			try{
 				t.join();
 			} catch(InterruptedException e){}
 		}
 		isFinished.set(true);
 	}
 	
 	public static void searchBlocking(Search4 search, State4 s, int player, long time, long inc, int[] moveStore){
 		final TimerThread3 t = new TimerThread3(search, s, player, time, inc, moveStore);
 		t.start();
 		while(t.isAlive()){
 			try{
 				t.join();
 			} catch(InterruptedException e){}
 		}
 	}
 	
 	public static Controller searchNonBlocking(Search4 search, State4 s, int player, long time, long inc, int[] moveStore){
 		final TimerThread3 t = new TimerThread3(search, s, player, time, inc, moveStore);
 		final Controller temp = new Controller(){
 			@Override
 			public void stopSearch() {
 				t.endSearch();
 			}
 			@Override
 			public boolean isFinished() {
 				return t.isFinished.get();
 			}
 		};
 		t.start();
 		return temp;
 	}
 	
 	private void endSearch(){
 		stopSearch.set(true);
 		interrupt();
 	}
 	
 	private static int getMaterial(State4 s){
 		int m = 0;
 		for(int a = 0; a < 2; a++){
 			m += s.pieceCounts[a][State4.PIECE_TYPE_BISHOP]*3;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_KNIGHT]*3;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_PAWN]*1;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_QUEEN]*9;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_ROOK]*5;
 		}
 		return m;
 	}
 	
 	/**
 	 * gets remaining half moves as calculated by
 	 * 'time management procedures in computer chess'
 	 * @param material
 	 * @return returns half moves remaining
 	 */
 	private static int getHalfMovesRemaining(int material){
 		if(material < 20){
 			return material+10;
 		} else if(material <= 60){
 			return (int)(3./8*material+22);
 		} else{
 			return (int)(5./4*material-30);
 		}
 	}
 };

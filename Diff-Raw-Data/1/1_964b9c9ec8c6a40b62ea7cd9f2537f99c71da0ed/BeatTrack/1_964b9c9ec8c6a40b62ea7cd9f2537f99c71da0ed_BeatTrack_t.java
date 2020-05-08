 package sound;
 
 
 import game.GameControl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BeatTrack {
 	
 	private boolean[] beats = new boolean[32];
 	private List<BeatListener> listeners;
 	private boolean playing = false;
 	
 	private long startTime;
 	private int beatIndex;
 	private long beatTime;
 	private long endTime;
 	private int loopsLeft = 1;
 	
 	public static long DELAY = 1287;
 	
 	/**
 	 * Creates a BeatTrack based on the code. The code consists of 0's and 1's, where
 	 * 0 corresponds to an eighth rest and 1 corresponds to an eighth beat.
 	 * @param code - A string that tells when to make a heartbeat.
 	 */
 	public BeatTrack(String code) {
 		listeners = new ArrayList<BeatListener>();
 		char[] c = code.toCharArray();
 		for (int i=0;i<32;i++) {
 			if (c[i] == '0') {
 				beats[i] = false;
 			}
 			else {
 				beats[i] = true;
 			}
 		}
 	}
 	
 	public void addBeatListener(BeatListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void play(long time) {
 		startTime = time;
 		endTime = startTime + 16*429L;
 		playing = true;
 		beatIndex = 0;
 	}
 	
 	public void update(long currentTime) {
 		if (playing) {
 			if (currentTime >= beatTime) {
 				getNextBeat();
 				for (BeatListener listener : listeners) {
 					listener.beat();
 				}
 			}
 			if (currentTime >= endTime) {
 				if (loopsLeft > 0) {
 					play(currentTime);
 					loopsLeft--;
 				}
 				else {
 					for (BeatListener listener : listeners) {
 						listener.endOfTrack();
 					}
					playing = false;
 				}
 			}
 		}
 	}
 	
 	private void getNextBeat() {
 		beatIndex++;
 		while (beatIndex < 32 && beats[beatIndex] == false) {
 			beatIndex++;
 		}
 		if (beats[beatIndex]) {
 			beatTime = startTime + beatIndex*429;
 		}
 	}
 	
 	
 }

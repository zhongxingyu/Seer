 package models;
 
 import java.util.ArrayList;
 import java.util.Timer;
 
import util.interfaces.DataUpdatedListener;
 import util.interfaces.Listener;
 
public class BoardObserver implements DataUpdatedListener{
 	public Board board;
 	public Timer timer;
 	public boolean timer_is_running;
 	
 	public BoardObserver(){
 		board = new Board();
 		board.subscribeListener(this);
 	}
 	
 	public void build_timer(){
 		timer = new Timer();
 	}
 	
 	/*************************************************
 	 * Observer Pattern
 	 *************************************************/
 	
 	private ArrayList<Listener> listeners = new ArrayList<Listener>();
 	
 	public void notify_observers(){
 		// call this method everytime we have new date for our observers rdy.
 		// that mean, everytime our computation thread has produced new data
 		
 		for(int i = 0; i < listeners.size(); i++){
 			listeners.get(i).handleEvent();
 		}
 	}
 	
 	public void subscribeListener(Listener l) {
 		listeners.add(l);
 	}
 	
 	public void unsubscribeListener(Listener l) {
 		listeners.remove(l);
 	}
 
 	
 	/*************************************************
 	 * Event Listening: Board has new data
 	 *************************************************/
 
 	int tmp = 0;
 	@Override
 	public void handleEvent() {
 		if(tmp != board.points)System.out.println("Points: " + board.points + " lines: " + board.line_count);
 		tmp = board.points;
 		notify_observers();	
 	}
 
 }

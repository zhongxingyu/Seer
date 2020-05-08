 package mainframe;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import votingStation.VotingStationAction;
 import votingStation.VotingStation_window;
 
 import mainframe.IMainframeWindow.MainframeAction;
 
public class MainframeClick implements ActionListener{
 	private MainframeWindow button_window;
 	private MainframeAction mainframe_action;
 	private Object lock;
 	
	public MainframeClick(MainframeWindow callerWindow, MainframeAction action, Object lock) {
 		button_window = callerWindow;
 		mainframe_action = action;
 		this.lock = lock;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		synchronized (lock) {
 			button_window.setAction(mainframe_action);
 			lock.notify();
 		}
 	}
 }

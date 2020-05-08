 package logic;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import java.util.Observable;
 
 public class Timer extends Observable {
 
 	private static final int	BEGIN_TIJD	= 200;
 	private static final int	JOKER_TIJD	= 16;
 	private int					huidigeTijd;
 	private javax.swing.Timer timer;
 	private Date metingBeginTijd;
 
 	public Timer() {
 		huidigeTijd = BEGIN_TIJD;
 		timer = new javax.swing.Timer(10, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Date metingEindTijd = new Date();
 				long verschil = (metingEindTijd.getTime() - metingBeginTijd.getTime()) / 1000;
 				if(verschil >= 1) {
 					huidigeTijd--;
 					
 					setChanged();
 					notifyObservers();
 					metingBeginTijd = new Date();
 				}
 			}
 		});
 	}
 	
 	public void start(){
 		metingBeginTijd = new Date();
 		timer.start();
 	}
 	
 	public void stop(){
 		timer.stop();
 	}
 
 	public boolean canDeductJoker(int jokerAmount) {
 		return BEGIN_TIJD - jokerAmount * JOKER_TIJD > 0 ? true : false;
 	}
 
 	public void deductJoker(int jokerAmount) {
 		setTime(getTime() - jokerAmount * JOKER_TIJD);
 	}
 
 	public int getTime() {
 		return huidigeTijd;
 	}
 
 	public boolean hasLost() {
		return huidigeTijd < 0;
 	}
 
 	private void setTime(int time) {
 		huidigeTijd = time;
 	}
 
 	@Override
 	public String toString() {
 		return Integer.toString(huidigeTijd);
 	}
 
 }

 package javaCore.ch6;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 public class TalkingClock {
 
 	private int interval;
 	private boolean beep;
 
 	public TalkingClock(int interval, boolean beep) {
 		this.interval = interval;
 		this.beep = beep;
 	}
 
 	public void start() {
 	}
	
	public class TimerPrinter implements ActionListener{
 
		@Override
 		public void actionPerformed(ActionEvent e) {
			Date now=new Date();
			System.out.println("the time is"+now);
			if(beep){
 				Toolkit.getDefaultToolkit().beep();
 			}
 		}
		
 	}
	
	public static void main(String[] args){
	    System.out.println(System.getProperty("user.dir"));
 	}
 
 }

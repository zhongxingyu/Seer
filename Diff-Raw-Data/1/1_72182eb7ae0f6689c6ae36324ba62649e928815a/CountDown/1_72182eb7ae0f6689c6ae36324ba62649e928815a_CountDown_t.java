 package cha.domain;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 import cha.event.EventBus;
 import cha.event.Event;
 /**
  * A class which creates a timer.
  * @author Malla
  */
 
 public class CountDown implements ActionListener{
 	private Timer timer;
 	private int count;
 
 	public CountDown(){
 		count = 30;
 		timer = new Timer(1000, this);
 		timer.start();
 	}
 
 	private void ticktock(){
 		EventBus.getInstance().publish(Event.TimeTick, Integer.toString(count), null);
		--count;
 		if (count==-1){
 			timeUp();
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		ticktock();
 	}
 	
 	private void timeUp(){
 		timer.stop();
 		EventBus.getInstance().publish(Event.TimeOver, null, null);
 	}
 	
 	public void stopTimer(){
 		timer.stop();
 	}
 }

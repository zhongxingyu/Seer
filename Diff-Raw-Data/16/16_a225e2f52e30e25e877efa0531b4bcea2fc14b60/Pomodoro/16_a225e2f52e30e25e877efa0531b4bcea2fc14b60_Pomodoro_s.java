 package org.tomighty.states;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.tomighty.State;
 import org.tomighty.bus.Bus;
 import org.tomighty.bus.messages.ChangeState;
 import org.tomighty.time.Time;
 import org.tomighty.time.Timer;
 import org.tomighty.time.TimerListener;
 import org.tomighty.util.Label;
 
 public class Pomodoro implements State, ActionListener, TimerListener {
 
 	private JLabel remainingTime;
 	private Timer timer;
 
 	@Override
 	public Component render() throws Exception {
		Time time = new Time(0, 10);
 		
 		JButton interruptButton = new JButton("Interrupt");
 		interruptButton.addActionListener(this);
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.add(Label.small("Pomodoro"), BorderLayout.NORTH);
 		panel.add(remainingTime(time), BorderLayout.CENTER);
 		panel.add(interruptButton, BorderLayout.SOUTH);
 		
 		timer = new Timer("Pomodoro");
 		timer.listener(this);
 		timer.start(time);
 		
 		return panel;
 	}
 	
 	private JLabel remainingTime(Time time) {
 		remainingTime = Label.big(time.toString());
 		return remainingTime;
 	}
 
 	@Override
 	public void tick(Time time) {
 		if(time.isZero()) {
 			finished();
 		} else {
 			remainingTime.setText(time.toString());
 		}
 	}
 
 	private void finished() {
 		Bus.publish(new ChangeState(PomodoroFinished.class));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		timer.stop();
 		Bus.publish(new ChangeState(PomodoroInterrupted.class));
 	}
 
 }

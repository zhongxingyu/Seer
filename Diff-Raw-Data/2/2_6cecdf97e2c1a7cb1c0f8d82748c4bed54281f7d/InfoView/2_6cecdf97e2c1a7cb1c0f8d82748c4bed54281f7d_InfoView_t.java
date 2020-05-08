 package edu.mharper.tp2;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class InfoView extends JPanel implements ActionListener {
 	
 	JLabel timeRemaining;
 	JPanel remainingPanel;
 	JLabel whiteRemaining;
 	JLabel blackRemaining;
 	JButton endTurn;
 	
 	Timer timer;
 	public long time;
 	
 	int white, black;
 	
 	public InfoView() {
 		white = View.gameView.gameManager.countWhite();
 		black = View.gameView.gameManager.countBlack();
 		
 		endTurn = new JButton("End turn");
		//endTurn.setPreferredSize(new Dimension(Main.windowHeight / 5, 25));
 		
 		setPreferredSize(new Dimension(Main.windowWidth, Main.displayInfoSize));
 		
 		timeRemaining = new JLabel("" + time / 1000, JLabel.CENTER);
 		timeRemaining.setPreferredSize(new Dimension(Main.windowWidth, 25));
 		whiteRemaining = new JLabel("White remaining: " + white, JLabel.CENTER);
 		blackRemaining = new JLabel("Black remaining: " + black, JLabel.CENTER);
 		remainingPanel = new JPanel(new FlowLayout());
 		remainingPanel.setPreferredSize(new Dimension(Main.windowWidth, 50));
 		
 		time = Main.defaultTime;
 		timer = new Timer(1000, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (time > 0) {
 					time -= 1000;
 					timeRemaining.setText("" + time / 1000);
 				}
 				else {
 					resetTime();
 				}
 			}
 		});
 		timer.start();
 		
 		setLayout(new FlowLayout());
 		remainingPanel.add(whiteRemaining);
 		remainingPanel.add(blackRemaining);
 		add(timeRemaining, BorderLayout.NORTH);
 		add(remainingPanel, BorderLayout.SOUTH);
 		add(endTurn);
 	}
 	
 	public void resetTime() {
 		time = Main.defaultTime;
 		timeRemaining.setText("" + time / 1000);
 	}
 	
 	public void updateColors() {
 		white = View.gameView.gameManager.countWhite();
 		black = View.gameView.gameManager.countBlack();
 		
 		whiteRemaining.setText("White remaining: " + white);
 		blackRemaining.setText("Black remaining: " + black);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// End turn
 	}
 }

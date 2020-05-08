 package com.rojel.typetris;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 public class Typetris extends JFrame implements ActionListener {
 	private static final long serialVersionUID = 1395134340736969442L;
 	
 	private GameDisplay gameDisplay;
 	private NextDisplay nextDisplay;
 	private QueueDisplay queueDisplay;
 	private JButton run;
 	private JButton help;
 	private JTextField input;
 	private JLabel score;
 	
 	private GameLogic logic;
 	
 	public static void main(String[] args) {
 		new Typetris();
 	}
 	
 	public Typetris() {
 		super("Typetris");
 		this.setSize(440, 685);
 		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - 440) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - 685) / 2);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(false);
 		this.setLayout(null);
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch(Exception e) {
 			System.out.println("L&F fehlgeschlagen.");
 		}
 		
 		gameDisplay = new GameDisplay();
 		
 		gameDisplay.setBounds(5, 0, 300, 600);
 		this.add(gameDisplay);
 		
 		nextDisplay = new NextDisplay();
 		nextDisplay.setBounds(310, 0, 120, 120);
 		this.add(nextDisplay);
 		
 		queueDisplay = new QueueDisplay();
 		queueDisplay.setBounds(310, 125, 150, 475);
 		this.add(queueDisplay);
 		
 		run = new JButton("GO");
 		run.setBounds(310, 605, 55, 50);
 		run.addActionListener(this);
 		this.add(run);
 		
 		help = new JButton("Hilfe");
 		help.setBounds(370, 605, 60, 50);
 		help.addActionListener(this);
 		this.add(help);
 		
 		input = new JTextField();
 		input.setBounds(155, 605, 150, 50);
 		Font inputFont = new Font("Lucida Console", Font.PLAIN, 20);
 		input.setFont(inputFont);
 		input.addActionListener(this);
 		this.add(input);
 		
 		score = new JLabel("Score:0");
 		score.setBounds(5, 605, 145, 50);
 		score.setFont(inputFont);
 		this.add(score);
 		
 		setVisible(true);
 		
 		while(true) {
 			logic = new GameLogic(gameDisplay, nextDisplay, queueDisplay, score);
 			logic.run();
 			
 			while(logic.running) {
 			}
 			
 			JOptionPane.showMessageDialog(this, "Spiel beendet\n\nDeine Punktzahl: " + logic.getScore(), "Spiel beendet", JOptionPane.PLAIN_MESSAGE);
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == input || e.getSource() == run) {
 			String command = input.getText();
 			if(logic.isValid(command)) {
 				this.logic.addCommand(command);
 				input.setText("");
 				input.setBackground(Color.WHITE);
 			} else {
 				input.setText("");
 				input.setBackground(Color.RED);
 			}
 		} else if(e.getSource() == help) {
 			logic.paused = true;
			JOptionPane.showMessageDialog(this, "Typetris - Ein Spiel von Robert Heedt\nInspiriert vom Langzeitklassiker TETRIS\n\nTypetris ist eine Variation vom altbekannten TETRIS, bei dem es darum geht,\ndie herunterfallenden Blcke so zu stapeln, dass sie die Decke nicht erreichen.\nWenn das passiert, ist das Spiel vorbei. Doch im Gegensatz zum Original sind die Blcke nicht direkt kontrollierbar,\nsondern mssen durch eingetippte Befehle gesteuert werden. Zulssige Befehle sind:\n\nlinks, rechts, runter, dreh links, dreh rechts\n\nBereits eingegebene Befehle knnen mit einem Linksklick auf den Befehl wieder aus der Befehlsschleife entfernt werden.", "Hilfe", JOptionPane.PLAIN_MESSAGE);
 			logic.paused = false;
 		}
 	}
 }

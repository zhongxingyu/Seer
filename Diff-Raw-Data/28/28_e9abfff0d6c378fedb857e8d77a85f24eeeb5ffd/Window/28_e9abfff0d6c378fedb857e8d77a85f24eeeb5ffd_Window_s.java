 package poker;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.text.DefaultCaret;
 
 public class Window {
 
 	private JTextArea community;
 	private JTextArea score;
 	private JTextArea playcards;
 	private JTextArea terminal;
 	private JTextField bidfield;
 	private JButton bid;
 	private JButton call;
 	private JButton fold;
 	private Poker caller; //used to see who called this window
 	private JMenuItem play;
 	private JMenuItem how;
 	private JMenuItem about;
 	private JMenuItem exit;
 	private JMenu menu;
 	private JMenuBar menubar;
 	private JFrame window;
 	
 	public Window(final Poker caller) {
 		this.caller = caller;
 		//Creates the Main Window and sets settings
 		window = new JFrame("Texas Hold 'em");
 		window.setSize(640, 480);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//Creates main panel and sets settings
 		JPanel full = new JPanel();
 		full.setLayout(new BoxLayout(full, BoxLayout.Y_AXIS));
 		//create sub panels and sets settings
 		JPanel bottom = new JPanel();
 		bottom.setSize(640, 192);
 		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
 		JPanel interaction = new JPanel();
 		interaction.setMaximumSize(new Dimension(192, 192));
 		interaction.setLayout(new BoxLayout(interaction, BoxLayout.Y_AXIS));
 		interaction.setBorder(BorderFactory.createMatteBorder(5,0,5,5, Color.black));
 		JPanel middle = new JPanel();
 		middle.setSize(640,144);
 		middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
 		JPanel empty = new JPanel();
 		empty.setMaximumSize(new Dimension (192, 144));
 		JPanel top  = new JPanel();
 		top.setSize(640, 144);
 		top.setLayout(new BoxLayout(top,BoxLayout.X_AXIS));
 					
 		//initialize the text areas
 		community = new JTextArea();
 		community.setMaximumSize(new Dimension(448, 144));
 		community.setLineWrap(true);
 		community.setEditable(false);
 		community.setBorder(BorderFactory.createLineBorder(Color.black, 5));
 		score = new JTextArea();
 		score.setMaximumSize(new Dimension(192,144));
 		score.setLineWrap(true);
 		score.setEditable(false);
 		score.setBorder(BorderFactory.createMatteBorder(5,0,5,5, Color.black));
 		playcards = new JTextArea(); 
 		playcards.setMaximumSize(new Dimension (448, 144));
 		playcards.setLineWrap(true);
 		playcards.setEditable(false);
 		playcards.setBorder(BorderFactory.createMatteBorder(0,5,5,5, Color.black));
 		terminal = new JTextArea();
 		terminal.setMaximumSize(new Dimension(448, 192));
 		terminal.setLineWrap(true);
 		terminal.setEditable(false);
 		DefaultCaret caret = (DefaultCaret)terminal.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(terminal);
 		scrollPane.setMaximumSize(new Dimension(448, 192));
 		scrollPane.setBorder(BorderFactory.createMatteBorder(0,5,5,5, Color.black));
 		//Initializes interactive widgets
 		bidfield = new JTextField();
 		bidfield.setMaximumSize(new Dimension(192,32));
 		bid = new JButton("Bid");
 		bid.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae){
 				String entered = bidfield.getText();
 				int ent;
 				try {
 					ent = Integer.parseInt(entered);
 					Poker.dropbox = ent;
 					synchronized (Poker.getLock()){
 						Poker.getLock().notify();
 					}
 				}
 				catch (NumberFormatException e){
 					print("Invalid Number, please enter again");
 				}
 				finally {
 					bidfield.setText(null);
 				}
 				
 				
 				
 			}
 		});
 		call = new JButton("Call");
 		call.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				synchronized (Poker.getLock()) {
 					Poker.dropbox = "call";
 					Poker.getLock().notify();
 				}
 			}
 		});
 		fold = new JButton("Fold");
 		fold.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				synchronized (Poker.getLock()){
 					Poker.dropbox = "folding";
 					Poker.getLock().notify();
 				}
 			}
 		});
 		//menu bar editing
 		menu = new JMenu("File");
 		play = new JMenuItem("Play new game"); 
 		play.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
 				Thread t = new Thread(caller);
 				t.start();
 			}
 		});
 		how = new JMenuItem("How to play");
 		how.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
 				
 			}
 		});
 		about = new JMenuItem("About");
 		about.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				
 			}
 		});
 		exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
 				System.exit(0);
 			}
 		});
 		//Initializes MenuBar
 		menubar = new JMenuBar();
 		menu.add(play);
 		menu.add(how);
 		menu.add(about);
 		menu.add(exit);
 		menubar.add(menu);
 		//add widgets to panels
 		interaction.add(Box.createGlue());
 		interaction.add(bidfield);
 		interaction.add(Box.createGlue());
 		interaction.add(bid);
 		interaction.add(Box.createGlue());
 		interaction.add(call);
 		interaction.add(Box.createGlue());
 		interaction.add(fold);
 		interaction.add(Box.createGlue());
 		top.add(community);
 		top.add(score);
 		middle.add(playcards);
 		middle.add(empty);
 		//bottom.add(terminal);
 		bottom.add(scrollPane);
 		bottom.add(interaction);
 		//add panels to main panel
 		full.add(top);
 		full.add(middle);
 		full.add(bottom);
 		//add main panel to window
 		window.add(full);
 		//add the menubar
 		window.setJMenuBar(menubar);
 		//display window
 		window.setResizable(false);
 		window.setVisible(true);
 	}
 
 	public void redrawScore(){
 		score.setText("-----Cash Amounts-----\n"); //clears out the text
 		for (int i = 1; i < caller.PLAYERS + 1; i++){
 			score.append(caller.getPlayer(i).getName()+": $"+caller.getPlayer(i).getCash() + "\n");
 		}
 		score.append("\nCash in pot: " + caller.getPot());
 	}
 
 	
 	public void print(String str){
 		terminal.append(str + "\n");
 	}
 	
 	public void printToCommunity(String str) {
 		community.append(str);
 	}
 	public void clearCommunity(){
 		community.setText("-----Community Cards-----\n");
 	}
 }

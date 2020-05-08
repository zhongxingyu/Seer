 package menu;
 import java.awt.*; 
 import javax.swing.*;
 
 import core.GameManager;
 
 import java.awt.event.*;
  
 //Main Menu
 public class MainMenuFrame { 
 	
 	//Variables of Components for Main Frame
 	private JFrame frame;
 	private JFrame OptionFrame;
 	private JPanel panel;
 	
 	private JButton startbutton;
 	private JButton optionbutton;
 	private JButton quitbutton;
 	
 	private JLabel label;
 	
 	//VAriables of Options Frame
 	private JButton sound;
 	private JButton difficulty;  
 	private JButton load;
 	private JPanel optionspanel;
 	
 	public MainMenuFrame(){
 		
 		//Frame of Menu
 		frame = new JFrame("Programmierpraktikum");
 		frame.setVisible(true);
 		frame.setSize(300,300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setLayout(null);
 		
 		//Panel for Components on Frame
 		panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0,0,300,400);
 		frame.add(panel);
 		
 		//Buttons
 		
 		//Start button
 		startbutton = new JButton("Start");
 		startbutton.setBounds(100,50,100,25);
 		panel.add(startbutton);
 		startbutton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				GameManager.ingame = true;
 				GameManager.menu = false;
 				}
 			}
 		);
 		
 		
 		//Option button
 		optionbutton = new JButton("options");
 		optionbutton.setBounds(100,120,100,25);
 		panel.add(optionbutton);
 		optionbutton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				
 				//Options Frame
 				JFrame optionsFrame = new JFrame("Option");
 				optionsFrame.setVisible(true);
 				optionsFrame.setSize(200, 180);
 				optionsFrame.setLocationRelativeTo(null);
 				optionsFrame.setResizable(false);
 				optionsFrame.setLayout(null);
 				
 				//Panel for Components on Frame
 				optionspanel = new JPanel();
 				optionspanel.setLayout(null);
 				optionspanel.setBounds(0,0,200,180);
 				optionsFrame.add(optionspanel);
 				optionspanel.setLayout(null);
 				
 				//Buttons for Options
 				sound = new JButton("Sound");
 				sound.setBounds(50, 20, 100, 25);
 				optionspanel.add(sound);
 				
 				difficulty = new JButton("Sound");
 				difficulty.setBounds(50, 60, 100, 25);
 				optionspanel.add(difficulty);
 				
 				load = new JButton("load game");
 				load.setBounds(50, 100, 100, 25);
 				optionspanel.add(load);
 			}		
 		}
 	);
 		
 		//Quit button
 		quitbutton = new JButton("Quit");
 		quitbutton.setBounds(100,190,100,25);
 		panel.add(quitbutton);
 		quitbutton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent e) {
 					System.exit(0);
 				}
 			}
 		);
 		
 		//Label
 		label = new JLabel("Gruppe 11");
 		label.setBounds(220,240,70,25);
 		panel.add(label);
 	}
 	
 	public static void main(String[] args) {		
 		
		//new MainMenuFrame();
 	}
 }
 
 // diesmal keine tutorials oder templates muhaha

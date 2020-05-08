import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
 
 public class Frame extends JFrame{
 
 	public static final String FRAME_NAME = "3203 - Networking";
 	Network network;
 	Panel view;
 	int radius,n;
 	
 	
 	public Frame(String name) {
 		super(name);
 		radius = 50;
 		n=10;
 
 		// Make sure it exists properly
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Add the JPanel
 		view = new Panel();
 		getContentPane().add(view);
 		
 		//Create the menu bar
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		JMenu sensorsMenu = new JMenu("Sensors");
 		JMenuItem sampleSensors = new JMenuItem("10");
 		JMenuItem sampleSensors2 = new JMenuItem("20");
 		sensorsMenu.add(sampleSensors);
 		sensorsMenu.add(sampleSensors2);
 		menuBar.add(sensorsMenu);
 		JMenu radiusMenu = new JMenu("Radius");
 		JMenuItem sampleRadius = new JMenuItem("50");
 		JMenuItem sampleRadius2 = new JMenuItem("60");
 		radiusMenu.add(sampleRadius);
 		radiusMenu.add(sampleRadius2);
 		menuBar.add(radiusMenu);
 		
 		
 		//action listeners for the menu bar
 		sampleSensors.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				n = 10;
 				network = new Network(n,radius);
 				view.DrawNetwork(network);
 			}
 		});
 		sampleSensors2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				n = 20;
 				network = new Network(n,radius);
 				view.DrawNetwork(network);
 			}
 		});
 		sampleRadius.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				radius = 50;
 				network = new Network(n,radius);
 				view.DrawNetwork(network);
 			}
 		});
 		sampleRadius2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				radius = 60;
 				network = new Network(n,radius);
 				view.DrawNetwork(network);
 			}
 		});
 		
 
 		// Size the frame.
 		setSize(Panel.WIDTH, Panel.HEIGHT);
 //		pack();
 
 		// Show it.
 		setVisible(true);
 	}
 	
 	public static void main(String [] args){
 		new Frame(FRAME_NAME);
 	}
 	
 }

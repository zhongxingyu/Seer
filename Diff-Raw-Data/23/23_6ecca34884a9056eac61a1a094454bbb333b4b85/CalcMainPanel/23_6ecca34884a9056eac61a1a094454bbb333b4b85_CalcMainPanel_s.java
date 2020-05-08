 
 import java.awt.BorderLayout;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 public class CalcMainPanel {
 	public static void main(String[] args){
 	JFrame theframe = setupFrame();
 	theframe.setVisible(true);
 	}
 	public static JFrame setupFrame(){
 		JFrame theframe = new JFrame("Calculator");
 		theframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		addComponents(theframe);
 		theframe.pack();
 		theframe.setSize(new Dimension(500,500));
 		return theframe;
 	}
 	public static void addComponents(JFrame frame){
 		Container stuff = frame.getContentPane();
 		stuff.setLayout(new GridBagLayout());
 		
 		//===set up gridbag===
 		GridBagConstraints loc = new GridBagConstraints();
 		loc.fill = GridBagConstraints.BOTH;
 		loc.weightx = .5;
 		loc.weighty = .5;
 		loc.gridx = 0;
 		loc.gridx = 0;
 		//=====================
 		
 		//===init the screen panel
 		loc.gridy = 1;
 		JPanel screenPanel = new JPanel(new BorderLayout());
 		OutDisplay screen = new OutDisplay();
 		stuff.add(screenPanel,loc);
 		screenPanel.add(screen.getLabel(),BorderLayout.CENTER);
 		//===================
 		
 		//===make the number button panel
 		loc.gridy = 2;
 		stuff.add(makeNumberPanel(screen),loc);
 		//=====================
 		
 		//===make the calc button panel
 		loc.weightx = .1;
 		loc.gridx = 1;
 		stuff.add(makeCalcPanel(screen),loc);
 		//===================
 
 		//===make the menu bar and add it
 		frame.setJMenuBar(makeMenuBar());
 	}
 	public static JMenuBar makeMenuBar(){
 		JMenuBar menubar = new JMenuBar();
 		JMenu test = new JMenu("Test");
 		menubar.add(test);
 		JMenuItem test2 = new JMenuItem("Test2");
 		test.add(test2);
 		return menubar;
 	}
 	public static JPanel makeCalcPanel(OutDisplay out){
 		JPanel calcPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints loc = new GridBagConstraints();
 		loc.weightx = .5;
 		loc.weighty = .5;
 		loc.fill = GridBagConstraints.BOTH;
 		loc.gridx = 0;
 		loc.gridy = 0;
 		calcPanel.add(new NumberButton("+",out),loc);
 		loc.gridy++;
 		calcPanel.add(new NumberButton("-",out),loc);
 		loc.gridy++;
 		calcPanel.add(new NumberButton("*",out),loc);
 		loc.gridy++;
 		calcPanel.add(new NumberButton("/",out),loc);
 		loc.gridy++;
 		calcPanel.add(new ExecuteButton("=",out),loc);
 		
 		
 		return calcPanel;
 	}
 	public static JPanel makeNumberPanel(OutDisplay out){
 		JPanel numberPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints loc = new GridBagConstraints();
 		loc.weightx = .5;
 		loc.weighty = .5;
 		loc.fill = GridBagConstraints.BOTH;
 		for(int i=1;i<4;i++){
 			for(int j=0;j<3;j++){
 				int num = i+(3*j);
 				loc.gridx=i-1;
 				loc.gridy=j;
 				numberPanel.add(new NumberButton(""+num,out),loc);
 			}
 		}
 		loc.gridy++;
 		loc.gridx = 1;
 		numberPanel.add(new NumberButton(""+0,out),loc);
 		
 		return numberPanel;
 	}
 	
 
 	
 	
 }

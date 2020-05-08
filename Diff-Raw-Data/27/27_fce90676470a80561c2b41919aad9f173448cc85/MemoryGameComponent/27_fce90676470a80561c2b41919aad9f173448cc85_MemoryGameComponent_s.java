 package edu.ucsb.cs56.S12.issues.issue0000805;
 import java.awt.GridLayout;
 import javax.swing.JComponent;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Font;
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.*;
 import java.lang.Math;
 
 
 /**
  * A Swing component for playing the Memory Card Game
    @author Bryce McGaw and Jonathan Yau (with some of Phill Conrad's code as a basis)
    @version CS56 Spring 2012
    @see MemoryGrid 
  */
 public class MemoryGameComponent extends JComponent
 {
     private MemoryGrid grid;
     private int gameCounter=0;
     private ArrayList<Icon> imgIcons = new ArrayList<Icon>();
     public JComponent restartB = new JButton("Restart");
     
     private JButton [] buttons;
 
     /** Constructor
 	
 	@param game an object that implements the MemoryGrid interface to keep track
 	            of the moves in each game, ensuring the rules are followed and detecting
 	            when the user has won.
     */
        
     public MemoryGameComponent(MemoryGrid game) {
 	super(); 
 	
 	this.grid= game;
         int gridSize = grid.getSize();
 	buttons= new JButton[gridSize];
 
 	//get the current classloader (needed for getResource method..  )
 	//                            (which is required for jws to work)
 	//ClassLoader classLoader = this.getClass().getClassLoader();
 	Class classs = this.getClass();
 
 	//load Icons 
 	Icon imgBlank = new ImageIcon(classs.getResource("/images/000.jpg"));
 	Icon img0     = new ImageIcon(classs.getResource("/images/036.jpg"));
 	Icon img1     = new ImageIcon(classs.getResource("/images/033.jpg"));
 	Icon img2     = new ImageIcon(classs.getResource("/images/029.jpg"));
 	Icon img3     = new ImageIcon(classs.getResource("/images/025.jpg"));
 	Icon img4     = new ImageIcon(classs.getResource("/images/018.jpg"));
 	Icon img5     = new ImageIcon(classs.getResource("/images/013.jpg"));
 	Icon img6     = new ImageIcon(classs.getResource("/images/006.jpg"));
 	Icon img7     = new ImageIcon(classs.getResource("/images/005.jpg"));
 
 	
 	//add all the images into the Icon ArrayList
 	imgIcons.add(img0);
 	imgIcons.add(img1);
 	imgIcons.add(img2);
 	imgIcons.add(img3);
 	imgIcons.add(img4);
 	imgIcons.add(img5);
 	imgIcons.add(img6);
 	imgIcons.add(img7);
 	
 	
 	//set layout to a grid of length sqrt(grid size)
 	this.setLayout(new GridLayout((int)Math.sqrt(gridSize),0)); 
 
 	for(int i=0; i<=(gridSize-1); i++) {
 	    JButton jb = new JButton(imgBlank);   //initially all buttons are blank
 	    buttons[i] = jb;
 	    jb.addActionListener(new ButtonListener(i));
 	    jb.setFocusPainted(false);          //get rid of annoying boxes appearing around icon next to clicked icon
 	    
 	    this.add(jb);  
 	}
     }
 
 
     class ButtonListener implements ActionListener {
 	private int num;
 
 	public ButtonListener(int i) {
 	    super();
 	    this.num = i;
 	}
 
         @Override
 	public void actionPerformed (ActionEvent event) {
 	    
 	Class classs = this.getClass();
 	Icon imgBlank = new ImageIcon(classs.getResource("/images/000.jpg"));
             
             //if 2 MemoryCards are flipped, flip back over
             if(grid.isTwoFlipped()){
 
                 JButton jb = buttons[grid.getFlipped()];
                 jb.setEnabled(true);
                 jb.setIcon(imgBlank);
                 grid.flip(grid.getFlipped());
                 jb = buttons[grid.getFlipped()];
                 jb.setEnabled(true);
                 jb.setIcon(imgBlank);
                 grid.flip(grid.getFlipped());
 
             }
 
 
             //if no MemoryCards are flipped, flip one
             if (!grid.isOneFlipped()){
                grid.flip(num);
                JButton jb = buttons[num];
 
 	       jb.setIcon(imgIcons.get(grid.getVal(num)-1));      //set image according to val
 	      
                jb.setEnabled(false);                               //make unclickable
             }
 
 
             
             //if one MemoryCard is flipped, flip other
             //then check if theyre matching
             else{
 
                 grid.flip(num);
                 JButton jb = buttons[num];
 
 		jb.setIcon(imgIcons.get(grid.getVal(num)-1));      //set image according to val
       
                 jb.setEnabled(false);
                 if (grid.flippedEquals(num)){    //if they're matching keep num displayed and set flipped as false
                     gameCounter++;
                     grid.flip(num); 
                     grid.flip(grid.getFlipped());
                     
                     //check if game is over
                     if(gameCounter==grid.getSize()/2){
                         grid.isOver=true;
                         
                         JOptionPane popup = new JOptionPane("Good Job!");
                         JOptionPane.showMessageDialog(popup,
 						      "-~*´¨¯¨`*·~-.¸-  You won!!  -,.-~*´¨¯¨`*·~-", "Good Job!",1);
                     } 
                 } else {
 			// start flip back timer here.
 			int delay = 1000;
 			ActionListener listener = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 				    Class classs = this.getClass();
 				    Icon imgBlank = new ImageIcon(classs.getResource("/images/000.jpg"));
				    JButton jb = buttons[grid.getFlipped()];
				    jb.setEnabled(true);
				    jb.setIcon(imgBlank);
				    grid.flip(grid.getFlipped());
				    jb = buttons[grid.getFlipped()];
				    jb.setEnabled(true);
				    jb.setIcon(imgBlank);
				    grid.flip(grid.getFlipped());
 				}
 			};
			new Timer(delay, listener).start();
 	       }
             }
         }
     }
 }

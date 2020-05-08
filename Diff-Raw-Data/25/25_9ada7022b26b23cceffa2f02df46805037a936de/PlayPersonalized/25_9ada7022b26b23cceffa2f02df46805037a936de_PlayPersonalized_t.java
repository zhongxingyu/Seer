	package dk.itu.mario.engine;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import dk.itu.mario.level.generator.bio.BioLevelGenerator;
 
 public class PlayPersonalized {
 
 	public static void main(String[] args)
      {
 		    	//JFrame frame = new JFrame("Mario Experience Showcase");
 		    	//MarioComponent mario = new MarioComponent(640, 480,true);
 
 		    	
 		    	int bioLoggerPort = 0;
 				
 		    	JFrame frame = new JFrame("Mario Experience Showcase");
 		    	//MarioComponent mario = new MarioComponent(640, 480,false);
 		    	MarioComponentRecording mario = new MarioComponentRecording(640, 480,true);
 		    	mario.isPersonalized = true;
		    	BioLevelGenerator.personalizedSCL = "trainingSamples_20121213_164044.arff.res";
 		    	
 		    	mario.biologgerPort = bioLoggerPort;
 		    	
 		    	frame.setContentPane(mario);
 		    	frame.setResizable(false);
 		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		        frame.pack();
 
 		        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
 
 		        frame.setVisible(true);
 
 		        mario.start();   
 	}	
 
 }

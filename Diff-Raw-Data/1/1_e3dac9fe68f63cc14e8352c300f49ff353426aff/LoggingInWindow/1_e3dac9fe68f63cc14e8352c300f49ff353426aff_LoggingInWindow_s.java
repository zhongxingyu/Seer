 package com.jpii.navalbattle.gui;
 
 import javax.swing.*;
 
 import com.jpii.navalbattle.data.Constants;
 
 @SuppressWarnings("serial")
 public class LoggingInWindow extends Window {
 	
 	private ImageChanger imageChanger;
 	
 	public LoggingInWindow() {
 		getContentPane().setLayout(null);
 		
 		JProgressBar progressBar = new JProgressBar();
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/jpii_title.png")));
 		imageChanger = new ImageChanger(label);
 		
 		progressBar.setBounds(0, 297, 485, 14);
 		label.setBounds(0, 0, 485, 311);
 		
 		progressBar.setIndeterminate(true);
 		
 		getContentPane().add(progressBar);
 		getContentPane().add(label);
 		
 		imageChanger.start();
 	}
 	
 	class ImageChanger extends Thread {
 		
 		private JLabel label;
 		
 	    public ImageChanger(JLabel label) {
 	        super("ImageChanger");
 	        this.label = label;
 	    }
 	    
 	    public void run() {
 	        sleep(Constants.SPLASH_DURATION);
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/roketgamer_title.png")));
 	        
 	        sleep(Constants.SPLASH_DURATION);
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/navalbattle_title.png")));
 	        
 	        sleep(Constants.SPLASH_DURATION);
 	        
 	        try
 			{
	        	nextWindow("MainMenuWindow");
 			}
 			catch (Exception ex) {
 			}
 	    }
 	    
 	    public void sleep(int millseconds) {
 	    	try {
 		    	long orig = System.currentTimeMillis();
 		    	while (orig + millseconds > System.currentTimeMillis()) { }
 	    	} catch (Exception e) {
 	    		
 	    	}
 	    }
 	}
 }

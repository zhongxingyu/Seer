 package org.ben.socialgraph;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.SwingUtilities;
 
 
 
 
 
 
 import org.gephi.preview.api.ProcessingTarget;
 
 import processing.core.PApplet;
 
 public class MainFrame  {
 	
 	private static  ProcessingTarget  target;
 	public static boolean RIGHT_TO_LEFT = false;
 	
 
 	public static void addComponentsToPane(Container pane) {
         
 //        if (!(pane.getLayout() instanceof BorderLayout)) {
 //            pane.add(new JLabel("Container doesn't use BorderLayout!"));
 //            return;
 //        }
 //         
 //        if (RIGHT_TO_LEFT) {
 //            pane.setComponentOrientation(
 //                    java.awt.ComponentOrientation.RIGHT_TO_LEFT);
 //        }
          
        
         //pane.add(button, BorderLayout.PAGE_START);
         //pane.add(button, BorderLayout.PAGE_END);
       //pane.add(button, BorderLayout.LINE_END);
         
         //button.setPreferredSize(new Dimension(200, 100));
         //pane.add(button, BorderLayout.CENTER);
          
         addGraphToContainer(pane);
         
         
         JButton buttonOUT = new JButton("Zoom Out");
         buttonOUT.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
             	target.zoomMinus();
             }
         });
         
         JButton buttonIN = new JButton("Zoom In");
         buttonIN.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
             	target.zoomPlus();
             }
         });
         
         pane.add(buttonOUT, BorderLayout.LINE_START);
         pane.add(buttonIN, BorderLayout.LINE_START);
         
     }
 	
 	
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event dispatch thread.
      */
     private static void createAndShowGUI() {
          
         //Create and set up the window.
         JFrame frame = new JFrame("tibbr Social Graph");
         //frame.setLayout(new FlowLayout());
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
         //Use the content pane's default BorderLayout. 
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
 	
 
 	private static void addGraphToContainer(Container pane)
 	{
 		// get the processing target, and then the applet 
 		RenderGraph tsb = new RenderGraph();
 		target = tsb.buildGraph();
 		PApplet applet = target.getApplet();
 		
 		
 		applet.setPreferredSize(new Dimension(800, 800));
 		applet.mousePressed();
 		
 		//add applet to frame
         pane.add(applet, BorderLayout.CENTER);
 		
 		applet.mousePressed();
 		
 		
 	}
 	
 	public static void main(String[] args) {
 
 		 //Schedule a job for the event dispatch thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
 			
 		
 	}
 
 	
 	
 }

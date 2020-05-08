 package HW4;
 /**
  * This should be just a separate thread for displaying items in the
  * kitchen and marking them as complete.
  */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.lang.management.ManagementFactory;
 
 import javax.swing.*;
 
 import org.apache.tools.ant.DirectoryScanner;
 
 
 public class KitchenDisplay{
 
 	/**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
     private static void createAndShowGUI(final String[] args) {
 		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
 		Color background = new Color(0,0,0) ;
 		
         //Create and set up the window.
         JFrame frame = new JFrame("PIZZA_STORE_NAME");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JDesktopPane desktop = new JDesktopPane();
 		frame.setContentPane(desktop) ;
 		desktop.setBackground(background) ;
 		frame.setMinimumSize(size) ;
 		desktop.setLayout(new GridBagLayout()); // this is the framing structure
 		GridBagConstraints gBC = new GridBagConstraints();
 		gBC.fill = GridBagConstraints.HORIZONTAL;
 		gBC.weightx = .75 ;
 		gBC.gridx = 0 ;
 		gBC.gridy = 0 ;
 		JLabel heading1 = new JLabel("<html><h1>ORDER</h1><hr></html>") ;
 		JLabel heading2 = new JLabel("<html><h1>COMPLETE?</h1><hr></html>") ;
 		heading1.setForeground(Color.white) ;
 		heading2.setForeground(Color.white) ; 
 		
 		desktop.add(heading1, gBC) ;
 		gBC.gridx = 1 ;
 		gBC.weightx = .25 ;
 		desktop.add(heading2, gBC) ;
 		gBC.gridx = 0 ;
 		gBC.weightx = .75 ;
 
 		//////// adding all the orders to the screen /////////
 		DirectoryScanner scanner = new DirectoryScanner() ;
 		scanner.setIncludes(new String[]{"**/*.POS"});
 		scanner.setBasedir(".");
 		scanner.setCaseSensitive(false);
 		scanner.scan();
 		final String[] files = scanner.getIncludedFiles();
 		for(int i=0; i< files.length; i++) {
 			BufferedReader in;
 			String order = "<html><h2>" ;
 			try {
 				in = new BufferedReader(new FileReader(files[i]));
 				order += in.readLine() + "</h2><table>" ;
 				String line = in.readLine() ;
 				int lineNum = 1 ;
 				while(line != null){
 					if(lineNum != 1) {
 						order += "<tr>" ;
 						if (lineNum == 2)
 							order += "Phone: " ;
 						order += line + "</tr>" ;
 					}
 					lineNum++ ;
 					line = in.readLine() ;
 				}
 				order += "</table></html>" ;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			JLabel o = new JLabel(order) ;
 			o.setForeground(Color.white) ;
 			gBC.gridy++ ;
 			desktop.add(o, gBC) ;
 		}
 		/////////////////////////////////////////////////////////
 		
 		///// adding buttons for marking orders as complete ////
 		gBC.gridy = 0 ;
 		gBC.gridx = 1 ;
 		gBC.weightx = .25 ;
 		for (int i=0; i<files.length; i++){
 			JButton done = new JButton("Done") ;
 			done.setPreferredSize(new Dimension(100, 75));
 			final int k = i ;
 			done.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e){
 					// on button press...
 					File f = new File(files[k]) ;
 				    boolean success = f.delete();
 				    if (!success)
 				        throw new IllegalArgumentException("Delete: deletion failed");
 				    else
 				    	restartProgram(args) ;
 				}
 			}) ;
 			
 			gBC.gridy++ ;
 			desktop.add(done, gBC) ;
 		}
 		////////////////////////////////////////////////////////
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
     public static void restartProgram(String[] args)
 	{
 		StringBuilder cmd = new StringBuilder();
         cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
         for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
             cmd.append(jvmArg + " ");
         }
         cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
         cmd.append(KitchenDisplay.class.getName()).append(" ");
         for (String arg : args) {
             cmd.append(arg).append(" ");
         }
         try {
 			Runtime.getRuntime().exec(cmd.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         System.exit(0);
 	}
     
     
 	public static void main(final String[] args) {
 		//Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI(args);
             }
         });
 	}
 
 }

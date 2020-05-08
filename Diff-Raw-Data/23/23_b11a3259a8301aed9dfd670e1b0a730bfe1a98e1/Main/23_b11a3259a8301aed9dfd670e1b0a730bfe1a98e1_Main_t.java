 /*
  * Created on Mar 29, 2006
  *
  */
 package org.reprap;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFrame;
import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 public class Main {
 
 	public Main() {
 	}
 
 	private void createAndShowGUI() {
         JFrame.setDefaultLookAndFeelDecorated(false);
         JFrame frame = new JFrame("RepRap");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         JMenuBar menubar = new JMenuBar();
         JMenu fileMenu = new JMenu("File");
         fileMenu.setMnemonic(KeyEvent.VK_F);
         menubar.add(fileMenu);
         
         JMenuItem fileOpen = new JMenuItem("Open", KeyEvent.VK_O);
         fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
         fileOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null, "Open not implemented yet");
 			}});
         fileMenu.add(fileOpen);
         
         fileMenu.addSeparator();
 
         JMenuItem fileExit = new JMenuItem("Exit", KeyEvent.VK_X);
         fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
         fileExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}});
         fileMenu.add(fileExit);
         
         JMenu toolsMenu = new JMenu("Tools");
         toolsMenu.setMnemonic(KeyEvent.VK_T);
         menubar.add(toolsMenu);
         
         JMenuItem toolsExerciser = new JMenuItem("Stepper exerciser", KeyEvent.VK_S);
         toolsExerciser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
           		  org.reprap.steppertestgui.Main gui = new org.reprap.steppertestgui.Main();
                   gui.createAndShowGUI(false);
 				}
               	catch (Exception ex) {
              		JOptionPane.showMessageDialog(null, "General exception: " + ex);
          			ex.printStackTrace();
              	}
 			}});
         toolsMenu.add(toolsExerciser);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.ipadx = 50;
        c.ipady = 50;
        panel.add(new JLabel("3D progress window will go here"), c);
         
         frame.getContentPane().add(panel);


        
         frame.setJMenuBar(menubar);
         

        
         frame.pack();
         frame.setVisible(true);
 
 	}
 	
 	public static void main(String[] args) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
 	            	try {
 		        		Main gui = new Main();
 		            gui.createAndShowGUI();
 	            	}
 	            	catch (Exception ex) {
 	            		JOptionPane.showMessageDialog(null, "General exception: " + ex);
 	        			ex.printStackTrace();
 	            	}
             }
         });
 
 	}
 
 }

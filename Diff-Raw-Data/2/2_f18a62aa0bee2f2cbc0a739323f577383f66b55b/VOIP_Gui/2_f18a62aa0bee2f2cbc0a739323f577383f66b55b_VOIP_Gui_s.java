 package Gui_Box;
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 
 /**
  * 
  * Gui_Box/VOIP_Gui.java
  * 
  * @author(s)	: Ian Middleton, Zach Ogle, Matthew J Swann
  * @version  	: 1.0
  * Last Update	: 2013-02-18
  * Update By	: Matthew J Swann
  * 
  * 
  */
 
 public class VOIP_Gui extends JFrame{
 
 	// Variables
 	private JButton button_one, button_two, button_three;
 	
 	private JLabel label_one, label_two, label_three;
 	
 	private JPanel panel_one, panel_two, primary;
     
 	private JTextArea text_area_one;
 	
 	private JTextField text_field_one, text_field_two;
 	
 	private TransmitListener TransmitListener;
 	
 	/**
      * Constructor.
      */
 	public VOIP_Gui() throws IOException{
 		
 		// JButtons
 		this.button_one = new JButton("Button_One");
 		this.button_one.setFont(new Font("Courier", Font.BOLD, 12));
 		this.button_one.setPreferredSize(new Dimension(150,50));
 		TransmitListener = new TransmitListener();
 		this.button_one.addActionListener(TransmitListener);
 		
 		
 		this.button_two = new JButton("Button_Two");
 		TransmitListener = new TransmitListener();
 		this.button_two.addActionListener(TransmitListener);
 		
 		this.button_three = new JButton("Button_Three");
 		this.button_three.setFont(new Font("Courier", Font.BOLD, 12));
 		
 		
 		// JLabels
 		this.label_one   = new JLabel("Label_One");
 		this.label_two   = new JLabel("Label_Two");
 		this.label_three = new JLabel("Label_Three");
 	
 	
 		// JPanels
 		this.panel_one = new JPanel();
 		this.panel_two = new JPanel();
 		this.primary   = new JPanel();
 		
 		
 		// JTextArea
 		this.text_area_one = new JTextArea();
 		
 		
 		// JTextField
 		this.text_field_one = new JTextField(15);
 		this.text_field_two = new JTextField(15);
 		
 		
 		
 		// Control Block
 		this.compile_components();
 	}
 	
 	
     /**
      * Places components on the appropriate panels.
      */	
 	public void compile_components(){
 		
 		this.getContentPane().add(primary);
 		
 		//this.update_area(); IF NECESSARY RE FRESH
 		
 		// Panel One additions
 		this.panel_one.add(this.button_one);
 		this.panel_one.add(this.label_one);
 		
 		// Panel Two additions
 		this.panel_two.add(this.button_two);
 		this.panel_two.add(this.button_three);
 		
 		this.panel_two.add(this.label_two);
 		this.panel_two.add(this.label_three);
 		
 		
 		// Primary Panel additions
 		this.primary.setPreferredSize(new Dimension(4*100,4*100));
 		this.primary.add(this.panel_one);
 		this.primary.add(this.panel_two);
 	}
 	
 	/**
 	 * Updates the UI. May not be necessary
 	 */
 	public void update_area(){
 		
 		// re-establish variables
 		
 		// refresh UI
 	}
 	
 	/**
 	 * Action listener tied to the ____________ button.
 	 */
 	private class TransmitListener implements ActionListener{
 		
 		public void actionPerformed(ActionEvent event){
 			
 			// DO SOMETHING
 			
 			// primary.removeAll();
 			// MUST HAVE ^^ TO REFRESH!!!
 			
 			// re-compile UI components if UI refresh is needed
 			
 			// primary.updateUI();
 		}
 	}
 		
 	
 		/**
 		 * Main run.
 		 * @param args
 		 * @throws IOException
 		 */
 	    public static void main(String[] args) throws IOException {
 			VOIP_Gui teh_gui = new VOIP_Gui();
 			
 			teh_gui.setTitle("VOIP to end all VOIP's");
 			teh_gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			
 			teh_gui.pack();
 			teh_gui.setVisible(true);
 		}
 		
 	}
 	
 

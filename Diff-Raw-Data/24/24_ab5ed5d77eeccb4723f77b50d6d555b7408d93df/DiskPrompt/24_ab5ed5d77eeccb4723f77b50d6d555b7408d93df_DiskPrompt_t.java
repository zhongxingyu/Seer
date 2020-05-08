 package edu.ucsb.cs56.projects.games.towers_of_hanoi.utility;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 
 public class DiskPrompt extends JFrame{
 
 	private static String diskInput;
 	private static JTextField txt = new JTextField("Number of Disks");
 	
 	public DiskPrompt(){
 		super("How many disks?");
 		
 		JButton doneButton = new JButton("Done");
 		doneButton.addActionListener(new doneButtonListener());
 		this.add(txt,BorderLayout.NORTH);
 		this.add(doneButton,BorderLayout.SOUTH);
 		this.setPreferredSize(new Dimension(300, 100));
 		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setVisible(true);
 	}
 	
 	
 	
 	public String getString(){
 		return diskInput;
 	}
 	
 	private class doneButtonListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
             diskInput = txt.getText();
             System.out.println(diskInput);
             //txt.setText("");
            //System.out.println(diskInput);  
 			
 		}
 		
 	}
 	
 }

 package edu.mharper.tp2;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class SettingsView implements ActionListener {
 
 	JFrame frame;
 	
 	JTextField rowField, colField;
 	JButton rowButton, colButton;
 	
 	public SettingsView() {
 		initWindow();
 	}
 	
 	private void initWindow() {
 		frame = new JFrame("Settings");
 		frame.setLayout(new BorderLayout());
 		
 		JPanel rowPanel = new JPanel(new FlowLayout());
 		rowButton = new JButton("Set number of rows");
		rowButton.setPreferredSize(new Dimension(175, 35));
 		
 		rowField = new JTextField("" + Main.verticalSpaces);
		rowField.setPreferredSize(new Dimension(175, 25)); 
 		
 		rowButton.addActionListener(this);
 		rowPanel.add(rowField);
 		rowPanel.add(rowButton);
 		rowPanel.setPreferredSize(new Dimension(400, 40));
 		
 		JPanel colPanel = new JPanel(new FlowLayout());
 		colButton = new JButton("Set number of columns");
		colButton.setPreferredSize(new Dimension(175, 35));
 		
 		colField = new JTextField("" + Main.horizontalSpaces); 
		colField.setPreferredSize(new Dimension(175, 25));
 		
 		colButton.addActionListener(this);
 		colPanel.add(colField);
 		colPanel.add(colButton);
 		
 		
 		frame.add(rowPanel, BorderLayout.NORTH);
 		frame.add(colPanel, BorderLayout.SOUTH);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getActionCommand().equalsIgnoreCase("Set number of rows")) {
 			System.out.println("Rows");
 			Main.verticalSpaces = Integer.parseInt(rowField.getText());
 		}
 		else if (e.getActionCommand().equalsIgnoreCase("Set number of columns")) {
 			Main.horizontalSpaces = Integer.parseInt(colField.getText());
 		}
 		
 		// Trigger new game
 	}
 	
 }

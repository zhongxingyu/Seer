 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class BankOpenPanel extends JPanel
 {
 	private JButton okay, cancel;
 	private JLabel label;
 	private JPanel buttonPanel;
 	
 	
 	public BankOpenPanel ()
 	{
 	okay = new JButton ("Okay");
	canel = new JButton ("Canel");
 	
	ButtonListener = new ButtonListener():
 	okay.addActionListener (listener);
 	cancel.addActionListener (listener);
 	
 	label = new JLabel ("Click \"Okay\" to continue");
 	
	nuttonPanel = new JPanel();
 	buttonPanel.setPreferredSize (new Dimension (200, 40));
	buttonPanel.setBackground (Color.tellow);
 	buttonPanel.add (okay);
 	buttonPanel.add (cancel);
 	
	setPrederredSize (new Dimension(200,80));
	setBackgroun (Color.green);
 	add (label);
 	add (buttonPanel);
 	}
 }

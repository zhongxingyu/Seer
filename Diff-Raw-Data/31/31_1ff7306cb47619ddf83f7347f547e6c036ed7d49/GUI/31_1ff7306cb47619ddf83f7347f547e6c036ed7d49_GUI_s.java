 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class GUI extends JFrame
 {
  private static final int H = 700; // Height of window
 	private static final int W = 600; // Width of window
 	private static final int TM = 10; // Top margin
 
 	private static final int MH = H / 2; // Mid Height
 	private static final int HSIZE = MH - 80; // Height of sub window
 	private static final String NAME1 = "Encrypt";
 	private static final String NAME2 = "Decrypt";
 	
 	private JLabel label1 = new JLabel("Enter text to encrypt");
 	private JLabel label2 = new JLabel("Enter key");
 	
 	private JLabel label3 = new JLabel("Enter encrypted text");
 	private JLabel label4 = new JLabel("Enter key");
 	
 	private JTextArea theInput1 = new JTextArea(); // Visual Comp.
 	private JTextArea theKey1 = new JTextArea();
 	
 	private JTextArea theInput2 = new JTextArea(); // Visual Comp.
 	private JTextArea theKey2 = new JTextArea();
 	
 	private JEditorPane theOutput1 = new JEditorPane(); // Visual Comp.
 	private JButton theButton1 = new JButton(NAME1); // Visual Comp.
 	private Transaction1 theFO1 = new Transaction1(); //
 	
 	private JEditorPane theOutput2 = new JEditorPane(); // Visual Comp.
 	private JButton theButton2 = new JButton(NAME2); // Visual Comp.
 	private Transaction2 theFO2 = new Transaction2(); //
 	
 	private OTPDecryptor dec = new OTPDecryptor();
 	private OTPEncryptor enc = new OTPEncryptor();
 
 	public GUI()
 	{
 		Container cp = getContentPane(); // Content pane
 		cp.setLayout(null); // No layout manager
 		setSize(W, H); // Size of Window
		setTitle("Encryptor/Decryptor 0.1 by Almas");
 		Font font = new Font("Monospaced", Font.PLAIN, 12); // Font Used
 		
 		label1.setBounds(10, TM, 150, 20);
 		label1.setFont(font);
 		cp.add(label1);
 		
 		theInput1.setBounds(10, TM + 30, 300 - 40, HSIZE);// Input Area Size
 		theInput1.setFont(font); // Font
 		theInput1.setLineWrap(true);
 		cp.add(theInput1); // Add to c. pane
 		
 		label2.setBounds(10, TM + 30 + HSIZE, 100, 20);
 		label2.setFont(font);
 		cp.add(label2);
 		
 		theKey1.setBounds(10, TM + 30 + HSIZE + 20, 300 - 40, 20);// Input Area Size
 		theKey1.setFont(font); // Font
 		cp.add(theKey1); // Add to c. pane
 		
 		theButton1.setBounds(10, TM + 30 + HSIZE + 20 + 30, 300 - 40, 40);
 		theButton1.addActionListener(theFO1);
 		cp.add(theButton1);
 
 		JScrollPane theSP = new JScrollPane(); // Scrolling window
 		theSP.setBounds(10, TM + 30 + HSIZE + 20 + 50 + 40, 300 - 40, HSIZE - 40); // Size of window
 		theSP.setFont(font); // Font
 		
 		font = new Font("Monospaced", Font.PLAIN, 20); // Font Used
 
 		cp.add(theSP); // Add to c. pane
 		theOutput1.setFont(font);
 		theSP.getViewport().add(theOutput1); // Add output area
 		
 		
 		//DECRYPTION
 		
 		font = new Font("Monospaced", Font.PLAIN, 12); // Font Used
 		
 		label3.setBounds(300, TM, 150, 20);
 		label3.setFont(font);
 		cp.add(label3);
 		
 		theInput2.setBounds(300, TM + 30, 300 - 40, HSIZE);// Input Area Size
 		theInput2.setFont(font); // Font
 		theInput2.setLineWrap(true);
 		cp.add(theInput2); // Add to c. pane
 		
 		label4.setBounds(300, TM + 30 + HSIZE, 100, 20);
 		label4.setFont(font);
 		cp.add(label4);
 		
 		theKey2.setBounds(300, TM + 30 + HSIZE + 20, 300 - 40, 20);// Input Area Size
 		theKey2.setFont(font); // Font
 		cp.add(theKey2); // Add to c. pane
 		
 		theButton2.setBounds(300, TM + 30 + HSIZE + 20 + 30, 300 - 40, 40);
 		theButton2.addActionListener(theFO2);
 		cp.add(theButton2);
 
 		JScrollPane theSP2 = new JScrollPane(); // Scrolling window
 		theSP2.setBounds(300, TM + 30 + HSIZE + 20 + 50 + 40, 300 - 40, HSIZE - 40); // Size of window
 		theSP2.setFont(font); // Font
 		
 		font = new Font("Monospaced", Font.PLAIN, 20); // Font Used
 
 		cp.add(theSP2); // Add to c. pane
 		theOutput2.setFont(font);
 		theSP2.getViewport().add(theOutput2); // Add output area
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);// Exit on close
 	}
 
 	// Inner class (Instance of which is a function object)
 	class Transaction1 implements ActionListener
 	{
 		// Called on button press
 		public void actionPerformed(ActionEvent e)
 		{
 			String text = theInput1.getText(); // User input
 			String key = theKey1.getText();
 			
			if (enc.validate(text) && enc.validate(key))
				theOutput1.setText(enc.process(text, key));
 			else
				theOutput1.setText("Your text or key contains not yet supported characters!");
 		}
 	}
 	
 	class Transaction2 implements ActionListener
 	{
 		// Called on button press
 		public void actionPerformed(ActionEvent e)
 		{
 			String text = theInput2.getText(); // User input
 			String key = theKey2.getText();
 			
			if (dec.validate(text) && dec.validate(key))
				theOutput1.setText(dec.process(text, key));
 			else
				theOutput1.setText("Your text or key contains not yet supported characters!");
 		}
 	}
 }

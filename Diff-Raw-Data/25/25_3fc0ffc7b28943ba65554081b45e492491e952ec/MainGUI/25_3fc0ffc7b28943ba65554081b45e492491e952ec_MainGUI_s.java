 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 import java.awt.event.KeyListener;
import java.util.Scanner;

 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import main.*;
 
 public class MainGUI{
 
 	private JFrame frame;
 	private Font messageFont;
 	private KeyListener reader = new InputReader(this);
 	public MainGUI() {
 
 		messageFont = new Font("SansSerif", Font.BOLD, 42);
 		
 		frame = new JFrame();
 		frame.setSize(800, 700);
 		frame.setLayout(new GridBagLayout());
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JLabel message = new JLabel("Please swipe your Blastercard");
 		message.setFont(messageFont);
 
 		JPanel centerPanel = new JPanel(new BorderLayout());
 		centerPanel.add(message);
 
 		frame.add(centerPanel);
 		frame.setVisible(true);
 
 		boolean bool = false;
 		if ( bool ) {		
 			frame = new HomeScreen();
 		}
 		
 		frame.addKeyListener(reader);
 		frame.setFocusable(true);
 	}
 
 	public void handleInput() {
 		
 		String input = ((InputReader) reader).getInput();
 
 		try {
 			((InputReader) reader).strip();
 			if ( !((InputReader) reader).getCWID().equals("") ) {
 				frame.setVisible(false);
 				frame = new HomeScreen();
 				frame.setVisible(true);
 			}
 		} catch (InputReaderException e) {
 			String message = e.getMessage();
 			JOptionPane.showMessageDialog(frame, message);
 		}
 	}
 	
 	public void enterPressed() {
 		handleInput();
 	}
 
 	public static void main(String[] args) {
		MainGUI m = new MainGUI();
		
 	}
 
 }

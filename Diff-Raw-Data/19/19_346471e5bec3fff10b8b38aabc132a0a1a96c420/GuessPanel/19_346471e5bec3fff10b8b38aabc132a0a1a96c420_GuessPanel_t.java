 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
 
 import client.CommunicationMonitor;
 
 public class GuessPanel extends JPanel {
 	
 	private static JTextField textField = new JTextField(10);
 	private static JTextPane textPane = new JTextPane();
 	private static GuessButton gb = null;
 	
 	public GuessPanel(CommunicationMonitor cm) {
 	
 		JPanel bottom = new JPanel();
 		bottom.setLayout(new BorderLayout());
 		gb = new GuessButton(textField, cm);
 		
 		textField.addActionListener(gb);
 		
 		bottom.add(gb, BorderLayout.EAST);
 
 		bottom.add(textField, BorderLayout.CENTER);
 		
 		setLayout(new BorderLayout());
 
 		textPane.setPreferredSize(new Dimension(4, 40));
 		textPane.setEditable(false);
 		JScrollPane jsp = new JScrollPane(textPane);
 		jsp.setPreferredSize(new Dimension(100, 70));
 
 		add(jsp, BorderLayout.CENTER);
 		add(bottom, BorderLayout.SOUTH);
 	}
 	
 	public static void disableGuessing() {
 		gb.setEnabled(false);
 		textField.setText("");
 		textField.setEnabled(false);
 	}
 	
 	public static void enableGuessing() {
 		gb.setEnabled(true);
 		textField.setEnabled(true);
 	}
 	
 	
 	
 	public static void output(String text) {
 	     outputColored(text + "\n", Color.BLACK);
 	}
 	
 	public static void outputRed(String text) {
 		outputColored(text + "\n", Color.RED);
 	}
 	
 	public static void outputGreen(String text) {
 		outputColored(text + "\n", Color.GREEN);
 	}
 	
 	private static void outputColored(String text, Color c) {
 		StyleContext sc = StyleContext.getDefaultStyleContext();
         AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
 
         aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
         aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
 
        StyledDocument doc = textPane.getStyledDocument();
        try {
			doc.insertString(doc.getLength(), text, aset);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         int len = textPane.getDocument().getLength();
         textPane.setCaretPosition(len);
         textPane.setCharacterAttributes(aset, false);
        
       
 	}
 	
 	
 }

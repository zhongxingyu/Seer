 package gui.brainfuck;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.util.Observable;
 import java.util.Observer;
 
 import gui.MachineEditor;
 
 import javax.swing.JTextPane;
 import javax.swing.JScrollPane;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 import machine.Simulation;
 import machine.brainfuck.BrainfuckSimulation;
 
 /**
  * Represents an editor for brainfuck files.
  * @author Sven Schuster
  * 
  */
 public class BrainfuckEditor extends MachineEditor implements Observer {
 	private static final long serialVersionUID = -6379014025769077968L;
 	
 	private JTextPane codeArea;
 	private JScrollPane codePane;
 	private DefaultStyledDocument doc;
 
 	/**
 	 * Creates a new BrainfuckEditor.
 	 */
 	public BrainfuckEditor() {
 		doc = new DefaultStyledDocument();
 		codeArea = new JTextPane(doc);
 		codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
 
 		codePane = new JScrollPane(codeArea);
 		codePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		setLayout(new BorderLayout());
 		add(codePane, BorderLayout.CENTER);
 	}
 	
 	/**
 	 * Set code of editor's editorpane.
 	 * @param code Code
 	 */
 	public void setCode(String code) {
 		this.codeArea.setText(code);
 	}
 	
 	/**
 	 * Returns code of editor's editorpane.
 	 * @return code
 	 */
 	public String getCode() {
 		return this.codeArea.getText();
 	}
 	
 	public DefaultStyledDocument getDocument() {
 		return this.doc;
 	}
 	
 	public void setHighlight(int position) {
 		SimpleAttributeSet attributes = new SimpleAttributeSet();
 		StyleConstants.setForeground(attributes, Color.RED);
 		doc.setCharacterAttributes(position, 1, attributes, false);
 	}
 	
 	public void resetHighlight() {
 		SimpleAttributeSet attributes = new SimpleAttributeSet();
 		StyleConstants.setForeground(attributes, Color.BLACK);
 		doc.setCharacterAttributes(0, doc.getLength()-1, attributes, false);
 	}
 	
 	@Override
 	public void update(Observable obs, Object obj) {
 		if(obs instanceof BrainfuckSimulation && obj instanceof Integer) {
 			resetHighlight();
			setHighlight((int) obj);
 		}
 		else if(obj instanceof Simulation.simulationState) {
 			resetHighlight();
 		}
 	}
 }

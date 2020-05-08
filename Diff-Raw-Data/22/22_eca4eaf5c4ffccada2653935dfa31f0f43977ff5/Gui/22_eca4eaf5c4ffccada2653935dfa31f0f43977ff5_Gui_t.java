 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import members.Time;
 import sort.Formater;
 
 @SuppressWarnings("serial")
 public class Gui extends JFrame {
 
 	// This number +1 is the number of lines of text kept in memory.
 	private static final int maxNrOfEntriesShown = 3;
 
 	private JPanel controlNorthPanel;
 	private JScrollPane textCenterPanel;
 	private JTextArea textArea;
 	private JTextField textField;
 
 	private Font bigFont;
 	private GuiPrinter printer;
 	private Dimension screenSize;
 
 	/**
 	 * A simple frame for entering times of racers.
 	 * 
 	 * @param output
 	 *            The file to write the entries to.
 	 */
 	public Gui(String output) {
 
 		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		bigFont = new Font("Times New Roman", Font.BOLD, screenSize.height / 7);
 
 		setLayout(new BorderLayout());
 		controlNorthPanelSetUp();
 		textCentralPanelSetUp();
 		setTitle("ENDURO");
 
 		printer = new GuiPrinter(output);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 		setVisible(true);
 		pack();
		// if (!new File(output).exists()) {
		// Object[] options = { "Startstation", "Slutstation" };
		// int choice = JOptionPane.showOptionDialog(this,
		// "Startstation eller slutstation?", "Stationsval",
		// JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		// null, options, options[0]);
		// printer.writeLine(Formater.formatColumns(Formater.START_NR,
		// choice == 0 ? Formater.START_TIME : Formater.FINISH_TIME));
		// // setTitle("ENDURO - " + options[choice]);
		// } else {
		// }
 
 		setSize(screenSize);
 	}
 
 	/**
 	 * Sets up the upper bar and its formatting.
 	 */
 	private void controlNorthPanelSetUp() {
 
 		// Format dimensions
 		int textFieldLength = (screenSize.width / 3) / bigFont.getSize();
 		Dimension buttonDimension = new Dimension(screenSize.width / 3,
 				screenSize.height / 7);
 
 		// Add the Panel
 		controlNorthPanel = new JPanel();
 		add(controlNorthPanel, BorderLayout.NORTH);
 
 		// Create text field
 		textField = new JTextField(textFieldLength);
 		textField.setFont(bigFont);
 		addRespondToKey();
 		controlNorthPanel.add(textField, BorderLayout.WEST);
 
 		// Create the button (separate class)
 		controlNorthPanel.add(new RegisterButton(this, buttonDimension),
 				BorderLayout.EAST);
 	}
 
 	/**
 	 * Makes the enter key have the same function as the button.
 	 */
 	private void addRespondToKey() {
 		textField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent evt) {
 				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
 					register();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Sets up the central text field showing recent entries.
 	 */
 	private void textCentralPanelSetUp() {
 
 		// Format and create text area.
 		this.textArea = new JTextArea(6, 12);
 		textArea.setFont(bigFont);
 		textArea.setFocusable(false);
 		textArea.setEditable(false);
 
 		// Add the panel
 		textCenterPanel = new JScrollPane(textArea);
 		add(textCenterPanel, BorderLayout.CENTER);
 
 	}
 
 	/**
 	 * Tells the program to read the textField and write to file etc.
 	 */
 	public void register() {
 		// Read and then flush the textField
 		String comNr = textField.getText();
 		textField.setText("");
 
 		// Format the new entry.
 		Time t = Time.fromCurrentTime();
 		String temp = Formater.formatColumns(comNr, t);
 
 		// Print to file
 		printer.writeLine(temp);
 
 		// Add the entry to the top of the recent list.
 		String[] temprows = textArea.getText().split("\\n");
 		for (int i = 0; i < temprows.length && i < maxNrOfEntriesShown; i++) {
 			temp = temp + "\n" + temprows[i];
 		}
 		textArea.setText(temp);
 
 		// Finishing updates
 		textField.requestFocus();
 		textArea.invalidate();
 		repaint();
 	}
 
 }

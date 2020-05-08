 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 /**
  * A graphical user interface for the calculator. No calculation is being done
  * here. This class is responsible just for putting up the display on screen. It
  * then refers to the "CalcEngine" to do all the real work.
  * 
  * @author David J. Barnes and Michael Kolling
  * @version 2008.03.30
  */
 public class UserInterface extends JFrame implements ActionListener {
 	private CalcEngine calc;
 	private boolean showingAuthor;
 
 	// private JFrame frame;
 	private JTextField display;
 	private JTextField stringDisplay;
 	private JLabel status;
 
 	/**
 	 * Create a user interface.
 	 * 
 	 * @param engine
 	 *            The calculator engine.
 	 */
 	public UserInterface(CalcEngine engine) {
 		calc = engine;
 		showingAuthor = true;
 		makeFrame();
 		this.setVisible(true);
 	}
 
 	/**
 	 * Make the frame for the user interface.
 	 */
 	private void makeFrame() {
 		// frame = new JFrame(calc.getTitle());
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel contentPane = (JPanel) this.getContentPane();
 		JPanel numberPane = new JPanel();
 		numberPane.setLayout(new BorderLayout());
 		contentPane.setLayout(new BorderLayout(8, 8));
 		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		display = new JTextField();
 		stringDisplay = new JTextField();
 		contentPane.add(stringDisplay, BorderLayout.NORTH);
 		contentPane.add(numberPane, BorderLayout.CENTER);
 		numberPane.add(display, BorderLayout.NORTH);
 
 		JPanel buttonPanelContainer = new JPanel(new BorderLayout());
 
 		JPanel buttonPanel = new JPanel(new GridLayout(3, 6));
 		addButton(buttonPanel, "∩");
 		addButton(buttonPanel, "∪");
 		addButton(buttonPanel, "\\");
 		addButton(buttonPanel, "PUSH");
 		addButton(buttonPanel, ",");
 		addButton(buttonPanel, "=");
 
 		addButton(buttonPanel, "9");
 		addButton(buttonPanel, "8");
 		addButton(buttonPanel, "7");
 		addButton(buttonPanel, "6");
 		addButton(buttonPanel, "5");
 //		addButton(buttonPanel, "=");
 
 		addButton(buttonPanel, "Clear");
 		addButton(buttonPanel, "4");
 		addButton(buttonPanel, "3");
 		addButton(buttonPanel, "2");
 		addButton(buttonPanel, "1");
 		addButton(buttonPanel, "0");
 
 		buttonPanelContainer.add(buttonPanel, BorderLayout.NORTH);
 		numberPane.add(buttonPanelContainer, BorderLayout.CENTER);
 
 		status = new JLabel(calc.getAuthor());
 		contentPane.add(status, BorderLayout.SOUTH);
 
 		this.pack();
 	}
 
 	/**
 	 * Add a button to the button panel.
 	 * 
 	 * @param panel
 	 *            The panel to receive the button.
 	 * @param buttonText
 	 *            The text for the button.
 	 */
 	private void addButton(Container panel, String buttonText) {
 		JButton button = new JButton(buttonText);
 		button.addActionListener(this);
 		panel.add(button);
 	}
 
 	/**
 	 * An interface action has been performed. Find out what it was and handle
 	 * it.
 	 * 
 	 * @param event
 	 *            The event that has occured.
 	 */
 	public void actionPerformed(ActionEvent event) {
 		String command = event.getActionCommand();
 		if (command.equals("0") || command.equals("1") || command.equals("2")
 				|| command.equals("3") || command.equals("4")
 				|| command.equals("5") || command.equals("6")
 				|| command.equals("7") || command.equals("8")
 				|| command.equals("9")) {
 			calc.addToSet(Integer.parseInt(command));
 
 		} else if (command.equals("∩")) {
 			calc.buttonPressed(command);
 
 		} else if (command.equals("∪")) {
 			calc.buttonPressed(command);
 
 		} else if (command.equals("=")) {
 			calc.equals();
 
 		} else if (command.equals("Clear")) {
 			calc.clear();
 
 		} else if (command.equals("\\")) {
 			calc.buttonPressed(command);
 
 		} else if (command.equals(",")) {
 			calc.buttonPressed(command);
 
 		} else if (command.equals("PUSH")) {
 			int st = calc.numberSet();
 			JOptionPane.showMessageDialog(null,"Die Anzahl der Elemente beträgt: "+ st,"Eig. des aktuellen Sets",JOptionPane.PLAIN_MESSAGE);
 		}
 
 		// else unknown command.
 
 		redisplay();
 
 	}
 
 	/**
 	 * Update the interface display to show the current value of the calculator.
 	 */
 	private void redisplay() {
 		display.setText(calc.getDisplayValue().toString());
 		stringDisplay.setText(calc.getDisplayString());
 	}
 
 	/**
 	 * Toggle the info display in the calculator's status area between the
 	 * author and version information.
 	 */
 	// private void showInfo()
 	// {
 	// if(showingAuthor)
 	// status.setText(calc.getVersion());
 	// else
 	// status.setText(calc.getAuthor());
 	//
 	// showingAuthor = !showingAuthor;
 	// }
 }

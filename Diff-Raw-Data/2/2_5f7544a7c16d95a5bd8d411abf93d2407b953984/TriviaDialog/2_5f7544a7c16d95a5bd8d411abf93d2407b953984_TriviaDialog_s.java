 package net.bubbaland.trivia.client;
 
 import java.awt.Frame;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.WindowConstants;
 
 /**
  * Custom dialog that handles setting up a lot of the characteristics used by all trivia dialogs.
  * 
  * The TriviaDialog class handles the creation of the option pane for the dialog. It also handles the saving and loading
  * of position and size and makes the dialog resizable.
  * 
  * The constructor takes in the parent frame and title for the dialog box, followed by the usual arguments for
  * JOptionPane.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class TriviaDialog extends JDialog implements WindowListener, PropertyChangeListener {
 
 	private static final long	serialVersionUID	= 5954954270512670220L;
 
 	// The internal option pane
 	private final JOptionPane	optionPane;
 
 	/**
 	 * Create a TriviaDialog with no arguments for the JOptionPane
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 */
 	public TriviaDialog(Frame frame, String title) {
 		this(frame, title, new JOptionPane());
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane. This is generally used internally after the option pane
 	 * has been created.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param optionPane
 	 *            Option pane to use
 	 */
 	public TriviaDialog(Frame frame, String title, final JOptionPane optionPane) {
		super(frame, title, true);
 		this.optionPane = optionPane;
 		this.setName(title);
 		this.addWindowListener(this);
 
 		// Register an event handler that reacts to option pane state changes.
 		this.optionPane.addPropertyChangeListener(this);
 
 		this.setContentPane(this.optionPane);
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setResizable(true);
 		this.setModal(false);
 		this.setAlwaysOnTop(false);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param panel
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel) {
 		this(frame, title, new JOptionPane(panel));
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param message
 	 * @see JOptionPane
 	 * @param messageType
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel, int messageType) {
 		this(frame, title, new JOptionPane(panel, messageType));
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param message
 	 * @see JOptionPane
 	 * @param messageType
 	 * @see JOptionPane
 	 * @param optionType
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel, int messageType, int optionType) {
 		this(frame, title, new JOptionPane(panel, messageType, optionType));
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param message
 	 * @see JOptionPane
 	 * @param messageType
 	 * @see JOptionPane
 	 * @param optionType
 	 * @see JOptionPane
 	 * @param icon
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel, int messageType, int optionType, Icon icon) {
 		this(frame, title, new JOptionPane(panel, messageType, optionType, icon));
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param message
 	 * @see JOptionPane
 	 * @param messageType
 	 * @see JOptionPane
 	 * @param optionType
 	 * @see JOptionPane
 	 * @param icon
 	 * @see JOptionPane
 	 * @param options
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel, int messageType, int optionType, Icon icon,
 			Object[] options) {
 		this(frame, title, new JOptionPane(panel, messageType, optionType, icon, options));
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Create a TriviaDialog using the specified option pane arguments.
 	 * 
 	 * @param frame
 	 *            Parent frame for the dialog
 	 * @param title
 	 *            Title for the dialog
 	 * @param message
 	 * @see JOptionPane
 	 * @param messageType
 	 * @see JOptionPane
 	 * @param optionType
 	 * @see JOptionPane
 	 * @param icon
 	 * @see JOptionPane
 	 * @param options
 	 * @see JOptionPane
 	 * @param initialValue
 	 * @see JOptionPane
 	 */
 	public TriviaDialog(Frame frame, String title, TriviaDialogPanel panel, int messageType, int optionType, Icon icon,
 			Object[] options, Object initialValue) {
 		this.optionPane = new JOptionPane(panel, messageType, optionType, icon, options, initialValue);
 		this.addWindowListener(panel);
 	}
 
 	/**
 	 * Click the OK button on the option pane.
 	 */
 	public void clickOK() {
 		this.optionPane.setValue(JOptionPane.OK_OPTION);
 	}
 
 	/**
 	 * Save the position of the dialog before disposing.
 	 */
 	@Override
 	public void dispose() {
 		TriviaClient.savePosition(this);
 		super.dispose();
 	}
 
 	/**
 	 * Pass through the value of the option pane.
 	 * 
 	 * @return The value of the option pane
 	 */
 	public Object getValue() {
 		return this.optionPane.getValue();
 	}
 
 	/**
 	 * Detect when the state of the option pane has changed and close the dialog.
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		final String prop = e.getPropertyName();
 		if (this.isVisible() && ( e.getSource() == this.optionPane )
 				&& ( JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop) )) {
 			this.dispose();
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 	}
 
 	/**
 	 * Set the option pane value if the dialog is closed using the window decoration.
 	 */
 	@Override
 	public void windowClosing(WindowEvent e) {
 		this.optionPane.setValue(JOptionPane.CLOSED_OPTION);
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 		loadPosition();
 	}
 
 	/**
 	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
 	 * 
 	 */
 	protected void loadPosition() {
 		try {
 			final String frameID = this.getName();
 
 			final int x = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".X"));
 			final int y = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Y"));
 			final int width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Width"));
 			final int height = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Height"));
 
 			this.setBounds(x, y, width, height);
 
 		} catch (final NumberFormatException e) {
 			System.out.println("Couldn't load window position, may not exist yet.");
 			this.pack();
 			this.setLocationRelativeTo(null);
 		}
 	}
 
 }

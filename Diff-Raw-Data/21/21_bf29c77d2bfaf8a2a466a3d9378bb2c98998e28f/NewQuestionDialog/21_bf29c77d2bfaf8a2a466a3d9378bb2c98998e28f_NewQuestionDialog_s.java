 package net.bubbaland.trivia.client;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.event.WindowEvent;
 import java.rmi.RemoteException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerNumberModel;
 
 import net.bubbaland.trivia.Trivia;
 
 /**
  * Creates a prompt to enter new question data.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class NewQuestionDialog extends TriviaDialogPanel {
 
 	/** The Constant serialVersionUID. */
 	private static final long	serialVersionUID	= 8250659442772286086L;
 
 	private final TriviaClient	client;
 	private final int			nQuestions;
 	private final int			qNumberStart;
 	private final JSpinner		qNumberSpinner;
 	private final JSpinner		qValueSpinner;
 	private final JTextArea		qTextArea;
 
 	/**
 	 * Instantiates a new question entry window.
 	 * 
 	 * @param client
 	 *            The local trivia client
 	 * @param nQuestions
 	 *            the number of questions
 	 * @param qNumberStart
 	 *            the default question number
 	 */
 	public NewQuestionDialog(TriviaClient client, int nQuestions, int qNumberStart) {
 		this(client, nQuestions, qNumberStart, 10, "");
 	}
 
 	/**
 	 * Instantiates a new question entry window.
 	 * 
 	 * @param client
 	 *            The local trivia client
 	 * @param nQuestions
 	 *            the number of questions
 	 * @param qNumberStart
 	 *            the default question number
 	 * @param qValueStart
 	 *            the default question value
 	 */
 	public NewQuestionDialog(final TriviaClient client, int nQuestions, int qNumberStart, int qValueStart) {
 		this(client, nQuestions, qNumberStart, qValueStart, "");
 	}
 
 	/**
 	 * Instantiates a new question entry window.
 	 * 
 	 * @param client
 	 *            The local trivia client
 	 * @param nQuestions
 	 *            the number of questions
 	 * @param qNumberStart
 	 *            the default question number
 	 * @param qValueStart
 	 *            the default question value
 	 * @param qTextStart
 	 *            the initial question text
 	 */
 	public NewQuestionDialog(final TriviaClient client, int nQuestions, int qNumberStart, int qValueStart,
 			String qTextStart) {
 
 		super();
 
 		this.client = client;
 		this.nQuestions = nQuestions;
 		this.qNumberStart = qNumberStart;
 		// Set up layout constraints
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		// Create the question number spinner
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		JLabel label = new JLabel("Question Number: ");
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		this.qNumberSpinner = new JSpinner(new SpinnerNumberModel(qNumberStart, 1, nQuestions, 1));
 		qNumberSpinner.setFont(qNumberSpinner.getFont().deriveFont(fontSize));
 		this.addEnterOverride(qNumberSpinner);
 		this.add(qNumberSpinner, constraints);
 
 		// Create the question value spinner
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		label = new JLabel("Question Value: ");
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		this.qValueSpinner = new JSpinner(new SpinnerNumberModel(qValueStart, 10, 1000, 5));
 		qValueSpinner.setFont(qValueSpinner.getFont().deriveFont(fontSize));
 		this.addEnterOverride(qValueSpinner);
 		this.add(qValueSpinner, constraints);
 		( (JSpinner.NumberEditor) qValueSpinner.getEditor() ).getTextField().addAncestorListener(this);
 		( (JSpinner.NumberEditor) qValueSpinner.getEditor() ).getTextField().addFocusListener(this);
 
 		// Create input area for the question text
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		label = new JLabel("Question: ");
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, constraints);
 
 		constraints.gridx = 0;
 		constraints.gridy = 3;
 		constraints.gridwidth = 2;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.qTextArea = new JTextArea(qTextStart, 4, 50);
 		qTextArea.setLineWrap(true);
 		qTextArea.setWrapStyleWord(true);
 		qTextArea.setFont(qTextArea.getFont().deriveFont(textAreaFontSize));
 		// qTextArea.addAncestorListener(this);
 		this.addEnterOverride(qTextArea);
 
 		JScrollPane scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(0, 200));
 		this.add(scrollPane, constraints);
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 		constraints.gridwidth = 1;
 
 		// Display the dialog box
 		this.dialog = new TriviaDialog(null, "Open New Question", this, JOptionPane.PLAIN_MESSAGE,
 				JOptionPane.OK_CANCEL_OPTION);
 		this.dialog.setVisible(true);
 	}
 
 	@Override
 	public void windowClosed(WindowEvent event) {
 		super.windowClosed(event);
 		// If the OK button was pressed, open the question
 		final int option = ( (Integer) this.dialog.getValue() ).intValue();
 		if (option == JOptionPane.OK_OPTION) {
 			// Get the input data
 			final int qNumber = (int) qNumberSpinner.getValue();
 			final int qValue = (int) qValueSpinner.getValue();
 			String qText = qTextArea.getText();
 
 			// Get the current Trivia data object
 			final Trivia trivia = client.getTrivia();
 			final int currentRound = trivia.getCurrentRoundNumber();
 
 			if (qNumberStart != qNumber && trivia.isOpen(qNumberStart) && !trivia.beenOpen(currentRound, qNumber)) {
 
 				this.removeAll();
 
 				GridBagConstraints constraints = new GridBagConstraints();
 				constraints.fill = GridBagConstraints.BOTH;
 				constraints.anchor = GridBagConstraints.CENTER;
 
 				constraints.gridx = 0;
 				constraints.gridy = 0;
 				JLabel label = new JLabel("Change question number from " + qNumberStart + " to " + qNumber + "?");
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				this.dialog = new TriviaDialog(null, "Confirm Question Number Change " + qNumberStart + " to "
 						+ qNumber, this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
 				this.dialog.setVisible(true);
 
 				final int confirm = ( (Integer) this.dialog.getValue() ).intValue();
 				if (confirm != JOptionPane.OK_OPTION) {
 					new NewQuestionDialog(client, nQuestions, qNumberStart, qValue, qText);
 					return;
 				}
 
 				// Open the question on the server
 				int tryNumber = 0;
 				boolean success = false;
 				while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
 						&& success == false) {
 					tryNumber++;
 					try {
 						client.getServer().remapQuestion(qNumberStart, qNumber);
 						success = true;
 					} catch (final RemoteException e) {
 						client.log("Couldn't open question on server (try #" + tryNumber + ").");
 					}
 				}
 
 				if (!success) {
 					client.disconnected();
 					return;
 				}
 
 				client.log("Question #" + qNumberStart + " changed to " + qNumber);
 			}
 
 			if (trivia.beenOpen(currentRound, qNumber)) {
 				// If the question has already been open, confirm that we want to overwrite
 
 				// Get the existing question data
 				final int existingQValue = trivia.getValue(currentRound, qNumber);
 				final String existingQText = trivia.getQuestionText(currentRound, qNumber);
 
 				this.removeAll();
 
 				GridBagConstraints constraints = new GridBagConstraints();
 				constraints.fill = GridBagConstraints.BOTH;
 				constraints.anchor = GridBagConstraints.CENTER;
 
 				constraints.gridx = 0;
 				constraints.gridy = 0;
 				JLabel label = new JLabel("Question alread open, overwrite?");
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				// Show existing question data
 				constraints.gridx = 0;
 				constraints.gridy = 1;
 				label = new JLabel("Existing question:");
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				constraints.gridx = 0;
 				constraints.gridy = 2;
 				constraints.weightx = 0.5;
 				constraints.weighty = 0.5;
 				JTextArea textArea = new JTextArea(existingQText);
 				textArea.setLineWrap(true);
 				textArea.setWrapStyleWord(true);
 				textArea.setEditable(false);
 				JScrollPane scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 				scrollPane.setPreferredSize(new Dimension(100, 100));
 				scrollPane.setViewportView(textArea);
 				scrollPane.setBorder(BorderFactory.createEmptyBorder());
 				this.add(scrollPane, constraints);
 				constraints.weightx = 0.0;
 				constraints.weighty = 0.0;
 
 				constraints.gridx = 0;
 				constraints.gridy = 3;
 				label = new JLabel("Value: " + existingQValue);
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				// Show new question data
 				constraints.gridx = 0;
 				constraints.gridy = 4;
 				label = new JLabel("Entered question:");
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				constraints.gridx = 0;
 				constraints.gridy = 5;
 				constraints.weightx = 0.5;
 				constraints.weighty = 0.5;
 				textArea = new JTextArea(qText);
 				textArea.setLineWrap(true);
 				textArea.setWrapStyleWord(true);
 				textArea.setEditable(true);
 				textArea.addAncestorListener(this);
 				this.addEnterOverride(textArea);
 				scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 				scrollPane.setPreferredSize(new Dimension(100, 100));
 				scrollPane.setViewportView(textArea);
 				scrollPane.setBorder(BorderFactory.createEmptyBorder());
 				this.add(scrollPane, constraints);
 				constraints.weightx = 0.0;
 				constraints.weighty = 0.0;
 
 				constraints.gridx = 0;
 				constraints.gridy = 6;
 				label = new JLabel("Value: " + qValue);
 				label.setFont(label.getFont().deriveFont(fontSize));
 				this.add(label, constraints);
 
 				this.dialog = new TriviaDialog(null, "Confirm Question Overwrite " + qNumber, this,
 						JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
 				this.dialog.setVisible(true);
 
 				final int confirm = ( (Integer) this.dialog.getValue() ).intValue();
 				if (confirm != JOptionPane.OK_OPTION) {
 					new NewQuestionDialog(client, nQuestions, qNumber, qValue, qText);
 					return;
 				}
 
 				qText = textArea.getText();
 			}
 
 			// Open the question on the server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
 				tryNumber++;
 				try {
 					client.getServer().open(client.getUser(), qNumber, qValue, qText);
 					success = true;
 				} catch (final RemoteException e) {
 					client.log("Couldn't open question on server (try #" + tryNumber + ").");
 				}
 			}
 
 			if (!success) {
 				client.disconnected();
 				return;
 			}
 
 			client.log("Question #" + qNumber + " submitted.");
 
 		}
 	}
 
 
 }

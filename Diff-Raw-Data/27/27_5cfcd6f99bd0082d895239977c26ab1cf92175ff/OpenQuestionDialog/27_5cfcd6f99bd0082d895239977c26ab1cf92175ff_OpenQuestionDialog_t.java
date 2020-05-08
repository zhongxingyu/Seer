 package net.bubbaland.trivia.client;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
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
 import net.bubbaland.trivia.TriviaInterface;
 
 /**
  * Creates a prompt to enter new question data.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class OpenQuestionDialog extends TriviaDialogPanel {
 
 	/** The Constant serialVersionUID. */
 	private static final long	serialVersionUID	= 8250659442772286086L;
 
 	/**
 	 * Font sizes
 	 */
 	private static final float	LABEL_FONT_SIZE		= 20.0f;
 	private static final float	TEXTAREA_FONT_SIZE	= 16.0f;	
 	
 	/**
 	 * Instantiates a new question entry window.
 	 * 
 	 * @param server
 	 *            The remote trivia server
 	 * @param client
 	 *            The local trivia client
 	 * @param nQuestions
 	 *            the number of questions
 	 * @param qNumberStart
 	 *            the default question number
 	 */
 	public OpenQuestionDialog(TriviaInterface server, TriviaClient client, int nQuestions, int qNumberStart) {
 		this(server, client, nQuestions, qNumberStart, 10, "");
 	}
 
 	/**
 	 * Instantiates a new question entry window.
 	 * 
 	 * @param server
 	 *            The remote trivia server
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
 	public OpenQuestionDialog(final TriviaInterface server, final TriviaClient client, int nQuestions, int qNumberStart,
 			int qValueStart, String qTextStart) {
 
 		super( );
 
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
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		final JSpinner qNumberSpinner = new JSpinner(new SpinnerNumberModel(qNumberStart, 1, nQuestions, 1));
 		qNumberSpinner.setFont(qNumberSpinner.getFont().deriveFont(LABEL_FONT_SIZE));
		this.addEnterOverride(qNumberSpinner);
 		this.add(qNumberSpinner, constraints);
 
 		// Create the question value spinner
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		label = new JLabel("Question Value: ");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		final JSpinner qValueSpinner = new JSpinner(new SpinnerNumberModel(qValueStart, 10, 1000, 5));
 		qValueSpinner.setFont(qValueSpinner.getFont().deriveFont(LABEL_FONT_SIZE));
		this.addEnterOverride(qValueSpinner);
 		this.add(qValueSpinner, constraints);
 
 		// Create input area for the question text
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		label = new JLabel("Question: ");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		constraints.gridx = 0;
 		constraints.gridy = 3;
 		constraints.gridwidth = 2;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		final JTextArea qTextArea = new JTextArea(qTextStart, 4, 50);
 		qTextArea.setLineWrap(true);
 		qTextArea.setWrapStyleWord(true);
 		qTextArea.setFont(qTextArea.getFont().deriveFont(TEXTAREA_FONT_SIZE));
 		qTextArea.addAncestorListener(this);
 		this.addEnterOverride(qTextArea);
 		
 		JScrollPane scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(0, 200));
 		this.add(scrollPane, constraints);
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 		constraints.gridwidth = 1;
 		
 		// Display the dialog box
 		this.dialog = new TriviaDialog(client.getFrame(), "Open New Question", this, JOptionPane.PLAIN_MESSAGE,
 				JOptionPane.OK_CANCEL_OPTION);
 		this.dialog.setVisible(true);
 
 		// If the OK button was pressed, open the question
 		final int option = ( (Integer) dialog.getValue() ).intValue();
 		if (option == JOptionPane.OK_OPTION) {
 			// Get the input data
 			final int qNumber = (int) qNumberSpinner.getValue();
 			final int qValue = (int) qValueSpinner.getValue();
 			String qText = qTextArea.getText();
 
 			// Get the current Trivia data object
 			final Trivia trivia = client.getTrivia();
 			final int currentRound = trivia.getCurrentRoundNumber();
 
 			if (trivia.beenOpen(currentRound, qNumber)) {
 				// If the question has already been open, confirm that we want to overwrite
 
 				// Get the existing question data
 				final int existingQValue = trivia.getValue(currentRound, qNumber);
 				final String existingQText = trivia.getQuestionText(currentRound, qNumber);
 
 				this.removeAll();
 
 				constraints = new GridBagConstraints();
 				constraints.fill = GridBagConstraints.BOTH;
 				constraints.anchor = GridBagConstraints.CENTER;
 
 				constraints.gridx = 0;
 				constraints.gridy = 0;
 				label = new JLabel("Question alread open, overwrite?");
 				label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 				this.add(label, constraints);
 
 				// Show existing question data
 				constraints.gridx = 0;
 				constraints.gridy = 1;
 				label = new JLabel("Existing question:");
 				label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 				this.add(label, constraints);
 
 				constraints.gridx = 0;
 				constraints.gridy = 2;
 				constraints.weightx = 0.5;
 				constraints.weighty = 0.5;
 				JTextArea textArea = new JTextArea(existingQText);
 				textArea.setLineWrap(true);
 				textArea.setWrapStyleWord(true);
 				textArea.setEditable(false);
 				scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
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
 				label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 				this.add(label, constraints);
 
 				// Show new question data
 				constraints.gridx = 0;
 				constraints.gridy = 4;
 				label = new JLabel("Entered question:");
 				label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
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
 				label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 				this.add(label, constraints);
 
 				this.dialog = new TriviaDialog(client.getFrame(), "Confirm Question Overwrite " + qNumber, this,
 						JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
 				this.dialog.setVisible(true);
 
 				final int confirm = ( (Integer) dialog.getValue() ).intValue();
 				if (confirm != JOptionPane.OK_OPTION) {
 					new OpenQuestionDialog(server, client, nQuestions, qNumber, qValue, qText);
 					return;
 				}
 								
 				qText = textArea.getText();
 			}
 
 			// Open the question on the server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 				tryNumber++;
 				try {
 					server.open(client.getUser(), qNumber, qValue, qText);
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

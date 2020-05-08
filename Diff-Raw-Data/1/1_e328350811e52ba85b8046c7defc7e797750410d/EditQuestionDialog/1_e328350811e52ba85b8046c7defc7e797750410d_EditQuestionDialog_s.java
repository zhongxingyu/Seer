 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerNumberModel;
 
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 
 public class EditQuestionDialog extends TriviaDialogPanel implements ActionListener {
 
 	private static final long	serialVersionUID	= 8157338357601793846L;
 
 	/**
 	 * Font sizes
 	 */
 	private static final float	LABEL_FONT_SIZE		= 20.0f;
 	private static final float	TEXTAREA_FONT_SIZE	= 16.0f;
 
 	/**
 	 * GUI elements we will need to change on an action
 	 */
 	private final JToggleButton	correctButton;
 	private final JTextField	submitterTextField, operatorTextField;
 
 	public EditQuestionDialog(TriviaInterface server, TriviaClient client, int rNumber, int qNumber) {
 
 		super( );
 
 		// Get all of the current data for the question		
 		final Trivia trivia = client.getTrivia();
 
 		final boolean existingCorrect = trivia.isCorrect(rNumber, qNumber);
 		final int existingValue = trivia.getValue(rNumber, qNumber);
 		final String existingQText = trivia.getQuestionText(rNumber, qNumber);
 		final String existingAText = trivia.getAnswerText(rNumber, qNumber);
 		final String existingSubmitter = trivia.getSubmitter(rNumber, qNumber);
 		final String existingOperator = trivia.getOperator(rNumber, qNumber);
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		// Round number label
 		constraints.gridwidth = 2;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		JLabel label = new JLabel("Round: " + rNumber);
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Question number label
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		label = new JLabel("Question: " + qNumber);
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 		constraints.gridwidth = 1;
 
 		// Value label
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		label = new JLabel("Value:");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Value spinner
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		constraints.weightx = 0.5;
 		final JSpinner qValueSpinner = new JSpinner(new SpinnerNumberModel(existingValue, 10, 1000, 5));
 		qValueSpinner.setFont(qValueSpinner.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.addEnterOverride(qValueSpinner);
 		this.add(qValueSpinner, constraints);
 		constraints.weightx = 0.0;
 
 		// Toggle button to change correctness
 		constraints.gridx = 3;
 		constraints.gridy = 1;
 		this.correctButton = new JToggleButton();
 		this.correctButton.setMargin(new Insets(0, 0, 0, 0));
 		this.correctButton.addActionListener(this);
 		this.add(this.correctButton, constraints);
 		constraints.weightx = 0.0;
 
 		// Question label
 		constraints.gridwidth = 4;
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		label = new JLabel("Question:");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Question text
 		constraints.gridx = 0;
 		constraints.gridy = 3;
 		constraints.weightx = 1.0;
 		constraints.weighty = 0.6;
 		final JTextArea qTextArea = new JTextArea(existingQText, 4, 50);
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
 
 		// Answer label
 		constraints.gridx = 0;
 		constraints.gridy = 4;
 		label = new JLabel("Answer:");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Answer text
 		constraints.gridx = 0;
 		constraints.gridy = 5;
 		constraints.weightx = 1.0;
 		constraints.weighty = 0.4;
 		final JTextArea aTextArea = new JTextArea(existingAText, 4, 50);
 		aTextArea.setLineWrap(true);
 		aTextArea.setWrapStyleWord(true);
 		aTextArea.setFont(qTextArea.getFont().deriveFont(TEXTAREA_FONT_SIZE));
		aTextArea.addAncestorListener(this);
 		this.addEnterOverride(aTextArea);
 		scrollPane = new JScrollPane(aTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(0, 200));
 		this.add(scrollPane, constraints);
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		// Submitter label
 		constraints.gridwidth = 2;
 		constraints.gridx = 0;
 		constraints.gridy = 6;
 		label = new JLabel("Credit:");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Submitter text field; will be disable if question is incorrect
 		constraints.gridx = 2;
 		constraints.gridy = 6;
 		this.submitterTextField = new JTextField(existingSubmitter);
 		this.add(this.submitterTextField, constraints);
 
 		// Operator label
 		constraints.gridx = 0;
 		constraints.gridy = 7;
 		label = new JLabel("Operator:");
 		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.add(label, constraints);
 
 		// Operator text field; will be disable if question is incorrect
 		constraints.gridx = 2;
 		constraints.gridy = 7;
 		this.operatorTextField = new JTextField(existingOperator);
 		this.add(this.operatorTextField, constraints);
 
 		// Change text on correct button and set editable state of sub/op boxes based on correctness
 		if (existingCorrect) {
 			this.correctButton.setSelected(true);
 			this.correctButton.setText("Correct");
 			this.submitterTextField.setEditable(true);
 			this.operatorTextField.setEditable(true);
 			this.submitterTextField.setBackground(Color.WHITE);
 			this.operatorTextField.setBackground(Color.WHITE);
 		} else {
 			this.correctButton.setSelected(false);
 			this.correctButton.setText("Incorrect");
 			this.submitterTextField.setEditable(false);
 			this.operatorTextField.setEditable(false);
 			this.submitterTextField.setBackground(this.getBackground());
 			this.operatorTextField.setBackground(this.getBackground());
 		}
 
 		// Display the dialog box
 		this.dialog = new TriviaDialog(client.getFrame(), "Edit Question", this,
 				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
 		this.dialog.setVisible(true);
 		
 		// If the OK button was pressed, add the proposed answer to the queue
 		final int option = ( (Integer) this.dialog.getValue() ).intValue();
 		if (option == JOptionPane.OK_OPTION) {
 			// Get the input data
 			final boolean isCorrect = this.correctButton.isSelected();
 			final int qValue = (int) qValueSpinner.getValue();
 			final String qText = qTextArea.getText();
 			final String aText = aTextArea.getText();
 			final String submitter = this.submitterTextField.getText();
 			final String operator = this.operatorTextField.getText();
 
 			// Edit the question on the server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 				tryNumber++;
 				try {
 					server.editQuestion(rNumber, qNumber, qValue, qText, aText, isCorrect, submitter, operator);
 					success = true;
 				} catch (final RemoteException e) {
 					client.log("Couldn't edit question on server (try #" + tryNumber + ").");
 				}
 			}
 
 			if (!success) {
 				client.disconnected();
 				return;
 			}
 
 			client.log("Question #" + qNumber + " editted.");
 
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.correctButton)) {
 			// If the correctness was changed, update GUI elements appropriately
 			if (this.correctButton.isSelected()) {
 				this.correctButton.setText("Correct");
 				this.submitterTextField.setEditable(true);
 				this.operatorTextField.setEditable(true);
 				this.submitterTextField.setBackground(Color.WHITE);
 				this.operatorTextField.setBackground(Color.WHITE);
 			} else {
 				this.correctButton.setText("Incorrect");
 				this.submitterTextField.setEditable(false);
 				this.operatorTextField.setEditable(false);
 				this.submitterTextField.setBackground(this.getBackground());
 				this.operatorTextField.setBackground(this.getBackground());
 			}
 		}
 
 	}
 
 }

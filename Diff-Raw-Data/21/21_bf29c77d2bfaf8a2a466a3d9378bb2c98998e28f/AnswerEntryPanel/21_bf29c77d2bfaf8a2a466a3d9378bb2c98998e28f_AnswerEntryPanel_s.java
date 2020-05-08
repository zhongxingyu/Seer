 package net.bubbaland.trivia.client;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.WindowEvent;
 import java.rmi.RemoteException;
 import java.util.Hashtable;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 
 import net.bubbaland.trivia.Trivia;
 
 /**
  * Creates a dialog box that prompts user to propose an answer.
  * 
  * @author Walter Kolczynski
  */
 public class AnswerEntryPanel extends TriviaDialogPanel {
 
 	private static final long	serialVersionUID	= -5797789908178154492L;
 
 	private final String		user;
 	private final int			qNumber;
 	private final JTextArea		answerTextArea;
 	private final JSlider		confidenceSlider;
 	private final TriviaClient	client;
 
 	/**
 	 * Creates a dialog box and prompt for response
 	 * 
 	 * @param client
 	 *            The local trivia client
 	 * @param qNumber
 	 *            The question number
 	 * @param user
 	 *            The user's name
 	 */
 	public AnswerEntryPanel(TriviaClient client, int qNumber, String user) {
 
 		super();
 
 		this.client = client;
 		this.qNumber = qNumber;
 		this.user = user;
 
 		// Retrieve current trivia data object
 		final Trivia trivia = client.getTrivia();
 
 		// Get the question text
 		final String qText = trivia.getQuestionText(qNumber);
 
 		// Set up layout constraints
 		final GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.anchor = GridBagConstraints.CENTER;
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 
 		// Display question text
 		c.gridwidth = 2;
 		c.gridx = 0;
 		c.gridy = 0;
 		JLabel label = new JLabel("Question:");
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, c);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 0.5;
 		c.weighty = 0.5;
 		final JTextArea textArea = new JTextArea(qText);
 		textArea.setFont(textArea.getFont().deriveFont(textAreaFontSize));
 		textArea.setEditable(false);
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(0, 200));
 		this.add(scrollPane, c);
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 
 		// Create answer text box for input
 		c.gridx = 0;
 		c.gridy = 2;
 		label = new JLabel("Answer: ");
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, c);
 
 		c.gridx = 0;
 		c.gridy = 3;
 		c.weightx = 0.5;
 		c.weighty = 0.5;
 		this.answerTextArea = new JTextArea("", 4, 50);
 		this.answerTextArea.setLineWrap(true);
 		this.answerTextArea.setWrapStyleWord(true);
 		this.answerTextArea.setFont(this.answerTextArea.getFont().deriveFont(textAreaFontSize));
 		this.answerTextArea.addAncestorListener(this);
 		this.addEnterOverride(this.answerTextArea);
 		scrollPane = new JScrollPane(this.answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(0, 200));
 		this.add(scrollPane, c);
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 
 		// Create confidence slider
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 4;
 		label = new JLabel("Confidence", SwingConstants.RIGHT);
 		label.setVerticalAlignment(SwingConstants.CENTER);
 		label.setFont(label.getFont().deriveFont(fontSize));
 		this.add(label, c);
 
 		c.gridx = 1;
 		c.gridy = 4;
 		c.insets = new Insets(sliderPaddingBottom, sliderPaddingLeft, sliderPaddingRight, sliderPaddingTop);
		this.confidenceSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 5, 1);
 		this.confidenceSlider.setMajorTickSpacing(1);
 		final Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 		labelTable.put(new Integer(1), new JLabel("Guess"));
 		labelTable.put(new Integer(5), new JLabel("Sure"));
 		this.confidenceSlider.setLabelTable(labelTable);
 		this.confidenceSlider.setPaintLabels(true);
 		this.confidenceSlider.setPaintTicks(true);
 		this.add(this.confidenceSlider, c);
 
 		// Display the dialog box
 		this.dialog = new TriviaDialog(null, "Submit Answer for Question " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
 				JOptionPane.OK_CANCEL_OPTION);
 		this.dialog.setName("Answer Question");
 		this.dialog.setVisible(true);
 	}
 
 	@Override
 	public void windowClosed(WindowEvent event) {
 		super.windowClosed(event);
 		// If the OK button was pressed, add the proposed answer to the queue
 		final int option = ( (Integer) this.dialog.getValue() ).intValue();
 		if (option == JOptionPane.OK_OPTION) {
 			final String answer = answerTextArea.getText();
 			final int confidence = confidenceSlider.getValue();
 
 			if (answer.equals("")) {
 				new AnswerEntryPanel(client, qNumber, user);
 				return;
 			}
 
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
 				tryNumber++;
 				try {
 					client.getServer().proposeAnswer(qNumber, answer, user, confidence);
 					success = true;
 				} catch (final RemoteException e) {
 					client.log("Couldn't set announced round scores on server (try #" + tryNumber + ").");
 				}
 			}
 
 			if (!success) {
 				client.disconnected();
 				return;
 			}
 
 			client.log("Submitted an answer for Question #" + qNumber);
 
 		}
 	}
 
 }

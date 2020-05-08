 package net.bubbaland.trivia.client;
 
 // imports for GUI
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.math.BigInteger;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JToggleButton;
 import javax.swing.ListCellRenderer;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 
 import net.bubbaland.trivia.Answer;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.client.TriviaFrame.QueueSort;
 
 /**
  * A panel that shows the submitted answers for the current round.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class AnswerQueuePanel extends TriviaMainPanel implements MouseListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long				serialVersionUID	= 784049314825719490L;
 
 	/** Valid statuses for queue items */
 	private static final String[]			STATUSES			= { "Duplicate", "Not Called In", "Calling",
 			"Incorrect", "Partial", "Correct"					};
 
 	/** Sort icons */
 	private static final ImageIcon			upArrow				= new ImageIcon(
 																		AnswerQueuePanel.class
 																				.getResource("images/upArrow.png"));
 	private static final ImageIcon			downArrow			= new ImageIcon(
 																		AnswerQueuePanel.class
 																				.getResource("images/downArrow.png"));
 
 	// Set up layout constraints
 	private static final GridBagConstraints	buttonConstraints	= new GridBagConstraints();
 	static {
 		buttonConstraints.fill = GridBagConstraints.BOTH;
 		buttonConstraints.anchor = GridBagConstraints.CENTER;
 		buttonConstraints.weightx = 1.0;
 		buttonConstraints.weighty = 1.0;
 		buttonConstraints.gridx = 0;
 		buttonConstraints.gridy = 0;
 		buttonConstraints.fill = GridBagConstraints.NONE;
 	}
 
 	private static Color					oddRowBackgroundColor, evenRowBackgroundColor, duplicateColor,
 			notCalledInColor, callingColor, incorrectColor, partialColor, correctColor, agreeColor;
 	private static int						rowHeight, timeWidth, qNumWidth, answerWidth, confidenceWidth,
 			subCallerWidth, operatorWidth, statusWidth;
 	private static float					fontSize, qNumFontSize;
 
 	/**
 	 * GUI elements that will be updated
 	 */
 	final private JLabel					timestampLabel, qNumberLabel, answerLabel, confidenceLabel, subCallerLabel,
 			operatorLabel, statusLabel, queueSizeLabel;
 	final private JPanel					spacer;
 	final private JScrollPane				scrollPane;
 
 	/** The workflow queue sub panel */
 	final private AnswerQueueSubPanel		answerQueueSubPanel;
 
 	/** The local client */
 	final private TriviaClient				client;
 	final private TriviaFrame				parent;
 
 	/**
 	 * Instantiates a new workflow queue panel.
 	 * 
 	 * @param parent
 	 *            The parent top-level frame
 	 * @param client
 	 *            The local trivia client
 	 */
 	public AnswerQueuePanel(TriviaFrame parent, TriviaClient client) {
 
 		super();
 
 		this.parent = parent;
 		this.client = client;
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.NORTH;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		/**
 		 * Create the header row
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		this.timestampLabel = this.enclosedLabel("Time", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 		this.timestampLabel.setHorizontalTextPosition(SwingConstants.LEFT);
 		this.timestampLabel.addMouseListener(this);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		this.qNumberLabel = this.enclosedLabel("Q#", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 		this.qNumberLabel.setHorizontalTextPosition(SwingConstants.LEFT);
 		this.qNumberLabel.addMouseListener(this);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		constraints.weightx = 1.0;
 		this.answerLabel = this.enclosedLabel("Proposed Answer", constraints, SwingConstants.LEFT,
 				SwingConstants.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		this.confidenceLabel = this.enclosedLabel("Conf", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		this.subCallerLabel = this.enclosedLabel("Sub/Caller", constraints, SwingConstants.CENTER,
 				SwingConstants.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		this.operatorLabel = this.enclosedLabel("Operator", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		this.statusLabel = this.enclosedLabel("Status", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 		this.statusLabel.setHorizontalTextPosition(SwingConstants.LEFT);
 		this.statusLabel.addMouseListener(this);
 
 		/**
 		 * Create the sub-panel that will show the queue data and put it in a scroll pane
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 8;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		final JPanel scrollPanel = new JPanel(new GridBagLayout());
 		this.scrollPane = new JScrollPane(scrollPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
 		this.add(this.scrollPane, constraints);
 
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.weightx = 1.0;
 		constraints.weighty = 0.0;
 
 		this.answerQueueSubPanel = new AnswerQueueSubPanel(client);
 		scrollPanel.add(this.answerQueueSubPanel, constraints);
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		constraints.gridx = 7;
 		constraints.gridy = 0;
 		this.queueSizeLabel = this.enclosedLabel("0", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		/**
 		 * Create a blank spacer row at the bottom
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 1;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.spacer = new JPanel();
 		this.spacer.setPreferredSize(new Dimension(0, 0));
 		scrollPanel.add(this.spacer, constraints);
 
 		this.loadProperties(TriviaClient.PROPERTIES);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		final JComponent source = (JComponent) event.getSource();
 		final QueueSort sortMethod = this.parent.getQueueSort();
 		if (source.equals(this.timestampLabel)) {
 			if (sortMethod.equals(QueueSort.TIMESTAMP_ASCENDING)) {
 				this.parent.setSort(QueueSort.TIMESTAMP_DESCENDING);
 			} else {
 				this.parent.setSort(QueueSort.TIMESTAMP_ASCENDING);
 			}
 		} else if (source.equals(this.qNumberLabel)) {
 			if (sortMethod.equals(QueueSort.QNUMBER_ASCENDING)) {
 				this.parent.setSort(QueueSort.QNUMBER_DESCENDING);
 			} else {
 				this.parent.setSort(QueueSort.QNUMBER_ASCENDING);
 			}
 		} else if (source.equals(this.statusLabel)) {
 			if (sortMethod.equals(QueueSort.STATUS_ASCENDING)) {
 				this.parent.setSort(QueueSort.STATUS_DESCENDING);
 			} else {
 				this.parent.setSort(QueueSort.STATUS_ASCENDING);
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent event) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent event) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public synchronized void update(boolean force) {
 		// Update the queue size
 		final int queueSize = this.client.getTrivia().getAnswerQueueSize();
 		this.queueSizeLabel.setText(queueSize + "");
 		this.answerQueueSubPanel.update(force);
 		final QueueSort sortMethod = this.parent.getQueueSort();
 
 		switch (sortMethod) {
 			case TIMESTAMP_ASCENDING:
 				this.timestampLabel.setIcon(downArrow);
 				this.qNumberLabel.setIcon(null);
 				this.statusLabel.setIcon(null);
 				break;
 			case QNUMBER_ASCENDING:
 				this.timestampLabel.setIcon(null);
 				this.qNumberLabel.setIcon(downArrow);
 				this.statusLabel.setIcon(null);
 				break;
 			case STATUS_ASCENDING:
 				this.timestampLabel.setIcon(null);
 				this.qNumberLabel.setIcon(null);
 				this.statusLabel.setIcon(downArrow);
 				break;
 			case TIMESTAMP_DESCENDING:
 				this.timestampLabel.setIcon(upArrow);
 				this.qNumberLabel.setIcon(null);
 				this.statusLabel.setIcon(null);
 				break;
 			case QNUMBER_DESCENDING:
 				this.timestampLabel.setIcon(null);
 				this.qNumberLabel.setIcon(upArrow);
 				this.statusLabel.setIcon(null);
 				break;
 			case STATUS_DESCENDING:
 				this.timestampLabel.setIcon(null);
 				this.qNumberLabel.setIcon(null);
 				this.statusLabel.setIcon(upArrow);
 				break;
 		}
 	}
 
 	/**
 	 * Load properties from the client and apply.
 	 */
 	@Override
 	protected void loadProperties(Properties properties) {
 		/**
 		 * Colors
 		 */
 		final Color headerColor = new Color(
 				new BigInteger(properties.getProperty("AnswerQueue.Header.Color"), 16).intValue());
 		final Color headerBackgroundColor = new Color(new BigInteger(
 				properties.getProperty("AnswerQueue.Header.BackgroundColor"), 16).intValue());
 		oddRowBackgroundColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.OddRow.BackgroundColor"),
 				16).intValue());
 		evenRowBackgroundColor = new Color(new BigInteger(
 				properties.getProperty("AnswerQueue.EvenRow.BackgroundColor"), 16).intValue());
 		duplicateColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Duplicate.Color"), 16).intValue());
 		notCalledInColor = new Color(
 				new BigInteger(properties.getProperty("AnswerQueue.NotCalledIn.Color"), 16).intValue());
 		callingColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Calling.Color"), 16).intValue());
 		incorrectColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Incorrect.Color"), 16).intValue());
 		partialColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Partial.Color"), 16).intValue());
 		correctColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Correct.Color"), 16).intValue());
 		agreeColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Agree.Color"), 16).intValue());
 
 		/**
 		 * Sizes
 		 */
 		final int headerHeight = Integer.parseInt(properties.getProperty("AnswerQueue.Header.Height"));
 		rowHeight = Integer.parseInt(properties.getProperty("AnswerQueue.Row.Height"));
 
 		timeWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Timestamp.Width"));
 		qNumWidth = Integer.parseInt(properties.getProperty("AnswerQueue.QNumber.Width"));
 		answerWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Answer.Width"));
 		confidenceWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Confidence.Width"));
 		subCallerWidth = Integer.parseInt(properties.getProperty("AnswerQueue.SubCaller.Width"));
 		operatorWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Operator.Width"));
 		statusWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Status.Width"));
 
 		/**
 		 * Font sizes
 		 */
 		final float headerFontSize = Float.parseFloat(properties.getProperty("AnswerQueue.Header.FontSize"));
 		qNumFontSize = Float.parseFloat(properties.getProperty("AnswerQueue.QNumber.FontSize"));
 		fontSize = Float.parseFloat(properties.getProperty("AnswerQueue.FontSize"));
 
 		/** The number of open questions to show at one time */
 		final int answersShow = Integer.parseInt(properties.getProperty("AnswerQueue.AnswersShow"));
 
 		setLabelProperties(this.timestampLabel, timeWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.answerLabel, answerWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.confidenceLabel, confidenceWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.subCallerLabel, subCallerWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.operatorLabel, operatorWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 		setLabelProperties(this.statusLabel, statusWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 
 		final int scrollBarWidth;
 		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
 			scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
 		} else {
 			scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
 		}
 		setLabelProperties(this.queueSizeLabel, scrollBarWidth, headerHeight, headerColor, headerBackgroundColor,
 				headerFontSize);
 
 		this.scrollPane.setPreferredSize(new Dimension(0, answersShow * rowHeight + 3));
 		this.scrollPane.setMinimumSize(new Dimension(0, rowHeight + 3));
 
 		this.spacer.setBackground(headerBackgroundColor);
 
 		this.answerQueueSubPanel.loadProperties(properties);
 	}
 
 	/**
 	 * A panel that will show the current answer queue
 	 */
 	private class AnswerQueueSubPanel extends TriviaMainPanel implements ItemListener, ActionListener {
 
 		/** The Constant serialVersionUID */
 		private static final long					serialVersionUID	= -5462544756397828556L;
 
 		/** The last status (used for determining if the status has changed) */
 		private volatile ArrayList<String>			lastStatus;
 
 		/**
 		 * GUI elements that will be updated
 		 */
 		final private ArrayList<JLabel>				queuenumberLabels, timestampLabels, qNumberLabels,
 				confidenceLabels, submitterLabels, operatorLabels, callerLabels;
 		final private ArrayList<JComboBox<String>>	statusComboBoxes;
 		final private ArrayList<JTextArea>			answerTextAreas;
 		final private ArrayList<JToggleButton>		agreeButtons;
 		final private Hashtable<Integer, Boolean>	agreements;
 		private final JPopupMenu					contextMenu;
 
 		/**
 		 * Data
 		 */
 		private Answer[]							answerQueue;
 
 		/**
 		 * Data sources
 		 */
 		final private TriviaClient					client;
 
 
 		/**
 		 * Instantiates a new workflow queue sub panel.
 		 * 
 		 * @param server
 		 *            the server
 		 * @param client
 		 *            the client
 		 */
 		public AnswerQueueSubPanel(TriviaClient client) {
 
 			super();
 
 			this.client = client;
 			this.answerQueue = new Answer[0];
 
 			/**
 			 * Build context menu
 			 */
 			this.contextMenu = new JPopupMenu();
 
 			final JMenuItem viewItem = new JMenuItem("View");
 			viewItem.setActionCommand("View");
 			viewItem.addActionListener(this);
 			this.contextMenu.add(viewItem);
 			this.add(this.contextMenu);
 
 			// Set up layout constraints
 			final GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.BOTH;
 			constraints.anchor = GridBagConstraints.NORTH;
 			constraints.weightx = 0.0;
 			constraints.weighty = 0.0;
 
 			this.queuenumberLabels = new ArrayList<JLabel>(0);
 			this.timestampLabels = new ArrayList<JLabel>(0);
 			this.qNumberLabels = new ArrayList<JLabel>(0);
 			this.confidenceLabels = new ArrayList<JLabel>(0);
 			this.agreeButtons = new ArrayList<JToggleButton>(0);
 			this.agreements = new Hashtable<Integer, Boolean>();
 			this.submitterLabels = new ArrayList<JLabel>(0);
 			this.operatorLabels = new ArrayList<JLabel>(0);
 			this.callerLabels = new ArrayList<JLabel>(0);
 			this.statusComboBoxes = new ArrayList<JComboBox<String>>(0);
 			this.answerTextAreas = new ArrayList<JTextArea>(0);
 			this.lastStatus = new ArrayList<String>(0);
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		@Override
 		public synchronized void actionPerformed(ActionEvent event) {
 			final Trivia trivia = this.client.getTrivia();
 			final int rNumber = trivia.getCurrentRoundNumber();
 			final String command = event.getActionCommand();
 			switch (command) {
 				case "View":
 					int queueIndex = Integer.parseInt(this.contextMenu.getName());
 					final int qNumber = trivia.getAnswerQueueQNumber(queueIndex);
 					final int qValue = trivia.getValue(rNumber, qNumber);
 					final String qText = trivia.getQuestionText(rNumber, qNumber);
 					final String aText = trivia.getAnswerQueueAnswer(queueIndex);
 					new ViewAnswerDialog(this.client, qNumber, qValue, qText, aText);
 					break;
 				case "Agree":
 					JToggleButton source = ( (JToggleButton) event.getSource() );
 					queueIndex = Integer.parseInt(source.getName());
 					this.agreements.put(queueIndex, true);
 
 					// Update status on server
 					int tryNumber = 0;
 					boolean success = false;
 					while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
 							&& success == false) {
 						tryNumber++;
 						try {
 							this.client.getServer().agree(this.client.getUser(), queueIndex);
 							success = true;
 						} catch (final RemoteException e) {
 							this.client.log("Couldn't register agreement server (try #" + tryNumber + ").");
 						}
 					}
 
 					// Unable to update status on server, show disconnected dialog
 					if (!success) {
 						this.client.disconnected();
 						return;
 					}
 
 					this.client.log("Agreed with answer #" + ( queueIndex + 1 ));
 					break;
 				case "Disagree":
 					source = ( (JToggleButton) event.getSource() );
 					queueIndex = Integer.parseInt(source.getName());
 					this.agreements.put(queueIndex, false);
 					// Update status on server
 					tryNumber = 0;
 					success = false;
 					while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
 							&& success == false) {
 						tryNumber++;
 						try {
 							this.client.getServer().disagree(this.client.getUser(), queueIndex);
 							success = true;
 						} catch (final RemoteException e) {
 							this.client.log("Couldn't register disagreement on server (try #" + tryNumber + ").");
 						}
 					}
 
 					// Unable to update status on server, show disconnected dialog
 					if (!success) {
 						this.client.disconnected();
 						return;
 					}
 
 					this.client.log("Disagreed with answer #" + ( queueIndex + 1 ));
 					break;
 				default:
 					break;
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public synchronized void itemStateChanged(ItemEvent event) {
 			final JComponent source = (JComponent) event.getSource();
 			if (source instanceof JComboBox<?> && event.getStateChange() == ItemEvent.SELECTED) {
 				// Event was a change to the status combo box
 				final String newStatus = (String) ( ( (JComboBox<String>) source ).getSelectedItem() );
 				final int queueIndex = Integer.parseInt(( (JComboBox<String>) source ).getName());
 
 				// Update status on server
 				int tryNumber = 0;
 				boolean success = false;
 				while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
 						&& success == false) {
 					tryNumber++;
 					try {
 						switch (newStatus) {
 							case "Duplicate":
 								this.client.getServer().markDuplicate(this.client.getUser(), queueIndex);
 								break;
 							case "Not Called In":
 								this.client.getServer().markUncalled(this.client.getUser(), queueIndex);
 								break;
 							case "Calling":
 								this.client.getServer().callIn(queueIndex, this.client.getUser());
 								break;
 							case "Incorrect":
 								this.client.getServer().markIncorrect(queueIndex, this.client.getUser());
 								break;
 							case "Partial":
 								this.client.getServer().markPartial(queueIndex, this.client.getUser());
 								break;
 							case "Correct":
 								new CorrectEntryDialog(this.client, this.client.getUser(), queueIndex,
 										( (JComboBox<String>) source ));
 								break;
 							default:
 								break;
 						}
 						success = true;
 					} catch (final RemoteException e) {
 						this.client.log("Couldn't change answer status on server (try #" + tryNumber + ").");
 					}
 				}
 
 				// Unable to update status on server, show disconnected dialog
 				if (!success) {
 					this.client.disconnected();
 					return;
 				}
 
 				this.client.log("Changed status of answer #" + ( queueIndex + 1 ) + " in the queue to " + newStatus);
 
 			}
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see net.bubbaland.trivia.TriviaPanel#update()
 		 */
 		@Override
 		public synchronized void update(boolean force) {
 			// Get the current Trivia data object
 			final Trivia trivia = this.client.getTrivia();
 
 			final boolean hideClosed = AnswerQueuePanel.this.parent.hideClosed();
 			final boolean hideDuplicates = AnswerQueuePanel.this.parent.hideDuplicates();
 			final QueueSort sortMethod = AnswerQueuePanel.this.parent.getQueueSort();
 
 			// Get the queue data from the server
 			final Answer[] newAnswerQueue = trivia.getAnswerQueue();
 
 			switch (sortMethod) {
 				case TIMESTAMP_ASCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.TimestampCompare());
 					break;
 				case QNUMBER_ASCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.QNumberCompare());
 					break;
 				case STATUS_ASCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.StatusCompare());
 					break;
 				case TIMESTAMP_DESCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.TimestampCompareReverse());
 					break;
 				case QNUMBER_DESCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.QNumberCompareReverse());
 					break;
 				case STATUS_DESCENDING:
 					Arrays.sort(newAnswerQueue, new Answer.StatusCompareReverse());
 					break;
 			}
 
 			final int queueSize = newAnswerQueue.length;
 
 			// Determine if each item in the queue has been updated
 			final boolean[] qUpdated = new boolean[queueSize];
 			for (int a = 0; a < queueSize; a++) {
 				if (a < this.answerQueue.length) {
 					qUpdated[a] = !( this.answerQueue[a].equals(newAnswerQueue[a]) );
 				} else {
 					qUpdated[a] = true;
 				}
 			}
 
 			this.answerQueue = newAnswerQueue;
 
 			// We need to track how many rows have been shown to do alternating background colors correctly
 			int shownRows = 0;
 
 			while (this.queuenumberLabels.size() < this.answerQueue.length) {
 				this.makeNewRow();
 			}
 
 			for (int a = 0; a < queueSize; a++) {
 
 				// Get the new data from the answer queue
 				final int newQueueNumber = this.answerQueue[a].getQueueLocation();
 				final String newTimestamp = this.answerQueue[a].getTimestamp();
 				final int newQNumber = this.answerQueue[a].getQNumber();
 				final String newAnswer = this.answerQueue[a].getAnswer();
 				final int newConfidence = this.answerQueue[a].getConfidence();
 				final int newAgreement = this.answerQueue[a].getAgreement();
 				final String newSubmitter = this.answerQueue[a].getSubmitter();
 				final String newOperator = this.answerQueue[a].getOperator();
 				final String newCaller = this.answerQueue[a].getCaller();
 				final String newStatus = this.answerQueue[a].getStatusString();
 
 				this.lastStatus.set(a, newStatus);
 				final boolean closed = !trivia.isOpen(newQNumber);
 
 				if (!( hideClosed && closed ) && !( hideDuplicates && newStatus.equals("Duplicate") )) {
 					shownRows++;
 				}
 
 				if (qUpdated[a] || force) {
 					// If the status has changed, update the labels and color
 					Color color = notCalledInColor;
 					final int statusIndex = Arrays.asList(STATUSES).indexOf(newStatus);
 					switch (newStatus) {
 						case "Duplicate":
 							color = duplicateColor;
 							break;
 						case "Not Called In":
 							color = notCalledInColor;
 							break;
 						case "Calling":
 							color = callingColor;
 							break;
 						case "Incorrect":
 							color = incorrectColor;
 							break;
 						case "Partial":
 							color = partialColor;
 							break;
 						case "Correct":
 							color = correctColor;
 							break;
 						default:
 							color = notCalledInColor;
 							break;
 					}
 
 					// Alternate background color based on rows shown
 					Color bColor;
 					if (shownRows % 2 == 1) {
 						bColor = oddRowBackgroundColor;
 					} else {
 						bColor = evenRowBackgroundColor;
 					}
 
 					this.queuenumberLabels.get(a).setText("#" + newQueueNumber);
 					this.queuenumberLabels.get(a).setForeground(color);
 					this.queuenumberLabels.get(a).getParent().setBackground(bColor);
 					this.queuenumberLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.timestampLabels.get(a).setText(newTimestamp);
 					this.timestampLabels.get(a).setForeground(color);
 					this.timestampLabels.get(a).getParent().setBackground(bColor);
 					this.timestampLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.qNumberLabels.get(a).setText(newQNumber + "");
 					this.qNumberLabels.get(a).setForeground(color);
 					this.qNumberLabels.get(a).getParent().setBackground(bColor);
 					this.qNumberLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.answerTextAreas.get(a).setText(newAnswer);
 					// this.answerTextAreas.get(a).setToolTipText(newAnswer);
 					this.answerTextAreas.get(a).setForeground(color);
 					this.answerTextAreas.get(a).setBackground(bColor);
 					this.answerTextAreas.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					String confidenceText = ( newAgreement == 0 ) ? newConfidence + "" : newConfidence + " (+"
 							+ newAgreement + ")";
 					this.confidenceLabels.get(a).setText(confidenceText);
 					this.confidenceLabels.get(a).setForeground(color);
 					this.confidenceLabels.get(a).getParent().setBackground(bColor);
 					this.confidenceLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.agreeButtons.get(a).setForeground(color);
 					this.agreeButtons.get(a).getParent().setBackground(bColor);
 					this.agreeButtons.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					if (this.agreements.get(newQueueNumber - 1)) {
 						this.agreeButtons.get(a).setSelected(true);
 						this.agreeButtons.get(a).setBackground(agreeColor);
 						this.agreeButtons.get(a).setActionCommand("Disagree");
 					} else {
 						this.agreeButtons.get(a).setSelected(false);
 						this.agreeButtons.get(a).setBackground(null);
 						this.agreeButtons.get(a).setActionCommand("Agree");
 					}
 
 					this.submitterLabels.get(a).setText(newSubmitter);
 					this.submitterLabels.get(a).setForeground(color);
 					this.submitterLabels.get(a).getParent().setBackground(bColor);
 					this.submitterLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.operatorLabels.get(a).setText(newOperator);
 					this.operatorLabels.get(a).setForeground(color);
 					this.operatorLabels.get(a).getParent().setBackground(bColor);
 					this.operatorLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					this.callerLabels.get(a).setText(newCaller);
 					this.callerLabels.get(a).setForeground(color);
 					this.callerLabels.get(a).getParent().setBackground(bColor);
 					this.callerLabels.get(a).setName("" + ( newQueueNumber - 1 ));
 
 					// Temporarily remove the status box listener to prevent trigger when we change it to match server
 					// status
 					final ItemListener[] listeners = this.statusComboBoxes.get(a).getItemListeners();
 					for (final ItemListener listener : listeners) {
 						this.statusComboBoxes.get(a).removeItemListener(listener);
 					}
 					this.statusComboBoxes.get(a).setForeground(bColor);
 					this.statusComboBoxes.get(a).setBackground(color);
 					this.statusComboBoxes.get(a).getParent().setBackground(bColor);
 					this.statusComboBoxes.get(a).setName(( newQueueNumber - 1 ) + "");
 					this.statusComboBoxes.get(a).setSelectedIndex(statusIndex);
 					// Add the status box listener back to monitor user changes
 					for (final ItemListener listener : listeners) {
 						this.statusComboBoxes.get(a).addItemListener(listener);
 					}
 
 					if (( hideClosed && closed ) || ( hideDuplicates && newStatus.equals("Duplicate") )) {
 						// Hide this row
 						this.queuenumberLabels.get(a).setVisible(false);
 						this.timestampLabels.get(a).setVisible(false);
 						this.qNumberLabels.get(a).setVisible(false);
 						this.answerTextAreas.get(a).setVisible(false);
 						this.answerTextAreas.get(a).getParent().setVisible(false);
 						this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
 						this.confidenceLabels.get(a).setVisible(false);
 						this.agreeButtons.get(a).setVisible(false);
 						this.submitterLabels.get(a).setVisible(false);
 						this.operatorLabels.get(a).setVisible(false);
 						this.callerLabels.get(a).setVisible(false);
 						this.statusComboBoxes.get(a).setVisible(false);
 
 						this.queuenumberLabels.get(a).getParent().setVisible(false);
 						this.timestampLabels.get(a).getParent().setVisible(false);
 						this.qNumberLabels.get(a).getParent().setVisible(false);
 						this.answerTextAreas.get(a).getParent().setVisible(false);
 						this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
 						this.confidenceLabels.get(a).getParent().setVisible(false);
 						this.agreeButtons.get(a).getParent().setVisible(false);
 						this.submitterLabels.get(a).getParent().setVisible(false);
 						this.operatorLabels.get(a).getParent().setVisible(false);
 						this.callerLabels.get(a).getParent().setVisible(false);
 						this.statusComboBoxes.get(a).getParent().setVisible(false);
 					} else {
 						// Make this row visible
 						this.queuenumberLabels.get(a).setVisible(true);
 						this.timestampLabels.get(a).setVisible(true);
 						this.qNumberLabels.get(a).setVisible(true);
 						this.answerTextAreas.get(a).setVisible(true);
 						this.answerTextAreas.get(a).getParent().setVisible(true);
 						this.answerTextAreas.get(a).getParent().getParent().setVisible(true);
 						this.confidenceLabels.get(a).setVisible(true);
 						this.agreeButtons.get(a).setVisible(true);
 						this.submitterLabels.get(a).setVisible(true);
 						this.operatorLabels.get(a).setVisible(true);
 						this.callerLabels.get(a).setVisible(true);
 						this.statusComboBoxes.get(a).setVisible(true);
 
 						this.queuenumberLabels.get(a).getParent().setVisible(true);
 						this.timestampLabels.get(a).getParent().setVisible(true);
 						this.qNumberLabels.get(a).getParent().setVisible(true);
 						this.answerTextAreas.get(a).getParent().setVisible(true);
 						this.answerTextAreas.get(a).getParent().getParent().setVisible(true);
 						this.confidenceLabels.get(a).getParent().setVisible(true);
 						this.agreeButtons.get(a).getParent().setVisible(true);
 						this.submitterLabels.get(a).getParent().setVisible(true);
 						this.operatorLabels.get(a).getParent().setVisible(true);
 						this.callerLabels.get(a).getParent().setVisible(true);
 						this.statusComboBoxes.get(a).getParent().setVisible(true);
 					}
 
 				}
 
 			}
 
 			// Hide unused rows
 			for (int a = queueSize; a < this.queuenumberLabels.size(); a++) {
 				this.queuenumberLabels.get(a).setVisible(false);
 				this.timestampLabels.get(a).setVisible(false);
 				this.qNumberLabels.get(a).setVisible(false);
 				this.answerTextAreas.get(a).setVisible(false);
 				this.confidenceLabels.get(a).setVisible(false);
				this.agreeButtons.get(a).setVisible(false);
 				this.submitterLabels.get(a).setVisible(false);
 				this.operatorLabels.get(a).setVisible(false);
 				this.callerLabels.get(a).setVisible(false);
 				this.statusComboBoxes.get(a).setVisible(false);
 
 				this.queuenumberLabels.get(a).getParent().setVisible(false);
 				this.timestampLabels.get(a).getParent().setVisible(false);
 				this.qNumberLabels.get(a).getParent().setVisible(false);
 				this.answerTextAreas.get(a).getParent().setVisible(false);
 				this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
 				this.confidenceLabels.get(a).getParent().setVisible(false);
				this.agreeButtons.get(a).getParent().setVisible(false);
 				this.submitterLabels.get(a).getParent().setVisible(false);
 				this.operatorLabels.get(a).getParent().setVisible(false);
 				this.callerLabels.get(a).getParent().setVisible(false);
 				this.statusComboBoxes.get(a).getParent().setVisible(false);
 
 			}
 
 		}
 
 		/**
 		 * Load properties from the client and apply.
 		 */
 		@Override
 		protected void loadProperties(Properties properties) {
 
 			for (int a = 0; a < this.queuenumberLabels.size(); a++) {
 				setLabelProperties(this.queuenumberLabels.get(a), timeWidth, rowHeight / 2, null, null, fontSize);
 				setLabelProperties(this.timestampLabels.get(a), timeWidth, rowHeight / 2, null, null, fontSize);
 				setLabelProperties(this.qNumberLabels.get(a), qNumWidth, rowHeight, null, null, qNumFontSize);
 				setTextAreaProperties(this.answerTextAreas.get(a), answerWidth, rowHeight, null, null, fontSize);
 				setLabelProperties(this.confidenceLabels.get(a), confidenceWidth, rowHeight / 2, null, null, fontSize);
 				setButtonProperties(this.agreeButtons.get(a), confidenceWidth, rowHeight / 2, null, fontSize);
 				setPanelProperties((JPanel) this.agreeButtons.get(a).getParent(), confidenceWidth, rowHeight / 2, null);
 				setLabelProperties(this.submitterLabels.get(a), subCallerWidth, rowHeight / 2, null, null, fontSize);
 				setLabelProperties(this.operatorLabels.get(a), operatorWidth, rowHeight, null, null, fontSize);
 				setLabelProperties(this.callerLabels.get(a), subCallerWidth, rowHeight / 2, null, null, fontSize);
 
 				setPanelProperties((JPanel) this.statusComboBoxes.get(a).getParent(), statusWidth, rowHeight, null);
 				this.statusComboBoxes.get(a).setFont(this.statusComboBoxes.get(a).getFont().deriveFont(fontSize));
 			}
 
 		}
 
 		/**
 		 * Make a new answer row.
 		 */
 		@SuppressWarnings("unchecked")
 		private void makeNewRow() {
 			final int a = this.queuenumberLabels.size();
 
 			// Set up layout constraints
 			final GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.BOTH;
 			constraints.anchor = GridBagConstraints.NORTH;
 			constraints.weightx = 0.0;
 			constraints.weighty = 0.0;
 
 			constraints.gridheight = 1;
 			constraints.gridx = 0;
 			constraints.gridy = 2 * a;
 			this.queuenumberLabels.add(this.enclosedLabel("", timeWidth, rowHeight / 2, null, null, constraints,
 					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
 			this.queuenumberLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridx = 0;
 			constraints.gridy = 2 * a + 1;
 			this.timestampLabels.add(this.enclosedLabel("", timeWidth, rowHeight / 2, null, null, constraints,
 					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
 			this.timestampLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridheight = 2;
 			constraints.gridx = 1;
 			constraints.gridy = 2 * a;
 			this.qNumberLabels.add(this.enclosedLabel("", qNumWidth, rowHeight, null, null, constraints, qNumFontSize,
 					SwingConstants.CENTER, SwingConstants.CENTER));
 			this.qNumberLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridx = 2;
 			constraints.gridy = 2 * a;
 			constraints.weightx = 1.0;
 			this.answerTextAreas.add(this.scrollableTextArea("", answerWidth, rowHeight, null, null, constraints,
 					fontSize, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
 					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED));
 			this.answerTextAreas.get(a).setEditable(false);
 			this.answerTextAreas.get(a).addMouseListener(new PopupListener(this.contextMenu));
 			constraints.weightx = 0.0;
 
 			constraints.gridheight = 1;
 			constraints.gridx = 3;
 			constraints.gridy = 2 * a;
 			this.confidenceLabels.add(this.enclosedLabel("", confidenceWidth, rowHeight / 2, null, null, constraints,
 					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
 			this.confidenceLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridx = 3;
 			constraints.gridy = 2 * a + 1;
 			final JPanel buttonPanel = new JPanel(new GridBagLayout());
 			buttonPanel.setPreferredSize(new Dimension(confidenceWidth, rowHeight / 2));
 			buttonPanel.setMinimumSize(new Dimension(confidenceWidth, rowHeight / 2));
 			this.add(buttonPanel, constraints);
 			this.agreeButtons.add(new JToggleButton("+1"));
 			this.agreeButtons.get(a).setToolTipText("Concur with this answer");
 			this.agreeButtons.get(a).setPreferredSize(new Dimension(confidenceWidth, rowHeight / 2));
 			this.agreeButtons.get(a).setMinimumSize(new Dimension(confidenceWidth, rowHeight / 2));
 			this.agreeButtons.get(a).setBorder(BorderFactory.createEmptyBorder());
 			this.agreeButtons.get(a).setMargin(new Insets(0, 0, 0, 0));
 			this.agreeButtons.get(a).setVisible(true);
 			this.agreeButtons.get(a).setActionCommand("Agree");
 			this.agreeButtons.get(a).addActionListener(this);
 			buttonPanel.add(this.agreeButtons.get(a), buttonConstraints);
 			this.agreements.put(a, false);
 			constraints.gridheight = 2;
 
 			constraints.gridheight = 1;
 			constraints.gridx = 4;
 			constraints.gridy = 2 * a;
 			this.submitterLabels.add(this.enclosedLabel("", subCallerWidth, rowHeight / 2, null, null, constraints,
 					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
 			this.submitterLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridx = 4;
 			constraints.gridy = 2 * a + 1;
 			this.callerLabels.add(this.enclosedLabel("", subCallerWidth, rowHeight / 2, null, null, constraints,
 					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
 			this.callerLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 			constraints.gridheight = 2;
 
 			constraints.gridx = 5;
 			constraints.gridy = 2 * a;
 			this.operatorLabels.add(this.enclosedLabel("", operatorWidth, rowHeight, null, null, constraints, fontSize,
 					SwingConstants.CENTER, SwingConstants.CENTER));
 			this.operatorLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
 
 			constraints.gridx = 6;
 			constraints.gridy = 2 * a;
 			final JPanel panel = new JPanel(new GridBagLayout());
 			panel.setPreferredSize(new Dimension(statusWidth, rowHeight));
 			this.add(panel, constraints);
 			this.statusComboBoxes.add(new JComboBox<String>(STATUSES));
 			this.statusComboBoxes.get(a).setName(a + "");
 			this.statusComboBoxes.get(a).addItemListener(this);
 			this.statusComboBoxes.get(a).setRenderer(
 					new StatusCellRenderer((ListCellRenderer<String>) this.statusComboBoxes.get(a).getRenderer(),
 							this.statusComboBoxes.get(a)));
 			panel.add(this.statusComboBoxes.get(a));
 
 			this.lastStatus.add("new");
 		}
 
 		/**
 		 * A custom renderer to control how the status combo box is displayed.
 		 * 
 		 * @author Walter Kolczynski
 		 * 
 		 */
 		public class StatusCellRenderer implements ListCellRenderer<String> {
 			private final ListCellRenderer<String>	wrapped;
 			private final JComboBox<String>			comboBox;
 
 			public StatusCellRenderer(ListCellRenderer<String> listCellRenderer, JComboBox<String> comboBox) {
 				this.wrapped = listCellRenderer;
 				this.comboBox = comboBox;
 			}
 
 			@Override
 			public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
 					boolean isSelected, boolean cellHasFocus) {
 				final String displayName = String.valueOf(value); // customize here
 				final Component renderer = this.wrapped.getListCellRendererComponent(list, displayName, index,
 						isSelected, cellHasFocus);
 				if (renderer instanceof JLabel) {
 					Color color = notCalledInColor;
 					switch (value) {
 						case "Duplicate":
 							color = duplicateColor;
 							break;
 						case "Not Called In":
 							color = notCalledInColor;
 							break;
 						case "Calling":
 							color = callingColor;
 							break;
 						case "Incorrect":
 							color = incorrectColor;
 							break;
 						case "Partial":
 							color = partialColor;
 							break;
 						case "Correct":
 							color = correctColor;
 							break;
 						default:
 							color = notCalledInColor;
 							break;
 					}
 					if (isSelected) {
 						( (JLabel) renderer ).setForeground(color);
 						( (JLabel) renderer ).setBackground(this.comboBox.getForeground());
 					} else {
 						( (JLabel) renderer ).setForeground(this.comboBox.getForeground());
 						( (JLabel) renderer ).setBackground(color);
 					}
 
 				}
 				return renderer;
 			}
 		}
 
 		/**
 		 * Listener for events to display pop-up menu.
 		 * 
 		 * @author Walter Kolczynski
 		 * 
 		 */
 		private class PopupListener extends MouseAdapter {
 
 			private final JPopupMenu	menu;
 
 			public PopupListener(JPopupMenu menu) {
 				this.menu = menu;
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				this.checkForPopup(e);
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				this.checkForPopup(e);
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				this.checkForPopup(e);
 			}
 
 			private void checkForPopup(MouseEvent event) {
 				final JComponent source = (JComponent) event.getSource();
 				if (event.isPopupTrigger() && !source.getName().equals("")) {
 					this.menu.setName(source.getName());
 					this.menu.show(source, event.getX(), event.getY());
 				}
 			}
 
 		}
 	}
 }

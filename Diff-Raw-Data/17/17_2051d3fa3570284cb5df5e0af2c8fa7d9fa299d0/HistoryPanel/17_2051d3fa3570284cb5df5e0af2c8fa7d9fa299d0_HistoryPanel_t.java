 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 
 /**
  * A panel to select and display any round.
  * 
  * The <code>History</code> class is a panel that allows you to display the question data for any round. There are two
  * parts: a top area that has a selector to choose the desired round, and a <code>RoundQlistPanel</code> that displays
  * the question data for the chosen round.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class HistoryPanel extends TriviaPanel implements ItemListener {
 
 	/** The Constant serialVersionUID. */
 	final private static long		serialVersionUID			= -5094201314926851039L;
 
 	/** Font size for the round selector */
 	final private static float		ROUND_FONT_SIZE				= 20.0f;
 
 	/**
 	 * Colors
 	 */
 	final private static Color		TOPLINE_BACKGROUND_COLOR	= Color.BLACK;
 	final private static Color		ROUND_COLOR					= Color.YELLOW;
 	final private static Color		SELECTOR_BACKGROUND_COLOR	= Color.WHITE;
 	private static final Color		TOTAL_COLOR					= Color.RED;
 	private static final Color		ANNOUNCED_COLOR				= Color.ORANGE;
 
 	/**
 	 * Sizes
 	 */
 	final private static int		SELECTOR_ROW_HEIGHT			= 30;
	final private static int		SELECTOR_HEIGHT				= 30;
	final private static int		SELECTOR_WIDTH				= 50;
 
 	private static final int		ROUND_LABEL_WIDTH			= 90;
 	private static final int		ROUND_WIDTH					= 110;
 	private static final int		TOTAL_LABEL_WIDTH			= 75;
 	private static final int		TOTAL_WIDTH					= 180;
 	private static final int		PLACE_LABEL_WIDTH			= 80;
 	private static final int		PLACE_WIDTH					= 120;
 
 
 	/**
 	 * GUI Elements that will need to be updated
 	 */
 	private final JComboBox<String>	roundSelector;
 	private final RoundQlistPanel	roundQListPanel;
 	private final JLabel			earnedLabel, valueLabel, placeLabel;
 
 	/**
 	 * Data
 	 */
 	private final int				nRounds;
 
 	/**
 	 * Data sources
 	 */
 	TriviaInterface					server;
 	TriviaClient					client;
 
 	/**
 	 * Instantiates a new history panel.
 	 * 
 	 * @param server
 	 *            The remote trivia server
 	 * @param client
 	 *            The local trivia client
 	 */
 	public HistoryPanel(TriviaInterface server, TriviaClient client) {
 
 		super();
 
 		this.server = server;
 		this.client = client;
 		this.nRounds = client.getTrivia().getNRounds();
 
 		// Set up layout constraints
 		final GridBagConstraints solo = new GridBagConstraints();
 		solo.fill = GridBagConstraints.NONE;
 		solo.anchor = GridBagConstraints.CENTER;
 		solo.weightx = 1.0;
 		solo.weighty = 1.0;
 		solo.gridx = 0;
 		solo.gridy = 0;
 
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		/**
 		 * Create the top row with the selector combo box
 		 */
 		constraints.weightx = 0.0;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		this.enclosedLabel(" Round:", ROUND_LABEL_WIDTH, SELECTOR_ROW_HEIGHT, ROUND_COLOR, TOPLINE_BACKGROUND_COLOR,
 				constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		this.earnedLabel = this.enclosedLabel("", ROUND_WIDTH, SELECTOR_ROW_HEIGHT, ROUND_COLOR,
 				TOPLINE_BACKGROUND_COLOR, constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 
 		constraints.weightx = 1.0;
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		this.enclosedLabel("", -1, SELECTOR_ROW_HEIGHT, ROUND_COLOR, TOPLINE_BACKGROUND_COLOR, constraints,
 				ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		this.enclosedLabel("Total: ", TOTAL_LABEL_WIDTH, SELECTOR_ROW_HEIGHT, TOTAL_COLOR, TOPLINE_BACKGROUND_COLOR,
 				constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		this.valueLabel = this.enclosedLabel("", TOTAL_WIDTH, SELECTOR_ROW_HEIGHT, TOTAL_COLOR,
 				TOPLINE_BACKGROUND_COLOR, constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 
 		constraints.weightx = 1.0;
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		this.enclosedLabel("", -1, SELECTOR_ROW_HEIGHT, ROUND_COLOR, TOPLINE_BACKGROUND_COLOR, constraints,
 				ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 7;
 		constraints.gridy = 0;
 		this.enclosedLabel("Place: ", PLACE_LABEL_WIDTH, SELECTOR_ROW_HEIGHT, ANNOUNCED_COLOR,
 				TOPLINE_BACKGROUND_COLOR, constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 
 		constraints.gridx = 8;
 		constraints.gridy = 0;
 		this.placeLabel = this.enclosedLabel("", PLACE_WIDTH, SELECTOR_ROW_HEIGHT, ANNOUNCED_COLOR,
 				TOPLINE_BACKGROUND_COLOR, constraints, ROUND_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		constraints.weightx = 0.0;
 		final JPanel panel = new JPanel();
 		panel.setBackground(TOPLINE_BACKGROUND_COLOR);
 		this.add(panel, constraints);
 		final String[] rNumbers = new String[this.nRounds];
 		for (int r = 0; r < this.nRounds; r++) {
 			rNumbers[r] = ( r + 1 ) + "";
 		}
 		this.roundSelector = new JComboBox<String>(rNumbers);
 		this.roundSelector.addItemListener(this);
 		this.roundSelector.setBackground(SELECTOR_BACKGROUND_COLOR);
 		this.roundSelector.setPreferredSize(new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT));
 		this.roundSelector.setMinimumSize(new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT));
 		panel.add(this.roundSelector, solo);
 
 		/**
 		 * Add a question list panel to show the selected round data
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 9;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.roundQListPanel = new RoundQlistPanel(server, client, false, 1);
 		this.add(this.roundQListPanel, constraints);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public synchronized void itemStateChanged(ItemEvent e) {
 		final JComboBox<String> source = (JComboBox<String>) e.getSource();
 		final int rNumber = Integer.parseInt((String) source.getSelectedItem());
 		this.roundQListPanel.setRound(rNumber);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public synchronized void update(boolean force) {
 		final Trivia trivia = this.client.getTrivia();
 		final int rNumber = Integer.parseInt((String) this.roundSelector.getSelectedItem());
 
 		this.earnedLabel.setText(trivia.getEarned(rNumber) + " / " + trivia.getValue(rNumber));
 		this.valueLabel.setText(trivia.getCumulativeEarned(rNumber) + " / " + trivia.getCumulativeValue(rNumber));
 		if (trivia.isAnnounced(rNumber)) {
 			this.placeLabel.setText(TriviaClient.ordinalize(trivia.getAnnouncedPlace(rNumber)) + " / "
 					+ trivia.getNTeams() + " ");
 		} else {
 			this.placeLabel.setText("-- / " + trivia.getNTeams() + " ");
 		}
 
 		this.roundQListPanel.update(force);
 	}
 }

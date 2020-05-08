 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 
 /**
  * A panel that displays the scores from each round.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class ScoreByRoundPanel extends TriviaPanel {
 
 	/** The Constant serialVersionUID. */
 	private static final long			serialVersionUID		= -2047479093232798581L;
 
 	/**
 	 * Colors
 	 */
 	final private static Color			HEADER_BACKGROUND_COLOR	= Color.BLACK;
 	final private static Color			BACKGROUND_COLOR		= Color.DARK_GRAY;
 	final private static Color			ALT_BACKGROUND_COLOR	= Color.BLACK;
 	final private static Color			HOUR_COLOR				= Color.WHITE;
 	final private static Color			EARNED_COLOR			= Color.GREEN;
 	final private static Color			VALUE_COLOR				= new Color(30, 144, 255);
 	final private static Color			PERCENT_COLOR			= Color.PINK;
 	final private static Color			CUMULATIVE_EARNED_COLOR	= Color.GREEN.brighter();
 	final private static Color			CUMULATIVE_VALUE_COLOR	= ( new Color(30, 144, 255) ).brighter();
 	final private static Color			PERCENT_TOTAL_COLOR		= Color.PINK;
 	final private static Color			ANNOUNCED_COLOR			= Color.YELLOW;
 	final private static Color			PLACE_COLOR				= Color.ORANGE;
 	final private static Color			DISCREPENCY_COLOR		= Color.MAGENTA;
 
 	/** Interval specifying how often the alternate color should be used */
 	final private static int			ALT_INTERVAL			= 5;
 
 	/**
 	 * Sizes
 	 */
 	final private static int			HEADER_HEIGHT			= 28;
 	final private static int			ROW_HEIGHT				= 12;
 
 	final private static int			HOUR_WIDTH				= 35;
 	final private static int			EARNED_WIDTH			= 65;
 	final private static int			VALUE_WIDTH				= 65;
 	final private static int			PERCENT_WIDTH			= 80;
 	final private static int			CUMULATIVE_EARNED_WIDTH	= 80;
 	final private static int			CUMULATIVE_VALUE_WIDTH	= 80;
 	final private static int			PERCENT_TOTAL_WIDTH		= 80;
 	final private static int			ANNOUNCED_WIDTH			= 85;
 	final private static int			PLACE_WIDTH				= 40;
 	final private static int			DISCREPENCY_WIDTH		= 12;
 
 	/**
 	 * Font sizes
 	 */
 	final private static float			HEADER_FONT_SIZE		= 12.0f;
 	final private static float			DATA_FONT_SIZE			= 18.0f;
 
 	/** The scroll panel that will hold the round data */
 	final private InternalScrollPanel	internalScrollPanel;
 
 	/**
 	 * Instantiates a new score by round panel.
 	 * 
 	 * @param server
 	 *            The remote trivia server
 	 * @param client
 	 *            The local trivia client
 	 * 
 	 */
 	public ScoreByRoundPanel(TriviaInterface server, TriviaClient client) {
 
 		super();
 
 		// Set up the layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.NORTH;
 
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		/**
 		 * Create the header
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		this.enclosedLabel("", HOUR_WIDTH, HEADER_HEIGHT / 2, HOUR_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		this.enclosedLabel("Hour", HOUR_WIDTH, HEADER_HEIGHT / 2, HOUR_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		this.enclosedLabel("", EARNED_WIDTH, HEADER_HEIGHT / 2, EARNED_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		this.enclosedLabel("Earned", EARNED_WIDTH, HEADER_HEIGHT / 2, EARNED_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		this.enclosedLabel("", VALUE_WIDTH, HEADER_HEIGHT / 2, VALUE_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 2;
 		constraints.gridy = 1;
 		this.enclosedLabel("Possible", VALUE_WIDTH, HEADER_HEIGHT / 2, VALUE_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		this.enclosedLabel("", PERCENT_WIDTH, HEADER_HEIGHT / 2, PERCENT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 3;
 		constraints.gridy = 1;
 		this.enclosedLabel("Percent", PERCENT_WIDTH, HEADER_HEIGHT / 2, PERCENT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		this.enclosedLabel("Cumulative", CUMULATIVE_EARNED_WIDTH, HEADER_HEIGHT / 2, CUMULATIVE_EARNED_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 4;
 		constraints.gridy = 1;
 		this.enclosedLabel("Score", CUMULATIVE_EARNED_WIDTH, HEADER_HEIGHT / 2, CUMULATIVE_EARNED_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		this.enclosedLabel("Cumulative", CUMULATIVE_VALUE_WIDTH, HEADER_HEIGHT / 2, CUMULATIVE_VALUE_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 5;
 		constraints.gridy = 1;
 		this.enclosedLabel("Possible", CUMULATIVE_VALUE_WIDTH, HEADER_HEIGHT / 2, CUMULATIVE_VALUE_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		this.enclosedLabel("Percent", PERCENT_TOTAL_WIDTH, HEADER_HEIGHT / 2, PERCENT_TOTAL_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 6;
 		constraints.gridy = 1;
 		this.enclosedLabel("Total", PERCENT_TOTAL_WIDTH, HEADER_HEIGHT / 2, PERCENT_TOTAL_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 7;
 		constraints.gridy = 0;
 		this.enclosedLabel("Announced", ANNOUNCED_WIDTH, HEADER_HEIGHT / 2, ANNOUNCED_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 7;
 		constraints.gridy = 1;
 		this.enclosedLabel("Score", ANNOUNCED_WIDTH, HEADER_HEIGHT / 2, ANNOUNCED_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 8;
 		constraints.gridy = 0;
 		this.enclosedLabel("", PLACE_WIDTH, HEADER_HEIGHT / 2, PLACE_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 8;
 		constraints.gridy = 1;
 		this.enclosedLabel("Place", PLACE_WIDTH, HEADER_HEIGHT / 2, PLACE_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.weightx = 1.0;
 		constraints.gridx = 9;
 		constraints.gridy = 0;
 		this.enclosedLabel("", DISCREPENCY_WIDTH, HEADER_HEIGHT / 2, DISCREPENCY_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.gridx = 9;
 		constraints.gridy = 1;
 		this.enclosedLabel("Discrepency Notes", DISCREPENCY_WIDTH, HEADER_HEIGHT / 2, DISCREPENCY_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.weightx = 0.00;
 
 		/**
 		 * Create a scroll panel with the round data the round data
 		 */
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		constraints.gridwidth = 11;
 		this.internalScrollPanel = new InternalScrollPanel(server, client);
 		final JScrollPane scrollPane = new JScrollPane(this.internalScrollPanel,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(200, 200));
 		scrollPane.setBorder(BorderFactory.createEmptyBorder());
 		this.add(scrollPane, constraints);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public void update(boolean force) {
 		this.internalScrollPanel.update(force);
 	}
 
 	/**
 	 * Scroll panel that contains the score data for every round
 	 */
 	private class InternalScrollPanel extends TriviaPanel implements ActionListener {
 
 		/** The Constant serialVersionUID. */
 		private static final long		serialVersionUID	= 7121481355244434308L;
 
 		/**
 		 * GUI elements that update
 		 */
 		final private JLabel[]			earnedLabels, valueLabels, percentLabels, cumulativeEarnedLabels,
 				cumulativeValueLabels, percentTotalLabels, announcedScoreLabels, placeLabels;
 		final private JTextField[]		discrepancyTextField;
 
 		/** The nunber of rounds */
 		final private int				nRounds;
 
 		private final String[]			discrepancies;
 
 		/** Data sources */
 		final private TriviaInterface	server;
 		final private TriviaClient		client;
 
 		/**
 		 * Instantiates a new internal scroll panel.
 		 * 
 		 * @param server
 		 *            the server
 		 * @param client
 		 *            the client application
 		 */
 		public InternalScrollPanel(TriviaInterface server, TriviaClient client) {
 			super();
 
 			this.server = server;
 			this.client = client;
 
 			this.nRounds = client.getTrivia().getNRounds();
 
 			// Set up layout constraints
 			final GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.BOTH;
 			constraints.anchor = GridBagConstraints.NORTH;
 			constraints.weighty = 0.0;
 
 			this.earnedLabels = new JLabel[this.nRounds];
 			this.valueLabels = new JLabel[this.nRounds];
 			this.percentLabels = new JLabel[this.nRounds];
 			this.cumulativeEarnedLabels = new JLabel[this.nRounds];
 			this.cumulativeValueLabels = new JLabel[this.nRounds];
 			this.percentTotalLabels = new JLabel[this.nRounds];
 			this.announcedScoreLabels = new JLabel[this.nRounds];
 			this.placeLabels = new JLabel[this.nRounds];
 			this.discrepancyTextField = new JTextField[this.nRounds];
 			this.discrepancies = new String[this.nRounds];
 
 			// Create the labels for each round
 			for (int r = 0; r < this.nRounds; r++) {
 
 				constraints.weightx = 0.0;
 				constraints.weighty = 0.0;
 
 				Color bColor = BACKGROUND_COLOR;
 				if (( r + 1 ) % ALT_INTERVAL == 0) {
 					bColor = ALT_BACKGROUND_COLOR;
 				}
 
 				constraints.gridx = 0;
 				constraints.gridy = r;
 				this.enclosedLabel(( r + 1 ) + "", HOUR_WIDTH, ROW_HEIGHT, HOUR_COLOR, bColor, constraints,
 						DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 1;
 				constraints.gridy = r;
 				this.earnedLabels[r] = this.enclosedLabel("", EARNED_WIDTH, ROW_HEIGHT, EARNED_COLOR, bColor,
 						constraints, DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 2;
 				constraints.gridy = r;
 				this.valueLabels[r] = this.enclosedLabel("", VALUE_WIDTH, ROW_HEIGHT, VALUE_COLOR, bColor, constraints,
 						DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 3;
 				constraints.gridy = r;
 				this.percentLabels[r] = this.enclosedLabel("", PERCENT_WIDTH, ROW_HEIGHT, PERCENT_COLOR, bColor,
 						constraints, DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 4;
 				constraints.gridy = r;
 				this.cumulativeEarnedLabels[r] = this.enclosedLabel("", CUMULATIVE_EARNED_WIDTH, ROW_HEIGHT,
 						CUMULATIVE_EARNED_COLOR, bColor, constraints, DATA_FONT_SIZE, SwingConstants.CENTER,
 						SwingConstants.CENTER);
 
 				constraints.gridx = 5;
 				constraints.gridy = r;
 				this.cumulativeValueLabels[r] = this.enclosedLabel("", CUMULATIVE_VALUE_WIDTH, ROW_HEIGHT,
 						CUMULATIVE_VALUE_COLOR, bColor, constraints, DATA_FONT_SIZE, SwingConstants.CENTER,
 						SwingConstants.CENTER);
 
 				constraints.gridx = 6;
 				constraints.gridy = r;
 				this.percentTotalLabels[r] = this.enclosedLabel("", PERCENT_TOTAL_WIDTH, ROW_HEIGHT,
 						PERCENT_TOTAL_COLOR, bColor, constraints, DATA_FONT_SIZE, SwingConstants.CENTER,
 						SwingConstants.CENTER);
 
 				constraints.gridx = 7;
 				constraints.gridy = r;
 				this.announcedScoreLabels[r] = this.enclosedLabel("", ANNOUNCED_WIDTH, ROW_HEIGHT, ANNOUNCED_COLOR,
 						bColor, constraints, DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 8;
 				constraints.gridy = r;
 				this.placeLabels[r] = this.enclosedLabel("", PLACE_WIDTH, ROW_HEIGHT, PLACE_COLOR, bColor, constraints,
 						DATA_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.weightx = 1.0;
 				constraints.gridx = 9;
 				constraints.gridy = r;
 				this.discrepancyTextField[r] = new JTextField("", 10);
 				this.discrepancyTextField[r].setBackground(bColor.brighter());
 				this.discrepancyTextField[r].setForeground(DISCREPENCY_COLOR);
 				this.discrepancyTextField[r].setBorder(BorderFactory.createEmptyBorder());
 				this.discrepancyTextField[r].setFont(this.discrepancyTextField[r].getFont().deriveFont(DATA_FONT_SIZE));
 				this.discrepancyTextField[r].addActionListener(this);
 				this.add(this.discrepancyTextField[r], constraints);
 
 				this.discrepancies[r] = "";
 
 			}
 
 			// Add a blank row at the bottom as a spacer
 			constraints.weightx = 1.0;
 			constraints.weighty = 1.0;
 			constraints.gridx = 0;
 			constraints.gridy = this.nRounds;
 			constraints.gridwidth = 11;
 			final JPanel panel = new JPanel(new GridBagLayout());
 			panel.setBackground(HEADER_BACKGROUND_COLOR);
 			panel.setPreferredSize(new Dimension(0, 0));
 			this.add(panel, constraints);
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		@Override
 		public synchronized void actionPerformed(ActionEvent e) {
 			/**
 			 * This method isn't working as I'd hoped, need to replace
 			 */
 			final JTextField source = (JTextField) e.getSource();
 			for (int r = 0; r < this.nRounds; r++) {
 				if (source.equals(this.discrepancyTextField[r])) {
 					int tryNumber = 0;
 					boolean success = false;
 					while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 						tryNumber++;
 						try {
 							this.server.setDiscrepancyText(this.client.getUser(), r + 1, source.getText());
 							success = true;
 						} catch (final Exception exception) {
 							this.client.log("Couldn't set discrepancy text on server (try #" + tryNumber + ").");
 						}
 					}
 
 					if (!success) {
 						this.client.disconnected();
 						return;
 					}
 
 					return;
 				}
 			}
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see net.bubbaland.trivia.TriviaPanel#update()
 		 */
 		@Override
 		public void update(boolean force) {
 			// Get the current Trivia data object
 			final Trivia trivia = this.client.getTrivia();
 
 			int cumulativeEarned = 0;
 			int cumulativeValue = 0;
 			for (int r = 0; r < this.nRounds; r++) {
 				// Read in the data
 				final int earned = trivia.getEarned(r + 1);
 				final int value = trivia.getValue(r + 1);
 				final int announced = trivia.getAnnouncedPoints(r + 1);
 				final int place = trivia.getAnnouncedPlace(r + 1);
 				final String discrepancy = trivia.getDiscrepancyText(r + 1);
 				final boolean isAnnounced = trivia.isAnnounced(r + 1);
 				cumulativeEarned += earned;
 				cumulativeValue += value;
 
				if (value != 0) {
					final boolean updated = !( this.earnedLabels[r].getText().equals(earned + "")
							&& this.valueLabels[r].getText().equals(value + "")
							&& this.cumulativeEarnedLabels[r].getText().equals(cumulativeEarned + "")
							&& this.cumulativeValueLabels[r].getText().equals(cumulativeValue + "")
							&& ( this.announcedScoreLabels[r].getText().equals(announced + "") || !isAnnounced )
							&& ( this.placeLabels[r].getText().equals(TriviaClient.ordinalize(place)) || !isAnnounced ) && this.discrepancies[r]
								.equals(discrepancy) ) || force;

					if (updated) {
 						// If the round has started, update all of the labels for the round
 						final String percent = String.format("%04.1f", ( earned * 100.0 / value )) + "%";
 						final String percentTotal = String.format("%04.1f",
 								( cumulativeEarned * 100.0 / cumulativeValue )) + "%";
 						this.earnedLabels[r].setText(earned + "");
 						this.valueLabels[r].setText(value + "");
 						this.percentLabels[r].setText(percent);
 						this.cumulativeEarnedLabels[r].setText(cumulativeEarned + "");
 						this.cumulativeValueLabels[r].setText(cumulativeValue + "");
 						this.percentTotalLabels[r].setText(percentTotal);
 						if (isAnnounced) {
 							this.announcedScoreLabels[r].setText(announced + "");
 							this.placeLabels[r].setText(TriviaClient.ordinalize(place));
 						}
 						this.discrepancyTextField[r].setText(discrepancy);
 						this.discrepancies[r] = discrepancy;
 					}
 				}
 
 			}
 
 		}
 
 	}
 
 
 }

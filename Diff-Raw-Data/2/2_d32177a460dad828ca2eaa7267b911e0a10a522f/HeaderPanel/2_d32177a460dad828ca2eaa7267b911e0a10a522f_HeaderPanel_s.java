 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.SwingConstants;
 
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 
 /**
  * A panel which displays summary information of the trivia contest.
  * 
  * The <code>HeaderPanel</code> class is a panel that contains summary information about the current state of the trivia
  * contest and of the current round. It also provides buttons to make the current round a speed round (or not) and
  * advance to a new round.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class HeaderPanel extends TriviaPanel implements ActionListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long		serialVersionUID		= 3544918496657028139L;
 
 	/**
 	 * Colors
 	 */
 	protected static final Color	BACKGROUND_COLOR		= Color.BLACK;
 	private static final Color		LABEL_COLOR				= Color.WHITE;
 	private static final Color		EARNED_COLOR			= Color.GREEN;
 	private static final Color		VALUE_COLOR				= new Color(30, 144, 255);
 	private static final Color		ANNOUNCED_COLOR			= Color.ORANGE;
 	private static final Color		SPEED_COLOR				= Color.RED;
 	private static final Color		NEW_ROUND_COLOR			= Color.YELLOW;
 	private static final Color		CONFLICT_COLOR			= Color.RED;
 
 	/**
 	 * Sizes
 	 */
 	private static final int		TOP_ROW_HEIGHT			= 24;
 	private static final int		MIDDLE_ROW_HEIGHT		= 30;
 	private static final int		BOTTOM_ROW_HEIGHT		= 30;
 
 	private static final int		COL0_WIDTH				= 85;
 	private static final int		COL1_WIDTH				= 90;
 	private static final int		COL2_WIDTH				= 100;
 	private static final int		COL3_WIDTH				= 120;
 	private static final int		COL4_WIDTH				= 60;
 	private static final int		COL5_WIDTH				= 75;
 
 	/**
 	 * Font sizes
 	 */
 	private static final float		LABEL_FONT_SIZE			= (float) 18.0;
 	private static final float		POINT_FONT_SIZE			= (float) 28.0;
 
 	/**
 	 * Button sizes
 	 */
 	private static final int		CENTER_BUTTON_WIDTH		= 110;
 	private static final int		CENTER_BUTTON_HEIGHT	= BOTTOM_ROW_HEIGHT - 4;
 	private static final int		CONFLICT_BUTTON_WIDTH	= 110;
 	private static final int		CONFLICT_BUTTON_HEIGHT	= TOP_ROW_HEIGHT - 4;
 
 	/**
 	 * GUI Elements that will need to be updated
 	 */
 	private final JLabel			roundEarnedLabel, roundValueLabel, totalEarnedLabel;
 	private final JLabel			totalValueLabel, announcedLabel, placeLabel;
 	private final JLabel			announcedBannerLabel, scoreTextLabel, placeTextLabel;
 	private final JLabel			currentHourLabel;
 	private final JToggleButton		speedButton;
 	private final JButton			newRoundButton, conflictButton;
 	private final UserListPanel		userListPanel;
 
 	/**
 	 * Data sources
 	 */
 	private final TriviaInterface	server;
 	private final TriviaClient		client;
 
 	/**
 	 * Instantiates a new header panel.
 	 * 
 	 * @param server
 	 *            The remote trivia server
 	 * @param client
 	 *            The local trivia client
 	 */
 	public HeaderPanel(TriviaInterface server, TriviaClient client) {
 
 		super();
 
 		this.server = server;
 		this.client = client;
 
 		// Set up layout constraints
 		final GridBagConstraints buttonConstraints = new GridBagConstraints();
 		buttonConstraints.anchor = GridBagConstraints.CENTER;
 		buttonConstraints.weightx = 1.0;
 		buttonConstraints.weighty = 1.0;
 		buttonConstraints.gridx = 0;
 		buttonConstraints.gridy = 0;
 		buttonConstraints.fill = GridBagConstraints.NONE;
 
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 1.0;
 
 		/**
 		 * Top row
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		JPanel panel = new JPanel(new GridBagLayout());
 		panel.setPreferredSize(new Dimension(COL0_WIDTH, TOP_ROW_HEIGHT));
 		panel.setBackground(BACKGROUND_COLOR);
 		this.add(panel, constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		this.enclosedLabel("Round", COL1_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		this.enclosedLabel("Total", COL2_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		constraints.weightx = 1.0;
 		this.enclosedLabel(client.getTrivia().getTeamName(), COL3_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR,
 				constraints, LABEL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		constraints.gridwidth = 2;
 		this.announcedBannerLabel = this.enclosedLabel("Last Round ", COL4_WIDTH, TOP_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 		
 		this.conflictButton = new JButton("Conflict!");
 		this.conflictButton.setMargin(new Insets(0, 0, 0, 0));
 		this.conflictButton.setPreferredSize(new Dimension(CONFLICT_BUTTON_WIDTH, CONFLICT_BUTTON_HEIGHT));
 		this.conflictButton.setMinimumSize(new Dimension(CONFLICT_BUTTON_WIDTH, CONFLICT_BUTTON_HEIGHT));
 		this.conflictButton.setVisible(false);
 //		this.conflictButton.setBackground(NEW_ROUND_COLOR);
 		this.conflictButton.setFont(this.conflictButton.getFont().deriveFont(LABEL_FONT_SIZE));
 		this.announcedBannerLabel.getParent().add(this.conflictButton, buttonConstraints);
 		this.conflictButton.addActionListener(this);
 
 		constraints.gridwidth = 1;
 		
 
 		/**
 		 * Middle row
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		this.enclosedLabel("Earned", COL0_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		this.roundEarnedLabel = this.enclosedLabel("", COL1_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR, BACKGROUND_COLOR,
 				constraints, POINT_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 2;
 		constraints.gridy = 1;
 		this.totalEarnedLabel = this.enclosedLabel("", COL2_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR, BACKGROUND_COLOR,
 				constraints, POINT_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 3;
 		constraints.gridy = 1;
 		this.currentHourLabel = this.enclosedLabel("", COL3_WIDTH, MIDDLE_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR,
 				constraints, LABEL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 4;
 		constraints.gridy = 1;
 		this.scoreTextLabel = this.enclosedLabel("Points ", COL4_WIDTH, MIDDLE_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 1;
 		this.announcedLabel = this.enclosedLabel("", COL5_WIDTH, MIDDLE_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR,
 				constraints, LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		/**
 		 * Bottom row
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		this.enclosedLabel("Possible", COL0_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 1;
 		constraints.gridy = 2;
 		this.roundValueLabel = this.enclosedLabel("", COL1_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR,
 				constraints, POINT_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 2;
 		constraints.gridy = 2;
 		this.totalValueLabel = this.enclosedLabel("", COL2_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR,
 				constraints, POINT_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 3;
 		constraints.gridy = 2;
 
 		// Put both the speed button and new round button in the same place, we'll hide the one we don't need
 		panel = new JPanel(new GridBagLayout());
 		panel.setPreferredSize(new Dimension(COL3_WIDTH, BOTTOM_ROW_HEIGHT));
 		panel.setMinimumSize(new Dimension(COL3_WIDTH, BOTTOM_ROW_HEIGHT));
 		panel.setBackground(BACKGROUND_COLOR);
 		this.add(panel, constraints);
 
 		this.speedButton = new JToggleButton("");
 		this.speedButton.setMargin(new Insets(0, 0, 0, 0));
 		this.speedButton.setPreferredSize(new Dimension(CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT));
 		this.speedButton.setMinimumSize(new Dimension(CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT));
 		this.speedButton.setVisible(true);
 		this.speedButton.setFont(this.speedButton.getFont().deriveFont(LABEL_FONT_SIZE));
 		panel.add(this.speedButton, buttonConstraints);
 		this.speedButton.addActionListener(this);
 
 		this.newRoundButton = new JButton("New Round");
 		this.newRoundButton.setMargin(new Insets(0, 0, 0, 0));
 		this.newRoundButton.setPreferredSize(new Dimension(CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT));
 		this.newRoundButton.setMinimumSize(new Dimension(CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT));
 		this.newRoundButton.setVisible(false);
 		this.newRoundButton.setBackground(NEW_ROUND_COLOR);
 		this.newRoundButton.setFont(this.newRoundButton.getFont().deriveFont(LABEL_FONT_SIZE));
 		panel.add(this.newRoundButton, buttonConstraints);
 		this.newRoundButton.addActionListener(this);
 
 		constraints.gridx = 4;
 		constraints.gridy = 2;
 		this.placeTextLabel = this.enclosedLabel("Place ", COL4_WIDTH, BOTTOM_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR, constraints,
 				LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 2;
 		this.placeLabel = this.enclosedLabel("", COL5_WIDTH, BOTTOM_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR,
 				constraints, LABEL_FONT_SIZE, SwingConstants.RIGHT, SwingConstants.CENTER);
 
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		constraints.gridheight = 3;
 		this.userListPanel = new UserListPanel(client);
 		this.userListPanel.setBackground(BACKGROUND_COLOR);
 		this.add(this.userListPanel, constraints);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public synchronized void actionPerformed(ActionEvent event) {
 		final JComponent source = (JComponent) event.getSource();
 		if (source.equals(this.speedButton)) {
 			// Speed button changed
 			if (this.speedButton.isSelected()) {
 				// Speed button now pressed, tell server
 				int tryNumber = 0;
 				boolean success = false;
 				while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 					tryNumber++;
 					try {
 						this.server.setSpeed(this.client.getUser());
 						success = true;
 					} catch (final Exception e) {
 						this.client.log("Couldn't make this a speed round (try #" + tryNumber + ").");
 					}
 
 					if (!success) {
 						this.client.disconnected();
 						return;
 					}
 
 					this.client.log("Made this a speed round.");
 				}
 
 			} else {
 				// Speed button now not pressed, tell server
 				int tryNumber = 0;
 				boolean success = false;
 				while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 					tryNumber++;
 					try {
 						this.server.unsetSpeed(this.client.getUser());
 						success = true;
 					} catch (final RemoteException e) {
 						this.client.log("Couldn't make this a normal round (try #" + tryNumber + ").");
 						return;
 					}
 				}
 
 				if (!success) {
 					this.client.disconnected();
 					return;
 				}
 
 				this.client.log("Made this a normal round");
 
 			}
 		} else if (source.equals(this.newRoundButton)) {
 			// New round button pressed, tell server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 				tryNumber++;
 				try {
 					this.server.newRound(this.client.getUser());
 					success = true;
 				} catch (final Exception e) {
 					this.client.log("Couldn't get current round number from server (try #" + tryNumber + ").");
 				}
 
 			}
 
 			if (!success) {
 				this.client.log("Connection failed!");
 				return;
 			}
 
 			this.client.log("Started new round");
 
 		} else if (source.equals(this.conflictButton)) {
 			new ConflictDialog(this.client);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public synchronized void update(boolean force) {
 		// Get the current Trivia object from the client
 		final Trivia trivia = this.client.getTrivia();
 
 		// Get the current round
 		final int currentRound = trivia.getCurrentRoundNumber();
 
 		// Update all the labels to match the current data
 		this.roundEarnedLabel.setText("" + trivia.getCurrentRoundEarned());
 		this.totalEarnedLabel.setText("" + trivia.getEarned());
 		this.roundValueLabel.setText("" + trivia.getCurrentRoundValue());
 		this.totalValueLabel.setText("" + trivia.getValue());
 		this.currentHourLabel.setText("Current Round: " + currentRound);
 
 		// Only show announced values once they've been announced
 		if (trivia.isAnnounced(currentRound - 1)) {
 			final int announcedPoints = trivia.getAnnouncedPoints(currentRound - 1);
 			this.announcedLabel.setText("" + announcedPoints);
			this.placeLabel.setText("" + trivia.getAnnouncedPlace(currentRound - 1));
 			if (announcedPoints != trivia.getCumulativeEarned(currentRound - 1)) {
 				this.announcedBannerLabel.getParent().setBackground(CONFLICT_COLOR);
 				this.scoreTextLabel.getParent().setBackground(CONFLICT_COLOR);
 				this.placeTextLabel.getParent().setBackground(CONFLICT_COLOR);
 				this.announcedLabel.getParent().setBackground(CONFLICT_COLOR);
 				this.placeLabel.getParent().setBackground(CONFLICT_COLOR);
 				
 				this.announcedBannerLabel.setVisible(false);
 				this.conflictButton.setVisible(true);				
 			} else {
 				this.announcedBannerLabel.getParent().setBackground(BACKGROUND_COLOR);
 				this.scoreTextLabel.getParent().setBackground(BACKGROUND_COLOR);
 				this.placeTextLabel.getParent().setBackground(BACKGROUND_COLOR);
 				this.announcedLabel.getParent().setBackground(BACKGROUND_COLOR);
 				this.placeLabel.getParent().setBackground(BACKGROUND_COLOR);
 				
 				this.announcedBannerLabel.setVisible(true);
 				this.conflictButton.setVisible(false);
 			}
 
 		} else {
 			this.announcedLabel.setText("");
 			this.placeLabel.setText("");
 		}
 
 		// If the round is over, hide speed round button and show new round button
 		if (trivia.roundOver()) {
 			this.speedButton.setVisible(false);
 			this.newRoundButton.setVisible(true);
 		} else {
 			this.speedButton.setVisible(true);
 			this.newRoundButton.setVisible(false);
 			if (trivia.isCurrentSpeed()) {
 				this.speedButton.setText("Speed");
 				this.speedButton.setSelected(true);
 				this.speedButton.setForeground(SPEED_COLOR);
 			} else {
 				this.speedButton.setText("Normal");
 				this.speedButton.setSelected(false);
 				this.speedButton.setForeground(Color.BLACK);
 			}
 		}
 
 		this.userListPanel.update(force);
 
 	}
 
 }

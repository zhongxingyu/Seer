 package YahtzeeGame;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.EventListenerList;
 
 import YahtzeeGame.Categories.Category;
 import YahtzeeGame.Components.Resettable;
 import YahtzeeGame.Components.ScoreGroup;
 import YahtzeeGame.Components.StaticScoreGroup;
 import YahtzeeGame.Components.YahtzeeDice;
 
 /**
  * Class representing a scoreboard for a game of Yahtzee This class wraps a
  * series of Yahtzee ScoreGroup objects and provides logic for the game itself.
  * 
  * @author Ryan Harrison
  * 
  */
 class ScoreBoard extends JPanel implements Resettable
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * A list of listeners for use when adding ChangeListeners to the class and
 	 * then firing the listeners stateChanged method.
 	 */
 	private final EventListenerList listenerList;
 
 	/**
 	 * A series of score groups that this ScoreBoard displays and handles
 	 */
 	private ScoreGroup[] categories;
 
 	/**
 	 * The set of dice used to display scores for each score group
 	 */
 	private YahtzeeDice[] dice;
 
 	/**
 	 * The Player this ScoreBoard applies to
 	 */
 	private Player player;
 
 	/**
 	 * A panel housing the higher categories of a game of Yahtzee (e.g ones,
 	 * twos etc)
 	 */
 	private JPanel higherCategories;
 
 	/**
 	 * A Panel housing the lower categories of a game of Yahtzee (e.g full
 	 * house, chance)
 	 */
 	private JPanel lowerCategories;
 
 	/**
 	 * A series of static score groups which cannot be chosen by the user. They
 	 * exist simply to display totals and bonuses to the user
 	 */
 	private StaticScoreGroup upperSectionBonus;
 	private StaticScoreGroup upperSectionTotal;
 	private StaticScoreGroup lowerSectionYahtzeeBonus;
 	private StaticScoreGroup grandTotal;
 
 	/**
 	 * Create a new ScoreBoard object with specified Player and set of dice
 	 * 
 	 * @param player
 	 *            The Player this ScoreBoard applies to
 	 * @param dice
 	 *            The set of dice the Player is using to play
 	 */
 	public ScoreBoard(Player player, YahtzeeDice[] dice)
 	{
 		// Initialise size and layout
 		this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
 		this.setPreferredSize(new Dimension(750, 300));
 
 		// Initialise fields
 		listenerList = new EventListenerList();
 		this.dice = dice;
 		this.player = player;
 		categories = new ScoreGroup[13];
 
 		// Construct first panel
 		higherCategories = new JPanel();
 		higherCategories.setPreferredSize(new Dimension(350, 300));
 		higherCategories.setLayout(new BoxLayout(higherCategories,
 				BoxLayout.Y_AXIS));
 
 		// Construct second panel
 		lowerCategories = new JPanel();
 		lowerCategories.setPreferredSize(new Dimension(350, 300));
 		lowerCategories.setLayout(new BoxLayout(lowerCategories,
 				BoxLayout.Y_AXIS));
 
 		// Fill the Category array with a series of Categories
 		fillCategories();
 
 		// Add the array of Categories to the appropriate panel
 		addCategories();
 
 		// Add events to each Category
 		addEvents();
 	}
 
 	/**
 	 * Add the categories to the appropriate panel
 	 */
 	private void addCategories()
 	{
 		// The first six are added to the higher panel
 		for (int i = 0; i < 6; i++)
 		{
 			addCategory(higherCategories, categories[i]);
 		}
 
 		// Add other static groups to the higher panel
 		addCategory(higherCategories, upperSectionBonus);
 		addCategory(higherCategories, upperSectionTotal);
 
 		// The other groups are added to the lower panel
 		for (int i = 6; i < categories.length; i++)
 		{
 			addCategory(lowerCategories, categories[i]);
 		}
 
 		// Add other static groups to the lower panel
 		addCategory(lowerCategories, lowerSectionYahtzeeBonus);
 		addCategory(lowerCategories, grandTotal);
 
 		// Add both category panels to the ScoreBoard
 		this.add(higherCategories);
 		this.add(lowerCategories);
 	}
 
 	/**
 	 * Add a ScoreGroup to a panel along with a vertical spacing
 	 * 
 	 * @param panel
 	 *            The panel to add the group to
 	 * @param group
 	 *            The group to add to the panel
 	 */
 	private void addCategory(JPanel panel, ScoreGroup group)
 	{
 		panel.add(group);
 		panel.add(Box.createVerticalStrut(7));
 	}
 
 	/**
 	 * Add a listener to the list of listeners
 	 * 
 	 * @param listener
 	 *            The ChangeListener to add to the list of listeners
 	 */
 	public void addChangeListener(ChangeListener listener)
 	{
 		listenerList.add(ChangeListener.class, listener);
 	}
 
 	/**
 	 * Add a mouse listener to every category to run game logic on mouse events
 	 */
 	private void addEvents()
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			categories[i].addMouseListener(new MouseListener()
 			{
 				@Override
 				public void mouseClicked(MouseEvent e)
 				{}
 
 				/**
 				 * If the ScoreGroup the mouse is currently over can be selected
 				 * and hasn't been chosen, display the score it would generate
 				 * in the category box.
 				 */
 				@Override
 				public void mouseEntered(MouseEvent e)
 				{
 					ScoreGroup sg = (ScoreGroup) e.getSource();
 					if (sg.getCanBeSelected())
 					{
 						if (!sg.getChosen())
 						{
 							sg.updateScore(dice);
 						}
 					}
 				}
 
 				/**
 				 * If the ScoreGroup the mouse has exited can be selected and
 				 * hasn't been chosen, then reset the display to only include
 				 * the text and not the possible score.
 				 */
 				@Override
 				public void mouseExited(MouseEvent e)
 				{
 					ScoreGroup sg = (ScoreGroup) e.getSource();
 					if (sg.getCanBeSelected())
 					{
 						if (!sg.getChosen())
 						{
 							sg.setTextToCategory();
 						}
 					}
 				}
 
 				/**
 				 * If the ScoreGroup the mouse has pressed in can be selected
 				 * and has not been chosen then add the score from the category
 				 * using the current dice, fire the event listener to signal the
 				 * end of a turn and set Yahtzee flag if necessary.
 				 */
 				@Override
 				public void mousePressed(MouseEvent e)
 				{
 					ScoreGroup sg = (ScoreGroup) e.getSource();
 					if (sg.getCanBeSelected())
 					{
 						if (!sg.getChosen())
 						{
 							sg.updateScore(dice);
 							player.addScore(sg.getScore(dice), sg.getIsUpper());
 							sg.setChosen(true);
 
 							fireChangeEvent(new ChangeEvent(sg.getScore(dice)));
 							// check Yahtzee
 							if (sg.isYahtzeeScoreGroup()
 									&& sg.getScore(dice) == 50)
 								player.setHaveYahtzee();
 						}
 					}
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent e)
 				{}
 			});
 		}
 	}
 
 	/**
 	 * Fill the array with a series of Category objects (in order). Initialise
 	 * static score groups.
 	 */
 	private void fillCategories()
 	{
 		// Fill the array
 		for (int i = 0; i < categories.length; i++)
 		{
 			categories[i] = new ScoreGroup(Category.getCategory(i + 1));
 		}
 
 		// Initialise static score groups
 		upperSectionBonus = new StaticScoreGroup("Upper Section Bonus");
 		upperSectionTotal = new StaticScoreGroup("Upper Total");
 		lowerSectionYahtzeeBonus = new StaticScoreGroup("Yahtzee Bonus");
 		grandTotal = new StaticScoreGroup("Grand Total");
 	}
 
 	/**
 	 * Fire the change event for this class by looping through each
 	 * ChangeListener associated with this class and calling its .stateChanged
 	 * method.
 	 * 
 	 * @param event
 	 *            An object holding event information to pass into each listener
 	 *            when fired
 	 */
 	private void fireChangeEvent(ChangeEvent event)
 	{
 		updateUpperBonus();
 
 		// Fire each listener in the list of listeners
 		for (ChangeListener listener : listenerList
 				.getListeners(ChangeListener.class))
 		{
 			listener.stateChanged(event);
 		}
 	}
 
 	/**
 	 * If the set of dice is a Yahtzee and the Yahtzee category is zero we can
 	 * initiate the joker rules.
 	 * 
 	 * Set can be selected to appropriate upper section box based on number of
 	 * Yahtzee. If the box is already chosen, release all lower section boxes
 	 * that are not chosen. If none free score zero in any box left.
 	 * 
 	 * For example if dice are 5,5,5,5,5 first try and set the Fives category to
 	 * be can be selected forcing user to choose it. If the Fives category has
 	 * already been chosen, then release all the lower section categories that
 	 * haven't already been chosen scoring the override rules. If there are no
 	 * lower section categories left, score zero in any category left.
 	 */
 	private void initiateJokerRules()
 	{
 		// Get a string representation of the Yahtzee number
 		String num = Category.getUpperCategoryFromNum(
 				Integer.toString(dice[0].getValue())).toString();
 
 		// Set upper category can be selected based on num
 		for (int i = 0; i < categories.length; i++)
 		{
 			if (categories[i].getCategoryName().equals(num)
 					&& !categories[i].getChosen())
 			{
 				categories[i].setCanBeSelected(true);
 			}
 			else
 			{
 				categories[i].setCanBeSelected(false);
 			}
 		}
 
 		// If none can be selected in upper section (already chosen) release all
 		// lower sections with override rules
 		if (isNoneCanBeSelected())
 		{
 			for (int i = 6; i < categories.length; i++)
 			{
 				categories[i].setCanBeSelected(true);
 				categories[i].setUsingOverrideScore(true);
 			}
 
 			// If all lower sections full release all upper sections with
 			// override rules
 			if (isAllLowerSectionsIsChosen())
 			{
 				for (int i = 0; i < 6; i++)
 				{
 					categories[i].setCanBeSelected(true);
 					categories[i].setUsingOverrideScore(true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines if all ScoreGroup objects in the set of categories have been
 	 * chosen by the user.
 	 * 
 	 * @return True if every ScoreGroup in the set of categories has been
 	 *         chosen, false otherwise.
 	 */
 	public boolean isAllChosen()
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			if (!categories[i].getChosen())
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Determines if all lower ScoreGroup objects in the set of categories have
 	 * been chosen by the user.
 	 * 
 	 * @return True if every lower ScoreGroup in the set of categories has been
 	 *         chosen, false otherwise.
 	 */
 	private boolean isAllLowerSectionsIsChosen()
 	{
 		for (int i = 6; i < categories.length; i++)
 		{
 			if (!categories[i].getChosen())
 			{
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Determines if the current die values are a Yahtzee (all values are the
 	 * same)
 	 * 
 	 * @param dice
 	 *            The set of dice to test.
 	 * @return True if the dice are a Yahtzee, false otherwise.
 	 */
 	private boolean isDiceYahtzee(YahtzeeDice[] dice)
 	{
 		return Category.getYahtzeeScore(dice) == 50;
 	}
 
 	/**
 	 * Determines if no ScoreGroup object in the set of categories can be
 	 * selected by the user
 	 * 
 	 * @return True if no ScoreGroup in the set of categories can be selected by
 	 *         the user, false otherwise.
 	 */
 	private boolean isNoneCanBeSelected()
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			if (categories[i].getCanBeSelected())
 			{
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Remove a listener from the list of listeners
 	 * 
 	 * @param listener
 	 *            The ChangeListener to remove from the list of listeners
 	 */
 	public void removeChangeListener(ChangeListener listener)
 	{
 		listenerList.remove(ChangeListener.class, listener);
 	}
 
 	/*
 	 * Jokers Rules - Score total of all five dice in appropriate upper section
 	 * box if already filled in score in lower section as normal If both filled
 	 * in score zero in any free upper section
 	 */
 
 	/**
 	 * Reset the state of the ScoreBoard. Reset each ScoreGroup category.
 	 */
 	@Override
 	public void reset()
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			categories[i].reset();
 		}
 		upperSectionBonus.reset();
 		upperSectionTotal.reset();
 		lowerSectionYahtzeeBonus.reset();
 		grandTotal.reset();
 	}
 
 	/**
 	 * Set the can be selected property for every ScoreGroup in the set of
 	 * categories
 	 * 
 	 * @param selected
 	 *            True if every ScoreGroup can be selected, false if not
 	 *            category can be selected by the user
 	 */
 	public void setCanBeSelected(boolean selected)
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			categories[i].setCanBeSelected(selected);
 		}
 	}
 
 	/**
 	 * Set the series of dice to use when calculating scores
 	 * 
 	 * @param dice
 	 *            The new set of dices
 	 */
 	public void setDice(YahtzeeDice[] dice)
 	{
 		this.dice = dice;
 
 		updateYahtzeeBonus();
 
 		// If the set of dice is a Yahtzee and the Yahtzee category is zero we
 		// can initiate the joker rules.
		if (isDiceYahtzee(dice) && getYahtzeeScoreGroup().getChosen())
 		{
 			initiateJokerRules();
 		}
 	}
 
 	/**
	 * Get the Yahtzee ScoreGroup from the set of categories
	 * 
	 * @return The Yahtzee ScoreGroup for the current game, null if not present.
	 */
	private ScoreGroup getYahtzeeScoreGroup()
	{
		for (int i = 0; i < categories.length; i++)
		{
			if (categories[i].isYahtzeeScoreGroup())
				return categories[i];
		}
		return null;
	}

	/**
 	 * Set the Player that this ScoreBoard is associated with
 	 * 
 	 * @param player
 	 *            The Player to associate with this ScoreBoard
 	 */
 	public void setPlayer(Player player)
 	{
 		this.player = player;
 	}
 
 	/**
 	 * Set if every category is using the override rules
 	 * 
 	 * @param selected
 	 *            True if every category should use the override rules, false if
 	 *            no category should use the override rules.
 	 */
 	public void setUsingJokerRulesOverrideScore(boolean selected)
 	{
 		for (int i = 0; i < categories.length; i++)
 		{
 			categories[i].setUsingOverrideScore(selected);
 		}
 	}
 
 	/**
 	 * Determine if the Player is eligible for an upper bonus, if so add one and
 	 * update the scores
 	 * 
 	 * If the sum of all upper categories is 63 or higher, then we can add a 35
 	 * point upper bonus
 	 */
 	private void updateUpperBonus()
 	{
 		// If we have no had a bonus and the upper score is 63 then add the
 		// bonus
 		if (player.getUpperBonus() == 0 && player.getUpperScore() >= 63)
 		{
 			player.addUpperBonus();
 		}
 
 		// Update the relevant static score groups
 		upperSectionBonus.setScore(player.getUpperBonus());
 		upperSectionTotal.setScore(player.getUpperScore());
 		grandTotal.setScore(player.getScore());
 	}
 
 	/**
 	 * Determine if the Player is eligible for another Yahtzee Bonus, is so add
 	 * one and update the scores
 	 * 
 	 * If Player has scored a Yahtzee, then add a bonus of 100 points for every
 	 * subsequent Yahtzee
 	 */
 	private void updateYahtzeeBonus()
 	{
 		// If we have had a Yahtzee and the current dice is a Yahtzee, then add
 		// the bonus
 		if (player.getHaveYahtzee() && isDiceYahtzee(dice))
 		{
 			player.addYahtzeeBonus();
 		}
 
 		// Update the relevant static score groups
 		lowerSectionYahtzeeBonus.setScore(player.getYahtzeeBonus());
 		grandTotal.setScore(player.getScore());
 	}
 }

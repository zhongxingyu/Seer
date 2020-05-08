 /**
 * Hand - JPanel to manage a player's hands and 
  * display the cards on the board
  */
 package com.swm.blackjack;
 
 import static com.swm.blackjack.BlackJack.darkGreen;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * @author smartin
  * 
  */
 public abstract class Hand extends JPanel
 {
	/**
	 * Hand
	 * JPanel to manage a player's hand and display the cards on the board 
	 */
	private static final long serialVersionUID = -4281747320119006211L;
	
 	public static final Font cardFont = new Font( "Helvetica", Font.BOLD, 16 );
 	private static final int GAP = 10;
 	static final int MAX_CARDS_IN_HAND = 6;
 	private JPanel innerPanel = new JPanel();
 	private JLabel label;
 	private JLabel totalLabel;
 	private int cardIndex = 0;
 	private String name;
 	private boolean soft = false;
 	private ArrayList<Card> cards = new ArrayList<Card>();
 
 	/**
 	 * Constructor sets up initial hand with empty cards to establish 
 	 * the space on the board without displaying any cards
 	 * @param name the name of the player for display
 	 */
 	public Hand(String name)
 	{
 		this.name = name;
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cardIndex = 0;
 		
 		setLayout( new BorderLayout( GAP, GAP ) );
 		setBackground( darkGreen );
 
 		label = new JLabel( name, JLabel.CENTER );
 		label.setBackground( darkGreen );
 		label.setForeground( Color.WHITE );
 		label.setOpaque( true );
 		add( label, BorderLayout.NORTH );
 
 		totalLabel = new JLabel( name + " total:  0", JLabel.CENTER );
 		totalLabel.setBackground( Color.BLACK );
 		totalLabel.setForeground( Color.WHITE );
 		totalLabel.setOpaque( true );
 		add( totalLabel, BorderLayout.SOUTH );
 
 		innerPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
 		innerPanel.setBackground( darkGreen );
 		for (Card card : cards) {
 			innerPanel.add(card);
 		}
 		add( innerPanel, BorderLayout.CENTER );
 	}
 
 	/**
 	 * Add a new card to the Hand
 	 * @param card the Card
 	 */
 	public void addCard(Card card, boolean faceup)
 	{
 
 		if ( cardIndex >= cards.size()
 				|| cards.get( cardIndex ).getOrientation() != CardOrientation.EMPTY ) {
 			return;
 		}
 		cards.set( cardIndex, card );
 		if (faceup) {
 			cards.get( cardIndex ).turnFaceUp();
 		}
 
 		innerPanel.remove( cardIndex );
 		innerPanel.add( cards.get( cardIndex ), cardIndex );
 		innerPanel.validate();
 		cardIndex++;
 		setTotalText();
 
 		repaint();
 	}
 
 	/**
 	 * Set the label which displays the current Total for the Hand
 	 */
 	public void setTotalText()
 	{
 		String txt = name + " total: " + getTotal();
 		this.totalLabel.setText( txt );
 	}
 
 	/**
 	 * Set a new hand with two cards dealt to begin with.
 	 */
 	public void newHand()
 	{
 		cards.clear();
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 		cards.add( new Card() );
 
 		innerPanel.removeAll();
 		for (Card card : cards) {
 			innerPanel.add(card);
 		}
 		innerPanel.validate();
 		
 		cardIndex = 0;
 		setTotalText();
 		repaint();
 	}
 
 	/**
 	 * Each player class will update the winnings based on who wins. i.e. Dealer
 	 * wins, and bet is removed from winnings. Player wins, and bet is added.
 	 * @param winnings the current player total winnings
 	 * @param bet the current amount of the bet
 	 * @return the players winnings after the bet is added or subtracted
 	 */
 	abstract public int pay(int winnings, int bet);
 
 	/**
 	 * Utility function simply returns the name of the player
 	 */
 	public String toString()
 	{
 		return name;
 	}
 
 	/**
 	 * Turn the given Card Face up
 	 * @param index the index of the card to be turned up
 	 */
 	public void turnFaceUp(int index)
 	{
 		if ( index < cards.size()
 				&& cards.get( index ).getOrientation() != CardOrientation.EMPTY ) {
 			cards.get( index ).turnFaceUp();
 			setTotalText();
 		}
 	}
 
 	/**
 	 * Returns true if the hand total is soft because it uses and Ace
 	 * @return
 	 */
 	public boolean isSoft()
 	{
 		return soft;
 	}
 	
 	/**
 	 * Return the total value of this Hand per standard BlackJack rules If there
 	 * are aces in the hand, make sure they are counted appropriately
 	 * @return <code>int</code> value of the hand
 	 */
 	public int getTotal()
 	{
 		int ret = 0;
 		int aces = 0;
 
 		soft = false;
 		
 		for ( Card card : cards ) {
 			if ( card.getOrientation() == CardOrientation.FACE_UP ) {
 				int val = card.getValue();
 				ret += val;
 				if ( val == 1 ) {
 					aces += 1;
 					ret += 10;
 				}
 				if ( ret > 21 && aces > 0 ) {
 					aces -= 1;
 					ret -= 10;
 				}
 			}
 		}
 		if ( aces > 0 ) soft = true;
 		return ret;
 	}
 	
 	/**
 	 * The card index showing number of cards in the current hand.
 	 * @return int value of the current index
 	 */
 	protected int getCardIndex()
 	{
 		return cardIndex;
 	}
 }

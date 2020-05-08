 package Field;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import CardAssociation.Card;
 import CardAssociation.Type;
 import Game.Game;
 import Game.Phase;
 import Game.Player;
 
 public class Hand extends FieldElement {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7959268439908932650L;
 	private ArrayList<Card> handCards;
 
 	// private Player associatedPlayer;
 	private int selectedIndex = -1;
 	private Card selected = null;
 
 	public Hand(String imageFileName, int xa, int ya, Player player) {
 		super(imageFileName, xa, ya, "Hand", player);
 		handCards = new ArrayList<Card>();
 		// associatedPlayer = player;
 	}
 
 	public void setCard(Card c) {
 		if (c != null)
 			handCards.add(c);
 	}
 
 	public ArrayList<Card> showHand() {
 		return handCards;
 	}
 
 	public Card playCard(Card c) {
 		if (handCards.remove(c))
 			return c;
 		return null;
 	}
 
 	@Override
 	public boolean containCards() {
 		return handCards.size() > 0;
 	}
 
 	@Override
 	public void paint(Graphics g, Card c) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.pink);
 		g2.setBackground(Color.pink);
 		g2.fillRect(0, 0, Game.maxWidth, Game.maxHeight);
 		for (int i = 0; i < handCards.size(); i++) {
 			Card thisCard = handCards.get(i);
 			// System.out.print(thisCard.getCardName() + ", ");
 			thisCard.setDisplay(true, false);
 			thisCard.toCanvas().setLocation(
 					(int) ((x + 110 * i) * Game.gameScale), (int) (y));
 			if (selected != null
 					&& thisCard.getCardName().equals(selected.getCardName())) {
 				thisCard.toCanvas().setLocation(
 						(int) ((x + 110 * i) * Game.gameScale), y - 10);
 			}
 			thisCard.toCanvas().paint(g2);
 		}
 		g2.setFont(new Font("TimesRoman", Font.PLAIN, 12));
 		g2.setColor(Color.BLUE);
 
 		g2.drawString("Player Hand", this.x + 10, this.y + 20);
 	}
 
 	@Override
 	public Card selectCard(MouseEvent e) {
 		String output;
		Card card, result = null;
 		for (int i = 0; i < handCards.size(); i++) {
 			card = handCards.get(i);
 			output = "HAND: x = " + e.getX() + ", y = " + e.getY() + " "
 					+ card.getCardName() + " : " + card.getCardBound().x
 					+ " + " + card.getCardBound().width + " , "
 					+ card.getCardBound().y + " + "
 					+ card.getCardBound().height;
 			if (card.getCardBound().contains(e.getPoint())) {
 				selectedIndex = i;
 				output += " match!";
				result = card;
 			}
 			System.out.println(output);
 			// System.out.println();
 		}
		return result;
 	}
 
 	@Override
 	public Card showCard() {
 		if (handCards.size() > 0)
 			return handCards.get(handCards.size() - 1);
 		return null;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Card card = null;
 		if (containCards()) {
 			card = selectCard(e);
 		}
 
 		if (e.getButton() == MouseEvent.BUTTON3) {
 
 			if (card != null) {
 				if (associatedPlayer.getCurrentPhase() == Phase.CLOCK_PHASE) {
 					if (selectedIndex > -1) {
 						Card temp = handCards.remove(selectedIndex);
 						selectedIndex = -1;
 						System.out.println(temp.getCardName() + " was clocked");
 						System.out.println("new hand " + handCards);
 						associatedPlayer.getField().setSelected(card);
 					}
 				} else if (associatedPlayer.getCurrentPhase() == Phase.MAIN_PHASE) {
 					if (card.getT() == Type.CHARACTER) {
 						if (!associatedPlayer.getField().getFrontRow1()
 								.containCards()) {
 							associatedPlayer.getField().getFrontRow1()
 									.setCard(card);
 						} else if (!associatedPlayer.getField().getFrontRow2()
 								.containCards()) {
 							associatedPlayer.getField().getFrontRow2()
 									.setCard(card);
 						} else if (!associatedPlayer.getField().getFrontRow3()
 								.containCards()) {
 							associatedPlayer.getField().getFrontRow3()
 									.setCard(card);
 						} else if (!associatedPlayer.getField().getBackRow1()
 								.containCards()) {
 							associatedPlayer.getField().getBackRow1()
 									.setCard(card);
 						} else if (!associatedPlayer.getField().getBackRow2()
 								.containCards()) {
 							associatedPlayer.getField().getBackRow2()
 									.setCard(card);
 						} else {
 							selectedIndex = -1;
 						}
 						if (selectedIndex > -1) {
 							handCards.remove(selectedIndex);
 							associatedPlayer.getField().repaint();
 						}
 					} else if (card.getT() == Type.EVENT) {
 						associatedPlayer.getField().getRandomZone()
 								.setCard(card);
 						if (selectedIndex > -1) {
 							handCards.remove(selectedIndex);
 						}
 					}
 				} else if (associatedPlayer.getCurrentPhase() == Phase.CLIMAX_PHASE
 						&& card.getT() == Type.CLIMAX) {
 					associatedPlayer.getField().getClimaxZone().setCard(card);
 					if (selectedIndex > -1) {
 						handCards.remove(selectedIndex);
						//handCards.remove(card);
 					}
 				} else if (associatedPlayer.getCurrentPhase() == Phase.ATTACK_PHASE
 						&& card.getT() == Type.CHARACTER) {
 					/*
 					 * associatedPlayer.getField().getRandomZone().setCard(card);
 					 * if (selectedIndex > -1) {
 					 * handCards.remove(selectedIndex); }
 					 */
 				}
 			}
 		} else if (e.getButton() == MouseEvent.BUTTON1) {
 			/*if (containCards()) {
 				card = selectCard(e);
 			}*/
 			repaint();
 		}
 		selected = card;
 
 	}
 
 	public void paint(Graphics g) {
 		paint(g, null);
 	}
 
 	public boolean isList() {
 		return true;
 	}
 
 	public Card getSelected() {
 		return selected;
 	}
 }

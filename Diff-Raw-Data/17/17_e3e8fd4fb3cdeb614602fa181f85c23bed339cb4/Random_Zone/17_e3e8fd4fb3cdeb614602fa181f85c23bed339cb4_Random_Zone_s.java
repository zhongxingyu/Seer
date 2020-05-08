 package Field;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import CardAssociation.Card;
 import CardAssociation.State;
 import Game.Player;
 
 public class Random_Zone extends FieldElement {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4719315349186983715L;
 
 	private ArrayList<Card> thisCard;
 
 	public Random_Zone(String imageFileName, int xa, int ya, Player player) {
 		super(imageFileName, xa, ya, "Resolution Area", player);
 		thisCard = new ArrayList<Card>();
 	}
 
 	@Override
 	public boolean containCards() {
 		return thisCard.size() > 0;
 	}
 
 	protected void constructPopup(MouseEvent e) {
 		JPopupMenu popmenu = new JPopupMenu();
 
 		JMenuItem waitingAction = new JMenuItem("all to waiting room");
 		waitingAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toWaitingRoom();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(waitingAction);
 
 		JMenuItem clockAction = new JMenuItem("all to clock");
 		clockAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toClock();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(clockAction);
 
 		JMenuItem memoryAction = new JMenuItem("all to memory");
 		memoryAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toMemory();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(memoryAction);
 
 		JMenuItem stockAction = new JMenuItem("all to stock");
 		stockAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toStock();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(stockAction);
 
 		JMenuItem handAction = new JMenuItem("all to hand");
 		handAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toHand();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(handAction);
 
 		JMenuItem deckTopAction = new JMenuItem("all to deck top");
 		deckTopAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toDeck(true);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(deckTopAction);
 
 		JMenuItem deckBotAction = new JMenuItem("all to deck bottom");
 		deckBotAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toDeck(false);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(deckBotAction);
 
 		popmenu.show(e.getComponent(), e.getX(), e.getY());
 	}
 
 	protected void toDeck(boolean isTop) {
 		for (int i = thisCard.size() - 1; i >= 0; i--) {
 			if (isTop)
 				associatedPlayer.getField().getDeckZone().setCard(thisCard.get(i));
 			else
 				associatedPlayer.getField().getDeckZone().setBotCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	protected void toHand() {
 		for (int i = 0; i < thisCard.size(); i++) {
 			associatedPlayer.getHand().setCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	protected void toStock() {
 		for (int i = 0; i < thisCard.size(); i++) {
 			associatedPlayer.getField().getStockZone().setCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	protected void toMemory() {
 		for (int i = 0; i < thisCard.size(); i++) {
 			associatedPlayer.getField().getMemoryZone().setCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	protected void toClock() {
 		for (int i = 0; i < thisCard.size(); i++) {
 			associatedPlayer.getField().getClockZone().setCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	protected void toWaitingRoom() {
 		for (int i = 0; i < thisCard.size(); i++) {
 			associatedPlayer.getField().getWaitingRoom().setCard(thisCard.get(i));
 		}
 		thisCard.clear();
 		associatedPlayer.getField().repaint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		Card card = selectCard(e);
 		if (containCards() == false || card == null)
 			return;
 
 		associatedPlayer.getField().setSelected(card);
 
 		if (e.getButton() == MouseEvent.BUTTON3) {
 			constructPopup(e);
 		} else if (e.getButton() == MouseEvent.BUTTON1) {
			if (showCard().getCardBound().contains(e.getPoint()))
				removeCard();
 		}
 	}
 
 	@Override
 	public void paint(Graphics g, Card c) {
 		if (containCards()) {
 			showCard().setDisplay(true, false);
 			showCard().toCanvas().setLocation(x, y);
 			showCard().toCanvas().paint(g);
 			System.out.println("RESOLUTION: " + thisCard);
 		} else {
 			g.drawImage(bi, x, y, null);
 		}
 		g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
 		g.setColor(Color.BLUE);
 
 		g.drawString(zoneName, x + 10, y + 20);
 	}
 
 	public Card removeCard() {
 		if (!containCards())
 			return null;
 		Card tempCard = thisCard.get(thisCard.size() - 1);
 		thisCard.remove(thisCard.size() - 1);
 		associatedPlayer.getField().repaintElements();
 		return tempCard;
 	}
 
 	// @Override
 	// public Card selectCard(MouseEvent e) {
 	// if (!containCards())
 	// return null;
 	//
 	// Card tempCard = showCard();
 	// if (tempCard.getCardBound().contains(e.getPoint())) {
 	// return tempCard;
 	//
 	// }
 	// return null;
 	// }
 
 	@Override
 	public void setCard(Card c) {
 		c.setCurrentState(State.STAND);
 		thisCard.add(c);
 	}
 
 	@Override
 	public Card showCard() {
 		return thisCard.get(thisCard.size() - 1);
 	}
 
 }

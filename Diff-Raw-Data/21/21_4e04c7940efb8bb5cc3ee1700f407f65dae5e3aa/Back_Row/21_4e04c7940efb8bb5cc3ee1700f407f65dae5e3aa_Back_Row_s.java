 package Field;
 
 // Back_Row field display information
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import CardAssociation.*;
 import Game.Player;
 
 public class Back_Row extends FieldElement {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6095129785625327860L;
 
 	// public ArrayList<Card> backRow;
 	Card backCard;
 
 	public Back_Row(String imageFileName, int xa, int ya, Player player) {
 		super(imageFileName, xa, ya, "Back-Row", player);
 
 		// backRow = new ArrayList<Card>();
 
 		backCard = null;
 	}
 
 	public void setCard(Card c) {
 		if (c != null) {
 			c.setCurrentState(State.STAND);
 			removeCard();
 		}
 		backCard = c;
 		repaint();
 	}
 
 	// public ArrayList<Card> showCards() {
 	// return backRow;
 	// }
 
 	public Card showCard() {
 		return backCard;
 	}
 
 	public Card removeCard() {
 		Card c = backCard;
 		backCard = null;
 		return c;
 	}
 
 	public boolean snapToZone(Card c) {
 
 		if (c.getT() == Type.CHARACTER && c.getCardBound().intersects(rect)) {
 			c.getCardBound().setLocation(x, y);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean containCards() {
 		// return backRow.size() > 0;
 		return backCard != null;
 	}
 
 	@Override
 	public void paint(Graphics g, Card c) {
 		if (showCard() != c && showCard() != null) {
 			// backCard.toCanvas().paintCard(g, this.x, this.y, true, false);
 			backCard.setDisplay(true, false);
 			backCard.toCanvas().setLocation(x, y);
 			backCard.toCanvas().paint(g);
 			System.out.println("BACK_ROW: " + backCard.getCardName());
 		} else {
 			g.drawImage(bi, x, y, null);
 		}
 		g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
 		g.setColor(Color.BLUE);
 
 		g.drawString(zoneName, x + 10, y + 20);
 	}
 
 	// @Override
 	// public Card selectCard(MouseEvent e) {
 	// if (containCards()
 	// && backCard.getCardBound().contains(e.getX(), e.getY())) {
 	// return showCard();
 	// }
 	// return null;
 	// }
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (containCards() == false)
 			return;
 
 		if (backCard.getCardBound().contains(e.getPoint()))
 			associatedPlayer.getField().setSelected(backCard);
 
 		if (e.getButton() == MouseEvent.BUTTON3) {
 			constructPopup(e);
 		} else if (e.getButton() == MouseEvent.BUTTON1) {
 			if (backCard.getCardBound().contains(e.getPoint())) {
 				// removeCard();
 			}
 		}
 	}
 
 	protected void constructPopup(MouseEvent e) {
 		JPopupMenu popmenu = new JPopupMenu();
 
 		JMenuItem standAction = new JMenuItem("stand");
 		standAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				backCard.setCurrentState(State.STAND);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(standAction);
 
 		JMenuItem restAction = new JMenuItem("rest");
 		restAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				backCard.setCurrentState(State.REST);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(restAction);
 
 		JMenuItem reverseAction = new JMenuItem("reverse");
 		reverseAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				backCard.setCurrentState(State.REVERSE);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(reverseAction);
 
 		JMenuItem waitingRoom = new JMenuItem("to waiting room");
 		waitingRoom.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				associatedPlayer.getField().getWaitingRoom().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(waitingRoom);
 
 		JMenuItem handAction = new JMenuItem("to hand");
 		handAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				associatedPlayer.getHand().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(handAction);
 
 		JMenuItem topDeckAction = new JMenuItem("to top of deck");
 		topDeckAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				associatedPlayer.getField().getDeckZone().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(topDeckAction);
 
 		JMenuItem botDeckAction = new JMenuItem("to bottom of deck");
 		botDeckAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				associatedPlayer.getField().getDeckZone().setBotCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(botDeckAction);
 
 		JMenuItem memoryAction = new JMenuItem("to memory");
 		memoryAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				associatedPlayer.getField().getMemoryZone().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(memoryAction);
 
		JMenuItem stockAction = new JMenuItem("to waiting room");
 		stockAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				associatedPlayer.getField().getClockZone().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(stockAction);
 
 		JMenuItem clockAction = new JMenuItem("to clock");
 		clockAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				associatedPlayer.getField().getWaitingRoom().setCard(backCard);
 				removeCard();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(clockAction);
 
 		popmenu.show(e.getComponent(), e.getX(), e.getY());
 	}
 }

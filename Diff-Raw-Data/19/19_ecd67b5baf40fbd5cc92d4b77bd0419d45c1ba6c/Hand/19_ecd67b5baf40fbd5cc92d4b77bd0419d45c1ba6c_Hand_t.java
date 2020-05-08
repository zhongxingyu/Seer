 package Field;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.ListModel;
 
 import CardAssociation.Card;
 import CardAssociation.State;
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
 		c.setCurrentState(State.STAND);
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
 		g2.setBackground(Color.getHSBColor(61, 87, 64));
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
 
 		System.out.println("HAND " + handCards);
 
 		g2.setFont(new Font("TimesRoman", Font.PLAIN, 12));
 		g2.setColor(Color.BLUE);
 
 		g2.drawString("Player Hand", this.x + 10, this.y + 20);
 	}
 
 	private void constructPopup(MouseEvent e) {
 
 		final JPopupMenu popmenu = new JPopupMenu();
 
 		JMenuItem fieldAction = new JMenuItem("to field");
 		fieldAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toField();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(fieldAction);
 
 		JMenuItem waitingRoomAction = new JMenuItem("to waiting room");
 		waitingRoomAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toWaitingRoom();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(waitingRoomAction);
 
 		JMenuItem deckTopAction = new JMenuItem("to top deck");
 		deckTopAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toDeck(true);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(deckTopAction);
 
 		JMenuItem deckBotAction = new JMenuItem("to bottom deck");
 		deckBotAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toDeck(false);
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(deckBotAction);
 
 		JMenuItem memoryAction = new JMenuItem("to memory");
 		memoryAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toMemory();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(memoryAction);
 
 		JMenuItem clockAction = new JMenuItem("to clock");
 		clockAction.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toClock();
 				associatedPlayer.getField().repaintElements();
 			}
 		});
 		popmenu.add(clockAction);
 
 		popmenu.show(e.getComponent(), e.getX(), e.getY());
 	}
 
 	private void toMemory() {
 		associatedPlayer.getField().getMemoryZone().setCard(selected);
 	}
 
 	private void toDeck(boolean toTop) {
 		if (toTop)
 			associatedPlayer.getField().getDeckZone().setCard(selected);
 		else
 			associatedPlayer.getField().getDeckZone().setBotCard(selected);
 	}
 
 	private void toWaitingRoom() {
 		associatedPlayer.getField().getWaitingRoom().setCard(selected);
 	}
 
 	private void toClock() {
 		if (selectedIndex > -1) {
 			Card temp = handCards.remove(selectedIndex);
 			selectedIndex = -1;
 			associatedPlayer.getField().getClockZone().setCard(temp);
 			if (associatedPlayer.getCurrentPhase() == Phase.CLOCK_PHASE) {
 				associatedPlayer.getField().getDeckZone().drawCard();
 				associatedPlayer.getField().getDeckZone().drawCard();
 				associatedPlayer.setCurrentPhase(Phase.MAIN_PHASE);
 			}
 		}
 
 	}
 
 	private void toField() {
 		if (associatedPlayer.getCurrentPhase() == Phase.MAIN_PHASE) {
 			if (selected.getT() == Type.CHARACTER) {
 				if (!associatedPlayer.getField().getFrontRow1().containCards()) {
 					associatedPlayer.getField().getFrontRow1()
 							.setCard(selected);
 				} else if (!associatedPlayer.getField().getFrontRow2()
 						.containCards()) {
 					associatedPlayer.getField().getFrontRow2()
 							.setCard(selected);
 				} else if (!associatedPlayer.getField().getFrontRow3()
 						.containCards()) {
 					associatedPlayer.getField().getFrontRow3()
 							.setCard(selected);
 				} else if (!associatedPlayer.getField().getBackRow1()
 						.containCards()) {
 					associatedPlayer.getField().getBackRow1().setCard(selected);
 				} else if (!associatedPlayer.getField().getBackRow2()
 						.containCards()) {
 					associatedPlayer.getField().getBackRow2().setCard(selected);
 				} else {
 					selectedIndex = -1;
 				}
 				if (selectedIndex > -1) {
 					handCards.remove(selectedIndex);
 				}
 			} else if (selected.getT() == Type.EVENT) {
 				associatedPlayer.getField().getRandomZone().setCard(selected);
 				if (selectedIndex > -1) {
 					handCards.remove(selectedIndex);
 				}
 			}
 		} else if (associatedPlayer.getCurrentPhase() == Phase.CLIMAX_PHASE
 				&& selected.getT() == Type.CLIMAX) {
 			associatedPlayer.getField().getClimaxZone().setCard(selected);
 			if (selectedIndex > -1) {
 				handCards.remove(selectedIndex);
 			}
 		}
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
 	public void mouseReleased(MouseEvent e) {
 		if (containCards() == false)
 			return;
 		selected = selectCard(e);
 		if (selected == null)
 			return;
 
 		associatedPlayer.getField().setSelected(selected);
 
 		if (e.getButton() == MouseEvent.BUTTON3) {
 			constructPopup(e);
 		} else if (e.getButton() == MouseEvent.BUTTON1) {
 			repaint();
 		}
 		// selected = card;
 
 	}
 
 	public void paint(Graphics g) {
 		paint(g, null);
 	}
 
 	public boolean isList() {
 		return true;
 	}
 
 	public int getCount() {
 		return handCards.size();
 	}
 
 	public Card getSelected() {
 		return selected;
 	}
 
 	public void preGameDiscard(Game game) {
 		PreGameDisplay pgd = new PreGameDisplay(handCards, associatedPlayer);
 		pgd.execute();
 	}

	public void clear() {
		handCards.clear();
	}
 }
 
 class PreGameDisplay extends DisplayList {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4051710667562173041L;
 
 	PreGameDisplay(ArrayList<Card> importList, Player p) {
 		super(importList, p);
 	}
 
 	protected Box setButtons() {
 		Box buttonRow = Box.createHorizontalBox();
 
 		JButton add = new JButton("add to queue");
 		add.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (!showList.contains(selectedCard))
 					showList.add(selectedCard);
 				refresh();
 			}
 		});
 		buttonRow.add(add);
 
 		JButton remove = new JButton("remove to queue");
 		remove.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showList.remove(selectedCard);
 				refresh();
 			}
 		});
 		buttonRow.add(remove);
 
 		JButton discard = new JButton("ready");
 		discard.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				for (Card card : showList) {
 					thisPlayer.getField().getWaitingRoom().setCard(card);
 					thisPlayer.getHand().playCard(card);
 					thisPlayer.getField().getDeckZone().drawCard();
 				}
 				System.out.println("HAND SIZE: "
 						+ thisPlayer.getHand().getCount());
 				dispose();
 
 				thisPlayer.drawField();
 
 				thisPlayer.getField().repaintElements();
 			}
 		});
 		buttonRow.add(discard);
 
 		buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
 		return buttonRow;
 	}
 
 	protected JScrollPane displayList() {
 		String[] cardNames = new String[showList.size()];
 		for (int i = 0; i < showList.size(); i++) {
 			cardNames[i] = showList.get(i).getCardName();
 		}
 
 		final JList<String> displayShow = new JList<String>(cardNames);
 		displayShow.setPrototypeCellValue("Index 1234567890");
 		MouseListener listener = new MouseAdapter() {
 			public void mouseReleased(MouseEvent e) {
 				selectedCard = showList.get(displayShow.getSelectedIndex());
 				refresh();
 			}
 		};
 		displayShow.addMouseListener(listener);
 		JScrollPane jsp = new JScrollPane(displayShow);
 		return jsp;
 	}
 
 	protected void buildSelector() {
 		displaySelect();
 
 		JPanel buttons = new JPanel();
 		buttons.add(setButtons());
 		buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		displayInfo.add(displayList());
 
 		add(displayInfo, BorderLayout.PAGE_START);
 		add(fillPane(), BorderLayout.CENTER);
 		add(buttons, BorderLayout.PAGE_END);
 		setResizable(false);
 		pack();
 		setLocationRelativeTo(null);
 		
 	}
 
 	protected void refresh() {
 		displayInfo.removeAll();
 		displayInfo.validate();
 		displaySelect();
 		displayInfo.add(displayList());
 		
 		setResizable(false);
 		pack();
 		setVisible(true);
 	}
 
 	public boolean execute() {
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		buildSelector();
 		setVisible(true);
 		return true;
 	}
 
 }

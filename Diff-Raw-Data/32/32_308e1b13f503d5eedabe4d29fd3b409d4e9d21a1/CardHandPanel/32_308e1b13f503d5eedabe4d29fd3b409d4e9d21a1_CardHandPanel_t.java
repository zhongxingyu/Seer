 package de.nordakademie.nakjava.client.game.gui;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import de.nordakademie.nakjava.client.internal.gui.ValueHolder;
 import de.nordakademie.nakjava.gamelogic.shared.cards.CardInformation;
 import de.nordakademie.nakjava.server.internal.VisibleModelField;
 
 public class CardHandPanel extends JPanel implements ValueHolder {
 
 	private Map<String, CardInteractionPanel> cards;
 	private VisibleModelField<List<CardInformation>> cardField;
 
 	public CardHandPanel(VisibleModelField<List<CardInformation>> cardField) {
 		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
 		setMaximumSize(new Dimension(getMaximumSize().width,
 				CardInformationPanel.CARD_HEIGHT + 2));
 		this.cardField = cardField;
 		cards = new HashMap<>();
 	}
 
 	public void setCards(List<CardInformation> cards) {
 		List<String> newCardNames = new LinkedList<>();
 
 		for (CardInformation card : cards) {
 			newCardNames.add(card.getTitle());
 		}
 
		List<String> toRemove = new LinkedList<>();

 		for (String knownCard : this.cards.keySet()) {
 			if (!newCardNames.contains(knownCard)) {
 				remove(this.cards.get(knownCard));
				toRemove.add(knownCard);
 			}
 		}
 
		for (String remove : toRemove) {
			this.cards.remove(remove);
		}

 		for (CardInformation newCard : cards) {
 			if (!this.cards.containsKey(newCard.getTitle())) {
 				CardInteractionPanel cardPanel = new CardInteractionPanel(
 						newCard);
 				this.cards.put(newCard.getTitle(), cardPanel);
 				this.add(cardPanel);
 			}
 		}
 
 		this.revalidate();
 	}
 
 	// public static void main(String[] args) {
 	// JFrame frame = new JFrame();
 	// CardHandPanel cardHandPanel = new CardHandPanel();
 	// frame.add(cardHandPanel);
 	//
 	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	//
 	// List<CardInformation> cards = new LinkedList<>();
 	// cards.add(new CardInformation(
 	// "Test1",
 	// "tut blub\n tut bläh\n nochmehr bla blub bläh \n auf ein letztes",
 	// "5 Holz, 3 Gold, 4 Steine", CardType.SPEZIAL));
 	// CardInformation cardInformation = new CardInformation(
 	// "Test2",
 	// "tut blub\n tut bläh\n nochmehr muh mäh blub bläh \n auf ein letztes",
 	// "5 Holz, 3 Gold, 4 Steine", CardType.SPEZIAL);
 	// cards.add(cardInformation);
 	//
 	// cardHandPanel.setCards(cards);
 	//
 	// frame.setVisible(true);
 	//
 	// try {
 	// Thread.sleep(5000);
 	// } catch (InterruptedException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//
 	// cards.add(new CardInformation("Test3", "ein kurzer",
 	// "5 Holz, 3 Gold, 4 Steine", CardType.SPEZIAL));
 	// cards.remove(cardInformation);
 	//
 	// cardHandPanel.setCards(cards);
 	// }
 
 	@Override
 	public void pickValue(Map<String, Object> genericValues) {
 		setCards(cardField.getValue(genericValues));
 	}
 }

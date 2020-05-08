 package clueGame;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class CardDisplayPanel extends JPanel {
	private Set<Card> humanCards;
 	
 	public CardDisplayPanel(Board board) {
 		super();
 		setLayout(new GridLayout(0,1));
 		humanCards = board.getHuman().getCards();
 		JLabel label = new JLabel("My Cards:");
 		add(label);
 		add(new CardPanel(Card.CardType.PERSON));
 		add(new CardPanel(Card.CardType.WEAPON));
 		add(new CardPanel(Card.CardType.ROOM));
 	}
 	
 	private class CardPanel extends JPanel {
 		public CardPanel(Card.CardType type) {
 			super();
 			if(type.equals(Card.CardType.PERSON))
 				setBorder(new TitledBorder (new EtchedBorder(), "People"));
 			else if(type.equals(Card.CardType.WEAPON))
 				setBorder(new TitledBorder (new EtchedBorder(), "Weapons"));
 			else if(type.equals(Card.CardType.ROOM))
 				setBorder(new TitledBorder (new EtchedBorder(), "Room"));
 			else
 				return;
 			
 			setLayout(new GridLayout(0,1));
			ArrayList<JLabel> cards = loadHumanCards(type);
			for(JLabel tf : cards) {
 				add(tf);
 			}
 		}
 	}
 	
	public ArrayList<JLabel> loadHumanCards(Card.CardType type){
		ArrayList<JLabel> list = new ArrayList<JLabel>();
 		for(Card c : humanCards) {
 			if(c.getType().equals(type)) {
				JLabel cField = new JLabel();
 				cField.setText(c.getName());
 				list.add(cField);
 			}
 		}
 		return list;
 	}
 }

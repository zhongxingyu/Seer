 package clueGame;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class CardDisplayPanel extends JPanel {
	private Set<Card> humanCards = new HashSet<Card>();
 	
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
			ArrayList<JTextField> cards = loadHumanCards(type);
			for(JTextField tf : cards) {
 				add(tf);
 			}
 		}
 	}
 	
	public ArrayList<JTextField> loadHumanCards(Card.CardType type){
		ArrayList<JTextField> list = new ArrayList<JTextField>();
 		for(Card c : humanCards) {
 			if(c.getType().equals(type)) {
				JTextField cField = new JTextField();
 				cField.setText(c.getName());
				cField.setEditable(false);
 				list.add(cField);
 			}
 		}
 		return list;
 	}
 }

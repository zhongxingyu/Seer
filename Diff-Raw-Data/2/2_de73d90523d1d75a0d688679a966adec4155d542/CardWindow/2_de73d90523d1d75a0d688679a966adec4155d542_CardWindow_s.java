 package org.ojim.client.gui.CardBar;
 
 import java.awt.BorderLayout;
 import java.awt.ComponentOrientation;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.ojim.client.gui.GUIClient;
 import org.ojim.language.Localizer;
 import org.ojim.logic.state.fields.Field;
 
 /**
  * Das Kartenfenster zeichnet die Kartenstapel
  * 
  */
 public class CardWindow extends JPanel {
 
 	// Hält CardStacks
 	private CardStack[] cardStacks;
 	private static final int MAX_CARD_STACKS = 4;
 	private int row = 0;
 	
 	private JPanel overPanel = new JPanel();
 	
 	private GUIClient gui;
 	
 	private JButton freeButton = new JButton();
	private JLabel buttonLabel = new JLabel();
 	private JButton freeMoneyButton = new JButton();
 	private JLabel buttonMoneyLabel = new JLabel();
 	private JPanel leftPanel = new JPanel();
 	
 	
 	private ActionListener freeButtonListener = new ActionListener() {
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			gui.freeMe(0);
 			
 		}
 	};;;
 	private ActionListener freeMoneyListener = new ActionListener() {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			gui.freeMe(1);
 			
 		}
 	};;;
 
 	/**
 	 * Initialisert das Fenster
 	 */
 	public CardWindow(GUIClient gui) {
 
 		this.gui = gui;
 		
 		freeButton.add(buttonLabel);
 		leftPanel.add(freeButton);
 		
 		freeButton.addActionListener(freeButtonListener);
 		
 		buttonMoneyLabel.setText("1000");
 		freeMoneyButton.add(buttonMoneyLabel);
 		leftPanel.add(freeMoneyButton);
 		
 		freeMoneyButton.addActionListener(freeMoneyListener);
 
 		cardStacks = new CardStack[MAX_CARD_STACKS * 3];
 
 		for (int i = 0; i < MAX_CARD_STACKS * 3; i++) {
 			cardStacks[i] = new CardStack();
 		}
 		
 		this.setLayout(new BorderLayout());
 		this.add(leftPanel, BorderLayout.WEST);	
 		this.add(overPanel, BorderLayout.EAST);	
 		this.validate();
 		this.repaint();
 		draw();
 	}
 
 	/**
 	 * Setzt die Sprache
 	 * 
 	 * @param language
 	 *            die neue Sprache
 	 */
 	public void setLanguage(Localizer language) {
 		for (int i = 0; i < MAX_CARD_STACKS; i++) {
 			cardStacks[i].setLanguage(language);
 		}
 		draw();
 	}
 
 	/**
 	 * fügt eine Karte zu den Kartenstapeln hinzu
 	 * 
 	 * @param card
 	 *            das Spielfeld
 	 */
 	public void addCard(org.ojim.logic.state.fields.BuyableField card, GUIClient gui) {
 		boolean found = false;
 
 		for (int i = 0; i < MAX_CARD_STACKS * (row + 1)
 				&& !(cardStacks[i].getFieldGroup() == null); i++) {
 			if (cardStacks[i].getFieldGroup().equals(card.getFieldGroup())) {
 				cardStacks[i].addCard(card, gui);
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 
 			for (int i = 0; i < MAX_CARD_STACKS * 2; i++) {
 
 				if (cardStacks[i].getFieldGroup() == null) {
 					cardStacks[i].addCard(card, gui);
 					if (i >= MAX_CARD_STACKS * (row + 1)) {
 						row++;
 					}
 					break;
 				}
 
 			}
 		}
 		// System.out.println("Karte gekauft");
 		draw();
 	}
 
 	/**
 	 * Entfernt eine Karte aus einem Kartenstapel wenn die Karte enthalten ist
 	 * 
 	 * @param card
 	 *            das zu entferndende Feld
 	 */
 	public void removeCard(org.ojim.logic.state.fields.BuyableField card, GUIClient gui) {
 		int empty = -1;
 		try {
 		for (int i = 0; i < MAX_CARD_STACKS * (row + 1)
 				&& !cardStacks[i].getFieldGroup().equals(null); i++) {
 			if (cardStacks[i].getFieldGroup().equals(card.getFieldGroup())) {
 				cardStacks[i].removeCard(card, gui);
 				if (cardStacks[i].isEmpty()) {
 					empty = i;
 				}
 				break;
 			}
 		}
 		} catch (NullPointerException e ){
 			System.out.println("Nullpointer");
 		}
 		if (empty != -1) {
 			for (int i = empty; i < MAX_CARD_STACKS * (row + 1) - 1; i++) {
 				cardStacks[i] = cardStacks[i + 1];
 			}
 			cardStacks[MAX_CARD_STACKS * (row + 1) - 1] = new CardStack();
 			if (empty < MAX_CARD_STACKS * (row + 1)) {
 				row--;
 			}
 		}
 		draw();
 	}
 
 	/**
 	 * Zeichnet das Fenster
 	 */
 	public void draw() {
 		overPanel.setLayout(new GridLayout(3, MAX_CARD_STACKS));
 
 		for (int i = 0; i < MAX_CARD_STACKS * (row + 1); i++) {
 			overPanel.remove(cardStacks[i]);
 			cardStacks[i].draw();
 			overPanel.add(cardStacks[i]);
 		}
 	}
 
 	/**
 	 * Dreht eine Karte im Kartenstapel um
 	 * @param field die umzudrehende Karte
 	 */
 	public void switchCardStatus(Field field) {
 		for (int i = 0; i < cardStacks.length; i++){
 			cardStacks[i].switchMortage(field);
 		}
 	}
 	
 
 
 	public void jailCards(int numberOfGetOutOfJailCards) {
 		buttonLabel.setText(numberOfGetOutOfJailCards+"");
 		
 	}
 
 }

 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.xml.sax.SAXException;
 
 import deck.Deck;
 import deck.MTGCard;
 import deck.MTGDeckMain;
 import deck.XMLParser;
 
 public class AddCardButton extends JButton implements ActionListener{
 
 	private CardDisplayPanel cardDisplay;
 	private XMLParser parser;
 	private DeckPanel deckPane;
 
 	public AddCardButton(String string, CardDisplayPanel cardDisplay, XMLParser parser, DeckPanel deckPanel) throws ParserConfigurationException, SAXException, IOException {
 		super(string);
 		this.cardDisplay = cardDisplay;
 		this.addActionListener(this);
 		this.parser = parser;
 		this.deckPane = deckPanel;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		String name = JOptionPane.showInputDialog(MTGDeckMain.messages.getString("GUISearch1"));
 		String number = JOptionPane.showInputDialog(MTGDeckMain.messages.getString("GUISearch2"));
 		MTGCard card;
 		try {
 			card = this.parser.searchForCardName(name);
 		} catch (XPathExpressionException e1) {
 			JOptionPane.showMessageDialog(this, e1.getMessage());
 			return;
 		}
 		Integer num = Integer.parseInt(number);
 		for(int i = 0; i < num.intValue(); i++){
 			try{
 				this.deckPane.getDeck().addCardToDeck(card);
				JOptionPane.showMessageDialog(this,  MTGDeckMain.messages.getString("Added"));
 			}catch(CardNotInRulesetException e){
 				JOptionPane.showMessageDialog(this, MTGDeckMain.messages.getString("RuleVio"));
 			}
 		}
 		this.cardDisplay.setListOfCards(this.deckPane.getDeck().cards);
 		//Gets menu button panel's parent, which is deck panel which we want to repaint
 		this.getParent().getParent().repaint();
 	}
 
 }

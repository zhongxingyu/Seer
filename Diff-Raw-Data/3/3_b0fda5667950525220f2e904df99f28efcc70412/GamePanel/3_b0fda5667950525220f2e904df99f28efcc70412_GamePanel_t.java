 import java.awt.*;
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 
 
 /**	Class holds the interfaces to the user, as well as the main functionality of the game.
  * 
  * @author Kenton Martin
  *
  */
 
 public class GamePanel extends JFrame  {
 
 	private ArrayList<Card> deck, cardPile;
 
 	private Stack[] pileList, labelList;
 
 	private Stack<Card> foundation1, foundation2, foundation3, foundation4, pile1, pile2, pile3, pile4, pile5, pile6, pile7;
 
 	private Stack<JLabel> pile1Labels, pile2Labels, pile3Labels, pile4Labels, pile5Labels, pile6Labels, pile7Labels;
 
 	private JLabel cardDeckLabel, cardPileLabel, cardPileLabel2, cardPileLabel3, foundation1Label, foundation2Label, foundation3Label, foundation4Label, pile1Label, pile2Label, pile2Label2, pile3Label, pile3Label2, pile3Label3, pile4Label, pile4Label2, pile4Label3, pile4Label4, pile5Label, pile5Label2, pile5Label3, pile5Label4, pile5Label5, pile6Label, pile6Label2, pile6Label3, pile6Label4, pile6Label5, pile6Label6, pile7Label, pile7Label2, pile7Label3, pile7Label4, pile7Label5, pile7Label6, pile7Label7, pile1Base, pile2Base, pile3Base, pile4Base, pile5Base, pile6Base, pile7Base;
 
 	private boolean firstDeal = true, dragging = false;
 
 	public GamePanel(ArrayList<Card> deck){  
 
 		super("Solitaire");
 
 		this.deck = deck;
 
 		setupBoard(); 
 
 		MouseManager mouseManager = new MouseManager();
 		this.addMouseListener(mouseManager);
 		this.addMouseMotionListener(mouseManager);
 
 	}
 
 	
 	/**	Helper method to initialize all objects with a size and a location, and adds them to the GamePanel
 	 * 
 	 */
 	private void setupBoard(){
 
 		cardPile = new ArrayList<Card>();
 
 		getContentPane().setLayout(null);
 
 		getContentPane().setBackground(new java.awt.Color(0,128,0));  
 
 		cardDeckLabel = new JLabel();
 		cardDeckLabel.setIcon(new ImageIcon(getClass().getResource("./images/deck.png")));
 		cardDeckLabel.setSize(100,96);
 
 		cardPileLabel = new JLabel();
 		cardPileLabel.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		cardPileLabel.setSize(100,96);
 
 		cardPileLabel2 = new JLabel();
 		cardPileLabel2.setSize(100,96);
 
 		cardPileLabel3 = new JLabel();
 		cardPileLabel3.setSize(100,96);
 
 		foundation1Label = new JLabel();
 		foundation1Label.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		foundation1Label.setSize(100,96);
 
 		foundation2Label = new JLabel();
 		foundation2Label.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		foundation2Label.setSize(100,96);
 
 		foundation3Label = new JLabel();
 		foundation3Label.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		foundation3Label.setSize(100,96);
 
 		foundation4Label = new JLabel();
 		foundation4Label.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		foundation4Label.setSize(100,96);
 
 		pile1Base = new JLabel();
 		pile1Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile1Base.setSize(100,96);
 
 		pile1Label = new JLabel();
 		pile1Label.setSize(100,96);
 		
 		pile2Base = new JLabel();
 		pile2Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile2Base.setSize(100,96);
 
 		pile2Label = new JLabel();
 		pile2Label.setSize(100,96);
 
 		pile2Label2 = new JLabel();
 		pile2Label2.setSize(100,96);
 
 		pile3Base = new JLabel();
 		pile3Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile3Base.setSize(100,96);
 
 		pile3Label = new JLabel();
 		pile3Label.setSize(100,96);
 
 		pile3Label2 = new JLabel();
 		pile3Label2.setSize(100,96);
 
 		pile3Label3 = new JLabel();
 		pile3Label3.setSize(100,96);
 
 		pile4Base = new JLabel();
 		pile4Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile4Base.setSize(100,96);
  		
 		pile4Label = new JLabel();
 		pile4Label.setSize(100,96);
 
 		pile4Label2 = new JLabel();
 		pile4Label2.setSize(100,96);
 
 		pile4Label3 = new JLabel();
 		pile4Label3.setSize(100,96);
 
 		pile4Label4 = new JLabel();
 		pile4Label4.setSize(100,96);
 
 		pile5Base = new JLabel();
 		pile5Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile5Base.setSize(100,96);
 		
 		pile5Label = new JLabel();
 		pile5Label.setSize(100,96);
 
 		pile5Label2 = new JLabel();
 		pile5Label2.setSize(100,96);
 
 		pile5Label3 = new JLabel();
 		pile5Label3.setSize(100,96);
 
 		pile5Label4 = new JLabel();
 		pile5Label4.setSize(100,96);
 
 		pile5Label5 = new JLabel();
 		pile5Label5.setSize(100,96);
 
 		pile6Base = new JLabel();
 		pile6Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile6Base.setSize(100,96);
 		
 		pile6Label = new JLabel();
 		pile6Label.setSize(100,96);
 
 		pile6Label2 = new JLabel();
 		pile6Label2.setSize(100,96);
 
 		pile6Label3 = new JLabel();
 		pile6Label3.setSize(100,96);
 
 		pile6Label4 = new JLabel();
 		pile6Label4.setSize(100,96);
 
 		pile6Label5 = new JLabel();
 		pile6Label5.setSize(100,96);
 
 		pile6Label6 = new JLabel();
 		pile6Label6.setSize(100,96);
 
 		pile7Base = new JLabel();
 		pile7Base.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		pile7Base.setSize(100,96);
 		
 		pile7Label = new JLabel();
 		pile7Label.setSize(100,96);
 
 		pile7Label2 = new JLabel();
 		pile7Label2.setSize(100,96);
 
 		pile7Label3 = new JLabel();
 		pile7Label3.setSize(100,96);
 
 		pile7Label4 = new JLabel();
 		pile7Label4.setSize(100,96);
 
 		pile7Label5 = new JLabel();
 		pile7Label5.setSize(100,96);
 
 		pile7Label6 = new JLabel();
 		pile7Label6.setSize(100,96);
 
 		pile7Label7 = new JLabel();
 		pile7Label7.setSize(100,96);
 
 		cardDeckLabel.setLocation(16, 8);
 		cardPileLabel.setLocation(128, 8);
 		cardPileLabel2.setLocation(138, 8);
 		cardPileLabel3.setLocation(148, 8);
 		foundation1Label.setLocation(352, 8);
 		foundation2Label.setLocation(464, 8);
 		foundation3Label.setLocation(576, 8);
 		foundation4Label.setLocation(688, 8);
 
 		pile1Base.setLocation(16, 200);		
 		pile1Label.setLocation(16, 200);
 		pile2Base.setLocation(128, 200);
 		pile2Label.setLocation(128, 200);
 		pile2Label2.setLocation(128,215);
 		pile3Base.setLocation(240, 200);		
 		pile3Label.setLocation(240, 200);
 		pile3Label2.setLocation(240,215);
 		pile3Label3.setLocation(240,230);
 		pile4Base.setLocation(352, 200);		
 		pile4Label.setLocation(352, 200);
 		pile4Label2.setLocation(352, 215);
 		pile4Label3.setLocation(352, 230);
 		pile4Label4.setLocation(352, 245);
 		pile5Base.setLocation(464, 200);		
 		pile5Label.setLocation(464, 200);
 		pile5Label2.setLocation(464, 215);
 		pile5Label3.setLocation(464, 230);
 		pile5Label4.setLocation(464, 245);
 		pile5Label5.setLocation(464, 260);
 		pile6Base.setLocation(576, 200);		
 		pile6Label.setLocation(576, 200);
 		pile6Label2.setLocation(576, 215);
 		pile6Label3.setLocation(576, 230);
 		pile6Label4.setLocation(576, 245);
 		pile6Label5.setLocation(576, 260);
 		pile6Label6.setLocation(576, 275);
 		pile7Base.setLocation(688, 200);		
 		pile7Label.setLocation(688, 200);
 		pile7Label2.setLocation(688, 215);
 		pile7Label3.setLocation(688, 230);
 		pile7Label4.setLocation(688, 245);
 		pile7Label5.setLocation(688, 260);
 		pile7Label6.setLocation(688, 275);
 		pile7Label7.setLocation(688, 290);
 
 		
 		getContentPane().add(pile1Label);
 		getContentPane().add(pile1Base);
 		getContentPane().add(pile2Label2);		
 		getContentPane().add(pile2Label);
 		getContentPane().add(pile2Base);
 		getContentPane().add(pile3Label3);
 		getContentPane().add(pile3Label2);
 		getContentPane().add(pile3Label);
 		getContentPane().add(pile3Base);
 		getContentPane().add(pile4Label4);		
 		getContentPane().add(pile4Label3);
 		getContentPane().add(pile4Label2);
 		getContentPane().add(pile4Label);
 
 
 
 
 
 		getContentPane().add(pile4Base);
 		getContentPane().add(pile5Label5);
 		getContentPane().add(pile5Label4);
 		getContentPane().add(pile5Label3);
 		getContentPane().add(pile5Label2);		
 		getContentPane().add(pile5Label);
 		getContentPane().add(pile5Base);
 		getContentPane().add(pile6Label6);		
 		getContentPane().add(pile6Label5);		
 		getContentPane().add(pile6Label4);		
 		getContentPane().add(pile6Label3);		
 		getContentPane().add(pile6Label2);		
 		getContentPane().add(pile6Label);
 		getContentPane().add(pile6Base);
 		getContentPane().add(pile7Label7);		
 		getContentPane().add(pile7Label6);		
 		getContentPane().add(pile7Label5);		
 		getContentPane().add(pile7Label4);		
 		getContentPane().add(pile7Label3);		
 		getContentPane().add(pile7Label2);		
 		getContentPane().add(pile7Label);
 		getContentPane().add(pile7Base);
 
 		getContentPane().add(cardDeckLabel);
 		getContentPane().add(cardPileLabel3);		
 		getContentPane().add(cardPileLabel2);
 		getContentPane().add(cardPileLabel);
 		getContentPane().add(foundation1Label);
 		getContentPane().add(foundation2Label);
 		getContentPane().add(foundation3Label);
 		getContentPane().add(foundation4Label);
 		
 		// play initial cards
 
 		// pile 1
 		pile1 = new Stack<Card>();
 		pile1Labels = new Stack<JLabel>();
 		pile1Labels.push(pile1Base);
 		// first card
 		Card pile1Init = new Card(dealCard());
 		pile1Init.setDealt(true);
 		pile1.push(pile1Init);
 		pile1Label.setIcon(pile1Init.getImage());
 		pile1Labels.push(pile1Label);
 
 		// pile 2		
 		pile2 = new Stack<Card>();
 		pile2Labels = new Stack<JLabel>();
 		pile2Labels.push(pile2Base);	
 		// first card	
 		Card pile2Init1 = new Card(dealCard());
 		pile2Init1.setDealt(true);
 		pile2Init1.setFaceUp(false);
 		pile2.push(pile2Init1);
 		pile2Label.setIcon(pile2Init1.getImage());
 		pile2Labels.push(pile2Label);
 		// second card
 		Card pile2Init2 = new Card(dealCard());
 		pile2Init2.setDealt(true);
 		pile2.push(pile2Init2);
 		pile2Label2.setIcon(pile2Init2.getImage());
 		pile2Labels.push(pile2Label2);
 
 		// pile3
 		pile3 = new Stack<Card>();
 		pile3Labels = new Stack<JLabel>();
 		pile3Labels.push(pile3Base);
 		// first card
 		Card pile3Init1 = new Card(dealCard());
 		pile3Init1.setDealt(true);
 		pile3Init1.setFaceUp(false);
 		pile3.push(pile3Init1);
 		pile3Label.setIcon(pile3Init1.getImage());
 		pile3Labels.push(pile3Label);
 		// second card
 		Card pile3Init2 = new Card(dealCard());
 		pile3Init2.setDealt(true);
 		pile3Init2.setFaceUp(false);
 		pile3.push(pile3Init2);
 		pile3Label2.setIcon(pile3Init2.getImage());
 		pile3Labels.push(pile3Label2);
 		// third card
 		Card pile3Init3 = new Card(dealCard());
 		pile3Init3.setDealt(true);
 		pile3.push(pile3Init3);
 		pile3Label3.setIcon(pile3Init3.getImage());
 		pile3Labels.push(pile3Label3);
 
 		// pile4
 		pile4 = new Stack<Card>();
 		pile4Labels = new Stack<JLabel>();
 		pile4Labels.push(pile4Base);
 		//first card
 		Card pile4Init1 = new Card(dealCard());
 		pile4Init1.setDealt(true);
 		pile4Init1.setFaceUp(false);
 		pile4.push(pile4Init1);
 		pile4Label.setIcon(pile4Init1.getImage());
 		pile4Labels.push(pile4Label);
 		//second card
 		Card pile4Init2 = new Card(dealCard());
 		pile4Init2.setDealt(true);
 		pile4Init2.setFaceUp(false);
 		pile4.push(pile4Init2);
 		pile4Label2.setIcon(pile4Init2.getImage());
 		pile4Labels.push(pile4Label2);
 		//third card
 		Card pile4Init3 = new Card(dealCard());
 		pile4Init3.setDealt(true);
 		pile4Init3.setFaceUp(false);
 		pile4.push(pile4Init3);
 		pile4Label3.setIcon(pile4Init3.getImage());
 		pile4Labels.push(pile4Label3);
 		//fourth card
 		Card pile4Init4 = new Card(dealCard());
 		pile4Init4.setDealt(true);
 		pile4.push(pile4Init4);
 		pile4Label4.setIcon(pile4Init4.getImage());
 		pile4Labels.push(pile4Label4);
 
 		// pile5
 		pile5 = new Stack<Card>();
 		pile5Labels = new Stack<JLabel>();
 		pile5Labels.push(pile5Base);
 		//first card
 		Card pile5Init1 = new Card(dealCard());
 		pile5Init1.setDealt(true);
 		pile5Init1.setFaceUp(false);
 		pile5.push(pile5Init1);
 		pile5Label.setIcon(pile5Init1.getImage());
 		pile5Labels.push(pile5Label);
 		//second card
 		Card pile5Init2 = new Card(dealCard());
 		pile5Init2.setDealt(true);
 		pile5Init2.setFaceUp(false);
 		pile5.push(pile5Init2);
 		pile5Label2.setIcon(pile5Init2.getImage());
 		pile5Labels.push(pile5Label2);
 		//third card
 		Card pile5Init3 = new Card(dealCard());
 		pile5Init3.setDealt(true);
 		pile5Init3.setFaceUp(false);
 		pile5.push(pile5Init3);
 		pile5Label3.setIcon(pile5Init3.getImage());
 		pile5Labels.push(pile5Label3);
 		//fourth card
 		Card pile5Init4 = new Card(dealCard());
 		pile5Init4.setDealt(true);
 		pile5Init4.setFaceUp(false);
 		pile5.push(pile5Init4);
 		pile5Label4.setIcon(pile5Init4.getImage());
 		pile5Labels.push(pile5Label4);
 		//fifth card
 		Card pile5Init5 = new Card(dealCard());
 		pile5Init5.setDealt(true);
 		pile5.push(pile5Init5);
 		pile5Label5.setIcon(pile5Init5.getImage());
 		pile5Labels.push(pile5Label5);
 
 		// pile6
 		pile6 = new Stack<Card>();
 		pile6Labels = new Stack<JLabel>();
 		pile6Labels.push(pile6Base);
 		//first card
 		Card pile6Init1 = new Card(dealCard());
 		pile6Init1.setDealt(true);
 		pile6Init1.setFaceUp(false);
 		pile6.push(pile6Init1);
 		pile6Label.setIcon(pile6Init1.getImage());
 		pile6Labels.push(pile6Label);
 		//second card
 		Card pile6Init2 = new Card(dealCard());
 		pile6Init2.setDealt(true);
 		pile6Init2.setFaceUp(false);
 		pile6.push(pile6Init2);
 		pile6Label2.setIcon(pile6Init2.getImage());
 		pile6Labels.push(pile6Label2);
 		//third card
 		Card pile6Init3 = new Card(dealCard());
 		pile6Init3.setDealt(true);
 		pile6Init3.setFaceUp(false);
 		pile6.push(pile6Init3);
 		pile6Label3.setIcon(pile6Init3.getImage());
 		pile6Labels.push(pile6Label3);
 		//fourth card
 		Card pile6Init4 = new Card(dealCard());
 		pile6Init4.setDealt(true);
 		pile6Init4.setFaceUp(false);
 		pile6.push(pile6Init4);
 		pile6Label4.setIcon(pile6Init4.getImage());
 		pile6Labels.push(pile6Label4);
 		//fifth card
 		Card pile6Init5 = new Card(dealCard());
 		pile6Init5.setDealt(true);
 		pile6Init5.setFaceUp(false);
 		pile6.push(pile6Init5);
 		pile6Label5.setIcon(pile6Init5.getImage());
 		pile6Labels.push(pile6Label5);
 		//sixth card
 		Card pile6Init6 = new Card(dealCard());
 		pile6Init6.setDealt(true);
 		pile6.push(pile6Init6);
 		pile6Label6.setIcon(pile6Init6.getImage());
 		pile6Labels.push(pile6Label6);
 
 		// pile7
 		pile7 = new Stack<Card>();
 		pile7Labels = new Stack<JLabel>();
 		pile7Labels.push(pile7Base);
 		//first card
 		Card pile7Init1 = new Card(dealCard());
 		pile7Init1.setDealt(true);
 		pile7Init1.setFaceUp(false);
 		pile7.push(pile7Init1);
 		pile7Label.setIcon(pile7Init1.getImage());
 		pile7Labels.push(pile7Label);
 		//second card
 		Card pile7Init2 = new Card(dealCard());
 		pile7Init2.setDealt(true);
 		pile7Init2.setFaceUp(false);
 		pile7.push(pile7Init2);
 		pile7Label2.setIcon(pile7Init2.getImage());
 		pile7Labels.push(pile7Label2);
 		//third card
 		Card pile7Init3 = new Card(dealCard());
 		pile7Init3.setDealt(true);
 		pile7Init3.setFaceUp(false);
 		pile7.push(pile7Init3);
 		pile7Label3.setIcon(pile7Init3.getImage());
 		pile7Labels.push(pile7Label3);
 		//fourth card
 		Card pile7Init4 = new Card(dealCard());
 		pile7Init4.setDealt(true);
 		pile7Init4.setFaceUp(false);
 		pile7.push(pile7Init4);
 		pile7Label4.setIcon(pile7Init4.getImage());
 		pile7Labels.push(pile7Label4);
 		//fifth card
 		Card pile7Init5 = new Card(dealCard());
 		pile7Init5.setDealt(true);
 		pile7Init5.setFaceUp(false);
 		pile7.push(pile7Init5);
 		pile7Label5.setIcon(pile7Init5.getImage());
 		pile7Labels.push(pile7Label5);
 		//sixth card
 		Card pile7Init6 = new Card(dealCard());
 		pile7Init6.setDealt(true); 
 		pile7Init6.setFaceUp(false);
 		pile7.push(pile7Init6);
 		pile7Label6.setIcon(pile7Init6.getImage());
 		pile7Labels.push(pile7Label6);
 		//seventh card
 		Card pile7Init7 = new Card(dealCard());
 		pile7Init7.setDealt(true);
 		pile7.push(pile7Init7);
 		pile7Label7.setIcon(pile7Init7.getImage());
 		pile7Labels.push(pile7Label7);
 
 		// add stacks to pile list
 		pileList = new Stack[7];
 		pileList[0] = pile1;
 		pileList[1] = pile2;
 		pileList[2] = pile3;
 		pileList[3] = pile4;
 		pileList[4] = pile5;
 		pileList[5] = pile6;
 		pileList[6] = pile7;
 
 		labelList = new Stack[7];
 		labelList[0] = pile1Labels;
 		labelList[1] = pile2Labels;
 		labelList[2] = pile3Labels;
 		labelList[3] = pile4Labels;
 		labelList[4] = pile5Labels;
 		labelList[5] = pile6Labels;
 		labelList[6] = pile7Labels;
 
 	}
 
 	public Card dealCard()
 	{
 		int r = (int)(Math.random()*deck.size());
 		Card retCard = new Card();
 		retCard = (Card) (deck.get(r));
 		deck.remove(r);
 		return retCard;
 	}
 
 	public Card dealSeq()
 	{
 		Card retCard = new Card();
 		retCard = (Card) (deck.get(0));
 		deck.remove(0);
 		return retCard;
 	}
 
 	public void dealThree()
 	{
 		if(deck.size() >= 3)
 		{
 			Card card1, card2, card3;
 			if(firstDeal == true) {		
 				card1 = new Card(dealCard());
 				card2 = new Card(dealCard());
 				card3 = new Card(dealCard());
 			}
 			else {
 				card1 = new Card(dealSeq());
 				card2 = new Card(dealSeq());
 				card3 = new Card(dealSeq());
 			}
 
 			card1.setDealt(true);
 			cardPile.add(card1);		
 			cardPileLabel.setIcon(card1.getImage());
 
 			card2.setDealt(true);
 			cardPile.add(card2);		
 			cardPileLabel2.setIcon(card2.getImage());
 
 			card3.setDealt(true);
 			cardPile.add(card3);		
 			cardPileLabel3.setIcon(card3.getImage());
 		}
 		else if(deck.size() == 2)
 		{
 			// handle two cards	
 		}
 		else if(deck.size() == 1)
 		{
 			// handle one card	
 		}
 		else if(deck.size() == 0)
 		{
 			// reset deck, must keep order
 			if(firstDeal == true)
 			{
 				firstDeal = false;
 			}
 			int cardPileSize = cardPile.size();
 			for(int i=0; i<cardPileSize; i++)
 			{
 				Card toAdd = cardPile.get(0);
 				toAdd.setDealt(false);
 				deck.add(toAdd);
 				cardPile.remove(0);
 			}
 			// set icons for 3 cards null, reset deck
 			cardDeckLabel.setIcon(new ImageIcon(getClass().getResource("./images/deck.png")));
 			cardPileLabel.setIcon(null);
 			cardPileLabel2.setIcon(null);
 			cardPileLabel3.setIcon(null);	
 		}		
 
 		// show deck is empty
 		if(deck.size() == 0)
 		{
 			cardDeckLabel.setIcon(new ImageIcon(getClass().getResource("./images/emptystack.png")));
 		}
 	}
 
 	class MouseManager extends MouseAdapter implements MouseMotionListener {
 
 		private Stack cardSrc;
 		private Stack labelSrc;
 		private JLabel draggedCard;
 		private Card movingCard;
 
 		private Stack tempLabels = new Stack<Card>();
 		private Stack tempCards = new Stack<Card>();
 		private boolean multCards = false;
 		
 		public void mouseMoved(MouseEvent e) {
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			Point p = e.getPoint();
 			if(dragging) {
 				draggedCard.setLocation((int)p.getX()-50,(int)p.getY()-48);
 			}
 		}
 
 		public void mousePressed(MouseEvent e) {
 			if( !e.isMetaDown() && !e.isControlDown() && !e.isShiftDown() ) {
 				Point p = e.getPoint();
 				if(hasCard(cardDeckLabel,p)){
 					dealThree();
 				}
 				else{
 					for( int i=0; i<pileList.length; i++) {
 						if(!pileList[i].isEmpty() && hasCard((JLabel)labelList[i].peek(), p)) { // this selects the single card on the end of a stack
 							cardSrc = pileList[i];
 							labelSrc = labelList[i];
 
 							draggedCard = (JLabel)labelList[i].pop();
 							getContentPane().remove(draggedCard); // remove to draw on top
 							getContentPane().add(draggedCard, 0); //0th spot the top
 							movingCard = (Card)pileList[i].pop();
 							dragging = true;
 							draggedCard.setLocation((int)p.getX()-50,(int)p.getY()-48);
 						}
 						else { // check if stack selected
 							
 							/*do{
 								tempLabels.push(labelList[i].pop());
 								tempCards.push(pileList[i].pop());
 								if(hasCard((JLabel)labelList[i].peek(), p) && ((Card)pileList[i].peek()).isFaceUp() == true)
 								{
 									System.out.println("multiple pile");
 									multCards = true;
 									// create pile of labels
 									// keeps cards in temp stack, pop labels and make relational to draggedCard
 									movingCard = (Card)pileList[i].pop();
 									draggedCard = (JLabel)labelList[i].pop();
 									dragging = true;
 									draggedCard.setLocation((int)p.getX()-50,(int)p.getY()-48);
 									for(int j=0; tempLabels.isEmpty() == false; j++)
 									{
 										JLabel popped = (JLabel)tempLabels.pop();
 										popped.setLocation((int)draggedCard.getX()-((j+1)*15),(int)draggedCard.getY()-((j+1)*15));
 									}
 								}
 								
 							}
 							while(!pileList[i].isEmpty() && !labelList[i].isEmpty());
 							if(multCards == false)
 							{
 								while(!tempCards.isEmpty()) {
 									pileList[i].push(tempCards.pop());
 									labelList[i].push(tempLabels.pop());
 								}
 							}*/
 						}
 					}
 				}				
 		    	}
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			Point p = e.getPoint();
 			boolean overlap = false;
 			if(dragging) {
 				// evaluate where the dragged card is
 				for( int i=0; i<pileList.length; i++ ) {
 					if(!pileList[i].isEmpty() && hasCard((JLabel)labelList[i].peek(), p) && labelList[i] != labelSrc) {
 						// flag it found an overlap
 						overlap = true;						
 						// check to see if valid stack
 						if(checkValid(movingCard, (Card)pileList[i].peek())) {
 							// place on stack
 							Point sendTo = ((JLabel)labelList[i].peek()).getLocation();
 							draggedCard.setLocation((int)sendTo.getX(),(int)sendTo.getY()+15);
 							pileList[i].push(movingCard);
 							labelList[i].push(draggedCard);
 
 							// check if there is a top of the src to flip
 							if(!cardSrc.isEmpty())
 							{
 								Card checkNewTop = (Card)cardSrc.peek();
 								JLabel checkNewTopLabel = (JLabel)labelSrc.peek();
 								if( !checkNewTop.isFaceUp()) {
 									checkNewTop.setFaceUp(true);
 									checkNewTopLabel.setIcon(checkNewTop.getImage());
 								}
 							}
							dragging = false;
							draggedCard = null;
							movingCard = null;
 						}	
 						else{
 							// invalid, send back
 							// send it back
 							// get top of src stack and send it back on top
 							if(labelSrc.size() > 1){				
 								Point sendTo = ((JLabel)labelSrc.peek()).getLocation();
 								draggedCard.setLocation((int)sendTo.getX(),(int)sendTo.getY()+15);
 							}
 							else {
 								// place on top of blank spot
 								Point sendTo = ((JLabel)labelSrc.peek()).getLocation();
 								draggedCard.setLocation((int)sendTo.getX(),(int)sendTo.getY());
 							}
 							// add back to top
 							labelSrc.push(draggedCard);
 							cardSrc.push(movingCard);
 
 							dragging = false;
 							draggedCard = null;
 							movingCard = null;
 						}
 
 					}
 				}
 				if(overlap == false) {
 					// send it back
 					// get top of src stack and send it back on top
 					if(labelSrc.size() > 1){				
 						Point sendTo = ((JLabel)labelSrc.peek()).getLocation();
 						draggedCard.setLocation((int)sendTo.getX(),(int)sendTo.getY()+15);
 					}
 					else {
 						// place on top of blank spot
 						Point sendTo = ((JLabel)labelSrc.peek()).getLocation();
 						draggedCard.setLocation((int)sendTo.getX(),(int)sendTo.getY());
 					}
 					// add back to top
 					labelSrc.push(draggedCard);
 					cardSrc.push(movingCard);
 
 					dragging = false;
 					draggedCard = null;
 					movingCard = null;
 				}	
 			}
 		}
 
 	}
 
 	public boolean hasCard(JLabel label, Point p)
 	{
 		if((label.getX() <= p.getX() && label.getY() <= p.getY() ) && (label.getX()+ 100 >= p.getX() && label.getY() + 96 >= p.getY())) {
 			return true;		
 		}
 		else {
 			return false;
 		}
 
 	}
 
 	public boolean checkValid(Card bottomCard, Card topCard)
 	{
 		String bottomSuit = bottomCard.getSuit();		
 		int bottomValue = bottomCard.getValue();
 
 		String topSuit = topCard.getSuit();
 		int topValue = topCard.getValue();
 
 		if(bottomSuit == "s" || bottomSuit == "c"){
 			if( (topSuit == "h" || topSuit == "d") && (bottomValue + 1 == topValue) ) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 
 		else{
 			if( (topSuit == "s" || topSuit == "c") && (bottomValue + 1 == topValue) ) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 	}
 
 }

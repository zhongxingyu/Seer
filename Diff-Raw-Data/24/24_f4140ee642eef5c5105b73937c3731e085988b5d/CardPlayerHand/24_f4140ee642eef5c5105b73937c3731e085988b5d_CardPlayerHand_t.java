 package fr.eurecom.util;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.view.View;
 import android.widget.RelativeLayout;
 import fr.eurecom.cardify.Game;
 import fr.eurecom.cardify.R;
 
 public class CardPlayerHand {
 
 	public List<CardView> cardStack;
 	public List<CardView> cardPublic;
 	public Game game;
 	
 	private Point displaySize;
 	private CardView ghostView;
 	
 	public static int width;
 	public static int height;
 	
 	public CardPlayerHand(Game game){
 		this.game = game;
 		cardStack = new LinkedList<CardView>();
 		cardPublic = new LinkedList<CardView>();
 		displaySize = game.getDisplaySize();
 		
 		initCardSize();
 	}
 	
 	public void dealCards(List<Card> cards){
 		for (Card card : cards){
 			CardView view = addCardGraphics(card);
 			cardStack.add(view);
 		}
 		Collections.sort(cardStack, new CardComparator(CardSortingRule.S_H_D_C_ACE_HIGH));
 		stackCards();
 	}
 	
 	public void reDealCards(List<Card> cards){
 		for (CardView v : cardStack) {
 			removeCardGraphics(v);
 		}
 		cardStack.clear();
 		for (CardView v : cardPublic) {
 			removeCardGraphics(v);
 		}
 		cardPublic.clear();
 		
 		for (Card card : cards){
 			CardView view = addCardGraphics(card);
 			cardStack.add(view);
 		}
 		Collections.sort(cardStack, new CardComparator(CardSortingRule.S_H_D_C_ACE_HIGH));
 		stackCards();
 	}
 	
 	public void redeal(int numberOfCards) {
 		reDealCards(game.getDeck().draw(numberOfCards));
 		game.getClient().pushNewCards(game.getDeck(), numberOfCards);
 	}
 	
 	public void shuffle() {
 		for (CardView v : cardPublic) {
 			game.getDeck().addCard(v.getCard());
 			removeCardGraphics(v);
 		}
 		game.getDeck().shuffle();
 		game.getClient().pushShuffledDeck(game.getDeck());
 	}
 	
 	public void blindShuffleDeck(String[] cards) {
 		game.getDeck().clear();
 		for (CardView v : cardPublic) {
 			game.getDeck().addCard(v.getCard());
 			removeCardGraphics(v);
 		}
 		game.getDeck().shuffle();
 	}
 	
 	protected void addToDeckFromStack(CardView view) {
 		game.getDeck().addCard(view.getCard());
 		game.getDeck().toggleHighlight(false);
 
 		this.printMessage("You added", "to the deck", view.getCard(), !view.getCard().getTurned());
 		game.getClient().publishAddCardToDeck(view.getCard(), game.getDeviceName());
 
 		stackCards();
 		removeCardGraphics(view);
 	}
 	
 	protected void addToDeckFromPublic(CardView view) {
 		game.getDeck().addCard(view.getCard());
 		game.getDeck().toggleHighlight(false);
 		
 		this.printMessage("You added", "to the deck", view.getCard(), !view.getCard().getTurned());
 		game.getClient().publishAddCardToDeck(view.getCard(), game.getDeviceName());
 		
 		removeCardGraphics(view);
 	}
 	
 	protected void addToPublicFromStack(CardView view) {
 		cardPublic.add(view);
 		game.getClient().publishPutCardInPublicZone(view.getCard(), getPosX(view), getPosY(view), game.getDeviceName());
 		stackCards();
 		this.printMessage("You played", "", view.getCard(), !view.getCard().getTurned());
 	}
 	
 	protected void addToPublicFromDeck(CardView view) {
 		cardPublic.add(view);
 		game.getClient().publishPutCardInPublicZone(view.getCard(), getPosX(view), getPosY(view), game.getDeviceName());
 		this.printMessage("You drew", "from the deck", view.getCard(), false);
 	}
 	
 	protected void addToStackFromPublic(CardView view) {
 		cardStack.add(getPositionInStack(view.getX()), view);
 		stackCards();
 		game.getClient().publishTakeCardFromPublicZone(view.getCard(), game.getDeviceName());
 		this.printMessage("You took", "from the table", view.getCard(), !view.getCard().getTurned());
 	}
 	
 	protected void addToStackFromDeck(CardView view) {
 		cardStack.add(view);
 		stackCards();
 		game.getClient().publishDrawFromDeckToStack(view.getCard(), game.getDeviceName());
 		this.printMessage("You drew", "from the deck to your hand", view.getCard(), false);
 	}
 	
 	protected void broadcastPositionUpdate(CardView view) {
 		game.getClient().publishCardPositionUpdate(view.getCard(), getPosX(view), getPosY(view), game.getDeviceName());
 	}
 	
 	protected int getPositionInStack(float x) {
 		int pos = 0;
 		for (CardView v : cardStack) {
 			if (v.getX() >= x) break;
 			pos++;
 		}
 		return pos;
 	}
 	
 	public void blindAddToDeck(char suit, int face, boolean turned, String originatorName) {
 		CardView view = getCardViewFromPublic(suit, face);
 		
 		if (view == null) { //from opponents stack, no alterations in graphics
 			Card card = new Card(suit, face, true);
 			game.getDeck().addCard(card);
 			this.printMessage(originatorName+" added", "from his stack to the deck", card, false);
 		} else { //from public area
 			cardPublic.remove(view);
 			game.getDeck().addCard(view.getCard());
 			this.printMessage(originatorName+" added", "from the table to the deck", view.getCard(), !view.getCard().getTurned());
 			removeCardGraphics(view);
 		}
 	}
 	
 	public void blindDrawFromDeckToStack(String originatorName) {
 		game.getDeck().drawFromDeck();
 		this.printMessage(originatorName+" drew", "from the deck to his hand", null, false);
 	}
 
 	
 	public void blindAddToPublic(char suit, int face, boolean turned, float x, float y, String originatorName) {
 		Card top = game.getDeck().peek();
 		CardView view;
 		
 		if (top != null && suit == top.getSuit() && face == top.getFace()) { //from deck
 			view = addCardGraphics(game.getDeck().drawFromDeck());
 			this.printMessage(originatorName+" drew", "from the deck to the table", view.getCard(), false);
 		} else { //from opponents stack
 			Card card = new Card(suit, face, turned);
 			view = addCardGraphics(card);
 			this.printMessage(originatorName+" played", "", view.getCard(), !view.getCard().getTurned());
 		}
 		
 		view.setX(displaySize.x * x);
 		view.setY(displaySize.y * y);
 		cardPublic.add(view);
 		
 	}
 	
 	public void blindRemoveFromPublic(char suit, int face, String originatorName){
 		CardView view = getCardViewFromPublic(suit, face);
 		if (cardPublic.remove(view)) {
 			this.printMessage(originatorName+" took", "from the table", view.getCard(), !view.getCard().getTurned());
 			removeCardGraphics(view);
 		}
 	}
 	
 	public void blindTurnInPublic(char suit, int face, String originatorName) {
 		CardView view = getCardViewFromPublic(suit, face);
 		if (view != null) {
 			view.getCard().turn();
 			String turned = view.getCard().getTurned() ? "face down" : "face up";
 			view.updateGraphics();
 			
 			this.printMessage(originatorName+" turned", turned, view.getCard(), true);
 		}
 	}
 	
 	public void blindMoveInPublic(char suit, int face, float x, float y) {
 		CardView view = getCardViewFromPublic(suit, face);
 		if (view != null) {
 			view.setX(displaySize.x * x);
 			view.setY(displaySize.y * y);
 			view.bringToFront();
 		}
 	}
 	
 	public void blindDealCards(String[] cardStrings) {
 		this.dealCards(getCardListFromStrings(cardStrings));
 	}
 	
 	public void blindNewCards(String[] cardStrings) {
		final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("The deck was re-dealt with %s cards each\n", cardStrings.length));
 		sb.append((Spannable) game.getMessageStream().getText());
 		game.getMessageStream().setText(sb);
 		this.reDealCards(getCardListFromStrings(cardStrings));
 	}
 	
 	public void blindAddDeck(String[] cardStrings) {
 		game.removeView(game.getDeck());
 		if (cardStrings == null) {
 			game.setDeck(new CardDeck(game.getApplicationContext(), new LinkedList<Card>()));
 		} else {
 			game.setDeck(new CardDeck(game.getApplicationContext(),getCardListFromStrings(cardStrings)));
 		}
 		game.addView(game.getDeck());
 		setGhost();
 	}
 	
 	private CardView getCardViewFromPublic(char suit, int face) {
 		CardView view = null;
 		for (CardView v : cardPublic) {
 			if (v.getCard().getSuit() == suit && v.getCard().getFace() == face) {
 				view = v;
 				break;
 			}
 		}
 		return view;
 	}
 	
 	private List<Card> getCardListFromStrings(String[] strings) {
 		List<Card> cards = new LinkedList<Card>();
 		for (String str : strings) {
 			boolean turned = str.charAt(0) == '1' ? true : false;
 			char suit = str.charAt(1);
 			int face = Integer.parseInt(str.substring(2));
 			cards.add(new Card(suit, face, turned));
 		}
 		return cards;
 	}
 	
 	public void stackCards(){
 		if (cardStack.isEmpty()) return;
 		
 		int maximumStackWidth = displaySize.x - 10; //5px free on each side of stack
 		double pixelsBetweenCards = cardStack.size() != 1 ? Math.min(width/2, (maximumStackWidth - width)/(cardStack.size() - 1)) : 0;
 		int y = displaySize.y - height;
 		int x = 0;
 		
 		if (pixelsBetweenCards <= width/2) {
 			x = (int) Math.round(((displaySize.x - ((cardStack.size() - 1)*pixelsBetweenCards + width))/2));
 		} else {
 			x = 5;
 		}
 		
 		for (CardView view : cardStack) {
 			view.setX(x);
 			view.setY(y);
 			x += Math.round(pixelsBetweenCards);
 			queueBringToFront(view);
 		}
 		applyBringToFront(cardStack.get(0));
 	}
 	
 	public boolean inStackZone(float x, float y) {
 		if (x < 0 || x > displaySize.x) return false;
 		if (y < displaySize.y - 1.75*height || y > displaySize.y) return false;
 		return true;
 	}
 	
 	public boolean inCardDeck(float x, float y) {
 		CardDeck d = game.getDeck();
 		float w = d.getWidth()/2;
 		float h = d.getHeight()/2;
 		
 		return 	(x < d.getX() + w) && 
 				(x > d.getX() - w) &&
 				(y < d.getY() + h) &&
 				(y > d.getY() - h);
 	}
 	
 	public void liftCard(CardView view) {
 		view.bringToFront();
 		view.setAlpha(0.5f);
 	}
 	
 	public void dropDeck() {
 		moveGhost();
 	}
 	
 	public void dropCard(CardView view){
 		if(inStackZone(view.getX(), view.getY())) {
 			if (cardPublic.remove(view)) {
 				addToStackFromPublic(view);
 			} else if (cardStack.remove(view)){
 				cardStack.add(getPositionInStack(view.getX()),view);
 				stackCards();
 			}
 		} else if (inCardDeck(view.getX(), view.getY())) {
 			if (cardPublic.remove(view)) {
 				addToDeckFromPublic(view);
 			} else if(cardStack.remove(view)) {
 				addToDeckFromStack(view);
 			}
 		} else {
 			if (cardStack.remove(view)) {
 				addToPublicFromStack(view);
 			}
 		}
 		
 		view.setAlpha(1.0f);
 	}
 	
 	public void dropGhost(CardView view) {
 		if (inStackZone(view.getX(), view.getY())) {
 			view.setCard(game.getDeck().drawFromDeck());
 			view.setOnTouchListener(view);
 			view.setAlpha(1.0f);
 			addToStackFromDeck(view);
 		} else if (inCardDeck(view.getX(), view.getY())) {
 			view.setAlpha(0.0f);
 			game.getDeck().toggleHighlight(false);
 			moveGhost();
 			return;
 		} else {
 			view.setCard(game.getDeck().drawFromDeck());
 			view.setOnTouchListener(view);
 			view.setAlpha(1.0f);
 			addToPublicFromDeck(view);
 		}
 		
 		setGhost();
 	}
 	
 	public void moveCard(CardView view) {
 		if (inCardDeck(view.getX(), view.getY())) {
 			game.getDeck().toggleHighlight(true);
 		} else {
 			game.getDeck().toggleHighlight(false);
 		}
 		
 	}
 	
 	public void turnCard(CardView view) {
 		if(!inStackZone(view.getX(), view.getY())) {
 			String turned = view.getCard().getTurned() ? "face down" : "face up";
 			this.printMessage("You turned", turned, view.getCard(), true);
 			game.getClient().publishTurnCardInPublicZone(view.getCard(), game.getDeviceName());
 		} 
 	}
 	
 	private void queueBringToFront(View v){
 		v.bringToFront();
 	}
 	
 	private void applyBringToFront(View view){
 		view.requestLayout();
 		view.invalidate();
 	}
 	
 	public void sortCards(CardSortingRule sorting) {
 		Collections.sort(cardStack, new CardComparator(sorting));
 		stackCards();
 	}
 	
 	private void printMessage(String start, String end, Card c, boolean showValue) {
 		String value;
 		if (showValue) {
 			String suitString;
 			String faceString;
 			switch (c.getSuit()) {
 			case 'h':
 				suitString = "♥";
 				break;
 			case 's':
 				suitString = "♠";
 				break;
 			case 'c':
 				suitString = "♣";
 				break;
 			case 'd':
 				suitString = "♦";
 				break;
 			default:
 				suitString = "";
 				break;
 			}
 			switch (c.getFace()) {
 			case 1:
 				faceString = "A";
 				break;
 			case 13:
 				faceString = "K";
 				break;
 			case 12:
 				faceString = "Q";
 				break;
 			case 11:
 				faceString = "J";
 				break;
 			default:
 				faceString = ""+c.getFace();
 				break;
 			}
 			value = faceString+suitString;
 		} else {
 			value = "a card";
 		}
 		String formatted = String.format("%s %s %s\n", start, value, end);
 		final SpannableStringBuilder sb = new SpannableStringBuilder(formatted);
 		if (showValue) {
 			final ForegroundColorSpan fcs = new ForegroundColorSpan((c.getSuit() == 's' || c.getSuit() == 'c') ? Color.rgb(0, 0, 0) : Color.rgb(184, 59, 50)); 
 			sb.setSpan(fcs, start.length()+1, start.length()+1+value.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
 		}
 		
 		//If there are more than 25 lines, clip the last line
 		Spannable oldMessages = game.getMessageStream().getLineCount() > 25 ? (Spannable) game.getMessageStream().getText().subSequence(0, game.getMessageStream().getText().toString().lastIndexOf("\n")) : (Spannable) game.getMessageStream().getText();
 		sb.append(oldMessages);
 		game.getMessageStream().setText(sb);
 	}
 	
 	protected CardView addCardGraphics(Card card) {
 		CardView cView = new CardView(this.game, card, this);
 		cView.setOwner(this);
 		game.addView(cView);
 		return cView;
 	}
 	
 	protected void removeCardGraphics(CardView view) {
 		game.removeView(view);
 		view = null;
 		System.gc();
 	}
 	
 	public void setGhost() {
 		ghostView = new CardView(this.game, this);
 		moveGhost();
 		this.game.addView(ghostView);
 	}
 	
 	private void moveGhost() {
 		ghostView.setLayoutParams(new RelativeLayout.LayoutParams(width,height));
 		ghostView.setX(game.getDeck().getX()+10);
 		ghostView.setY(game.getDeck().getY()+10);
 	}
 	
 	public void initCardSize() {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeResource(game.getResources(), R.drawable.back_blue, options);
 		
 		width = options.outWidth;
 		height = options.outHeight;
 	}
 	
 	private float getPosX(View view) {
 		return view.getX()/displaySize.x;
 	}
 	
 	private float getPosY(View view) {
 		return view.getY()/displaySize.y;
 	}
 
 }

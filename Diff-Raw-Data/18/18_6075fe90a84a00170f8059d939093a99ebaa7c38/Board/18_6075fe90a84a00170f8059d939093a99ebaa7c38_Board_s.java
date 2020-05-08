 package com.refnil.uqcard.data;
 
 import java.util.List;
 import java.util.Stack;
 
 import com.refnil.uqcard.event.AttackEvent;
 import com.refnil.uqcard.event.BeginGameEvent;
 import com.refnil.uqcard.event.BeginTurnEvent;
 import com.refnil.uqcard.event.DrawCardEvent;
 import com.refnil.uqcard.event.EndGameEvent;
 import com.refnil.uqcard.event.EndTurnEvent;
 import com.refnil.uqcard.event.Event;
 import com.refnil.uqcard.event.Event_Type;
 import com.refnil.uqcard.event.PutCardEvent;
 import com.refnil.uqcard.library.AbstractListenable;
 
 import android.util.Log;
 
 public class Board extends AbstractListenable<Event> {
 
 	public boolean temp;
 	private final static String TAG = "Board";
 
 	private int phase;
 	private int tour;
 	private int playerID;
 	private List<Card> opponentHandCards;
 	private List<Card> playerHandCards;
 	private List<Card> opponentBoardCards;
 	private List<Card> playerBoardCards;
 	private Stack<Card> opponentStackCards;
 	private Stack<Card> playerStackCards;
 	private Stack<Card> opponentGraveyardCards;
 	private Stack<Card> playerGraveyardCards;
 
 	public int getPhase() {
 		return phase;
 	}
 
 	public void setPhase(int phase) {
 		this.phase = phase;
 	}
 
 	public int getTour() {
 		return tour;
 	}
 
 	public void setTour(int tour) {
 		this.tour = tour;
 	}
 
 	public List<Card> getOpponentHandCards() {
 		return opponentHandCards;
 	}
 
 	public void setOpponentHandCards(List<Card> opponentCards) {
 		this.opponentHandCards = opponentCards;
 	}
 
 	public void addOpponentHandCard(Card card) {
 		this.opponentHandCards.add(card);
 	}
 
 	public void deleteOpponentHandCard(Card card) {
 		this.opponentHandCards.remove(card);
 	}
 
 	public List<Card> getPlayerHandCards() {
 		return playerHandCards;
 	}
 
 	public void setPlayerHandCards(List<Card> playerCards) {
 		this.playerHandCards = playerCards;
 	}
 
 	public void addPlayerHandCard(Card card) {
 		this.playerHandCards.add(card);
 	}
 
 	public void deletePlayerHandCard(Card card) {
 		this.playerHandCards.remove(card);
 	}
 
 	public List<Card> getOpponentBoardCards() {
 		return opponentBoardCards;
 	}
 
 	public void setOpponentBoardCards(List<Card> opponentBoardCards) {
 		this.opponentBoardCards = opponentBoardCards;
 	}
 
 	public void addOpponentBoardCard(Card card) {
 		this.opponentBoardCards.add(card);
 	}
 
 	public void deleteOpponentBoardCard(Card card) {
 		this.opponentBoardCards.remove(card);
 	}
 
 	public List<Card> getPlayerBoardCards() {
 		return playerBoardCards;
 	}
 
 	public void setPlayerBoardCards(List<Card> playerBoardCards) {
 		this.playerBoardCards = playerBoardCards;
 	}
 
 	public void addPlayerBoardCard(Card card) {
 		this.playerBoardCards.add(card);
 	}
 
 	public void deletePlayerBoardCard(Card card) {
 		this.playerBoardCards.remove(card);
 	}
 
 	public Stack<Card> getOpponentStackCards() {
 		return opponentStackCards;
 	}
 
 	public void setOpponentStackCards(Stack<Card> opponentStackCards) {
 		this.opponentStackCards = opponentStackCards;
 	}
 
 	public Card opponentTakeCardInStack() {
 		return this.opponentStackCards.pop();
 	}
 
 	public Stack<Card> getPlayerStackCards() {
 		return playerStackCards;
 	}
 
 	public void setPlayerStackCards(Stack<Card> playerStackCards) {
 		this.playerStackCards = playerStackCards;
 	}
 
 	public Card playerTakeCardInStack() {
 		return this.playerStackCards.pop();
 	}
 
 	public Stack<Card> getOpponentGraveyardCards() {
 		return opponentGraveyardCards;
 	}
 
 	public void setOpponentGraveyardCards(Stack<Card> opponentGraveyardCards) {
 		this.opponentGraveyardCards = opponentGraveyardCards;
 	}
 
 	public void opponentAddCardInGraveyard(Card c) {
 		this.opponentGraveyardCards.push(c);
 	}
 
 	public Stack<Card> getPlayerGraveyardCards() {
 		return playerGraveyardCards;
 	}
 
 	public void setPlayerGraveyardCards(Stack<Card> playerGraveyardCards) {
 		this.playerGraveyardCards = playerGraveyardCards;
 	}
 
 	public void playerAddCardInGraveyard(Card c) {
 		this.playerGraveyardCards.push(c);
 	}
 
 	public int getPlayerID() {
 		return playerID;
 	}
 
 	public void setPlayerID(int playerID) {
 		this.playerID = playerID;
 	}
 
 
 	public void receiveEvent(Event event) {
 		if (event.type == Event_Type.BEGIN_GAME) {
 			BeginGameAction((BeginGameEvent)event);
 		}
 
 		else if (event.type == Event_Type.BEGIN_TURN) {
 			BeginTurnAction((BeginTurnEvent)event);
 		}
 
 		else if (event.type == Event_Type.END_TURN) {
 			EndTurnAction((EndTurnEvent)event);
 		}
 
 		else if (event.type == Event_Type.END_GAME) {
 			EndGameAction((EndGameEvent)event);
 		}
 		
 		else if(event.type == Event_Type.DRAW_CARD){
 			DrawCardAction((DrawCardEvent)event);
 		}
 		
 		else if(event.type == Event_Type.PUT_CARD)
 		{
 			PutCardAction((PutCardEvent)event);
 		}
 
 		else if (event instanceof AttackEvent) {
 			BattleAction((AttackEvent)event);
 		}
 	}
 	
 	void BeginGameAction(BeginGameEvent event)
 	{
 		Log.i(TAG, "Game begins");
 		tell(event);
 	}
 	
 	void BeginTurnAction(BeginTurnEvent event)
 	{
 		this.setTour(this.getTour() + 1);
 		Log.i(TAG, "Turn " + this.getTour() + " begins");
 		tell(event);
 	}
 	
 	void DrawCardAction(DrawCardEvent event)
 	{
 		Log.i(TAG, "Draw card");
 		DrawCardEvent dce = (DrawCardEvent)event;
 		Card c = CardStoreBidon.getCard(dce.getCard());
 		playerTakeCardInStack();
 		addPlayerHandCard(c);
 		tell(event);
 	}
 	
 	void PutCardAction(PutCardEvent event)
 	{
 		Log.i(TAG,"Put card");
 		Card c = CardStoreBidon.getCard(event.getCard());
 		deletePlayerHandCard(c);
 		addPlayerBoardCard(c);
 		tell(event);
 	}
 	
 	void BattleAction(AttackEvent event)
 	{
 		Card c = CardStoreBidon.getCard(event.getOpponent());
 		List<Card> list =  getOpponentBoardCards();
 		for(int i=0;i<list.size();i++)
 		{
 			if(list.get(i).getUid() == c.getUid())
 			{
 				list.set(i,c);
 				break;
 			}
 		}
 		tell(event);
 	}
 	
 	void EndTurnAction(EndTurnEvent event)
 	{
 		Log.i(TAG, "Turn " + this.getTour() + " ends");
 		tell(event);
 	}
 	
 	void EndGameAction(EndGameEvent event)
 	{
 		Log.i(TAG, "Game ends");
 		tell(event);
 	}
 }

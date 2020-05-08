 package com.sshipsey.holdem.models;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Random;
 
 import com.sshipsey.holdem.Card;
 
 public class Game extends Observable {
 	
 	private ArrayList<Player> m_players = new ArrayList<Player>();
 	private ArrayList<Card> m_tableCards = new ArrayList<Card>();
 	private int m_pot;
 	private int m_bet;
 	private int m_turn;
 	private int m_smallBlind;
 	private int m_dealerButton;
 	private int m_leftToAct;
 	
 	public Game(ArrayList<Player> players, int smallBlind) {
 		m_players = players;
 		m_smallBlind = smallBlind;
 		initDealerButton();
 		resetAll();
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return m_players;
 	}
 	
 	public int getNumPlayers() {
 		return m_players.size();
 	}
 	
 	public Player getPlayer(int index) {
 		return m_players.get(index);
 	}
 	
 	public void resetPlayers() {
 		for(Player p : m_players) p.reset();
 	}
 	
 	public void addTableCard(Card card) {
 		m_tableCards.add(card);
         setChanged();
 	}
 	
 	public ArrayList<Card> getTableCards() {
 		return m_tableCards;
 	}
	public String displayTableCards() {
	    String retVal = "";
	    for (int i = 0; i < m_tableCards.size(); ++i) {
	        retVal += (m_tableCards.get(i) + " ");
	    }
	    return retVal.trim();
	    
	}
 	public void resetTableCards() {
 		m_tableCards.clear();
         setChanged();
 	}
 
 	public int getPot() {
 		return m_pot;
 	}
 	
 	public void addPot(int num) {
 		m_pot += num;
         setChanged();
 	}
 	
 	public void setPot(int num) {
 		m_pot = num;
 	}
 
 	public int getBet() {
 		return m_bet;
 	}
 	
 	public void setBet(int num) {
 		m_bet = num;
         setChanged();
 	}
 	
 	public void resetBet() {
 		m_bet = 0;
 	}
 	
 	public int getTurn() {
 		return m_turn;
 	}
 	
 	public void nextTurn() {
 		m_turn = (m_turn + 1) % m_players.size();
 	}
 	
 	public void resetTurn() {
 		m_turn = (m_dealerButton + 3) % m_players.size();
 	}
 	
 	public Player getCurrentPlayer() {
 		return m_players.get(m_turn);
 	}
 	
 	public int getSmallBlind() {
 		return m_smallBlind;
 	}
 	
 	public void setSmallBlind(int num) {
 		m_smallBlind = num;
         setChanged();
 	}
 	
 	public int getDealerButton() {
 		return m_dealerButton;
 	}
 	
 	public void moveDealerButton() {
 		m_dealerButton = (m_dealerButton + 1) % m_players.size();
 	}
 	
 	public void setDealerButton(int index) {
 		m_dealerButton = index;
 	}
 	
    public boolean getNewDeal() {
        boolean retVal = false;
        if(m_leftToAct == 0)
            retVal = true;
        return retVal;
    }	
    
    public void playerAct() {
        m_leftToAct--;
    }
    
    public void resetLeftToAct() {
       m_leftToAct = m_players.size() - 1;
    }
    
    public void resetAll() {
         resetPlayers();
         resetTableCards();
         resetBet();
         resetTurn();
         resetLeftToAct();
     }
 
 	private void initDealerButton() {
 		Random rnd = new Random();
 		m_dealerButton = rnd.nextInt(m_players.size());
 	}
 }

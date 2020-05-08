 package client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import message.data.ClientResponse;
 import message.data.ClientTurn;
 import poker.arturka.Card;
 
 /**
  * The model of the game, holds all necessary data for
  * the execution of the client side game and notifies {@link ClientController}
  * about any changes in the models state.
  * @author Aleksey
  *
  */
 
 public class ClientModel extends Observable {
 
 	/**
 	 * Conn: the connection to the Server
 	 * fieldcards[]: array of the cards on the table
 	 * state: state of the game
 	 * players: all players participating the game
 	 * id: the id of this client in the game
 	 */
 	private final Conn conn;
 	private Card fieldcards[];
 	private State state;
 	private List<ClientSidePlayer> players;	
 	private int id;
 	
 	/**
 	 * Constructs a model from the open connection
 	 * @param conn
 	 *  connection to the server
 	 */
 	
 	public ClientModel(Conn conn) {
 		this.conn = conn;
 		this.fieldcards = new Card[]{null, null, null, null, null};
 		this.state = State.READY;
 		this.players = new ArrayList<ClientSidePlayer>();
 	}
 	
 	/**
 	 * Sets the players of the game and notifies any Observers

 	 *  A list of players participating in the game
 	 */
 	
 	public int getDealer() {
 		for(ClientSidePlayer i: players) {
 			if(i.isDealer()) return i.getId();
 		}
 		
 		return -1;
 	}
 	
 	public void setPlayerList(List<ClientSidePlayer> players) {
 		this.players = players;
 		
 		setChanged();
         notifyObservers(this.players);
 	}
 
     public ArrayList<ClientSidePlayer> getPlayerList() {
         return (ArrayList<ClientSidePlayer>) players;
     }
 	
 	/**
 	 * Get's the player from the player list by it's id
 	 * @param id 
 	 * 	id of the player to search
 	 * @return
 	 *  Player with specified id or null if id is not found
 	 */
 	
 	public ClientSidePlayer getPlayer(int id) {
         for(ClientSidePlayer i: players) {
             if(i.getId() == id) {
                 return i;
             }
         }
 
         return null;
     }
 	
 	/**
 	 * Get's the id of this client
 	 * @return id
 	 */
 	
 	public int getID() {
 		return id;
 	}
 	
 	/**
 	 * Set's the id of this client and notfies any observers
 	 * @param id
 	 *  id to be set
 	 */
 	
 	public void setID(int id) {
 		this.id = id;
 		
 		setChanged();
         notifyObservers(this.id);
 	}
 	/**
 	 * Set's the hand held cards of the player by his id
 	 * @param id
 	 * 	The id of the player
 	 * @param card1
 	 * 	First card
 	 * @param card2
 	 * 	Second card
 	 */
 	
 	
 	public void setCards(int id, Card card1, Card card2) {
 		for(ClientSidePlayer i: players) {
 			if(i.getId() == id) {
 				i.giveCards(card1, card2);
 			}
 		}
 	}
 	
 	/**
 	 * Gets the card of the player by his id
 	 * @param idt
 	 *  the id of the player
 	 * @return Card array consisting of 2 Card elements or null if the
 	 * id was not found
 	 */
 	
 	public Card[] getCards(int idt) {
 		for(ClientSidePlayer i: players) {
 			if(i.getId() == idt) {
 				return i.getHand();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Get's the hand held cards of this client
 	 * @return card array or null on error
 	 */
 	
 	public Card[] getMyCards() {
 		return this.getCards(this.id);
 	}
 	
 	/**
 	 * Set's the hand held cards of this client player and notifies
 	 * any observers
 	 * @param card1
 	 * 	The first card
 	 * @param card2
 	 * 	The second card
 	 */
 	
 	public void setMyHand(Card card1, Card card2) {
 		this.setCards(id, card1, card2);
 	}
 	
 	
 	
 	/**
 	 * Get's the cards on the table
 	 * @return card array of the table
 	 */
 	
 	public Card[] getFieldCards() {
 		return fieldcards.clone();
 	}
 	
 	/**
 	 * Changes the field cards and notifies observers
 	 * @param newField
 	 *  An array of cards corresponding to the new field
 	 */
 	
 	public void changeFieldCards(Card newField[]) {
 		assert(newField.length == 5);
 		this.fieldcards = newField;
 		
 		setChanged();
         notifyObservers(fieldcards);
 	}
 	
 	/**
 	 * Gets the state of the client
 	 * @return State enum
 	 */
 	
 	public State getState() {
 		return this.state;
 	}
 	
 	/**
 	 * Set's the state of this client
 	 * @param newState
 	 *  The new state
 	 */
 	
 	public void changeState(State newState) {
 		this.state = newState;
 		setChanged();
         notifyObservers(state);
         
 	}
 	
 	/**
 	 * Sends server the input received from the user, 
 	 * @param turn
 	 *  The desired turn of the user
 	 * @param cash
 	 *  (Optional) the cash betted by the user
 	 */
 	
 	private void pressedButton(ClientTurn turn, int cash) {
 		if(State.INPUTCALL == this.getState() ||
 			State.INPUTCHECK == this.getState()) 
 		{
 			conn.sendResponse(new ClientResponse(turn, cash));
 			this.changeState(State.PLAYING);
 		}
 	}
 
 	/**
 	 * Sends server a message that users turn is 'Call'
 	 * @param cash
 	 *  cash associated with call
 	 */
 	
 	public void pressedCall(int cash) {
 		this.pressedButton(ClientTurn.CALL, cash);
 	}
 	
 	/**
 	 * Sends server a message that users turn is 'Raise'
 	 * @param cash
 	 *  cash to raise
 	 */
 	
 	public void pressedRaise(int cash) {
 		this.pressedButton(ClientTurn.RAISE, cash);
 	}
 	/**
 	 * Sends server a message that user has folded
 	 */
 	
 	public void pressedFold() {
 		this.pressedButton(ClientTurn.FOLD, -1);
 	}
 	
 	/**
 	 * Sends server a message that user has checked
 	 */
 	
 	public void pressedCheck() {
 		this.pressedButton(ClientTurn.CHECK, -1);
 
 	}
 	
 	
 	/**
 	 * Get's maximum bet across the player list
 	 * @return the maximum bet
 	 */
 	public int getMaxBet() {
 		int temp = 0;
 		int maxBet = 0;
 		
 		for(ClientSidePlayer i: players) {
 			temp = i.getBet();
 			if(temp > maxBet) maxBet = temp;
 		}
 		
 		return temp;
 	}
 	
 	/**
 	 * Return the bet of a concrete player
 	 * @param id of the player
 	 * @return current bet
 	 */
 	
 	public int getPlayerBet(int id) {
 		return this.getPlayer(id).getBet();
 	}
 	
 	public int getCash(int id) {
 		return this.getPlayer(id).getCash();
 	}
 }

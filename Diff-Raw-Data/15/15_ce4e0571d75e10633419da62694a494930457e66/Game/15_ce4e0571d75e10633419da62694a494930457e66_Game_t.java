 import java.util.ArrayList;
 
 
 public class Game {
 
 	public Card[] AllCards = {new Card()};
 	public ArrayList<Player> players;
 	public ArrayList<Card> bank;
 	public int turn = 0;
 	public int playerNum;
 	
 	public Game(){
 		this.players = new ArrayList<Player>();
 		this.bank = new ArrayList<Card>();
		makeBank();		
 	}
 	
 	public void makePlayers(int number){
 		this.playerNum = number;
 		for (int i = 0; i < number; i++){
 			this.players.add(new Player());
 			this.players.get(i).setup();
 		}
 	}
 	
 	public void makeBank(){
 		for (int i = 0; i < 10; i++){
 			if (i < this.AllCards.length){
 				this.bank.add(this.AllCards[i]);
 			} else {
 				break;
 			}
 		}
 		
 	}
 	
 	public void newTurn(){
 		if (this.turn == (this.playerNum - 1)){
 			this.turn = 0;
 		} else {
 			this.turn++;
 		}
 	}
 	
 	public void playerUseCard(int card){
 		Player curr = this.players.get(this.turn);
 		curr.hand.get(card).use(curr);
 	}
 	
 	
 	
 }

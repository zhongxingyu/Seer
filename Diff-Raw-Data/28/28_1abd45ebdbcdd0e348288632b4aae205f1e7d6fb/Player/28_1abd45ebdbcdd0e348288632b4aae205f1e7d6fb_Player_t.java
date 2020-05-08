 package matador.business_logic;
 
 import java.util.*;
 
 import matador.board.*;
 
 /**
  * Provides a Player that is used in a dice game. The Player holds a name, carColor and a Konto.
  * Provides necessary functions used by the Game class.
  * @author Martin Caspersen
  * @see Game
  * @see Konto
  *
  */
 public class Player {
 	private int carColor;
 	private String name;
 	private Konto konto;
	private List<Ownable> ownedFields = new ArrayList(); 	//Array af fields den enkelte spiller ejer.
 
 	/**
 	 * Constructs a Player with the provided name, carColor and balance.
 	 * @param name name of the Player as a String.
 	 * @param carColor int describing the color of this Players car [0:5].
 	 * @param balance initial balance of the Players Konto as an int.
 	 */
 	public Player(String name, int carColor, int balance) {
 		this.name = name;
 		this.carColor = carColor;
 		konto = new Konto(balance);
 	}
 	
 	/**
 	 * Returns the name of the Player.
 	 * @return Players name as a String.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the carColor of the Player.
 	 * @return carColor as an int.
 	 */
 	public int getCarColor() {
 		return carColor;
 	}
 
 	/**
 	 * Returns the players Konto.
 	 * @return Konto of the player.
 	 * @see Konto
 	 */
 	public Konto getKonto() {
 		return konto;
 	}
 	
 	/**
 	 * Handles processes of buying a Field.
 	 * Withdraws the price of the field from the players account.
 	 * Adds bought field to arrayList of players owned fields.
 	 * 
 	 * @param field
 	 */
	public void buyField(Ownable field){
 		this.konto.withdraw(field.getPrice());	
 		this.ownedFields.add(field);			
 	}
 	
 	@Override
 	public String toString() {
 		return "Player [carColor=" + carColor + ", name=" + name + ", konto="
 				+ konto + "]";
	}

 }

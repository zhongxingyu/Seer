 package battleship;
 
 import java.util.ArrayList;
 
 import entities.Aircraft;
 import entities.Battleship;
 import entities.Cruiser;
 import entities.Destroyer;
 import entities.Ship;
 import entities.Submarine;
 
 public class Player {
 	private ArrayList<Ship> shipList = new ArrayList<Ship>();
 	private String name;
 	
 	public Player(String name) {
 		this.name = name;
 		try{
 			shipList.add(new Aircraft(1,1,Ship.HORIZONTAL));
 			shipList.add(new Battleship(1,1,Ship.HORIZONTAL));
 			shipList.add(new Battleship(1,1,Ship.VERTICAL));
 			shipList.add(new Submarine(1,1,Ship.HORIZONTAL));
 			shipList.add(new Cruiser(1,1,Ship.VERTICAL));
 			shipList.add(new Destroyer(1,1,Ship.HORIZONTAL));
 			shipList.add(new Destroyer(1,1,Ship.VERTICAL));			
 		}
 		catch(Exception e)
 		{
			System.err.println("Ship instantiation error: ("+this.name+") : ");
 			e.printStackTrace();
 		}
 	}
 }

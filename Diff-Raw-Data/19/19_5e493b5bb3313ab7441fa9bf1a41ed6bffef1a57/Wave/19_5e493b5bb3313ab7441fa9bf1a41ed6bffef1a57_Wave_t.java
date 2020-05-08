 package fr.umlv.escape.game;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.ship.Ship;
 
 /**
  * Class that represent a wave of {@link Ship}.
  */
 public class Wave {
 	final String name;
 	final ArrayList<Ship> shipList;
 	
 	/**
 	 * Constructor.
 	 * @param name The name of the wave.
 	 */
 	public Wave(String name){
 		Objects.requireNonNull(name);
 		
 		this.name=name;
 		this.shipList=new ArrayList<Ship>();
 	}
 	
 	/**
 	 * Active and launch all {@link Ship} containing in the wave.
 	 */
 	public void startWave(){
		/*Iterator<Ship> iterShip=this.shipList.iterator();
 		while(iterShip.hasNext()){
 			Ship ship=iterShip.next();
 			Game.getTheGame().getFrontApplication().getBattleField().addShip(ship);
 			ship.body.setActive(true);
 			ship.move();
		}*/
 	}
 
 	@Override
 	public String toString(){
 		String res=this.name;
 		Iterator<Ship> iterShip=this.shipList.iterator();
 		while(iterShip.hasNext()){
 			res+=" "+iterShip.next().toString();
 		}
 		return res;
 	}
 }

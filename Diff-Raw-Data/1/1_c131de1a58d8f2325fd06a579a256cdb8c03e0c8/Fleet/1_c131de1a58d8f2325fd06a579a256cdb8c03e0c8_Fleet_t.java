 package battledemo;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class Fleet {
 	
 	private List<Ship> ships = new ArrayList<Ship>();
 	private int nbrOfAttacks = 0;
 	// TODO private Planet planet = null;
 	
 	/*
 	 * TODO: Placeholder for player
 	 * Should not be string
 	 */
 	private String player = null;
 
 	public Fleet(List<Ship> ships) {
 		for (int i = 0; i < ships.size(); i++) {
 			this.ships.add(ships.get(i));
 			nbrOfAttacks = nbrOfAttacks + ships.get(i).getAttacks();
 		}
 	}
 
 	/**
 	 * Merge several fleets to a new fleet
 	 * @param ships
 	 */
 	public Fleet(Fleet[] fleets) {
 		for (int i = 0; i < fleets.length; i++) {
 			for (int j = 0; j < fleets[i].ships.size(); j++) {
 				this.ships.add(fleets[i].ships.get(j));
				nbrOfAttacks = nbrOfAttacks + fleets[i].ships.get(i).getAttacks();
 			}
 		}
 	}
 	
 	/**
 	 * One ship fleet (For new ships)
 	 * @param ship
 	 */
 	public Fleet(Ship ship) {
 		ships.add(ship);
 		nbrOfAttacks = ship.getAttacks();
 	}
 	
 	/*
 	 * TODO: Split fleet
 	 */
 	public Fleet[] splitFleet(/*ARGS*/) {
 		// TODO
 		return null;
 	}
 	
 	public void setPlayer(String player) {
 		// PLACEHOLDER TODO
 		this.player = player;
 	}
 	
 	public int shipCount(ShipType type) {
 		int nbrOfType = 0;
 		for (int i = 0; i < ships.size(); i++) {
 			if (ships.get(i).shipType == type) {
 				nbrOfType++;
 			}
 		}
 		return nbrOfType;
 	}
 	
 	public List<Integer> getDamage(int initiative) {
 		List<Integer> attacks = new ArrayList<Integer>();
 		// TODO if (hasPanet) {
 		// attacks.add(planet.fire());
 		// }
 			for (int i = 0; i < ships.size(); i++) {
 				if (ships.get(i).getInitiative() == initiative) {
 					attacks.add(ships.get(i).fire());
 				}
 			}
 		return attacks;
 	}
 	
 	public boolean hasPlanet() {
 		//TODO Kolla om du har en l�mplig planet som ska vara med i fighten. Dess f�rsvar ska kunna d� utan 
 		// att planeten f�rsvinner om dess fleet vinner fighten!
 		return false;
 	}
 	
 	public void takeDamage (List<Integer> dmgList) {
 		List<Ship> remove = new ArrayList<Ship>();
 		for (int i = 0; i < dmgList.size(); i++) {
 			int target = (int) (Math.random() * ships.size()+(hasPlanet() ? 1 : 0));
 			if (target == ships.size()) {
 				//TODO planet.takeHit(); -> planet = null?
 			} else {
 				if (!ships.get(target).shield(dmgList.get(i))) {
 					remove.add(ships.get(target));
 				}
 			}
 		}
 		ships.removeAll(remove);
 	}
 	
 	public void resetShields() {
 		for (int i = 0; i < ships.size(); i++) {
 			ships.get(i).reset();
 		}
 	}
 }

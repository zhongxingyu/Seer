 package pw;
 import java.util.*;
 import java.io.*;
 
 /**
  * Class representing the current game state.
  * Contains complete information about planets, fleets and forces.
  * Does not contain any information not taken from bots input (turns etc.)
  * Contains methods for parsing input Strings and getting meta-information.
  * Setters and getters omitted for a more dynamic development speed.
  */
 public class PlanetWars {
 	/**
 	 * List of all planets.
 	 */
     public ArrayList<Planet> planets;
 	/**
 	 * List of all planets controlled by the bot.
 	 */
     public ArrayList<Planet> myplanets;
 	/**
 	 * List of all planets controlled by the enemy.
 	 */
     public ArrayList<Planet> enplanets;
 	/**
 	 * List of all fleets.
 	 */
     public ArrayList<Fleet> fleets;
 	/**
 	 * Sum of the strength of all bot's fleets and planets.
 	 */
     public int myForces = 0;
 	/**
 	 * Sum of the strength of all enemy's fleets and planets.
 	 */
     public int enemyForces = 0;
 
 	
 	/**
 	 * Contructor initialises lists and parses the game state.
 	 * @param gameStateString contains the input to be parsed
 	 */
     public PlanetWars(String gameStateString) {
 		planets = new ArrayList<Planet>();
 		myplanets = new ArrayList<Planet>();
 		enplanets = new ArrayList<Planet>();
 		fleets = new ArrayList<Fleet>();
 		ParseGameState(gameStateString);
     }
     
     public double[] getNElement() {
     	double enemyAttFleets = 0;
     	double enemyDefFleets = 0;
     	double averageFleetSize = 0;
     	for (Fleet f : fleets)
     		if (f.owner == 2) {
     			averageFleetSize += f.numShips;
     			Planet dest = planets.get(f.destinationPlanet);
     			if (dest.owner == 2) enemyDefFleets++;
     			else enemyAttFleets++;
     		}
    	double fleetRatio = enemyAttFleets / (enemyAttFleets + enemyDefFleets + 1);
    	averageFleetSize /= enemyAttFleets + enemyDefFleets + 1;
    	averageFleetSize = averageFleetSize > 100 ? 1 : averageFleetSize/100;
    	
    	double[] result = {fleetRatio,1-fleetRatio,averageFleetSize};
     	return result;
     }
     
 	
 	/**
 	 * Calculates which of your planets are the closest to the target.
 	 * @param px the target planet to which distance is measured
 	 * @return list of planets sorted by rising distance to px
 	 */
 	public List<Planet> MyClosestPlanets(Planet px) {
 		List<Planet> myplanets = new ArrayList<Planet>();
 		for (Planet p : planets) if (p.owner == 1) myplanets.add(p);
 		sortByClosest(px, myplanets);
 		return myplanets;
 	}
 	
 	/**
 	 * Calculates which of the given planets are the closest to the target.
 	 * @param px the target planet to which distance is measured
 	 * @param plist the list of planets which are analysed
 	 * @return list of planets sorted by rising distance to px
 	 */	
 	public void sortByClosest(Planet px, List<Planet> plist) {
 		for (Planet p : plist) {
 			p.tmp = Distance(p, px);
 		}
 		Collections.sort(plist,new Comparator<Planet>()
 			{public int compare(Planet p1, Planet p2){return p1.tmp-p2.tmp;}});
     }
     
 	/**
 	 * Calculates which of the given planets are the closest to the target.
 	 * @param px the target planet to which distance is measured
 	 * @param plist the list of planets which are analysed
 	 * @return a single planet that is the closest from among the listed
 	 */	
     public Planet getClosest(Planet px, List<Planet> plist) {
 		Planet closest = null;
 		int dist = Integer.MAX_VALUE;
 		for (Planet p : plist) {
 			p.tmp = Distance(p, px);
 			if (p.tmp < dist) {dist = p.tmp; closest = p;}
 		}
 		return closest;
     }
 
     /** 
      * Returns the distance between two planets, rounded up to the next highest
      * integer. This is the number of discrete time steps it takes to get
      * between the two planets.
      */
     public int Distance(int sourcePlanet, int destinationPlanet) {
 		Planet source = planets.get(sourcePlanet);
 		Planet destination = planets.get(destinationPlanet);
 		return Distance(source, destination);
     }
 
     /** 
      * Returns the distance between two planets, rounded up to the next highest
      * integer. This is the number of discrete time steps it takes to get
      * between the two planets.
      */
     public static int Distance(Planet source, Planet destination) {
 		double dx = source.x - destination.x;
 		double dy = source.y - destination.y;
 		return (int)Math.ceil(Math.sqrt(dx * dx + dy * dy));
     }
 
     /**
      * Sends an order to the game engine. An order is composed of a source
      * planet number, a destination planet number, and a number of ships.
      * Actually calls the other overloaded version of the method.
      * @param sourcePlanet the index number of your planet that sends the ships
      * @param destinationPlanet the index number of the targetted planet
      * @param numShips the number of ships to be sent
      */
     public void IssueOrder(int sourcePlanet,
                            int destinationPlanet,
                            int numShips) {
         IssueOrder(planets.get(sourcePlanet), planets.get(destinationPlanet), numShips);
     }
 
     /**
      * Sends an order to the game engine.
      * Performs full validation of the eligibility of the order to
      * prevent accidental illegal moves due to bug.
      * Checks the following:
      * - you must own the source planet.
      * - you can't move more ships than are currently on the source planet.
      * @param source the Planet that sends the ships
      * @param dest the Planet targetted by the order
      * @param numShips the number of ships to be sent
      */
     public void IssueOrder(Planet source, Planet dest, int numShips) {
    //	MyBot.log("order called: "+source.planetID+" "+dest.planetID+" "+numShips);
 		if (source == dest) return;
 		if (numShips <= 0) return;
 		if (source.numShips >= numShips) {
 			source.numShips -= numShips;
 			source.safety -= numShips;
 		} else return;
 		int distance = Distance(source.planetID,dest.planetID);
 		Fleet f = new Fleet(1,numShips,source.planetID,dest.planetID,
 			distance,distance);
 		fleets.add(f);
 		dest.incoming.add(f);
 		
     	System.out.println("" + source.planetID + " " + dest.planetID +
         	" " + numShips);
 		System.out.flush();
     }
 
     /**
      * Sends the game engine a message to let it know that we're done sending
      * orders. This signifies the end of our turn.
      */
     public void FinishTurn() {
 		System.out.println("go");
 		System.out.flush();
     }
 
     /**
      * Returns true if the named player owns at least one planet or fleet.
      * Otherwise, the player is deemed to be dead and false is returned.
      */
     public boolean IsAlive(int playerID) {
 		for (Planet p : planets) {
 		    if (p.owner == playerID) {
 				return true;
 		    }
 		}
 		for (Fleet f : fleets) {
 		    if (f.owner == playerID) {
 				return true;
 		    }
 		}
 		return false;
     }
 
     /**
      * If the game is not yet over (ie: at least two players have planets or
      * fleets remaining), return -1. If the game is over (ie: only one player
      * is left) then that player's number is returned. If there are no
      * remaining players, then the game is a draw and 0 is returned.
      */
     public int Winner() {
 	Set<Integer> remainingPlayers = new TreeSet<Integer>();
 	for (Planet p : planets) {
 	    remainingPlayers.add(p.owner);
 	}
 	for (Fleet f : fleets) {
 	    remainingPlayers.add(f.owner);
 	}
 	switch (remainingPlayers.size()) {
 	case 0:
 	    return 0;
 	case 1:
 	    return ((Integer)remainingPlayers.toArray()[0]).intValue();
 	default:
 	    return -1;
 	}
     }
 
     /**
      * Parses a game state from a string.
      * @param s the input string containing game state
      * @return On success, return 1. On failure, return 0.
      */
     private int ParseGameState(String s) {
 	planets.clear();
 	fleets.clear();
 	int planetID = 0;
 	String[] lines = s.split("\n");
 	for (int i = 0; i < lines.length; ++i) {
 	    String line = lines[i];
 	    int commentBegin = line.indexOf('#');
 	    if (commentBegin >= 0) {
 		line = line.substring(0, commentBegin);
 	    }
 	    if (line.trim().length() == 0) {
 		continue;
 	    }
 	    String[] tokens = line.split(" ");
 	    if (tokens.length == 0) {
 		continue;
 	    }
 	    if (tokens[0].equals("P")) {
 		if (tokens.length != 6) {
 		    return 0;
 		}
 		double x = Double.parseDouble(tokens[1]);
 		double y = Double.parseDouble(tokens[2]);
 		int owner = Integer.parseInt(tokens[3]);
 		int numShips = Integer.parseInt(tokens[4]);
 		int growthRate = Integer.parseInt(tokens[5]);
 		Planet p = new Planet(planetID++,
 				      owner,
 				      numShips,
 				      growthRate,
 				      x, y);
 		planets.add(p);
 		if (owner == 1) {myForces += numShips; myplanets.add(p);}
 		if (owner >= 2) {enemyForces += numShips; enplanets.add(p);}
 	    } else if (tokens[0].equals("F")) {
 		if (tokens.length != 7) {
 		    return 0;
 		}
 		int owner = Integer.parseInt(tokens[1]);
 		int numShips = Integer.parseInt(tokens[2]);
 		int source = Integer.parseInt(tokens[3]);
 		int destination = Integer.parseInt(tokens[4]);
 		int totalTripLength = Integer.parseInt(tokens[5]);
 		int turnsRemaining = Integer.parseInt(tokens[6]);
 		Fleet f = new Fleet(owner,
 				    numShips,
 				    source,
 				    destination,
 				    totalTripLength,
 				    turnsRemaining);
 		fleets.add(f);
 		planets.get(destination).incoming.add(f);
 		if (owner == 1) myForces += numShips;
 		if (owner >= 2) enemyForces += numShips;
 	    } else {
 		return 0;
 	    }
 	}
 	return 1;
     }
 
     /**
      * Loads a map from a text file. The text file contains a description of
      * the starting state of a game. See the project wiki for a description of
      * the file format. It should be called the Planet Wars Point-in-Time
      * format.
      * @return On success, return 1. On failure, return 0.
      */
     private int LoadMapFromFile(String mapFilename) {
 	String s = "";
 	BufferedReader in = null;
 	try {
 		in = new BufferedReader(new FileReader(mapFilename));
 		int c;
 		while ((c = in.read()) >= 0) {
 		    s += (char)c;
 		}
 	} catch (Exception e) {
 	    return 0;
 	} finally {
 	    try {
 		in.close();
 	    } catch (Exception e) {
 		// Do not care.
 	    }
 	}
 	return ParseGameState(s);
     }
 
 }

 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.*;
 
 /** Another smarter kind of bot, which implements a minimax algorithm with look-ahead of two turns.
  * It simulates the opponent using the BullyBot strategy and simulates the possible outcomes for any
  * choice of source and destination planets in the attack. The simulated outcome states are ranked by
  * the evaluation function, which returns the most promising one.
  * 
  * Try to improve this bot. For example, you can try to answer some of this questions. 
  * Can you come up with smarter heuristics/scores for the evaluation function? 
  * What happens if you run this bot against your bot from week1? 
  * How can you change this bot to beat your week1 bot? 
  * Can you extend the bot to look ahead more than two turns? How many turns do you want to look ahead?
  * Is there a smart way to make this more efficient?
  */
 
 public class LookaheadBot {
 
 	public static void DoTurn(PlanetWars pw) {
 		
 		double score = Double.MIN_VALUE;		
 		Planet source = null;
 		Planet dest = null;
 	
 		
 		// We try to simulate each possible action and its outcome after two turns
 		// considering each of my planets as a possible source 
 		// and each enemy planet as a possible destination
 		for (Planet myPlanet: pw.MyPlanets()){
 			
 			//avoid planets with only one ship
 			if (myPlanet.NumShips() <= 1)
 				continue;		
 			
 			for (Planet notMyPlanet: pw.NotMyPlanets()){
 
 				// Create simulation environment - need to create one for each simulation
 				SimulatedPlanetWars simpw = createSimulation(pw);
 				
 				// (1) simulate my turn with the current couple of source and destination
 				simpw.simulateAttack(myPlanet, notMyPlanet);
 				// (2) simulate the growth of ships that happens in each turn
 				simpw.simulateGrowth();
 
 				// (3) simulate the opponent's turn, assuming that the opponent is the BullyBot		
 				//     here you can add other opponents
 				simpw.simulateBullyBotAttack();
 				// (4) simulate the growth of ships that happens in each turn
 				simpw.simulateGrowth();
 			
 				
 				// (5) evaluate how the current simulated state is
 				//     here you can change how a state is evaluated as good
 				double scoreMax = evaluateState(simpw);
 				
 				// (6) find the planet with the maximum evaluated score
 				//     this is the most promising future state
 				if (scoreMax > score) {					
 					score = scoreMax;
 					source = myPlanet;
 					dest = notMyPlanet;
 					
 				}
 				
 			}
 		}
 		
 		
 			
 		// Attack using the source and destinations that lead to the most promising state in the simulation
 		if (source != null && dest != null) {
 			pw.IssueOrder(source, dest);
 		}
 		
 
 	}
 	
 	
 	/**
 	 * This function evaluates how promising a simulated state is.
 	 * You can change it to anything that makes sense, using combinations 
 	 * of number of planets, ships or growth rate.
 	 * @param SimulatedPlanetWars pw
 	 * @return score of the final state of the simulation
 	 */
 	public static double evaluateState(SimulatedPlanetWars pw){
 		
 		// CHANGE HERE
 		
 		double enemyShips = 1.0;
 		double myShips = 1.0;
 		
 		for (Planet planet: pw.EnemyPlanets()){
 			enemyShips += planet.NumShips();
 		}
 		
 		for (Planet planet: pw.MyPlanets()){
 			myShips += planet.NumShips();
 		}
 		
 		return myShips/enemyShips;
 	}
 	
 
 	
 	// don't change this
 	public static void main(String[] args) {
 		
 		String line = "";
 		String message = "";
 		int c;
 		try {
 			while ((c = System.in.read()) >= 0) {
 				switch (c) {
 				case '\n':
 					if (line.equals("go")) {
 						PlanetWars pw = new PlanetWars(message);
 						DoTurn(pw);
 						pw.FinishTurn();
 						message = "";
 					} else {
 						message += line + "\n";
 					}
 					line = "";
 					break;
 				default:
 					line += (char) c;
 					break;
 				}
 			}
 		} catch (Exception e) {
 			StringWriter writer = new StringWriter();
 			e.printStackTrace(new PrintWriter(writer));
 			String stackTrace = writer.toString();
 			System.err.println(stackTrace);
 			System.exit(1); //just stop now. we've got a problem
 		}
 	}
 	
 	/**
 	 * Create the simulation environment. Returns a SimulatedPlanetWars instance.
 	 * Call every time you want a new simulation environment.
 	 * @param The original PlanetWars object
 	 * @return SimulatedPlanetWars instance on which to simulate your attacks. Create a new one everytime you want to try alternative simulations.
 	 */
 	public static SimulatedPlanetWars createSimulation(PlanetWars pw){
 		return dummyBot.new SimulatedPlanetWars(pw);
 	}
 	
 	
 	/**
 	 * Static LookaheadBot, used only to access SimulatedPlanetWars (DON'T CHANGE)
 	 */
 	static LookaheadBot dummyBot = new LookaheadBot();
 	
 	/**
 	 * Class which provide the simulation environment, has same interface as PlanetWars 
 	 * (except for Fleets, that are not used).
 	 *
 	 */
 	public class SimulatedPlanetWars{
 
 		List<Planet> planets = new ArrayList<Planet>();
 		
 		public SimulatedPlanetWars(PlanetWars pw) {
 
 			for (Planet planet: pw.Planets()){
 				planets.add(planet);
 			}
 		}
 		
 		public void simulateGrowth() {
 			for (Planet p: planets){
 				
 				if(p.Owner() == 0)
 					continue;
 				
 				Planet newp = new Planet(p.PlanetID(), p.Owner(), p.NumShips()+p.GrowthRate() , 
 						p.GrowthRate(), p.X(), p.Y());
 				
 				planets.set(p.PlanetID(), newp);
 			}
 		}
 		
 		public void simulateAttack( int player, Planet source, Planet dest){
 			
 			if (source.Owner() != player){
 				return;
 			}
 			
 			
 			// Simulate attack
 			if (source != null && dest != null) {
 						
 				Planet newSource = new Planet(source.PlanetID(), source.Owner(), source.NumShips()/2 , 
 						source.GrowthRate(), source.X(), source.Y());
 				Planet newDest = new Planet(dest.PlanetID(), dest.Owner(), Math.abs(dest.NumShips()-source.NumShips()/2 ), 
 						dest.GrowthRate(), dest.X(), dest.Y());
 				
 				if(dest.NumShips()< source.NumShips()/2){
 					//change owner
 					newDest.Owner(player);
 				}
 				
 				planets.set(source.PlanetID(), newSource);
 				planets.set(dest.PlanetID(), newDest);
 			}
 
 
 		}
 		
 		public void simulateAttack( Planet source, Planet dest){
 			simulateAttack(1, source, dest);
 		}
 		
 		
 		public void simulateBullyBotAttack(){
 			Planet source = null;
 			Planet dest = null;
 
 			
 			// (1) Apply your strategy
 			double sourceScore = Double.MIN_VALUE;
 			double destScore = Double.MAX_VALUE;
 			
 			for (Planet planet : planets) {
 				if(planet.Owner() == 2) {// skip planets with only one ship
 					if (planet.NumShips() <= 1)
 						continue;
 					
 					//This score is one way of defining how 'good' my planet is. 
					double scoreMin = (double) planet.NumShips();
 					
					if (scoreMin > sourceScore) {
 						//we want to maximize the score, so store the planet with the best score
						sourceScore = scoreMin;
 						source = planet;
 					}
 				}	
 				
 				// (2) Find the weakest enemy or neutral planet.
 				if(planet.Owner() != 2){
					double scoreMax = (double) (planet.NumShips());
 					//if you want to debug how the score is computed, decomment the System.err.instructions
 		//			System.err.println("Planet: " +notMyPlanet.PlanetID()+ " Score: "+ score);
 		//			System.err.flush();
					if (scoreMax < destScore) {
 						//The way the score is defined, is that the weaker a planet is, the higher the score. 
 						//So again, we want to select the planet with the best score
						destScore = scoreMax;
 						dest = planet;
 					}
 				}
 				
 			}
 			
 			// (3) Simulate attack
 			if (source != null && dest != null) {
 				simulateAttack(2, source, dest);
 			}
 
 		}
 		
 		public List<Planet> Planets(){
 			return planets;
 		}
 		
 	    // Returns the number of planets. Planets are numbered starting with 0.
 	    public int NumPlanets() {
 	    	return planets.size();
 	    }
 		
 	    // Returns the planet with the given planet_id. There are NumPlanets()
 	    // planets. They are numbered starting at 0.
 	    public Planet GetPlanet(int planetID) {
 	    	return planets.get(planetID);
 	    }
 	    
 	    // Return a list of all the planets owned by the current player. By
 	    // convention, the current player is always player number 1.
 	    public List<Planet> MyPlanets() {
 			List<Planet> r = new ArrayList<Planet>();
 			for (Planet p : planets) {
 			    if (p.Owner() == 1) {
 				r.add(p);
 			    }
 			}
 			return r;
 	    }
 	    
 	    // Return a list of all neutral planets.
 	    public List<Planet> NeutralPlanets() {
 		List<Planet> r = new ArrayList<Planet>();
 		for (Planet p : planets) {
 		    if (p.Owner() == 0) {
 			r.add(p);
 		    }
 		}
 		return r;
 	    }
 
 	    // Return a list of all the planets owned by rival players. This excludes
 	    // planets owned by the current player, as well as neutral planets.
 	    public List<Planet> EnemyPlanets() {
 		List<Planet> r = new ArrayList<Planet>();
 		for (Planet p : planets) {
 		    if (p.Owner() >= 2) {
 			r.add(p);
 		    }
 		}
 		return r;
 	    }
 
 	    // Return a list of all the planets that are not owned by the current
 	    // player. This includes all enemy planets and neutral planets.
 	    public List<Planet> NotMyPlanets() {
 		List<Planet> r = new ArrayList<Planet>();
 		for (Planet p : planets) {
 		    if (p.Owner() != 1) {
 			r.add(p);
 		    }
 		}
 		return r;
 	    }
 	    
 	    // Returns the distance between two planets, rounded up to the next highest
 	    // integer. This is the number of discrete time steps it takes to get
 	    // between the two planets.
 		public int Distance(int sourcePlanet, int destinationPlanet) {
 			Planet source = planets.get(sourcePlanet);
 			Planet destination = planets.get(destinationPlanet);
 			double dx = source.X() - destination.X();
 			double dy = source.Y() - destination.Y();
 			return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
 		}
 	    
 	    // If the game is not yet over (ie: at least two players have planets or
 	    // fleets remaining), returns -1. If the game is over (ie: only one player
 	    // is left) then that player's number is returned. If there are no
 	    // remaining players, then the game is a draw and 0 is returned.
 		public int Winner() {
 			Set<Integer> remainingPlayers = new TreeSet<Integer>();
 			for (Planet p : planets) {
 				remainingPlayers.add(p.Owner());
 			}
 			switch (remainingPlayers.size()) {
 			case 0:
 				return 0;
 			case 1:
 				return ((Integer) remainingPlayers.toArray()[0]).intValue();
 			default:
 				return -1;
 			}
 		}
 
 	    // Returns the number of ships that the current player has, either located
 	    // on planets or in flight.
 	    public int NumShips(int playerID) {
 		int numShips = 0;
 		for (Planet p : planets) {
 		    if (p.Owner() == playerID) {
 			numShips += p.NumShips();
 		    }
 		}
 		return numShips;
 	    }
 
 	    
 
 	    public void IssueOrder(Planet source, Planet dest) {
 	    	simulateAttack(source,dest);
 	    }
 	    
 	
 	}
 }

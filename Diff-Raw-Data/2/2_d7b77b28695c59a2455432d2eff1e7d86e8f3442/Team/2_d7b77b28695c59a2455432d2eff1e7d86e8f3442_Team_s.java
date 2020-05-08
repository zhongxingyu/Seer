 package hoop.g1;
 
 import hoop.sim.Game.Round;
 import hoop.sim.Hoop;
 
 import java.util.Arrays;
 //here
 //Test Jiang
 import java.util.Random;
 
 public class Team implements hoop.sim.Team, Logger {
 	
 	private static int versions;
 	
 	private static final Random gen = new Random();
 	private static final boolean DEBUG = true;
 	private static final int TEAM_SIZE = 5;
 	
 	public enum Status {
 		DEFENDING,
 		START,
 		PASSING,
 		SHOOTING
 	}
 	
 	public void log(String message) {
 		if(DEBUG) {
 			System.err.println(name() + ": "  + message);			
 		}
 	}
 
 	public String name()
 	{
 		return "G1-" + version;
 	}
 
 	private final int version = ++versions;
 	private int holder = 0;
 	private TeamPicker picker;
 
 	private Game game;
 	
 	private int currentPerspective;
 	private Game[] games = new Game[2];
 
 	private Game attackingGame;
 
 	private static boolean in(int[] a, int n, int x)
 	{
 		for (int i = 0 ; i != n ; ++i)
 			if (a[i] == x) return true;
 		return false;
 	}
 
 	private static void shuffle(int[] a, Random gen)
 	{
 		for (int i = 0 ; i != a.length ; ++i) {
 			int r = gen.nextInt(a.length - i) + i;
 			int t = a[i];
 			a[i] = a[r];
 			a[r] = t;
 		}
 	}
 
 	@Override
 	public void opponentTeam(int[] opponentPlayers) {
 		log("Called opponentTeam()");
 		// We're told what players were picked to play us. 123
 		// Keep track of these players for the game Jose & Jiang & Albert
 	}
 
 	@Override
 	public int[] pickTeam(String opponent, int totalPlayers, hoop.sim.Game[] history) {
 		log("Called pickTeam() with opponent: " + opponent + " with tP: " + totalPlayers);
 		// Here we find out how many players we have for the game.
 		
 		
 		game = new Game();
 		attackingGame = game;
 		if(name().equals(opponent)) {
 			// We're playing a self game.
 			log("Setting game to self game.");
 			game.selfGame = true;
 			games[currentPerspective] = game;
 			currentPerspective = (currentPerspective + 1) % 2;
 		}
 		
 		// First initialize the scores.
 		game.ourScore = -1;
 		game.theirScore = 0;
 		
 		if(picker == null) {
 			picker = new PivotTeamPicker();
 			picker.initialize(totalPlayers, Hoop.selfGames(), Hoop.gameTurns());
 			picker.setLogger(this);
 		}
 		
 		return picker.pickTeam();
 	}
 
 	@Override
 	public int pickAttack(int yourScore, int opponentScore, Round previousRound) {
 		picker.reportLastRound(previousRound);
 		
 		log("Called pickAttack()");
 		log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
 		if(game.selfGame) {
 			game = games[currentPerspective];
 			attackingGame = game;
 			currentPerspective = (currentPerspective + 1) % 2;
 		}
 		
 		if(game.ourScore == -1) {
 			// This is the first turn.
 			game.ourScore = 0;
 		} else if (game.theirScore == opponentScore) {
 			// They failed to make their shot.
 			// Or their pass was blocked.
 		} else {
 			game.theirScore = opponentScore;
 			// They made their shot.
 		}
 		
 		if(game.selfGame) {
 			holder =  picker.getBallHolder();
 		} else {
 			holder = gen.nextInt(5) + 1;
 		}
 		
 		// Set status to holding until action.
 		game.lastMove = new Move(holder, 0, Status.START);
 		
 		return holder;
 	}
 
 	/**
 	 * Return
 	 *  0 - For shoot
 	 *  # - Of player to pass to
 	 */
 	@Override
 	public int action(int[] defenders) {
 		
 		if(game.selfGame) {
 			Move m = picker.action(defenders, attackingGame.lastMove);
 			attackingGame.lastMove = m;
 			return (m.action == Status.SHOOTING) ? 0 : m.toPlayer;
 		}
 		
 		switch(attackingGame.lastMove.action) {
 			case START:  
 				// then we pass
 				
 				// Then we should be shooting
 				
 				// whether or not to pass again ?
 				// decide
 				// Also who to pass to??
 				
 				// last move is holder is passing to the new holder.
 				
 				int oldHolder = holder;
 				
 				int newHolder = holder;
 				while (newHolder == holder)
 					newHolder = gen.nextInt(5) + 1;
 				holder = newHolder;
 				
 				attackingGame.lastMove = new Move(oldHolder, newHolder, Status.PASSING);
 				return holder;
 				
 				
 			case PASSING:
 				// We want to logs the success of a pass
 				// from player x on defending player y
 				
 				// then we shoot.
 				
 				//lastMove = shooting move.
 				
 				attackingGame.lastMove = new Move(holder, 0, Status.SHOOTING);
 				return 0;// return 0 cause we're shooting.
 				
 			case DEFENDING:
 			case SHOOTING:
 				// This should never happen.
 				throw new IllegalArgumentException("Illegal status on action: " + attackingGame.lastMove.action);
 		}
 		
 		return 0;
 	}
 
 	// Pick defend.
 	@Override
 	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
 		log("Called pickDefend()");
 		log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
 		if(game.selfGame) {
 			log("Current perspective: " + currentPerspective);
 			game = games[currentPerspective];
 		}
 		
 		if(game.ourScore == -1) {
 			// This is the first turn.
 			game.ourScore = 0;
 		} else if (game.ourScore == yourScore) {
 			// so either pass is blocked
 			// or shot failed.
 			
 			switch(game.lastMove.action) {
 				case PASSING:
 					// pass was blocked.
 					break;
 				case SHOOTING:
 					// shot was blocked.
 					// shot as blocked by:
 					// lastMove.theirPlayer;
 					// log that their player has the ability
 					// to block our shooter.
 					break;
 				default:
 					throw new IllegalArgumentException("Illegal status for defend:" + game.lastMove.action);
 			}
 			
 		} else {
 			// supposedly can only go up.
 			// Maybe we made a shot?
 			game.ourScore = yourScore;
 		}
 		
 		// We're on defense so our last move is not defending.
 		game.lastMove = new Move(0, 0, Status.DEFENDING);
 		
 		if(game.selfGame) {
 			return picker.getDefenseMatch();
 		}
 		
 		int[] defenders = new int [] {1,2,3,4,5};
 		shuffle(defenders, gen);
 		return defenders;
 	}
 	
 	public interface TeamPicker {
 		//intitalize the total # of players, # of games, and # of turns per game
 		void initialize(int players, int games, int turns);
 
 		//Returns the array of teams
 		int[] pickTeam();
 
 		//Returns the Move object based on the defenders & lastMove
 		Move action(int[] defenders, Move lastMove);
 
 		//Reports previous Rounds
 		void reportLastRound(Round previousRound);
 		
 		//Returns the array of defense matchup
 		int[] getDefenseMatch();
 
 		//Returns who will be the starting player of the game
 		int getBallHolder();
 		
 		//logger
 		void setLogger(Logger logger);
 	}
 	
 	public static class PivotTeamPicker implements TeamPicker {
 		
 		private int totalPlayers;
 		private int games;
 		private int turns;
 		private int firstPivot;
 		private int secondPivot;
 		
 		private int[] shotsMade;
 		private int[] shotsTaken;
 		
 		private int[] teamA = new int[TEAM_SIZE];
 		private int[] teamB = new int[TEAM_SIZE];
 		
 		private int shooter = -1;
 		private int changeShooter = 0;
 		
 		private int pickingTeam; // Changes every game twice.
 		private int currentPlayer; //
 		
 		private int pickingDefense; // Changes every turn.
 		
 		private Logger logger = DEFAULT_LOGGER;
 		
 
 		@Override
 		public void initialize(int players, int games, int turns) {
 			this.totalPlayers = players;
 			this.games = games;
 			this.turns = turns;
 
 			shotsMade = new int[players];
 			shotsTaken = new int[players];
 			
 			firstPivot = gen.nextInt(players) + 1;
 			secondPivot = firstPivot;
 			
 			while(secondPivot == firstPivot) {
 				secondPivot = gen.nextInt(players) + 1;
 			}
 			
 			logger.log("First pivot is: " + firstPivot);
 			logger.log("Second pivot is: " + secondPivot);
 			currentPlayer = 1;
 			while(currentPlayer == firstPivot || currentPlayer == secondPivot) {
 				currentPlayer++;
 			}
 		}
 
 		@Override
 		public int[] pickTeam() {
 			
 			int curPos = currentPlayer;
 			
 			int[] team = null;
 			if(pickingTeam++ % 2 == 0) { //this 
 				team = teamA;
 				team[0] = firstPivot;
 			} else {
 				team = teamB;
 				team[0] = secondPivot;
 			}
 			
 			for(int i = 1; i < TEAM_SIZE;) {
 				if(curPos != firstPivot && curPos != secondPivot) {
 					team[i] = curPos;
 					i++;
 				}
 				
 				curPos = ++curPos % totalPlayers;
 				if(curPos == 0) {
 					curPos = totalPlayers;
 				}
 			}
 			
 			currentPlayer = curPos;
 			
 			return team;
 		}
 
 		@Override
 		public int getBallHolder() {
 			if(changeShooter == 0) {
 				shooter = ++shooter % TEAM_SIZE;
 			}
 			changeShooter = ++changeShooter % 2;
 			
 			int[] players = null;
 			if(pickingDefense == 0) {
 				players = teamA;
 			} else {
 				players = teamB;
 			}
 			pickingDefense = ++pickingDefense % 2;
 			
			int ballHolder = shooter + 1;
 			logger.log(whatTeam("attack") + ": Picker: ballHolder: [playerID]: "+ players[ballHolder-1] + " | [sim#]: " + ballHolder);
 			return ballHolder;
 		}
 		
 		@Override
 		public Move action(int[] defenders, Move lastMove) {
 			Move move = null;
 			
 			int[] players = null;
 			if(pickingDefense == 0) {
 				players = teamB;
 			} else {
 				players = teamA;
 			}
 			
 			switch(lastMove.action) {
 				case START:
 					// do the pass.
 					int nextHolder = shooter + 1;
 					logger.log(whatTeam("attack") + ": Passing to --> playerID " + players[shooter] + " ( [Sim#]: " + nextHolder + ") ");
 					move = new Move(lastMove.ourPlayer, nextHolder, Status.PASSING);
 					break;
 				case PASSING:
 					// Shoot
 					logger.log("Shooting..." + " from " + whatTeam("attack"));
 					move = new Move(lastMove.ourPlayer, 0, Status.SHOOTING);
 					break;
 				default:
 					throw new IllegalArgumentException("Invalid status: " + lastMove.action);
 				
 			}
 			
 			return move;
 		}
 
 		@Override
 		public void reportLastRound(Round previousRound) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public int[] getDefenseMatch() {
 			
 			int[] match = new int[TEAM_SIZE];
 			
 			match[0] = shooter + 1;
 			for(int i = 1; i < TEAM_SIZE; i++) {
 				match[i] = ((shooter + i) % TEAM_SIZE) + 1;
 			}
 
 			logger.log("Picker: DefMatch: " + Arrays.toString(match) + " of " + whatTeam("defend"));
 			return match;
 		}
 		
 		public void setLogger(Logger logger) {
 			this.logger = logger;
 		}
 
 		public String whatTeam(String attackOrDefend){
 			//depending on the state of the game, returns the attacking team 
 
 				if(attackOrDefend.equals("attack"))
 					return ( changeShooter == 0 )? "Team A" : "Team B" ;
 				else
 					return ( changeShooter == 0 )? "Team B" : "Team A" ;
 
 		}
 
 
 	}
 
 
 	public static final Logger DEFAULT_LOGGER = new Logger() {
 		@Override
 		public void log(String message) {
 			System.out.println(message);
 		}
 	};
 	
 	
 }

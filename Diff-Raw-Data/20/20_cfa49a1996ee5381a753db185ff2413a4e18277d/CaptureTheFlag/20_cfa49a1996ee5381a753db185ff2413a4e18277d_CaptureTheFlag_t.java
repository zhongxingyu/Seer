 package game;
 
 import item.Flag;
 
 import java.util.*;
 
 import exception.InvalidFileException;
 import exception.InvalidNbPlayersException;
 import grid.GridBuilder;
 
 /**
  * This class represents the capture the flag mode of the game.
  * 
  * @invar	The minimum amount of players is 2.
  * 			| getNbPlayers() >= 2
  * @invar	The maximum amount of players is 9.
  * 			| getNbPlayers() <= 9
  * 
  * @author 	Groep 8
  * 
  * @version	May 2013
  */
 public class CaptureTheFlag extends Mode {
 	
 	/**
 	 * The minimum amount of players.
 	 */
 	public static final int MIN_NB_PLAYERS = 2;
 	/**
 	 * The maximum amount of players.
 	 */
 	public static final int MAX_NB_PLAYERS = 9;
 	
 	/**
 	 * The map of captured flags for each player.
 	 */
 	private Map<Player,Set<Flag>> capturedFlags = new HashMap<Player,Set<Flag>>();
 	
 	/**
 	 * Constructor for the game mode
 	 * 
 	 * @param 	nbPlayers
 	 * 			The number of players for the game.
	 * @throws 	InvalidNbPlayersException 
	 * 			When a wrong number of players is given.
 	 * @post	The number of players are set.
 	 * 			| new.getNbPlayers() = nbPlayers
 	 * 
 	 * @throws	IllegalArgumentException
 	 * 			When the amount of players is not valid.
 	 */
	public CaptureTheFlag(int nbPlayers) throws InvalidNbPlayersException {
 		if(nbPlayers >= MIN_NB_PLAYERS && nbPlayers <= MAX_NB_PLAYERS) {
 			setNbPlayers(nbPlayers);
		} else throw new InvalidNbPlayersException();
 	}
 	
 	/**
 	 * Initialize the grid for the capture the flag game.
 	 * 
 	 * @param 	gridBuilder
 	 * 			The builder that is used to create the grid.
 	 * 
 	 * @effect	The standard grid is initialized.
 	 * 			| super.initializeGrid(gridBuilder, game)
 	 * @effect	The flags are placed.
 	 * 			| gridBuilder.placeFlags(getNbPlayers())
 	 * 
 	 * @throws 	InvalidFileException 
 	 * @throws 	InvalidNbPlayersException 
 	 */
 	@Override
 	public void initializeGrid(GridBuilder gridBuilder, Game game) throws InvalidFileException, InvalidNbPlayersException {
 		super.initializeGrid(gridBuilder, game);
 		gridBuilder.placeFlags();
 	}
 
 	/**
 	 * This method checks whether a player wins the game.
 	 * 
 	 * @param	player
 	 * 			The player who could win the game.
 	 * 
 	 * @return	True when the player captures all the flags.
 	 * 			| return capturedFlags.get(player).size() >= getNbPlayers()-1
 	 */
 	@Override
 	public boolean checkWin(Player player) {
 		if(capturedFlags.get(player) != null) {
 			int nbCapturedFlags = capturedFlags.get(player).size();
 			return nbCapturedFlags >= getNbPlayers() - 1;
 		}
 		return false;
 	}
 
 	/**
 	 * Captures the given flag for the given player.
 	 * 
 	 * @param 	player
 	 * 			The player capturing the given flag.
 	 * @param 	flag
 	 * 			The flag to capture.
 	 * 
 	 * @effect	The flag is captured and added to the list of captured flags.
 	 * 			|capturedFlags.get(player).add(flag)
 	 */
 	public void capture(Player player, Flag flag) {
 		if(!capturedFlags.containsKey(player)) {
 			capturedFlags.put(player, new HashSet<Flag>());
 		}
 		capturedFlags.get(player).add(flag);	
 	}
 
 }

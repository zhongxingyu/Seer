 package theresistance.core;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import theresistance.core.util.ExtraInfoBag;
 
 /**
  * A game of The Resistance.
  */
 public class Game
 {
 	private String id;
 	private final GameConfig config;
 	private final List<Round> rounds = new LinkedList<>();
 	private List<Player> players = new LinkedList<>();
 	private Map<String, Player> playersByName = new TreeMap<>();
 
 	private final ExtraInfoBag extraInfo = new ExtraInfoBag();
 
 	private boolean isStarted = false;
 	private int curRound = 0;
 	private final int curLeader = 0;
 	private Alignment winners = Alignment.NEITHER;
 
 	public Game(GameConfig config)
 	{
 		this.config = config;
 	}
 
 	/**
 	 * set id
 	 * 
 	 * @param id
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * @return id
 	 */
 	public String getId()
 	{
 		return id;
 	}
 
 	/**
 	 * @return config
 	 */
 	public GameConfig getConfig()
 	{
 		return config;
 	}
 
 	/**
 	 * Adds a new player to the game
 	 * 
 	 * @param player
 	 */
 	public void addPlayer(Player player)
 	{
 		if (playersByName.put(player.getName(), player) == null)
 		{
 			players.add(player);
 		}
 	}
 	
 	/**
 	 * Removes a player from the game.
 	 * 
 	 * @param player
 	 */
 	public void removePlayer(Player player)
 	{
		if (playersByName.remove(player.getName()) != null)
 		{
 			players.remove(player);
 		}
 	}
 
 	/**
 	 * initializes the game and prepares for play
 	 */
 	public void start()
 	{
 		new RoleAssigner().assign(players, config.getRoles());
 		
 		for (Player p : players) 
 		{
 			playersByName.put(p.getName(), p);
 		}
 
 		for (Mission mission : config.getMissions())
 		{
 			rounds.add(new Round(rounds.size(), mission));
 		}
 
 		for (PostRoundEventHandler handler : config.getHandlers())
 		{
 			handler.init(this);
 		}
 
 		isStarted = true;
 	}
 
 	/**
 	 * @return true if the game has already started, false, otherwise
 	 */
 	public boolean isStarted()
 	{
 		return isStarted;
 	}
 
 	/**
 	 * make the next proposal
 	 * 
 	 * @param participants
 	 * @return proposal
 	 */
 	public Proposal propose(List<Player> participants)
 	{
 		Proposal proposal = new Proposal(getNumPlayers());
 		proposal.setParticipants(participants);
 
 		getCurrentRound().addProposal(proposal);
 
 		return proposal;
 	}
 
 	/**
 	 * sends the proposal on the mission
 	 * 
 	 * @param proposal
 	 * @return mission
 	 */
 	public Round send(Proposal proposal)
 	{
 		Round curRound = getCurrentRound();
 		curRound.setParticipants(proposal.getParticipants());
 		return curRound;
 	}
 
 	/**
 	 * progresses the game to the next round and calls the post round event
 	 * handlers.
 	 */
 	public void completeRound()
 	{
 		for (PostRoundEventHandler handler : config.getHandlers())
 		{
 			handler.roundFinished();
 		}
 
 		curRound++;
 	}
 
 	/**
 	 * @return true if the game is over, false, otherwise
 	 */
 	public boolean isOver()
 	{
 		return winners != Alignment.NEITHER;
 	}
 
 	/**
 	 * @return the winning side
 	 */
 	public Alignment getWinners()
 	{
 		return winners;
 	}
 
 	/**
 	 * set winning team
 	 * 
 	 * @param winners
 	 */
 	public void setWinners(Alignment winners)
 	{
 		this.winners = winners;
 	}
 
 	/**
 	 * @return extra info
 	 */
 	public ExtraInfoBag getExtraInfo()
 	{
 		return extraInfo;
 	}
 
 	/**
 	 * @return rounds
 	 */
 	public List<Round> getRounds()
 	{
 		return rounds;
 	}
 
 	/**
 	 * @return players
 	 */
 	public List<Player> getPlayers()
 	{
 		return players;
 	}
 
 	/**
 	 * get a player by their name
 	 * 
 	 * @param name
 	 * @return player
 	 */
 	public Player getPlayer(String name)
 	{
 		return playersByName.get(name);
 	}
 
 	/**
 	 * @return number of players
 	 */
 	public int getNumPlayers()
 	{
 		return players.size();
 	}
 
 	/**
 	 * @return current round of play
 	 */
 	public Round getCurrentRound()
 	{
 		return rounds.get(curRound);
 	}
 
 	/**
 	 * @return round handlers
 	 */
 	public List<PostRoundEventHandler> getPostRoundEventHandlers()
 	{
 		return config.getHandlers();
 	}
 
 	/**
 	 * @return true if the game is ready to start, false, otherwise
 	 */
 	public boolean isReady()
 	{
 		return players.size() == config.getRoles().size();
 	}
 
 	/**
 	 * @return current mission leader
 	 */
 	public Player getCurrentLeader()
 	{
 		return players.get(curLeader);
 	}
 }

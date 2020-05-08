 package fearlesscode;
 
 import fearlesscode.util.*;
 
 public class PlayField
 {
 	private boolean blockMode;
 	private Game game;
 	private Player player;
 	private Entity spawnPosition;
 	private Block blocks;
 
 	public PlayField()
 	{
 		Logger.reg(this, "playerField");
 	}
 	/**
 	 * 
 	 * @param position
 	 * @param block
 	 */
 	public void addBlock(Position position, Block block)
 	{
 		Logger.call(this, "addBlock(Position, Block)");
 
 		Logger.ret(this, "addBlock(Position, Block)");
 	}
 
 	/**
 	 * 
 	 * @param block
 	 * @param direction
 	 */
 	public void move(Block block, int direction)
 	{
 		Logger.call(this, "move(Block, int)");
 
 		Logger.ret(this, "move(Block, int)");    
 	}
 
 	/**
 	 * 
 	 * @param player
 	 */
 	public void setPlayer(Player player)
 	{
 		Logger.call(this, "setPlayer(Player)");
 		this.player=player;
 		Logger.ret(this, "setPlayer(Player)");
 	}
 
 	public Player getPlayer()
 	{
		return 
 	}
 
 	/**
 	 * 
 	 * @param entity
 	 */
 	public void setSpawnPosition(Entity entity)
 	{
 		Logger.call(this, "setSpawnPosition(Entity)");
 
 		Logger.ret(this, "setSpawnPosition(Entity)");
 	}
 
 	public void tick()
 	{
 		Logger.call(this, "tick()");
 
 		Logger.ret(this, "tick()");
 	}
 
 	public void toggleMode()
 	{
 		Logger.call(this, "toggleMode()");
 
 		Logger.ret(this, "toggleMode()");
 	}
 
 }

 package fearlesscode.model.core;
 
 import java.util.*;
 import fearlesscode.tools.*;
 import fearlesscode.gui.*;
 import fearlesscode.model.container.*;
 import fearlesscode.model.misc.*;
 import fearlesscode.model.block.*;
 import fearlesscode.model.player.*;
 import fearlesscode.model.entity.*;
 import fearlesscode.model.exception.*;
 
 /**
  * A pályát (Blokkok összességét) reprezentáló objektum.
  * 
  * Ez az objektum tárolja el a pályán található blokkokat, amik közül pontosan egy darab üres. Az ő felelőssége mozgatni őket,
  * és ellenőrizni a szabályosságát a mozgatásnak, és a játékos által felvett utolsó kulcs pozíciójának (vagy ha ilyen nincs, a kezdőpozíciónak) a tárolása,
  * és a játékos visszahelyzése ebbe a pontba, amennyiben szükséges.
  */
 public class PlayField
 {
 	/**
	 * Az üres blokk helye a Blockokat tároló listában.
 	 */
 	private int emptyBlockIndex;
 	/**
 	 * A gravitáció mértéke.
 	 */
 	public static final double GRAVITY=0.21;
 
 	/**
 	 * A PlayField állapota. Blokk módban az értéke igaz, egyébként hamis.
 	 */
 	private boolean blockMode;
 	
 	/**
 	 * A tárolt referencia a Game objektumra, ezt értesíti a pálya megnyerésekor.
 	 */
 	private Game game;
 	
 	/**
 	 * A Player-eket tároló lista.
 	 */
 	private ArrayList<PlayerSpawnPoint> players;
 	
 	/**
 	 * A Block-okat tároló lista.
 	 */
 	private ArrayList<BlockContainer> blocks;
 
 	/**
 	 * A PlayField szélessége.
 	 */
 	private int width;
 
 	/**
 	 * A PlayField magassága.
 	 */
 	private int height;
 
 	/**
 	 * Nyilvántartja, hogy a játék kezdete óta hály "üteme" volt a játéknak,
 	 * azaz hányszor futott le a játék eseméykezelője.
 	 */
 	private long tick;
 
 	/**
 	 * Példányosít egy PlayField objektumot.
 	 *
 	 * Alapértelmezetten teljesen üres, nincsenek benne blokkok, mindent setterekkel állíthatunk be.
 	 * @param game Egy Game objektum referencia, ha értesíteni kell a pálya megnyeréséről.
 	 */
 	public PlayField(Game game)
 	{
 		this.game=game;
 		blocks=new ArrayList<BlockContainer>();
 		players=new ArrayList<PlayerSpawnPoint>();
 		tick=0;
 	}
 	
 	/**
 	 * Egy új blokkot ad hozzá a PlayField objektumhoz a megadott koordinátán.
 	 *
 	 * @param position A blokk helye.
 	 * @param block Az elhelyezendő blokk.
 	 */
 	public void addBlock(Position position, Block block)
 	{
 		BlockContainer container=new BlockContainer(position, block);
 		container.setPlayField(this);
 		blocks.add(container);
 	}
 
 	/**
 	 * A megadott blokkot a megadott irányba elmozdítja (kicseréli a megadott irány beli szomszédjával).
 	 *
 	 * @param block Az elmozdítandó blokk.
 	 * @param direction A mozgatás iránya.
 	 */
 	public void move(Block block, int direction)
 	{
 		for(PlayerContainer pc:block.getPlayers())
 		{
 			if(pc.getPlayer().getActiveBlocks().size() != 1)
 			{
 				return;
 			}
 		}
 		Block neighbour = block.getNeighbour(direction);
 		if(neighbour != null)
 		{
 			for(PlayerContainer pc:neighbour.getPlayers())
 			{
 				if(pc.getPlayer().getActiveBlocks().size() != 1)
 				{
 					return;
 				}
 			}
 		}
 		else
 		{
 			//Ha a szomszéd null, úgyse lehet mozgatni.
 			return;
 		}
 		Position blockPos = new Position(-1,-1);
 		Position neighbPos = new Position(-1,-1);
 		
 		for(BlockContainer blockcontainer : blocks)
 		{									
 			if(blockcontainer.getBlock() == block)
 			{
 				blockPos=blockcontainer.getPosition();
 			}
 			else if(blockcontainer.getBlock() == neighbour)
 			{
 				neighbPos=blockcontainer.getPosition();
 			}
 		}
 		for(BlockContainer blockcontainer : blocks)
 		{									
 			if(blockcontainer.getBlock() == block)
 			{
 				blockcontainer.setPosition(neighbPos);
 			}
 			else if(blockcontainer.getBlock() == neighbour)
 			{
 				blockcontainer.setPosition(blockPos);
 			}
 		}
 		block.setNeighbour(getBlock(new Position(neighbPos.getX(),neighbPos.getY()-1)),0,true);
 		block.setNeighbour(getBlock(new Position(neighbPos.getX()+1,neighbPos.getY())),1,true);
 		block.setNeighbour(getBlock(new Position(neighbPos.getX(),neighbPos.getY()+1)),2,true);
 		block.setNeighbour(getBlock(new Position(neighbPos.getX()-1,neighbPos.getY())),3,true);
 		neighbour.setNeighbour(getBlock(new Position(blockPos.getX(),blockPos.getY()-1)),0,true);
 		neighbour.setNeighbour(getBlock(new Position(blockPos.getX()+1,blockPos.getY())),1,true);
 		neighbour.setNeighbour(getBlock(new Position(blockPos.getX(),blockPos.getY()+1)),2,true);
 		neighbour.setNeighbour(getBlock(new Position(blockPos.getX()-1,blockPos.getY())),3,true);
 	}
 
 	/**
 	 * Egy új játékos referenciát ad hozzá a játékosok listájához.
 	 * 
 	 * @param player Az új játékos referenciája.
 	 * @param spawnPoint Az új játékos SpawnPoint-jának referenciája.
 	 */
 	public void addPlayer(Player player, Entity spawnPoint)
 	{
 		players.add(new PlayerSpawnPoint(player, spawnPoint));
 	}
 
 	/**
 	 * Visszaadja a játékosok listáját.
 	 * 
 	 * @return A PlayField objektumban eltárolt játékosok és spawnpoint-juk listája.
 	 */
 	public ArrayList<PlayerSpawnPoint> getPlayers()
 	{
 		return players;
 	}
 	
 	/**
 	 * Adott Position-ű Block-ot visszaadó metódus.
 	 * @param pos Az adott Position.
 	 */
 	public Block getBlock(Position pos)
 	{
 		for(BlockContainer blockcontainer : blocks)
 		{								
 			if(blockcontainer.getPosition().getX()==pos.getX()&&blockcontainer.getPosition().getY()==pos.getY())
 			{
 				return blockcontainer.getBlock();
 			}
 		}
 		return null;
 		
 	}
 
 	/**
 	 * Beállítja a megadott játékos újraéledési helyét a pályán belül (egy Entity objektumra).
 	 *
 	 * @param player A módosítani kívánt játékos referenciája.
 	 * @param entity Az új újraéledési pont.
 	 */
 	public void setSpawnPosition(Player player, Entity entity)
 	{
 		for(PlayerSpawnPoint spawn : players)
 		{
 			//elég a referencia szerint egyezést vizsgálni
 			if(spawn.getPlayer() == player)
 			{
 				spawn.setSpawnPoint(entity);
 			}
 		}
 	}
 
 	/**
 	 * A játék-mód eseménykezelője.
 	 *
 	 * Minden egyes frissítésre lefuttatja az ütközésdetektálásokat, és a mozgást.
 	 * (blokkon belüli, és kívüli).
 	 */
 	public void tick()
 	{
 		tick++;
 		ArrayList<Block> active=new ArrayList<Block>();
 		for(PlayerSpawnPoint player:players)
 		{
 			player.getPlayer().setProcessed(false);
 			player.getPlayer().move(new Speed(0, GRAVITY));
 			for(Block block:player.getPlayer().getActiveBlocks())
 			{
 				if(!active.contains(block))
 				{
 					active.add(block);
 				}
 			}
 		}
 		for(Block block:active)
 		{
 			block.processCollisions();
 		}
 		for(Block block:active)
 		{
 			block.checkBorders();
 		}
 	}
 
 	/**
 	 * Váltás játék-mód és blokk-mód között.
 	 *
 	 * Játék módban a blokkok nem mozognak, blokk módban a tick nem fut.
 	 */
 	public void toggleMode()
 	{
 		blockMode=!blockMode;
 		Logger.log("Game mode has been toggled.");
 	}
 
 	/**
 	 * A metódus bejelenti a Game objektumnak, hogy a pálya végére ért a játékos.
 	 *
 	 * Ha minden feltétel teljesül, a pálya véget ér, és betöltődik a következő pálya.
 	 */
 	public void win()
 	{
 		game.loadNextLevel();
 		Logger.log("Level finished!");
 	}
 
 	/**
 	 * A megadott játékost visszahelyezi az újraéledési helyére.
 	 *
 	 * @param player Az áthelyezni kívánt játékos.
 	 */
 	public void resetPlayer(PlayerContainer player)
 	{
 		for(PlayerSpawnPoint spawn : players)
 		{
 			if(spawn.getPlayer().getID() == player.getPlayer().getID())
 			{
 				Logger.log(spawn.getPlayer(),"been reset to "+spawn.getSpawnPoint().getName());
 				spawn.getPlayer().reset();
 				spawn.getPlayer().enterBlock(
 					spawn.getSpawnPoint().getContainer().getBlock(),
 					spawn.getSpawnPoint().getContainer().getPosition());
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Visszaadja a PlayField blokkjait.
 	 * 
 	 * @return A PlayFieldben lévő minden blokk.
 	 */
 	public ArrayList<BlockContainer> getBlocks()
 	{
 		return blocks;
 	}
 
 	/**
 	 * Visszaadja a megadott azonosítójú blokkot.
 	 * Ha nincs ilyen, akkor kivétel keletkezik (`CommandException`).
 	 * 
 	 * @param id A blokk azonosítója.
 	 * @return A kért blokk.
 	 */
 	public Block getBlock(int id) throws CommandException
 	{
 		for(BlockContainer container : blocks)
 		{
 			if(container.getBlock().getID() == id)
 			{
 				return container.getBlock();
 			}
 		}
 		throw new CommandException("Block #"+id+" not found.");
 	}
 	
 	/**
 	 * Visszaadja a megadott azonosítójú blokk pozicióját.
 	 * Ha nincs ilyen, akkor kivétel keletkezik (`CommandException`).
 	 * 
 	 * @param id A blokk azonosítója.
 	 * @return A kért blokk poziciója.
 	 */
 	public Position getBlockPosition(int id) throws CommandException
 	{
 		for(BlockContainer container : blocks)
 		{
 			if(container.getBlock().getID() == id)
 			{
 				return container.getPosition();
 			}
 		}
 		throw new CommandException("Block #"+id+" not found.");
 	}
 	
 	/**
 	 * Visszaadja a megadott azonosítójú játékost.
 	 * Ha nincs ilyen, akkor kivétel keletkezik (`CommandException`).
 	 *
 	 * @param id A játékos azonosítója.
 	 * @return A kért játékos.
 	 */
 	public Player getPlayer(int id) throws CommandException
 	{
 		for(PlayerSpawnPoint container:players)
 		{
 			if(container.getPlayer().getID() == id)
 			{
 				return container.getPlayer();
 			}
 		}
 		throw new CommandException("Player #"+id+" not found.");
 	}
 	
 	/**
 	 * Visszaadja, hogy a játéktér blokk-módban van-e.
 	 * @return Blokk módban vagyunk-e.
 	 */
 	public boolean isBlockMode()
 	{
 		return blockMode;
 	}
 
 	
 	/**
 	 * Beállítja a PlayField szélességét(setter).
 	 */
 	public void setWidth(int x)
 	{
 		width=x;
 	}
 	
 	/**
 	 * Visszaadja a PlayField szélességét.
 	 * @return A PlayField szélessége.
 	 */
 	public int getWidth()
 	{
 		return width;
 	}
 	
 	/**
 	 * Beállítja a PlayField magasságát(setter).
 	 */
 	public void setHeight(int y)
 	{
 		height=y;
 	}
 	
 	/**
 	 * Visszaadja a PlayField magasságát.
 	 * @return A PlayField magassága.
 	 */
 	public int getHeight()
 	{
 		return height;
 	}
 	
 	/**
 	 * Visszaadja a PlayField eseménykezelő-lefutásának a számát.
 	 * @return A PlayField eseménykezelő-lefutásának a száma.
 	 */
 	public long getTick()
 	{
 		return tick;
 	}
 	
 	/**
	 * Beállítja az üres blokk indexszámát.
 	 */
 	public void setEmptyBlockIndex(int index)
 	{
 		this.emptyBlockIndex=index;
 	}
 	/**
	 * Visszaadja az üres blokk indexszámát.
	 * @return Az üres blokk indexszáma.
 	 */	
 	public int getEmptyBlockIndex()
 	{
 		return this.emptyBlockIndex;
 	}
 }

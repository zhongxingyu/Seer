 package fearlesscode;
 
 import fearlesscode.util.*;
 import java.util.*;
 
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
 	}
 	
 	/**
 	 * Egy új blokkot ad hozzá a PlayField objektumhoz a megadott koordinátán.
 	 *
 	 * @param position A blokk helye.
 	 * @param block Az elhelyezendő blokk.
 	 */
 	public void addBlock(Position position, Block block)
 	{
 		blocks.add(new BlockContainer(position, block));
 	}
 
 	/**
 	 * A megadott blokkot a megadott irányba elmozdítja (kicseréli a megadott irány beli szomszédjával).
 	 *
 	 * @param block Az elmozdítandó blokk.
 	 * @param direction A mozgatás iránya.
 	 */
 	public void move(Block block, int direction)
 	{
		/*
		block=blocks.get(0).block;
		Logger.call(this, "move(Block, int)");			
		block.getNeighbour(direction);		
		//player.getActiveBlocks();
		if(Logger.ask("Letrejohet a csere?"))
		{
			block.getNeighbours();				  
			blocks.get(1).block.getNeighbours();
			blocks.get(1).block.setNeighbours(null);
			block.setNeighbours(null);
			block.setNeighbour(blocks.get(1).block, 3, true);
		}
		Logger.ret(this, "move(Block, int)");
		*/ 
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
 		
 	}
 
 	/**
 	 * Váltás játék-mód és blokk-mód között.
 	 *
 	 * Játék módban a blokkok nem mozognak, blokk módban a tick nem fut.
 	 */
 	public void toggleMode()
 	{
 		blockMode=!blockMode;
 	}
 
 	/**
 	 * A metódus bejelenti a Game objektumnak, hogy a pálya végére ért a játékos.
 	 *
 	 * Ha minden feltétel teljesül, a pálya véget ér, és betöltődik a következő pálya.
 	 */
 	public void win()
 	{
 		game.loadNextLevel();
 	}
 
 	/**
 	 * A megadott játékost visszahelyezi az újraéledési helyére.
 	 *
 	 * @param player Az áthelyezni kívánt játékos.
 	 */
 	public void resetPlayer(Player player)
 	{
		
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
 	 * Visszaadja a megadott azonosítójú blokkot
 	 * 
 	 * @param id A blokk azonosítója
 	 */
 	public Block getBlock(int id) throws CommandException
 	{
 		for(BlockContainer container : blocks)
 		{
 			/*
 			if(container.getBlock().getId() == id)
 			{
 				return container.getBlock();
 			}
 			*/
 		}
 		throw new CommandException("Block #"+id+" not found.");
 	}
 }

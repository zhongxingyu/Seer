 package com.ftwinston.Killer.ChunkKiller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldType;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 import com.ftwinston.Killer.GameMode;
 import com.ftwinston.Killer.Helper;
 import com.ftwinston.Killer.Option;
 import com.ftwinston.Killer.WorldConfig;
 
 public class ChunkKiller extends GameMode
 {
 	public static final int useSlaves = 0, outlastYourOwnChunk = 1;	
 	
 	String[] playerIndices;
 	boolean[] chunksStillAlive;
 	int[] slaveMasters;
 	int chunkRows, chunkCols, chunksOnLastRow;
 	final int chunkSpacing = 3, maxCoreY = 63, chunkCoreX = 8, chunkCoreZ = 8;
 	
 	int coreMaterial = Material.EMERALD_BLOCK.getId();
 	
 	@Override
 	public int getMinPlayers() { return 2; } // one player on each team is our minimum
 	
 	@Override
 	public Option[] setupOptions()
 	{
 		Option[] options = {
 			new Option("Losing players become slaves", true),
 			new Option("Outlast your own chunk", false)
 		};
 		
 		return options;
 	}
 	
 	@Override
 	public String getHelpMessage(int num, int team)
 	{
 		switch ( num )
 		{
 			case 0:
 			default:
 				return null;
 		}
 	}
 	
 	@Override
 	public boolean teamAllocationIsSecret() { return false; }
 	
 	@Override
 	public boolean isLocationProtected(Location l, Player player)
 	{
 		// if a player is a slave, their master's chunk is protected from them
 		int index = getPlayerIndex(player);
 		int slaveMaster = slaveMasters[index];
 		
 		if ( slaveMaster != -1 && l.getChunk() == getChunkByIndex(slaveMaster) )
 			return true;
 		
 		// cores can only be affected directly by players (not by explosions or pistons)
 		if ( player == null && l.getBlock().getTypeId() == coreMaterial )
 			return true;
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isAllowedToRespawn(Player player)
 	{
 		// you can respawn if your chunk is still alive, OR if slaves are enabled and you have a master
 		int index = getPlayerIndex(player);
 		return chunksStillAlive[index] ||
 		(getOption(useSlaves).isEnabled() && slaveMasters[index] != -1);
 	}
 	
 	@Override
 	public boolean useDiscreetDeathMessages() { return false; }
 	
 	@Override
 	public Environment[] getWorldsToGenerate() { return new Environment[] { Environment.NORMAL }; }
 	
 	@Override
 	public void beforeWorldGeneration(int worldNumber, WorldConfig world)
 	{
 		// get a list of all players, in a random order
 		List<Player> players = getOnlinePlayers();
 		Collections.shuffle(players);
 		
 		// set up an array of player names, so we can easily determine the index (and thus the chunk) of any given player
 		playerIndices = new String[players.size()];
 		chunksStillAlive = new boolean[playerIndices.length];
 		slaveMasters = new int[playerIndices.length];
 		for ( int i=0; i<playerIndices.length; i++ )
 		{
 			playerIndices[i] = players.get(i).getName();
 			chunksStillAlive[i] = true;
 			slaveMasters[i] = -1;
 		}
 		
 		chunkRows = (int)Math.ceil(Math.sqrt(playerIndices.length));
 		chunkCols = (int)Math.ceil((double)playerIndices.length/chunkRows);
 		chunksOnLastRow = playerIndices.length % chunkRows;
 		if ( chunksOnLastRow == 0 )
 			chunksOnLastRow = chunkCols;
 		
 		world.setWorldType(WorldType.FLAT);
 		world.setGenerator(new PlayerChunkGenerator());
 	}
 	
 	public class PlayerChunkGenerator extends ChunkGenerator
 	{
 		int chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, lastRowChunk;
 		public PlayerChunkGenerator()
 		{
 			chunkMinX = 0; chunkMaxX = (chunkRows-1) * chunkSpacing;
 			chunkMinZ = 0; chunkMaxZ = (chunkCols-1) * chunkSpacing;
 			lastRowChunk = (chunksOnLastRow-1) * chunkSpacing;
 		}
 		
 		@Override
 		public List<BlockPopulator> getDefaultPopulators(World world)
 		{
 			List<BlockPopulator> list = new ArrayList<BlockPopulator>();
 			list.add(new DetailPopulator());
 			return list;
 		}
 		
 		@Override
 		public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes)
 		{
 			if ( isEmptyChunk(x, z) )
 				return new byte[1][];
 			
 			// a player chunk goes here
 			final int numChunkSections = 4;
 			byte[][] data = new byte[numChunkSections][];
 			
 			// stone
 			for ( int i=0; i<numChunkSections; i++ )
 			{
 				data[i] = new byte[4096];
 				for ( int j=0; j<4096; j++ )
 					data[i][j] = 1;
 			}
 
 			// bedrock
 			int i = 0;
 			for ( int j=0; j<256; j++ )
 				data[i][j] = 7;
 			
 			// dirt
 			i = numChunkSections - 1;
 			for ( int j=2048; j<3840; j++ )
 				data[i][j] = 3;
 			
 			// grass
 			for ( int j=3840; j<4096; j++ )
 				data[i][j] = 2;
 			
 			return data;
 		}
 		
 		void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
 	        if (result[y >> 4] == null) {
 	            result[y >> 4] = new byte[4096];
 	        }
 	        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
 	    }
 		
 		private boolean isEmptyChunk(int x, int z)
 		{
 			return x < chunkMinX || x > chunkMaxX || z < chunkMinZ || z > chunkMaxZ || x % chunkSpacing != 0 || z % chunkSpacing != 0 || (x == chunkMaxX && z > lastRowChunk);
 		}
 		
 		private Block getRandomBlock(Chunk c, Random r, int yMin, int yMax)
 		{
 			Block b = null;
 			for ( int i=0; i<3; i++ )
 			{
 				int x = 1 + r.nextInt(13), z = 1 + r.nextInt(13);
 				if ( x >= chunkCoreX )
 					x++;
 				if ( z >= chunkCoreZ )
 					z++;
 				b = c.getBlock(x, yMin + r.nextInt(yMax-yMin+1), z);
 				if ( b.getType() == Material.STONE )
 					break;
 			}
 			return b;
 		}
 		
 		public class DetailPopulator extends BlockPopulator
 		{
 			@Override
 			public void populate(World w, Random r, Chunk c)
 			{
 				if ( isEmptyChunk(c.getX(), c.getZ()) )
 					return;
 				
 				r.setSeed(w.getSeed()); // ensure each chunk generates the same
 				
 				for ( int y=0;y<=maxCoreY; y++ )
 					c.getBlock(chunkCoreX, y, chunkCoreZ).setTypeId(coreMaterial);
 				
 				int num = 5; // 3 clumps of gravel, each 3x3
 				for ( int i=0; i<num; i++ )
 				{
 					Block b = getRandomBlock(c, r, 40, 62);
 					for ( int j=0; j<3; j++ )
 					{
 						b.setType(Material.GRAVEL);
 						b.getRelative(BlockFace.NORTH).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.EAST).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.SOUTH).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.WEST).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.NORTH_EAST).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.NORTH_WEST).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.SOUTH_EAST).setType(Material.GRAVEL);
 						b.getRelative(BlockFace.SOUTH_WEST).setType(Material.GRAVEL);
 						b = b.getRelative(BlockFace.DOWN);
 					}
 				}
 				
 				num = r.nextInt(4) + 2; // 2-5 veins of 2-4 diamonds
 				for ( int i=0; i<num; i++ )
 				{
 					Block b = getRandomBlock(c, r, 2, 20);
 					b.setType(Material.DIAMOND_ORE);
 					if ( r.nextInt(3) != 1 )	
 						b.getRelative(r.nextBoolean() ? BlockFace.NORTH : BlockFace.SOUTH).setType(Material.DIAMOND_ORE);
 					if ( r.nextInt(3) != 1 )						
 						b.getRelative(r.nextBoolean() ? BlockFace.EAST : BlockFace.WEST).setType(Material.DIAMOND_ORE);
 					if ( r.nextInt(3) != 1 )						
 						b.getRelative(r.nextBoolean() ? BlockFace.UP : BlockFace.DOWN).setType(Material.DIAMOND_ORE);
 				}
 				
 				num = r.nextInt(3) + 5; // 5-7 veins of up to 8 iron
 				for ( int i=0; i<num; i++ )
 				{
 					Block b = getRandomBlock(c, r, 20, 44);
 					BlockFace f1 = r.nextBoolean() ? BlockFace.NORTH : BlockFace.SOUTH, f2 = r.nextBoolean() ? BlockFace.EAST : BlockFace.WEST;
 					for ( int j=0; j<2; j++ )					
 					{
 						b.setType(Material.IRON_ORE);
 						if ( r.nextInt(7) != 1 )	
 							b.getRelative(f1).setType(Material.IRON_ORE);
 						if ( r.nextInt(7) != 1 )						
 							b.getRelative(f2).setType(Material.IRON_ORE);
 						if ( r.nextInt(7) != 1 )						
 							b.getRelative(f1).getRelative(f2).setType(Material.IRON_ORE);
 						b = b.getRelative(r.nextBoolean() ? BlockFace.UP : BlockFace.DOWN);
 					}
 				}
 				
 				num = r.nextInt(4) + 5; // 5-8 veins of up to 8 coal
 				for ( int i=0; i<num; i++ )
 				{
 					Block b = getRandomBlock(c, r, 32, 48);
 					BlockFace f1 = r.nextBoolean() ? BlockFace.NORTH : BlockFace.SOUTH, f2 = r.nextBoolean() ? BlockFace.EAST : BlockFace.WEST;
 					for ( int j=0; j<2; j++ )					
 					{
 						b.setType(Material.COAL_ORE);
 						if ( r.nextInt(7) != 1 )	
 							b.getRelative(f1).setType(Material.COAL_ORE);
 						if ( r.nextInt(7) != 1 )						
 							b.getRelative(f2).setType(Material.COAL_ORE);
 						if ( r.nextInt(7) != 1 )						
 							b.getRelative(f1).getRelative(f2).setType(Material.COAL_ORE);
 						b = b.getRelative(r.nextBoolean() ? BlockFace.UP : BlockFace.DOWN);
 					}
 				}
 				
 				num = r.nextInt(4) + 2; // 2-5 veins of up to 8 redstone
 				for ( int i=0; i<num; i++ )
 				{
 					Block b = getRandomBlock(c, r, 32, 48);
 					BlockFace f1 = r.nextBoolean() ? BlockFace.NORTH : BlockFace.SOUTH, f2 = r.nextBoolean() ? BlockFace.EAST : BlockFace.WEST;
 					for ( int j=0; j<2; j++ )					
 					{
 						b.setType(Material.REDSTONE_ORE);
 						if ( r.nextInt(4) != 1 )	
 							b.getRelative(f1).setType(Material.REDSTONE_ORE);
 						if ( r.nextInt(4) != 1 )						
 							b.getRelative(f2).setType(Material.REDSTONE_ORE);
 						if ( r.nextInt(4) != 1 )						
 							b.getRelative(f1).getRelative(f2).setType(Material.REDSTONE_ORE);
 						b = b.getRelative(r.nextBoolean() ? BlockFace.UP : BlockFace.DOWN);
 					}
 				}
 				
 				w.generateTree(c.getBlock(1 + r.nextInt(7), maxCoreY+1, 1 + r.nextInt(7)).getLocation(), TreeType.BIRCH);
 				w.generateTree(c.getBlock(9 + r.nextInt(6), maxCoreY+1, 1 + r.nextInt(7)).getLocation(), TreeType.JUNGLE);
 				w.generateTree(c.getBlock(1 + r.nextInt(7), maxCoreY+1, 9 + r.nextInt(6)).getLocation(), TreeType.REDWOOD);
 				w.generateTree(c.getBlock(9 + r.nextInt(6), maxCoreY+1, 9 + r.nextInt(6)).getLocation(), TreeType.TREE);
 			}
 		}
 	}
 	
 	@Override
 	public Location getSpawnLocation(Player player)
 	{
 		// each player should spawn on their own chunk ... unless they're a slave (having been defeated),
 		// in which case they should spawn on their master's chunk. Assuming slavery is enabled.
 		
 		int index = getPlayerIndex(player);
 		
 		if ( !chunksStillAlive[index] && getOption(useSlaves).isEnabled() )
 			index = slaveMasters[index];
 		
 		Chunk c = getChunkByIndex(index);
 		
 		int x = (c.getX() << 4) + 8, z = (c.getZ() << 4) + 8;
 		Location loc = c.getWorld().getHighestBlockAt(x, z).getRelative(BlockFace.UP).getLocation();
 		
 		// if chunk has been destroyed, spawn where the top would have been
 		if ( loc.getY() < 1 )
 			loc.setY(64);
 		
 		return loc;
 	}
 	
 	@Override
 	public void gameStarted(boolean isNewWorlds)
 	{
 		if ( !isNewWorlds )
 			; // this ... wouldn't work. Game mode ought to have a function for whether this is allowed at all??
 	}
 	
 	public int getPlayerIndex(Player player)
 	{
 		for ( int i=0; i< playerIndices.length; i++ )
 			if ( player.getName().equals(playerIndices[i]) )
 				return i;
 		return 0;
 	}
 	
 	public Chunk getChunkByIndex(int index)
 	{
		int row = index / chunkRows, col = index % chunkRows;
 		return getWorld(0).getChunkAt(row * chunkSpacing, col * chunkSpacing);
 	}
 	
 	public int getIndexOfChunk(Chunk c)
 	{
 		return (c.getX() / chunkSpacing) * chunkCols + (c.getZ() / chunkSpacing);
 	}
 	
 	public int getIndexByName(String name)
 	{
 		for ( int i=0; i<playerIndices.length; i++ )
 			if ( name.equals(playerIndices[i]) )
 				return i;
 		return -1;
 	}
 	
 	@Override
 	public void gameFinished()
 	{
 		
 	}
 	
 	@Override
 	public void playerJoinedLate(Player player, boolean isNewPlayer)
 	{
 		if ( !isNewPlayer )
 			return;
 		
 		// make them be a spectator
 		Helper.makeSpectator(getGame(), player);
 	}
 	
 	@Override
 	public void playerKilledOrQuit(OfflinePlayer player)
 	{
 		if ( hasGameFinished() )
 			return;
 	}
 	
 	@Override
 	public void toggleOption(int num)
 	{
 		super.toggleOption(num);
 		Option.ensureOnlyOneEnabled(getOptions(), num, useSlaves, outlastYourOwnChunk);
 	}
 	
 	@Override
 	public Location getCompassTarget(Player player)
 	{
 		return getSpawnLocation(player);
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
     {
 		Block b = event.getBlock();
 		if ( shouldIgnoreEvent(b) || b.getTypeId() != coreMaterial )
 			return;
 		
     	event.setExpToDrop(50);
 		
 		// remove all blocks on the same level as this, in the same chunk
 		Chunk c = b.getChunk();
 		World w = b.getWorld();
 		int y = b.getY();
 		for ( int x=0; x<16; x++ )
 			for ( int z=0; z<16; z++ )
 			{
 				Block remove = c.getBlock(x,y,z);
 				if ( remove != b )
 					remove.setType(Material.AIR);
 			}
 		
 		// if there are no other blocks of this type in this chunk, this chunk's player has been defeated
 		for ( y = 0; y<=maxCoreY; y++ )
 			if ( w.getBlockTypeIdAt(b.getX(), y, b.getZ()) == coreMaterial )
 				return; // don't continue, because this player isn't defeated
 		
 		// update this player to be defeated
 		int index = getIndexOfChunk(c);
 		chunksStillAlive[index] = false;
 		Player victimPlayer = getPlugin().getServer().getPlayerExact(playerIndices[index]);
 		Player killerPlayer = event.getPlayer();
 		
 		if ( getOption(useSlaves).isEnabled() )
 		{// this player becomes a slave of the player who defeated them, as do any slaves THEY may have
 			int newMaster = killerPlayer == null ? -1 : getIndexByName(killerPlayer.getName());
 			slaveMasters[index] = newMaster;
 			for ( int i=0; i<slaveMasters.length; i++ )
 				if ( slaveMasters[i] == index )
 					slaveMasters[i] = newMaster;
 		}
 		else if ( victimPlayer != null && !getOption(outlastYourOwnChunk).isEnabled() )
 			victimPlayer.setHealth(0); // this player should die, now
 		
 		if ( killerPlayer != null )
 			broadcastMessage(ChatColor.RED + playerIndices[index] + "'s chunk was destroyed by " + killerPlayer.getName());
 		else
 			broadcastMessage(ChatColor.RED + playerIndices[index] + "'s chunk was destroyed");
 		
 		// check for game end ... this only accounts for ONE combination of options
 		int numRemaining = 0, remainingIndex = 0;
 		for ( int i=0; i<chunksStillAlive.length; i++ )
 			if ( chunksStillAlive[i] )
 			{
 				numRemaining++;
 				remainingIndex = i;
 			}
 		
 		if ( numRemaining == 1 )
 		{
 			broadcastMessage(ChatColor.YELLOW + "Only one chunk remaining - " + playerIndices[remainingIndex] + " wins the game!");
 			finishGame();
 		}
 		else if ( numRemaining == 0 )
 		{
 			broadcastMessage(ChatColor.YELLOW + "All chunks have been destroyed, game drawn");
 			finishGame();
 		}
     }
 	
 	@EventHandler
     public void onBlockPlace(BlockPlaceEvent event)
     {
     	if ( shouldIgnoreEvent(event.getBlock()) )
 			return;
 		
     	if ( event.getBlock().getTypeId() == coreMaterial )
 			event.setCancelled(true);
     }
 }

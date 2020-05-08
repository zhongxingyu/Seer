 package com.ftwinston.Killer;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftSkeleton;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.ftwinston.Killer.Killer.GameState;
 
 public class StagingWorldManager
 {
 	public StagingWorldManager(Killer plugin, World world)
 	{
 		this.plugin = plugin;
 		stagingWorld = world;
 	}
 	Killer plugin;
 	World stagingWorld;
 	
 	Random random = new Random();
 	public Location getStagingWorldSpawnPoint()
 	{
 		return new Location(stagingWorld, -13.5f + random.nextDouble() * 4 - 2, StagingWorldGenerator.floorY + 1, 26.5f - random.nextDouble(), 180, 0);
 	}
 
 	public enum StagingWorldOption
 	{
 		NONE,
 		GAME_MODE,
 		GAME_MODE_OPTION,
 		WORLD_OPTION,
 		GLOBAL_OPTION,
 		MONSTERS,
 		ANIMALS,
 	}
 	
 	private StagingWorldOption currentOption = StagingWorldOption.NONE;
 	public void setCurrentOption(StagingWorldOption option)
 	{
 		if ( option == currentOption )
 			return;
 		
 		// disable whatever's currently on
 		switch ( currentOption )
 		{
 		case GAME_MODE:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.gameModeButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		case GAME_MODE_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.gameOptionButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		case WORLD_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.worldOptionButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		case GLOBAL_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.globalOptionButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		case MONSTERS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.monstersButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		case ANIMALS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.animalsButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			hideSetupOptionButtons();
 			break;
 		}
 		
 		currentOption = option;
 		String[] labels;
 		boolean[] values;
 		
 		// now set up the new option
 		switch ( currentOption )
 		{
 		case GAME_MODE:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.gameModeButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			labels = new String[GameMode.gameModes.size()];
 			values = new boolean[labels.length];
 			for ( int i=0; i<labels.length; i++ )
 			{
 				GameMode mode = GameMode.gameModes.get(i); 
 				labels[i] = mode.getName();
 				values[i] = mode == plugin.getGameMode();
 			}
 			showSetupOptionButtons("Game mode:", labels, values);
 			break;
 		case GAME_MODE_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.gameOptionButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			List<GameMode.Option> options = plugin.getGameMode().getOptions();
 			labels = new String[options.size()];
 			values = new boolean[labels.length];
 			for ( int i=0; i<options.size(); i++ )
 			{
 				labels[i] = options.get(i).getName();
 				values[i] = options.get(i).isEnabled();
 			}
 			showSetupOptionButtons("Mode option:", labels, values);
 			break;
 		case WORLD_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.worldOptionButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			labels = new String[WorldOption.options.size()];
 			values = new boolean[labels.length];
 			for ( int i=0; i<labels.length; i++ )
 			{
 				WorldOption worldOption = WorldOption.options.get(i); 
 				labels[i] = worldOption.getName();
 				values[i] = worldOption == plugin.getWorldOption();
 			}
 			showSetupOptionButtons("World option:", labels, values);
 			break;
 		case GLOBAL_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.globalOptionButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[] { "Craftable monster eggs", "Easier dispenser recipe", "Eyes of ender find nether fortresses" };
 			values = new boolean[] { true, true, true, true };
 			showSetupOptionButtons("Global option:", labels, values);
 			break;
 		case MONSTERS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.monstersButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[5];
 			values = new boolean[5];
 			for ( int i=0; i<5; i++ )
 			{
 				labels[i] = StagingWorldGenerator.getQuantityText(i);
 				values[i] = i == plugin.monsterNumbers;
 			}
 			showSetupOptionButtons("Monsters:", labels, values);
 			break;
 		case ANIMALS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinCorridorX, StagingWorldGenerator.buttonY, StagingWorldGenerator.animalsButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[5];
 			values = new boolean[5];
 			for ( int i=0; i<5; i++ )
 			{
 				labels[i] = StagingWorldGenerator.getQuantityText(i);
 				values[i] = i == plugin.animalNumbers;
 			}
 			showSetupOptionButtons("Animals:", labels, values);
 		}
 	}
 	
 	private void showSetupOptionButtons(String heading, String[] labels, boolean[] values)
 	{
 		Block b;
 		for ( int i=0; i<labels.length; i++ )
 		{
 			int z = StagingWorldGenerator.getOptionButtonZ(i);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, StagingWorldGenerator.buttonY-1, z);
 			b.setType(Material.WOOL);
 			b.setData(StagingWorldGenerator.signBackColor);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, StagingWorldGenerator.buttonY, z);
 			b.setType(Material.WOOL);
 			b.setData(values[i] ? StagingWorldGenerator.colorOptionOn : StagingWorldGenerator.colorOptionOff);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, StagingWorldGenerator.buttonY+1, z);
 			b.setType(Material.WOOL);
 			b.setData(StagingWorldGenerator.signBackColor);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, StagingWorldGenerator.buttonY, z);
 			b.setType(Material.STONE_BUTTON);
 			b.setData((byte)0x2);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, StagingWorldGenerator.buttonY+1, z);
 			b.setType(Material.WALL_SIGN);
 			b.setData((byte)0x4);
 			Sign s = (Sign)b.getState();
 			s.setLine(0, heading);
 			
 			StagingWorldGenerator.fitTextOnSign(s, labels[i]);
 			s.update();
 		}
 	}
 
 	private void hideSetupOptionButtons()
 	{
 		Block b;
 		for ( int i=0; i<12; i++ ) // there's only space for 12 options, without extending the world further
 		{
 			int z = StagingWorldGenerator.getOptionButtonZ(i);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, StagingWorldGenerator.buttonY, z);
 			b.setType(Material.AIR);
 			b.setData((byte)0x2);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, StagingWorldGenerator.buttonY+1, z);
 			b.setType(Material.AIR);
 			
 			for ( int y=StagingWorldGenerator.floorY+1; y<StagingWorldGenerator.buttonY+2; y++)
 			{
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, y, z);
 				b.setType(Material.SMOOTH_BRICK);
 			}
 		}
 	}
 	
 	private void updateSetupOptionButtons(boolean[] values)
 	{
 		Block b;
 		for ( int i=0; i<values.length; i++ )
 		{
 			int z = StagingWorldGenerator.getOptionButtonZ(i);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, StagingWorldGenerator.buttonY, z);
 			b.setData(values[i] ? StagingWorldGenerator.colorOptionOn : StagingWorldGenerator.colorOptionOff);
 		}
 	}
 	
 	boolean arenaModeIsSpleef = true;
 	int monsterWaveNumber = 0, numMonstersAlive = 0;
 	public void stagingWorldMonsterKilled()	
 	{
 		if ( monsterWaveNumber == 0 )
 			return;
 		
 		numMonstersAlive--;
 		
 		if ( numMonstersAlive <= 0 )
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				@Override
 				public void run() {
 					spawnMonsterWave(); // needs to start (that's easy enough) when player enters					
 				}
 			}, 50);
 			
 	}
 	
 	public void stagingWorldPlayerKilled()
 	{
 		if ( arenaModeIsSpleef || countPlayersInArena() > 0 )
 			return;
 		
 		endMonsterArena();
 	}
 	
 	public void endMonsterArena()
 	{
 		for ( Entity entity : stagingWorld.getEntities() )
 			if ( entity instanceof Monster )
 				entity.remove();
 		monsterWaveNumber = 0; numMonstersAlive = 0;
 		stagingWorld.setMonsterSpawnLimit(0);
 		
 		// clear the world writing
 		for ( int x=arenaScoreX - 25; x<arenaScoreX + 25; x++ )
 			for ( int y=StagingWorldGenerator.floorY+3; y<StagingWorldGenerator.floorY+8; y++ )
 				stagingWorld.getBlockAt(x, y, arenaScoreZ).setType(Material.AIR);
 	}
 	
 	private int countPlayersInArena()
 	{
 		int numPlayersInArena = 0;
 		for ( Player player : stagingWorld.getPlayers() )
 		{
 			Location loc = player.getLocation();
 			if ( loc.getBlockX() < StagingWorldGenerator.spleefMinX - 1 || loc.getBlockX() > StagingWorldGenerator.spleefMaxX + 1
 			  || loc.getBlockZ() < StagingWorldGenerator.spleefMinZ - 1 || loc.getBlockZ() > StagingWorldGenerator.spleefMaxZ + 1)
 				continue;
 			numPlayersInArena ++;
 		}
 		return numPlayersInArena;
 	}
 	
 	private static final int arenaScoreZ = StagingWorldGenerator.spleefMaxZ + 8, arenaScoreX = StagingWorldGenerator.waitingMonsterButtonX + 2;
 	public void spawnMonsterWave()
 	{
 		monsterWaveNumber++;
 		
 		// write the wave number into the world
 		boolean[][] text = StagingWorldGenerator.writeBlockText("WAVE " + monsterWaveNumber);
 		int xMin = arenaScoreX + text.length/2, yMin = StagingWorldGenerator.floorY + 3;
 		for ( int i=0; i<text.length; i++ )
 			for ( int j=0; j<text[i].length; j++ )
 				stagingWorld.getBlockAt(xMin-i, yMin + j, arenaScoreZ).setType(text[i][j] ? Material.SNOW_BLOCK : Material.AIR);
 		
 		
 		WorldServer ws = ((CraftWorld)stagingWorld).getHandle();
 		ws.allowMonsters = true;
 		stagingWorld.setMonsterSpawnLimit(monsterWaveNumber);
 
 		Location loc;
 		
 		// wither + normal skeleton
 		if ( monsterWaveNumber == 5 )
 		{
			numMonstersAlive = 2;
 			
 			loc = new Location(stagingWorld, StagingWorldGenerator.spleefMinX + random.nextDouble() * 16, StagingWorldGenerator.spleefY+1, StagingWorldGenerator.spleefMinZ + random.nextDouble() * 16);
 			loc.setY(stagingWorld.getHighestBlockYAt(loc));
 			
 			CraftSkeleton skeleton = (CraftSkeleton)stagingWorld.spawnEntity(loc, EntityType.SKELETON);
 			skeleton.getHandle().setSkeletonType(1);
 			skeleton.getHandle().setEquipment(0, new net.minecraft.server.ItemStack(net.minecraft.server.Item.STONE_SWORD));
 			skeleton.getHandle().a(new NBTTagCompound()); // this triggers attack behaviour
 			
 			skeleton = (CraftSkeleton)stagingWorld.spawnEntity(loc, EntityType.SKELETON);
 			skeleton.getHandle().setSkeletonType(0);
 			skeleton.getHandle().setEquipment(0, new net.minecraft.server.ItemStack(net.minecraft.server.Item.BOW));
 			skeleton.getHandle().a(new NBTTagCompound()); // this triggers attack behaviour
 			
 			loc = new Location(stagingWorld, StagingWorldGenerator.spleefMinX + random.nextDouble() * 16, StagingWorldGenerator.spleefY+1, StagingWorldGenerator.spleefMinZ + random.nextDouble() * 16);
 			loc.setY(stagingWorld.getHighestBlockYAt(loc));
 			return;
 		}
 		
 		numMonstersAlive = monsterWaveNumber;
 		
 		if ( monsterWaveNumber % 10 == 0 )
 		{
 			loc = new Location(stagingWorld, StagingWorldGenerator.spleefMinX + random.nextDouble() * 16, StagingWorldGenerator.spleefY+1, StagingWorldGenerator.spleefMinZ + random.nextDouble() * 16);
 			loc.setY(stagingWorld.getHighestBlockYAt(loc));
 			numMonstersAlive++;
 			
 			// add a witch into the mix every 10th wave
 			stagingWorld.spawnEntity(loc, EntityType.WITCH);
 		}
 		
 		for ( int i=0; i<monsterWaveNumber; i++ )
 		{
 			loc = new Location(stagingWorld, StagingWorldGenerator.spleefMinX + random.nextDouble() * 16, StagingWorldGenerator.spleefY+1, StagingWorldGenerator.spleefMinZ + random.nextDouble() * 16);
 			loc.setY(stagingWorld.getHighestBlockYAt(loc));
 			
 			switch ( random.nextInt(4) )
 			{
 			case 0:
 				stagingWorld.spawnEntity(loc, EntityType.SPIDER);
 				break;
 			case 1:
 				stagingWorld.spawnEntity(loc, EntityType.ZOMBIE);
 				break;
 			case 2:
 				CraftSkeleton skeleton = (CraftSkeleton)stagingWorld.spawnEntity(loc, EntityType.SKELETON);
 				skeleton.getHandle().setSkeletonType(0);
 				skeleton.getHandle().setEquipment(0, new net.minecraft.server.ItemStack(net.minecraft.server.Item.BOW));
 				skeleton.getHandle().a(new NBTTagCompound()); // this triggers attack behaviour
 				
 				break;
 			case 3:
 				stagingWorld.spawnEntity(loc, EntityType.CREEPER);
 				break;
 			}
 		}
 		
 		ws.allowMonsters = false;
 	}
 	
 	
 	public void setupButtonClicked(int x, int z, Player player)
 	{
 		if ( z == StagingWorldGenerator.waitingButtonZ )
 		{
 			if ( x == StagingWorldGenerator.waitingSpleefButtonX )
 			{
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingSpleefButtonX+1, StagingWorldGenerator.buttonY, z).setData(StagingWorldGenerator.colorOptionOn);
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingMonsterButtonX-1, StagingWorldGenerator.buttonY, z).setData(StagingWorldGenerator.colorOptionOff);
 				arenaModeIsSpleef = true;
 			}
 			else if ( x == StagingWorldGenerator.waitingMonsterButtonX )
 			{
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingSpleefButtonX+1, StagingWorldGenerator.buttonY, z).setData(StagingWorldGenerator.colorOptionOff);
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingMonsterButtonX-1, StagingWorldGenerator.buttonY, z).setData(StagingWorldGenerator.colorOptionOn);
 				arenaModeIsSpleef = false;
 			}
 			
 			endMonsterArena();
 		}
 		else if ( z == StagingWorldGenerator.spleefPressurePlateZ )
 		{
 			PlayerInventory inv = player.getInventory();
 			inv.clear();
 			if ( arenaModeIsSpleef )
 				player.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
 			else
 				player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 			
 			if ( countPlayersInArena() == 0 )
 			{
 				rebuildArena();				
 				endMonsterArena();
 				if ( !arenaModeIsSpleef )
 					spawnMonsterWave();
 			}
 		}
 		else if ( x == StagingWorldGenerator.mainButtonX )
 		{
 			if ( z == StagingWorldGenerator.gameModeButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.GAME_MODE ? StagingWorldOption.NONE : StagingWorldOption.GAME_MODE);
 			else if ( z == StagingWorldGenerator.gameOptionButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.GAME_MODE_OPTION ? StagingWorldOption.NONE : StagingWorldOption.GAME_MODE_OPTION);
 			else if ( z == StagingWorldGenerator.worldOptionButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.WORLD_OPTION ? StagingWorldOption.NONE : StagingWorldOption.WORLD_OPTION);
 			else if ( z == StagingWorldGenerator.globalOptionButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.GLOBAL_OPTION ? StagingWorldOption.NONE : StagingWorldOption.GLOBAL_OPTION);
 			else if ( z == StagingWorldGenerator.monstersButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.MONSTERS ? StagingWorldOption.NONE : StagingWorldOption.MONSTERS);
 			else if ( z == StagingWorldGenerator.animalsButtonZ )
 				setCurrentOption(currentOption == StagingWorldOption.ANIMALS ? StagingWorldOption.NONE : StagingWorldOption.ANIMALS);
 		}
 		else if ( x == StagingWorldGenerator.optionButtonX )
 		{
 			int num = StagingWorldGenerator.getOptionNumFromZ(z);
 			
 			boolean[] newValues; Block b; Sign s;
 			switch ( currentOption )
 			{
 			case GAME_MODE:
 				// change mode
 				GameMode gameMode = GameMode.get(num);
 				if ( plugin.getGameMode() == gameMode )
 					return;
 				plugin.setGameMode(gameMode);
 				
 				// update block colors
 				newValues = new boolean[GameMode.gameModes.size()];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(newValues);
 				
 				// update game mode sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, StagingWorldGenerator.buttonY+1, StagingWorldGenerator.gameModeButtonZ-1);
 				s = (Sign)b.getState();
 				StagingWorldGenerator.fitTextOnSign(s, gameMode.getName());
 				s.update();
 				break;
 			case GAME_MODE_OPTION:
 				// toggle this option
 				plugin.getGameMode().toggleOption(num);
 				List<GameMode.Option> options = plugin.getGameMode().getOptions();
 				
 				// update block colors
 				newValues = new boolean[options.size()];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = options.get(i).isEnabled();
 				updateSetupOptionButtons(newValues);
 				break;
 			case WORLD_OPTION:
 				// change option
 				WorldOption option = WorldOption.get(num);
 				if ( plugin.getWorldOption() == option )
 					return;
 				plugin.setWorldOption(option);
 				
 				// update block colors
 				newValues = new boolean[WorldOption.options.size()];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(newValues);
 				
 				// update world option sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, StagingWorldGenerator.buttonY+1, StagingWorldGenerator.worldOptionButtonZ-1);
 				s = (Sign)b.getState();
 				StagingWorldGenerator.fitTextOnSign(s, option.getName());
 				s.update();
 				break;
 			case GLOBAL_OPTION:
 				if ( num == 0 )
 					plugin.toggleMonsterEggRecipes();
 				else if ( num == 1 )
 					plugin.toggleDispenserRecipe();
 				else if ( num == 2 )
 					plugin.toggleEnderEyeRecipe();
 
 				newValues = new boolean[] { plugin.monsterEggsEnabled, plugin.dispenserRecipeEnabled, plugin.enderEyeRecipeEnabled };
 				updateSetupOptionButtons(newValues);
 				break;
 			case MONSTERS:
 				plugin.monsterNumbers = num;
 				
 				// update block colors
 				newValues = new boolean[5];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(newValues);
 				
 				// update main sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, StagingWorldGenerator.buttonY+1, StagingWorldGenerator.monstersButtonZ-1);
 				s = (Sign)b.getState();
 				s.setLine(1, StagingWorldGenerator.padSignLeft(StagingWorldGenerator.getQuantityText(num)));
 				s.update();
 				break;
 			case ANIMALS:
 				plugin.animalNumbers = num;
 				
 				// update block colors
 				newValues = new boolean[5];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(newValues);
 				
 				// update main sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, StagingWorldGenerator.buttonY+1, StagingWorldGenerator.monstersButtonZ-1);
 				s = (Sign)b.getState();
 				s.setLine(3, StagingWorldGenerator.padSignLeft(StagingWorldGenerator.getQuantityText(num)));
 				s.update();
 				break;
 			}
 		}
 		else if ( z == StagingWorldGenerator.startButtonZ )
 		{
 			if ( x == StagingWorldGenerator.startButtonX )
 			{
 				if ( plugin.getOnlinePlayers().size() >= plugin.getGameMode().getMinPlayers() )
 				{
 					setCurrentOption(StagingWorldOption.NONE);
 					plugin.setGameState(GameState.worldGeneration);
 				}
 				else
 					plugin.setGameState(GameState.stagingWorldConfirm);
 			}
 			else if ( x == StagingWorldGenerator.overrideButtonX )
 			{
 				setCurrentOption(StagingWorldOption.NONE);
 				plugin.setGameState(GameState.worldGeneration);
 			}
 			else if ( x == StagingWorldGenerator.cancelButtonX )
 				plugin.setGameState(GameState.stagingWorldReady);
 		}
 	}
 	
 	private void rebuildArena()
 	{
 		for ( int x=StagingWorldGenerator.spleefMinX; x<=StagingWorldGenerator.spleefMaxX; x++ )
 			for ( int z=StagingWorldGenerator.spleefMinZ; z<=StagingWorldGenerator.spleefMaxZ; z++ )
 				stagingWorld.getBlockAt(x, StagingWorldGenerator.spleefY, z).setType(Material.DIRT);
 		
 		if ( !arenaModeIsSpleef )
 		{
 			int centerZ = (StagingWorldGenerator.spleefMinZ + StagingWorldGenerator.spleefMaxZ) / 2;
 			for ( int x=StagingWorldGenerator.spleefMinX + 3; x<=StagingWorldGenerator.spleefMaxX - 3; x++ )
 				for ( int y=StagingWorldGenerator.spleefY + 1; y < StagingWorldGenerator.spleefY + 3; y++ )
 				{
 					stagingWorld.getBlockAt(x, y, centerZ).setType(Material.DIRT);
 					stagingWorld.getBlockAt(x, y, centerZ + 1).setType(Material.DIRT);
 				}
 		}
 	}
 
 	public void showStartButtons(boolean confirm)
 	{
 		Block bStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block sStart = bStart.getRelative(BlockFace.UP);
 		Block backStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 		
 		Block bOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block sOverride = bOverride.getRelative(BlockFace.UP);
 		Block backOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 		
 		Block bCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block sCancel = bCancel.getRelative(BlockFace.UP);
 		Block backCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 		
 		Block sHighInfo = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY + 2, StagingWorldGenerator.startButtonZ);
 		
 		if ( confirm )
 		{
 			bStart.setType(Material.AIR);
 			sStart.setType(Material.AIR);
 			backStart.setData(StagingWorldGenerator.colorOptionOff);
 			
 			bOverride.setType(Material.STONE_BUTTON);
 			bOverride.setData((byte)0x3);
 			
 			bCancel.setType(Material.STONE_BUTTON);
 			bCancel.setData((byte)0x3);
 			
 			sOverride.setType(Material.WALL_SIGN);
 			sOverride.setData((byte)0x3);
 			Sign s = (Sign)sOverride.getState();
 			s.setLine(1, "Push to start");
 			s.setLine(2, "the game anyway");
 			s.update();
 
 			//sCancel.setData((byte)0x3); // because it still has the "data" value from the start button, which is different 
 			sCancel.setType(Material.WALL_SIGN);
 			sCancel.setData((byte)0x3);
 			s = (Sign)sCancel.getState();
 			s.setLine(1, "Push to cancel");
 			s.setLine(2, "and choose");
 			s.setLine(3, "something else");
 			s.update();
 			
 			sHighInfo.setType(Material.WALL_SIGN);
 			sHighInfo.setData((byte)0x3);
 			s = (Sign)sHighInfo.getState();
 			s.setLine(0, "This mode needs");
 			s.setLine(1, "at least " + plugin.getGameMode().getMinPlayers());
 			s.setLine(2, "players. You");
 			s.setLine(3, "only have " + plugin.getOnlinePlayers().size() + ".");
 			s.update();
 			
 			backOverride.setData(StagingWorldGenerator.colorOverrideButton);
 			backCancel.setData(StagingWorldGenerator.colorCancelButton);
 		}
 		else
 		{
 			bStart.setType(Material.STONE_BUTTON);
 			bStart.setData((byte)0x3);
 			
 			sStart.setType(Material.WALL_SIGN);
 			sStart.setData((byte)0x3);
 			Sign s = (Sign)sStart.getState();
 			s.setLine(1, "Push to");
 			s.setLine(2, "start the game");
 			s.update();
 			
 			backStart.setData(StagingWorldGenerator.colorStartButton);
 			backOverride.setData(StagingWorldGenerator.colorOptionOff);
 			backCancel.setData(StagingWorldGenerator.colorOptionOff);
 			
 			bOverride.setType(Material.AIR);
 			bCancel.setType(Material.AIR);
 			sOverride.setType(Material.AIR);
 			sCancel.setType(Material.AIR);
 			sHighInfo.setType(Material.AIR);
 		}
 	}
 	
 	public void showWaitForDeletion()
 	{
 		Block sign = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY + 1, StagingWorldGenerator.startButtonZ);
 			
 		sign.setType(Material.WALL_SIGN);
 		sign.setData((byte)0x3);
 		Sign s = (Sign)sign.getState();
 		s.setLine(0, "Please wait for");
 		s.setLine(1, "the last game's");
 		s.setLine(2, "worlds to be");
 		s.setLine(3, "deleted...");
 		s.update();
 		
 		// hide all the start buttons' stuff
 		Block bStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block backStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 		
 		Block bOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block sOverride = bOverride.getRelative(BlockFace.UP);
 		Block backOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 		
 		Block bCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.startButtonZ);
 		Block sCancel = bCancel.getRelative(BlockFace.UP);
 		Block backCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, StagingWorldGenerator.buttonY, StagingWorldGenerator.wallMinCorridorZ);
 
 		Block sHighInfo = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, StagingWorldGenerator.buttonY + 2, StagingWorldGenerator.startButtonZ);
 		
 		bStart.setType(Material.AIR);
 		bOverride.setType(Material.AIR);
 		bCancel.setType(Material.AIR);
 		sOverride.setType(Material.AIR);
 		sCancel.setType(Material.AIR);
 		sHighInfo.setType(Material.AIR);
 		
 		backStart.setData(StagingWorldGenerator.colorOptionOff);
 		backOverride.setData(StagingWorldGenerator.colorOptionOff);
 		backCancel.setData(StagingWorldGenerator.colorOptionOff);
 	}
 	
 	public void showWorldGenerationIndicator(float completion)
 	{
 		/*
 		int maxCompleteZ = (int)((StagingWorldGenerator.wallMinZ - StagingWorldGenerator.wallMaxZ) * completion) + StagingWorldGenerator.wallMaxZ;
 		if ( completion != 0f )
 			maxCompleteZ --;
 		
 		for ( int x=StagingWorldGenerator.wallMinX+1; x<StagingWorldGenerator.wallMaxX; x++ )
 			for ( int z=StagingWorldGenerator.wallMinZ; z<StagingWorldGenerator.wallMaxZ; z++ )
 			{				
 				Block b = stagingWorld.getBlockAt(x, StagingWorldGenerator.floorY-2, z);
 				if ( b != null )
 				{
 					if ( z < maxCompleteZ )
 					{
 						b.setType(Material.REDSTONE_TORCH_ON);
 						b.setData((byte)5);
 					}
 					else
 						b.setType(Material.AIR);
 				}
 			}*/
 	}
 	
 	public void removeWorldGenerationIndicator()
 	{
 		showWorldGenerationIndicator(0f);
 	}
 }

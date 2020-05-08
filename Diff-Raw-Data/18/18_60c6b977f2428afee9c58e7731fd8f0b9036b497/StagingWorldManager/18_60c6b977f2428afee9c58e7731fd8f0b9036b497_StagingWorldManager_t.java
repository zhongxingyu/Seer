 package com.ftwinston.Killer;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import com.ftwinston.Killer.Game.GameState;
 
 class StagingWorldManager
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
 		return new Location(stagingWorld, StagingWorldGenerator.startButtonX + random.nextDouble() * 3 - 1, StagingWorldGenerator.baseFloorY + 1, StagingWorldGenerator.gameModeButtonZ + 8.5 - random.nextDouble() * 1.5, 180, 0);
 	}
 
	private Location getGameSetupSpawnLocation(int gameNum)
 	{
		Location loc = getStagingWorldSpawnPoint();
		loc.setY(StagingWorldGenerator.getFloorY(gameNum) + 1);
		return loc;
 	}
 
 	public enum StagingWorldOption
 	{
 		NONE,
 		GAME_MODE,
 		GAME_MODE_CONFIG,
 		WORLD,
 		WORLD_CONFIG,
 		MONSTERS,
 		ANIMALS,
 		GLOBAL_OPTION,
 	}
 	
 	private StagingWorldOption currentOption = StagingWorldOption.NONE;
 	public void setCurrentOption(Game game, StagingWorldOption option)
 	{
 		if ( option == currentOption )
 			return;
 		
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		
 		// disable whatever's currently on
 		switch ( currentOption )
 		{
 		case GAME_MODE:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.gameModeButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case GAME_MODE_CONFIG:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.gameModeConfigButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case WORLD:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.worldButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case WORLD_CONFIG:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.worldConfigButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case MONSTERS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.monstersButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case ANIMALS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.animalsButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		case GLOBAL_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.globalOptionButtonZ).setData(StagingWorldGenerator.colorOptionOff);
 			break;
 		}
 		hideSetupOptionButtons(game);
 		
 		currentOption = option;
 		String[] labels;
 		boolean[] values;
 		Option[] options;
 		
 		// now set up the new option
 		switch ( currentOption )
 		{
 		case GAME_MODE:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.gameModeButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			labels = new String[GameMode.gameModes.size()];
 			values = new boolean[labels.length];
 			for ( int i=0; i<labels.length; i++ )
 			{
 				GameModePlugin mode = GameMode.gameModes.get(i); 
 				labels[i] = mode.getName();
 				values[i] = mode.getName().equals(game.getGameMode().getName());
 			}
 			showSetupOptionButtons(game, "Game mode:", true, labels, values);
 			break;
 		case GAME_MODE_CONFIG:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.gameModeConfigButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			options = game.getGameMode().getOptions();
 			labels = new String[options.length];
 			values = new boolean[options.length];
 			for ( int i=0; i<options.length; i++ )
 			{
 				labels[i] = options[i].getName();
 				values[i] = options[i].isEnabled();
 			}
 			showSetupOptionButtons(game, "Mode option:", false, labels, values);
 			break;
 		case WORLD:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.worldButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			labels = new String[WorldOption.worldOptions.size()];
 			values = new boolean[labels.length];
 			for ( int i=0; i<labels.length; i++ )
 			{
 				WorldOptionPlugin worldOption = WorldOption.worldOptions.get(i); 
 				labels[i] = worldOption.getName();
 				values[i] = worldOption.getName().equals(game.getWorldOption().getName());
 			}
 			showSetupOptionButtons(game, "World:", false, labels, values);
 			break;
 		case WORLD_CONFIG:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.worldConfigButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			
 			options = game.getWorldOption().getOptions();
 			labels = new String[options.length];
 			values = new boolean[options.length];
 			for ( int i=0; i<options.length; i++ )
 			{
 				labels[i] = options[i].getName();
 				values[i] = options[i].isEnabled();
 			}
 			showSetupOptionButtons(game, "World option:", false, labels, values);
 			break;
 		case MONSTERS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.monstersButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[5];
 			values = new boolean[5];
 			for ( int i=0; i<5; i++ )
 			{
 				labels[i] = StagingWorldGenerator.getQuantityText(i);
 				values[i] = i == game.monsterNumbers;
 			}
 			showSetupOptionButtons(game, "Monsters:", false, labels, values);
 			break;
 		case ANIMALS:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.animalsButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[5];
 			values = new boolean[5];
 			for ( int i=0; i<5; i++ )
 			{
 				labels[i] = StagingWorldGenerator.getQuantityText(i);
 				values[i] = i == game.animalNumbers;
 			}
 			showSetupOptionButtons(game, "Animals:", false, labels, values);
 			break;
 		case GLOBAL_OPTION:
 			stagingWorld.getBlockAt(StagingWorldGenerator.wallMinX, buttonY, StagingWorldGenerator.globalOptionButtonZ).setData(StagingWorldGenerator.colorOptionOn);
 			labels = new String[] { "Craftable monster eggs", "Easier dispenser recipe", "Eyes of ender find nether fortresses" };
 			values = new boolean[] { true, true, true, true };
 			showSetupOptionButtons(game, "Global option:", false, labels, values);
 			break;
 		}
 	}
 	
 	private void showSetupOptionButtons(Game game, String heading, boolean forGameMode, String[] labels, boolean[] values)
 	{
 		Block b;
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		for ( int i=0; i<labels.length; i++ )
 		{
 			int buttonZ = StagingWorldGenerator.getOptionButtonZ(i, forGameMode);
 			int maxZ = forGameMode ? buttonZ + 1: buttonZ;
 			
 			for ( int y=buttonY-1; y<=buttonY+1; y++ )
 				for ( int z=buttonZ; z<=maxZ; z++ )
 				{
 					b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, y, z);
 					b.setType(Material.WOOL);
 					b.setData(StagingWorldGenerator.signBackColor);
 				}
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, buttonY, buttonZ);
 			b.setData(values[i] ? StagingWorldGenerator.colorOptionOn : StagingWorldGenerator.colorOptionOff);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, buttonY, buttonZ);
 			b.setType(Material.STONE_BUTTON);
 			b.setData((byte)0x2);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, buttonY+1, buttonZ);
 			b.setType(Material.WALL_SIGN);
 			b.setData((byte)0x4);
 			Sign s = (Sign)b.getState();
 			s.setLine(0, heading);
 			
 			StagingWorldGenerator.fitTextOnSign(s, labels[i]);
 			s.update();
 			
 			if ( !forGameMode )
 				continue;
 			
 			// show game mode description signs
 			String[] descLines = GameMode.get(i).getSignDescription();
 			if ( descLines == null )
 				continue;
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, buttonY+1, maxZ);
 			StagingWorldGenerator.setupWallSign(b, (byte)0x4,
 				descLines.length > 0 ? descLines[0] : "",
 				descLines.length > 1 ? descLines[1] : "",
 				descLines.length > 2 ? descLines[2] : "",
 				descLines.length > 3 ? descLines[3] : "");
 				
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, buttonY, maxZ);
 			StagingWorldGenerator.setupWallSign(b, (byte)0x4,
 				descLines.length > 4 ? descLines[4] : "",
 				descLines.length > 5 ? descLines[5] : "",
 				descLines.length > 6 ? descLines[6] : "",
 				descLines.length > 7 ? descLines[7] : "");
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, buttonY-1, maxZ);
 			StagingWorldGenerator.setupWallSign(b, (byte)0x4,
 				descLines.length > 8 ? descLines[8] : "",
 				descLines.length > 9 ? descLines[9] : "",
 				descLines.length > 10 ? descLines[10] : "",
 				descLines.length > 11 ? descLines[11] : "");
 		}
 	}
 
 	private void hideSetupOptionButtons(Game game)
 	{
 		Block b;
 		int minZ = StagingWorldGenerator.getOptionButtonZ(0, false);
 		int maxZ = StagingWorldGenerator.getWallMaxZ()-1;
 		int floorY = StagingWorldGenerator.getFloorY(game.getNumber());
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		
 		for ( int z=minZ; z<=maxZ; z++ )
 			for ( int y=buttonY-1; y<=buttonY+1; y++ )
 			{
 				if ( z != StagingWorldGenerator.gameModeButtonZ + 5 || y != floorY + 1 )
 				{// don't remove the welcome sign
 					b = stagingWorld.getBlockAt(StagingWorldGenerator.optionButtonX, y, z);
 					b.setType(Material.AIR);
 				}
 			
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, y, z);
 				b.setType(Material.SMOOTH_BRICK);
 			}
 	}
 	
 	private void updateSetupOptionButtons(Game game, boolean[] values, boolean forGameMode)
 	{
 		Block b;
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		for ( int i=0; i<values.length; i++ )
 		{
 			int z = StagingWorldGenerator.getOptionButtonZ(i, forGameMode);
 			
 			b = stagingWorld.getBlockAt(StagingWorldGenerator.wallMaxX, buttonY, z);
 			b.setData(values[i] ? StagingWorldGenerator.colorOptionOn : StagingWorldGenerator.colorOptionOff);
 		}
 	}
 	
 	public void setupButtonClicked(Game game, int x, int z, Player player)
 	{
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		if ( z == StagingWorldGenerator.waitingButtonZ )
 		{
 			if ( x == StagingWorldGenerator.waitingSpleefButtonX )
 			{
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingSpleefButtonX+1, buttonY, z).setData(StagingWorldGenerator.colorOptionOn);
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingMonsterButtonX-1, buttonY, z).setData(StagingWorldGenerator.colorOptionOff);
 				plugin.arenaManager.monsterArenaModeEnabled = false;
 			}
 			else if ( x == StagingWorldGenerator.waitingMonsterButtonX )
 			{
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingSpleefButtonX+1, buttonY, z).setData(StagingWorldGenerator.colorOptionOff);
 				stagingWorld.getBlockAt(StagingWorldGenerator.waitingMonsterButtonX-1, buttonY, z).setData(StagingWorldGenerator.colorOptionOn);
 				plugin.arenaManager.monsterArenaModeEnabled = true;
 			}
 			
 			plugin.arenaManager.endMonsterArena();
 		}
 		else if ( x == StagingWorldGenerator.mainButtonX )
 		{
 			if ( z == StagingWorldGenerator.gameModeButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.GAME_MODE ? StagingWorldOption.NONE : StagingWorldOption.GAME_MODE);
 			else if ( z == StagingWorldGenerator.gameModeConfigButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.GAME_MODE_CONFIG ? StagingWorldOption.NONE : StagingWorldOption.GAME_MODE_CONFIG);
 			else if ( z == StagingWorldGenerator.worldButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.WORLD ? StagingWorldOption.NONE : StagingWorldOption.WORLD);
 			else if ( z == StagingWorldGenerator.worldConfigButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.WORLD_CONFIG ? StagingWorldOption.NONE : StagingWorldOption.WORLD_CONFIG);
 			else if ( z == StagingWorldGenerator.monstersButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.MONSTERS ? StagingWorldOption.NONE : StagingWorldOption.MONSTERS);
 			else if ( z == StagingWorldGenerator.animalsButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.ANIMALS ? StagingWorldOption.NONE : StagingWorldOption.ANIMALS);
 			else if ( z == StagingWorldGenerator.globalOptionButtonZ )
 				setCurrentOption(game, currentOption == StagingWorldOption.GLOBAL_OPTION ? StagingWorldOption.NONE : StagingWorldOption.GLOBAL_OPTION);
 		}
 		else if ( x == StagingWorldGenerator.optionButtonX )
 		{
 			int num = StagingWorldGenerator.getOptionNumFromZ(z, currentOption == StagingWorldOption.GAME_MODE);
 			
 			boolean[] newValues; Block b; Sign s; Option[] options;
 			switch ( currentOption )
 			{
 			case GAME_MODE:
 				// change mode
 				GameModePlugin gameMode = GameMode.get(num);
 				if ( game.getGameMode().getName().equals(gameMode.getName()) )
 					return;
 				game.setGameMode(gameMode);
 				
 				// update block colors
 				newValues = new boolean[GameMode.gameModes.size()];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(game, newValues, true);
 				
 				// update game mode sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, buttonY+1, StagingWorldGenerator.gameModeButtonZ-1);
 				s = (Sign)b.getState();
 				StagingWorldGenerator.fitTextOnSign(s, gameMode.getName());
 				s.update();
 				break;
 			case GAME_MODE_CONFIG:
 				// toggle this option
 				game.getGameMode().toggleOption(num);
 				options = game.getGameMode().getOptions();
 				
 				// update block colors
 				newValues = new boolean[options.length];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = options[i].isEnabled();
 				updateSetupOptionButtons(game, newValues, false);
 				break;
 			case WORLD:
 				// change option
 				WorldOptionPlugin worldOption = WorldOption.get(num);
 				if ( game.getWorldOption().getName().equals(worldOption.getName()) )
 					return;
 				game.setWorldOption(worldOption);
 				
 				// update block colors
 				newValues = new boolean[WorldOption.worldOptions.size()];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(game, newValues, false);
 				
 				// update world option sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, buttonY+1, StagingWorldGenerator.worldButtonZ-1);
 				s = (Sign)b.getState();
 				StagingWorldGenerator.fitTextOnSign(s, worldOption.getName());
 				s.update();
 				break;
 			case WORLD_CONFIG:
 				// toggle this option
 				game.getWorldOption().toggleOption(num);
 				options = game.getWorldOption().getOptions();
 				
 				// update block colors
 				newValues = new boolean[options.length];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = options[i].isEnabled();
 				updateSetupOptionButtons(game, newValues, false);
 				break;
 			case MONSTERS:
 				game.monsterNumbers = num;
 				
 				// update block colors
 				newValues = new boolean[5];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(game, newValues, false);
 				
 				// update main sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, buttonY+1, StagingWorldGenerator.monstersButtonZ-1);
 				s = (Sign)b.getState();
 				s.setLine(1, StagingWorldGenerator.padSignLeft(StagingWorldGenerator.getQuantityText(num)));
 				s.update();
 				break;
 			case ANIMALS:
 				game.animalNumbers = num;
 				
 				// update block colors
 				newValues = new boolean[5];
 				for ( int i=0; i<newValues.length; i++ )
 					newValues[i] = i == num;
 				updateSetupOptionButtons(game, newValues, false);
 				
 				// update main sign
 				b = stagingWorld.getBlockAt(StagingWorldGenerator.mainButtonX, buttonY+1, StagingWorldGenerator.monstersButtonZ-1);
 				s = (Sign)b.getState();
 				s.setLine(3, StagingWorldGenerator.padSignLeft(StagingWorldGenerator.getQuantityText(num)));
 				s.update();
 				break;
 			case GLOBAL_OPTION:
 				if ( num == 0 )
 					game.toggleMonsterEggRecipes();
 				else if ( num == 1 )
 					game.toggleDispenserRecipe();
 				else if ( num == 2 )
 					game.toggleEnderEyeRecipe();
 
 				newValues = new boolean[] { game.isMonsterEggRecipeEnabled(), game.isDispenserRecipeEnabled(), game.isEnderEyeRecipeEnabled() };
 				updateSetupOptionButtons(game, newValues, false);
 				break;
 			}
 		}
 		else if ( z == StagingWorldGenerator.startButtonZ )
 		{
 			if ( x == StagingWorldGenerator.startButtonX )
 			{
 				if ( game.getOnlinePlayers().size() >= game.getGameMode().getMinPlayers() )
 				{
 					setCurrentOption(game, StagingWorldOption.NONE);
 					game.setGameState(GameState.worldGeneration);
 				}
 				else
 					game.setGameState(GameState.stagingWorldConfirm);
 			}
 			else if ( x == StagingWorldGenerator.overrideButtonX )
 			{
 				setCurrentOption(game, StagingWorldOption.NONE);
 				game.setGameState(GameState.worldGeneration);
 			}
 			else if ( x == StagingWorldGenerator.cancelButtonX )
 				game.setGameState(GameState.stagingWorldSetup);
 		}
 	}
 	
 	public void playerInteracted(Game game, int x, int z, final Player player)
 	{
 		if ( z == StagingWorldGenerator.spleefPressurePlateZ )
 		{
 			plugin.arenaManager.pressurePlatePressed(player);
 		}
 		else if ( z == StagingWorldGenerator.getGamePortalZ() )
 		{
 			for ( int i=0; i<Settings.maxSimultaneousGames; i++ )
 				if ( x == StagingWorldGenerator.getGamePortalX(i) )
 				{
 
 					final Game game2 = plugin.games[i];
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,  new Runnable() {
 						@Override
 						public void run() {
 							if ( game2.getGameState().usesGameWorlds )
 							{
 								plugin.playerManager.resetPlayer(player);
 								plugin.playerManager.setAlive(player, !Settings.lateJoinersStartAsSpectator);
 								plugin.playerManager.teleport(player, game2.getGameMode().getSpawnLocation(player));
 							}
 							else
 								player.teleport(getGameSetupSpawnLocation(game2.getNumber()));
 						}
 					});
 					break;
 				}
 		}
 		else if ( z == StagingWorldGenerator.getWallMaxZ() + 1 && game != null && Settings.maxSimultaneousGames != 1 )
 		{// exit this game, back to the staging world spawn
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,  new Runnable() {
 				@Override
 				public void run() {
 					player.teleport(getStagingWorldSpawnPoint());
 				}
 			});
 		}
 		else if ( z == StagingWorldGenerator.exitPortalZ && !plugin.stagingWorldIsServerDefault )
 		{// exit killer minecraft altogether
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,  new Runnable() {
 				@Override
 				public void run() {
 					plugin.playerManager.movePlayerOutOfKillerGame(player);
 				}
 			});
 		}
 	}
 
 	public void showStartButtons(Game game, boolean confirm)
 	{
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		
 		Block bStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block sStart = bStart.getRelative(BlockFace.UP);
 		Block backStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 		
 		Block bOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block sOverride = bOverride.getRelative(BlockFace.UP);
 		Block backOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 		
 		Block bCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block sCancel = bCancel.getRelative(BlockFace.UP);
 		Block backCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 		
 		Block sHighInfo = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY + 2, StagingWorldGenerator.startButtonZ);
 		
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
 			s.setLine(1, "at least " + game.getGameMode().getMinPlayers());
 			s.setLine(2, "players. You");
 			s.setLine(3, "only have " + game.getOnlinePlayers().size() + ".");
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
 	
 	public void showWaitForDeletion(Game game)
 	{
 		int buttonY = StagingWorldGenerator.getButtonY(game.getNumber());
 		Block sign = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY + 1, StagingWorldGenerator.startButtonZ);
 			
 		sign.setType(Material.WALL_SIGN);
 		sign.setData((byte)0x3);
 		Sign s = (Sign)sign.getState();
 		s.setLine(0, "Please wait for");
 		s.setLine(1, "the last game's");
 		s.setLine(2, "worlds to be");
 		s.setLine(3, "deleted...");
 		s.update();
 		
 		// hide all the start buttons' stuff
 		Block bStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block backStart = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 		
 		Block bOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block sOverride = bOverride.getRelative(BlockFace.UP);
 		Block backOverride = stagingWorld.getBlockAt(StagingWorldGenerator.overrideButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 		
 		Block bCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, buttonY, StagingWorldGenerator.startButtonZ);
 		Block sCancel = bCancel.getRelative(BlockFace.UP);
 		Block backCancel = stagingWorld.getBlockAt(StagingWorldGenerator.cancelButtonX, buttonY, StagingWorldGenerator.wallMinZ);
 
 		Block sHighInfo = stagingWorld.getBlockAt(StagingWorldGenerator.startButtonX, buttonY + 2, StagingWorldGenerator.startButtonZ);
 		
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
 	
 	public void showWorldGenerationIndicator(Game game, float completion)
 	{
 		int minZ = StagingWorldGenerator.wallMinZ + 2, maxZ = StagingWorldGenerator.getWallMaxZ() - 2;
 		int maxCompleteZ = (int)((maxZ - minZ) * completion + 0.5f) + minZ;
 		if ( completion != 0f )
 			maxCompleteZ += 2;
 	
 		int x = StagingWorldGenerator.wallMaxX, y = StagingWorldGenerator.getButtonY(game.getNumber()) + 1;
 		for ( int z = minZ; z <= maxZ; z++ )
 		{
 			Block b = stagingWorld.getBlockAt(x, y, z);
 			b.setType(Material.WOOL);
 			if ( z < maxCompleteZ )
 				b.setData(StagingWorldGenerator.colorOptionOn);
 			else
 				b.setData(StagingWorldGenerator.colorOptionOff);
 		}
 	}
 	
 	public void removeWorldGenerationIndicator(Game game)
 	{
 		int minZ = StagingWorldGenerator.wallMinZ + 2, maxZ = StagingWorldGenerator.getWallMaxZ() - 2;
 		
 		int x = StagingWorldGenerator.wallMaxX, y = StagingWorldGenerator.getButtonY(game.getNumber()) + 1;
 		for ( int z = minZ; z <= maxZ; z++ )
 		{
 			Block b = stagingWorld.getBlockAt(x, y, z);
 			b.setType(Material.SMOOTH_BRICK);
 		}
 	}
 }

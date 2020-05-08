 package yuuki;
 
 import java.io.IOException;
 import java.util.Map;
 
 import yuuki.battle.Battle;
 import yuuki.battle.BattleRunner;
 import yuuki.entity.ActionFactory;
 import yuuki.entity.Character;
 import yuuki.entity.EntityFactory;
 import yuuki.entity.NonPlayerCharacter;
 import yuuki.entity.PlayerCharacter;
 import yuuki.file.ActionLoader;
 import yuuki.file.EntityLoader;
 import yuuki.file.ImageLoader;
 import yuuki.file.SoundLoader;
 import yuuki.file.TileLoader;
 import yuuki.file.WorldLoader;
 import yuuki.graphic.ImageFactory;
 import yuuki.ui.GraphicalInterface;
 import yuuki.ui.Interactable;
 import yuuki.ui.UiExecutor;
 import yuuki.util.Progressable;
 import yuuki.util.Progression;
 import yuuki.world.TileFactory;
 import yuuki.world.World;
 
 /**
  * The game engine for the Yuuki JRPG project. This class may be executed
  * directly to run Yuuki.
  */
 public class Engine implements Runnable, UiExecutor {
 	
 	/**
 	 * Handles querying of loading progress and updating of the loading bar.
 	 */
 	private static class LoadingBarUpdater implements Runnable {
 		private double lastPercent = 0.0;
 		private Progressable monitor;
 		private Interactable ui;
 		public LoadingBarUpdater(Progressable monitor, Interactable ui) {
 			this.monitor = monitor;
 			this.ui = ui;
 		}
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					Thread.sleep(10);
 					update();
 				}
 			} catch (InterruptedException e) {
 				update();
 				Thread.currentThread().interrupt();
 			}
 		}
 		private void update() {
 			double percent = monitor.getProgress();
 			double diff = Math.abs(lastPercent - percent);
 			if (diff > Progressable.PROGRESS_PRECISION) {
 				lastPercent = percent;
 				ui.updateLoadingProgress(percent);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Handles the execution of a world in its own thread.
 	 */
 	private class WorldRunner implements Runnable {
 		private volatile boolean paused = false;
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					while (paused) {
 						Thread.sleep(10);
 					}
 					advanceWorld();
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
 			}
 		}
 		public void setPaused(boolean paused) {
 			this.paused = paused;
 		}
 		
 	}
 	
 	/**
 	 * The location of the file containing the action definitions. The location
 	 * is relative to the package structure.
 	 */
 	public static final String ACTIONS_FILE = "actions.csv";
 	
 	/**
 	 * The path to definitions files.
 	 */
 	public static final String DEFINITIONS_PATH = "/yuuki/resource/data/";
 	
 	/**
 	 * The location of the file containing the monster definitions. The
 	 * location is relative to the package structure.
 	 */
 	public static final String ENTITIES_FILE = "monsters.csv";
 	
 	/**
 	 * The path to the image definitions file.
 	 */
 	public static final String IMAGE_FILE = "graphics.csv";
 	
 	/**
 	 * The path to image files.
 	 */
 	public static final String IMAGE_PATH = "/yuuki/resource/images/";
 	
 	/**
 	 * The path to land files.
 	 */
 	public static final String LAND_PATH = "/yuuki/resource/land/";
 	
 	/**
 	 * The location of the file containing the music definitions.
 	 */
 	public static final String MUSIC_FILE = "music.csv";
 	
 	/**
 	 * The location of music files.
 	 */
 	public static final String MUSIC_PATH = "/yuuki/resource/audio/bgm/";
 	
 	/**
 	 * The path to the sound effect definitions file.
 	 */
 	public static final String SOUND_EFFECT_FILE = "effects.csv";
 	
 	/**
 	 * The path to sound effect files.
 	 */
 	public static final String SOUND_EFFECT_PATH =
 			"/yuuki/resource/audio/sfx/";
 	
 	/**
 	 * The name of the tile definitions file.
 	 */
 	public static final String TILE_FILE = "tiles.csv";
 	
 	/**
 	 * The name of the world definitions file.
 	 */
 	public static final String WORLD_FILE = "world.csv";
 	
 	/**
 	 * Program execution hook. Creates a new instance of Engine and then runs
 	 * it.
 	 *
 	 * @param args Command line arguments. Not used.
 	 */
 	public static void main(String[] args) {
 		Engine game = new Engine();
 		game.run();
 	}
 	
 	/**
 	 * The current battle.
 	 */
 	private BattleRunner battleRunner;
 	
 	/**
 	 * Creates all entities.
 	 */
 	private EntityFactory entityMaker;
 	
 	/**
 	 * The main battle.
 	 */
 	private Battle mainBattle;
 	
 	/**
 	 * The options for the game.
 	 */
 	private Options options;
 	
 	/**
 	 * The player character.
 	 */
 	private PlayerCharacter player;
 	
 	/**
 	 * The user interface.
 	 */
 	private Interactable ui;
 	
 	/**
 	 * The world controller.
 	 */
 	private World world;
 	
 	/**
 	 * The thread running the world advancement.
 	 */
 	private WorldRunner worldRunner;
 	
 	/**
 	 * Creates a new Engine with a Swing-based GUI.
 	 */
 	public Engine() {
 		options = new Options();
 		ui = new GraphicalInterface(this, options);
 	}
 	
 	@Override
 	public void requestBattle(boolean isMain, Character[] t1, Character[] t2) {
 		Character[][] fighters = {t1, t2};
 		Battle battle = new Battle(fighters);
 		if (isMain) {
 			mainBattle = battle;
 			exitOverworldMode();
 			ui.switchToBattleScreen(fighters);
 		}
 	}
 	
 	@Override
 	public void requestBattleEnd() {
 		Character winner = mainBattle.getFighters(0).get(0);
 		ui.getChoice(winner.getName() + " won", new String[]{"Continue"});
 		enterOverworldMode();
 		ui.display(null, "Your health has been restored.", false);
 		player.restoreHP();
 		player.restoreMP();
 	}
 	
 	@Override
 	public void requestBattleKill() {
 		ui.resetPrompt();
 		if (battleRunner != null) {
 			battleRunner.stop();
 		}
 	}
 	
 	@Override
 	public void requestBattlePause() {
 		if (battleRunner != null) {
 			battleRunner.setPaused(true);
 		}
 	}
 	
 	@Override
 	public void requestBattleResume() {
 		if (battleRunner != null) {
 			battleRunner.setPaused(false);
 		}
 	}
 	
 	@Override
 	public void requestBattleStart() {
 		spawnBattleThread(mainBattle, true);
 	}
 	
 	@Override
 	public void requestCharacterCreation(String name, int level) {
 		world = loadWorld(new Progression());
 		setInitialWorld();
 		player = entityMaker.createPlayer(name, level, ui);
 		player.setLocation(world.getPlayerStart());
 		world.addResident(player);
 		enterOverworldMode();
 	}
 	
 	@Override
 	public void requestCloseGame() {
 		exitOverworldMode();
 		ui.switchToIntroScreen();
 	}
 	
 	@Override
 	public void requestLoadGame() {
 		ui.display(null, "Loading hasn't yet been implemented", false);
 	}
 	
 	@Override
 	public void requestNewGame() {
 		exitOverworldMode();
 		ui.switchToCharacterCreationScreen();
 	}
 	
 	@Override
 	public void requestOptionApplication() {
 		applyOptions();
 	}
 	
 	@Override
 	public void requestOptionsScreen() {
 		ui.switchToOptionsScreen();
 	}
 	
 	@Override
 	public void requestOptionsSubmission() {
 		// TODO: do not depend on Interactable to set options
 		ui.switchToLastScreen();
 	}
 	
 	@Override
 	public void requestQuit() {
 		boolean quit = ui.showConfirmDialog("Are you sure you want to quit?");
 		if (quit) {
 			ui.destroy();
 			System.exit(0);
 		}
 	}
 	
 	@Override
 	public void requestSaveGame() {
 		ui.display(null, "Saving hasn't yet been implemented", false);
 	}
 	
 	@Override
 	public void run() {
 		ui.initialize();
 		ui.switchToLoadingScreen();
 		loadAssets();
 		applyOptions();
 		try {
 			ui.playMusicAndWait("BGM_MAIN_MENU");
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 		ui.switchToIntroScreen();
 	}
 	
 	/**
 	 * Advances the world by one tick and updates the GUI with the new world
 	 * data.
 	 */
 	private void advanceWorld() {
 		world.advance();
 		yuuki.world.Movable bumped = world.getLastBump(player);
 		if (bumped != null) {
 			class Runner implements Runnable {
 				private Character[] t2;
 				public Runner(Character[] t2) {
 					this.t2 = t2;
 				}
 				@Override
 				public void run() {
 					Character[] t1 = {player};
 					requestBattle(true, t1, t2);
 				}
 			};
 			Character[] npcs = {(NonPlayerCharacter) bumped};
 			Runner r = new Runner(npcs);
 			(new Thread(r, "WorldCommunication")).start();
 		}
 		ui.updateWorldView(player.getLocation());
 	}
 	
 	/**
 	 * Applies each of the options in the game options object to obtain their
 	 * respective effects.
 	 */
 	private void applyOptions() {
 		ui.applyOptions(options);
 	}
 	
 	/**
 	 * Switches to the overworld screen and begins overworld advancement.
 	 */
 	private void enterOverworldMode() {
		ui.switchToOverworldScreen();
 		startWorldThread();
 	}
 	
 	/**
 	 * Sets the world advancer to stop clicking through the world.
 	 */
 	private void exitOverworldMode() {
 		if (worldRunner != null) {
 			pauseWorldThread();
 		}
 	}
 	
 	/**
 	 * Loads the action definitions from disk.
 	 * 
 	 * @param monitor Monitors the progress of the load.
 	 * 
 	 * @return An ActionFactory containing the loaded definitions.
 	 */
 	private ActionFactory loadActionDefinitions(Progressable monitor) {
 		ActionFactory factory = null;
 		ActionLoader loader = new ActionLoader(DEFINITIONS_PATH);
 		loader.setProgressMonitor(monitor);
 		try {
 			factory = loader.load(ACTIONS_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load action definitions!");
 		}
 		monitor.finishProgress();
 		return factory;
 	}
 	
 	/**
 	 * Loads all game assets and updates the loading screen as they are loaded.
 	 */
 	private void loadAssets() {
 		Progressable monitor = new Progression();
 		LoadingBarUpdater updater = new LoadingBarUpdater(monitor, ui);
 		Thread updateThread = new Thread(updater, "LoadingBarUpdater");
 		updateThread.start();
 		Progressable m;
 		m = monitor.getSubProgressable(0.25);
 		Map<String, byte[]> effectData	= loadSoundEffects(m);
 		m = monitor.getSubProgressable(0.25);
 		Map<String, byte[]> musicData	= loadMusic(m);
 		m = monitor.getSubProgressable(0.25);
 		ImageFactory imageFactory		= loadImages(m);
 		m = monitor.getSubProgressable(0.25);
 		entityMaker						= loadEntities(m);
 		monitor.finishProgress();
 		updateThread.interrupt();
 		ui.initializeSounds(effectData, musicData);
 		ui.initializeImages(imageFactory);
 	}
 	
 	/**
 	 * Loads the entities from disk.
 	 * 
 	 * @param monitor Monitors loading progress.
 	 * 
 	 * @return The loaded EntityFactory.
 	 */
 	private EntityFactory loadEntities(Progressable monitor) {
 		Progressable m;
 		m = monitor.getSubProgressable(0.5);
 		ActionFactory factory = loadActionDefinitions(m);
 		m = monitor.getSubProgressable(0.5);
 		EntityFactory entityMaker = loadEntityDefinitions(factory, m);
 		monitor.finishProgress();
 		return entityMaker;
 	}
 	
 	/**
 	 * Loads the entity definitions from disk.
 	 * 
 	 * @param af The ActionFactory for generating Actions used by the loaded
 	 * entities.
 	 * 
 	 * @param monitor Monitors the progress of the load.
 	 * 
 	 * @return An EntityFactory containing the loaded entity definitions.
 	 */
 	private EntityFactory loadEntityDefinitions(ActionFactory af,
 			Progressable monitor) {
 		EntityFactory factory = null;
 		EntityLoader loader = new EntityLoader(DEFINITIONS_PATH, af);
 		loader.setProgressMonitor(monitor);
 		try {
 			factory = loader.load(ENTITIES_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load entity definitions!");
 		}
 		monitor.finishProgress();
 		return factory;
 	}
 	
 	/**
 	 * Loads the images from disk.
 	 * 
 	 * @param monitor Monitors loading progress.
 	 * 
 	 * @return The ImageFactory with the loaded images.
 	 */
 	private ImageFactory loadImages(Progressable monitor) {
 		ImageLoader loader = new ImageLoader(DEFINITIONS_PATH, IMAGE_PATH);
 		ImageFactory factory = null;
 		loader.setProgressMonitor(monitor);
 		try {
 			factory = loader.load(IMAGE_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load image file!");
 		}
 		monitor.finishProgress();
 		return factory;
 	}
 	
 	/**
 	 * Loads the background music from disk.
 	 * 
 	 * @param monitor Monitors loading progress.
 	 * 
 	 * @return A map that contains the background music data mapped to a sound
 	 * index.
 	 */
 	private Map<String, byte[]> loadMusic(Progressable monitor) {
 		SoundLoader loader = new SoundLoader(DEFINITIONS_PATH, MUSIC_PATH);
 		Map<String, byte[]> soundData = null;
 		loader.setProgressMonitor(monitor);
 		try {
 			soundData = loader.load(MUSIC_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load music!");
 		}
 		monitor.finishProgress();
 		return soundData;
 	}
 	
 	/**
 	 * Loads the sound effects from disk.
 	 * 
 	 * @param monitor Monitors loading progress.
 	 * 
 	 * @return A map that contains the sound effect data mapped to a sound
 	 * index.
 	 */
 	private Map<String, byte[]> loadSoundEffects(Progressable monitor) {
 		SoundLoader loader = new SoundLoader(DEFINITIONS_PATH,
 				SOUND_EFFECT_PATH);
 		Map<String, byte[]> soundData = null;
 		loader.setProgressMonitor(monitor);
 		try {
 			soundData = loader.load(SOUND_EFFECT_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load sound effects!");
 		}
 		monitor.finishProgress();
 		return soundData;
 	}
 	
 	/**
 	 * Loads the tile definitions file from disk.
 	 * 
 	 * @param monitor Monitors the loading progress.
 	 * 
 	 * @return The TileFactory containing the tile definitions.
 	 */
 	private TileFactory loadTileDefinitions(Progressable monitor) {
 		TileFactory factory = null;
 		TileLoader loader = new TileLoader(DEFINITIONS_PATH);
 		loader.setProgressMonitor(monitor);
 		try {
 			factory = loader.load(TILE_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load tile file!");
 		}
 		monitor.finishProgress();
 		return factory;
 	}
 	
 	/**
 	 * Loads the world from disk.
 	 * 
 	 * @param monitor The monitor for this loading.
 	 * 
 	 * @return The loaded World.
 	 */
 	private World loadWorld(Progressable monitor) {
 		Progressable m;
 		m = monitor.getSubProgressable(0.5);
 		TileFactory factory = loadTileDefinitions(m);
 		m = monitor.getSubProgressable(0.5);
 		World world = loadWorldDefinitions(factory, entityMaker, m);
 		monitor.finishProgress();
 		return world;
 	}
 	
 	/**
 	 * Loads the world definitions and all land files within it into a new
 	 * World object.
 	 * 
 	 * @param tf The factory to use for generating the tiles within the Lands
 	 * contained within the World.
 	 * @param ef The factory to use for generating the entities within the
 	 * Lands contained within the World.
 	 * @param monitor Monitors the progress of the load.
 	 * 
 	 * @return The World as read from the data files.
 	 */
 	private World loadWorldDefinitions(TileFactory tf, EntityFactory ef,
 			Progressable monitor) {
 		World w = null;
 		WorldLoader loader;
 		loader = new WorldLoader(DEFINITIONS_PATH, LAND_PATH, tf, ef);
 		loader.setProgressMonitor(monitor);
 		try {
 			w = loader.load(WORLD_FILE);
 		} catch (IOException e) {
 			System.err.println("Could not load world file!");
 		}
 		monitor.finishProgress();
 		return w;
 	}
 	
 	
 	
 	/**
 	 * Pauses the thread running the world.
 	 */
 	private void pauseWorldThread() {
 		worldRunner.setPaused(true);
 	}
 	
 	/**
 	 * Sets the world to use the initial land.
 	 */
 	private void setInitialWorld() {
 		String[] lands = world.getAllLandNames();
 		world.changeLand(lands[0]);
 	}
 	
 	/**
 	 * Spawns a thread that runs a battle to completion.
 	 * 
 	 * @param battle The battle to run through.
 	 * @param display Whether the battle should be displayed on the GUI.
 	 */
 	private void spawnBattleThread(Battle battle, boolean display) {
 		Interactable i = ((display) ? ui : null);
 		BattleRunner r = new BattleRunner(battle, i, this);
 		Thread t = new Thread(r);
 		if (display) {
 			battleRunner = r;
 			t.setName("MainBattle");
 		} else {
 			t.setName("Battle");
 		}
 		t.start();
 	}
 	
 	/**
 	 * Starts the thread running the world. If the thread has not yet been
 	 * created, it is created.
 	 */
 	private void startWorldThread() {
 		if (worldRunner == null) {
 			worldRunner = new WorldRunner();
 			(new Thread(worldRunner, "World")).start();
 		}
 		worldRunner.setPaused(false);
 		ui.clearWorldLocatables();
 		ui.setWorldView(world.getTiles());
 		ui.addWorldPortals(world.getPortals());
 		ui.addWorldEntities(world.getResidents());
 		ui.updateWorldView(player.getLocation());
 	}
 	
 }

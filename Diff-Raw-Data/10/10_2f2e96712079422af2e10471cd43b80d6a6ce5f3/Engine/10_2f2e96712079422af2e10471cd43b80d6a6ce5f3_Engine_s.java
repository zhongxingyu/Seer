 package yuuki;
 
 import java.io.File;
 import java.io.IOException;
 
 import yuuki.battle.Battle;
 import yuuki.battle.BattleRunner;
 import yuuki.content.ContentManager;
 import yuuki.content.ContentPack;
 import yuuki.entity.Character;
 import yuuki.entity.EntityFactory;
 import yuuki.entity.NonPlayerCharacter;
 import yuuki.entity.PlayerCharacter;
 import yuuki.file.ResourceNotFoundException;
 import yuuki.ui.DialogHandler;
 import yuuki.ui.GraphicalInterface;
 import yuuki.ui.Interactable;
 import yuuki.ui.UiExecutor;
 import yuuki.util.InvalidIndexException;
 import yuuki.util.Progressable;
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
 		private String lastText = "Loading...";
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
 			String text = monitor.getText();
 			double diff = Math.abs(lastPercent - percent);
 			if (diff > Progressable.PROGRESS_PRECISION ||
 					!lastText.equals(text)) {
 				lastPercent = percent;
 				lastText = text;
 				ui.updateLoadingProgress(percent, text);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Handles the execution of a world in its own thread.
 	 */
 	private class WorldRunner implements Runnable {
 		private volatile boolean paused = false;
 		private Thread worldThread = null;
 		public boolean isRunning() {
 			return (worldThread != null);
 		}
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					while (paused) {
 						Thread.sleep(10);
 					}
 					advanceWorld();
 				}
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
 		public void setPaused(boolean paused) {
 			this.paused = paused;
 		}
 		public void start() {
 			setPaused(false);
 			worldThread = new Thread(this, "World");
 			worldThread.start();
 		}
 		public void stop() {
 			worldThread.interrupt();
 			worldThread = null;
 		}
 	}
 	
 	/**
 	 * The path to the mods directory.
 	 */
 	private static final String PATH_MODS = "./mods";
 	
 	/**
 	 * Program execution hook. Creates a new instance of Engine and then runs
 	 * it.
 	 *
 	 * @param args Command line arguments. Not used.
 	 */
 	public static void main(String[] args) {
 		Engine game = null;
 		try {
 			game = new Engine();
 		} catch (IOException e) {
 			DialogHandler.showFatalError(e);
 		}
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
 	 * Loads all resources.
 	 */
 	private ContentManager resourceManager;
 	
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
 	 * 
 	 * @throws ResourceNotFoundException If the resource manifest file could
 	 * not be found.
 	 * @throws IOException If an I/O error occurs while reading the resource
 	 * manifest file.
 	 */
 	public Engine() throws ResourceNotFoundException, IOException {
 		resourceManager = new ContentManager();
 		resourceManager.scanBuiltIn();
 		options = new Options();
 		ui = new GraphicalInterface(this, options);
 		worldRunner = new WorldRunner();
 	}
 	
 	@Override
 	public void requestBattle(boolean isMain, Character[] t1, Character[] t2) {
 		Character[][] fighters = {t1, t2};
 		Battle battle = new Battle(fighters);
 		if (isMain) {
 			mainBattle = battle;
 			worldRunner.setPaused(true);
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
 		player = entityMaker.createPlayer(name, level, ui);
 		(new Thread(new Runnable() {
 			@Override
 			public void run() {
 				ui.updateLoadingProgress(0.0, "Loading...");
 				Progressable m = resourceManager.startWorldLoadMonitor(
 						ContentPack.BUILT_IN_NAME);
 				ui.switchToLoadingScreen();
 				Thread updateThread = getLoadUpdater(m);
 				updateThread.start();
 				try {
					resourceManager.loadEnabledWorlds();
 				} catch (IOException e) {
 					DialogHandler.showFatalError(e);
 				}
 				updateThread.interrupt();
 				world = resourceManager.getWorldEngine();
 				setInitialWorld();
 				player.setLocation(world.getPlayerStart());
 				world.addResident(player);
 				enterOverworldMode();
 			}
 		}, "WorldLoadingThread")).start();
 	}
 	
 	@Override
 	public void requestCloseGame() {
 		requestBattleKill();
 		if (worldRunner.isRunning()) {
 			worldRunner.stop();
 		}
 		ui.switchToIntroScreen();
 	}
 	
 	@Override
 	public void requestLoadGame() {
 		ui.display(null, "Loading hasn't yet been implemented", false);
 	}
 	
 	@Override
 	public void requestNewGame() {
 		requestBattleKill();
 		if (worldRunner.isRunning()) {
 			worldRunner.stop();
 		}
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
 		try {
 			ui.initialize();
 			ui.switchToLoadingScreen();
 			scanMods();
 			loadAssets();
 			applyOptions();
 			try {
 				ui.playMusicAndWait("BGM_MAIN_MENU");
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 			ui.switchToIntroScreen();
 		} catch (RuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			DialogHandler.showFatalError(e);
 		}
 	}
 	
 	/**
 	 * Advances the world by one tick and updates the GUI with the new world
 	 * data.
 	 * 
 	 * @throws InterruptedException If the current thread is interrupted while
 	 * waiting for the player to select a move.
 	 */
 	private void advanceWorld() throws InterruptedException {
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
 		if (worldRunner.isRunning()) {
 			worldRunner.setPaused(false);
 		} else {
 			worldRunner.start();
 		}
 		updateWorldViewer();
 		ui.switchToOverworldScreen();
 	}
 	
 	/**
 	 * Creates a Thread containing a LoadingBarUpdater for use with the loading
 	 * screen.
 	 * 
 	 * @param p The {@link yuuki.util.Progressable} to use for monitoring
 	 * progress.
 	 * 
 	 * @return The thread containing the LoadingBarUpdater.
 	 */
 	private Thread getLoadUpdater(Progressable p) {
 		p.setText(("Loading..."));
 		LoadingBarUpdater updater = new LoadingBarUpdater(p, ui);
 		Thread updateThread = new Thread(updater, "LoadingBarUpdater");
 		return updateThread;
 	}
 	
 	/**
 	 * Loads all game assets and updates the loading screen as they are loaded.
 	 * 
 	 * @throws ResourceNotFoundException If a resource in the built-in content
 	 * could not be found.
 	 * @throws IOException If an IOException occurs while loading the built-in
 	 * content.
 	 */
 	private void loadAssets() throws ResourceNotFoundException, IOException {
 		Progressable m;
 		m = resourceManager.startAssetLoadMonitor(ContentPack.BUILT_IN_NAME);
 		Thread updateThread = getLoadUpdater(m);
 		updateThread.start();
 		resourceManager.loadAssets(ContentPack.BUILT_IN_NAME);
 		updateThread.interrupt();
 		resourceManager.enable(ContentPack.BUILT_IN_NAME);
 		entityMaker = resourceManager.getEntityFactory();
 		ui.initializeSounds(resourceManager.getSoundEngine());
 		ui.initializeImages(resourceManager.getImageFactory());
 	}
 	
 	/**
 	 * Scans a folder called 'mods' at the same location as the root and loads
 	 * any valid mods found.
 	 */
 	private void scanMods() {
 		File modFolder = new File(PATH_MODS);
 		if (modFolder.isDirectory()) {
 			File[] contentDirs = modFolder.listFiles();
 			for (File mod : contentDirs) {
 				try {
 					resourceManager.scan(mod.getName(), mod);
 				} catch (IOException e) {
 					DialogHandler.showError(e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sets the world to use the initial land.
 	 */
 	private void setInitialWorld() {
 		String[] lands = world.getAllLandNames();
 		try {
 			world.changeLand(lands[0]);
 		} catch (InvalidIndexException e) {
 			// should never happen
 			DialogHandler.showFatalError(e);
 		}
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
 	private void updateWorldViewer() {
 		ui.clearWorldLocatables();
 		ui.setWorldView(world.getTiles());
 		ui.addWorldPortals(world.getPortals());
 		ui.addWorldEntities(world.getResidents());
 		ui.updateWorldView(player.getLocation());
 	}
 	
 }

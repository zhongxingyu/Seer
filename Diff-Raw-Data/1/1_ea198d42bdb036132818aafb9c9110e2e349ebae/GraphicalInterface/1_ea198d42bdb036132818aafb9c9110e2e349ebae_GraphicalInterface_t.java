 package yuuki.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import yuuki.Options;
 import yuuki.action.Action;
 import yuuki.animation.engine.AnimationManager;
 import yuuki.buff.Buff;
 import yuuki.entity.Character;
 import yuuki.entity.PlayerCharacter.Orientation;
 import yuuki.entity.Stat;
 import yuuki.graphic.ImageComponent;
 import yuuki.graphic.ImageFactory;
 import yuuki.item.InventoryPouch;
 import yuuki.item.Item;
 import yuuki.sound.DualSoundEngine;
 import yuuki.ui.menu.FileMenu;
 import yuuki.ui.menu.MenuBar;
 import yuuki.ui.menu.MenuBarListener;
 import yuuki.ui.screen.BattleScreen;
 import yuuki.ui.screen.CharacterCreationScreen;
 import yuuki.ui.screen.CharacterCreationScreenListener;
 import yuuki.ui.screen.IntroScreen;
 import yuuki.ui.screen.IntroScreenListener;
 import yuuki.ui.screen.InvenScreenListener;
 import yuuki.ui.screen.InventoryScreen;
 import yuuki.ui.screen.LoadingScreen;
 import yuuki.ui.screen.OptionsScreen;
 import yuuki.ui.screen.OptionsScreenListener;
 import yuuki.ui.screen.OverworldMovementListener;
 import yuuki.ui.screen.OverworldScreen;
 import yuuki.ui.screen.OverworldScreenListener;
 import yuuki.ui.screen.Screen;
 import yuuki.util.Grid;
 import yuuki.util.InvalidIndexException;
 import yuuki.world.Locatable;
 import yuuki.world.Movable;
 import yuuki.world.Portal;
 import yuuki.world.Tile;
 import yuuki.world.WalkGraph;
 
 /**
  * A graphical user interface that uses the Swing framework.
  */
 public class GraphicalInterface implements Interactable, IntroScreenListener,
 CharacterCreationScreenListener, OptionsScreenListener, MenuBarListener,
 OverworldScreenListener, InvenScreenListener {
 	
 	/**
 	 * The speed of game animation.
 	 */
 	public static final int ANIMATION_FPS = 30;
 	
 	/**
 	 * The height of the message box within the window.
 	 */
 	public static final int MESSAGE_BOX_HEIGHT = 100;
 	
 	/**
 	 * The amount of time to show a message on the screen for.
 	 */
 	public static final int MESSAGE_DISPLAY_TIME = 5000;
 	
 	/**
 	 * The delay, in milliseconds, between each letter printed on the message
 	 * box.
 	 */
 	public static final int MESSAGE_LETTER_DELAY = 100;
 	
 	/**
 	 * The height of the game window.
 	 */
 	public static final int WINDOW_HEIGHT = 600;
 	
 	/**
 	 * The width of the game window.
 	 */
 	public static final int WINDOW_WIDTH = 800;
 	
 	/**
 	 * Invokes the given Runnable later if not on the EDT. If currently on the
 	 * EDT, executes the Runnable immediately on the current thread.
 	 * 
 	 * @param doRun The Runnable to be executed.
 	 */
 	private static void invokeLaterIfNeeded(Runnable doRun) {
 		if (SwingUtilities.isEventDispatchThread()) {
 			doRun.run();
 		} else {
 			SwingUtilities.invokeLater(doRun);
 		}
 	}
 	
 	/**
 	 * The animation engine.
 	 */
 	private AnimationManager animationEngine;
 	
 	/**
 	 * The battle screen.
 	 */
 	private BattleScreen battleScreen;
 	/**
 	 * The screen where character creation is done.
 	 */
 	private CharacterCreationScreen charCreationScreen;
 	
 	/**
 	 * The content pane of the window.
 	 */
 	private ImageComponent contentPane;
 	
 	/**
 	 * The screen that the interface is currently on.
 	 */
 	private Screen<?> currentScreen;
 	
 	/**
 	 * The ending screen.
 	 */
 	private Screen<?> endingScreen;
 	
 	/**
 	 * The screen that the interface was previously on.
 	 */
 	private Screen<?> formerScreen;
 	
 	/**
 	 * Loads image files.
 	 */
 	private ImageFactory imageEngine;
 	
 	/**
 	 * The intro screen.
 	 */
 	private IntroScreen introScreen;
 	
 	/**
 	 * The screen that shows inventory and stats.
 	 */
 	private InventoryScreen invenScreen;
 	
 	/**
 	 * The loading screen.
 	 */
 	private LoadingScreen loadingScreen;
 	
 	/**
 	 * The object performing the actual work.
 	 */
 	private UiExecutor mainProgram;
 	
 	/**
 	 * The main window of the program.
 	 */
 	private JFrame mainWindow;
 	
 	/**
 	 * The game's menu bar.
 	 */
 	private MenuBar menuBar;
 	
 	/**
 	 * The message box for the game.
 	 */
 	private MessageBox messageBox;
 	
 	/**
 	 * The options of the game.
 	 */
 	private Options options;
 	
 	/**
 	 * The options screen.
 	 */
 	private OptionsScreen optionsScreen;
 	
 	/**
 	 * The overworld screen.
 	 */
 	private OverworldScreen overworldScreen;
 	
 	/**
 	 * The pause screen.
 	 */
 	private Screen<?> pauseScreen;
 	
 	/**
 	 * Plays all sounds.
 	 */
 	private DualSoundEngine soundEngine;
 	
 	/**
 	 * Allocates a new GraphicalInterface. Its components are created.
 	 * 
 	 * @param mainProgram The class that executes requests made by the GUI.
 	 * @param options The options of the program.
 	 */
 	public GraphicalInterface(UiExecutor mainProgram, Options options) {
 		this.options = options;
 		this.mainProgram = mainProgram;
 		currentScreen = null;
 		formerScreen = null;
 		animationEngine = new AnimationManager(ANIMATION_FPS);
 		soundEngine = null;
 	}
 	
 	@Override
 	public void addMods(final String[] names, final String[] ids) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				optionsScreen.addMods(names, ids);
 			}
 		});
 	}
 	
 	@Override
 	public void addWorldEntities(ArrayList<Movable> characters) {
 		addWorldLocatables(characters, OverworldScreen.Z_INDEX_ENTITY_LAYER);
 	}
 	
 	@Override
 	public void addWorldItems(List<Item> items) {
 		addWorldLocatables(items, OverworldScreen.Z_INDEX_ITEM_LAYER);
 	}
 	
 	@Override
 	public void addWorldPortals(ArrayList<Portal> portals) {
 		addWorldLocatables(portals, OverworldScreen.Z_INDEX_PORTAL_LAYER);
 	}
 	
 	@Override
 	public void applyOptions(Options options) {
 		soundEngine.setEffectVolume(options.sfxVolume);
 		soundEngine.setMusicVolume(options.bgmVolume);
 	}
 	
 	@Override
 	public void bgmVolumeChanged(int volume) {
 		options.bgmVolume = volume;
 		mainProgram.requestOptionApplication();
 	}
 	
 	@Override
 	public void clearWorldLocatables() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				overworldScreen.clearWorldLocatables();
 			}
 		});
 	}
 	
 	@Override
 	public void closeInvenClicked() {
 		mainProgram.requestInventoryClose();
 	}
 	
 	@Override
 	public boolean confirm(String prompt, String yes, String no) {
 		String[] ops = {yes, no};
 		String choice = (String) getChoice(prompt, ops);
 		return (choice.equals(yes));
 	}
 	
 	@Override
 	public void createCharacterClicked() {
 		String name = charCreationScreen.getEnteredName();
 		int level = charCreationScreen.getEnteredLevel();
 		if (!name.equals("")) {
 			mainProgram.requestCharacterCreation(name, level);
 		} else {
 			showAlertDialog("You must enter a name!");
 		}
 	}
 	
 	@Override
 	public void destroy() {
 		mainWindow.dispose();
 	}
 	
 	@Override
 	public void display(Character speaker, String message, boolean animated) {
 		long letterTime = (animated) ? MESSAGE_LETTER_DELAY : 0;
 		messageBox.display(speaker, message, letterTime, MESSAGE_DISPLAY_TIME);
 	}
 	
 	@Override
 	public void dropItemClicked(Item item) {
 		mainProgram.requestItemDrop(item);
 		overworldScreen.redrawWorldViewport();
 	}
 	
 	@Override
 	public void exitClicked() {
 		mainProgram.requestQuit();
 	}
 	
 	@Override
 	public Object getChoice(Object[] options) {
 		return getChoice("Select an option", options);
 	}
 	
 	@Override
 	public Object getChoice(String prompt, Object[] options) {
 		class Runner implements Runnable, MessageBoxInputListener {
 			public Object value = null;
 			private Object[] options;
 			private String prompt;
 			public Runner(String prompt, Object[] options) {
 				this.options = options;
 				this.prompt = prompt;
 			}
 			@Override
 			public void enterClicked(String s) {}
 			@Override
 			public void optionClicked(Object option) {
 				value = option;
 				messageBox.removeListener(this);
 			}
 			@Override
 			public void run() {
 				messageBox.addListener(this);
 				messageBox.getChoice(prompt, options);
 			}
 		};
 		Runner r = new Runner(prompt, options);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.value == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			messageBox.removeListener(r);
 			messageBox.exitPrompt();
 			Thread.currentThread().interrupt();
 		}
 		return r.value;
 	}
 	
 	@Override
 	public double getDouble() {
 		return getDouble("Enter an integer");
 	}
 	
 	@Override
 	public double getDouble(double min, double max) {
 		return getDouble("Enter an integer", min, max);
 	}
 	
 	@Override
 	public double getDouble(String prompt) {
 		Double answer = null;
 		while (answer == null) {
 			answer = Double.parseDouble(getString(prompt));
 		}
 		return answer.doubleValue();
 	}
 	
 	@Override
 	public double getDouble(String prompt, double min, double max) {
 		double answer = 0;
 		boolean answerIsGood = false;
 		while (!answerIsGood) {
 			answer = getDouble(prompt);
 			if (answer >= min && answer <= max) {
 				answerIsGood = true;
 			}
 		}
 		return answer;
 	}
 	
 	@Override
 	public int getInt() {
 		return getInt("Enter an integer");
 	}
 	
 	@Override
 	public int getInt(int min, int max) {
 		return getInt("Enter an integer", min, max);
 	}
 	
 	@Override
 	public int getInt(String prompt) {
 		Integer answer = null;
 		while (answer == null) {
 			answer = Integer.parseInt(getString(prompt));
 		}
 		return answer.intValue();
 	}
 	
 	@Override
 	public int getInt(String prompt, int min, int max) {
 		int answer = 0;
 		boolean answerIsGood = false;
 		while (!answerIsGood) {
 			answer = getInt(prompt);
 			if (answer >= min && answer <= max) {
 				answerIsGood = true;
 			}
 		}
 		return answer;
 	}
 	
 	@Override
 	public void getItemClicked() {
 		mainProgram.requestGetItem();
 		overworldScreen.redrawWorldViewport();
 	}
 	
 	@Override
 	public String getString() {
 		return getString("Enter a value");
 	}
 	
 	@Override
 	public String getString(String prompt) {
 		class Runner implements MessageBoxInputListener, Runnable {
 			public String prompt;
 			public String value = null;
 			public Runner(String prompt) {
 				this.prompt = prompt;
 			}
 			@Override
 			public void enterClicked(String input) {
 				value = input;
 				messageBox.removeListener(this);
 			}
 			@Override
 			public void optionClicked(Object option) {}
 			@Override
 			public void run() {
 				messageBox.addListener(this);
 				messageBox.getString(prompt);
 			}
 		}
 		Runner r = new Runner(prompt);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.value == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			messageBox.removeListener(r);
 			messageBox.exitPrompt();
 			Thread.currentThread().interrupt();
 		}
 		return r.value;
 	}
 	
 	/**
 	 * Creates the components of the main JFrame and draws the main window on
 	 * screen. In order to prevent asynchronous access of child components
 	 * before they have finished constructing, this method blocks until
 	 * construction is complete.
 	 */
 	@Override
 	public void initialize() {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					createComponents();
 					refreshWindow();
 				}
 			});
 		} catch (Exception e) {
 			e.printStackTrace();
 			Thread.currentThread().interrupt();
 		}
 	}
 	
 	@Override
 	public void initializeImages(ImageFactory factory) {
 		this.imageEngine = factory;
 		overworldScreen.setImageFactory(factory);
 		invenScreen.setImageFactory(factory);
 	}
 	
 	@Override
 	public void initializeSounds(DualSoundEngine soundEngine) {
 		this.soundEngine = soundEngine;
 		introScreen.setSoundEngine(soundEngine);
 	}
 	
 	@Override
 	public void invenButtonClicked() {
 		mainProgram.requestInventoryOpen();
 	}
 	
 	@Override
 	public void loadGameClicked() {
 		mainProgram.requestLoadGame();
 	}
 	
 	@Override
 	public void menuItemTriggered(int menuId, int itemId) {
 		switch (menuId) {
 			case MenuBar.MENU_ID_FILE:
 				switch (itemId) {
 					case FileMenu.ITEM_ID_NEW:
 						mainProgram.requestNewGame();
 						break;
 						
 					case FileMenu.ITEM_ID_LOAD:
 						mainProgram.requestLoadGame();
 						break;
 						
 					case FileMenu.ITEM_ID_SAVE:
 						mainProgram.requestSaveGame();
 						break;
 						
 					case FileMenu.ITEM_ID_CLOSE:
 						mainProgram.requestCloseGame();
 						break;
 						
 					case FileMenu.ITEM_ID_OPTIONS:
 						mainProgram.requestOptionsScreen();
 						break;
 						
 					case FileMenu.ITEM_ID_EXIT:
 						mainProgram.requestQuit();
 						break;
 				}
 				break;
 		}
 	}
 	
 	@Override
 	public void modDisabled(String id) {
 		mainProgram.requestModDisable(id);
 	}
 	
 	@Override
 	public void modEnabled(String id) {
 		mainProgram.requestModEnable(id);
 	}
 	
 	@Override
 	public void newGameClicked() {
 		mainProgram.requestNewGame();
 	}
 	
 	@Override
 	public void optionsClicked() {
 		mainProgram.requestOptionsScreen();
 	}
 	
 	@Override
 	public void optionsSubmitted() {
 		mainProgram.requestOptionsSubmission();
 	}
 	
 	@Override
 	public void playMusic(String musicIndex) {
 		if (soundEngine != null) {
 			soundEngine.playMusic(musicIndex, false);
 		}
 	}
 	
 	@Override
 	public void playMusicAndWait(String musicIndex) throws
 	InterruptedException {
 		if (soundEngine != null) {
 			soundEngine.playMusicAndWait(musicIndex);
 		}
 	}
 	
 	@Override
 	public void playSound(String effectIndex) {
 		if (soundEngine != null) {
 			soundEngine.playEffect(effectIndex);
 		}
 	}
 	
 	@Override
 	public void removeWorldItems(Item[] items, int count) {
 		overworldScreen.removeWorldItems(items, count);
 	}
 	
 	@Override
 	public void resetPrompt() {
 		messageBox.exitPrompt();
 		messageBox.clear();
 	}
 	
 	@Override
 	public Action selectAction(Action[] actions) {
 		return (Action) getChoice("Select an action", actions);
 	}
 	
 	@Override
 	public Item selectItem(final Item[] choices) {
 		class Runner implements Runnable, InvenScreenListener {
 			public boolean exited = false;
 			public volatile Item item;
 			private InventoryScreen subInven;
 			public void run() {
 				subInven = new InventoryScreen(WINDOW_WIDTH, getScreenHeight());
				subInven.setImageFactory(imageEngine);
 				subInven.addListener(this);
 				for (Item item : choices) {
 					subInven.addItem(item);
 				}
 				switchToScreen(subInven);
 			}
 
 			@Override
 			public void closeInvenClicked() {
 				exitSubInven();
 			}
 
 			@Override
 			public void useItemClicked(Item item) {
 				exitSubInven();
 				messageBox.display(null, "You used '" + item.getName() +
 						"'...\nBut nothing happened because it isn't " +
 						"implemented yet.", 0, MESSAGE_DISPLAY_TIME);
 			}
 
 			@Override
 			public void dropItemClicked(Item item) {
 				// do nothing; can't drop item in battle!
 			}
 			
 			private void exitSubInven() {
 				subInven.removeListener(this);
 				switchToScreen(battleScreen);
 				exited = true;
 			}
 		}
 		Runner r = new Runner();
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.exited == false) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			switchToScreen(battleScreen);
 			Thread.currentThread().interrupt();
 		}
 		return r.item;
 	}
 	
 	@Override
 	public Point selectMove(WalkGraph graph, Orientation orientation) throws
 	InterruptedException {
 		class Runner implements Runnable, OverworldMovementListener {
 			public Point move = null;
 			private WalkGraph graph;
 			private Orientation orientation;
 			public Runner(WalkGraph graph, Orientation orientation) {
 				this.graph = graph;
 				this.orientation = orientation;
 			}
 			@Override
 			public void movementButtonClicked(Point moveLocation) {
 				overworldScreen.removeMovementListener(this);
 				this.move = moveLocation;
 			}
 			@Override
 			public void run() {
 				overworldScreen.setWalkGraph(graph);
 				overworldScreen.setWalkOrientation(orientation);
 				overworldScreen.refreshButtons();
 				overworldScreen.addMovementListener(this);
 			}
 			@Override
 			public void turnButtonClicked(Orientation orientation) {
 				mainProgram.requestPlayerTurn(orientation);
 				overworldScreen.setWalkOrientation(orientation);
 				overworldScreen.redrawWorldViewport();
 				overworldScreen.refreshButtons();
 			}
 		};
 		Runner r = new Runner(graph, orientation);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.move == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			overworldScreen.setWalkGraph(null);
 			overworldScreen.setWalkOrientation(null);
 			overworldScreen.refreshButtons();
 			overworldScreen.removeMovementListener(r);
 			throw e;
 		}
 		return r.move;
 	}
 	
 	@Override
 	public Character selectTarget(ArrayList<ArrayList<Character>> fighters) {
 		ArrayList<Character> chars = new ArrayList<Character>();
 		for (int i = 0; i < fighters.size(); i++) {
 			ArrayList<Character> team = fighters.get(i);
 			for (int j = 0; j < team.size(); j++) {
 				chars.add(team.get(j));
 			}
 		}
 		Character[] charsArr = chars.toArray(new Character[0]);
 		return (Character) getChoice("Select a target", charsArr);
 	}
 	
 	@Override
 	public void setLoadingIndeterminate(boolean ind) {
 		class Runner implements Runnable {
 			private boolean ind;
 			public Runner(boolean ind) {
 				this.ind = ind;
 			}
 			@Override
 			public void run() {
 				loadingScreen.setIndeterminate(ind);
 			}
 		}
 		SwingUtilities.invokeLater(new Runner(ind));
 	}
 	
 	@Override
 	public void setWorldView(Grid<Tile> view, String name) {
 		class Runner implements Runnable {
 			private String name;
 			private Grid<Tile> view;
 			public Runner(Grid<Tile> tg, String n) {
 				view = tg;
 				name = n;
 			}
 			@Override
 			public void run() {
 				overworldScreen.setWorldView(view, name);
 			}
 		}
 		Runner r = new Runner(view, name);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void sfxTestClicked() {
 		playSound("SFX_TEST");
 	}
 	
 	@Override
 	public void sfxVolumeChanged(int volume) {
 		options.sfxVolume = volume;
 		mainProgram.requestOptionApplication();
 	}
 	
 	@Override
 	public void showActionFailure(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionFailure(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showActionPreperation(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionPreparation(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showActionUse(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionUse(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showAlertDialog(String msg) {
 		JOptionPane.showMessageDialog(null, msg);
 	}
 	
 	@Override
 	public void showBuffActivation(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffActivation(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showBuffApplication(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffApplication(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showBuffDeactivation(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffDeactivation(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showCharacterRemoval(Character c) {
 		class Runner implements Runnable {
 			public Character c;
 			@Override
 			public void run() {
 				battleScreen.showCharacterRemoval(c);
 			}
 		}
 		Runner r = new Runner();
 		r.c = c;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void showCharacterVictory(Character[] cs) {
 		class Runner implements Runnable {
 			public Character[] cs;
 			@Override
 			public void run() {
 				battleScreen.showCharacterVictory(cs);
 			}
 		}
 		Runner r = new Runner();
 		r.cs = cs;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public boolean showConfirmDialog(String msg) {
 		int response = JOptionPane.showConfirmDialog(null, msg, "Confirmation",
 				JOptionPane.YES_NO_OPTION);
 		return (response == JOptionPane.YES_OPTION);
 	}
 	
 	@Override
 	public void showDamage(Character fighter, Stat stat, double damage) {
 		if (stat.getName().equalsIgnoreCase("health")) {
 			playSound("HIT");
 		}
 		battleScreen.showDamage(fighter, stat, damage);
 	}
 	
 	@Override
 	public void showDamage(Character fighter, Stat stat, int damage) {
 		if (stat.getName().equalsIgnoreCase("health")) {
 			playSound("HIT");
 		}
 		battleScreen.showDamage(fighter, stat, damage);
 	}
 	
 	@Override
 	public void showRecovery(Character fighter, Stat stat, double amount) {
 		battleScreen.showRecovery(fighter, stat, amount);
 	}
 	
 	@Override
 	public void showRecovery(Character fighter, Stat stat, int amount) {
 		battleScreen.showRecovery(fighter, stat, amount);
 	}
 	
 	@Override
 	public void showStatUpdate(Character fighter) {
 		class Runner implements Runnable {
 			public Character fighter;
 			@Override
 			public void run() {
 				battleScreen.showStatUpdate(fighter);
 			}
 		}
 		Runner r = new Runner();
 		r.fighter = fighter;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * Shows that a feature has not yet been implemented.
 	 */
 	public void showUnimpMsg() {
 		display(null, "That feature has not yet been implemented.", true);
 	}
 	
 	@Override
 	public void switchToBattleScreen(Character[][] fighters) {
 		class Runner implements Runnable {
 			public Character[][] fighters;
 			@Override
 			public void run() {
 				battleScreen.initBattle(fighters);
 				switchWindow(battleScreen);
 				battleScreen.showStart();
 				mainProgram.requestBattleStart();
 			}
 		}
 		Runner r = new Runner();
 		r.fighters = fighters;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void switchToCharacterCreationScreen() {
 		charCreationScreen.addListener(this);
 		switchWindow(charCreationScreen);
 	}
 	
 	@Override
 	public void switchToEndingScreen() {
 		switchWindow(endingScreen);
 	}
 	
 	@Override
 	public void switchToIntroScreen() {
 		introScreen.addListener(this);
 		switchWindow(introScreen);
 	}
 	
 	@Override
 	public void switchToInvenScreen() {
 		invenScreen.addListener(this);
 		switchWindow(invenScreen);
 	}
 	
 	@Override
 	public void switchToLastScreen() {
 		switchWindow(formerScreen);
 	}
 	
 	@Override
 	public void switchToLoadingScreen() {
 		switchWindow(loadingScreen);
 	}
 	
 	@Override
 	public void switchToOptionsScreen() {
 		optionsScreen.addListener(this);
 		optionsScreen.setValues(options);
 		switchWindow(optionsScreen);
 		mainProgram.requestBattlePause();
 		messageBox.freeze();
 	}
 	
 	@Override
 	public void switchToOverworldScreen() {
 		overworldScreen.addListener(this);
 		switchWindow(overworldScreen);
 	}
 	
 	@Override
 	public void switchToPauseScreen() {
 		switchWindow(pauseScreen);
 	}
 	
 	@Override
 	public void updateInventory(final InventoryPouch pouch) {
 		GraphicalInterface.invokeLaterIfNeeded(new Runnable() {
 			@Override
 			public void run() {
 				invenScreen.clearInventory();
 				invenScreen.addPouch(pouch);
 				invenScreen.repaint();
 			}
 		});
 	}
 	
 	@Override
 	public void updateLoadingProgress(double percent, String text) {
 		class Runner implements Runnable {
 			private double percent;
 			private String text;
 			public Runner(double percent, String text) {
 				this.percent = percent;
 				this.text = text;
 			}
 			@Override
 			public void run() {
 				loadingScreen.updateProgress(percent, text);
 			}
 		}
 		SwingUtilities.invokeLater(new Runner(percent, text));
 	}
 	
 	@Override
 	public void updateWorldView(Point center) {
 		class Runner implements Runnable {
 			private Point p;
 			public Runner(Point p) {
 				this.p = p;
 			}
 			@Override
 			public void run() {
 				overworldScreen.updateWorldViewport(p);
 			}
 		}
 		Runner r = new Runner(center);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void useItemClicked(Item item) {
 		mainProgram.requestItemUse(item);
 	}
 	
 	@Override
 	public void waitForDisplay() {
 		messageBox.waitForClean();
 	}
 	
 	/**
 	 * Adds a list of Locatables to one of the layers in the world viewer.
 	 * 
 	 * @param l The list of Locatables to add.
 	 * @param zIndex The Z-index of the layer to add the Locatables to.
 	 */
 	private void addWorldLocatables(List<? extends Locatable> l, int zIndex) {
 		ArrayList<Locatable> list = new ArrayList<Locatable>(l);
 		class Runner implements Runnable {
 			private ArrayList<Locatable> list;
 			private int zIndex;
 			public Runner(ArrayList<Locatable> list, int zIndex) {
 				this.zIndex = zIndex;
 				this.list = list;
 			}
 			@Override
 			public void run() {
 				overworldScreen.addWorldLocatables(list, zIndex);
 			}
 		}
 		Runner r = new Runner(list, zIndex);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * Clears the main window of all components.
 	 */
 	private void clearWindow() {
 		mainWindow.getContentPane().removeAll();
 	}
 	
 	/**
 	 * Creates the battle screen.
 	 */
 	private void createBattleScreen() {
 		int height = getScreenHeight();
 		battleScreen = new BattleScreen(WINDOW_WIDTH, height, animationEngine);
 		battleScreen.setBackgroundMusic("BGM_BATTLE");
 	}
 	
 	/**
 	 * Creates the screens used in this GUI.
 	 */
 	private void createComponents() {
 		createContentPane();
 		createMainWindow();
 		createMenuBar(); // must come before any screen creation
 		createLoadingScreen();
 		createMessageBox();
 		createIntroScreen();
 		createOptionsScreen();
 		createBattleScreen();
 		createOverworldScreen();
 		createPauseScreen();
 		createEndingScreen();
 		createPlayerCreationScreen();
 		createInventoryScreen();
 	}
 	
 	/**
 	 * Creates the main window's content pane.
 	 */
 	private void createContentPane() {
 		contentPane = new ImageComponent();
 	}
 	
 	/**
 	 * Creates the ending screen.
 	 */
 	private void createEndingScreen() {
 		int height = getScreenHeight();
 		endingScreen = Screen.getInstance(WINDOW_WIDTH, height);
 		endingScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the intro screen.
 	 */
 	private void createIntroScreen() {
 		int height = getScreenHeight();
 		introScreen = new IntroScreen(WINDOW_WIDTH, height);
 		introScreen.setBackgroundMusic("BGM_MAIN_MENU");
 		introScreen.setBackgroundImage("BG_INTRO_SCREEN");
 	}
 	
 	/**
 	 * Creates the inventory screen.
 	 */
 	private void createInventoryScreen() {
 		int height = getScreenHeight();
 		invenScreen = new InventoryScreen(WINDOW_WIDTH, height);
 	}
 	
 	/**
 	 * Creates the loading screen.
 	 */
 	private void createLoadingScreen() {
 		int height = getScreenHeight();
 		loadingScreen = new LoadingScreen(WINDOW_WIDTH, height,
 				animationEngine);
 		loadingScreen.setBackgroundMusic(null);
 		loadingScreen.setBackgroundImage(null);
 	}
 	
 	/**
 	 * Creates the primary window.
 	 */
 	private void createMainWindow() {
 		WindowListener l = new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				mainProgram.requestQuit();
 			}
 		};
 		Dimension windowSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
 		mainWindow = new JFrame("Yuuki - A JRPG");
 		mainWindow.setDefaultCloseOperation(
 				WindowConstants.DO_NOTHING_ON_CLOSE);
 		mainWindow.setResizable(false);
 		mainWindow.addWindowListener(l);
 		mainWindow.setContentPane(contentPane);
 		mainWindow.setPreferredSize(windowSize);
 		mainWindow.setSize(windowSize);
 		mainWindow.setLocationRelativeTo(null);
 	}
 	
 	/**
 	 * Creates the menu bar.
 	 */
 	private void createMenuBar() {
 		menuBar = new MenuBar();
 		menuBar.addListener(this);
 	}
 	
 	/**
 	 * Creates the message box.
 	 */
 	private void createMessageBox() {
 		AnimationManager a = animationEngine;
 		messageBox = new MessageBox(a, WINDOW_WIDTH, MESSAGE_BOX_HEIGHT);
 	}
 	
 	/**
 	 * Creates the options screen.
 	 */
 	private void createOptionsScreen() {
 		int height = getScreenHeight();
 		optionsScreen = new OptionsScreen(WINDOW_WIDTH, height);
 		optionsScreen.setBackgroundMusic(null);
 	}
 	
 	/**
 	 * Creates the overworld screen.
 	 */
 	private void createOverworldScreen() {
 		int height = getScreenHeight();
 		overworldScreen = new OverworldScreen(WINDOW_WIDTH, height);
 		overworldScreen.setBackgroundMusic("BGM_OVERWORLD");
 	}
 	
 	/**
 	 * Creates the pause screen.
 	 */
 	private void createPauseScreen() {
 		int height = getScreenHeight();
 		pauseScreen = Screen.getInstance(WINDOW_WIDTH, height);
 		pauseScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the player creation screen.
 	 */
 	private void createPlayerCreationScreen() {
 		int height = getScreenHeight();
 		charCreationScreen = new CharacterCreationScreen(WINDOW_WIDTH, height);
 		charCreationScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates an image if the image engine has been initialized.
 	 * 
 	 * @param index The index of the image to create.
 	 * 
 	 * @return The generated image, or null if the image engine hasn't been
 	 * initialized.
 	 */
 	private Image getImage(String index) {
 		Image img = null;
 		if (imageEngine != null) {
 			try {
 				img = imageEngine.createImage(index);
 			} catch (InvalidIndexException e) {
 				// silently fail
 			}
 		}
 		return img;
 	}
 	
 	/**
 	 * Gets the height of a screen.
 	 * 
 	 * @return The height of a screen.
 	 */
 	private int getScreenHeight() {
 		int menuHeight = menuBar.getPreferredSize().height;
 		int height = WINDOW_HEIGHT - menuHeight - MESSAGE_BOX_HEIGHT;
 		return height;
 	}
 	
 	/**
 	 * Packs, revalidates, and repaints the main window.
 	 */
 	private void refreshWindow() {
 		mainWindow.pack();
 		// revalidate is incompatible with JRE 6
 		mainWindow.invalidate();
 		mainWindow.validate();
 		mainWindow.repaint();
 	}
 	
 	/**
 	 * Sets the screen of this window. The current screen is moved to the
 	 * former screen to make room.
 	 * 
 	 * @param screen The screen to switch to.
 	 */
 	private void setScreen(Screen<?> screen) {
 		formerScreen = currentScreen;
 		currentScreen = screen;
 	}
 	
 	/**
 	 * Switches the window to display the specified screen.
 	 * 
 	 * @param screen The screen to switch to.
 	 */
 	private void switchWindow(Screen<?> screen) {
 		setScreen(screen);
 		class Runner implements Runnable {
 			private Screen<?> screen;
 			public Runner(Screen<?> screen) {
 				this.screen = screen;
 			}
 			@Override
 			public void run() {
 				switchToScreen(screen);
 			}
 		}
 		GraphicalInterface.invokeLaterIfNeeded(new Runner(screen));
 		String music = screen.getBackgroundMusic();
 		if (music != null) {
 			playMusic(music);
 		}
 		if (messageBox.isFrozen()) {
 			messageBox.unfreeze();
 		}
 		mainProgram.requestBattleResume();
 	}
 	
 	/**
 	 * Swaps out the current screen with the given screen. Only execute on EDT.
 	 * 
 	 * @param screen The screen to switch to.
 	 */
 	private void switchToScreen(Screen<?> screen) {
 		clearWindow();
 		mainWindow.add(menuBar, BorderLayout.NORTH);
 		mainWindow.add(screen, BorderLayout.CENTER);
 		mainWindow.add(messageBox.getComponent(), BorderLayout.SOUTH);
 		String index = screen.getBackgroundImage();
 		if (index != null) {
 			contentPane.setBackgroundImage(getImage(index));
 		} else {
 			contentPane.setBackgroundImage(null);
 		}
 		refreshWindow();
 		mainWindow.setVisible(true);
 		screen.setInitialProperties();
 	}
 	
 }

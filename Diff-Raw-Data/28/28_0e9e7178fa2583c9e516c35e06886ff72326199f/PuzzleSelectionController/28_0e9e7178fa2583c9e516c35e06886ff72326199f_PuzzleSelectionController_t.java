 package at.ac.uibk.akari.puzzleSelector.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import android.util.Log;
 import at.ac.uibk.akari.common.menu.DefaultMenuItem;
 import at.ac.uibk.akari.common.menu.MenuItem;
 import at.ac.uibk.akari.controller.AbstractController;
 import at.ac.uibk.akari.core.Puzzle;
 import at.ac.uibk.akari.core.Puzzle.Difficulty;
 import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
 import at.ac.uibk.akari.listener.MenuListener;
 import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
 import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
 import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedEvent;
 import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedListener;
 import at.ac.uibk.akari.puzzleSelector.view.DifficultyMenuScene;
 import at.ac.uibk.akari.puzzleSelector.view.DifficultySelectorHUD;
 import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;
 import at.ac.uibk.akari.puzzleSelector.view.PuzzleSelectorHUD;
 import at.ac.uibk.akari.utils.ListenerList;
 import at.ac.uibk.akari.utils.PuzzleManager;
 import at.ac.uibk.akari.utils.SceneManager;
 import at.ac.uibk.akari.view.Insets;
 
 public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener, MenuListener, PuzzleSelectionListener, ValueChangedListener<Integer> {
 
 	private Scene puzzleSelectorScene;
 	private DifficultyMenuScene difficultyScene;
 	private Camera camera;
 	private VertexBufferObjectManager vertexBufferObjectManager;
 	private LevelSelector levelSelector;
 	private PuzzleSelectorHUD puzzleSelectorHUD;
 	private DifficultySelectorHUD difficultySelectorHUD;
 
 	protected ListenerList listeners;
 
 	public PuzzleSelectionController(final Scene scene, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
 		this.listeners = new ListenerList();
 		this.puzzleSelectorScene = scene;
 		this.camera = camera;
 		this.init();
 	}
 
 	private void init() {
 
 		List<MenuItem> itemTypes = new ArrayList<MenuItem>();
 		itemTypes.add(Puzzle.Difficulty.EASY);
 		itemTypes.add(Puzzle.Difficulty.MEDIUM);
 		itemTypes.add(Puzzle.Difficulty.HARD);
 
 		this.difficultySelectorHUD = new DifficultySelectorHUD((int) this.camera.getWidth(), this.vertexBufferObjectManager);
 		this.difficultyScene = new DifficultyMenuScene(this.camera, this.vertexBufferObjectManager, itemTypes);
 
 		this.puzzleSelectorHUD = new PuzzleSelectorHUD((int) this.camera.getWidth(), this.vertexBufferObjectManager);
 		this.levelSelector = new LevelSelector(3, 2, new Insets(this.puzzleSelectorHUD.getDesiredHUDHeight(), 40, 0, 40), this.camera, this.vertexBufferObjectManager);
 		this.puzzleSelectorScene.attachChild(this.levelSelector);
 	}
 
 	@Override
 	public boolean start() {
 		// puzzle-selector
 		this.puzzleSelectorScene.setOnSceneTouchListener(this);
 		this.levelSelector.addPuzzleSelectionListener(this);
 		this.levelSelector.addValueChangedListener(this);
 		this.puzzleSelectorHUD.addPuzzleControlListener(this);
 
 		this.difficultyScene.addMenuListener(this);
 		this.difficultySelectorHUD.addPuzzleControlListener(this);
 
 		this.startDifficultySelector();
 		return true;
 	}
 
 	@Override
 	public boolean stop() {
 		this.puzzleSelectorScene.setOnSceneTouchListener(null);
 		this.levelSelector.removePuzzleSelectionListener(this);
 		this.levelSelector.removeValueChangedListener(this);
 		this.puzzleSelectorHUD.removePuzzleControlListener(this);
 
 		this.difficultyScene.removeMenuListener(this);
 		this.difficultySelectorHUD.removePuzzleControlListener(this);
 
 		return true;
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
 		this.levelSelector.onTouchEvent(pSceneTouchEvent);
 		return false;
 	}
 
 	@Override
 	public void menuItemSelected(final MenuItemSeletedEvent event) {
 		if (event.getSource().equals(this.puzzleSelectorHUD)) {
 			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
 			switch (selectedItem) {
 			case BACK:
 				this.stopPuzzleSelector();
				this.startDifficultySelector();
 				break;
 			default:
 				break;
 			}
 		} else if (event.getSource().equals(this.difficultySelectorHUD)) {
 			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
 			switch (selectedItem) {
 			case BACK:
				this.stop();
				this.firePuzzleSelectionCanceled();
 				break;
 			default:
 				break;
 			}
 		} else if (event.getSource().equals(this.difficultyScene)) {
 			Puzzle.Difficulty difficulty = (Puzzle.Difficulty) event.getMenuItem();
 			Log.i(this.getClass().getName(), "Selected difficulty " + difficulty);
 			this.startPuzzleSelector(difficulty);
 		}
 	}
 
	private void startPuzzleSelector(final Difficulty difficulty) {
 		this.levelSelector.setLevels(PuzzleManager.getInstance().getPuzzles(difficulty));
 		this.levelSelector.start();
 		this.puzzleSelectorHUD.setIndicatorIndex(this.levelSelector.getCurrentPageIndex(), this.levelSelector.getPagesCount());
 		SceneManager.getInstance().setCurrentScene(this, this.puzzleSelectorScene, this.puzzleSelectorHUD);
 	}
 
	private void stopPuzzleSelector() {
 		this.levelSelector.stop();
 	}
 
	private void startDifficultySelector() {
 		SceneManager.getInstance().setCurrentScene(this, this.difficultyScene, this.difficultySelectorHUD);
 	}
 
 	public void addPuzzleSelectionListener(final PuzzleSelectionListener listener) {
 		this.listeners.addListener(PuzzleSelectionListener.class, listener);
 	}
 
 	public void removePuzzleSelectionListener(final PuzzleSelectionListener listener) {
 		this.listeners.removeListener(PuzzleSelectionListener.class, listener);
 	}
 
 	protected void firePuzzleSelected(final Puzzle puzzle) {
 		PuzzleSelectionEvent event = new PuzzleSelectionEvent(this, puzzle);
 		for (PuzzleSelectionListener listener : this.listeners.getListeners(PuzzleSelectionListener.class)) {
 			listener.puzzleSelected(event);
 		}
 	}
 
 	protected void firePuzzleSelectionCanceled() {
 		PuzzleSelectionEvent event = new PuzzleSelectionEvent(this, null);
 		for (PuzzleSelectionListener listener : this.listeners.getListeners(PuzzleSelectionListener.class)) {
 			listener.puzzleSelectionCanceled(event);
 		}
 	}
 
 	@Override
 	public void puzzleSelectionCanceled(final PuzzleSelectionEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void puzzleSelected(final PuzzleSelectionEvent event) {
 		if (event.getSource().equals(this.levelSelector)) {
			this.stopPuzzleSelector();
 			this.stop();
 			this.firePuzzleSelected(event.getPuzzle());
 		}
 	}
 
 	@Override
 	public void valueChanged(final ValueChangedEvent<Integer> event) {
 		if (event.getSource().equals(this.levelSelector)) {
 			Log.d(this.getClass().getName(), "Changed page from " + event.getOldValue() + " to " + event.getNewValue());
 			this.puzzleSelectorHUD.setIndicatorIndex(event.getNewValue(), this.levelSelector.getPagesCount());
 		}
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		Scene currentScene = SceneManager.getInstance().getCurrentScene();
 		if (currentScene.equals(this.puzzleSelectorScene)) {
 			this.stopPuzzleSelector();
 		} else if (currentScene.equals(this.difficultyScene)) {
			this.stop();
			this.firePuzzleSelectionCanceled();
 		}
 	}
 
 }

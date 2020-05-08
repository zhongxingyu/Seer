 package pipes.editing;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import pipes.editing.actions.EditAction;
 import pipes.editing.io.TuneSerializer;
 import pipes.model.NewTuneParameters;
 import pipes.model.Tune;
 import pipes.model.TuneFactory;
 import pipes.sound.TunePlayer;
 import pipes.view.TuneView;
 import pipes.view.tools.EditTool;
 import pipes.view.tools.NullTool;
 
 /**
  * Controls the editing of a Tune from a logical perspective, as independent of the UI as possible.
  *
  */
 public class TuneEditController {
 	
 	/**
 	 * @return true if there was an EditAction performed that can now be undone
 	 */
 	public boolean canUndo() {
 		return history.canUndo();
 	}
 	
 	/**
 	 * @return true if there was an EditAction undone that can now be redone
 	 */
 	public boolean canRedo() {
 		return history.canRedo();
 	}
 	
 	/**
 	 * Steps back one EditAction to the previous state of the Tune
 	 */
 	public void undo() {
 		EditAction undone = history.pullFromUndoHistory();
 		undone.undo();
 		notifyEdit(null);
 	}
 	
 	/**
 	 * Redoes that last EditAction that was undone.
 	 */
 	public void redo() {
 		EditAction redone = history.pullFromRedoHistory();
 		redone.execute();
 		notifyEdit(redone);
 	}
 
 	/**
	 * Performs an EditAction on the Tune being edited.
 	 * The action is committed to the edit history and dependents are notified of the edit.
 	 */
 	public void execute(EditAction action) {
 		isDirty = true;
 		action.execute();
 		history.addToHistory(action);
 		notifyEdit(action);
 	}
 
 	/**
 	 * Executes an edit but without committing the action to the history.
 	 * This is useful when an EditAction is being "previewed" in a UI, but should not clutter the undo history.
 	 */
 	public void executeNoHistory(EditAction action) {
 		action.execute();
 		notifyEdit(action);
 	}
 	
 	/**
 	 * Subscribes a listener to receive calls when an EditAction is executed upon the Tune
 	 */
 	public void addEditListener(TuneEditListener l) {
 		listeners.add(l);
 	}
 
 	/**
 	 * Sets the tool being used in the editing context.
 	 */
 	public void setCurrentTool(EditTool tool) {
 		currentTool = tool;
 	}
 
 	/**
 	 * @return the Tune currently being edited
 	 */
 	public Tune getTune() {
 		return tune;
 	}
 
 	/**
 	 * @return the TunePlayer that should be used to playback the Tune
 	 */
 	public TunePlayer getPlayer() {
 		return player;
 	}
 	
 	/**
 	 * @return true if the Tune has been edited since the last save
 	 */
 	public boolean getIsDirty() {
 		return isDirty;
 	}
 
 	/**
 	 * @return the File associated with the Tune being edited. Null if the Tune is new and has never been saved.
 	 */
 	public File getEditingFile() {
 		return editingFile;
 	}
 	
 	/**
 	 * Creates a new Tune from the parameters given and set that Tune in the editing context.  
 	 */
 	public void newTune(NewTuneParameters parameters) {
 		tune = TuneFactory.getNewTune(parameters);
 		view.setTune(tune);
 		editingFile = null;
 		isDirty = false;
 		history.clear();
 	}
 	
 	/**
 	 * Loads a Tune from the given File and sets that Tune in the editing context.
 	 * @throws IOException due to a failure accessing the file
 	 */
 	public void loadTune(File file) throws IOException {
 		tune = TuneSerializer.loadTune(file);
 		view.setTune(tune);
 		editingFile = file;
 		isDirty = false;
 		history.clear();
 	}
 
 	/**
 	 * Saves the Tune to the file that is associated with it.
 	 * @throws IOException due to an error writing the file
 	 */
 	public void saveTune() throws IOException {
 		saveTune(editingFile);
 	}
 
 	/**
 	 * Saves the Tune to the given file 
 	 * @throws IOException due to an error writing the file
 	 */
 	public void saveTune(File saveTo) throws IOException {
 		editingFile = TuneSerializer.saveTune(tune, saveTo);
 		isDirty = false;
 		history.clear();
 	}
 
 	public TuneEditController(final TuneView view) {
 		this.view = view;
 		player = new TunePlayer();
 		history = new TuneEditHistory();
 		currentTool = new NullTool();
 		
 		view.addMouseListener(new MouseListener() {
 			public void mouseReleased(MouseEvent e) {
 				currentTool.mouseUp(e.getX(), e.getY());
 			}
 			public void mousePressed(MouseEvent e) {
 				currentTool.mouseDown(e.getX(), e.getY());
 			}
 			public void mouseExited(MouseEvent e) {
 			}
 			public void mouseEntered(MouseEvent e) {
 			}
 			public void mouseClicked(MouseEvent e) {
 				currentTool.mouseClicked(e.getX(), e.getY());
 			}
 		});
 		
 		view.addMouseMotionListener(new MouseMotionListener() {
 			public void mouseMoved(MouseEvent e) {
 			}
 			public void mouseDragged(MouseEvent e) {
 				currentTool.mouseDragged(e.getX(), e.getY());
 			}
 		});
 		
 		listeners = new LinkedList<TuneEditListener>();
 		listeners.add(new TuneEditListener() {
 			public void tuneEdited(EditAction action) {
 				view.updateMusic();
 			}
 		});
 		listeners.add(player);
 	}
 
 	private void notifyEdit(EditAction action) {
 		for (TuneEditListener l : listeners)
 			l.tuneEdited(action);
 	}
 
 	private Tune tune;
 	private TuneView view;
 	private TunePlayer player;
 	private EditTool currentTool;
 	private TuneEditHistory history;
 	
 	private boolean isDirty;
 	private File editingFile;
 
 	private LinkedList<TuneEditListener> listeners;
 }

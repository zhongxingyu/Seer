 package raven.edit.editor;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 import javax.swing.filechooser.FileFilter;
 
 import raven.edit.tools.EditorTool;
 import raven.edit.tools.SelectTool;
 import raven.game.RavenMap;
 import raven.utils.MapSerializer;
 
 public class EditorViewController implements EditorViewDelegate {
 	JFileChooser fileChooser;
 	
 	EditorView view;
 	EditorTool tool;
 	RavenMap level;
 	
 	boolean isDirty;
 	
 	private class LevelFilter extends FileFilter {
 
 		@Override
 		public boolean accept(File file) {
 			if (file.isDirectory()) {
 				return false;
 			}
 			
 			return file.getName().endsWith(".raven");
 		}
 
 		@Override
 		public String getDescription() {
 			return "Raven levels (*.raven)";
 		}
 	}
 	
 	public EditorViewController() {		
 		fileChooser = new JFileChooser();
 		fileChooser.setFileFilter(new LevelFilter());
 		
 		this.level = new RavenMap();
 		
 		view = new EditorView(level);
 		view.setDelegate(this);
 		view.create();
 		
 		this.changeTool(new SelectTool(view));
 		
 		isDirty = false;
 	}
 
 	@Override
 	public boolean doNewLevel() {	
 		if (!doSaveIfDirty())
 			return false;
 		RavenMap newMap = new RavenMap();
 		view = new EditorView(newMap);
 		changeLevel(newMap);
 		return true;
 	}
 	
 	@Override
 	public boolean doOpen() {
 		// If there have been changes, ask about saving them.
 		if (!doSaveIfDirty())
 			return false;
 		
 		int result = fileChooser.showOpenDialog((Component)view);
 		
 		if (result == JFileChooser.APPROVE_OPTION) {
 			RavenMap level;
 			try {
 				level = MapSerializer.deserializeMapFromFile(fileChooser.getSelectedFile());
 				changeLevel(level);
 				view.updateStatus("Opened level " + level.getPath());
 			} catch (FileNotFoundException e) {
 				JOptionPane.showMessageDialog(view,
 						"File " + fileChooser.getSelectedFile().getPath() + " was not found!",
 						"File not found", 
 						JOptionPane.ERROR_MESSAGE);
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(view,
 						"Unable to open " + fileChooser.getSelectedFile().getPath() + "\n" + e.getLocalizedMessage(),
 						"Unable to open file", 
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Returns true if the save was successful.
 	 */
 	public boolean doSaveIfDirty() {
 		if (isDirty) {
 			int confirm = JOptionPane.showConfirmDialog(view,
 					"Level has been modified. Would you like to save the changes?", "Confirm save...",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE);
 			if (confirm == JOptionPane.YES_OPTION) {
 				doSave();
 			} else if (confirm == JOptionPane.CANCEL_OPTION) {
 				return false;
 			} // else ignore changes
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean doSave() {
 		// Ask where to save the file if it doesn't have a filename yet
 		if (level.getPath() == null) {
 			fileChooser.setSelectedFile(new File(level.getName() + ".raven"));
 			int result = fileChooser.showSaveDialog((Component)view);
 			
 			if (result == JFileChooser.APPROVE_OPTION) {
 				level.setPath(fileChooser.getSelectedFile().getPath());
 			} else {
 				return false;
 			}
 		}
 		isDirty = false;
 
 		// Write it
 		try {
 			MapSerializer.serializeMapToPath(level, level.getPath());
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(view,
 					"Unable to save " + fileChooser.getSelectedFile().getPath() + "\n" + e.getLocalizedMessage(),
 					"Unable to save file", 
 					JOptionPane.ERROR_MESSAGE);
 		}
 		
 		view.updateStatus("Level saved to " + level.getPath());
 		
 		return true;
 	}
 
 
 	@Override
 	public void changeLevel(RavenMap level) {
 		isDirty = false;
 		this.level = level;
 		view.setLevel(level);	
 	}
 
 
 	@Override
 	public EditorView getView() {
 		return view;
 	}
 
 
 	@Override
 	public void makeDirty() {
 		isDirty = true;		
 	}
 
 	@Override
 	public void changeTool(EditorTool newTool) {
 		this.tool = newTool;
 		view.setTool(newTool);
 	}
 
 	@Override
 	public RavenMap getLevel() {
 		return level;
 	}
 
 }

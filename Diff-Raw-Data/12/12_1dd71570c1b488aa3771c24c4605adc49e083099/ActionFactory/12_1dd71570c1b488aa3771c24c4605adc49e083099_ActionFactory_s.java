 package gsingh.learnkirtan.ui.action;
 
 import gsingh.learnkirtan.FileManager;
 import gsingh.learnkirtan.FileManager.SaveResult;
 import gsingh.learnkirtan.shabad.Shabad;
 import gsingh.learnkirtan.shabad.ShabadMetaData;
 import gsingh.learnkirtan.ui.shabadeditor.ShabadEditor;
 import gsingh.learnkirtan.ui.shabadeditor.SwingShabadEditor;
 import gsingh.learnkirtan.utility.DialogUtility;
 
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 public class ActionFactory {
 
 	private ShabadEditor shabadEditor;
 	private FileManager fileManager;
 
 	public ActionFactory(ShabadEditor e, FileManager f) {
 		shabadEditor = e;
 		fileManager = f;
 	}
 
 	public Action newSaveAction() {
 		return new SaveAction();
 	}
 
 	public Action newOpenAction() {
 		return new OpenAction();
 	}
 
 	public Action newCreateAction() {
 		return new CreateAction();
 	}
 	
 	public Action newPropertiesAction() {
 		return new PropertiesAction();
 	}
 
 	@SuppressWarnings("serial")
 	private class SaveAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			try {
 				if (shabadEditor.isModified()) {
 					SaveResult result = fileManager.saveShabad(shabadEditor
 							.getShabad());
 					if (result != SaveResult.NOTSAVEDCANCELLED) {
 						if (shabadEditor instanceof SwingShabadEditor) {
 							SwingShabadEditor editor = (SwingShabadEditor) shabadEditor;
 							editor.reset();
 						}
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@SuppressWarnings("serial")
 	private class OpenAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			try {
 				// Save file if modified
 				SaveResult result = null;
 				if (shabadEditor.isModified()) {
 					result = fileManager.safeSave(shabadEditor.getShabad());
 				}
 
 				// Open file if not cancelled
 				if (result != SaveResult.NOTSAVEDCANCELLED) {
 					if (fileManager.openFile(shabadEditor)) {
 						if (shabadEditor instanceof SwingShabadEditor) {
 							SwingShabadEditor editor = (SwingShabadEditor) shabadEditor;
 							editor.reset();
 						}
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@SuppressWarnings("serial")
 	private class CreateAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			try {
 				// Save file if modified
 				SaveResult result = null;
 				if (shabadEditor.isModified()) {
 					result = fileManager.safeSave(shabadEditor.getShabad());
 				}
 
 				// Create new file if not cancelled
 				if (result != SaveResult.NOTSAVEDCANCELLED) {
 					shabadEditor.setShabad(new Shabad(""));
 					fileManager.newFile();
 					if (shabadEditor instanceof SwingShabadEditor) {
 						SwingShabadEditor editor = (SwingShabadEditor) shabadEditor;
 						editor.reset();
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@SuppressWarnings("serial")
 	public class PropertiesAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			DialogUtility.showMetaDataDialog(shabadEditor.getMetaData(),
 					new MetaDataDialogCallback());
 		}
 
 		public class MetaDataDialogCallback {
 			public void completed(ShabadMetaData data) {
 				shabadEditor.setMetaData(data);
				// TODO set modified
 			}
 		}
 	}
 }

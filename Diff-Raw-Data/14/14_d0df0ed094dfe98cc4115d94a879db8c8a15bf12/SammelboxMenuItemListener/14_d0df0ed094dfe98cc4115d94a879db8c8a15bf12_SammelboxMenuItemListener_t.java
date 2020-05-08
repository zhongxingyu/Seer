 package org.sammelbox.controller.menu;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.sammelbox.controller.filesystem.exporting.CSVExporter;
 import org.sammelbox.controller.filesystem.exporting.HTMLExporter;
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.controller.managers.AlbumViewManager;
 import org.sammelbox.controller.managers.DatabaseIntegrityManager;
 import org.sammelbox.model.album.AlbumItemStore;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.browser.BrowserFacade;
 import org.sammelbox.view.sidepanes.EmptySidepane;
 import org.sammelbox.view.sidepanes.ImportSidepane;
 import org.sammelbox.view.various.PanelType;
 import org.sammelbox.view.various.TextInputDialog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class SammelboxMenuItemListener {
 	private static final Logger LOGGER = LoggerFactory.getLogger(SammelboxMenuItemListener.class);
 	
 	private SammelboxMenuItemListener() {
 		// not needed
 	}
 	
 	static SelectionAdapter getImportAlbumItemsListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				ApplicationUI.changeRightCompositeTo(PanelType.IMPORT, ImportSidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 	
 	static SelectionAdapter getExportAlbumItemsListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
 					FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 					saveFileDialog.setText(Translator.get(DictKeys.DIALOG_EXPORT_VISIBLE_ITEMS));
 					saveFileDialog.setFilterPath(System.getProperty("user.home"));
 					String[] filterExt = { "*.html", "*.csv"};
 					saveFileDialog.setFilterExtensions(filterExt);
 					String[] filterNames = { 
 							Translator.get(DictKeys.DIALOG_HTML_FOR_PRINT) , 
 							Translator.get(DictKeys.DIALOG_CSV_FOR_SPREADSHEET) };
 					saveFileDialog.setFilterNames(filterNames);
 	
 					String filepath = saveFileDialog.open();
 					if (filepath != null) {
 						if (filepath.endsWith(".csv")) {
 							// ask for separation character
 							TextInputDialog separationCharacterInput = new TextInputDialog(ApplicationUI.getShell());
 							String separationCharacter = separationCharacterInput.open(
 									Translator.toBeTranslated("Choose a separation character"),
									Translator.toBeTranslated("Please choose a separation character"), 
									CSVExporter.DEFAULT_SEPARATION_CHARACTER, 
 									Translator.toBeTranslated("Export"));
 							
							if (separationCharacter == null) {
								separationCharacter = CSVExporter.DEFAULT_SEPARATION_CHARACTER;
							}
							
 							CSVExporter.exportAlbum(AlbumItemStore.getAllAlbumItems(), filepath, separationCharacter);
 						} else if (filepath.endsWith(".html")) {
 							HTMLExporter.exportAlbum(AlbumItemStore.getAllAlbumItems(), filepath);
 						}
 					}
 				}
 			}
 		};
 	}
 	
 	static SelectionAdapter getBackupListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 				saveFileDialog.setText(Translator.get(DictKeys.DIALOG_BACKUP_TO_FILE));
 				saveFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				saveFileDialog.setFilterExtensions(filterExt);
 
 				final String backupPath = saveFileDialog.open();
 				if (backupPath != null) {
 					BrowserFacade.showBackupInProgressPage();
 					ApplicationUI.getShell().setEnabled(false);
 					
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							try {
 								DatabaseIntegrityManager.backupToFile(backupPath);
 							} catch (DatabaseWrapperOperationException e) {
 								LOGGER.error("An error occurred while creating the backup", e);
 							}
 							
 							ApplicationUI.getShell().setEnabled(true);
 							BrowserFacade.showBackupFinishedPage();
 						}
 					});
 				}
 			}
 		};
 	}
 	
 	static SelectionAdapter getRestoreListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 
 				FileDialog openFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.OPEN);
 				openFileDialog.setText(Translator.get(DictKeys.DIALOG_RESTORE_FROM_FILE));
 				openFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				openFileDialog.setFilterExtensions(filterExt);
 
 				String path = openFileDialog.open();
 				if (path != null) {
 					try {
 						DatabaseIntegrityManager.restoreFromFile(path);
 						AlbumViewManager.initialize();
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("An error occured while trying to restore albums from a backup file", ex);
 					}
 					// No default album is selected on restore
 					ApplicationUI.refreshAlbumList();
 					BrowserFacade.showAlbumRestoredPage();
 				}
 			}
 		};
 	}
 	
 	static SelectionAdapter getExitListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				ApplicationUI.getShell().close();
 			}
 		};
 	}
 }

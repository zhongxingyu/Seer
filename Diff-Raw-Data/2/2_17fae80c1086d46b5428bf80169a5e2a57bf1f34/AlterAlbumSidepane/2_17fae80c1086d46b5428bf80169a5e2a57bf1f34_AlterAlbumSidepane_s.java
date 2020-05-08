 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.view.sidepanes;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.sammelbox.controller.MetaItemFieldFilter;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.model.album.AlbumItemStore;
 import org.sammelbox.model.album.FieldType;
 import org.sammelbox.model.album.MetaItemField;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.browser.BrowserFacade;
 import org.sammelbox.view.various.ComponentFactory;
 import org.sammelbox.view.various.TextInputDialog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AlterAlbumSidepane {
 	private final static Logger LOGGER = LoggerFactory.getLogger(AlterAlbumSidepane.class);
 	
 	/** Returns an "alter album" composite. This composite provides the user interface to alter an existing album. Meaning that an 
 	 * album name can be renamed, fields can be removed, fields can be added etc.. This composite is created based on an an existing
 	 * album and its field names/types.
 	 * @param parentComposite the parent composite
 	 * @param album the album which should be altered
 	 * @return a new "alter album" composite */
 	public static Composite build(final Composite parentComposite, final String album) {
 		// setup alter album composite
 		final Composite alterAlbumComposite = new Composite(parentComposite, SWT.NONE);
 		alterAlbumComposite.setLayout(new GridLayout());
 
 		// description (header) label
 		ComponentFactory.getPanelHeaderComposite(alterAlbumComposite, Translator.get(DictKeys.LABEL_ALTER_ALBUM));
 
 		// album name label & text-box to enter album name
 		Label albumNameLabel = new Label(alterAlbumComposite, SWT.NONE);
 		albumNameLabel.setText(Translator.get(DictKeys.LABEL_NEW_ALBUM_NAME));
 		final Text albumNameText = new Text(alterAlbumComposite, SWT.BORDER);
 		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 		albumNameText.setText(album);
 		albumNameText.setData(album);
 
 		// Rename album button
 		Button renameAlbumButton = new Button(alterAlbumComposite, SWT.PUSH);
 		renameAlbumButton.setText(Translator.get(DictKeys.BUTTON_RENAME_ALBUM));
 		renameAlbumButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		Label seperator = new Label(alterAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridData gridData= new GridData(GridData.FILL_BOTH);
 		gridData.heightHint = 15;
 		seperator.setLayoutData(gridData);
 
 		Composite innerComposite = new Composite(alterAlbumComposite, SWT.BORDER);
 		innerComposite.setLayout(new GridLayout(1, false));
 		innerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// picture question label & radio buttons
 		Label label = new Label(innerComposite, SWT.NONE);
 		label.setText(Translator.get(DictKeys.LABEL_SHOULD_CONTAIN_IMAGES));
 		Composite composite = new Composite(innerComposite, SWT.NULL);
 		composite.setLayout(new RowLayout());
 		final Button yesButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		final Button noButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		yesButtonForIncludingImages.setText(Translator.get(DictKeys.BUTTON_YES));
 		noButtonForIncludingImages.setText(Translator.get(DictKeys.BUTTON_NO));
 		try {
 			yesButtonForIncludingImages.setSelection(DatabaseOperations.isPictureAlbum(album));
 			noButtonForIncludingImages.setSelection(!DatabaseOperations.isPictureAlbum(album));
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("Couldn't determine whether the album contains pictures or not.", ex);
 		}
 		
 		Label innerSeperator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridData gridDataForInnerSeperator = new GridData(GridData.FILL_BOTH);
 		gridDataForInnerSeperator.heightHint = 15;
 		innerSeperator.setLayoutData(gridDataForInnerSeperator);
 
 		// fieldname label and text-box to enter the name of the field
 		Label fieldNameLabel = new Label(innerComposite, SWT.NONE);
 		fieldNameLabel.setText(Translator.get(DictKeys.LABEL_FIELD_NAME));
 		final Text fieldNameText = new Text(innerComposite, SWT.BORDER);
 		fieldNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// fieldtype label and combo-box to enter the type of the field
 		Label fieldTypeLabel = new Label(innerComposite, SWT.NONE);
 		fieldTypeLabel.setText(Translator.get(DictKeys.LABEL_FIELD_TYPE));
 		final Combo fieldTypeCombo = new Combo(innerComposite, SWT.DROP_DOWN);
 		fieldTypeCombo.setItems(FieldType.toUserTypeStringArray());	    
 		fieldTypeCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
 		fieldTypeCombo.setText(fieldTypeCombo.getItem(0).toString());
 
 		// Add-field-button --> listener comes after table
 		Button addFieldButton = new Button(innerComposite, SWT.PUSH);
		addFieldButton.setText("Add additional field to Album");
 		addFieldButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// Field table
 		final Table albumFieldNamesAndTypesTable = new Table(innerComposite, SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
 		albumFieldNamesAndTypesTable.setLinesVisible(true);
 		albumFieldNamesAndTypesTable.setHeaderVisible(true);
 
 		Menu popupMenu = new Menu(albumFieldNamesAndTypesTable);
 		MenuItem moveUp = new MenuItem(popupMenu, SWT.NONE);
 		moveUp.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_UP));
 		moveUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() > 0) {
 					int newPosition = albumFieldNamesAndTypesTable.getSelectionIndex() - 1; // move one up
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 					MetaItemField metaItemField = new MetaItemField(originalItem.getText(1), FieldType.valueOf(originalItem.getText(2)), originalItem.getChecked());
 					originalItem.dispose();
 
 					TableItem itemAtNewPosition = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, newPosition);
 					itemAtNewPosition.setText(1, metaItemField.getName());
 					itemAtNewPosition.setText(2, metaItemField.getType().toString());
 					itemAtNewPosition.setChecked(metaItemField.isQuickSearchable());					
 
 					try {
 						String albumName = albumNameText.getData().toString();
 						if (newPosition == 0) {
 							DatabaseOperations.reorderAlbumItemField(albumName, metaItemField, null);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_MOVED_UP, metaItemField.getName()));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						} else {
 							TableItem moveAfterTableItem = albumFieldNamesAndTypesTable.getItem(newPosition-1);
 							MetaItemField moveAfterField = new MetaItemField(moveAfterTableItem.getText(1), FieldType.valueOf(moveAfterTableItem.getText(2)), moveAfterTableItem.getChecked());
 							DatabaseOperations.reorderAlbumItemField(albumName, metaItemField, moveAfterField);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_MOVED_UP, metaItemField.getName()));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						}
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("Couldn't reorder album items", ex);
 					}
 				}
 			}
 		});
 
 		renameAlbumButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String newAlbumName = albumNameText.getText();
 
 				boolean isAlbumNameValid;
 				try {
 					isAlbumNameValid = DatabaseOperations.isAlbumNameAvailable(newAlbumName);
 					if (!isAlbumNameValid) {
 						ComponentFactory.getMessageBox(
 								parentComposite, 
 								Translator.get(DictKeys.DIALOG_TITLE_ALBUM_NAME_ALREADY_USED), 
 								Translator.get(DictKeys.DIALOG_CONTENT_ALBUM_NAME_ALREADY_USED), 
 								SWT.ICON_INFORMATION).open();
 						return;
 					}
 				} catch (DatabaseWrapperOperationException ex) {
 					LOGGER.error("Couldn't rename the album", ex);
 				} 
 
 				if (!FileSystemAccessWrapper.isNameFileSystemCompliant(newAlbumName)) {
 							
 					ComponentFactory.getMessageBox(parentComposite, Translator.get(DictKeys.DIALOG_TITLE_ALBUM_NAME_INVALID), Translator.get(DictKeys.DIALOG_CONTENT_ALBUM_NAME_INVALID), SWT.ICON_WARNING).open();
 					return;
 				}
 
 				String oldAlbumName = albumNameText.getData().toString();
 				try {
 					DatabaseOperations.renameAlbum(oldAlbumName, newAlbumName);
 					albumNameText.setData(newAlbumName);
 					ApplicationUI.refreshAlbumList();
 					ApplicationUI.setSelectedAlbum(newAlbumName);
 
 					BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUM_RENAMED, oldAlbumName, newAlbumName));
 					
 					AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 				} catch (DatabaseWrapperOperationException ex) {
 					LOGGER.error("Could rename the album with name '" + oldAlbumName + "' to '" + newAlbumName, ex);
 					albumNameText.setText(albumNameText.getData().toString());
 				}
 			}
 		});
 		
 		yesButtonForIncludingImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String albumName = albumNameText.getData().toString();
 
 				try {
 					if (!DatabaseOperations.isPictureAlbum(albumName)) {
 						try {
 							DatabaseOperations.setAlbumPictureFunctionality(albumName, true);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUM_PICTURES_ENABLED));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						} catch (DatabaseWrapperOperationException failedDatabaseWrapperOperationException){
 							LOGGER.error("Pictures could not be enabled for the album");
 						}
 					}
 				} catch(DatabaseWrapperOperationException ex) {
 					LOGGER.error("A database error occured", ex);
 				}
 			}
 		});
 		
 		noButtonForIncludingImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String currentAlbumName = albumNameText.getData().toString();
 				try {
 					if (DatabaseOperations.isPictureAlbum(currentAlbumName)) {
 						boolean removalConfirmed = ComponentFactory.showYesNoDialog(alterAlbumComposite, 
 								Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM_PICTURES), 
 								Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM_PICTURES));
 						if (removalConfirmed) {
 							DatabaseOperations.setAlbumPictureFunctionality(currentAlbumName, false);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUM_PICTURES_DISABLED));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						} else {
 							yesButtonForIncludingImages.setSelection(true);
 							noButtonForIncludingImages.setSelection(false);
 						}
 					}
 				} catch (DatabaseWrapperOperationException ex) {
 					LOGGER.error("A database error occured", ex);
 				}
 			}
 		});
 		
 		MenuItem moveDown = new MenuItem(popupMenu, SWT.NONE);
 		moveDown.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_DOWN));
 		moveDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() < (albumFieldNamesAndTypesTable.getItemCount() - 1)) {
 					int newPosition = albumFieldNamesAndTypesTable.getSelectionIndex() + 1; // move one down
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 					MetaItemField metaItemField = new MetaItemField(originalItem.getText(1), FieldType.valueOf(originalItem.getText(2)), originalItem.getChecked());
 					originalItem.dispose();
 
 					TableItem itemAtNewPosition = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, newPosition);
 					itemAtNewPosition.setText(1, metaItemField.getName());
 					itemAtNewPosition.setText(2, metaItemField.getType().toString());
 					itemAtNewPosition.setChecked(metaItemField.isQuickSearchable());
 
 					try {
 						String albumName = albumNameText.getData().toString();
 						if (newPosition == 0) {
 							DatabaseOperations.reorderAlbumItemField(albumName, metaItemField, null);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_MOVED_DOWN, metaItemField.getName()));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						} else {
 							TableItem moveAfterTableItem = albumFieldNamesAndTypesTable.getItem(newPosition - 1);
 							MetaItemField moveAfterField = new MetaItemField(moveAfterTableItem.getText(1), FieldType.valueOf(moveAfterTableItem.getText(2)), moveAfterTableItem.getChecked());
 							DatabaseOperations.reorderAlbumItemField(albumName, metaItemField, moveAfterField);
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_MOVED_DOWN, metaItemField.getName()));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						}
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("A database error occured", ex);
 					}
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem rename = new MenuItem(popupMenu, SWT.NONE);
 		rename.setText(Translator.get(DictKeys.DROPDOWN_RENAME));
 		rename.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
 				String newFieldName = textInputDialog.open(
 						Translator.get(DictKeys.DIALOG_TITLE_RENAME_FIELD),
 						Translator.get(DictKeys.DIALOG_CONTENT_RENAME_FIELD), item.getText(1), 
 						Translator.get(DictKeys.DIALOG_BUTTON_RENAME_FIELD));
 
 				if (newFieldName != null) {	    			
 					MetaItemField oldMetaItemField = new MetaItemField(item.getText(1),  FieldType.valueOf(item.getText(2)), item.getChecked());
 					MetaItemField newMetaItemField = new MetaItemField(newFieldName,  FieldType.valueOf(item.getText(2)), item.getChecked());
 
 					String albumName = albumNameText.getData().toString();
 					try {
 					    DatabaseOperations.renameAlbumItemField( albumName, oldMetaItemField, newMetaItemField);
 					    item.setText(1, newFieldName);
 						BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_RENAMED, oldMetaItemField.getName(), newMetaItemField.getName()));
 						AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("An error occured while renaming the album field", ex);
 					}
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem delete = new MenuItem(popupMenu, SWT.NONE);
 		delete.setText(Translator.get(DictKeys.DROPDOWN_REMOVE));
 		delete.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() != -1) {	    	
 					boolean removalConfirmed = ComponentFactory.showYesNoDialog(
 							alterAlbumComposite,
 							Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM_ITEM), 
 							Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM_ITEM));
 					if (removalConfirmed) {
 						TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 						String albumName = albumNameText.getData().toString();
 						try {
 							DatabaseOperations.removeAlbumItemField(albumName, new MetaItemField(item.getText(1), FieldType.valueOf(item.getText(2)), item.getChecked()));
 							BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_REMOVED, item.getText(1)));
 							AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 						} catch (DatabaseWrapperOperationException ex) {
 							LOGGER.error("An error occured while trying to delete an album item from the " + albumName + " album", ex);
 						}
 
 						item.dispose();				
 					}					
 				}
 			}
 		});
 
 		albumFieldNamesAndTypesTable.setMenu(popupMenu);
 
 		// Setup table
 		TableColumn isImportantColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		isImportantColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_QUICKSEARCH));
 		TableColumn fieldNameColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldNameColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_FIELD_NAME));
 		TableColumn fieldTypeColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldTypeColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_FIELD_TYPE));
 		albumFieldNamesAndTypesTable.getColumn(0).pack ();
 		albumFieldNamesAndTypesTable.getColumn(1).pack ();
 		albumFieldNamesAndTypesTable.getColumn(2).pack ();
 
 		// Init the table
 		java.util.List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
 		try {
 			validMetaItemFields = MetaItemFieldFilter.getValidMetaItemFields(DatabaseOperations.getAlbumItemFieldNamesAndTypes(album));
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An error occured while trying to get the list of valid meta item fields", ex);
 		}
 		for (MetaItemField metaItemField : validMetaItemFields) {
 			TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
 			item.setChecked(metaItemField.isQuickSearchable());
 			item.setText(1, metaItemField.getName());
 			item.setText(2, metaItemField.getType().toString());			
 		}
 
 		// Set table layout data
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.heightHint = 110;
 		albumFieldNamesAndTypesTable.setLayoutData(data);
 
 		albumFieldNamesAndTypesTable.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 
 				int index =0;
 				for (index=0; index< albumFieldNamesAndTypesTable.getItemCount(); index++) {
 					if (((TableItem)event.item).equals(albumFieldNamesAndTypesTable.getItem(index)) ) {
 						break;
 					}
 				} 
 
 				if (event.detail == SWT.CHECK) {
 					MetaItemField metaItemField = new MetaItemField(
 							albumFieldNamesAndTypesTable.getItem(index).getText(1),
 							FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(index).getText(2)),
 							albumFieldNamesAndTypesTable.getItem(index).getChecked());
 
 					String albumName = albumNameText.getData().toString();
 					try {
 						DatabaseOperations.updateQuickSearchable(albumName, metaItemField);
 						BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_NOW_QUICKSEARCHABLE, metaItemField.getName()));
 						AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("A database error occured while trying to make the '" + metaItemField + "' " +
 								"in the '" + albumName + "' album quicksearchable", ex);
 					}
 				}
 			}
 		});
 
 		// Add listener to Add-field-button
 		addFieldButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (fieldNameText.getText().isEmpty()) {				
 					ComponentFactory.getMessageBox(parentComposite.getShell(),
 							Translator.get(DictKeys.DIALOG_TITLE_FIELD_MUST_HAVE_NAME),
 							Translator.get(DictKeys.DIALOG_CONTENT_FIELD_MUST_HAVE_NAME),
 							SWT.ICON_WARNING | SWT.OK).open();
 					return;
 				}
 
 
 				MetaItemField metaItemField = new MetaItemField(fieldNameText.getText(), FieldType.valueOf(fieldTypeCombo.getText()), false);
 				String albumName = albumNameText.getData().toString();
 
 				try {
 					if (!DatabaseOperations.isItemFieldNameAvailable(albumName, metaItemField.getName())) {
 						ComponentFactory.getMessageBox(parentComposite.getShell(),
 								Translator.get(DictKeys.DIALOG_TITLE_FIELD_NAME_ALREADY_USED),
 								Translator.get(DictKeys.DIALOG_CONTENT_FIELD_NAME_ALREADY_USED),
 								SWT.ICON_WARNING | SWT.OK).open();
 						fieldNameText.selectAll();
 						fieldNameText.setFocus();
 					} else {
 						DatabaseOperations.appendNewAlbumField(albumName, metaItemField);
 						TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
 						item.setText(1, fieldNameText.getText());
 						item.setText(2, fieldTypeCombo.getText());
 	
 						BrowserFacade.addModificationToAlterationList(Translator.get(DictKeys.BROWSER_ALBUMFIELD_ADDED, fieldNameText.getText(), fieldTypeCombo.getText()));
 						
 						AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 	
 						fieldNameText.setText("");
 					}
 				} catch (DatabaseWrapperOperationException ex) {
 					LOGGER.error("A database error occured while checking whether '" + metaItemField.getName() + "' is avialable as album name", ex);
 				}
 			}
 		});
 
 		BrowserFacade.clearAlterationList();
 		AlterAlbumSidepane.updateAlterAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
 		
 		return alterAlbumComposite;
 	}
 	
 	private static void updateAlterAlbumPage(Button yesButtonForIncludingImages, Table albumFieldNamesAndTypesTable) {
 		boolean containsImages = false;
 		if (yesButtonForIncludingImages.getSelection()) {
 			containsImages = true;
 		}
 		
 		ArrayList<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();
 
 		for ( int i=0 ; i < albumFieldNamesAndTypesTable.getItemCount() ; i++ ) {					
 			metaItemFields.add(
 					new MetaItemField(
 							albumFieldNamesAndTypesTable.getItem(i).getText(1),
 							FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(i).getText(2)),
 							albumFieldNamesAndTypesTable.getItem(i).getChecked()));
 		}
 		
 		BrowserFacade.showCreateAlterAlbumPage(AlbumItemStore.getSampleAlbumItem(containsImages, metaItemFields));
 	}
 }

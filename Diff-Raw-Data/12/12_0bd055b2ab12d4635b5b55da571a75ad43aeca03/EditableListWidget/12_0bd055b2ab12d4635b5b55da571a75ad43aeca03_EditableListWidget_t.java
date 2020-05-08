 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements an editable List-like widget 
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.utils;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IParticipantInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class EditableListWidget {
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fMainComposite.
 	 */
 	protected Composite fMainComposite = null;
 
 	/**
 	 * Field fMainTable.
 	 */
 	protected Table fMainTable = null;
 
 	/**
 	 * Field fAddButton.
 	 */
 	protected Button fAddButton = null;
 
 	/**
 	 * Field fRemoveButton.
 	 */
 	protected Button fRemoveButton = null;
 
 	/**
 	 * Field fListener.
 	 */
 	protected IEditableListListener fListener = null;
 
 	/**
 	 * Field fInstanceId.
 	 */
 	protected int fInstanceId = 0;
 
 	/**
 	 * Field fValues.
 	 */
 	protected String[] fValues = null;
 
 	/**
 	 * Field fEditableControl.
 	 */
 	Control fEditableControl = null;
 
 	// ------------------------------------------------------------------------
 	// Constructor
 	// ------------------------------------------------------------------------
 
 	//TODO:  This should be made more generic to accept various tables.  Right now it is very hard-coded to our stuff...
 	/**
 	 * Constructor for EditableListWidget.
 	 * 
 	 * @param aToolkit
 	 *            - FormToolkit
 	 * @param aParent
 	 *            - Composite
 	 * @param aLayoutData
 	 *            - Object
 	 * @param aListener
 	 *            - IEditableListListener
 	 * @param aInstanceId
 	 *            - int
 	 * @param aEditableWidgetClass
 	 *            - Class<?>
 	 * @param aEditableValues
 	 *            - String[]
 	 */
 	public EditableListWidget(FormToolkit aToolkit, Composite aParent, Object aLayoutData,
 			IEditableListListener aListener, int aInstanceId, Class<?> aEditableWidgetClass, String[] aEditableValues) {
 		fMainComposite = (null != aToolkit) ? aToolkit.createComposite(aParent) : new Composite(aParent, SWT.NONE);
 		fMainComposite.setLayoutData(aLayoutData);
 		fMainComposite.setLayout(new GridLayout(4, false));
 		fListener = aListener;
 		fInstanceId = aInstanceId;
 		fValues = aEditableValues;
 		createEditableListFromTable(aToolkit, aEditableWidgetClass);
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method dispose.
 	 */
 	public void dispose() {
 
 		fMainTable.dispose();
 		fMainComposite.dispose();
 	}
 
 	/**
 	 * Method createEditableListFromTable. Builds the editable list in the provided table
 	 * 
 	 * @param aToolkit
 	 *            - FormToolkit
 	 * @param aEditableWidgetClass
 	 *            - Class<?>
 	 */
 	public void createEditableListFromTable(FormToolkit aToolkit, final Class<?> aEditableWidgetClass) {
 
 		final Composite tableComposite = (null != aToolkit) ? aToolkit.createComposite(fMainComposite) : new Composite(
 				fMainComposite, SWT.NONE);
 		fMainTable = (null != aToolkit)
 				? aToolkit.createTable(tableComposite, SWT.FULL_SELECTION | SWT.BORDER)
 				: new Table(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
 
 		final GridData tableCompositeData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		if (aEditableWidgetClass.equals(Date.class) || aEditableWidgetClass.equals(Label.class)) {
 			tableCompositeData.horizontalSpan = 1;
 		} else {
 			tableCompositeData.horizontalSpan = 2;
 		}
 		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
 		tableComposite.setLayout(tableColumnLayout);
 
 		final TableColumn tableColumn = new TableColumn(fMainTable, SWT.NONE, 0);
 		final TableColumn tableColumn2;
 		fMainTable.setLinesVisible(true);
 
 		if (aEditableWidgetClass.equals(Date.class) || aEditableWidgetClass.equals(Label.class)) {
 			fMainTable.setHeaderVisible(true);
 			tableColumn2 = new TableColumn(fMainTable, SWT.NONE, 1);
 			if (aEditableWidgetClass.equals(Date.class)) {
 				tableColumn.setText(R4EUIConstants.SPENT_TIME_COLUMN_HEADER);
 				tableColumn2.setText(R4EUIConstants.ENTRY_TIME_COLUMN_HEADER);
 			} else if (aEditableWidgetClass.equals(Label.class)) {
 				tableColumn.setText(R4EUIConstants.ID_LABEL);
 				tableColumn2.setText(R4EUIConstants.EMAIL_LABEL);
 			}
 			tableColumn.pack();
 			tableColumn2.pack();
 			tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(50, tableColumn.getWidth() * 2, true));
 			tableColumnLayout.setColumnData(tableColumn2, new ColumnWeightData(50, tableColumn.getWidth(), true));
 
 		} else {
 			tableColumn2 = null; //only 1 column
 			tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(10, 100, true));
 		}
 		tableComposite.setLayoutData(tableCompositeData);
 
 		fMainComposite.addControlListener(new ControlListener() {
 			public void controlResized(ControlEvent e) {
 
 				//TODO:  This does not work 100% as resizing the colums lags when the parent area gets shrinked quickly.
 				//        This causes the rightmost part of the table composite to be clipped away.
 				//        This will need to be improved, see bug 356857.
 				final Rectangle area = fMainTable.getClientArea();
 				final Point size = tableComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 				final ScrollBar vBar = fMainTable.getVerticalBar();
 				final int vBarWidth = vBar.getSize().x;
 				int width = area.width - vBarWidth * 3;
 				if (width < 0) {
 					return;
 				}
 
 				if (size.y > area.height + fMainTable.getHeaderHeight()) {
 					// Subtract the scrollbar width from the total column width
 					// if a vertical scrollbar will be required
 					width -= vBarWidth;
 				}
 
 				// Set column width first and then resize the table to
 				// match the client area width
 				if (null != tableColumn2) {
 					tableColumn.setWidth(width / 2);
 					tableColumn2.setWidth(width - tableColumn.getWidth());
 				} else {
 					tableColumn.setWidth(width);
 				}
 			}
 
 			public void controlMoved(ControlEvent e) {
 				// ignor
 			}
 		});
 
 		fMainTable.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				//Send items updated notification
 				if (null != fListener) {
 					//If items are empty, do not consider them
 					final TableItem[] items = fMainTable.getItems();
 					for (int i = 0; i < items.length; i++) {
 						if (items[i].getText().trim().length() < 1) {
 							fMainTable.remove(i);
 						}
 					}
 					fListener.itemsUpdated(fMainTable.getItems(), fInstanceId);
 				}
 			}
 
 			public void focusGained(FocusEvent e) {
 				//Do nothing
 			}
 		});
 
 		fMainTable.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				fListener.itemSelected(fMainTable.getSelection()[0], fInstanceId); //Only 1 element selected at any given time
 			}
 		});
 
 		final Composite buttonsComposite = (null != aToolkit)
 				? aToolkit.createComposite(fMainComposite)
 				: new Composite(fMainComposite, SWT.NONE);
 		buttonsComposite.setLayout(new GridLayout());
 		buttonsComposite.setLayoutData(new GridData(GridData.CENTER, SWT.TOP, false, false));
 
 		fAddButton = (null != aToolkit) ? aToolkit.createButton(buttonsComposite, R4EUIConstants.BUTTON_ADD_LABEL,
 				SWT.NONE) : new Button(buttonsComposite, SWT.NONE);
 		fAddButton.setText(R4EUIConstants.BUTTON_ADD_LABEL);
 		if (aEditableWidgetClass.equals(CCombo.class)) {
 			if (null == fValues || 0 == fValues.length) {
 				fAddButton.setEnabled(false);
 			}
 		}
 		fRemoveButton = (null != aToolkit) ? aToolkit.createButton(buttonsComposite,
 				R4EUIConstants.BUTTON_REMOVE_LABEL, SWT.NONE) : new Button(buttonsComposite, SWT.NONE);
 		fRemoveButton.setText(R4EUIConstants.BUTTON_REMOVE_LABEL);
 		if (0 == fMainTable.getItemCount()) {
 			fRemoveButton.setEnabled(false);
 		} else {
 			fRemoveButton.setEnabled(true);
 		}
 
 		fAddButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		fAddButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				if (aEditableWidgetClass.equals(Label.class)) {
 					final IParticipantInputDialog dialog = R4EUIDialogFactory.getInstance().getParticipantInputDialog(
 							false);
 					final int result = dialog.open();
 					if (result == Window.OK) {
 						TableItem newItem = null;
 						for (R4EParticipant participant : dialog.getParticipants()) {
 							newItem = null;
 							String[] tableStrs = new String[2];
 							tableStrs[0] = participant.getId();
 							tableStrs[1] = participant.getEmail();
 							//Check if values already exist
 							for (TableItem item : fMainTable.getItems()) {
 								if (item.getText(0).equals(tableStrs[0])) {
 									newItem = item;
 								}
 							}
 							if (null == newItem) {
 								newItem = new TableItem(fMainTable, SWT.NONE);
 							}
 							fMainTable.showItem(newItem);
 							newItem.setText(tableStrs);
 						}
 						if (null != newItem) {
 							fMainTable.showItem(newItem);
 						}
 						fListener.itemsUpdated(fMainTable.getItems(), fInstanceId);
 					}
 					R4EUIDialogFactory.getInstance().removeParticipantInputDialog();
 					return;
 				}
 
 				final TableItem newItem = new TableItem(fMainTable, SWT.NONE);
 				fMainTable.showItem(newItem);
 				if (aEditableWidgetClass.equals(Text.class)) {
 					fEditableControl = new Text(fMainTable, SWT.SINGLE | SWT.BORDER);
 					((Text) fEditableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							newItem.setText(((Text) fEditableControl).getText());
 						}
 					});
 					((Text) fEditableControl).addKeyListener(new KeyListener() {
 
 						public void keyReleased(KeyEvent ke) {
 							if (ke.keyCode == SWT.CR) {
 								return;
 							}
 						}
 
 						public void keyPressed(KeyEvent ke) {
 							// Nothing to do
 						}
 					});
 				} else if (aEditableWidgetClass.equals(CCombo.class)) {
 					fEditableControl = new CCombo(fMainTable, SWT.BORDER | SWT.READ_ONLY);
 					//Only add the values not already in the table in the CCombo box
 					final List<String> currentValues = new ArrayList<String>();
 					for (String currentValue : fValues) {
 						currentValues.add(currentValue);
 					}
 					final TableItem[] currentItems = fMainTable.getItems();
 					for (TableItem currentItem : currentItems) {
 						if (currentValues.contains(currentItem.getText())) {
 							currentValues.remove(currentItem.getText());
 						}
 					}
 					((CCombo) fEditableControl).setItems(currentValues.toArray(new String[currentValues.size()]));
 					((CCombo) fEditableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							newItem.setText(((CCombo) fEditableControl).getText());
 						}
 					});
 				} else if (aEditableWidgetClass.equals(Date.class)) {
 					fEditableControl = new Text(fMainTable, SWT.NONE);
 					final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 					final String[] data = { ((Text) fEditableControl).getText(),
 							dateFormat.format(Calendar.getInstance().getTime()) };
 					newItem.setText(data);
 					((Text) fEditableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							//Only accept numbers
 							String newText = ((Text) fEditableControl).getText();
 							try {
 								Integer.valueOf(newText);
 							} catch (NumberFormatException nfe) {
 								if (newText.length() > 0) {
 									newText = newText.substring(0, newText.length() - 1);
 									((Text) fEditableControl).setText(newText);
 									((Text) fEditableControl).setSelection(newText.length());
 								}
 							}
 							newItem.setText(0, newText);
 						}
 					});
 				} else {
 					return;
 				}
 				fEditableControl.addFocusListener(new FocusListener() {
 					public void focusLost(FocusEvent fe) {
 						((Control) fe.getSource()).dispose();
 
 						//Send items updated notification
 						if (null != fListener) {
 							//If items are empty, do not consider them
 							final TableItem[] items = fMainTable.getItems();
							final int numColumns = fMainTable.getColumnCount();
							boolean isSameItem = false;
 							for (int i = 0; i < items.length; i++) {
 								if (items[i].getText().trim().length() < 1) {
 									fMainTable.remove(i);
 									return;
 								}
 							}
 
 							//If items are already present, do not consider the duplicates
 							if (items.length > 1) {
 								for (int i = 0; i < items.length; i++) {
 									for (int j = i + 1; j < items.length; j++) {
										isSameItem = true;
										for (int k = 0; k < numColumns; k++) {
											if (!items[i].getText(k).equals(items[j].getText(k))) {
												isSameItem = false;
												break; //at least one column is different go to next item
											}
										}
										if (isSameItem) {
 											fMainTable.remove(j);
 											return;
 										}
 									}
 								}
 							}
 							fListener.itemsUpdated(fMainTable.getItems(), fInstanceId);
 						}
 					}
 
 					public void focusGained(FocusEvent fe) {
 						//Nothing to do
 					}
 				});
 
 				fEditableControl.setFocus();
 				final TableEditor editor = new TableEditor(fMainTable);
 				editor.grabHorizontal = true;
 				editor.grabVertical = true;
 				editor.setEditor(fEditableControl, newItem, 0);
 				fRemoveButton.setEnabled(true);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 			}
 		});
 		fRemoveButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		fRemoveButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final int numItems = fMainTable.getItemCount();
 				if (numItems > 0) {
 					//Find the table index for the first control
 					final Control[] controls = fMainTable.getChildren();
 					final int firstControlIndex = numItems - controls.length;
 
 					//Currently selected item
 					int tableItemIndex = fMainTable.getSelectionIndex();
 					if (R4EUIConstants.INVALID_VALUE == tableItemIndex) {
 						//Remove the selected element (and control if there is one) or the last one if none is selected
 						tableItemIndex = numItems - 1;
 					}
 					if (tableItemIndex >= firstControlIndex) {
 						controls[tableItemIndex - firstControlIndex].dispose();
 					}
 					fMainTable.getItem(tableItemIndex).dispose();
 				}
 				if (0 == fMainTable.getItemCount()) {
 					fRemoveButton.setEnabled(false);
 				} else {
 					fRemoveButton.setEnabled(true);
 				}
 				if (null != fListener) {
 					fListener.itemsUpdated(fMainTable.getItems(), fInstanceId);
 				}
 				fMainTable.redraw();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 			}
 		});
 	}
 
 	/**
 	 * Method removeAll.
 	 */
 	public void removeAll() {
 		fMainTable.removeAll();
 	}
 
 	/**
 	 * Method addItem.
 	 * 
 	 * @return Item
 	 */
 	public Item addItem() {
 		return new TableItem(fMainTable, SWT.NONE);
 	}
 
 	/**
 	 * Method getItem.
 	 * 
 	 * @param aIndex
 	 *            - int
 	 * @return Item
 	 */
 	public Item getItem(int aIndex) {
 		return fMainTable.getItem(aIndex);
 	}
 
 	/**
 	 * Method getSelectedItem.
 	 * 
 	 * @return Item
 	 */
 	public Item getSelectedItem() {
 		if (fMainTable.getSelection().length == 0) {
 			return null;
 		}
 		return fMainTable.getSelection()[0]; //Only one element selectable at any given time
 	}
 
 	/**
 	 * Method getItems.
 	 * 
 	 * @return Item[]
 	 */
 	public Item[] getItems() {
 		return fMainTable.getItems();
 	}
 
 	/**
 	 * Method getItemCount.
 	 * 
 	 * @return int
 	 */
 	public int getItemCount() {
 		return fMainTable.getItemCount();
 	}
 
 	/**
 	 * Method setEnabled.
 	 * 
 	 * @param aEnabled
 	 *            - boolean
 	 */
 	public void setEnabled(boolean aEnabled) {
 		fMainComposite.setEnabled(aEnabled);
 		fMainTable.setEnabled(aEnabled);
 		if (!aEnabled) {
 			fAddButton.setEnabled(aEnabled);
 			fRemoveButton.setEnabled(aEnabled);
 		} else {
 			updateButtons();
 		}
 	}
 
 	/**
 	 * Method setVisible.
 	 * 
 	 * @param aVisible
 	 *            - boolean
 	 */
 	public void setVisible(boolean aVisible) {
 		fMainComposite.setVisible(aVisible);
 		fMainTable.setVisible(aVisible);
 		fAddButton.setVisible(aVisible);
 		fRemoveButton.setVisible(aVisible);
 	}
 
 	/**
 	 * Method getComposite.
 	 * 
 	 * @return Composite
 	 */
 	public Composite getComposite() {
 		return fMainComposite;
 	}
 
 	/**
 	 * Method setEditableValues.
 	 * 
 	 * @param aValues
 	 *            - String[]
 	 */
 	public void setEditableValues(String[] aValues) {
 		fValues = aValues;
 		if (null == fValues || 0 == fValues.length) {
 			fAddButton.setEnabled(false);
 		} else {
 			fAddButton.setEnabled(true);
 		}
 		if (0 == fMainTable.getItemCount()) {
 			fRemoveButton.setEnabled(false);
 		} else {
 			fRemoveButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Method setTableHeader.
 	 * 
 	 * @param aIndex
 	 *            int
 	 * @param aText
 	 *            String
 	 */
 	public void setTableHeader(int aIndex, String aText) {
 		try {
 			final TableColumn column = fMainTable.getColumn(aIndex);
 			column.setText(aText);
 			updateTable();
 		} catch (IllegalArgumentException e) {
 			R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * Method setToolTipText.
 	 * 
 	 * @param aTooltip
 	 *            String
 	 */
 	public void setToolTipText(String aTooltip) {
 		fMainComposite.setToolTipText(aTooltip);
 	}
 
 	/**
 	 * Method updateButtons.
 	 */
 	public void updateButtons() {
 		if (0 == fMainTable.getItemCount()) {
 			fRemoveButton.setEnabled(false);
 		} else {
 			fRemoveButton.setEnabled(true);
 		}
 		fAddButton.setEnabled(true);
 	}
 
 	/**
 	 * Method updateButtons.
 	 */
 	public void updateTable() {
 		for (TableColumn column : fMainTable.getColumns()) {
 			column.pack();
 		}
 	}
 
 	//Test methods
 	/**
 	 * Method remove.
 	 * 
 	 * @param removeStr
 	 */
 	public void remove(String removeStr) {
 		for (TableItem item : fMainTable.getItems()) {
 			if (item.getText().equals(removeStr)) {
 				fMainTable.setSelection(item);
 			}
 		}
 		fRemoveButton.notifyListeners(SWT.Selection, null);
 	}
 
 	/**
 	 * Method add.
 	 * 
 	 * @param addStr
 	 */
 	public void add(String addStr) {
 		fAddButton.notifyListeners(SWT.Selection, null);
 		if (null != fEditableControl) {
 			if (fEditableControl instanceof Text) {
 				((Text) fEditableControl).setText(addStr);
 			} else if (fEditableControl instanceof CCombo) {
 				String[] items = ((CCombo) fEditableControl).getItems();
 				for (int index = 0; index < items.length; index++) {
 					if (items[index].equals(addStr)) {
 						((CCombo) fEditableControl).select(index);
 					}
 				}
 			}
 		}
 	}
 }

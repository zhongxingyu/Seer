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
 
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
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
 import org.eclipse.swt.widgets.Item;
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
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field DEFAULT_TABLE_WIDTH. (value is 200)
 	 */
 	private static final int DEFAULT_TABLE_WIDTH = 300;
 
 	/**
 	 * Field DEFAULT_TABLE_HEIGHT. (value is 300)
 	 */
 	private static final int DEFAULT_TABLE_HEIGHT = 200;
 
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
 		fMainComposite = aToolkit.createComposite(aParent);
 		fMainComposite.setLayoutData(aLayoutData);
 		fMainComposite.setLayout(new GridLayout(4, false));
 		fMainTable = aToolkit.createTable(fMainComposite, SWT.FULL_SELECTION | SWT.BORDER);
 		final GridData tableData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		if (aEditableWidgetClass.equals(Date.class)) {
 			tableData.horizontalSpan = 1;
 		} else {
 			tableData.horizontalSpan = 2;
 		}
 		fMainTable.setLayoutData(tableData);
 		fMainTable.setLinesVisible(true);
 		fListener = aListener;
 		fInstanceId = aInstanceId;
 		fValues = aEditableValues;
 		createEditableListFromTable(aToolkit, aEditableWidgetClass);
 		fMainTable.setSize(DEFAULT_TABLE_WIDTH, DEFAULT_TABLE_HEIGHT); //default table size
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
 
 		final TableColumn tableColumn = new TableColumn(fMainTable, SWT.LEFT, 0);
 		final TableColumn tableColumn2;
 		fMainTable.setHeaderVisible(true);
 
 		if (aEditableWidgetClass.equals(Date.class)) {
 			tableColumn2 = new TableColumn(fMainTable, SWT.RIGHT, 1);
 			tableColumn.setText(R4EUIConstants.SPENT_TIME_COLUMN_HEADER);
 			tableColumn2.setText(R4EUIConstants.ENTRY_TIME_COLUMN_HEADER);
 			tableColumn.pack();
 			tableColumn2.pack();
 		} else {
 			tableColumn2 = null; //only 1 column
 		}
 
 		fMainComposite.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 
 				final Rectangle area = fMainTable.getClientArea();
 				final Point size = fMainTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 				final ScrollBar vBar = fMainTable.getVerticalBar();
 				int width = area.width - fMainTable.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
 				if (width < 0) {
 					return;
 				}
 
 				if (size.y > area.height + fMainTable.getHeaderHeight()) {
 					// Subtract the scrollbar width from the total column width
 					// if a vertical scrollbar will be required
 					final Point vBarSize = vBar.getSize();
 					width -= vBarSize.x;
 				}
 				final Point oldSize = fMainTable.getSize();
 				if (oldSize.x > area.width) {
 					// table is getting smaller so make the columns smaller first and then resize the table to
 					// match the client area width
 					if (aEditableWidgetClass.equals(Date.class)) {
 						tableColumn.setWidth(width / 3);
 						tableColumn2.setWidth(width - tableColumn.getWidth());
 					} else {
 						tableColumn.setWidth(width);
 					}
 					fMainTable.setSize(area.width, area.height);
 				} else {
 					// table is getting bigger so make the table bigger first and then make the columns wider
 					// to match the client area width
 					fMainTable.setSize(area.width, area.height);
 					if (aEditableWidgetClass.equals(Date.class)) {
 						tableColumn.setWidth(width / 3);
 						tableColumn2.setWidth(width - tableColumn.getWidth());
 					} else {
 						tableColumn.setWidth(width);
 					}
 				}
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
 
 		final Composite buttonsComposite = aToolkit.createComposite(fMainComposite);
 		buttonsComposite.setLayout(new GridLayout());
 		buttonsComposite.setLayoutData(new GridData(GridData.CENTER, SWT.TOP, false, false));
 
 		fAddButton = aToolkit.createButton(buttonsComposite, R4EUIConstants.BUTTON_ADD_LABEL, SWT.NONE);
 		if (aEditableWidgetClass.equals(CCombo.class)) {
 			if (null == fValues || 0 == fValues.length) {
 				fAddButton.setEnabled(false);
 			}
 		}
 		fRemoveButton = aToolkit.createButton(buttonsComposite, R4EUIConstants.BUTTON_REMOVE_LABEL, SWT.NONE);
 		if (0 == fMainTable.getItemCount()) {
 			fRemoveButton.setEnabled(false);
 		} else {
 			fRemoveButton.setEnabled(true);
 		}
 
 		fAddButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		fAddButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final TableItem newItem = new TableItem(fMainTable, SWT.NONE);
 				final Control editableControl;
 				if (aEditableWidgetClass.equals(Text.class)) {
 					editableControl = new Text(fMainTable, SWT.SINGLE | SWT.BORDER);
 					((Text) editableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							newItem.setText(((Text) editableControl).getText());
 						}
 					});
 					((Text) editableControl).addKeyListener(new KeyListener() {
 
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
 					editableControl = new CCombo(fMainTable, SWT.BORDER | SWT.READ_ONLY);
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
 					((CCombo) editableControl).setItems(currentValues.toArray(new String[currentValues.size()]));
 					((CCombo) editableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							newItem.setText(((CCombo) editableControl).getText());
 						}
 					});
 				} else if (aEditableWidgetClass.equals(Date.class)) {
 					editableControl = new Text(fMainTable, SWT.NONE);
 					final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 					final String[] data = { ((Text) editableControl).getText(),
 							dateFormat.format(Calendar.getInstance().getTime()) };
 					newItem.setText(data);
 					((Text) editableControl).addModifyListener(new ModifyListener() {
 						public void modifyText(ModifyEvent me) {
 							//Only accept numbers
 							String newText = ((Text) editableControl).getText();
 							try {
 								Integer.valueOf(newText);
 							} catch (NumberFormatException nfe) {
 								if (newText.length() > 0) {
 									newText = newText.substring(0, newText.length() - 1);
 									((Text) editableControl).setText(newText);
 									((Text) editableControl).setSelection(newText.length());
 								}
 							}
 							newItem.setText(0, newText);
 						}
 					});
 				} else {
 					return;
 				}
 				editableControl.addFocusListener(new FocusListener() {
 					public void focusLost(FocusEvent fe) {
 						((Control) fe.getSource()).dispose();
 
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
 
 					public void focusGained(FocusEvent fe) {
 						//Nothing to do
 					}
 				});
 
 				editableControl.setFocus();
 				final TableEditor editor = new TableEditor(fMainTable);
 				editor.grabHorizontal = true;
 				editor.grabVertical = true;
 				editor.setEditor(editableControl, newItem, 0);
 				fRemoveButton.setEnabled(true);
 				fMainTable.showItem(newItem);
 				fMainTable.redraw();
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
 	 * Method clearAll.
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
		if (aEnabled == false) {
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
 }

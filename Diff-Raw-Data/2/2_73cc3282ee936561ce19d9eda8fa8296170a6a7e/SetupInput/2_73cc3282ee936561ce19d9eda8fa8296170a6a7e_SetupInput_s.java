 package com.follett.mywebapp.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.follett.mywebapp.client.mywebapp.AddSetupHandler;
 import com.follett.mywebapp.util.SetupDataItem;
 import com.follett.mywebapp.util.TableData;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 
 public class SetupInput {
 
 	private SetupBuilderServiceAsync setupBuildingService = GWT.create(SetupBuilderService.class);
 	private LayoutPanel setupPanel;
 	private ArrayList<AddSetupHandler> sHandler;
 
 	public SetupInput (LayoutPanel setupPanel, ArrayList<AddSetupHandler> sHandler) {
 		this.setupPanel = setupPanel;
 		this.setupPanel.add(buildSetupPanel());
 		this.sHandler = sHandler;
 	}
 
 	public DialogBox createSetupDialogBox() {
 		// Create a dialog box and set the caption text
 		final DialogBox dialogBox = new DialogBox(false);
 
 		Button closeButton = new Button(
 				"Close", new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						resetSetup();
 						dialogBox.hide();
 					}
 				});
 
 		//evaluate the size of their window and make this the bulk of it.
 		dialogBox.setWidget(buildSetupDialogPanel(closeButton));
 		dialogBox.setGlassEnabled(true);
 		return dialogBox;
 	}
 
 	private LayoutPanel buildSetupDialogPanel(Button closeButton) {
 		final LayoutPanel panel = new LayoutPanel();
 		panel.setSize("1400px", "700px");
 		final Tree setupTree = new Tree();
 		final SetupDataItem allData = new SetupDataItem();
 		final Button addTab = new Button("Add Tab");
 		final FlexTable mainTable = new FlexTable();
 		final Button saveButton = new Button("Save All");
 		final ArrayList<String> tagIndex = new ArrayList<String>();
 
 		panel.add(setupTree);
 		panel.setWidgetLeftWidth(setupTree, 1, Unit.EM, 20, Unit.EM);
 		panel.setWidgetTopHeight(setupTree, 1, Unit.EM, 40, Unit.EM);
 		panel.add(mainTable);
 		panel.setWidgetLeftWidth(mainTable, 21, Unit.EM, 100, Unit.EM);
 		panel.setWidgetTopHeight(mainTable, 1, Unit.EM, 40, Unit.EM);
 		panel.add(saveButton);
 		panel.setWidgetLeftWidth(saveButton, 1, Unit.EM, 10, Unit.EM);
 		panel.setWidgetBottomHeight(saveButton, 1, Unit.EM, 3, Unit.EM);
 		panel.add(addTab);
 		panel.setWidgetLeftWidth(addTab, 12, Unit.EM, 10, Unit.EM);
 		panel.setWidgetBottomHeight(addTab, 1, Unit.EM, 3, Unit.EM);
 		panel.add(closeButton);
 		panel.setWidgetLeftWidth(closeButton, 23, Unit.EM, 10, Unit.EM);
 		panel.setWidgetBottomHeight(closeButton, 1, Unit.EM, 3, Unit.EM);
 
 	    if (this.setupBuildingService == null) {
 	    	this.setupBuildingService = GWT.create(SetupBuilderService.class);
 	    }
 
 	    // Set up the callback object.
 	    AsyncCallback<SetupDataItem> callback = new AsyncCallback<SetupDataItem>() {
 	      public void onFailure(Throwable caught) {
 	      }
 
 	      @Override
 	      public void onSuccess(SetupDataItem result) {
 	    	  allData.setData(result);
 	    	  ArrayList<String> tabs = result.getTabs();
 	    	  for (String tab : tabs) {
 	    		  TreeItem tabItem = new TreeItem(tab);
 	    		  ArrayList<String> columns = result.getColumnsOnTab(tab);
 	    		  for (String column : columns) {
 	    			  TreeItem columnItem = new TreeItem(column);
 	    			  tabItem.addItem(columnItem);
 	    		  }
 	    		  setupTree.addItem(tabItem);
 	    	  }
 	      }
 	    };
 
 	    class SetupHandler implements SelectionHandler<TreeItem>{
 	    	String lastColumn = null;
 	    	String[] title = {"Internal Field tagID.", "Displaying Label.", "Number of associated text fields.", "Description of associated text fields."};
 
 			@Override
 			public void onSelection(SelectionEvent<TreeItem> event) {
 				TreeItem selected = event.getSelectedItem();
 				mainTable.removeAllRows();
 				if(selected.getParentItem() != null) {
 					//I could set the lock here! lock down the column while its being edited... but then I would need to save once the column is left.
 					EnterPressHandler enter = new EnterPressHandler();
 					RadioButton checkBox = new RadioButton("checkType", "Check Box");
 					RadioButton radioButton = new RadioButton("checkType", "Radio Button");
 					RadioChangeHandler handler = new RadioChangeHandler();
 					checkBox.addClickHandler(handler);
 					radioButton.addClickHandler(handler);
 					TextBox tableBox;
 					tableBox = new TextBox();
 					tableBox.setText(selected.getText());
 					tableBox.addKeyPressHandler(enter);
 					tableBox.addBlurHandler(enter);
 					mainTable.setWidget(0, 0, tableBox);
 					mainTable.setWidget(1, 0, checkBox);
 					mainTable.setWidget(1, 1, radioButton);
 					String[] columnHeaders = {"TagID", "Diplay", "Editable Fields", "Description of fields"};
 					for(int c = 0; c < 4; c++) {
 						mainTable.setText(2,c,columnHeaders[c]);
 					}
 					int a = 0;
 					int rowOffset = 3;
 					ArrayList<TableData> columnData = allData.getData().get(selected.getText());
 					if(columnData == null) {
 						columnData = new ArrayList<TableData>();
 						checkBox.setValue(Boolean.TRUE);
 					}
 					int size = columnData.size() + 1;
 					TableData data;
 					Button removeStep;
 					for(a = 0; a < size; a++) {
 						if(a <= columnData.size()) {
 							if (a == columnData.size()) {
 								boolean value = (a > 0) ? columnData.get(a-1).isCheckbox() : checkBox.getValue().booleanValue();
 								data = new TableData(allData.getNextHighestTag(), "", value, Integer.valueOf(0));
 							} else {
 								data = columnData.get(a);
 							}
 							String[] textData = {data.getTagID(), data.getLabel(), data.getTextfields().toString(), data.getDescriptionsToString()};
 							if(data.isCheckbox() && !checkBox.getValue().booleanValue()) {
 								checkBox.setValue(Boolean.TRUE);
 							}
 							if(!data.isCheckbox() && !radioButton.getValue().booleanValue()) {
 								radioButton.setValue(Boolean.TRUE);
 							}
 							for(int b = 0; b < 4; b++) {
 								tableBox = new TextBox();
 								tableBox.addKeyPressHandler(enter);
 								tableBox.addBlurHandler(enter);
 								if(b == 0) {
 									tableBox.setEnabled(false);
 								}
 								if(b == 3) {
 									tableBox.setWidth("500px");
 								}
 								tableBox.setText(textData[b]);
 								tableBox.setTitle(this.title[b]);
 								mainTable.setWidget(a + rowOffset, b, tableBox);
 							}
 							removeStep = new Button("x");
 							removeStep.addClickHandler(new StepRemover(data.getTagID()));
 							mainTable.setWidget(a + rowOffset, 4, removeStep);
 
 						}
 					}
 					this.lastColumn = selected.getText();
 				} else {
 					String tabName = selected.getText();
 					TextBox tab = new TextBox();
 					tab.setText(tabName);
 					tab.setTitle("The displayed text on the tab");
 					mainTable.setText(0, 0, "The displayed text on the tab");
 					mainTable.setWidget(0, 1, tab);
 					mainTable.setText(1, 0, "The columns to appear within the tab");
 					ArrayList<String> columns = allData.getColumnsOnTab(tabName);
 					TabEnterPressHandler enter = new TabEnterPressHandler(tabName);
 					int a = 0;
 					TextBox box;
 					int size = (columns == null) ? 0: columns.size();
 					for(a = 0; a < size + 1; a++) {
 						box = new TextBox();
 						if(a < size) {
 							box.setText(columns.get(a));
 							box.setEnabled(false);
 						}
 						box.addKeyPressHandler(enter);
 						box.addBlurHandler(enter);
 						mainTable.setWidget(a+2, 0, box);
 					}
 					this.lastColumn = null;
 				}
 			}
 
 			class StepRemover implements ClickHandler{
 				private String tagID;
 
 				public StepRemover(String tagID) {
 					this.tagID = tagID;
 				}
 
 				@Override
 				public void onClick(ClickEvent event) {
 					mainTable.removeRow(tagIndex.indexOf(this.tagID)+4);
 					tagIndex.remove(this.tagID);
 					allData.removeTag(this.tagID);
 				}
 			}
 
 			class RadioChangeHandler implements ClickHandler{
 
 				@Override
 				public void onClick(ClickEvent event) {
 					RadioButton selected = (RadioButton)event.getSource();
 					ArrayList<TableData> columnData = allData.getDataforColumn(SetupHandler.this.lastColumn);
 					for (int a = 0; a < columnData.size(); a++) {
 						columnData.get(a).setCheckbox((selected.getText().equals("Check Box")) ? true : false);
 					}
 					allData.updateDataInColumn(SetupHandler.this.lastColumn, columnData);
 				}
 			}
 
 			class TabEnterPressHandler implements KeyPressHandler, BlurHandler{
 
 				String tabName;
 
 				//This needs to have duplicate column name checking
 
 				public TabEnterPressHandler(String tabName) {
 					this.tabName = tabName;
 				}
 
 				@Override
 				public void onKeyPress(KeyPressEvent event) {
 					Object source = event.getSource();
 					if(source instanceof TextBox) {
 						if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
 							addData();
 						}
 						if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
 							//Tab is firing a blurring event... the jerk
 						}
 					}
 				}
 
 				@Override
 				public void onBlur(BlurEvent event) {
 					Object source = event.getSource();
 					if(source instanceof TextBox) {
 						if(event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER &&
 							event.getNativeEvent().getKeyCode() != KeyCodes.KEY_TAB) {
 							addData();
 						}
 					}
 				}
 
 				private void addData() {
 					ArrayList<String> columns = new ArrayList<String>();
 					String columnToAdd;
 					String newTabName;
 					newTabName = ((TextBox)mainTable.getWidget(0, 1)).getText();
 					for(int row = 0; row < mainTable.getRowCount(); row++) {
 						if(mainTable.getWidget(row, 0) != null) {
 							columnToAdd = ((TextBox)mainTable.getWidget(row, 0)).getText();
 							if(!columnToAdd.isEmpty()) {
 								columns.add(columnToAdd);
 							}
 						}
 					}
 					if(allData.doesTabExist(newTabName)) {
 						allData.overWriteColumnsOnTab(newTabName, columns);
 					} else {
 						allData.overWriteTab(this.tabName, newTabName);
 						allData.updateTabWithColumns(this.tabName, newTabName, columns);
 						this.tabName = newTabName;
 					}
 					resetSetupTree(panel, setupTree, allData, setupTree.getSelectedItem());
 				}
 			}
 
 			class EnterPressHandler implements KeyPressHandler, BlurHandler{
 
 				//this is crap I know... I save the whole table everytime I lose focus or press the enter key
 
 				@Override
 				public void onKeyPress(KeyPressEvent event) {
 					Object source = event.getSource();
 					if(source instanceof TextBox) {
 						if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
 							addData();
 						}
 					}
 				}
 
 				@Override
 				public void onBlur(BlurEvent event) {
 					Object source = event.getSource();
 					if(source instanceof TextBox) {
 						addData();
 					}
 				}
 
 				private void addData() {
 					ArrayList<TableData> columnData = new ArrayList<TableData>();
 					String tagID;
 					String label;
 					Integer fields;
 					boolean checkbox;
 					TableData data;
 					for(int a = 3; a < mainTable.getRowCount(); a++) {
 						tagID = ((TextBox)mainTable.getWidget(a, 0)).getText();
 						label = ((TextBox)mainTable.getWidget(a, 1)).getText();
 						fields = Integer.valueOf(((TextBox)mainTable.getWidget(a, 2)).getText());
 						checkbox = ((RadioButton)mainTable.getWidget(1, 0)).getValue().booleanValue();
 						if(!label.isEmpty()) {
 							data = new TableData(tagID, label, checkbox, fields);
 							data.addDescriptions(((TextBox)mainTable.getWidget(a, 3)).getText());
 							columnData.add(data);
 						}
 					}
 					allData.updateDataInColumn(SetupHandler.this.lastColumn, columnData);
 					resetSetupTree(panel, setupTree, allData, setupTree.getSelectedItem());
 					//Still loses focus!! Get the focus back to the element they were in. Also tab button will cause a loss in focus(blur) but we don't want that to go away!
 //					source.setFocus(true);
 				}
 			}
 	    }
 
 	    SetupHandler treeHandler = new SetupHandler();
 	    setupTree.addSelectionHandler(treeHandler);
 
 		class TabAdder implements ClickHandler{
 
 			//bad garbage collection as I'm only ripping out the top level of the tree, all of the associated children are probably still there.
 			@Override
 			public void onClick(ClickEvent event) {
 				allData.addTab("New Tab");
 				resetSetupTree(panel, setupTree, allData);
 			}
 		}
 
 		TabAdder tabHandler = new TabAdder();
 		addTab.addClickHandler(tabHandler);
 
 		class SaveAll implements ClickHandler{
 
 			@Override
 			public void onClick(ClickEvent event) {
 			    if (SetupInput.this.setupBuildingService == null) {
 			    	SetupInput.this.setupBuildingService = GWT.create(SetupBuilderService.class);
 			    }
 
 				AsyncCallback<Boolean> callbackSave = new AsyncCallback<Boolean>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.out.print("Failure!");
 					}
 
 					@Override
 					public void onSuccess(Boolean result) {
 						if(result.booleanValue()) {
 							System.out.print("Exception!");
 						}
 					}
 				};
 				SetupInput.this.setupBuildingService.saveSetupData(allData, callbackSave);
 				SetupInput.this.setupBuildingService = null;
 			}
 		}
 
 		SaveAll saveHandler = new SaveAll();
 		saveButton.addClickHandler(saveHandler);
 
 	    this.setupBuildingService.getSetupData(callback);
 
 		return panel;
 	}
 
 	private void resetSetupTree(final LayoutPanel panel, final Tree setupTree,
 			final SetupDataItem allData) {
 		resetSetupTree(panel, setupTree, allData, null);
 	}
 
 	private void resetSetupTree(final LayoutPanel panel, final Tree setupTree,
 			final SetupDataItem allData, TreeItem selected) {
 		TreeItem selectedItem = null;
 		String selectedText = null;
 		if(selected != null) {
 			selectedText = selected.getText();
 		}
 		panel.remove(setupTree);
 		setupTree.removeItems();
 		ArrayList<String> tabs = allData.getTabs();
 		for (String tab : tabs) {
 			TreeItem tabItem = new TreeItem(tab);
 			if(selected != null && tab.equals(selectedText)) {
 				selectedItem = tabItem;
 			}
 			ArrayList<String> columns = allData.getColumnsOnTab(tab);
 			if(!(columns == null || columns.isEmpty())) {
 				for (String column : columns) {
 					TreeItem columnItem = new TreeItem(column);
 					tabItem.addItem(columnItem);
 					if(selected != null && column.equals(selectedText)) {
 						selectedItem = columnItem;
 					}
 				}
 			}
 			setupTree.addItem(tabItem);
 		}
 		panel.add(setupTree);
 		panel.setWidgetLeftWidth(setupTree, 1, Unit.EM, 20, Unit.EM);
 		panel.setWidgetTopHeight(setupTree, 1, Unit.EM, 40, Unit.EM);
 		if(selectedItem != null) {
 			if(selectedItem.getParentItem() != null) {
 				selectedItem.getParentItem().setState(true);
 			}
 			setupTree.setSelectedItem(selectedItem);
 			setupTree.getSelectedItem().setState(selected.getState());
 		}
 	}
 
 	public TabLayoutPanel buildSetupPanel() {
 		final TabLayoutPanel localSetupPanel = new TabLayoutPanel(.7, Unit.CM);
 
 		// Initialize the service proxy.
 	    if (this.setupBuildingService == null) {
 	    	this.setupBuildingService = GWT.create(SetupBuilderService.class);
 	    }
 
 	    // Set up the callback object.
 	    AsyncCallback<SetupDataItem> callback = new AsyncCallback<SetupDataItem>() {
 	      public void onFailure(Throwable caught) {
 	      }
 
 	      @Override
 	      public void onSuccess(SetupDataItem result) {
 	    	  ArrayList<String> tabList = result.getTabs();
 	    	  for (String tab : tabList) {
 	    		  LayoutPanel panel = new LayoutPanel();
 	    		  buildPanel(panel, result.getColumnsOnTab(tab), result.getData());
 	    		  localSetupPanel.add(panel, tab);
 	    	  }
 	      }
 
 	      private void buildPanel(LayoutPanel panel, ArrayList<String> columns, HashMap<String,ArrayList<TableData>> tableData) {
 	    	  final ArrayList<Object> boxesAndButtons = new ArrayList<Object>();
 	    	  final HashMap<String, TableData> allData = new HashMap<String, TableData>();
 	    	  final FlexTable table = new FlexTable();
 	    	  ArrayList<TableData> columnData;
 	    	  int a = 0;
 	    	  for (String columnHeader : columns) {
 	    		  table.setText(0, a, columnHeader);
 	    		  columnData = tableData.get(columnHeader);
 	    		  int b = 1;
 	    		  for (TableData data : columnData) {
 	    			  if(data.isCheckbox()) {
 	    				  CheckBox box = new CheckBox(data.getLabel());
 	    				  boxesAndButtons.add(box);
 	    				  table.setWidget(b, a, box);
 	    			  }else {
 	    				  RadioButton button = new RadioButton(columnHeader, data.getLabel());
 	    				  boxesAndButtons.add(button);
 	    				  table.setWidget(b, a, button);
 	    			  }
 	    			  allData.put(data.getLabel(), data);
 	    			  b++;
 	    		  }
 	    		  a++;
 	    	  }
 
 	    	  Button addSetupButton = new Button("Add this!");
	    	  SetupInput.this.sHandler.add(new AddSetupHandler(allData, boxesAndButtons));
 	    	  addSetupButton.addClickHandler(sHandler.get(sHandler.size()));
 	    	  table.setWidget(0, a, addSetupButton);
 	    	  panel.add(table);
 	      }
 	    };
 	    this.setupBuildingService.getSetupData(callback);
 		return localSetupPanel;
 	}
 
 	public void resetSetup() {
 		this.setupPanel.remove(0);
 		this.setupPanel.add(buildSetupPanel());
 	}
 }

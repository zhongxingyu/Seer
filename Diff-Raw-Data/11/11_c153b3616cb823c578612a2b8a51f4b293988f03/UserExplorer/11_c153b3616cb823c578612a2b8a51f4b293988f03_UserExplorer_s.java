 package edu.nrao.dss.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.http.client.RequestBuilder;
 
 public class UserExplorer extends Explorer {
 
 	public UserExplorer() {
 		super("/users", new UserType());
 		initFilters();
 		initLayout(initColumnModel(), false);
 		initUserToolBar();
 		
 	}
 	
 	private void initFilters() {
 		filterAction = new Button("Filter");
 		filterAction.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be){
 				String filtersURL = "?";
 				
 				SimpleComboValue<String> value;
 				String[] filterNames = new String[] {};
 				for (int i = 0; i < advancedFilters.size(); i++) {
 					value = advancedFilters.get(i).getValue();
 					if (value != null) {
 						filtersURL += (filtersURL.equals("?") ? filterNames[i] + "=" : "&" + filterNames[i] + "=") + value.getValue();
 					}
 				}
 
 				String filterText = filter.getTextField().getValue();
 				if (filterText != null) {
 					filterText = filtersURL.equals("?") ? "filterText=" + filterText : "&filterText=" + filterText;
 				} else {
 					filterText = "";
 				}
 				String url = getRootURL() + filtersURL + filterText;
 				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 				DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> proxy = getProxy();
 				proxy.setBuilder(builder);
 				loadData();
 				
 			}
 		});
 	}
 
 	private void initUserToolBar() {
 		final PagingToolBar pagingToolBar = new PagingToolBar(50);
 		final TextField<String> pages = new TextField<String>();
 		pages.setWidth(30);
 		pages.setValue("50");
 		pages.addKeyListener(new KeyListener() {
 			public void componentKeyPress(ComponentEvent e) {
 				if (e.getKeyCode() == 13) {
 					int page_size = Integer.valueOf(pages.getValue()).intValue();
 					pagingToolBar.setPageSize(page_size);
 					setPageSize(page_size);
 					loadData();
 				}
 			}
 		});
 		pages.setTitle("Page Size");
 		pagingToolBar.add(pages);
 		setBottomComponent(pagingToolBar);
 		pagingToolBar.bind(loader);
 		
 		ToolBar toolBar = new ToolBar();
 		setTopComponent(toolBar);
 		
 		filter = new FilterItem(UserExplorer.this);
 		toolBar.add(filter.getTextField());
 
 		for (SimpleComboBox<String> f : advancedFilters) {
 			toolBar.add(new SeparatorToolItem());
 		    toolBar.add(f);
 		}
 		toolBar.add(new SeparatorToolItem());
 		Button reset = new Button("Reset");
 		reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				for (SimpleComboBox<String> f : advancedFilters) {
 					f.reset();
					filter.getTextField().setValue("");
 				}
 			}
 		});
 		toolBar.add(reset);
 		toolBar.add(new SeparatorToolItem());
 		if (filterAction != null) {
 			toolBar.add(filterAction);
 		}
 		
 		toolBar.add(new FillToolItem());
 		toolBar.add(new SeparatorToolItem());
 
 		saveItem = new Button("Save");
 		toolBar.add(saveItem);
 
 		// Commit outstanding changes to the server.
 		saveItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				setCommitState(true);
 				store.commitChanges();
 				setCommitState(false);
 				loadData();
 				grid.getView().refresh(true);
 			}
 		});
 	}
 
 	private ColumnModel initColumnModel() {
 		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 		
 		ColumnConfig column = new ColumnConfig("username", "Username", 100);
 		CellEditor editor   = new CellEditor(new TextField<String>());
 	    editor.disable();
 	    column.setEditor(editor);
 	    configs.add(column);
 	    
 	    column = new ColumnConfig("first_name", "First Name", 100);
 	    column.setEditor(new CellEditor(new TextField<String>()));
 	    configs.add(column);
 	    
 	    column = new ColumnConfig("last_name", "Last Name", 100);
 	    column.setEditor(new CellEditor(new TextField<String>()));
 	    configs.add(column);
 	    
 	    CheckColumnConfig checkColumn = new CheckColumnConfig("sanctioned", "Sanctioned?", 65);
 	    checkColumn.setEditor(new CellEditor(new CheckBox()));
 	    configs.add(checkColumn);
 	    checkBoxes.add(checkColumn);
 	    
 	    column = new ColumnConfig("role", "Role", 80);
 	    column.setEditor(initCombo(new String[]{"Administrator", "Observer", "Operator"}));
 	    configs.add(column);
 	    
 	    column = new ColumnConfig("projects", "Projects", 800);
 	    editor = new CellEditor(new TextField<String>());
 	    editor.disable();
 	    column.setEditor(editor);
 	    configs.add(column);
 	    
 	    return new ColumnModel(configs);
 	}
 
 }

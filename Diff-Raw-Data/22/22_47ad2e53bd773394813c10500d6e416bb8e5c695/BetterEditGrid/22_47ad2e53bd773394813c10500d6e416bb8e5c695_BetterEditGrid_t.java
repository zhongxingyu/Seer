 package com.scholastic.sbam.client.uiobjects.foundation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.data.BaseListLoader;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelFactory;
 import com.extjs.gxt.ui.client.data.BeanModelLookup;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.ListLoadResult;
 import com.extjs.gxt.ui.client.data.ListLoader;
 import com.extjs.gxt.ui.client.data.ModelData; 
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ColumnModelEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Record;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.grid.RowEditor;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.FieldValidationServiceAsync;
 import com.scholastic.sbam.client.uiobjects.uitop.HelpTextDialog;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.client.validation.AsyncTextField;
 import com.scholastic.sbam.shared.objects.BetterRowEditInstance;
 
 public abstract class BetterEditGrid<I extends BetterRowEditInstance> extends LayoutContainer implements AppSleeper {
 	
 	private static final int COLUMN_WIDTH_PADDING	= 2; //20;
 	private static final int GRID_WIDTH_PADDING		= 20; //20;
 	
 	protected String				helpTextId;
 	
 	protected ListStore<BeanModel>	store;
 	protected Grid<BeanModel>		grid;
 	protected ContentPanel 			panel;
 	
 	protected List<BeanModel> 		selection;
 	
 	/**
 	 * The heading of the enclosing panel.
 	 */
 	protected String				panelHeading		= null;
 	/**
 	 * The ID of the column which will expand to absorb unused width.
 	 */
 	protected String				autoExpandColumn	= null;
 	/**
 	 * Arbitrary, additional padding to add to the width of the grid if the width is autocalculated (i.e. forceWidth <= 0).
 	 */
 	protected int					additionalWidthPadding = 0;
 	/**
 	 * The width to force the grid into.  If set to negative or zero, this will be computed from the width of the columns added.
 	 */
 	protected int					forceWidth			= -1;
 	/**
 	 * The height to force the grid into.  If set to negative or zero, the height will be determined by GXT.
 	 */
 	protected int					forceHeight			= -1;
 	/**
 	 * The maximum height for an auto expand.
 	 */
 	protected int					maxHeight			= -1;
 	/**
 	 * Whether or not the grid should use automatic height change.
 	 */
 	protected boolean				autoHeight			= false;
 	/**
 	 * The label for the button to be used to create new grid rows.
 	 */
 	protected String				newButtonLabel		= "New";
 	/**
 	 * The label for the button to be used to refresh the grid data (if null or zero length string, then no refresh button will be available).
 	 */
 	protected String				refreshButtonLabel		= "Refresh";
 	/**
 	 * Should grid buttons (New, Refresh) be placed at the top or bottom of the layout.
 	 */
 	protected boolean				gridButtonsAtBottom	= true;
 	/**
 	 * The amount of space to leave at the top and bottom combined between the embedded panel and the container.
 	 */
 	protected int					verticalMargins = 20;
 	
 	public BetterEditGrid() {
 		super();
 		this.helpTextId = this.getClass().getName();
 		if (helpTextId.lastIndexOf('.') >= 0)
 			helpTextId = helpTextId.substring(helpTextId.lastIndexOf('.') + 1);
 	}
 	
 	public BetterEditGrid(final String autoExpandColumn) {
 		this();
 		this.autoExpandColumn = autoExpandColumn;
 	}
 	
 	public BetterEditGrid(final int forceWidth) {
 		this();
 		this.forceWidth = forceWidth;
 	}
 	
 	public BetterEditGrid(final int forceWidth, final String autoExpandColumn) {
 		this();
 		this.forceWidth = forceWidth;
 		this.autoExpandColumn = autoExpandColumn;
 	}
  	
 	@Override  
 	protected void onRender(Element parent, int index) {  
 		super.onRender(parent, index);
 		
 //		setLayout(new FitLayout());
 		
 //		setStyleAttribute("padding", "20px");
 		
 		panel = new ContentPanel() {
 			@Override
 			protected void initTools() {
 				addHelp(this);
 				super.initTools();
 			}		
 		};
 //		panel.setLayout(new CenterLayout());
 		
 		// loader and store  
 		ListLoader<ListLoadResult<ModelData>> loader = getLoader();
 		store = new ListStore<BeanModel>(loader);
 		
 		addStoreListeners();
 		
 		// column model
 		ColumnModel cm = getColumnModel();
 		
 		grid = new Grid<BeanModel>(store, cm);
 		setGridAttributes();
 
 		makeFilters();
 		
 		makeRowEditor();
 		
 		int width = cm.getTotalWidth() + additionalWidthPadding + (cm.getColumnCount() * COLUMN_WIDTH_PADDING) + GRID_WIDTH_PADDING;
 		
 		if (forceWidth >= 0)
 			width = forceWidth;
 		
 		if (panelHeading != null)
 			panel.setHeading(panelHeading);
 		else
 			panel.setHeaderVisible(false);
 
 //		System.out.println("forceHeight starts at " + forceHeight);
 //		if (getParent() != null && getParent().getOffsetHeight() > 0) {
 //			System.out.println("Using parent hieght " + getParent().getOffsetHeight());
 //			if (getParent().getOffsetHeight() > 200)
 //				forceHeight = getParent().getOffsetHeight() - 200;
 //			else
 //				forceHeight = (int) (getParent().getOffsetHeight() * 0.9);
 //			System.out.println("forceHeight set to " + forceHeight);
 //		}
 		
 		panel.setFrame(true);
 		panel.setWidth(width);
 		if (forceHeight > 0)
 			grid.setSize(width, forceHeight);
 		else {
 			grid.setWidth(width);
 		}
 		
 		addAutoHeight();
 		
 		//panel.setIcon(Resources.ICONS.table());
 		panel.setLayout(new FitLayout());
 		
 		resizePanelHeight();
 		addResizeListener();
 		
 		add(panel);
 		
 		grid.mask("Loading...");
 		loader.load();
 	}
 
 	
 	protected void addHelp(ContentPanel panel) {
 		if (helpTextId == null)
 			return;
 		
 		ToolButton helpBtn = new ToolButton("x-tool-help");
 //		if (GXT.isAriaEnabled()) {
 //			helpBtn.setTitle(GXT.MESSAGES.pagingToolBar_beforePageText());
 //		}
 		helpBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
 			public void handleEvent(ComponentEvent ce) {
 				HelpTextDialog htd = new HelpTextDialog(helpTextId);
 				htd.show();
 			}
 		});
 		panel.getHeader().addTool(helpBtn);
 	}
 	
 //	public void showThingy(Widget c) {
 //		if (c == null) {
 //			System.out.println("--------------------------");
 //			return;
 //		}
 //		if (c instanceof LayoutContainer) {
 //			LayoutContainer l = (LayoutContainer) c;
 //			System.out.println(c.getClass().getName() + ", height " + c.getOffsetHeight() + ", layout " + l.getLayout());
 //		} else
 //			System.out.println(c.getClass().getName() + ", height " + c.getOffsetHeight());
 //		showThingy(c.getParent());
 //	}
 //	@Override
 //	public void afterRender() {
 //		super.afterRender();
 //		System.out.println("afterRender");
 //		if (isRendered()) {
 //			showThingy(grid);
 //			setHeight(getParent().getOffsetHeight() - 50);
 //			layout(true);
 ////			System.out.println("Rendered " + getWidth() + " / " + getHeight());
 ////			System.out.println(grid + " is " + grid.getOffsetHeight());
 ////			System.out.println(panel + " is " + panel.getOffsetHeight());
 //			System.out.println("***********************");
 //			System.out.println(this.getClass().getName() + " is " + getOffsetHeight());
 //			System.out.println(getParent().getClass().getName() + " is " + getParent().getOffsetHeight());
 //			if (getParent().getParent() != null) System.out.println(getParent().getParent().getClass().getName() + " is " + getParent().getParent().getOffsetHeight());
 //		} else System.out.println("NOT rendered");
 //	}
 	
 	
 	/**
 	 * Add a listener to detect a change in the parent container size, and resize the grid panel
 	 */
 	public void addResizeListener() {
 		if (getParent() != null && getParent() instanceof LayoutContainer) {
 			LayoutContainer c = (LayoutContainer) getParent();
 			c.addListener(Events.Resize, new Listener<BaseEvent>() {
 
 				@Override
 				public void handleEvent(BaseEvent be) {
 					if (be.getType().getEventCode() == Events.ResizeEnd.getEventCode()) {
 						resizePanelHeight();
 					}
 				}
 				
 			});
 		}
 	}
 	
 	/**
 	 * Resize the panel height based on the parent container height
 	 */
 	public void resizePanelHeight() {
 		if (forceHeight <= 0 && getParent() != null && isRendered()) {
 			int newHeight = getParent().getOffsetHeight();
 			if (newHeight > verticalMargins)
 				newHeight -= verticalMargins;
 			if (!panel.isRendered() || panel.getHeight() != newHeight) {
 				panel.setHeight(newHeight);
 				if (isRendered())
 					layout(true);
 			}
 		}	
 	}
 	
 	protected void setGridAttributes() {
 		grid.setBorders(true);
 		grid.setStripeRows(true);
 		grid.setColumnLines(true);
 		grid.setColumnReordering(true);
 		grid.setLoadMask(true);
 		if (autoExpandColumn != null)
 			grid.setAutoExpandColumn(autoExpandColumn);
 		grid.getAriaSupport().setLabelledBy(panel.getHeader().getId() + "-label"); // access for people with disabilities -- ARIA
 		setGridView();
 	}
 	
 	protected void setGridView() {
 		if (grid.getView() != null)
 			grid.getView().setEmptyText("There is no data to display.");
 		else {
 			GridView gridView= new GridView();
 			gridView.setEmptyText("There is no data to display.");
 			grid.setView(gridView);
 		}
 	}
 	
 	protected ListLoader<ListLoadResult<ModelData>> getLoader() {
 		// proxy and reader  
 		RpcProxy<List<I>> proxy = new RpcProxy<List<I>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<List<I>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<List<I>> myCallback = new AsyncCallback<List<I>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Data load failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						callback.onFailure(caught);
 						grid.unmask();
 					}
 
 					public void onSuccess(List<I> list) {
 						callback.onSuccess(list);
 						grid.unmask();
 					}
 				};
 				
 				asyncLoad(loadConfig, myCallback);
 				
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		ListLoader<ListLoadResult<ModelData>> loader = new BaseListLoader<ListLoadResult<ModelData>>(proxy, reader);
 		return loader;
 	}
 	
 	/**
 	 * Implement this method to execute the actual asynchronous call to the loader service, i.e. to make the service call.
 	 * @param loadConfig
 	 * @param callback
 	 */
 	protected abstract void asyncLoad(Object loadConfig, AsyncCallback<List<I>> callback);
 	// example: { myLoadService.loadMyData(callback); }
 	
 	protected void addStoreListeners() {
 		store.addListener(Store.Update, new Listener<StoreEvent<BeanModel>>() {
             public void handleEvent(final StoreEvent<BeanModel> se) {
                 if (se.getOperation() == Record.RecordUpdate.COMMIT && se.getModel() != null) {
                 	asyncUpdate(se.getModel());
                 }
                 
             }
         });	
 	}
 	
 	public void refreshGridData() {
 		if (store != null && store.getLoader() != null) {
 			grid.mask("Loading");
 			store.getLoader().load();
 		}
 	}
 	
 	protected ColumnModel getColumnModel() {  
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		addColumns(columns);
 		
 		if (autoExpandColumn != null && autoExpandColumn.length() > 0) {
 			boolean hasAutoExpandColumn = false;
 			for (ColumnConfig column : columns) {
 				if (column.getId() != null && column.getId().equals(autoExpandColumn)) {
 					hasAutoExpandColumn = true;
 					break;
 				}
 			}
 			if (!hasAutoExpandColumn) {
 				ColumnConfig spacer = new ColumnConfig();
 				spacer.setId(autoExpandColumn);
 				spacer.setHeader("");
 				columns.add(spacer);
 			}
 		}
 		
 		return new ColumnModel(columns);
 	}
 	
 	/**
 	 * Implement this method to add the grid columns.
 	 * 
 	 * This method will basically be a series of columns.add(column) statements.
 	 * @param columns
 	 * The List of column configurations to which columns will be added.
 	 */
 	protected abstract void addColumns(List<ColumnConfig> columns);
 	
 	/**
 	 * Override this method to add any desired fitlers.
 	 */
 	protected void makeFilters() {
 
 	/* 
 	 * Sample code for adding filters
 	 * 
 		GridFilters filters = new GridFilters();  
 		filters.setLocal(true);  
 		    
 		NumericFilter numericFilter	 = new NumericFilter("numeric_column_field");  
 		StringFilter stringFilter	 = new StringFilter("string_column_field"); 
 		DateFilter   dateFilter		 = new DateFilter("date_column_field");  
 		BooleanFilter booleanFilter	 = new BooleanFilter("boolean_column_field");  
 		  
 		ListStore<ModelData> choiceStore = new ListStore<ModelData>();
 		for (int i = 0; i < whatever.length(); i++)
 			typeStore.add(whatever [i]);
 		ListFilter listFilter = new ListFilter("choice_column_field", choiceStore);  
 		listFilter.setDisplayProperty("choice_display_property");
 		
 		filters.addFilter(numericFilter);  
 		filters.addFilter(stringFilter);  
 		filters.addFilter(dateFilter);  
 		filters.addFilter(booleanFilter); 
 		filters.addFilter(listFilter);
 		
 		grid.addPlugin(filters);
 	*
 	*
 	*/
 	}
 	
 	protected void makeRowEditor() {
 		
 		final RowEditor<ModelData> re = new BetterRowEditor<ModelData>(store, true, getCustomRowButtons(), getUnchangeableKeyField());
 
 		grid.addPlugin(re);
 		
 		Button newButton = new Button(newButtonLabel);
 		IconSupplier.setIcon(newButton, IconSupplier.getNewIconName());
 		newButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			   
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				I instance = getNewInstance();
 				instance.setNewRecord(true);
 				 
 				re.stopEditing(false);
 				BeanModel userModel = getModel(instance);
 				store.insert(userModel, 0);
 				re.startEditing(store.indexOf(userModel), true);
 			 
 			}  
 		 
 		});
 		
 		Button refreshButton = null;
 		if (refreshButtonLabel != null && refreshButtonLabel.length() > 0) {
 			refreshButton = new Button(refreshButtonLabel);
 			IconSupplier.setIcon(refreshButton, IconSupplier.getRefreshIconName());
 			refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 				   
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					refreshGridData();
 				}  
 			 
 			});
 		}
 		
 		panel.setButtonAlign(HorizontalAlignment.CENTER);
 		
 		if (!gridButtonsAtBottom) {
 			panel.addButton(newButton);
 			if (refreshButton != null) panel.addButton(refreshButton);
 		}
 		
 		panel.add(grid);
 		
 		if (gridButtonsAtBottom) {
 			panel.addButton(newButton);
 			if (refreshButton != null) panel.addButton(refreshButton);
 		}
 	}
 	
 	/**
 	 * Override this method to return any single field from the column data which may not be changed on an existing row, but may be entered for a new row.
 	 * 
 	 * This may return null, if no such behavior is desired.
 	 * 
 	 * The default implementation returns the first field in the row.
 	 * @return
 	 */
 	public Field<?> getUnchangeableKeyField() {
 		return grid.getColumnModel().getColumns().get(0).getEditor().getField();
 	}
 	
 	/**
 	 * Override this method to return a list of buttons to be added after the Cancel/Save/Delete buttons.
 	 * 
 	 * This may return null, if no custom buttons are needed.
 	 */
 	public List<Button> getCustomRowButtons() {
 		return null;
 	}
 	
 	/**
 	 * Implement this method to return a newly initialized instance to use as the basis for creating a new persistent object.
 	 * @return
 	 */
 	protected abstract I getNewInstance();
 	
	protected BeanModel getModel(I instance) {
 		BeanModelFactory factory = BeanModelLookup.get().getFactory(instance.getClass());
 		BeanModel model = factory.createModel(instance);
 		return model;
 	}
 	
 	public void awaken() {
 		reload();
 	}
 	
 	public void sleep() {
 		clear();
 	}
 	
 	protected void reload() {
 		if (store != null) {
 			grid.mask("Loading...");
 			store.getLoader().load();
 		}
 		if (grid != null && grid.getSelectionModel() != null&& selection != null)
 			grid.getSelectionModel().setSelection(selection);
 	}
 	
 	protected void clear() {
 		if (grid != null && grid.getSelectionModel() != null)
 			selection = grid.getSelectionModel().getSelection();
 		if (store != null)
 			store.removeAll();
 	}
 
 	protected void doAutoHeight() { 
 		if (!autoHeight)
 			return;
 		 if (grid.isViewReady()) {  
 			grid.getView().getScroller().setStyleAttribute("overflowY", "hidden");  
 			int newHeight = (grid.getView().getBody().isScrollableX() ? 19 : 0) + grid.el().getFrameWidth("tb")  
 				+ grid.getView().getHeader().getHeight() + panel.getFrameHeight()  
 				+ grid.getView().getBody().firstChild().getHeight();
 			if (newHeight > maxHeight)
 				newHeight = maxHeight;
 			panel.setHeight(newHeight);  
 		}  
 	}  
 	
 	protected void addAutoHeight() {
 		if (!autoHeight)
 			return;
 		grid.addListener(Events.ViewReady, new Listener<ComponentEvent>() {  
 		  public void handleEvent(ComponentEvent be) {  
 		    grid.getStore().addListener(Store.Add, new Listener<StoreEvent<ModelData>>() {  
 		      public void handleEvent(StoreEvent<ModelData> be) {  
 		        doAutoHeight();  
 		      }  
 		    });  
 		    doAutoHeight();  
 		  }  
 		});  
 		grid.addListener(Events.ColumnResize, new Listener<ComponentEvent>() {  
 		  public void handleEvent(ComponentEvent be) {  
 		    doAutoHeight();  
 		  }  
 		});  
 		grid.getColumnModel().addListener(Events.HiddenChange, new Listener<ColumnModelEvent>() {  
 		  public void handleEvent(ColumnModelEvent be) {  
 		    doAutoHeight();  
 		  }  
 		});  
 	}
 	
 	/**
 	 * Utility method to create a basic display only text field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @return
 	 */
 	protected ColumnConfig getColumn(String name, String header, int width) {
 		return getColumn(name, header, width, null);
 	}
 	
 	/**
 	 * Utility method to create a basic date field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @return
 	 */
 	protected ColumnConfig getDateColumn(String name, String header, int width, String toolTip) {
 		return getDateColumn(name, header, width, toolTip, UiConstants.APP_DATE_TIME_FORMAT);
 	}
 
 
 	
 	/**
 	 * Utility method to create a basic date field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @param format
 	 *  The DateTimeFormat to use for the field.
 	 * @return
 	 */
 	protected ColumnConfig getDateColumn(String name, String header, int width, String toolTip, DateTimeFormat format) {
 		DateField dateField = new DateField();  
 		dateField.getPropertyEditor().setFormat(UiConstants.APP_DATE_TIME_FORMAT);
 		dateField.setReadOnly(true);
 		
 		ColumnConfig column = getColumn(name, header, width, toolTip);
 		column.setDateTimeFormat(format);
 		column.setEditor(new CellEditor(dateField));
 		return column;
 	}
 	
 	/**
 	 * Utility method to create a basic date field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @param toolTip
 	 *  The value of the tool tip to use for the field.
 	 * @return
 	 */
 	protected ColumnConfig getColumn(String name, String header, int width, String toolTip) {
 		ColumnConfig column = new ColumnConfig();
 		column.setId(name);
 		column.setHeader(header);
 		column.setWidth(width);
 		if (toolTip != null && toolTip.length() > 0)
 			column.setToolTip(toolTip);
 		return column;
 	}
 	
 	/**
 	 * Utility method to create a basic editable text field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @return
 	 */
 	protected ColumnConfig getEditColumn(String name, String header, int width) {
 		return getEditColumn(name, header, width, null);
 	}
 	
 	/**
 	 * Utility method to create a basic editable text field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @param toolTip
 	 *  The value of the tool tip to use for the field.
 	 * @return
 	 */
 	protected ColumnConfig getEditColumn(String name, String header, int width, String toolTip) {
 		ColumnConfig column = getColumn(name, header, width, toolTip);
 		TextField<String> text = new TextField<String>();
 		text.setName(name);
 		text.setAllowBlank(false);
 		column.setEditor(new CellEditor(text));
 		return column;
 	}
 
 	
 	/**
 	 * Utility method to create a basic editable asynchronous text field with custom synchronous and asynchronous validation.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @param toolTip
 	 *  The value of the tool tip to use for the field.
 	 * @param validator
 	 *  The validator to use for the field for local editing.
 	 * @param validationService
 	 *  The asynchronous validation service to use with the field.
 	 * @return
 	 */
 	protected ColumnConfig getEditColumn(String name, String header, int width, String toolTip, Validator validator, FieldValidationServiceAsync validationService) {
 		ColumnConfig column = getColumn(name, header, width, toolTip);
 		AsyncTextField<String> text = new AsyncTextField<String>();
 		text.setAllowBlank(false);
 		if (validator != null) {
 			text.setValidator(validator);
 		}
 		if (validationService != null) {
 			text.setValidationService(validationService);
 		}
 		column.setEditor(new CellEditor(text));
 		return column;
 	}
 	
 	protected ColumnConfig getEditDateColumn(String name, String header, int width, String toolTip, Validator validator) {
 		DateField dateField = new DateField();  
 		dateField.getPropertyEditor().setFormat(UiConstants.APP_DATE_TIME_FORMAT);  
 		
 		ColumnConfig column = getColumn(name, header, width, toolTip);  
 		column.setEditor(new CellEditor(dateField));  
 		column.setDateTimeFormat(UiConstants.APP_DATE_TIME_FORMAT);
 		return column;
 	}
 	
 	/**
 	 * Utility method to create a basic checkbox field.
 	 * @param name
 	 * 	The name of the field.
 	 * @param header
 	 * 	The header for the field.
 	 * @param width
 	 * 	The width of the field.
 	 * @param toolTip
 	 *  The value of the tool tip to use for the field.
 	 * @return
 	 */
 	protected ColumnConfig getEditCheckColumn(String name, String header, int width, String toolTip) {
 		CheckColumnConfig checkColumn = new CheckColumnConfig(name, header, width); 
 		if (toolTip != null && toolTip.length() > 0)
 			checkColumn.setToolTip(toolTip);
 		CellEditor checkBoxEditor = new CellEditor(new CheckBox());
 		checkColumn.setEditor(checkBoxEditor);
 		return checkColumn;
 	}
 	
 	/**
 	 * Utility method to create a combo box column.
 	 * @param name
 	 *  The name of the column.
 	 * @param header
 	 *  The header for the column.
 	 * @param width
 	 *  The width of the column.
 	 * @param values
 	 *  The list of valid values for the column.
 	 * @return
 	 */
 	protected ColumnConfig getComboColumn(String name, String header, int width, String toolTip, String [] values) {
 		return getComboColumn(name, header, width, toolTip, values, null);
 	}
 	
 	/**
 	 * Utility method to create a combo box column.
 	 * @param name
 	 *  The name of the column.
 	 * @param header
 	 *  The header for the column.
 	 * @param width
 	 *  The width of the column.
 	 * @param values
 	 *  The list of valid values for the column.
 	 * @param validator
 	 *  The validator to use for these values.
 	 * @return
 	 */
 	protected ColumnConfig getComboColumn(String name, String header, int width, String toolTip, String [] values, Validator validator) {
 		
 		final SimpleComboBox<String> combo = new SimpleComboBox<String>();
 		combo.setForceSelection(true);
 		combo.disableTextSelection(false);
 		combo.setTriggerAction(TriggerAction.ALL);
 		if (validator != null)
 			combo.setValidator(validator);
 	//	combo.setEditable(false);
 		
 		for (int i = 0; i < values.length; i++) {
 			combo.add(values [i]);
 		}
 		
 		CellEditor editor = new CellEditor(combo) {  
 		  @Override  
 		  public Object preProcessValue(Object value) {  
 		    if (value == null) {  
 		      return value;
 		    }  
 		    return combo.findModel(value.toString());
 		  }  
  
 		  @Override  
 		  public Object postProcessValue(Object value) {  
 		    if (value == null) {  
 		      return value;
 		    }  
 		    return ((ModelData) value).get("value");
 		  }  
 		};
 
 		ColumnConfig column = new ColumnConfig();
 		column.setId(name);
 		column.setHeader(header);
 		column.setWidth(width);
 		column.setEditor(editor);
 		if (toolTip != null && toolTip.length() > 0)
 			column.setToolTip(toolTip);
 	
 		return column; 
 	}
 	
 	protected ColumnConfig getComboColumn(String name, String header, int width, String toolTip, BeanModel [] values, String valueField, String displayField, Validator validator) {
 		
 		ListStore<BeanModel> listStore = new ListStore<BeanModel>();
 		for (int i = 0; i < values.length; i++) {
 			listStore.add(values [i]);
 		}
 		
 		return getComboColumn(name, header, width, toolTip, listStore, valueField, displayField, validator);
 	}
 	
 	protected ColumnConfig getComboColumn(String name, String header, int width, String toolTip, ListStore<BeanModel> listStore, String valueField, String displayField, Validator validator) {
 		
 		final ComboBox<BeanModel> combo = new ComboBox<BeanModel>();
 		combo.setForceSelection(true);
 		combo.disableTextSelection(false);
 		combo.setTriggerAction(TriggerAction.ALL);
 		combo.setDisplayField(displayField);
 		combo.setValueField(valueField);
 		if (validator != null)
 			combo.setValidator(validator);
 	//	combo.setEditable(false);
 		
 		combo.setStore(listStore);
 		
 		CellEditor editor = new CellEditor(combo);
 
 		ColumnConfig column = new ColumnConfig();
 		column.setId(name);
 		column.setHeader(header);
 		column.setWidth(width);
 		column.setEditor(editor);
 		if (toolTip != null && toolTip.length() > 0)
 			column.setToolTip(toolTip);
 	
 		return column; 
 	}
 	
 	/**
 	 * Implement this method to make the asynchronous call to perform an actual update.
 	 * 
 	 * @param beanModel
 	 * Save/use the bean model, as needed, to reflect changes enforced by response from the service call.
 	 */
 	protected abstract void asyncUpdate(BeanModel beanModel);
 	
 	public String getFailureMessage() {
 		return "Update failed unexpectedly.";
 	}
 
 	public ListStore<BeanModel> getStore() {
 		return store;
 	}
 
 	public void setStore(ListStore<BeanModel> store) {
 		this.store = store;
 	}
 
 	public Grid<BeanModel> getGrid() {
 		return grid;
 	}
 
 	public void setGrid(Grid<BeanModel> grid) {
 		this.grid = grid;
 	}
 
 	public ContentPanel getPanel() {
 		return panel;
 	}
 
 	public void setPanel(ContentPanel panel) {
 		this.panel = panel;
 	}
 
 	public int getAdditionalWidthPadding() {
 		return additionalWidthPadding;
 	}
 
 	public void setAdditionalWidthPadding(int additionalWidthPadding) {
 		this.additionalWidthPadding = additionalWidthPadding;
 	}
 
 	public String getAutoExpandColumn() {
 		return autoExpandColumn;
 	}
 
 	public void setAutoExpandColumn(String autoExpandColumn) {
 		this.autoExpandColumn = autoExpandColumn;
 	}
 
 	public boolean isGridButtonsAtBottom() {
 		return gridButtonsAtBottom;
 	}
 
 	public void setGridButtonsAtBottom(boolean gridButtonsAtBottom) {
 		this.gridButtonsAtBottom = gridButtonsAtBottom;
 	}
 
 	public int getForceHeight() {
 		return forceHeight;
 	}
 
 	public void setForceHeight(int forceHeight) {
 		this.forceHeight = forceHeight;
 	}
 
 	public int getForceWidth() {
 		return forceWidth;
 	}
 
 	public void setForceWidth(int forceWidth) {
 		this.forceWidth = forceWidth;
 	}
 
 	public String getRefreshButtonLabel() {
 		return refreshButtonLabel;
 	}
 
 	public void setRefreshButtonLabel(String refreshButtonLabel) {
 		this.refreshButtonLabel = refreshButtonLabel;
 	}
 
 	public String getNewButtonLabel() {
 		return newButtonLabel;
 	}
 
 	public void setNewButtonLabel(String newButtonLabel) {
 		this.newButtonLabel = newButtonLabel;
 	}
 
 	public String getPanelHeading() {
 		return panelHeading;
 	}
 
 	public void setPanelHeading(String panelHeading) {
 		this.panelHeading = panelHeading;
 		if (panel != null)
 			panel.setHeading(panelHeading);
 	}
 
 	public int getMaxHeight() {
 		return maxHeight;
 	}
 
 	public void setMaxHeight(int maxHeight) {
 		this.maxHeight = maxHeight;
 	}
 
 	public boolean isAutoHeight() {
 		return autoHeight;
 	}
 
 	public void setAutoHeight(boolean autoHeight) {
 		this.autoHeight = autoHeight;
 	}
 
 	public int getVerticalMargins() {
 		return verticalMargins;
 	}
 
 	public void setVerticalMargins(int verticalMargins) {
 		this.verticalMargins = verticalMargins;
 	}
 
 	public String getHelpTextId() {
 		return helpTextId;
 	}
 
 	public void setHelpTextId(String helpTextId) {
 		this.helpTextId = helpTextId;
 	}
 
 }

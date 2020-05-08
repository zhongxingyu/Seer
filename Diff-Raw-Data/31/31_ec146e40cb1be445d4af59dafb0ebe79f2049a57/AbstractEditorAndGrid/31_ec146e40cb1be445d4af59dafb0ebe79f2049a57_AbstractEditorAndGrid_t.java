 package topshelf.gwt.editor.client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import topshelf.gwt.editor.client.AbstractEditorAndGrid.CrudGridOptions;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.Duration;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorContext;
 import com.google.gwt.editor.client.EditorVisitor;
 import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.client.adapters.ListEditor;
 import com.google.gwt.editor.client.impl.AbstractEditorContext;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent.Handler;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
 import com.google.web.bindery.requestfactory.shared.EntityProxy;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.Request;
 import com.google.web.bindery.requestfactory.shared.RequestContext;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 public abstract class AbstractEditorAndGrid<E extends EntityProxy,
 	ENTITYCTX extends RequestContext,
 	LISTCTX extends RequestContext,
 	LISTOPTIONS extends CrudGridOptions> extends Composite {
 	
 	public static interface CrudGridOptions {
 		public int getFirstResult();
 		public void setFirstResult(int firstResult);
 		public int getMaxResults();
 		public void setMaxResults(int maxResults);
 	}
 	
 	public static enum EditorOptionRequest {
 		SAVE,DELETE,CANCEL,COPY,CUSTOM;
 	}
 	
 	interface B extends UiBinder<Widget, AbstractEditorAndGrid<?,?,?,?>> {
 		static B I = GWT.create(B.class);
 	}
 
 	private List<EditorContext<?>> ignoredEditorsToVisit = new ArrayList<EditorContext<?>>();
 	private boolean ignoreRangeChange = false;
 	private boolean listLoaded = false;
 	private ArrayList<E> providerData;
 	private AsyncDataProvider<E> provider;
 	
 	Class<E> proxyType;
 	ENTITYCTX editorRequestContext;
 	EditorOptionRequest currentEditorOptionRequest = null;
 	boolean editing = false;
 	E selected = null;
 	E staged = null;
 	int providerDisplayFirst = -1;
 	long totalRowCount = -1;
 	Editor<E> editor;
 	Widget editorWidget;
 	String[] listProperties = new String[0];
 	String[] editorProperties;
 
 	private RequestFactoryEditorDriver<E, Editor<E>> driver;
 	
 	@UiField(provided = true) DataGrid<E> dataGrid;
 	@UiField(provided = true) SimplePager pager;
 	@UiField SimplePanel editorPanel;
 	@UiField ListBox pageSize;
 	@UiField InlineLabel message;
 	@UiField SplitLayoutPanel frame;
 	@UiField DockLayoutPanel editorFrame;
 	@UiField SimplePanel extraEditorControls;
 	@UiField SimplePanel extraListControls;
 
 	protected void setExtraEditorControls(Widget controls) {
 		extraEditorControls.add(controls);
 	}
 
 	protected void setExtraListControls(Widget controls) {
 		extraListControls.add(controls);
 	}
 	
 	protected void setListProperties(String... listProperties) {
 		this.listProperties = listProperties;
 	}
 	
 	protected abstract void createColumns(DataGrid<E> dataGrid);
 	protected abstract LISTCTX createListRequestContext();
 	protected abstract ENTITYCTX createEditorRequestContext();
 	protected abstract Request<List<E>> getPage(LISTCTX ctx, LISTOPTIONS options);
 	protected abstract Request<Long> getTotalRowCount(LISTCTX ctx, LISTOPTIONS options);
 	public abstract E copyProxy(ENTITYCTX ctx, E original);
 	public abstract void setupStagedProxy(ENTITYCTX ctx, E staged, boolean isNew);
 
 	protected abstract void modalAlertShow(String msg);
 	protected abstract void modalAlertCancel();
 	
 	protected abstract void editorRequest(E entity, ENTITYCTX reqCtx, EditorOptionRequest optionRequest,
 			Receiver<E> updateRcvr, Receiver<Void> actionRcvr);
 	protected abstract LISTOPTIONS getListOptions(LISTCTX reqCtx);
 	protected abstract boolean removeItemInListOnSave(EditorOptionRequest editorOptionRequest);
 
 	/**
 	 * Override this if, for example, you need to call a service to get a refreshed
 	 * entity instead of using the RequestContext's finder.
 	 * 
 	 * @param entity
 	 * @return refresher Request
 	 */
 	@SuppressWarnings("unchecked")
 	protected Request<E> refreshEntityForEditor(E entity) {
 		return (Request<E>) createListRequestContext().find(entity.stableId());
 	}
 
 	// must call 'init()' after zero-arg constructor
 	public AbstractEditorAndGrid() {}
 
 	public AbstractEditorAndGrid(Class<E> proxyType, Editor<E> editor,
 			RequestFactoryEditorDriver<E, Editor<E>> driver, String... editorProperties) {
 		init(proxyType, editor, driver, editorProperties);
 	}
 	
 	public void init(Class<E> proxyType, Editor<E> editor,
 			RequestFactoryEditorDriver<E, Editor<E>> driver, String... editorProperties) {
 
 		this.proxyType = proxyType;
 		this.editorProperties = editorProperties;
 		this.editor = editor;
 		if (editor instanceof Widget) {
 			this.editorWidget = (Widget)editor;			
 		} else if (editor instanceof IsWidget) {
 			this.editorWidget = ((IsWidget)editor).asWidget();
 		} else {
 			throw new RuntimeException("Editor needs to be a Widget or IsWidget");
 		}
 		this.driver = driver;
 		driver.initialize(editor);
 
 		// init grid w/ key provider
 		@SuppressWarnings("unchecked")
 		ProvidesKey<E> keyProvider = (ProvidesKey<E>)EntityKeyProvider.getInstance();
 		dataGrid = new DataGrid<E>(keyProvider);
 		dataGrid.setSelectionModel(new SingleSelectionModel<E>(
 				new SingleSelectionModel<E>(keyProvider)));
 
 		// create pager for grid
 		SimplePager.Resources resources = GWT
 				.create(SimplePager.Resources.class);
 		pager = new SimplePager(TextLocation.CENTER, resources, false, 0, true);
 		pager.setDisplay(dataGrid);
 
 		// create cols
 		createCheckColumn(dataGrid);
 		createColumns(dataGrid);
 		
 		// bind widget to ui.xml
 		initWidget(B.I.createAndBindUi(this));
 		
 		pageSize.addItem("10");
 		pageSize.addItem("50");
 		pageSize.addItem("100");
 		pageSize.addItem("500");
 		pageSize.addItem("1000");
 		pageSize.setVisibleItemCount(1); // drop down, not multi-select
 		pageSize.setItemSelected(1, true); // select "50"
 		pageSize.addChangeHandler(new ChangeHandler() {
 			@Override public void onChange(ChangeEvent event) {
 				String sz = pageSize.getItemText(pageSize.getSelectedIndex());
 				dataGrid.setVisibleRange(0, Integer.parseInt(sz));
 			}
 		});
 		
 		this.editorPanel.add(editorWidget);
 		toggleEditor(false);
 	}
 	
 	@UiHandler("refresh")
 	void doR(ClickEvent ce) {
 		loadList(true);
 	}
 	
 	@UiHandler("createnew")
 	void doC(ClickEvent e) {
 		if (abandonCurrentEdit()) {
 			deselect();
 			stageEdit(null);
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@UiHandler("copy")
 	void doCp(ClickEvent e) {
 		if (null != selected & abandonCurrentEdit()) {
 			// we need a new reqctx to create the proxy, and
 			// we'll have to pass that into stage so the controller
 			// uses that instead of creates its own
 			modalAlertShow("Copying...");
 			createEditorRequestContext().find(selected.stableId())
 				.with(editorProperties).fire(new Receiver() {
 				@Override public void onSuccess(Object copyMe) {	
 					modalAlertCancel();
 					ENTITYCTX ctx = AbstractEditorAndGrid.this.createEditorRequestContext();
 					E copy = copyProxy(ctx, (E)copyMe);
 					if (null == copy) {
 						modalAlertShow("Copy not supported!");
 					} else {
 						deselect();
 						stageProxy(copy, ctx);
 					}
 				}
 				@Override public void onFailure(ServerFailure error) {
 					modalAlertCancel();
 					setMessage("Error: failed to retrieve fresh entity");
 				}
 			});
 		}
 	}
 
 	@UiHandler("edit")
 	void doE(ClickEvent e) {
 		if (null != selected && abandonCurrentEdit()) {
 			modalAlertShow("Editing...");
 			stageEdit(selected);
 		}
 	}
 	
 	@UiHandler("delete")
 	void doD(ClickEvent e) {
 		if (null != selected && Window.confirm("Are you sure you want to delete this record?")) {
 			this.currentEditorOptionRequest = EditorOptionRequest.DELETE;
 			stageProxy(selected, null);
 			modalAlertShow("Deleting...");
 			flushAndFire();
 		}
 	}
 	
 	@UiHandler("cancel")
 	void doCanc(ClickEvent e) {
 		if (abandonCurrentEdit()) {
 			cancelEdit();
 		}
 	}
 	
 	@UiHandler("save")
 	void doS(ClickEvent e) {
 		if (editing) {
 			this.currentEditorOptionRequest = EditorOptionRequest.SAVE;
 			modalAlertShow("Saving...");
 			flushAndFire();
 		}
 	}
 	
 	public Editor<E> getEditor() {
 		return editor;
 	}
 	
 	private boolean abandonCurrentEdit() {
 		if (editing && null != driver && driver.isDirty()) {
 			return Window.confirm("Abandon unsaved changes?");
 		}
 		return true;
 	}
 	
 	private void createCheckColumn(final DataGrid<E> dataGrid) {
 		
 		dataGrid.getSelectionModel().addSelectionChangeHandler(new Handler() {
 			@SuppressWarnings({ "unchecked" })
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				SingleSelectionModel<E> ssm = (SingleSelectionModel<E>)event.getSource();
 				E object = ssm.getSelectedObject();
 				boolean sel = dataGrid.getSelectionModel().isSelected(object);
 				if (sel && selected != object) {
 					selected = object;
 				} else if (!sel) {
 					selected = null;
 				}
 			}			
 		});
 		
 		// Checkbox column. This table will uses a checkbox column for selection.
 		// Alternatively, you can call dataGrid.setSelectionEnabled(true) to
 		// enable mouse selection.
 		Column<E, Boolean> checkColumn = new Column<E, Boolean>(new CheckboxCell(true, false)) {
 			@Override public Boolean getValue(E object) {
 				// Get the value from the selection model.
 				return dataGrid.getSelectionModel().isSelected(object);
 			}
 		};
 		
 		dataGrid.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
 		dataGrid.setColumnWidth(checkColumn, 40, Unit.PX);
 	}
 
 	// override if necessary
 	public void onSuccess(E proxy, EditorOptionRequest editorRequest) {}
 	public void onFailure(ServerFailure fail, EditorOptionRequest editorRequest) {}
 	
 	// override if we need to prime a staged proxy, ie add an empty
 	// list to a setter so visiting the editor chain doesn't die via NPE
 	// on a null list
 	public void onStaged(EditorOptionRequest optionRequest, ENTITYCTX reqCtx, E proxy) {}
 	
 	private void stageEdit(E proxy) {
 		// get full object and send to editor
 		if (null != proxy) {
 			final double start = Duration.currentTimeMillis();
 			refreshEntityForEditor(proxy).with(editorProperties).fire(new Receiver<E>() {
 				@Override
 				public void onSuccess(E response) {
 					stageProxy(response, null);
 					modalAlertCancel();
 					GWT.log("Staged edit in " + (Duration.currentTimeMillis()-start) + "ms");
 				}
 				@Override
 				public void onFailure(ServerFailure fail) {
 					cancelEdit();
 					modalAlertCancel();
 				}
 			});		
 		} else {
 			stageProxy(null, null);
 		}
 	}
 	
 	public abstract E createProxy(ENTITYCTX ctx);
 	
 	private void stageProxy(E proxy, ENTITYCTX editorCtx) {
 		
 		editorRequestContext = null != editorCtx ? editorCtx : createEditorRequestContext();
 		editing = true;
 		clearMessage();
 
 		staged = null == proxy ? editorRequestContext.create(proxyType) :
 			editorRequestContext.edit(proxy);		
 		setupStagedProxy(editorRequestContext, staged, null == proxy);
 		driver.edit(staged, editorRequestContext);
 
 		onStaged(currentEditorOptionRequest, editorRequestContext, staged);
 		
 		if (EditorOptionRequest.DELETE != currentEditorOptionRequest) {
			driver.accept(EditorVisitorHelpers.UNLOCK_EDITOR_VISITOR);
			for (EditorContext<?> ctx : this.ignoredEditorsToVisit) {
				EditorVisitorHelpers.UNLOCK_EDITOR_VISITOR.visit(ctx);
			}
 			toggleEditor(true);
 		}
 	}
 	
 	private abstract class EditorFlushReceiver<V> extends Receiver<V> {
 
 		@Override
 		public void onFailure(ServerFailure fail) {
 			cancelEdit();
 			modalAlertCancel();
 			setMessage("Error: " + fail.getMessage());
 		}
 		
 		@Override
 		public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
 			modalAlertCancel();
 			setMessage("Error: Has validation errors");
 			for (ConstraintViolation<?> cv : violations) {
 				GWT.log("path=" + cv.getPropertyPath() + ", invalidVal=" +
 						cv.getInvalidValue() + ", msg=" + cv.getMessage());
 			}
 			driver.setConstraintViolations(violations);
 		}
 		
 		@Override
 		public void onSuccess(V response) {
 			modalAlertCancel();
 			staged = null;
 			if (EditorOptionRequest.DELETE != currentEditorOptionRequest) {
 				setMessage("Success!");
 			}
 			lockEdit();
 			doOnSuccess(response);
 		}
 		
 		protected abstract void doOnSuccess(V response);
 	}
 	
 	private void flushAndFire() {
 		
 		Receiver<Void> actionRcvr = new EditorFlushReceiver<Void>() {
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override public void doOnSuccess(Void nil) {
 				if (null != selected) {
 					modalAlertShow("Updating List...");
 					createListRequestContext().find(selected.stableId())
 						.with(editorProperties).fire(new Receiver() {
 						@Override
 						public void onSuccess(Object response) {
 							updateInList((E)response);
 							modalAlertCancel();
 						}
 						@Override
 						public void onFailure(ServerFailure fail) {
 							modalAlertCancel();
 						}
 					});
 				}
 			}
 		};
 		Receiver<E> updateRcvr = new EditorFlushReceiver<E>() {
 			@Override public void doOnSuccess(E obj) {
 				modalAlertCancel();
 				staged = null;
 				setMessage("Success!");
 				lockEdit();
 				if (null != selected) {
 					updateInList(obj);
 				}
 			}
 		};
 
		// call subclass to create the correct request from the context
		//
 		editorRequest(staged, editorRequestContext, currentEditorOptionRequest, updateRcvr, actionRcvr);
		
		// iterate the editor change and stick empty lists where they're null
		//
		driver.accept(new EditorVisitor() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public <T> boolean visit(EditorContext<T> eCtx) {
				if (eCtx.getEditor() instanceof ListEditor) {
					ListEditor<?,?> le = (ListEditor<?,?>)eCtx.getEditor();
					if (le.getList() == null) {
						le.setValue(new ArrayList());
					}
				}
				return true;
			}
		});
 		driver.flush().fire();
 	}
 	
 	private void updateInList(E updated) {
 		int index = providerData.indexOf(selected);
 		selected = updated;
 		if (index > -1) {
 
 			providerData.remove(index);
 			boolean removedItem = removeItemInListOnSave(currentEditorOptionRequest);
 			
 			if (removedItem) {
 				deselect();
 				// if we've removed an item from the providerData and
 				// don't plan to re-add it w/ updated data, let's update
 				// the total count in case we're at less than 1 page
 				// -- otherwise the last row doesn't shift
 				toggleEditor(false);
 				
 				LISTCTX reqCtx = createListRequestContext();
 				getTotalRowCount(reqCtx, getListOptions(reqCtx)).fire(new Receiver<Long>() {
 					@Override public void onSuccess(Long count) {
 						provider.updateRowCount(count.intValue(), true);
 						provider.updateRowData(providerDisplayFirst, providerData);
 						ignoreRangeChange = true; // ignore so we don't load new data
 						dataGrid.setVisibleRange(0, providerData.size());
 					}
 				});
 			} else {
 				// re-add the newly updated response to the grid
 				providerData.add(index, updated);
 				provider.updateRowData(providerDisplayFirst, providerData);							
 			}
 		}
 	}
 	
 	public void setSelected(E selected) {
 		this.selected = selected;
 	}
 	
 	public E getSelected() {
 		return selected;
 	}
 	
 	public void deselect() {
 		if (null != selected) {
 			dataGrid.getSelectionModel().setSelected(selected, false);
 			selected = null;
 		}
 	}
 	
 	private void toggleEditor(boolean open) {
 		editorWidget.setVisible(open);
 		if (open) {
 			EditorVisitor visitor = EditorVisitorHelpers.createFocusFirstVisitor();
 			driver.accept(visitor);
 			for (EditorContext<?> ctx : this.ignoredEditorsToVisit) {
 				visitor.visit(ctx);
 			}
 		}
 		updateEditorSize();
 
 		// TODO enable/disable editor menu options
 	}
 	
 	public void updateEditorSize() {
 		int eH = editorWidget.getOffsetHeight();
 		int eMH = editorFrame.getWidget(0).getOffsetHeight();
 		int frameH = eMH;
 		if (eH > 0) {
 			frameH = eH+(eMH*2);
 			int fullH = frame.getOffsetHeight();
 			if (frameH > fullH) {
 				frameH = fullH;
 			}
 		}
 		frame.setWidgetSize(editorFrame, frameH);
 		frame.animate(400);		
 	}
 	
 	public void cancelEdit() {
 		currentEditorOptionRequest = null;
 		toggleEditor(false);
 		if (null != driver) {
 			driver.accept(EditorVisitorHelpers.CLEAR_EDITOR_VISITOR);
 			for (EditorContext<?> ctx : this.ignoredEditorsToVisit) {
 				EditorVisitorHelpers.CLEAR_EDITOR_VISITOR.visit(ctx);
 			}
 		}
 		clearMessage();
 //		editor.hide();
 		editorWidget.setVisible(false);
 		editorRequestContext = null;
 		editing = false;
 		staged = null;
 	}
 	
 	public void lockEdit() {
 		driver.accept(EditorVisitorHelpers.LOCK_EDITOR_VISITOR);
 		for (EditorContext<?> ctx : this.ignoredEditorsToVisit) {
 			EditorVisitorHelpers.LOCK_EDITOR_VISITOR.visit(ctx);
 		}
 		editorRequestContext = null;
 		editing = false;
 		staged = null;
 	}
 	
 	public void setMessage(String msg) {
 		this.message.setText(msg);
 	}
 	
 	public void clearMessage() {
 		this.message.setText("");
 	}
 	
 	public void loadList(boolean force) {
 		
 		if (!force && listLoaded) { return; }
 		
 		// get full row count
 		//
 		final LISTCTX ctx = createListRequestContext();
 		Request<Long> req = getTotalRowCount(ctx, getListOptions(ctx));
 		if (null != req) {
 			req.to(new Receiver<Long>() {
 				@Override public void onSuccess(Long count) {
 					totalRowCount = count;
 				}
 			});
 		}
 
 		// setup range-change handler to load pages
 		//
 		this.provider = new AsyncDataProvider<E>() {
 			boolean firstCtx = true;
 			@Override protected void onRangeChanged(HasData<E> display) {
 		        
 				if (!ignoreRangeChange) {
 
 					modalAlertShow("Loading list...");
 					final int first = display.getVisibleRange().getStart();
 			        int size = display.getVisibleRange().getLength();
 					int max = first + size;
 			        
 					String shouldBeStr = pageSize.getItemText(pageSize.getSelectedIndex());
 					int shouldBe = Integer.parseInt(shouldBeStr);
 					if (shouldBe != size) {
 						// resize, prob shrank due to a deletion
 						max = first + shouldBe;
 						ignoreRangeChange = true;
 						dataGrid.setVisibleRange(first, max);
 					}
 			        
 			        LISTCTX reqCtx = firstCtx ? ctx : createListRequestContext();
 			        LISTOPTIONS options = getListOptions(reqCtx);
 			        options.setFirstResult(first);
 			        options.setMaxResults(max);
 			        Request<List<E>> pageReq = getPage(reqCtx, options);
 					onLoadPage(pageReq, first);
 					firstCtx = false;
 					listLoaded = true;
 				}
 				ignoreRangeChange = false;				
 			}
 		};
 		
 		// init provider w/ our datagrid display
 		//
 		this.provider.addDataDisplay(this.dataGrid);
 		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 			@Override public void execute() {
 				updateEditorSize();
 			}
 		});
 	}	
 	
 	private void onLoadPage(Request<List<E>> req, final int first) {
 		this.providerDisplayFirst = first;
 		req.with(listProperties).fire(new Receiver<List<E>>() {
 			@Override public void onSuccess(List<E> page) {
 				providerData = new ArrayList<E>(page);
 		        if (totalRowCount >= 0) {
 		        	provider.updateRowCount((int)totalRowCount, true);
 		        }
 		        provider.updateRowData(first, page);	
 		        modalAlertCancel();
 			}
 			@Override public void onFailure(ServerFailure error) { modalAlertCancel(); }
 		});
 	}
 
 	private final List<E> EMPTY = new ArrayList<E>();
 	
 	public void clearList() {
 		if (null != provider) {
 	        provider.updateRowData(0, EMPTY);						
 	        provider.updateRowCount(0, true);
 		}
 	}
 	
 	public <T> void addIgnoredEditorToVisit(final LeafValueEditor<T> editor, final Class<T> type,
 			final String path) {
 
 		ignoredEditorsToVisit.add(new AbstractEditorContext<T>(editor, path) {
 			@Override
 			public boolean canSetInModel() {
 				return true;
 			}
 
 			@Override
 			public T checkAssignment(Object value) {
 				return editor.getValue();
 			}
 
 			@Override
 			public Class<T> getEditedType() {
 				return type;
 			}
 
 			@Override
 			public T getFromModel() {
 				return editor.getValue();
 			}
 
 			@Override
 			public void setInModel(T data) {
 				editor.setValue(data);
 			}
 		});
 	}
 	
 	protected static <E> void createColumn(DataGrid<E> grid, String title, int widthPx, Column<E,?> col) {
 		grid.addColumn(col, title);
 		if (widthPx > 0) {
 			grid.setColumnWidth(col, widthPx, Unit.PX);
 		}
 	}
 	
 	public static interface ColVal<IN,OUT> {
 		OUT getValue(IN in);
 	}
 	
 	public static abstract class StringColVal<IN> implements ColVal<IN,String> {
 		final public String getValue(IN in) {
 			return null == in ? "" : getString(in);
 		}
 		abstract public String getString(IN in);
 	}
 	
 	protected static <E> void createColumnTextCell(DataGrid<E> grid, String title, int widthPx,
 			final ColVal<E,String> converter) {
 		createColumn(grid, title, widthPx, new Column<E, String>(new TextCell()) {
 			@Override public String getValue(E object) {
 				return converter.getValue(object);
 			}
 		});
 	}
 }

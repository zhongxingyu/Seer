 package org.iplantc.de.client.views.panels;
 
 import static org.iplantc.de.client.models.Notification.PROP_CATEGORY;
 import static org.iplantc.de.client.models.Notification.PROP_MESSAGE;
 import static org.iplantc.de.client.models.Notification.PROP_TIMESTAMP;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.Notification;
 import org.iplantc.de.client.services.MessageServiceFacade;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotificationHelper.Category;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 /**
  * Displays user notifications.
  * 
  * @author sriram, hariolf
  */
 public class NotificationPanel extends ContentPanel {
     private Grid<Notification> grdNotifications;
     private ColumnModel columnModel; // an instance of a column model configured for this
     // window
     private SimpleComboBox<Category> dropdown;
     private CheckBoxSelectionModel<Notification> checkBoxModel;
 
     /**
      * ids of notification that will be preselected
      * 
      */
     private List<String> selectedIds;
 
     private PagingLoader<PagingLoadResult<Notification>> loader;
 
     private ListStore<Notification> store;
     private ToolBar toolBar;
     private int currentOffSet;
     private Category currentCategory;
     private SortDir currSortDir;
 
     /**
      * Creates a new NotificationPanel with config.
      */
     public NotificationPanel(int currentOffSet, List<String> currentSelection, Category curr_category,
             SortDir dir) {
         this.currentOffSet = currentOffSet;
         this.selectedIds = currentSelection;
         this.currentCategory = curr_category;
         this.currSortDir = dir;
         init();
     }
 
     public NotificationPanel() {
         this(1, null, Category.ALL, SortDir.DESC);
     }
 
     /**
      * Initialize all components used by the window.
      */
     private void init() {
         buildNotificationGrid();
         compose();
     }
 
     private void select() {
         ListStore<Notification> store = grdNotifications.getStore();
         if (selectedIds != null) {
             for (String id : selectedIds) {
                 Notification n = store.findModel("id", id);
                 if (n != null) {
                     checkBoxModel.select(n, true);
                 }
             }
         }
     }
 
     private void buildNotificationGrid() {
         initProxyLoader();
         buildColumnModel();
 
         grdNotifications = new Grid<Notification>(store, columnModel);
         // enable multi select of checkboxes and select all / unselect all
         grdNotifications.addPlugin(checkBoxModel);
         grdNotifications.setSelectionModel(checkBoxModel);
 
         addGridEventListeners();
         grdNotifications.setStateful(true);
         grdNotifications.setAutoExpandColumn(PROP_MESSAGE);
         grdNotifications.setAutoExpandMax(2048);
 
         grdNotifications.setStripeRows(true);
         grdNotifications.setLoadMask(true);
 
         GridView view = new GridView();
         view.setForceFit(true);
         view.setEmptyText(I18N.DISPLAY.noNotifications());
         grdNotifications.setView(view);
 
     }
 
     private void initProxyLoader() {
         RpcProxy<PagingLoadResult<Notification>> proxy = new RpcProxy<PagingLoadResult<Notification>>() {
             @Override
             protected void load(Object loadConfig,
                     final AsyncCallback<PagingLoadResult<Notification>> callback) {
                 final PagingLoadConfig config = (PagingLoadConfig)loadConfig;
                 MessageServiceFacade facade = new MessageServiceFacade();
                 facade.getNotifications(config.getLimit(), config.getOffset(),
                         config.get("filter") != null ? config.get("filter").toString() : "", config
                                 .getSortDir().toString(), new NotificationServiceCallback(config,
                                 callback));
             }
         };
         loader = new BasePagingLoader<PagingLoadResult<Notification>>(proxy);
         loader.setRemoteSort(true);
 
         store = new ListStore<Notification>(loader);
 
         final PagingToolBar toolBar = new PagingToolBar(10);
         toolBar.bind(loader);
         setBottomComponent(toolBar);
 
     }
 
     private void compose() {
         setHeaderVisible(false);
         setLayout(new FitLayout());
         add(grdNotifications);
         buildButtonBar();
         setTopComponent(toolBar);
     }
 
     private Button buildDeleteButton() {
         Button b = new Button(I18N.DISPLAY.delete(), new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 mask(I18N.DISPLAY.loadingMask());
                 deleteSelected(new Command() {
 
                     @Override
                     public void execute() {
                         unmask();
                         loader.load();
                     }
                 });
 
             }
         });
 
         b.setId("idBtnDelete");
         b.setIcon(AbstractImagePrototype.create(Resources.ICONS.cancel()));
         b.disable();
         return b;
     }
 
     /**
      * Configure and return a toolbar include all necessary buttons.
      * 
      * @return a configure instance of a toolbar ready to be rendered.
      */
     private void buildButtonBar() {
         toolBar = new ToolBar();
 
         toolBar.add(new Label(I18N.CONSTANT.filterBy() + "&nbsp;"));
         toolBar.add(buildFilterDropdown());
         toolBar.add(new SeparatorToolItem());
         toolBar.add(buildDeleteButton());
     }
 
     /**
      * Instantiate, configure, and return the appropriate column model.
      * 
      * A column model describes the columns and includes things like the name of the column, the width,
      * and the column header.
      * 
      * This method sets the instance variables columnModel and checkBoxModel.
      */
     private void buildColumnModel() {
         List<ColumnConfig> configs = new LinkedList<ColumnConfig>();
 
         checkBoxModel = new CheckBoxSelectionModel<Notification>();
         checkBoxModel.setSelectionMode(SelectionMode.SIMPLE);
         ColumnConfig colCheckBox = checkBoxModel.getColumn();
         colCheckBox.setAlignment(HorizontalAlignment.CENTER);
         configs.add(colCheckBox);
 
         ColumnConfig colCategory = new ColumnConfig(PROP_CATEGORY, PROP_CATEGORY, 100);
         colCategory.setHeader(I18N.CONSTANT.category());
         configs.add(colCategory);
         colCategory.setMenuDisabled(true);
         colCategory.setSortable(false);
 
         ColumnConfig colMessage = new ColumnConfig(PROP_MESSAGE, PROP_MESSAGE, 420);
         colMessage.setHeader(I18N.DISPLAY.messagesGridHeader());
         colMessage.setRenderer(new NameCellRenderer());
         configs.add(colMessage);
         colMessage.setSortable(false);
         colMessage.setMenuDisabled(true);
 
         ColumnConfig colTimestamp = new ColumnConfig(PROP_TIMESTAMP, PROP_TIMESTAMP, 170);
         colTimestamp.setHeader(I18N.DISPLAY.createdDateGridHeader());
         colTimestamp.setDateTimeFormat(DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM));
         configs.add(colTimestamp);
         columnModel = new ColumnModel(configs);
     }
 
     /**
      * Remove selected notifications.
      */
     private void deleteSelected(final Command callback) {
         NotificationHelper notiMgr = NotificationHelper.getInstance();
         List<Notification> notifications = new ArrayList<Notification>();
 
         for (Notification notification : checkBoxModel.getSelectedItems()) {
             notifications.add(notification);
         }
 
         notiMgr.delete(notifications, callback);
     }
 
     private SimpleComboBox<Category> buildFilterDropdown() {
         dropdown = new SimpleComboBox<Category>();
         dropdown.add(Category.ALL);
         dropdown.add(Category.DATA);
         dropdown.add(Category.ANALYSIS);
         dropdown.setSimpleValue(currentCategory); // select first item
         dropdown.setTriggerAction(TriggerAction.ALL); // Always show all categories in the
         // drop-down
         dropdown.setEditable(false);
 
         // remove existing filter so it doesn't interfere with the new filter
         List<StoreFilter<Notification>> filters = grdNotifications.getStore().getFilters();
         if (filters != null) {
             for (Iterator<StoreFilter<Notification>> iter = filters.iterator(); iter.hasNext();) {
                 iter.next();
                 iter.remove();
             }
         }
         dropdown.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Category>>() {
             @Override
             public void selectionChanged(SelectionChangedEvent<SimpleComboValue<Category>> se) {
                 SimpleComboValue<Category> selectedItem = se.getSelectedItem();
                 PagingLoadConfig config = buildLoadConfig(getCurrentSortDir(), selectedItem.getValue()
                         .toString());
                 loader.load(config);
             }
         });
 
         return dropdown;
     }
 
     /**
      * Filters the list of notifications by a given Category.
      * 
      * @param category
      */
     public void filterBy(Category category) {
         dropdown.setValue(dropdown.findModel(category));
     }
 
     /**
      * get current filter for notification window
      * 
      * @return {@link Category} category
      */
     public Category getCurrentFilter() {
         return dropdown.getValue().getValue();
     }
 
     @SuppressWarnings("rawtypes")
     private void addGridEventListeners() {
         grdNotifications.getSelectionModel().addListener(Events.SelectionChange,
                 new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(BaseEvent be) {
                         if (grdNotifications.getSelectionModel().getSelectedItems().size() > 0) {
                             toolBar.getItemByItemId("idBtnDelete").enable();
                         } else {
                             toolBar.getItemByItemId("idBtnDelete").disable();
                         }
                     }
                 });
         grdNotifications.addListener(Events.Attach, new Listener<GridEvent<Notification>>() {
             public void handleEvent(GridEvent<Notification> be) {
                 PagingLoadConfig config = buildDefaultLoadConfig();
                 loader.load(config);
             }
         });
 
         grdNotifications.getStore().addListener(Store.BeforeSort, new Listener<StoreEvent>() {
 
             @Override
             public void handleEvent(StoreEvent be) {
                 SortDir current = getCurrentSortDir();
                 PagingLoadConfig config = null;
 
                 if (current.equals(SortDir.DESC)) {
                     config = buildLoadConfig(SortDir.ASC,
                             dropdown.getSimpleValue().toString().equals(Category.ALL.toString()) ? ""
                                     : dropdown.getSimpleValue().toString());
                 } else {
                     config = buildLoadConfig(SortDir.DESC,
                             dropdown.getSimpleValue().toString().equals(Category.ALL.toString()) ? ""
                                     : dropdown.getSimpleValue().toString());
                 }
 
                 loader.load(config);
                 be.setCancelled(true);
             }
         });
 
     }
 
     private final class NotificationServiceCallback implements AsyncCallback<String> {
         private final PagingLoadConfig config;
         private final AsyncCallback<PagingLoadResult<Notification>> callback;
 
         private NotificationServiceCallback(PagingLoadConfig config,
                 AsyncCallback<PagingLoadResult<Notification>> callback) {
             this.config = config;
             this.callback = callback;
         }
 
         @Override
         public void onFailure(Throwable caught) {
             org.iplantc.core.uicommons.client.ErrorHandler.post(caught);
 
         }
 
         @Override
         public void onSuccess(String result) {
             final NotificationHelper instance = NotificationHelper.getInstance();
             instance.addInitialNotificationsToStore(result);
             callback.onSuccess(new NotificationPagingLoadResult(config, instance));
             select();
         }
     }
 
     private final class NotificationPagingLoadResult implements PagingLoadResult<Notification> {
         private final PagingLoadConfig config;
         private final NotificationHelper instance;
 
         private NotificationPagingLoadResult(PagingLoadConfig config, NotificationHelper instance) {
             this.config = config;
             this.instance = instance;
         }
 
         @Override
         public List<Notification> getData() {
             return instance.getStoreAll();
         }
 
         @Override
         public int getOffset() {
             return config.getOffset();
         }
 
         @Override
         public int getTotalLength() {
             return instance.getTotal();
         }
 
         @Override
         public void setOffset(int offset) {
             config.setOffset(offset);
 
         }
 
         @Override
         public void setTotalLength(int totalLength) {
             instance.setTotal(totalLength);
 
         }
     }
 
     /**
      * A custom renderer that renders notification messages as hyperlinks
      * 
      * @author sriram, hariolf
      * 
      */
     private class NameCellRenderer implements GridCellRenderer<Notification> {
 
         @Override
         public Object render(final Notification notification, String property, ColumnData config,
                 int rowIndex, int colIndex, ListStore<Notification> store, Grid<Notification> grid) {
             if (notification == null) {
                 return null;
             }
             String message = notification.getMessage();
             String context = notification.getContext();
             HorizontalPanel renderer;
             renderer = new HorizontalPanel();
             HTML html = new HTML(message, true);
             renderer.add(html);
             if (context != null && !context.isEmpty()) {
                 html.setStyleName("notification_context");
                 html.addClickHandler(new ClickHandler() {
 
                     @Override
                     public void onClick(ClickEvent event) {
                         NotificationHelper.getInstance().view(notification);
                     }
                 });
             }
 
             return renderer;
         }
     }
 
     /**
      * Get list of selected notification
      * 
      * @return a list containing selected notification objects
      */
     public List<Notification> getSelectedItems() {
         return checkBoxModel.getSelectedItems();
     }
 
     private PagingLoadConfig buildLoadConfig(SortDir dir, String filter) {
         PagingLoadConfig config = new BasePagingLoadConfig();
         config.setOffset(0);
         config.setLimit(10);
 
         Map<String, Object> state = grdNotifications.getState();
 
         if (state.containsKey("offset")) {
             int offset = (Integer)state.get("offset");
             int limit = (Integer)state.get("limit");
             config.setOffset(offset);
             config.setLimit(limit);
         }
         if (state.containsKey("sortField")) {
             config.setSortField(Notification.PROP_TIMESTAMP);
             config.setSortDir(dir);
         }
 
        if (filter != null && !filter.isEmpty()) {
             config.set("filter", filter.toLowerCase());
         }
 
         return config;
     }
 
     private PagingLoadConfig buildDefaultLoadConfig() {
         PagingLoadConfig config = new BasePagingLoadConfig();
         config.setOffset(currentOffSet);
         config.setLimit(10);
         config.setSortField(Notification.PROP_TIMESTAMP);
         config.setSortDir(currSortDir);
 
         if (!currentCategory.toString().equalsIgnoreCase("ALL")) {
             config.set("filter", currentCategory.toString().toLowerCase());
         }
 
         return config;
     }
 
     public SortDir getCurrentSortDir() {
         Map<String, Object> state = grdNotifications.getState();
         if (state.containsKey("sortField")) {
             String dir = (String)state.get("sortDir").toString();
             return SortDir.valueOf(dir.equalsIgnoreCase("NONE") ? "DESC" : dir);
         }
         return SortDir.DESC;
     }
 
     public int getCurrentOffset() {
         return loader.getOffset();
     }
 }

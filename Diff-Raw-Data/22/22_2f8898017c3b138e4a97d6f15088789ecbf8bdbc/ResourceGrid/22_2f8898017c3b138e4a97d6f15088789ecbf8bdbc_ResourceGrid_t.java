 package org.cloudlet.web.core.client;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 
 import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.XTemplates.Formatter;
 import com.sencha.gxt.core.client.XTemplates.FormatterFactories;
 import com.sencha.gxt.core.client.XTemplates.FormatterFactory;
 import com.sencha.gxt.core.client.resources.CommonStyles;
 import com.sencha.gxt.core.client.util.Format;
 import com.sencha.gxt.core.client.util.ToggleGroup;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.loader.DataProxy;
 import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
 import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
 import com.sencha.gxt.data.shared.loader.PagingLoader;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.ListView;
 import com.sencha.gxt.widget.core.client.ListViewCustomAppearance;
 import com.sencha.gxt.widget.core.client.button.ButtonBar;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.button.ToggleButton;
 import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
 import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
 import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
 import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 
 import org.cloudlet.web.core.Feed;
 import org.cloudlet.web.core.Media;
 import org.cloudlet.web.core.Resource;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 public abstract class ResourceGrid<T extends Resource, F extends Feed<T>> extends ContentPanel implements ResourceWidget<F> {
 
   @FormatterFactories(@FormatterFactory(factory = ShortenFactory.class, name = "shorten"))
   interface Renderer extends XTemplates {
     @XTemplate(source = "ResourceGrid.html")
     public SafeHtml renderItem(String name, String imageUrl, Style style);
   }
 
   interface Resources extends ClientBundle {
     ImageResource add();
 
     ImageResource cover();
 
     @Source("ResourceGrid.css")
     Style css();
 
     ImageResource delete();
 
     ImageResource edit();
 
     ImageResource refresh();
   }
 
   enum SelectButtonCar {
     ADD("Add", resources.add()), REFRESH("Refresh", resources.refresh()), DELETE("Delete", resources.delete()), EDIT("Edit", resources
         .edit());
 
     private String name;
     private ImageResource image;
 
     SelectButtonCar(String name, ImageResource image) {
       this.name = name;
       this.image = image;
     }
 
     ImageResource getImage() {
       return image;
     }
 
     String getName() {
       return name;
     }
   }
 
   static class Shorten implements Formatter<String> {
 
     private int length;
 
     public Shorten(final int length) {
       this.length = length;
     }
 
     @Override
     public String format(final String data) {
       return Format.ellipse(data, length);
     }
   }
 
   static class ShortenFactory {
     public static Shorten getFormat(final int length) {
       return new Shorten(length);
     }
   }
 
   interface Style extends CssResource {
     String over();
 
     String select();
 
     String thumb();
 
     String thumbWrap();
   }
 
   enum ViewButtonCar {
     GRID("Grid"), TABLE("Table");
 
     private String name;
 
     ViewButtonCar(String name) {
       this.name = name;
     }
 
     String getName() {
       return name;
     }
   }
 
   public final static String LIST = "list";
 
   static Renderer r;
   static Resources resources;
 
   @Inject
   ResourceManager resourceManager;
 
   protected PagingLoader<FilterPagingLoadConfig, PagingLoadResult<T>> loader;
   private boolean initialized = false;
   private VerticalLayoutContainer con;
   private Grid<T> grid;
   private ListView<T, T> listView;
   private T selectedItem;
   private ResourcePlace<F> place;
 
   private ResourceSearch<T, F> resourceSearch;
 
   static {
     r = GWT.create(Renderer.class);
     resources = GWT.create(Resources.class);
     resources.css().ensureInjected();
   }
 
   @Override
   public ResourcePlace<F> getPlace() {
     return place;
   }
 
   @Override
   public void setPlace(ResourcePlace<F> place) {
     this.place = place;
     if (resourceSearch != null) {
       resourceSearch.setPlace(place);
     }
     refresh();
   }
 
   protected abstract AbstractSafeHtmlRenderer<T> getCell();
 
   protected String getCoverUrl(Media media) {
    if (media == null || media.getTitle().equals("")) {
       return resources.cover().getSafeUri().asString();
     }
     return ResourcePlace.getMediaUrl(media);
   }
 
   protected abstract void initColumn(List<ColumnConfig<T, ?>> l);
 
   protected void initFilter(GridFilters<T> filters) {
   }
 
   protected abstract ResourceSearch<T, F> initSearch();
 
   protected void initView() {
     final Style style = resources.css();
 
     ListStore<T> store = new ListStore<T>(new ModelKeyProvider<T>() {
 
       @Override
       public String getKey(T item) {
         return item.getId();
       }
     });
 
     IdentityValueProvider<T> valueProvider = new IdentityValueProvider<T>();
 
     DataProxy<FilterPagingLoadConfig, PagingLoadResult<T>> proxy = new DataProxy<FilterPagingLoadConfig, PagingLoadResult<T>>() {
       @Override
       public void load(final FilterPagingLoadConfig loadConfig, final Callback<PagingLoadResult<T>, Throwable> callback) {
         MultivaluedMap<String, String> queryParameters = getPlace().getQueryParameters();
         final QueryBuilder builder = QueryBuilder.get(queryParameters);
         builder.filter(loadConfig.getFilters());
         builder.sort(loadConfig.getSortInfo());
         builder.limit(String.valueOf(loadConfig.getLimit()), String.valueOf(loadConfig.getOffset()));
         getPlace().load(new AsyncCallback<ResourcePlace<F>>() {
           @Override
           public void onFailure(final Throwable reason) {
           }
 
           @Override
           public void onSuccess(final ResourcePlace<F> result) {
             List<T> books = result.getResource().getEntries();
             builder.clear();
             callback.onSuccess(new PagingLoadResultBean<T>(books, result.getResource().getQueryCount().intValue(), loadConfig.getOffset()));
           }
         });
       }
     };
 
     loader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<T>>(proxy) {
       @Override
       protected FilterPagingLoadConfig newLoadConfig() {
         return new FilterPagingLoadConfigBean();
       }
     };
     loader.setRemoteSort(true);
     loader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, T, PagingLoadResult<T>>(store));
 
     CheckBoxSelectionModel<T> sm = new CheckBoxSelectionModel<T>(valueProvider);
 
     List<ColumnConfig<T, ?>> l = new ArrayList<ColumnConfig<T, ?>>();
     l.add(sm.getColumn());
     initColumn(l);
     ColumnModel<T> cm = new ColumnModel<T>(l);
 
     grid = new Grid<T>(store, cm);
     grid.setSelectionModel(sm);
     grid.getView().setForceFit(true);
     grid.getView().setStripeRows(true);
     grid.getView().setColumnLines(true);
     grid.setLoadMask(true);
     grid.setBorders(true);
     grid.setLoader(loader);
     grid.getView().setEmptyText("Please hit the load button.");
     grid.getSelectionModel().addSelectionHandler(new SelectionHandler<T>() {
 
       @Override
       public void onSelection(final SelectionEvent<T> event) {
         selectedItem = event.getSelectedItem();
       }
     });
 
     GridFilters<T> filters = new GridFilters<T>(loader);
     filters.initPlugin(grid);
     initFilter(filters);
 
     ListViewCustomAppearance<T> appearance = new ListViewCustomAppearance<T>("." + style.thumbWrap(), style.over(), style.select()) {
 
       @Override
       public void renderEnd(final SafeHtmlBuilder builder) {
         String markup = new StringBuilder("<div class=\"").append(CommonStyles.get().clear()).append("\"></div>").toString();
         builder.appendHtmlConstant(markup);
       }
 
       @Override
       public void renderItem(final SafeHtmlBuilder builder, final SafeHtml content) {
         builder.appendHtmlConstant("<div class='" + style.thumbWrap() + "' style='border: 1px solid white'>");
         builder.append(content);
         builder.appendHtmlConstant("</div>");
       }
     };
 
     listView = new ListView<T, T>(store, valueProvider, appearance);
     listView.setCell(new SimpleSafeHtmlCell<T>(getCell()));
     listView.getSelectionModel().addSelectionHandler(new SelectionHandler<T>() {
 
       @Override
       public void onSelection(final SelectionEvent<T> event) {
         selectedItem = event.getSelectedItem();
       }
     });
 
     final ButtonBar buttonBar = new ButtonBar();
     buttonBar.addStyleName("x-toolbar-mark");
     buttonBar.setMinButtonWidth(75);
     buttonBar.setPack(BoxLayoutPack.START);
 
     for (final SelectButtonCar car : SelectButtonCar.values()) {
       final TextButton button = new TextButton(car.getName(), car.getImage());
       button.addSelectHandler(new SelectHandler() {
 
         @Override
         public void onSelect(SelectEvent event) {
           selectBase(car);
         }
       });
       buttonBar.add(button);
     }
 
     final PagingToolBar toolBar = new PagingToolBar(5);
     toolBar.getElement().getStyle().setProperty("borderBottom", "none");
     toolBar.bind(loader);
 
     ToggleGroup viewGroup = new ToggleGroup();
     ToolBar viewBar = new ToolBar();
     viewBar.setPack(BoxLayoutPack.END);
     viewBar.setBorders(false);
     viewBar.add(new LabelToolItem("View :&nbsp&nbsp&nbsp"));
     for (final ViewButtonCar car : ViewButtonCar.values()) {
       final ToggleButton button = new ToggleButton(car.getName());
       button.setWidth(40);
       button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event) {
           if (event.getValue() == true) {
             selectView(car);
           }
         }
       });
       viewGroup.add(button);
       viewBar.add(button);
     }
     ToggleButton gridButton = (ToggleButton) viewBar.getWidget(1);
     gridButton.setValue(true);
 
     HorizontalLayoutContainer hor1 = new HorizontalLayoutContainer();
     hor1.add(toolBar, new HorizontalLayoutData(0.85, 1));
     hor1.add(viewBar, new HorizontalLayoutData(0.15, 1));
 
     HorizontalLayoutContainer hor2 = new HorizontalLayoutContainer();
     resourceSearch = initSearch();
     hor2.add(buttonBar, new HorizontalLayoutData(resourceSearch == null ? 1 : 0.4, 1));
     if (resourceSearch != null) {
       resourceSearch.setPack(BoxLayoutPack.END);
       hor2.add(resourceSearch, new HorizontalLayoutData(0.6, 1));
     }
 
     con = new VerticalLayoutContainer();
     con.add(hor2, new VerticalLayoutData(1, 34));
     con.add(hor1, new VerticalLayoutData(1, 27));
     selectView(ViewButtonCar.GRID);
 
     setWidget(con);
     setBodyBorder(false);
   }
 
   @Override
   protected void onAttach() {
     ensureInitialized();
     super.onAttach();
   }
 
   protected void refresh() {
     grid.getView().getBody().setId("resourcegrid");
     loader.load();
   }
 
   private void ensureInitialized() {
     if (!initialized) {
       initialized = true;
       initView();
     }
   }
 
   private void selectBase(SelectButtonCar car) {
     switch (car) {
       case ADD:
         ResourcePlace place = getPlace().getRendition(UserFeedEditor.NEW);
         resourceManager.goTo(place);
         break;
       case REFRESH:
         refresh();
         break;
       case DELETE:
         if (selectedItem == null || selectedItem.equals("")) {
           return;
         }
         resourceManager.getPlace(selectedItem).delete(new AsyncCallback<ResourcePlace<T>>() {
           @Override
           public void onFailure(Throwable caught) {
           }
 
           @Override
           public void onSuccess(ResourcePlace<T> result) {
             refresh();
           }
         });
         break;
       case EDIT:
         if (selectedItem == null) {
           return;
         }
         resourceManager.goTo(selectedItem);
         break;
       default:
         break;
     }
   }
 
   private void selectView(ViewButtonCar car) {
     switch (car) {
       case TABLE:
         con.remove(grid);
         con.add(listView, new VerticalLayoutData(1, 1));
         listView.setSize("100%", "100%");
         break;
       case GRID:
         con.remove(listView);
         con.add(grid, new VerticalLayoutData(1, 1));
         break;
       default:
         break;
     }
     con.onResize();
   }
 }

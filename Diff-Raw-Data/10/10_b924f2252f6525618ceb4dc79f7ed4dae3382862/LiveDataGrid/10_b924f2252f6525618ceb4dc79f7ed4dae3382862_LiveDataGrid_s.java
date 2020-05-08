 package com.safedoor.testing.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.editor.client.Editor.Path;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.PropertyAccess;
 import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
 import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.PagingLoader;
 import com.sencha.gxt.widget.core.client.FramedPanel;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.LiveGridView;
 import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 
 public class LiveDataGrid implements IsWidget{
 	
 	public static int RECORDS = 400;
 
 	//Data provider interface
 	interface Properties extends PropertyAccess<Data>{
 		ValueProvider<Data, String> name();
 		ValueProvider<Data, String> symbol();
 		ValueProvider<Data, String> date();
 		ValueProvider<Data, String> notes();
 		ValueProvider<Data, Integer> id();
 		ValueProvider<Data, Double> price();
 		ValueProvider<Data, Double> change();
 		ValueProvider<Data, Boolean> available();
 		ValueProvider<Data, Boolean> checked();
 		ValueProvider<Data, Boolean> compliant();
 		@Path("name")
 		ModelKeyProvider<Data> key();
 	}
 
 	/**
 	 * Creates the grid widget
 	 */
 	@Override
 	public Widget asWidget() {
 		FramedPanel panel = new FramedPanel();
 		panel.setHeadingText("Paging Grid");
 		panel.setWidth(625);
 		
 		//create the property access
 		Properties properties = GWT.create(Properties.class);
 		
 		//Create colum configurations
 		ColumnConfig<Data, String> nameCol = new ColumnConfig<Data, String>(properties.name(),60,"Name");
 		ColumnConfig<Data, String> symbolCol = new ColumnConfig<Data, String>(properties.symbol(),50,"Symbol");
 		ColumnConfig<Data, String> dateCol = new ColumnConfig<Data, String>(properties.date(),150,"Date");
 		ColumnConfig<Data, String> notesCol = new ColumnConfig<Data, String>(properties.notes(),50,"Notes");
 		ColumnConfig<Data, Integer> idCol = new ColumnConfig<Data, Integer>(properties.id(),30,"Id");
 		ColumnConfig<Data, Double> priceCol = new ColumnConfig<Data, Double>(properties.price(),50,"Price");
 		ColumnConfig<Data, Double> changeCol = new ColumnConfig<Data, Double>(properties.change(),50,"Change");
 		ColumnConfig<Data, Boolean> availableCol = new ColumnConfig<Data, Boolean>(properties.available(),60,"Available");
 		ColumnConfig<Data, Boolean> checkedCol = new ColumnConfig<Data, Boolean>(properties.checked(),50,"Checked");
 		ColumnConfig<Data, Boolean> compliantCol = new ColumnConfig<Data, Boolean>(properties.compliant(),60,"Compliant");
 
 		//Add column config data to a list
 		List<ColumnConfig<Data,?>> list = new ArrayList<ColumnConfig<Data,?>>();
 		list.add(nameCol);
 		list.add(symbolCol);
 		list.add(dateCol);
 		list.add(notesCol);
 		list.add(idCol);
 		list.add(priceCol);
 		list.add(changeCol);
 		list.add(availableCol);
 		list.add(checkedCol);
 		list.add(compliantCol);
 		//create a column model and put the column config data into it
 		ColumnModel<Data> cm = new ColumnModel<Data>(list);
 		
 		ListStore<Data> store = new ListStore<Data>(new ModelKeyProvider<Data>(){
 			@Override
 			public String getKey(Data item) {
 				return item.getId().toString();
 			}
 		});
 		
 		PagingMemoryProxy proxy = new PagingMemoryProxy(new TestData(RECORDS).getData());
 		
 		final PagingLoader<PagingLoadConfig, PagingLoadResult<Data>> loader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Data>>(proxy);
 		loader.setRemoteSort(true);
 		loader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, Data, PagingLoadResult<Data>>(store));
 		
 		final PagingToolBar pagingToolBar = new PagingToolBar(10);
 		pagingToolBar.getElement().getStyle().setProperty("borderBottom", "none");
 		pagingToolBar.bind(loader);
 		
 		/*final LiveGridView<Data> view = new LiveGridView<Data>();
 		view.setCacheSize(10);
 		view.setForceFit(true);*/
 		
 		Grid<Data> grid = new Grid<Data>(store,cm){
 			@Override
 			protected void onAfterFirstAttach() {
 				super.onAfterFirstAttach();
 				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 					@Override
 					public void execute() {
						loader.load(1,10);
 					}
 				});
 			}
 		};
 		grid.setLoadMask(true);
 		grid.setLoader(loader);
 		grid.getView().setForceFit(true);
 		//grid.setView(view);
 		
 		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
 		vlc.setBorders(true);
 		vlc.add(grid);
 		vlc.add(pagingToolBar);
 		
 	    panel.setWidget(vlc);
 		return panel;
 	}
 
 }

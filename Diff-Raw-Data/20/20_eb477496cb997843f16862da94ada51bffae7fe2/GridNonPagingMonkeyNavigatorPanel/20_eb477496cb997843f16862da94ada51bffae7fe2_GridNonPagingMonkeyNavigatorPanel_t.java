 package org.iucn.sis.client.panels;
 
 import org.iucn.sis.client.panels.NonPagingMonkeyNavigatorPanel.NavigationModelData;
 
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.grid.BufferView;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 
 public abstract class GridNonPagingMonkeyNavigatorPanel<T> extends NonPagingMonkeyNavigatorPanel<NavigationModelData<T>> {
 
 	protected Grid<NavigationModelData<T>> grid;
 	
 	public GridNonPagingMonkeyNavigatorPanel() {
 		super(new FillLayout());
 		
 		grid = new Grid<NavigationModelData<T>>(getStoreInstance(), getColumnModel());
 		grid.setBorders(false);
 		grid.setHideHeaders(true);
 		grid.setLoadMask(true);
 		grid.setView(getView());
 		grid.setSelectionModel(new NavigationGridSelectionModel<T>());
 		grid.setAutoExpandColumn("name");
 		grid.setContextMenu(createMarkingContextMenu(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (!hasSelection())
 					return;
 				
 				mark(grid.getSelectionModel().getSelectedItem(), ce.getItem().getItemId());
 			}
 		}));
 		grid.addListener(Events.RowClick, new Listener<GridEvent<NavigationModelData<T>>>() {
 			public void handleEvent(GridEvent<NavigationModelData<T>> be) {
 				Boolean header = be.getModel().get("header");
 				if (Boolean.TRUE.equals(header))
 					be.setCancelled(true);
 				else
 					onSelectionChanged(be.getModel());
 			}
 		});
 		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<NavigationModelData<T>>>() {
 			public void handleEvent(GridEvent<NavigationModelData<T>> be) {
 				Boolean header = be.getModel().get("header");
 				if (Boolean.TRUE.equals(header))
 					be.setCancelled(true);
 				else
 					open(be.getModel());
 			}
 		});
 		
 		setupToolbar();
 		
 		add(grid);
 	}
 	
 	protected abstract String getEmptyText();
 	
 	protected final GridView getView() {
 		BufferView view = new BufferView();
 		view.setForceFit(true);
 		view.setEmptyText(getEmptyText());
 		view.setRowHeight(22);
 		
 		return view;
 	}
 	
 	public T getSelected() {
 		NavigationModelData<T> model = grid.getSelectionModel().getSelectedItem();
 		if (model != null)
 			return model.getModel();
 		else
 			return null;
 	}
 	
 	public boolean hasSelection() {
 		return getSelected() != null;
 	}
 	
 	@Override
 	public void refreshView() {
		refreshView(true);
	}
	
	public void refreshView(boolean updateSelection) {
 		grid.getView().refresh(false);
		if (updateSelection && hasSelection())
 			setSelection(getSelected());
 	}
 	
 	protected abstract ColumnModel getColumnModel();
 	
 	protected abstract void open(NavigationModelData<T> model);
 	
 	protected abstract void mark(NavigationModelData<T> model, String color);
 	
 	protected abstract void onSelectionChanged(NavigationModelData<T> model);
 	
 	protected abstract void setSelection(T navigationModel);
 	
 	protected abstract void setupToolbar();
 	
 }

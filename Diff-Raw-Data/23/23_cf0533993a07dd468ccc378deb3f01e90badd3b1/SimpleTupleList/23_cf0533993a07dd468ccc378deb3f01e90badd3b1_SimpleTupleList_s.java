 package com.szas.server.gwt.client.universalwidgets;
 
 import java.util.Collection;
 import java.util.List;
 
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.History;
 import com.google.gwt.view.client.CellPreviewEvent;
 import com.google.gwt.view.client.DefaultSelectionEventManager;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.szas.sync.DAOObserver;
 import com.szas.sync.Tuple;
 import com.szas.sync.local.LocalDAO;
 
 public abstract class SimpleTupleList<T extends Tuple> extends CellTable<T> {
 	private static class UniversalProvidesKey<TP extends Tuple> implements ProvidesKey<TP> {
 
 		@Override
 		public Object getKey(TP item) {
 			return (item == null) ? null : item.getId();
 		}
 
 	}
 
 	private ProvidesKey<T> providesKey;
 	private SingleSelectionModel<T> selectionModel;
	private List<T> list;
 	private DAOObserver contentObserver;
 	protected abstract void addColumns(CellTable<T> cellTable2);
 	protected abstract LocalDAO<T> getLocalDAO();
 	protected abstract String getListName();
 
 	protected void changeSellection() {
 		T tuple = selectionModel.getSelectedObject();
 		if (tuple == null)
 			return;
 		History.newItem(getListName()+"," + tuple.getId(),true);
 	}
 
 	protected SingleSelectionModel<T> createSelectionModel() {
 		SingleSelectionModel<T> singleSelectionModel = 
 			new SingleSelectionModel<T>(providesKey);
 		singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				changeSellection();
 			}
 		});
 		return singleSelectionModel;
 	}
 
 	private void createDataProvider() {
		ListDataProvider<T> listDataProvider = new ListDataProvider<T>();
 		listDataProvider.addDataDisplay(this);
		list =listDataProvider.getList();
 	}
 
 	public SimpleTupleList() {
 		super(new UniversalProvidesKey<T>());
 	}
 
 	protected CellPreviewEvent.Handler<T> getSelectionEventManager() {
 		return DefaultSelectionEventManager.createDefaultManager();
 	}
 	protected boolean filter(T tuple) {
 		return true;
 	}
 
 	public void daoUpdated() {
 		Collection<T> tuples = getLocalDAO().getAll();
		this.setRowCount(tuples.size(), true);
		while (list.size() != 0)
			list.remove(0);
 		for (T tuple : tuples) {
 			if (!filter(tuple))
 				continue;
 			list.add(tuple);
 		}
 	}
 
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 		providesKey = getKeyProvider();
 
 
 		addColumns(this);
 
 		selectionModel = createSelectionModel();
 		this.setSelectionModel(selectionModel, getSelectionEventManager());
 
 		createDataProvider();
 		
 		daoUpdated();
 		contentObserver = new DAOObserver() {
 
 			@Override
 			public void onChange(boolean whileSync) {
 				daoUpdated();
 			}
 		};
 		getLocalDAO().addDAOObserver(contentObserver);
 	}
 	@Override
 	protected void onDetach() {
 		if (contentObserver != null)
 			getLocalDAO().removeDAOObserver(contentObserver);
 		contentObserver = null;
 		super.onDetach();
 	}
 
 
 }

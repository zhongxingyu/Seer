 package pl.art.mnp.rogalin.ui.tab;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.vaadin.dialogs.ConfirmDialog;
 
 import pl.art.mnp.rogalin.TabsController.PredicateListener;
 import pl.art.mnp.rogalin.db.DbConnection;
 import pl.art.mnp.rogalin.db.FieldInfo;
 import pl.art.mnp.rogalin.db.ObjectsDao;
 import pl.art.mnp.rogalin.db.predicate.Predicate;
 
 import com.mongodb.DBObject;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.event.Action;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.Action.Handler;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.Runo;
 
 public class ObjectList extends VerticalLayout implements Handler, PredicateListener {
 
 	private static final Logger LOG = Logger.getLogger(ObjectList.class.getName());
 
	private static final long serialVersionUID = 3249982629925972329L;
 
 	private static final Action PREVIEW = new Action("Zobacz");
 
 	private static final Action EDIT = new Action("Edytuj");
 
 	private static final Action REMOVE = new Action("Usuń");
 
 	private static final Action[] ACTIONS = new Action[] { PREVIEW, EDIT, REMOVE };
 
 	private static final Set<FieldInfo> VISIBLE_COLUMNS = EnumSet.of(FieldInfo.IDENTIFIER, FieldInfo.NAME,
 			FieldInfo.TYPE, FieldInfo.EVALUATION_DATE);
 
 	private final VerticalLayout layout;
 
 	private TextField searchText;
 
 	private ShowAllListener showAllListener;
 
 	private Label filterInfo;
 
 	private List<Predicate> predicates;
 
 	private String query;
 
 	private Table table;
 
 	public ObjectList() {
 		super();
 		this.showAllListener = new ShowAllListener() {
 			private static final long serialVersionUID = -5072458379584711625L;
 
 			@Override
 			public void showAll() {
 			}
 		};
 
 		setMargin(true);
 		setSpacing(true);
 		setWidth("100%");
 
 		layout = new VerticalLayout();
 		layout.addComponent(searchField());
 		layout.addComponent(renderTable());
 		addComponent(layout);
 	}
 
 	private Component searchField() {
 		HorizontalLayout layout = new HorizontalLayout();
 		layout.setSpacing(true);
 		searchText = new TextField();
 		searchText.setImmediate(true);
 		searchText.addValueChangeListener(new ValueChangeListener() {
 			private static final long serialVersionUID = 6289526628390104589L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				filterResults(searchText.getValue());
 			}
 		});
 		Button searchButton = new Button("Wyszukaj");
 		searchButton.addClickListener(new ClickListener() {
 			private static final long serialVersionUID = -1583248192476499700L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				filterResults(searchText.getValue());
 			}
 		});
 		Button resetSearch = new Button("Pokaż wszystkie");
 		resetSearch.addClickListener(new ClickListener() {
 			private static final long serialVersionUID = 934242241334057589L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				predicates = null;
 				if (StringUtils.isEmpty(searchText.getValue())) {
 					refreshTable();
 				} else {
 					searchText.setValue("");
 				}
 				showAllListener.showAll();
 			}
 		});
 		filterInfo = new Label("");
 		layout.addComponents(searchText, searchButton, resetSearch, filterInfo);
 		return layout;
 	}
 
 	private void filterResults(String value) {
 		if (StringUtils.isEmpty(value)) {
 			this.query = null;
 		} else {
 			this.query = value;
 		}
 		refreshTable();
 	}
 
 	private Table renderTable() {
 		table = new Table();
 		table.setWidth("100%");
 		table.setStyleName(Runo.TABLE_SMALL);
 		table.setSelectable(true);
 		table.setColumnCollapsingAllowed(true);
 		table.setColumnReorderingAllowed(true);
 		table.setImmediate(true);
 		table.setMultiSelect(false);
 		table.addActionHandler(this);
 		table.setSelectable(false);
 		table.addItemClickListener(new ItemClickListener() {
 			private static final long serialVersionUID = 236557764476429213L;
 
 			@Override
 			public void itemClick(ItemClickEvent event) {
 				if (event.isDoubleClick()) {
 					DBObject object = DbConnection.getInstance().getObjectsDao().getObject(event.getItemId());
 					showPreview(object);
 				}
 			}
 		});
 		for (FieldInfo f : FieldInfo.values()) {
 			table.addContainerProperty(f, String.class, "-");
 			table.setColumnCollapsed(f, !VISIBLE_COLUMNS.contains(f));
 		}
 
 		refreshTable();
 		return table;
 	}
 
 	public void refreshTable() {
 		LOG.info("refresh table invoked");
 		table.removeAllItems();
 		Collection<DBObject> objects;
 		ObjectsDao objectDao = DbConnection.getInstance().getObjectsDao();
 		if (StringUtils.isEmpty(query)) {
 			objects = objectDao.getObjectList();
 		} else {
 			objects = objectDao.getFilteredObjectList(query);
 		}
 		int i = 0;
 		for (DBObject o : objects) {
 			if (matchesPredicates(o)) {
 				i++;
 				table.addItem(createTableRow(o), o.get("_id"));
 			}
 		}
 		filterInfo.setValue(String.format("Ilość rezultatów: %d. %s", i, predicates == null ? ""
 				: "Wyniki podlegają filtrowaniu."));
 	}
 
 	private boolean matchesPredicates(DBObject o) {
 		if (predicates == null) {
 			return true;
 		}
 		for (Predicate p : predicates) {
 			if (!p.matches(o)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private Object[] createTableRow(DBObject dbObject) {
 		List<Object> properties = new ArrayList<Object>(FieldInfo.values().length);
 		for (FieldInfo f : FieldInfo.values()) {
 			properties.add(f.getFieldType().getValue(dbObject));
 		}
 		return properties.toArray();
 	}
 
 	private final ClickListener showTable = new ClickListener() {
 		private static final long serialVersionUID = -1863572674350666127L;
 
 		@Override
 		public void buttonClick(ClickEvent event) {
 			removeAllComponents();
 			addComponent(layout);
 		}
 	};
 
 	@Override
 	public Action[] getActions(Object target, Object sender) {
 		return ACTIONS;
 	}
 
 	@Override
 	public void handleAction(Action action, Object sender, Object target) {
 		if (sender == table) {
 			DBObject object = DbConnection.getInstance().getObjectsDao().getObject(target);
 			if (action == PREVIEW) {
 				showPreview(object);
 			} else if (action == EDIT) {
 				showEditView(object);
 			} else if (action == REMOVE) {
 				remove(object);
 			}
 		}
 	}
 
 	private void showPreview(DBObject object) {
 		if (object == null) {
 			return;
 		}
 		removeAllComponents();
 
 		Button back = new Button("Powrót do listy");
 		back.addClickListener(showTable);
 		addComponent(back);
 
 		ObjectPreview preview = new ObjectPreview(object);
 		addComponent(preview);
 	}
 
 	private void showEditView(DBObject object) {
 		if (object == null) {
 			return;
 		}
 		removeAllComponents();
 
 		Button back = new Button("Powrót do listy");
 		back.addClickListener(showTable);
 		addComponent(back);
 
 		ObjectForm objectForm = new ObjectForm(new SaveActionListener() {
 			private static final long serialVersionUID = 8084094197862972496L;
 
 			@Override
 			public void onSaveAction() {
 				refreshTable();
 				removeAllComponents();
 				addComponent(layout);
 			}
 		}, object);
 		addComponent(objectForm);
 	}
 
 	private void remove(final DBObject object) {
 		ConfirmDialog.show(this.getUI(), "Potwierdzenie", "Czy na pewno chcesz usunąć ten obiekt?", "OK",
 				"Anuluj", new ConfirmDialog.Listener() {
 					private static final long serialVersionUID = -3518627734961298673L;
 
 					@Override
 					public void onClose(ConfirmDialog dialog) {
 						if (!dialog.isConfirmed()) {
 							return;
 						}
 						if (object != null) {
 							DbConnection.getInstance().getObjectsDao().removeObject(object);
 							refreshTable();
 						}
 					}
 				});
 	}
 
 	@Override
 	public void gotPredicates(List<Predicate> predicates) {
 		this.predicates = predicates;
 		if (!StringUtils.isEmpty(this.searchText.getValue())) {
 			this.searchText.setValue("");
 		} else {
 			this.refreshTable();
 		}
 	}
 
 	public void setShowAllListener(ShowAllListener listener) {
 		this.showAllListener = listener;
 	}
 
 	public static interface ShowAllListener extends Serializable {
 		void showAll();
 	}
 }

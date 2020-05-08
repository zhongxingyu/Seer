 package pl.art.mnp.rogalin.ui.tab;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import pl.art.mnp.rogalin.TabsController.PredicateListener;
 import pl.art.mnp.rogalin.db.DbConnection;
 import pl.art.mnp.rogalin.db.FieldInfo;
 import pl.art.mnp.rogalin.db.predicate.DummyPredicate;
 import pl.art.mnp.rogalin.db.predicate.Predicate;
 import pl.art.mnp.rogalin.field.FieldType;
 import pl.art.mnp.rogalin.ui.field.UiFieldType;
 
 import com.mongodb.DBObject;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("serial")
 public class SearchTab extends VerticalLayout {
 
 	private final Collection<UiFieldType> fields = new ArrayList<UiFieldType>();
 
 	private final PredicateListener predicateListener;
 
 	private final CheckBox noPhotoCheckbox;
 
 	public SearchTab(PredicateListener predicateListener) {
 		super();
 		this.predicateListener = predicateListener;
 		this.setSpacing(true);
 		this.setMargin(true);
 
 		Button clear = new Button("Wyczyść formularz");
 		clear.addClickListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				clear();
 			}
 		});
 
 		Button search = new Button("Wyszukaj obiekty");
 		search.addClickListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				showObjects();
 			}
 		});
 		addComponent(new HorizontalLayout(search, clear));
 
 		GridLayout columns = new GridLayout(2, 1);
 		columns.setSpacing(true);
 		FormLayout leftColumn = new FormLayout();
 		FormLayout rightColumn = new FormLayout();
 		FormLayout belowColumns = new FormLayout();
 		columns.addComponent(leftColumn, 0, 0);
 		columns.addComponent(rightColumn, 1, 0);
 		addComponent(columns);
 		addComponent(belowColumns);
 
 		int i = 0;
 		Layout container = null;
 		for (FieldInfo f : FieldInfo.values()) {
 			if (!f.isSearchable()) {
 				continue;
 			}
 			FieldType fieldType = f.getFieldType();
 			UiFieldType formField = fieldType.getSearchField();
 			fields.add(formField);
 
 			if (i <= 21) {
 				container = leftColumn;
 				i++;
 			} else {
 				container = rightColumn;
 			}
 			container.addComponent(formField.getComponent());
 		}
 		noPhotoCheckbox = new CheckBox("Obiekty bez fotografii");
 		container.addComponent(noPhotoCheckbox);
 	}
 
 	private void showObjects() {
 		List<Predicate> predicates = new ArrayList<Predicate>();
 		for (UiFieldType field : fields) {
 			Predicate p = field.getPredicate();
 			if (p instanceof DummyPredicate) {
 				continue;
 			}
 			predicates.add(field.getPredicate());
 		}
 		if (noPhotoCheckbox.getValue()) {
 			predicates.add(NO_PHOTO_PREDICATE);
 		}
 		predicateListener.gotPredicates(predicates);
 	}
 
 	private void clear() {
		noPhotoCheckbox.setValue(false);
 		for (UiFieldType field : fields) {
 			field.clear();
 		}
 	}
 
 	private static final Predicate NO_PHOTO_PREDICATE = new Predicate() {
 		@Override
 		public boolean matches(DBObject dbObject) {
 			return DbConnection.getInstance().getObjectsDao().getPhotos(dbObject).isEmpty();
 		}
 	};
 }

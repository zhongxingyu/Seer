 package pl.art.mnp.rogalin.ui.tab;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import pl.art.mnp.rogalin.db.DbConnection;
 import pl.art.mnp.rogalin.db.FieldInfo;
 import pl.art.mnp.rogalin.db.ObjectsDao;
 import pl.art.mnp.rogalin.ui.photo.DbPhoto;
 
 import com.mongodb.DBObject;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 @SuppressWarnings("serial")
 public class OptionsTab extends VerticalLayout {
 
 	protected static final Logger LOG = Logger.getLogger(OptionsTab.class.getName());
 
 	private final ComboBox fieldSelect;
 
 	private final ListSelect listSelect;
 
 	private final TextField newTextField;
 
 	private FieldInfo selectedField;
 
 	private List<String> options;
 
 	public OptionsTab() {
 		super();
 
 		setMargin(true);
 		List<FieldInfo> listFields = new ArrayList<FieldInfo>();
 		for (FieldInfo f : FieldInfo.values()) {
 			if (f.getFieldType().hasOptions()) {
 				listFields.add(f);
 			}
 		}
 
 		FormLayout formLayout = new FormLayout();
 		addComponent(formLayout);
 
 		fieldSelect = new ComboBox("Kategoria", listFields);
 		fieldSelect.setImmediate(true);
 		fieldSelect.setNullSelectionAllowed(false);
 		fieldSelect.setNewItemsAllowed(false);
 		fieldSelect.setTextInputAllowed(false);
 		fieldSelect.setPageLength(0);
 
 		fieldSelect.addValueChangeListener(new Property.ValueChangeListener() {
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				selectedField = (FieldInfo) event.getProperty().getValue();
 				options = DbConnection.getInstance().getOptionsDao().getOptions(selectedField);
 				updateListSelect(false);
 			}
 		});
 		formLayout.addComponent(fieldSelect);
 
 		listSelect = new ListSelect("Opcje");
 		listSelect.setNullSelectionAllowed(false);
 		listSelect.setImmediate(true);
 		formLayout.addComponent(listSelect);
 
 		fieldSelect.select(listFields.get(0));
 
 		Button upButton = new Button("Do góry");
 		upButton.setImmediate(true);
 		upButton.addClickListener(new Button.ClickListener() {
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				up();
 			}
 		});
 		Button downButton = new Button("W dół");
 		downButton.setImmediate(true);
 		downButton.addClickListener(new Button.ClickListener() {
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				down();
 			}
 		});
 		Button addButton = new Button("Dodaj");
 		addButton.setImmediate(true);
 		addButton.addClickListener(new Button.ClickListener() {
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				add();
 			}
 		});
 		Button deleteButton = new Button("Usuń");
 		deleteButton.setImmediate(true);
 		deleteButton.addClickListener(new Button.ClickListener() {
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				delete();
 			}
 		});
 		HorizontalLayout horizLayout = new HorizontalLayout(upButton, downButton, addButton, deleteButton);
 		formLayout.addComponent(horizLayout);
 
 		newTextField = new TextField("Nowa wartość:");
 		formLayout.addComponent(newTextField);
 
 		addComponent(new HorizontalSplitPanel());
 		Button createThumbnails = new Button("Generuj miniatury");
 		createThumbnails.setImmediate(true);
 		createThumbnails.addClickListener(new Button.ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				generateThumbnails();
 			}
 		});
 		addComponent(createThumbnails);
 	}
 
 	private static void generateThumbnails() {
 		DbConnection connection = DbConnection.getInstance();
 		ObjectsDao dao = connection.getObjectsDao();
 		try {
 			for (DBObject o : dao.getObjectList()) {
 				LOG.info((String) o.get(FieldInfo.NAME.name()));
 				List<DbPhoto> photos = dao.getPhotos(o);
 				for (DbPhoto p : photos) {
 					p.generateThumbnail();
 				}
 			}
			Notification.show("Wygenerowano miniatury", Notification.Type.HUMANIZED_MESSAGE);
 		} catch (IOException e) {
 			Notification.show("Wystąpił błąd", Notification.Type.ERROR_MESSAGE);
 			LOG.log(Level.WARNING, "Can't generate thumbnails", e);
 		}
 	}
 
 	private void updateListSelect(boolean rememberSelection) {
 		Object selection = null;
 		if (rememberSelection) {
 			selection = listSelect.getValue();
 		}
 		listSelect.removeAllItems();
 		for (String item : options) {
 			listSelect.addItem(item);
 		}
 		if (selection != null) {
 			listSelect.setValue(selection);
 		}
 	}
 
 	private void up() {
 		String value = (String) listSelect.getValue();
 		int i = options.indexOf(value);
 		if (i > 0) {
 			options.set(i, options.get(i - 1));
 			options.set(i - 1, value);
 		}
 		saveAndUpdate(selectedField, options, true);
 	}
 
 	private void down() {
 		String value = (String) listSelect.getValue();
 		int i = options.indexOf(value);
 		if (i < (options.size() - 1) && i > -1) {
 			options.set(i, options.get(i + 1));
 			options.set(i + 1, value);
 		}
 		saveAndUpdate(selectedField, options, true);
 	}
 
 	private void add() {
 		String newValue = newTextField.getValue();
 		newTextField.setValue("");
 		if (options.indexOf(newValue) > -1) {
 			listSelect.select(newValue);
 			return;
 		} else if (newValue == null || newValue.isEmpty()) {
 			return;
 		}
 
 		String value = (String) listSelect.getValue();
 		int i = options.indexOf(value);
 
 		if (i == -1) {
 			options.add(newValue);
 		} else {
 			options.add(i + 1, newValue);
 		}
 		saveAndUpdate(selectedField, options, false);
 		listSelect.select(newValue);
 	}
 
 	private void delete() {
 		String value = (String) listSelect.getValue();
 		options.remove(value);
 		saveAndUpdate(selectedField, options, false);
 	}
 
 	private void saveAndUpdate(FieldInfo field, List<String> options, boolean rememberSelection) {
 		DbConnection.getInstance().getOptionsDao().saveOptions(selectedField, options);
 		updateListSelect(rememberSelection);
 	}
 }

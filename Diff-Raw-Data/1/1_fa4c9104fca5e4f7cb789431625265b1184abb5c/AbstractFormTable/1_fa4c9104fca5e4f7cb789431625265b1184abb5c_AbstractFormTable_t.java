 package ch.exmachina.vaadin.autoforms;
 
 import com.vaadin.data.Item;
 import com.vaadin.server.Resource;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.BaseTheme;
 
 public abstract class AbstractFormTable extends FormField {
 
 	protected static final Object DELETE_BUTTON = "delete_button";
 
 	protected TableColumn[] columns;
 
 	protected Resource deleteButtonIcon;
 
 	protected ClickListener listener;
 
 	protected AbstractFormTable(Class<? extends Table> tableClass, String fieldName, TableColumn... columns) {
 		super(fieldName, tableClass);
 		this.columns = columns;
 
 		for (TableColumn column : columns) {
 			getTable().addContainerProperty(column.getColumnName(), column.getColumnType(), column.getDefaultValue());
 		}
 		getTable().addContainerProperty(DELETE_BUTTON, Button.class, null);
 
 		getTable().setEditable(true);
 		getTable().setSelectable(true);
		getTable().setMultiSelect(true);
 	}
 
 	@Override
 	public FormComponent setupForForm(UnbindedFormCreator formCreator) {
 		for (TableColumn column : columns) {
 			getTable().setColumnHeader(column.getColumnName(), formCreator.getLabelFor("table." + column.getColumnName()));
 		}
 		getTable().setColumnHeader(DELETE_BUTTON, "");
 
 		return super.setupForForm(formCreator);
 	}
 
 	public void addRow(final Object itemId, Object... colValues) {
 		assertColumnsCount(colValues);
 
 		Item item = addNewItemWithValues(itemId, colValues);
 
 		Button button = new Button();
 		button.setIcon(deleteButtonIcon);
 		button.setStyleName(BaseTheme.BUTTON_LINK);
 		button.addClickListener(new Button.ClickListener() {
 			@Override
 			public void buttonClick(Button.ClickEvent clickEvent) {
 				if (listener == null) {
 					getTable().removeItem(itemId);
 				} else {
 					listener.onClick(itemId);
 				}
 			}
 		});
 		item.getItemProperty(DELETE_BUTTON).setValue(button);
 	}
 
 	protected Item addNewItemWithValues(Object itemId, Object[] colValues) {
 		Item item = getTable().addItem(itemId);
 
 		for (int i = 0; i < colValues.length; i++) {
 			item.getItemProperty(columns[i].getColumnName()).setValue(colValues[i]);
 		}
 
 		return item;
 	}
 
 	public void setDeleteButtonIcon(Resource deleteButtonIcon) {
 		this.deleteButtonIcon = deleteButtonIcon;
 	}
 
 	public void setDeleteButtonClickListener(ClickListener listener) {
 		this.listener = listener;
 	}
 
 	public void setNotDeletable(Object itemId) {
 		getTable().getItem(itemId).getItemProperty(DELETE_BUTTON).setValue(null);
 	}
 
 	public Table getTable() {
 		return (Table) field;
 	}
 
 	public interface ClickListener {
 		void onClick(Object itemId);
 	}
 
 	protected void assertColumnsCount(Object[] colValues) {
 		if (colValues.length != columns.length) {
 			throw new RuntimeException("The number of value must be equal to the number of columns");
 		}
 	}
 }

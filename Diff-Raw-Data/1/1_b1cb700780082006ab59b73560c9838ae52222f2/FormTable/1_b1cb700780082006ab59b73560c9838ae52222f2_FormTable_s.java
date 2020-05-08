 package ch.exmachina.vaadin.autoforms;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.server.Resource;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.BaseTheme;
 
 public class FormTable extends FormField {
 
 	private static final Object DELETE_BUTTON = "delete_button";
 
 	private TableColumn[] columns;
 
 	private Resource deleteButtonIcon;
 
 	public FormTable(String fieldName, TableColumn... columns) {
 		super(fieldName, ImplicitSelectionTable.class);
 		this.columns = columns;
 
 		IndexedContainer container = new IndexedContainer();
 
 		for (TableColumn column : columns) {
 			container.addContainerProperty(column.getColumnName(), column.getColumnType(), column.getDefaultValue());
 		}
 		container.addContainerProperty(DELETE_BUTTON, Button.class, null);
 
 		getTable().setContainerDataSource(container);
 		getTable().setEditable(true);
 		getTable().setMultiSelect(true);
 	}
 
 	@Override
 	public FormComponent setupForForm(FormCreator formCreator) {
 
 		for (TableColumn column : columns) {
 			getTable().setColumnHeader(column.getColumnName(), formCreator.getLabelFor("table." + column.getColumnName()));
 		}
 		getTable().setColumnHeader(DELETE_BUTTON, "");
 
 		return super.setupForForm(formCreator);
 	}
 
 	public void setDeleteButtonIcon(Resource deleteButtonIcon) {
 		this.deleteButtonIcon = deleteButtonIcon;
 	}
 
 	public void addRow(final Object itemId, Object... colValues) {
 		if (colValues.length != columns.length) {
 			throw new RuntimeException("The number of value must be equal to the number of columns");
 		}
 
 		Item item = getTable().addItem(itemId);
 
 		for (int i = 0; i < colValues.length; i++) {
 			item.getItemProperty(columns[i].getColumnName()).setValue(colValues[i]);
 		}
 
 		Button button = new Button();
 		button.setIcon(deleteButtonIcon);
 		button.setStyleName(BaseTheme.BUTTON_LINK);
 		button.addClickListener(new Button.ClickListener() {
 			@Override
 			public void buttonClick(Button.ClickEvent clickEvent) {
 				getTable().removeItem(itemId);
 			}
 		});
 		item.getItemProperty(DELETE_BUTTON).setValue(button);
 	}
 
 	public Table getTable() {
 		return (Table) field;
 	}
 }

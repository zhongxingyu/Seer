 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.common.form;
 
 import org.cotrix.web.common.client.widgets.CustomDisclosurePanel;
 import org.cotrix.web.manage.client.codelist.common.form.ItemPanelHeader.Button;
 import org.cotrix.web.manage.client.codelist.common.form.ItemPanelHeader.HeaderListener;
 import org.cotrix.web.manage.client.codelist.common.form.ItemsEditingPanel.ItemEditingPanelListener;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.IsWidget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ItemPanel<T> extends Composite {
 	
 	public interface ItemView extends IsWidget, HasValueChangeHandlers<Void> {
 	}
 
 	public interface ItemEditor<T> {
 	
 		public void onStartEditing();
 		public void onStopEditing();
 		public void onEdit(AsyncCallback<Boolean> callBack);
 		public void onSave();
 
 		public void read();
 		public void write();
 		
 		public String getHeaderTitle();
 		public String getHeaderSubtitle();
 
 		public boolean validate();
 
 		public T getItem();
 	}
 
 	private boolean readOnly;
 	private boolean editable;
 	private boolean editing;
 	private boolean edited;
 
 	private ItemPanelHeader header;
 	private ItemEditingPanelListener<T> listener;
 	private ItemEditor<T> editor;
 
 	private CustomDisclosurePanel disclosurePanel;
 
 	public ItemPanel(final ItemPanelHeader header, ItemView view, ItemEditor<T> editor) {
 		this.editor = editor;
 		this.header = header;
 		this.edited = false;
 		
 		disclosurePanel = new CustomDisclosurePanel(header);
 		disclosurePanel.setWidth("100%");
 		disclosurePanel.setAnimationEnabled(true);
 		
 		header.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				disclosurePanel.toggle();
 			}
 		});
 
 		disclosurePanel.add(view);
 		initWidget(disclosurePanel);
 
 		view.addValueChangeHandler(new ValueChangeHandler<Void>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<Void> event) {
 				updateHeaderLabel();
 				validate();
 				edited = true;
 			}
 		});
 
 		disclosurePanel.addCloseHandler(new CloseHandler<CustomDisclosurePanel>() {
 
 			@Override
 			public void onClose(CloseEvent<CustomDisclosurePanel> event) {
 				header.setEditVisible(false);
 				header.setControlsVisible(false);
 				header.setSwitchVisible(true);
 				fireSelected();
 			}
 		});
 
 		disclosurePanel.addOpenHandler(new OpenHandler<CustomDisclosurePanel>() {
 
 			@Override
 			public void onOpen(OpenEvent<CustomDisclosurePanel> event) {
 				updateHeaderButtons();
 				fireSelected();
 				if (editing) validate();
 			}
 		});
 
 		header.setListener(new HeaderListener() {
 
 			@Override
 			public void onButtonClicked(Button button) {
 				switch (button) {
 					case EDIT: onEdit(); break;
 					case REVERT: onCancel(); break;
 					case SAVE: {
 						if (edited) onSave();
 						else onCancel();
 					} break;
 				}
 			}
 
 			@Override
 			public void onSwitchChange(boolean isDown) {
 				onSwitch(isDown);
 			}
 		});
 
 		editor.onStopEditing();
 		editing = false;
 		editable = false;
 
 		writeItem();
 		updateHeaderLabel();
 	}
 
 	private void fireSelected() {
 		if (listener!=null) listener.onSelect();
 	}
 
 	public void setSelected(boolean selected) {
 		header.setHeaderSelected(selected);
 	}
 
 	private void onSave() {
 		stopEdit();
 		readItem();
 		editor.onSave();
 		if (listener!=null) listener.onSave(editor.getItem());
 		updateHeaderLabel();
 	}
 
 	private void onEdit() {
 		edited = false;
 		editor.onEdit(new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result) {
 					startEdit();
 					validate();
 				}
 			}
 		});
 
 	}
 
 	private void onSwitch(boolean isDown) {
 		if (listener!=null) listener.onSwitch(isDown);
 	}
 
 	public void syncWithModel() {
 		writeItem();
 	}
 
 	private void readItem() {
 		editor.read();
 	}
 
 	public void enterEditMode() {
 		editable = true;
 		editing = true;
 		disclosurePanel.setOpen(true);
 		startEdit();
 	}
 
 	private void startEdit() {
 		editing = true;
 		editor.onStartEditing();
 		updateHeaderButtons();
 		updateHeaderLabel();
 	}
 
 	private void stopEdit() {
 		editing = false;
 		editor.onStopEditing();
 		updateHeaderButtons();	
 	}
 
 	private void onCancel() {
 		stopEdit();
 		if (listener!=null) listener.onCancel();
 		writeItem();
 		updateHeaderLabel();
 	}
 
 	private void writeItem() {
 		editor.write();
 	}
 
 	private void updateHeaderLabel() {
 		header.setHeaderTitle(editor.getHeaderTitle());
 		header.setHeaderSubtitle(editor.getHeaderSubtitle());
 	}
 
 	private void updateHeaderButtons() {
 		if (disclosurePanel.isOpen()) {
 			header.setEditVisible(!editing && editable && !readOnly);
 			header.setControlsVisible(editing);
 			header.setRevertVisible(editing);
 			header.setSaveVisible(false);
 			header.setSwitchVisible(false);
 		} else {
 			header.setEditVisible(false);
 			header.setControlsVisible(false);
 			header.setRevertVisible(false);
 			header.setSaveVisible(false);
 			header.setSwitchVisible(true);
 		}
 	}
 
 	private void validate() {
 		boolean valid = editor.validate();
 		header.setSaveVisible(valid);
 	}
 
 	public void setEditable(boolean editable) {
 		this.editable = editable;
 		updateHeaderButtons();
 	}
 
 	public void setListener(ItemEditingPanelListener<T> listener) {
 		this.listener = listener;
 	}
 
 	public void setSwitchDown(boolean down) {
 		header.setSwitchDown(down);
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 }

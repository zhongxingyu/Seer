 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.attribute;
 
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.client.widgets.CustomDisclosurePanel;
 import org.cotrix.web.common.shared.Language;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemEditingPanel;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemEditingPanelListener;
 import org.cotrix.web.manage.client.util.LabelHeader;
 import org.cotrix.web.manage.client.util.LabelHeader.Button;
 import org.cotrix.web.manage.client.util.LabelHeader.HeaderListener;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.Composite;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributePanel extends Composite implements ItemEditingPanel<UIAttribute> {
 
 	private boolean readOnly;
 	private boolean editable;
 	private boolean editing;
 
 	private LabelHeader header;
 	private AttributeDetailsPanel detailsPanel;
 	private ItemEditingPanelListener<UIAttribute> listener;
 	private UIAttribute attribute;
 
 	private CustomDisclosurePanel disclosurePanel;
 
 	private String id = Document.get().createUniqueId();
 
 	public AttributePanel(UIAttribute attribute, AttributeNameSuggestOracle oracle) {
 		this.attribute = attribute;
 		
 		header = new LabelHeader();
 		header.setSwitchVisible(true);
 		disclosurePanel = new CustomDisclosurePanel(header);
 		disclosurePanel.setWidth("100%");
 		disclosurePanel.setAnimationEnabled(true);
 
 		detailsPanel = new AttributeDetailsPanel(oracle);
 		disclosurePanel.add(detailsPanel);
 		initWidget(disclosurePanel);
 
 		detailsPanel.addValueChangeHandler(new ValueChangeHandler<Void>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<Void> event) {
 				validate();
 			}
 		});
 
 		disclosurePanel.addCloseHandler(new CloseHandler<CustomDisclosurePanel>() {
 
 			@Override
 			public void onClose(CloseEvent<CustomDisclosurePanel> event) {
 				header.setEditVisible(false);
 				header.setControlsVisible(false);
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
 					case SAVE: onSave(); break;
 				}
 			}
 
 			@Override
 			public void onSwitchChange(boolean isDown) {
 				onSwitch(isDown);
 			}
 		});
 
 		detailsPanel.setReadOnly(true);
 		editing = false;
 		editable = false;
 		
 		writeLink();
 		updateHeaderLabel();
 	}
 	
 	public void setSwitchVisible(boolean visible) {
 		header.setSwitchVisible(visible);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	private void fireSelected() {
 		if (listener!=null) listener.onSelect();
 	}
 
 	public void setSelected(boolean selected) {
 		header.setHeaderSelected(selected);
 	}
 
 	private void onSave() {
 		stopEdit();
 		readAttribute();
 		if (listener!=null) listener.onSave(attribute);
 		updateHeaderLabel();
 	}
 
 	private void onEdit() {
 		startEdit();
 		validate();
 	}
 	
 	private void onSwitch(boolean isDown) {
 		if (listener!=null) listener.onSwitch(isDown);
 	}
 	
 	public void syncWithModel() {
 		writeLink();
 	}
 
 	private void readAttribute() {
 		String name = detailsPanel.getName();
 		attribute.setName(ValueUtils.getValue(name));
 		
 		String type = detailsPanel.getType();
 		attribute.setType(ValueUtils.getValue(type));
 		
 		Language language = detailsPanel.getLanguage();
 		attribute.setLanguage(language);
 		
 		String value = detailsPanel.getValue();
 		attribute.setValue(value);
 
 	}
 
 	public void enterEditMode() {
 		editable = true;
 		editing = true;
 		disclosurePanel.setOpen(true);
 		startEdit();
 	}
 
 	private void startEdit() {
 		editing = true;
 		detailsPanel.setReadOnly(false);
 		updateHeaderButtons();
 	}
 
 	private void stopEdit() {
 		editing = false;
 		detailsPanel.setReadOnly(true);
 		updateHeaderButtons();	
 	}
 
 	private void onCancel() {
 		stopEdit();
 		if (listener!=null) listener.onCancel();
 		writeLink();
 	}
 
 	private void writeLink() {
 		detailsPanel.setName(ValueUtils.getLocalPart(attribute.getName()));
 		detailsPanel.setType(ValueUtils.getLocalPart(attribute.getType()));
 		detailsPanel.setLanguage(attribute.getLanguage());
 		detailsPanel.setValue(attribute.getValue());
 	}
 	
 	private void updateHeaderLabel() {
 		header.setHeaderLabel(ValueUtils.getLocalPart(attribute.getName()));
 	}
 
 	private void updateHeaderButtons() {
 		if (disclosurePanel.isOpen()) {
 			header.setEditVisible(!editing && editable && !readOnly);
 			header.setControlsVisible(editing);
 			header.setRevertVisible(editing);
 			header.setSaveVisible(false);
 		} else {
 			header.setEditVisible(false);
 			header.setControlsVisible(false);
 			header.setRevertVisible(false);
 			header.setSaveVisible(false);
 		}
 	}
 
 	private void validate() {
 		boolean valid = true;
 
 		String name = detailsPanel.getName();
 		boolean nameValid = name!=null && !name.isEmpty();
 		detailsPanel.setNameFieldValid(nameValid);
 		valid &= nameValid;
 
 		header.setSaveVisible(valid);
 	}
 
 	@Override
 	public void setEditable(boolean editable) {
 		this.editable = editable;
 		updateHeaderButtons();
 	}
 
 	@Override
 	public void setListener(ItemEditingPanelListener<UIAttribute> listener) {
 		this.listener = listener;
 	}
 
 	@Override
 	public void setSwitchDown(boolean down) {
 		header.setSwitchDown(down);
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 }

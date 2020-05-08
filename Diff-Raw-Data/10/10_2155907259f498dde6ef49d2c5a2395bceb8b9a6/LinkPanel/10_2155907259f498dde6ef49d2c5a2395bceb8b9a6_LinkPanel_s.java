 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.link;
 
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.client.widgets.CustomDisclosurePanel;
 import org.cotrix.web.common.shared.codelist.UILink;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemEditingPanel;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemEditingPanelListener;
 import org.cotrix.web.manage.client.util.LabelHeader;
 import org.cotrix.web.manage.client.util.LabelHeader.Button;
 import org.cotrix.web.manage.client.util.LabelHeader.HeaderListener;
 import org.cotrix.web.manage.shared.UICodeInfo;
 import org.cotrix.web.manage.shared.UILinkTypeInfo;
 
 import com.allen_sauer.gwt.log.client.Log;
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
 public class LinkPanel extends Composite implements ItemEditingPanel<UILink> {
 
 	private boolean editable;
 	private boolean editing;
 
 	private LabelHeader header;
 	private LinkDetailsPanel detailsPanel;
 	private ItemEditingPanelListener<UILink> listener;
 	private UILink link;
 
 	private CustomDisclosurePanel disclosurePanel;
 
 	private String id = Document.get().createUniqueId();
 
 	public LinkPanel(UILink link, LinksCodelistInfoProvider codelistInfoProvider) {
 		this.link = link;
 		
 		header = new LabelHeader();
 		disclosurePanel = new CustomDisclosurePanel(header);
 		disclosurePanel.setWidth("100%");
 		disclosurePanel.setAnimationEnabled(true);
 
 		detailsPanel = new LinkDetailsPanel(codelistInfoProvider);
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
 		});
 
 		detailsPanel.setReadOnly(true);
 		editing = false;
 		editable = false;
 		
 		writeLink();
 		updateHeaderLabel();
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
 		readLink();
 		if (listener!=null) listener.onSave(link);
 		updateHeaderLabel();
 	}
 
 	private void onEdit() {
 		startEdit();
 		validate();
 	}
 	
 	public void syncWithModel() {
 		writeLink();
 	}
 
 	private void readLink() {
 		UILinkTypeInfo type = detailsPanel.getLinkType();
 		link.setTypeId(type!=null?type.getId():null);
 		link.setTypeName(type!=null?type.getName():null);
 		
 		UICodeInfo code = detailsPanel.getCode();
 		link.setTargetId(code!=null?code.getId():null);
 		link.setTargetName(code!=null?code.getName():null);
 
 		link.setAttributes(detailsPanel.getAttributes());
 	}
 
 	public void enterEditMode() {
 		editable = true;
 		editing = true;
 		disclosurePanel.setOpen(true);
 		detailsPanel.setValueVisible(false);
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
 		detailsPanel.setLinkType(link.getTypeId(), link.getTypeName());
 		detailsPanel.setCode(link.getTargetId(), link.getTargetName());
 		detailsPanel.setValue(link.getValue());
 		detailsPanel.setValueVisible(link.getValue()!=null);
 		detailsPanel.setAttributes(link.getAttributes());
 	}
 	
 	private void updateHeaderLabel() {
 		header.setHeaderLabel(ValueUtils.getLocalPart(link.getTypeName()));
 	}
 
 	private void updateHeaderButtons() {
 		if (disclosurePanel.isOpen()) {
 			header.setEditVisible(!editing && editable);
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
 
 		UILinkTypeInfo linkType = detailsPanel.getLinkType();
 		boolean linkTypeValid = linkType!=null;
 		detailsPanel.setValidLinkType(linkTypeValid);
 		valid &= linkTypeValid;
 		
 		UICodeInfo code = detailsPanel.getCode();
 		boolean codeValid = code!=null;
		detailsPanel.setValidLinkType(codeValid);
 		valid &= codeValid;
 
		
 		valid &= detailsPanel.areAttributesValid();
 
 		Log.trace("Valid ? "+valid);
 		header.setSaveVisible(valid);
 
 	}
 
 	@Override
 	public void setEditable(boolean editable) {
 		this.editable = editable;
 		updateHeaderButtons();
 	}
 
 	@Override
 	public void setListener(ItemEditingPanelListener<UILink> listener) {
 		this.listener = listener;
 	}
 
 }

 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.attribute;
 
 import org.cotrix.web.common.client.widgets.EditableLabel;
 import org.cotrix.web.common.client.widgets.LanguageListBox;
 import org.cotrix.web.common.shared.codelist.linktype.CodeNameType;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SuggestListBox;
import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributeDetailsPanel extends Composite implements HasValueChangeHandlers<Void>{
 
 	public static final String CODE_NAME_VALUE_TYPE = Document.get().createUniqueId();
 	public static final CodeNameType CODE_NAME_TYPE = new CodeNameType();
 
 	private static AtttributeDetailsPanelUiBinder uiBinder = GWT.create(AtttributeDetailsPanelUiBinder.class);
 
 	interface AtttributeDetailsPanelUiBinder extends UiBinder<Widget, AttributeDetailsPanel> {}
 
 	interface Style extends CssResource {
 		String error();
 		String editor();
 	}
 
 	@UiField EditableLabel nameBoxContainer;
 	@UiField(provided=true) SuggestListBox nameBox;
 	
 	@UiField EditableLabel typeBoxContainer;
 	@UiField TextBox typeBox;
 	
 	@UiField EditableLabel languageBoxContainer;
 	@UiField LanguageListBox languageBox;
 	
 	@UiField EditableLabel valueBoxContainer;
 	@UiField TextBox valueBox;
 
 	@UiField Style style;
 
 	public AttributeDetailsPanel(AttributeNameSuggestOracle oracle) {
 
 		nameBox = new SuggestListBox(oracle);
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		setupNameField();
 		setupTypeField();
 		setupLanguageField();
 		setupValueField();
 	}
 	
 	private void setupNameField() {
 		nameBox.addValueChangeHandler(nameBoxContainer);
		nameBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				nameBoxContainer.setText(event.getSelectedItem().getDisplayString());
				fireChange();
			}
		});
 		nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		nameBox.getValueBox().addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	private void setupTypeField() {
 		typeBox.addValueChangeHandler(typeBoxContainer);
 		typeBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		typeBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	private void setupLanguageField() {
 		languageBox.addValueChangeHandler(languageBoxContainer);
 		languageBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 	}
 	
 	private void setupValueField() {
 		valueBox.addValueChangeHandler(valueBoxContainer);
 		valueBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		valueBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	public String getName() {
 		return nameBox.getValue();
 	}
 	
 	public void setName(String name) {
 		nameBox.setValue(name);
 		nameBoxContainer.setText(name);
 	}
 	
 	public void setNameFieldValid(boolean valid) {
 		nameBox.setStyleName(style.error(), !valid);
 	}
 	
 	public String getType() {
 		return typeBox.getValue();
 	}
 	
 	public void setType(String type) {
 		typeBox.setValue(type, false);
 		typeBoxContainer.setText(type);
 	}
 	
 	public void setTypeFieldValid(boolean valid) {
 		typeBox.setStyleName(style.error(), !valid);
 	}
 	
 	public String getLanguage() {
 		return languageBox.getLanguage();
 	}
 	
 	public void setLanguage(String language) {
 		languageBox.setLanguage(language);
 		languageBoxContainer.setText(language);
 	}
 	
 	public void setLanguageFieldValid(boolean valid) {
 		languageBox.setStyleName(style.error(), !valid);
 	}
 	
 	public String getValue() {
 		return valueBox.getValue();
 	}
 	
 	public void setValue(String value) {
 		valueBox.setValue(value, false);
 		valueBoxContainer.setText(value);
 	}
 	
 	public void setValueFieldValid(boolean valid) {
 		valueBox.setStyleName(style.error(), !valid);
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		
 		nameBoxContainer.setReadOnly(readOnly);
 		if (readOnly) nameBox.setStyleName(style.error(), false);
 		
 		typeBoxContainer.setReadOnly(readOnly);
 		if (readOnly) typeBox.setStyleName(style.error(), false);
 		
 		languageBoxContainer.setReadOnly(readOnly);
 		if (readOnly) languageBox.setStyleName(style.error(), false);
 
 		valueBoxContainer.setReadOnly(readOnly);
 		if (readOnly) valueBox.setStyleName(style.error(), false);
 	}
 
 	private void fireChange() {
 		ValueChangeEvent.fire(this, null);
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Void> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 
 }

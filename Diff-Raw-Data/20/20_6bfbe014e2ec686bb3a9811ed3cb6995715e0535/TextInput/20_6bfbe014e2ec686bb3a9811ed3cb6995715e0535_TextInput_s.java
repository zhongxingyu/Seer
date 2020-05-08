 package com.byclosure.gwtinput.ui.widgets;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class TextInput extends TextBox implements IsUserTextInput {
 
	private static final String ERRORLABEL_INVALID = "errorlabel invalid";
	private static final String ERRORLABEL_VALID = "errorlabel";
 	private HasText errorLabel;
 	private String errorMessage;
 
 	public TextInput(){}
 	
 	public TextInput(InputType type) {
 		super();
 		setType(type.getType());
 	}
 	
 	public IValidityState getValidityState(){
 		return getNativeValidityState(getElement());
 	}
 	
 	@Override
 	public void checkValidity() {
 		if(errorLabel != null && errorMessage !=  null){
 			errorLabel.setText(errorMessage);
 			
 			String errorStyleName = isValid() ? ERRORLABEL_VALID : ERRORLABEL_INVALID;
 			((Widget)errorLabel).setStyleName(errorStyleName);
 		}
 	}
 	
 	public boolean isValid(){
 		return checkValidity(getElement());
 	}
 	
 	/*
 	 * UIBuinder API 
 	 */
 	@Override
 	public void setPlaceholder(String text) {
 		getElement().setAttribute(PLACEHOLDER, text);
 	}
 	
 	@Override
 	public void setRequired(boolean isRequired){
 		getElement().setAttribute(REQUIRED, String.valueOf(isRequired));
 	}
 	
 	@Override
 	public void setType(String type) {
 		getElement().setAttribute(TYPE, type);
 	}
 	
 	@Override
 	public void setErrorLabel(HasText errorLabel) {
 		((Widget)errorLabel).setStyleName(ERRORLABEL_VALID);
 		this.errorLabel =  errorLabel;
 	}
 	
 	@Override
 	public void setErrorMessage(String message) {
 		this.errorMessage =  message;
 	}

 	
 	/*
 	 *  Helper native methods
 	 */
 	protected final native ValidityState getNativeValidityState(Element element)/*-{
 		return element.validity;
 	}-*/;
 	
 	protected final native  boolean  checkValidity(Element element)/*-{
 		return element.checkValidity();
 	}-*/;
 	
 	protected final native  boolean  willValidate(Element element)/*-{
 		return element.willValidate();
 	}-*/;
 
 }

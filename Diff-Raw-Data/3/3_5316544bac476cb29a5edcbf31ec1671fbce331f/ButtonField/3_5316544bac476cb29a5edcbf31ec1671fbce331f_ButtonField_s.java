 package monbulk.shared.Form;
 
 import monbulk.shared.util.GWTLogger;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class ButtonField extends FormField {
 
 	public ButtonField(String FormName, String ButtonName) {
 		super(FormName, "Button");
 		PushButton selectButton = new PushButton();
 		selectButton.setStyleName("btnDefault-Add");
 		selectButton.setText(ButtonName);
		
 		this._FieldVaLueWidget = selectButton;
 		Label FieldNameLabel = new Label();
 		FieldNameLabel.setText(this.FieldName);
 		this._ValidationWidget = new Label();
 		this.returnWidget = new FormWidget(this.FieldName);
 		
 		this.returnWidget.setLabelWidget(FieldNameLabel);
 		this.returnWidget.setFormWidget(_FieldVaLueWidget);
 		this.returnWidget.setValidWidget(_ValidationWidget);
 		//this._FieldVaLueWidget 
 	}
 	public void setButtonStyleName(String StyleName)
 	{
 		this._FieldVaLueWidget.setStyleName(StyleName);
 		
 	}
 	public void setParentStyleName(String StyleName)
 	{
 		this.returnWidget.setStyleName(StyleName);
 	}
 	public void attachClickHandler(ClickHandler handler)
 	{
 		try
 		{
 		PushButton selectButton = (PushButton) this._FieldVaLueWidget;
 		selectButton.addClickHandler(handler);
 		}
 		catch(java.lang.ClassCastException ex)
 		{
 			GWTLogger.Log("Could not cast Field Widget to PushButton:" + this.GetFieldName(), "ButtonField", "attachClickHandler", "49");
 		}
 	}
 
 }
 

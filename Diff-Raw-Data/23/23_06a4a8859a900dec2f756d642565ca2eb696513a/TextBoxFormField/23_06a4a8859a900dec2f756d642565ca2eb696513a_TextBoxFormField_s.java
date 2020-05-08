 package kornell.gui.client.uidget.formfield;
 
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class TextBoxFormField implements KornellFormField<TextBox> {
 
 	TextBox field;
 	
 	public TextBoxFormField(TextBox field) {
 		this.field = field;
 	}
 
 	@Override
 	public Widget getFieldWidget() {
 		return field;
 	}
 
 	@Override
 	public String getDisplayText() {
 		return field.getValue();
 	}
 
 	@Override
 	public String getPersistText() {
 		return field.getValue();
 	}
 
 }

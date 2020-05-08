 package topshelf.gwt.editor.client.display;
 
 import java.math.BigDecimal;
 
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 
 public class BigDecimalLabel extends Composite
 	implements LeafValueEditor<BigDecimal> {
 
 	Label label = new Label();
 	BigDecimal value;
 	public BigDecimalLabel() {
 		initWidget(label);
 	}
 	
 	@Override
 	public void setValue(BigDecimal value) {
 		this.value = value;
		if (null != value)
			label.setText(value.toPlainString());
 	}
 	
 	@Override
 	public BigDecimal getValue() {
 		return value;
 	}
 }

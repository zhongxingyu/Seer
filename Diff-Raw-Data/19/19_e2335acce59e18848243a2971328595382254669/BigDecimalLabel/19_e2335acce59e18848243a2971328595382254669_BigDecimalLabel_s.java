 package topshelf.gwt.editor.client;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class BigDecimalLabel extends Composite
 	implements LeafValueEditor<BigDecimal> {
 
 	HasText holder;
 	BigDecimal value;
 	RoundingMode roundingMode = RoundingMode.HALF_UP;
 	Integer scale;
 	
 	boolean currencyFormat = false;
 	boolean decimalFormat = false;
 	boolean percentFormat = false;
 	boolean scientificFormat = false;
 	
 	public void setCurrency(boolean currencyFormat) {
 		this.currencyFormat = currencyFormat;
 	}
 
 	public void setDecimal(boolean decimalFormat) {
 		this.decimalFormat = decimalFormat;
 	}
 
 	public void setPercent(boolean percentFormat) {
 		this.percentFormat = percentFormat;
 	}
 
 	public void setScientific(boolean scientificFormat) {
 		this.scientificFormat = scientificFormat;
 	}
 
 	public BigDecimalLabel() {
 		this(new Label());
 		setStyleName("ts-BigDecimalLabel");
 	}
 	
 	protected BigDecimalLabel(HasText inner) {
 		holder = inner;
 		initWidget((Widget)inner);
 	}
 	
 	public void setScale(int scale) {
 		this.scale = scale;
 	}
 	
 	public void setRoundingMode(RoundingMode roundingMode) {
 		this.roundingMode = roundingMode;
 	}
 	
 	@Override
 	public void setValue(BigDecimal value) {
 		this.value = value;
 		if (null != value) {
			BigDecimal scaled = (null == scale) ? value : value.setScale(scale, roundingMode);
 			String txt;
 			if (currencyFormat)
				txt = NumberFormat.getCurrencyFormat().format(scaled);
 			else if (decimalFormat)
				txt = NumberFormat.getDecimalFormat().format(scaled);
 			else if (percentFormat)
				txt = NumberFormat.getPercentFormat().format(scaled);
 			else if (scientificFormat)
				txt = NumberFormat.getScientificFormat().format(scaled);
			else
				txt = value.toPlainString();
 			holder.setText(txt);
 		}
 	}
 	
 	@Override
 	public BigDecimal getValue() {
 		return value;
 	}
 }

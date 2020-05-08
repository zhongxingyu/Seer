 package presentationLayer;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.FacesConverter;
 import javax.inject.Named;
 
 import org.joda.money.Money;
 import org.joda.money.format.MoneyAmountStyle;
 import org.joda.money.format.MoneyFormatterBuilder;
 
 import util.Config;
 import util.Messages;
 
 @Named
 @RequestScoped
 @FacesConverter(forClass = Money.class)
 public class MoneyConverter implements Converter {
 
 	public MoneyConverter() {}
 
 
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
 		// il valore viene convertito da numeri decimali, senza valuta
 		try {
 			String s = value.replaceAll("\\.", "");
 			s = s.replaceAll(",", "\\.");
 			System.out.println("Money converter: value = " + value + "; s = " + s);
 			return Money.of(Config.currency, new BigDecimal(s).setScale(2, RoundingMode.HALF_EVEN));
 		} catch (NumberFormatException e) {
 			String[] params = { (String) component.getAttributes().get("label") };
 			throw new ConverterException(Messages.getErrorMessage("err_invalidValue", params));
 		}
 	}
 
 
 	@Override
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
 		if (null == value) {
 			return "";
 		}
 
 		Boolean plain = true;
 		try {
 			plain = Boolean.valueOf(component.getAttributes().get("plain").toString());
 		} catch (NullPointerException e) {
 			plain = true;
 		}
		
		Money m = Money.of(Config.currency, ((Money) value).getAmount().setScale(2));
 
 		if (null == plain || true == plain) {
 			// return ((Money) value).getAmount().toPlainString();
 			String s = new MoneyFormatterBuilder().appendAmount(MoneyAmountStyle.ASCII_DECIMAL_COMMA_GROUP3_DOT).toFormatter(Config.locale)
					.print(m);
 			return s;
 		}
 		else {
 			String s = new MoneyFormatterBuilder().appendAmount(MoneyAmountStyle.ASCII_DECIMAL_COMMA_GROUP3_DOT).appendCurrencySymbolLocalized()
					.toFormatter(Config.locale).print(m);
 			return s;
 		}
 	}
 }

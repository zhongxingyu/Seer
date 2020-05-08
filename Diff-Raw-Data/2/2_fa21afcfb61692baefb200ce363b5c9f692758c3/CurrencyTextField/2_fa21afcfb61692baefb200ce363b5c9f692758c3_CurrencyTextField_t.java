 package ch.exmachina.vaadin.autoforms.components;
 
 import com.vaadin.event.FieldEvents;
 import com.vaadin.ui.TextField;
 import org.apache.commons.lang3.StringUtils;
 
 public class CurrencyTextField extends ConstrainedTextField {
 
 	private static final String SEPARATOR = ",";
 	private static final String CURRENCY_SUFFIX = SEPARATOR + "00";
 
 	public CurrencyTextField() {
 		super();
 
 		addBlurListener(new FieldEvents.BlurListener() {
 			@Override
 			public void blur(FieldEvents.BlurEvent event) {
 				TextField source = (TextField) event.getSource();
 				String value = source.getValue();
 
				if (StringUtils.isBlank(value)) {
 					return;
 				}
 
 				int indexOf = value.indexOf(SEPARATOR) == -1 ? value.length() : value.indexOf(SEPARATOR);
 
 				String newValue = value + CURRENCY_SUFFIX.substring(value.length() - indexOf);
 
 				source.setValue(newValue);
 			}
 		});
 	}
 
 	@Override
 	protected boolean isValidText(String text) {
 		return containsOnlyAllowedChars(text) && containsOnlyOneSeparator(text) && containsOnlyTwoDecimals(text);
 	}
 
 	private boolean containsOnlyTwoDecimals(String text) {
 		String[] tokens = text.split(SEPARATOR);
 		return !(tokens.length > 1) || tokens[1].length() <= 2;
 	}
 
 	private boolean containsOnlyOneSeparator(String text) {
 		return StringUtils.countMatches(text, SEPARATOR) < 2 && !text.startsWith(SEPARATOR);
 	}
 
 	private boolean containsOnlyAllowedChars(String text) {
 		return text.matches(String.format("[0-9%s]*", SEPARATOR));
 	}
 }

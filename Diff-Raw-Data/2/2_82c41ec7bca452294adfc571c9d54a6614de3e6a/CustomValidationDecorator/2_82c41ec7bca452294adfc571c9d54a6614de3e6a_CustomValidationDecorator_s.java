 
 package axirassa.webapp.services.validation;
 
 import org.apache.tapestry5.BaseValidationDecorator;
 import org.apache.tapestry5.CSSClassConstants;
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.ValidationTracker;
 import org.apache.tapestry5.dom.Element;
 import org.apache.tapestry5.services.Environment;
 
 /**
  * 
  * Based on: http://jumpstart.doublenegative.com.au/jumpstart/examples/input/
  * novalidationbubbles1
  * 
  * @author wiktor
  * 
  */
 public class CustomValidationDecorator extends BaseValidationDecorator {
 	private final MarkupWriter markupwriter;
 	private final Environment environment;
 
 
 	public CustomValidationDecorator(final Environment environment, final MarkupWriter markupwriter) {
 		this.environment = environment;
 		this.markupwriter = markupwriter;
 	}
 
 
 	@Override
 	public void insideLabel(final Field field, final Element element) {
 		if (field == null)
 			return;
 
 		if (field.isRequired()) {
 			element.addClassName("required-label");
 			element.getContainer().addClassName("required-label-c");
 		}
 
 		if (inError(field)) {
 			element.addClassName("error-label");
 			element.getContainer().addClassName("error-label-c");
 		}
 	}
 
 
 	@Override
 	public void insideField(final Field field) {
 		if (field != null && inError(field)) {
 			markupwriter.getElement().addClassName(CSSClassConstants.ERROR);
 		}
 	}
 
 
 	@Override
 	public void afterField(final Field field) {
 		if (field.isRequired()) {
 			getElement().addClassName("required-field");
 			getElement().getContainer().addClassName("required-field-c");
 		}
 
 		if (inError(field)) {
 			getElement().addClassName("error-field");
 			getElement().getContainer().addClassName("error-field-c");
 		}
 	}
 
 
 	private boolean inError(final Field field) {
 		return getTracker().inError(field);
 	}
 
 
 	private ValidationTracker getTracker() {
 		return environment.peekRequired(ValidationTracker.class);
 	}
 
 
 	private Element getElement() {
 		return markupwriter.getElement();
 	}
 }

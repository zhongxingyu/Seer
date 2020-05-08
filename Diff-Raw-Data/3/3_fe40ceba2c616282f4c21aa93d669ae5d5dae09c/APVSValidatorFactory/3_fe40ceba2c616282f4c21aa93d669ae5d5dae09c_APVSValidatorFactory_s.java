 package ch.cern.atlas.apvs.client.ui;
 
 import javax.validation.Validator;
 
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.User;
 import ch.cern.atlas.apvs.domain.Device;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
 import com.google.gwt.validation.client.GwtValidation;
 import com.google.gwt.validation.client.impl.AbstractGwtValidator;
 
 public final class APVSValidatorFactory extends AbstractGwtValidatorFactory {
 
 	/**
 	 * Validator marker for the Validation Sample project. Only the classes
 	 * listed in the {@link GwtValidation} annotation can be validated.
 	 */
 	@GwtValidation(value = { User.class, Device.class, Intervention.class })
 	public interface GwtValidator extends Validator {
 	}
 
 	@Override
 	public AbstractGwtValidator createValidator() {
 		return GWT.create(GwtValidator.class);
 	}
}

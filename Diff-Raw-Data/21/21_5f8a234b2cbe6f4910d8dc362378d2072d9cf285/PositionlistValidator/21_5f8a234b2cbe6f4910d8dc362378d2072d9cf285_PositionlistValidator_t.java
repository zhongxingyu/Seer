 package de.ptb.epics.eve.editor.views.motoraxisview.positionlistcomposite;
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 
 /**
  * @author Marcus Michalsky
  * @since 1.7
  */
 public class PositionlistValidator implements IValidator {
	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IStatus validate(Object value) {
 		String positions = String.valueOf(value);
 		if (positions.isEmpty()) {
 			return ValidationStatus.error("Please provide at least 1 position!");
 		}
 		if (!isValid(positions)) {
 			return ValidationStatus.error("The list contains syntax errors");
 		}
 		return ValidationStatus.ok();
 	}
 	
 	/*
 	 * future implementations could check, if a reasonable string is given
 	 */
 	private boolean isValid(String list) {
 		// TODO provide implementation
 		return true;
 	}
 }

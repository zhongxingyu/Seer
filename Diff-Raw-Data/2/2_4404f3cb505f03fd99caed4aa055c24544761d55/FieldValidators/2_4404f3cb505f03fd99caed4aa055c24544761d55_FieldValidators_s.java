 package org.eclipse.dltk.ui.preferences;
 
 import java.io.File;
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.dialogs.StatusInfo;
 
 public class FieldValidators {
 
	protected static class FilePathValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setWarning(ValidatorMessages.FilePathIsEmpty);
 			} else {
 				File file = Path.fromOSString(text).toFile();
 
 				if (!file.exists()) {
 					status.setError(MessageFormat.format(
 							ValidatorMessages.FilePathNotExists,
 							new Object[] { text }));
 				} else if (file.isDirectory()) {
 					status.setError(MessageFormat.format(
 							ValidatorMessages.FilePathIsInvalid,
 							new Object[] { text }));
 				}
 			}
 
 			return status;
 		}
 	}
 
 	public static class PositiveNumberValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setError(ValidatorMessages.PositiveNumberIsEmpty);
 			} else {
 				try {
 					int value = Integer.parseInt(text);
 					if (value < 0) {
 						status
 								.setError(Messages
 										.format(
 												ValidatorMessages.PositiveNumberIsInvalid,
 												text));
 					}
 				} catch (NumberFormatException e) {
 					status.setError(Messages.format(
 							ValidatorMessages.PositiveNumberIsInvalid, text));
 				}
 			}
 
 			return status;
 		}
 	}
 
 	public static class PortValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setError(ValidatorMessages.PortIsEmpty);
 			} else {
 				try {
 					int value = Integer.parseInt(text);
 					if (value < 1000 || value > 65535) {
 						status.setError(Messages.format(
 								ValidatorMessages.PortShouldBeInRange, text));
 					}
 				} catch (NumberFormatException e) {
 					status.setError(Messages.format(
 							ValidatorMessages.PortShouldBeInRange, text));
 				}
 			}
 
 			return status;
 		}
 	}
 
 	// Available validators
 	public static IFieldValidator PATH_VALIDATOR = new FilePathValidator();
 
 	public static IFieldValidator POSITIVE_NUMBER_VALIDATOR = new PositiveNumberValidator();
 
 	public static IFieldValidator PORT_VALIDATOR = new PortValidator();
 }

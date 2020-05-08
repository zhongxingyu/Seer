 package org.eclipse.dltk.ui.preferences;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.dialogs.StatusInfo;
 
 public final class FieldValidators {
 
 	public static class DirValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setError(ValidatorMessages.DirPathIsEmpty);
 				return status;
 			}
 
 			File dir = Path.fromOSString(text).toFile();
 			if (!dir.exists()) {
 				status.setError(Messages.format(
 						ValidatorMessages.DirPathNotExists, text));
 			} else if (!dir.isDirectory()) {
 				status.setError(Messages.format(
 						ValidatorMessages.DirPathIsInvalid, text));
 			}
 
 			return status;
 		}
 	}
 
 	public static class FileNameValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setError(ValidatorMessages.FileNameIsEmpty);
 			}
			
 			return status;
 		}
 	}
 
 	public static class FilePathValidator implements IFieldValidator {
 		public IStatus validate(String text) {
 			StatusInfo status = new StatusInfo();
 
 			if (text.trim().length() == 0) {
 				status.setWarning(ValidatorMessages.FilePathIsEmpty);
 			} else {
				File file = Path.fromOSString(text).toFile();
 
 				if (!file.exists()) {
 					status.setError(Messages.format(
 							ValidatorMessages.FilePathNotExists, text));
 				} else if (file.isDirectory()) {
 					status.setError(Messages.format(
 							ValidatorMessages.FilePathIsInvalid, text));
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
 	public static IFieldValidator FILE_NAME_VALIDATOR = new FileNameValidator();
	
 	public static IFieldValidator PATH_VALIDATOR = new FilePathValidator();
 
 	public static IFieldValidator POSITIVE_NUMBER_VALIDATOR = new PositiveNumberValidator();
 
 	public static IFieldValidator PORT_VALIDATOR = new PortValidator();
 
 	public static IFieldValidator DIR_VALIDATOR = new DirValidator();
 }

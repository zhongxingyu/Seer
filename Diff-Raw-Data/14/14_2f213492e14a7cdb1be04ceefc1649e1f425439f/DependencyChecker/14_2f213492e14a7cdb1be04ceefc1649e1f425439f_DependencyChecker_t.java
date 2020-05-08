 package gui.pluginRegistry;
 
 import java.util.Map.Entry;
 import java.util.Set;
 
 import annotations.Requirement;
 import annotations.SimProp;
 
 public class DependencyChecker {
 
	public static Boolean errorsInConfig;
 	public static void checkAll(SimPropRegistry gcr) {
		errorsInConfig = false;
		
 		Set<Entry<String, SimProp>> allSimProps = gcr.getAllSimProps();
 
 		Requirement enableRequirement = null;
 		Requirement valueRequirement = null;
 
 		// Marker for errors in instantiate
 		Boolean errorValue;
 		Boolean errorEnabled;
 
 		// Marker if the simprop is disabled so that 
 		// 1. no further check for enabled are made
 		// 2. ValueChecks are not executed
 		Boolean disabled;
 
 		for (Entry<String, SimProp> entry : allSimProps) {
 			errorValue = false;
 			errorEnabled = false;
 			disabled = false;
 
 			// *********** Check Enabled
 			// Get Value from Annotation
 			Class<? extends Requirement>[] enableRequirements = (Class<? extends Requirement>[]) (entry
 					.getValue()).getEnable_requirements();
 			// Annotation is always null if it is not set
 			if (enableRequirements != null) {
 				// Iterate over all annotated Classes
 				for (Class<? extends Requirement> requirement : enableRequirements) {
 					// Checks if the simprop is not already disabled.
 					if (!disabled) {
 						try {
 							// Try to instante the Requirement
 							enableRequirement = requirement.newInstance();
 						} catch (InstantiationException e) {
 							// TODO Logging
 							e.printStackTrace();
 							errorEnabled = true;
 						} catch (IllegalAccessException e) {
 							e.printStackTrace();
 							// TODO Logging
 							errorEnabled = true;
 						}
 						if (!errorEnabled) {
 							disabled = !enableRequirement.check();
 						}
 					}
 				}
 			}
 
 			// Checks if the simprop is not already disabled.
 			if (!disabled) {
 
 				// *********** Check Enabled
 				// Get Value from Annotation
 				Class<? extends Requirement>[] valueRequirements = (Class<? extends Requirement>[]) (entry
 						.getValue()).getValue_requirements();
 				if (valueRequirements != null) {
 					for (Class<? extends Requirement> requirement : valueRequirements) {
 						// Checks if the simprop is not already disabled.
 						try {
 							// Try to instante the Requirement
 							valueRequirement = requirement.newInstance();
 						} catch (InstantiationException e) {
 							// TODO Logging
 							e.printStackTrace();
 							errorValue = true;
 						} catch (IllegalAccessException e) {
 							e.printStackTrace();
 							// TODO Logging
 							errorValue = true;
 						}
 						if (!errorValue) {
 							valueRequirement.check();
 						}
 					}
 
 				}
 			}
 
 		}
 
 	}
 }

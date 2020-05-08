 package org.eclipse.dltk.validators.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.compiler.problem.DLTKProblemReporter;
 import org.eclipse.dltk.compiler.problem.DefaultProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.validators.internal.core.CompositeId;
 import org.eclipse.dltk.validators.internal.core.ListenerList;
 import org.eclipse.dltk.validators.internal.core.ValidatorDefinitionsContainer;
 import org.eclipse.dltk.validators.internal.core.ValidatorManager;
 import org.eclipse.dltk.validators.internal.core.ValidatorsCore;
 
 public final class ValidatorRuntime {
 
 	public static final String PREF_VALIDATOR_XML = ValidatorsCore.PLUGIN_ID
 			+ ".PREF_VALIDATOR_XML"; //$NON-NLS-1$
 
 	public static final String MARKER_VALIDATOR = ValidatorsCore.PLUGIN_ID
 			+ ".marker_validator_id";
 
 	// lock for interpreter initialization
 	private static Object fgValidatorLock = new Object();
 	private static boolean fgInitializingValidators = false;
 	//
 	private static ListenerList fgValidatorListeners = new ListenerList(5);
 	//	
 	//
 	// private static ThreadLocal fgProjects = new ThreadLocal(); // Lists
 	// private static ThreadLocal fgEntryCount = new ThreadLocal(); // Integers
 
 	private static Set fgContributedValidators = new HashSet();
 
 	private ValidatorRuntime() {
 	}
 
 	public static IValidatorType getValidatorType(String id) {
 		IValidatorType[] interpreterTypes = getValidatorTypes();
 		for (int i = 0; i < interpreterTypes.length; i++) {
 			if (interpreterTypes[i].getID().equals(id)) {
 				return interpreterTypes[i];
 			}
 		}
 		return null;
 	}
 
 	public static IValidatorType[] getValidatorTypes() {
 		initializeValidators();
 		try {
 			return ValidatorManager.getAllValidatorTypes();
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static IValidatorType[] getValidatorTypes(String nature) {
 		try {
 			return ValidatorManager.getValidators(nature);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String getCompositeIdFromValidator(IValidator validator) {
 		if (validator == null) {
 			return null;
 		}
 		IValidatorType validatorType = validator.getValidatorType();
 		String typeID = validatorType.getID();
 		CompositeId id = new CompositeId(new String[] { typeID,
 				validator.getID() });
 		return id.toString();
 	}
 
 	public static IValidator getValidatorFromCompositeId(String idString) {
 		if (idString == null || idString.length() == 0) {
 			return null;
 		}
 		CompositeId id = CompositeId.fromString(idString);
 		if (id.getPartCount() == 2) {
 			IValidatorType validatorType = getValidatorType(id.get(0));
 			if (validatorType != null) {
 				return validatorType.findValidator(id.get(1));
 			}
 		}
 		return null;
 	}
 
 	public static void saveInterpreterConfiguration() throws CoreException {
 		IValidatorType[] vals = getValidatorTypes();
 		if (vals == null || vals.length == 0) {
 			// if the Interpreter types have not been instantiated, there can be
 			// no changes.
 			return;
 		}
 		try {
 			String xml = getValidatorsAsXML();
 			getPreferences().setValue(PREF_VALIDATOR_XML, xml);
 			savePreferences();
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR, "Error",
 					IStatus.ERROR, "Exception occured", e));
 		} catch (ParserConfigurationException e) {
 			throw new CoreException(new Status(IStatus.ERROR, "Error",
 					IStatus.ERROR, "Exception occured", e));
 		} catch (TransformerException e) {
 			throw new CoreException(new Status(IStatus.ERROR, "Error",
 					IStatus.ERROR, "Exception occured", e));
 		}
 	}
 
 	private static String getValidatorsAsXML() throws IOException,
 			ParserConfigurationException, TransformerException {
 		ValidatorDefinitionsContainer container = new ValidatorDefinitionsContainer();
 
 		IValidatorType[] validatorTypes = getValidatorTypes();
 		for (int i = 0; i < validatorTypes.length; ++i) {
 			IValidator[] Interpreters = validatorTypes[i].getValidators();
 			for (int j = 0; j < Interpreters.length; j++) {
 				IValidator install = Interpreters[j];
 				container.addValidator(install);
 			}
 		}
 		return container.getAsXML();
 	}
 
 	private static boolean addPersistedValidators(
 			ValidatorDefinitionsContainer interpreterDefs) throws IOException {
 
 		String validatorXMLString = getPreferences().getString(
 				PREF_VALIDATOR_XML);
 
 		if (validatorXMLString.length() > 0) {
 			try {
 				ByteArrayInputStream inputStream = new ByteArrayInputStream(
 						validatorXMLString.getBytes());
 				ValidatorDefinitionsContainer.parseXMLIntoContainer(
 						inputStream, interpreterDefs);
 				return false;
 			} catch (IOException ioe) {
 				// DLTKLaunchingPlugin.log(ioe);
 			}
 		}
 		return true;
 	}
 
 	public static boolean isContributedValidator(String id) {
 		getValidatorTypes();
 		return fgContributedValidators.contains(id);
 	}
 
 	public static Preferences getPreferences() {
 		return ValidatorsCore.getDefault().getPluginPreferences();
 	}
 
 	public static void savePreferences() {
 		ValidatorsCore.getDefault().savePluginPreferences();
 	}
 
 	/**
 	 * Perform Interpreter type and Interpreter install initialization. Does not
 	 * hold locks while performing change notification.
 	 * 
 	 * 
 	 */
 	private static void initializeValidators() {
 		ValidatorDefinitionsContainer validatorDefs = null;
 		boolean setPref = false;
 		synchronized (fgValidatorLock) {
 
 			try {
 				fgInitializingValidators = true;
 				// 1. load Validators type extensions
 				// initializeValidatorTypeExtensions();
 				try {
 					validatorDefs = new ValidatorDefinitionsContainer();
 
 					// 2. add persisted Validators
 					setPref = addPersistedValidators(validatorDefs);
 
 					// 4. load contributed Validators installs
 					// addValidatorExtensions(InterpreterDefs);
 
 				} catch (IOException e) {
 					// DLTKLaunchingPlugin.log(e);
 				}
 			} finally {
 				fgInitializingValidators = false;
 			}
 		}
 		if (validatorDefs != null) {
 			// notify of initial Interpreters for backwards compatibility
 			IValidatorType[] validatorTypes = null;
 			try {
 				validatorTypes = ValidatorManager.getAllValidatorTypes();
 			} catch (CoreException e1) {
 				return;
 			}
 			for (int i = 0; i < validatorTypes.length; i++) {
 				IValidatorType type = validatorTypes[i];
 				IValidator[] installs = type.getValidators();
 				if (installs != null) {
 					for (int j = 0; j < installs.length; j++) {
 						fireInterpreterAdded(installs[j]);
 					}
 				}
 			}
 
 			// save settings if required
 			if (setPref) {
 				try {
 					String xml = validatorDefs.getAsXML();
 					getPreferences().setValue(PREF_VALIDATOR_XML, xml);
 				} catch (ParserConfigurationException e) {
 					// DLTKLaunchingPlugin.log(e);
 				} catch (IOException e) {
 					// DLTKLaunchingPlugin.log(e);
 				} catch (TransformerException e) {
 					// DLTKLaunchingPlugin.log(e);
 				}
 
 			}
 
 		}
 	}
 
 	public static void addValidatorChangedListener(
 			IValidatorChangedListener listener) {
 		fgValidatorListeners.add(listener);
 	}
 
 	public static void removeValidatorInstallChangedListener(
 			IValidatorChangedListener listener) {
 		fgValidatorListeners.remove(listener);
 	}
 
 	public static void fireInterpreterChanged(IValidator validator) {
 		Object[] listeners = fgValidatorListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 			listener.validatorChanged(validator);
 		}
 	}
 
 	public static void fireInterpreterAdded(IValidator Interpreter) {
 		if (!fgInitializingValidators) {
 			Object[] listeners = fgValidatorListeners.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 				listener.validatorAdded(Interpreter);
 			}
 		}
 	}
 
 	public static void fireInterpreterRemoved(IValidator Interpreter) {
 		Object[] listeners = fgValidatorListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 			listener.validatorRemoved(Interpreter);
 		}
 	}
 
 	public static IValidatorType[] getPossibleValidatorTypes() {
 		List possible = new ArrayList();
 		IValidatorType[] vals = getValidatorTypes();
 		for (int i = 0; i < vals.length; i++) {
 			if (!vals[i].isBuiltin()) {
 				possible.add(vals[i]);
 			}
 		}
 		return (IValidatorType[]) possible.toArray(new IValidatorType[possible
 				.size()]);
 	}
 
 	public static IValidator[] getActiveValidators() {
 		List possible = new ArrayList();
 		IValidatorType[] vals = getValidatorTypes();
 		for (int i = 0; i < vals.length; i++) {
 			IValidator[] v = vals[i].getValidators();
 			for (int j = 0; j < v.length; j++) {
 				if (v[j].isActive() && v[j].isValidatorValid()) {
 					if (!possible.contains(v[j])) {
 						possible.add(v[j]);
 					}
 				}
 			}
 		}
 		return (IValidator[]) possible.toArray(new IValidator[possible.size()]);
 	}
 
 	public static IValidator[] getValidValidators() {
 		List possible = new ArrayList();
 		IValidatorType[] vals = getValidatorTypes();
 		for (int i = 0; i < vals.length; i++) {
 			IValidator[] v = vals[i].getValidators();
 			for (int j = 0; j < v.length; j++) {
 				if (v[j].isValidatorValid()) {
 					if (!possible.contains(v[j])) {
 						possible.add(v[j]);
 					}
 				}
 			}
 		}
 		return (IValidator[]) possible.toArray(new IValidator[possible.size()]);
 	}
 
 	public static void executeActiveValidatorsWithConsole(OutputStream stream,
 			List elements, List resources) {
 		IValidator[] activeValidators = getActiveValidators();
 		process(stream, elements, resources, activeValidators, processValidate);
 	}
 
 	public static void executeAllValidatorsWithConsole(OutputStream stream,
 			List elements, List resources) {
 		IValidator[] activeValidators = getValidValidators();
 		process(stream, elements, resources, activeValidators, processValidate);
 	}
 
 	private interface IProcessAction {
 		IStatus execute(IValidator validator, ISourceModule o, OutputStream out);
 
 		IStatus execute(IValidator validator, IResource o, OutputStream out);
 	}
 
 	public static IProcessAction processValidate = new IProcessAction() {
 		public IStatus execute(IValidator validator, ISourceModule o,
 				OutputStream out) {
 			return validator.validate(o, out);
 		}
 
 		public IStatus execute(IValidator validator, IResource o,
 				OutputStream out) {
 			return validator.validate(o, out);
 		}
 	};
 	public static IProcessAction processClean = new IProcessAction() {
 		public IStatus execute(IValidator validator, ISourceModule o,
 				OutputStream out) {
 			validator.clean(o);
 			return null;
 		}
 
 		public IStatus execute(IValidator validator, IResource o,
 				OutputStream out) {
 			validator.clean(o);
 			return null;
 		}
 	};
 
 	private static void process(OutputStream stream, List elements,
 			List resources, IValidator[] activeValidators, IProcessAction action) {
 		if (elements != null) {
 			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
 				IModelElement el = (IModelElement) iterator.next();
 				if (el instanceof ISourceModule) {
 					ISourceModule module = (ISourceModule) el;
 					IDLTKLanguageToolkit toolkit = null;
 					try {
 						toolkit = DLTKLanguageManager
 								.getLanguageToolkit(module);
 					} catch (CoreException e) {
 						try {
 							if (stream != null) {
 								stream
 										.write(("Error to check element:" + module
 												.getPath()).getBytes());
 							}
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 						e.printStackTrace();
 						continue;
 					}
 					for (int i = 0; i < activeValidators.length; i++) {
 						IValidator v = activeValidators[i];
 						String nature = v.getValidatorType().getNature();
 						if (toolkit.getNatureID().equals(nature)
 								|| nature.equals("#")) {
 
 							// IStatus e = v.validate(module, stream);
 							IStatus e = action.execute(v, module, stream);
 							if (e != null) {
 								if (e.getSeverity() == IStatus.ERROR) {
 									if (stream != null) {
 										String message = e.getMessage();
 										try {
 											stream.write(message.getBytes());
 										} catch (IOException e1) {
 											e1.printStackTrace();
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 
 			}
 		}
 		if (resources != null) {
 			for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
 				IResource el = (IResource) iterator.next();
 				for (int i = 0; i < activeValidators.length; i++) {
 					IValidator v = activeValidators[i];
 					// v.validate(el, stream);
 					action.execute(v, el, stream);
 				}
 			}
 		}
 	}
 
 	public static void executeCleanAllValidatorsWithConsole(List elements,
 			List resources) {
		IValidator[] activeValidators = getValidValidators();
 		process(null, elements, resources, activeValidators, processClean);
 	}
 }

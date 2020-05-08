 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.core;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.validators.internal.core.ListenerList;
 import org.eclipse.dltk.validators.internal.core.ValidatorDefinitionsContainer;
 import org.eclipse.dltk.validators.internal.core.ValidatorManager;
 import org.eclipse.dltk.validators.internal.core.ValidatorsCore;
 import org.xml.sax.InputSource;
 
 public final class ValidatorRuntime {
 
 	public static final String PREF_VALIDATOR_XML = ValidatorsCore.PLUGIN_ID
 			+ ".PREF_VALIDATOR_XML"; //$NON-NLS-1$
 
 	public static final String MARKER_VALIDATOR = ValidatorsCore.PLUGIN_ID
 			+ ".marker_validator_id"; //$NON-NLS-1$
 
 	// lock for interpreter initialization
 	private static final Object fgValidatorLock = new Object();
 	private static boolean fgInitializingValidators = false;
 	private static boolean isInitialized = false;
 	//
 	private static final ListenerList fgValidatorListeners = new ListenerList(5);
 
 	public static final String ANY_NATURE = "#"; //$NON-NLS-1$
 
 	private ValidatorRuntime() {
 	}
 
 	public static IValidatorType getValidatorType(String id) {
 		IValidatorType[] types = getValidatorTypes();
 		for (int i = 0; i < types.length; i++) {
 			if (types[i].getID().equals(id)) {
 				return types[i];
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
 		initializeValidators();
 		try {
 			return ValidatorManager.getValidators(nature);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static void saveValidatorConfiguration() throws CoreException {
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
 			throw new CoreException(new Status(IStatus.ERROR,
 					ValidatorsCore.PLUGIN_ID, IStatus.OK,
 					Messages.ValidatorRuntime_exceptionOccurred, e));
 		} catch (ParserConfigurationException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					ValidatorsCore.PLUGIN_ID, IStatus.OK,
 					Messages.ValidatorRuntime_exceptionOccurred, e));
 		} catch (TransformerException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					ValidatorsCore.PLUGIN_ID, IStatus.OK,
 					Messages.ValidatorRuntime_exceptionOccurred, e));
 		}
 	}
 
 	private static String getValidatorsAsXML() throws IOException,
 			ParserConfigurationException, TransformerException {
 		ValidatorDefinitionsContainer container = new ValidatorDefinitionsContainer();
 
 		IValidatorType[] validatorTypes = getValidatorTypes();
 		for (int i = 0; i < validatorTypes.length; ++i) {
 			container.addValidators(validatorTypes[i].getValidators());
 		}
 		return container.getAsXML();
 	}
 
 	private static boolean addPersistedValidators(
 			ValidatorDefinitionsContainer interpreterDefs) throws IOException {
 
 		String validatorXMLString = getPreferences().getString(
 				PREF_VALIDATOR_XML);
 
 		if (validatorXMLString.length() > 0) {
 			try {
 				interpreterDefs.parseXML(new InputSource(new StringReader(
 						validatorXMLString)));
 				return false;
 			} catch (IOException ioe) {
 				// DLTKLaunchingPlugin.log(ioe);
 			}
 		}
 		return true;
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
 			if (isInitialized) {
 				// return;
 			}
 			isInitialized = true;
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
 				final IValidatorType type = validatorTypes[i];
 				final IValidator[] validators = type.getValidators();
 				if (validators != null) {
 					for (int j = 0; j < validators.length; j++) {
 						final IValidator validator = validators[j];
 						if (type.findValidator(validator.getID()) == null) {
 							type.addValidator(validator);
 						}
 						fireValidatorAdded(validator);
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
 
 	public static void removeValidatorChangedListener(
 			IValidatorChangedListener listener) {
 		fgValidatorListeners.remove(listener);
 	}
 
 	public static void fireValidatorChanged(IValidator validator) {
 		Object[] listeners = fgValidatorListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 			listener.validatorChanged(validator);
 		}
 	}
 
 	public static void fireValidatorAdded(IValidator Interpreter) {
 		if (!fgInitializingValidators) {
 			Object[] listeners = fgValidatorListeners.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 				listener.validatorAdded(Interpreter);
 			}
 		}
 	}
 
 	public static void fireValidatorRemoved(IValidator Interpreter) {
 		Object[] listeners = fgValidatorListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IValidatorChangedListener listener = (IValidatorChangedListener) listeners[i];
 			listener.validatorRemoved(Interpreter);
 		}
 	}
 
 	/**
 	 * Returns array of validator types which are not built-in, i.e. new
 	 * instances of that types could be added by the user.
 	 * 
 	 * @return
 	 */
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
 
 	public static IValidator[] getAllValidators() {
 		List possible = new ArrayList();
 		IValidatorType[] vals = getValidatorTypes();
 		for (int i = 0; i < vals.length; i++) {
 			IValidator[] v = vals[i].getValidators();
 			for (int j = 0; j < v.length; j++) {
 				if (!possible.contains(v[j])) {
 					possible.add(v[j]);
 				}
 			}
 		}
 		return (IValidator[]) possible.toArray(new IValidator[possible.size()]);
 	}
 
 	private static ISourceModule[] filterModulesForValidator(List elements,
 			IValidator v, IProgressMonitor monitor) {
 		final List result = new ArrayList();
 		final String nature = v.getValidatorType().getNature();
 		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
 			IModelElement el = (IModelElement) iterator.next();
 			if (el instanceof ISourceModule) {
 				ISourceModule module = (ISourceModule) el;
 				IDLTKLanguageToolkit toolkit = null;
 				toolkit = DLTKLanguageManager.getLanguageToolkit(module);
 
 				if (toolkit != null && toolkit.getNatureId().equals(nature)
 						|| nature.equals(ANY_NATURE)) {
 					result.add(module);
 				}
 			}
 		}
 		return (ISourceModule[]) result
 				.toArray(new ISourceModule[result.size()]);
 	}
 
 	public static void executeCleanAllValidatorsWithConsole(List elements,
 			List resources) {
 		executeCleanAllValidatorsWithConsole(elements, resources);
 	}
 
 	public static final IValidatorPredicate AUTOMATIC = new IValidatorPredicate() {
 
 		public boolean evaluate(IValidator validator) {
 			return validator.isAutomatic();
 		}
 
 	};
 
 	public static final IValidatorPredicate ALL = new IValidatorPredicate() {
 
 		public boolean evaluate(IValidator validator) {
 			return true;
 		}
 
 	};
 
 	public static IValidator[] getProjectValidators(IScriptProject project,
 			Class validatorType, IValidatorPredicate predicate) {
 		String[] natureIds;
 		try {
 			natureIds = project.getProject().getDescription().getNatureIds();
 		} catch (CoreException e) {
 			ValidatorsCore.log(e.getStatus());
 			natureIds = new String[0];
 		}
 		final List result = new ArrayList();
 		final IValidatorType[] types = ValidatorRuntime.getValidatorTypes();
 		for (int i = 0; i < types.length; ++i) {
 			final IValidatorType type = types[i];
 			if (checkValidatorTypeNature(type, natureIds)
 					&& type.supports(validatorType)) {
 				final IValidator[] validators = type.getValidators();
 				for (int j = 0; j < validators.length; ++j) {
					final IValidator validator = validators[j];
 					if (predicate.evaluate(validator)
 							&& validator.isValidatorValid(project)) {
 						result.add(validator);
 					}
 				}
 			}
 		}
 		return (IValidator[]) result.toArray(new IValidator[result.size()]);
 	}
 
 	public static IBuildParticipant[] getBuildParticipants(
 			IScriptProject project, String natureId,
 			IValidatorPredicate predicate) {
 		final List result = new ArrayList();
 		final IValidatorType[] types = ValidatorRuntime.getValidatorTypes();
 		for (int i = 0; i < types.length; ++i) {
 			final IValidatorType type = types[i];
 			if (checkValidatorTypeNature(type, natureId)
 					&& type.supports(IBuildParticipant.class)) {
 				final IValidator[] validators = type.getValidators();
 				for (int j = 0; j < validators.length; ++j) {
 					final IValidator validator = validators[i];
 					if (predicate.evaluate(validator)
 							&& validator.isValidatorValid(project)) {
 						final IBuildParticipant participant = (IBuildParticipant) validator
 								.getValidator(project, IBuildParticipant.class);
 						if (participant != null) {
 							result.add(participant);
 						}
 					}
 				}
 			}
 		}
 		return (IBuildParticipant[]) result
 				.toArray(new IBuildParticipant[result.size()]);
 	}
 
 	/**
 	 * @param type
 	 * @param natureIds
 	 * @return
 	 */
 	private static boolean checkValidatorTypeNature(IValidatorType type,
 			String[] natureIds) {
 		final String natureId = type.getNature();
 		if (ANY_NATURE.equals(natureId)) {
 			return true;
 		}
 		for (int i = 0; i < natureIds.length; ++i) {
 			if (natureId.equals(natureIds[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param type
 	 * @param natureIds
 	 * @return
 	 */
 	private static boolean checkValidatorTypeNature(IValidatorType type,
 			String natureIds) {
 		final String typeNature = type.getNature();
 		return ANY_NATURE.equals(typeNature) || natureIds.equals(typeNature);
 	}
 
 	public static IStatus executeAutomaticSourceModuleValidators(
 			IScriptProject project, List sourceModules,
 			IValidatorOutput output, IProgressMonitor monitor) {
 		return executeSourceModuleValidators(project, sourceModules, output,
 				AUTOMATIC, monitor);
 	}
 
 	public static IStatus executeSourceModuleValidators(IScriptProject project,
 			List sourceModules, IValidatorOutput output,
 			IValidatorPredicate predicate, IProgressMonitor monitor) {
 		final IValidator[] validators = getProjectValidators(project,
 				ISourceModuleValidator.class, predicate);
 		if (validators.length != 0) {
 			monitor.beginTask(Messages.ValidatorRuntime_runningValidators,
 					validators.length * 100);
 			for (int i = 0; i < validators.length; ++i) {
 				if (monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				final IValidator validator = validators[i];
 				final ISourceModuleValidator mValidator = (ISourceModuleValidator) validator
 						.getValidator(project, ISourceModuleValidator.class);
 				if (mValidator != null) {
 					final ISourceModule[] mArray = filterModulesForValidator(
 							sourceModules, validator, monitor);
 					if (monitor.isCanceled()) {
 						return Status.CANCEL_STATUS;
 					}
 					final IProgressMonitor submonitor = new SubProgressMonitor(
 							monitor, 100);
 					if (mArray.length != 0) {
 						mValidator.validate(mArray, output, submonitor);
 					}
 					submonitor.done();
 				}
 			}
 			monitor.done();
 		}
 		return Status.OK_STATUS;
 	}
 
 	public static IStatus executeAutomaticResourceValidators(
 			IScriptProject project, List resources, IValidatorOutput output,
 			IProgressMonitor monitor) {
 		return executeResourceValidators(project, resources, output, AUTOMATIC,
 				monitor);
 	}
 
 	public static IStatus executeResourceValidators(IScriptProject project,
 			List resources, IValidatorOutput output,
 			IValidatorPredicate predicate, IProgressMonitor monitor) {
 		final IValidator[] validators = getProjectValidators(project,
 				IResourceValidator.class, predicate);
 		if (validators.length != 0) {
 			final IResource[] resArray = (IResource[]) resources
 					.toArray(new IResource[resources.size()]);
 			monitor.beginTask(Messages.ValidatorRuntime_runningValidators,
 					validators.length * 100);
 			for (int i = 0; i < validators.length; ++i) {
 				if (monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				final IValidator validator = validators[i];
 				final IResourceValidator resourceValidator = (IResourceValidator) validator
 						.getValidator(project, IResourceValidator.class);
 				if (resourceValidator != null) {
 					final IProgressMonitor submonitor = new SubProgressMonitor(
 							monitor, 100);
 					resourceValidator.validate(resArray, output, submonitor);
 					submonitor.done();
 				}
 			}
 			monitor.done();
 		}
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * @param project
 	 * @param modules
 	 * @param resources
 	 * @param monitor
 	 */
 	public static void cleanAll(IScriptProject project,
 			ISourceModule[] modules, IResource[] resources,
 			IProgressMonitor monitor) {
 		final IValidatorType[] types = ValidatorRuntime.getValidatorTypes();
 		for (int i = 0; i < types.length; ++i) {
 			final IValidatorType type = types[i];
 			final IValidator[] validators = type.getValidators();
 			for (int j = 0; j < validators.length; ++j) {
 				final IValidator validator = validators[i];
 				final ISourceModuleValidator smValidator = (ISourceModuleValidator) validator
 						.getValidator(project, ISourceModuleValidator.class);
 				if (smValidator != null) {
 					smValidator.clean(modules);
 				}
 				final IResourceValidator rValidator = (IResourceValidator) validator
 						.getValidator(project, IResourceValidator.class);
 				if (rValidator != null) {
 					rValidator.clean(resources);
 				}
 			}
 		}
 	}
 
 }

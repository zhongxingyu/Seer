 /*******************************************************************************
  * Copyright (c) 2007, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.validation.internal;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
 import org.eclipse.wst.validation.Friend;
 import org.eclipse.wst.validation.IPerformanceMonitor;
 import org.eclipse.wst.validation.IValidatorGroupListener;
 import org.eclipse.wst.validation.PerformanceCounters;
 import org.eclipse.wst.validation.ValidationFramework;
 import org.eclipse.wst.validation.ValidationResult;
 import org.eclipse.wst.validation.ValidationState;
 import org.eclipse.wst.validation.Validator;
 import org.eclipse.wst.validation.internal.model.GlobalPreferences;
 import org.eclipse.wst.validation.internal.model.IValidatorVisitor;
 import org.eclipse.wst.validation.internal.model.ProjectPreferences;
 import org.eclipse.wst.validation.internal.operations.ManualValidatorsOperation;
 import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * A central place to keep track of all the validators.
  * @author karasiuk
  *
  */
 public class ValManager implements IValChangedListener, IFacetedProjectListener, IProjectChangeListener {
 	
 	private static ValManager _me;
 		
 	/**
 	 * Projects may be allowed to override the global validation settings. If that is the case then those
 	 * project specific settings are saved here. If the key exists, but the value is null, then that
 	 * means that the project has been checked and it does not have any specific settings.
 	 */
 	private Map<IProject, ProjectPreferences> _projectPreferences = 
 		Collections.synchronizedMap(new HashMap<IProject, ProjectPreferences>(50));
 	
 	private GlobalPreferences _globalPreferences;
 		
 	/**
 	 * This number increases each time any of the validation configurations change. It is used to determine
 	 * if information that we have cached in the ValProperty is stale or not. This starts off at zero, each time
 	 * the workbench is started.
 	 */
 	private int _configNumber;
 	private ValidatorIdManager _idManager = new ValidatorIdManager();
 	
 	private ValidatorProjectManager _projectManager = new ValidatorProjectManager();
 	
 	private static final QualifiedName StatusBuild = new QualifiedName(ValidationPlugin.PLUGIN_ID, "sb"); //$NON-NLS-1$
 	private static final QualifiedName StatusManual = new QualifiedName(ValidationPlugin.PLUGIN_ID, "sm"); //$NON-NLS-1$
 			
 	public static synchronized ValManager getDefault(){
 		if (_me == null)_me = new ValManager();
 		return _me;
 	}
 	
 	private ValManager(){
 		ValPrefManagerGlobal.getDefault().addListener(this);
 		ValPrefManagerProject.addListener(this);
 		FacetedProjectFramework.addListener(this, IFacetedProjectEvent.Type.PROJECT_MODIFIED);
 		EventManager.getManager().addProjectChangeListener(this);
 	}
 	
 	/**
 	 * This needs to be called if the ValManager is ever deleted.
 	 */
 	public void dispose(){
 		// currently nobody calls this method, because this instance is never removed, but the method is
 		// here for completeness none the less.
 		ValPrefManagerGlobal.getDefault().removeListener(this);
 		ValPrefManagerProject.removeListener(this);
 		FacetedProjectFramework.removeListener(this);
 		EventManager.getManager().removeProjectChangeListener(this);	
 	}
 	
 	/**
 	 * Answer all the registered validators. If you are planning on making changes to the validators,
 	 * and then saving them in a preference store then you probably want the getValidatorsCopy method.
 	 * Because if you make changes to the original validators, and since we only save differences,
 	 * there won't be any differences. 
 	 * 
 	 * @return Answer an empty array if there are no validators.
 	 * 
 	 * @see #getValidatorsCopy()
 	 */
 	public Validator[] getValidators(){
 		return getValidators(null);
 	}
 	
 	/**
 	 * Answer copies of all the registered validators. If you are going to be making changes to the validators
 	 * and then saving them backing into the preference store, then this is the method to use.
 	 * 
 	 * @return Answer an empty array if there are no validators.
 	 */
 	public Validator[] getValidatorsCopy(){
 		Validator[] orig = getValidators();
 		Validator[] copy = new Validator[orig.length];
 		for (int i=0; i<orig.length; i++)copy[i] = orig[i].copy();
 		return copy;
 	}
 	
 	/**
 	 * Answer all the validators for the given project.
 	 * <p>
 	 * Individual projects may override the global validation preference
 	 * settings. If the project has it's own settings, then those validators are
 	 * returned via this method.
 	 * </p>
 	 * <p>
 	 * The following approach is used. For version 1 validators, the validator
 	 * is only returned if it is defined to operate on this project type. This
 	 * is the way that the previous version of the framework did it. For version
 	 * 2 validators, they are all returned.
 	 * </p>
 	 * 
 	 * @param project
 	 *            this may be null, in which case the global preferences are
 	 *            returned.
 	 * @param respectOverrideSettings
 	 *            if this is true then the validators that get returned are
 	 *            based on the override settings. So for example, if the global
 	 *            preferences do not allow project overrides then none of the
 	 *            project settings are used. Normal validation would set this to true.
 	 *            The properties page would set this to false.
 	 *            
 	 * @deprecated Use {@link #getValidators(IProject)} instead            
 	 */
 	public synchronized Validator[] getValidators(IProject project, boolean respectOverrideSettings) throws ProjectUnavailableError {
 		return getValidators(project);
 	}
 	
 	/**
 	 * Answer all the validators for the given project.
 	 * <p>
 	 * Individual projects may override the global validation preference
 	 * settings. If the project has it's own settings, then those validators are
 	 * returned via this method.
 	 * </p>
 	 * <p>
 	 * The following approach is used. For version 1 validators, the validator
 	 * is only returned if it is defined to operate on this project type. This
 	 * is the way that the previous version of the framework did it. For version
 	 * 2 validators, they are all returned.
 	 * </p>
 	 * 
 	 * @param project
 	 *            This may be null, in which case the global preferences are returned.
 	 */
 	public synchronized Validator[] getValidators(IProject project) throws ProjectUnavailableError {
 		Map<String,Validator> v2Vals = getV2Validators(project);
 		TreeSet<Validator> sorted = new TreeSet<Validator>();
 		for (Validator v : v2Vals.values())sorted.add(v);
 		
 		try {
 			ValidationConfiguration vc = ConfigurationManager.getManager().getConfiguration(project);
 			if (project == null){
 				// If the project is null we need to use this approach, since you can not use new ManualValidatorsOperation(null)
 				ValidatorMetaData[] vmds = vc.getValidators();
 				for (ValidatorMetaData vmd : vmds){
 					Validator v = Validator.create(vmd, vc, project);
 					sorted.add(v);
 				}
 			}
 			else {
 				ManualValidatorsOperation mvo = new ManualValidatorsOperation(project);
 				Set<ValidatorMetaData> vmds = mvo.getEnabledValidators();
 				for (ValidatorMetaData vmd : vmds){
 					Validator v = Validator.create(vmd, vc, project);
 					sorted.add(v);
 				}
 			}
 		}
 		catch (InvocationTargetException e){
 			ValidationPlugin.getPlugin().handleException(e);
 		}
 		
 		Validator[] vals = new Validator[sorted.size()];
 		sorted.toArray(vals);
 		return vals;
 	}
 	
 	/**
 	 * Answer the V2 validators that are in effect for this project. The following approach is used:
 	 * <ol>
 	 * <li>The validators that are defined by the extension point are loaded.</li>
 	 * <li>They are customized by any global preferences.</li>
 	 * <li>If project customizations are allowed, they are customized by the project preferences.
 	 * </ol>
 	 * 
 	 * @param project
 	 *            This may be null, in which case only the global preferences are used.
 	 * @return
 	 */
 	private Map<String,Validator> getV2Validators(IProject project){
 		Map<String,Validator> extVals = ExtensionValidators.instance().getMapV2Copy();
 		try {
 			List<Validator> vals = ValPrefManagerGlobal.getDefault().getValidators();
 			for (Validator v : vals)extVals.put(v.getId(), v);
 			
 			if (!mustUseGlobalValidators(project)){
 				//TODO should probably cache this vpm
 				ValPrefManagerProject vpm = new ValPrefManagerProject(project);
 				vals = vpm.getValidators(extVals);
 				for (Validator v : vals)extVals.put(v.getId(), v);
 			}		
 		}
 		catch (BackingStoreException e){
 			ValidationPlugin.getPlugin().handleException(e);
 		}
 		return extVals;
 	}
 	
 
 	/**
 	 * Answer true if we must use the global settings for this project. If the global preferences do not
 	 * allow overrides, or if this project does not allow overrides then the global preferences must be used.
 	 *  
 	 * @param project project that is being tested. It can be null, in which case the global preferences must be used.
 	 * @return true if the global preferences must be used.
 	 */
 	public boolean mustUseGlobalValidators(IProject project){
 		if (project == null)return true;
 		if (!getGlobalPreferences().getOverride())return true;
 		ProjectPreferences pp = getProjectPreferences2(project);
 		if (pp == null){
 			ValPrefManagerProject vpm = new ValPrefManagerProject(project);
 			pp = new ProjectPreferences(project); 
 			vpm.loadProjectPreferencesShallow(pp);
 		}
 		
 		return !pp.getOverride();
 	}
 	
 	/**
 	 * Answer the validator with the given id that is in effect for the given project.
 	 * 
 	 * @param id The validator id.
 	 * @param project
 	 * @return null if the validator is not found
 	 */
 	public Validator getValidator(String id, IProject project){
 		Validator[] vals = getValidators(project);
 		for (Validator v : vals){
 			if (v.getId().equals(id))return v;
 		}
 		return null;
 	}
 	
 	/**
 	 * @see ValidationFramework#getValidator(String, IProject)
 	 */
 	public Validator getValidatorWithId(String id, IProject project){
 		Validator[] vals = getValidators(project);
 		for (Validator v : vals){
 			if (v.getId().equals(id))return v;
 		}
 		return null;
 	}
 					
 	/**
 	 * Answer true if the resource has any enabled validators.
 	 * 
 	 * @param resource a file, folder or project.
 	 * 
 	 * @param isManual if true then the validator must be turned on for manual validation. 
 	 * If false then the isManualValidation setting isn't used to filter out validators.
 	 *   
 	 * @param isBuild if true then the validator must be turned on for build based validation.
 	 * If false then the isBuildValidation setting isn't used to filter out validators.  
 	 */
 	public boolean hasValidators(IResource resource, boolean isManual, boolean isBuild){
 		if (resource instanceof IProject){
 			IProject project = (IProject)resource;
 			return ValManager.getDefault().getValidators(project).length > 0;
 		}
 		else if (resource instanceof IFolder){
 			IFolder folder = (IFolder)resource;
 			HasValidatorVisitor v = new HasValidatorVisitor(isManual, isBuild);
 			return v.hasValidator(folder);
 		}
 		else {
 			ContentTypeWrapper ctw = new ContentTypeWrapper();
 			for (Validator val : ValManager.getDefault().getValidators(resource.getProject())){
 				if (Friend.shouldValidate(val, resource, isManual, isBuild, ctw))return true;
 			}			
 		}
 		return false;
 	}
 	
 	/**
 	 * Answer true if the project has disabled all of it's validators, or if project overrides are not
 	 * allowed if global validation has been disabled.
 	 * 
 	 * @param project the project that is being consulted, or null if only the global settings are to be 
 	 * checked.
 	 */
 	public boolean isDisabled(IProject project){
 		GlobalPreferences gp = getGlobalPreferences();
 		if (!gp.getOverride() || project == null)return gp.getDisableAllValidation();
 		
 		ProjectPreferences pp = getProjectPreferences2(project);
 		if (pp == null)return gp.getDisableAllValidation();
 		return pp.getSuspend();		
 	}
 			
 	/**
 	 * Answer all the registered validators as they were defined by the extension points. That is
 	 * answer the validators as if the user has never applied any customizations.
 	 * 
 	 * @return Answer an empty array if there are no validators.
 	 */
 	public static Validator[] getDefaultValidators() throws InvocationTargetException {
 		Map<String,Validator> extVals = ExtensionValidators.instance().getMapV2();
 		TreeSet<Validator> sorted = new TreeSet<Validator>();
 		for (Validator v : extVals.values())sorted.add(v);
 		
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		GlobalConfiguration gc = new GlobalConfiguration(root);
 		gc.resetToDefault();
 		for (ValidatorMetaData vmd : gc.getValidators()){
 			Validator v = Validator.create(vmd, gc, null);
 			v.setBuildValidation(vmd.isBuildValidation());
 			v.setManualValidation(vmd.isManualValidation());
 			sorted.add(v);
 		}
 		
 		Validator[] val = new Validator[sorted.size()];
 		sorted.toArray(val);
 		return val;
 	}
 	
 	public static Validator[] getDefaultValidators(IProject project) throws InvocationTargetException {
 		Map<String,Validator> extVals = ExtensionValidators.instance().getMap(project);
 		Validator[] val = new Validator[extVals.size()];
 		extVals.values().toArray(val);
 		return val;
 	}
 
 	/**
 	 * Answer all the registered validators.
 	 * 
 	 * @param project the project to use for getting the version 1 validator settings. This can
 	 * be null in which case the global preferences are used.
 	 * 
 	 * @return Answer an empty array if there are no validators.
 	 */
 //	Validator[] getValidators2(IProject project) throws ProjectUnavailableError {
 //		// If I use a local variable I don't need to synchronize the method.
 //		
 //		Validator[] validators = _validators;
 //		if (project == null && validators != null)return validators;
 //				
 //		Validator[] val = loadExtensions(false, project);
 //		ValPrefManagerGlobal vpm = ValPrefManagerGlobal.getDefault();
 //		if (!vpm.loadPreferences(val)){
 //			val = restoreDefaults2(project);
 //			saveStateTimestamp();				
 //		}
 //		else {
 //			if (getGlobalPreferences().getStateTimeStamp() != Platform.getStateStamp())
 //				val = migrateSettings(val, project);
 //		}
 //		
 //		TreeSet<Validator> set = new TreeSet<Validator>();
 //		for (Validator v : val)set.add(v);
 //		
 //		List<Validator> list = new LinkedList<Validator>();
 //		try {
 //			ValidationConfiguration vc = ConfigurationManager.getManager().getConfiguration(project);
 //			for (ValidatorMetaData vmd : vc.getValidators()){
 //				list.add(Validator.create(vmd, vc, project));
 //			}							
 //			
 //		}
 //		catch (InvocationTargetException e){
 //			if (project != null && (!project.exists() || !project.isOpen()))
 //				throw new ProjectUnavailableError(project);
 //			ValidationPlugin.getPlugin().handleException(e);
 //		}
 //		
 //		set.addAll(list);
 //		val = new Validator[set.size()];
 //		set.toArray(val);
 //		if (project == null)_validators = val;
 //		return val;
 //	}
 	
 	/**
 	 * This method needs to be called whenever the validation configuration has changed.
 	 */
 	private void configHasChanged(){
 		_configNumber++;
 		_projectManager.reset();
 	}
 		
 	/**
 	 * Answer the global validation preferences.
 	 */
 	public synchronized GlobalPreferences getGlobalPreferences(){
 		GlobalPreferences gp = _globalPreferences;
 		if (gp == null){
 			ValPrefManagerGlobal vpm = ValPrefManagerGlobal.getDefault();
 			gp = new GlobalPreferences();
 			vpm.loadGlobalPreferences(gp);
 			_globalPreferences = gp;
 		}
 		return gp;		
 	}
 	
 	public ProjectPreferences getProjectPreferences(IProject project) {
 		ProjectPreferences pp = getProjectPreferences2(project);
 		if (pp != null)return pp;
 		
 		/* hopefully we rarely get this far */
 		
 		Map<String,Validator> extVals = ExtensionValidators.instance().getMapV2Copy();
 		try {
 			List<Validator> vals = ValPrefManagerGlobal.getDefault().getValidators();
 			for (Validator v : vals)extVals.put(v.getId(), v);
 			
 			pp = getProjectPreferences(project, extVals);
 		}
 		catch (BackingStoreException e){
 			ValidationPlugin.getPlugin().handleException(e);
 		}	
 		return pp;
 	}
 
 	
 	private ProjectPreferences getProjectPreferences(IProject project, Map<String, Validator> baseValidators) 
 		throws BackingStoreException {
 		if (_projectPreferences.containsKey(project)){
 			return _projectPreferences.get(project);
 		}
 		
 		ValPrefManagerProject vpm = new ValPrefManagerProject(project);
 		ProjectPreferences pp = new ProjectPreferences(project); 
 		vpm.loadProjectPreferences(pp, baseValidators);
 		_projectPreferences.put(project, pp);
 		return pp;		
 	}
 	
 	/**
 	 * Answer the project specific validation preferences from the cache
 	 * 
 	 * @param project
 	 * 
 	 * @return null if the project is not in the cache.
 	 */
 	private ProjectPreferences getProjectPreferences2(IProject project){
 		if (_projectPreferences.containsKey(project)){
 			return _projectPreferences.get(project);
 		}
 		return null;
 	}
 	
 	/**
 	 * Restore all the validation defaults, as defined by the individual validators via the
 	 * validation extension point.
 	 */
 //	public synchronized void restoreDefaults() {
 //		getGlobalPreferences().resetToDefault();
 //		_validators = null;
 //		getValidators(true);
 //	}
 	
 
 	/**
 	 * Run all the validators that are applicable to this resource.
 	 * <p>
 	 * If this is a manual validation both the version 1 and version 2 validators are run. If it
 	 * is a build validation, then only the version 2 validators are run, because the old framework handles
 	 * the running of the old validators.
 	 * </p>
 	 * 
 	 * @param project project that is being validated
 	 * 
 	 * @param resource the resource that is being validated
 	 * 
 	 * @param kind the kind of resource delta. It will be one of the IResourceDelta constants, like
 	 * IResourceDelta.CHANGED for example.
 	 * 
 	 * @param valType The type of validation request.
 	 * @param buildKind the kind of build that triggered this validation. See IncrementalProjectBuilder for values.
 	 * @param operation the operation that this validation is running under
 	 * @param monitor the monitor to use to report progress 
 	 */
 	public void validate(IProject project, final IResource resource, final int kind, ValType valType, 
 		int buildKind, ValOperation operation, final IProgressMonitor monitor) {
 		
 		MarkerManager.getDefault().deleteMarkers(resource, operation.getStarted(), IResource.DEPTH_ZERO);
 		
 		IValidatorVisitor visitor = new IValidatorVisitor(){
 
 			public void visit(Validator validator, IProject project, ValType vt,
 				ValOperation operation, IProgressMonitor monitor) {
 								
 				Validator.V1 v1 = validator.asV1Validator();
 				if (vt == ValType.Build && v1 != null)return;
 				
 				SubMonitor subMonitor = SubMonitor.convert(monitor);
 				String task = NLS.bind(ValMessages.LogValStart, validator.getName(), resource.getName());
 				subMonitor.beginTask(task, 1);
 				validate(validator, operation, resource, kind, subMonitor.newChild(1));
 			}			
 		};
 		SubMonitor sm = SubMonitor.convert(monitor, getValidators(project).length);
 		accept(visitor, project, resource, valType, operation, sm);
 		
 	}
 	
 	/**
 	 * Validate a single resource with a single validator. This will call the validator whether the validator
 	 * is enabled or not.
 	 * <p>
 	 * Callers of this method should ensure that the shouldValidate was tested before making this call.
 	 * 
 	 * @param validator the validator
 	 * @param operation the operation that the validation is running in.
 	 * @param resource the resource to validate
 	 * @param kind the kind of resource change. See IResourceDelta.
 	 * @param monitor
 	 */
 	public void validate(Validator validator, ValOperation operation, IResource resource, int kind, 
 			IProgressMonitor monitor){
 		if (operation.isValidated(validator.getId(), resource))return;
 		long time = 0;
 		long cpuTime = -1;
 		String msg1 = NLS.bind(ValMessages.LogValStart, validator.getName(), resource.getName());
 		monitor.subTask(msg1);
 		IPerformanceMonitor pm = ValidationFramework.getDefault().getPerformanceMonitor();
 		if (pm.isCollecting()){
 			time = System.currentTimeMillis();
 			cpuTime = Misc.getCPUTime();
 		}
 		
 		if (Tracing.matchesExtraDetail(validator.getId())){
 			Tracing.log("ValManager-03: validating ", resource); //$NON-NLS-1$
 		}
 		ValidationResult vr = validator.validate(resource, kind, operation, monitor);
 		if (pm.isCollecting()){
 			if (cpuTime != -1){
 				cpuTime = Misc.getCPUTime() - cpuTime;
 			}
 			int num = 0;
 			if (vr != null)num = vr.getNumberOfValidatedResources();
 			PerformanceCounters pc = new PerformanceCounters(validator.getId(), 
 				validator.getName(), resource.getName(),
 				num, System.currentTimeMillis()-time, cpuTime);
 			pm.add(pc);
 		}
 		if (ValidationPlugin.getPlugin().isDebugging() && !pm.isCollecting()){
 			String msg = time != 0 ? 
 				NLS.bind(ValMessages.LogValEndTime,	new Object[]{validator.getName(), 
 					validator.getId(), resource, Misc.getTimeMS(System.currentTimeMillis()-time)}) :
 				NLS.bind(ValMessages.LogValEnd, validator.getName(), resource);
 			Tracing.log("ValManager-01: " + msg); //$NON-NLS-1$
 		}
 		if (vr != null){
 			operation.getResult().mergeResults(vr);
 			if (vr.getSuspendValidation() != null)operation.suspendValidation(vr.getSuspendValidation(), validator);
 		}
 	}
 	
 	/**
 	 * Accept a visitor for all the validators that are enabled for the given project.
 	 * 
 	 * @param visitor
 	 * @param project
 	 * @param valType the type of validation
 	 * @param operation
 	 * @param monitor
 	 */
 	public void accept(IValidatorVisitor visitor, IProject project, ValType valType, 
 		ValOperation operation, IProgressMonitor monitor){
 		
 		if (isDisabled(project))return;
 		
 		for (Validator val : getValidators(project)){
 			if (monitor.isCanceled())return;
 			if (!_projectManager.shouldValidate(val, project, valType))continue;
 			if (operation.isSuspended(val, project))continue;
 			try {
 				visitor.visit(val, project, valType, operation, monitor);
 			}
 			catch (Exception e){
 				ValidationPlugin.getPlugin().handleException(e);
 			}
 		}		
 	}
 	
 	/**
 	 * Accept a visitor for all the validators that are enabled for the given project, resource, 
 	 * and validation mode.
 	 * 
 	 * @param valType the type of validation request
 	 */
 	public void accept(IValidatorVisitor visitor, IProject project, IResource resource, 
 			ValType valType, ValOperation operation, IProgressMonitor monitor){
 		
 		if (isDisabled(project))return;
 		
 		Map<String,IValidatorGroupListener[]> groupListeners = new HashMap<String,IValidatorGroupListener[]>();
 		
 		ValProperty vp = getValProperty(resource, valType, _configNumber);
 		if (vp != null){
 			BitSet bs = vp.getConfigSet();
 			for (Validator val : getValidators(project)){
 				if (!monitor.isCanceled()) {
 					if (!bs.get(_idManager.getIndex(val.getId())))continue;
					if (operation.isSuspended(val, project))continue;
 					Validator.V2 v2 = val.asV2Validator();
 					if (v2 != null) {
 						notifyGroupListenersStarting(resource, operation.getState(), monitor, groupListeners, v2);
 					}
 					try {
 						visitor.visit(val, project, valType, operation, monitor);
 					}
 					catch (Exception e){
 						ValidationPlugin.getPlugin().handleException(e);
 					}
 				}
 			}
 			notifyGroupFinishing(resource, operation.getState(), monitor, groupListeners);
 			return;
 		}
 		
 		vp = new ValProperty();
 		vp.setConfigNumber(_configNumber);
 		ContentTypeWrapper ctw = new ContentTypeWrapper();
 		for (Validator val : getValidators(project)){
 			if (!monitor.isCanceled()) {
 				if (!_projectManager.shouldValidate(val, project, valType))continue;
 				if (Friend.shouldValidate(val, resource, valType, ctw)){
 					vp.getConfigSet().set(_idManager.getIndex(val.getId()));
 					// we do the suspend check after figuring out if it needs to be validated, because we save
 					// this information for the session.
 					if (operation.isSuspended(val, project))continue;
 					Validator.V2 v2 = val.asV2Validator();
 					if (v2 != null) {
 						notifyGroupListenersStarting(resource, operation.getState(), monitor, groupListeners, v2);
 					}
 					try {
 						visitor.visit(val, project, valType, operation, monitor);
 					}
 					catch (Exception e){
 						ValidationPlugin.getPlugin().handleException(e);
 					}
 				}
 			}
 		}
 		notifyGroupFinishing(resource, operation.getState(), monitor, groupListeners);
 		putValProperty(vp, resource, valType);
 	}
 
 	/**
 	 * Let the group listeners know that validation might be starting for the group of validators. 
 	 */
 	private void notifyGroupListenersStarting(final IResource resource,	 
 			final ValidationState state, final IProgressMonitor monitor, 
 			Map<String, IValidatorGroupListener[]> groupListeners, Validator.V2 v2) {
 		
 		String[] groups = v2.getValidatorGroups();
 		for (String group : groups) {
 			if (!groupListeners.containsKey(group)) {
 				IValidatorGroupListener[] createdListeners = null;
 				try {
 					createdListeners = ValidatorGroupExtensionReader.getDefault().createListeners(group);
 				}
 				catch (CoreException e){
 					String msg = NLS.bind(ValMessages.ErrConfig, v2.getId());
 					Status status = new Status(IStatus.ERROR, ValidationPlugin.PLUGIN_ID, msg);
 					CoreException core = new CoreException(status);
 					ValidationPlugin.getPlugin().handleException(core);
 					ValidationPlugin.getPlugin().handleException(e);
 					
 					// we create this to ensure that we don't signal the same exception over and over. 
 					createdListeners = new IValidatorGroupListener[0];
 				}
 				
 				// create and notify just this once
 				final IValidatorGroupListener[] listeners = createdListeners;
 					
 				groupListeners.put(group, listeners);
 				for (final IValidatorGroupListener listener : listeners) {
 					SafeRunner.run(new ISafeRunnable() {
 						public void run() throws Exception {
 							listener.validationStarting(resource, monitor, state);
 						}
 
 						public void handleException(Throwable exception) {
 							ValidationPlugin.getPlugin().handleException(exception);
 						}
 					});
 				}
 			}
 		}
 	}
 
 	/**
 	 * Let the group listeners know that validation is finished for the group of validators. 
 	 */
 	private void notifyGroupFinishing(final IResource resource, 
 			final ValidationState state, final IProgressMonitor monitor,
 			Map<String, IValidatorGroupListener[]> groupListeners) {
 		for (final IValidatorGroupListener[] listeners : groupListeners.values()) {
 			for (final IValidatorGroupListener listener : listeners) {
 				SafeRunner.run(new ISafeRunnable() {
 					public void run() throws Exception {
 						listener.validationFinishing(resource, monitor, state);
 					}
 
 					public void handleException(Throwable exception) {
 						ValidationPlugin.getPlugin().handleException(exception);
 					}
 				});
 			}
 		}
 	}
 
 	private ValProperty getValProperty(IResource resource, ValType valType, int configNumber) {
 		ValProperty vp = null;
 		try {
 			if (valType == ValType.Build)vp = (ValProperty)resource.getSessionProperty(StatusBuild);
 			else if (valType == ValType.Manual)vp = (ValProperty)resource.getSessionProperty(StatusManual);
 		}
 		catch (CoreException e){
 			// don't care about this one
 		}
 		if (vp == null)return null;
 		if (vp.getConfigNumber() != _configNumber)return null;
 		return vp;
 	}
 	
 	/**
 	 * Let the validation manager know that a project has been changed.
 	 * 
 	 * @param project The project that has been opened, created, or had it's description change.
 	 */
 	public void projectChanged(IProject project){
 		_projectManager.change(project);		
 	}
 	
 	/**
 	 * Let the validation manager know that a project has been removed.
 	 * 
 	 * @param project The project that has been closed or deleted.
 	 * 
 	 */
 	public void projectRemoved(IProject project){
 		_projectManager.remove(project);
 	}
 	
 	private void putValProperty(ValProperty vp, IResource resource, ValType valType) {
 		try {
 			if (valType == ValType.Build)resource.setSessionProperty(StatusBuild, vp);
 			else if (valType == ValType.Manual)resource.setSessionProperty(StatusManual, vp);
 		}
 		catch (CoreException e){
 			ValidationPlugin.getPlugin().handleException(e, IStatus.WARNING);
 		}
 	}
 
 	/**
 	 * Let each of the enabled validators know that a clean has been requested.
 	 * 
 	 * @param project the project that is being cleaned, or null if the entire workspace is being cleaned.
 	 * @param monitor
 	 */
 	void clean(final IProject project, final ValOperation operation, final IProgressMonitor monitor) {
 		IValidatorVisitor visitor = new IValidatorVisitor(){
 
 			public void visit(Validator validator, IProject project, ValType valType,
 				ValOperation operation, IProgressMonitor monitor) {
 				validator.clean(project, monitor);					
 			}
 			
 		};
 		accept(visitor, project, ValType.Build, operation, monitor);
 	}
 	
 	/**
 	 * Let each of the enabled validators know that a clean has been requested.
 	 * 
 	 * @param project the project that is being cleaned, or null if the entire workspace is being cleaned.
 	 * @param monitor
 	 */
 	public void clean(IProject project, IProgressMonitor monitor){
 		IValidatorVisitor visitor = new IValidatorVisitor(){
 
 			public void visit(Validator validator, IProject project, ValType valType,
 				ValOperation operation, IProgressMonitor monitor) {
 				validator.clean(project, monitor);					
 			}
 			
 		};
 		ValidationFramework.getDefault().getDependencyIndex().clear(project);
 		ValOperation operation = new ValOperation();
 		accept(visitor, project, ValType.Build, operation, monitor);
 	}
 
 	public void validatorsForProjectChanged(IProject project, boolean validationSettingChanged) {
 		if (validationSettingChanged){
 			if (project != null)_projectPreferences.remove(project);
 			configHasChanged();
 		}
 	}
 	
 	private class HasValidatorVisitor implements IResourceVisitor {
 		
 		private boolean 	_hasValidator;
 		private boolean		_isManual;
 		private boolean		_isBuild;
 		
 		public HasValidatorVisitor(boolean isManual, boolean isBuild){
 			_isManual = isManual;
 			_isBuild = isBuild;			
 		}
 		
 		public boolean hasValidator(IFolder folder){
 			try {
 				folder.accept(this);
 			}
 			catch (CoreException e){
 				ValidationPlugin.getPlugin().handleException(e);
 			}
 			return _hasValidator;
 		}
 
 		public boolean visit(IResource resource) throws CoreException {
 			if (resource instanceof IFolder)return true;
 			if (hasValidators(resource, _isManual, _isBuild)){
 				_hasValidator = true;
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	/**
 	 * Map validator id's to an index number on a bit set, so that we can quickly determine if a
 	 * particular validator needs to validate a particular resource.
 	 * @author karasiuk
 	 *
 	 */
 	private static class ValidatorIdManager {
 		
 		/**
 		 * Map validator id's to Integers. The integers correspond to bits in the ValProperty instances.
 		 */
 		private HashMap<String, Integer> _map = new HashMap<String, Integer>(100);
 		private HashMap<Integer, String> _reverseMap = new HashMap<Integer, String>(100);
 		
 		/** Next available bit. */
 		private int _next;
 		
 		/**
 		 * Answer the index number for this validator. If we haven't seen it yet allocate a new index number.
 		 * @param id validator id.
 		 * @return index into the validator bit mask.
 		 */
 		public int getIndex(String id){
 			Integer i = _map.get(id);
 			if (i != null)return i;
 			
 			i = _next++;
 			_map.put(id, i);
 			_reverseMap.put(i, id);
 			
 			return i;
 		}
 		
 		/**
 		 * Answer the validator id for the index.
 		 * @param index
 		 * @return null if the index number has not been set.
 		 */
 		public String getId(Integer index){
 			return _reverseMap.get(index);
 		}
 		
 		public void reset(){
 			_map.clear();
 			_reverseMap.clear();
 			_next = 0;
 		}
 		
 		/**
 		 * Answer the ids for the bit in the bitset. This is used for debugging. 
 		 * @param bs
 		 */
 		public String[] getIds(BitSet bs){
 			List<String> list = new LinkedList<String>();
 			for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
 				String id = getId(i);
 				if (id != null)list.add(id);
 			}
 			String[] s = new String[list.size()];
 			return list.toArray(s);
 		}		
 	}
 	
 	/**
 	 * This is used to keep track of which validators are enabled with which projects. We want to ensure
 	 * that we don't activate a validator (and it's plug-in) if it has nothing to validate in the workspace.
 	 * The ValManager keeps a single instance of this class. 
 	 * @author karasiuk
 	 *
 	 */
 	private static class ValidatorProjectManager {
 		
 		private ValProjectMap _manual = new ValProjectMap(ValType.Manual);
 		private ValProjectMap _build = new ValProjectMap(ValType.Build);
 		
 		/**
 		 * Should this validator attempt to validate any resources in this project?
 		 * 
 		 * @param validator
 		 *            The validator that is being tested.
 		 * @param project
 		 *            The project that is being tested. This can be null, which
 		 *            means that all projects will be tested.
 		 * @param type
 		 *            The type of validation operation.
 		 * @return true if the validator should attempt to validate.
 		 */
 		public synchronized boolean shouldValidate(Validator validator, IProject project, ValType type){
 			if (type == ValType.Build)return _build.shouldValidate(validator, project);
 			if (type == ValType.Manual)return _manual.shouldValidate(validator, project);
 				
 			return false;
 		}
 		
 		/**
 		 * A project has been created, opened, or had it's description changed.
 		 * @param project
 		 */
 		public void change(IProject project) {
 			reset();
 		}
 		
 		public void remove(IProject project) {
 			reset();
 		}
 		
 		
 		public synchronized void reset(){
 			_build.reset();
 			_manual.reset();
 		}
 		
 		/**
 		 * This is used to keep track of which validators are enabled for which projects. We want to ensure
 		 * that we don't activate a validator (and it's plug-in) if it has nothing to validate in the workspace.
 		 * @author karasiuk
 		 *
 		 */
 		private static class ValProjectMap {
 			/**
 			 * Map a validator to the projects that it validates. 
 			 * <p>
 			 * I've gone back and forth on whether the key should
 			 * be a Validator or the validator id. I'm back to it being the id because I was
 			 * running into cases where because of copying I wasn't getting the matches that I expected. If I run into
 			 * false matches, it is probably because reset isn't being called when it should be.
 			 */
 			private Map<String, Set<IProject>> _map = new HashMap<String, Set<IProject>>(50);
 			
 			private ValType _type;
 			
 			/** Have we been initialized yet? */
 			private boolean	_initialized;
 			
 			public ValProjectMap(ValType type){
 				_type = type;
 			}
 			
 			/**
 			 * Should this validator attempt to validate any resources in this project?
 			 * 
 			 * @param validator
 			 *            The validator that is being tested.
 			 * @param project
 			 *            The project that is being tested. This can be null, which
 			 *            means that all projects will be tested, and if any of them return true, 
 			 *            then true is answered for this method.
 			 *            
 			 * @return true if the validator should attempt to validate.
 			 */
 			public synchronized boolean shouldValidate(Validator validator, IProject project){
 				if (!_initialized)load();
 				String vid = validator.getId();
 				Set<IProject> projects = _map.get(vid);
 				if (projects == null)return false;
 				if (project == null)return projects.size() > 0;
 				return projects.contains(project);
 			}
 			
 			private void load() {
 				ValManager vm = ValManager.getDefault();
 				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 				Tracing.log("ValManager-02: loading " + projects.length + " projects");  //$NON-NLS-1$//$NON-NLS-2$
 				for (IProject project : projects){
 					if (!project.isOpen())continue;
 					Validator[] vals = vm.getValidators(project);
 					for (Validator v : vals){
 						String vid = v.getId();
 						Set<IProject> set = _map.get(vid);
 						if (set == null){
 							set = new HashSet<IProject>(50);
 							_map.put(vid, set);
 						}
 						
 						if (v.shouldValidateProject(project, _type))set.add(project);
 					}					
 				}
 				_initialized = true;
 			}
 			
 			public synchronized void reset(){
 				_initialized = false;
 				_map.clear();
 			}
 		}
 		
 	}
 
 	public void handleEvent(IFacetedProjectEvent event) {
 		projectChanged(event.getProject().getProject());
 	}
 
 	public void projectChanged(IProject project, int type) {
 		switch (type){
 		case IProjectChangeListener.ProjectClosed:
 		case IProjectChangeListener.ProjectDeleted:
 			projectRemoved(project);
 			break;
 		case IProjectChangeListener.ProjectOpened:
 		case IProjectChangeListener.ProjectChanged:
 		case IProjectChangeListener.ProjectAdded:
 			projectChanged(project);
 			break;
 		}
 		
 	}
 }

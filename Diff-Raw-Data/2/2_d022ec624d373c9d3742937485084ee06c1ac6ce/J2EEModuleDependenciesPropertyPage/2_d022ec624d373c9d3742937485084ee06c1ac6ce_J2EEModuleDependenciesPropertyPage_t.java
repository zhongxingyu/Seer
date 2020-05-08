 /******************************************************************************
  * Copyright (c) 2009 Red Hat and Others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Rob Stryker - initial implementation and ongoing maintenance
  *    Chuck Bridgham - additional support
  *    Konstantin Komissarchik - misc. UI cleanup
  ******************************************************************************/
 package org.eclipse.jst.j2ee.internal.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.componentcore.JavaEEModuleHandler;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.wst.common.componentcore.internal.IModuleHandler;
 import org.eclipse.wst.common.componentcore.internal.impl.TaskModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.ComponentDependencyContentProvider;
 import org.eclipse.wst.common.componentcore.ui.internal.taskwizard.TaskWizard;
 import org.eclipse.wst.common.componentcore.ui.propertypage.AddModuleDependenciesPropertiesPage;
 import org.eclipse.wst.common.componentcore.ui.propertypage.IReferenceWizardConstants;
 import org.eclipse.wst.common.componentcore.ui.propertypage.IReferenceWizardConstants.ProjectConverterOperationProvider;
 import org.eclipse.wst.common.componentcore.ui.propertypage.ModuleAssemblyRootPage;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 
 public class J2EEModuleDependenciesPropertyPage extends
 		AddModuleDependenciesPropertiesPage {
 
 	public J2EEModuleDependenciesPropertyPage(IProject project,
 			ModuleAssemblyRootPage page) {
 		super(project, page);
 	}
 	
 	public class ClasspathEntryProxy {
 		public IClasspathEntry entry;
 		public ClasspathEntryProxy(IClasspathEntry entry){
 			this.entry = entry;
 		}
 	}
 	
 	protected List <IClasspathEntry> originalClasspathEntries = new ArrayList<IClasspathEntry>(); 
 	protected List <ClasspathEntryProxy> currentClasspathEntries = new ArrayList<ClasspathEntryProxy>(); 
 	
 	@Override
 	protected void initialize() {
 		super.initialize();
 		resetClasspathEntries();
 	}
 
 	private void resetClasspathEntries() {
 		originalClasspathEntries.clear();
 		currentClasspathEntries.clear();
 		originalClasspathEntries.addAll(readRawEntries());
 		for(IClasspathEntry entry:originalClasspathEntries){
 			currentClasspathEntries.add(new ClasspathEntryProxy(entry));
 		}
 	}
 
 	@Override
 	public void performDefaults() {
 		resetClasspathEntries();
 		super.performDefaults();
 	}
 	
 	protected List <IClasspathEntry> readRawEntries(){
 		return readRawEntries(rootComponent);
 	}
 		
 	public static List <IClasspathEntry> readRawEntries(IVirtualComponent component){
 		List <IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
 		IJavaProjectLite javaProjectLite = JavaCoreLite.create(component.getProject());
 		try {
 			Map<IClasspathEntry, IClasspathAttribute> rawComponentClasspathDependencies = ClasspathDependencyUtil.getRawComponentClasspathDependencies(javaProjectLite, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
 			entries.addAll(rawComponentClasspathDependencies.keySet());
 		} catch (CoreException e) {
         	J2EEUIPlugin.logError(e);
 		}
 		return entries;
 	}
 	
 	@Override
 	public boolean postHandleChanges(IProgressMonitor monitor) {
 		return true;
 	}
 	
 	@Override
 	protected void handleRemoved(ArrayList<IVirtualReference> removed) {
 		super.handleRemoved(removed);
 		J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(rootComponent.getProject());
 	}
 	
 	@Override
 	protected void remove(Object selectedItem) {
 		if(selectedItem instanceof ClasspathEntryProxy){
 			ClasspathEntryProxy entry = (ClasspathEntryProxy)selectedItem;
 			currentClasspathEntries.remove(entry);
 		} else {
 			super.remove(selectedItem);
 		}
 	}
 	
 	@Override
 	protected String getModuleAssemblyRootPageDescription() {
 		if (JavaEEProjectUtilities.isEJBProject(project))
 			return Messages.J2EEModuleDependenciesPropertyPage_3;
 		if (JavaEEProjectUtilities.isApplicationClientProject(project))
 			return Messages.J2EEModuleDependenciesPropertyPage_4;
 		if (JavaEEProjectUtilities.isJCAProject(project))
 			return Messages.J2EEModuleDependenciesPropertyPage_5;
 		return super.getModuleAssemblyRootPageDescription();
 	}
 
 	@Override
 	protected IModuleHandler getModuleHandler() {
 		if(moduleHandler == null)
 			moduleHandler = new JavaEEModuleHandler();
 		return moduleHandler;
 	}
 	
 	@Override
 	protected void setCustomReferenceWizardProperties(TaskModel model) {
 		model.putObject(IReferenceWizardConstants.PROJECT_CONVERTER_OPERATION_PROVIDER, getConverterProvider());
 	}
 	
 	public ProjectConverterOperationProvider getConverterProvider() {
 		return new ProjectConverterOperationProvider() {
 			public IDataModelOperation getConversionOperation(IProject project) {
 				return J2EEProjectUtilities.createFlexJavaProjectForProjectOperation(project);
 			}
 		};
 	}
 	
 	@Override
 	protected ComponentDependencyContentProvider createProvider() {
 		JavaEEComponentDependencyContentProvider provider = new JavaEEComponentDependencyContentProvider(this);
 		provider.setClasspathEntries(currentClasspathEntries);
 		return provider;
 	}
 	
 	@Override
 	protected boolean canRemove(Object selectedObject) {
 		return super.canRemove(selectedObject) && !(selectedObject instanceof JavaEEComponentDependencyContentProvider.ConsumedClasspathEntryProxy);
 	}
 	
 	@Override
 	protected RuntimePathCellModifier getRuntimePathCellModifier() {
 		return new AddModuleDependenciesPropertiesPage.RuntimePathCellModifier(){
 			@Override
 			public boolean canModify(Object element, String property) {
 				if( property.equals(DEPLOY_PATH_PROPERTY) && element instanceof ClasspathEntryProxy) {
 					return true; 
 				}
 				return super.canModify(element, property);
 			}
 
 			@Override
 			public Object getValue(Object element, String property) {
 				if(element instanceof ClasspathEntryProxy){
 					IClasspathEntry entry = ((ClasspathEntryProxy)element).entry;
 					return ClasspathDependencyUtil.getRuntimePath(entry).toString();
 				}
 				return super.getValue(element, property);
 			}
 
 			@Override
 			public void modify(Object element, String property, Object value) {
 				if (property.equals(DEPLOY_PATH_PROPERTY)) {
 					TreeItem item = (TreeItem) element;
 					if(item.getData() instanceof ClasspathEntryProxy){
 						TreeItem[] components = availableComponentsViewer.getTree().getItems();
 						int tableIndex = -1;
 						for(int i=0; i < components.length; i++) {
 							if(components[i] == item) {
 								tableIndex = i;
 								break;
 							}
 						}
 						ClasspathEntryProxy proxy = (ClasspathEntryProxy)item.getData();
 						IPath runtimePath = new Path((String)value);
 						IClasspathEntry newEntry = ClasspathDependencyUtil.modifyDependencyPath(proxy.entry, runtimePath);
 						proxy.entry = newEntry;
 						resourceMappingsChanged = true;
 						if(tableIndex >= 0)
 							components[tableIndex].setText(AddModuleDependenciesPropertiesPage.DEPLOY_COLUMN, (String)value);
 					}
 				}
 				super.modify(element, property, value);
 			}
 		};
 	}
 	
 	
 	@Override
 	protected boolean saveReferenceChanges() {
 		boolean subResult = super.saveReferenceChanges();
 		if(!subResult){
 			return subResult;
 		}
 		Map <IPath, IClasspathEntry> modified = new HashMap <IPath, IClasspathEntry>();
 		
 		Map <IPath, IClasspathEntry> originalMap = new HashMap <IPath, IClasspathEntry>();
 		for(IClasspathEntry originalEntry : originalClasspathEntries){
 			originalMap.put(originalEntry.getPath(), originalEntry);
 		}
 
 		for(ClasspathEntryProxy proxy: currentClasspathEntries){
 			IClasspathEntry currentEntry = proxy.entry;
 			IPath path = currentEntry.getPath();
 			IClasspathEntry originalEntry = originalMap.remove(path);
 			if(currentEntry.equals(originalEntry)){
 				//no change
 				continue;
 			}
 			modified.put(path, currentEntry);
 		}
 		
 		Map <IPath, IClasspathEntry> removed = originalMap;
 		
 		IJavaProject javaProject = JavaCore.create(rootComponent.getProject());
 		try {
 			final IClasspathEntry [] rawClasspath = javaProject.getRawClasspath();
 			List <IClasspathEntry> newClasspath = new ArrayList <IClasspathEntry>();
 			for(IClasspathEntry entry:rawClasspath){
 				IPath path = entry.getPath();
 				if(removed.containsKey(path)){
 					//user removed entry
 					IClasspathEntry newEntry = ClasspathDependencyUtil.modifyDependencyPath(entry, null);
 					newClasspath.add(newEntry);
 				} else if(modified.containsKey(path)){
 					//user changed path value
 					IClasspathEntry newEntry = modified.get(path);
 					IPath runtimePath = ClasspathDependencyUtil.getRuntimePath(newEntry);
					if(runtimePath.toString().length() == 0){
 						//prevent the user from specifying no path
 						newEntry = ClasspathDependencyUtil.modifyDependencyPath(newEntry, new Path("/")); //$NON-NLS-1$
 					}
 					newClasspath.add(newEntry);
 				} else {
 					//no change
 					newClasspath.add(entry);
 				}
 			}
 			javaProject.setRawClasspath( newClasspath.toArray( new IClasspathEntry[ newClasspath.size() ] ), null );
 			
 			originalClasspathEntries.clear();
 			currentClasspathEntries.clear();
 			resetClasspathEntries();
 		} catch (JavaModelException e) {
         	J2EEUIPlugin.logError(e);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	protected void handleAddDirective( final TaskWizard wizard ) 
 	{
 		final List<IClasspathEntry> classpathEntries 
 			= (List<IClasspathEntry>) wizard.getTaskModel().getObject( AddJavaBuildPathEntriesWizardFragment.PROP_SELECTION );
 		
 		if( classpathEntries != null && ! classpathEntries.isEmpty() )
 		{
 			for( IClasspathEntry cpe : classpathEntries )
 			{
 				this.currentClasspathEntries.add( new ClasspathEntryProxy( cpe ) );
 			}
 		}
 		else
 		{
 			super.handleAddDirective(wizard);
 		}
 	}
 
 //	
 //	@Override
 //	protected IDataModelProvider getAddReferenceDataModelProvider(IVirtualComponent component) {
 //		return new AddComponentToEnterpriseApplicationDataModelProvider();
 //	}
 //	
 //	protected void addToManifest(ArrayList<IVirtualComponent> components) {
 //		StringBuffer newComps = getCompsForManifest(components);
 //		if(newComps.toString().length() > 0) {
 //			UpdateManifestOperation op = createManifestOperation(newComps.toString());
 //			try {
 //				op.run(new NullProgressMonitor());
 //			} catch (InvocationTargetException e) {
 //				J2EEUIPlugin.logError(e);
 //			} catch (InterruptedException e) {
 //				J2EEUIPlugin.logError(e);
 //			}	
 //		}
 //	}
 //
 //	protected void addOneComponent(IVirtualComponent component, IPath path, String archiveName) throws CoreException {
 //		//Find the Ear's that contain this component
 //		IProject[] earProjects = EarUtilities.getReferencingEARProjects(rootComponent.getProject());
 //		for (int i = 0; i < earProjects.length; i++) {
 //			IProject project = earProjects[i];
 //			
 //			IDataModelProvider provider = getAddReferenceDataModelProvider(component);
 //			IDataModel dm = DataModelFactory.createDataModel(provider);
 //			
 //			dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, ComponentCore.createComponent(project));
 //			dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, Arrays.asList(component));
 //			
 //			//[Bug 238264] the uri map needs to be manually set correctly
 //			Map<IVirtualComponent, String> uriMap = new HashMap<IVirtualComponent, String>();
 //			uriMap.put(component, archiveName);
 //			dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, uriMap);
 //	        dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);
 //	
 //			IStatus stat = dm.validateProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 //			if (stat != OK_STATUS)
 //				throw new CoreException(stat);
 //			try {
 //				dm.getDefaultOperation().execute(new NullProgressMonitor(), null);
 //			} catch (ExecutionException e) {
 //				ModuleCoreUIPlugin.logError(e);
 //			}	
 //		}
 //	}
 //	
 //
 //	protected StringBuffer getCompsForManifest(ArrayList<IVirtualComponent> components) {
 //		StringBuffer newComps = new StringBuffer();
 //		for (Iterator iterator = components.iterator(); iterator.hasNext();) {
 //			IVirtualComponent comp = (IVirtualComponent) iterator.next();
 //			String archiveName = new Path(derivedRefsObjectToRuntimePath.get(comp)).lastSegment();
 //			newComps.append(archiveName);
 //			newComps.append(' ');
 //		}
 //		return newComps;
 //	}
 //	
 //	protected UpdateManifestOperation createManifestOperation(String newComps) {
 //		return new UpdateManifestOperation(project.getName(), newComps, false);
 //	}
 //
 //	private void removeFromManifest(ArrayList<IVirtualComponent> removed) {
 //		String sourceProjName = project.getName();
 //		IProgressMonitor monitor = new NullProgressMonitor();
 //		IFile manifestmf = J2EEProjectUtilities.getManifestFile(project);
 //		ArchiveManifest mf = J2EEProjectUtilities.readManifest(project);
 //		if (mf == null)
 //			return;
 //		IDataModel updateManifestDataModel = DataModelFactory.createDataModel(new UpdateManifestDataModelProvider());
 //		updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.PROJECT_NAME, sourceProjName);
 //		updateManifestDataModel.setBooleanProperty(UpdateManifestDataModelProperties.MERGE, false);
 //		updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.MANIFEST_FILE, manifestmf);
 //		String[] cp = mf.getClassPathTokenized();
 //		List cpList = new ArrayList();
 //		
 //		for (int i = 0; i < cp.length; i++) {
 //			boolean foundMatch = false;
 //			for (Iterator iterator = removed.iterator(); iterator.hasNext();) {
 //				IVirtualComponent comp = (IVirtualComponent) iterator.next();
 //				String cpToRemove = new Path(derivedRefsOldComponentToRuntimePath.get(comp)).lastSegment();
 //				if (cp[i].equals(cpToRemove))
 //					foundMatch = true;
 //			}
 //			if (!foundMatch)
 //				cpList.add(cp[i]);
 //		}
 //		updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.JAR_LIST, cpList);
 //		try {
 //			updateManifestDataModel.getDefaultOperation().execute(monitor, null );
 //		} catch (ExecutionException e) {
 //			J2EEUIPlugin.logError(e);
 //		}
 //	}
 
 }

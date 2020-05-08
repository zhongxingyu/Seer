 package de.tum.ascodt.plugin.project;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.StandardLocation;
 import javax.tools.ToolProvider;
 import javax.tools.JavaCompiler.CompilationTask;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.RegistryFactory;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 
 import de.tum.ascodt.plugin.ASCoDTKernel;
 import de.tum.ascodt.plugin.project.ProjectBuilder;
 import de.tum.ascodt.plugin.project.natures.ASCoDTNature;
 import de.tum.ascodt.plugin.repository.ClasspathRepository;
 import de.tum.ascodt.plugin.ui.editors.gef.WorkbenchEditor;
 import de.tum.ascodt.plugin.ui.gef.model.Diagram;
 import de.tum.ascodt.plugin.ui.tabs.UITab;
 import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
 import de.tum.ascodt.plugin.utils.tracing.Trace;
 import de.tum.ascodt.repository.Target;
 import de.tum.ascodt.resources.ResourceManager;
 import de.tum.ascodt.sidlcompiler.frontend.node.Start;
 import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
 import de.tum.ascodt.utils.TemplateFile;
 import de.tum.ascodt.utils.exceptions.ASCoDTException;
 import org.eclipse.core.filesystem.IFileInfo;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.internal.resources.Folder;
 import org.eclipse.core.internal.resources.Workspace;
 import org.osgi.resource.Resource;
 
 
 /**
  * Represents one particular project.
  * 
  * @image html ../../../../../architecture_packages-and-important-classes.png
  * 
  * @author Tobias Weinzierl
  */
 public class Project {
 
 	class Pair<A,B>{
 		A _first;
 		B _second;
 		Pair(A first,B second){
 			_first=first;
 			_second=second;
 		}
 	}
 	/**
 	 * @return the _eclipseProjectHandle
 	 */
 	public org.eclipse.core.resources.IProject getEclipseProjectHandle() {
 		return _eclipseProjectHandle;
 	}
 
 
 
 
 	private static final String DEPENDENCIES = "dependencies";
 
 	private Trace _trace = new Trace(Project.class.getCanonicalName());
 
 	/**
 	 * holds the eclipse project handler
 	 */
 	private org.eclipse.core.resources.IProject                               _eclipseProjectHandle;
 
 	/**
 	 * instance of the static repository
 	 */
 	private de.tum.ascodt.repository.Repository  _staticRepository;
 
 	private String                                                            _projectFileName;
 
 	/**
 	 * list of all folders, which can be displayed in the navigator
 	 */
 	private Vector<IFolder> _folders;
 
 	/**
 	 * All the sidl files associated to the project. Should be equal to the sidl 
 	 * files stored in src, so if the user does a refresh, this list should be 
 	 * updated. 
 	 */
 	//TODO private java.util.Set<String>                                             _SIDLFiles;
 
 	/**
 	 * Set of full qualified SIDL files that are imported both in the project and 
 	 * by all compiler calls.
 	 */
 	//TODO private java.util.Set<String>                                             _importSIDLFiles;
 
 	//TODO private java.util.Set<String>                                             _importedJars;
 
 	/**
 	 * a global symbol table for all  sidl files in the project
 	 */
 	private SymbolTable _symbolTable;
 
 	/**
 	 * a classpath repository
 	 */
 	private ClasspathRepository _classpathRepository;
 
 	private boolean _loaderRunning;
 
 	private Object _mutex = new Object();
 	public Project(org.eclipse.core.resources.IProject eclipseProjectHandle) throws ASCoDTException, MalformedURLException {
 		_trace.in( "Project(...)", eclipseProjectHandle.getName() );
 		_eclipseProjectHandle = eclipseProjectHandle;
 		_staticRepository     = new de.tum.ascodt.repository.Repository();
 		_loaderRunning=false;
 		resetClasspathRepository();
 
 		_projectFileName      = "." + _eclipseProjectHandle.getName() + ".ascodt";
 		_folders = new Vector<IFolder>();
 		setSymbolTable(new SymbolTable());
 		
 		org.eclipse.core.resources.IFile projectFile = _eclipseProjectHandle.getFile( getNameOfProjectFile() );
 		if (!projectFile.exists()) {
 			writeProjectFile();
 			addClasspathEntries();
 
 		}
 
 		//folder for sidl files
 		createSource();
 
 		//folder for included sidl files
 		createIncludes();
 		//folder where all native libraries are collected
 		createNative();
 		//folder for all imported components
 		createImports();
 		//workspace for dynamic repositories
 		createWorkspace();
 		createJavaFolders();
 		readProjectFile();
 
 		_trace.out( "Project(...)", eclipseProjectHandle.getName() );
 	}
 
 
 
 	public void setLoaderFlag(boolean flag){
 		synchronized(_mutex){
 			_loaderRunning=flag;
 		}
 	}
 	
 	public boolean loaderIsRunning(){
 		boolean flag=false;
 		synchronized(_mutex){
 			flag= _loaderRunning;
 		}
 		
 	
 		return flag;
 	}
 
 	private void cleanClasspathRepository(){
 		if(_classpathRepository!=null){
 			_classpathRepository.clear();
 			_classpathRepository=null;
 		}
 
 	}
 
 	/**
 	 * @throws MalformedURLException
 	 */
 	private void resetClasspathRepository() throws MalformedURLException {
 
 		_classpathRepository=new ClasspathRepository(_eclipseProjectHandle,ASCoDTKernel.getDefault().getClass().getClassLoader());
 		_classpathRepository.addURL(new File(_eclipseProjectHandle.getLocation().toPortableString()+"/bin").toURI().toURL());
 	}
 
 
 
 	/**
 	 * @return the _classpathRepository
 	 */
 	public ClasspathRepository getClasspathRepository() {
 		return _classpathRepository;
 	}
 
 
 
 
 	/**
 	 * Build the global symbol table
 	 * @throws ASCoDTException 
 	 * @throws CoreException 
 	 * @see buildProjectSources
 	 */
 	public void buildProjectSources() throws ASCoDTException{
 		try{
 			Vector<Pair<String,Start>> sources=new Vector<Pair<String,Start>>();
 			Vector<Pair<String,Start>> deps=new Vector<Pair<String,Start>>();
 			Vector<Pair<String,Start>> imports=new Vector<Pair<String,Start>>();
 			SymbolTable symbolTable=new SymbolTable();
 			for(String dep:getSIDLDependencies()){
 				deps.add(new Pair<String,Start>(
 						dep,
 						de.tum.ascodt.plugin.project.builders.ProjectBuilder.buildStartSymbolsForSIDLResource(dep))
 						);
 			}
 			buildStartSymbolsForSIDLResources(imports,_eclipseProjectHandle.getFolder(getImportsFolder()));
 			buildStartSymbolsForSIDLResources(sources,_eclipseProjectHandle.getFolder(getSourcesFolder()));
 			for(Pair<String,Start> resourceEntry:deps){
 				de.tum.ascodt.plugin.project.builders.ProjectBuilder.extendSymbolTable(resourceEntry._second, symbolTable, resourceEntry._first);
 			}
 
 			for(Pair<String,Start> resourceEntry:imports){
 				de.tum.ascodt.plugin.project.builders.ProjectBuilder.extendSymbolTable(resourceEntry._second, symbolTable, resourceEntry._first);
 			}
 
 			for(Pair<String,Start> resourceEntry:sources){
 				de.tum.ascodt.plugin.project.builders.ProjectBuilder.extendSymbolTable(resourceEntry._second, symbolTable, resourceEntry._first);
 			}
 
 			_symbolTable=symbolTable;
 			de.tum.ascodt.plugin.project.builders.ProjectBuilder.generateBlueprints(_eclipseProjectHandle);
 			compileComponents();
 			de.tum.ascodt.plugin.project.ProjectBuilder.getInstance().notifyProjectChangedListeners();
 
 
 
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "buildProjectSources()", "getting sidl dependencies failed", e);
 		}
 	}
 	/**
 	 * build all project sidl files in given folder
 	 * @param startSymbolsMap a hash map for stroring resources startsymbols
 	 * @throws ASCoDTException
 	 */
 	private void buildStartSymbolsForSIDLResources(Vector<Pair<String,Start>> startSymbolsMap,IResource resource) throws ASCoDTException {
 
 		try{
 			if(resource instanceof IFolder){
 				Vector<IResource> files=new Vector<IResource>(); 
 				for(IResource child:((IFolder)resource).members()){
 					if(child instanceof IFile)
 						files.add(child);
 					else
 						buildStartSymbolsForSIDLResources(startSymbolsMap,child);
 
 				}
 				for(IResource file:files)
 					buildStartSymbolsForSIDLResources(startSymbolsMap,file);
 
 			}else if(resource instanceof IFile &&resource.getName().contains(".sidl")){
 				startSymbolsMap.add(new Pair<String,Start>(
 						resource.getLocation().toPortableString(),
 						de.tum.ascodt.plugin.project.builders.ProjectBuilder.buildStartSymbolsForSIDLResource(resource.getLocation().toPortableString())
 						)
 						);
 			}
 
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "buildProjectSources()", "building sidl files failed", e);
 		}
 	}
 
 
 	/**
 	 * creates the source folder for the project and the corresponding model
 	 * @throws ASCoDTException 
 	 */
 	private void createSource() throws ASCoDTException{
 		IFolder srcFolder=_eclipseProjectHandle.getFolder(getSourcesFolder());
 
 		try{
 			if(!srcFolder.exists())
 				createParentFolders(srcFolder);
 			_folders.add(srcFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createSource()", "creating a source folder failed", e);
 		}
 	}
 
 
 
 
 	/**
 	 * creates a workbench file with the given name 
 	 * @param workbenchName the name of the workbench to be created
 	 */
 	public void createWorkbech(String workbenchName) throws ASCoDTException{
 		_trace.in( "createWorkbech()" );
 		org.eclipse.core.resources.IFile workbenchFile = _eclipseProjectHandle.getFile( getSourcesFolder()+"/"+workbenchName+".workbench");
 
 		try {
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			ObjectOutputStream objectStream=new ObjectOutputStream(out);
 			objectStream.writeObject(new Diagram());
 			objectStream.flush();
 			objectStream.close();
 			createProjectFile(workbenchFile,new ByteArrayInputStream(out.toByteArray()));
 
 			workbenchFile.refreshLocal(IResource.DEPTH_INFINITE,null);
 			ProjectBuilder.getInstance().notifyProjectChangedListeners();
 		} catch (CoreException e) {
 			throw new ASCoDTException(getClass().getName(), "createWorkbech()", "creating a workbench file failed", e); 
 		} catch (IOException e) {
 			throw new ASCoDTException(getClass().getName(), "createWorkbech()", "creating a workbench file initial content failed", e); 
 		}
 		_trace.out( "createWorkbech()" );
 	}
 
 	/**
 	 * creates the includes folder for the project and the corresponding model
 	 * @throws ASCoDTException 
 	 */
 	private void createIncludes() throws ASCoDTException{
 		IFolder includesFolder=_eclipseProjectHandle.getFolder(getIncludesFolder());
 		try{
 
 			includesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			if(!includesFolder.exists())
 				createParentFolders(includesFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createIncludes()", "creating an includes folder failed", e);
 		}
 	}
 
 	/**
 	 * creates a folder where we store the libraries needed
 	 * for native components
 	 * @throws ASCoDTException 
 	 */
 	private void createNative() throws ASCoDTException {
 		IFolder nativeFolder=_eclipseProjectHandle.getFolder(getNativeFolder());
 		try{
 			nativeFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			if(!nativeFolder.exists())
 				createParentFolders(nativeFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createIncludes()", "creating an includes folder failed", e);
 		}
 
 	}
 
 	/**
 	 * creates a folder where we store the libraries needed
 	 * for native components
 	 * @throws ASCoDTException 
 	 */
 	private void createImports() throws ASCoDTException {
 		IFolder importsFolder=_eclipseProjectHandle.getFolder(getImportsFolder());
 		try{
 			importsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			if(!importsFolder.exists())
 				createParentFolders(importsFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createIncludes()", "creating an includes folder failed", e);
 		}
 
 	}
 
 	/**
 	 * creates all folders where the java classes are generated and sets the corresponding classpaths
 	 * @throws ASCoDTException
 	 */
 	private void createJavaFolders() throws ASCoDTException {
 		IFolder sourcesFolder=_eclipseProjectHandle.getFolder(getJavaSourcesFolder());
 		IFolder proxiesFolder=_eclipseProjectHandle.getFolder(getJavaProxiesFolder());
 
 		IFolder classOutputFolder=_eclipseProjectHandle.getFolder(getClassOutputFolder());
 		try{
 			sourcesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			proxiesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			classOutputFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			if(!sourcesFolder.exists())
 				createParentFolders(sourcesFolder);
 			if(!proxiesFolder.exists())
 				createParentFolders(proxiesFolder);
 			if(!classOutputFolder.exists())
 				createParentFolders(classOutputFolder);
			addClasspathSource("/"+sourcesFolder.getLocation().removeFirstSegments(_eclipseProjectHandle.getLocation().segmentCount()-1
 					).toPortableString());
			addClasspathSource("/"+proxiesFolder.getLocation().removeFirstSegments(_eclipseProjectHandle.getLocation().segmentCount()-1
 					).toPortableString());
 
 
 			_folders.add(sourcesFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createJavaFolders()", "creating java folders failed!", e);
 		}
 	}
 	/**
 	 * creates the workspace folder used for the storage of the dynamic repository files
 	 * @throws ASCoDTException 
 	 */
 	private void createWorkspace() throws ASCoDTException{
 		IFolder workspaceFolder=_eclipseProjectHandle.getFolder(getWorkspaceFolder());
 		try{
 			createParentFolders(workspaceFolder);
 			_folders.add(workspaceFolder);
 		}catch(CoreException e){
 			throw new ASCoDTException(getClass().getName(), "createWorkspace()", "creating a workspace folder failed", e);
 		}
 	}
 
 	/**
 	 * creates a new source file for specified component. It sets the initial structure
 	 * of the sidl file (namespace and component definition)
 	 * @param componentName name of the component
 	 * @param namespace namespace of the component coded as <n0>.<n1>.<n2>...
 	 * @param componentTarget target language for the component to be compiled
 	 * @throws ASCoDTException 
 	 */
 	public void createComponentSIDLSourceFile(String componentName, String namespace, Target componentTarget) throws ASCoDTException{
 		_trace.in( "createComponentSIDLSourceFile()" );
 		org.eclipse.core.resources.IFile sourceFile = _eclipseProjectHandle.getFile( getSourcesFolder()+"/"+componentName+".sidl");
 
 		try {
 			//createProjectFile(sourceFile,new ByteArrayInputStream(new byte[]{}));
 			Assert.isNotNull(namespace);
 			String[] namespaces;
 			if(namespace.equals("")){
 				namespaces = new String[]{"default"};
 			}else{
 				if(!namespace.contains("."))
 					namespaces = new String[]{namespace};
 				else
 					namespaces = namespace.split("\\.");
 			}
 			TemplateFile templateFile = new TemplateFile(ResourceManager.getResourceAsStream("new-sidl-component.template",ASCoDTKernel.ID),sourceFile.getLocationURI().toURL(),namespaces,TemplateFile.getLanguageConfigurationForSIDL(),true);
 			templateFile.addMapping("__CLASS_NAME__",componentName);
 			templateFile.addMapping("__TARGET__", componentTarget.getType().toString());
 			templateFile.open();
 			templateFile.close();
 			_eclipseProjectHandle.refreshLocal(IResource.DEPTH_INFINITE,null);
 			ProjectBuilder.getInstance().notifyProjectChangedListeners();
 		} catch (Exception e) {
 			throw new ASCoDTException(getClass().getName(), "createComponentSIDLSourceFile()", "creating SIDL file from template failed", e);
 		}
 		_trace.out( "createComponentSIDLSourceFile()" );
 	}
 
 	/**
 	 * creates a workbench file for the dynamic ASCoDT repository
 	 * @param workbenchName name of the workbench 
 	 */
 	public void createWorkbench(String workbenchName) throws ASCoDTException{
 		_trace.in( "createWorkbench()" );
 		org.eclipse.core.resources.IFile workbenchFile = _eclipseProjectHandle.getFile( getWorkspaceFolder()+"/"+workbenchName+".workbench");
 
 		try {
 			ByteArrayOutputStream out=new ByteArrayOutputStream();
 			ObjectOutputStream outputStream = new ObjectOutputStream(out);
 			outputStream.writeObject(null);
 			outputStream.close();	
 			createProjectFile(workbenchFile,new ByteArrayInputStream(out.toByteArray()));
 			workbenchFile.refreshLocal(IResource.DEPTH_INFINITE,null);
 			ProjectBuilder.getInstance().notifyProjectChangedListeners();
 		} catch (Exception e) {
 			throw new ASCoDTException(getClass().getName(), "createWorkbench()", "creating SIDL file from template failed", e);
 		}
 		_trace.out( "createComponentSIDLSourceFile()" );
 	}
 
 	/**
 	 * A method used to create the java classes for a new user interface for specific 
 	 * component.
 	 * @param componentInterface identifier of the component
 	 * @throws ASCoDTException 
 	 */
 	public void createUserInterface(String componentInterface) throws ASCoDTException{
 		_trace.in("createUserInterface()");
 		try {
 			org.eclipse.core.resources.IFile sourceUIFile = createJavaSourceFile(componentInterface.replaceAll("\\.", "/")+"UI.java");
 			String[] namespaces = retrieveNamespaces(componentInterface);
 			TemplateFile templateFile = new TemplateFile(ResourceManager.getResourceAsStream("new-ui.template",ASCoDTKernel.ID),sourceUIFile.getLocationURI().toURL(),namespaces,TemplateFile.getLanguageConfigurationForJava(),true);
 			templateFile.addMapping("__COMPONENT_NAME__", componentInterface.substring(componentInterface.lastIndexOf(".")+1));
 			templateFile.addMapping("__UITAB_CLASS__", UITab.class.getCanonicalName());
 			templateFile.open();
 			templateFile.close();
 
 
 
 			//			CreateLocalJavaComponent createJavaComponent = new CreateLocalJavaComponent(
 			//					_symbolTable,
 			//					ResourceManager.getResource("de/tum/ascodt/plugin/ui/resources/",ASCoDTKernel.ID), 
 			//					new File(_eclipseProjectHandle.getLocation().toPortableString()+getJavaSourcesFolder()).toURI().toURL(),
 			//					_symbolTable.getScope(_symbolTable.getGlobalScope().getClassDefinition(componentInterface)).getFullIdentifierOfPackage(),
 			//					"__UI__"
 			//					);
 			//			_symbolTable.getGlobalScope().getClassDefinition(componentInterface).apply(createJavaComponent);
 
 			_eclipseProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
 			compileComponents();
 		} catch (Exception e) {
 			throw new ASCoDTException(getClass().getName(), "createUserInterface()", "creating user interface for \""+componentInterface+"\" from template failed", e);
 		}
 		_trace.out("createUserInterface()");
 	}
 
 
 
 	/**
 	 * @param componentInterface
 	 * @return
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	public org.eclipse.core.resources.IFile createJavaSourceFile(
 			String relativePath) throws CoreException, IOException {
 		org.eclipse.core.resources.IFile sourceUIFile = _eclipseProjectHandle.getFile( getJavaSourcesFolder()+"/"+relativePath);
 
 		createProjectFile(sourceUIFile,null);
 		sourceUIFile.refreshLocal(IResource.DEPTH_INFINITE,null);
 		return sourceUIFile;
 	}
 
 
 
 	/**
 	 * @param componentInterface
 	 * @return
 	 */
 	public String[] retrieveNamespaces(String componentInterface) {
 		String namespace=componentInterface.substring(0, componentInterface.lastIndexOf("."));
 		String[] namespaces;
 		if(namespace.equals("")){
 			namespaces = new String[]{"default"};
 		}else{
 			if(!namespace.contains("."))
 				namespaces = new String[]{namespace};
 			else
 				namespaces = namespace.split("\\.");
 		}
 		return namespaces;
 	}
 
 
 
 	/**
 	 * Reads the project file and loads the saved static repository.
 	 */
 	private void readProjectFile() throws ASCoDTException{
 		_trace.in( "readProjectFile()" );
 
 		org.eclipse.core.resources.IFile projectFile = _eclipseProjectHandle.getFile( getNameOfProjectFile() );
 
 		try {
 			projectFile.refreshLocal(0,null);
 			if(projectFile.getContents()!=null){
 				ObjectInputStream in = new ObjectInputStream(projectFile.getContents());
 				Object object=in.readObject();
 				if(object!=null&&object instanceof de.tum.ascodt.repository.Repository)
 					setStaticRepository((de.tum.ascodt.repository.Repository)object);
 				in.close();
 			}
 		} catch (Exception e) {
 			throw new ASCoDTException(getClass().getName(), "readProjectFile()", "reading project file failed", e);
 		}
 		_trace.out( "readProjectFile()" );
 	}
 
 
 	private void evaluateContributions(IExtensionRegistry registry, Set<IClasspathEntry> classpathEntries) throws CoreException, ASCoDTException{
 		IConfigurationElement[] config =
 				registry.getConfigurationElementsFor(de.tum.ascodt.plugin.extensions.Project.ID);
 
 		for (IConfigurationElement e : config) {
 
 			final Object o =
 					e.createExecutableExtension("class");
 			if (o!=null&&o instanceof de.tum.ascodt.plugin.extensions.Project) {
 				_trace.debug("evaluateContributions()","executing a contribution");
 				((de.tum.ascodt.plugin.extensions.Project)o).addClasspathEntries(classpathEntries);
 			}
 		}
 
 	}
 	/**
 	 * sets the default classpath entries of the ascodt project
 	 * @throws ASCoDTException 
 	 */
 	private void addClasspathEntries() throws ASCoDTException {
 		IJavaProject javaProject = JavaCore.create(_eclipseProjectHandle); 
 		try {
 			Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
 			for(IClasspathEntry classElement: Arrays.asList(javaProject.getRawClasspath())){
 				if(classElement.getEntryKind()==org.eclipse.jdt.core.IClasspathEntry.CPE_CONTAINER||
 						(!classElement.getPath().toString().toLowerCase().contains("ascodt")&&
 								classElement.getEntryKind()==org.eclipse.jdt.core.IClasspathEntry.CPE_LIBRARY))
 					entries.add(classElement);
 			}
 
 			entries.add(JavaCore.newLibraryEntry(new Path(ResourceManager.getResourceAsPath("",ASCoDTKernel.ID).getPath()),null,null,false));
 			entries.add(JavaCore.newLibraryEntry(new Path(ResourceManager.getResourceAsPath("swt.jar",ASCoDTKernel.ID).getPath()),null,null,false));
 
 			entries.add(JavaRuntime.getDefaultJREContainerEntry());
 			IExtensionRegistry reg = RegistryFactory.getRegistry();
 			evaluateContributions(reg,entries);
 			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
 		} catch (JavaModelException e) {
 			throw new ASCoDTException(getClass().getName(), "addClasspathEntries()", "adding default classpath entries to project "+_eclipseProjectHandle.getLocation().toString()+" failed", e);
 		} catch (IOException e) {
 			throw new ASCoDTException(getClass().getName(), "addClasspathEntries()", "adding default classpath entries to project "+_eclipseProjectHandle.getLocation().toString()+" failed", e);
 		} catch (CoreException e) {
 			throw new ASCoDTException(getClass().getName(), "addClasspathEntries()", "adding extensions classpath entries to project "+_eclipseProjectHandle.getLocation().toString()+" failed", e);
 
 		}
 	}
 
 
 	/**
 	 * add a new source entry to the project. This extends the build path of the project
 	 * @param entryPath the path of the entry
 	 * @throws ASCoDTException 
 	 */
 	public void addClasspathSource( String entryPath) throws ASCoDTException {
 		try {
 			IJavaProject javaProject = JavaCore.create(_eclipseProjectHandle); 
 			Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
 			for(IClasspathEntry classElement: Arrays.asList(javaProject.getRawClasspath())){
 				entries.add(classElement);
 			}
 			entries.add(JavaCore.newLibraryEntry(new Path(ResourceManager.getResourceAsPath("",ASCoDTKernel.ID).getPath()),null,null,false));
 			IClasspathEntry entry=JavaCore.newSourceEntry(new Path(entryPath));
 			if(!entries.contains(entry))
 				entries.add(entry);
 			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
 
 		} catch (JavaModelException e) {
 			throw new ASCoDTException(getClass().getName(), "addClasspathSource()", "adding default classpath source entry to project "+_eclipseProjectHandle.getLocation().toString()+" failed", e);
 		} catch (IOException e) {
 			throw new ASCoDTException(getClass().getName(), "addClasspathSource()", "adding default classpath source entry to project "+_eclipseProjectHandle.getLocation().toString()+" failed", e);
 
 		}
 	}
 
 	/**
 	 * creates all parents for a given file
 	 * @param folder
 	 * @throws CoreException
 	 */
 	private static void createParentFolders(org.eclipse.core.resources.IFolder folder) throws CoreException {
 		org.eclipse.core.resources.IContainer parent = folder.getParent();
 		if (parent instanceof org.eclipse.core.resources.IFolder) {
 			createParentFolders((org.eclipse.core.resources.IFolder) parent);
 		}
 		@SuppressWarnings("restriction")
 		IFileStore store = ((Folder)folder).getStore();
 		IFileInfo localInfo = store.fetchInfo();
 		if (!folder.exists()) {
 			if(!localInfo.exists())
 				folder.create(false, true, null);
 			folder.refreshLocal(IResource.DEPTH_INFINITE, null);
 		}
 	}
 
 	/**
 	 * creates a file and all missing parent folders in the given project
 	 * @param projectFile
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void createProjectFile(org.eclipse.core.resources.IFile projectFile,InputStream stream) throws CoreException, IOException{
 		if(projectFile.getParent()!=null && projectFile.getParent() instanceof org.eclipse.core.resources.IFolder)
 			createParentFolders((org.eclipse.core.resources.IFolder)projectFile.getParent());
 		if(!projectFile.exists()){
 			projectFile.create(stream,true, null);
 		}else 
 			projectFile.setContents(stream, true,true, null);
 	}
 
 	public void compileComponents(){
 		Job job = new Job("Classes Compilation") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					cleanClasspathRepository();
 					System.gc();
 					
 					compileComponents_i();
 					resetClasspathRepository();
 					return Status.OK_STATUS;
 				} catch (Exception e) {
 					return Status.CANCEL_STATUS;
 				}
 			}
 
 		};
 		job.schedule();
 	}
 	/**
 	 * compiles the java classes for all components in the project
 	 * @throws ASCoDTException
 	 */
 	public void compileComponents_i() {
 		try{
 			//Vector<IFile> workbenchInstances=closeRunningWorkbenchInstances();
 
 			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 			if(compiler == null){
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "compileComponents()",  "Exclipse not executed via JDK", null );
 
 				return;
 			}
 			//use standard java file manager
 			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
 			//retrieve all sources 
 			List<File> sources=new LinkedList<File>(Arrays.asList(getComponentJavaProxies()));
 			sources.addAll(Arrays.asList(getComponentJavaSources()));
 			if(!sources.isEmpty()){
 				Iterable<? extends JavaFileObject> compilationUnitsForJavaProxies =
 						fileManager.getJavaFileObjectsFromFiles(sources);
 
 				//set the compiler output folder
 				fileManager.setLocation(StandardLocation.CLASS_OUTPUT,Arrays.asList(new File( _eclipseProjectHandle.getLocation().toPortableString()+getClassOutputFolder())));
 				List<File> fileList=new ArrayList<File>();
 				IJavaProject jProject=JavaCore.create(_eclipseProjectHandle);
 
 
 				for(IClasspathEntry entry:jProject.getRawClasspath()){
 					if(entry.getEntryKind()==IClasspathEntry.CPE_LIBRARY)
 						//if(!entry.getPath().toString().startsWith("/"+_eclipseProjectHandle.getName()))
 						fileList.add(entry.getPath().toFile());
 
 				}
 
 				fileManager.setLocation(StandardLocation.CLASS_PATH,fileList);
 
 				//here we collect error messages
 				DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
 
 				CompilationTask compilationTask=compiler.getTask(null, fileManager, diagnostics,null, null, compilationUnitsForJavaProxies);
 				boolean status=compilationTask.call();
 				fileManager.close();
 				if(status){
 					_eclipseProjectHandle.refreshLocal(IResource.DEPTH_INFINITE,null);
 					//TODO resetClasspathRepository();
 					//TODO notifyRepository();
 					//TODO openWorkbenchEditors(workbenchInstances);
 				}else{
 					String errors="";
 					for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()){
 
 						errors+=diagnostic+"\n";
 					}
 					ErrorWriterDevice.getInstance().showError( getClass().getName(), "compileComponents()",  "Compilation error:"+errors, null );
 
 				}
 			}
 		}catch (Exception e) {
 			ErrorWriterDevice.getInstance().showError( getClass().getName(), "compileComponents()",  e.getLocalizedMessage(), e ); 
 		}
 
 	}
 
 	private void openWorkbenchEditors(final Vector<IFile> workbenchInputs) throws ASCoDTException{
 
 		Display.getDefault().asyncExec(new Runnable(){
 
 			@Override
 			public void run() {
 				for(IFile input:workbenchInputs)
 					try {
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
 								new FileEditorInput(input),WorkbenchEditor.ID);
 					} catch (PartInitException e) {
 
 					}
 			}
 
 		});
 
 
 	}
 	/**
 	 * this method closes all running workbench instances. It is needed to assure the consistency of the
 	 * component classes. the method is invoked by the compileComponents method
 	 */
 	private Vector<IFile> closeRunningWorkbenchInstances() {
 		final Vector<IFile> editorInputs=new Vector<IFile>();
 		Display.getDefault().syncExec(new Runnable(){
 
 			@Override
 			public void run() {
 				if(PlatformUI.getWorkbench()!=null&&PlatformUI.getWorkbench().getActiveWorkbenchWindow()!=null
 						&&PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()!=null&&
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()!=null){
 
 					for(IEditorReference ref:PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
 						if(ref.getEditor(false) instanceof WorkbenchEditor &&
 								ProjectBuilder.getInstance().getProject(((WorkbenchEditor)ref.getEditor(false)).getProject()).equals(Project.this)){
 							editorInputs.add(((FileEditorInput)ref.getEditor(false).getEditorInput()).getFile());
 							ref.getEditor(false).getSite().getPage().closeEditor(ref.getEditor(false), false);
 
 						}
 
 					}
 				}
 
 
 			}
 		});
 		return editorInputs;
 	}
 
 
 
 
 
 	/**
 	 * Writes the project file.
 	 * 
 	 * Overwrites an existing file. Basically streams the repositories to the 
 	 * file. If they are empty, the operation creates the startup project file.
 	 */
 	private void writeProjectFile() throws ASCoDTException {
 		_trace.in( "writeProjectFile()" );
 
 		org.eclipse.core.resources.IFile projectFile = _eclipseProjectHandle.getFile( getNameOfProjectFile() );
 
 		try {
 			java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
 			java.io.ObjectOutputStream    objectOutputStream    = new java.io.ObjectOutputStream(byteArrayOutputStream);
 
 			objectOutputStream.writeObject(_staticRepository); // argument must be Serializable
 			objectOutputStream.close();
 			createProjectFile(projectFile,new java.io.ByteArrayInputStream( byteArrayOutputStream.toByteArray()));
 
 		}
 		catch (Exception e) {
 			throw new ASCoDTException(getClass().getName(), "writeProjectFile()", "writing project file failed", e);
 		}
 
 		_trace.out( "writeProjectFile()" );
 	}
 
 
 	/**
 	 * 
 	 * @return identifier of the source folder
 	 */
 	private String getSourcesFolder() {
 		return "sidl";
 	}
 
 	/**
 	 * 
 	 * @return folder for the storage of dynamic repositories
 	 */
 	private String getWorkspaceFolder() {
 		return "/workspace";
 	}
 
 	private String getIncludesFolder(){
 		return "/includes";
 	}
 
 	public String getJavaSourcesFolder(){
 		return "/src";
 	}
 
 	public String getNativeFolder(){
 		return "/native";
 	}
 
 
 	public String getImportsFolder(){
 		return "/imports";
 	}
 	public String getJavaProxiesFolder(){
 		return "/components/java";
 	}
 
 	/**
 	 * @return The full qualified project file
 	 */
 	public String getNameOfProjectFile() {
 		return _projectFileName;
 	}
 
 	/**
 	 * 
 	 * @return the name of the project
 	 */
 	public String getName(){
 		return _eclipseProjectHandle.getName();
 	}
 
 	/**
 	 * 
 	 * @return Project file or throw exception if it doesn't exist.
 	 * 
 	 * @throws ASCoDTException
 	 */
 	public org.eclipse.core.resources.IFile getProjectFile() throws ASCoDTException {
 		String projectFilePath = getNameOfProjectFile();
 		org.eclipse.core.resources.IFile result = _eclipseProjectHandle.getFile( projectFilePath );
 		if (!result.exists()) {
 			throw new ASCoDTException( getClass().getName(), "getProjectFile()", "project file " + projectFilePath + " does not exist", null );
 		}
 		return result;
 	}
 
 
 	/**
 	 * @return static repository
 	 */
 	public de.tum.ascodt.repository.Repository getStaticRepository() {
 		return _staticRepository;
 	}
 
 
 	/**
 	 * @param staticRepository the staticRepository to set
 	 */
 	private void setStaticRepository(
 			de.tum.ascodt.repository.Repository staticRepository) {
 		this._staticRepository = staticRepository;
 	}
 
 	/**
 	 * 
 	 * @return all children folders
 	 */
 	public Object[] getFolders() {
 
 		return _folders.toArray();
 	}
 
 
 	/**
 	 * @param symbolTable the symbolTable to set
 	 */
 	public void setSymbolTable(SymbolTable symbolTable) {
 		this._symbolTable = symbolTable;
 	}
 
 	/**
 	 * notify all project listeners for the changed symbolTable and write down the change
 	 * @throws ASCoDTException 
 	 */
 	public void notifyRepository() throws ASCoDTException{
 		for ( 
 				de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement component:
 					_symbolTable.getGlobalScope().getFlattenedClassElements()
 				){
 
 			_staticRepository.addComponent(_symbolTable.getScope(component).getFullQualifiedName(component.getName().getText()),component.getTarget().getText());
 		}
 		writeProjectFile();
 	}
 	/**
 	 * @return the _symbolTable
 	 */
 	public SymbolTable getSymbolTable() {
 		return _symbolTable;
 	}
 
 	/**
 	 * retrieves all source files in given directory.
 	 * @param path directory to search for source files.
 	 * @param sources a collection, where to put the founded source files
 	 */
 	private void retrieveSources(String path,
 			Vector<File> sources){
 		File f=new File(path);
 		if(f.isFile()&&f.getName().endsWith(".java"))
 			sources.add(f);
 		else{
 			if(f.listFiles()!=null)
 				for(File file:f.listFiles())
 					retrieveSources(file.getAbsolutePath(),sources);
 		}
 	}
 	/**
 	 * 
 	 * @param workbenchName name of the workbench
 	 * @return true if the project has a workbench in the workspace
 	 */
 	public boolean hasWorkbench(String workbenchName) {
 		return _eclipseProjectHandle.getFile( getWorkspaceFolder()+"/"+workbenchName+".workbench").exists();
 	}
 
 	public File[] getComponentJavaProxies() {
 		// TODO Auto-generated method stub
 		Vector<File> proxies=new Vector<File>();
 		retrieveSources(_eclipseProjectHandle.getLocation().toPortableString()+getJavaProxiesFolder(),proxies);
 		return proxies.toArray(new File[]{});
 	}
 
 	public File[] getComponentJavaSources() {
 		Vector<File> sources=new Vector<File>();
 		retrieveSources(_eclipseProjectHandle.getLocation().toPortableString()+getJavaSourcesFolder(),sources);
 		return sources.toArray(new File[]{});
 	}
 
 	public String getClassOutputFolder() {
 		return "/bin";
 	}
 
 
 	/**
 	 * A getter for all project dependencies (sidl include deps)
 	 * @return a list of all sidl dependencies
 	 * @throws CoreException 
 	 */
 	public String[] getSIDLDependencies() throws CoreException{
 		String deps=_eclipseProjectHandle.getPersistentProperty(new QualifiedName("de.tum.ascodt.plugin", DEPENDENCIES));
 		if(deps!=null){
 			if(deps.contains(","))
 				return deps.split(",");
 			else
 				return new String[]{deps};
 		}		
 		return new String[]{};
 	}
 
 	/**
 	 * adds a new dependency to the project. The add operation has two phases:
 	 * 1. code generation with the sidl compiler
 	 * 2. extending the project persistent properties
 	 * @param dependency the sidl dependency to be compiled
 	 * @throws ASCoDTException 
 	 * @throws CoreException 
 	 */
 	public void addSIDLDependency(String dependency) throws ASCoDTException, CoreException {
 
 		Start startNode= de.tum.ascodt.plugin.project.builders.ProjectBuilder.buildStartSymbolsForSIDLResource(dependency);
 		String err="";
 		if((err=de.tum.ascodt.plugin.project.builders.ProjectBuilder.validateSymbolTableForSIDLResource(startNode,dependency, _symbolTable)).equals(""))
 		{
 			de.tum.ascodt.plugin.project.builders.ProjectBuilder.extendSymbolTable(startNode, _symbolTable, dependency);
 			de.tum.ascodt.plugin.project.builders.ProjectBuilder.generateBlueprints(_eclipseProjectHandle);
 			compileComponents();
 			String oldDependencies=_eclipseProjectHandle.getPersistentProperty(new QualifiedName("de.tum.ascodt.plugin.ASCoDTKernel", DEPENDENCIES));
 
 			if(oldDependencies!=null){
 				_eclipseProjectHandle.setPersistentProperty(new QualifiedName("de.tum.ascodt.plugin.ASCoDTKernel", DEPENDENCIES), oldDependencies+","+dependency);
 			}else
 				_eclipseProjectHandle.setPersistentProperty(new QualifiedName("de.tum.ascodt.plugin.ASCoDTKernel", DEPENDENCIES), dependency);
 		}
 	}
 }

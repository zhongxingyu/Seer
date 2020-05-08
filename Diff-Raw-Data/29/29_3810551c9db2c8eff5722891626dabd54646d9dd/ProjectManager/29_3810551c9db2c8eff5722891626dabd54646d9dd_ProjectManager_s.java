 package org.magnolialang.resources.internal;
 
 import static org.magnolialang.terms.TermFactory.vf;
 
 import java.io.PrintWriter;
 import java.net.URI;
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.runtime.RuntimePlugin;
 import org.magnolialang.compiler.ICompiler;
 import org.magnolialang.errors.ErrorMarkers;
 import org.magnolialang.errors.ImplementationError;
 import org.magnolialang.magnolia.MagnoliaFacts;
 import org.magnolialang.magnolia.MarkerListener;
 import org.magnolialang.resources.*;
 import org.rascalmpl.interpreter.NullRascalMonitor;
 import org.rascalmpl.tasks.Transaction;
 
 public class ProjectManager implements IModuleManager, IManagedResourceListener {
 	private final IResourceManager manager;
 	private Transaction tr;
 	private final Map<IPath, FileLinkFact> resources = new HashMap<IPath, FileLinkFact>();
 	private final Map<String, FileLinkFact> modulesByName = new HashMap<String, FileLinkFact>();
 	private final Map<IPath, String> moduleNamesByPath = new HashMap<IPath, String>();
 	private final Map<ILanguage, ICompiler> compilers = new HashMap<ILanguage, ICompiler>();
 	private final IProject project;
 	private final IPath basePath;
 	private final MarkerListener markerListener;
 	private final List<IManagedResourceListener> listeners = new ArrayList<IManagedResourceListener>();
 	private final static String MODULE_LANG_SEP = "%";
 	private final boolean debug = false;
 
 	public ProjectManager(IResourceManager manager, IProject project,
 			Set<IPath> contents) {
 
 		this.manager = manager;
 		this.project = project;
 		this.basePath = project.getFullPath();
 		this.markerListener = new MarkerListener();
 		manager.addListener(this);
 		initializeTransaction();
 		for(IPath p : contents) {
 			resourceAdded(p);
 		}
 		dataInvariant();
 	}
 
 	private IPath makeProjectRelativePath(IPath path) {
 		return path.makeRelativeTo(basePath);
 	}
 
 	@Override
 	public Transaction getTransaction() {
 		return tr;
 	}
 
 	private void initializeTransaction() {
 		PrintWriter stderr = new PrintWriter(RuntimePlugin.getInstance()
 				.getConsoleStream());
 		if(tr != null)
 			tr.abandon();
 		tr = new Transaction(manager.getTransaction(), stderr, false);
 		tr.registerListener(markerListener, MagnoliaFacts.Type_ErrorMark);
 
 	}
 
 	@Override
 	public IManagedResource find(IPath path) {
 		IManagedResource res = resources.get(path);
 		if(res == null)
 			res = resources.get(project.findMember(path).getFullPath());
 		return res;
 	}
 
 	@Override
 	public IManagedResource find(IProject project, IPath path) {
 		if(project.equals(this.project))
 			return find(path);
 		else
 			return null;
 	}
 
 	@Override
 	public void resourceAdded(IPath path) {
 		IManagedResource resource = manager.find(path);
 		if(resource instanceof IManagedFile
 				&& project.equals(resource.getProject())) {
 
 			if(resources.get(path) != null)
 				resourceRemoved(path);
 
 			addFileResource(path, (IManagedFile) resource);
 			addModuleResource(path, (IManagedFile) resource);
 
 			for(IManagedResourceListener l : listeners)
 				l.resourceAdded(path);
 		}
		dataInvariant();
 	}
 
 	private void addFileResource(IPath path, IManagedFile resource) {
 		if(debug)
 			System.err.println("PROJECT NEW FILE: " + path);
 		FileLinkFact fact = new FileLinkFact(resource, Type_FileResource,
 				vf.string(makeProjectRelativePath(path).toString()));
 		resources.put(resource.getFullPath(), fact);
 		try {
 			tr.setFact(Type_FileResource,
 					vf.string(makeProjectRelativePath(path).toString()), fact);
 		}
 		catch(NullPointerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void addModuleResource(IPath path, IManagedFile resource) {
 		if(debug)
 			System.err.println("PROJECT NEW MODULE: " + path);
 		ILanguage lang = resource.getLanguage();
 		if(lang != null) {
 			String modName = lang.getModuleName(resource.getPath());
 			IConstructor modNameAST = lang.getNameAST(modName);
 			FileLinkFact fact = new FileLinkFact(resource, Type_ModuleResource,
 					modNameAST);
 			resources.put(resource.getFullPath(), fact);
 			modulesByName.put(lang.getId() + MODULE_LANG_SEP + modName, fact);
 			moduleNamesByPath.put(path, lang.getId() + MODULE_LANG_SEP
 					+ modName);
 			tr.setFact(Type_ModuleResource, modNameAST, fact);
 		}
 	}
 
 	@Override
 	public void resourceRemoved(IPath path) {
 		if(debug)
 			System.err.println("PROJECT REMOVED: " + path);
 		FileLinkFact removed = resources.remove(path);
 		if(removed != null) {
 			tr.removeFact(removed);
 
 			for(IManagedResourceListener l : listeners)
 				l.resourceRemoved(path);
 		}
 
 		String modName = moduleNamesByPath.remove(path);
 		if(modName != null) {
 			removed = modulesByName.remove(modName);
 			if(removed != null)
 				tr.removeFact(removed);
 		}
		dataInvariant();
 	}
 
 	@Override
 	public void addListener(IManagedResourceListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public void removeListener(IManagedResourceListener listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void dispose() {
 		// for(IPath path : resources.keySet()) {
 		// resourceRemoved(path);
 		// }
 		try {
 			if(!resources.isEmpty())
 				throw new ImplementationError(
 						"Leftover files in project on shutdown: " + project);
 			if(!moduleNamesByPath.isEmpty())
 				throw new ImplementationError(
 						"Leftover module-name mappings in project on shutdown: "
 								+ project);
 			if(!modulesByName.isEmpty())
 				throw new ImplementationError(
 						"Leftover modules in project on shutdown: " + project);
 		}
 		finally {
 			resources.clear();
 			moduleNamesByPath.clear();
 			modulesByName.clear();
 		}
 		tr.abandon();
 		dataInvariant();
 	}
 
 	public void dataInvariant() {
 		for(IPath p : resources.keySet()) {
 			if(resources.get(p) == null
 					|| !resources.get(p).getFullPath().equals(p)
 					|| !resources.get(p).getProject().equals(project))
 				throw new ImplementationError("Data invariant check failed: "
 						+ " inconsistent resource map entry for  " + p);
 		}
 		for(IPath p : moduleNamesByPath.keySet()) {
 			if(!resources.containsKey(p)
 					|| !modulesByName.containsKey(moduleNamesByPath.get(p))
 					|| !modulesByName.get(moduleNamesByPath.get(p))
 							.getFullPath().equals(p)
 					|| !modulesByName.get(moduleNamesByPath.get(p))
 							.getProject().equals(project))
 				throw new ImplementationError("Data invariant check failed: "
 						+ " inconsistent module resource maps entry for  " + p);
 
 		}
 		if(moduleNamesByPath.size() != modulesByName.size())
 			throw new ImplementationError(
 					"Data invariant check failed: "
 							+ " modulesNamesByPath should be same size as modulesByName");
 		if(moduleNamesByPath.size() > resources.size())
 			throw new ImplementationError("Data invariant check failed: "
 					+ " more modules than resources");
 
 	}
 
 	@Override
 	public ICompiler getCompiler(ILanguage language) {
 		ICompiler compiler = compilers.get(language);
 		if(compiler == null)
 			compiler = language.makeCompiler(this);
 		return compiler;
 	}
 
 	@Override
 	public ICompiler getCompiler(IPath sourceFile) {
 		ILanguage language = LanguageRegistry.getLanguageForFile(sourceFile);
 		ICompiler compiler = compilers.get(language);
 		if(compiler == null)
 			compiler = language.makeCompiler(this);
 		return compiler;
 	}
 
 	@Override
 	public IManagedResource findModule(ILanguage language, String moduleName) {
 		return modulesByName.get(language.getId() + MODULE_LANG_SEP
 				+ moduleName);
 	}
 
 	@Override
 	public IManagedResource findModule(IValue moduleName) {
 		IConstructor res = (IConstructor) tr.getFact(new NullRascalMonitor(),
 				Type_ModuleResource, moduleName);
 		if(res != null)
 			return resources
 					.get(new Path(((IString) res.get("val")).getValue()));
 		else
 			return null;
 	}
 
 	@Override
 	public IConstructor getModuleId(IPath path) {
 		IManagedResource resource = find(path);
 		String modName = resource.getLanguage().getModuleName(
 				resource.getPath());
 		return resource.getLanguage().getNameAST(modName);
 	}
 
 	@Override
 	public String getModuleName(IPath path) {
 		IManagedResource resource = find(path);
 		return resource.getLanguage().getModuleName(resource.getPath());
 	}
 
 	@Override
 	public IPath getPath(URI uri) {
 		if(uri.getScheme().equals("project")) {
 			String projName = uri.getHost();
 			if(projName.equals(project.getName())) {
 				IPath p = project.getFullPath().append(uri.getPath());
 				return p;
 			}
 		}
 		return manager.getPath(uri);
 	}
 
 	@Override
 	public IPath getPath(String path) {
 		IPath p = new Path(path);
 		if(p.isAbsolute())
 			return p;
 		else
 			return project.getFullPath().append(p);
 	}
 
 	@Override
 	public void refresh() {
 		List<IPath> paths = new ArrayList<IPath>(resources.keySet());
 		for(IPath p : paths)
			resourceRemoved(p);
 		dispose();
 		initializeTransaction();
 		for(IPath p : paths) {
			resourceAdded(p);
 		}
 		for(ICompiler c : compilers.values()) {
 			c.refresh();
 		}
 		markerListener.refresh();
 		dataInvariant();
 	}
 
 	@Override
 	public Collection<IPath> allModules(final ILanguage language) {
 		List<IPath> list = new ArrayList<IPath>();
 		for(Entry<IPath, FileLinkFact> entry : resources.entrySet()) {
 			if(entry.getValue().getLanguage().equals(language))
 				list.add(entry.getKey());
 		}
 		return list;
 	}
 
 	@Override
 	public Collection<IPath> allFiles() {
 		return Collections.unmodifiableSet(resources.keySet());
 	}
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc) {
 		addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR,
 				ErrorMarkers.SEVERITY_ERROR_NUMBER);
 	}
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc, int severity) {
 		addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR, severity);
 	}
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc, String markerType) {
 		addMarker(message, loc, markerType, ErrorMarkers.SEVERITY_ERROR_NUMBER);
 	}
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc,
 			String markerType, int severity) {
 		if(loc != null) {
 			URI uri = loc.getURI();
 			IPath path = getPath(uri);
 			FileLinkFact fact = resources.get(path);
 			markerListener.addMarker(message, loc, markerType, severity, fact);
 		}
 		else
 			throw new ImplementationError("Missing location on marker add: "
 					+ message);
 	}
 
 }

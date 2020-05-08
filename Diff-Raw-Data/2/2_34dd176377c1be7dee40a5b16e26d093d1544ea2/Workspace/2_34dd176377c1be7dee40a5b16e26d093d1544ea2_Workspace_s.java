 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.workspace;
 
 import static org.oobium.utils.StringUtils.blank;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.oobium.build.BuildBundle;
 import org.oobium.build.gen.ProjectGenerator;
 import org.oobium.utils.Config;
 import org.oobium.utils.StringUtils;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.Config.OsgiRuntime;
 
 public class Workspace {
 
 	public enum EventType {
 		/**
 		 * An individual bundle has been either added, removed, or refreshed.<br/>
 		 * Added: oldValue == null, and newValue == {@link Bundle}.<br/>
 		 * Removed: oldValue == {@link Bundle}, and newValue == null.<br/>
 		 * Removed: oldValue == {@link Bundle}, and newValue == {@link Bundle}.<br/>
 		 */
 		Bundle,
 		/**
 		 * The bundle repositories have been refreshed, causing all bundles to be recalculated and refreshed.<br/>
 		 * Both oldValue and newValue will be {@link List}s of {@link Bundle} objects.
 		 */
 		Bundles,
 		/**
 		 * The bundle repositories have been set.<br/>
 		 * Both oldValue and newValue will be arrays of of {@link File} objects.
 		 */
 		BundleRepos,
 		/**
 		 * The workspace directory has been set.<br/>
 		 * The oldValue will be either null or a {@link File} representing the old setting.<br/>
 		 * The newValue will be either null or a {@link File} representing the new setting.
 		 */
 		Workspace
 	}
 
 	public static final String BUNDLE_REPOS = "org.oobium.bundle.repos";
 	public static final String JAVA_DIR = "org.oobium.java.dir";
 	public static final String WORKSPACE = "org.oobium.workspace";
 	public static final String RUNTIME = "org.oobium.runtime";
 
 	public static final String RUNTIME_EQUINOX = "equinox";
 	public static final String RUNTIME_FELIX = "felix";
 
 
 	private final ReadWriteLock lock;
 	
 	private Workspace parentWorkspace;
 	private final Map<File, Bundle> bundles;
 	private final Map<File, Application> applications;
 	private File[] bundleRepos;
 	private Set<File> workspaceFiles;
 	private Set<File> bundleRepoFiles;
 	private WorkspaceListener[] listeners;
 	private File dir;
 	private File workingDir;
 	
 	private Bundle buildBundle;
 	private Bundle equinoxRuntimeBundle;
 	
 	private Bundle felixRuntimeBundle;
 	private Bundle knopplerFishRuntimeBundle;
 	
 	// always handy to have a place for spare data :)
 	// follows the style of SWT widgets
 	private Object data;
 
 	
 	public Workspace() {
 		this(null);
 	}
 
 	public Workspace(File workingDirectory) {
 		lock = new ReentrantReadWriteLock();
 		bundles = new HashMap<File, Bundle>();
 		applications = new HashMap<File, Application>();
 		listeners = new WorkspaceListener[0];
 		setWorkingDirectory(workingDirectory);
 	}
 
 	private Bundle addBundle(File file) {
 		try {
 			File cfile = file.getCanonicalFile();
 			Bundle bundle = Bundle.create(cfile);
 			if(bundle != null) {
 				bundles.put(cfile, bundle);
 				if(bundle instanceof Application) {
 					applications.put(cfile, (Application) bundle);
 				}
 				if(bundle.name.equals(BuildBundle.ID)) {
 					buildBundle = bundle;
 				} else if(bundle.name.equals("org.eclipse.osgi")) {
 					equinoxRuntimeBundle = bundle;
 				} else if(bundle.name.equals("org.apache.felix.main")) {
 					felixRuntimeBundle = bundle;
 				}
 				return bundle;
 			}
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void addListener(WorkspaceListener listener) {
 		synchronized(listeners) {
 			listeners = Arrays.copyOf(listeners, listeners.length + 1);
 			listeners[listeners.length-1] = listener;
 		}
 	}
 	
 	public Application createApplication(File file, Map<String, String> properties) {
 		if(file != null) {
 			return (Application) loadBundle(ProjectGenerator.createApplication(file, properties));
 		}
 		return null;
 	}
 	
 	public Migrator createMigrator(Module module) {
 		if(module != null) {
 			Set<Bundle> dependencies = module.getDependencies(this);
 			return (Migrator) loadBundle(ProjectGenerator.createMigrator(module, dependencies));
 		}
 		return null;
 	}
 	
 	public Module createModule(File file, Map<String, String> properties) {
 		if(file != null) {
 			return (Module) loadBundle(ProjectGenerator.createModule(file, properties));
 		}
 		return null;
 	}
 
 	public TestSuite createTestSuite(Module module, Map<String, String> properties) {
 		if(module != null) {
 			return (TestSuite) loadBundle(ProjectGenerator.createTests(module, properties));
 		}
 		return null;
 	}
 	
 	public Module createWebservice(File file, Map<String, String> properties) {
 		if(file != null) {
 			return (Module) loadBundle(ProjectGenerator.createWebservice(file, properties));
 		}
 		return null;
 	}
 	
 	void fireEvent(EventType eventType, Object oldValue, Object newValue) {
 		WorkspaceEvent event = new WorkspaceEvent(eventType, oldValue, newValue);
 		for(WorkspaceListener listener : Arrays.copyOf(this.listeners, this.listeners.length)) {
 			listener.handleEvent(event);
 		}
 	}
 	
 	public Application getApplication(File file) {
 		if(file == null) {
 			return null;
 		}
 		
 		lock.readLock().lock();
 		try {
 			Application app = applications.get(file);
 			if(app == null && parentWorkspace != null) {
 				return parentWorkspace.getApplication(file);
 			}
 			return app;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Application getApplication(String name) {
 		if(name == null || name.length() == 0) {
 			return null;
 		}
 
 		lock.readLock().lock();
 		try {
 			for(Application application : applications.values()) {
 				if(application.name.equals(name)) {
 					return application;
 				}
 			}
 			if(parentWorkspace != null) {
 				return parentWorkspace.getApplication(name);
 			}
 			return null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Application[] getApplications() {
 		lock.readLock().lock();
 		try {
 			Set<Application> apps = new HashSet<Application>(applications.values());
 			return apps.toArray(new Application[apps.size()]);
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Bundle getBuildBundle() {
 		if(buildBundle != null) {
 			return buildBundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getBuildBundle();
 		}
 		return null;
 	}
 	
 	public Bundle getBundle(File file) {
 		if(file == null) {
 			return null;
 		}
 		
 		lock.readLock().lock();
 		try {
 			Bundle bundle = bundles.get(file);
 			if(bundle == null && parentWorkspace != null) {
 				return parentWorkspace.getBundle(file);
 			}
 			return bundle;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Bundle getBundle(ImportedPackage importedPackage) {
 		lock.readLock().lock();
 		try {
 			for(Bundle bundle : bundles.values()) {
 				if(bundle.resolves(importedPackage)) {
 					return bundle;
 				}
 			}
 			if(parentWorkspace != null) {
 				return parentWorkspace.getBundle(importedPackage);
 			}
 			return null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Bundle getBundle(OsgiRuntime runtime) {
 		Bundle bundle = null;
 		switch(runtime) {
 		case Equinox:		bundle = equinoxRuntimeBundle; break;
 		case Felix:			bundle = felixRuntimeBundle; break;
 		case Knopplerfish:	bundle = knopplerFishRuntimeBundle; break;
 		default:			throw new IllegalArgumentException("unsupported runtime: " + runtime);
 		}
 		if(bundle == null && parentWorkspace != null) {
 			return parentWorkspace.getBundle(runtime);
 		}
 		return bundle;
 	}
 	
 	public Bundle getBundle(RequiredBundle requiredBundle) {
 		lock.readLock().lock();
 		try {
 			for(Bundle bundle : bundles.values()) {
 				if(bundle.resolves(requiredBundle)) {
 					return bundle;
 				}
 			}
 			if(parentWorkspace != null) {
 				return parentWorkspace.getBundle(requiredBundle);
 			}
 			return null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	/**
 	 * Get the first bundle that matches the given full name (the name and version range).
 	 * If the fullName does not include a version range, than a version range of [*, *] is used.
 	 * @param fullName
 	 * @return the matching {@link Bundle} object
 	 */
 	public Bundle getBundle(String fullName) {
 		if(blank(fullName)) {
 			return null;
 		}
 
 		int ix = fullName.lastIndexOf('_');
 		String name = (ix == -1) ? fullName : fullName.substring(0, ix);
 		VersionRange range = (ix == -1) ? null : new VersionRange(fullName.substring(ix+1));
 
 		lock.readLock().lock();
 		try {
 			for(Bundle bundle : bundles.values()) {
 				if(bundle.name.equals(name) && bundle.version.resolves(range)) {
 					return bundle;
 				}
 			}
 			if(parentWorkspace != null) {
 				return parentWorkspace.getBundle(fullName);
 			}
 			return null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	public Bundle getBundle(String name, String version) {
 		return getBundle(name, new Version(version));
 	}
 
 	/**
 	 * Get the bundle that matches the given name and exact version.
 	 * @param name the name of the bundle
 	 * @param version the exact version of the bundle
 	 * @return the matching {@link Bundle} object if found; null otherwise (also returns null
 	 * if name is empty or version is null)
 	 */
 	public Bundle getBundle(String name, Version version) {
 		if(blank(name) || version == null) {
 			return null;
 		}
 
 		lock.readLock().lock();
 		try {
 			for(Bundle bundle : bundles.values()) {
 				if(bundle.name.equals(name) && bundle.version.equals(version)) {
 					return bundle;
 				}
 			}
 			if(parentWorkspace != null) {
 				return parentWorkspace.getBundle(name);
 			}
 			return null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	/**
 	 * Get the bundle repositories that this workspace is currently using.
 	 * The returned array is a copy; modifications to it will not affect the workspace.
 	 * @return an array of File objects; never null
 	 */
 	public File[] getBundleRepositories() {
 		if(bundleRepos != null) {
 			return Arrays.copyOf(bundleRepos, bundleRepos.length);
 		}
 		return new File[0];
 	}
 
 	public Bundle[] getBundles() {
 		lock.readLock().lock();
 		try {
 			Set<Bundle> bundles = new HashSet<Bundle>(this.bundles.values());
 			return bundles.toArray(new Bundle[bundles.size()]);
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Get all the bundles that match the given full name (the name and version range).
 	 * If the fullName does not include a version range, than a version range of [*, *] is used.<br/>
 	 * Does NOT include bundles from the parent workspace.
 	 * @param fullName
 	 * @return the matching {@link Bundle} object
 	 */
 	public Bundle[] getBundles(String fullName) {
 		if(blank(fullName)) {
 			return null;
 		}
 
 		int ix = fullName.lastIndexOf('_');
 		String name = (ix == -1) ? fullName : fullName.substring(0, ix);
 		VersionRange range = (ix == -1) ? null : new VersionRange(fullName.substring(ix+1));
 
 		lock.readLock().lock();
 		try {
 			List<Bundle> list = new ArrayList<Bundle>();
 			for(Bundle bundle : bundles.values()) {
 				if(bundle.name.equals(name) && bundle.version.resolves(range)) {
 					list.add(bundle);
 				}
 			}
 			return list.toArray(new Bundle[list.size()]);
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Object getData() {
 		return data;
 	}
 	
 	public Object getData(String key) {
 		return (data instanceof Map<?,?>) ? ((Map<?,?>) data).get(key) : null;
 	}
 	
 	public File getDirectory() {
 		lock.readLock().lock();
 		try {
 			return dir;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public Migrator getMigrator(File file) {
 		Bundle bundle = getBundle(file);
 		if(bundle instanceof Migrator) {
 			return (Migrator) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getMigrator(file);
 		}
 		return null;
 	}
 	
 	public Migrator getMigrator(String name) {
 		Bundle bundle = getBundle(name);
 		if(bundle instanceof Migrator) {
 			return (Migrator) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getMigrator(name);
 		}
 		return null;
 	}
 	
 	public Migrator getMigratorFor(Module module) {
 		return getMigrator(new File(module.file.getAbsoluteFile() + ".migrator"));
 	}
 	
 	public Module getModule(File file) {
 		Bundle bundle = getBundle(file);
 		if(bundle instanceof Module) {
 			return (Module) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getModule(file);
 		}
 		return null;
 	}
 
 	public Module getModule(String fullName) {
 		Bundle bundle = getBundle(fullName);
 		if(bundle instanceof Module) {
 			return (Module) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getModule(fullName);
 		}
 		return null;
 	}
 	
 	public Module[] getModules() {
 		lock.readLock().lock();
 		try {
 			Set<Module> modules = new HashSet<Module>();
 			for(Bundle bundle : bundles.values()) {
 				if(bundle instanceof Module) {
 					modules.add((Module) bundle);
 				}
 			}
 			return modules.toArray(new Module[modules.size()]);
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	private File[] getOpenProjects() {
 		File dir = new File(this.dir, StringUtils.join(File.separatorChar, ".metadata", ".plugins", "org.eclipse.core.resources", ".projects"));
 		if(dir.isDirectory()) {
 			String[] names = dir.list();
 			List<File> projectFiles = new ArrayList<File>();
 			for(String name : names) {
 				File project = new File(this.dir, name);
 				if(project.exists()) {
 					projectFiles.add(project);
 				}
 			}
 			return projectFiles.toArray(new File[projectFiles.size()]);
 		} else if(this.dir.isDirectory()) {
 			return this.dir.listFiles();
 		} else {
 			return new File[0];
 		}
 	}
 	
 	public Workspace getParent() {
 		return parentWorkspace;
 	}
 	
 	private File[] getProjects(File repo) {
 		if(repo.isDirectory()) {
 			File[] files = repo.listFiles(new FileFilter() {
 				@Override
 				public boolean accept(File file) {
 					if(file.isFile() && file.getName().endsWith(".jar")) {
 						return true;
 					}
 					if(file.isDirectory()) {
 						return new File(file, "META-INF" + File.separator + "MANIFEST.MF").exists();
 					}
 					return false;
 				}
 			});
 			return files;
 		}
 		return new File[0];
 	}
 	
 	public TestSuite getTestSuite(File file) {
 		Bundle bundle = getBundle(file);
 		if(bundle instanceof TestSuite) {
 			return (TestSuite) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getTestSuite(file);
 		}
 		return null;
 	}
 	
 	public TestSuite getTestSuite(String name) {
 		Bundle bundle = getBundle(name);
 		if(bundle instanceof TestSuite) {
 			return (TestSuite) bundle;
 		}
 		if(parentWorkspace != null) {
 			return parentWorkspace.getTestSuite(name);
 		}
 		return null;
 	}
 
 	public Migrator getTestSuiteFor(Module module) {
 		return getMigrator(new File(module.file.getAbsoluteFile() + ".tests"));
 	}
 	
 	/**
 	 * Get the location of the working directory for this workspace.
 	 * The working directory is a location that the system has write access
 	 * to and can be used for building and exporting bundles.
 	 * It is often the same as the workspace directory, but this is not required.
 	 */
 	public File getWorkingDirectory() {
 		lock.readLock().lock();
 		try {
 			return (workingDir != null) ? workingDir : dir;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	public boolean isSet() {
 		lock.readLock().lock();
 		try {
 			return dir != null;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	public Bundle loadBundle(File file) {
 		if(file == null || !file.exists()) {
 			return null;
 		}
 		
 		lock.writeLock().lock();
 		try {
 			Bundle bundle = bundles.get(file);
 			if(bundle == null) {
 				bundle = addBundle(file);
 				if(bundle != null) {
 					File parent = file.getParentFile();
 					if(parent.equals(dir)) {
 						workspaceFiles.add(file);
 					} else if(bundleRepos != null) {
 						for(File repo : bundleRepos) {
 							if(parent.equals(repo)) {
 								bundleRepoFiles.add(file);
 								break;
 							}
 						}
 					}
 				}
 			}
 			return bundle;
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	public void refresh() {
 		List<Bundle> added = new ArrayList<Bundle>();
 		List<Bundle> removed = new ArrayList<Bundle>();
 		lock.writeLock().lock();
 		try {
 			File[] files = bundles.keySet().toArray(new File[bundles.size()]);
 			for(File file : files) {
 				if(!file.exists()) {
 					removed.add(removeBundle(file));
 				}
 			}
 
 			if(dir.isDirectory()) {
 				files = getOpenProjects();
 				for(File file : files) {
 					if(!bundles.containsKey(file)) {
 						workspaceFiles.add(file);
 						added.add(addBundle(file));
 					}
 				}
 			}
 			if(bundleRepos != null) {
 				for(File repo : bundleRepos) {
 					files = getProjects(repo);
 					for(File file : files) {
 						if(!bundles.containsKey(file)) {
 							bundleRepoFiles.add(file);
 							added.add(addBundle(file));
 						}
 					}
 				}
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 
 		if(!added.isEmpty() || !removed.isEmpty()) {
 			fireEvent(EventType.Bundles, removed.toArray(), added.toArray());
 		}
 	}
 	
 	public void refresh(Bundle bundle) {
 		refresh(bundle.file);
 	}
 	
 	public void refresh(File file) {
 		Bundle oldBundle;
 		Bundle newBundle;
 		lock.writeLock().lock();
 		try {
 			oldBundle = removeBundle(file);
 			newBundle = loadBundle(file);
 		} finally {
 			lock.writeLock().unlock();
 		}
 
 		fireEvent(EventType.Bundle, oldBundle, newBundle);
 	}
 
 	public void remove(Bundle bundle) {
 		remove(bundle.file);
 	}
 	
 	public void remove(File file) {
 		Bundle oldBundle;
 		lock.writeLock().lock();
 		try {
 			oldBundle = removeBundle(file);
 			if(oldBundle != null) {
 				fireEvent(EventType.Bundle, oldBundle, null);
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	private Bundle removeBundle(File file) {
 		if(bundleRepoFiles != null) bundleRepoFiles.remove(file);
 		if(workspaceFiles != null) workspaceFiles.remove(file);
 		if(applications != null) applications.remove(file);
 		if(bundles != null) return bundles.remove(file);
 		return null;
 	}
 	
 	public void removeListener(WorkspaceListener listener) {
 		synchronized(listeners) {
 			for(int i = 0; i < listeners.length; i++) {
 				if(listeners[i] == listener) {
 					WorkspaceListener[] tmp = new WorkspaceListener[listeners.length-1];
 					System.arraycopy(listeners, 0, tmp, 0, i);
 					System.arraycopy(listeners, i+1, tmp, i, listeners.length-1-i);
 					listeners = tmp;
 					break;
 				}
 			}
 		}
 	}
 
 	public void removeRuntimeBundle(Collection<Bundle> bundles) {
 		Bundle bundle = getBundle(OsgiRuntime.Equinox);
 		if(bundle != null) {
 			bundles.remove(bundle);
 		}
 		bundle = getBundle(OsgiRuntime.Felix);
 		if(bundle != null) {
 			bundles.remove(bundle);
 		}
 		bundle = getBundle(OsgiRuntime.Knopplerfish);
 		if(bundle != null) {
 			bundles.remove(bundle);
 		}
 	}
 	
 	public void setBundleRepositories(File...repos) {
 		lock.writeLock().lock();
 		try {
 			if(bundleRepoFiles != null) {
 				for(File bundleRepoFile : bundleRepoFiles) {
 					removeBundle(bundleRepoFile);
 				}
 				bundleRepoFiles.clear();
 			}
 			
 			File[] oldValue = (bundleRepos == null) ? new File[0] : bundleRepos;
 			bundleRepos = Arrays.copyOf(repos, repos.length);
 			bundleRepoFiles = new HashSet<File>();
 			
 			for(File repo : bundleRepos) {
 				bundleRepoFiles.addAll(Arrays.asList(getProjects(repo)));
 			}
 			for(File bundleRepoFile : bundleRepoFiles) {
 //				System.out.println(bundleRepoFile.getName());
 				addBundle(bundleRepoFile);
 			}
 			fireEvent(EventType.BundleRepos, oldValue, bundleRepos);
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	/**
 	 * @param commaSeparatedRepos a comma separated list of absolute file paths
 	 */
 	public void setBundleRepositories(String commaSeparatedRepos) {
		String[] sa = commaSeparatedRepos.split("\\s*;\\s*");
 		File[] repos = new File[sa.length];
 		for(int i = 0; i < sa.length; i++) {
 			repos[i] = new File(sa[i]);
 			if(!repos[i].isAbsolute()) {
 				throw new IllegalArgumentException("only absolute file paths are permitted (" + commaSeparatedRepos + ")");
 			}
 		}
 		setBundleRepositories(repos);
 	}
 	
 	public void setData(Object data) {
 		this.data = data;
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void setData(String key, Object value) {
 		if(data instanceof Map) {
 			((Map) data).put(key, value);
 		} else {
 			Map map = new HashMap();
 			map.put(key, value);
 			data = map;
 		}
 	}
 
 	public void setDirectory(File dir) {
 		setDirectory(dir, false);
 	}
 	
 	public void setDirectory(File dir, boolean force) {
 		if(!force && dir != null && dir.equals(this.dir)) {
 			return;
 		}
 		
 		lock.writeLock().lock();
 		try {
 			if(workspaceFiles != null) {
 				for(File workspaceFile : workspaceFiles.toArray(new File[workspaceFiles.size()])) {
 					removeBundle(workspaceFile);
 				}
 				workspaceFiles.clear();
 			}
 
 			File oldValue = this.dir;
 			this.dir = dir;
 			workspaceFiles = new HashSet<File>();
 
 			if(this.dir != null) {
 				if(!this.dir.exists()) {
 					this.dir.mkdirs();
 				} else if(this.dir.isDirectory()) {
 					workspaceFiles.addAll(Arrays.asList(getOpenProjects()));
 					for(File workspaceFile : workspaceFiles) {
 						addBundle(workspaceFile);
 					}
 				} else {
 					throw new IllegalArgumentException("workspace must be a directory");
 				}
 			}
 			
 			fireEvent(EventType.Workspace, oldValue, this.dir);
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	public void setDirectory(String absolutePath) {
 		setDirectory(new File(absolutePath));
 	}
 
 	public void setDirectory(String absolutePath, boolean force) {
 		setDirectory(new File(absolutePath), force);
 	}
 
 	public void setParent(Workspace workspace) {
 		this.parentWorkspace = workspace;
 	}
 
 	public void setRuntimeBundle(Config configuration, Mode mode, Collection<Bundle> bundles) {
 		setRuntimeBundle(configuration.getRuntime(mode), bundles);
 	}
 	
 	public void setRuntimeBundle(OsgiRuntime runtime, Collection<Bundle> bundles) {
 		Bundle equinox = getBundle(OsgiRuntime.Equinox);
 		Bundle felix = getBundle(OsgiRuntime.Felix);
 		Bundle kfish = getBundle(OsgiRuntime.Knopplerfish);
 		switch(runtime) {
 		case Equinox:
 			throw new UnsupportedOperationException("TODO: Equinox Runtime is not yet implemented");
 		case Felix:
 			if(felix == null) {
 				throw new IllegalStateException("unresolved runtime: " + OsgiRuntime.Felix);
 			} else {
 				bundles.add(felix);
 				if(equinox != null || kfish != null) {
 					for(Iterator<Bundle> iter = bundles.iterator(); iter.hasNext(); ) {
 						Bundle bundle = iter.next();
 						if(bundle == equinox) {
 							iter.remove();
 							equinox = null;
 							if(kfish == null) break;
 						} else if(bundle == kfish) {
 							iter.remove();
 							kfish = null;
 							if(equinox == null) break;
 						}
 					}
 				}
 			}
 			break;
 		case Knopplerfish:
 			throw new UnsupportedOperationException("TODO: Knopplerfish Runtime is not yet implemented");
 		}
 	}
 
 	/**
 	 * Set the location of the working directory for this workspace.
 	 * The working directory is a location that the system has write access
 	 * to and can be used for building and exporting bundles.
 	 * It is often the same as the workspace directory, but this is not required.
 	 * @param dir the working directory (will be created if it doesn't exist)
 	 */
 	public void setWorkingDirectory(File dir) {
 		if(dir != null && !dir.exists()) {
 			dir.mkdirs();
 		}
 		this.workingDir = dir;
 	}
 	
 }

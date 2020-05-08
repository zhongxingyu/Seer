 package wyclipse;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.launching.JREContainer;
 
 import wyc.builder.WhileyBuilder;
 import wyc.lang.WhileyFile;
 import wyclipse.builder.IContainerRoot;
 import wyclipse.builder.IContainerRoot.IFileEntry;
 import wycore.lang.*;
 import wycore.util.JarFileRoot;
 import wycore.util.StandardBuildRule;
 import wycore.util.Trie;
 import wyil.Pipeline;
 import wyil.lang.WyilFile;
 
 /**
  * <p>
  * A WhileyProject is responsible for managing resources in the system which are
  * directly or indirectly relevant to compiling Whiley files. For example,
  * source folders containing whiley files are (obviously) directly relevant;
  * however, other containers may be relevant (e.g. if they hold jar files on the
  * whileypath).
  * </p>
  * 
  * <p>
  * The key issue is that when a resource changes which is relevant to the
  * builder (e.g. a Whiley file, etc) then we need to update the namespace to
  * reflect that change.
  * </p>
  * 
  * @author David J. Pearce
  * 
  */
 public class WhileyProject implements NameSpace {
 
 	/**
 	 * The roots of all external entries known to the builder. This includes all
 	 * external archives (e.g. jars), as well as the standard library.
 	 */
 	protected final ArrayList<Path.Root> externalRoots;
 
 	/**
 	 * The roots of all binary entries known to the builder. This is essentially
 	 * the list of all output directories.
 	 */
 	protected final ArrayList<IContainerRoot> binaryRoots;
 
 	/**
 	 * The roots of all source entries known to the builder. From this pool of
 	 * resources, the set of files needing recompilation is determined.
 	 */
 	protected final ArrayList<IContainerRoot> sourceRoots;
 
 	/**
 	 * The delta is a list of entries which require recompilation. As entries
 	 * are changed, they may be added to this list (e.g. Whiley). Entries which
 	 * depend upon them may also be added. Or, if they represent e.g. binary
 	 * dependents (e.g. jar files) then this may force a total recompilation.
 	 */
 	protected final ArrayList<IFileEntry> delta;
 
 	/**
 	 * <p>
 	 * The whiley builder is responsible for actually compiling whiley files
 	 * into binary files (e.g. class files). The builder operates using a given
 	 * set of build rules which determine what whiley files are compiled, what
 	 * their target types are and where their binaries should be written.
 	 * </p>
 	 */
 	private WhileyBuilder builder;
 
 	/**
 	 * The build rules identify how source files are converted into binary
 	 * files. In particular, they determine which whiley files are compiled,
 	 * what their target types are and where their binaries should be written.
 	 */
 	protected final ArrayList<BuildRule> rules;
 
 	/**
 	 * Construct a build manager from a given IJavaProject. This will traverse
 	 * the projects raw classpath to identify all classpath entries, such as
 	 * source folders and jar files. These will then be managed by this build
 	 * manager.
 	 * 
 	 * @param project
 	 */
 	public WhileyProject(IWorkspace workspace, IJavaProject javaProject)
 			throws CoreException {
 
 		externalRoots = new ArrayList<Path.Root>();
 		binaryRoots = new ArrayList<IContainerRoot>();
 		sourceRoots = new ArrayList<IContainerRoot>();
 		this.delta = new ArrayList<IFileEntry>();
 		this.rules = new ArrayList<BuildRule>();
 		
 		IWorkspaceRoot workspaceRoot = workspace.getRoot();
 		
 		// ===============================================================
 		// First, initialise roots
 		// ===============================================================
 
 		IContainer defaultOutputDirectory = workspaceRoot.getFolder(javaProject
 				.getOutputLocation());
 
 		IContainerRoot outputRoot = null;
 		if (defaultOutputDirectory != null) {
 			// we have a default output directory, so make sure to include it!
 			outputRoot = new IContainerRoot(defaultOutputDirectory, registry);
 			binaryRoots.add(outputRoot);
 		} else {
 			throw new RuntimeException(
 					"Whiley Plugin currently unable to handle projects without default output folder");
 		}
 
 		initialise(workspace.getRoot(), javaProject, javaProject.getRawClasspath());
 
 		// ===============================================================
 		// Second, initialise builder + rules
 		// ===============================================================
 
 		Pipeline pipeline = new Pipeline(Pipeline.defaultPipeline);
 		this.builder = new WhileyBuilder(this, pipeline);
 		Content.Filter<WhileyFile> includes = Content.filter(
 				Trie.fromString("**"), WhileyFile.ContentType);
 		StandardBuildRule rule = new StandardBuildRule(builder);
 
 		for (Path.Root source : sourceRoots) {
 			if (outputRoot != null) {
 				rule.add(source, includes, outputRoot, WyilFile.ContentType);
 			} else {
 				// default backup
 				rule.add(source, includes, source, WyilFile.ContentType);
 			}
 		}
 
 		rules.add(rule);		
 	}
 
 	private void initialise(IWorkspaceRoot workspaceRoot,
 			IJavaProject javaProject, IClasspathEntry[] entries)
 			throws CoreException {
 		for (IClasspathEntry e : entries) {
 			switch (e.getEntryKind()) {
 				case IClasspathEntry.CPE_LIBRARY : {
 					IPath location = e.getPath();							
 					// IFile file = workspaceRoot.getFile(location);
 										
 					try {
 						externalRoots.add(new JarFileRoot(
 								location.toOSString(), registry));
 					} catch (IOException ex) {
 						// ignore entries which don't exist
 					}
 					break;
 				}
 				case IClasspathEntry.CPE_SOURCE : {
 					IPath location = e.getPath();
 					IFolder folder = workspaceRoot.getFolder(location);
 					sourceRoots.add(new IContainerRoot(folder, registry));
 					break;
 				}
 				case IClasspathEntry.CPE_CONTAINER :
 					IPath location = e.getPath();									
 					IClasspathContainer container = JavaCore
 							.getClasspathContainer(location, javaProject);
 					if (container instanceof JREContainer) {
 						// Ignore JRE container
 					} else if (container != null) {
 						// Now, recursively add paths
 						initialise(workspaceRoot, javaProject,
 								container.getClasspathEntries());						
 					}
 					break;
 			}
 		}
 	}
 
 	public void setLogger(Logger logger) {
 		builder.setLogger(logger);
 	}
 	
 	// ======================================================================
 	// Accessors
 	// ======================================================================
 
 	public boolean exists(Path.ID id, Content.Type<?> ct) throws Exception {
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			if (sourceRoots.get(i).exists(id, ct)) {
 				return true;
 			}
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			if (binaryRoots.get(i).exists(id, ct)) {
 				return true;
 			}
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			if (externalRoots.get(i).exists(id, ct)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public <T> Path.Entry<T> get(Path.ID id, Content.Type<T> ct)
 			throws Exception {
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			Path.Entry<T> e = sourceRoots.get(i).get(id, ct);
 			if (e != null) {
 				return e;
 			}
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			Path.Entry<T> e = binaryRoots.get(i).get(id, ct);
 			if (e != null) {
 				return e;
 			}
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			Path.Entry<T> e = externalRoots.get(i).get(id, ct);
 			if (e != null) {
 				return e;
 			}
 		}
 		return null;
 	}
 
 	public <T> ArrayList<Path.Entry<T>> get(Content.Filter<T> filter)
 			throws Exception {
 		ArrayList<Path.Entry<T>> r = new ArrayList<Path.Entry<T>>();
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			r.addAll(sourceRoots.get(i).get(filter));
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			r.addAll(binaryRoots.get(i).get(filter));
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			r.addAll(externalRoots.get(i).get(filter));
 		}
 		return r;
 	}
 
 	public <T> HashSet<Path.ID> match(Content.Filter<T> filter)
 			throws Exception {
 		HashSet<Path.ID> r = new HashSet<Path.ID>();
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			r.addAll(sourceRoots.get(i).match(filter));
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			r.addAll(binaryRoots.get(i).match(filter));
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			r.addAll(externalRoots.get(i).match(filter));
 		}
 		return r;
 	}
 
 	// ======================================================================
 	// Mutators
 	// ======================================================================
 
 	public void flush() throws Exception {
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			sourceRoots.get(i).flush();
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			binaryRoots.get(i).flush();
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			externalRoots.get(i).flush();
 		}
 	}
 
 	public void refresh() throws Exception {
 		for (int i = 0; i != sourceRoots.size(); ++i) {
 			sourceRoots.get(i).refresh();
 		}
 		for (int i = 0; i != binaryRoots.size(); ++i) {
 			binaryRoots.get(i).refresh();
 		}
 		for (int i = 0; i != externalRoots.size(); ++i) {
 			externalRoots.get(i).refresh();
 		}
 	}
 
 	/**
 	 * A resource of some sort has changed, and we need to update the namespace
 	 * accordingly. Note that the given resource may not actually be managed by
 	 * this namespace manager --- in which case it can be safely ignored.
 	 * 
 	 * @param delta
 	 */
	public void changed(IResource resource) throws CoreException {		
 		for(IContainerRoot srcRoot : sourceRoots) {
 			IFileEntry<?> ife = srcRoot.getResource(resource);
 			if(ife != null) {
 				// Ok, this file is managed by a source root; therefore, mark it
 				// for recompilation. Note that we must refresh the entry as
 				// well, since it has clearly changed.
 				ife.refresh();
 				delta.add(ife);
 			}
 		}
 	}
 
 	/**
 	 * A resource of some sort has been created, and we need to update the
 	 * namespace accordingly. Note that the given resource may not actually be
 	 * managed by this namespace manager --- in which case it can be safely
 	 * ignored.
 	 * 
 	 * @param delta
 	 */
 	public void added(IResource resource) throws CoreException {
 		IPath location = resource.getLocation();		
 		for(IContainerRoot srcRoot : sourceRoots) {
 			IFileEntry e = srcRoot.create(resource);
 			if(e != null) {
 				delta.add(e);
 				return; // done
 			}
 		}		
 		
 		// otherwise, what is this file that we've added??
 	}
 
 	/**
 	 * A resource of some sort has been removed, and we need to update the
 	 * namespace accordingly. Note that the given resource may not actually be
 	 * managed by this namespace manager --- in which case it can be safely
 	 * ignored.
 	 * 
 	 * @param delta
 	 */
 	public void removed(IResource resource) throws CoreException {
 		// We could actually do better here, in some cases. For example, if a
 		// source file is removed then we only need to recompile those which
 		// depend upon it. 
 		for(IContainerRoot srcRoot : sourceRoots) {
 			srcRoot.refresh();
 		}
 		for(IContainerRoot binaryRoot : binaryRoots) {
 			binaryRoot.refresh();
 		}
 		
 		// FIXME: need to deal with externals here as well.
 		
 		clean();
 	}
 
 	/**
 	 * Delete all entries and corresponding IFiles from all binary roots. That
 	 * is, delete all output files. An immediate consequence of this is that all
 	 * known source files are marked for recompilation. However, these files are
 	 * not actually recompiled until build() is called.
 	 */
 	public void clean() throws CoreException {
 		HashSet<Path.Entry<?>> allTargets = new HashSet();
 		try {
 			delta.clear();
 			
 			// first, identify all source files
			
 			for(IContainerRoot srcRoot : sourceRoots) {
 				for(IFileEntry<?> e : srcRoot.contents()) {
 					delta.add(e);
 				}
 			}
 			
 			// second, determine all target files
			
 			for (BuildRule r : rules) {
 				for (IFileEntry<?> source : delta) {
 					allTargets.addAll(r.dependentsOf(source));
 				}
 			}
 			
 			// third, delete all target files
 			for(Path.Entry<?> _e : allTargets) {
 				IFileEntry<?> e = (IFileEntry<?>) _e;
 				//e.delete();
 			}		
 		} catch(CoreException e) {
 			throw e;
 		} catch(RuntimeException e) {
 			throw e;
 		} catch(Exception e) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root. 
 		}
 	}
 
 	/**
 	 * Build those source files which are known to have changed, and their
 	 * dependencies.
 	 */
 	public void build() throws CoreException {		
 		HashSet<Path.Entry<?>> allTargets = new HashSet();
		try {
 			// Firstly, initialise list of targets to rebuild.		
 			for (BuildRule r : rules) {
 				for (IFileEntry<?> source : delta) {
 					allTargets.addAll(r.dependentsOf(source));
 				}
 			}

 			// Secondly, add all dependents on those being rebuilt.
 			int oldSize;
 			do {
 				oldSize = allTargets.size();
 				for (BuildRule r : rules) {
 					for (Path.Entry<?> target : allTargets) {
 						allTargets.addAll(r.dependentsOf(target));
 					}
 				}
 			} while (allTargets.size() != oldSize);
 
 			// Thirdly, remove all markers from those entries
 			for(Path.Entry<?> _e : delta) {
 				IFileEntry e = (IFileEntry) _e;
 				e.getFile().deleteMarkers(IMarker.PROBLEM, true,
 						IResource.DEPTH_INFINITE);
 			}
 			
 			// Finally, build all identified targets!		
 			do {
 				oldSize = allTargets.size();
 				for(BuildRule r : rules) {
 					r.apply(allTargets);
 				}
 			} while(allTargets.size() < oldSize);
 			
 		} catch(CoreException e) {
 			throw e;
 		} catch(SyntaxError e) {
 			// FIXME: this is a hack because syntax error doesn't retain the
 			// correct information.
 			for(IContainerRoot srcRoot : sourceRoots) {
 				for(IFileEntry entry : srcRoot.contents()) {
 					IFile file = entry.getFile();
 					if(file.getLocation().toFile().getAbsolutePath().equals(e.filename())) {
 						// hit
 						highlightSyntaxError(file,e);
 						break;
 					}
 				}
 			}
 		} catch(RuntimeException e) {
 			throw e;
 		} catch(Exception e) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root. 
 		}
 		
 		delta.clear();
 	}
 
 	/**
 	 * Build all known source files, regardless of whether they have changed or
 	 * not.
 	 */
 	public void buildAll() throws CoreException {		
 		delta.clear();
 		for(IContainerRoot srcRoot : sourceRoots) {
 			for(IFileEntry<?> e : srcRoot.contents()) {
 				delta.add(e);
 			}
 		}
 		build();
 	}
 
 	
 	protected void highlightSyntaxError(IResource resource, SyntaxError err)
 			throws CoreException {
 		IMarker m = resource.createMarker("wyclipse.whileymarker");
 		m.setAttribute(IMarker.CHAR_START, err.start());
 		m.setAttribute(IMarker.CHAR_END, err.end() + 1);
 		m.setAttribute(IMarker.MESSAGE, err.msg());
 		m.setAttribute(IMarker.LOCATION, "Whiley File");
 		m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
 		m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);			
 	}	
 	
 	/**
 	 * The master project content type registry.
 	 */
 	public static final Content.Registry registry = new Content.Registry() {
 
 		public void associate(Path.Entry e) {
 			if (e.suffix().equals("whiley")) {
 				e.associate(WhileyFile.ContentType, null);
 			} else if (e.suffix().equals("class")) {
 				// this could be either a normal JVM class, or a Wyil class. We
 				// need to determine which.
 				try {
 					WyilFile c = WyilFile.ContentType.read(e, e.inputStream());
 					if (c != null) {
 						e.associate(WyilFile.ContentType, c);
 					}
 				} catch (Exception ex) {
 					// hmmm, not ideal
 				}
 			}
 		}
 
 		public String suffix(Content.Type<?> t) {
 			if (t == WhileyFile.ContentType) {
 				return "whiley";
 			} else if (t == WyilFile.ContentType) {
 				return "class";
 			} else {
 				return "dat";
 			}
 		}
 	};
 }

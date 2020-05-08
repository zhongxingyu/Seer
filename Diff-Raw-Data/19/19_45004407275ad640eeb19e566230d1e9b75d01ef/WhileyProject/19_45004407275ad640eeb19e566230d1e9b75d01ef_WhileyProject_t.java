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
 
 import wyc.builder.Whiley2WyilBuilder;
 import wyc.lang.WhileyFile;
 import wyclipse.builder.CoreIOException;
 import wyclipse.builder.IContainerRoot;
 import wyclipse.builder.IContainerRoot.IFileEntry;
 import wybs.lang.*;
 import wybs.util.JarFileRoot;
 import wybs.util.StandardProject;
 import wybs.util.StandardBuildRule;
 import wybs.util.Trie;
 import wyil.Pipeline;
 import wyil.lang.WyilFile;
 import wyil.transforms.VerificationCheck;
import wyjvm.lang.ClassFile;
 
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
  * When a resource changes which is relevant to the builder (e.g. a Whiley file,
  * etc) then the WhileyProject must be notified of this. In turn, it updates its
  * knowledge of the system accordingly and may schedule files to be rebuilt.
  * </p>
  * 
  * @author David J. Pearce
  * 
  */
 public class WhileyProject extends StandardProject {
 
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
 	private Whiley2WyilBuilder builder;
 
 	/**
 	 * This is something of a hack. Basically it's a generic filter to return
 	 * all source files
 	 */
 	protected static final Content.Filter<WhileyFile> includes = Content
 			.filter(Trie.fromString("**"), WhileyFile.ContentType);
 
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
 
 		this.delta = new ArrayList<IFileEntry>();
 
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
 			roots.add(outputRoot);
 		} else {
 			throw new RuntimeException(
 					"Whiley Plugin currently unable to handle projects without default output folder");
 		}
 
 		initialise(workspace.getRoot(), javaProject,
 				javaProject.getRawClasspath());
 
 		// ===============================================================
 		// Second, initialise builder + rules
 		// ===============================================================
 
 		Pipeline pipeline = new Pipeline(Pipeline.defaultPipeline);
 		pipeline.setOption(VerificationCheck.class, "enable", true);
 		this.builder = new Whiley2WyilBuilder(this, pipeline);
 
 		StandardBuildRule rule = new StandardBuildRule(builder);
 
 		for (Path.Root root : roots) {
 			if (root instanceof ISourceRoot) {
 				if (outputRoot != null) {
 					rule.add(root, includes, outputRoot,
 							WhileyFile.ContentType, WyilFile.ContentType);
 				} else {
 					rule.add(root, includes, root, WhileyFile.ContentType,
 							WyilFile.ContentType);
 				}
 			}
 		}
 
 		add(rule);
 	}
 
 	private void initialise(IWorkspaceRoot workspaceRoot,
 			IJavaProject javaProject, IClasspathEntry[] entries)
 			throws CoreException {
 		for (IClasspathEntry e : entries) {
 			switch (e.getEntryKind()) {
 			case IClasspathEntry.CPE_LIBRARY: {
 				IPath location = e.getPath();
 				// IFile file = workspaceRoot.getFile(location);
 
 				try {
 					roots.add(new JarFileRoot(location.toOSString(), registry));
 				} catch (IOException ex) {
 					// ignore entries which don't exist
 				}
 				break;
 			}
 			case IClasspathEntry.CPE_SOURCE: {
 				IPath location = e.getPath();
 				IFolder folder = workspaceRoot.getFolder(location);
 				roots.add(new ISourceRoot(folder, registry));
 				break;
 			}
 			case IClasspathEntry.CPE_CONTAINER:
 				IPath location = e.getPath();
 				IClasspathContainer container = JavaCore.getClasspathContainer(
 						location, javaProject);
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
 	
 	/**
 	 * A resource of some sort has changed, and we need to update the namespace
 	 * accordingly. Note that the given resource may not actually be managed by
 	 * this namespace manager --- in which case it can be safely ignored.
 	 * 
 	 * @param delta
 	 */
 	public void changed(IResource resource) throws CoreException {
 		System.out.println("RESOURCE CHANGED: " + resource.getFullPath());
 		for (Path.Root root : roots) {
 			if (root instanceof ISourceRoot) {
 				ISourceRoot srcRoot = (ISourceRoot) root;
 				IFileEntry<?> ife = srcRoot.getResource(resource);
 				if (ife != null) {
 					// Ok, this file is managed by a source root; therefore,
 					// mark it
 					// for recompilation. Note that we must refresh the entry as
 					// well, since it has clearly changed.
 					ife.refresh();
 					delta.add(ife);
 					return;
 				}
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
 		System.out.println("RESOURCE ADDED: " + resource.getFullPath());
 		IPath location = resource.getLocation();
 		for (Path.Root root : roots) {
 			if (root instanceof ISourceRoot) {
 				ISourceRoot srcRoot = (ISourceRoot) root;
 				IFileEntry e = srcRoot.create(resource);
 				if (e != null) {
 					delta.add(e);
 					return; // done
 				}
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
 		System.out.println("RESOURCE REMOVED: " + resource.getFullPath());
 		// We could actually do better here, in some cases. For example, if a
 		// source file is removed then we only need to recompile those which
 		// depend upon it.
 		for (Path.Root srct : roots) {
 			try {
 				srct.refresh();
 			} catch (CoreIOException e) {
 				throw e.payload;
 			} catch (IOException e) {
 				// deadcode
 			}
 		}
 
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
 			for (Path.Root root : roots) {
 				if (root instanceof ISourceRoot) {
 					ISourceRoot srcRoot = (ISourceRoot) root;
 					for (Path.Entry<?> e : srcRoot.get(includes)) {
 						delta.add((IFileEntry) e);
 					}
 				}
 			}
 
 			// second, determine all target files
 			for (BuildRule r : rules) {
 				for (IFileEntry<?> source : delta) {
 					allTargets.addAll(r.dependentsOf(source));
 				}
 			}
 
 			// third, delete all target files
 			for (Path.Entry<?> _e : allTargets) {
 				IFileEntry<?> e = (IFileEntry<?>) _e;
 				e.getFile().delete(true, null);
 			}
 		} catch (CoreException e) {
 			throw e;
 		} catch (RuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root.
 		}
 	}
 
 	/**
 	 * Build those source files which are known to have changed (i.e. those
 	 * entries found in delta). To do this, we must identify all corresponding
 	 * targets, as well as any other dependencies.
 	 */
 	public void build() throws IOException, CoreException {
 		HashSet<Path.Entry<?>> allTargets = new HashSet();
 		try {
 			System.out
 					.println("BUILDING: " + delta.size() + " source file(s).");
 //			// Firstly, initialise list of targets to rebuild.
 //			for (BuildRule r : rules) {
 //				for (IFileEntry<?> source : delta) {
 //					allTargets.addAll(r.dependentsOf(source));
 //				}
 //			}
 //
 //			// Secondly, add all dependents on those being rebuilt.
 //			int oldSize;
 //			do {
 //				oldSize = allTargets.size();
 //				for (BuildRule r : rules) {
 //					for (Path.Entry<?> target : allTargets) {
 //						allTargets.addAll(r.dependentsOf(target));
 //					}
 //				}
 //			} while (allTargets.size() != oldSize);
 
 			// Thirdly, remove all markers from those entries
 			for (Path.Entry<?> _e : delta) {
 				IFileEntry e = (IFileEntry) _e;
 				e.getFile().deleteMarkers(IMarker.PROBLEM, true,
 						IResource.DEPTH_INFINITE);
 			}
 
 			super.build((ArrayList) delta);
 			
 			// Finally, build all identified targets!
 //			do {
 //				oldSize = allTargets.size();
 //				for (BuildRule r : rules) {
 //					r.apply(allTargets);
 //				}
 //			} while (allTargets.size() < oldSize);
 
 		} catch (SyntaxError e) {
 			// FIXME: this is a hack because syntax error doesn't retain the
 			// correct information (i.e. it should store an Path.Entry, not a
 			// String filename).
 			for (Path.Root root : roots) {
 				for (Path.Entry entry : root.get(includes)) {
 					IFile file = ((IFileEntry) entry).getFile();
 					if (file.getLocation().toFile().getAbsolutePath()
 							.equals(e.filename())) {
 						// hit
 						highlightSyntaxError(file, e);
 						return;
 					}
 				}
 			}
 			// this is temporary hack, for now.
 			throw new RuntimeException("Unable to assign syntax error");
 		} catch (RuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root.
 		}
 
 		delta.clear();
 	}
 
 	/**
 	 * Build all known source files, regardless of whether they have changed or
 	 * not.
 	 */
 	public void buildAll() throws IOException, CoreException {
 		delta.clear();
 		for (Path.Root root : roots) {
 			if (root instanceof ISourceRoot) {
 				ISourceRoot srcRoot = (ISourceRoot) root;
 				for (Path.Entry<?> e : srcRoot.get(includes)) {
 					delta.add((IFileEntry) e);
 				}
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
 
 	private static final class ISourceRoot extends IContainerRoot {
 		public ISourceRoot(IContainer dir, Content.Registry contentTypes) {
 			super(dir, contentTypes);
 		}
 	}
 
 	/**
 	 * The master project content type registry.
 	 */
 	public static final Content.Registry registry = new Content.Registry() {
 
 		public void associate(Path.Entry e) {
 			if (e.suffix().equals("whiley")) {
 				e.associate(WhileyFile.ContentType, null);
			} else if (e.suffix().equals("wyil")) {
				e.associate(WyilFile.ContentType, null);
 			} else if (e.suffix().equals("class")) {
				e.associate(ClassFile.ContentType, null);				
 			}
 		}
 
 		public String suffix(Content.Type<?> t) {
 			if (t == WhileyFile.ContentType) {
 				return "whiley";
 			} else if (t == WyilFile.ContentType) {
				return "wyil";
			} else if (t == ClassFile.ContentType) {
 				return "class";
 			} else {
 				return "dat";
 			}
 		}
 	};
 }

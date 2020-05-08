 // Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //    * Redistributions of source code must retain the above copyright
 //      notice, this list of conditions and the following disclaimer.
 //    * Redistributions in binary form must reproduce the above copyright
 //      notice, this list of conditions and the following disclaimer in the
 //      documentation and/or other materials provided with the distribution.
 //    * Neither the name of the <organization> nor the
 //      names of its contributors may be used to endorse or promote products
 //      derived from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
 // DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package wyclipse.core.builder;
 
 import java.io.*;
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 import wyclipse.core.Activator;
 import wyclipse.core.WhileyNature;
 import wyclipse.core.builder.ContainerRoot.IFileEntry;
 import wybs.lang.*;
 import wybs.lang.Path;
 import wybs.util.StandardBuildRule;
 import wybs.util.StandardProject;
 import wybs.util.Trie;
 import wyc.builder.WhileyBuilder;
 import wyc.lang.WhileyFile;
 import wyc.util.WycBuildTask;
 import wycs.builders.Wyal2WycsBuilder;
 import wycs.core.WycsFile;
 import wycs.syntax.WyalFile;
 import wycs.util.WycsBuildTask;
 import wyil.builders.Wyil2WyalBuilder;
 import wyil.checks.*;
 import wyil.io.WyilFilePrinter;
 import wyil.lang.WyilFile;
 import wyil.transforms.*;
 import wyjc.util.WyjcBuildTask;
 import jasm.lang.ClassFile;
 
 /**
  * <p>
  * Responsible for managing resources in the system which are directly or
  * indirectly relevant to compiling Whiley files. For example, source folders
  * containing whiley files are (obviously) directly relevant; however, other
  * containers may be relevant (e.g. if they hold jar files on the whileypath).
  * </p>
  * 
  * <p>
  * When a resource changes which is relevant to the builder (e.g. a Whiley file,
  * etc) then the builder updates its knowledge of the system accordingly and may
  * schedule files to be rebuilt.
  * </p>
  * 
  * @author David J. Pearce
  * 
  */
 public class WhileyProjectBuilder extends IncrementalProjectBuilder {
 	private static final boolean verbose = true;
 
 	/**
 	 * This is the WyBS project which actually controls the While compiler. This
 	 * contains the various roots of the project and the build rules which have
 	 * been configured.
 	 */
 	private StandardProject whileyProject;
 	
 	/**
 	 * The delta is a list of entries which require recompilation. As entries
 	 * are changed, they may be added to this list (e.g. Whiley). Entries which
 	 * depend upon them may also be added. Or, if they represent e.g. binary
 	 * dependents (e.g. jar files) then this may force a total recompilation.
 	 */
 	protected final ArrayList<IFileEntry> delta = new ArrayList<IFileEntry>();
 
 	public void initialise() throws CoreException {
 		System.err.println("WHILEY PROJECT BUILDER INITIALISED");
 		// First, get the whileypath from the nature
 		IProject iproject = (IProject) getProject();
 		WhileyNature nature = (WhileyNature) iproject
 				.getNature(Activator.WYCLIPSE_NATURE_ID);
 		WhileyPath whileypath = nature.getWhileyPath();
 		
 		this.whileyProject = new StandardProject();
 		
 		// Second, initialise the list of available builders
 		Map<String,Builder> builders = initialiseBuilders();
 		
 		// Finally, initialise the whiley project from whileypath
 		initialiseWhileyProject(whileypath, builders,
 				iproject);				
 	}
 	
 	/**
 	 * Create a standard (wybs) project from a given whileypath and set of
 	 * available builders.
 	 * 
 	 * @param whileypath
 	 * @param builders
 	 * @return
 	 */
 	protected void initialiseWhileyProject(WhileyPath whileypath,
 			Map<String, Builder> builders, IProject project) {
 		// First, create an appropriate root for the default output folder (if
 		// applicable)
 		
 		List<Path.Root> whileyProjectRoots = whileyProject.roots();
 		ContainerRoot defaultOutputRoot = null;
 		if (whileypath.getDefaultOutputFolder() != null) {
 			IFolder defaultOutputFolder = project.getFolder(whileypath
 					.getDefaultOutputFolder());
 			Content.Filter defaultOutputIncludes = Content.filter(
 					Trie.fromString("**"), WyilFile.ContentType);
 			defaultOutputRoot = new ContainerRoot(defaultOutputFolder, registry);
 			whileyProjectRoots.add(defaultOutputRoot);
 		}
 		
 		// Second, iterate all entries looking for: actions which define source
 		// and output folders, as well as build rules; and, also external
 		// libraries. 
 		for (WhileyPath.Entry entry : whileypath.getEntries()) {
 			
 			if (entry instanceof WhileyPath.Action) {
 				WhileyPath.Action action = (WhileyPath.Action) entry;
 				
 				// ============================================================
 				// First, create the corresponding source root
 				// ============================================================
 				Content.Filter<WhileyFile> sourceIncludes = Content.filter(
 						action.getSourceIncludes(), WhileyFile.ContentType);
 				IFolder sourceFolder = project.getFolder(action
 						.getSourceFolder());
 				SourceRoot<WhileyFile> sourceRoot = new SourceRoot<WhileyFile>(
 						sourceFolder, sourceIncludes, registry);
 				whileyProjectRoots.add(sourceRoot);
 				
 				System.err.println("*** INITIALISING WHILEY SOURCE ROOT: " + action.getSourceFolder());
 				// ============================================================
 				// Second, create the corresponding output root (if applicable)
 				// ============================================================
 				Path.Root outputRoot = defaultOutputRoot; 
 				if(action.getOutputFolder() != null) {
 					IFolder outputFolder = project.getFolder(action.getOutputFolder());
 					outputRoot = new ContainerRoot(outputFolder,registry);					
 					whileyProjectRoots.add(outputRoot);
 				}
 				
 				// ============================================================
 				// Third, create the corresponding build rule(s)
 				// ============================================================
 				Builder whileyBuilder = builders.get("wyc");
 				StandardBuildRule br = new StandardBuildRule(whileyBuilder);
 				br.add(sourceRoot, sourceIncludes, outputRoot,
 						WhileyFile.ContentType, WyilFile.ContentType);
 				// FIXME: clearly, need more build rules
 				whileyProject.add(br);
 				
 			} else if(entry instanceof WhileyPath.ExternalLibrary){
 				WhileyPath.ExternalLibrary ext = (WhileyPath.ExternalLibrary) entry;					
 				// TODO: implement me!
 				System.err.println("IGNORING EXTERNAL CONTAINER: " + ext.getLocation());
 			}
 		}			
 	}
 	
 	/**
 	 * <p>
 	 * Initialise the set of available builders for use within a Whiley project.
 	 * This would include the standard builders (e.g. for compiling Whiley files
 	 * to WyIL files). This might also include non-standard builders for the
 	 * plethora of available back-ends (e.g. for compiling WyIL files to JVM
 	 * Classfiles, etc).
 	 * </p>
 	 * 
 	 * @return A map from builder IDs to builder objects. The builder IDs are
 	 *         those used within the whileypath to connect build rules with
 	 *         builders.
 	 */
 	protected Map<String,Builder> initialiseBuilders() {
 		HashMap<String,Builder> builders = new HashMap<String,Builder>();
 		
 		// TODO: For now, I'm hard coding the set of available builders. In
 		// principle, this list should be dynamic depending on what plugins are
 		// available and/or options are selected.
 				
 		// First, add the standard Whiley Compiler (WyC), which compiles Whiley
 		// files to WyIL files.
 		Pipeline<WyilFile> pipeline = new Pipeline(WycBuildTask.defaultPipeline);
 		WhileyBuilder wyc = new WhileyBuilder(whileyProject, pipeline);
 		wyc.setLogger(new Logger.Default(System.err));
 		builders.put("wyc", wyc);
 		
 		// Second, add the standard WyAL builder, which compiles WyIL files to
 		// WyAL files.
 		Wyil2WyalBuilder wyal = new Wyil2WyalBuilder(whileyProject);
 		wyal.setLogger(new Logger.Default(System.err));
 		builders.put("wyal", wyal);
 		
 		// Third, add the standard WyCS builder, which compiles WyAL files to
 		// WyCS files.
 		Pipeline<WycsFile> wycsPipeline = new Pipeline(WycsBuildTask.defaultPipeline);
 		Wyal2WycsBuilder wycs = new Wyal2WycsBuilder(whileyProject,wycsPipeline);		
 		wycs.setLogger(new Logger.Default(System.err));
 		builders.put("wycs", wycs);
 		
 		// Done.
 		return builders;
 	}
 	
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		try {
 			if(whileyProject == null) {
 				initialise();
 			}
 
 			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
 				buildAll();
 			} else if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD
 					|| kind == IncrementalProjectBuilder.AUTO_BUILD) {
 				IResourceDelta delta = getDelta(getProject());			
 				if (delta == null) {
 					buildAll();
 				} else {
 					incrementalBuild(delta, monitor);
 				}
 			}
 		} catch(CoreIOException e) {
 			throw e.payload;
 		} catch(IOException e) {
 			// dead code
 		}
 		return null;
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		
 		actionChangedResources(delta);
 
 		// finally, give the whiley project a changed to recompile any whiley
 		// files that are affected the by changes. 
 		try {
 			build();
 		} catch(CoreIOException e) {
 			throw e.payload;
 		} catch(IOException e) {
 			// dead code
 		}
 	}
 
 	/**
 	 * Delete all entries and corresponding IFiles from all binary roots. That
 	 * is, delete all output files. An immediate consequence of this is that all
 	 * known source files are marked for recompilation. However, these files are
 	 * not actually recompiled until build() is called.
 	 */
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		HashSet<Path.Entry<?>> allTargets = new HashSet();
 		try {
 			delta.clear();
 
 			// first, identify all source files
 			for (Path.Root root : whileyProject.roots()) {
 				if (root instanceof SourceRoot) {
 					SourceRoot srcRoot = (SourceRoot) root;
 					for (Object e : srcRoot.get()) {
 						delta.add((IFileEntry) e);
 					}
 				}
 			}
 
 			// second, determine all target files
 			for (BuildRule r : whileyProject.rules()) {
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
 			// hmmm, obviously I don't like doing this.  Probably the best way
 			// around it is to not extend abstract root.
 		}
 	}
 	
 	/**
 	 * This simply recurses the delta and actions all changes to the whiley
 	 * project. The whiley project will then decide whether or not those changes
 	 * are actually relevant.
 	 * 
 	 * @param delta
 	 * @return
 	 */
 	protected void actionChangedResources(IResourceDelta delta) throws CoreException {		
 		try {
 			delta.accept(new IResourceDeltaVisitor() {
 				public boolean visit(IResourceDelta delta) throws CoreException {					
 					IResource resource = delta.getResource();
 					if (resource != null) {
 						switch(delta.getKind()) {
 							case IResourceDelta.ADDED:
 								added(resource);
 								break;
 							case IResourceDelta.REMOVED:
 								removed(resource);
 								break;
 							case IResourceDelta.CHANGED:
 								if(isWhileyPath(resource)) {
 									// In this case, the ".classpath" file has
 									// changed. This could be as a result of a
 									// jar file being added or removed from the
 									// classpath. Basically, we don't know in
 									// what way exactly it has changed.
 									// Therefore, we must assume the worst-case
 									// and recompile *everything*.
 									initialise(); 			 
 									clean(null);
 								} else {									
 									changed(resource);
 								}
 								break;
 						}						
 					}
 					return true; // visit children as well.
 				}
 			});
 		} catch (CoreException e) {
 			e.printStackTrace();
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
 		System.out.println("RESOURCE CHANGED: " + resource.getFullPath());
 		if (resource instanceof IFile) {
 			// This indicates a file of some description has changed. What we do
 			// now, is to check whether or not it's a source file and, if it is,
 			// we then recompile it.
 			for (Path.Root root : whileyProject.roots()) {
 				if (root instanceof SourceRoot) {
 					SourceRoot srcRoot = (SourceRoot) root;
 					IFileEntry<?> ife = srcRoot.getResource(resource);
 					if (ife != null) {
 						// Ok, this file is managed by a source root; therefore,
 						// mark it
 						// for recompilation. Note that we must refresh the
 						// entry as
 						// well, since it has clearly changed.
 						ife.refresh();
 						delta.add(ife);
 						return;
 					}
 				}
 			}
 		} else {
 			System.out.println("IGNORED REOURCE CHANGE: " + resource.getFullPath());
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
 		for (Path.Root root : whileyProject.roots()) {
 			if (root instanceof SourceRoot) {
 				SourceRoot srcRoot = (SourceRoot) root;
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
 		for (Path.Root srct : whileyProject.roots()) {
 			try {
 				srct.refresh();
 			} catch (CoreIOException e) {
 				throw e.payload;
 			} catch (IOException e) {
 				// deadcode
 			}
 		}
 
 		clean(null);
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
 
 			// First, remove all markers from those entries
 			for (Path.Entry<?> _e : delta) {
 				IFileEntry e = (IFileEntry) _e;
 				e.getFile().deleteMarkers(IMarker.PROBLEM, true,
 						IResource.DEPTH_INFINITE);
 			}
 			
 			whileyProject.build((ArrayList) delta);
 
 		} catch (SyntaxError e) {
 			// FIXME: this is a hack because syntax error doesn't retain the
 			// correct information (i.e. it should store an Path.Entry, not a
 			// String filename).
 			for (Path.Root root : whileyProject.roots()) {
 				if (root instanceof SourceRoot) {
 					SourceRoot srcRoot = (SourceRoot) root;
 					for (Object entry : srcRoot.get()) {
 						IFile file = ((IFileEntry) entry).getFile();
 						if (file.getLocation().toFile().getAbsolutePath()
 								.equals(e.filename())) {
 							// hit
 							highlightSyntaxError(file, e);
 							return;
 						}
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
 		for (Path.Root root : whileyProject.roots()) {
 			if (root instanceof SourceRoot) {
 				SourceRoot srcRoot = (SourceRoot) root;
 				for (Object e : srcRoot.get()) {
 					delta.add((IFileEntry) e);
 				}
 			}
 		}
 		build();
 	}
 
 	protected void highlightSyntaxError(IResource resource, SyntaxError err)
 			throws CoreException {
 		IMarker m = resource.createMarker(Activator.WYCLIPSE_MARKER_ID);
 		m.setAttribute(IMarker.CHAR_START, err.start());
 		m.setAttribute(IMarker.CHAR_END, err.end() + 1);
 		m.setAttribute(IMarker.MESSAGE, err.msg());
 		m.setAttribute(IMarker.LOCATION, "Whiley File");
 		m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
 		m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
 	}
 
 	public class SourceRoot<T> extends ContainerRoot {
 		private final Content.Filter<T> includes;
 		
 		public SourceRoot(IContainer dir, Content.Filter<T> includes,
 				Content.Registry contentTypes) {
 			super(dir, contentTypes);
 			this.includes = includes;
 		}
 		
 
 		public List<wybs.lang.Path.Entry<T>> get() throws IOException {
 			return super.get(includes);
 		}		
 	}
 	
 	private static boolean isWhileyPath(IResource resource) {
 		return resource instanceof IFile && resource.getName().equals(".whileypath");
 	}	
 	
 	// =====================================================================
 	// Registry
 	// =====================================================================
 
 	/**
 	 * The master project content type registry. This associates suffixes with
 	 * the corresponding Object for decoding them. In essence, this determines
 	 * what file kinds are known to the compiler.
 	 */
 	public static final Content.Registry registry = new Content.Registry() {
 
 		public void associate(Path.Entry e) {
 			if (e.suffix().equals("whiley")) {
 				e.associate(WhileyFile.ContentType, null);
 			} else if (e.suffix().equals("wyil")) {
 				e.associate(WyilFile.ContentType, null);
 			} else if (e.suffix().equals("wyal")) {
 				e.associate(WyalFile.ContentType, null);
 			} else if (e.suffix().equals("wycs")) {
 				e.associate(WycsFile.ContentType, null);
 			} else if (e.suffix().equals("class")) {
 				e.associate(WyjcBuildTask.ContentType, null);				
 			} 
 		}
 
 		public String suffix(Content.Type<?> t) {
 			if (t == WhileyFile.ContentType) {
 				return "whiley";
 			} else if (t == WyilFile.ContentType) {
 				return "wyil";
 			} else if (t == WyalFile.ContentType) {
 				return "wyal";
 			} else if (t == WycsFile.ContentType) {
 				return "wycs";
 			} else if (t == WyjcBuildTask.ContentType) {
 				return "class";
 			} else {
 				return "dat";
 			}
 		}
 	};
 }

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

import wyclipse.core.builder.IContainerRoot.IFileEntry;
 
 import wybs.lang.*;
 import wybs.lang.Path;
 import wybs.util.StandardBuildRule;
 import wybs.util.StandardProject;
 import wybs.util.Trie;
 import wyc.builder.WhileyBuilder;
 import wyc.lang.WhileyFile;

 import wycs.core.WycsFile;
 import wycs.syntax.WyalFile;
import wyil.checks.*;
 import wyil.io.WyilFilePrinter;
 import wyil.lang.WyilFile;
import wyil.transforms.*;
 import wyjvm.lang.ClassFile;
 
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
 public class Builder extends IncrementalProjectBuilder {
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
 	
 	/**
 	 * This is something of a hack. Basically it's a generic filter to return
 	 * all source files
 	 */
 	protected static final Content.Filter<WhileyFile> includes = Content
 			.filter(Trie.fromString("**"), WhileyFile.ContentType);
 
 	protected void initialise() throws CoreException {		
 		IProject iproject = (IProject) getProject();
 		this.whileyProject = new StandardProject();
 		
 		Pipeline pipeline = new Pipeline(defaultPipeline);
 		WhileyBuilder builder = new WhileyBuilder(whileyProject, pipeline);
 		StandardBuildRule rule = new StandardBuildRule(builder);
 		builder.setLogger(new Logger.Default(System.err));
 		
 		// FIXME: actually do something to the build rule!
 		
 		whileyProject.add(rule);		
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
 				if (root instanceof ISourceRoot) {
 					ISourceRoot srcRoot = (ISourceRoot) root;
 					for (Path.Entry<?> e : srcRoot.get(includes)) {
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
 								if(isClassPath(resource)) {
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
 				if (root instanceof ISourceRoot) {
 					ISourceRoot srcRoot = (ISourceRoot) root;
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
 		for (Path.Root root : whileyProject.roots()) {
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
 		
 	private static boolean isClassPath(IResource resource) {
 		return resource instanceof IFile && resource.getName().equals(".classpath");
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
 				e.associate(ClassFile.ContentType, null);				
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
 			} else if (t == ClassFile.ContentType) {
 				return "class";
 			} else {
 				return "dat";
 			}
 		}
 	};
 
 	// =====================================================================
 	// Default Pipeline (for whiley -> wyil)
 	// =====================================================================
 
 	public static final List<Pipeline.Template> defaultPipeline = Collections
 			.unmodifiableList(new ArrayList<Pipeline.Template>() {
 				{
 					//						add(new Pipeline.Template(WyilFilePrinter.class,
 					//								Collections.EMPTY_MAP));
 					add(new Pipeline.Template(DefiniteAssignmentCheck.class,
 							Collections.EMPTY_MAP));
 					add(new Pipeline.Template(ModuleCheck.class, Collections.EMPTY_MAP));
 					add(new Pipeline.Template(RuntimeAssertions.class,
 							Collections.EMPTY_MAP));
 					add(new Pipeline.Template(BackPropagation.class,
 							Collections.EMPTY_MAP));
 					add(new Pipeline.Template(LoopVariants.class, Collections.EMPTY_MAP));
 					add(new Pipeline.Template(ConstantPropagation.class,
 							Collections.EMPTY_MAP));
 					add(new Pipeline.Template(CoercionCheck.class, Collections.EMPTY_MAP));
 					add(new Pipeline.Template(DeadCodeElimination.class,
 							Collections.EMPTY_MAP));
 					add(new Pipeline.Template(LiveVariablesAnalysis.class,
 							Collections.EMPTY_MAP));
 					//						add(new Pipeline.Template(WyilFilePrinter.class,
 					//								Collections.EMPTY_MAP));
 				}
 			});
 
 	/**
 	 * Register default transforms. This is necessary so they can be referred to
 	 * from the command-line using abbreviated names, rather than their full
 	 * names.
 	 */
 	static {
 		Pipeline.register(BackPropagation.class);
 		Pipeline.register(DefiniteAssignmentCheck.class);
 		Pipeline.register(LoopVariants.class);
 		Pipeline.register(ConstantPropagation.class);
 		Pipeline.register(ModuleCheck.class);
 		Pipeline.register(RuntimeAssertions.class);
 		Pipeline.register(CoercionCheck.class);
 		Pipeline.register(WyilFilePrinter.class);
 		Pipeline.register(DeadCodeElimination.class);
 		Pipeline.register(LiveVariablesAnalysis.class);
 	}
 }

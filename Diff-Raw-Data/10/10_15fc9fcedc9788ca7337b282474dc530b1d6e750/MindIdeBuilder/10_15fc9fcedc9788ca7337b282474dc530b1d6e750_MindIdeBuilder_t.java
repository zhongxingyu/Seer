 package org.ow2.mindEd.ide.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.CompilerError;
 import org.objectweb.fractal.adl.StaticJavaGenerator.InvalidCommandLineException;
 import org.objectweb.fractal.adl.error.Error;
 import org.objectweb.fractal.adl.error.ErrorLocator;
 import org.ow2.mindEd.ide.model.MindFile;
 import org.ow2.mindEd.ide.model.MindObject;
 import org.ow2.mindEd.ide.model.MindPackage;
 import org.ow2.mindEd.ide.model.MindPathEntry;
 import org.ow2.mindEd.ide.model.MindPathKind;
 import org.ow2.mindEd.ide.model.MindProject;
 import org.ow2.mindEd.ide.model.MindRootSrc;
 import org.ow2.mindEd.ide.model.MindidePackage;
 
 /**
  * Mind builder. Do noting in lot 1. Keeping for next lot.
  * @author chomats
  *
  */
 public class MindIdeBuilder extends IncrementalProjectBuilder {
 	
 	
 	/**
  * ID of mind ide builder.
  */
	public static final String BUILDER_ID = MindActivator.ID+".core.builder";
 	
 
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		IProject currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible()) return ;
 		
 		MindCMarker.unmark(currentProject, false, IResource.DEPTH_INFINITE);
 	}
 	
 	@Override
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		IProject currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible()) return new IProject[0];
 
 		MindProject mp = MindIdeCore.get(currentProject);
 		if (mp == null) return new IProject[0];
 		
 		
 		IResourceDelta delta = getDelta(currentProject);
 		if (delta == null) {
 			// full build
 			for (MindFile mf : mp.getAllFiles()) {
 				try {
 					if (mf.eClass() != MindidePackage.Literals.MIND_ADL) continue;
 					
 					IResource mfRsc = MindIdeCore.getResource(mf);
 					MindCMarker.unmark(mfRsc, false, IResource.DEPTH_ZERO);
 					checkFile(currentProject, Collections.singletonList(mf));
 				} catch (InvalidCommandLineException e) {
 					addError(currentProject, mf, e);
 				} catch (ADLException e) {
 					addError(currentProject, mf, e);
 				} catch (CompilerError e) {
 					e.printStackTrace();
 				}
 			}
 			
 		} else  {
 			final List<MindFile> adls = new ArrayList<MindFile>();
 			
 			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
 				
 				@Override
 				public boolean visit(IResourceDelta delta) throws CoreException {
 					IResource r = delta.getResource();
 					if (r == null) return false;
 					MindObject mo = MindIdeCore.get(r);
 					
 					if (mo == null) return false;
 					
 					if (mo.eClass() == MindidePackage.Literals.MIND_PROJECT)
 						return true;
 					if (mo.eClass() == MindidePackage.Literals.MIND_PACKAGE)
 						return true;
 					if (mo.eClass() == MindidePackage.Literals.MIND_ROOT_SRC)
 						return true;
 					if (mo.eClass() == MindidePackage.Literals.MIND_ADL)
 						adls.add((MindFile) mo);
 					return false;
 				}
 			};
 			
 			delta.accept(visitor );
 			//incrementalBuild
 			for (MindFile mf : adls) {
 				try {
 					IResource mfRsc = MindIdeCore.getResource(mf);
 					MindCMarker.unmark(mfRsc, false, IResource.DEPTH_ZERO);
 					checkFile(currentProject, Collections.singletonList(mf));
 				} catch (InvalidCommandLineException e) {
 					addError(currentProject, mf, e);
 				} catch (ADLException e) {
 					addError(currentProject, mf, e);
 				} catch (CompilerError e) {
 					e.printStackTrace();
 				}
 			}
 		} 
 		return new IProject[0];
 	}	
 
 	private void addError(IProject currentProject, MindFile mf,
 			InvalidCommandLineException e) throws CoreException {
 		IResource mfRsc = MindIdeCore.getResource(mf);
 		
 		IMarker marker = MindCMarker.mark(mfRsc);
 		MindCMarker.setSeverity(marker, IMarker.SEVERITY_ERROR);
 		MindCMarker.setDescription(marker, e.getMessage());
 	}
 	
 	private void addError(IProject currentProject, MindFile mf,
 			ADLException e) throws CoreException {
 		Error error = e.getError();
 		ErrorLocator locator = error.getLocator();
 		int[] charsIndex = computeCharStartAndEnd(mf, locator);
 		
 		IResource mfRsc = MindIdeCore.getResource(mf);
 		
 		IMarker marker = MindCMarker.mark(mfRsc);
 		MindCMarker.setSeverity(marker, IMarker.SEVERITY_ERROR);
 		marker.setAttribute(IMarker.CHAR_START, charsIndex[0]);
 		marker.setAttribute(IMarker.CHAR_END, charsIndex[1]);
 		if (locator != null)
 			marker.setAttribute(IMarker.LINE_NUMBER, locator.getBeginLine());
 		
 		// To test XTEXT integration
 		addXtextAttributes(marker, e);
 		MindCMarker.setDescription(marker, error.getMessage());
 		e.printStackTrace();
 	}
 	
 	private void addXtextAttributes(final IMarker marker, ADLException e) throws CoreException {
 		
 		marker.setAttribute("CODE_KEY",e.getError().getTemplate().getGroupId()+"-"+e.getError().getTemplate().getErrorId());
 		marker.setAttribute("URI_KEY", URI.createPlatformResourceURI(marker.getResource().getFullPath().toString(),false).appendFragment("/").toString());
 	}
 	
 	public int[] computeCharStartAndEnd(MindFile mf, ErrorLocator locator) {
 		IFile	ifile = MindIdeCore.getResource(mf);
 		
 		File f = ifile == null ? null : ifile.getLocation() == null ? null : ifile.getLocation().toFile();
 		
 		if (locator == null) {
 			return new int[] { 0, 0 };
 		}
 		if (locator == null || f == null || !f.exists() || f.length() == 0) return new int[] { 0, 0 };
 		
 		int char_start = 0, char_end = 0;
 		int bl = locator.getBeginLine();
 		int bc = locator.getBeginColumn();
 		int el = locator.getEndLine();
 		int ec = locator.getEndColumn();
 		
 		
 		if (bl == 1) {
 			char_start = bc;
 		}
 		
 		if (el == 1) {
 			return new int[] { char_start, ec };
 		}
 
 		
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(f));
 			int begin = 0;
 			int line  = 1;
 			int column = 0;
 			int nextchar = -1;
 			int tabSize = 8;
 			while(true) {
 				
 				int c = nextchar;
 				if (c == -1) {
 					c = br.read();
 					if (c == -1) break;
 				}
 				begin++;
 				nextchar =  br.read();
 				
 				if ((c == '\r' && nextchar == '\n') || (c == '\n') || (c =='\r' && nextchar != '\n')) {
 					if (c == '\r' && nextchar == '\n') {
 						begin++;
 						nextchar = -1;
 					}
 					line++;
 					column = 0;
 					if (line>el) {
 						return new int[] {char_start, begin};
 					}
 					continue;
 				}
 				if (c == '\t') {
 					column += (tabSize - (column % tabSize));
 				} else {
 					column++;
 				}
 				if (line == bl && column == bc) {
 					char_start = begin-1;
 				}
 				if (line == el && column == ec) {
 					char_end = begin;
 					return new int[] {char_start, char_end};
 				}
 				
 			}
 			if (char_start != 0) {
 				return new int[] {char_start, begin };
 			}
 		} catch (IOException e) {
 		} finally {
 			if (br != null)
 				try {
 					br.close();
 				} catch (IOException e) {
 				}
 		}
 		return new int[] { 0, 0 };
 	}
 	
 
 	private static final String MINDC_METHOD = "nonExitMain";
 	static URLClassLoader mindCClassLoader = null;
 	
 	
 	public static URLClassLoader getMindCClassLoader() throws CoreException {
 		if (mindCClassLoader == null) {
 			String mindLocation = MindActivator.getPref().getMindCLocation();
 			if (mindLocation == null)
 				throw new CoreException(new Status(Status.ERROR, MindActivator.ID, "Cannot find mindc, set mindc location in preference"));
 			File libMindC = new File(new File( mindLocation), "lib");
 			ArrayList<URL> urls = new ArrayList<URL>();
 			File[] jars = libMindC.listFiles();
 			if (jars != null) {
 				for (File j : jars) {
 					if (j != null&& j.getName().endsWith(".jar")) {
 						try {
 							urls.add(j.toURI().toURL());
 						} catch (MalformedURLException e) {
 							MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Cannot get the url of "+j));
 						}
 					}
 				}
 			}
 			mindCClassLoader = new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]), MindIdeBuilder.class.getClassLoader());
 		}
 		return mindCClassLoader;
 	}
 	
 	public static void computeResolvedMindPath(MindProject mp, Set<MindObject> visited, Set<MindObject> path) {
 		if (visited.contains(mp)) return;
 		visited.add(mp);
 		for (MindPathEntry mpe : mp.getMindpathentries()) {
 			if (mpe.getEntryKind() == MindPathKind.SOURCE) {
 				MindRootSrc mrs = (MindRootSrc) mpe.getResolvedBy();
 				if (mrs != null) {
 					path.add(mrs);
 				}
 			} else if (mpe.getEntryKind() == MindPathKind.PROJECT) {
 				MindProject mp2 = (MindProject) mpe.getResolvedBy();
 				if (mp2 != null) {
 					computeResolvedMindPath(mp2, visited, path);
 				}
 			} else if (mpe.getEntryKind() == MindPathKind.IMPORT_PACKAGE) {
 				MindPackage p = (MindPackage) mpe.getResolvedBy();
 				MindRootSrc rs;
 				MindProject mp2;
 				if (p != null && ((rs = p.getRootsrc()) != null) && ((mp2 = rs.getProject()) != null)) {
 					computeResolvedMindPath(mp2, visited, path);
 				}
 			}
 		}
 	}
 	
 	public static void checkFile(IProject project, List<MindFile> filesToCheck) throws InvalidCommandLineException, ADLException, CompilerError, CoreException {
 		MindProject mp = MindIdeCore.get(project);
 		if (mp == null) return;
 		ArrayList<String> args = new ArrayList<String>();
 		
 		Set<MindObject> visited = new HashSet<MindObject>();
 		Set<MindObject> path = new HashSet<MindObject>();
 		computeResolvedMindPath(mp, visited, path);
 		for (MindObject mo : path) {
 			if (mo instanceof MindRootSrc) {
 				IFolder f = MindIdeCore.getResource((MindRootSrc)mo);
 				if (f.exists())
 					args.add("-S="+f.getLocation().toOSString());
 			}
 		}
 		args.add("-o="+project.getLocation().append("build").toOSString());
 		args.add("--check-adl");
 		
 		for (MindFile mf : filesToCheck) {
 			if (mf.eClass() == MindidePackage.Literals.MIND_ADL) {
 				args.add(mf.getQualifiedName());
 			}
 		}
 		
 		System.out.println("Call mindc :");
 		for (String a : args) {
 			System.out.println("   "+a);
 		}
 		
 		MindIdeBuilder.mindc((String[]) args.toArray(new String[args.size()]));		
 	}
 	/**
 	 * class : org.ow2.mind.Launcher
 method: public static void nonExitMain(final String... args)
       throws InvalidCommandLineException, CompilerInstantiationException,
       ADLException
 
 	 * @param args
 	 * @throws ADLException
 	 * @throws CoreException 
 	 */
 	
 	static public void mindc(String... args) throws InvalidCommandLineException,
     ADLException, CompilerError, CoreException {
 		String mindClassName =MindActivator.getPref().getMindCMainClass();
 		
 		if (mindClassName == null || "".equals(mindClassName))
 			mindClassName = "org.ow2.mind.Launcher";
 		URLClassLoader mindCClassLoader2 = getMindCClassLoader();
 		
 		try {
 			Method method = mindCClassLoader2.loadClass(mindClassName).getMethod(MINDC_METHOD, args.getClass());
 			method.invoke(null, (Object) args);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Bad mindc class, (not found method) "+mindClassName, e));
 		} catch (ClassNotFoundException e) {
 			MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Cannot found the mindc class "+mindClassName ,e));
 		} catch (IllegalArgumentException e) {
 			MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Bad mindc class, bad signature "+mindClassName,e));
 		} catch (IllegalAccessException e) {
 			MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Bad mindc class, illegal access on "+mindClassName,e));			
 		} catch (InvocationTargetException e) {
 			Throwable targetException = e.getTargetException();
 			Class c = targetException.getClass();
 			Class cbis = ADLException.class;
 			if (targetException instanceof ADLException) {
 				throw (ADLException) targetException;
 			} 
 			if (targetException instanceof InvalidCommandLineException) {
 				throw (InvalidCommandLineException) targetException;
 			}
 			if (targetException instanceof CompilerError) {
 				throw (CompilerError) targetException;
 			}
 			
 			else
 				MindActivator.log(new Status(Status.ERROR, MindActivator.ID, "Bad mindc class, unknown error "+mindClassName, targetException));
 		}
 	}
 	
 	/*
 	 *
 Usage: mindc [OPTIONS] (<definition>[:<execname>])+
   where <definition> is the name of the component to be compiled,
   and <execname> is the name of the output file to be created.
 
 Available options are :
   -h, --help                      Print this help and exit
   -S=<path list>, --src-path      the search path of ADL,IDL and implementation files (list of path separated by ':'). This option may be specified several times.
   -o=<output path>, --out-path    the path where generated files will be put (default is '.')
   -t=<name>, --target-descriptor  Specify the target descriptor
   --compiler-command=<path>       the command of the C compiler (default is 'gcc')
   -c=<flags>, --c-flags           the c-flags compiler directives. This option may be specified several times.
   -I=<path list>, --inc-path      the list of path to be added in compiler include paths. This option may be specified several times.
   --linker-command=<path>         the command of the linker (default is 'gcc')
   -l=<flags>, --ld-flags          the ld-flags compiler directives. This option may be specified several times.
   -L=<path list>, --ld-path       the list of path to be added to linker library search path. This option may be specified several times.
   -T=<path>, --linker-script      linker script to use (given path is resolved in source path)
   -j=<number>, --jobs             The number of concurrent compilation jobs (default is '1')
   -e                              Print error stack traces
   --check-adl                     Only check input ADL(s), do not compile
   -d, --def2c                     Only generate source code of the given definitions
   -D, --def2o                     Generate and compile source code of the given definitions, do not link an executable application
   -F, --force                     Force the regeneration and the recompilation of every output files
   -K, --keep                      Keep temporary output files in default output directory
   -B, --no-bin                    Do not generate binary ADL/IDL ('.def', '.itfdef' and '.idtdef' files).
 
 	 */
 	
 	public void compile(MindFile f) {
 		
 	}
 	
 	public static void changeMindCLocation() {
 		mindCClassLoader = null;
 	}
 
 }

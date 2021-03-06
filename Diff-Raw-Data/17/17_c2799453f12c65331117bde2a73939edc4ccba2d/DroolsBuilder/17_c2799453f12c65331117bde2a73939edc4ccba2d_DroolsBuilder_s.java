 package org.drools.ide.builder;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.runtime.RecognitionException;
 import org.apache.commons.jci.problems.CompilationProblem;
 import org.drools.compiler.DrlParser;
 import org.drools.compiler.DroolsError;
 import org.drools.compiler.DroolsParserException;
 import org.drools.compiler.GlobalError;
 import org.drools.compiler.PackageBuilder;
 import org.drools.compiler.ParserError;
 import org.drools.compiler.RuleError;
 import org.drools.ide.DroolsIDEPlugin;
 import org.drools.ide.editors.DSLAdapter;
 import org.drools.ide.preferences.IDroolsConstants;
 import org.drools.ide.util.ProjectClassLoader;
 import org.drools.lang.descr.PackageDescr;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * Automatically syntax checks .drl files and adds possible
  * errors or warnings to the problem list. Nominally is triggerd on save.
  * 
  * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
  */
 public class DroolsBuilder extends IncrementalProjectBuilder {
 
     public static final String BUILDER_ID = "org.drools.ide.droolsbuilder";
 
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
             throws CoreException {
         IProject currentProject = getProject();
         if (currentProject == null || !currentProject.isAccessible()) {
             return new IProject[0];
         }
         try {
             if (monitor != null && monitor.isCanceled())
                 throw new OperationCanceledException();
 
             if (kind == IncrementalProjectBuilder.FULL_BUILD) {
                 fullBuild(monitor);
             } else {
                 IResourceDelta delta = getDelta(getProject());
                 if (delta == null) {
                     fullBuild(monitor);
                 } else {
                     incrementalBuild(delta, monitor);
                 }
             }
         } catch (CoreException e) {
             IMarker marker = currentProject.createMarker(IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER);
             marker.setAttribute(IMarker.MESSAGE, "Error when trying to build Drools project: " + e.getLocalizedMessage());
             marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
         }
         return getRequiredProjects(currentProject);
     }
     
     protected void fullBuild(IProgressMonitor monitor)
             throws CoreException {
         getProject().accept(new DroolsBuildVisitor());
     }
     
     protected void incrementalBuild(IResourceDelta delta,
             IProgressMonitor monitor) throws CoreException {
     	boolean buildAll = DroolsIDEPlugin.getDefault().getPreferenceStore().getBoolean(IDroolsConstants.BUILD_ALL);
         if (buildAll) {
         	// to make sure that all rules are checked when a java file is changed 
         	fullBuild(monitor);
         } else {
         	delta.accept(new DroolsBuildDeltaVisitor());
         }
     }
 
     private class DroolsBuildVisitor implements IResourceVisitor {
         public boolean visit(IResource res) {
             return parseResource(res);
         }
     }
 
     private class DroolsBuildDeltaVisitor implements IResourceDeltaVisitor {
         public boolean visit(IResourceDelta delta) throws CoreException {
             return parseResource(delta.getResource());
         }
     }
     
     private boolean parseResource(IResource res) {
         try {
             IJavaProject project = JavaCore.create(res.getProject());
             // exclude files that are located in the output directory,
             // unless the ouput directory is the same as the project location
             if (!project.getOutputLocation().equals(project.getPath())
                     && project.getOutputLocation().isPrefixOf(res.getFullPath())) {
                 return false;
             }
         } catch (JavaModelException e) {
             // do nothing
         }
 
         if (res instanceof IFile && "drl".equals(res.getFileExtension())) {
             removeProblemsFor(res);
             try {
             	DroolsBuildMarker[] markers = parseFile((IFile) res, new String(Util.getResourceContentsAsCharArray((IFile) res)));
 		        for (int i = 0; i < markers.length; i++) {
 		        	createMarker(res, markers[i].getText(), markers[i].getLine());
 		        }
             } catch (Throwable t) {
             	createMarker(res, t.getMessage(), -1);
             }
             return false;
         }
         return true;
     }
     
     public static DroolsBuildMarker[] parseFile(IFile file, String content) {
     	List markers = new ArrayList();
         DrlParser parser = new DrlParser();
         try {
             Reader dslReader = DSLAdapter.getDSLContent(content, file);
             ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
             ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
             if (file.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
                 IJavaProject project = JavaCore.create(file.getProject());
                 newLoader = ProjectClassLoader.getProjectClassLoader(project);
             }
             try {
                 Thread.currentThread().setContextClassLoader(newLoader);
                 
                 //First we parse the source
                 PackageDescr packageDescr = parsePackage( content, parser, dslReader );
                 //parser errors
                 markParseErrors( markers,
                                  parser );  
                 
                 if (!parser.hasErrors()) {
                     //now we compile the AST to binary, and process any downstream errors.
                     PackageBuilder builder = new PackageBuilder();
                     builder.addPackage(packageDescr);
                     //downstream errors
                     markOtherErrors( markers,
                                      builder );
                 }
                 
             } catch (DroolsParserException e) {
                 //we have an error thrown from DrlParser
                 Throwable cause = e.getCause();
                 if (cause instanceof RecognitionException ) {
                     RecognitionException recogErr = (RecognitionException) cause;
                     markers.add(new DroolsBuildMarker(recogErr.getMessage(), recogErr.line)); //flick back the line number
                 }
             } catch (Exception t) {
                 throw t;
             } finally {
                 Thread.currentThread().setContextClassLoader(oldLoader);
             }
         } catch (Throwable t) {
         	// t.printStackTrace();
             // TODO create markers for exceptions containing line number etc.
             String message = t.getMessage();
             if (message == null || message.trim().equals( "" )) {
                 message = "Error: " + t.getClass().getName();
             }
             markers.add(new DroolsBuildMarker(message));
         }
         return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
     }
 
     /**
      * This will create markers for ALL errors that happen AFTER parsing.
      * In some cases, these may be bogus if parse errors exist.
      */
     private static void markOtherErrors(List markers,
                                         PackageBuilder builder) {
         DroolsError[] errors = builder.getErrors();
         // TODO are there warnings too?
         for (int i = 0; i < errors.length; i++ ) {
         	DroolsError error = errors[i];
         	if (error instanceof GlobalError) {
         		GlobalError globalError = (GlobalError) error;
         		markers.add(new DroolsBuildMarker(globalError.getGlobal(), -1));
         	} else if (error instanceof RuleError) {
         		RuleError ruleError = (RuleError) error;
         		// TODO try to retrieve line number (or even character start-end)
         		// disabled for now because line number are those of the rule class,
         		// not the rule file itself
         		if (ruleError.getObject() instanceof CompilationProblem[]) {
         			CompilationProblem[] problems = (CompilationProblem[]) ruleError.getObject();
         			for (int j = 0; j < problems.length; j++) {
         				markers.add(new DroolsBuildMarker(problems[j].getMessage(), ruleError.getLine()));
         			}
         		} else {
         			markers.add(new DroolsBuildMarker(ruleError.getRule().getName() + ":" + ruleError.getMessage(), ruleError.getLine()));
         		}
         	} else if (error instanceof ParserError) {
         		ParserError parserError = (ParserError) error;
         		// TODO try to retrieve character start-end
         		markers.add(new DroolsBuildMarker(parserError.getMessage(), parserError.getRow()));
         	} else {
         		markers.add(new DroolsBuildMarker("Unknown DroolsError " + error.getClass() + ": " + error));
         	}
         }
     }
 
     /**
      * This will create markers for parse errors.
      * Parse errors mean that antlr has picked up some major typos in the input source.
      */
     private static void markParseErrors(List markers,
                                         DrlParser parser) {
         if (parser.hasErrors()) {
             for ( Iterator iter = parser.getErrors().iterator(); iter.hasNext(); ) {
                 ParserError err = (ParserError) iter.next();
                 markers.add(new DroolsBuildMarker(err.getMessage(), err.getRow()));
             }
         }
     }
 
     /** Actually parse the rules into the AST */
    private static PackageDescr parsePackage(String content,
                                              DrlParser parser,
                                              Reader dslReader) throws DroolsParserException {
         if (dslReader != null) 
             return parser.parse(content, dslReader);
         else
             return parser.parse( content );
     }
 
     private void createMarker(final IResource res, final String message, final int lineNumber) {
         try {
         	IWorkspaceRunnable r= new IWorkspaceRunnable() {
         		public void run(IProgressMonitor monitor) throws CoreException {
             		IMarker marker = res
                     	.createMarker(IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER);
 		            marker.setAttribute(IMarker.MESSAGE, message);
 		            marker.setAttribute(IMarker.SEVERITY,
 		                    IMarker.SEVERITY_ERROR);
 		            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
 	    		}
 			};
 			res.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
         } catch (CoreException e) {
             DroolsIDEPlugin.log(e);
         }
     }
     
     private void removeProblemsFor(IResource resource) {
         try {
             if (resource != null && resource.exists()) {
                 resource.deleteMarkers(
                         IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER, false,
                         IResource.DEPTH_INFINITE);
             }
         } catch (CoreException e) {
             DroolsIDEPlugin.log(e);
         }
     }
     
     private IProject[] getRequiredProjects(IProject project) {
     	IJavaProject javaProject = JavaCore.create(project);
     	List projects = new ArrayList();
     	try {
     		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
     		for (int i = 0, l = entries.length; i < l; i++) {
     			IClasspathEntry entry = entries[i];
     			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
 					IProject p = project.getWorkspace().getRoot().getProject(entry.getPath().lastSegment()); // missing projects are considered too
 	    			if (p != null && !projects.contains(p)) {
 	    				projects.add(p);
 	    			}
     			}
     		}
     	} catch(JavaModelException e) {
     		return new IProject[0];
     	}
     	return (IProject[]) projects.toArray(new IProject[projects.size()]);
     }
 
 }

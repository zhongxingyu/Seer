 /**
  * 
  */
 package org.rubypeople.rdt.internal.core.builder;
 
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.core.parser.MarkerUtility;
 import org.rubypeople.rdt.internal.core.parser.RdtWarnings;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.parser.TaskParser;
 
 /**
  * @author Chris
  * 
  */
 public class RubyBuilder extends IncrementalProjectBuilder {
 
	private static final boolean DEBUG = true;
	public static int MAX_AT_ONCE = 1000;
 	private IProject currentProject;
	protected boolean compiledAllAtOnce;
 	private int totalWork = 10000;
 
 	public RubyBuilder() {}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask("build", totalWork);
 		IProject[] returnProjects = new IProject[0];
 		this.currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible()) return returnProjects;
 		
 		if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
 			if (DEBUG) System.out.println("INCREMENTAL build...");
 			IResourceDelta delta = getDelta(currentProject);
 			List files = getAffectedFiles(delta.getAffectedChildren());
 			IFile[] fileArray = new IFile[files.size()];
 			System.arraycopy(files.toArray(), 0, fileArray, 0, fileArray.length);
 			compile(fileArray, monitor);
 			return returnProjects;
 		}
 		build(monitor);
 
 		// FIXME Get the required projects as in JavaBuilder
 		if (DEBUG) System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
 				+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
 		return returnProjects;
 	}
 
 	private List getAffectedFiles(IResourceDelta[] deltas) {
 		List files = new ArrayList();
 		for (int i = 0; i < deltas.length; i++) {
 			IResourceDelta curDelta = deltas[i];
 			IResource resource = curDelta.getResource();
 			if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
 				files.addAll(getAffectedFiles(curDelta.getAffectedChildren()));
 				continue;
 			}
 			if (resource.getType() != IResource.FILE) continue;
 			if (!org.rubypeople.rdt.internal.core.util.Util.isRubyLikeFileName(resource.getName())) continue;
 			files.add(resource);
 		}
 		return files;
 	}
 
 	private void build(IProgressMonitor monitor) {
 		if (DEBUG) System.out.println("FULL build"); //$NON-NLS-1$
 
 		try {
 			ArrayList sourceFiles = new ArrayList(33);
 			addAllSourceFiles(sourceFiles);
 
 			if (sourceFiles.size() > 0) {
 				IFile[] allSourceFiles = new IFile[sourceFiles.size()];
 				sourceFiles.toArray(allSourceFiles);
 				compile(allSourceFiles, monitor);
 			}
 
 		} catch (CoreException e) {
 			// throw internalException(e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
 		currentProject.accept(new IResourceProxyVisitor() {
 
 			public boolean visit(IResourceProxy proxy) throws CoreException {
 				IResource resource = null;
 				switch (proxy.getType()) {
 				case IResource.FILE:
 					if (org.rubypeople.rdt.internal.core.util.Util.isRubyLikeFileName(proxy.getName())) {
 						if (resource == null) resource = proxy.requestResource();
 						sourceFiles.add(resource);
 					}
 					return false;
 				}
 				return true;
 			}
 		}, IResource.NONE);
 	}
 
 	/*
 	 * Compile the given elements, adding more elements to the work queue if
 	 * they are affected by the changes.
 	 */
 	protected void compile(IFile[] units, IProgressMonitor monitor) {
 		int unitsLength = units.length;
 		int percentPerUnit = totalWork / unitsLength;
 		// do them all now
 		for (int i = 0; i < unitsLength; i++) {
 			try {
 				IFile file = units[i];
 				if (DEBUG) System.out.println("About to compile " + file); //$NON-NLS-1$
 				RdtWarnings warnings = new RdtWarnings();
 				RubyParser parser = new RubyParser(warnings);
 				MarkerUtility.removeMarkers(file);
 				try {					
 					parser.parse(units[i].getName(), new InputStreamReader(file.getContents()));
 				} catch (SyntaxException e) {
 					MarkerUtility.createSyntaxError(file, e);
 				}
 				MarkerUtility.createProblemMarkers(file, warnings.getWarnings());
 				createTasks(file);
 				monitor.worked(percentPerUnit);
 			} catch (CoreException e) {
 				RubyCore.log(e);
 			}
 		}
 
 	}
 
 	private void createTasks(IFile file) throws CoreException {
 		IEclipsePreferences preferences = RubyCore.getInstancePreferences();
 		TaskParser taskParser = new TaskParser(preferences);
 		taskParser.parse(new InputStreamReader(file.getContents()));
 		MarkerUtility.createTasks(file, taskParser.getTasks());
 	}
 }

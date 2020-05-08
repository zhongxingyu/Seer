 package cdigen;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IImportDeclaration;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 public class CdiGenBuilder extends IncrementalProjectBuilder {
 
 	class SampleDeltaVisitor implements IResourceDeltaVisitor {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse .core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				// handle added resource
 				build(resource);
 				break;
 			case IResourceDelta.REMOVED:
 				// handle removed resource
 				break;
 			case IResourceDelta.CHANGED:
 				// handle changed resource
 				build(resource);
 				break;
 			}
 			// return true to continue visiting children.
 			return true;
 		}
 	}
 
 	class SampleResourceVisitor implements IResourceVisitor {
 		public boolean visit(IResource resource) {
 			try {
 				build(resource);
 			} catch (CoreException e) {
 				addMarker(resource, "CdiGen unhandled error: " + e.getClass().getName() + "/" + e.getMessage(), 0, IMarker.SEVERITY_ERROR);
 				e.printStackTrace();
 			}
 			// return true to continue visiting children.
 			return true;
 		}
 	}
 
	public static final String BUILDER_ID = "eclipse-cdigen.cdigen-builder";
 
	private static final String MARKER_TYPE = "eclipse-cdigen.xmlProblem";
 
 	private void addMarker(IResource file, String message, int lineNumber, int severity) {
 		try {
 			IMarker marker = file.createMarker(MARKER_TYPE);
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.SEVERITY, severity);
 			if (lineNumber == -1) {
 				lineNumber = 1;
 			}
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
 		} catch (CoreException e) {
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 		if (kind == FULL_BUILD) {
 
 			getProject().deleteMarkers(MARKER_TYPE, false, IProject.DEPTH_INFINITE);
 
 			fullBuild(monitor);
 		} else {
 			IResourceDelta delta = getDelta(getProject());
 			if (delta == null) {
 				fullBuild(monitor);
 			} else {
 				incrementalBuild(delta, monitor);
 			}
 		}
 		return null;
 	}
 
 	private static final String[] TRIGGERS = new String[] { "javax.enterprise.context.Dependent",
 			"javax.enterprise.context.ConversationScoped", "javax.enterprise.context.SessionScoped",
 			"javax.enterprise.context.RequestScoped", "javax.enterprise.context.ApplicationScoped", "javax.inject.Inject",
 			"javax.interceptor.Interceptor" };
 
 	private void build(IResource resource) throws CoreException {
 		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
 			ICompilationUnit cu = JavaCore.createCompilationUnitFrom(((IFile) resource));
 			if (cu != null) {
 				for (IType type : cu.getTypes()) {
 					buildType(cu, type);
 				}
 			}
 		}
 	}
 
 	private void buildType(ICompilationUnit cu, IType type) throws CoreException, JavaModelException {
 		if (isComponent(cu, type)) {
 			IJavaProject javaProject = JavaCore.create(getProject());
 
 			String fileName = type.getFullyQualifiedName('$').replace('.', '/') + ".component";
 			IPath path = javaProject.getOutputLocation();
 			path = path.removeFirstSegments(1);
 
 			ensurePackageExists(path, fileName);
 
 			path = path.append(fileName);
 			IFile file = getProject().getFile(path);
 
			String contents = "eclipse-cdigen: " + new Date().toString();
 			InputStream in = new ByteArrayInputStream(contents.getBytes());
 
 			if (!file.exists()) {
 				file.create(in, true, null);
 			} else {
 				file.setContents(in, IFile.FORCE, null);
 			}
 		}
 
 		for (IType nested : type.getTypes()) {
 			buildType(cu, nested);
 		}
 
 	}
 
 	private void ensurePackageExists(IPath root, String filename) throws CoreException {
 
 		String[] segments = filename.split("/");
 		IFolder cursor = getProject().getFolder(root);
 		for (int i = 0; i < segments.length - 1; i++) {
 			String segment = segments[i];
 			cursor = cursor.getFolder(segment);
 			if (!cursor.exists()) {
 				cursor.create(true, true, null);
 				cursor.setDerived(true, null);
 			}
 		}
 	}
 
 	private boolean isComponent(ICompilationUnit cu, IType type) throws CoreException {
 		for (IAnnotation annotation : type.getAnnotations()) {
 			String name = annotation.getElementName();
 
 			for (String trigger : TRIGGERS) {
 				if (trigger.equals(name)) {
 					// we matched a fully qualified name of a
 					// trigger annot, we are good
 					return true;
 				} else if (trigger.endsWith("." + name)) {
 					// we matched an unqualified name of a trigger
 					// annot, check if there is an import
 					for (IImportDeclaration decl : cu.getImports()) {
 						if (decl.getElementName().equals(trigger)) {
 							return true;
 						} else if (decl.isOnDemand()) {
 
 							// strip simple name from fully qualified, leaving just the trigger's package
 							String triggerPackage = trigger.substring(0, trigger.lastIndexOf("."));
 							// strip .* from the end of ondemand import
 							String importPackage = decl.getElementName().substring(0, decl.getElementName().length() - 2);
 
 							if (triggerPackage.equals(importPackage)) {
 								return true;
 							}
 						}
 					}
 
 				}
 			}
 		}
 		return false;
 	}
 
 	private void deleteMarkers(IFile file) {
 		try {
 			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
 		} catch (CoreException ce) {
 		}
 	}
 
 	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
 		try {
 			getProject().accept(new SampleResourceVisitor());
 		} catch (CoreException e) {
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
 		// the visitor does the work.
 		delta.accept(new SampleDeltaVisitor());
 	}
 }

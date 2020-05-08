 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.javaee.core.EjbLocalRef;
 import org.eclipse.jst.javaee.core.JavaEEObject;
 import org.eclipse.jst.javaee.core.Listener;
 import org.eclipse.jst.javaee.core.ResourceRef;
 import org.eclipse.jst.javaee.core.SecurityRole;
 import org.eclipse.jst.javaee.core.SecurityRoleRef;
 import org.eclipse.jst.javaee.ejb.MessageDrivenBean;
 import org.eclipse.jst.javaee.ejb.SecurityIdentityType;
 import org.eclipse.jst.javaee.ejb.SessionBean;
 import org.eclipse.jst.javaee.web.Filter;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.model.internal.common.AbstractAnnotationModelProvider;
 import org.eclipse.jst.jee.model.internal.common.ManyToOneRelation;
 import org.eclipse.jst.jee.model.internal.common.Result;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class WebAnnotationReader extends AbstractAnnotationModelProvider<WebApp> {
 
 	private static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
 
 	private ManyToOneRelation<JavaEEObject, IFile> modelToResource;
 
 	private ManyToOneRelation<JavaEEObject, IFile> modelToInterfaceResource;
 
 	private WebAnnotationFactory annotationFactory;
 
 	private WebApp ddApp;
 
 	public WebAnnotationReader(IFacetedProject facetedProject, WebApp ddApp) {
 		super(facetedProject);
 		if (ddApp == null)
 			throw new IllegalArgumentException("The deployment descriptor model can not be null!"); //$NON-NLS-1$
 		this.ddApp = ddApp;
 	}
 
 	@Override
 	protected void preLoad() {
 		modelObject = WebFactory.eINSTANCE.createWebApp();
 		annotationFactory = WebAnnotationFactory.createFactory();
 	}
 
 	@Override
 	protected void loadModel() throws CoreException {
 		IJavaProject javaProject = JavaCore.create(facetedProject.getProject());
 		final Collection<IFile> javaFiles = new HashSet<IFile>();
 		for (final IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
 			visitJavaFiles(javaFiles, root);
 		}
 		annotationFactory = WebAnnotationFactory.createFactory();
 
 		modelToInterfaceResource = new ManyToOneRelation<JavaEEObject, IFile>();
 		modelToResource = new ManyToOneRelation<JavaEEObject, IFile>();
 		for (IFile file : javaFiles) {
 			Result result = analyzeFile(file);
 			if (result == null)
 				continue;
 			processResult(file, result);
 		}
 	}
 
 	/**
 	 * Analyze this file for a bean. If the file is not a valid java compilation
 	 * unit or it does not contain a class the method returns <code>null</code>
 	 * 
 	 * Only the primary type of the compilation unit is processed.
 	 * 
 	 * @param file
 	 *            the file to be processed.
 	 * @return result from processing the file
 	 * @throws JavaModelException
 	 */
 	private Result analyzeFile(IFile file) throws JavaModelException {
 		org.eclipse.core.runtime.Assert.isTrue(JAVA_EXTENSION.equals(file.getFileExtension()),
 				"A file with extension different from \"java\" is analyzed for beans"); //$NON-NLS-1$
 		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);
 		if (compilationUnit == null)
 			return null;
 		IType rootType = compilationUnit.findPrimaryType();
 		/*
 		 * If the compilation unit is not valid and can not be compiled the
 		 * rootType may be null. This can happen if a class is define as follows
 		 * <code> @Stateless public SomeClass implements SomeInterface{}</code>.
 		 * Here the "class" word is missed and the type is not valid.
 		 */
 		if (rootType == null || !rootType.isClass())
 			return null;
 		for (Iterator iter = ddApp.getServlets().iterator(); iter.hasNext();) {
 			Servlet servlet = (Servlet) iter.next();
 			if (rootType.getFullyQualifiedName().equals(servlet.getServletClass()))
				return annotationFactory.createServlet(rootType, servlet.getServletName());
 		}
 		for (Iterator iter = ddApp.getListeners().iterator(); iter.hasNext();) {
 			Listener listener = (Listener) iter.next();
 			if (rootType.getFullyQualifiedName().equals(listener.getListenerClass()))
 				return annotationFactory.createListener(rootType);
 		}
 		for (Iterator iter = ddApp.getFilters().iterator(); iter.hasNext();) {
 			Filter filter = (Filter) iter.next();
 			if (rootType.getFullyQualifiedName().equals(filter.getFilterClass())) {
				return annotationFactory.createFilter(rootType, filter.getFilterName());
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Process the result from parsing the file. Depending on the result this
 	 * might include adding a session bean, message driven bean, securityRole
 	 * etc.
 	 * 
 	 * @see #sessionBeanFound(IFile, SessionBean, Collection)
 	 * @see #messageBeanFound(IFile, MessageDrivenBean, Collection)
 	 * @see #securityRoleFound(IFile, SecurityRole)
 	 * @param file
 	 * @param result
 	 * @throws JavaModelException
 	 */
 	private void processResult(IFile file, Result result) throws JavaModelException {
 		JavaEEObject mainObject = result.getMainObject();
 		if (Servlet.class.isInstance(mainObject))
 			servletFound(file, (Servlet) result.getMainObject(), result.getDependedTypes());
 		for (JavaEEObject additional : result.getAdditional()) {
 			if (EjbLocalRef.class.isInstance(additional)) {
 				ejbLocalRefFound(file, (EjbLocalRef) additional, result.getDependedTypes());
 			} else if (ResourceRef.class.isInstance(additional)) {
 				resourceRefFound(file, (ResourceRef) additional, result.getDependedTypes());
 			} else if (SecurityRole.class.isInstance(additional)) {
 				securityRoleFound(result.getMainObject(), (SecurityRole) additional);
 			} else if (SecurityIdentityType.class.isInstance(additional)) {
 				securityIdentityTypeFound(file, (SecurityIdentityType) additional);
 			}
 		}
 	}
 
 	private void servletFound(IFile file, Servlet servlet, Collection<IType> dependedTypes) throws JavaModelException {
 		modelObject.getServlets().add(servlet);
 		connectObjectWithFile(file, servlet, dependedTypes);
 	}
 
 	private void securityIdentityTypeFound(IFile file, SecurityIdentityType additional) {
 	}
 
 	private void resourceRefFound(IFile file, ResourceRef resourceRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelObject.getResourceRefs().add(resourceRef);
 		connectObjectWithFile(file, resourceRef, dependedTypes);
 	}
 
 	private void ejbLocalRefFound(IFile file, EjbLocalRef localRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelObject.getEjbLocalRefs().add(localRef);
 		connectObjectWithFile(file, localRef, dependedTypes);
 	}
 
 	private void connectObjectWithFile(IFile file, JavaEEObject localRef, Collection<IType> dependedTypes)
 			throws JavaModelException {
 		modelToResource.connect(localRef, file);
 		Collection<IFile> files = new HashSet<IFile>(dependedTypes.size());
 		for (IType type : dependedTypes) {
 			if (type.isBinary() || type.isInterface() == false)
 				continue;
 			IResource resource = type.getCompilationUnit().getCorrespondingResource();
 			if (resource != null && resource.exists())
 				modelToInterfaceResource.connect(localRef, (IFile) resource);
 		}
 	}
 
 	private void visitJavaFiles(final Collection<IFile> javaFiles, final IPackageFragmentRoot root)
 			throws CoreException {
 		if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
 			return;
 		root.getCorrespondingResource().accept(new IResourceProxyVisitor() {
 			public boolean visit(IResourceProxy proxy) throws CoreException {
 				if (proxy.getType() == IResource.FILE) {
 					if (proxy.getName().endsWith("." + JAVA_EXTENSION)) { //$NON-NLS-1$
 						IFile file = (IFile) proxy.requestResource();
 						if (root.getJavaProject().isOnClasspath(file))
 							javaFiles.add(file);
 					}
 					return false;
 				}
 				return true;
 			}
 		}, IContainer.NONE);
 
 	}
 
 	@Override
 	protected boolean isProjectRelative(IProject project) {
 		if (project == null)
 			return false;
 		else if (project.equals(facetedProject.getProject()))
 			return true;
 		return false;
 	}
 
 	@Override
 	public void dispose() {
 //		Job disposeJob = new Job(Messages.getString("WebAnnotationReader.DisposeWebAnnotationReader")) { //$NON-NLS-1$
 //			@Override
 //			protected IStatus run(IProgressMonitor monitor) {
 				IModelProviderEvent modelEvent = createModelProviderEvent();
 				modelEvent.addResource(facetedProject.getProject());
 				modelEvent.setEventCode(modelEvent.getEventCode() | IModelProviderEvent.PRE_DISPOSE);
 				ResourcesPlugin.getWorkspace().removeResourceChangeListener(WebAnnotationReader.this);
 				modelToResource = null;
 				modelObject = null;
 				notifyListeners(modelEvent);
 				clearListeners();
 //				return Status.OK_STATUS;
 //			}
 //		};
 //		disposeJob.schedule();
 	}
 
 	@Override
 	protected synchronized void processAddedFile(IModelProviderEvent modelEvent, IFile file) throws CoreException {
 		Result result = analyzeFile(file);
 		if (result == null)
 			return;
 		processResult(file, result);
 		modelEvent.addResource(file);
 	}
 
 	@Override
 	protected synchronized void processChangedFile(IModelProviderEvent modelEvent, IFile file) throws CoreException {
 		if (modelToResource.containsTarget(file))
 			processChangedModelFile(modelEvent, file);
 		else
 			processAddedFile(modelEvent, file);
 	}
 
 	private void processChangedModelFile(IModelProviderEvent modelEvent, IFile file) throws CoreException {
 		processRemovedFile(modelEvent, file);
 		processAddedFile(modelEvent, file);
 	}
 
 	@Override
 	protected synchronized void processRemovedFile(IModelProviderEvent modelEvent, IFile file) throws CoreException {
 		if (modelToResource.containsTarget(file))
 			processRemovedModelResource(modelEvent, file);
 		else if (modelToInterfaceResource.containsTarget(file))
 			processRemoveInterface(modelEvent, file);
 	}
 
 	private void processRemoveInterface(IModelProviderEvent event, IFile file) {
 	}
 
 	private void processRemovedModelResource(IModelProviderEvent event, IFile file) {
 		Collection<JavaEEObject> modelObjects = modelToResource.getSources(file);
 		for (JavaEEObject o : modelObjects) {
 			if (Servlet.class.isInstance(o))
 				disconnectFromRoles(o);
 			EcoreUtil.remove((EObject) o);
 		}
 		modelToResource.disconnect(file);
 		event.setEventCode(event.getEventCode() | IModelProviderEvent.REMOVED_RESOURCE);
 		event.addResource(file);
 	}
 
 	public void modify(Runnable runnable, IPath modelPath) {
 	}
 
 	public IStatus validateEdit(IPath modelPath, Object context) {
 		return null;
 	}
 
 	@Override
 	protected Collection<SecurityRoleRef> getSecurityRoleRefs(JavaEEObject target) {
 		if (Servlet.class.isInstance(target))
 			return ((Servlet) target).getSecurityRoleRefs();
 		return null;
 	}
 
 	@Override
 	protected Collection<SecurityRole> getSecurityRoles() {
 		return modelObject.getSecurityRoles();
 	}
 
 }

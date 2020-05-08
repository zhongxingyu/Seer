 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.internal.actions;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.ejb.componentcore.util.EJBArtifactEdit;
 import org.eclipse.jst.j2ee.internal.ejb.provider.J2EEJavaClassProviderHelper;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEEditorUtility;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.Servlet;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webservice.wsdd.BeanLink;
 import org.eclipse.jst.j2ee.webservice.wsdd.EJBLink;
 import org.eclipse.jst.j2ee.webservice.wsdd.ServletLink;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ISetSelectionTarget;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 /**
  * Action for opening a J2EE resource from the J2EE navigator.
  */
 public class OpenJ2EEResourceAction extends AbstractOpenAction {
 	
 	public static final String ID = "org.eclipse.jst.j2ee.internal.internal.ui.actions.OpenJ2EEResourceAction"; //$NON-NLS-1$
 	public static final String JAVA_EDITOR_ID = "org.eclipse.jst.j2ee.internal.internal.ejb.ui.java.EnterpriseBeanJavaEditor"; //$NON-NLS-1$
 	public static final String BASE_JAVA_EDITOR_ID = "org.eclipse.jdt.ui.CompilationUnitEditor"; //$NON-NLS-1$
 	
 	protected static IEditorDescriptor javaEditorDescriptor;
 	protected static IEditorDescriptor baseJavaEditorDescriptor;
 
 	/**
 	 * Create an instance of this class
 	 */
 	public OpenJ2EEResourceAction() {
 		super(""); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the action ID.
 	 */
 	public String getID() {
 		return ID;
 	}
 
 	public static IEditorDescriptor getJavaEditorDescriptor() {
 		if (javaEditorDescriptor == null)
 			javaEditorDescriptor = findEditorDescriptor(JAVA_EDITOR_ID);
 		return javaEditorDescriptor;
 	}
 
 	public static IEditorDescriptor getBaseJavaEditorDescriptor() {
 		if (baseJavaEditorDescriptor == null)
 			baseJavaEditorDescriptor = findEditorDescriptor(BASE_JAVA_EDITOR_ID);
 		return baseJavaEditorDescriptor;
 	}
 	
 	/**
 	 * open the appropriate editor
 	 */
 	protected void openAppropriateEditor(IResource r) {
 		if (r == null)
 			return;
 		IWorkbenchPage page = null;
 		IEditorPart editor = null;
 		try {
 			page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 			editor = page.openEditor(new FileEditorInput((IFile) r), currentDescriptor.getId());
 			if (editor instanceof ISetSelectionTarget)
 				((ISetSelectionTarget) editor).selectReveal(getStructuredSelection());
 		} catch (Exception e) {
 			MessageDialog.openError(page.getWorkbenchWindow().getShell(), J2EEUIMessages.getResourceString("Problems_Opening_Editor_ERROR_"), e.getMessage()); //$NON-NLS-1$ = "Problems Opening Editor"
 		}
 	}
 	
 	/**
 	 * The user has invoked this action
 	 */
 	public void run() {
 		if (!isEnabled())
 			return;
 		
 		if (srcObject instanceof J2EEJavaClassProviderHelper) {
 			((J2EEJavaClassProviderHelper) srcObject).openInEditor();
 			return;
 		}
 		if (srcObject instanceof EObject) {
 			EObject ro = (EObject) srcObject;
 			IProject p = ProjectUtilities.getProject(ro);
 			if (ro instanceof BeanLink) {
 				openBeanLinkInJavaEditor((BeanLink) ro, p);
 				return;
 			}
 			openAppropriateEditor(WorkbenchResourceHelper.getFile((EObject)srcObject));
 		}
 		else if (srcObject instanceof Resource)
 			openAppropriateEditor(WorkbenchResourceHelper.getFile((Resource)srcObject));
 	}
 
 	/**
 	 * The structured selection has changed in the workbench. Subclasses should override this method
 	 * to react to the change. Returns true if the action should be enabled for this selection, and
 	 * false otherwise.
 	 * 
 	 * When this method is overridden, the super method must always be invoked. If the super method
 	 * returns false, this method must also return false.
 	 * 
 	 * @param sel the new structured selection
 	 */
 	public boolean updateSelection(IStructuredSelection s) {
 		if (!super.updateSelection(s))
 			return false;
 
 		// Make sure this is one of the selections we can handle,
 		// then set the source object as is. The run() will do the hard stuff.
 		Object obj = s.getFirstElement();
 		
 		if (obj instanceof J2EEJavaClassProviderHelper)
 			currentDescriptor = getJavaEditorDescriptor();
 		else if (obj instanceof BeanLink)
 			currentDescriptor = getBaseJavaEditorDescriptor();	
 		else if (obj instanceof EObject) {
 			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			currentDescriptor = registry.getDefaultEditor(WorkbenchResourceHelper.getFile((EObject)obj).getName());
 		}
 		else if (obj instanceof Resource) {
 			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			currentDescriptor = registry.getDefaultEditor(WorkbenchResourceHelper.getFile((Resource)obj).getName());
 		}
 		else {
 			currentDescriptor = null;
 			return false;
 		}
 		setAttributesFromDescriptor();
 		srcObject = obj;
 		return true;
 	}
 	
 	/**
 	 * @param link
 	 */
 	private void openBeanLinkInJavaEditor(BeanLink link, IProject p) {
 		String linkName = null;
 		JavaClass javaClass = null;
 		IVirtualComponent comp = ComponentUtilities.findComponent(link);
 		// Handle EJB Link case
 		if (link instanceof EJBLink) {
 			linkName = ((EJBLink) link).getEjbLink();
 			EJBArtifactEdit artifactEdit = null;
 			try {
 				artifactEdit = EJBArtifactEdit.getEJBArtifactEditForRead(comp);
 				EJBJar ejbJar = artifactEdit.getEJBJar();
 				if (ejbJar == null)
 					return;
 				EnterpriseBean bean = ejbJar.getEnterpriseBeanNamed(linkName);
 				if (bean == null)
 					return;
 				javaClass = bean.getEjbClass();
 			} finally {
 				if (artifactEdit!=null)
 					artifactEdit.dispose();
 			}
 		}
 		// Handle Servlet Link case
 		else {
 			linkName = ((ServletLink) link).getServletLink();
 			WebArtifactEdit artifactEdit = null;
 			try {
 				artifactEdit = WebArtifactEdit.getWebArtifactEditForRead(comp);
 				WebApp webApp = artifactEdit.getWebApp();
 			if (webApp == null)
 				return;
 			Servlet servlet = webApp.getServletNamed(linkName);
 			if (servlet == null)
 				return;
 			javaClass = servlet.getServletClass();
 			} finally {
 				if (artifactEdit!=null)
 					artifactEdit.dispose();
 			}
 		}
 		// Open java editor on the selected objects associated java file
 		try {
 			J2EEEditorUtility.openInEditor(javaClass, p);
 		} catch (Exception cantOpen) {
 			cantOpen.printStackTrace();
 		}
 	}
 
 	protected EObject getRootObject(Object obj) {
 		if (obj instanceof EObject) {
 			EObject refObj = (EObject) obj;
 			while (refObj != null && refObj.eContainer() != null)
 				refObj = refObj.eContainer();
 			return refObj;
 		}
 		return null;
 	}
 }

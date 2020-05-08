 /*******************************************************************************
  * Copyright (c) 2007, 2008 SAP AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Kaloyan Raev, kaloyan.raev@sap.com - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.servlet.ui.internal.wizard;
 
 import static org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel.USE_ANNOTATIONS;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CLASS_NAME;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.GENERATE_DD;
 import static org.eclipse.jst.servlet.ui.internal.wizard.IWebWizardConstants.BROWSE_BUTTON_LABEL;
 import static org.eclipse.jst.servlet.ui.internal.wizard.IWebWizardConstants.CLASS_NAME_LABEL;
 import static org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties.PROJECT_NAME;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.J2EEFileUtil;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.wizard.AnnotationsStandaloneGroup;
 import org.eclipse.jst.j2ee.internal.wizard.NewJavaClassWizardPage;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.project.WebUtilities;
 import org.eclipse.jst.j2ee.web.IServletConstants;
 import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
 import org.eclipse.jst.servlet.ui.internal.navigator.CompressedJavaProject;
 import org.eclipse.jst.servlet.ui.internal.plugin.ServletUIPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public abstract class NewWebClassWizardPage extends NewJavaClassWizardPage {
 
 	protected AnnotationsStandaloneGroup annotationsGroup;
 	
 	protected Button existingClassButton;
 	protected Label existingClassLabel;
 	protected Text existingClassText;
 	protected Button existingButton;
 	
 	public NewWebClassWizardPage(IDataModel model, String pageName, String pageDesc, String pageTitle, String moduleType) {
 		super(model, pageName, pageDesc, pageTitle, moduleType);
 	}
 	
 	@Override
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite composite = super.createTopLevelComposite(parent);
 		
 		//bug 303931
 		// projectNameLabel.setText(WEBUIMessages.WEB_PROJECT_LBL);
 		
 		addSeperator(composite, 3);
 		createUseExistingGroup(composite);
 		createAnnotationsGroup(composite);
 		
 		Dialog.applyDialogFont(composite);
 		
 		return composite;
 	}
 	
 	@Override
 	protected IProject getExtendedSelectedProject(Object selection) {
 		if (selection instanceof CompressedJavaProject) {
 			return ((CompressedJavaProject) selection).getProject().getProject();
 		}
 		
 		return super.getExtendedSelectedProject(selection);
 	}
 	
 	protected abstract String getUseExistingCheckboxText();
 	
 	protected abstract String getUseExistingProperty();
 	
 	protected abstract void handleClassButtonSelected();
 	
 	/**
 	 * Create annotations group and set default enablement
 	 */
 	private void createAnnotationsGroup(Composite parent) {
 		if (isWebDocletProject()) {
 			annotationsGroup = new AnnotationsStandaloneGroup(parent, model, J2EEProjectUtilities.EJB.equals(projectType),
 					J2EEProjectUtilities.DYNAMIC_WEB.equals(projectType));
 			if (!model.isPropertySet(PROJECT_NAME))
 				return;
 			IProject project = ProjectUtilities.getProject(model.getStringProperty(PROJECT_NAME));
 			annotationsGroup.setEnablement(project);
 			// annotationsGroup.setUseAnnotations(true);
 		} else {
 			// not a Web Doclet project - make sure that the USE_ANNOTATIONS property is off
 			model.setProperty(USE_ANNOTATIONS, false);
 		}
 	}
 
 	private void createUseExistingGroup(Composite composite) {
 		existingButton = new Button(composite, SWT.CHECK);
 		existingButton.setText(getUseExistingCheckboxText());
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		data.horizontalSpan = 3;
 		existingButton.setLayoutData(data);
 		synchHelper.synchCheckbox(existingButton, getUseExistingProperty(), null);
 		existingButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleExistingButtonSelected();
 			}
 		});
 		
 		existingClassLabel = new Label(composite, SWT.LEFT);
 		existingClassLabel.setText(CLASS_NAME_LABEL);
 		existingClassLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 		existingClassLabel.setEnabled(false);
 
 		existingClassText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
 		existingClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		existingClassText.setEnabled(false);
 		synchHelper.synchText(existingClassText, CLASS_NAME, null);
 
 		existingClassButton = new Button(composite, SWT.PUSH);
 		existingClassButton.setText(BROWSE_BUTTON_LABEL);
 		existingClassButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 		existingClassButton.setEnabled(false);
 		existingClassButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleClassButtonSelected();
 			}
 		});
 	}
 	
 	private void handleExistingButtonSelected() {
 		boolean enable = existingButton.getSelection();
 		existingClassLabel.setEnabled(enable);
 		existingClassButton.setEnabled(enable);
 		packageText.setEnabled(!enable);
 		packageButton.setEnabled(!enable);
 		packageLabel.setEnabled(!enable);
 		classText.setEnabled(!enable);
 		classLabel.setEnabled(!enable);
 		superText.setEnabled(!enable);
 		superButton.setEnabled(!enable);
 		superLabel.setEnabled(!enable);
 	}
 
 	private boolean isWebDocletProject() {
 		String projectName = model.getStringProperty(PROJECT_NAME);
 		if(projectName != null && !"".equals(projectName.trim())){ //$NON-NLS-1$
 			IProject project = ProjectUtilities.getProject(projectName);
 			try {
 				IFacetedProject facetedProject = ProjectFacetsManager.create(project);
 				return facetedProject.hasProjectFacet(WebFacetUtils.WEB_XDOCLET_FACET);
 			} catch (CoreException e) {
 				ServletUIPlugin.log(e);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected void validateProjectRequirements(IProject project)
 	{
 		if(JavaEEProjectUtilities.isWebFragmentProject(project)){
 			// don't create a web DD, since web fragment project is created with one.
 			model.setBooleanProperty(GENERATE_DD, Boolean.FALSE);
 			return;
 		}
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if(component.getRootFolder() != null
 				&& component.getRootFolder().getUnderlyingFolder() != null){
			IFile ddXmlFile = component.getRootFolder().getUnderlyingFolder().getFile(new Path(J2EEConstants.WEBAPP_DD_URI));
 			if (!ddXmlFile.exists())
 			{
 				// add a flag into the model to create the DD at the beginning of the operation
 				model.setBooleanProperty(GENERATE_DD, Boolean.TRUE);
 			}
 			else
 			{
 				// don't create a DD, since one already exists.
 				model.setBooleanProperty(GENERATE_DD, Boolean.FALSE);
 			}
 		}
 	}
 
 	protected Object getSelectedObject() {
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (window == null)
 			return null;
 		ISelection selection = window.getSelectionService().getSelection();
 		if (selection == null)
 			return null;
 		if (!(selection instanceof IStructuredSelection)) 
 			return null;
 		IStructuredSelection ssel = (IStructuredSelection) selection;
 		if (ssel.size() != 1)
 			return null;
 		return ssel.getFirstElement();
 	}
 	
 	private IType getPrimaryType(ICompilationUnit cu) {
 		return cu.getType(new Path(cu.getElementName()).removeFileExtension().toString());
 	}
 	
 	private boolean isObjectSubclassOf(Object obj, String base) {
 		IJavaElement jelem = getJavaElement(obj);
 		
 		try {
 			if (jelem != null) {
 				if (jelem instanceof ICompilationUnit) {
 					ICompilationUnit cu = (ICompilationUnit) jelem;
 					jelem = getPrimaryType(cu);
 				}
 				
 				if (jelem instanceof IType && ((IType) jelem).isClass()) {
 					IType type = (IType) jelem;
 					ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
 					for (IType superType : typeHierarchy.getAllSuperInterfaces(type)) {
 						if (base.equals(superType.getFullyQualifiedName()))
 							return true;
 					}
 				} 
 			}
 		} catch (JavaModelException e) {
 			ServletUIPlugin.log(e);
 		}
 		
 		return false;
 	}
 	
 	protected boolean isServlet(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Servlet) 
 			return true;
 		
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Servlet)
 			return true;
 		
 		return isObjectSubclassOf(obj, IServletConstants.QUALIFIED_SERVLET);
 	}
 	
 	protected boolean isFilter(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Filter) 
 			return true;
 		
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Filter)
 			return true;
 		
 		return isObjectSubclassOf(obj, IServletConstants.QUALIFIED_FILTER);
 	}
 	
 	private String getJavaClass(Object obj) {
 		IJavaElement jelem = getJavaElement(obj); 
 		
 		if (jelem == null) {
 			throw new IllegalArgumentException("the object parameter must be instance of IJavaElement"); //$NON-NLS-1$
 		}
 		
 		if (jelem instanceof ICompilationUnit) {
 			ICompilationUnit cu = (ICompilationUnit) jelem;
 			jelem = getPrimaryType(cu);
 		}
 		return ((IType) jelem).getFullyQualifiedName();		
 	}
 	
 	protected String getServletClass(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Servlet) {
 			org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) obj;
 			return servlet.getServletClass();
 		} 
 
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Servlet) {
 			org.eclipse.jst.j2ee.webapplication.Servlet servlet = (org.eclipse.jst.j2ee.webapplication.Servlet) obj;
 			return servlet.getServletClass().getQualifiedName();
 		}
 		
 		return getJavaClass(obj);
 	}
 	
 	protected String getFilterClass(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Filter) {
 			org.eclipse.jst.javaee.web.Filter filter = (org.eclipse.jst.javaee.web.Filter) obj;
 			return filter.getFilterClass();
 		} 
 
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Filter) {
 			org.eclipse.jst.j2ee.webapplication.Filter filter = (org.eclipse.jst.j2ee.webapplication.Filter) obj;
 			return filter.getFilterClass().getQualifiedName();
 		}
 		
 		return getJavaClass(obj);
 	}
 	
 	protected String getServletName(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Servlet) {
 			org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) obj;
 			return servlet.getServletName();
 		} 
 
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Servlet) {
 			org.eclipse.jst.j2ee.webapplication.Servlet servlet = (org.eclipse.jst.j2ee.webapplication.Servlet) obj;
 			return servlet.getServletName();
 		}
 		
 		String servletClass = getServletClass(obj);
 		IProject project = getJavaElement(obj).getJavaProject().getProject();
 		IModelProvider provider = ModelProviderManager.getModelProvider(project);
 		Object modelObject = provider.getModelObject();
 		if (modelObject instanceof org.eclipse.jst.javaee.web.WebApp) {
 			org.eclipse.jst.javaee.web.WebApp webApp = (org.eclipse.jst.javaee.web.WebApp) modelObject;
 			Iterator servlets = webApp.getServlets().iterator();
 			while (servlets.hasNext()) {
 				org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) servlets.next();
 				String qualified = servlet.getServletClass(); 
 				if (qualified.equals(servletClass)) {
 					return servlet.getServletName();
 				}
 			}
 		} else if (modelObject instanceof org.eclipse.jst.j2ee.webapplication.WebApp) {
 			org.eclipse.jst.j2ee.webapplication.WebApp webApp = (org.eclipse.jst.j2ee.webapplication.WebApp) modelObject;
 			Iterator servlets = webApp.getServlets().iterator();
 			while (servlets.hasNext()) {
 				org.eclipse.jst.j2ee.webapplication.Servlet servlet = (org.eclipse.jst.j2ee.webapplication.Servlet) servlets.next();
 				String qualified = servlet.getServletClass().getQualifiedName(); 
 				if (qualified.equals(servletClass)) {
 					return servlet.getServletName();
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	protected boolean isServletJSP(Object obj) {
 		return getServletJSPFile(obj) != null;
 	}
 	
 	protected String getServletJSPFile(Object obj) {
 		if (obj instanceof org.eclipse.jst.javaee.web.Servlet) {
 			org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) obj;
 			return servlet.getJspFile();
 		} 
 
 		if (obj instanceof org.eclipse.jst.j2ee.webapplication.Servlet) {
 			org.eclipse.jst.j2ee.webapplication.Servlet servlet = (org.eclipse.jst.j2ee.webapplication.Servlet) obj;
 			org.eclipse.jst.j2ee.webapplication.WebType webType = servlet.getWebType();
 			if (webType.isJspType()) {
 				org.eclipse.jst.j2ee.webapplication.JSPType jspType = (org.eclipse.jst.j2ee.webapplication.JSPType) webType;
 				return jspType.getJspFile();
 			}
 		}
 		
 		return null;
 	}
 	
 	protected boolean isWebFolder(Object obj) {
 		if (obj instanceof IFolder) {
 			return WebUtilities.isWebResource(obj);
 		}
 		return false;
 	}
 	
 	protected boolean isJSP(Object obj) {
 		if (obj instanceof IFile) {
 			IFile file = (IFile) obj;
 			return WebUtilities.isWebResource(file) && file.getName().endsWith(J2EEFileUtil.DOT_JSP);
 		}
 		return false;
 	}
 
 	protected String getWebResourcePath(IResource resource) {
 		IVirtualComponent comp = ComponentCore.createComponent(resource.getProject());
 		if (comp != null) {
 			IPath rootPath = comp.getRootFolder().getWorkspaceRelativePath();
 			return "/" + resource.getFullPath().makeRelativeTo(rootPath).toString(); //$NON-NLS-1$
 		}
 		return null;
 	}
 	
 	protected String makeFirstCharUppercase(String str) {
 		if (str == null || str.length() == 0) 
 			return str;
 		
 		StringBuilder builder = new StringBuilder(str);
 		builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
 		return builder.toString();
 	}
 	
 	protected String getFileNameWithouFileExtension(IFile file) {
 		String name = file.getName();
 		String ext = file.getFileExtension();
 		
 		if (ext == null) 
 			return name;
 		
 		return name.substring(0, name.length() - (ext.length() + 1));
 	}
 	
 	protected void checkExistingButton(boolean state) {
 		existingButton.setSelection(state);
 		existingButton.notifyListeners(SWT.Selection, new Event());
 	}
 	
 	@Override
 	protected boolean isProjectValid(IProject project) {
 		boolean result;
 		try {
 			result = project.isAccessible() && 
 				project.hasNature(IModuleConstants.MODULE_NATURE_ID) && 
 			 	(JavaEEProjectUtilities.isDynamicWebProject(project) || JavaEEProjectUtilities.isWebFragmentProject(project));
 		} catch (CoreException ce) {
 			result = false;
 		}
 		return result;
 	}
 	
 }

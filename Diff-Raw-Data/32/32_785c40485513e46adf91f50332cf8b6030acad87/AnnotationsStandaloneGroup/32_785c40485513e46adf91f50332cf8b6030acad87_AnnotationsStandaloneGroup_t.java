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
 /*
  * Created on Mar 29, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPDataModelSynchHelper;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class AnnotationsStandaloneGroup {
 
 	protected WTPOperationDataModel model;
 	protected WTPDataModelSynchHelper synchHelper;
 	protected Button useAnnotations;
 	private boolean isForBean;
 	private boolean useServletString = false;
 
 	public static final String EJBTAGSET = "ejb"; //$NON-NLS-1$
 	public static boolean shouldBeanDefaultUseAnnotations = false;
 	public static boolean shouldProjectDefaultUseAnnotations = false;
 
 
 	private class CheckboxSelectionListener implements SelectionListener {
 		public void widgetSelected(SelectionEvent e) {
 			Button button = (Button) e.getSource();
 			if (isForBean)
 				shouldBeanDefaultUseAnnotations = button.getSelection();
 			else
 				shouldProjectDefaultUseAnnotations = button.getSelection();
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			//do nothing
 		}
 	}
 
 	private CheckboxSelectionListener checkboxSelectionListener = new CheckboxSelectionListener();
 
 	/**
 	 * Constructor
 	 */
 	public AnnotationsStandaloneGroup(Composite parent, WTPOperationDataModel model, boolean forBean) {
 		this(parent, model, forBean, false);
 	}
 
 	/**
 	 * Constructor
 	 */
 	public AnnotationsStandaloneGroup(Composite parent, WTPOperationDataModel model, boolean forBean, boolean useServlet) {
 		super();
 		this.model = model;
 		this.isForBean = forBean;
 		this.useServletString = useServlet;
 		synchHelper = new WTPDataModelSynchHelper(model);
 		buildComposites(parent);
 	}
 
 	/**
 	 * @param parent
 	 */
 	protected void buildComposites(Composite parent) {
 		// Add separator
 		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 3;
 		separator.setLayoutData(gd);
 		// Add spacer
 		Label spacer = new Label(parent, SWT.NONE);
 		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
 		gd1.horizontalSpan = 3;
 		spacer.setLayoutData(gd1);
 		// Add annotations checkbox and label
 		useAnnotations = new Button(parent, SWT.CHECK);
 		String labelText;
 		if (useServletString)
 			labelText = J2EEUIMessages.getResourceString(J2EEUIMessages.USE_ANNOTATIONS_SERVLET);
 		else if (isForBean)
 			labelText = J2EEUIMessages.getResourceString(J2EEUIMessages.USE_ANNOTATIONS);
 		else
 			labelText = J2EEUIMessages.getResourceString(J2EEUIMessages.ADD_ANNOTATIONS_SUPPORT);
 		useAnnotations.setText(labelText);
 		synchHelper.synchCheckbox(useAnnotations, IAnnotationsDataModel.USE_ANNOTATIONS, null);
 		useAnnotations.addSelectionListener(checkboxSelectionListener);
 		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
 		gd2.horizontalSpan = 2;
 		useAnnotations.setLayoutData(gd2);
 		// If for project, set the global annotations supported
 		if (!isForBean)
 			setEnablement(null);
 	}
 
 	public void dispose() {
 		model.removeListener(synchHelper);
 		synchHelper = null;
 		model = null;
 	}
 
 	public boolean isAnnotationsSupported(IProject project) {
		//TODO clean up to be module based, not project based
 		try {
 			//			if (isForBean) {
 			//				AnnotationsController controller =
 			// AnnotationsControllerManager.INSTANCE.getAnnotationsController(project);
 			//				return (controller !=null && controller.isTagHandlerInstalled(EJBTAGSET));
 			//			}
//			J2EEModuleNature nature = null;
//			if (project != null && project.hasNature(IWebNatureConstants.J2EE_NATURE_ID))
//				nature = (J2EEModuleNature) project.getNature(IWebNatureConstants.J2EE_NATURE_ID);
//			else if (project != null && project.hasNature(IEJBNatureConstants.NATURE_ID))
//				nature = (J2EEModuleNature) project.getNature(IEJBNatureConstants.NATURE_ID);
//
//			if (!isForBean || (nature != null && nature.getJ2EEVersion() > J2EEVersionConstants.VERSION_1_2))
 				return true;
//			return false;
 		} catch (Throwable t) {
 			t.printStackTrace();
 			return false;
 		}
 	}
 
 	public void setEnablement(IProject project) {
 		//TODO
 		//boolean isEnabled = isAnnotationsSupported(project);
 		useAnnotations.setEnabled(true);
 //		if (!isEnabled || (!isForBean && !shouldProjectDefaultUseAnnotations) || (isForBean && !shouldBeanDefaultUseAnnotations)) {
 //			useAnnotations.setSelection(false);
 //			model.setProperty(IAnnotationsDataModel.USE_ANNOTATIONS, Boolean.FALSE);
 //		} else {
 //			useAnnotations.setSelection(true);
 //			model.setProperty(IAnnotationsDataModel.USE_ANNOTATIONS, Boolean.TRUE);
 //		}
 	}
 
 	public void setUseServlet(boolean aBoolean) {
 		useServletString = aBoolean;
 	}
 	
 	public void setUseAnnotations(boolean aBoolean) {
 		if (useAnnotations != null) {
 			useAnnotations.setSelection(aBoolean);
 			model.setProperty(IAnnotationsDataModel.USE_ANNOTATIONS, new Boolean(aBoolean));
 		}
 	}
 }

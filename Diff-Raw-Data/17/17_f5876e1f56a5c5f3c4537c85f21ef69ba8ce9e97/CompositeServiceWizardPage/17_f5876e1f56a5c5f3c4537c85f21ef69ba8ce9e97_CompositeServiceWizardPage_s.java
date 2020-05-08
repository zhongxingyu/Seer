 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.ui.wizards;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 
 
 
 
 /**
  * class PluginContentWsdlWizardPage
  * 
  * @author akopachevsky
  */
 public class CompositeServiceWizardPage extends WizardPage {
	

	
    private CheckboxTableViewer consumerProjectViewer;
 
     private static final String COMPOSITE_SERVICES_PAGE_TITLE = "Composite Services Page";
     private static final String COMPOSITE_SERVICES_PAGE_DESCRIPTION = "Select JAX-WS Consumer Projects";
     
     private static final String AVAIABLE_PROJECT_LIST = "Available JAX-WS Consumer Projects";
 
     private static final int PROJECT_LIST_MULTIPLIER = 15;
 
     
 	protected CompositeServiceWizardPage() {
 		super(COMPOSITE_SERVICES_PAGE_TITLE);
 		setTitle(COMPOSITE_SERVICES_PAGE_TITLE);
 		setDescription(COMPOSITE_SERVICES_PAGE_DESCRIPTION);
 	}
 
 
     public void createControl(Composite parent) {

         Font font = parent.getFont();
 
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout());
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         composite.setFont(font);
 
 
         Label referenceLabel = new Label(composite, SWT.NONE);
         referenceLabel.setText(AVAIABLE_PROJECT_LIST);
         referenceLabel.setFont(font);

        consumerProjectViewer = CheckboxTableViewer.newCheckList(composite,SWT.BORDER);
         
         consumerProjectViewer.getTable().setFont(composite.getFont());
         GridData data = new GridData();
         data.horizontalAlignment = GridData.FILL;
         data.grabExcessHorizontalSpace = true;
 
         data.heightHint = getDefaultFontHeight(consumerProjectViewer.getTable(), PROJECT_LIST_MULTIPLIER);
         consumerProjectViewer.getTable().setLayoutData(data);
         consumerProjectViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
         consumerProjectViewer.setContentProvider(getContentProvider());
         consumerProjectViewer.setComparator(new ViewerComparator());
         consumerProjectViewer.setInput(ResourcesPlugin.getWorkspace());
         consumerProjectViewer.addFilter(new JaxWsConsumerProjectFilter());
         setControl(composite);
     }
 
 
     protected IStructuredContentProvider getContentProvider() {
         return new WorkbenchContentProvider() {
             public Object[] getChildren(Object element) {
                 if (!(element instanceof IWorkspace)) {
 					return new Object[0];
 				}
                 IProject[] projects = ((IWorkspace) element).getRoot()
                         .getProjects();
                 return projects == null ? new Object[0] : projects;
             }
         };
     }
 
 
     private static int getDefaultFontHeight(Control control, int lines) {
         FontData[] viewerFontData = control.getFont().getFontData();
         int fontHeight = 10;
 
         //If we have no font data use our guess
         if (viewerFontData.length > 0) {
 			fontHeight = viewerFontData[0].getHeight();
 		}
         return lines * fontHeight;
 
     }
 
 
     public IProject[] getSelectedConsumerProjects() {
     	IProject[] projects = new IProject[0];
    	if(consumerProjectViewer != null) {
     		Object[] elements = consumerProjectViewer.getCheckedElements();
     		projects = new IProject[elements.length];
     		System.arraycopy(elements, 0, projects, 0, elements.length);
     	}
         return projects;
     }
 }
 
 

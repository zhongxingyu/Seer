 /*******************************************************************************
  * Copyright (c) 2006 University College London Software Systems Engineering
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	Bruno Wassermann - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.bpel.runtimes.ui.wizards;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.web.ui.internal.wizards.DataModelFacetCreationWizardPage;
 import org.jboss.tools.bpel.runtimes.IBPELModuleFacetConstants;
 import org.jboss.tools.bpel.runtimes.IRuntimesUIConstants;
 import org.jboss.tools.bpel.runtimes.RuntimesPlugin;
 
 /**
  * <code>DataModelFacetCreationWizardPage</code> for setting up a new BPEL
  * project.
  *
  * @author Bruno Wassermann, written Jun 29, 2006
  */
 public class NewBPELProjectWizardPage1 extends DataModelFacetCreationWizardPage {
 
 	public NewBPELProjectWizardPage1(IDataModel dataModel, String pageName) {
 		super(dataModel, pageName);
 		setTitle(org.jboss.tools.bpel.runtimes.ui.wizards.Messages.NewProjectWizard_1);
 		setDescription(org.jboss.tools.bpel.runtimes.ui.wizards.Messages.NewProjectWizardPage1_1);		
 		setImageDescriptor(RuntimesPlugin.getPlugin().getImageDescriptor(IRuntimesUIConstants.ICON_NEWPRJ_WIZARD_BANNER));
 	}
 	
 	protected String getModuleFacetID() {
 		return IBPELModuleFacetConstants.BPEL_MODULE_TYPE;
 	}
 
 	protected String getModuleTypeID() {
 		return IBPELModuleFacetConstants.BPEL_PROJECT_FACET;
 	}
 	
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite top = new Composite(parent, SWT.NONE);
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, getInfopopID());
 		top.setLayout(new GridLayout());
 		top.setLayoutData(new GridData(GridData.FILL_BOTH));
 		createProjectGroup(top);
 		createServerTargetComposite(top);
 //		createPrimaryFacetComposite(top);
		primaryProjectFacet = ProjectFacetsManager.getProjectFacet( getModuleFacetID() );
 		createPresetPanel(top);
         return top;
 	}
 }

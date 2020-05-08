 package org.eclipse.stem.ui.populationmodels.standard.wizards;
 
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.stem.populationmodels.standard.PopulationInitializer;
 import org.eclipse.stem.populationmodels.standard.PopulationModel;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.widgets.LocationPickerDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * This class represents the SWT control for defining a disease.
  */
 public class PopulationInitializerDefinitionControl extends Composite {
 
 	private static PopulationInitializer[] populationInitializers = null;
 
 	private final Combo combo;
 
 	private final PopulationInitializerPropertyComposite populationInitializerPropertyComposite;
 
 	private String isoKey="";
 
 	
 	/**
 	 * Create the composite
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param projectValidator
 	 */
 	public PopulationInitializerDefinitionControl(final Composite parent, final int style,
 			ModifyListener projectValidator) {
 		super(parent, style);
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.marginWidth = 0;
 		gridLayout.numColumns = 2;
 		setLayout(gridLayout);
 
		final Label populationModelLabel = new Label(this, SWT.NONE);
		final GridData gd_populationModelLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_populationModelLabel.widthHint = 100;
		populationModelLabel.setLayoutData(gd_populationModelLabel);
		populationModelLabel.setText(PopulationModelWizardMessages.getString("DDC.0")); //$NON-NLS-1$
 
 		combo = new Combo(this, SWT.READ_ONLY);
 		combo.setTextLimit(30);
 		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true,
 				false);
 		gd_combo.widthHint = 303;
 		combo.setLayoutData(gd_combo);
 
 		combo.setToolTipText(PopulationModelWizardMessages.getString("DDC.1")); //$NON-NLS-1$
 		// Initialize the list of disease models available
 		combo.setItems(getPopulationInitializerNames(getPopulationInitializers()));
 		combo.select(0);
 		combo.addModifyListener(projectValidator);
 
 		
 		populationInitializerPropertyComposite = new PopulationInitializerPropertyComposite(this,
 				SWT.NONE, getPopulationInitializers(), projectValidator);
 		final GridData gd_populationInitializerPropertyControl = new GridData(SWT.FILL,
 				SWT.CENTER, true, false, 2, 1);
 		populationInitializerPropertyComposite
 				.setLayoutData(gd_populationInitializerPropertyControl);
 
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				populationInitializerPropertyComposite
 						.displayPopulationInitializer(getPopulationInitializers()[combo
 								.getSelectionIndex()]);
 				// layout();
 			} // widgetSelected
 
 		});
 		final PopulationInitializerDefinitionControl self = this;
 
 		// Location picker
 		// ISO Key
 		final Label isoKeyLabel = new Label(this, SWT.NONE);
 		isoKeyLabel.setText(PopulationModelWizardMessages.getString("NPopWizISOK")); //$NON-NLS-1$
 		final GridData gd_isoKeyLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		isoKeyLabel.setLayoutData(gd_isoKeyLabel);
 		
 		final Label isokeyValueLabel = new Label(this, SWT.NONE);
 		isokeyValueLabel.setText(isoKey);
 
 		final GridData gd_isoKeyLabelValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		isokeyValueLabel.setLayoutData(gd_isoKeyLabelValue);
 		
 		final Button locationButton = new Button(this, SWT.NONE);
 		locationButton.setText(PopulationModelWizardMessages.getString("NPopWizPickLoc"));
 		final GridData lb_isoKeyLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		locationButton.setLayoutData(lb_isoKeyLabel);
 		
 		locationButton.addSelectionListener(new SelectionListener() {
 			
 			public void widgetSelected(SelectionEvent arg0) {
 				LocationPickerDialog lpDialog = new LocationPickerDialog(PopulationInitializerDefinitionControl.this.getShell(), SWT.NONE, PopulationModelWizardMessages.getString("NPopWizPickLocTitle"), PopulationInitializerDefinitionControl.this.isoKey);
 				isoKey = lpDialog.open();
 				isokeyValueLabel.setText(isoKey);
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				
 			}
 		});
 		
 
 	} // DiseaseDefinitionControl
 
 	/**
 	 * @return an array of the {@link DiseaseModels}s defined in the system.
 	 */
 	private PopulationInitializer[] getPopulationInitializers() {
 		if (populationInitializers == null) {
 			final IExtensionRegistry registry = Platform.getExtensionRegistry();
 			final IConfigurationElement[] populationInitializerConfigElements = registry
 					.getConfigurationElementsFor(org.eclipse.stem.populationmodels.Constants.ID_POPULATION_INITIALIZER_EXTENSION_POINT);
 
 			final List<PopulationInitializer> temp = new ArrayList<PopulationInitializer>();
 
 			populationInitializers = new PopulationInitializer[populationInitializerConfigElements.length];
 
 			for (int i = 0; i < populationInitializerConfigElements.length; i++) {
 				final IConfigurationElement element = populationInitializerConfigElements[i];
 				// Does the element specify the class of the population model?
 				if (element.getName().equals(org.eclipse.stem.populationmodels.Constants.POPULATION_INITIALIZER_ELEMENT)) {
 					// Yes
 					try {
 						temp.add((PopulationInitializer) element
 								.createExecutableExtension(PopulationModelWizardMessages.getString("DDC.2"))); //$NON-NLS-1$
 					} catch (final CoreException e) {
 						Activator.logError(
 								PopulationModelWizardMessages.getString("DDC.4"), e); //$NON-NLS-1$
 					}
 				} // if
 			} // for each configuration element
 
 			populationInitializers = temp.toArray(new PopulationInitializer[] {});
 		} // if populationModels not created
 
 		return populationInitializers;
 	} // getPopulationModels
 
 	/**
 	 * @return the names of the disease models
 	 */
 	private String[] getPopulationInitializerNames(final PopulationInitializer[] populationModels) {
 		final String[] retValue = new String[populationInitializers.length];
 		for (int i = 0; i < populationInitializers.length; i++) {
 			String name = populationInitializers[i].getDublinCore().getTitle();
 			// Was a name specified?
 			if ((name == null) || name.equals("")) { //$NON-NLS-1$
 				// No
 				name = populationInitializers[i].getClass().getSimpleName();
 			} // if
 
 			retValue[i] = name;
 		} // for i
 		return retValue;
 	} // getPopulationModelNames
 
 	
 	PopulationInitializer getSelectedPopulationInitializer() {
 		final PopulationInitializer retValue = (PopulationInitializer)EcoreUtil
 				.copy(getPopulationInitializers()[combo.getSelectionIndex()]);
 		populationInitializerPropertyComposite.populatePopulationInitializer(retValue);
 		retValue.setTargetISOKey(this.getIsoKey());
 		return retValue;
 	} // getSelectedPopulationModel
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents sub-classing of SWT components
 	}
 
 	/**
 	 * @return <code>true</code> if the contents of the control are valid,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean validate() {
 		return populationInitializerPropertyComposite.validate();
 	} // validate
 
 	/**
 	 * @return the error message set by {@link #validate()}
 	 */
 	public String getErrorMessage() {
 		return populationInitializerPropertyComposite.getErrorMessage();
 	}
 
 	public String getIsoKey() {
 		return isoKey;
 	}
 } 

 // StandardDiseaseModelPropertyEditor.java
 package org.eclipse.stem.ui.populationmodels.standard.wizards;
 
 /*******************************************************************************
  * Copyright (c) 2007, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.stem.populationmodels.standard.PopulationModel;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModel;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProvider;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapter;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapterFactory;
 import org.eclipse.stem.ui.populationmodels.adapters.PopulationModelPropertyEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * This class is a {@link Composite} that presents the properties of a
  * {@link DiseaseModel} for editing.
  */
 public class StandardPopulationModelPropertyEditor extends PopulationModelPropertyEditor {
 
 	/**
 	 * Create the composite
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param projectValidator
 	 */
 	public StandardPopulationModelPropertyEditor(final Composite parent, final int style,
 			final PopulationModel populationModel,
 			final ModifyListener projectValidator) {
 		super(parent, style);
 
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		setLayout(gridLayout);
 
 		// Get the adapter that will provide NLS'd names for the
 		// properties of the disease model
 		final PropertyStringProviderAdapter pspa = (PropertyStringProviderAdapter) PropertyStringProviderAdapterFactory.INSTANCE
 				.adapt(populationModel, PropertyStringProvider.class);
 
 		final ComposedAdapterFactory itemProviderFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		final IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 				.adapt(populationModel, IItemPropertySource.class);
 		
 		final List<IItemPropertyDescriptor> properties = propertySource
 				.getPropertyDescriptors(populationModel);
 
 		for (final IItemPropertyDescriptor descriptor : properties) {
 			final EStructuralFeature feature = (EStructuralFeature) descriptor
 					.getFeature(null);
 			// Is this a disease model property that the user should specify?
 			if (isUserSpecifiedPopulationModelProperty(feature)) {
 				// Yes
 				final Label label = new Label(this, SWT.NONE);
 				label.setText(pspa.getPropertyName(descriptor));
 
 				final GridData labelGD = new GridData(GridData.BEGINNING);
 				labelGD.grabExcessHorizontalSpace = true;
 				labelGD.horizontalAlignment = SWT.FILL;
 				labelGD.horizontalIndent = 0;
 				label.setLayoutData(labelGD);
 
 				// Get a string value for the default value of the feature
 
 				final String defaultValueString = getPropertyDefaultValueString(descriptor);
 
 				final Text text = new Text(this, SWT.BORDER | SWT.TRAIL);
 				text.setText(defaultValueString);
 				text.setToolTipText(pspa.getPropertyToolTip(descriptor));
 				map.put(feature, text);
 
 				final GridData textGD = new GridData(GridData.END);
 				textGD.grabExcessHorizontalSpace = true;
 				textGD.horizontalAlignment = SWT.FILL;
 				text.setLayoutData(textGD);
 
 				text.addModifyListener(projectValidator);
 
 				boolean isDataPath = false;
 				if (feature.getName().equals("dataPath")) { //$NON-NLS-1$
 					isDataPath = true;
 				}
 				if (!isDataPath) {
 					final Label unitLabel = new Label(this, SWT.NONE);
 					unitLabel.setText(pspa.getPropertyUnits(descriptor));
 					final GridData unitLabelGD = new GridData(GridData.END);
 					unitLabelGD.verticalAlignment = GridData.CENTER;
 					unitLabel.setLayoutData(unitLabelGD);
 				}
 
 			} // if user specified
 		} // for each disease model property
 
 		final Label pmtpDefLabel = new Label(this, SWT.NONE);
 		pmtpDefLabel.setText(PopulationModelWizardMessages.getString("PMTP_Def")); //$NON-NLS-1$
 		final GridData pmtpDefGD = new GridData(GridData.END);
 		pmtpDefGD.grabExcessHorizontalSpace = true;
 		pmtpDefGD.horizontalSpan = 3;
 		pmtpDefLabel.setLayoutData(pmtpDefGD);
 	} // StandardDiseaseModelPropertyEditor
 
 	/**
 	 * @param diseaseModel
 	 *            the {@link DiseaseModel} instance to populate.
 	 */
 	public void populate(final PopulationModel populationModel) {
 		super.populate(populationModel);
 		
 		for (final Map.Entry<EStructuralFeature, Text> entry : map.entrySet()) {
 			switch (entry.getKey().getFeatureID()) {
 			case StandardPackage.STANDARD_POPULATION_MODEL__POPULATION_IDENTIFIER:
 				((StandardPopulationModel)populationModel).setPopulationIdentifier(entry.getValue().getText());
 				break;
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				((StandardPopulationModel)populationModel)
 						.setBirthRate(Double.parseDouble(entry.getValue().getText().trim()));
 				break;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				((StandardPopulationModel)populationModel)
 				.setDeathRate(Double.parseDouble(entry.getValue().getText().trim()));
 				break;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				((StandardPopulationModel)populationModel)
 				.setTimePeriod(Long.parseLong(entry.getValue().getText().trim()));
 				break;
 			default:
 				break;
 			} // switch
 		} // for each Map.entry
 	} // populate
 
 	/**
 	 * @return <code>true</code> if the contents are valid, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean validate() {
 		boolean retValue = super.validate();
 
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.POPULATION_MODEL__POPULATION_IDENTIFIER);
 			retValue = !text.getText().equals(""); //$NON-NLS-1$
 			// nothing?
 			if (!retValue) {
 				// Yes
 				errorMessage = PopulationModelWizardMessages.getString("NDizWizErr0"); //$NON-NLS-1$
 			} // if
 		} // if
 
 		// 
 		// Background mortality rate?
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.STANDARD_POPULATION_MODEL__DEATH_RATE);
 			retValue = !text.getText().equals(""); //$NON-NLS-1$
 			// nothing?
 			if (!retValue) {
 				// Yes
 				errorMessage = PopulationModelWizardMessages.getString("NDizWizErr4"); //$NON-NLS-1$
 			} // if
 			else {
 				// No
 				// Is it a valid value > 0?
 				retValue = isValidValue(text.getText(), 0.0);
 				if (!retValue) {
 					// No
 					errorMessage = PopulationModelWizardMessages
 							.getString("NDizWizErr5"); //$NON-NLS-1$
 				} // if
 			}
 		} // if Background mortality rate
 		
 		// 
 		// birth rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.STANDARD_POPULATION_MODEL__BIRTH_RATE);
 			retValue = !text.getText().equals(""); //$NON-NLS-1$
 			// nothing?
 			if (!retValue) {
 				// Yes
 				errorMessage = PopulationModelWizardMessages.getString("NDizWizErr6"); //$NON-NLS-1$
 			} // if
 			else {
 				// No
 				// Is it a valid value > 0?
 				retValue = isValidValue(text.getText(), 0.0);
 				if (!retValue) {
 					// No
 					errorMessage = PopulationModelWizardMessages
 							.getString("NDizWizErr7"); //$NON-NLS-1$
 				} // if
 			}
 		} // if Background mortality rate
 
 		// 
 		// birth rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.STANDARD_POPULATION_MODEL__TIME_PERIOD);
 			retValue = !text.getText().equals(""); //$NON-NLS-1$
 			// nothing?
 			if (!retValue) {
 				// Yes
 				errorMessage = PopulationModelWizardMessages.getString("NDizWizErr8"); //$NON-NLS-1$
 			} // if
 			else {
 				// No
 				// Is it a valid value > 0?
 				retValue = isValidLong(text.getText(), 0L);
 				if (!retValue) {
 					// No
 					errorMessage = PopulationModelWizardMessages
 							.getString("NDizWizErr9"); //$NON-NLS-1$
 				} // if
 			}
 		} // if Background mortality rate
 		
 		// birth rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN);
			if(text != null) {
				retValue = !text.getText().equals(""); //$NON-NLS-1$
				// nothing?
 				if (!retValue) {
					// Yes
					errorMessage = PopulationModelWizardMessages.getString("NDizWizErr10"); //$NON-NLS-1$
 				} // if
				else {
					// No
					// Is it a valid value > 0?
					retValue = isValidValue(text.getText(), 0.0);
					if (!retValue) {
						// No
						errorMessage = PopulationModelWizardMessages
								.getString("NDizWizErr10"); //$NON-NLS-1$
					} // if
				}
 			}
 		} // if gain
 		
 		return retValue;
 	} // validate
 
 } // StandardPropertyModelPropertyEditor

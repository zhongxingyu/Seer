 // StandardDiseaseModelPropertyEditor.java
 package org.eclipse.stem.ui.wizards;
 
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
 import org.eclipse.stem.diseasemodels.standard.DiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.SEIR;
 import org.eclipse.stem.diseasemodels.standard.SI;
 import org.eclipse.stem.diseasemodels.standard.SIR;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 import org.eclipse.stem.diseasemodels.standard.StandardStochasticDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.StochasticDiseaseModel;
 import org.eclipse.stem.ui.adapters.diseasemodelpropertyeditor.DiseaseModelPropertyEditor;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProvider;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapter;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapterFactory;
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
 public class StandardDiseaseModelPropertyEditor extends DiseaseModelPropertyEditor {
 
 	/**
 	 * Create the composite
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param projectValidator
 	 */
 	public StandardDiseaseModelPropertyEditor(final Composite parent, final int style,
 			final DiseaseModel diseaseModel,
 			final ModifyListener projectValidator) {
 		super(parent, style);
 
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		setLayout(gridLayout);
 
 		// Get the adapter that will provide NLS'd names for the
 		// properties of the disease model
 		final PropertyStringProviderAdapter pspa = (PropertyStringProviderAdapter) PropertyStringProviderAdapterFactory.INSTANCE
 				.adapt(diseaseModel, PropertyStringProvider.class);
 
 		final ComposedAdapterFactory itemProviderFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		final IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 				.adapt(diseaseModel, IItemPropertySource.class);
 		final List<IItemPropertyDescriptor> properties = propertySource
 				.getPropertyDescriptors(diseaseModel);
 
 		for (final IItemPropertyDescriptor descriptor : properties) {
 			final EStructuralFeature feature = (EStructuralFeature) descriptor
 					.getFeature(null);
 			// Is this a disease model property that the user should specify?
 			if (isUserSpecifiedDiseaseModelProperty(feature)) {
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
 					final Composite buttons = new Composite(this, SWT.NONE);
 					final RowLayout buttonsLayout = new RowLayout();
 					buttonsLayout.marginTop = 0;
 					buttonsLayout.marginBottom = 0;
 					buttons.setLayout(buttonsLayout);
 					final Shell shell = this.getShell();
 
 					final Button fileChooserButton = new Button(buttons,
 							SWT.NONE);
 					fileChooserButton.setText(DiseaseWizardMessages
 							.getString("fileChooserButtonText")); //$NON-NLS-1$
 					fileChooserButton.setToolTipText(DiseaseWizardMessages
 							.getString("fileChooserButtonTooltipText")); //$NON-NLS-1$
 					fileChooserButton
 							.addSelectionListener(new SelectionAdapter() {
 								@Override
 								public void widgetSelected(
 										final SelectionEvent e) {
 									final FileDialog fd = new FileDialog(shell,
 											SWT.OPEN | SWT.MULTI);
 									fd
 											.setText(DiseaseWizardMessages
 													.getString("fileChooserDialogTitle")); //$NON-NLS-1$
 									final String[] extensionsFilter = {
 											"*.txt", "*.csv" };
 									fd.setFilterExtensions(extensionsFilter);
 
 									// String format for single selected file
 									// will be:
 									// "path/file_name"
 									// For multi-files the format will be:
 									// "path/file_name1" "file_name2"
 									// "file_name3"...
 
 									String selected = "\"" + fd.open() + "\"";
 									final String[] selectedFiles = fd
 											.getFileNames();
 									if (selectedFiles.length > 1) { // if
 										// multi-files
 										// selected
 										for (int i = 1; i < selectedFiles.length; i++) {
 											selected += " \""
 													+ selectedFiles[i] + "\"";
 										}
 									}
 									text.setText(selected);
 								} // widgetSelected
 							} // SelectionAdapter
 							);
 
 					final Button dirChooserButton = new Button(buttons,
 							SWT.NONE);
 					dirChooserButton.setText(DiseaseWizardMessages
 							.getString("dirChooserButtonText")); //$NON-NLS-1$
 					dirChooserButton.setToolTipText(DiseaseWizardMessages
 							.getString("dirChooserButtonTooltipText")); //$NON-NLS-1$
 					dirChooserButton
 							.addSelectionListener(new SelectionAdapter() {
 								@Override
 								public void widgetSelected(
 										final SelectionEvent e) {
 									final DirectoryDialog dd = new DirectoryDialog(
 											shell, SWT.OPEN);
 									dd
 											.setText(DiseaseWizardMessages
 													.getString("dirChooserDialogTitle")); //$NON-NLS-1$
 									final String selected = dd.open();
 									text.setText(selected);
 								} // widgetSelected
 							} // SelectionAdapter
 							);
 
 					final GridData fileBtnGD = new GridData(GridData.END);
 					// fileChooserButton.setLayoutData(fileBtnGD);
 					buttons.setLayoutData(fileBtnGD);
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
 		pmtpDefLabel.setText(DiseaseWizardMessages.getString("PMTP_Def")); //$NON-NLS-1$
 		final GridData pmtpDefGD = new GridData(GridData.END);
 		pmtpDefGD.grabExcessHorizontalSpace = true;
 		pmtpDefGD.horizontalSpan = 3;
 		pmtpDefLabel.setLayoutData(pmtpDefGD);
 	} // StandardDiseaseModelPropertyEditor
 
 	/**
 	 * @param diseaseModel
 	 *            the {@link DiseaseModel} instance to populate.
 	 */
 	public void populate(final DiseaseModel diseaseModel) {
 		super.populate(diseaseModel);
 		
 		for (final Map.Entry<EStructuralFeature, Text> entry : map.entrySet()) {
 			switch (entry.getKey().getFeatureID()) {
 			case StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY:
 				((StandardDiseaseModel) diseaseModel)
 						.setReferencePopulationDensity(Double.parseDouble(entry
 								.getValue().getText()));
 				break;
 			case StandardPackage.STOCHASTIC_DISEASE_MODEL__SEED:
 				((StochasticDiseaseModel) diseaseModel).setSeed(Long
 						.parseLong(entry.getValue().getText()));
 				break;
 			case StandardPackage.STANDARD_STOCHASTIC_DISEASE_MODEL__GAIN:
 				((StandardStochasticDiseaseModel) diseaseModel).setGain(Double
 						.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SI__TRANSMISSION_RATE:
 				((SI) diseaseModel).setTransmissionRate(Double
 						.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SI__NON_LINEARITY_COEFFICIENT:
 				((SI) diseaseModel).setNonLinearityCoefficient(Double
 						.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SI__RECOVERY_RATE:
 				((SI) diseaseModel).setRecoveryRate(Double.parseDouble(entry
 						.getValue().getText()));
 				break;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY_RATE:
 				((SI) diseaseModel).setInfectiousMortalityRate(Double
 						.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION:
 				((SI) diseaseModel)
 						.setPhysicallyAdjacentInfectiousProportion(Double
 								.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION:
 				((SI) diseaseModel)
 						.setRoadNetworkInfectiousProportion(Double
 								.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SIR__IMMUNITY_LOSS_RATE:
 				((SIR) diseaseModel).setImmunityLossRate(Double
 						.parseDouble(entry.getValue().getText()));
 				break;
 			case StandardPackage.SEIR__INCUBATION_RATE:
 				((SEIR) diseaseModel).setIncubationRate(Double
 						.parseDouble(entry.getValue().getText()));
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
 
 		
 
 		// Transmission Rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SI__TRANSMISSION_RATE);
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr6"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidValue(text.getText(), 0.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr7"); //$NON-NLS-1$
 					} // if
 				}
 			} // if text != null
 		} // if Transmission Rate
 
 		// Non-Linearity Coefficient
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SI__NON_LINEARITY_COEFFICIENT);
 			if (text != null) {
 				// Yes
 
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr8"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
					retValue = isValidValue(text.getText(), 1.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr9"); //$NON-NLS-1$
 					} // if
 				}
 			} // if text != null
 		} // if Non-Linearity Coefficient
 
 		// Infectious Recovery Rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SI__RECOVERY_RATE);
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr10"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidValue(text.getText(), 0.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr11"); //$NON-NLS-1$
 					} // if
 				}
 			} // if
 		} // if Infectious Recovery Rate
 
 		// Infectious Mortality Rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SI__INFECTIOUS_MORTALITY_RATE);
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr12"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidValue(text.getText(), 0.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr13"); //$NON-NLS-1$
 					} // if
 				}
 			} // if
 		} // if Infectious Mortality Rate
 
 		// Immunity Loss Rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SIR__IMMUNITY_LOSS_RATE);
 			// specified?
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr16"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidValue(text.getText(), 0.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr17"); //$NON-NLS-1$
 					} // if
 				}
 			} // if
 		} // if Immunity Loss Rate
 
 		// Incubation Rate
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(StandardPackage.Literals.SEIR__INCUBATION_RATE);
 			if (text != null) {
 				// Yes
 
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = DiseaseWizardMessages
 							.getString("NDizWizErr18"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidValue(text.getText(), 0.0);
 					if (!retValue) {
 						// No
 						errorMessage = DiseaseWizardMessages
 								.getString("NDizWizErr19"); //$NON-NLS-1$
 					} // if
 				}
 			} // if
 		} // if Incubation Rate
 
 		return retValue;
 	} // validate
 
 } // StandardDiseaseModelPropertyEditor

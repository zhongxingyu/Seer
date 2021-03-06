 /*******************************************************************************
  * Caleydo - visualization for molecular biology - http://caleydo.org
  *
  * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
  * Lex, Christian Partl, Johannes Kepler University Linz </p>
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>
  *******************************************************************************/
 package org.caleydo.core.io.gui.dataimport.widget;
 
 import org.caleydo.core.data.collection.EDataType;
 import org.caleydo.core.io.NumericalProperties;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author Christian
  *
  */
 public class NumericalDataPropertiesWidget {
 
 	/**
 	 * Parent composite of all widgets.
 	 */
 	protected Composite parent;
 
 	/**
 	 * Text field that specifies the minimum data clipping value.
 	 */
 	protected Text minTextField;
 	/**
 	 * Text field that specifies the minimum data clipping value.
 	 */
 	protected Text maxTextField;
 
 	/**
 	 * Button to determine whether a data center is used.
 	 */
 	protected Button useDataCenterButton;
 
 	/**
 	 * Text field used to define the data center.
 	 */
 	protected Text dataCenterTextField;
 
 	/**
 	 * Button to enable the {@link #maxTextField};
 	 */
 	protected Button maxButton;
 
 	/**
 	 * Button to enable the {@link #minTextField};
 	 */
 	protected Button minButton;
 
 	/**
 	 * Combo to define the scaling method that should be applied to the data.
 	 */
 	protected Combo scalingCombo;
 
 	/**
 	 * Group that contains widgets associated with determining the data center.
 	 */
 	protected Group dataCenterGroup;
 
 	/**
 	 * Combo to define the type of the data.
 	 */
 	protected Combo dataTypeCombo;
 
 	protected Group scalingGroup;
 
 	protected Group clippingGroup;
 
 	protected Group dataTypeGroup;
 
 	protected NumericalProperties numericalProperties;
 
 	/**
 	 *
 	 */
 	public NumericalDataPropertiesWidget(Composite parent, Listener listener) {
 		this.parent = parent;
 
 		createDataTypeGroup(parent);
 
 		createScalingGroup(parent);
 
 		createClippingGroup(parent, listener);
 
 		createDataCenterGroup(parent, listener);
 	}
 
 	public void updateNumericalProperties(NumericalProperties numericalProperties) {
 		this.numericalProperties = numericalProperties;
 
 		Double dataCenter = numericalProperties.getDataCenter();
 		useDataCenterButton.setSelection(dataCenter != null);
 		dataCenterTextField.setEnabled(dataCenter != null);
 		dataCenterTextField.setText(dataCenter == null ? "0" : dataCenter.toString());
 
 		boolean maxDefined = numericalProperties.getMax() != null;
 		maxButton.setSelection(maxDefined);
 		maxTextField.setEnabled(maxDefined);
 		if (maxDefined)
 			maxTextField.setText(numericalProperties.getMax().toString());
 		else
 			maxTextField.setText("");
 
 		boolean minDefined = numericalProperties.getMin() != null;
 		minButton.setEnabled(true);
 		minButton.setSelection(minDefined);
 		minTextField.setEnabled(minDefined);
 		if (minDefined)
 			minTextField.setText(numericalProperties.getMin().toString());
 		else
 			minTextField.setText("");
 
 		String previousMathFiltermode = numericalProperties.getDataTransformation();
 		if (previousMathFiltermode.equals("Log10"))
 			scalingCombo.select(1);
 		else if (previousMathFiltermode.equals("Log2"))
 			scalingCombo.select(2);
 		else
 			scalingCombo.select(0);
 	}
 
 	private void createDataTypeGroup(Composite parent) {
 		dataTypeGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
 		dataTypeGroup.setText("Data Type");
 		dataTypeGroup.setLayout(new GridLayout(2, false));
 		dataTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		Label dataTypeExplanationLabel = new Label(dataTypeGroup, SWT.WRAP);
 		dataTypeExplanationLabel.setText("Specify the type of numerical data.");
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 		gridData.widthHint = 200;
 		dataTypeExplanationLabel.setLayoutData(gridData);
 
 		Label scalingMethodLabel = new Label(dataTypeGroup, SWT.NONE);
 		scalingMethodLabel.setText("Type");
 
 		dataTypeCombo = new Combo(dataTypeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.widthHint = 250;
 		dataTypeCombo.setLayoutData(gridData);
 
 		String[] scalingOptions = { "Real Number (Float)", "Natural Number (Integer)" };
 		dataTypeCombo.setItems(scalingOptions);
 		dataTypeCombo.setEnabled(true);
 
 		dataTypeCombo.select(0);
 	}
 
 	private void createDataCenterGroup(Composite parent, Listener listener) {
 		dataCenterGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
 		dataCenterGroup.setText("Data Center");
 		dataCenterGroup.setLayout(new GridLayout(2, false));
 		dataCenterGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		Label dateCenterExplanationLabel = new Label(dataCenterGroup, SWT.WRAP);
 		dateCenterExplanationLabel
 				.setText("The data center is a value that, if set, determines a neutral center point of the data. A common example is that 0 is the neutral value, lower values are in the negative and larger values are in the positive range. If the data center is set it is assumed that the extend into both, positive and negative direction is the same. For example, for a dataset [-0.5, 0.7] with a center set at 0, the value range will be set to -0.7 to 0.7.");
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 		gridData.widthHint = 600;
 		dateCenterExplanationLabel.setLayoutData(gridData);
 		useDataCenterButton = new Button(dataCenterGroup, SWT.CHECK);
 		// useDataCenterButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 		// true, true, 1,
 		// 1));
 		useDataCenterButton.setText("Use data center ");
 		useDataCenterButton.addListener(SWT.Selection, listener);
 		useDataCenterButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				useDataCenterButtonSelected();
 			}
 		});
 		useDataCenterButton.setSelection(false);
 
 		dataCenterTextField = new Text(dataCenterGroup, SWT.BORDER);
 		gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
 		gridData.widthHint = 70;
 		dataCenterTextField.setLayoutData(gridData);
 		dataCenterTextField.addListener(SWT.Modify, listener);
 		dataCenterTextField.setEnabled(false);
 	}
 
 	private void createClippingGroup(Composite parent, Listener listener) {
 
 		clippingGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
 		clippingGroup.setText("Data Clipping");
 		clippingGroup.setLayout(new GridLayout(2, false));
 		clippingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		Label clippingExplanationLabel = new Label(clippingGroup, SWT.WRAP);
 		clippingExplanationLabel
 				.setText("Specify the value range for the dataset. By default the value range is infered as the minimum and maximum of the dataset. This can be used to specify bounds in the data. For example, if you want to see your data relative to 0 but all data points are greater than 0 you should specify 0 here as the minimum.  You can also use this to clip your data. If you, for example, select '3' as the maximum every data point exceeding this range will be clipped to '3' for display.");
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 		gridData.widthHint = 200;
 		clippingExplanationLabel.setLayoutData(gridData);
 
 		maxButton = new Button(clippingGroup, SWT.CHECK);
 		maxButton.setText("Max");
 		maxButton.addListener(SWT.Selection, listener);
 		maxButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				maxButtonSelected();
 			}
 		});
 
 		maxTextField = new Text(clippingGroup, SWT.BORDER);
 		maxTextField.addListener(SWT.Modify, listener);
 		maxTextField.setEnabled(false);
 		maxButton.setEnabled(true);
 
 		minButton = new Button(clippingGroup, SWT.CHECK);
 		minButton.setText("Min");
 		minButton.addListener(SWT.Selection, listener);
 		minButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				minButtonSelected();
 			}
 		});
 
 		minTextField = new Text(clippingGroup, SWT.BORDER);
 		minTextField.addListener(SWT.Modify, listener);
 		minTextField.setEnabled(false);
 
 	}
 
 	private void createScalingGroup(Composite parent) {
 		scalingGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
 		scalingGroup.setText("Data Scale");
 		scalingGroup.setLayout(new GridLayout(2, false));
 		scalingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		Label scalingExplanationLabel = new Label(scalingGroup, SWT.WRAP);
 		scalingExplanationLabel
 				.setText("Specify the way the data should be scaled. A linear scale is best if you are interested in absolute relationships within your data. A logarithmic scale is best if you are interested in the relative differences between data points and if your data is distributed inhomogeneously on a wide scale. Gene expression data, for example, is usually interpreted on a logarithmic scale.");
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 		gridData.widthHint = 200;
 		scalingExplanationLabel.setLayoutData(gridData);
 
 		Label scalingMethodLabel = new Label(scalingGroup, SWT.NONE);
 		scalingMethodLabel.setText("Scaling Method");
 
 		scalingCombo = new Combo(scalingGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.widthHint = 120;
 		scalingCombo.setLayoutData(gridData);
 
 		String[] scalingOptions = { "Linear", "Log10", "Log2" };
 		scalingCombo.setItems(scalingOptions);
 		scalingCombo.setEnabled(true);
 
 		scalingCombo.select(0);
 	}
 
 	/**
 	 * Enables or disables maxTextField.
 	 */
 	public void maxButtonSelected() {
 		maxTextField.setEnabled(maxButton.getSelection());
 	}
 
 	/**
 	 * Enables or disables minTextField.
 	 */
 	public void minButtonSelected() {
 		minTextField.setEnabled(minButton.getSelection());
 	}
 
 	public void useDataCenterButtonSelected() {
 		dataCenterTextField.setEnabled(useDataCenterButton.getSelection());
 	}
 
 	public boolean isDataValid() {
 		if (maxButton.getSelection()) {
 			try {
 				Float.parseFloat(maxTextField.getText());
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		}
 		if (minButton.getSelection()) {
 			try {
 				Float.parseFloat(minTextField.getText());
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		}
 		if (useDataCenterButton.getSelection()) {
 			try {
 				Double.parseDouble(dataCenterTextField.getText());
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return {@link NumericalProperties} as specified by the gui elements of this widget.
 	 */
 	public NumericalProperties getNumericalProperties() {
 		NumericalProperties numericalProperties = new NumericalProperties();
 		if (minTextField.getEnabled() && !minTextField.getText().isEmpty()) {
 			float min = Float.parseFloat(minTextField.getText());
 			if (!Float.isNaN(min)) {
 				numericalProperties.setMin(min);
 			}
 		}
 		if (maxTextField.getEnabled() && !maxTextField.getText().isEmpty()) {
 			float max = Float.parseFloat(maxTextField.getText());
 			if (!Float.isNaN(max)) {
 				numericalProperties.setMax(max);
 			}
 		}
 		if (useDataCenterButton.getSelection()) {
 			numericalProperties.setDataCenter(Double.parseDouble(dataCenterTextField.getText()));
 		}
 
		numericalProperties.setDataTransformation(scalingCombo.getText());
 
 		return numericalProperties;
 	}
 
 	public EDataType getDataType() {
 		if (dataTypeCombo.getSelectionIndex() == 0)
 			return EDataType.FLOAT;
 		return EDataType.INTEGER;
 	}
 
 	public void setDataType(EDataType dataType) {
 		if (dataType == EDataType.FLOAT)
 			dataTypeCombo.select(0);
 		else
 			dataTypeCombo.select(1);
 	}
 
 	public void dispose() {
 		dataTypeGroup.dispose();
 		dataCenterGroup.dispose();
 		clippingGroup.dispose();
 		scalingGroup.dispose();
 	}
 }

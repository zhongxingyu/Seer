 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    jpasch - Bug 345315  Missing units for "Choose scale-factor
 *    mgorning - Bug 352874 - Exports of Diagrams > 3000x3000 px
 *    mwenz - Bug 375037 - ArrayIndexOutOfBoundsException when exporting diagram to SVG
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util.ui.print;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.util.ui.DoubleFieldWithDropDown;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * This is a simple dialog to choose the image-format and the image-scale-factor
  * for a save-as-image operation.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ExportDiagramDialog extends AbstractFigureSelectionDialog implements ModifyListener {
 
	private String[] IMAGE_FILE_EXTENSIONS = new String[] { "BMP", "GIF", "JPG", "PNG", "RLE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 
	private int[] IMAGE_FILE_TYPES = new int[] { SWT.IMAGE_BMP, SWT.IMAGE_GIF, SWT.IMAGE_JPEG, SWT.IMAGE_PNG,
			SWT.IMAGE_BMP_RLE };
 
 	public static final String[] WIDTHS = new String[] { "320", "640", "800", "1024", "1280", "1600" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 
 	public static final String[] HEIGHTS = new String[] { "200", "400", "600", "768", "1024", "1280" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 
 	public static final double[] SCALE_FACTORS = new double[] { 0.5, 1.0, 2.0, 4.0 };
 
 	// controls
 	private Combo _formatCombo;
 
 	private Combo _widthCombo;
 
 	private Combo _heightCombo;
 
 	private DoubleFieldWithDropDown _scaleFactorText;
 
 	// selected values
 	private int _formatIndex = 0;
 
 	private Map<String, Boolean> additionalExporterTypes;
 
 	/**
 	 * Creates a new ExportDiagramDialog.
 	 * 
 	 * @param shell
 	 *            The Shell of this dialog.
 	 * @param graphicalViewer
 	 *            The GraphicalViewer, which to save.
 	 */
 	public ExportDiagramDialog(Shell shell, GraphicalViewer graphicalViewer) {
 		super(shell, graphicalViewer);
 	}
 
 	public void addExporters(Map<String, Boolean> diagramExporterTypes) {
 		additionalExporterTypes = diagramExporterTypes;
 		List<String> asList = new ArrayList<String>();
 		for (String string : IMAGE_FILE_EXTENSIONS) {
 			asList.add(string);
 		}
 		for (String string : diagramExporterTypes.keySet()) {
 			asList.add(string);
 		}
 		IMAGE_FILE_EXTENSIONS = asList.toArray(new String[] {});
 
 	}
 
 	// ======================= overwritten methods ============================
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = (Composite) super.createDialogArea(parent);
 		GridLayout layout = new GridLayout(2, false);
 		composite.setLayout(layout);
 
 		createChooseFigureGroup(composite);
 
 		Group scaleGroup = createChooseScaleFactorGroup(composite);
 
 		Label widthComboText = new Label(scaleGroup, SWT.NONE);
 		widthComboText.setText(Messages.SaveFigureAsImageDialog_0_xfld);
 		_widthCombo = new Combo(scaleGroup, SWT.DROP_DOWN);
 		_widthCombo.setItems(WIDTHS);
 		_widthCombo.addModifyListener(this);
 		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
 		_widthCombo.setLayoutData(data);
 
 		Label heightComboText = new Label(scaleGroup, SWT.NONE);
 		heightComboText.setText(Messages.SaveFigureAsImageDialog_1_xfld);
 		_heightCombo = new Combo(scaleGroup, SWT.DROP_DOWN);
 		_heightCombo.setItems(HEIGHTS);
 		_heightCombo.addModifyListener(this);
 		GridData data2 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
 		_heightCombo.setLayoutData(data2);
 
 		new Label(scaleGroup, SWT.FILL);
 		createFormatGroup(composite);
 
 		updateControls();
 
 		return composite;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(Messages.SaveFigureAsImageDialog_5_xtxt);
 	}
 
 	// ======================== private helper methods ========================
 
 	private void createFormatGroup(Composite composite) {
 		Group formatGroup = new Group(composite, SWT.NONE);
 		formatGroup.setText(Messages.SaveFigureAsImageDialog_2_xfld);
 		formatGroup.setLayout(new GridLayout(2, false));
 		GridData layoutData = new GridData();
 		layoutData.grabExcessVerticalSpace = true;
 		layoutData.grabExcessHorizontalSpace = true;
 		layoutData.verticalAlignment = SWT.FILL;
 		layoutData.horizontalAlignment = SWT.FILL;
 		formatGroup.setLayoutData(layoutData);
 
 		_formatCombo = new Combo(formatGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
 		_formatCombo.setItems(IMAGE_FILE_EXTENSIONS);
 		_formatCombo.setText(IMAGE_FILE_EXTENSIONS[_formatIndex]);
 		_formatCombo.addModifyListener(this);
 		GridData data3 = new GridData();
 		data3.grabExcessHorizontalSpace = true;
 		data3.horizontalAlignment = SWT.FILL;
 		_formatCombo.setLayoutData(data3);
 	}
 
 	private Group createChooseScaleFactorGroup(Composite composite) {
 		Group scaleGroup = new Group(composite, SWT.NONE);
 		scaleGroup.setText(Messages.SaveFigureAsImageDialog_3_xfld);
 		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 1;
 		data.verticalSpan = 2;
 		data.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
 		data.grabExcessVerticalSpace = true;
 		scaleGroup.setLayoutData(data);
 		GridLayout layout = new GridLayout(2, false);
 		scaleGroup.setLayout(layout);
 
 		Label scaleFactorText = new Label(scaleGroup, SWT.NONE);
 		scaleFactorText.setText(Messages.SaveFigureAsImageDialog_4_xfld);
 		_scaleFactorText = new DoubleFieldWithDropDown(scaleGroup, SWT.NONE, _preferences,
 				DefaultPrintPreferences.SCALE_FACTOR, SCALE_FACTORS);
 		data = new GridData();
 		data.grabExcessHorizontalSpace = true;
 		data.horizontalAlignment = SWT.FILL;
 		_scaleFactorText.setLayoutData(data);
 		_scaleFactorText.addModifyListener(this);
 
 		return scaleGroup;
 	}
 
 	// =============================== eventing ===============================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
 	 * .ModifyEvent)
 	 */
 	public void modifyText(ModifyEvent e) {
 		if (_insideInternalModify) // avoid endless-loops
 			return;
 
 		// super.modifyText(e);
 
 		if (e.getSource() == _formatCombo) {
 			_formatIndex = _formatCombo.getSelectionIndex();
 			String format = _formatCombo.getText();
 			Boolean b = !additionalExporterTypes.containsKey(format) || additionalExporterTypes.get(format);
 			setControlsEnabled(b);
 		} else if (e.getSource() == _widthCombo) {
 			try {
 				String text = _widthCombo.getText();
 				if (text.length() == 0)
 					text = "0"; //$NON-NLS-1$
 				int width = Integer.parseInt(text);
 				if (width < 0) {
 					throw new NumberFormatException(Messages.ExportDiagramDialog_0 + width);
 				}
 				_preferences.setDoublePreference(DefaultPrintPreferences.SCALE_FACTOR, (double) width
 						/ (double) _figure.getBounds().width);
 			} catch (NumberFormatException x) {
 				T.racer().debug(x.getMessage());
 			}
 			updateControls();
 		} else if (e.getSource() == _heightCombo) {
 			try {
 				String text = _heightCombo.getText();
 				if (text.length() == 0)
 					text = "0"; //$NON-NLS-1$
 				int height = Integer.parseInt(text);
 				if (height < 0) {
 					throw new NumberFormatException(Messages.ExportDiagramDialog_1 + height);
 				}
 				_preferences.setDoublePreference(DefaultPrintPreferences.SCALE_FACTOR,
 						((double) height / (double) _figure.getBounds().height));
 
 			} catch (NumberFormatException x) {
 				T.racer().debug(x.getMessage());
 			}
 			updateControls();
 
 		} else
 			updateControls();
 
 	}
 
 	/**
 	 * @param b
 	 */
 	private void setControlsEnabled(boolean b) {
 		_heightCombo.setEnabled(b);
 		_widthCombo.setEnabled(b);
 		_scaleFactorText.setEnabled(b);
 
 	}
 
 	@Override
 	public void updateControls() {
 		super.updateControls();
 
 		_insideInternalModify = true;
 		try {
 			double scaleFactor = _preferences.getDoublePreference(DefaultPrintPreferences.SCALE_FACTOR);
 			String newText = Long.toString(Math.round((scaleFactor * _figure.getBounds().width)));
 			if (!newText.equals(_widthCombo.getText()))
 				// don't update if identical, otherwise cursor will move to the
 				// first character
 				_widthCombo.setText(newText);
 			newText = Long.toString(Math.round((scaleFactor * _figure.getBounds().height)));
 			if (!newText.equals(_heightCombo.getText()))
 				// don't update if identical, otherwise cursor will move to the
 				// first character
 				_heightCombo.setText(newText);
 
 			_scaleFactorText.updateControl();
 		} finally {
 			_insideInternalModify = false;
 		}
 	}
 
 	@Override
 	protected void okPressed() {
 		if (getImageFormat() == SWT.IMAGE_PNG) {
 			setScaledImage(getImageScaleFactor(), 10000.0d);
 		} else {
 			setScaledImage(getImageScaleFactor());
 		}
 		super.okPressed();
 	}
 
 	// =========================== public access ==============================
 
 	public final int getImageFormat() {
		if (_formatIndex >= IMAGE_FILE_TYPES.length) {
			// Custom file type provided via extension
			return -1;
		}
 		return IMAGE_FILE_TYPES[_formatIndex];
 	}
 
 	public final String getFormattedFileExtension() {
 		return IMAGE_FILE_EXTENSIONS[_formatIndex].toLowerCase(Locale.ENGLISH);
 	}
 
 	public final String getFileExtension() {
 		return IMAGE_FILE_EXTENSIONS[_formatIndex];
 	}
 
 	public double getImageScaleFactor() {
 		return _preferences.getDoublePreference(DefaultPrintPreferences.SCALE_FACTOR);
 	}
 }

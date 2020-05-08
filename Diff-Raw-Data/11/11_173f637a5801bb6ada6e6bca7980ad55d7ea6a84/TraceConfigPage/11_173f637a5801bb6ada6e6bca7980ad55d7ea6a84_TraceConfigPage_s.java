 /*******************************************************************************
  * Copyright (c) 2010 Oak Ridge National Laboratory.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.csstudio.swt.xygraph.toolbar;
 
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
 import org.csstudio.swt.xygraph.dataprovider.ISample;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.Trace.BaseLine;
 import org.csstudio.swt.xygraph.figures.Trace.ErrorBarType;
 import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
 import org.csstudio.swt.xygraph.figures.Trace.TraceType;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
 import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**This will help to create the necessary widgets 
  * to configure an axis's properties.
  * @author Xihui Chen
  *
  */
 public class TraceConfigPage {
 	private XYGraph xyGraph;
 	private Trace trace;
 	private Text nameText;
 	private Combo xAxisCombo;
 	private Combo yAxisCombo;
 	private ColorSelector traceColorSelector;
 	private Combo traceTypeCombo;
 	private Spinner lineWidthSpinner;
 	private Combo pointStyleCombo;
 	private Spinner pointSizeSpinner;
 	private Combo baseLineCombo;
 	private Spinner areaAlphaSpinner;
 	private Button antiAliasing;
 	
 	private Button errorBarEnabledButton;
 	private Combo xErrorBarTypeCombo;
 	private Combo yErrorBarTypeCombo;
 	private ColorSelector errorBarColorSelector;
 	private Spinner errorBarCapWidthSpinner;
 	private Button drawYErrorInAreaButton;
 	
 
 	
 	private Composite composite;
 	
 	public TraceConfigPage(XYGraph xyGraph, Trace trace) {
 		this.xyGraph = xyGraph;
 		this.trace = trace;
 	}
 	
 	public void createPage(final Composite composite){
 		this.composite = composite;
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		composite.setLayout(new GridLayout(2, false));
 		
 		Composite traceCompo = new Composite(composite, SWT.NONE);
 		traceCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		traceCompo.setLayout(new GridLayout(3, false));
 		
 		final Group errorBarGroup = new Group(composite, SWT.NONE);
 		errorBarGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
 		errorBarGroup.setLayout(new GridLayout(2, false));
 		errorBarGroup.setText("Error Bar");
 		
 		GridData gd;
 		GridData labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);		
 		
 		final Label nameLabel = new Label(traceCompo, 0);
 		nameLabel.setText("Name: ");
 		nameLabel.setLayoutData(labelGd);
 		
 		nameText = new Text(traceCompo, SWT.BORDER | SWT.SINGLE);
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		nameText.setLayoutData(gd);		
 		
 		final Label xAxisLabel = new Label(traceCompo, 0);
 		xAxisLabel.setText("X-Axis: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		xAxisLabel.setLayoutData(labelGd);
 		
 		xAxisCombo = new Combo(traceCompo, SWT.DROP_DOWN);
 		for(Axis axis : xyGraph.getXAxisList())
 			xAxisCombo.add(axis.getTitle());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		xAxisCombo.setLayoutData(gd);
 		
 		final Label yAxisLabel = new Label(traceCompo, 0);
 		yAxisLabel.setText("Y-Axis: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		yAxisLabel.setLayoutData(labelGd);
 		
 		yAxisCombo = new Combo(traceCompo, SWT.DROP_DOWN);
 		for(Axis axis : xyGraph.getYAxisList())
 			yAxisCombo.add(axis.getTitle());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		yAxisCombo.setLayoutData(gd);
 		
 		final Label traceColorLabel = new Label(traceCompo, 0);
 		traceColorLabel.setText("Trace Color: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 		traceColorLabel.setLayoutData(labelGd);	
 		
 		traceColorSelector = new ColorSelector(traceCompo);
 		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
 		traceColorSelector.getButton().setLayoutData(gd);
 		
 		final Label traceTypeLabel = new Label(traceCompo, 0);
 		traceTypeLabel.setText("Trace Type: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		traceTypeLabel.setLayoutData(labelGd);	
 		
 		traceTypeCombo = new Combo(traceCompo, SWT.DROP_DOWN);
 		traceTypeCombo.setItems(TraceType.stringValues());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		traceTypeCombo.setLayoutData(gd);
 		
 		traceTypeCombo.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {				
 				baseLineCombo.setEnabled(
 						traceTypeCombo.getSelectionIndex() == 
 						Arrays.asList(TraceType.values()).indexOf(TraceType.BAR) ||
 						traceTypeCombo.getSelectionIndex() == 
 						Arrays.asList(TraceType.values()).indexOf(TraceType.AREA));				
 			}
 		});
 		
 		final Label lineWidthLabel = new Label(traceCompo, 0);
 		lineWidthLabel.setText("Line Width (pixels): ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 		lineWidthLabel.setLayoutData(labelGd);	
 		
 		lineWidthSpinner = new Spinner(traceCompo, SWT.BORDER);
 		lineWidthSpinner.setMaximum(100);
 		lineWidthSpinner.setMinimum(0);
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		lineWidthSpinner.setLayoutData(gd);
 		
 		final Label pointStyleLabel = new Label(traceCompo, 0);
 		pointStyleLabel.setText("Point Style: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		pointStyleLabel.setLayoutData(labelGd);	
 		
 		pointStyleCombo = new Combo(traceCompo, SWT.DROP_DOWN);
 		pointStyleCombo.setItems(PointStyle.stringValues());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		pointStyleCombo.setLayoutData(gd);
 		
 		final Label pointSizeLable = new Label(traceCompo, 0);
 		pointSizeLable.setText("Point Size (pixels): ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 		pointSizeLable.setLayoutData(labelGd);	
 		
 		pointSizeSpinner = new Spinner(traceCompo, SWT.BORDER);
 		pointSizeSpinner.setMaximum(100);
 		pointSizeSpinner.setMinimum(0);
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		pointSizeSpinner.setLayoutData(gd);
 		
 		final Label baseLineLabel = new Label(traceCompo, 0);
 		baseLineLabel.setText("Base Line: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		baseLineLabel.setLayoutData(labelGd);	
 		baseLineLabel.setToolTipText("The baseline for BAR or AREA trace type");
 		
 		baseLineCombo = new Combo(traceCompo, SWT.DROP_DOWN);
 		baseLineCombo.setItems(BaseLine.stringValues());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		baseLineCombo.setLayoutData(gd);
 		
 		
 		final Label alphaLable = new Label(traceCompo, 0);
 		alphaLable.setText("Area Alpha: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		alphaLable.setLayoutData(labelGd);	
 		
 		areaAlphaSpinner = new Spinner(traceCompo, SWT.BORDER);
 		areaAlphaSpinner.setMaximum(255);
 		areaAlphaSpinner.setMinimum(0);
 		gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1);
 		areaAlphaSpinner.setLayoutData(gd);
 		areaAlphaSpinner.setToolTipText("0 for transparent, 255 for opaque");
 		
 		antiAliasing = new Button(traceCompo, SWT.CHECK);
 		antiAliasing.setText("Anti Aliasing Enabled");	
 		antiAliasing.setLayoutData(
 				new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));		
 		
 		Button export = new Button(traceCompo, SWT.NONE);
 		export.setText("Export data...");	
 		export.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));		
 		export.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				IFile exportTo = WorkspaceResourceDialog.openNewFile(Display.getDefault().getActiveShell(), 
 						                            "Create file to export to", 
 						                            "Export data from "+trace.getName()+"'", 
 						                            null, null);
 				
 				if (exportTo!=null) {
 				    try {
 						exportToDat(exportTo, trace.getDataProvider());
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		//error bar settings...
 		errorBarEnabledButton = new Button(errorBarGroup, SWT.CHECK);
 		errorBarEnabledButton.setText("Error Bar Enabled");
 		errorBarEnabledButton.setLayoutData(
 				new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
 		errorBarEnabledButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				boolean enabled = errorBarEnabledButton.getSelection();
 				xErrorBarTypeCombo.setEnabled(enabled);
 				yErrorBarTypeCombo.setEnabled(enabled);
 				errorBarColorSelector.setEnabled(enabled);
 				errorBarCapWidthSpinner.setEnabled(enabled);
 				drawYErrorInAreaButton.setEnabled(enabled);
 			}
 		});
 		
 		final Label xErrorBarTypeLabel = new Label(errorBarGroup, 0);
 		xErrorBarTypeLabel.setText("X Error Bar Type: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		xErrorBarTypeLabel.setLayoutData(labelGd);	
 		
 		xErrorBarTypeCombo = new Combo(errorBarGroup, SWT.DROP_DOWN);
 		xErrorBarTypeCombo.setItems(ErrorBarType.stringValues());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		xErrorBarTypeCombo.setLayoutData(gd);
 		
 		
 		final Label yErrorBarTypeLabel = new Label(errorBarGroup, 0);
 		yErrorBarTypeLabel.setText("Y Error Bar Type: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		yErrorBarTypeLabel.setLayoutData(labelGd);	
 		
 		yErrorBarTypeCombo = new Combo(errorBarGroup, SWT.DROP_DOWN);
 		yErrorBarTypeCombo.setItems(ErrorBarType.stringValues());
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		yErrorBarTypeCombo.setLayoutData(gd);
 		
 		final Label errorBarColorLabel = new Label(errorBarGroup, 0);
 		errorBarColorLabel.setText("Error Bar Color: ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		errorBarColorLabel.setLayoutData(labelGd);	
 		
 		errorBarColorSelector = new ColorSelector(errorBarGroup);
 		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
 		errorBarColorSelector.getButton().setLayoutData(gd);
 		
 		final Label errorBarCapWidthLabel = new Label(errorBarGroup, 0);
 		errorBarCapWidthLabel.setText("Error Bar Cap Width \n(pixels): ");
 		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		errorBarCapWidthLabel.setLayoutData(labelGd);	
 		
 		errorBarCapWidthSpinner = new Spinner(errorBarGroup, SWT.BORDER);
 		errorBarCapWidthSpinner.setMaximum(100);
 		errorBarCapWidthSpinner.setMinimum(0);
 		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		errorBarCapWidthSpinner.setLayoutData(gd);
 
 		drawYErrorInAreaButton = new Button(errorBarGroup, SWT.CHECK);
 		drawYErrorInAreaButton.setText("Draw Y Error In Area");
 		drawYErrorInAreaButton.setLayoutData(
 				new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
 		initialize();
 	}
 
 	
 	
 
 	
 	protected void exportToDat(IFile exportTo, IDataProvider dataProvider) throws Exception {
 
 		final IFile dat  = getUniqueFile(exportTo, null, "dat");
 		final StringBuilder contents = new StringBuilder();
 		final IDataProvider prov = trace.getDataProvider();
 		final NumberFormat format = new DecimalFormat("##0.#####E0");
 		for (int i = 0; i < prov.getSize(); i++) {
 			final ISample isample = prov.getSample(i);
 			contents.append(format.format(isample.getXValue()));
 			contents.append("\t");
 			contents.append(format.format(isample.getYValue()));
 			contents.append("\n");
 		}
 
 		InputStream stream = new ByteArrayInputStream(contents.toString().getBytes());
 		dat.create(stream, true, new NullProgressMonitor());
 		dat.getParent().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 
 		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName());
         if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName()+".txt");
 		page.openEditor(new FileEditorInput(dat), desc.getId());
 	}
 
 	/**
 	 * @return the composite
 	 */
 	public Composite getComposite() {
 		return composite;
 	}
 
 	public void applyChanges(){
 		trace.setName(nameText.getText());
 		trace.setXAxis(xyGraph.getXAxisList().get(xAxisCombo.getSelectionIndex()));
 		trace.setYAxis(xyGraph.getYAxisList().get(yAxisCombo.getSelectionIndex()));
 		trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(
 				traceColorSelector.getColorValue()));
 		trace.setTraceType(TraceType.values()[traceTypeCombo.getSelectionIndex()]);
 		trace.setLineWidth(lineWidthSpinner.getSelection());
 		trace.setPointStyle(PointStyle.values()[pointStyleCombo.getSelectionIndex()]);
 		trace.setPointSize(pointSizeSpinner.getSelection());
 		trace.setBaseLine(BaseLine.values()[baseLineCombo.getSelectionIndex()]);
 		trace.setAreaAlpha(areaAlphaSpinner.getSelection());
 		trace.setAntiAliasing(antiAliasing.getSelection());
 		trace.setErrorBarEnabled(errorBarEnabledButton.getSelection());
 		trace.setXErrorBarType(ErrorBarType.values()[xErrorBarTypeCombo.getSelectionIndex()]);
 		trace.setYErrorBarType(ErrorBarType.values()[yErrorBarTypeCombo.getSelectionIndex()]);
 		trace.setErrorBarColor(XYGraphMediaFactory.getInstance().getColor(
 				errorBarColorSelector.getColorValue()));
 		trace.setErrorBarCapWidth(errorBarCapWidthSpinner.getSelection());
 		trace.setDrawYErrorInArea(drawYErrorInAreaButton.getSelection());
 	}
 
 
 	
 	private void initialize(){
 		nameText.setText(trace.getName());
 		xAxisCombo.select(xyGraph.getXAxisList().indexOf(trace.getXAxis()));
 		yAxisCombo.select(xyGraph.getYAxisList().indexOf(trace.getYAxis()));
 		traceColorSelector.setColorValue(trace.getTraceColor().getRGB());
 		traceTypeCombo.select(Arrays.asList(TraceType.values())
 				.indexOf(trace.getTraceType()));
 		lineWidthSpinner.setSelection(trace.getLineWidth());
 		pointStyleCombo.select(Arrays.asList(PointStyle.values())
 				.indexOf(trace.getPointStyle()));
 		pointSizeSpinner.setSelection(trace.getPointSize());
 		baseLineCombo.select(Arrays.asList(BaseLine.values())
 				.indexOf(trace.getBaseLine()));
 		areaAlphaSpinner.setSelection(trace.getAreaAlpha());
 		antiAliasing.setSelection(trace.isAntiAliasing());
 		errorBarEnabledButton.setSelection(trace.isErrorBarEnabled());
 		xErrorBarTypeCombo.select(Arrays.asList(ErrorBarType.values())
 				.indexOf(trace.getXErrorBarType()));
 		yErrorBarTypeCombo.select(Arrays.asList(ErrorBarType.values())
 				.indexOf(trace.getYErrorBarType()));
 		errorBarColorSelector.setColorValue(trace.getErrorBarColor().getRGB());
 		errorBarCapWidthSpinner.setSelection(trace.getErrorBarCapWidth());
 		drawYErrorInAreaButton.setSelection(trace.isDrawYErrorInArea());
 		
 		boolean enabled = errorBarEnabledButton.getSelection();
 		xErrorBarTypeCombo.setEnabled(enabled);
 		yErrorBarTypeCombo.setEnabled(enabled);
 		errorBarColorSelector.setEnabled(enabled);
 		errorBarCapWidthSpinner.setEnabled(enabled);
 		drawYErrorInAreaButton.setEnabled(enabled);
 		
 		baseLineCombo.setEnabled(
 				traceTypeCombo.getSelectionIndex() == 
 				Arrays.asList(TraceType.values()).indexOf(TraceType.BAR) ||
 				traceTypeCombo.getSelectionIndex() == 
 				Arrays.asList(TraceType.values()).indexOf(TraceType.AREA));		
 	}
 	
 	/**
 	 * Gets a unique file. The file must have a parent of IFolder.
 	 * @param file
 	 * @return new file, not created.
 	 */
 	public static IFile getUniqueFile(IFile file, final String conjunctive, final String extension) {
 		
 		final String name = file.getName();
 		final Matcher matcher = Pattern.compile("(.+)(\\d+)\\."+extension, Pattern.CASE_INSENSITIVE).matcher(name);
 		int start   = 0;
 		String frag=name;
 		try {
 			frag = name.substring(0,name.lastIndexOf("."));
 		} catch (Throwable ignored) {
 			// Nowt
 		}
 		if (matcher.matches()) {
 			frag  = matcher.group(1);
			start = Integer.parseInt(matcher.group(2));
 		}
 		
 		if (conjunctive!=null) {
 			frag = frag+conjunctive;
 		}
 		
 		// First try without a start position
 		final IContainer parent = file.getParent();
 		final IFile newFile;
 		if (parent instanceof IFolder) {
			newFile = ((IFolder)parent).getFile(frag+"."+extension);
 		} else if (parent instanceof IProject) {
			newFile = ((IProject)parent).getFile(frag+"."+extension);
 		} else {
 			newFile = null;
 		}
 		if (newFile!=null&&!newFile.exists()) return newFile;
 		
 		return getUniqueFile(parent, frag, ++start, extension);
 	}
 
 	private static IFile getUniqueFile(IContainer parent, String frag, int start, final String extension) {
 		final IFile file;
 		if (parent instanceof IFolder) {
 			file = ((IFolder)parent).getFile(frag+start+"."+extension);
 		} else if (parent instanceof IProject) {
 			file = ((IProject)parent).getFile(frag+start+"."+extension);
 		} else {
 			throw new RuntimeException("The parent is neither a project nor a folder.");
 		}
 		if (!file.exists()) return file;
 		return getUniqueFile(parent, frag, ++start, extension);
 	}
 }

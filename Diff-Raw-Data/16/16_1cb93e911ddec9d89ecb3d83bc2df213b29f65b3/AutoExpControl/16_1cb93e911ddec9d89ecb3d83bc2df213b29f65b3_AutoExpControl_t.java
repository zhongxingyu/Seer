 // AutoExpControl.java
 package org.eclipse.stem.analysis.automaticexperiment.views;
 
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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.birt.chart.model.attribute.ColorDefinition;
 import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.stem.analysis.ErrorResult;
 import org.eclipse.stem.analysis.automaticexperiment.ALGORITHM_STATUS;
 import org.eclipse.stem.analysis.automaticexperiment.AutomaticExperimentManager;
 import org.eclipse.stem.analysis.automaticexperiment.AutomaticExperimentManagerEvent;
 import org.eclipse.stem.analysis.automaticexperiment.AutomaticExperimentManagerListener;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithm;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithmEvent;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithmListener;
 import org.eclipse.stem.analysis.automaticexperiment.MANAGER_STATUS;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * This class is a SWT GUI component that uses BIRT to plot
  */
 public class AutoExpControl extends AnalysisControl {
 	
 	
 	private static Display display = null;
 	
 	
 	/**
 	 * Folder to contain the data
 	 * 
 	 */
 	static CTabFolder tabFolder;
 	
 	/**
 	 * Text displaying the currently selected parameter estimator
 	 */
 	
 	
 	/**
 	 * A label to report status (not yet used)
 	 */
 	static Label statusLabel;
 	
 	
 	/**
 	 * Colors for the time series chart
 	 */
 	static final ColorDefinition foreGround = ColorDefinitionImpl.create(180, 0, 0);
 	static final ColorDefinition backgroundGround = ColorDefinitionImpl.create(255, 255, 225);
 	static final ColorDefinition frameColor = ColorDefinitionImpl.WHITE();
 	
 	// some colors
 	private static Color cyan;
 	private static Color cyanDark;
 	private static Color darkRed;
 	private static Color white;
 	private static Color black;
 	
 	
	
	protected static RunHistoryCanvas errorConvergenceByRun = null;
 	static final int ERROR_CONVERGENCE_BY_RUN_ID = 0;
 	
 	protected static TimeSeriesCanvas currentErrorByTime = null;
 	static final int CURRENT_ERROR_BY_TIME_ID = 1;
 	
 	//protected static Composite chartComposite;
 	protected static Composite timeSeriesComposite;
 	protected static Composite runHistoryComposite;
 	protected static Composite valuesComposite;
 	protected static Composite controlsComposite;
 	
 	private static List<Double> errorHistoryList = new ArrayList<Double>();
 	private static double[] errorHistory;
 	private static double[] bestSeries;
 	private static double bestError = Double.MAX_VALUE;
 	private static double[] newTimeSeries;
 	
 	protected static String[] runParamNames;
 	protected static double[] initParamValues;
 	protected static double[] currentParamValues;
 	
 	protected static CLabel[] attributeLabels;
 	protected static CLabel[] initialValueLabels;
 	protected static CLabel[] latestValueLabels;
 	/**
 	 * The dialog for the wizard
 	 */
 	static WizardDialog wizardDialog;	
 	
 	//PropertySelector propertySelector;
 	List<ItemPropertyDescriptor> selectedProperties;
 
 	/**
 	 * 
 	 * @param parent
 	 */
 	public AutoExpControl(final Composite parent) {
 		super(parent, SWT.None);
 		createContents();
 	} // AutoExpControl
 	
 	
 
 	/**
 	 * Create the contents of the plotter
 	 */
 	void createContents() {
				
 		// Use form layout
 		setLayout(new FormLayout());
 		if(display==null) display = this.getDisplay();
 		cyan = display.getSystemColor(SWT.COLOR_CYAN);
 		cyanDark = new Color(display, 60, 240, 240);
 		darkRed = display.getSystemColor(SWT.COLOR_DARK_RED);
 		white = display.getSystemColor(SWT.COLOR_WHITE);
 		black = display.getSystemColor(SWT.COLOR_BLACK);
 		
 		
 		identifiableTitle = new Label(this, SWT.NONE);
 		identifiableTitle.setText(Messages.getString("AUTO.TITLELABEL"));
 		
 		
 		int bottom = 5;
 		final FormData titleFormData = new FormData();
 		identifiableTitle.setLayoutData(titleFormData);
 		titleFormData.top = new FormAttachment(0, 0);
 		titleFormData.bottom = new FormAttachment(bottom, 0);
 		titleFormData.left = new FormAttachment(0, 0);
 		titleFormData.right = new FormAttachment(100, 0);
 		
 		bottom += 1;
 		
 		tabFolder = new CTabFolder(this, SWT.BORDER);
 		// set up the values composite
 		valuesComposite = new Composite(tabFolder, SWT.BORDER);
 		
 		final FormData tabFormData = new FormData();
 		tabFormData.top = new FormAttachment(bottom, 0);
 		tabFormData.bottom = new FormAttachment(100, 0);
 		tabFormData.left = new FormAttachment(0, 0);
 		tabFormData.right = new FormAttachment(100, 0);
 		tabFolder.setLayoutData(tabFormData);
 		tabFolder.setSimple(false);
 		
 		
 		tabFolder.setSelectionBackground(new Color[]{display.getSystemColor(SWT.COLOR_CYAN),
 				display.getSystemColor(SWT.COLOR_WHITE),
 				cyanDark,
 				display.getSystemColor(SWT.COLOR_BLUE)},
 				new int[] {25, 50, 100}); 
 		
 		tabFolder.setBackground(cyanDark);
 		tabFolder.setForeground(darkRed);
 		tabFolder.setBorderVisible(true);
 		
         /////////////
 		// RUN HISTORY
 		CTabItem item0 = new CTabItem(tabFolder, SWT.BORDER);
 		item0.setText ("  "+Messages.getString("AUTO.HISTORY")+" ");
 		runHistoryComposite = new Composite(tabFolder, SWT.BORDER);
 		runHistoryComposite.setLayout(new FormLayout());
  		item0.setControl(runHistoryComposite);
  		// set up the four data time series inside the chartComposite
  		getRunHistoryChart(runHistoryComposite); 
  		
  		/////////////
 		// TIME SERIES
  		CTabItem item1 = new CTabItem(tabFolder, SWT.BORDER);
 		item1.setText ("  "+Messages.getString("AUTO.TIMESERIES")+" ");
 		timeSeriesComposite = new Composite(tabFolder, SWT.BORDER);
 		timeSeriesComposite.setLayout(new FormLayout());
  		item1.setControl(timeSeriesComposite);
  		// set up the four data time series inside the chartComposite
  		getTimeSeriesChart(timeSeriesComposite); 
  		
  		//////////////
  		// VALUES
 		CTabItem item2 = new CTabItem(tabFolder, SWT.BORDER);
 		item2.setText(Messages.getString("AUTO.VALUES"));
 		item2.setControl(valuesComposite);
 		
 		//////////////
  		// RESTART
 		controlsComposite = new Composite(tabFolder, SWT.BORDER);
 		controlsComposite.setLayout(new FormLayout());
 		CTabItem item3 = new CTabItem(tabFolder, SWT.BORDER);
 		item3.setText(Messages.getString("AUTO.RESTART"));
 		item3.setControl(controlsComposite);
 		Label restartLabel = new Label(controlsComposite, SWT.BORDER);
 		restartLabel.setText("Restart Controls Will Go HERE");
 		
 		
 		
 		tabFolder.setSelection(item0);
 		
 		AutomaticExperimentManager manager = AutomaticExperimentManager.getInstance();
 		manager.addListener(new AutomaticExperimentManagerListener() {
 			
 			@Override
 			public void eventReceived(AutomaticExperimentManagerEvent evt) {
 				if(evt.status == MANAGER_STATUS.SCHEDULED) {
 					ErrorAnalysisAlgorithm alg = evt.algorithm;
 					alg.addListener(new ErrorAnalysisAlgorithmListener() {
 						@Override
 						public void eventReceived(ErrorAnalysisAlgorithmEvent evt) {
 							
 							System.out.println("Event is "+evt.status);
 							
 							if(evt.status == ALGORITHM_STATUS.STARTING_SIMULATION) {
 								// set the label info
 								runParamNames = evt.parameterNames;
 								currentParamValues = evt.parameterValues;
 								// Add a Runnable to the UI thread's execution queue 
 								final Display display = Display.getDefault();
 								if (!display.isDisposed()) {
 									// Yes
 									try {
 										display.asyncExec(new Runnable() {
 											public void run() {
 												initializeValueLabels(runParamNames, currentParamValues);
 											} // run
 										}); // display.asyncExec
 									} // try
 									catch (final Exception e) {
 										// Ignore there could be a race condition with the display being disposed
 									} // catch Exception
 								} // if (!display.isDisposed()) 
 							//////////////////////////////////////
 							} else if(evt.status == ALGORITHM_STATUS.FINISHED_ALGORITHM) {
 								// The algorithm has finished. Smallest value in 
 								//evt.result
 								/////////////////
 								
 							} else if(evt.status == ALGORITHM_STATUS.FINISHED_SIMULATION) {
 								// One simulation is done. The result is READY and stored in evt.result
 								ErrorResult result = evt.result;
 								if(result != null) {
 									// Plot 1 from result.getError() (keep appending)
 									appendLatestErrorData(result.getError());
 									// Plot 2 from result.getErrorByTimestep() (same as we show in scenario comparison view)
 									setRecentTimeSeries(result.getError(), result.getErrorByTimeStep() );
 									final double[] latestVals = evt.parameterValues;
 									////////////////////////////////////////////////////////////////////////
 									// Add a Runnable to the UI thread's execution queue 
 									final Display display = Display.getDefault();
 									if (!display.isDisposed()) {
 										// Yes
 										try {
 											display.asyncExec(new Runnable() {
 												public void run() {
 													updateCharts();
 													updateValueLabels(latestVals);
 												} // run
 											}); // display.asyncExec
 										} // try
 										catch (final Exception e) {
 											// Ignore there could be a race condition with the display being disposed
 										} // catch Exception
 									} // if (!display.isDisposed()) 
 								}// if(result != null)
 							} else if(evt.status == ALGORITHM_STATUS.RESTARTED_ALGORITHM) {
 								// The algorithm has restarted. Smallest value in 
 								// evt.result
 							}
 						}
 					});
 				}
 			}
 		});
 		
 		updateCharts();
 		
 	} // createContents
 	
 	/**
 	 * update the graphs
 	 */
 	public static void updateCharts() {
 		errorConvergenceByRun.draw();
 		currentErrorByTime.draw();
 	}
 	
 	/**
 	 * redraw the graphs
 	 */
 	public static void redrawCharts() {
 		errorConvergenceByRun.redraw();
 		currentErrorByTime.redraw();
 	}
 	
 	
 	
 	/**
 	 * set up the time series chart
 	 * @param dataComposite
 	 */
 	private void getTimeSeriesChart(Composite dataComposite) {
 				
 		// currentErrorByTime
 		currentErrorByTime = new TimeSeriesCanvas(this,dataComposite,
 				Messages.getString("AUTO.TITLE2"),
 				Messages.getString("AUTO.ERROR"),// y axis
 				Messages.getString("AUTO.TIME"),// x axis
 				Messages.getString("AUTO.TITLE2"), //first property (line series name)
 				foreGround,
 				backgroundGround,
 				frameColor, CURRENT_ERROR_BY_TIME_ID, true);
 		final FormData TimeSeries2FormData = new FormData();
 		currentErrorByTime.setLayoutData(TimeSeries2FormData);
 		TimeSeries2FormData.top = new FormAttachment(dataComposite, 0);
 		TimeSeries2FormData.bottom = new FormAttachment(100, 0);
 		TimeSeries2FormData.left = new FormAttachment(0, 0);
 		TimeSeries2FormData.right = new FormAttachment(100, 0);
 		
 		currentErrorByTime.redraw();
 		
 	}// getEquationSeries
 	
 	
 	/**
 	 * set up the run history charts
 	 * @param dataComposite
 	 */
 	private void getRunHistoryChart(Composite dataComposite) {
 		// errorConvergenceByRun
		errorConvergenceByRun = new RunHistoryCanvas(this,dataComposite,
 				Messages.getString("AUTO.TITLE1"),
 				Messages.getString("AUTO.ERROR"), // yaxis
 				Messages.getString("AUTO.RUN"),// x axis
 				Messages.getString("AUTO.TITLE1"), // first propery (line series name)
 				foreGround,
 				backgroundGround,
 				frameColor, ERROR_CONVERGENCE_BY_RUN_ID, true);
 		final FormData TimeSeries1FormData = new FormData();
 		errorConvergenceByRun.setLayoutData(TimeSeries1FormData);
 		TimeSeries1FormData.top = new FormAttachment(dataComposite, 0);
 		TimeSeries1FormData.bottom = new FormAttachment(100, 0);
 		TimeSeries1FormData.left = new FormAttachment(0, 0);
 		TimeSeries1FormData.right = new FormAttachment(100, 0);
 		
 		errorConvergenceByRun.redraw();
 	
 	}// getEquationSeries
 	
 	
 	
 	/**
 	 * set up the four data time series charts
 	 * @param dataComposite
 	 */
 	protected static void updateValueLabels(double[] values) {
 		
 		for(int i = 0; i < values.length; i ++) {
 			latestValueLabels[i].setText(""+values[i]);
 		}
 		valuesComposite.redraw();
 	
 	}// updateValueLabels
 	
 	
 	/**
 	 * 
 	 * @param attributes
 	 * @param initialValues
 	 */
 	protected static void initializeValueLabels(String[] attributes, double[] initialValues) {
 		
 		int numAttrEntries = attributes.length;
 		int numColumns = numAttrEntries;
 
 		// Use form layout
 		valuesComposite.setLayout(new FormLayout());
 		int width = 100/numColumns;
 		int left = 0;
 		int right = width;
 		int height = 10;
 		
 		attributeLabels = new CLabel[numColumns];
 		initialValueLabels = new CLabel[numColumns];
 		latestValueLabels = new CLabel[numColumns];
 		
 			for(int i = 0; i < numColumns; i ++) {
 				
 				int bottom = height;
 				
 				attributeLabels[i] = new CLabel(valuesComposite, SWT.BORDER);
 				attributeLabels[i].setText(attributes[i]);
 				attributeLabels[i].setBackground(cyan);
 				final FormData titleFormData = new FormData();
 				attributeLabels[i].setLayoutData(titleFormData);
 				titleFormData.top = new FormAttachment(0, 0);
 				titleFormData.bottom = new FormAttachment(height, 0);
 				titleFormData.left = new FormAttachment(left, 0);
 				titleFormData.right = new FormAttachment(right, 0);
 				
 				bottom += height;
 				
 				initialValueLabels[i] = new CLabel(valuesComposite, SWT.BORDER);
 				initialValueLabels[i].setText(""+initialValues[i]);
 				initialValueLabels[i].setBackground(white);
 				initialValueLabels[i].setForeground(black);
 				final FormData initFormData = new FormData();
 				initialValueLabels[i].setLayoutData(initFormData);
 				initFormData.top = new FormAttachment(attributeLabels[i], 0);
 				initFormData.bottom = new FormAttachment(bottom, 0);
 				initFormData.left = new FormAttachment(left, 0);
 				initFormData.right = new FormAttachment(right, 0);
 				
 				bottom += height;
 				
 				latestValueLabels[i] = new CLabel(valuesComposite, SWT.BORDER);
 				latestValueLabels[i].setText(""+initialValues[i]); // start = initial values
 				latestValueLabels[i].setBackground(white);
 				latestValueLabels[i].setForeground(darkRed);
 				final FormData latestFormData = new FormData();
 				latestValueLabels[i].setLayoutData(latestFormData);
 				latestFormData.top = new FormAttachment(initialValueLabels[i], 0);
 				latestFormData.bottom = new FormAttachment(bottom, 0);
 				latestFormData.left = new FormAttachment(left, 0);
 				latestFormData.right = new FormAttachment(right, 0);
 				
 				left += width;
 				right += width;
 				
 				
 			}
 				
 		 
 		valuesComposite.layout();
 		//valuesComposite.pack();
 		//valuesComposite.setVisible(true);
 		valuesComposite.redraw();
 		
 	
 	}// updateValueLabels
 	
 	
 	
 	
 	
 
 	/**
 	 * @see org.eclipse.swt.widgets.Widget#dispose()
 	 */
 	@Override
 	public void dispose() {
 						
 		super.dispose();
 		
 	} // dispose
 
 	
 	/**
 	 * not used
 	 * @return null
 	 */
 	public Identifiable getIdentifiable() {
 		return null;
 	}
 
 	
 
 	/**
 	 * get the value for plotting
 	 * @see org.eclipse.stem.util.analysis.views.AnalysisControl#getValues(int, int)
 	 */
 	public double[] getValues(int chartIndex, int state) {
 		
 		if(chartIndex == 0) return errorHistory;
 		if(chartIndex == 1) {
 			if(state==0) return newTimeSeries;
 			return bestSeries;
 		}
 		// should never happen
 		return null;
 	}
 	
 	/**
 	 * Not used
 	 * @param chartIndex not used (only one chart)
 	 * @param state
 	 * @return property name
 	 */
 	public String getProperty(int chartIndex, int state) {
 		if(chartIndex==0) return Messages.getString("AUTO.TITLE1");
 		if(chartIndex==1) {
 			if(state==0) return Messages.getString("AUTO.TITLE2");
 			return Messages.getString("AUTO.TITLE3");
 		}
 		return  Messages.getString("AUTO.NOTFOUNDERROR");
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Not used
 	 * @param chartIndex not used (only one chart)
 	 * @return the number of properties, only one
 	 * @see org.eclipse.stem.util.analysis.views.AnalysisControl#getNumProperties(int chartIndex)
 	 */
 	public int getNumProperties(int chartIndex) {
 		if(chartIndex==ERROR_CONVERGENCE_BY_RUN_ID) return 1; // one thing to chart
 		if(chartIndex==CURRENT_ERROR_BY_TIME_ID) return 2; // two things to chart
 		return 0; // should never happen
 	}
 
 	/**
 	 * 
 	 * @param error
 	 */
 	public static void appendLatestErrorData(double error) {
 		errorHistoryList.add(new Double(error));
 		errorHistory = new double[errorHistoryList.size()];
 		for(int i = 0; i < errorHistoryList.size(); i ++) {
 			errorHistory[i] = errorHistoryList.get(i).doubleValue();
 		}
 	}
 
 	/**
 	 * 
 	 * @param errorByTimeStep
 	 */
 	public static void setRecentTimeSeries(double error, EList<Double> errorByTimeStep) {
 		newTimeSeries = new double[errorByTimeStep.size()];
 		for(int i = 0; i < errorByTimeStep.size(); i ++) {
 			newTimeSeries[i] = errorByTimeStep.get(i).doubleValue();
 		}
 		if(bestSeries==null) bestSeries = newTimeSeries;
 		if(error <= bestError) {
 			bestError = error;
 			bestSeries = newTimeSeries;
 		}
 		
 		
 	}
 
 
 
 	@Override
 	protected void initializeHeader(String folderName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public void remove() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	
 
 } // AutoExpControl

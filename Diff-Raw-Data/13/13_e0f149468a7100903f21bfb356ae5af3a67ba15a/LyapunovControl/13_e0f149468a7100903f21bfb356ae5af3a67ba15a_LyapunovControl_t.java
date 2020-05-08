 // EstimatorControl.java
 package org.eclipse.stem.util.analysis.views;
 
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
 import java.util.Map;
 
 import org.eclipse.birt.chart.model.attribute.ColorDefinition;
 import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.stem.analysis.ScenarioInitializationException;
 import org.eclipse.stem.diseasemodels.Activator;
 import org.eclipse.stem.util.analysis.CSVAnalysisWriter;
 import org.eclipse.stem.util.analysis.PhaseSpaceCoordinate;
 import org.eclipse.stem.util.analysis.ScenarioAnalysisSuite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * This class is a SWT GUI component that uses BIRT to plot
  */
 public class LyapunovControl extends AnalysisControl {
 	
 	/**
 	 * used to identify user preferences for this class
 	 */
 	private static final String CONSUMER = "LYAPUNOV_CONTROL";
 	
 	/**
 	 * used to remember the primary directory in the user preferences
 	 */
 	private static final String PRIMARY_FOLDER_KEY = CONSUMER+"_PRIMARY";
 	/**
 	 * used to remember the secondary directory in the user preferences
 	 */
 	private static final String SECONDARY_FOLDER_KEY = CONSUMER+"_SECONDARY";
 
 	/**
 	 *  First Scenario Label - indicates number of regions being watched for analysis.
 	 *  There are locations with relative value history providers attached
 	 */
 	Label firstScenarioFolderLabel;
 	
 	/**
 	 *  Second Scenario Label - indicates number of regions being watched for analysis.
 	 *  There are locations with relative value history providers attached
 	 */
 	Label secondScenarioFolderLabel;
 	
 	/**
 	 * First input text field for the scenario folder of data to use in making the estimation
 	 */
 	Text text1;
 	
 
 	/**
 	 * Second Input text field for the scenario folder of data to be compared
 	 **/
 	Text text2;
 	
 
 	
 	
 	/**
 	 * A label to report status (not yet used)
 	 */
 	Label statusLabel;
 	
 	
 	/**
 	 * Results of analysis
 	 */
 	Label resultsLabel;
 	
 	/**
 	 * the chart of two (or more) trajectories in phase space
 	 */
 	LyapunovTrajectoryCanvas lyapunovTrajectoryCanvas;
 	
 	/**
 	 * the time series chart of the divergence of the Lyapunov trajectory(ies) from the reference trajectory
 	 */
 	TimeSeriesCanvas timeSeriesCanvas;
 	/**
 	 * Colors for the time series chart
 	 */
 	static final ColorDefinition foreGround = ColorDefinitionImpl.create(180, 0, 0);
 	static final ColorDefinition backgroundGround = ColorDefinitionImpl.create(255, 255, 225);
 	static final ColorDefinition frameColor = ColorDefinitionImpl.WHITE();
 	
 	/**
 	 * Label for line series Data (legend)
 	 */
 
 	
 	/**
 	 * the results of various scenario comparisons
 	 * TODO: eventually this might be a map keyed by comparison id
 	 * for now there is only one comparison, the mean square difference.
 	 */
 	List<PhaseSpaceCoordinate[]> comparisonValues = new ArrayList<PhaseSpaceCoordinate[]>();
 	
 	/**
 	 * this is the cumulative deviation between the trajectories (relative to the reference
 	 * trajectory
 	 */
 	List<EList<Double>> cumulativeDeviation = new ArrayList<EList<Double>>();
 	
 	private static final String LYAPUNOV_FILE_PREFIX = "LyapunovAnalysis";
 	private static final String LYAPUNOV_DEVIATION_FILE_PREFIX = "LyapunovDeviation";
 	private static final String SELECT_FOLDER_DIALOG_TEXT = "Pick a scenario folder";
 	
 	ScenarioAnalysisSuite analyzer = new ScenarioAnalysisSuite(this);
 	
 	/**
 	 * 
 	 * @param parent
 	 */
 	public LyapunovControl(final Composite parent) {
 		super(parent, SWT.None);
 		createContents();
 	} // EstimatorControl
 
 	/**
 	 * Create the contents of the plotter
 	 */
 	private void createContents() {
 		setLayout(new FormLayout());
 
 		identifiableTitle = new Label(this, SWT.NONE);
 		identifiableTitle.setText(Messages.getString("LYA.TITLE"));
 		//propertySelector = new PropertySelector(this, SWT.NONE);
 		Display display = this.getDisplay();
 		
 		Color labelBackground = new Color(display, new RGB(180, 180, 200));
 		
 		statusLabel = new Label(this, SWT.BORDER);
 		statusLabel.setBackground(labelBackground);
 		statusLabel.setText("");
 		
 		firstScenarioFolderLabel = new Label(this, SWT.BORDER);
 		firstScenarioFolderLabel.setBackground(labelBackground);
 		firstScenarioFolderLabel.setText(Messages.getString("LYA.FOLDER1LABEL"));
 		
 		text1 = new Text(this, SWT.BORDER);
 	    text1.setBounds(10, 10, 100, 20);
 	    String primaryDir=prefs.getRecentFolder(PRIMARY_FOLDER_KEY);
 	    text1.setText(primaryDir);
 	    
 	    secondScenarioFolderLabel = new Label(this, SWT.BORDER);
 	    secondScenarioFolderLabel.setBackground(labelBackground);
 	    secondScenarioFolderLabel.setText(Messages.getString("LYA.FOLDER2LABEL"));
 		
 		text2 = new Text(this, SWT.BORDER);
 	    text2.setBounds(10, 10, 100, 20);
 	    String secondaryDir=prefs.getRecentFolder(SECONDARY_FOLDER_KEY);
 	    text2.setText(secondaryDir);
 		  
 	    Composite analyzeButtonComposite = getAnalyzeButtonComposite(this,Messages.getString("LYA.COMPARISON"));
 
 		resultsLabel = new Label(this, SWT.SHADOW_OUT);
 		resultsLabel.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
 		resultsLabel.setText(Messages.getString("LYA.RESULTS"));
 		
 		int deltaBottom = 5;
         int bottom = deltaBottom;
 		
 		final FormData titleFormData = new FormData();
 		identifiableTitle.setLayoutData(titleFormData);
 		titleFormData.top = new FormAttachment(0, 0);
 		titleFormData.bottom = new FormAttachment(bottom, 0);
 		titleFormData.left = new FormAttachment(0, 0);
 		titleFormData.right = new FormAttachment(100, 0);
 
 		bottom += deltaBottom;
         /////////////////////////////////////////////////////////////////////////
 		// TextField folder label
 		final FormData firstScenarioFolderLabelFormData = new FormData();
 		// propertySelectorFormData.top = new FormAttachment(cSVLoggerCanvas,
 		// 0);
 		firstScenarioFolderLabelFormData.top = new FormAttachment(identifiableTitle, 0);
 		firstScenarioFolderLabelFormData.bottom = new FormAttachment(bottom, 0);
 		firstScenarioFolderLabelFormData.left = new FormAttachment(0, 0);
 		firstScenarioFolderLabelFormData.right = new FormAttachment(15, 0);
 		firstScenarioFolderLabel.setLayoutData(firstScenarioFolderLabelFormData);
 		// first text field for parameter Estimator
 		final FormData text1FormData = new FormData();
 		text1FormData.top = new FormAttachment(identifiableTitle, 0);
 		text1FormData.bottom = new FormAttachment(bottom, 0);
 		text1FormData.left = new FormAttachment(firstScenarioFolderLabel, 0);
 		text1FormData.right = new FormAttachment(75, 0);
 		text1.setLayoutData(text1FormData);
 		
 		Button selectFolder1Button = new Button(this, SWT.NONE);
 		selectFolder1Button.setText(Messages.getString("LYA.SELECTFOLDER"));
 		final FormData selectFolder1FormData = new FormData();
 		selectFolder1FormData.top = new FormAttachment(identifiableTitle, 0);
 		selectFolder1FormData.bottom = new FormAttachment(bottom, 0);
 		selectFolder1FormData.left = new FormAttachment(text1, 0);
 		selectFolder1FormData.right = new FormAttachment(100, 0);
 		selectFolder1Button.setLayoutData(selectFolder1FormData);
 		
 		final Shell shell = this.getShell();
 		
 		selectFolder1Button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(@SuppressWarnings("unused")
 			final SelectionEvent e) {
 				final DirectoryDialog dd = new DirectoryDialog(
 						shell, SWT.OPEN);
 					dd.setText(Messages.getString("LYA.SELECTFOLDERDIALOG")); //$NON-NLS-1$
 					String beginSearch = text1.getText();
 					if((beginSearch==null)||(beginSearch.length()<1)) beginSearch = ROOT_PATH;
 					
 					dd.setFilterPath(beginSearch);
 					final String selected = dd.open();
 					if(selected!=null) text1.setText(selected);
 			}
 		});
         /////////////////////////////////////////////////////////////////////////
 		
 		bottom += deltaBottom;
 		
         /////////////////////////////////////////////////////////////////////////
 		// TextField folder label
 		final FormData secondScenarioFolderLabelFormData = new FormData();
 		// propertySelectorFormData.top = new FormAttachment(cSVLoggerCanvas,
 		// 0);
 		secondScenarioFolderLabelFormData.top = new FormAttachment(firstScenarioFolderLabel, 0);
 		secondScenarioFolderLabelFormData.bottom = new FormAttachment(bottom, 0);
 		secondScenarioFolderLabelFormData.left = new FormAttachment(0, 0);
 		secondScenarioFolderLabelFormData.right = new FormAttachment(15, 0);
 		secondScenarioFolderLabel.setLayoutData(secondScenarioFolderLabelFormData);
 		// first text field for parameter Estimator
 		final FormData text2FormData = new FormData();
 		text2FormData.top = new FormAttachment(text1, 0);
 		text2FormData.bottom = new FormAttachment(bottom, 0);
 		text2FormData.left = new FormAttachment(secondScenarioFolderLabel, 0);
 		text2FormData.right = new FormAttachment(75, 0);
 		text2.setLayoutData(text2FormData);
 		
 		Button selectFolder2Button = new Button(this, SWT.NONE);
 		selectFolder2Button.setText(Messages.getString("LYA.SELECTFOLDER"));
 		final FormData selectFolder2FormData = new FormData();
 		selectFolder2FormData.top = new FormAttachment(text1, 0);
 		selectFolder2FormData.bottom = new FormAttachment(bottom, 0);
 		selectFolder2FormData.left = new FormAttachment(text2, 0);
 		selectFolder2FormData.right = new FormAttachment(100, 0);
 		selectFolder2Button.setLayoutData(selectFolder2FormData);
 		
 		selectFolder2Button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(@SuppressWarnings("unused")
 			final SelectionEvent e) {
 				final DirectoryDialog dd = new DirectoryDialog(
 						shell, SWT.OPEN);
 					dd.setText(SELECT_FOLDER_DIALOG_TEXT); //$NON-NLS-1$
 					String beginSearch = text2.getText();
 					if((beginSearch==null)||(beginSearch.length()<1)) beginSearch = ROOT_PATH;
 					
 					dd.setFilterPath(beginSearch);
 					final String selected = dd.open();
 					if(selected!=null) text2.setText(selected);
 			}
 		});
 		
 		/////////////////////////////////////////////////////////////////////////
 		
 		bottom += deltaBottom;
 		// AnalyzeButton
 		final FormData analysisButtonFormData = new FormData();
 		// propertySelectorFormDataX.top = new FormAttachment(propertySelectorY,
 		// 0);
 		analysisButtonFormData.top = new FormAttachment(text2, 0);
 		analysisButtonFormData.bottom = new FormAttachment(bottom, 0);
 		analysisButtonFormData.left = new FormAttachment(0, 0);
 		analysisButtonFormData.right = new FormAttachment(100, 0);
 		analyzeButtonComposite.setLayoutData(analysisButtonFormData);
 		
 		
 		
 		// Trajectories
 		lyapunovTrajectoryCanvas = new LyapunovTrajectoryCanvas(this);
 		final FormData trajectoryChartFormData = new FormData();
 		lyapunovTrajectoryCanvas.setLayoutData(trajectoryChartFormData);
 		trajectoryChartFormData.top = new FormAttachment(bottom, 0);
 		trajectoryChartFormData.bottom = new FormAttachment(100, 0);
 		trajectoryChartFormData.left = new FormAttachment(0, 0);
 		trajectoryChartFormData.right = new FormAttachment(50, 0);
 		
 		// divergence graph
 		timeSeriesCanvas = new TimeSeriesCanvas(this,
 													Messages.getString("LYA.DIVERGENCELEGEND"),
 													Messages.getString("LYA.DIVERGENCEYAXIS"),
 													Messages.getString("LYA.DIVERGENCEYAXIS"),
 													foreGround,
 													backgroundGround,
 													frameColor, 0);
 		
 		
 		
 		final FormData analysisChartFormData = new FormData();
 		timeSeriesCanvas.setLayoutData(analysisChartFormData);
 		analysisChartFormData.top = new FormAttachment(bottom, 0);
 		analysisChartFormData.bottom = new FormAttachment(100, 0);
 		analysisChartFormData.left = new FormAttachment(50, 0);
 		analysisChartFormData.right = new FormAttachment(100, 0);
 		
 		
 		final ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(this.getShell());
 
 		analyzeButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(@SuppressWarnings("unused")
 			final SelectionEvent e) {
 				
 				String referenceDirectory  = text1.getText();
 				String comparisonDirectory = text2.getText();
 				
 				/*
 				 * reset the label text
 				 */
 			    identifiableTitle.setText(Messages.getString("LYA.TITLE"));
 			    /*
 				 * reinitialize the graph
 				 */
 				comparisonValues.clear();
 				cumulativeDeviation.clear();
 				lyapunovTrajectoryCanvas.reset();
 			    
 				/*
 				 * VALIDATE the text input Fields
 				 */
 				if(!analyzer.validate(referenceDirectory)) {
 					identifiableTitle.setText(ScenarioAnalysisSuite.NOT_FOUND_MSG);
 					text1.setText("");
 				}
                 if(!analyzer.validate(comparisonDirectory)) {
                 	identifiableTitle.setText(ScenarioAnalysisSuite.NOT_FOUND_MSG);
                 	text2.setText("");
 				}
                 if((!analyzer.validate(referenceDirectory)) || (!analyzer.validate(comparisonDirectory))) {
                 	return;
                 }
                 
 				try {
 					comparisonValues.addAll( analyzer.getLyapunovTrajectory(referenceDirectory, comparisonDirectory, progressDialog) );
 					//success... remember the users prefs
 					prefs.setRecentFolder(PRIMARY_FOLDER_KEY,referenceDirectory);
 					prefs.setRecentFolder(SECONDARY_FOLDER_KEY,comparisonDirectory);
 				} catch(ScenarioInitializationException sie) {
 					Activator.logError("", sie);
 				}
 				
 				cumulativeDeviation.addAll(ScenarioAnalysisSuite.getCumulativePhaseSpaceDeviation(comparisonValues));
 				
 				statusLabel.setText(Messages.getString("LYA.COMPLETE"));
 				lyapunovTrajectoryCanvas.draw();
 				
 				// Log the trajectory data
 				String outFileName = LYAPUNOV_FILE_PREFIX+"_"+
 				 getScenarioNameFromDirectoryName(referenceDirectory)+
 				 "_"+
 				 getScenarioNameFromDirectoryName(comparisonDirectory);
 				
 				CSVAnalysisWriter writer = new CSVAnalysisWriter(outFileName);
 				writer.logData(2,comparisonValues);
 
 				// Log the cumulative deviation data
 				outFileName = LYAPUNOV_DEVIATION_FILE_PREFIX+"_"+
 				 getScenarioNameFromDirectoryName(referenceDirectory)+
 				 "_"+
 				 getScenarioNameFromDirectoryName(comparisonDirectory);
 				 CSVAnalysisWriter writer2 = new CSVAnalysisWriter(outFileName);
 				 writer2.logData(cumulativeDeviation);
 				 timeSeriesCanvas.draw();
 			}
 		});
 		
 	} // createContents
 	
 	
 	
 
 
 	/**
 	 * @param dirName
 	 * @return short name of scenario
 	 */
 	public static String getScenarioNameFromDirectoryName(String dirName) {
 		int last = dirName.lastIndexOf("/");
 		int last2 = dirName.lastIndexOf("\\");
 		if (last2 > last)
 			last = last2;
 		if (last <= 0)
 			last = 0;
 		String retVal = dirName.substring(last + 1, dirName.length());
 		return retVal;
 	}
 	
 	/**
 	 * This returns the results of a comparison of type comparisonType
 	 * as an array of double. 
 	 * 
 	 * @param trajectory the trajectory to return
 	 * @return comparison
 	 */
 	public PhaseSpaceCoordinate[] getPointValues(int trajectory) {
 		
 		return comparisonValues.get(trajectory);
 	}
 	
 	/**
 	 * returns the analysis of lyapunov tractory(ies) relative to reference.
 	 * @param chartIndex not used (only one chart)
 	 * @param trajectory 
 	 * @return Cumulative deviation from reference trajectory
 	 */
 	@SuppressWarnings("boxing")
 	@Override
 	public double[] getValues(int chartIndex, int trajectory) {
 		
 		EList<Double> l = cumulativeDeviation.get(trajectory);
 		double [] res = new double[l.size()];
 		for(int i=0;i<res.length;++i) res[i] = l.get(i);
 		return res;
 	}
 
 	/**
 	 * Not used
 	 * @param chartIndex not used (only one chart)
 	 * @param state
 	 * @return property name
 	 */
 	@Override
 	public String getProperty(int chartIndex, int state) {
 		return Messages.getString("LYA.DIVERGENCEYAXIS");
 	}
 	
 	/**
 	 * Only one property to plot = the RMS difference
 	 * @param chartIndex not used (only one chart)
 	 * @return the number of properties
 	 * @see org.eclipse.stem.util.analysis.views.AnalysisControl#getNumProperties(int chartIndex)
 	 */
 	@Override
 	public int getNumProperties(int chartIndex) {
 		return 1;
 	}
 
 
 	
 	
 	/**
 	 * to remove the control e.g. by a remove button event
 	 */
 	@Override
 	public void remove() {
 		updateStatusLabel();
 	}
 
 	
 	
 
 	protected void updateStatusLabel() {
 		statusLabel.setText(AnalysisControl.STATUS_TEXT);
 	}
 	
 	
 
 	/**
 	 * Initialize the header label
 	 * 
 	 * @param folderName
 	 */
 	@Override
 	protected void initializeHeader(String folderName) {
 		simulationNameLabel.setText("analyzing "+folderName);
 
 	} // initializeFromSimulation
 
 
 	
 	
 	
 
 	/**
 	 * @see org.eclipse.swt.widgets.Widget#dispose()
 	 */
 	@Override
 	public void dispose() {
 						
 		super.dispose();
 		
 	} // dispose
 
 	/**
 	 * Each Control class may add objects to this map
 	 * @return the control parameters maps
 	 */
 	public Map<String, Object> getControlParametersMap() {
 		// add nothing for now
 		return controlParametersMap;
 	}
 	
 
 } // EstimatorControl

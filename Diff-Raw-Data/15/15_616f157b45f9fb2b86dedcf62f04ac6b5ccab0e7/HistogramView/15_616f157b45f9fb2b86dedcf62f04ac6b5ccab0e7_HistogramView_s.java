 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.views;
 
 import gda.observable.IObservable;
 import gda.observable.IObserver;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
import java.util.Map;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUI;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.mapfunctions.AbstractMapFunction;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.mapfunctions.SpecialExposureFunction;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DAppearance;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DGraphTable;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.Plot1DStyles;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.ScaleType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.TickFormatting;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.GlobalColourMaps;
 import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
 import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;
 
 /**
  * View that shows a histogram of a DataSet object, it allows to navigate in 
  * the histogram and build a colour table mapping
  */
 
 public class HistogramView extends ViewPart implements SelectionListener,
 		IObservable, IObserver {
 	
 //	private static final Logger logger = LoggerFactory.getLogger(HistogramView.class);
 
 	/**
 	 * 
 	 */
 	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView"; //$NON-NLS-1$
 
 	private static HashMap<String, Integer> colourSettings = new HashMap<String,Integer>();
 	
 	protected List<AbstractDataset> histograms = null;
 	protected List<IObserver> observers = 
 		Collections.synchronizedList(new LinkedList<IObserver>());
 
 	protected DataSetPlotter histogramPlotter;
 	protected AxisValues xAxis = null;
 	protected Composite parent;
 	protected int histogramSize = 128;
 
 	private static final int SLIDERSECTIONS = 1000; // Number of discrete points the sliders for max and min sliders have
 	protected AbstractDataset data = null;
 
 	private CCombo cmbColourMap;
 	private Composite composite;
 	private Composite container;
 	private Button btnExpertMode;
 	protected CCombo cmbAlpha;	
 	protected CCombo cmbRedColour;
 	protected CCombo cmbGreenColour;
 	protected CCombo cmbBlueColour;
 	protected Button btnGreenInverse;
 	protected Button btnBlueInverse;
 	protected Button btnAlphaInverse;
 	protected Button btnRedInverse;
 	protected double specialMinThreshold = 0.1;
 	protected double specialMaxThreshold = 0.9;
 	protected int curRedSelect = 3;
 	protected int curGreenSelect = 3;
 	protected int curBlueSelect = 3;
 	protected int curAlphaSelect = 3;
 	protected boolean curRedInverse = false;
 	protected boolean curGreenInverse = false;
 	protected boolean curBlueInverse = false;
 	protected boolean curAlphaInverse = false;
 	
 	private Spinner spnMinValue;
     private Spinner spnMaxValue;
     private Slider  sldMinValue;
     private Slider  sldMaxValue;
 	private FloatSpinner spnRangeStart;
 	private FloatSpinner spnRangeStop;
     private Histogram histogramFunc;
 	protected IPlotUI histogramUI = null;
 
 	class MaxMin {
 		public double max = Double.NaN;
 		public double min = Double.NaN;
 	}
 	private MaxMin currentMaxMin;
	private Map<Integer, MaxMin> cachedMaxMin;
 
 	private boolean autoContrast = true;
 	private boolean lockRange = false;
 	private boolean useLog = false;
 	private String id;
 	private String sId;
 	private Composite advancedComp;
 	private Group expertGroup;
 	private Group threshPanel;
 	private Group rangePanel;
 
 	/**
 	 * Default constructor
 	 */
 	
 	public HistogramView() {
 		histogramFunc = new Histogram(histogramSize);
 		xAxis = new AxisValues();
		cachedMaxMin = new HashMap<Integer, MaxMin>();
 	}
 
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 		id = site.getId();
 		sId = site.getSecondaryId();
 		if (sId == null) {
 			setPartName("Colour Mapping");
 		} else {
 			try {
 				// You can see it's a histogram by looking but
 				// you really need the file name to know which
 				// view is corresponding to which file.
 				setPartName("Colour Mapping: " + (new File(sId)).getName());
 			} catch (Exception ne) {
 				setPartName("Colour Mapping");
 			}
 		}
 	}
 	
 	protected void buildToolbar() {
 		IActionBars toolBar = getViewSite().getActionBars();		
 		histogramUI = new HistogramUI(this,toolBar,histogramPlotter);
 		histogramUI.addIObserver(this);
 		histogramPlotter.registerUI(histogramUI);		
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		this.parent = parent;
 		container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout(1,false));
 	
 		final Composite top = new Composite(container, SWT.NONE);
 		top.setLayout(new GridLayout(3, false));
 		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		
 		final Label scheme = new Label(top, SWT.NONE);
 		scheme.setText("Colour Scheme");
 		cmbColourMap = new CCombo(top, SWT.BORDER | SWT.READ_ONLY);
 		cmbColourMap.setToolTipText("Change the color scheme.");
 		cmbColourMap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		btnExpertMode = new Button(top, SWT.CHECK);
 		btnExpertMode.setText("Expert");
 		btnExpertMode.setToolTipText("Enable expert mode for customising colour channels");
 		btnExpertMode.addSelectionListener(this);
 
 		advancedComp = new Composite(container, SWT.NONE);
 		//controlGroup.setText("RGB Settings");   // Looks a bit busy with this label
 		advancedComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		advancedComp.setLayout(new GridLayout(3, false));
 
 		expertGroup = new Group(advancedComp, SWT.NONE);
 		expertGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		expertGroup.setLayout(new GridLayout(3, false));
 		{
 			Label lblRed = new Label(expertGroup, SWT.NONE);
 			lblRed.setText("Red");
 			cmbRedColour = new CCombo(expertGroup, SWT.BORDER | SWT.READ_ONLY);
 
 			btnRedInverse = new Button(expertGroup, SWT.CHECK);
 			btnRedInverse.setText("Inverse");
 			btnRedInverse.addSelectionListener(this);
 
 			Label lblGreen = new Label(expertGroup, SWT.NONE);
 			lblGreen.setText("Green");
 			cmbGreenColour = new CCombo(expertGroup, SWT.BORDER | SWT.READ_ONLY);
 			btnGreenInverse = new Button(expertGroup, SWT.CHECK);
 			btnGreenInverse.setText("Inverse");
 			btnGreenInverse.addSelectionListener(this);
 
 			Label lblBlue = new Label(expertGroup, SWT.NONE);
 			lblBlue.setText("Blue");
 			cmbBlueColour = new CCombo(expertGroup, SWT.BORDER | SWT.READ_ONLY);
 			btnBlueInverse = new Button(expertGroup, SWT.CHECK);
 			btnBlueInverse.setText("Inverse");
 			btnBlueInverse.addSelectionListener(this);
 
 			Label lblAlpha = new Label(expertGroup, SWT.NONE);
 			lblAlpha.setText("Alpha");
 			cmbAlpha = new CCombo(expertGroup, SWT.BORDER | SWT.READ_ONLY);
 			btnAlphaInverse = new Button(expertGroup, SWT.CHECK);
 			btnAlphaInverse.setText("Inverse");
 			btnAlphaInverse.addSelectionListener(this);
 		}
 		expertGroup.setVisible(getPreferenceColourMapExpertMode());
 		((GridData) expertGroup.getLayoutData()).exclude = !getPreferenceColourMapExpertMode();
 
 		threshPanel = new Group(advancedComp, SWT.NONE);
 		threshPanel.setText("Mark's special threshhold");  // Looks a bit busy with this label
 		threshPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
 		threshPanel.setLayout(new GridLayout(2, false));
 		{
 			Label lblMinValue = new Label(threshPanel, SWT.NONE);
 			lblMinValue.setText("Min threshold");
 			spnMinValue = new Spinner(threshPanel, SWT.BORDER);
 			spnMinValue.setMinimum(0);
 			spnMinValue.setMaximum(100);
 			spnMinValue.addSelectionListener(this);
 			Label lblMaxValue = new Label(threshPanel, SWT.NONE);
 			lblMaxValue.setText("Max threshold");
 			spnMaxValue = new Spinner(threshPanel, SWT.BORDER);
 			spnMaxValue.setMinimum(0);
 			spnMaxValue.setMaximum(100);
 			spnMaxValue.addSelectionListener(this);
 		}
 		threshPanel.setVisible(false);
 		((GridData) threshPanel.getLayoutData()).exclude = true;
 
 		rangePanel = new Group(container, SWT.NONE);
 		rangePanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		rangePanel.setLayout(new GridLayout(3, false));
 		rangePanel.setText("Histogram range");
 		{
 			Label lblStartValue = new Label(rangePanel, SWT.NONE);
 			lblStartValue.setText("Minimum");
 			spnRangeStart = new FloatSpinner(rangePanel, SWT.BORDER);
 			GridData gridData = new GridData(SWT.NONE, SWT.NONE, true, false);
 			spnRangeStart.setLayoutData(gridData);
 			spnRangeStart.addSelectionListener(this);
 			sldMinValue = new Slider(rangePanel, SWT.BORDER);
 			gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
 			gridData.minimumWidth = 90;
 			sldMinValue.setLayoutData(gridData);
 			sldMinValue.addSelectionListener(this);
 			sldMinValue.setMinimum(0);
 			sldMinValue.setMaximum(SLIDERSECTIONS);
 			sldMinValue.setIncrement(1);
 			Label lblStopValue = new Label(rangePanel, SWT.NONE);
 			lblStopValue.setText("Maximum");
 			gridData = new GridData(SWT.NONE, SWT.NONE, true, false);
 			spnRangeStop = new FloatSpinner(rangePanel, SWT.BORDER);
 			spnRangeStop.setLayoutData(gridData);
 			spnRangeStop.addSelectionListener(this);
 			spnRangeStart.setEnabled(false);
 			spnRangeStop.setEnabled(false);
 			sldMaxValue = new Slider(rangePanel, SWT.BORDER);
 			gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
 			gridData.minimumWidth = 90;
 			sldMaxValue.setLayoutData(gridData);
 			sldMaxValue.addSelectionListener(this);
 			sldMaxValue.setMinimum(0);
 			sldMaxValue.setMaximum(SLIDERSECTIONS);
 			sldMaxValue.setIncrement(1);
 
 			spnRangeStart.setEnabled(false);
 			spnRangeStop.setEnabled(false);
 			sldMinValue.setEnabled(false);
 			sldMaxValue.setEnabled(false);
 		}
 		{
 			composite = new Composite(container, SWT.NONE);
 			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			composite.setLayout(new FillLayout());
 
 			histogramPlotter = new DataSetPlotter(PlottingMode.BARCHART, composite, false);
 			histogramPlotter.setXAxisLabel("Intensity Value");
 			histogramPlotter.setYAxisLabel("Count");
 			histogramPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
 			histogramPlotter.setXTickLabelFormat(TickFormatting.plainMode);
 			histogramPlotter.setXAxisValues(xAxis, 1);
 			
 			Plot1DGraphTable colourTable = histogramPlotter.getColourTable();
 			Plot1DAppearance plotApp = new Plot1DAppearance(Color.RED, Plot1DStyles.SOLID, "");
 			colourTable.addEntryOnLegend(plotApp);
 			plotApp = new Plot1DAppearance(Color.GREEN, Plot1DStyles.SOLID, "");
 			colourTable.addEntryOnLegend(plotApp);
 			plotApp = new Plot1DAppearance(Color.BLUE, Plot1DStyles.SOLID, "");
 			colourTable.addEntryOnLegend(plotApp);
 			plotApp = new Plot1DAppearance(Color.ORANGE, Plot1DStyles.SOLID, "");
 			colourTable.addEntryOnLegend(plotApp);
 		}
 
 		cmbColourMap.addSelectionListener(this);
 		cmbRedColour.addSelectionListener(this);
 		cmbGreenColour.addSelectionListener(this);
 		cmbBlueColour.addSelectionListener(this);
 		cmbAlpha.addSelectionListener(this);
 		buildToolbar();
 		GlobalColourMaps.InitializeColourMaps();
 		fillupColourComboBoxes();
 		fillupColourMapBox();
 		buildGradientImage();
 	}
 
 	/**
 	 * Clear out all entries in the colour map combo box
 	 */
 	public void clearColourMapBox() {
 		cmbColourMap.removeAll();
 	}
 	
 	private void fillupColourMapBox() {
 		for (int i = 0; i < GlobalColourMaps.colourMapNames.length; i++)
 			cmbColourMap.add(GlobalColourMaps.colourMapNames[i]);
 		if (colourSettings.get(getPartName()) == null)
 			cmbColourMap.select(getPreferenceColourMapChoice());
 		else
 			cmbColourMap.select(colourSettings.get(getPartName()));
 
 		if (cmbColourMap.getItem(cmbColourMap.getSelectionIndex()).equals("Mark's special")) {
 			spnMinValue.setSelection((int)(specialMinThreshold*100));
 			spnMaxValue.setSelection((int)(specialMaxThreshold*100));
 			threshPanel.setVisible(true);
 			((GridData) threshPanel.getLayoutData()).exclude = false;
 			container.layout();
 		}
 
 		cmbAlpha.select(2);
 		curAlphaSelect = 2;
 		buildSelection();
 	}
 
 	protected void buildSelection() {
 		int selectNr =  cmbColourMap.getSelectionIndex();
 		int redSelect = GlobalColourMaps.colourSelectList.get(selectNr*4);
 		int greenSelect = GlobalColourMaps.colourSelectList.get(selectNr*4+1);
 		int blueSelect = GlobalColourMaps.colourSelectList.get(selectNr*4+2);
 		int alphaSelect = GlobalColourMaps.colourSelectList.get(selectNr*4+3);
 		cmbRedColour.select(Math.abs(redSelect));
 		cmbGreenColour.select(Math.abs(greenSelect));
 		cmbBlueColour.select(Math.abs(blueSelect));
 		curAlphaSelect = cmbAlpha.getSelectionIndex();
 		curRedSelect = cmbRedColour.getSelectionIndex();
 		curGreenSelect = cmbGreenColour.getSelectionIndex();
 		curBlueSelect = cmbBlueColour.getSelectionIndex();						
 	//	cmbAlpha.select(Math.abs(alphaSelect));
 		if (redSelect < 0)
 			btnRedInverse.setSelection(true);
 		else
 			btnRedInverse.setSelection(false);
 		if (greenSelect < 0)
 			btnGreenInverse.setSelection(true);
 		else
 			btnGreenInverse.setSelection(false);
 		if (blueSelect < 0)
 			btnBlueInverse.setSelection(true);
 		else
 			btnBlueInverse.setSelection(false);
 		if (alphaSelect < 0)
 			btnAlphaInverse.setSelection(false);
 		curAlphaInverse = btnAlphaInverse.getSelection();
 		curRedInverse = btnRedInverse.getSelection();
 		curGreenInverse = btnGreenInverse.getSelection();
 		curBlueInverse = btnBlueInverse.getSelection();
 	}
 
 	protected void fillupColourComboBoxes() {
 		for (int i = 0; i < GlobalColourMaps.mappingFunctions.size(); i++) {
 			String function = GlobalColourMaps.mappingFunctions.get(i).getMapFunctionName();
 			cmbRedColour.add(function);
 			cmbGreenColour.add(function);
 			cmbBlueColour.add(function);
 			cmbAlpha.add(function);			
 		}		
 	}
 
 	protected void buildGradientImage() {
 		GlobalColourMaps.mappingFunctions.get(0);
 		AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(cmbRedColour.getSelectionIndex());
 		AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(cmbGreenColour.getSelectionIndex());
 		AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(cmbBlueColour.getSelectionIndex());
 		AbstractMapFunction alphaFunc = GlobalColourMaps.mappingFunctions.get(cmbAlpha.getSelectionIndex());
 		boolean negRed = btnRedInverse.getSelection();
 		boolean negGreen = btnGreenInverse.getSelection();
 		boolean negBlue = btnBlueInverse.getSelection();
 		boolean negAlpha = btnAlphaInverse.getSelection();
 		histogramPlotter.applyColourCast(redFunc, greenFunc, blueFunc, alphaFunc, negRed, negGreen, negBlue, negAlpha, 0, 256);
 		histogramPlotter.refresh(false);
 	}
 	
 	private void notifyObservers(HistogramUpdate update) {
 		Iterator<IObserver> iter = observers.iterator();
 		while (iter.hasNext()) {
 			IObserver listener = iter.next();
 			listener.update(this,update);
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		// Nothing to do here
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// Nothing to do here
 	}
 
 	protected boolean hasData()	{
 		return data != null;
 	}
 	
 	protected void handleChange(@SuppressWarnings("unused") int channel) {
 	}
 	
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (e.getSource().equals(cmbRedColour)
 				|| e.getSource().equals(cmbGreenColour)
 				|| e.getSource().equals(cmbBlueColour)
 				|| e.getSource().equals(cmbAlpha)
 				|| e.getSource().equals(btnRedInverse)
 				|| e.getSource().equals(btnGreenInverse)
 				|| e.getSource().equals(btnBlueInverse)
 				|| e.getSource().equals(btnAlphaInverse)) {
 			curAlphaSelect = cmbAlpha.getSelectionIndex();
 			curRedSelect = cmbRedColour.getSelectionIndex();
 			curGreenSelect = cmbGreenColour.getSelectionIndex();
 			curBlueSelect = cmbBlueColour.getSelectionIndex();	
 			curAlphaInverse = btnAlphaInverse.getSelection();
 			curRedInverse = btnRedInverse.getSelection();
 			curGreenInverse = btnGreenInverse.getSelection();
 			curBlueInverse = btnBlueInverse.getSelection();
 			if (hasData()) {
 				if (e.getSource().equals(cmbRedColour))
 					handleChange(0);
 				else if (e.getSource().equals(cmbGreenColour))
 					handleChange(1);
 				else if (e.getSource().equals(cmbBlueColour))
 					handleChange(2);
 				else if (e.getSource().equals(cmbAlpha))
 					handleChange(3);
 					
 				updateChannelGraphs();
 				generateHistogramUpdate();
 			}
 			if (e.getSource().equals(cmbGreenColour) ||
 				e.getSource().equals(cmbRedColour) ||
 				e.getSource().equals(btnRedInverse) ||
 				e.getSource().equals(btnGreenInverse) ||
 				e.getSource().equals(btnBlueInverse) ||
 				e.getSource().equals(btnAlphaInverse))
 				histogramPlotter.refresh(true);
 			buildGradientImage();			
 		} else if (e.getSource().equals(cmbColourMap)) {
 		    colourSettings.put(getPartName(), cmbColourMap.getSelectionIndex());			
 			buildSelection();
 			handleChange(0);
 			handleChange(1);
 			handleChange(2);
 			if (hasData()) {
 				updateChannelGraphs();
 				generateHistogramUpdate();
 			}
 			histogramPlotter.refresh(true);
 			buildGradientImage();
 			if (cmbColourMap.getItem(cmbColourMap.getSelectionIndex()).equals("Mark's special"))
 			{
 				spnMinValue.setSelection((int)(specialMinThreshold*100));
 				spnMaxValue.setSelection((int)(specialMaxThreshold*100));
 				threshPanel.setVisible(true);
 				((GridData) threshPanel.getLayoutData()).exclude = false;
 			} else {
 				threshPanel.setVisible(false);
 				((GridData) threshPanel.getLayoutData()).exclude = true;
 			}
 			container.layout();
 		} else if (e.getSource().equals(spnMinValue) ||
 				   e.getSource().equals(spnMaxValue)) {
 			SpecialExposureFunction redFunc = (SpecialExposureFunction)GlobalColourMaps.mappingFunctions.get(cmbRedColour
 					.getSelectionIndex());
 			SpecialExposureFunction greenFunc = (SpecialExposureFunction)GlobalColourMaps.mappingFunctions.get(cmbGreenColour
 					.getSelectionIndex());
 			SpecialExposureFunction blueFunc = (SpecialExposureFunction)GlobalColourMaps.mappingFunctions.get(cmbBlueColour
 					.getSelectionIndex());
 			redFunc.setThresholds(spnMinValue.getSelection()/100.0, spnMaxValue.getSelection()/100.0);
 			greenFunc.setThresholds(spnMinValue.getSelection()/100.0, spnMaxValue.getSelection()/100.0);
 			blueFunc.setThresholds(spnMinValue.getSelection()/100.0, spnMaxValue.getSelection()/100.0);
 			specialMaxThreshold = spnMaxValue.getSelection()/100.0;
 			specialMinThreshold = spnMinValue.getSelection()/100.0;
 			updateChannelGraphs();
 			generateHistogramUpdate();
 			histogramPlotter.refresh(true);
 		} else if (e.getSource().equals(spnRangeStart.getControl()) ||
 				   e.getSource().equals(spnRangeStop.getControl())) {
 			final double min = spnRangeStart.getDouble();
 			final double max = spnRangeStop.getDouble();
 			updateHistogramGraph(min, max);		
 		} else if (e.getSource().equals(sldMinValue) ||
 				   e.getSource().equals(sldMaxValue)) {
 			final double min = spnRangeStart.getMinimum() + 
 							   spnRangeStart.getRange() * (sldMinValue.getSelection()/(double)sldMinValue.getMaximum());
 			final double max = spnRangeStart.getMinimum() + 
 							   spnRangeStart.getRange() * (sldMaxValue.getSelection()/(double)sldMaxValue.getMaximum());
 			spnRangeStart.setDouble(min);
 			spnRangeStop.setDouble(max);
 			if (hasData())
 				updateHistogramGraph(min, max);
 		} else if (e.getSource().equals(btnExpertMode)) {
 			expertGroup.setVisible(btnExpertMode.getSelection());
 			((GridData) expertGroup.getLayoutData()).exclude = !btnExpertMode.getSelection();
 			container.layout();
 		}
 
 	}
 
 	protected void generateHistogramUpdate() {
 		HistogramUpdate update = new HistogramUpdate(GlobalColourMaps.mappingFunctions
 				.get(curRedSelect), GlobalColourMaps.mappingFunctions
 				.get(curGreenSelect), GlobalColourMaps.mappingFunctions
 				.get(curBlueSelect), GlobalColourMaps.mappingFunctions
 				.get(curAlphaSelect), btnRedInverse
 				.getSelection(), btnGreenInverse.getSelection(), btnBlueInverse
 				.getSelection(), btnAlphaInverse.getSelection(),
 				currentMaxMin.min, currentMaxMin.max);
 		notifyObservers(update);
 	}
 
 	protected void updateChannelGraphs() {
 		if (histograms != null && histograms.size() > 1) {
 			// remove red channel graph
 			histograms.remove(0);
 			// remove green channel graph
 			histograms.remove(0);
 			// remove blue channel graph
 			histograms.remove(0);
 			// remove alpha channel graph
 			histograms.remove(0);
 		}
 		AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(curRedSelect);
 		AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(curGreenSelect);
 		AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(curBlueSelect);
 		AbstractMapFunction alphaFunc = GlobalColourMaps.mappingFunctions.get(curAlphaSelect);
 		
 		double maxValue = histograms.get(0).max().doubleValue();
 		int currentSize = histograms.get(0).getSize();
 		DoubleDataset redChannel = new DoubleDataset(currentSize);
 		DoubleDataset greenChannel = new DoubleDataset(currentSize);
 		DoubleDataset blueChannel = new DoubleDataset(currentSize);
 		DoubleDataset alphaChannel = new DoubleDataset(currentSize);
 		for (int i = 0; i < currentSize; i++) {
 			double value = i / (double) currentSize;
 			double redvalue = redFunc.clippedMapToDouble(value);
 
 			if (curRedInverse)
 				redvalue = 1.0 - redvalue;
 
 			redvalue *= maxValue;
 
 			double greenvalue = greenFunc.clippedMapToDouble(value);
 
 			if (curGreenInverse)
 				greenvalue = 1.0 - greenvalue;
 
 			greenvalue *= maxValue;
 
 			double bluevalue = blueFunc.clippedMapToDouble(value);
 
 			if (curBlueInverse)
 				bluevalue = 1.0 - bluevalue;
 
 			bluevalue *= maxValue;
 			
 			double alphavalue = alphaFunc.clippedMapToDouble(value);
 			
 			if (curAlphaInverse)
 				alphavalue = 1.0 - alphavalue;
 			alphavalue *= maxValue;
 			
 			redChannel.set(Math.max(redvalue, 0.0), i);
 			greenChannel.set(Math.max(greenvalue, 0.0), i);
 			blueChannel.set(Math.max(bluevalue, 0.0), i);
 			alphaChannel.set(Math.max(alphavalue,0.0),i);
 		}
 		histograms.add(0, alphaChannel);
 		histograms.add(0, blueChannel);
 		histograms.add(0, greenChannel);
 		histograms.add(0, redChannel);
 		try {
 			histogramPlotter.replaceAllPlots(histograms);
 		} catch (PlotException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void updateHistogramGraph(double min, double max) {
 		xAxis.clear();
 		currentMaxMin.max = max;
 		currentMaxMin.min = min;
 		histogramFunc.setMinMax(min,max);
 		histogramFunc.setIgnoreOutliers(true);
 		// sanity check if there is actually a histogram live
 		List<AbstractDataset> newHistogram = histogramFunc.value(data); 
 		AbstractDataset histogram = newHistogram.get(0);
 		if (histograms != null && histograms.size() > 1) {
 			histograms.set(histograms.size() - 1, histogram);
 		} else {
 			histograms = new ArrayList<AbstractDataset>();
 			histograms.add(histogram);
 		}
 		xAxis.setValues(DatasetUtils.linSpace(min, max, Math.max(1,histogram.getSize()+1), AbstractDataset.FLOAT64));
 		histogramPlotter.setXAxisValues(xAxis, 1);
 		generateHistogramUpdate();
 		updateChannelGraphs();
 		histogramPlotter.refresh(true);
 	}
 
 	private void autoRangeHistogram() {
 		histogramPlotter.clearZoomHistory();
 		double[] m;
 		if (autoContrast && data.getRank() == 2) {
 			try {
 				final int[] shape = data.getShape();
 				if (shape[0] > 512 && shape[1] > 512) {
 					int yReduce = (int) Math.ceil(shape[0] / 512.0);
 					int xReduce = (int) Math.ceil(shape[1] / 512.0);
 					Downsample sample = new Downsample(DownsampleMode.MAXIMUM, xReduce, yReduce);
 					m = Stats.quantile(sample.value(data).get(0), getPreferenceAutoContrastLo(), getPreferenceAutoContrastHi());
 				} else
 					m = Stats.quantile(data, getPreferenceAutoContrastLo(), getPreferenceAutoContrastHi());
 			} catch (Exception e) {
 				m = new double[] {data.min().doubleValue(), data.max().doubleValue()};
 			}
 		} else {
 			m = new double[] {data.min().doubleValue(), data.max().doubleValue()};
 		}
 		
 		if (Double.compare(m[1], m[0]) <= 0)
 			m[1] = m[0] + PreferenceConstants.MINIMUM_CONTRAST_DELTA/100.0;
 
 		currentMaxMin.max = m[1];
 		currentMaxMin.min = m[0];
 	}
 
 	public void createInitialHistogram() {
 		if (!hasData())
 			return;
 
 		MaxMin oldMM = currentMaxMin;
		currentMaxMin = cachedMaxMin.get(data.hashCode());
 		if (currentMaxMin == null) {
 			currentMaxMin = new MaxMin();
 			cachedMaxMin.put(data.hashCode(), currentMaxMin);
 		}
 		if (lockRange && oldMM != null) {
 			currentMaxMin.max = oldMM.max;
 			currentMaxMin.min = oldMM.min;
 		} else {
 			if (autoContrast || Double.isNaN(currentMaxMin.max) || Double.isNaN(currentMaxMin.min))
 				autoRangeHistogram();
 		}
 
 		parent.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				// Set the ranges from the data max and min
 				if (!hasData()) {
 					System.err.println("No data available in histogram view!");
 					return;
 				}
 				double max = data.max().doubleValue();
 				double min = data.min().doubleValue();
 				double step = max - min;
 				if (step != 0) {
 					int prec = (int) Math.ceil(-Math.log10(step/100));
 					int wide = (int) Math.ceil(Math.log10(Math.max(Math.abs(max), Math.abs(min))));
 					if (wide <= 0)
 						wide = 2;
 					if (prec <= 0)
 						prec = 1;
 					spnRangeStart.setFormat(prec+wide, prec);
 					spnRangeStop.setFormat(prec+wide, prec);
 				}
 				spnRangeStart.setMaximum(max);
 				spnRangeStart.setMinimum(min);
 				spnRangeStop.setMaximum(max);
 				spnRangeStop.setMinimum(min);
 
 				// Set the values from the max and min which were specified by the colour table (this persists locked values)
 				spnRangeStop.setDouble(currentMaxMin.max);
 				spnRangeStart.setDouble(currentMaxMin.min);
 
 				// Now the slider positions need to be calculated
 				double minProportion = (currentMaxMin.min - min)/step;
 				sldMinValue.setSelection((int) Math.floor((sldMinValue.getMaximum() * minProportion)));
 				double maxProportion = (currentMaxMin.max - min) / step;
 				sldMaxValue.setSelection((int) Math.floor((sldMinValue.getMaximum() * maxProportion)));
 
 				spnRangeStart.setEnabled(true);
 				spnRangeStop.setEnabled(true);
 				sldMinValue.setEnabled(true);
 				sldMaxValue.setEnabled(true);
 
 				updateHistogramGraph(currentMaxMin.min, currentMaxMin.max);
 				buildGradientImage();
 			}
 		});
 	}
 	
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if (theObserved.equals(histogramUI)) {
 			if (changeCode instanceof AreaSelectEvent) {
 				AreaSelectEvent event = (AreaSelectEvent) changeCode;
 				final double min = event.getPosition()[0];
 				final double max = event.getPosition()[1];
 				parent.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						updateHistogramGraph(min, max);
 					}
 				});
 			}
 		}
 		if (theObserved instanceof PlotWindow) {
 			if (changeCode instanceof HistogramDataUpdate) {
 				if (parent != null) {
 					data = ((HistogramDataUpdate) changeCode).getDataset();
 					createInitialHistogram();
 				}
 			}
 		}
 	}
 	
 	public void setData(HistogramDataUpdate histogramDataUpdate){
 		data = histogramDataUpdate.getDataset();
 		createInitialHistogram();
 	}
 
 	/**
 	 * @return Returns the id.
 	 */
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 		observers.add(anIObserver);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 		observers.remove(anIObserver);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		observers.clear();
 	}
 
 	@Override
 	public void dispose() {
 		// store current settings back into preferences
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		String pName = getPartName();
 		Integer setting = colourSettings.get(pName);
 		if (setting != null)
 			preferenceStore.setValue(pName+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP, setting);
 		preferenceStore.setValue(pName+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING, (useLog ? 1 : 0));
 		deleteIObservers();
 	    if (histogramPlotter != null)
 		   histogramPlotter.cleanUp();
 	    data = null;
 	    if (histogramUI != null) {
 	    	histogramUI.deactivate(false);
 	    	histogramUI.dispose();
 	    }
 		if (!cmbAlpha.isDisposed())
 			cmbAlpha.removeSelectionListener(this);
 		if (!cmbRedColour.isDisposed())
 			cmbRedColour.removeSelectionListener(this);
 		if (!cmbGreenColour.isDisposed())
 			cmbGreenColour.removeSelectionListener(this);
 		if (!cmbBlueColour.isDisposed())
 			cmbBlueColour.removeSelectionListener(this);
 		if (!btnGreenInverse.isDisposed())
 			btnGreenInverse.removeSelectionListener(this);
 		if (!btnBlueInverse.isDisposed())
 			btnBlueInverse.removeSelectionListener(this);
 		if (!btnAlphaInverse.isDisposed())
 			btnAlphaInverse.removeSelectionListener(this);
 		if (!btnRedInverse.isDisposed())
 			btnRedInverse.removeSelectionListener(this);
 		
 		super.dispose();
 	}
 
 	public void setAutoContrastScaling(boolean checked) {
 		autoContrast = checked;
 		if (hasData()) {
 			spnRangeStart.setEnabled(!checked);
 			spnRangeStop.setEnabled(!checked);
 		}
 		rangePanel.setEnabled(!checked);
 	}
 	
 	/**
 	 * Lock/Unlock the histogram range
 	 * @param isLocked true if the histogram range should be locked otherwise false
 	 */
 	public void setHistogramLock(boolean isLocked) {
 		lockRange = isLocked;
 	}
 	
 	public void setGraphLines(boolean checked) {
 		if (histogramPlotter != null) {
 			Plot1DGraphTable table = histogramPlotter.getColourTable();
 			for (int i = 0; i < 4; i++)
 				table.getLegendEntry(i).setVisible(checked);
 		}
 		histogramPlotter.updateAllAppearance();
 		histogramPlotter.refresh(false);
 	}
 	
 	public void setScaling(ScaleType newScaling) {
 		useLog = (newScaling != ScaleType.LINEAR);
 		histogramPlotter.setYAxisScaling(newScaling);
 	}
 
 	private int getPreferenceColourMapChoice() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		// try to retrieve setting from last session
 		int i = preferenceStore.getInt(getPartName()+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP); 
 		if (i != 0)
 			return i;
 		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP) ? 
 				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP)
 				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP);
 	}
 
 	private boolean getPreferenceColourMapExpertMode() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		// try to retrieve setting from last session
 		if (preferenceStore.getBoolean(getPartName()+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT))
 			return true;
 		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT) ? 
 				preferenceStore.getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT)
 				: preferenceStore.getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT);
 	}
 
 	private double getPreferenceAutoContrastLo() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		int v = preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD) ?
 				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD)
 				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD);
 		return v/100.0;
 	}
 
 	private double getPreferenceAutoContrastHi() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		int v = preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD) ?
 				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD)
 				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD);
 		return v/100.0;
 	}
 }

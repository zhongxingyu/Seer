 package org.dawnsci.rcp.histogram;
 
 import java.util.Arrays;
 import java.util.EventObject;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.histogram.HistogramBound;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ILineTrace.TraceType;
 import org.dawnsci.plotting.api.trace.IPaletteListener;
 import org.dawnsci.plotting.api.trace.IPaletteTrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.PaletteEvent;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.dawnsci.rcp.functions.ColourSchemeContribution;
 import org.dawnsci.rcp.functions.TransferFunctionContribution;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 
 public class HistogramToolPage extends AbstractToolPage {
 
 	private static final String ZINGER_LABEL = "Zinger Min value cutoff";
 
 
 	private static final String DEAD_PIXEL_LABEL = "Dead Pixel Max cutoff";
 
 
 	// LOGGER
 	private static final Logger logger = LoggerFactory.getLogger(HistogramToolPage.class);
 
 
 	// STATICS
 	private static final int SLIDER_STEPS = 1000;
 	private static final int MAX_BINS = 2048;
 	
 	// MODES
 	private static final int FULL = 0;
 	private static final int AUTO = 1;
 	private static final int FIXED = 2;
 
 	/**
 	 * Locking should be static so that the tool works in editor part mode.
 	 */
 	private IAction lockAction;
 	private int mode = FULL;
 
 
 	// HISTOGRAM 
 	private double rangeMax = 100.0;
 	private double rangeMin = 0.0;
 	private double histoMax = 50.0;
 	private double histoMin = 25.0;
 
 	private AbstractDataset imageDataset;
 
 	private AbstractDataset histogramX;
 	private AbstractDataset histogramY;
 	private int num_bins = MAX_BINS;
 
 	private boolean histogramDirty = true;
 
 	// GUI
 	private Composite composite;
 	private ScrolledComposite sc;
 	private ExpansionAdapter expansionAdapter;
 
 
 	// COLOUR SCHEME GUI 
 	private ExpandableComposite colourSchemeExpander;
 	private Composite colourSchemeComposite;
 	private CCombo cmbColourMap;	
 	private SelectionListener colourSchemeListener;
 
 
 	// PER CHANNEL SCHEME GUI
 	private ExpandableComposite perChannelExpander;
 	private Composite perChannelComposite;
 	private CCombo cmbAlpha;	
 	private CCombo cmbRedColour;
 	private CCombo cmbGreenColour;
 	private CCombo cmbBlueColour;
 	private Button btnGreenInverse;
 	private Button btnBlueInverse;
 	private Button btnAlphaInverse;
 	private Button btnRedInverse;
 
 	private SelectionListener colourSelectionListener;
 
 	// BRIGHTNESS CONTRAST GUI
 	private static final String BRIGHTNESS_LABEL = "Brightness";
 	private static final String CONTRAST_LABEL = "Contrast";
 	private ExpandableComposite bcExpander;
 	private Composite bcComposite;
 	//private SpinnerSliderSet brightnessContrastValue;
 	private SpinnerScaleSet brightnessContrastValue;
 	private SelectionListener brightnessContrastListener;
 	private SelectionListener rangeSelectionListener;
 	private KeyListener rangeKeyListener;
 
 
 	// MIN MAX GUI	
 	private static final String MAX_LABEL = "Max";
 	private static final String MIN_LABEL = "Min";
 	private ExpandableComposite rangeExpander;
 	private Composite rangeComposite;
 	//private SpinnerSliderSet minMaxValue;
 	private SpinnerScaleSet minMaxValue;
 	private SelectionListener minMaxValueListener;
 	
 	//Opal Min Max
 	private ExpandableComposite rangeOpalExpander;
 	private Composite rangeOpalComposite;
 	private HistogramRangeSlider rangeSlider;
 
 	// DEAD ZINGER GUI
 	private ExpandableComposite deadZingerExpander;
 	private Composite deadZingerComposite;
 	private SelectionListener deadZingerValueListener;
 	private Label deadPixelLabel;
 	private Text deadPixelText;
 	private Label zingerLabel;
 	private Text zingerText;
 
 	private Button resetButton;
 	private SelectionListener resetListener;
 
 
 	// HISTOGRAM PLOT
 	private ExpandableComposite histogramExpander;
 	private Composite histogramComposite;
 	private AbstractPlottingSystem histogramPlot;
 
 	private ITraceListener traceListener;
 
 	private ILineTrace histoTrace;
 	private ILineTrace redTrace;
 	private ILineTrace greenTrace;
 	private ILineTrace blueTrace;
 
 	// HELPERS
 	private ExtentionPointManager extentionPointManager;
 	private UIJob imagerepaintJob;
 	private PaletteData paletteData;
 	private int internalEvent = 0; // This is very likely to go wrong, suggest avoid
 	                               // having counters for events.
 
 
 	private IPaletteListener paletteListener;
 
 
 	private Button btnColourMapLog;
 
 
 	private SelectionListener colourSchemeLogListener;
 
 
 	private double scaleMax = 1;
 
 
 	private double scaleMin = 0;
 
 
 
 	protected boolean regionDragging = false;
 
 
 	private IROIListener histogramRegionListener;
 
 
 	/**
 	 * Basic Constructor
 	 */
 	public HistogramToolPage() {
 		super();
 		try {
 			histogramPlot = PlottingFactory.createPlottingSystem();
 
 
 		} catch (Exception ne) {
 			logger.error("Cannot locate any plotting systems!", ne);
 		}
 
 
 		// Connect to the trace listener to deal with new images coming in
 		traceListener = new ITraceListener.Stub() {
 			
 			@Override
 			public void traceWillPlot(TraceWillPlotEvent evt) {
 				if (!isActive()) return;
 				
 				// Does not all update(...) intentionally.
 				IImageTrace it = null;
 				if (evt.getSource() instanceof IImageTrace) {
 					it = (IImageTrace)evt.getSource();
 				}
 				updateImage(it, false);
 				updatePalette(it, null);
 			}
 			@Override
 			public void tracesAdded(TraceEvent evt) {
 				if (!isActive()) return;
 
 				logger.trace("tracelistener firing");
 				updateImage(null, false);
 			}
 
 		};
 
 
 		// get a palette update listener to deal with palatte updates
 		paletteListener = new IPaletteListener.Stub(){
 
 			@Override
 			public void paletteChanged(PaletteEvent event) {
 				if (internalEvent > 0) return;
 				logger.trace("paletteChanged");
 				paletteData = event.getPaletteData();
 				updateHistogramToolElements(event.getImageTrace(), null, false);
 			}
 
 			@Override
 			public void minChanged(PaletteEvent event) {
 				if (internalEvent > 0 || mode == FIXED) return;
 				logger.trace("paletteListener minChanged firing");
 				histoMin = ((IImageTrace)event.getSource()).getImageServiceBean().getMin().doubleValue();
 				updateHistogramToolElements(event.getImageTrace(), null, false);
 
 			}
 
 			@Override
 			public void maxChanged(PaletteEvent event) {
 				if (internalEvent > 0 || mode == FIXED) return;
 				logger.trace("paletteListener maxChanged firing");
 				histoMax = ((IImageTrace)event.getSource()).getImageServiceBean().getMax().doubleValue();
 				updateHistogramToolElements(event.getImageTrace(), null, false);
 			}
 
 			@Override
 			public void maxCutChanged(PaletteEvent evt) {
 				if (internalEvent > 0 || mode == FIXED) return;
 				logger.trace("paletteListener maxCutChanged firing");
 				rangeMax = ((IImageTrace)evt.getSource()).getImageServiceBean().getMaximumCutBound().getBound().doubleValue();
 				zingerText.setText(Double.toString(rangeMax));
 				if(histoMax > rangeMax) histoMax = rangeMax;
 				generateHistogram(imageDataset);
 				updateHistogramToolElements(evt.getImageTrace(), null, false);
 			}
 
 			@Override
 			public void minCutChanged(PaletteEvent evt) {
 				if (internalEvent > 0 || mode == FIXED) return;
 				logger.trace("paletteListener minCutChanged firing");
 				rangeMin = ((IImageTrace)evt.getSource()).getImageServiceBean().getMinimumCutBound().getBound().doubleValue();
 				deadPixelText.setText(Double.toString(rangeMin));
 				if(histoMin < rangeMin) histoMin = rangeMin;
 				generateHistogram(imageDataset);
 				updateHistogramToolElements(evt.getImageTrace(), null, false);
 
 			}
 
 			@Override
 			public void nanBoundsChanged(PaletteEvent evt) {
 				if (internalEvent > 0) return;
 				return;
 
 			}
 
 			@Override
 			public void maskChanged(PaletteEvent evt) {
 				// No action needed.
 			}
 
 		};
 
 
 		// Set up all the GUI element listeners
 		minMaxValueListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("minMaxValueListener");
 				histoMax = minMaxValue.getValue(MAX_LABEL);
 				histoMin = minMaxValue.getValue(MIN_LABEL);
 				if (histoMax < histoMin) {
 					histoMax = histoMin;
 				}
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 		
 		rangeSelectionListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("rangelisteer");
 				histoMax = rangeSlider.getMaxValue();
 				histoMin = rangeSlider.getMinValue();
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 		
 		rangeKeyListener = new KeyListener() {
 			
 			@Override
 			public void keyReleased(KeyEvent e) {
 				logger.trace("rangelisteer");
 				histoMax = rangeSlider.getMaxValue();
 				histoMin = rangeSlider.getMinValue();
 				updateHistogramToolElements(e);
 				
 			}
 			
 			@Override
 			public void keyPressed(KeyEvent e) {
 				logger.trace("rangelisteer");
 				histoMax = rangeSlider.getMaxValue();
 				histoMin = rangeSlider.getMinValue();
 				updateHistogramToolElements(e);
 				
 			}
 		};
 
 		brightnessContrastListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("brightnessContrastListener");
 				histoMax = brightnessContrastValue.getValue(BRIGHTNESS_LABEL)+
 						brightnessContrastValue.getValue(CONTRAST_LABEL)/2.0;
 				histoMin = brightnessContrastValue.getValue(BRIGHTNESS_LABEL)-
 						brightnessContrastValue.getValue(CONTRAST_LABEL)/2.0;
 				if (histoMax < histoMin) {
 					histoMax = histoMin;
 				}
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 
 		deadZingerValueListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("deadZingerValueListener");
 				try {
 					rangeMax = Double.parseDouble(zingerText.getText());
 					rangeMin = Double.parseDouble(deadPixelText.getText());
 					if (rangeMax < rangeMin) rangeMax = rangeMin;
 					if (histoMax > rangeMax) histoMax = rangeMax;
 					if (histoMin < rangeMin) histoMin = rangeMin;
 
 					IImageTrace image = getImageTrace();
 					if (image!=null) {
 						image.setMaxCut(new HistogramBound(rangeMax, image.getMaxCut().getColor()));		
 						image.setMinCut(new HistogramBound(rangeMin, image.getMinCut().getColor()));
 					}
 
 					// calculate the histogram
 					generateHistogram(imageDataset);
 
 					updateHistogramToolElements(event);
 
 				} catch (Exception e) {
 					// ignore this for now, might need to be a popup to the user
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		// Set up all the GUI element listeners
 		resetListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				rangeMax = Double.POSITIVE_INFINITY;
 				rangeMin = Double.NEGATIVE_INFINITY;
 
 				IImageTrace image = getImageTrace();
 				if (image!=null) {
 			        image.setMaxCut(new HistogramBound(rangeMax, image.getMaxCut().getColor()));		
 				    image.setMinCut(new HistogramBound(rangeMin, image.getMinCut().getColor()));
 				}
 
 				zingerText.setText(Double.toString(rangeMax));
 				deadPixelText.setText(Double.toString(rangeMin));
 
 				// calculate the histogram
 				generateHistogram(imageDataset);
 
 				updateHistogramToolElements(event);
 
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		colourSelectionListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("colourSelectionListener");
 				buildPaletteData();
 				updateHistogramToolElements(event);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		colourSchemeListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				logger.trace("colourSchemeListener");
 
 				updateColourScheme();
 				buildPaletteData();
 				updateHistogramToolElements(event);;
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 
 		colourSchemeLogListener = new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				IImageTrace image = getImageTrace();
 				if (image!=null) {
 					image.getImageServiceBean().setLogColorScale(btnColourMapLog.getSelection());
 					updateImage(null, true);
 				}
 
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		};
 
 		// Specify the expansion Adapter
 		expansionAdapter = new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				composite.layout();
 				sc.notifyListeners(SWT.Resize, null);
 			}
 		};
 
 
 		// Get all information from the extension points
 		extentionPointManager = ExtentionPointManager.getManager();
 
 
 		histogramRegionListener = new IROIListener.Stub() {
 
 			@Override
 			public void roiDragged(ROIEvent evt) {
 //				if (evt.getROI() instanceof RectangularROI) {
 //					regionDragging = true;
 //					IRegion region = histogramPlot.getRegion("Histogram Region");
 //					RectangularROI roi = (RectangularROI) region.getROI();
 //					histoMin = roi.getPoint()[0];
 //					histoMax = roi.getEndPoint()[0];
 //					updateRanges(null);
 //					plotHistogram();
 //					regionDragging=false;
 //				}
 			}
 
 			@Override
 			public void roiChanged(ROIEvent evt) {
 				if (evt.getROI() instanceof RectangularROI) {
 					regionDragging = true;
 					IRegion region = histogramPlot.getRegion("Histogram Region");
 					RectangularROI roi = (RectangularROI) region.getROI();
 					histoMin = roi.getPoint()[0];
 					histoMax = roi.getEndPoint()[0];
 					updateHistogramToolElements(null);
 					regionDragging=false;
 				}
 			}
 		};
 
 		// Set up the repaint job
 		imagerepaintJob = new UIJob("Colour Scale Image Update") {			
 
 			@Override
 			public IStatus runInUIThread(IProgressMonitor mon) {
 				if (!updatePalette(null, mon)) return Status.CANCEL_STATUS;
 				return Status.OK_STATUS;
 			}
 		};
 
 	}
 
 	/**
 	 * 
 	 * @param mon, may be null
 	 */
 	protected boolean updatePalette(IImageTrace eventsImage, IProgressMonitor mon) {
 		logger.trace("imagerepaintJob running");
 		internalEvent++;
 
 		IImageTrace image = eventsImage!=null ? eventsImage : getImageTrace();
 		if (image!=null) {
 			image.setMax(histoMax);
 			if (mon!=null && mon.isCanceled()) return false;
 	
 			image.setMin(histoMin);
 			if (mon!=null && mon.isCanceled()) return false;
 	
 			image.setPaletteData(paletteData);
 			logger.trace("Set palette data on image id = "+image.getName());
 			if (mon!=null && mon.isCanceled()) return false;
 	
 			internalEvent--;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void createControl(final Composite parent) {
 		// Set up the composite to hold all the information
 		sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
 		sc.setLayout(new FillLayout());
 
 		composite = new Composite(sc, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));		
 		
 		// Set up the Colour scheme part of the GUI
 		colourSchemeExpander = new ExpandableComposite(composite, SWT.NONE);
 		colourSchemeExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		colourSchemeExpander.setLayout(new GridLayout(1, false));
 		colourSchemeExpander.setText("Colour Scheme");
 
 		colourSchemeComposite = new Composite(colourSchemeExpander, SWT.NONE);
 		colourSchemeComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		colourSchemeComposite.setLayout(new GridLayout(2, false));
 
 		cmbColourMap = new CCombo(colourSchemeComposite, SWT.BORDER | SWT.READ_ONLY);
 		cmbColourMap.setToolTipText("Change the color scheme.");
 		cmbColourMap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		cmbColourMap.addSelectionListener(colourSchemeListener);
 
 		// Populate the control
 		for (ColourSchemeContribution contribution : extentionPointManager.getColourSchemeContributions()) {
 			cmbColourMap.add(contribution.getName());
 		}
 
 		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
         final String schemeName = store.getString("org.dawb.plotting.system.colourSchemeName");
 		cmbColourMap.select(Arrays.asList(cmbColourMap.getItems()).indexOf(schemeName));
 
 		btnColourMapLog = new Button(colourSchemeComposite, SWT.CHECK);
 		btnColourMapLog.setText("Log Scale");
 		btnColourMapLog.addSelectionListener(colourSchemeLogListener);
 
 		colourSchemeExpander.setClient(colourSchemeComposite);
 		colourSchemeExpander.addExpansionListener(expansionAdapter);
 		colourSchemeExpander.setExpanded(true);
 
 
 		// Set up the per channel colour scheme part of the GUI		
 		perChannelExpander = new ExpandableComposite(composite, SWT.NONE);
 		perChannelExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		perChannelExpander.setLayout(new GridLayout(1, false));
 		perChannelExpander.setText("Colour Scheme per Channel");
 
 		perChannelComposite = new Composite(perChannelExpander, SWT.NONE);
 		perChannelComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		perChannelComposite.setLayout(new GridLayout(3, false));
 		{
 			Label lblRed = new Label(perChannelComposite, SWT.NONE);
 			lblRed.setText("Red");
 			cmbRedColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbRedColour.addSelectionListener(colourSelectionListener);
 			btnRedInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnRedInverse.setText("Inverse");
 			btnRedInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblGreen = new Label(perChannelComposite, SWT.NONE);
 			lblGreen.setText("Green");
 			cmbGreenColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbGreenColour.addSelectionListener(colourSelectionListener);
 			btnGreenInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnGreenInverse.setText("Inverse");
 			btnGreenInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblBlue = new Label(perChannelComposite, SWT.NONE);
 			lblBlue.setText("Blue");
 			cmbBlueColour = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbBlueColour.addSelectionListener(colourSelectionListener);
 			btnBlueInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnBlueInverse.setText("Inverse");
 			btnBlueInverse.addSelectionListener(colourSelectionListener);
 
 			Label lblAlpha = new Label(perChannelComposite, SWT.NONE);
 			lblAlpha.setText("Alpha");
 			cmbAlpha = new CCombo(perChannelComposite, SWT.BORDER | SWT.READ_ONLY);
 			cmbAlpha.addSelectionListener(colourSelectionListener);
 			btnAlphaInverse = new Button(perChannelComposite, SWT.CHECK);
 			btnAlphaInverse.setText("Inverse");
 			btnAlphaInverse.addSelectionListener(colourSelectionListener);
 		}		
 
 		// populate the control
 		for (TransferFunctionContribution contribution : extentionPointManager.getTransferFunctionContributions()) {
 			cmbRedColour.add(contribution.getName());
 			cmbGreenColour.add(contribution.getName());
 			cmbBlueColour.add(contribution.getName());
 			cmbAlpha.add(contribution.getName());
 		}
 
 		cmbRedColour.select(0);
 		cmbGreenColour.select(0);
 		cmbBlueColour.select(0);
 		cmbAlpha.select(0);
 
 		perChannelExpander.setClient(perChannelComposite);
 		perChannelExpander.addExpansionListener(expansionAdapter);
 
 
 		// Set up the Brightness and contrast part of the GUI
 		bcExpander = new ExpandableComposite(composite, SWT.NONE);
 		bcExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		bcExpander.setLayout(new GridLayout(1, false));
 		bcExpander.setText("Brightness and Contrast");
 
 		bcComposite = new Composite(bcExpander, SWT.NONE);
 		bcComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		bcComposite.setLayout(new GridLayout(1, false));
 
 		//brightnessContrastValue = new SpinnerSliderSet(bcComposite, SLIDER_STEPS, BRIGHTNESS_LABEL, CONTRAST_LABEL);
 		brightnessContrastValue = new SpinnerScaleSet(bcComposite, SLIDER_STEPS, BRIGHTNESS_LABEL, CONTRAST_LABEL);
 		brightnessContrastValue.addSelectionListener(brightnessContrastListener);
 
 		bcExpander.setClient(bcComposite);
 		bcExpander.addExpansionListener(expansionAdapter);
 
 
 		// Set up the Min Max range part of the GUI
 		rangeExpander = new ExpandableComposite(composite, SWT.NONE);
 		rangeExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		rangeExpander.setLayout(new GridLayout(1, false));
 		rangeExpander.setText("Histogram Range");
 
 		rangeComposite = new Composite(rangeExpander, SWT.NONE);
 		rangeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		rangeComposite.setLayout(new GridLayout(1, false));
 
 		//minMaxValue = new SpinnerSliderSet(rangeComposite, SLIDER_STEPS, MAX_LABEL, MIN_LABEL);
 		minMaxValue = new SpinnerScaleSet(rangeComposite, SLIDER_STEPS, MAX_LABEL, MIN_LABEL);
 		minMaxValue.addSelectionListener(minMaxValueListener);
 
 		rangeExpander.setClient(rangeComposite);
 		rangeExpander.addExpansionListener(expansionAdapter);
 		
 		//new max/min range using opal range slider
 		// Set up the Min Max range part of the GUI
 		rangeOpalExpander = new ExpandableComposite(composite, SWT.NONE);
 		rangeOpalExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		rangeOpalExpander.setLayout(new GridLayout(1, false));
 		rangeOpalExpander.setText("Histogram Range Percentage");
 
 		rangeOpalComposite = new Composite(rangeOpalExpander, SWT.NONE);
 		rangeOpalComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		rangeOpalComposite.setLayout(new GridLayout(1, false));
 		
 		rangeOpalExpander.setClient(rangeOpalComposite);
 		rangeOpalExpander.addExpansionListener(expansionAdapter);
 		
 		rangeSlider = new HistogramRangeSlider(rangeOpalComposite, 10000);
 		rangeSlider.addSelectionListener(rangeSelectionListener);
 		rangeSlider.addKeyListener(rangeKeyListener);
 
 		// Set up the dead and zingers range part of the GUI
 		deadZingerExpander = new ExpandableComposite(composite, SWT.NONE);
 		deadZingerExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		deadZingerExpander.setLayout(new GridLayout(1, false));
 		deadZingerExpander.setText("Dead pixel and zinger cutoffs");
 
 		deadZingerComposite = new Composite(deadZingerExpander, SWT.NONE);
 		deadZingerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		deadZingerComposite.setLayout(new GridLayout(2, false));
 
 		deadPixelLabel = new Label(deadZingerComposite, SWT.NONE);
 		deadPixelLabel.setText(DEAD_PIXEL_LABEL);
 		deadPixelText = new Text(deadZingerComposite, SWT.NONE);
 		deadPixelText.addSelectionListener(deadZingerValueListener);
 
 		zingerLabel = new Label(deadZingerComposite, SWT.NONE);
 		zingerLabel.setText(ZINGER_LABEL);
 		zingerText = new Text(deadZingerComposite, SWT.NONE);
 		zingerText.addSelectionListener(deadZingerValueListener);
 
 		resetButton = new Button(deadZingerComposite, SWT.NONE);
 		resetButton.setText("Reset");
 		resetButton.addSelectionListener(resetListener);
 
 		deadZingerExpander.setClient(deadZingerComposite);
 		deadZingerExpander.addExpansionListener(expansionAdapter);
 
 
 
 		// Set up the histogram plot part of the GUI
 		histogramExpander = new ExpandableComposite(composite, SWT.NONE);
 		histogramExpander.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		histogramExpander.setLayout(new GridLayout(1, false));
 		histogramExpander.setText("Histogram Plot");
 
 		histogramComposite = new Composite(histogramExpander, SWT.NONE);
 		histogramComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		histogramComposite.setLayout(new GridLayout(1, false));
 		
 		final IPageSite site = getSite();
 
 		histogramPlot.createPlotPart( histogramComposite, 
 				getTitle(), 
 				null, 
 				PlotType.XY,
 				null);
 		histogramPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 
 		histogramExpander.setClient(histogramComposite);
 		histogramExpander.addExpansionListener(expansionAdapter);
 		histogramExpander.setExpanded(true);
 
 		createRegion();
 
 		Action reset = new Action("Reset histogram", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				
 				final IContributionItem action = getPlottingSystem().getActionBars().getToolBarManager().find("org.dawb.workbench.plotting.histo");
 			    if (action!=null && action.isVisible() && action instanceof ActionContributionItem) {
 			    	ActionContributionItem iaction = (ActionContributionItem)action;
 			    	iaction.getAction().setChecked(!iaction.getAction().isChecked());
 			    	iaction.getAction().run();
 			    }
 			}
 		};
 		
 		reset.setImageDescriptor(Activator.getImageDescriptor("icons/reset.gif"));
 		site.getActionBars().getMenuManager().add(reset);
 		getSite().getActionBars().getToolBarManager().add(reset);
 		
 		lockAction = new Action("Histogram range locked for this image", IAction.AS_CHECK_BOX) {
 			public void run() {
 				IImageTrace image = getImageTrace();
 				if (mode == FIXED) {
 					setChecked(false);
 					mode=AUTO;
 					image.setRescaleHistogram(true);
 				} else {
 					setChecked(true);
 					mode=FIXED;
 					image.setRescaleHistogram(false);
 				}
 			}
 		};
 		lockAction.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));
 		site.getActionBars().getMenuManager().add(lockAction);
 		getSite().getActionBars().getToolBarManager().add(lockAction);
 		
 		sc.setContent(composite);
 		sc.setExpandVertical(true);
 		sc.setExpandHorizontal(true);
 		sc.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 				Rectangle r = sc.getClientArea();
 				int height = composite.computeSize(r.width, SWT.DEFAULT).y;
 				if (histogramExpander.isExpanded()) {
 					height += 200;
 				}
 				sc.setMinHeight(height);
 				sc.setMinWidth(composite.computeSize(SWT.DEFAULT, r.height).x);
 			}
 		});
 		
 		// Activate this so the initial screen has content
 		activate();		
 		
 		store.addPropertyChangeListener(new IPropertyChangeListener() {			
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if ("org.dawb.plotting.system.colourSchemeName".equals(event.getProperty())) {
 					if (updatingColorSchemeInternally) return;
 					final String schemeName = (String)event.getNewValue();
 					cmbColourMap.select(Arrays.asList(cmbColourMap.getItems()).indexOf(schemeName));
 				}
 			}
 		});
 
 	}
 
 	private boolean updatingColorSchemeInternally = false;
 	/**
 	 * Use the controls from the GUI to set the individual colour elements from the selected colour scheme
 	 */
 	protected void updateColourScheme() {
 		ColourSchemeContribution colourScheme = extentionPointManager.getColourSchemeContribution(cmbColourMap.getText());
 		String red = extentionPointManager.getTransferFunctionFromID(colourScheme.getRedID()).getName();
 		String green = extentionPointManager.getTransferFunctionFromID(colourScheme.getGreenID()).getName();
 		String blue = extentionPointManager.getTransferFunctionFromID(colourScheme.getBlueID()).getName();
 		String alpha = extentionPointManager.getTransferFunctionFromID(colourScheme.getAlphaID()).getName();
 
 		setComboByName(cmbRedColour, red);
 		setComboByName(cmbGreenColour, green);
 		setComboByName(cmbBlueColour, blue);
 		setComboByName(cmbAlpha, alpha);
 
 		btnRedInverse.setSelection(colourScheme.getRedInverted());
 		btnGreenInverse.setSelection(colourScheme.getGreenInverted());
 		btnBlueInverse.setSelection(colourScheme.getBlueInverted());
 		btnAlphaInverse.setSelection(colourScheme.getAlphaInverted());
 
 		// Store as default preference
 		try {
 			updatingColorSchemeInternally=true;
 			final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		    store.setValue("org.dawb.plotting.system.colourSchemeName", colourScheme.getName());
 		} finally {
 			updatingColorSchemeInternally = false;
 		}
 	}
 
 	/**
 	 * Sets the selected item in a combo based on the name given
 	 * @param combo The combo box to set selected
 	 * @param name The name of the item to select
 	 */
 	private void setComboByName(CCombo combo, String name) {
 		for (int i = 0; i < combo.getItems().length; i++) {
 			if(combo.getItem(i).compareTo(name)== 0) {
 				combo.select(i);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Update when a new image comes in, this involves getting the data and then setting 
 	 * up all the local parameters
 	 * @param imageTrace, may be null.
 	 */
 	private void updateImage(IImageTrace imageTrace, boolean repaintImage) {
 		if (getControl()==null) return; // We cannot plot unless its been created.
 
 		IImageTrace image = imageTrace==null ? getImageTrace() : imageTrace;
 
 		if (image != null) {
 
 			// make sure that auto update is dissabled if needed
 			if (mode == FIXED) {
 				image.setRescaleHistogram(false);
 			} else {
 				image.setRescaleHistogram(true);
 			}
 
 			// get the image data
 			imageDataset = getImageData(image);
 
 			if (imageDataset.containsInvalidNumbers() ) {
 				logger.debug("imageDataset contains invalid numbers");
 			}
 
 			logger.trace("Image Data is of type :" + imageDataset.getDtype());
 			if (imageDataset.hasFloatingPointElements()) {
 				num_bins = MAX_BINS;
 			} else {
 				// set the number of points to the range
 				num_bins = (Integer) imageDataset.max(true).intValue() - imageDataset.min(true).intValue();
 				if (num_bins > MAX_BINS) num_bins = MAX_BINS;
 			}
 
 
 			switch (mode) {
 			case AUTO:
 				rangeMax = image.getImageServiceBean().getMaximumCutBound().getBound().doubleValue();
 				rangeMin = image.getImageServiceBean().getMinimumCutBound().getBound().doubleValue();
 				histoMax = image.getImageServiceBean().getMax().doubleValue();
 				histoMin = image.getImageServiceBean().getMin().doubleValue();
 				break;
 			case FIXED:
 				// Do nothing?
 				break;
 			default:
 				// this is the FULL implementation (a good default)
 				rangeMax = image.getImageServiceBean().getMaximumCutBound().getBound().doubleValue();
 				rangeMin = image.getImageServiceBean().getMinimumCutBound().getBound().doubleValue();
 				histoMax = image.getImageServiceBean().getMax().doubleValue();
 				histoMin = image.getImageServiceBean().getMin().doubleValue();
 				break;
 			}
 
 			zingerText.setText(Double.toString(image.getMaxCut().getBound().doubleValue()));
 			deadPixelText.setText(Double.toString(image.getMinCut().getBound().doubleValue()));
 
 			// Update the paletteData
 			if (paletteData==null) paletteData = image.getPaletteData();
 
 			// calculate the histogram
 			generateHistogram(imageDataset);
 
 			// update all based on slider positions
 			updateHistogramToolElements(image, null, repaintImage);
 
 			// finally tie in the listener to the paletedata changes
 			image.addPaletteListener(paletteListener);
 		}				
 	}
 
 	private AbstractDataset getImageData(IPaletteTrace image) {
 		AbstractDataset imageDataset = (AbstractDataset)image.getImageServiceBean().getImage();
 		if (image.getImageServiceBean().isLogColorScale()) {
			AbstractDataset result = Maths.subtract(imageDataset, image.getImageServiceBean().getLogOffset());
 			imageDataset = Maths.log10(result);
 		}
 		return imageDataset;
 	}
 
 	private void removeImagePaletteListener() {
 		if (getControl()==null) return; // We cannot plot unless its been created.
 
 		IImageTrace image = getImageTrace();
 		if (image != null) {
 
 			image.removePaletteListener(paletteListener);
 		}				
 	}
 
 
 	private void updateHistogramToolElements(EventObject event) {
 		updateHistogramToolElements(getImageTrace(), event, true);
 	}
 
 	/**
 	 * Update everything based on the new slider positions  
 	 * @param event  MAY BE NULL
 	 */
 	private void updateHistogramToolElements(IPaletteTrace trace, EventObject event, boolean repaintImage) {
 		// update the ranges
 		updateRanges(trace, event);
 
 		// plot the histogram
 		plotHistogram(trace);
 
 		// repaint the image if required
 		if(repaintImage) imagerepaintJob.schedule();
 	}
 
 
 	/**
 	 * This will take an image, and pull out all the parameters required to calculate the histogram
 	 * @param image the image to histogram
 	 */
 	private void generateHistogram(AbstractDataset image) {
 		// calculate the histogram for the whole image
 		double rMax = rangeMax;
 		double rMin = rangeMin;
 		if (Double.isInfinite(rMax)) rMax = imageDataset.max(true).doubleValue();
 		if (Double.isInfinite(rMin)) rMin = imageDataset.min(true).doubleValue();
 
 		Histogram hist = new Histogram(num_bins, rMin, rMax, true);
 		List<AbstractDataset> histogram_values = hist.value(image);
 		histogramX = histogram_values.get(1).getSlice(
 				new int[] {0},
 				new int[] {num_bins},
 				new int[] {1});
 		histogramX.setName("Intiesity");
 		histogramY = histogram_values.get(0);
 		histogramY = Maths.log10((Maths.add(histogramY, 1.0)));
 		histogramY.setName("Histogram");
 
 		histogramDirty = true;
 	}
 
 
 	/**
 	 * Update all the gui element ranges based on the internal values for them
 	 * @param event 
 	 */
 	private void updateRanges(IPaletteTrace image, EventObject event) {
 		
 		double scaleMaxTemp = rangeMax;
 		double scaleMinTemp = rangeMin;
 
 		if (getPlottingSystem()==null) return; // Nothing to update
 		if (image==null) return;
 		
 		imageDataset = getImageData(image);
 		
 		if (Double.isInfinite(scaleMaxTemp)) scaleMaxTemp = imageDataset.max(true).doubleValue();
 		if (Double.isInfinite(scaleMinTemp)) scaleMinTemp = imageDataset.min(true).doubleValue();
 		
 		if (mode == FIXED) {
 			if (scaleMaxTemp > scaleMax) scaleMax = scaleMaxTemp;
 			if (scaleMinTemp < scaleMin) scaleMin = scaleMinTemp;
 		} else {
 			scaleMax = scaleMaxTemp;
 			scaleMin = scaleMinTemp;
 		}
 
 		// set the minmax values
 		minMaxValue.setMin(MIN_LABEL, scaleMin);
 		minMaxValue.setMax(MIN_LABEL, scaleMax);
 		if (!minMaxValue.isSpinner(MIN_LABEL, event)) minMaxValue.setValue(MIN_LABEL, histoMin);
 
 		minMaxValue.setMin(MAX_LABEL, scaleMin);
 		minMaxValue.setMax(MAX_LABEL, scaleMax);
 		if (!minMaxValue.isSpinner(MAX_LABEL, event)) minMaxValue.setValue(MAX_LABEL, histoMax);
 
 		// Set the brightness
 		brightnessContrastValue.setMin(BRIGHTNESS_LABEL, scaleMin);
 		brightnessContrastValue.setMax(BRIGHTNESS_LABEL, scaleMax);
 		if (!brightnessContrastValue.isSpinner(BRIGHTNESS_LABEL, event)) brightnessContrastValue.setValue(BRIGHTNESS_LABEL, (histoMax+histoMin)/2.0);
 
 		// Set the contrast
 		brightnessContrastValue.setMin(CONTRAST_LABEL, 0.0);
 		brightnessContrastValue.setMax(CONTRAST_LABEL, scaleMax-scaleMin);
 		if (!brightnessContrastValue.isSpinner(CONTRAST_LABEL, event)) brightnessContrastValue.setValue(CONTRAST_LABEL, histoMax-histoMin);
 		
 		if (!rangeSlider.isEventSource(event)) {
 			double tempMin = scaleMin;
 			double tempMax = scaleMax;
 			if (histoMin < scaleMin) tempMin = histoMin;
 			if (histoMax > scaleMax) tempMax = histoMax;
 			rangeSlider.setRangeLimits(tempMin, tempMax);
 			rangeSlider.setSliderValues(histoMin, histoMax);
 		}
 	}
 
 
 	/**
 	 * Plots the histogram, and RGB lines
 	 */
 	private void plotHistogram(IPaletteTrace image) {	
 
 
 		// Initialise the histogram Plot if required
 
 		if (histoTrace == null) {
 
 			histogramPlot.clear();
 
 			// Set up the histogram trace
 			histoTrace = histogramPlot.createLineTrace("Histogram");
 			histoTrace.setTraceType(TraceType.AREA);
 			histoTrace.setLineWidth(1);
 			histoTrace.setTraceColor(new Color(null, 0, 0, 0));
 
 			// Set up the RGB traces
 			redTrace = histogramPlot.createLineTrace("Red");
 			greenTrace = histogramPlot.createLineTrace("Green");
 			blueTrace = histogramPlot.createLineTrace("Blue");
 
 			redTrace.setLineWidth(2);
 			greenTrace.setLineWidth(2);
 			blueTrace.setLineWidth(2);
 
 			redTrace.setTraceColor(new Color(null, 255, 0, 0));
 			greenTrace.setTraceColor(new Color(null, 0, 255, 0));
 			blueTrace.setTraceColor(new Color(null, 0, 0, 255));
 
 			// Finally add everything in a threadsafe way.
 			getControl().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					histogramPlot.addTrace(histoTrace);
 					histogramPlot.addTrace(redTrace);
 					histogramPlot.addTrace(greenTrace);
 					histogramPlot.addTrace(blueTrace);
 				};
 			});
 		}
 
 		// now build the RGB Lines  ( All the -3's here are to avoid the min/max/NAN colours)
 		if (image==null) return;
 		PaletteData paletteData = image.getPaletteData();
 		final DoubleDataset R = new DoubleDataset(paletteData.colors.length-3);
 		final DoubleDataset G = new DoubleDataset(paletteData.colors.length-3);
 		final DoubleDataset B = new DoubleDataset(paletteData.colors.length-3);
 		final DoubleDataset RGBX = new DoubleDataset(paletteData.colors.length-3);
 		R.setName("red");
 		G.setName("green");
 		B.setName("blue");
 		RGBX.setName("Axis");
 		double scale = ((histogramY.max(true).doubleValue())/256.0);
 		if(scale <= 0) scale = 1.0/256.0;
 
 		//palleteData.colors = new RGB[256];
 		for (int i = 0; i < paletteData.colors.length-3; i++) {
 			R.set(paletteData.colors[i].red*scale, i);
 			G.set(paletteData.colors[i].green*scale, i);
 			B.set(paletteData.colors[i].blue*scale, i);
 			RGBX.set(histoMin+(i*((histoMax-histoMin)/paletteData.colors.length)), i);
 		}
 
 		// Now update all the trace data in a thread-safe way
 		final double finalScale = scale;
 
 		getControl().getDisplay().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				if (histogramDirty) {
 					histoTrace.setData(histogramX, histogramY);
 					histogramDirty = false;
 				}
 				if(!regionDragging ) {
 					createRegion();
 				}
 				redTrace.setData(RGBX, R);
 				greenTrace.setData(RGBX, G);
 				blueTrace.setData(RGBX, B);
 				histogramPlot.getSelectedXAxis().setRange(scaleMin, scaleMax);
 				histogramPlot.getSelectedYAxis().setRange(0, finalScale*256);
 				histogramPlot.repaint();
 			}
 		});
 	}
 
 
 	/**
 	 * Add the trace listener and plot initial data
 	 */
 	public void activate() {
 		super.activate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 			
 			updateImage(null, true); 
 		}
 	}
 
 	/**
 	 * remove the trace listener to avoid unneeded event triggering
 	 */
 	public void deactivate() {
 		super.deactivate();
 
 		if (getPlottingSystem()!=null) {
 			removeImagePaletteListener();
 			getPlottingSystem().removeTraceListener(traceListener);
 		}
 	}
 
 
 	@Override
 	public Control getControl() {
 		return sc;
 	}
 
 	@Override
 	public void setFocus() {
 		if (composite!=null && !composite.isDisposed()) composite.setFocus();
 	}
 
 	/**
 	 * Build a palette data from the RGB values which have been set in the GUI
 	 */
 	private void buildPaletteData() {
 
 		// first get the appropriate bits from the extension points
 		int[] red = extentionPointManager.getTransferFunction(cmbRedColour.getText()).getFunction().getArray();
 		int[] green = extentionPointManager.getTransferFunction(cmbGreenColour.getText()).getFunction().getArray();
 		int[] blue = extentionPointManager.getTransferFunction(cmbBlueColour.getText()).getFunction().getArray();
 
 		if (btnRedInverse.getSelection()) {
 			red = invert(red);
 		}
 		if (btnGreenInverse.getSelection()) {
 			green = invert(green);
 		}
 		if (btnBlueInverse.getSelection()) {
 			blue = invert(blue);
 		}
 
 		paletteData.colors = new RGB[256];
 
 		for (int i = 0; i < 256; i++) {
 			paletteData.colors[i] = new RGB(red[i], green[i], blue[i]);
 		}
 	}
 
 	private int[] invert(int[] array) {
 		int[] result = new int[array.length];
 		for(int i = 0; i < array.length; i++) {
 			result[i] = array[array.length-1-i];
 		}
 		return result;
 	}
 
 
 	private void createRegion(){
 		try {
 			IRegion region = histogramPlot.getRegion("Histogram Region");
 			//Test if the region is already there and update the currentRegion
 			if (region == null || !region.isVisible()) {
 				region = histogramPlot.createRegion("Histogram Region", RegionType.XAXIS);
 				histogramPlot.addRegion(region);
 			}
 
 			RectangularROI rroi = new RectangularROI(histoMin, 0, histoMax-histoMin, 1, 0);
 			region.setROI(rroi);
 			region.addROIListener(histogramRegionListener);
 		} catch (Exception e) {
 			logger.error("Couldn't open histogram view and create ROI", e);
 		}
 	}
 	
 	public boolean isStaticTool() {
 		return true;
 	}
 
 }

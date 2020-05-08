 package org.dawb.workbench.plotting.system.dialog;
 
 import java.util.Arrays;
 
 import org.dawb.common.services.ImageServiceBean.HistoType;
 import org.dawb.common.services.HistogramBound;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
 import org.dawb.workbench.plotting.Activator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * TODO Replace with alternate widget library.
 */
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 
 public class ImageTraceComposite extends Composite {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTraceComposite.class);
 
 	private IImageTrace   imageTrace;
 	private NumberBox     maximum, minimum, minCut, maxCut;
 	private CCombo        downsampleChoice, histoChoice;
 	private Text          nameText;
     private ColorSelector minCutColor, maxCutColor, nanColor;
 	/**
 	 * 
 	 * @param dialog
 	 * @param plottingSystem - may be null!
 	 * @param imageTrace
 	 */
 	public ImageTraceComposite(final Composite       parent,
 			                   final Dialog          dialog, 
 			                   final IPlottingSystem plottingSystem, 
 			                   final IImageTrace     imageTrace) {
 		
 		super(parent, SWT.NONE);
 		this.imageTrace  = imageTrace;
 
 		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		setLayout(new GridLayout(1, false));
 		
 		Label label;
 		
 		final Composite top = new Composite(this, SWT.NONE);
 		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		top.setLayout(new GridLayout(2, false));
 		
 		label = new Label(top, SWT.NONE);
 		label.setText("Name");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		nameText = new Text(top, SWT.BORDER | SWT.SINGLE);
 		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));		
         nameText.setText(imageTrace.getName());
 		
 		final Group group = new Group(this, SWT.NONE);
 		group.setText("Histogramming");
 		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		group.setLayout(new GridLayout(2, false));
 		
 		boolean isInt = imageTrace.getData().getDtype()==AbstractDataset.INT16 ||
 		                imageTrace.getData().getDtype()==AbstractDataset.INT32 ||
 				        imageTrace.getData().getDtype()==AbstractDataset.INT64;
 
 		label = new Label(group, SWT.NONE);
 		label.setText("Minimum Intensity");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.minimum = new ScaleBox(group, SWT.NONE);
 		minimum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		if (imageTrace.getMin()!=null) minimum.setValue(imageTrace.getMin().doubleValue());
 		minimum.setActive(true);
 		minimum.setIntegerBox(isInt);
 
 		label = new Label(group, SWT.NONE);
 		label.setText("Maximum Intensity");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.maximum = new ScaleBox(group, SWT.NONE);
 		maximum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		if (imageTrace.getMax()!=null) maximum.setValue(imageTrace.getMax().doubleValue());
 		maximum.setActive(true);
         maximum.setIntegerBox(isInt);
       		
 		maximum.setMaximum(imageTrace.getData().max().doubleValue());
 		maximum.setMinimum(minimum);
 	    minimum.setMaximum(maximum);
 		minimum.setMinimum(imageTrace.getData().min().doubleValue());
 	
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Downsampling Type");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.downsampleChoice = new CCombo(group, SWT.READ_ONLY);
 		downsampleChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		for (DownsampleType ds : DownsampleType.values()) {
 			downsampleChoice.add(ds.getLabel());
 		}
 		downsampleChoice.setToolTipText("The algorithm used when downsampling the full image for display.");
 		downsampleChoice.select(imageTrace.getDownsampleType().getIndex());
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Histogram Type");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.histoChoice = new CCombo(group, SWT.READ_ONLY);
 		histoChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		for (HistoType ht : HistoType.values()) {
             histoChoice.add(ht.getLabel());
 		}
 		histoChoice.setToolTipText("The algorithm used when histogramming the downsampled image.\nNOTE: median is much slower. If you change this, max and min will be recalculated.");
 		histoChoice.select(imageTrace.getHistoType().getIndex());
 		histoChoice.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
 				if (imageTrace.getHistoType()!=type) {
 					imageTrace.setHistoType(type);
 					maximum.setNumericValue(imageTrace.getMax().doubleValue());
 					minimum.setNumericValue(imageTrace.getMin().doubleValue());
 				}
 			}
 		});
 		
 		if (plottingSystem!=null) {
 			label = new Label(group, SWT.NONE);
 			label.setText("Open histogram tool");
 			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 	
 			final Button openHisto = new Button(group, SWT.NONE);
 			openHisto.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 			openHisto.setImage(Activator.getImage("icons/brightness_contrast.gif"));
 			openHisto.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					dialog.close();
 					try {
 						((AbstractPlottingSystem)plottingSystem).setToolVisible("org.dawnsci.rcp.histogram.histogram_tool_page", 
 								                                                ToolPageRole.ROLE_2D, 
 								                                                "org.dawb.workbench.plotting.views.toolPageView.2D");
 					} catch (Exception e1) {
 						logger.error("Cannot show histogram tool programatically!", e1);
 					}
 					
 				}
 			});
 		}
 		
 		final Group cuts = new Group(this, SWT.NONE);
 		cuts.setText("Invalid Bounds");
 		cuts.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		cuts.setLayout(new GridLayout(3, false));
 
 		label = new Label(cuts, SWT.NONE);
 		label.setText("Lower cut");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.minCut = new ScaleBox(cuts, SWT.NONE);
 		minCut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		if (imageTrace.getMinCut()!=null) minCut.setNumericValue(imageTrace.getMinCut().getBound().doubleValue());
 		minCut.setActive(true);
 		minCut.setIntegerBox(isInt);
 		minCut.setMaximum(minimum);
 		
 		minCutColor = new ColorSelector(cuts);
 		minCutColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getMinCut()!=null) minCutColor.setColorValue(imageTrace.getMinCut().getColor());
 
 		label = new Label(cuts, SWT.NONE);
 		label.setText("Upper cut");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.maxCut = new ScaleBox(cuts, SWT.NONE);
 		maxCut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		if (imageTrace.getMaxCut()!=null) {
 			
 			final double max = imageTrace.getMaxCut().getBound().doubleValue();
 			if(max==Double.POSITIVE_INFINITY){
 			    ((StyledText)maxCut.getControl()).setText("Infinity");
 			} else {
 			    maxCut.setNumericValue(max);
 			}
 		}
 		maxCut.setActive(true);
 		maxCut.setIntegerBox(isInt);
 		maxCut.setMinimum(maximum);
 		
 		maxCutColor = new ColorSelector(cuts);
 		maxCutColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getMaxCut()!=null) maxCutColor.setColorValue(imageTrace.getMaxCut().getColor());
 	
 		label = new Label(cuts, SWT.NONE);
 		label.setText("Invalid number color");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 
 		nanColor = new ColorSelector(cuts);
 		nanColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getNanBound()!=null) nanColor.setColorValue(imageTrace.getNanBound().getColor());
 		
 		final Button reset = new Button(cuts, SWT.NONE);
 		reset.setLayoutData(new GridData());		
 		reset.setImage(Activator.getImage("icons/reset.gif"));
 		reset.setText("Reset");
 		reset.setToolTipText("Reset cut bounds");
 		reset.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				imageTrace.setMinCut(HistogramBound.DEFAULT_MINIMUM);
 				imageTrace.setMaxCut(HistogramBound.DEFAULT_MAXIMUM);
 				imageTrace.setNanBound(HistogramBound.DEFAULT_NAN);
 				minCut.setNumericValue(Double.NEGATIVE_INFINITY);
 				minCutColor.setColorValue(HistogramBound.DEFAULT_MINIMUM.getColor());
 				maxCut.setNumericValue(Double.POSITIVE_INFINITY);
 				maxCutColor.setColorValue(HistogramBound.DEFAULT_MAXIMUM.getColor());
 				((StyledText)maxCut.getControl()).setText("Infinity");
 				nanColor.setColorValue(HistogramBound.DEFAULT_NAN.getColor());
 			}
 		});
 		
 		final Group info = new Group(this, SWT.NONE);
 		info.setText("Current downsample");
 		info.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		info.setLayout(new GridLayout(2, false));
 
 		label = new Label(info, SWT.NONE);
 		label.setText("Bin size");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		Label value = new Label(info, SWT.NONE);
 		value.setText(imageTrace.getDownsampleBin()+"x"+imageTrace.getDownsampleBin()+" pixels");
 		value.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 
 		label = new Label(info, SWT.NONE);
 		label.setText("Shape");
 		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		value = new Label(info, SWT.NONE);
 		value.setText(Arrays.toString(imageTrace.getDownsampled().getShape()));
 		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		minimum.checkBounds();
 		maximum.checkBounds();
 		minCut.checkBounds();
 		maxCut.checkBounds();
 		minimum.getControl().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
 	}
 
 	public void applyChanges() {
 
 		try {
 			imageTrace.setImageUpdateActive(false);
 			imageTrace.setHistoType(HistoType.values()[histoChoice.getSelectionIndex()]); // Do first because overrides max and min
 			imageTrace.setName(nameText.getText());
 			if (!Double.isNaN(minimum.getNumericValue())) imageTrace.setMin(minimum.getNumericValue());
 			if (!Double.isNaN(maximum.getNumericValue())) imageTrace.setMax(maximum.getNumericValue());
 			
 			final double min = !Double.isNaN(minCut.getNumericValue()) ? minCut.getNumericValue() : imageTrace.getMinCut().getBound().doubleValue();
 			imageTrace.setMinCut(new HistogramBound(min, minCutColor.getColorValue()));
 			
 			final double max = !Double.isNaN(maxCut.getNumericValue()) ? maxCut.getNumericValue() : imageTrace.getMaxCut().getBound().doubleValue();
 			imageTrace.setMaxCut(new HistogramBound(max, maxCutColor.getColorValue()));
 			
 			imageTrace.setNanBound(new HistogramBound(Double.NaN, nanColor.getColorValue()));
 			imageTrace.setDownsampleType(DownsampleType.values()[downsampleChoice.getSelectionIndex()]);
 		} finally {
 			imageTrace.setImageUpdateActive(true);
 		}
 	}
 
 }

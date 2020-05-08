 package org.dawnsci.plotting.system.dialog;
 
 import java.util.Arrays;
 
 import org.dawb.common.services.HistogramBound;
 import org.dawb.common.services.ImageServiceBean.HistoType;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
 import org.dawb.common.ui.util.ColorUtility;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.draw2d.swtxy.BasePlottingConstants;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.components.scalebox.RangeBox;
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 import uk.ac.gda.richbeans.event.ValueAdapter;
 import uk.ac.gda.richbeans.event.ValueEvent;
 /**
  * TODO Replace with alternate widget library.
  */
 
 public class ImageTraceComposite extends Composite {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTraceComposite.class);
 
 	private IImageTrace   imageTrace;
 	private NumberBox     maximum, minimum, minCut, maxCut, lo, hi;
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
 		
 		Label histolabel = new Label(group, SWT.NONE);
 		histolabel.setText("Histogram Type");
 		histolabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		this.histoChoice = new CCombo(group, SWT.READ_ONLY);
 		histoChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		for (HistoType ht : HistoType.values()) {
             histoChoice.add(ht.getLabel());
 		}
 		histoChoice.setToolTipText("The algorithm used when histogramming the downsampled image.\nNOTE: median is much slower. If you change this, max and min will be recalculated.");
 		histoChoice.select(imageTrace.getHistoType().getIndex());
 		
 		final Composite outlierComp = new Composite(group, SWT.NONE);
 		outlierComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		outlierComp.setLayout(new GridLayout(2, false));
 
 		histoChoice.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
 				if (imageTrace.getHistoType()!=type) {
 					imageTrace.setHistoType(type);
 					maximum.setNumericValue(imageTrace.getMax().doubleValue());
 					minimum.setNumericValue(imageTrace.getMin().doubleValue());
 				}
 				GridUtils.setVisible(outlierComp, type==HistoType.OUTLIER_VALUES);
 				outlierComp.getParent().layout(new Control[]{outlierComp});
 				layout();
 				getParent().layout(new Control[]{ImageTraceComposite.this});
 				getShell().layout();
 			}
 		});
 
 		if (imageTrace.getData() instanceof RGBDataset) {
 			histoChoice.setEnabled(false);
 		}
 		
 		label = new Label(outlierComp, SWT.NONE);
 		label.setText("Outlier low");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
 		
 		this.lo = new RangeBox(outlierComp, SWT.NONE);
 		lo.setButtonVisible(false);
 		lo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 		if (imageTrace.getImageServiceBean()!=null) lo.setValue(imageTrace.getImageServiceBean().getLo());
 		lo.setActive(true);
 		lo.on();
 		lo.setDecimalPlaces(3);
 		lo.setUnit("%");
 		lo.addValueListener(new ValueAdapter() {		
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				if (!lo.isValidBounds()) return;
 				final double orig = imageTrace.getImageServiceBean().getLo();
 				try {
 					HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
 					imageTrace.getImageServiceBean().setLo(lo.getNumericValue());
 					boolean ok = imageTrace.setHistoType(type);
 					if (!ok) throw new Exception("Histo not working!");
 					getPreferenceStore().setValue(BasePlottingConstants.HISTO_LO, lo.getNumericValue());
 					maximum.setNumericValue(imageTrace.getMax().doubleValue());
 					minimum.setNumericValue(imageTrace.getMin().doubleValue());
 				} catch (Throwable ne) {
 					imageTrace.getImageServiceBean().setLo(orig);
 				}
 			}
 		});
 
 		label = new Label(outlierComp, SWT.NONE);
 		label.setText("Outlier high");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
 		
 		this.hi = new RangeBox(outlierComp, SWT.NONE);
 		hi.setButtonVisible(false);
 		hi.setUnit("%");
 		hi.setDecimalPlaces(3);
 		hi.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 		if (imageTrace.getImageServiceBean()!=null) hi.setValue(imageTrace.getImageServiceBean().getHi());
 		hi.setActive(true);
 		hi.on();
 		hi.addValueListener(new ValueAdapter() {		
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				if (!hi.isValidBounds()) return;
 				final double orig = imageTrace.getImageServiceBean().getHi();
 				try {
 					HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
 					imageTrace.getImageServiceBean().setHi(hi.getNumericValue());
 					boolean ok = imageTrace.setHistoType(type);
 					if (!ok) throw new Exception("Histo not working!");
 					getPreferenceStore().setValue(BasePlottingConstants.HISTO_HI, hi.getNumericValue());
 					maximum.setNumericValue(imageTrace.getMax().doubleValue());
 					minimum.setNumericValue(imageTrace.getMin().doubleValue());
 				} catch (Throwable ne) {
 					imageTrace.getImageServiceBean().setHi(orig);
 				}
 			}
 		});
       		
 		hi.setMaximum(99.999);
 		hi.setMinimum(lo);
 		lo.setMaximum(hi);
 		lo.setMinimum(0.001);
 		
 		if (imageTrace.getData() instanceof RGBDataset) {
 			histolabel.setEnabled(false);
 			histoChoice.setEnabled(false);
 			hi.setEnabled(false);
 			lo.setEnabled(false);
 		}
 
 		GridUtils.setVisible(outlierComp, imageTrace.getHistoType()==HistoType.OUTLIER_VALUES);
 		
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
 		minCut.setMinimum(Double.NEGATIVE_INFINITY);
 		
 		minCutColor = new ColorSelector(cuts);
 		minCutColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getMinCut()!=null) minCutColor.setColorValue(ColorUtility.getRGB(imageTrace.getMinCut().getColor()));
 
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
		maxCut.setMaximum(Double.POSITIVE_INFINITY);
 		
 		maxCutColor = new ColorSelector(cuts);
 		maxCutColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getMaxCut()!=null) maxCutColor.setColorValue(ColorUtility.getRGB(imageTrace.getMaxCut().getColor()));
 	
 		label = new Label(cuts, SWT.NONE);
 		label.setText("Invalid number color");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 
 		nanColor = new ColorSelector(cuts);
 		nanColor.getButton().setLayoutData(new GridData());		
 		if (imageTrace.getNanBound()!=null) nanColor.setColorValue(ColorUtility.getRGB(imageTrace.getNanBound().getColor()));
 		
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
 				minCutColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_MINIMUM.getColor()));
 				maxCut.setNumericValue(Double.POSITIVE_INFINITY);
 				maxCutColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_MAXIMUM.getColor()));
 				((StyledText)maxCut.getControl()).setText("Infinity");
 				nanColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_NAN.getColor()));
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
 	
 	private IPreferenceStore store;
 	private IPreferenceStore getPreferenceStore() {
 		if (store!=null) return store;
 		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		return store;
 	}
 
 
 	public void applyChanges() {
 
 		try {
 			imageTrace.setImageUpdateActive(false);
 			imageTrace.setHistoType(HistoType.values()[histoChoice.getSelectionIndex()]); // Do first because overrides max and min
 			imageTrace.setName(nameText.getText());
 			if (!Double.isNaN(minimum.getNumericValue())) imageTrace.setMin(minimum.getNumericValue());
 			if (!Double.isNaN(maximum.getNumericValue())) imageTrace.setMax(maximum.getNumericValue());
 			
 			final double min = !Double.isNaN(minCut.getNumericValue()) ? minCut.getNumericValue() : imageTrace.getMinCut().getBound().doubleValue();
 			imageTrace.setMinCut(new HistogramBound(min, ColorUtility.getIntArray(minCutColor.getColorValue())));
 			
 			final double max = !Double.isNaN(maxCut.getNumericValue()) ? maxCut.getNumericValue() : imageTrace.getMaxCut().getBound().doubleValue();
 			imageTrace.setMaxCut(new HistogramBound(max, ColorUtility.getIntArray(maxCutColor.getColorValue())));
 			
 			imageTrace.setNanBound(new HistogramBound(Double.NaN, ColorUtility.getIntArray(nanColor.getColorValue())));
 			imageTrace.setDownsampleType(DownsampleType.values()[downsampleChoice.getSelectionIndex()]);
 		} finally {
 			imageTrace.setImageUpdateActive(true);
 		}
 	}
 
 }

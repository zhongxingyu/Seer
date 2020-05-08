 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.util.Arrays;
 
 import org.dawb.common.services.ImageServiceBean.HistoType;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
 import org.dawb.workbench.plotting.Activator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 
 public class ImageTraceConfigPage {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTraceConfigPage.class);
 
 	private IPlottingSystem plottingSystem;
 	private IImageTrace   imageTrace;
 	private Composite     composite;
 	private NumberBox     maximum, minimum;
 	private CCombo        downsampleChoice, histoChoice;
 	private Text          nameText;
 	private Dialog        dialog;
 
 	/**
 	 * 
 	 * @param dialog
 	 * @param plottingSystem - may be null!
 	 * @param imageTrace
 	 */
 	public ImageTraceConfigPage(Dialog          dialog, 
 			                    IPlottingSystem plottingSystem, 
 			                    IImageTrace     imageTrace) {
 		this.plottingSystem = plottingSystem;
 		this.imageTrace  = imageTrace;
 		this.dialog      = dialog;
 	}
 
 	public void createPage(final Composite composite) {
 
 
 		this.composite = composite;
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		composite.setLayout(new GridLayout(1, false));
 		
 		Label label;
 		
 		final Composite top = new Composite(composite, SWT.NONE);
 		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		top.setLayout(new GridLayout(2, false));
 		
 		label = new Label(top, SWT.NONE);
 		label.setText("Name");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		
 		nameText = new Text(top, SWT.BORDER | SWT.SINGLE);
 		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));		
         nameText.setText(imageTrace.getName());
 		
 		final Group group = new Group(composite, SWT.NONE);
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
 		
 		final Group info = new Group(composite, SWT.NONE);
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
 
 	}
 
 	public Composite getComposite() {
 		return composite;
 	}
 
 	public void applyChanges() {
 
 		try {
 			imageTrace.setImageUpdateActive(false);
 			imageTrace.setHistoType(HistoType.values()[histoChoice.getSelectionIndex()]); // Do first because overrides max and min
 			imageTrace.setName(nameText.getText());
 			imageTrace.setMin(minimum.getNumericValue());
 			imageTrace.setMax(maximum.getNumericValue());
 			imageTrace.setDownsampleType(DownsampleType.values()[downsampleChoice.getSelectionIndex()]);
 		} finally {
 			imageTrace.setImageUpdateActive(true);
 		}
 	}
 
 }

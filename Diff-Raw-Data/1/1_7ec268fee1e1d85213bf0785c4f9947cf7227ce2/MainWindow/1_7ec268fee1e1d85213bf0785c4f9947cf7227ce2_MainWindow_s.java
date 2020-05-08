 package com.cateye.ui.swt;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import com.cateye.core.IPropertyChangedListener;
 import com.cateye.core.stage.IStage;
 import com.cateye.core.stage.StageFactory;
 import com.cateye.stageoperations.compressor.CompressorStageOperation;
 import com.cateye.stageoperations.compressor.ui.swt.CompressorStageOperationWidget;
 import com.cateye.stageoperations.hsb.HSBStageOperation;
 import com.cateye.stageoperations.hsb.ui.swt.HSBStageOperationWidget;
 import com.cateye.stageoperations.limiter.LimiterStageOperation;
 import com.cateye.stageoperations.rgb.RGBStageOperation;
 import com.cateye.stageoperations.rgb.ui.swt.RGBStageOperationWidget;
 import com.google.inject.Inject;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 
 public class MainWindow
 {
 	protected Shell shell;
 	protected Button button;
 	private final StageFactory stageFactory;
 	private Timer updateTimer;
 	
 	private PreciseBitmapsVernissage vern = new PreciseBitmapsVernissage(2, 1);
 	private PreciseImageViewer imageViewer;
 	private IStage stage;
 	
 	private RGBStageOperation rgbStageOperation;
 	private HSBStageOperation hsbStageOperation;
 	private CompressorStageOperation compressorStageOperation;
 
 	private RGBStageOperationWidget rgbStageOperationWidget;
 	private HSBStageOperationWidget hsbStageOperationWidget;
 	private CompressorStageOperationWidget compressorStageOperationWidget;
 	
 	IPropertyChangedListener stageOperationPropertyChanged = new IPropertyChangedListener() 
 	{
 		
 		@Override
 		public void invoke(Object sender, String propertyName, Object newValue) 
 		{
 			if (updateTimer != null)
 			{
 				updateTimer.cancel();
 			}
 			updateTimer = new Timer(); 
 			updateTimer.schedule(new TimerTask() {
 				
 				@Override
 				public void run() {
 					shell.getDisplay().asyncExec(new Runnable() {
 						
 						@Override
 						public void run() {
 							stage.processImage();
 							vern.setBitmap(1, 0, stage.getBitmap());
 							imageViewer.redraw();
 						}
 					});
 				}
 			}, 50);
 			
 			//imageViewer.redraw();
 		}
 	};
 	
 	@Inject
 	public MainWindow(StageFactory stageFactory)
 	{
 		this.stageFactory = stageFactory;
 	}
 
 	/**
 	 * Open the window.
 	 * @wbp.parser.entryPoint
 	 */
 	public void open()
 	{
 		stage = createStage();
 
 		Display display = Display.getDefault();
 		createContents();
 		
 		shell.open();
 		shell.layout();
 		
 		while (!shell.isDisposed())
 		{
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 	}
 	
 	private IStage createStage()
 	{
 		hsbStageOperation = new HSBStageOperation();
 		hsbStageOperation.setSaturation(0.9);
 		hsbStageOperation.setHue(0);
 		hsbStageOperation.setBrightness(8);
 		
 		rgbStageOperation = new RGBStageOperation();
 		rgbStageOperation.setB(2);
 		rgbStageOperation.setG(1.5);
 		
 		compressorStageOperation = new CompressorStageOperation();
 		
 		LimiterStageOperation limiterStageOperation = new LimiterStageOperation();
 		limiterStageOperation.setPower(5);
 		
 		IStage stage = stageFactory.create();
 		stage.addStageOperation(hsbStageOperation);
 		stage.addStageOperation(rgbStageOperation);
 		stage.addStageOperation(compressorStageOperation);
 		stage.addStageOperation(limiterStageOperation);
 		
 		stage.loadImage("..//..//data//test//IMG_1520.CR2");
 		stage.processImage();
 		
 		return stage;
 	}
 
 	
 	private void createImageViewer(IStage stage, Composite parent)
 	{
 		imageViewer = new PreciseImageViewer(parent);
 
 		vern.setUpdated(0, 0, true);
 		vern.setBitmap(0, 0, stage.getOriginalBitmap());
 		
 		vern.setUpdated(1, 0, true);
 		vern.setBitmap(1, 0, stage.getBitmap());
 		
 		vern.setCaption(1, 0, "This is the caption for (1, 0) picture"); 
 		
 		imageViewer.setVernissage(vern);
 		imageViewer.setLayout(new FillLayout(SWT.HORIZONTAL));
 	}
 	
 	/**	 * Create contents of the window.
 	 */
 	protected void createContents()
 	{
 		shell = new Shell();
 		shell.setSize(450, 300);
 		shell.setText("CatEyeB");
 		
 		GridLayout mainLayout = new GridLayout(2, false);
 		shell.setLayout(mainLayout);
 		mainLayout.horizontalSpacing = 5;
 		
 		Composite centerComposite = new Composite(shell, SWT.NONE);
 		centerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 		centerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Composite rightComposite = new Composite(shell, SWT.NONE);
 		rightComposite.setLayout(new GridLayout(1, false));
 		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 		
 		createImageViewer(stage, centerComposite);
 
 		// Stage operation widgets
 		
 		rgbStageOperationWidget = new RGBStageOperationWidget(rightComposite, SWT.NONE);
 		rgbStageOperationWidget.setRgbStageOperation(rgbStageOperation);
 		rgbStageOperation.addOnPropertyChangedListener(stageOperationPropertyChanged);
 		
 		hsbStageOperationWidget = new HSBStageOperationWidget(rightComposite, SWT.NONE);
 		hsbStageOperationWidget.setHsbStageOperation(hsbStageOperation);
 		hsbStageOperation.addOnPropertyChangedListener(stageOperationPropertyChanged);
 	}
 	
 	
 }

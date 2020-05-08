 package de.ptb.epics.eve.viewer.views;
 
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 import de.ptb.epics.eve.data.PlotModes;
 import de.ptb.epics.eve.data.scandescription.PlotWindow;
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 import de.ptb.epics.eve.ecp1.intern.DataModifier;
 import de.ptb.epics.eve.ecp1.intern.DataType;
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.math.XYPlot;
 
 /**
  * <code>plotGraphComposite</code> contains the xy-plot and is located in the 
  * Plot View.
  * 
  * @author Jens Eden
  * @author Marcus Michalsky
  */
 public class PlotGraphComposite extends Composite 
 							implements IMeasurementDataListener {
 
 	private String detector1Id;
 	private String detector2Id;
 	private String motorId;
 	private int detector1PosCount;
 	private int detector2PosCount;
 	private int motorPosCount;
 	private Double xValue;
 	private int chid;
 	private int smid;
 	private Double y2value;
 	private Double y1value;
 	private Canvas canvas;
 	private XYPlot xyPlot;
 	private String detector1Name;
 	private String detector2Name;
 	
 	/**
 	 * Constructs a <code>plotGraphComposite</code>.
 	 * 
 	 * @param parent the parent
 	 * @param style the style
 	 */
 	public PlotGraphComposite(Composite parent, int style) {
 		super(parent, style);
 
 		Activator.getDefault().getEcp1Client().addMeasurementDataListener(this);
 		
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		setLayout(gridLayout);
 		
 		GridData gridData = new GridData();
 		gridData.verticalSpan = 2;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.verticalAlignment = SWT.FILL;
 		gridData.minimumHeight = 400;
 		gridData.minimumWidth = 600;
 		canvas = new Canvas(this, SWT.NONE);
         canvas.setLayoutData(gridData);
 	    //use LightweightSystem to create the bridge between SWT and draw2D
 		final LightweightSystem lws = new LightweightSystem(canvas);
 		//set it as the content of LightwightSystem
 		xyPlot = new XYPlot();
 		lws.setContents(xyPlot);
 	}
 
 	/**
 	 * Refreshes the plot.
 	 * 
 	 * @param plotWindow the plot window
 	 * @param chid the id of the chain
 	 * @param smid the id of the scan module
 	 * @param motorId the id of the motor
 	 * @param motorName the name of the motor
 	 * @param detector1Id the id of the first detector
 	 * @param detector1Name the name of the first detector
 	 * @param detector2Id the id of the second detector
 	 * @param detector2Name the name of the second detector
 	 */
 	public void refresh(PlotWindow plotWindow, int chid, int smid, 
 						 String motorId, String motorName, String detector1Id, 
 						 String detector1Name, String detector2Id, 
 						 String detector2Name) {
 
 		// do not clean if plot has "isInit=false" AND detectors and motors are 
 		// still the same
 		if ((this.motorId == motorId) && (this.detector1Id == detector1Id) && 
 				(this.detector2Id == detector2Id)) {
 			if (plotWindow.isInit()) xyPlot.removeAllTraces();
 		}
 		else {
 			xyPlot.removeAllTraces();
 		}
 
		// set the new values for chain, scan module, motor and detectors
 		this.chid = chid;
 		this.smid = smid;
 		this.detector1Id = detector1Id;
 		this.detector2Id = detector2Id;
 		this.motorId = motorId;
 
 		// update the x axis with the new motor name
 		if(plotWindow.getMode() == PlotModes.LOG)
 			xyPlot.setXAxisTitle(motorName + " (log)");
 		else
 			xyPlot.setXAxisTitle(motorName);
              	
 		// update first y axis 
 		if (this.detector1Id != null)
 		{
 			this.detector1Name = detector1Name;
 			if (xyPlot.getTrace(detector1Name) == null)
 				xyPlot.addTrace(detector1Name);
 
 			xyPlot.getTrace(detector1Name).setTraceType(
 					plotWindow.getYAxes().get(0).getLinestyle());
 			xyPlot.getTrace(detector1Name).setPointStyle(
 					plotWindow.getYAxes().get(0).getMarkstyle());
 			xyPlot.getTrace(detector1Name).setTraceColor(
 					new Color(null, plotWindow.getYAxes().get(0).getColor()));
 			xyPlot.getTrace(detector1Name).getYAxis().setLogScale(
 					plotWindow.getYAxes().get(0).getMode() == PlotModes.LOG 
 					? true 
 					: false);
 			xyPlot.getTrace(detector1Name).getXAxis().setLogScale(
 					plotWindow.getMode() == PlotModes.LOG 
 					? true
 					: false);
 			if(plotWindow.getYAxes().get(0).getMode() == PlotModes.LOG)
 				xyPlot.getTrace(detector1Name).getYAxis().setTitle(
 													detector1Name + " (log)");
 		}
 		else {
 			xyPlot.removeTrace(detector1Name);
 			this.detector1Name = null;
 		}
 		// update second y axis
 		if (this.detector2Id != null)
 		{
 			this.detector2Name = detector2Name;
 			if (xyPlot.getTrace(detector2Name) == null)
 				xyPlot.addTrace(detector2Name);
 				
 			xyPlot.getTrace(detector2Name).setTraceType(
 					plotWindow.getYAxes().get(1).getLinestyle());
 			xyPlot.getTrace(detector2Name).setPointStyle(
 					plotWindow.getYAxes().get(1).getMarkstyle());
 			xyPlot.getTrace(detector2Name).setTraceColor(
 					new Color(null, plotWindow.getYAxes().get(1).getColor()));
 			xyPlot.getTrace(detector2Name).getYAxis().setLogScale(
 					plotWindow.getYAxes().get(1).getMode() == PlotModes.LOG 
 					? true 
 					: false);
 			xyPlot.getTrace(detector2Name).getXAxis().setLogScale(
 					plotWindow.getMode() == PlotModes.LOG 
 					? true
 					: false);
 			if(plotWindow.getYAxes().get(1).getMode() == PlotModes.LOG)
 				xyPlot.getTrace(detector2Name).getYAxis().setTitle(
 													detector2Name + " (log)");
 		}
 		else {
 			xyPlot.removeTrace(detector2Name);
 			this.detector2Name = null;
 		}
 		
 		// redraw
 		canvas.layout();
 		canvas.redraw();
 		this.layout();
 		this.redraw();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void measurementDataTransmitted(MeasurementData measurementData) {
 
 		// do nothing if no measurement data was given
 		if (measurementData == null) return;
 
 		// indicators for new data
 		boolean detector1HasData = false;
 		boolean detector2HasData = false;
 		boolean motorHasData = false;
 			
		// are we are still in the same scan module of the same chain ?
 		if ((measurementData.getChainId() == chid) && 
 			(measurementData.getScanModuleId() == smid))
 		{
			// is the data for detector 1 ?
 			if (this.detector1Id != null && 
 				this.detector1Id.equals(measurementData.getName()) && 
 				measurementData.getDataModifier() == DataModifier.UNMODIFIED) 
 			{
 				detector1PosCount = measurementData.getPositionCounter();
 				DataType dt = measurementData.getDataType();
 				
 				// is the data type correct ?
 				if (dt == DataType.DOUBLE || dt == DataType.FLOAT || 
 					dt == DataType.INT32 || dt == DataType.INT16 || 
 					dt == DataType.INT8)
 				{
 					// get the data and indicate that detector 1 has new data
 					y1value = (Double) measurementData.getValues().get(0);
 					detector1HasData = true;
 				}
			}
 			else if (this.detector2Id != null && 
 					  this.detector2Id.equals(measurementData.getName()) && 
 				   measurementData.getDataModifier() == DataModifier.UNMODIFIED) 
 			{
 				detector2PosCount = measurementData.getPositionCounter();
 				DataType dt = measurementData.getDataType();
 				
 				if (dt == DataType.DOUBLE || dt == DataType.FLOAT || 
 					dt == DataType.INT32 || dt == DataType.INT16 || 
 					dt == DataType.INT8)
 				{
 					y2value = (Double) measurementData.getValues().get(0);
 					detector2HasData = true;
 				}
 			}
 			else if (this.motorId.equals(measurementData.getName()) && 
 				   measurementData.getDataModifier() == DataModifier.UNMODIFIED) 
 			{
 				motorPosCount = measurementData.getPositionCounter();
 				DataType dt = measurementData.getDataType();
 				
 				if (dt == DataType.DOUBLE || 
 					dt == DataType.FLOAT || dt == DataType.INT32 || 
 					dt == DataType.INT16 || dt == DataType.INT8)
 				{
 					xValue = (Double) measurementData.getValues().get(0);
 					motorHasData = true;
 				}
 			}
 		
 			final boolean plotDetector1 = (detector1HasData || motorHasData)&& 
 										(detector1PosCount == motorPosCount);
 			final boolean plotDetector2 = (detector2HasData || motorHasData)&& 
 										(detector2PosCount == motorPosCount);
 			
 			// if there is new data, update the plot 
 			if ((plotDetector1 || plotDetector2) && !this.isDisposed()) 
 			{
 				// plot synchronously (to assure no side effects)
 				this.getDisplay().syncExec( new Runnable() {
 	
 					public void run() 
 					{
						// TODO logging, take a look at feature #25 in Redmine!
						// System.out.println("redrawing plot ");
 						if (!isDisposed()) 
 						{
 							if(plotDetector1) 
 								xyPlot.setData(detector1Name, xValue,y1value);
 							if(plotDetector2)
 								xyPlot.setData(detector2Name, xValue,y2value);
 						}
 					}
 				});
 			}
 		}
 	}
 }

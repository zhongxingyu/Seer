 package de.ptb.epics.eve.viewer.views.plotview;
 
 import gov.aps.jca.dbr.TimeStamp;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 import de.ptb.epics.eve.data.scandescription.PlotWindow;
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 import de.ptb.epics.eve.ecp1.types.DataModifier;
 import de.ptb.epics.eve.ecp1.types.DataType;
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.plot.XYPlot;
 
 /**
  * <code>plotGraphComposite</code> contains the xy-plot and is located in the
  * Plot View.
  * 
  * @author Jens Eden
  * @author Marcus Michalsky
  */
 public class PlotViewGraphComposite extends Composite implements
 		IMeasurementDataListener {
 
 	private static Logger logger = Logger
 			.getLogger(PlotViewGraphComposite.class);
 
 	private String detector1Id;
 	private String detector2Id;
 	private String motorId;
 	private boolean detector1normalized;
 	private boolean detector2normalized;
 	private int detector1PosCount;
 	private int detector2PosCount;
 	private int posCount;
 	TimeStamp timestamp;
 	TimeStamp timestamp_det1;
 	TimeStamp timestamp_det2;
 	private Double xValue;
 	private int chid;
 	private int smid;
 	private Double y2value;
 	private Double y1value;
 	private Canvas canvas;
 	private XYPlot xyPlot;
 	private String detector1Name;
 	private String detector2Name;
 
 	private String traceNameForDet1;
 	private String traceNameForDet2;
 
 	/**
 	 * Constructs a <code>plotGraphComposite</code>.
 	 * 
 	 * @param parent
 	 *            the parent
 	 * @param style
 	 *            the style
 	 */
 	public PlotViewGraphComposite(Composite parent, int style) {
 		super(parent, style);
 
 		// this composite wants to be informed if new data is available...
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
 		// use LightweightSystem to create the bridge between SWT and draw2D
 		final LightweightSystem lws = new LightweightSystem(canvas);
 		// set it as the content of LightwightSystem
 		xyPlot = new XYPlot();
 		lws.setContents(xyPlot);
 	}
 
 	/**
 	 * Refreshes the plot.
 	 * 
 	 * @param plotWindow
 	 *            the plot window
 	 * @param chid
 	 *            the id of the chain
 	 * @param smid
 	 *            the id of the scan module
 	 * @param motorId
 	 *            the id of the motor
 	 * @param motorName
 	 *            the name of the motor
 	 * @param detector1Id
 	 *            the id of the first detector
 	 * @param detector1Name
 	 *            the name of the first detector
 	 * @param detector2Id
 	 *            the id of the second detector
 	 * @param detector2Name
 	 *            the name of the second detector
 	 */
 	public void refresh(PlotWindow plotWindow, int chid, int smid,
 			String motorId, String motorName, String detector1Id,
 			String detector1Name, String detector2Id, String detector2Name) {
 		// XXX get rid of parameters, 9 is too much !
 		// do not clean if plot has "isInit=false" AND detectors and motors are
 		// still the same
 		if ((this.motorId == motorId) && (this.detector1Id == detector1Id)
 				&& (this.detector2Id == detector2Id)) {
 			if (plotWindow.isInit())
 				xyPlot.init(true);
 		} else {
 			xyPlot.init(true);
 		}
 
 		// set new values for chain, scan module, motor and detectors
 		this.chid = chid;
 		this.smid = smid;
 		this.detector1Id = detector1Id;
 		this.detector2Id = detector2Id;
 		this.motorId = motorId;
 		this.detector1normalized = plotWindow.getYAxes().get(0)
 				.getNormalizeChannel() != null;
 
 		// find the y axis of the detector channel in the plotWindow
 		int axis_pos = -1;
 
 		// Note: if both axes have the same detector channel (one with
 		// normalize)
 		// axis_pos is first set to 0 then to 1 (intended behavior).
 		for (int i = 0; i < plotWindow.getYAxes().size(); i++) {
 			if (plotWindow.getYAxes().get(i).getDetectorChannel().getID() == detector2Id) {
 				axis_pos = i;
 			}
 		}
 
 		if (axis_pos != -1) { // axis of detector 2 was found -> check if it is
 								// normalized
 			this.detector2normalized = plotWindow.getYAxes().get(axis_pos)
 					.getNormalizeChannel() != null;
 		}
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("detector 1 normalized ? " + detector1normalized);
 			logger.debug("detector 2 normalized ? " + detector2normalized);
 		}
 
 		// reset time stamp stuff (used in measurementDataReceived)
 		timestamp = null;
 		timestamp_det1 = null;
 		timestamp_det2 = null;
 
 		traceNameForDet1 = detector1Name;
 		if (detector1normalized) {
 			traceNameForDet1 += " / "
 					+ plotWindow.getYAxes().get(0).getNormalizeChannel()
 							.getName();
 		}
 
 		traceNameForDet2 = detector2Name;
 		if (detector2normalized) {
 			traceNameForDet2 += " / "
 					+ plotWindow.getYAxes().get(1).getNormalizeChannel()
 							.getName();
 		}
 
 		// update first y axis
 		if (this.detector1Id != null) {
 			// set the current (1st) detector name
 			this.detector1Name = detector1Name;
 			// does the plot already have a trace of this detector ?
 			// if not -> add as new trace
 			if (xyPlot.getTrace(traceNameForDet1) == null)
 				xyPlot.addTrace(traceNameForDet1, detector1Id, motorName,
 						motorId, plotWindow, 0);
 		} else {
 			// no first detector -> remove the trace (if present) and set null
 			xyPlot.removeTrace(traceNameForDet1);
 			this.detector1Name = null;
 		}
 		// update second y axis
 		if (this.detector2Id != null) {
 			// set the current (2nd) detector name
 			this.detector2Name = detector2Name;
 			// does the plot already have a trace of this detector ?
 			// if not -> add as new trace
 			if (xyPlot.getTrace(traceNameForDet2) == null)
 				xyPlot.addTrace(traceNameForDet2, detector2Id, motorName,
 						motorId, plotWindow, 1);
 		} else {
 			// no second detector -> remove the trace (if present) and set null
 			xyPlot.removeTrace(traceNameForDet2);
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
 	public void dispose() {
 		Activator.getDefault().getEcp1Client()
 				.removeMeasurementDataListener(this);
 		xyPlot.erase();
 		// super.dispose();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void measurementDataTransmitted(MeasurementData measurementData) {
 
 		// do nothing if no measurement data was given
 		if (measurementData == null)
 			return;
 
 		// indicators for new data
 		boolean detector1HasData = false;
 		boolean detector2HasData = false;
 		boolean motorHasData = false;
 
 		// ****************** logging *****************************************
 		if (logger.isDebugEnabled()) {
 			String val = "";
 			String val2 = "";
 			if (measurementData.getValues().size() > 1) {
 				val2 = measurementData.getValues().get(1).toString();
 			}
 			if (measurementData.getValues().size() > 0) {
 				val = measurementData.getValues().get(0).toString();
 			} else {
 				val = "no value";
 			}
 
 			logger.debug("["
 					+ xyPlot.hashCode()
 					+ "] "
 					+ "ChainID: "
 					+ measurementData.getChainId()
 					+ ", "
 					+ "ScanModuleID: "
 					+ measurementData.getScanModuleId()
 					+ ", "
 					+ "Name: "
 					+ measurementData.getName()
 					+ ", "
 					+ "Position: "
 					+ measurementData.getPositionCounter()
 					+ ", "
 					+ "Value: "
 					+ val
 					+ " / "
 					+ val2
 					+ ", "
 					+ "Data Type: "
 					+ measurementData.getDataType()
 					+ ", "
 					+ "Data Modifier: "
 					+ measurementData.getDataModifier()
 					+ ", "
 					+ "at: "
 					+ new TimeStamp(
 							measurementData.getGerenalTimeStamp() - 631152000,
 							measurementData.getNanoseconds()).toMONDDYYYY());
 		}
 		// **************end of: logging **************************************
 
 		// are we still in the same scan module of the same chain ?
 		if ((measurementData.getChainId() == chid)
 				&& (measurementData.getScanModuleId() == smid)) {
 			// since measurementDataTransmitted each time ANY data is
 			// transmitted we have to distinguish it
 
 			// detector 1 data ?
 			if (this.detector1Id != null
 					&& this.detector1Id.equals(measurementData.getName())) {
 				detector1PosCount = measurementData.getPositionCounter();
 				DataType datatype = measurementData.getDataType();
 
 				if (!detector1normalized
 						&& measurementData.getDataModifier() == DataModifier.UNMODIFIED) {
 					switch (datatype) {
 					case INT32:
 						y1value = ((Integer) measurementData.getValues().get(0))
 								.doubleValue();
 						detector1HasData = true;
 						break;
 					case DOUBLE:
 						y1value = (Double) measurementData.getValues().get(0);
 						detector1HasData = true;
 						break;
 					case DATETIME:
 						timestamp_det1 = new TimeStamp(
 								measurementData.getGerenalTimeStamp(),
 								measurementData.getNanoseconds());
 						detector1HasData = true;
 						break;
 					default:
 						break;
 					}
 				} else if (detector1normalized
 						&& measurementData.getDataModifier() == DataModifier.NORMALIZED) {
 					switch (datatype) {
 					case DOUBLE:
 						y1value = (Double) measurementData.getValues().get(0);
 						detector1HasData = true;
 						break;
 					default:
 						break;
 					}
 				}
 			}
 			// detector 2 data ?
 			if (this.detector2Id != null
 					&& this.detector2Id.equals(measurementData.getName())) {
 				detector2PosCount = measurementData.getPositionCounter();
 				DataType datatype = measurementData.getDataType();
 
 				if (!detector2normalized
 						&& measurementData.getDataModifier() == DataModifier.UNMODIFIED) {
 					switch (datatype) {
 					case INT32:
 						y2value = ((Integer) measurementData.getValues().get(0))
 								.doubleValue();
 						detector2HasData = true;
 						break;
 					case DOUBLE:
 						y2value = (Double) measurementData.getValues().get(0);
 						detector2HasData = true;
 						break;
 					case DATETIME:
 						timestamp_det2 = new TimeStamp(
 								measurementData.getGerenalTimeStamp(),
 								measurementData.getNanoseconds());
 						detector2HasData = true;
 						break;
 					default:
 						break;
 					}
 				} else if (detector2normalized
 						&& measurementData.getDataModifier() == DataModifier.NORMALIZED) {
 					switch (datatype) {
 					case DOUBLE:
 						y2value = (Double) measurementData.getValues().get(0);
 						detector2HasData = true;
 						break;
 					default:
 						break;
 					}
 				}
 			}
 			// motor data ?
 			else if (this.motorId != null
 					&& this.motorId.equals(measurementData.getName())) {
 				if (measurementData.getDataModifier() == DataModifier.UNMODIFIED) {
 					posCount = measurementData.getPositionCounter();
 					DataType datatype = measurementData.getDataType();
 
 					switch (datatype) {
 					case DOUBLE:
 						xValue = (Double) measurementData.getValues().get(0);
 						motorHasData = true;
 						break;
 					case INT8:
 						xValue = Double.valueOf((Integer) measurementData
 								.getValues().get(0));
 						motorHasData = true;
 						break;
 					case INT16:
 						xValue = Double.valueOf((Integer) measurementData
 								.getValues().get(0));
 						motorHasData = true;
 						break;
 					case INT32:
 						xValue = Double.valueOf((Integer) measurementData
 								.getValues().get(0));
 						motorHasData = true;
 						break;
 					case DATETIME:
 						timestamp = new TimeStamp(
 								measurementData.getGerenalTimeStamp(),
 								measurementData.getNanoseconds());
 						xValue = Double.valueOf(posCount);
 						motorHasData = true;
 						break;
 					// measurementData offers a date with Epoch=1/1/1970 (UNIX)
 					// the TimeStamp Object has an epoch of 1/1/1990 (EPICS)
 					// but the plot widget is also based on the unix epoch
 					// therefore nothing needs to be done, but be aware of
 					// the fact that timestamp.toMONDDYYYY returns the year +20
 					case STRING:
 						logger.info("discrete motor position "
 								+ "detected - not implemented" + "yet");
 						break;
 					default:
 						break;
 					}
 				}
 			}
 		} // end of if (chid, smid)
 
 		// if there is new data, update the plot
 		final boolean plotDetector1 = (detector1HasData || motorHasData)
 				&& (detector1PosCount == posCount);
 		final boolean plotDetector2 = (detector2HasData || motorHasData)
 				&& (detector2PosCount == posCount);
 
 		if ((plotDetector1 || plotDetector2) && !this.isDisposed()) {
 			// plot synchronously (to assure no side effects)
 			// (if async is used and a counter with no delay it fails)
 			this.getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (!isDisposed()) {
 						// plot (motor_pos, timestamp:det1)
 						if (plotDetector1 && timestamp_det1 != null)
 							xyPlot.setData(traceNameForDet1, xValue,
 									timestamp_det1);
 						// plot (motor_pos, timestamp:det2)
 						if (plotDetector2 && timestamp_det2 != null)
 							xyPlot.setData(traceNameForDet2, xValue,
 									timestamp_det2);
 						// plot (motor_pos, det1_val)
 						if (plotDetector1 && timestamp == null)
 							xyPlot.setData(traceNameForDet1, xValue, y1value);
 						// plot (motor_pos, det2_val)
 						if (plotDetector2 && timestamp == null)
 							xyPlot.setData(traceNameForDet2, xValue, y2value);
 						// plot (time stamp, det1_val)
 						if (plotDetector1 && timestamp != null)
 							xyPlot.setData(traceNameForDet1, posCount, y1value,
 									timestamp);
 						// plot(time stamp, det2_val)
 						if (plotDetector2 && timestamp != null)
 							xyPlot.setData(traceNameForDet2, posCount, y2value,
 									timestamp);
 					}
 				}
 			});
 		}
 	} // end of measurementDataTransmitted
 }

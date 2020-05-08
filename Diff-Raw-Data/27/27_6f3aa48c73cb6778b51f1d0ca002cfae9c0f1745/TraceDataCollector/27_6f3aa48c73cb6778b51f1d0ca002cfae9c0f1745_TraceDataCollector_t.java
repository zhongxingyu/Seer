 package de.ptb.epics.eve.viewer.views.plotview;
 
 import gov.aps.jca.dbr.TimeStamp;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
 import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
 import org.csstudio.swt.xygraph.dataprovider.ISample;
 import org.csstudio.swt.xygraph.linearscale.Range;
 
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 import de.ptb.epics.eve.ecp1.types.DataModifier;
 import de.ptb.epics.eve.viewer.Activator;
 
 /**
  * @author Marcus Michalsky
  * @since 1.13
  */
 public class TraceDataCollector implements IDataProvider,
 		IMeasurementDataListener {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(TraceDataCollector.class.getName());
 	
 	private List<IDataProviderListener> listeners;
 	private TraceInfo traceInfo;
 	private int size;
 	private List<Sample> data;
 	
 	private int motorPosCount;
 	private int detectorPosCount;
 	private double motorValue;
 	private double detectorValue;
 	
 	private double xMin;
 	private double xMax;
 	private double yMin;
 	private double yMax;
 	
 	private Calendar calendar;
 	private UpdateStrategy updateStrategy;
 	private int sampleSizeOfLastUpdate;
 	private long timeOfLastSample;
 	
 	/**
 	 * 
 	 * @param size
 	 */
 	public TraceDataCollector(int size, TraceInfo traceInfo) {
 		this.size = size;
 		this.traceInfo = traceInfo;
 		this.motorPosCount = 0;
 		this.detectorPosCount = 0;
 		this.motorValue = 0;
 		this.detectorValue = 0;
 		
 		this.xMin = this.yMin = Double.POSITIVE_INFINITY;
 		this.xMax = this.yMax = Double.NEGATIVE_INFINITY;
 		
 		this.calendar = Calendar.getInstance();
 		this.updateStrategy = new TimeAndSampleSizeUpdateStrategy(1000);
 		this.sampleSizeOfLastUpdate = 0;
 		this.timeOfLastSample = this.calendar.getTimeInMillis();
 		
 		this.data = new ArrayList<Sample>(this.size);
 		this.listeners = new LinkedList<IDataProviderListener>();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void measurementDataTransmitted(MeasurementData measurementData) {
 		if (measurementData.getName().equals(traceInfo.getMotorId())) {
 			this.motorPosCount = measurementData.getPositionCounter();
 			switch (measurementData.getDataType()) {
 			case INT8:
 			case INT16:
 			case INT32:
 				this.motorValue = ((Integer) measurementData.getValues().get(0))
 						.doubleValue();
 				break;
 			case FLOAT:
 				this.motorValue = ((Float) measurementData.getValues().get(0))
 				.doubleValue();
 				break;
 			case DOUBLE:
 				this.motorValue = ((Double) measurementData.getValues().get(0));
 				break;
 			case DATETIME:
 				TimeStamp time = new TimeStamp(
 						measurementData.getGeneralTimeStamp(),
 						measurementData.getNanoseconds());
 				this.motorValue = time.secPastEpoch() * 1000 + time.nsec()
 						/ 1000000;
 				break;
 			case STRING:
 				// not implemented yet
 				break;
 			}
 			this.checkForData();
 		}
 		if (!measurementData.getDataModifier().equals(traceInfo.getModifier())) {
 			return;
 		}
 		if ((this.traceInfo.getModifier().equals(DataModifier.UNMODIFIED) && 
 				measurementData.getName().equals(traceInfo.getDetectorId()))
 			|| (this.traceInfo.getModifier().equals(DataModifier.NORMALIZED) && 
 				measurementData.getName().equals(this.traceInfo.getNormalizeId()))) {
 			this.detectorPosCount = measurementData.getPositionCounter();
 			switch (measurementData.getDataType()) {
 			case INT8:
 			case INT16:
 			case INT32:
 				this.detectorValue = ((Integer) measurementData.getValues()
 						.get(0)).doubleValue();
 				break;
 			case FLOAT:
 				this.detectorValue = ((Float) measurementData.getValues()
 						.get(0)).doubleValue();
 				break;
 			case DOUBLE:
 				this.detectorValue = ((Double) measurementData.getValues()
 						.get(0));
 				break;
 			case DATETIME:
 				TimeStamp time = new TimeStamp(
 						measurementData.getGeneralTimeStamp(),
 						measurementData.getNanoseconds());
 				this.detectorValue = time.secPastEpoch() * 1000 + time.nsec()
 						/ 1000000;
 				break;
 			case STRING:
 				// not implemented yet
 				break;
 			}
 			this.checkForData();
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void checkForData() {
 		if (this.motorPosCount == this.detectorPosCount) {
 			Sample sample = new Sample(this.motorValue, this.detectorValue);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("Plot " + this.traceInfo.getPlotId() + " ("
 						+ this.traceInfo.getPlotName() + ") - " 
 						+ (!this.traceInfo.getNormalizeId().isEmpty() 
 							? this.traceInfo.getNormalizeId() 
 							: this.traceInfo.getDetectorId())
 						+ ": Pos: "
 						+ this.motorPosCount + " (" + sample.getXValue() + ", "
 						+ sample.getYValue() + ")");
 			}
 			this.data.add(sample);
 			
 			if (this.motorValue < this.xMin) {
 				this.xMin = this.motorValue;
			} 
			if (this.motorValue > this.xMax) {
 				this.xMax = this.motorValue;
 			}
 			if (this.detectorValue < this.yMin) {
 				this.yMin = this.detectorValue;
			}
			if (this.detectorValue > this.yMax) {
 				this.yMax = this.detectorValue;
 			}
 		
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("New Sample: (" + sample.getXValue() + ", " 
						+ sample.getYValue() + ")");
				LOGGER.debug("Current x-Range: (" + this.xMin + ", "
						+ this.xMax + ")");
				LOGGER.debug("Current y-Range: (" + this.yMin + ", "
						+ this.yMax + ")");
			}
			
 			if (this.updateStrategy.update(timeOfLastSample,
 					sampleSizeOfLastUpdate, this.data.size())) {
 				this.timeOfLastSample = Calendar.getInstance().getTimeInMillis();
 				this.sampleSizeOfLastUpdate = this.data.size();
 				LOGGER.debug("publish data");
 				this.publish();
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public void publish() {
 		final IDataProvider idpl = this;
 		for (final IDataProviderListener listener : this.listeners) {
 			Activator.getDefault().getWorkbench().getDisplay()
 			.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					listener.dataChanged(idpl);
 				}});
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ISample getSample(int i) {
 		return this.data.get(i);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getSize() {
 		return this.data.size();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Range getXDataMinMax() {
 		return new Range(this.xMin, this.xMax);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Range getYDataMinMax() {
 		return new Range(this.yMin, this.yMax);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isChronological() {
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param strategy
 	 */
 	public void setUpdateStratety(UpdateStrategy strategy) {
 		this.updateStrategy = strategy;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addDataProviderListener(IDataProviderListener listener) {
 		this.listeners.add(listener);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean removeDataProviderListener(IDataProviderListener listener) {
 		return this.listeners.remove(listener);
 	}
 }

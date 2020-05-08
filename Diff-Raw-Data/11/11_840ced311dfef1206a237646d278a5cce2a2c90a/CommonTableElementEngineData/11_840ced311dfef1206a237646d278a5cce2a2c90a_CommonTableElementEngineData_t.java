 package de.ptb.epics.eve.viewer.views.deviceinspectorview;
 
 import java.util.Formatter;
 import java.util.Locale;
 
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 import de.ptb.epics.eve.ecp1.types.DataModifier;
 import de.ptb.epics.eve.ecp1.types.DataType;
 import de.ptb.epics.eve.viewer.Activator;
 
 /**
  * <code>CommonTableElementEngineData</code>.
  * 
  * @author Jens Eden
  * @author Marcus Michalsky
  */
 public class CommonTableElementEngineData implements IMeasurementDataListener {
 	
 	private String dataId;
 	private DataModifier datamodifier;
 	private Boolean isActive;
 	private String textvalue;
 	private CommonTableElement tableElement;
 	
 	/**
 	 * Constructs a <code>CommonTableElementEngineData</code<.
 	 * 
 	 * @param dataId the id of the data
 	 * @param tableElement the 
 	 * {@link de.ptb.epics.eve.viewer.views.deviceinspectorview.CommonTableElement}
 	 * 		the data corresponds to
 	 */
 	public CommonTableElementEngineData(String dataId, CommonTableElement tableElement) {
 		this.dataId = dataId;
 		this.tableElement = tableElement;
 		Activator.getDefault().getEcp1Client().addMeasurementDataListener(this);
 		datamodifier = DataModifier.UNMODIFIED;
 		isActive = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void finalize() throws Throwable {
 		Activator.getDefault().getEcp1Client()
 				.removeMeasurementDataListener(this);
 		super.finalize();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void measurementDataTransmitted(final MeasurementData measurementData) {
 		if (!isActive || measurementData == null || dataId == null) {
 			return;
 		}
 		
 		if (this.dataId.equals(measurementData.getName()) && 
 			(measurementData.getDataModifier() == datamodifier)) {
 			if ((measurementData.getDataType() == DataType.DOUBLE) || 
 				(measurementData.getDataType() == DataType.FLOAT)) {
 					Double value = (Double) measurementData.getValues().get(0);
					Formatter formatter = new Formatter(
							new Locale(Locale.ENGLISH.getCountry()));
 					textvalue = formatter.format("%12.4g", value).out().
 							toString();
					formatter.close();
 			} else {
 				textvalue = measurementData.getValues().get( 0 ).toString();
 			}
 			tableElement.update();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param dataId
 	 */
 	public void setDataId(String dataId) {
 		this.dataId = dataId;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getValue() {
 		return textvalue;
 	}
 
 	/**
 	 * 
 	 * @param datamodifier
 	 */
 	public void setDataModifier(DataModifier datamodifier) {
 		this.datamodifier = datamodifier;
 	}
 	
 	/**
 	 * 
 	 * @param active
 	 */
 	public void setActive(boolean active) {
 		isActive = active;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Boolean getActive() {
 		return isActive;
 	}
 }

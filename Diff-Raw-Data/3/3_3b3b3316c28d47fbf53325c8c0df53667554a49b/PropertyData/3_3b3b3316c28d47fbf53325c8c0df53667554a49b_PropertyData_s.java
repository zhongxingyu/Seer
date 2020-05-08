 package com.genability.client.types;
 
 import java.math.BigDecimal;
 
 import org.joda.time.DateTime;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.annotation.JsonInclude.Include;
 
 @JsonInclude(Include.NON_NULL)
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class PropertyData {
 	
 	public static final String REST_TYPE = "PropertyData";
 	
 	protected String keyName;
 	
 	protected DateTime fromDateTime;
 	
 	protected DateTime toDateTime;
 	
 	protected String period;
 	
 	protected String unit;
 	
 	protected String dataValue;
 	
 	protected BigDecimal accuracy;
 	
 	protected DataType dataType;
 	
 	public PropertyData() {
 		//constructor
 	}
 
 
 	public void setKeyName(String keyName) {
 		this.keyName = keyName;
 	}
 	
 	public String getKeyName() {
 		return keyName;
 	}
 	
 	/**
 	 * @return the fromDate
 	 */
 	public DateTime getFromDateTime() {
 		return fromDateTime;
 	}
 
 	/**
 	 * @param fromDate
 	 *            the fromDate to set
 	 */
 
 	public void setFromDateTime(DateTime fromDate) {
 		this.fromDateTime = fromDate;
 	}
 
 	/**
 	 * @return the toDate
 	 */
 	public DateTime getToDateTime() {
 		return toDateTime;
 	}
 
 	/**
 	 * @param toDate
 	 *            the toDate to set
 	 */
 	public void setToDateTime(DateTime toDate) {
 		this.toDateTime = toDate;
 	}
 
 	/**
 	 * @param unit
 	 *            the unit to set
 	 */
 	public void setPeriod(String period) {
 		this.period = period;
 	}
 
 	/**
 	 * @return the unit
 	 */
 	@JsonInclude(Include.NON_NULL)
 	public String getPeriod() {
 		return period;
 	}
 	
 	/**
 	 * @param unit
 	 *            the unit to set
 	 */
 	public void setUnit(String unit) {
 		this.unit = unit;
 	}
 
 	/**
 	 * @return the unit
 	 */
 	@JsonInclude(Include.NON_NULL)
 	public String getUnit() {
 		return unit;
 	}
 
 	/**
 	 * 
 	 */
 	public void setDataValue(String dataValue) {
 		this.dataValue = dataValue;
 	}
 
 	/**
 	 * 
 	 */
 	@JsonIgnore
 	public void setDataValue(Number dataValue) {
 		this.dataValue = dataValue != null ? dataValue.toString() : null;
 	}
 
 	/**
 	 * 
 	 */
 	@JsonIgnore
 	public void setDataValue(Enum<?> dataValue) {
 		this.dataValue = dataValue != null ? dataValue.name() : null;
 	}
 
 	/**
 	 * 
 	 */
 	@JsonIgnore
 	public void setDataValue(DateTime dataValue) {
 		this.dataValue = dataValue != null ? dataValue.toString() : null;
 	}
 
 	/**
 	 * 
 	 */
 	@JsonIgnore
 	public void setDataValue(Boolean dataValue) {
 		this.dataValue = dataValue != null ? Boolean.toString(dataValue) : null;
 	}
 
 	/**
 	 * @return the unit
 	 */
 	@JsonInclude(Include.NON_NULL)
 	public String getDataValue() {
 		return dataValue;
 	}
 
 	/**
 	 * @param accuracy
 	 *            the accuracy to set
 	 */
 	public void setAccuracy(BigDecimal accuracy) {
 		this.accuracy = accuracy;
 	}
 
 	
 	/**
 	 * @return the accuracy
 	 */
 	@JsonInclude(Include.NON_NULL)
 	public BigDecimal getAccuracy() {
 		return accuracy;
 	}
 	
 	public void setDataType(DataType dataType) {
 		this.dataType = dataType;
 	}
 
 	public DataType getDataType() {
 		return dataType;
 	}
 }

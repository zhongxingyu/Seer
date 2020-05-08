 package org.hbird.core.spacesystemmodel.tmtc.provided;
 
 import org.codehaus.jackson.annotate.JsonCreator;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.hbird.core.spacesystemmodel.tmtc.Parameter;
 
 public class CalibratedParameter extends AbstractParameter<Double> {
 	private static final long serialVersionUID = -833822350920302695L;
 
 	private double calibratedValue;
 
	private long receivedTime;

 	@JsonCreator
 	public CalibratedParameter(@JsonProperty("qualifiedName") final String qualifiedName, @JsonProperty("name") final String name,
 			@JsonProperty("shortDescription") final String shortDescription, @JsonProperty("longDescription") final String longDescription) {
 		super(qualifiedName, name, shortDescription, longDescription);
 	}
 
 	/**
 	 * Factory method from constructing a calibrated version of an existing parameter with a calibrated value preset.
 	 * 
 	 * @param p
 	 * @param cval
 	 * @return
 	 */
 	public static CalibratedParameter createFromParameterAndValue(Parameter<?> p, double cval) {
 		CalibratedParameter cp = new CalibratedParameter(p.getQualifiedName(), p.getName(), p.getShortDescription(), p.getLongDescription());
 		cp.setValue(cval);
		cp.setReceivedTime(p.getReceivedTime());
 		return cp;
 	}
 
 	@Override
 	public Double getValue() {
 		return calibratedValue;
 	}
 
 	@Override
 	public void setValue(Double value) {
 		calibratedValue = value;
 	}
 
 	@Override
 	public long getReceivedTime() {
		return receivedTime;
 	}
 
 	@Override
 	public void setReceivedTime(long timestamp) {
		this.receivedTime = timestamp;
 	}
 
 	@Override
 	public boolean isReadOnly() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("CalibratedParameter [calibratedValue=").append(calibratedValue).append(", getValue()=").append(getValue())
 				.append(", getReceivedTime()=").append(getReceivedTime()).append(", getName()=").append(getName()).append(", getQualifiedName()=")
 				.append(getQualifiedName()).append("]");
 		return builder.toString();
 	}
 
 }

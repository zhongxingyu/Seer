 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Measurement implements Message, Serializable, Comparable<Measurement> {
 
 	private static final long serialVersionUID = -906069262585850986L;
 
 	private String ptuId;
 	private String name;
 	private Number value;
 	private String unit;
 	private Date date;
 
 	public Measurement() {
 	}
 
 	public Measurement(String name, Number value, String unit) {
 		this(null, name, value, unit, new Date());
 	}
 
 	public Measurement(String ptuId, String name, Number value, String unit, Date date) {
 		this.ptuId = ptuId;
 		this.name = name;
 		this.value = value;
 		this.unit = unit;
 		this.date = date;
 	}
 
 	@Override
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Number getValue() {
 		return value;
 	}
 
 	public String getUnit() {
 		return unit;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	@Override
 	public String getType() {
		return "measurement";
 	}
 
 	@Override
 	public int compareTo(Measurement o) {
 		// FIXME include PTU
 		return (o != null) ? getName().compareTo(o.getName()) : 1;
 	}
 
 	@Override
 	public int hashCode() {
 		return (getPtuId() != null ? getPtuId().hashCode() : 0)
 				+ (getName() != null ? getName().hashCode() : 0)
 				+ (getValue() != null ? getValue().hashCode() : 0)
 				+ (getUnit() != null ? getUnit().hashCode() : 0)
 				+ (getDate() != null ? getDate().hashCode() : 0)
 				+ (getType() != null ? getType().hashCode() : 0);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if ((obj != null) && (obj instanceof Measurement)) {
 			Measurement m = (Measurement) obj;
 			return (getPtuId() == null ? m.getPtuId() == null : getPtuId().equals(m.getPtuId()))
 					&& (getName() == null ? m.getName() == null : getName().equals(m.getName()))
 					&& (getValue() == null ? m.getValue() == null : getValue().equals(m.getValue()))
 					&& (getUnit() == null ? m.getUnit() == null : getUnit().equals(m.getUnit()))
 					&& (getDate() == null ? m.getDate() == null : getDate().equals(m.getDate()))
 					&& (getType() == null ? m.getType() == null : getType().equals(m.getType()));
 		}
 		return super.equals(obj);
 	}
 
 	@Override
 	public String toString() {
 		return "Measurement(" + getPtuId() + "): name=" + getName() + " value="
 				+ getValue() + " unit=" + getUnit() + " date: " + getDate();
 	}
 
 }

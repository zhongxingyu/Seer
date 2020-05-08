 package storage.dight;
 
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 import se.sics.dight.data.model.EntryId;
 import se.sics.dight.data.model.Objekt;
 import se.sics.dight.data.model.values.DateValue;
 import se.sics.dight.data.model.values.DoubleValue;
 import se.sics.dight.data.model.values.Value;
 
 public class MeasurementRecord extends BaseRecord{
 
 	private static MeasurementKlass klassContainer;
 
 	public MeasurementRecord(){
 		super();
 		klassContainer = new MeasurementKlass(e);
 		record = klassContainer.getKlass();
 	}
 	
 	public String newEntry(String measurement, String unit, Double value, String patientId){
 		klassContainer = new MeasurementKlass(e);
 		Value vMeasurement = e.makeStringValue(measurement);
 		Value vUnit = e.makeStringValue(unit);
 		Value vValue = e.makeDoubleValue(value);
 		Value vPatientId = e.makeStringValue(patientId);
 		Value vDate = e.makeDateValue(new Date());
 
 		//Make an Entry id
 		int byteval=1;
 		byte[] byts = new byte[]{(byte)(byteval >>> 24),(byte)(byteval >>> 16),(byte)(byteval >>> 8),(byte)byteval,(byte)(byteval >>> 24),(byte)(byteval >>> 16),(byte)(byteval >>> 8),(byte)byteval,(byte)(byteval >>> 24),(byte)(byteval >>> 16),(byte)(byteval >>> 8),(byte)byteval,(byte)(byteval >>> 24),(byte)(byteval >>> 16),(byte)(byteval >>> 8),(byte)byteval,(byte)(byteval >>> 24),(byte)(byteval >>> 16),(byte)(byteval >>> 8),(byte)byteval};
 		EntryId eid = e.makeEntryId(byts);
 	
 		
 		Objekt entry = e.createObjekt(klassContainer.getKlass(), eid, byts);
 		entry.setValue(klassContainer.patientId, vPatientId);
 		entry.setValue(klassContainer.date, vDate);
 		entry.setValue(klassContainer.measurement, vMeasurement);
 		entry.setValue(klassContainer.unit, vUnit);
 		entry.setValue(klassContainer.value, vValue);
 		entry.commit(klassContainer.getEntryAuthenticationAlgorithm());
 		//Send to server
 		return eid.getId().toString();
 	}
 	
 	public Set<Measurement> getMeasurement(String patientId, String measurement){
 		HashMap<String, Object> h = new HashMap<String, Object>();
 		Set<HashMap> s = new HashSet<HashMap>();
		Value vMeasurement = e.makeStringValue(measurement);
 		h.put("attribute", klassContainer.measurement);
 		h.put("value", vMeasurement);
 		s.add(h);		
		Value vPatientId = e.makeStringValue(patientId);
 		h.put("attribute", klassContainer.patientId);
		h.put("value", patientId);
 		s.add(h);
 		
 		//Retrive result
 		Set<Objekt> objekts = attributesQueryToObjekt(s);
 		Set<Measurement> ret = new TreeSet<Measurement>(new MeasurementComparator());		
 		for(Objekt o : objekts){
 			Measurement m = new Measurement();
 			m.setDate( ((DateValue) o.getValue(klassContainer.date)).getValue());
 			m.setValue( ((DoubleValue)o.getValue(klassContainer.value)).getValue() );
 			m.setUnit( o.getValue(klassContainer.unit).toString());
 			m.setMeasurement(measurement);
 			m.setPatientId(patientId);
 			ret.add(m);
 		}
 		return ret;
 	}
 	
 	
 	public class Measurement{
 		private Date date;
 		private String patientId;
 		private Double value;
 		private String measurement;
 		private String unit;
 		
 		public Date getDate() {
 			return date;
 		}
 		public void setDate(Date date) {
 			this.date = date;
 		}
 		public String getPatientId() {
 			return patientId;
 		}
 		public void setPatientId(String patientId) {
 			this.patientId = patientId;
 		}
 		public String getMeasurement() {
 			return measurement;
 		}
 		public void setMeasurement(String measurement) {
 			this.measurement = measurement;
 		}
 		public Double getValue() {
 			return value;
 		}
 		public void setValue(Double value) {
 			this.value = value;
 		}
 		public String getUnit() {
 			return unit;
 		}
 		public void setUnit(String unit) {
 			this.unit = unit;
 		}	
 	}	
 
 	private class MeasurementComparator implements Comparator<Measurement>{
 		public int compare(Measurement m1, Measurement m2){
 			if(m1.getDate().before(m2.getDate())){
 				return 1;
 			}
 			return 0;
 		}
 	}
 }

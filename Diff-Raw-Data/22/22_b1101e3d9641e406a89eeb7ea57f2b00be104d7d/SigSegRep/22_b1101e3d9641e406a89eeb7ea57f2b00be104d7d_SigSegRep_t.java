 /**
  * Simply a data structure representing a Signal Segment.
  */
 package edu.ucla.cens.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import javax.microedition.location.LocationProvider;
 
 public class SigSegRep {
 	/** The id of the record. -1 means it's not been set. */
 	public int id = -1;
 
 	/** The time stamp of the record, in miliseconds. -1 means it's not been set. */
 	public long timeMS = -1;
 
 	/** The data associated with the record. */
 	public double lat = 0;
 
 	public double lon = 0;
 
 	public Boolean isValid = new Boolean(false);
 
 	public Integer lpstate = new Integer(LocationProvider.OUT_OF_SERVICE);
 
 	public Float alt = new Float(Float.NaN);
 
 	public Float horizontal_accuracy = new Float(Float.NaN);
 
 	public Float vertical_accuracy = new Float(Float.NaN);
 
 	public Float course = new Float(Float.NaN);
 
 	public Float speed = new Float(Float.NaN);
 
 	public Long timestamp = new Long(0);
 
 	/**
 	 * This constructor doesn't do anything.
 	 */
 	public SigSegRep() {
 	}
 
 	/**
 	 * Constructor that just takes an id.
 	 * 
 	 * @param id -
 	 *            ID of corresponding record ID in some record store.
 	 */
 	// public SigSegRep(int id) {
 	// this(id, (long) -1, 0, 0);
 	// }
 	/**
 	 * Constructor that uses specified id, time-stamp, and data.
 	 * 
 	 * @param id -
 	 *            the record id. -1 means not initialized.
 	 * @param timeMS -
 	 *            the timestamp. -1 means not initialized.
 	 * @param data -
 	 *            the data. NULL means not initialized.
 	 */
 	public SigSegRep(int id, long timeMS, Boolean isValid, Integer lpstate,
 			double lat, double lon, Float alt, Float horizontal_accuracy,
 			Float vertical_accuray, Float course, Float speed, Long timestamp) {
 		this.id = id;
 		this.timeMS = timeMS;
 		this.isValid = isValid;
 		this.lpstate = lpstate;
 		this.lat = lat;
 		this.lon = lon;
 		this.alt = alt;
 		this.horizontal_accuracy = horizontal_accuracy;
 		this.vertical_accuracy = vertical_accuray;
 		this.course = course;
 		this.speed = speed;
 		this.timestamp = timestamp;
 	}
 
 	/**
 	 * Constructor that uses specified id and byte[] rep. of SigSegRep.
 	 * 
 	 * @param id -
 	 *            the record id. -1 means not initialized.
 	 * @param record -
 	 *            a byte[] representing the information held in a record store.
 	 * @throws IOException -
 	 *             if unable to unmarshal from the record.
 	 */
 	public SigSegRep(int id, byte[] record) throws IOException {
 		this.id = id;
 		this.fromByteArray(record);
 	}
 
 	/**
 	 * Modifies this to take on values of the fields contained in record.
 	 * 
 	 * @param record -
 	 *            The byte[] representation of the sig seg.
 	 * @throws IOException -
 	 *             If there's a problem with unmarshalling the data.
 	 * @return void
 	 */
 	public void fromByteArray(byte[] record) throws IOException {
 		ByteArrayInputStream byteIn = new ByteArrayInputStream(record);
 		DataInputStream dataIn = new DataInputStream(byteIn);
 		this.timeMS = dataIn.readLong();
 		this.isValid = new Boolean(dataIn.readBoolean());
 		this.lpstate = new Integer(dataIn.readInt());
 		this.lat = dataIn.readDouble();
 		this.lon = dataIn.readDouble();
 		this.alt = new Float(dataIn.readFloat());
 		this.horizontal_accuracy = new Float(dataIn.readFloat());
 		this.vertical_accuracy = new Float(dataIn.readFloat());
 		this.course = new Float(dataIn.readFloat());
 		this.speed = new Float(dataIn.readFloat());
 		this.timestamp = new Long(dataIn.readLong());
 
 		dataIn.close();
 		byteIn.close();
 	}
 
 	/**
 	 * Returns an XML representation of this, appropriate for upload to
 	 * SensorBase. Note that it needs to be URL encoded as part of upload.
 	 * 
 	 * @return String - XML representation of SigSegRep.
 	 */
 	public String toXML() {
 		StringBuffer result = new StringBuffer();
 		
 		result.append(this
 				.createField("recordTableID", String.valueOf(this.id)));
 		
 		String str_date = this.toDateStr();
 		result.append(this.createField("date", str_date));
 		
 		int isValidInt = 0;
 		if (this.isValid.booleanValue()) { 
 			isValidInt = 1; 
 		}
 		result.append(this.createField("isValid", String.valueOf(isValidInt)));
 	
 		if (this.lpstate != null) {
 			result.append(this.createField("LPState", this.lpstate.toString()));
 		}
 		
 		result.append(this.createField("lat", String.valueOf(this.lat)));
 		
 		result.append(this.createField("lon", String.valueOf(this.lon)));
 		
 		if ((this.alt != null) && (!this.alt.isNaN())) {
 			result.append(this.createField("alt", this.alt.toString()));
 		}
 		
 		if ((this.horizontal_accuracy != null) && (!this.horizontal_accuracy.isNaN())) {
 			result.append(this.createField("horizontalAccuracy", this.horizontal_accuracy.toString()));
 		}
 		
 		if ((this.vertical_accuracy != null) && (!this.vertical_accuracy.isNaN())) {
 			result.append(this.createField("verticalAccuracy", this.vertical_accuracy.toString()));
 		}
 		
 		if ((this.course != null) && (!this.course.isNaN())) {
 			result.append(this.createField("course", this.course.toString()));
 		}
 		
 		if ((this.speed != null) && (!this.speed.isNaN())) {
 			result.append(this.createField("speed", this.speed.toString()));
 		}
 		
 		if ((this.timestamp != null) && (this.timestamp.longValue() > 0)) {
 			result.append(this.createField("timestamp", this.timestamp.toString()));
 		}
 		return result.toString();
 	}
 
 	/**
 	 * @return String that represents date and time as YYYY-MM-DD HH:MM:SS
 	 */
 	private String toDateStr() {
 		java.util.Date date = new java.util.Date(this.timeMS);
 		java.util.Calendar cal = java.util.Calendar.getInstance();
 		cal.setTime(date);
 		String year = String.valueOf(cal.get(java.util.Calendar.YEAR));
 		String month = String.valueOf(cal.get(java.util.Calendar.MONTH));
 		String day = String.valueOf(cal.get(java.util.Calendar.DAY_OF_MONTH));
 		String hour = String.valueOf(cal.get(java.util.Calendar.HOUR_OF_DAY));
 		String minute = String.valueOf(cal.get(java.util.Calendar.MINUTE));
 		String second = String.valueOf(cal.get(java.util.Calendar.SECOND));
 		String str_date = year + "-" + month + "-" + day + " " + hour + ":"
 				+ minute + ":" + second;
 		return str_date;
 	}
 
//	public void conditionalWriteFloat(DataOutputStream dataOut, Float f) {
//		if ((f!=null) && (!f.isNaN())) {
//			dataOut.writeBoolean(true);
//			dataOut.writeFloat(f);
//		} else {
//			dataOut.writeBoolean(false);
//		}
//	}
//	
	
	
 	/**
 	 * Returns a byte[] representation of this.
 	 * 
 	 * @return A byte[] representation of this.
 	 * @throws IOException
 	 */
 	public byte[] toByteArray() throws IOException {
 		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 		DataOutputStream dataOut = new DataOutputStream(byteOut);
 		try {
 			dataOut.writeLong(this.timeMS);
 			dataOut.writeBoolean(this.isValid.booleanValue());
 			dataOut.writeInt(this.lpstate.intValue());
 			dataOut.writeDouble(this.lat);
 			dataOut.writeDouble(this.lon);
//			this.conditionalWriteFloat(dataOut, this.alt);
//			this.conditionalWriteFloat(dataOut, this.horizontal_accuracy);
//			this.conditionalWriteFloat(dataOut, this.vertical_accuracy);
//			this.conditionalWriteFloat(dataOut, this.course);
//			this.conditionalWriteFloat(dataOut, this.speed);
			
 			dataOut.writeFloat(this.horizontal_accuracy.floatValue());
 			dataOut.writeFloat(this.vertical_accuracy.floatValue());
 			dataOut.writeFloat(this.course.floatValue());
 			dataOut.writeFloat(this.speed.floatValue());
			dataOut.writeLong(this.timestamp.longValue());
 			byte[] result = byteOut.toByteArray();
 			dataOut.close();
 			byteOut.close();
 			return result;
 		} catch (IOException e) {
 			dataOut.close();
 			byteOut.close();
 			throw e;
 		}
 	}
 
 	/**
 	 * Helper function to create field strings, used by toXML().
 	 * 
 	 * @param name
 	 *            The name of the field.
 	 * @param val
 	 *            The value of the field.
 	 * @return A string with the field row filled out.
 	 */
 	private String createField(String name, String val) {
 		// <field name="name">VAL</field>
 		String result = "<field name=\"" + name + "\">" + val + "</field>\n";
 		return result;
 	}
 
 }

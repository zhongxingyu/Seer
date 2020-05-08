 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 //NOTE: implements IsSerializable in case serialization file cannot be found
 public class Data implements Serializable, IsSerializable {
 
 	private static final long serialVersionUID = 2802095781867809709L;
 
 	private Device device;
 	private String name;
 	private Number[][] data;
 	private int index;
 	private String unit;
 	private int maxEntries;
 
 	private final static int INITIAL_CAPACITY = 200;
 	private final static int INCREASE_CAPACITY = 200;
 	private final static int TIME = 0;
 	private final static int VALUE = 1;
 	private final static int LOW_LIMIT = 2;
 	private final static int HIGH_LIMIT = 3;
 	private final static int SAMPLING_RATE = 4;
 
 	private final static int SIZE = SAMPLING_RATE + 1;
 	private final static Map<String, Double> threshold;
 
 	static {
 		threshold = new HashMap<String, Double>();
 		threshold.put("BarometricPressure", 0.1);
 		threshold.put("BatteryLevel", 0.5);
 		threshold.put("BodyTemperature", 0.1);
 		threshold.put("CO2", 10.0);
 		threshold.put("DoseAccum", 1.0);
 		threshold.put("DoseRate", 0.5);
 		threshold.put("Heartbeat", 1.0);
 		threshold.put("Humidity", 0.5);
 		threshold.put("O2", 0.1);
 		threshold.put("Temperature", 0.5);
 	}
 
 	protected Data() {
 	}
 
 	public Data(Device device, String name, String unit, int maxEntries) {
 		this.device = device;
 		this.name = name;
 		this.unit = unit;
 		this.data = new Number[INITIAL_CAPACITY][SIZE];
 		this.maxEntries = maxEntries;
 		index = 0;
 	}
 
 	public Device getPtu() {
 		return device;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Number[][] getData() {
 		Number[][] result = new Number[index][2];
 
 		// FIXME #4 maybe needs handcopy, seems to work
 		System.arraycopy(data, 0, result, 0, index);
 		return result;
 	}
 
 	public Number[][] getLimits() {
 		Number[][] result = new Number[index][3];
 
 		for (int i = 0; i < index; i++) {
 			result[i][TIME] = data[i][TIME];
 			result[i][1] = data[i][LOW_LIMIT];
 			result[i][2] = data[i][HIGH_LIMIT];
 		}
 		return result;
 	}
 
 	public String getUnit() {
 		return unit;
 	}
 	
 	public int getMaxEntries() {
 		return maxEntries;
 	}
 
 	public boolean addEntry(long time, Number value, Number lowLimit,
 			Number highLimit, Integer samplingRate) {
 		// FIXME handle MaxEntries
 		
 		// add the new value
 		if (index >= data.length) {
 			Number[][] newData = new Number[data.length + INCREASE_CAPACITY][SIZE];
 			System.arraycopy(data, 0, newData, 0, data.length);
 			data = newData;
 		}
 
 		data[index][TIME] = time;
 		data[index][VALUE] = value;
 		data[index][LOW_LIMIT] = lowLimit;
 		data[index][HIGH_LIMIT] = highLimit;
 		data[index][SAMPLING_RATE] = samplingRate;
 
 		// fix for #502
 		if (index > 1) {
 			// keep at least two values, forward the last one...
 			Number[] beforeLast = data[index - 2];
 			Number[] last = data[index - 1];
 			Number[] current = data[index];
 			if (equals(beforeLast, last) && equals(beforeLast, current)) {
 				// same value, just forward time
 				last[TIME] = time;
 				return false;
 			}
 		}
 
 		// make the new value real
 		index++;
 
 		return true;
 	}
 
 	private double getThreshold(String sensor) {
 		Double t = threshold.get(sensor);
 		if (t == null) {
 			System.err.println("Cannot find threshold for '"+sensor+"'");
 		}
 		return t != null ? t : 0.1;
 	}
 
	// equals when value withing threshold, limits equal, samplingrate equal.
 	private boolean equals(Number[] m1, Number[] m2) {
 		return (Math.abs(m1[VALUE].doubleValue() - m2[VALUE].doubleValue()) < getThreshold(name))
				&& m1[LOW_LIMIT].equals(m2[LOW_LIMIT])
				&& m1[HIGH_LIMIT].equals(m2[HIGH_LIMIT])
				&& m1[SAMPLING_RATE].equals(m2[SAMPLING_RATE]);
 	}
 
 	public Measurement getMeasurement() {
 		int last = index - 1;
 		return index == 0 ? null : new Measurement(device, name,
 				data[last][VALUE].doubleValue(), data[last][LOW_LIMIT].doubleValue(),
 				data[last][HIGH_LIMIT].doubleValue(), unit,
 				data[last][SAMPLING_RATE].intValue(), new Date(
 						data[last][TIME].longValue()));
 	}
 
 	public int getSize() {
 		return index;
 	}
 }

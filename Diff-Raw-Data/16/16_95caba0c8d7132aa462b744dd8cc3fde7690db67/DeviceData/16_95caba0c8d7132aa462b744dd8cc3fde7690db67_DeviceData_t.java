 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class DeviceData implements Serializable, IsSerializable {
 
 	private static final long serialVersionUID = 2719767332984866247L;
 	private Device device;
 	private Map<String, Data> map = new HashMap<String, Data>();
 	
 	protected DeviceData() {
 	}
 	
 	public DeviceData(Device device) {
 		this.device = device;
 	}
 	
 	public Device getDevice() {
 		return device;
 	}
 
 	public Data get(String sensor) {
 		return map.get(sensor);
 	}
	
	public Set<String> getSensors() {
		return map.keySet();
	}
 
 	public void put(Data data) {
 		if (data.getPtu() != device) {
 			throw new RuntimeException("Device '"+device+"' table cannot accept entry from '"+data.getPtu());
 		}
 		map.put(data.getName(), data);
 	}
 	
 	public List<Measurement> getMeasurements() {
 		List<Measurement> list = new ArrayList<Measurement>(map.size());
 		for (Data data: map.values()) {
 			list.add(data.getMeasurement());
 		}
 		return list;
 	}
 }

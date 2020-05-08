 package ch.cern.atlas.apvs.client.ui;
 
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.Measurement;
 
 @SuppressWarnings("serial")
 public class NullMeasurement extends Measurement {
 
 	public NullMeasurement(Device device, String name) {
		super(device, name, null, null, null, null, null, null);
 	}
 }

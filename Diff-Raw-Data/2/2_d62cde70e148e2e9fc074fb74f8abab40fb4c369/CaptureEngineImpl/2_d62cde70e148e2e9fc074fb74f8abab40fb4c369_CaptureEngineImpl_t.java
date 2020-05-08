 package ccnt.gf.imonitor.engine.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jnetpcap.Pcap;
 import org.jnetpcap.PcapIf;
 
 import ccnt.gf.imonitor.engine.CaptureEngine;
 import ccnt.gf.imonitor.engine.DevInfo;
 
 public class CaptureEngineImpl
 	implements CaptureEngine{
 
 	@Override
 	public List<DevInfo> findAllDevs() {
 		List<PcapIf> alldevs = new ArrayList<PcapIf>();
 		StringBuilder errbuf = new StringBuilder();
 		List<DevInfo> devs = new ArrayList<DevInfo>();
 		int r = Pcap.findAllDevs(alldevs, errbuf);
 		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
 			System.err.printf("Cannot read list of devices, error is %s\n",
 					errbuf.toString());
 			return null;
 		}
 		
 		int index = 0;
 		for (PcapIf device : alldevs)
			devs.add(new DevInfo(index++, device.getName(), device.getDescription()));
 		return devs;
 	}
 	
 }

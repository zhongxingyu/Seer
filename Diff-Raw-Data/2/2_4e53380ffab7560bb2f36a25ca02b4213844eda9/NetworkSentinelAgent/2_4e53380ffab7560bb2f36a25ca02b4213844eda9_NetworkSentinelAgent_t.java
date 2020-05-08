 
 package com.zanoccio.axirassa.services.sentinel;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.hyperic.sigar.NetInterfaceStat;
 import org.hyperic.sigar.SigarException;
 
 public class NetworkSentinelAgent extends AbstractSentinelStatisticsAgent {
 
 	private final ArrayList<SentinelStatistic> networkstats = new ArrayList<SentinelStatistic>();
 	private final HashMap<String, NetworkIOSnapshot> previoussnapshots = new HashMap<String, NetworkIOSnapshot>();
 
 
 	@Override
 	public void execute() throws SigarException {
 		networkstats.clear();
 
 		String[] netinterfaces = getSigar().getNetInterfaceList();
 		networkstats.ensureCapacity(netinterfaces.length);
 		for (String netinterface : netinterfaces) {
 			NetInterfaceStat stat = getSigar().getNetInterfaceStat(netinterface);
 			String interfacename = getSigar().getNetInterfaceConfig(netinterface).getDescription();
 
 			NetworkIOSnapshot current = new NetworkIOSnapshot();
 			current.date = getDate();
 			current.rxbytes = stat.getRxBytes();
 			current.txbytes = stat.getTxBytes();
 
 			networkstats.add(new NetworkStatistic(getMachineID(), getDate(), interfacename, current.txbytes,
 			        current.rxbytes));
 
			NetworkIOSnapshot previous = previoussnapshots.get(interfacename);
 			if (previous != null) {
 				long millis = getDate().getTime() - previous.date.getTime();
 				long seconds = millis / 1000;
 
 				long rxrate = (current.rxbytes - previous.rxbytes) / seconds;
 				long txrate = (current.txbytes - previous.txbytes) / seconds;
 
 				networkstats.add(new NetworkIOStatistic(getMachineID(), getDate(), interfacename, rxrate, txrate));
 			}
 
 			previoussnapshots.put(interfacename, current);
 		}
 
 	}
 
 
 	@Override
 	public Collection<SentinelStatistic> getStatistics() {
 		return networkstats;
 	}
 
 }
 
 class NetworkIOSnapshot {
 	Date date;
 	long rxbytes;
 	long txbytes;
 }

 package de.hpi_web.cloudSim.profiling.example;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.Datacenter;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 
 import arx.ARX;
 
 import de.hpi_web.cloudSim.multitier.MultiTierCloudlet;
 import de.hpi_web.cloudSim.multitier.staticTier.CloudletFactory;
 import de.hpi_web.cloudSim.multitier.staticTier.VmFactory;
 import de.hpi_web.cloudSim.profiling.datacenter.DatacenterBuilder;
 import de.hpi_web.cloudSim.profiling.datacenter.ProfilingBroker;
 import de.hpi_web.cloudSim.profiling.utilization.UtilManager;
 
 public class SimpleExample {
 	
 	public static void main(String[] args) {
 		
 		Log.printLine("Starting...");
 		initializeCloudSim();
 		
 		Datacenter wsDatacenter = DatacenterBuilder.createDatacenter("WebserverCenter", 0, 3);
 		
 		ProfilingBroker wsBroker = createBroker("wsBroker");
 		List<ProfilingBroker> brokers = new ArrayList<ProfilingBroker>();
 		brokers.add(wsBroker);
 		
		List<Vm> wsVms = VmFactory.createVms(0, 1, wsBroker.getId());
 		
 		// submit vm lists to the brokers
 		wsBroker.submitVmList(wsVms);
 
 		List<List<Double>> cpuValues = ARX.predictCPUUsage("training.csv", "running.csv");
 		HashMap<DatacenterBroker, List<Double>> layers = new HashMap<DatacenterBroker, List<Double>>();
 		int index = 1;			// we dont start at 0, this is the LoadBalancer which we do not track atm
 		for (DatacenterBroker broker : brokers) {
 			layers.put(broker, cpuValues.get(index));
 		}
 		UtilManager utilManager = new UtilManager("UtilManager", 1, 70, 30, layers);
 		
 		CloudSim.startSimulation();
 		CloudSim.stopSimulation();
 
 		//Print results
 		List<Cloudlet> wsList = wsBroker.getCloudletReceivedList();
 		Log.printLine("Simulation finished!");
 		
 	}
 	
 	public static void initializeCloudSim() {
 		int num_user = 1; // number of cloud users
 		Calendar calendar = Calendar.getInstance();
 		boolean trace_flag = false; // mean trace events
 		
 		// Initialize the CloudSim library
 		CloudSim.init(num_user, calendar, trace_flag);
 	}
 
 	/**
 	 * Creates the broker.
 	 *
 	 * @return the datacenter broker
 	 */
 	private static ProfilingBroker createBroker(String brokerId) {
 		ProfilingBroker broker = null;
 		try {
 			broker = new ProfilingBroker(brokerId);
 			//broker = new DatacenterBroker(brokerId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return broker;
 	}
 	
 }

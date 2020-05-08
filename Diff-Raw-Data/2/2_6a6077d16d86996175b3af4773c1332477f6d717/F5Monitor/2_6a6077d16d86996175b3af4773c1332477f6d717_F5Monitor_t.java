 /**
  * Copyright 2013 AppDynamics
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 
 package com.appdynamics.monitors.f5;
 
 import iControl.CommonStatistic;
 import iControl.CommonULong64;
 import iControl.SystemStatisticsBindingStub;
 import iControl.SystemStatisticsHostStatisticEntry;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
 import com.singularity.ee.agent.systemagent.api.MetricWriter;
 import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
 import com.singularity.ee.agent.systemagent.api.TaskOutput;
 import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
 
 public class F5Monitor extends AManagedMonitor
 {
 
 	private Logger logger;
 	private SystemStatisticsBindingStub stats;
 	private iControl.Interfaces m_interfaces;
 	private List<String> monitoredPoolMembers;
 	private List<String> poolMemberMetrics = new ArrayList<String>();
 	private Map<String, String> poolMemberIPToName;
     private boolean isVersion11 = false;
 
 	private String metricPath = "Custom Metrics|";
 
 	/**
 	 * The main method is only for debugging purposes. If you would like to know
 	 * whether or not the metric retrieval works with the credentials you provide in
 	 * monitor.xml, you can simply run this java file and provide the credentials as
 	 * arguments.
 	 * @param args
 	 */
 	public static void main(String args[])
 	{
 
 		F5Monitor monitor = new F5Monitor();
 		monitor.monitoredPoolMembers = new ArrayList<String>();
 		monitor.poolMemberIPToName = new HashMap<String, String>();
 
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_BYTES_IN");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_BYTES_OUT");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_PACKETS_IN");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_PACKETS_OUT");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_CURRENT_CONNECTIONS");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_MAXIMUM_CONNECTIONS");
 		monitor.poolMemberMetrics.add("STATISTIC_SERVER_SIDE_TOTAL_CONNECTIONS");
 		monitor.poolMemberMetrics.add("STATISTIC_TOTAL_REQUESTS");
 
 
 		if(args.length != 3 && args.length != 4){
 			System.err.println("You have to provide three arguments in the correct order " +
 					"(IP, Username, Password)! In addition, as a 4th argument, you can provide a comma-separated" +
 					"list of pool member names you want to be monitored (no whitespaces in between).");
 			return;
 		}
 
 		if(args.length == 4){
 			String[] poolMembers = args[3].split(",");			
 			for(String poolMember : poolMembers){
 				System.out.println(poolMember);
 				monitor.monitoredPoolMembers.add(poolMember);
 			}
 		}
 
 		try{
 			monitor.m_interfaces = new iControl.Interfaces();
 			monitor.m_interfaces.initialize(args[0], args[1], args[2]);
 
 			if(!monitor.areCredentialsValid()){
 				System.out.println("The credentials you provided to the F5 monitor are invalid." +
 						" Terminating Monitor.");
 				return;
 			}
 
 			SystemStatisticsBindingStub stats = monitor.m_interfaces.getSystemStatistics();
 			System.out.println("-----GET MEMORY STATISTICS-----");
 			for (SystemStatisticsHostStatisticEntry stat : stats.get_all_host_statistics().getStatistics())
 			{
 				for (CommonStatistic st : stat.getStatistics())
 				{
 					if(st.getType().toString().equals("STATISTIC_MEMORY_TOTAL_BYTES") ||
 							st.getType().toString().equals("STATISTIC_MEMORY_USED_BYTES")){
 						System.out.println(st.getType() + " : " + monitor.new UsefulU64(st.getValue()).doubleValue());
 					}
 				}
 			}
 
 			System.out.println("-----GET CPU STATISTICS-----");
 			int count = 0;
 			int val = 0;
 			for (iControl.SystemCPUUsageExtended usage : monitor.m_interfaces.getSystemSystemInfo().get_all_cpu_usage_extended_information().getHosts())
 			{
 				for(CommonStatistic[] stats2 : usage.getStatistics()){
 					for(CommonStatistic stat : stats2){
 						if(stat.getType().toString().equals("STATISTIC_CPU_INFO_ONE_MIN_AVG_IDLE")){
 							count ++;
 							val += 100 - monitor.new UsefulU64(stat.getValue()).doubleValue();
 						}
 
 					}
 				}
 				if(count != 0){
 					System.out.println("CPU % BUSY : " + val / count);
 				}
 			}
 
 
 			System.out.println("-----GET CLIENT SSL STATISTICS-----");
 			for (CommonStatistic stat : stats.get_client_ssl_statistics().getStatistics())
 			{
 				if(stat.getType().getValue().equals("STATISTIC_SSL_COMMON_CURRENT_CONNECTIONS")){
 					System.out.println(stat.getType() + " : " + monitor.new UsefulU64(stat.getValue()).doubleValue());	
 				}
 			}
 
 
 
 			System.out.println("-----GET TCP STATISTICS-----");
 			for (CommonStatistic stat : stats.get_tcp_statistics().getStatistics())
 			{
 				if(stat.getType().getValue().equals("STATISTIC_TCP_OPEN_CONNECTIONS") ||
 						stat.getType().getValue().equals("STATISTIC_TCP_CLOSE_WAIT_CONNECTIONS") ||
 						stat.getType().getValue().equals("STATISTIC_TCP_ESTABLISHED_CONNECTIONS")){
 					System.out.println(stat.getType() + " : " + monitor.new UsefulU64(stat.getValue()).doubleValue());
 				}
 			}
 
 
 			//monitor.printPoolMemberStats();
 			//monitor.getPoolMemberStatus();
 
 		} catch (RemoteException e) {
 			System.out.println("Could not retrieve metrics: " + e.getMessage() +
 					"... Aborted metrics retrieval.");
 			e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Unable to connect to the F5 or initialize stats retrieval. Error Message: " +
 					"" + e.getMessage() + "\n\nTerminating monitor.");
 		}
 
 	}
 
 	/**
 	 * used for main method (debugging)
 	 * @throws Exception
 	 */
 	public void printPoolMemberStats() throws Exception
 	{
 		String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 		List<String> NodeNamesList = new ArrayList<String>();
 		iControl.LocalLBPoolMemberStatistics[] memberStats;
 
 		memberStats = m_interfaces.getLocalLBPool().get_all_member_statistics(pool_list);
 		for(iControl.LocalLBPoolMemberStatistics memberStatistics:memberStats){
 			iControl.LocalLBPoolMemberStatisticEntry[] memberStatsEntries = memberStatistics.getStatistics();
 
 			for(iControl.LocalLBPoolMemberStatisticEntry memberStatsEntry:memberStatsEntries){
 				iControl.CommonStatistic[] stats = memberStatsEntry.getStatistics();
 				System.out.println("   " + memberStatsEntry.getMember().getAddress() + " =");
 				NodeNamesList.add(memberStatsEntry.getMember().getAddress()); // USE IF YOU WANT TO SEE OUTPUT OF ALL POOLMEMBERS
 				if(isSupposedToBeMonitored(memberStatsEntry.getMember().getAddress())){
 					//NodeNamesList.add(memberStatsEntry.getMember().getAddress()); // USE IF YOU WANT TO SEE OUTPUT OF SPECIFIED POOLMEMBERS
 					for(iControl.CommonStatistic stat : stats){
 						if(this.poolMemberMetrics.contains(stat.getType().getValue())){
 							System.out.println("    " + "Pool Members" + memberStatsEntry.getMember().getAddress().replaceAll("/", "|") + "|" + stat.getType().getValue() + ": " + (new UsefulU64(stat.getValue()).doubleValue()));							
 						}
 					}
 				}
 			}
 		}
 
 		String[] NodeNamesArray = new String[1];
 		NodeNamesArray= NodeNamesList.toArray(NodeNamesArray);
 		String[] IPAddressesArray = m_interfaces.getLocalLBNodeAddressV2().get_address(NodeNamesArray);
 		for(int i = 0; i < IPAddressesArray.length; i++){
 			System.out.println(IPAddressesArray[i] + " TO " + NodeNamesArray[i]);
 			poolMemberIPToName.put(IPAddressesArray[i], NodeNamesArray[i]);
 		}
 
 	}
 
 	/**
 	 * used for main method (debugging)
 	 * @throws Exception
 	 */
 	public void getPoolMemberStatus() throws Exception
 	{
 		String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 
 		iControl.LocalLBPoolMemberMemberObjectStatus [][] objStatusAofA = 
 				m_interfaces.getLocalLBPoolMember().get_object_status(pool_list);
 
 		for(String poolMemberIPToNameEntry : poolMemberIPToName.keySet()){
 			System.out.println("KEY: " + poolMemberIPToNameEntry);
 		}
 
 		for(iControl.LocalLBPoolMemberMemberObjectStatus [] objStatusA : objStatusAofA)
 		{
 
 
 			for(int i=0; i<objStatusA.length; i++)
 			{
 				String IPAddress = objStatusA[i].getMember().getAddress();
 				if (poolMemberIPToName.containsKey(IPAddress)){
 					iControl.LocalLBObjectStatus objStatus = objStatusA[i].getObject_status();
 					iControl.LocalLBAvailabilityStatus availability = objStatus.getAvailability_status();
 					iControl.LocalLBEnabledStatus enabled = objStatus.getEnabled_status();
 					String description = objStatus.getStatus_description();
 					System.out.println("Member " + poolMemberIPToName.get(IPAddress) + " status:");
 					System.out.println("  Availability : " + availability.getValue());
 					System.out.println("  Enabled      : " + enabled.getValue());
 					System.out.println("  Description  : " + description);
 
 					int status;
 
 					if(availability.getValue().contains("GREEN") && enabled.getValue().contains("STATUS_ENABLED")){
 						status = 4; // Available (Enabled)
 					} else if(availability.getValue().contains("RED") && enabled.getValue().contains("STATUS_ENABLED")){
 						status = 3; // Offline (Enabled)
 					} else if(availability.getValue().contains("GREEN") && enabled.getValue().contains("STATUS_DISABLED")){
 						status = 2; // Available (Disabled)
 					} else if(availability.getValue().contains("RED") && enabled.getValue().contains("STATUS_DISABLED")){
 						status = 1; // Offline (Disabled)
 					} else {
 						status = 5; // UNKNOWN
 					}
 
 					System.out.println("  Pool Members" + poolMemberIPToName.get(IPAddress).replaceAll("/", "|") +  "|STATUS LIGHT =" + status);
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * used for main method (debugging)
 	 * @param poolMemberCandidate - used to check if this one is in the list of monitored pool members
 	 * @return
 	 */
 	private boolean isSupposedToBeMonitored(String poolMemberCandidate){
 		for(String poolMember : monitoredPoolMembers){
 			if(poolMemberCandidate.substring(poolMemberCandidate.lastIndexOf('/') + 1, poolMemberCandidate.length()).equals(poolMember)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * execution method used by the machine agent
 	 */
 	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext) throws TaskExecutionException
 	{
 		logger = Logger.getLogger(F5Monitor.class);
 		poolMemberIPToName = new HashMap<String, String>();
 		monitoredPoolMembers = new ArrayList<String>();
 
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_BYTES_IN");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_BYTES_OUT");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_PACKETS_IN");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_PACKETS_OUT");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_CURRENT_CONNECTIONS");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_MAXIMUM_CONNECTIONS");
 		poolMemberMetrics.add("STATISTIC_SERVER_SIDE_TOTAL_CONNECTIONS");
 		poolMemberMetrics.add("STATISTIC_TOTAL_REQUESTS");
 
 		if(!taskArguments.containsKey("Hostname") || !taskArguments.containsKey("Username") || !taskArguments.containsKey("Password")){
 			logger.error("monitor.xml needs to contain these three arguments: \"Hostname\", \"Username\", and \"Password\". " +
 					"Terminating monitor.");
 			return null;
 		}
 
 		try{
 			//connecting to F5, initializing statistics retrieval
 			m_interfaces = new iControl.Interfaces();
 			m_interfaces.initialize(taskArguments.get("Hostname"), taskArguments.get("Username"), taskArguments.get("Password"));
 
 			// check if the credentials provided are valid by trying to fetch some
 			// arbitrarily chosen statistics.
 			if(!areCredentialsValid()){
 				logger.error("The credentials you provided to the F5 Monitor are invalid." +
 						" Terminating Monitor.");
 				return null;
 			}
 
             String version = m_interfaces.getSystemServices().get_version();
             logger.info("BigIP version: " + version);
            if (version.startsWith("BIG-IP_v11.")) {
                 isVersion11 = true;
             }
 
 			printAllPoolMembers();
 			
 			stats = m_interfaces.getSystemStatistics();	
 			
 			// fill the Arraylist with poolmembers by excluding the pools listed in monitor.xml
 			String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 			monitoredPoolMembers.addAll(Arrays.asList(pool_list));
 			if(taskArguments.containsKey("pools-exclude") &&
 					null != taskArguments.get("pools-exclude") && !taskArguments.get("pools-exclude").equals(""))
 			{
 				String [] poolsExclude = taskArguments.get("pools-exclude").split(",");
 				for(String poolExclude : poolsExclude) {
 					Pattern pattern = Pattern.compile(poolExclude);
 					for(Iterator<String> pool = monitoredPoolMembers.iterator(); pool.hasNext();){
 						Matcher matcher = pattern.matcher(pool.next());
 						if(matcher.find())
 						{
 							pool.remove();
 						}
 					}
 				}
 			}
 			
 			// see if there is a custom metric path in monitor.xml
 			if(taskArguments.containsKey("metric-path") && !taskArguments.get("metric-path").equals("")){
 				metricPath = taskArguments.get("metric-path");
 				logger.debug("Metric path: " + metricPath);
 				if(!metricPath.endsWith("|")){
 					metricPath += "|";
 				}
 			}
 
 		} catch(Throwable t){
 			logger.error("Unable to connect to the F5 or initialize stats retrieval. Error Message: " +
 					"" + t.getMessage() + "... Terminating monitor.");
 			return null;
 		}
 
 		// executing task to retrieve and report F5 metrics, once per minute
 		while(true){
 			(new PrintMetricsThread()).start();
 			try {
 				Thread.sleep(60000);
 			} catch (InterruptedException e) {
 				logger.error("Monitor interrupted. Terminating Monitor");
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * tries to fetch some arbitrarily chosen statistics to see if login
 	 * is successful with the credentials provided.
 	 * @return
 	 */
 	private boolean areCredentialsValid() {
 		try{
 			m_interfaces.getSystemStatistics().get_ftp_statistics().getStatistics();
 		} catch (Exception e){
 			if(e.getMessage().contains("(401)")){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Returns the metric to the AppDynamics Controller.
 	 * @param 	metricName		Name of the Metric
 	 * @param 	metricValue		Value of the Metric
 	 * @param 	aggregation		Average OR Observation OR Sum
 	 * @param 	timeRollup		Average OR Current OR Sum
 	 * @param 	cluster			Collective OR Individual
 	 */
 	public void printMetric(String metricName, double metricValue, String aggregation, String timeRollup, String cluster)
 	{
 		MetricWriter metricWriter = getMetricWriter(metricPath + "F5 Monitor|" + metricName, 
 				aggregation,
 				timeRollup,
 				cluster
 				);
 
 		// conversion/casting to long necessary for monitor:
 		metricWriter.printMetric(String.valueOf((long) metricValue));
 	}
 	
 	/**
 	 * prints all pool members to logger.info and the number of metrics collected.
 	 */
 	private void printAllPoolMembers(){
 		try {
 
 			String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 			iControl.LocalLBPoolMemberStatistics[] memberStats;
 			int numOfPools = 0;
 
 			memberStats = m_interfaces.getLocalLBPool().get_all_member_statistics(pool_list);
 			for(iControl.LocalLBPoolMemberStatistics memberStatistics:memberStats){
 				iControl.LocalLBPoolMemberStatisticEntry[] memberStatsEntries = memberStatistics.getStatistics();
 				for(iControl.LocalLBPoolMemberStatisticEntry memberStatsEntry:memberStatsEntries){
 					logger.info("Found Pool Member: " + memberStatsEntry.getMember().getAddress());
 					numOfPools ++;
 				}
 			}
 			
 			logger.info("This monitor reports " + (7 + 9*numOfPools) + " metrics each minute.");
 		} catch (Exception e) {
 			logger.warn("Can't retrieve name of a Pool Member. Error Message: " + e.getMessage());
 		}
 	}
 
 	private class PrintMetricsThread extends Thread{
 		public void run(){			
 			try {
 
                 logger.info("Getting host statistics");
 				for (SystemStatisticsHostStatisticEntry stat : stats.get_all_host_statistics().getStatistics())
 				{
 					for (CommonStatistic st : stat.getStatistics())
 					{
 						if(st.getType().toString().equals("STATISTIC_MEMORY_TOTAL_BYTES") ||
 								st.getType().toString().equals("STATISTIC_MEMORY_USED_BYTES")){
 							printMetric("Memory Stats|" + st.getType().toString(), (new UsefulU64(st.getValue())).doubleValue(),
 									MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
 									MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
 									MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
 						}
 					}
 				}
 
                 logger.info("Getting client SSL statistics");
 				for (CommonStatistic stat : stats.get_client_ssl_statistics().getStatistics())
 				{
 					if(stat.getType().getValue().equals("STATISTIC_SSL_COMMON_CURRENT_CONNECTIONS")){
 						printMetric("SSL Stats|" + stat.getType().toString(), (new UsefulU64(stat.getValue())).doubleValue(),
 								MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
 								MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
 								MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
 					}
 				}
 
                 logger.info("Getting TCP statistics");
 				for (CommonStatistic stat : stats.get_tcp_statistics().getStatistics())
 				{
 					if(stat.getType().getValue().equals("STATISTIC_TCP_OPEN_CONNECTIONS") ||
 							stat.getType().getValue().equals("STATISTIC_TCP_CLOSE_WAIT_CONNECTIONS") ||
 							stat.getType().getValue().equals("STATISTIC_TCP_ESTABLISHED_CONNECTIONS")){
 						printMetric("TCP Stats|" + stat.getType().toString(), (new UsefulU64(stat.getValue())).doubleValue(),
 								MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
 								MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
 								MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
 					}
 				}
 
 				// GET CPU % BUSY
 				int count = 0;
 				int val = 0;
                 logger.info("Getting CPU statistics");
 				for (iControl.SystemCPUUsageExtended usage : m_interfaces.getSystemSystemInfo().get_all_cpu_usage_extended_information().getHosts())
 				{
 					for(CommonStatistic[] stats2 : usage.getStatistics()){
 						for(CommonStatistic stat : stats2){
 							if(stat.getType().toString().equals("STATISTIC_CPU_INFO_ONE_MIN_AVG_IDLE")){
 								count ++;
 								val += 100 - new UsefulU64(stat.getValue()).doubleValue();
 							}
 
 						}
 					}
 					if(count != 0){
 						printMetric("CPU Stats|CPU % BUSY", val / count,
 								MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
 								MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
 								MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
 					}
 				}
 
 				reportPoolMemberStats();
 
                 logger.info("------------------------------------");
 
 			} catch (Exception e) {
 				logger.error("Could not retrieve metrics: " + e.getMessage() +
 						"... Aborted metrics retrieval for this minute.");
 			}
 		}
 
 		/**
 		 * retrieves and reports the statistics for each poolmember that the user wants to be monitored
 		 */
 		private void reportPoolMemberStats(){
 			try {
 
 			//	String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 				String [] pool_list = monitoredPoolMembers.toArray(new String[monitoredPoolMembers.size()]);
 				logger.info("Pool size is "+monitoredPoolMembers.size());
 				List<String> NodeNamesList = new ArrayList<String>();
 
 
                 if (isVersion11) {
                     iControl.LocalLBPoolMemberStatistics[] memberStats;
                     memberStats = m_interfaces.getLocalLBPool().get_all_member_statistics(pool_list);
                     for(iControl.LocalLBPoolMemberStatistics memberStatistics:memberStats){
                         iControl.LocalLBPoolMemberStatisticEntry[] memberStatsEntries = memberStatistics.getStatistics();
                         for(iControl.LocalLBPoolMemberStatisticEntry memberStatsEntry:memberStatsEntries){
                             iControl.CommonStatistic[] stats = memberStatsEntry.getStatistics();
                             //if(isSupposedToBeMonitored(memberStatsEntry.getMember().getAddress())){
                                 NodeNamesList.add(memberStatsEntry.getMember().getAddress());
                                 for(iControl.CommonStatistic stat : stats){
                                     if(poolMemberMetrics.contains(stat.getType().getValue())){
                                         printMetric("Pool Members" + memberStatsEntry.getMember().getAddress().replaceAll("/", "|") +  "|" + stat.getType().getValue(), (new UsefulU64(stat.getValue())).doubleValue(),
                                                 MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                                 MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                                 MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
                                     }
                                 }
                             //}
                         }
                     }
                 } else {
                     iControl.LocalLBPoolMemberMemberStatistics[] memberStats;
                     memberStats = m_interfaces.getLocalLBPoolMember().get_all_statistics(pool_list);
                     for (iControl.LocalLBPoolMemberMemberStatistics memberStat : memberStats) {
                         iControl.LocalLBPoolMemberMemberStatisticEntry[] memberStatsEntries = memberStat.getStatistics();
                         for (iControl.LocalLBPoolMemberMemberStatisticEntry memberStatsEntry : memberStatsEntries) {
                             iControl.CommonStatistic[] stats = memberStatsEntry.getStatistics();
                             NodeNamesList.add(memberStatsEntry.getMember().getAddress());
                             for(iControl.CommonStatistic stat : stats){
                                 if(poolMemberMetrics.contains(stat.getType().getValue())){
                                     printMetric("Pool Members" + memberStatsEntry.getMember().getAddress().replaceAll("/", "|") +  "|" + stat.getType().getValue(), (new UsefulU64(stat.getValue())).doubleValue(),
                                             MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                             MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                             MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
                                 }
                             }
                         }
                     }
                 }
 
 				String[] NodeNamesArray = new String[1];
 				NodeNamesArray= NodeNamesList.toArray(NodeNamesArray);
 				String[] IPAddressesArray = m_interfaces.getLocalLBNodeAddressV2().get_address(NodeNamesArray);
 				for(int i = 0; i < IPAddressesArray.length; i++){
 					System.out.println(IPAddressesArray[i] + " TO " + NodeNamesArray[i]);
 					poolMemberIPToName.put(IPAddressesArray[i], NodeNamesArray[i]);
 				}
 
 				// with the recent information obtained about which members to monitor, report their status lights
 				reportPoolMemberStatusLights();
 
                 logger.info("Done reporting pool member stats");
 
             } catch (Exception e) {
 				logger.warn("Can't retrieve statistic for a poolMember. Error Message: " + e.getMessage());
 			}
 		}
 
 		/**
 		 * addition to reportPoolMemberStats. This method retrieves the status lights.
 		 * @throws Exception
 		 */
 		public void reportPoolMemberStatusLights() throws Exception
 		{
 			//String [] pool_list = m_interfaces.getLocalLBPool().get_list();
 			String [] pool_list = monitoredPoolMembers.toArray(new String[monitoredPoolMembers.size()]);
 
 			iControl.LocalLBPoolMemberMemberObjectStatus [][] objStatusAofA = 
 					m_interfaces.getLocalLBPoolMember().get_object_status(pool_list);
 
 
 			for(iControl.LocalLBPoolMemberMemberObjectStatus [] objStatusA : objStatusAofA)
 			{
 				for(int i=0; i<objStatusA.length; i++)
 				{
 					String IPAddress = objStatusA[i].getMember().getAddress();
 					if (poolMemberIPToName.containsKey(IPAddress)){
 						iControl.LocalLBObjectStatus objStatus = objStatusA[i].getObject_status();
 						iControl.LocalLBAvailabilityStatus availability = objStatus.getAvailability_status();
 						iControl.LocalLBEnabledStatus enabled = objStatus.getEnabled_status();
 
 						int status;
 
 						if(availability.getValue().contains("GREEN") && enabled.getValue().contains("STATUS_ENABLED")){
 							status = 4; // Available (Enabled)
 						} else if(availability.getValue().contains("RED") && enabled.getValue().contains("STATUS_ENABLED")){
 							status = 3; // Offline (Enabled)
 						} else if(availability.getValue().contains("GREEN") && enabled.getValue().contains("STATUS_DISABLED")){
 							status = 2; // Available (Disabled)
 						} else if(availability.getValue().contains("RED") && enabled.getValue().contains("STATUS_DISABLED")){
 							status = 1; // Offline (Disabled)
 						} else {
 							status = 5; // UNKNOWN
 						}
 
 						printMetric("Pool Members" + poolMemberIPToName.get(IPAddress).replaceAll("/", "|") +  "|STATUS LIGHT", status,
 								MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
 								MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
 								MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
 					}
 				}
 			}
 		}
 
 		/*
 		private boolean isSupposedToBeMonitored(String poolMemberCandidate){
 			for(String poolMember : monitoredPoolMembers){
 				if(poolMemberCandidate.substring(poolMemberCandidate.lastIndexOf('/') + 1, poolMemberCandidate.length()).equals(poolMember)){
 					return true;
 				}
 			}
 			return false;
 		}		
 		*/
 	}
 
 	/**
 	 * 
 	 * @author F5 devcentral
 	 * https://devcentral.f5.com/tech-tips/articles/a-class-to-handle-ulong64-return-values-in-java#.Ub-yZ6FAT-k
 	 *
 	 * Class for converting CommonULong64 to a double
 	 */
 	private class UsefulU64 extends CommonULong64 { 
 		// The following line is required of all serializable classes, but not utilized in our implementation.
 		static final long serialVersionUID = 1; 
 
 
 		//public UsefulU64() { super(); } 
 		//public UsefulU64(long arg0, long arg1) { super(arg0, arg1); } 
 		public UsefulU64(CommonULong64 orig) { 
 			this.setHigh(orig.getHigh()); 
 			this.setLow(orig.getLow()); 
 		} 
 
 		public Double doubleValue() { 
 			long high = getHigh(); 
 			long low = getLow(); 
 			Double retVal; 
 			// Since getHigh() and getLow() are declared as signed longs but hold unsigned data, make certain that we account for a long 
 			// that rolled over into the negatives. An alternative to this would be to hand modify the WSDL4J output, but then we'd  
 			// have to rewrite that code each time a new release of iControl came out. It is cleaner (in our opinion) to account for it here. 
 			Double rollOver = new Double((double)0x7fffffff); 
 			rollOver = new Double(rollOver.doubleValue() + 1.0); 
 			if(high >=0) 
 				retVal = new Double((high << 32 & 0xffff0000)); 
 			else 
 				retVal = new Double(((high & 0x7fffffff) << 32) + (0x80000000 << 32)); 
 
 			if(low >=0) 
 				retVal = new Double(retVal.doubleValue() + (double)low); 
 			else 
 				retVal = new Double(retVal.doubleValue() + (double)((low & 0x7fffffff)) + rollOver.doubleValue()); 
 
 			return retVal; 
 		} 
 	}
 }

 package com.appnomic.noc.availability;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.apache.struts2.interceptor.ParameterAware;
 
 import com.opensymphony.xwork2.Action;
 
 //import org.apache.struts2.convention.annotation.Action;
 //import org.apache.struts2.convention.annotation.Actions;
 //import org.apache.struts2.convention.annotation.Result;
 //import org.apache.struts2.convention.annotation.Results;
 //import org.apache.struts2.dispatcher.ServletDispatcherResult;
 
 
 /*
  * This action always retrieves the 'Meta' for the Grid to be displayed
  * The retrieved data is sent back to client as JSON - refer to the XML to find json there
  * Note: there is NO Query parameter here. The exact meta to send back is configured by the user in a 
  * separate configuration console. So this Action gets input from that configuration
  */
 
 //@Results({
 //	  @Result(name="SUCCESS", location="/availability/AvailabilityMeta.jsp")
 //	})
 
 public class AvailabilityDataClusterAction implements ParameterAware {
 	
 	private ClusterData clusterData;
 	private Map<String, String[]> parameters;
 	
 	public ClusterData getClusterData() {
 		return clusterData;
 	}
 
 	public void setClusterData(ClusterData clusterData) {
 		this.clusterData = clusterData;
 	}
 
 	public AvailabilityDataClusterAction() {
 		setDummyData();
 	}
 	
 	public void setParameters(Map<String, String[]> parameters) {
 		this.parameters = parameters;
 	}
 
 	public Map<String, String[]> getParameters() {
 		return this.parameters;
 	}
 	
 	public void setDummyData() {
 		clusterData = new ClusterData();
 		clusterData.setInstanceName("ClusterX");
 		
 		int hostCount = 3;
 		HostTimes [] hostTimes = new HostTimes[hostCount]; // cluster has 2 hosts
 		clusterData.setTimes(hostTimes);
 		
 		for(int i=0;i<hostCount;i++) {
 			hostTimes[i] = new HostTimes();
 			hostTimes[i].setTime("10:10");
 			
 			int hostDataPoints = 2;
 			HostDataPoint [] hostDataPoint = new HostDataPoint[hostDataPoints];
 			hostTimes[i].setHost(hostDataPoint);
 			
 			for(int j=0;j<hostDataPoints;j++) {
				hostDataPoint[j] = new HostDataPoint();
				hostDataPoint[j].setName("HostX");
				hostDataPoint[j].setValue(j); // in this dummy sample, j is just 0/1
 			}
 		}
 	}
 
 	public String execute() {
 		return Action.SUCCESS;
 	}
 
 }

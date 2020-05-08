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
 
 public class AvailabilityDataHostAction implements ParameterAware {
 	
 	private HostData hostData;
 	private Map<String, String[]> parameters;
 
 	public HostData getHostData() {
 		return hostData;
 	}
 
 	public void setHostData(HostData hostData) {
 		this.hostData = hostData;
 	}
 
 	public AvailabilityDataHostAction() {
 		setDummyData();
 	}
 	
 	public void setParameters(Map<String, String[]> parameters) {
 		this.parameters = parameters;
 	}
 
 	public Map<String, String[]> getParameters() {
 		return this.parameters;
 	}
 	
 	public void setDummyData() {
		hostData = new HostData();
 		hostData.setInstanceName("HostX");
 		
 		int kpiCount = 3;
 		KpiTimes [] kpiTimes = new KpiTimes[kpiCount];
 		hostData.setTimes(kpiTimes);
 		for(int i=0;i<kpiCount;i++) {
 			kpiTimes[i] = new KpiTimes();
 			kpiTimes[i].setTime("10:10");
 			
 			int kpiDataPoints = 2;
 			KpiDataPoint [] kpiDataPoint = new KpiDataPoint[kpiDataPoints];
 			kpiTimes[i].setHost(kpiDataPoint);
 			for(int j=0;j<kpiDataPoints;j++) {
 				kpiDataPoint[j] = new KpiDataPoint();
 				kpiDataPoint[j].setName("KpiX");
 				kpiDataPoint[j].setValue(j); // in this dummy example, j is just 0/1
 			}
 		}
 	}
 
 	public String execute() {
 		return Action.SUCCESS;
 	}
 
 }

 package com.appnomic.noc.action.alert;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.ParentPackage;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Action;
 
 import com.appnomic.domainobject.AlertCountSummary;
 import com.appnomic.domainobject.AlertCountSummary.SUMMARY_CATEGORY;
 import com.appnomic.domainobject.AlertSeverity;
 import com.appnomic.domainobject.ApplicationData;
 import com.appnomic.domainobject.Component;
 import com.appnomic.domainobject.ComponentAlertSummary;
 import com.appnomic.noc.action.AbstractNocAction;
 import com.appnomic.service.ApplicationDataService;
 import com.appnomic.service.ComponentDataService;
 import com.appnomic.service.AlertDataService;
 
 @ParentPackage("json-default")
 @Namespace("/alert")
 public class AlertInfoAction extends AbstractNocAction  {
 	
 	private Map<String, String[]> param;
 	
 	private AlertDataService alertDataService;
 	private ComponentDataService componentDataService;
 	private ApplicationDataService applicationDataService;
 	
 	private ApplicationMetaVO applicationMetaVO;
 	private ApplicationDataVO applicationDataVO;
 	
 	private Random rand;
 	private static final int min = 0, max = 100;
 	
 	public ComponentDataService getComponentDataService() {
 		return componentDataService;
 	}
 
 	public void setComponentDataService(ComponentDataService componentDataService) {
 		this.componentDataService = componentDataService;
 	}
 
 	
 	public ApplicationDataService getApplicationDataService() {
 		return applicationDataService;
 	}
 
 	public void setApplicationDataService(
 			ApplicationDataService applicationDataService) {
 		this.applicationDataService = applicationDataService;
 	}
 
 	public Map<String, String[]> getParam() {
 		return param;
 	}
 
 	public void setParam(Map<String, String[]> param) {
 		this.param = param;
 	}
 
 	public ApplicationMetaVO getApplicationVO() {
 		return applicationMetaVO;
 	}
 
 	public void setApplicationVO(ApplicationMetaVO applicationVO) {
 		this.applicationMetaVO = applicationVO;
 	}
 	
 	public ApplicationDataVO getApplicationDataVO() {
 		return applicationDataVO;
 	}
 
 	public void setApplicationDataVO(ApplicationDataVO applicationDataVO) {
 		this.applicationDataVO = applicationDataVO;
 	}
 
 	public AlertInfoAction() {
 	}
 	
 	public String nocAction() {		
 		return SUCCESS;
 	}
 
 	public AlertDataService getAlertDataService() {
 		return alertDataService;
 	}
 
 	public void setAlertDataService(AlertDataService alertDataService) {
 		this.alertDataService = alertDataService;
 	}
 
 	@Action(value="/alert/ApplicationMeta", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "session,SUCCESS,ERROR,alertDataService,componentDataService,applicationDataService,applicationDataVO",
 	                "enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String applicationAlertMetaAction() {
 		param = getParameters();
 		
 		applicationMetaVO = new ApplicationMetaVO();
 		
 		String [] metrics = new String[5];
 		metrics[0] = SUMMARY_CATEGORY.COMPONENT_ANALYTIC.name();
 		metrics[1] = SUMMARY_CATEGORY.COMPONENT_AVAILABILITY.name();
 		metrics[2] = SUMMARY_CATEGORY.COMPONENT_STATIC.name();
 		metrics[3] = SUMMARY_CATEGORY.TRANSACTION_BATCH_ANALYTIC.name();
 		metrics[4] = SUMMARY_CATEGORY.TRANSACTION_ONLINE_ANALYTIC.name();
 		applicationMetaVO.setMetrics(metrics);
 		
 		List<ApplicationData> allApplications = applicationDataService.getAll();
 		ApplicationVO [] applications = new ApplicationVO[allApplications.size()];
 		int i = 0;
 		for(ApplicationData application : allApplications) {
 			applications[i] = new ApplicationVO();
 			applications[i].setId(application.getId());
 			applications[i].setName(application.getName());
 			i++;
 		}
 		applicationMetaVO.setApplications(applications);
 		return SUCCESS;
 	}
 	
 	private void setDummyApplicationData(String applicationName, ApplicationDataVO applicationDataVO) {
 		MetricData [] metricDataset = new MetricData[5];
 		applicationDataVO.setMetrics(metricDataset);
 		applicationDataVO.setApplicationName(applicationName);
 	
 		metricDataset[0] = getDummyMetricData(SUMMARY_CATEGORY.COMPONENT_ANALYTIC);
 		metricDataset[1] = getDummyMetricData(SUMMARY_CATEGORY.COMPONENT_AVAILABILITY);
 		metricDataset[2] = getDummyMetricData(SUMMARY_CATEGORY.COMPONENT_STATIC);
 		metricDataset[3] = getDummyMetricData(SUMMARY_CATEGORY.TRANSACTION_BATCH_ANALYTIC);
 		metricDataset[4] = getDummyMetricData(SUMMARY_CATEGORY.TRANSACTION_ONLINE_ANALYTIC);
 	}
 	
 	private MetricData getDummyMetricData(SUMMARY_CATEGORY category) {
 		MetricData metricDataset = new MetricData();
 		int [] counts = new int[3];
 		rand = new Random();
 		counts[0] = rand.nextInt(max - min + 1) + min;
 		counts[1] = rand.nextInt(max - min + 1) + min;
 		counts[2] = rand.nextInt(max - min + 1) + min;
 		metricDataset.setCount(counts);
 		metricDataset.setName(category.name());
 		return metricDataset;
 	}
 	
 	@Action(value="/alert/ApplicationData", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "session,SUCCESS,ERROR,alertDataService,componentDataService,applicationDataService,applicationMetaVO",
 	                "enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String applicationAlertAction() {
 		param = getParameters();
 		
 		String keyVal = "Application Alert: ";
 		for(String key : parameters.keySet()) {
 			keyVal += "[ " + key + " = ";
 			for(String value : parameters.get(key)) {
 				keyVal += value + ", ";
 			}
 			keyVal += "] ";
 		}
 		System.out.println("key value map = " + keyVal);
 		
 		String applicationName = (parameters.get("name")[0]);
 		int id = Integer.parseInt(parameters.get("id")[0]);
 		System.out.println("application being assembled = " + applicationName);
 		
 		applicationDataVO = new ApplicationDataVO();
		setDummyApplicationData(applicationName, applicationDataVO);
 		
		/*Date currentTime = Calendar.getInstance().getTime();
 		Calendar hourBefore = Calendar.getInstance();
		hourBefore.set(Calendar.HOUR_OF_DAY, hourBefore.get(Calendar.HOUR_OF_DAY));
 		Date oneHourBeforeTime = hourBefore.getTime();
 		
 		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String startTime = timeFormat.format(currentTime);
		String endTime = timeFormat.format(oneHourBeforeTime);
 		
 		MetricData [] metricDataset = new MetricData[5];
 		applicationDataVO.setMetrics(metricDataset);
 		applicationDataVO.setApplicationName(applicationName);
 		
 		AlertCountSummary acs = alertDataService.getCountSummary(id, startTime, endTime);
 		
 		metricDataset[0] = getMetricData(acs, SUMMARY_CATEGORY.COMPONENT_ANALYTIC);
 		metricDataset[1] = getMetricData(acs, SUMMARY_CATEGORY.COMPONENT_AVAILABILITY);
 		metricDataset[2] = getMetricData(acs, SUMMARY_CATEGORY.COMPONENT_STATIC);
 		metricDataset[3] = getMetricData(acs, SUMMARY_CATEGORY.TRANSACTION_BATCH_ANALYTIC);
		metricDataset[4] = getMetricData(acs, SUMMARY_CATEGORY.TRANSACTION_ONLINE_ANALYTIC);*/
 		
 		return SUCCESS;
 	}
 	
 	private MetricData getMetricData(AlertCountSummary acs, SUMMARY_CATEGORY category) {
 		MetricData metricDataset = new MetricData();
 		int [] counts = new int[3];
 		if(acs != null) {
 			counts[0] = acs.getCount(category, AlertSeverity.HIGH);
 			counts[1] = acs.getCount(category, AlertSeverity.MEDIUM);
 			counts[2] = acs.getCount(category, AlertSeverity.LOW);
 		} else {
 			counts[0] = -1;
 			counts[1] = -1;
 			counts[2] = -1;
 		}
 		metricDataset.setCount(counts);
 		metricDataset.setName(category.name());
 		return metricDataset;
 	}
 
 	@Action(value="/alert/Cluster", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "session,SUCCESS,ERROR,alertDataService,componentDataService,applicationDataService",
 	                "enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String clusterAlertAction() {
 		param = getParameters();
 		
 		String keyVal = "Cluster Alert: ";
 		for(String key : parameters.keySet()) {
 			keyVal += "[ " + key + " = ";
 			for(String value : parameters.get(key)) {
 				keyVal += value + ", ";
 			}
 			keyVal += "] ";
 		}
 		System.out.println("key value map = " + keyVal);
 		
 		String clusterName = (parameters.get("name")[0]);
 		System.out.println("cluster name being assembled = " + clusterName);
 		
 		
 		return SUCCESS;
 	}
 	
 	@Action(value="/alert/Instance", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "session,SUCCESS,ERROR,alertDataService,componentDataService,applicationDataService",
 	                "enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String instanceAlertAction() {
 		param = getParameters();
 		
 		String keyVal = "Instance Alert: ";
 		for(String key : parameters.keySet()) {
 			keyVal += "[ " + key + " = ";
 			for(String value : parameters.get(key)) {
 				keyVal += value + ", ";
 			}
 			keyVal += "] ";
 		}
 		System.out.println("key value map = " + keyVal);
 		
 		String instanceName = (parameters.get("name")[0]);
 		System.out.println("instance name being assembled = " + instanceName);
 		return SUCCESS;
 	}
 	
 	@Override
 	public void setDummyData() {
 		// TODO Auto-generated method stub
 	}
 
 }

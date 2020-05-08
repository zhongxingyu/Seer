 package com.mycompany.myapp.client;
 
 import java.util.List;
 
import org.cloudfoundry.client.lib.CloudService;

 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 @RemoteServiceRelativePath("info")
 public interface CloudInfoService extends RemoteService {
 	
 
 	public String getInfo(String url);
 	//VCAP Functions
 	public String stopApp(String appName);
 	public String startApp(String appName);
 	public String updateAppmemory(String appName, int memory);
 	public String updateAppinstance(String appName, int instance);
 	public void addApp(String appName, String framework, int memory, List<String> uris, List<String> servicesname );
 	public String deleteApp(String appName);
 	public String restartApp(String appName);
 	//public void uploadAppfile();
 	public String bindingAppservice(String appName, String serviceName);
 	public String unbindingAppservice(String appName, String serviceName);
 	public void createAppservice(String serviceName, String vendor);
 
 	//AWS Functions
 	public String startAmazonCloudController(String c);
 	public String startAmazonDEA();
 	public String startAmazonMongoDB();
 	public String stopAmazonInstances(String command);
 	public String setElasticIp(String elasticIp);
 	public String getAwsInfo(String i);
 	
 	//1und1 Functions
 	public String start1und1();
 	public String stop1und1();
 	public String restart1und1();
 	public String suspend1und1();
 	public String poweroff1und1();
 	public String handle1und1Hardware(String cpu, String HDD, String ram);
 	
 }

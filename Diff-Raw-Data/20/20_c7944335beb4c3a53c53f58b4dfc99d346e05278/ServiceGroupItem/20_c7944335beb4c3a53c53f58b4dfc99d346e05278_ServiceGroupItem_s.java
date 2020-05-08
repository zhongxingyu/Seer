 package edu.wustl.cab2b.common.queryengine;
 
 import java.io.Serializable;
 
 import edu.wustl.cab2b.common.user.ServiceURL;
 
public class ServiceGroupItem implements Serializable {
 
 	private static final long serialVersionUID = 9084583500302186326L;
 	private Long id;
 	private String targetObject;
 	private ServiceGroup serviceGroup;
 	private ServiceURL serviceUrl;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getTargetObject() {
 		return targetObject;
 	}
 	public void setTargetObject(String targetObject) {
 		this.targetObject = targetObject;
 	}
 	public ServiceURL getServiceUrl() {
 		return serviceUrl;
 	}
 	public void setServiceUrl(ServiceURL serviceUrl) {
 		this.serviceUrl = serviceUrl;
 	}
 	public ServiceGroup getServiceGroup() {
 		return serviceGroup;
 	}
 	public void setServiceGroup(ServiceGroup serviceGroup) {
 		this.serviceGroup = serviceGroup;
 	}
 	
 	
 }

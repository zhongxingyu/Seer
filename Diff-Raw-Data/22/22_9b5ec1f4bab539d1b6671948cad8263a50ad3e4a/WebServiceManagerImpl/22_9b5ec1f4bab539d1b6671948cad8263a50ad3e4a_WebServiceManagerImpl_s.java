 package org.meerkat.webserviceManagement;
 
 import javax.jws.WebService;
 
 @WebService(endpointInterface = "org.meerkat.webserviceManagement.WebServiceManager")
 
 public class WebServiceManagerImpl implements WebServiceManager{
 
 	@Override
 	public String getVersion(String version) {
		return "Yeah!! It works fucker!";
 	}
 
 }

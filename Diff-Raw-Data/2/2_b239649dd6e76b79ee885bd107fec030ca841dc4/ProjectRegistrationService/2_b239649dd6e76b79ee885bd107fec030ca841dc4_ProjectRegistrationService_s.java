 package com.hackathonhub.client;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 /**
  * The client side stub for the RPC service.
  */
@RemoteServiceRelativePath("greet")
 public interface ProjectRegistrationService extends RemoteService {
 	String registerProject(String name, String url) throws IllegalArgumentException;
 }

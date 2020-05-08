 package com.aavu.server.service;
 
 import java.io.IOException;
 
 import com.google.gdata.util.ServiceException;
 
 public interface TheGoogleService {
 
 	int importDocsForUser(String username, String password) throws IOException, ServiceException;
 
 	String getAuthorizationURL(String googleAuthReturnURL);
 
	int importDocsForToken(String onetimeUseToken) throws IOException, ServiceException;
 
 
 
 }

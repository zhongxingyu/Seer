 package com.photon.phresco.framework.rest.api;
 
 
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.service.client.api.ServiceContext;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.client.impl.ServiceManagerImpl;
 
 public class RestBase<T> {
 
 	protected ServiceManager getServiceManager(String userName, String password) {
		ServiceManager serviceManager = null;
 		try {
 			ServiceContext context = new ServiceContext();
 			FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 			context.put("phresco.service.url", configuration.getServerPath());
 			context.put("phresco.service.username", userName);
 			context.put("phresco.service.password", password);
 			context.put("phresco.service.api.key", configuration.apiKey());
//			serviceManager = ServiceManagerMap.getServiceManager("sample");
 			if (serviceManager == null) {
 				serviceManager = new ServiceManagerImpl(context);
				ServiceManagerMap.putServiceManager(userName, serviceManager);
 			}
 		} catch (PhrescoWebServiceException ex) {
 			throw new PhrescoWebServiceException(ex.getResponse());
 		} catch (PhrescoException e) {
 			throw new PhrescoWebServiceException(e);
 		}
 
 		return serviceManager;
 	}
 
 	protected ResponseInfo<T> responseDataEvaluation(ResponseInfo<T> responseData, Exception e, String msg, T data ) {
 		responseData.setException(e);
 		responseData.setMessage(msg);
 		responseData.setData(data);
 		
 		return responseData;
 	}
 }

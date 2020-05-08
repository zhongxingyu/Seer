 package com.orangeleap.theguru.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.handler.WSHandlerConstants;
 
 public class WSClient {
 
 	public static Theguru getTheGuru() {
 		TheguruService guruService;
 		try {
 			guruService = new TheguruService(new URL(System.getProperty("casClient.serverUrl") +  "/" + System.getProperty("contextPrefix") + "clementine/services/1.0/theguru.wsdl"),new QName("http://www.orangeleap.com/theguru/services/1.0", "theguruService"));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 //		guruService = new TheguruService();
 		Theguru guruPort = guruService.getTheguruPort();
 

         Map outProps = new HashMap();
 		Client client = org.apache.cxf.frontend.ClientProxy.getClient(guruPort);
 		org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
 		
 		outProps.put(WSHandlerConstants.ACTION,WSHandlerConstants.USERNAME_TOKEN);
		outProps.put(WSHandlerConstants.USER, "nolan@company1");
 		outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
 		outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, PWCallbackHandler.class.getName());
 		outProps.put(WSHandlerConstants.ADD_UT_ELEMENTS,WSConstants.NONCE_LN + " " + WSConstants.CREATED_LN);
 
 		WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
 		cxfEndpoint.getOutInterceptors().add(wssOut);
 		return guruPort;
 	}
 }

 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.net.URL;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import javax.xml.namespace.QName;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPConstants;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Dispatch;
 import javax.xml.ws.Response;
 import javax.xml.ws.Service;
 import javax.xml.ws.Service.Mode;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.axis.message.SOAPEnvelope;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 
 /**
  * Test a JAX-WS web service using the JAX-WS API
  * @author bfitzpat
  *
  */
 public class JAXWSTester2 {
 
 	// the response message to pass back
 	private String resultBody;
 	
 	private SOAPEnvelope resultSOAP;
 	
 	private SOAPBody resultSOAPBody;
 	
 	// the result HTTP headers to pass back
 	private Map<String, String> resultHeaders;
 	
 	// utility constant
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	/**
 	 * Return the response message
 	 * @return 
 	 */
 	public String getResultBody() {
 		return this.resultBody;
 	}
 	
 	public SOAPBody getResultSOAPBody() {
 		return this.resultSOAPBody;
 	}
 	
 	/**
 	 * Return a map of HTTP headers from the response
 	 * @return
 	 */
 	public Map<String, String> getResultHeaders() {
 		return this.resultHeaders;
 	}
 	
 	public SOAPEnvelope getResultSOAP(){
 		return this.resultSOAP;
 	}
 	
 	public void doTest( IProgressMonitor monitor, String endpointurl, String actionurl, String ns, 
 			String serviceName, String messageName, String body ) throws Exception {
 		doTest(monitor, endpointurl, actionurl, ns, serviceName, messageName, body, null, null);
 	}
 	/**
 	 * Invoke the JAX-WS service
 	 * @param endpointurl
 	 * @param actionurl
 	 * @param body
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public void doTest( IProgressMonitor monitor, String endpointurl, String actionurl, String ns, 
 			String serviceName, String messageName, String body, String uid, String pwd ) throws Exception {
 		
 		this.resultBody = EMPTY_STRING;
 		
 		// in case we're using SSL security...
 		if (endpointurl.toLowerCase().startsWith("https://")) { //$NON-NLS-1$
 
 			TrustManager t = new X509TrustManager() {
 				
 				@Override
 				public X509Certificate[] getAcceptedIssuers() {
 					return new X509Certificate[0];
 				}
 				
 				@Override
 				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
 						throws CertificateException {
 				}
 				
 				@Override
 				public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
 				}
 			}; 
 			TrustManager[] tm = new TrustManager[] {t};
 			SSLContext ctx = SSLContext.getInstance("SSL"); //$NON-NLS-1$
 			ctx.init(null, tm, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
 			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
 				
 				@Override
 				public boolean verify(String hostname, SSLSession session) {
 					return true;
 				}
 			});
 		}
 		
 		URL serviceURL = new URL (endpointurl); //"http://www.ecubicle.net/gsearch_rss.asmx"
 		QName serviceQName = new QName (ns, serviceName); // "http://www.ecubicle.net/webservices", "gsearch_rss"
 		Service s = Service.create(serviceURL, serviceQName);
 
 		boolean isSOAP12 = TesterWSDLUtils.isRequestBodySOAP12(body);
 		
 		QName messageQName = new QName(ns, messageName); //"http://www.ecubicle.net/webservices", "gsearch_rssSoap"
 		Dispatch<SOAPMessage> d = s.createDispatch(messageQName, SOAPMessage.class, Mode.MESSAGE);
 		
 		MessageFactory mf = MessageFactory.newInstance();
 		if (isSOAP12) {
 			mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} else if (actionurl != null && !actionurl.trim().isEmpty()){
 			d.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
 			d.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, actionurl); //"http://www.ecubicle.net/webservices/GetSearchResults");
 		}
 
 		if (uid != null && pwd != null) {
 			d.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, uid); 
 			d.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
 		}
 
 		SOAPMessage m = mf.createMessage( null, new ByteArrayInputStream(body.getBytes()));
 		m.saveChanges();
 
 		// this is a different method of passing along security details
 //		if (uid != null && pwd != null) {
 //	        String authStr = uid + ':' + pwd;
 //			byte[] authEncByte = Base64.encodeBase64(authStr.getBytes());
 //			String authStringEnc = new String(authEncByte);
 //
 //			MimeHeaders hd = m.getMimeHeaders();
 //			hd.addHeader("Authorization", "Basic " + authStringEnc);  //$NON-NLS-1$//$NON-NLS-2$
 //		}
 
 		Response<SOAPMessage> response = d.invokeAsync(m);
 		while (!response.isDone()){
 			//go off and do some work
 			if (monitor != null) {
 				if (monitor.isCanceled()) {
 					response.cancel(true);
 				}
 			}
 		}
 
 		try {
 			if (!response.isCancelled()) {
 		        //get the actual result
 				SOAPMessage o = (javax.xml.soap.SOAPMessage)response.get();
 
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				o.writeTo(baos);
 				this.resultBody = baos.toString();
 				this.resultSOAPBody = o.getSOAPBody();
 				
 				if (response.getContext() != null) {
 					Object responseHeaders =
 						response.getContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
 					if ( responseHeaders != null && responseHeaders instanceof Map) {
 						this.resultHeaders = (Map<String, String>) responseHeaders;
 					}
 				}
 			} else {
 				throw new InterruptedException(JBossWSUIMessages.JAXRSWSTestView_Message_Service_Invocation_Cancelled);
 			}
 		} catch (ExecutionException ex){
 		        //get the actual cause
 		        Throwable cause = ex.getCause();
 		        throw new Exception(cause);
 		} catch (InterruptedException ie){
 		        //note interruptions
 				throw ie;
 		}
 	}
 }

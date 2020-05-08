 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.transport.as2;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.ServerSocket;
 import java.net.URI;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
 import org.mule.api.MuleContext;
 import org.mule.api.MuleEvent;
 import org.mule.api.MuleException;
 import org.mule.api.endpoint.EndpointURI;
 import org.mule.api.endpoint.ImmutableEndpoint;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.config.i18n.CoreMessages;
 import org.mule.transport.ConnectException;
 import org.mule.transport.file.ExpressionFilenameParser;
 import org.mule.transport.file.FilenameParser;
 import org.mule.transport.http.HttpConnector;
 
 /**
  * <code>As2Connector</code> TODO document
  */
 public class As2Connector extends HttpConnector
 {
 	/* Http private member */
     private boolean disableCleanupThread;
     private IdleConnectionTimeoutThread connectionCleaner;
     private As2ConnectionManager connectionManager;
 	
     /* This constant defines the main transport protocol identifier */
     public static final String AS2 = "as2";
 
     private String senderId;
     private String keystorePath;
     private String keystorePassword;
     private String partnerId;
     
     private FilenameParser filenameParser;
        
     public As2Connector(MuleContext context)
     {
         super(context);
         filenameParser = new ExpressionFilenameParser();
     }
        
 
     public String getProtocol()
     {
         return AS2;
     }
 
     public String getSenderId() {
     	return senderId;
     }
     
     public void setSenderId(String senderId) {
     	this.senderId = senderId;
     }
     
 	public String getKeystorePath() {
 		return keystorePath;
 	}
 
 
 	public void setKeystorePath(String keystorePath) {
 		this.keystorePath = keystorePath;
 	}
 
 
 	public String getKeystorePassword() {
 		return keystorePassword;
 	}
 
 
 	public void setKeystorePassword(String keystorePassword) {
 		this.keystorePassword = keystorePassword;
 	}
 
 
 	public String getPartnerId() {
 		return partnerId;
 	}
 
 
 	public void setPartnerId(String partnerId) {
 		this.partnerId = partnerId;
 	}
 	
 	
 	public FilenameParser getFilenameParser() {
 		return filenameParser;
 	}
 
 
 	public void setFilenameParser(FilenameParser filenameParser) {
 		
 		this.filenameParser = filenameParser;		
 		if (filenameParser != null) {
 			filenameParser.setMuleContext(muleContext);
 		}
 	}
 
 
 	protected void setupClientAuthorizationLocal(MuleEvent event, HttpMethod httpMethod, HttpClient client, ImmutableEndpoint endpoint) throws UnsupportedEncodingException {
 		this.setupClientAuthorization(event, httpMethod, client, endpoint);
 	}
 	
 	
 	protected HttpClient doClientConnectLocal() throws Exception {
 		return this.doClientConnect();
 	}
 	
 	@Override
 	protected void doInitialise() throws InitialisationException {
		
//		super.doInitialise();
 		/* Same as parent */
 		if (clientConnectionManager == null)
         {
             clientConnectionManager = new MultiThreadedHttpConnectionManager();
             String prop = System.getProperty("mule.http.disableCleanupThread");
             disableCleanupThread = prop != null && prop.equals("true");
             if (!disableCleanupThread)
             {
                 connectionCleaner = new IdleConnectionTimeoutThread();
                 connectionCleaner.setName("HttpClient-connection-cleaner-" + getName());
                 connectionCleaner.addConnectionManager(clientConnectionManager);
                 connectionCleaner.start();
             }
 
             HttpConnectionManagerParams params = new HttpConnectionManagerParams();
             if (getSendBufferSize() != INT_VALUE_NOT_SET)
             {
                 params.setSendBufferSize(getSendBufferSize());
             }
             if (getReceiveBufferSize() != INT_VALUE_NOT_SET)
             {
                 params.setReceiveBufferSize(getReceiveBufferSize());
             }
             if (getClientSoTimeout() != INT_VALUE_NOT_SET)
             {
                 params.setSoTimeout(getClientSoTimeout());
             }
             if (getSocketSoLinger() != INT_VALUE_NOT_SET)
             {
                 params.setLinger(getSocketSoLinger());
             }
 
             params.setTcpNoDelay(isSendTcpNoDelay());
             params.setMaxTotalConnections(dispatchers.getMaxTotal());
             params.setDefaultMaxConnectionsPerHost(dispatchers.getMaxTotal());
             clientConnectionManager.setParams(params);
         }
         //connection manager must be created during initialization due that devkit requires the connection manager before start phase.
         //That's why it not manager only during stop/start phases and must be created also here.
         if (connectionManager == null)
         {
             try
             {
                 connectionManager = new As2ConnectionManager(this, getReceiverWorkManager());
             }
             catch (MuleException e)
             {
                 throw new InitialisationException(CoreMessages.createStaticMessage("failed creating http connection manager"),this);
             }
         }
 		
 		
         if (filenameParser != null)
         {
             filenameParser.setMuleContext(muleContext);
         }
 		
 	}
 	
 	
	@Override
    protected void doDispose()
    {
        if (!disableCleanupThread)
        {
            connectionCleaner.shutdown();

            if (!muleContext.getConfiguration().isStandalone())
            {
                MultiThreadedHttpConnectionManager.shutdownAll();
            }
        }
        if (this.connectionManager != null)
        {
            connectionManager.dispose();
            connectionManager = null;
        }
//        super.doDispose();
    }
	
     @Override
     protected ServerSocket getServerSocket(URI uri) throws IOException
     {
         return super.getServerSocket(uri);
     }
     
     
     public void connect(EndpointURI endpointURI) throws ConnectException
     {
         connectionManager.addConnection(endpointURI);
     }
 
     
     public void disconnect(EndpointURI endpointURI)
     {
         connectionManager.removeConnection(endpointURI);
     }
     
     
 
 }

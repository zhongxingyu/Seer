 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) 2009 Osaka Gas Information System Research Institute Co., Ltd. 
  * All rights reserved.  http://www.ogis-ri.co.jp/
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE file.
  */
 
 package org.mule.transport.ssh;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.commons.ssh.Connection;
 import net.sf.commons.ssh.ExecSession;
 import net.sf.commons.ssh.ExecSessionOptions;
 
 import org.mule.DefaultMuleMessage;
 import org.mule.api.MuleEvent;
 import org.mule.api.MuleMessage;
 import org.mule.api.endpoint.OutboundEndpoint;
 import org.mule.api.retry.RetryContext;
 import org.mule.api.routing.ResponseTimeoutException;
 import org.mule.api.transport.PropertyScope;
 import org.mule.config.i18n.CoreMessages;
 import org.mule.transport.AbstractMessageDispatcher;
 import org.mule.transport.NullPayload;
 import org.mule.transport.ssh.config.SshNamespaceHandler;
 import org.mule.util.StringUtils;
 
 /**
  * <code>SshMessageDispatcher</code> TODO document
  */
 public class SshMessageDispatcher extends AbstractMessageDispatcher
 {
 	private int waitTime = 1000; //before waiting time to read result.
 	private final int responseTimeout;
 	private Connection sshConn;
 	private ExecSession session;
 	private String encoding = "UTF-8";
 	
     /* For general guidelines on writing transports see
        http://mule.mulesource.org/display/MULE/Writing+Transports */
 
     public SshMessageDispatcher(OutboundEndpoint endpoint)
     {
         super(endpoint);
         responseTimeout = endpoint.getResponseTimeout();
         encoding = endpoint.getEncoding();
     }
 
     public void doConnect() throws Exception
     {
     	this.sshConn = getConnector().openSshConnection(responseTimeout);
     }
 
     public void doDisconnect() throws Exception
     {
     	if(!session.isClosed())
     	{
     		session.close();
     	}
 		if(!this.sshConn.isClosed())
 		{
 			logger.debug("ssh connection is closing...");
 			this.sshConn.close();
 		}
     }
 
     public void doDispatch(MuleEvent event) throws Exception
     {
         /* IMPLEMENTATION NOTE: This is invoked when the endpoint is
            asynchronous.  It should invoke the transport but not return any
            result.  If a result is returned it should be ignorred, but if the
            underlying transport does have a notion of asynchronous processing,
            that should be invoked.  This method is executed in a different
            thread to the request thread. */
 
         // TODO Write the client code here to dispatch the event over this
         // transport
     	
     	logger.info("dispatch command");
         doSend(event);
     }
 
     public MuleMessage doSend(MuleEvent event) throws Exception
     {
         /* IMPLEMENTATION NOTE: Should send the event payload over the
            transport. If there is a response from the transport it shuold be
            returned from this method. The sendEvent method is called when the
            endpoint is running synchronously and any response returned will
            ultimately be passed back to the callee. This method is executed in
            the same thread as the request thread. */
 
         // TODO Write the client code here to send the event over this
         // transport (or to dispatch the event to a store or repository)
 
         // TODO Once the event has been sent, return the result (if any)
         // wrapped in a MuleMessage object
     	
 		String result = "";
 		int exitStatus = 0;
 		
 		String execCommand = buildCommand(event);
 		session = openExecSession(execCommand);
 		
 		if(isUseSudo() && isWaitingPrompt(session)){ // will be wait to prompt password
 			//TODO checking wheither to difine "sudoPassword" property
 			writeSudoPassword(session);
 		}
 		
 
 		result = readResult(session, System.currentTimeMillis(), event);
 		exitStatus = session.getExitStatus();
 
 		session.getInputStream().close();
 	
 		MuleMessage message = buildMuleMessage(result, exitStatus, event.getMessage());
 		
 		return message;
     }
 
 	private ExecSession openExecSession(String execCommand)
 			throws  IOException
 	{
 		logger.trace("open session");
 		ExecSessionOptions execSessionOptions = new ExecSessionOptions(execCommand);
 		ExecSession session = this.sshConn.openExecSession(execSessionOptions);
 		return session;
 	}
 
 	private String buildCommand(MuleEvent event) throws Exception 
 	{
 		String execCommand = event.getMessage().getPayloadAsString();
 		
 		if(isUseSudo())
 		{
			if(!execCommand.matches("^\\s*sudo"))
 			{
 				execCommand = "sudo " + execCommand;
 			}
 		}
 		logger.debug("exec command : \"" + execCommand + "\"");
 		return execCommand;
 	}
 
 	private boolean isWaitingPrompt(ExecSession session)
 	{
 		//TODO difine magic number
 		// this code is specific jsch
 		return session.getExitStatus() == -1;
 	}
 
 	private void writeSudoPassword(ExecSession session) throws IOException
 	{
 		logger.trace("send sudo password");
 		OutputStream out = session.getOutputStream();
 		String password = (String) endpoint.getProperty(SshNamespaceHandler.SUDO_PASSWORD);
 		out.write(password.getBytes(encoding));
 		out.flush();
 		out.close();
 	}
 
     public void doDispose()
     {
     	//do nothing
     }
     
 	private String readResult(ExecSession session, long startTime, MuleEvent event) throws IOException, ResponseTimeoutException {
 		byte[] tmp = new byte[1024];
 		//StringBuilder result = new StringBuilder();
 		ByteArrayOutputStream result = new ByteArrayOutputStream(1024);
 		BufferedInputStream in = new BufferedInputStream(session
 				.getInputStream());
 
 		while (true) {
 
 			if(responseTimeout != 0 && System.currentTimeMillis() - startTime >= responseTimeout)
 			{	//response timeout
 				in.close();
 				ResponseTimeoutException exception = new ResponseTimeoutException(CoreMessages.responseTimedOutWaitingForId(getEndpoint().getResponseTimeout(), event.getMessage().getUniqueId()), event.getMessage(), endpoint);
 				exception.addInfo("description", "SshMessageDispatcher {response timeout: " + responseTimeout + " ms}");
 				throw exception;
 			}
 			while (in.available() > 0) {
 				int i = in.read(tmp, 0, 1024);
 				if (i < 0) {
 					break;
 				}
 				//result.append(new String(tmp, 0, i));
 				result.write(tmp, 0, i);
 			}
 			if(session.isClosed()){
 				break;
 			}
 			try 
 			{
 				//polling while remote command is finished
 				Thread.sleep(waitTime);
 			} 
 			catch (InterruptedException e) {}
 		}
 
 		return result.toString(encoding);
 	}
 	
 	private MuleMessage buildMuleMessage(String payload, int exitStatus, MuleMessage origilanMessage)
 	{
 		if(payload != null)
 		{
 			logger.debug("command result : " + payload.toString());
 		}
 		else
 		{
 			logger.debug("command result is null");
 		}
 		logger.info("exit status : " + exitStatus);
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put(SshConnector.SSH_EXIT_STATUS, exitStatus);
 
 		MuleMessage message = new DefaultMuleMessage(payload, origilanMessage);
 		message.addProperties(params, PropertyScope.OUTBOUND);
 		if(StringUtils.isEmpty(payload))
 		{
 			message.setPayload(NullPayload.getInstance());
 		}
 		
 		if(logger.isDebugEnabled())
 		{
 			logger.debug("build message : " + message);
 		}
 			
 		return message;
 	}
 
 	private boolean isUseSudo()
 	{
 		String prop = (String) endpoint.getProperty(SshNamespaceHandler.USE_SUDO);
 		return (Boolean) Boolean.parseBoolean(prop);
 	}
 	
 	@Override
     public SshConnector getConnector()
     {
     	return (SshConnector) super.getConnector();
     }
 
 
     @Override
     public RetryContext validateConnection(RetryContext retryContext)
     {
         try
         {
         	// to valid the SSH connection.
         	// this is for retry policy capabile.
         	Connection conn = getConnector().openSshConnection(responseTimeout);
         	conn.close();
         	
         	retryContext.setOk();
         }
         catch(Exception e)
         {
         	retryContext.setFailed(e);
         }
         return retryContext;
     }
 }
 

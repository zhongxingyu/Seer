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
 
 package org.mule.transport.telnet;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.net.telnet.EchoOptionHandler;
 import org.apache.commons.net.telnet.InvalidTelnetOptionException;
 import org.apache.commons.net.telnet.TelnetClient;
 import org.apache.commons.net.telnet.TelnetOptionHandler;
 import org.mule.DefaultMuleMessage;
 import org.mule.api.MuleMessage;
 import org.mule.api.routing.ResponseTimeoutException;
 import org.mule.config.i18n.CoreMessages;
 import org.mule.transport.AbstractMessageDispatcher;
 import org.mule.util.BeanUtils;
 import org.mule.util.StringUtils;
 
 /*
  * //TODO
  */
 
 public class TelnetClientWrapper
 {
     private transient Log logger = LogFactory.getLog(getClass());
 	private TelnetClient client;
 	private AtomicBoolean isLogined = new AtomicBoolean(false);
 	
 	private InputStream reader;
 	private OutputStream writer;
 
 	private String exitStatusCommand = "echo $?";
 	
 	private AbstractMessageDispatcher dispatcher;
 	private AtomicInteger exitStatus = new AtomicInteger(-10);
 	private MuleMessage command = new DefaultMuleMessage("attempt login");
 	private int responseTimeout = -1;
 	
 	private String encoding = "UTF-8";
 	private long startTime = System.currentTimeMillis();
 	private int waitTime;
 
 	public TelnetClientWrapper(String host, int port, TelnetMessageDispatcher dispatcher, int waitTime) throws IOException
 	{
 
 		TelnetClient client = new TelnetClient();
 
 		TelnetOptionHandler handler = new EchoOptionHandler(true, true, true, true);
 		try
 		{
 			client.addOptionHandler(handler);
 		}
 		catch (InvalidTelnetOptionException e)
 		{
 			dispatcher.exceptionThrown(e);
 		}
 		
 		client.connect(host, port);
 
 		if (!client.isConnected())
 		{
 			throw new ConnectException("cannot connect : " + host + ":" + port);
 		}
 		
 		setWaitTime(waitTime);
 		this.responseTimeout = dispatcher.getEndpoint().getResponseTimeout();
 
 		this.client = client;
 		this.dispatcher = dispatcher;
 		this.encoding = dispatcher.getEndpoint().getEncoding();
 		exitStatusCommand = dispatcher.getConnector().getExitStatusCommand();
 
 		reader = new BufferedInputStream(client.getInputStream(), 1024);
 		
 		writer = new BufferedOutputStream(client.getOutputStream(), 1024);
 		client.setSoTimeout(responseTimeout + waitTime);
 		client.setReaderThread(true);
 		
 		if(logger.isTraceEnabled())
 			logger.trace(BeanUtils.describe(this));
 	}
 
 
 	private void setWaitTime(int waitTime)
 	{
 		if(waitTime >= 1000)
 			this.waitTime = waitTime;
 		else
 			this.waitTime = 1000;
 	}
 
 
 	public void login(String userId, String password) throws Exception
 	{
 
 		
 		logger.debug("attempt login : " + userId);
 		readStream(".*login: $");
 
 		writeStream(userId + "\n");
 
		readStream("Password: $");
 
 		writeStream(password + "\n");
 
 		if (!isSuccessLogin())
 		{
 			throw new IOException("invalid account");
 		}
 		isLogined.set(true);
 
 		logger.debug("login is successful");
 	}
 
 	/**
 	 * 
 	 * @param regex
 	 * @throws IOException
 	 * @throws Exception 
 	 */
 	protected String readStream(String regex) throws IOException, ResponseTimeoutException
 	{
 		logger.trace("reading stream until matched regex ["  + regex + "]");
 		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
 		ByteArrayOutputStream bytebuf = new ByteArrayOutputStream(1024); 
 		Matcher matcher = null;
 		
 		try
 		{
 			Thread.sleep(1000); // wait for running command
 		}
 		catch (InterruptedException e)
 		{
 		}
 		this.startTime = System.currentTimeMillis();
 		try
 		{
 			try
 			{
 				Thread.sleep(1000); // wait for running command
 			}
 			catch (InterruptedException e)
 			{
 			}
 			while(System.currentTimeMillis() - startTime < responseTimeout +60000)
 			{
 				
 				if (reader.available() > 0)
 				{
 	
 					byte[] tmp = new byte[1024];
 					int i = reader.read(tmp, 0, 1024);
 					if (i < 0)
 					{
 						break;
 					}
 					bytebuf.write(tmp, 0, i);
 
 				}
 				bytebuf.flush();
 
 				matcher = pattern.matcher(bytebuf.toString(encoding));
 				if (matcher.find())
 					break;
 				
 				// if seting responseTimeout to 0, timeout processing is disabled. 
 				if(responseTimeout > 0 && (System.currentTimeMillis()- startTime  ) > responseTimeout)
 				{
 					logger.info("timeout : " + responseTimeout + " ms");
 					throwTimeoutException();
 				}
 
 			}
 			if(matcher == null)
 			{
 				logger.debug("no response");
 				matcher = pattern.matcher(bytebuf.toString(encoding));
 			}
 			if (!matcher.find())
 			{
 				matcher = pattern.matcher(bytebuf.toString(encoding));
 			}
 			if (matcher != null && matcher.find(0) && matcher.groupCount() >= 1)
 			{
 				return (matcher.group(1));
 			}
 			return null;
 		}
 		catch(ResponseTimeoutException e) 
 		{
 			
 			throw e;
 		}
 		catch (IOException e)
 		{
 			throw e;
 		}
 	}
 
 	/**
 	 * 
 	 * @param str a string terminated by "\n"
 	 * @throws Exception 
 	 */
 	protected void writeStream(String str) throws UnsupportedEncodingException, IOException
 	{
 		writer.write(str.getBytes(encoding));
 		writer.flush();
 	}
 
 	protected boolean isSuccessLogin() throws Exception
 	{
		String str = readStream("(Last login:.*\\n.*)$");
 		if (str == null)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param command
 	 * @param waitTime
 	 *            mill sec.
 	 * @return exec command result lines
 	 * @throws Exception 
 	 */
 	public String execCommand(MuleMessage _command) throws IOException, ResponseTimeoutException 
 	{
 		logger.trace("execute command {" + command +"}");
 		String command;
 		try
 		{
 			command = _command.getPayloadAsString();
 		}
 		catch (Exception e)
 		{
 			IOException exception = new IOException("can't transform Object {" + _command + "}");
 			exception.setStackTrace(e.getStackTrace());
 			throw exception;
 		}
 		this.command = _command;
 		String[] result = null;
 		StringBuilder sb = new StringBuilder();
 
 		writeStream(command + "\n");
 		
		// reading stream until matching prompt message. (e.g. "[user@localhost ~]$ ") 
		result = readStream("((.*)(\\$|#)\\s*)$").split("\\n");
 
 		logger.trace("skip string - [" + result[0] + "]");
 		
 		for (int i = 1; i < result.length - 1; i++)
 		{
 			sb.append(result[i]).append("\n");
 		}
 		
		Pattern p2 = Pattern.compile("(\\$|#)\\s*$");
 		Matcher m2 = p2.matcher(result[result.length-1]);
 		if(!m2.find())
 		{
 			sb.append(result[result.length-1]);
 		}
 		else
 		{
 			logger.trace("skip string - [" + result[result.length-1]+ "]");
 		}
 		if(logger.isTraceEnabled()) 
 		{
 			logger.trace("command - " + command);
 			logger.trace("command result - " + sb.toString());
 		}
 		
 		setExitStatus();
 
 		return sb.toString();
 	}
 
 	public String sudoExecCommand(MuleMessage _command, String password) throws IOException, ResponseTimeoutException
 
 	{
 		logger.trace("execute sudo command {" + _command +"}");
 		String command;
 		try
 		{
 			command = _command.getPayloadAsString(encoding);
 		}
 		catch (Exception e)
 		{
 			IOException exception = new IOException("can't transform Object {" + _command + "}");
 			exception.setStackTrace(e.getStackTrace());
 			throw exception;
 		} 
 		this.command = _command;
 		String[] result = null;
 		StringBuilder before = new StringBuilder();
 		StringBuilder after = new StringBuilder();
 		
 		int marker = 0;
 		
 		writeStream(command + "\n");
 
 		
 		result = readStream("(.*)$").split("\\n");
 
 		if(result != null)
 		{
 			for (int i = 0; i < result.length ; i++)
 			{
 				before.append(result[i]).append("\\n");
 				
 			}
 			
 			if(logger.isTraceEnabled())
 				logger.trace("sudo result 1 : " + before.toString());
 			
 			if(result[result.length-1].matches(".*(Password:|パスワード:)"))
 			{
 				logger.debug("send password");
 				result = send(password);
 				marker +=1;
 			}
 			else
 			{
 				logger.trace("skip string - [" + result[marker] + "]");
 				marker += 1; // if don't required Password, skip to next line.
 			}
 			
 		}
 		else
 		{
 			setExitStatus();
 			return null;
 		}
 		
		Pattern p = Pattern.compile("(\\$|#)\\s*" + command );
 
 		if(result.length > marker)
 		{
 			Matcher m = p.matcher(result[marker]);
 			if(m.find())
 			{
 				logger.trace("skip string - [" + result[marker] + "]");
 				marker += 1;
 			}
 		}
 		for (int i = 0 + marker; i < result.length-1; i++)
 		{
 			
 			after.append(result[i]).append("\n");
 			
 		}
 		
		Pattern p2 = Pattern.compile("(\\$|#)\\s*$");
 		Matcher m2 = p2.matcher(result[result.length-1]);
 		if(!m2.find())
 		{
 			after.append(result[result.length-1]);
 		}
 
 		
 		setExitStatus();
 		try
 		{
 			logger.trace("result - '" + _command.getPayloadAsString() + "' : " + after.toString());
 		}
 		catch (Exception e)
 		{
 			
 		}
 		
 		return after.toString();
 	}
 
 	private String[] send(String message) throws IOException, ResponseTimeoutException
 	{
 		String str;
 		String[] result;
 		logger.trace("send(String) {message :" + message+"}");
 		writeStream(message + "\n");
 		try
 		{
 			Thread.sleep(getWaitTime()); // wait for running command
 		}
 		catch (InterruptedException e)
 		{
 		}
 		str = readStream("(.*)$");
 		logger.trace("send(String) : received [" + str + "]");
 		if(str != null)
 			result = str.split("\\n");
 		else
 			result = new String[1];
 		return result;
 
 	}
 
 	/**
 	 * 
 	 * @return exit status
 	 */
 	public int getExitStatus() 
 	{
 		return exitStatus.get();
 	}
 	
 	private void setExitStatus() throws ResponseTimeoutException, IOException
 	{
 		int status = -20;
 		String[] result = new String [1];
 		logger.trace("setExitStatus()");
 		result = send(exitStatusCommand);
 		
 		if(result == null || result.length < 1)
 			return;
 		
 		String str = null;
 
 		logger.trace("result - " + exitStatusCommand);
 		for(int i=0; i < result.length -1; i++)
 		{
 			logger.trace("    result[" + i + "] :" + result[i]);
 			Pattern p = Pattern.compile("^(\\d+)");
 			Matcher m = p.matcher(result[i]);
 			if(m.find())
 			{
 				str = m.group(1);
 				break;
 			}
 		}
 
 		logger.debug("set exitStatus : " + str);
 		str = StringUtils.stripToEmpty(str);
 		if(StringUtils.isNotEmpty(str))
 			status = Integer.parseInt(str);
 
 		exitStatus.set(status);
 	}
 
 	public void logout() throws Exception
 	{
 		
 		logger.info("telnet logout");
 		isLogined.set(false);
 		writeStream("exit\n");
 		String str = readStream("(.*)$");
 		logger.trace("exit command response : [" + str +"]");
 		disconnect();
 
 	}
 
 	private void disconnect() throws IOException
 	{
 		logger.info("closing telnet connection");
 		reader.close();
 		writer.close();
 		client.disconnect();
 
 
 		logger.debug("telnet connection is closed");
 	}
 
 
 	public boolean isConnected()
 	{
 		return client.isConnected();
 	}
 	public boolean isSuccesAuth()
 	{
 		return isLogined.get();
 	}
 
 	public boolean sendAYT() throws IllegalArgumentException, IOException, InterruptedException
 	{
 		if(client.isConnected())
 		{
 			return client.sendAYT(5000);
 		}
 		return false;
 	}
 	
 	public int getWaitTime()
 	{
 		return waitTime;
 	}
 
 	private void throwTimeoutException() throws ResponseTimeoutException
 	{
 		logger.info("response timeout");
 		//TODO change to throw IOException (Mule 3.x or later)
 		throw new ResponseTimeoutException(CoreMessages.createStaticMessage("TelnetTransport : response timeout"), command, dispatcher.getEndpoint());
 
 	}
 
 
 }

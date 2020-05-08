 /* $Id: AbstractSocketJaffreConnector.java 394 2009-03-21 20:28:26Z  $
  *
  * (C) Copyright 2008-2013 Alexander Veit
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package org.jaffre.server.spi;
 
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.jaffre.JaffreCallFrameSerializer;
 import org.jaffre.JaffreConfigurationException;
 import org.jaffre.JaffreReturnFrameSerializer;
 import org.jaffre.server.JaffreConnector;
 import org.jaffre.server.JaffreServer;
 import org.jaffre.spi.DefaultJaffreCallFrameSerializer;
 import org.jaffre.spi.DefaultJaffreReturnFrameSerializer;
 
 
 /**
  * @author Alexander Veit
  */
 public abstract class AbstractSocketJaffreConnector implements JaffreConnector
 {
 	private int m_iThreadCorePoolSize = 4;
 
 	private int m_iThreadMaxPoolSize = 10;
 
 	private long m_lStopTimeout = 1000;	// one second
 
 	private InetAddress m_inetAddr;
 
 	private int m_iPort = -1;
 
 	private JaffreCallFrameSerializer m_serCall = new DefaultJaffreCallFrameSerializer();
 
 	private JaffreReturnFrameSerializer m_serRtrn = new DefaultJaffreReturnFrameSerializer();
 
 
 	private JaffreServer m_server;
 
 
 	public AbstractSocketJaffreConnector()
 	{
 	}
 
 
 	public int getCoreThreadPoolSize()
 	{
 		return m_iThreadCorePoolSize;
 	}
 
 
 	public void setCoreThreadPoolSize(int p_iCoreThreadPoolSize)
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		if (p_iCoreThreadPoolSize < 1)
 		{
 			throw new IllegalArgumentException
 				(p_iCoreThreadPoolSize + " is not a valid thread pool size.");
 		}
 
 		m_iThreadCorePoolSize = p_iCoreThreadPoolSize;
 	}
 
 
 	public int getMaxThreadPoolSize()
 	{
 		return m_iThreadMaxPoolSize;
 	}
 
 
 	public void setMaxThreadPoolSize(int p_iMaxThreadPoolSize)
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		if (p_iMaxThreadPoolSize < 1)
 		{
 			throw new IllegalArgumentException
 				(p_iMaxThreadPoolSize + " is not a valid maximum thread pool size.");
 		}
 
 		m_iThreadMaxPoolSize = p_iMaxThreadPoolSize;
 	}
 
 
 	/**
 	 * Get the timeout to wait for worker threads when this connector's
 	 * {@link #stop()} method is being called. The default value is
 	 * <code>1000</code> milliseconds.
 	 * @return The timeout in milliseconds.
 	 */
 	public long getStopTimeout()
 	{
 		return m_lStopTimeout;
 	}
 
 
 	/**
 	 * Set the timeout to wait for worker threads when this connector's
 	 * {@link #stop()} method is being called.
 	 * @param p_lStopTimeout The timeout in milliseconds.
 	 * @throws IllegalArgumentException - If <code>p_lStopTimeout</code>
 	 *    is negative.
 	 */
 	public void setStopTimeout(long p_lStopTimeout)
 	{
 		if (p_lStopTimeout < 0)
 			throw new IllegalArgumentException("Negative timeout value.");
 
 		m_lStopTimeout = p_lStopTimeout;
 	}
 
 
 	/**
 	 * Get the connector's binding address.
 	 * @return The connector's binding address.
 	 */
 	public InetAddress getBindingInetAddress()
 	{
 		return m_inetAddr;
 	}
 
 
 	/**
 	 * Set the connector's binding address.
 	 * @param p_inetAddr The connector's binding address.
 	 */
 	public void setBindingInetAddress(InetAddress p_inetAddr)
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		m_inetAddr = p_inetAddr;
 	}
 
 
 	/**
 	 * Set the connector's binding address.
 	 * @param p_strBindingAddress The connector's binding address.
 	 * @throws UnknownHostException
 	 *    See {@link java.net.InetAddress#getByName(String)}.
 	 */
 	public void setBindingAddress(String p_strBindingAddress)
 		throws UnknownHostException
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		m_inetAddr = InetAddress.getByName(p_strBindingAddress);
 	}
 
 
 	public int getPort()
 	{
 		return m_iPort;
 	}
 
 
 	public void setPort(int p_iPort)
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		if (p_iPort < 0 || p_iPort > 0xFFFF)
 			throw new JaffreConfigurationException("Illegal port number " + p_iPort + ".");
 
 		m_iPort = p_iPort;
 	}
 
 
 	/**
 	 * Get the serializer for Jaffre frames.
 	 * @return The serializer for Jaffre frames.
 	 */
 	public JaffreCallFrameSerializer getCallFrameSerializer()
 	{
 		return m_serCall;
 	}
 
 
 	/**
 	 * Set the serializer for Jaffre frames.
 	 * @param p_serializer The serializer for Jaffre frames.
 	 */
 	public void setCallFrameSerializer(JaffreCallFrameSerializer p_serializer)
 	{
 		m_serCall = p_serializer;
 	}
 
 
 	/**
 	 * Get the serializer for Jaffre return frames.
 	 * @return The serializer for Jaffre return frames.
 	 */
 	public JaffreReturnFrameSerializer getReturnFrameSerializer()
 	{
 		return m_serRtrn;
 	}
 
 
 	/**
 	 * Set the serializer for Jaffre return frames.
	 * @param p_deserializer The serializer for Jaffre return frames.
 	 */
 	public void setReturnFrameSerializer(JaffreReturnFrameSerializer p_serializer)
 	{
 		m_serRtrn = p_serializer;
 	}
 
 
 	/*
 	 * @see org.jaffre.server.JaffreConnector#getServer()
 	 */
 	@Override
 	public synchronized JaffreServer getServer()
 	{
 		return m_server;
 	}
 
 
 	/*
 	 * @see org.jaffre.server.JaffreConnector#setServer(org.jaffre.server.JaffreServer)
 	 */
 	@Override
 	public synchronized void setServer(JaffreServer p_server)
 	{
 		if (isRunning())
 			throw new IllegalStateException();
 
 		m_server = p_server;
 	}
 }

 /* $Id: AbstractSocketJaffreClient.java 394 2009-03-21 20:28:26Z  $
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
 
 
 package org.jaffre.client.spi;
 
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.jaffre.JaffreCallFrameSerializer;
 import org.jaffre.JaffreCookieSupport;
 import org.jaffre.JaffreReturnFrameSerializer;
 import org.jaffre.client.JaffreClient;
 import org.jaffre.spi.DefaultJaffreCallFrameSerializer;
 import org.jaffre.spi.DefaultJaffreReturnFrameSerializer;
 
 
 /**
  * @author Alexander Veit
  */
 public abstract class AbstractSocketJaffreClient implements JaffreClient, JaffreCookieSupport
 {
 	private InetAddress m_inetAddr;
 
 	private int m_iPort = -1;
 
 	private boolean m_bKeepAlive = true;
 
 	private JaffreCallFrameSerializer m_serCall = new DefaultJaffreCallFrameSerializer();
 
 	private JaffreReturnFrameSerializer m_serRtrn = new DefaultJaffreReturnFrameSerializer();
 
 	private boolean m_bSendCookies = false;
 
 	private boolean m_bAcceptCookies = false;
 
 
 	protected final class InvocationHandlerImpl<T> implements InvocationHandler
 	{
 		private final Class<?> m_interface;
 
 		protected InvocationHandlerImpl(Class<T> p_interface)
 		{
 			m_interface = p_interface;
 		}
 
 		@Override
 		public Object invoke(Object p_proxy, Method p_method, Object[] p_args)
 			throws Throwable
 		{
 			return invokeImpl(m_interface, p_proxy, p_method, p_args);
 		}
 	}
 
 
 	public AbstractSocketJaffreClient()
 	{
 	}
 
 
 	/**
 	 * Get the address this client connects to.
 	 * @return The address this client connects to,
 	 *    or <code>null</code>.
 	 */
 	public synchronized InetAddress getServiceInetAddress()
 	{
 		return m_inetAddr;
 	}
 
 	/**
 	 * Set the address this client connects to.
 	 * @param p_inetAddr The address this client connects to.
 	 */
 	public synchronized void setServiceInetAddress(InetAddress p_inetAddr)
 	{
 		m_inetAddr = p_inetAddr;
 	}
 
 	/**
 	 * Set the address this client connects to.
 	 * @param p_strServiceAddress The address this client connects to.
 	 * @throws UnknownHostException
 	 *    See {@link java.net.InetAddress#getByName(String)}.
 	 */
 	public synchronized void setServiceAddress(String p_strServiceAddress)
 		throws UnknownHostException
 	{
 		m_inetAddr = InetAddress.getByName(p_strServiceAddress);
 	}
 
 
 	/**
 	 * Get the port this client connects to.
 	 * @return The port this client connects to.
 	 */
 	public synchronized int getServicePort()
 	{
 		return m_iPort;
 	}
 
 	/**
 	 * Set the port this client connects to.
 	 * @param p_iPort The port this client connects to.
 	 * @throws IllegalArgumentException
 	 *    If an illegal port number was specified.
 	 */
 	public synchronized void setServicePort(int p_iPort)
 	{
 		if (p_iPort < 0 || p_iPort > 0xFFFF)
 			throw new IllegalArgumentException("Illegal port number " + p_iPort + ".");
 
 		m_iPort = p_iPort;
 	}
 
 
 	/**
 	 * Get the keep-alive property. The default value is <code>true</code>.
 	 * @return <code>true</code> if this client is configured to try to
 	 *    keep TCP connections alive, or <code>false</code> otherwise.
 	 */
 	public boolean isKeepAlive()
 	{
 		return m_bKeepAlive;
 	}
 
 
 	/**
 	 * Set the keep-alive property.
 	 * @param p_bKeepAlive <code>true</code> if this client should try to
 	 *    keep TCP connections alive, or <code>false</code> otherwise.
 	 */
 	public void setKeepAlive(boolean p_bKeepAlive)
 	{
 		m_bKeepAlive = p_bKeepAlive;
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
	 * @param p_serializer The serializer for Jaffre return frames.
 	 */
 	public void setReturnFrameSerializer(JaffreReturnFrameSerializer p_serializer)
 	{
 		m_serRtrn = p_serializer;
 	}
 
 
 	/*
 	 * @see org.jaffre.JaffreCookieSupport#isAcceptCookies()
 	 */
 	@Override
 	public boolean isAcceptCookies()
 	{
 		return m_bAcceptCookies;
 	}
 
 
 	/*
 	 * @see org.jaffre.JaffreCookieSupport#setAcceptCookies(boolean)
 	 */
 	@Override
 	public void setAcceptCookies(boolean p_bAcceptCookies)
 	{
 		m_bAcceptCookies = p_bAcceptCookies;
 	}
 
 
 	/*
 	 * @see org.jaffre.JaffreCookieSupport#isSendCookies()
 	 */
 	@Override
 	public boolean isSendCookies()
 	{
 		return m_bSendCookies;
 	}
 
 
 	/*
 	 * @see org.jaffre.JaffreCookieSupport#setSendCookies(boolean)
 	 */
 	@Override
 	public void setSendCookies(boolean p_bSendCookies)
 	{
 		m_bSendCookies = p_bSendCookies;
 	}
 
 
 	/*
 	 * @see org.jaffre.client.JaffreClient#getProxy(java.lang.Class)
 	 */
 	@Override
 	public <T> T getProxy(Class<T> p_interface)
 	{
 		if (!p_interface.isInterface())
 			throw new IllegalArgumentException(p_interface.getName() + " is not an interface.");
 
 		@SuppressWarnings("unchecked")
 		final T l_proxy = (T)Proxy.newProxyInstance(p_interface.getClassLoader(),
 		                                            new Class<?>[] {p_interface},
 		                                            new InvocationHandlerImpl<T>(p_interface));
 
 		return l_proxy;
 	}
 
 
 	@Override
 	public void dispose()
 	{
 		// an empty default implementation
 	}
 
 
 	/**
 	 * This method actually performs the remote call.
 	 * @param p_interface
 	 * @param p_proxy
 	 * @param p_method
 	 * @param p_args
	 * @return The return value.
	 * @throws Throwable If an error occurred.
 	 */
 	protected abstract Object invokeImpl(Class<?> p_interface,
 	                                     Object   p_proxy,
 	                                     Method   p_method,
 	                                     Object[] p_args)
 		throws Throwable;
 }

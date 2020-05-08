 /*
  * Copyright (c) 2003-2005 JGGApi Development Team. All Rights Reserved. This program is free software; you can
  * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
  * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
  * copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package pl.radical.open.gg;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Properties;
 
 /**
  * Created on 2005-05-09
  * 
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  * @version $Id: AbstractGGPropertiesConfiguration.java,v 1.1 2005/11/05 23:34:52 winnetou25 Exp $
  */
 public abstract class AbstractGGPropertiesConfiguration implements IGGConfiguration {
 
 	private IGGConfiguration m_defaultGGConfiguration = new GGConfiguration();
 
 	private Properties m_prop = null;
 	protected String m_fileName = null;
 
 	public AbstractGGPropertiesConfiguration(final String fileName, final IGGConfiguration configuration) throws IOException, InvalidPropertiesFormatException {
 		this(fileName);
 		m_defaultGGConfiguration = configuration;
 	}
 
 	public AbstractGGPropertiesConfiguration(final String fileName) throws IOException, InvalidPropertiesFormatException {
 		if (fileName == null) {
 			throw new IOException("Unable to find file: " + fileName);
 		}
 		m_fileName = fileName;
 		m_prop = createProperties();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getConnectionThreadSleepTimeInMiliseconds()
 	 */
 	public int getConnectionThreadSleepTimeInMiliseconds() {
 		final String connectionThreadSleepTime = String.valueOf(m_defaultGGConfiguration.getConnectionThreadSleepTimeInMiliseconds());
 		return new Integer(m_prop.getProperty("connection.thread.sleep.time", connectionThreadSleepTime)).intValue();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getPingIntervalInMiliseconds()
 	 */
 	public int getPingIntervalInMiliseconds() {
 		final String pingInterval = String.valueOf(m_defaultGGConfiguration.getPingIntervalInMiliseconds());
 		return new Integer(m_prop.getProperty("ping.interval", pingInterval)).intValue();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getSocketTimeoutInMiliseconds()
 	 */
 	public int getSocketTimeoutInMiliseconds() {
 		final String defaultSocketTimeout = String.valueOf(m_defaultGGConfiguration.getSocketTimeoutInMiliseconds());
 		return new Integer(m_prop.getProperty("socket.timeout", defaultSocketTimeout)).intValue();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getRegistrationURL()
 	 */
 	public String getRegistrationURL() {
 		final String registrationURL = m_defaultGGConfiguration.getRegistrationURL();
 		return m_prop.getProperty("server.registration.url", registrationURL);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getSendPasswordURL()
 	 */
 	public String getSendPasswordURL() {
 		final String sendPasswordURL = m_defaultGGConfiguration.getSendPasswordURL();
 		return m_prop.getProperty("send.password.url", sendPasswordURL);
 	}
 
 	/**
 	 * s
 	 * 
 	 * @see pl.radical.open.gg.IGGConfiguration#getServerLookupURL()
 	 */
 	public String getServerLookupURL() {
 		final String serverLookupURL = m_defaultGGConfiguration.getServerLookupURL();
 		return m_prop.getProperty("server.lookup.url", serverLookupURL);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IGGConfiguration#getTokenRequestURL()
 	 */
 	public String getTokenRequestURL() {
 		final String tokenRequestURL = m_defaultGGConfiguration.getServerLookupURL();
 		return m_prop.getProperty("token.request.url", tokenRequestURL);
 	}
 
 	protected abstract Properties createProperties() throws IOException, InvalidPropertiesFormatException;
 
 	protected InputStream getResourceAsStream(final ClassLoader loader, final String name) {
 		return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
 			public Object run() {
 				if (loader != null) {
 					return loader.getResourceAsStream(name);
 				} else {
 					return ClassLoader.getSystemResourceAsStream(name);
 				}
 			}
 		});
 	}
 
 }

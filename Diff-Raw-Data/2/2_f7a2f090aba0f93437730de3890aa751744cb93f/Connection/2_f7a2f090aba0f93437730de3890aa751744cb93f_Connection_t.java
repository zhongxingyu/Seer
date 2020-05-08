 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.connections;
 
 import java.text.MessageFormat;
 
 import net.sf.rcer.conn.Messages;
 import net.sf.rcer.conn.locales.Locale;
 
 /**
  * The actual data of a connection at runtime. This interface comprises the connection data represented by 
  * {@link IConnectionData} and adds the actual client, user name and locale.
  * @author vwegert
  *
  */
 public class Connection extends ConnectionData implements IConnection {
 
 	// TODO add some unit tests for this class
 
 	/**
 	 * The actual client to log on to.
 	 */
 	private String client;
 
 	/**
 	 * The actual user name to use for logon.
 	 */
 	private String userName;
 
 	/**
 	 * The actual locale to use for logon.
 	 */
 	private Locale locale;
 
 	/**
 	 * Default constructor.
 	 */
 	public Connection() {
 		super();
 	}
 
 	/**
 	 * Copy constructor to create a {@link Connection} instance out of a {@link IConnectionData} object.
 	 * @param connectionData
 	 */
 	public Connection(IConnectionData connectionData) {
 		super();
 		setConnectionDataID(connectionData.getConnectionDataID());
 		setDescription(connectionData.getDescription());
 		setSystemID(connectionData.getSystemID());
 		setRouter(connectionData.getRouter());
 		switch(connectionData.getConnectionType()) {
 		case DIRECT:
 			setDirectConnection(connectionData.getApplicationServer(), connectionData.getSystemNumber());
 			break;
 		case LOAD_BALANCED:
 			setLoadBalancingConnection(connectionData.getMessageServer(), 
 					connectionData.getMessageServerPort(), connectionData.getLoadBalancingGroup());
 			break;
 		}
 		setDefaultClient(connectionData.getDefaultClient(), connectionData.isDefaultClientEditable());
 		setDefaultUser(connectionData.getDefaultUser(), connectionData.isDefaultUserEditable());
 		setDefaultLocale(connectionData.getDefaultLocale(), connectionData.isDefaultLocaleEditable());
 		setClient(getDefaultClient());
 		setUserName(getDefaultUser());
 		setLocale(getDefaultLocale()); // TODO set the locale matching the user language if no default locale is set
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#getConnectionID()
 	 */
 	public String getConnectionID() {
 		return MessageFormat.format("{0}$C={1}$U={2}", getConnectionDataID(), getClient(), getUserName()); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#getClient()
 	 */
 	public String getClient() {
 		return client;
 	}
 
 	/**
 	 * @param client the client to set
 	 */
 	public void setClient(String client) {
 		if (!isDefaultClientEditable()) {
 			throw new UnsupportedOperationException(Messages.Connection_DefaultClientUnchangeableError);
 		}
 		final String oldValue = this.client;
 		this.client = client;
 		propertyChangeSupport.firePropertyChange("client", oldValue, client); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#getUserName()
 	 */
 	public String getUserName() {
 		return userName;
 	}
 
 	/**
 	 * @param userName the user name to set
 	 */
 	public void setUserName(String userName) {
 		if (!isDefaultUserEditable()) {
 			throw new UnsupportedOperationException(Messages.Connection_DefaultUserUnchangeable);
 		}
 		final String oldValue = this.userName;
 		this.userName = userName;
 		propertyChangeSupport.firePropertyChange("userName", oldValue, userName); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#getLocale()
 	 */
 	public Locale getLocale() {
 		return locale;
 	}
 
 	/**
 	 * @param locale the locale to set
 	 */
 	public void setLocale(Locale locale) {
 		if (!isDefaultLocaleEditable()) {
 			throw new UnsupportedOperationException(Messages.Connection_DefaultLocaleUnchangeable);
 		}
 		final Locale oldValue = this.locale;
 		this.locale = locale;
 		propertyChangeSupport.firePropertyChange("locale", oldValue, locale); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.ConnectionData#toString()
 	 */
 	@Override
 	public String toString() {
		if (getClient() == null || getClient().equals("")) { //$NON-NLS-1$
 			return MessageFormat.format(Messages.Connection_ShortStringFormat, getSystemID(), getUserName(), getDescription());
 		}
 		return MessageFormat.format(Messages.Connection_LongStringFormat, getSystemID(), getClient(), getUserName(), getDescription());
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#isClientEditable()
 	 */
 	public boolean isClientEditable() {
 		if (getDefaultClient() == null || getDefaultClient().equals("")) { //$NON-NLS-1$
 			return true;
 		}
 		return isDefaultClientEditable();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#isLocaleEditable()
 	 */
 	public boolean isLocaleEditable() {
 		if (getDefaultLocale() == null) {
 			return true;
 		}
 		return isDefaultLocaleEditable();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.connections.IConnection#isUserEditable()
 	 */
 	public boolean isUserEditable() {
 		if (getDefaultUser() == null || getDefaultUser().equals("")) { //$NON-NLS-1$
 			return true;
 		}
 		return isDefaultUserEditable();
 	}
 
 }

 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs;
 
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.nio.Connection;
 
 /**
  * The class <code>Unknown</code> represents a non registered user or server,
  * which will be converted to an <code>User</code> or <code>Server</code> object
  * when the registration is complete. The class is thread-safe.
  * 
  * @author bhuisgen
  * 
  */
 public class Unknown extends Source
 {
 	/**
 	 * Constructs a new <code>Unknown</code> object.
 	 */
 	private Unknown ()
 	{
 		m_rwLock = new ReentrantReadWriteLock ();
 		m_connection = null;
 		m_client = null;
 		m_server = null;
 		m_strName = null;
 		m_strPassword = null;
 		m_strParameters = null;
 	}
 
 	/**
 	 * Create a new unknown source.
 	 * 
 	 * @param connection
 	 *            the connection.
 	 * @param client
 	 *            the client.
 	 * @param server
 	 *            the server.
 	 * @return the new <code>Unknown</code> object of the source.
 	 */
 	public static final Unknown create (Connection connection, Object client,
 			Server server)
 	{
 		if (connection == null)
 			throw new IllegalArgumentException ("invalid connection");
 
 		if (client == null)
 			throw new IllegalArgumentException ("invalid client");
 
 		if (server == null)
 			throw new IllegalArgumentException ("invalid server");
 
 		Unknown unknown = new Unknown ();
 
 		unknown.m_connection = connection;
 		unknown.m_client = client;
 		unknown.m_server = server;
 		unknown.m_strName = IRC.MASK_ANY;
 
 		m_logger.log (Level.FINE, "new unknown source created");
 
 		return (unknown);
 	}
 
 	/**
 	 * Destroys the object.
 	 */
 	public void destroy ()
 	{
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_connection = null;
 			m_client = null;
 			m_server = null;
 			m_strName = null;
 			m_strPassword = null;
 			m_strParameters = null;
 
 			m_logger.log (Level.FINE, "unknown source destroyed");
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#send(fr.hbis.ircs.Message)
 	 */
 	public void send (Message msg)
 	{
 		if ((msg == null) || ("".equals (msg)))
 			throw new IllegalArgumentException ("invalid message");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_connection.write (msg.toString ());
 
 			m_logger.log (Level.INFO, "message sended to unknown source '" + m_strName + "': "
 					+ msg.toString ());
 		}
 		finally
 		{
			m_rwLock.writeLock ().lock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getClient()
 	 */
 	public final Object getClient ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_client);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getServer()
 	 */
 	public final Server getServer ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_server);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getName()
 	 */
 	public final String getName ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strName);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the password of the unknown source.
 	 * 
 	 * @return the password.
 	 */
 	public final String getPassword ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strPassword);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the paramaters of the unknown source.
 	 * 
 	 * @return the parameters array or
 	 *         <code>null<code> if no parameter has been set.
 	 */
 	public final String[] getParameters ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strParameters);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Sets the name of the unknown source.
 	 * 
 	 * @param name
 	 *            the name.
 	 */
 	public final void setName (String name)
 	{
 		if ((name == null) || ("".equals (name)))
 			throw new IllegalArgumentException ("invalid name");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_strName = name;
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Sets the password of the unknown source.
 	 * 
 	 * @param password
 	 *            the password.
 	 */
 	public final void setPassword (String password)
 	{
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_strPassword = password;
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Sets the parameters of the unknown source.
 	 * 
 	 * @param parameters
 	 *            the parameters array.
 	 */
 	public final void setParameters (String[] parameters)
 	{
 		if (parameters == null)
 			throw new IllegalArgumentException ("invalid parameters");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_strParameters = parameters;
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	private ReentrantReadWriteLock m_rwLock;
 	private Connection m_connection;
 	private Object m_client;
 	private Server m_server;
 	private String m_strName;
 	private String m_strPassword;
 	private String[] m_strParameters;
 	private final static Logger m_logger = Logger
 			.getLogger ("fr.hbis.ircs.Unknown");
 }

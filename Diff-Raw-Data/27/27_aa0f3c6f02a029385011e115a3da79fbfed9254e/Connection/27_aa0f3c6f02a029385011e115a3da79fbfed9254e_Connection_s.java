 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.xatmi;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
 import org.jboss.blacktie.jatmibroker.core.transport.Message;
 import org.jboss.blacktie.jatmibroker.core.transport.Receiver;
 import org.jboss.blacktie.jatmibroker.core.transport.Transport;
 import org.jboss.blacktie.jatmibroker.core.transport.TransportFactory;
 
 /**
  * This is the connection to remote Blacktie services. It must be created using
  * the ConnectionFactory
  * 
  * @see ConnectionFactory
  */
 public class Connection {
 
 	private static final Logger log = LogManager.getLogger(Connection.class);
 
 	// AVAILABLE FLAGS
 	public static final int TPNOBLOCK = 0x00000001;
 	public static final int TPSIGRSTRT = 0x00000002;
 	public static final int TPNOREPLY = 0x00000004;
 	public static final int TPNOTRAN = 0x00000008;
 	public static final int TPTRAN = 0x00000010;
 	public static final int TPNOTIME = 0x00000020;
 	public static final int TPGETANY = 0x00000080;
 	public static final int TPNOCHANGE = 0x00000100;
 	public static final int TPCONV = 0x00000400;
 	public static final int TPSENDONLY = 0x00000800;
 	public static final int TPRECVONLY = 0x00001000;
 
 	// ERROR CONDITIONS
 	public static final int TPEBADDESC = 2;
 	public static final int TPEBLOCK = 3;
 	public static final int TPEINVAL = 4;
 	public static final int TPELIMIT = 5;
 	public static final int TPENOENT = 6;
 	public static final int TPEOS = 7;
 	public static final int TPEPROTO = 9;
 	public static final int TPESVCERR = 10;
 	public static final int TPESVCFAIL = 11;
 	public static final int TPESYSTEM = 12;
 	public static final int TPETIME = 13;
 	public static final int TPETRAN = 14;
 	public static final int TPGOTSIG = 15;
 	public static final int TPEITYPE = 17;
 	public static final int TPEOTYPE = 18;
 	public static final int TPEEVENT = 22;
 	public static final int TPEMATCH = 23;
 
 	// SERVICE CONDITIONS
 	public static final short TPFAIL = 0x00000001;
 	public static final short TPSUCCESS = 0x00000002;
 
 	// Events
 	public static final long TPEV_DISCONIMM = 0x0001;
 	public static final long TPEV_SVCERR = 0x0002;
 	public static final long TPEV_SVCFAIL = 0x0004;
 	public static final long TPEV_SVCSUCC = 0x0008;
 	public static final long TPEV_SENDONLY = 0x0020;
 
 	public static final int XATMI_SERVICE_NAME_LENGTH = 15;
 
 	private static int nextId;
 
 	private Map<String, Transport> transports = new HashMap<String, Transport>();
 
 	/**
 	 * Any local temporary queues created in this connection
 	 */
 	private Map<java.lang.Integer, Receiver> temporaryQueues = new HashMap<java.lang.Integer, Receiver>();
 
 	private Properties properties;
 
 	private Map<Integer, Session> sessions = new HashMap<Integer, Session>();
 
 	private static boolean warnedTPSIGRSTRT;
 
 	/**
 	 * The connection
 	 * 
 	 * @param properties
 	 * @param username
 	 * @param password
 	 * @throws ConnectionException
 	 */
 	Connection(Properties properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * Synchronous call
 	 * 
 	 * @param svc
 	 *            The name of the service to call
 	 * @param idata
 	 *            The inbound data
 	 * @param flags
 	 *            The flags to use
 	 * @return The returned buffer
 	 */
 	public Response tpcall(String svc, Buffer buffer, int len, int flags)
 			throws ConnectionException {
 		int cd = tpacall(svc, buffer, len, flags);
 		return receive(cd, flags);
 	}
 
 	/**
 	 * Asynchronous call
 	 * 
 	 * @param svc
 	 *            The name of the service to call
 	 * @param idata
 	 *            The inbound data
 	 * @param flags
 	 *            The flags to use
 	 * @return The connection descriptor
 	 */
 	public int tpacall(String svc, Buffer toSend, int len, int flags)
 			throws ConnectionException {
 
 		boolean hasTPSIGSTRT = (flags & TPSIGRSTRT) != 0;
 		if (hasTPSIGSTRT && !warnedTPSIGRSTRT) {
 			log.error("TPSIGRSTRT NOT SUPPORTED FOR SENDS OR RECEIVES");
 			warnedTPSIGRSTRT = true;
 		}
 		svc = svc.substring(0, Math.min(Connection.XATMI_SERVICE_NAME_LENGTH,
 				svc.length()));
 		int correlationId = 0;
 		synchronized (this) {
 			correlationId = ++nextId;
 		}
 		Transport transport = getTransport(svc);
 		Receiver endpoint = transport.createReceiver();
 		temporaryQueues.put(correlationId, endpoint);
 		// TODO HANDLE TRANSACTION
 		String type = null;
 		String subtype = null;
 		byte[] data = null;
 		if (toSend != null) {
 			toSend.serialize();
 			if (!toSend.equals("X_OCTET")) {
 				len = toSend.getLength();
 			}
 			data = toSend.getData();
 			type = toSend.getType();
 			subtype = toSend.getSubtype();
 		} else {
 			type = "X_OCTET";
 		}
 
 		transport.getSender(svc).send(endpoint.getReplyTo(), (short) 0, 0,
 				data, len, correlationId, flags, type, subtype);
 		if ((flags & Connection.TPNOREPLY) != 0) {
 			correlationId = 0;
 		}
 		return correlationId;
 	}
 
 	/**
 	 * Cancel the outstanding asynchronous call.
 	 * 
 	 * @param cd
 	 *            The connection descriptor
 	 * @param flags
 	 *            The flags to use
 	 */
 	public void tpcancel(int cd, int flags) throws ConnectionException {
 		Receiver endpoint = temporaryQueues.get(cd);
 		endpoint.close();
 	}
 
 	/**
 	 * Get the reply from the server
 	 * 
 	 * @param cd
 	 *            The connection descriptor to use
 	 * @param flags
 	 *            The flags to use
 	 * @return The response from the server
 	 */
 	public Response tpgetrply(int cd, int flags) throws ConnectionException {
 		boolean hasTPSIGSTRT = (flags & TPSIGRSTRT) != 0;
 		if (hasTPSIGSTRT && !warnedTPSIGRSTRT) {
 			log.error("TPSIGRSTRT NOT SUPPORTED FOR SENDS OR RECEIVES");
 			warnedTPSIGRSTRT = true;
 		}
 
 		Response toReturn = receive(cd, flags);
 		Session session = sessions.remove(cd);
 		if (session != null) {
 			session.close();
 		}
 		return toReturn;
 	}
 
 	/**
 	 * Handle the initiation of a conversation with the server
 	 * 
 	 * @param svc
 	 *            The name of the service
 	 * @param idata
 	 *            The outbound buffer
 	 * @param flags
 	 *            The flags to use
 	 * @return The connection descriptor
 	 */
 	public Session tpconnect(String svc, Buffer toSend, int len, int flags)
 			throws ConnectionException {
 
 		svc = svc.substring(0, Math.min(Connection.XATMI_SERVICE_NAME_LENGTH,
 				svc.length()));
 		// Initiate the session
 		boolean hasTPSIGSTRT = (flags & TPSIGRSTRT) != 0;
 		if (hasTPSIGSTRT && !warnedTPSIGRSTRT) {
 			log.error("TPSIGRSTRT NOT SUPPORTED FOR SENDS OR RECEIVES");
 			warnedTPSIGRSTRT = true;
 		}
 		svc = svc.substring(0, Math.min(Connection.XATMI_SERVICE_NAME_LENGTH,
 				svc.length()));
 		int correlationId = 0;
 		synchronized (this) {
 			correlationId = nextId++;
 		}
 		Transport transport = getTransport(svc);
 		Session session = new Session(transport, correlationId);
 		Receiver endpoint = session.getReceiver();
 		temporaryQueues.put(correlationId, endpoint);
 		// TODO HANDLE TRANSACTION
 		String type = null;
 		String subtype = null;
 		byte[] data = null;
 		if (toSend != null) {
 			toSend.serialize();
 			if (!toSend.equals("X_OCTET")) {
 				len = toSend.getLength();
 			}
 			data = toSend.getData();
 			type = toSend.getType();
 			subtype = toSend.getSubtype();
 		} else {
 			type = "X_OCTET";
 		}
 
 		transport.getSender(svc).send(endpoint.getReplyTo(), (short) 0, 0,
 				data, len, correlationId, flags, type, subtype);
 		// Return a handle to allow the connection to send/receive data on
 		sessions.put(correlationId, session);
 		return session;
 	}
 
 	/**
 	 * Close any resources associated with this connection
 	 * 
 	 * @throws ConnectionException
 	 */
 	public void close() throws ConnectionException {
 		Iterator<Receiver> receivers = temporaryQueues.values().iterator();
 		while (receivers.hasNext()) {
 			Receiver receiver = receivers.next();
 			receiver.close();
 		}
 		temporaryQueues.clear();
 		Iterator<Transport> transports = this.transports.values().iterator();
 		while (transports.hasNext()) {
 			Transport transport = transports.next();
 			transport.close();
 		}
 		this.transports.clear();
 	}
 
 	private Transport getTransport(String serviceName)
 			throws ConnectionException {
 		Transport toReturn = transports.get(serviceName);
 		if (toReturn == null) {
 			try {
 				toReturn = TransportFactory.loadTransportFactory(serviceName,
 						properties).createTransport();
 			} catch (ConfigurationException e) {
				throw new ConnectionException(-1,
 						"Could not load transport for: " + serviceName, e);
 			}
 			transports.put(serviceName, toReturn);
 		}
 		return toReturn;
 	}
 
 	private Response receive(int cd, int flags) throws ConnectionException {
 		Receiver endpoint = temporaryQueues.remove(cd);
 		if (endpoint == null) {
 			throw new ConnectionException(Connection.TPEBADDESC,
 					"Session does not exist");
 		}
 		Message m = endpoint.receive(flags);
 		// TODO WE SHOULD BE SENDING THE CONNECTION ID?
 		Buffer received = new Buffer(m.type, m.subtype);
 		received.setData(m.data);
 		Response response = new Response(m.rval, m.rcode, received, m.len,
 				m.flags);
 		if (m.rval == Connection.TPFAIL) {
 			throw new ConnectionException(m.rcode, 0L, m.rcode,
 					"Got a fail back from the remote service", received);
 		} else {
 			return response;
 		}
 	}
 }

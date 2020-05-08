 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Department of Computer Science, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import ch.ethz.iks.r_osgi.ChannelEndpoint;
 import ch.ethz.iks.r_osgi.NetworkChannelFactory;
 import ch.ethz.iks.r_osgi.RemoteOSGiMessage;
 import ch.ethz.iks.r_osgi.RemoteOSGiException;
 import ch.ethz.iks.r_osgi.RemoteOSGiService;
 import ch.ethz.iks.r_osgi.NetworkChannel;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 /**
  * <p>
  * The endpoint of a network channel encapsulates most of the communication
  * logic like sending of messages, service method invocation, timestamp
  * synchronization, and event delivery.
  * </p>
  * <p>
  * Endpoints exchange symmetric leases when they are established. These leases
  * contain the statements of supply and demand. The peer states the services it
  * offers and the event topics it is interested in. Whenever one of these
  * statements undergo a change, the lease has to be renewed. Leases expire with
  * the closing of the network channel and the two bound endpoints.
  * </p>
  * <p>
  * The network transport of channels is modular and exchangeable. Services can
  * state the supported protocols in their service url. R-OSGi maintains a list
  * of network channel factories and the protocols they support. Each channel
  * uses exactly one protocol.
  * <p>
  * <p>
  * When the network channel breaks down, the channel endpoint tries to reconnect
  * and to restore the connection. If this is not possible (for instance, because
  * the other endpoint is not available any more, the endpoint is unregistered.
  * </p>
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  */
 public final class ChannelEndpointImpl implements ChannelEndpoint {
 
 	/**
 	 * the channel.
 	 */
 	private NetworkChannel networkChannel;
 
 	/**
 	 * the services provided by the OSGi framework holding the remote channel
 	 * endpoint.
 	 */
 	private String[] remoteServices;
 
 	/**
 	 * the topics of interest of the OSGi framework holding the remote channel
 	 * endpoint.
 	 */
 	private String[] remoteTopics;
 
 	/**
 	 * the time offset between this peer's local time and the local time of the
 	 * remote channel endpoint.
 	 */
 	private TimeOffset timeOffset;
 
 	/**
 	 * Timeout.
 	 */
 	private static final int TIMEOUT = 120000;
 
 	/**
 	 * the receiver queue.
 	 */
 	private final Map receiveQueue = new HashMap(0);
 
 	/**
 	 * map of service url -> attribute dictionary.
 	 */
 	private final HashMap attributes = new HashMap(0);
 
 	/**
 	 * map of service url -> service object.
 	 */
 	private final HashMap services = new HashMap(2);
 
 	/**
 	 * a list of all registered proxy bundle. If the endpoint is closed, the
 	 * proxies are unregistered.
 	 */
 	private final List proxies = new ArrayList(0);
 
 	/**
 	 * 
 	 */
 	private ServiceRegistration handlerReg = null;
 
 	/**
 	 * keeps track if the channel endpoint has lost its connection to the other
 	 * endpoint.
 	 */
 	private boolean lostConnection = false;
 
 	/**
 	 * dummy object used for blocking method calls until the result message has
 	 * arrived.
 	 */
 	private static final Object WAITING = new Object();
 
 	/**
 	 * filter for events to prevent loops in the remote delivery if the peers
 	 * connected by this channel have non-disjoint topic spaces.
 	 */
 	private static final String NO_LOOPS = "(!("
 			+ RemoteOSGiServiceImpl.EVENT_SENDER_URL + "=*))";
 
 	/**
 	 * the TCPChannel factory.
 	 */
 	private static final TCPChannelFactory TCP_FACTORY = new TCPChannelFactory();
 
 	/**
 	 * create a new channel endpoint.
 	 * 
 	 * @param connection
 	 *            the transport channel.
 	 * @throws RemoteOSGiException
 	 * @throws IOException
 	 */
 	ChannelEndpointImpl(final NetworkChannelFactory factory,
 			final InetAddress address, final int port, final String protocol)
 			throws RemoteOSGiException, IOException {
 		this.networkChannel = (factory == null ? TCP_FACTORY : factory)
 				.getConnection(this, address, port, protocol);
 		renewLease(RemoteOSGiServiceImpl.getServices(), RemoteOSGiServiceImpl
 				.getTopics());
 		RemoteOSGiServiceImpl.registerChannel(this);
 	}
 
 	ChannelEndpointImpl(Socket socket) throws IOException {
 		System.out.println("GOING TO ACCEPT NEW CONNECTION "
 				+ socket.getInetAddress() + ":" + socket.getPort());
 		this.networkChannel = TCP_FACTORY.bind(this, socket);
 		System.out.println("HAVE ACCEPTED CONNECTION " + networkChannel);
 		RemoteOSGiServiceImpl.registerChannel(this);
 	}
 
 	/**
 	 * get the channel ID.
 	 * 
 	 * @return the channel ID.
 	 */
 	public String getID() {
 		final String protocol = networkChannel.getProtocol();
 		return (protocol != null ? protocol : "r-osgi") + "://"
 				+ networkChannel.getInetAddress().getHostAddress() + ":"
 				+ networkChannel.getPort();
 	}
 
 	/**
 	 * renew the lease.
 	 * 
 	 * @param services
 	 *            the (local) services.
 	 * @param topics
 	 *            the (local) topics.
 	 * @throws RemoteOSGiException
 	 *             if the channel cannot be established.
 	 */
 	void renewLease(final String[] services, final String[] topics)
 			throws RemoteOSGiException {
 		final LeaseMessage lease = (LeaseMessage) sendMessage(new LeaseMessage(
 				services, topics));
 		updateStatements(lease);
 	}
 
 	/**
 	 * update the statements of suppy and demand.
 	 * 
 	 * @param lease
 	 *            the original lease.
 	 */
 	private void updateStatements(final LeaseMessage lease) {
 		final String[] remoteServices = lease.getServices();
 		if (remoteServices != null) {
 			this.remoteServices = remoteServices;
 		}
 		final String[] theTopics = lease.getTopics();
 		if (theTopics != null) {
 			remoteTopics = theTopics;
 
 			if (handlerReg == null) {
 				if (theTopics.length == 0) {
 					// no change
 				} else {
 					// register handler
 					final Dictionary properties = new Hashtable();
 					properties.put(EventConstants.EVENT_TOPIC, theTopics);
 					properties.put(EventConstants.EVENT_FILTER, NO_LOOPS);
 					properties.put(RemoteOSGiServiceImpl.R_OSGi_INTERNAL,
 							Boolean.TRUE);
 					handlerReg = RemoteOSGiServiceImpl.context.registerService(
 							EventHandler.class.getName(), new EventForwarder(),
 							properties);
 				}
 			} else {
 				if (theTopics.length == 0) {
 					// unregister handler
 					handlerReg.unregister();
 					handlerReg = null;
 				} else {
 					// update topics
 					final Dictionary properties = new Hashtable();
 					properties.put(EventConstants.EVENT_TOPIC, theTopics);
 					properties.put(EventConstants.EVENT_FILTER, NO_LOOPS);
 					properties.put(RemoteOSGiServiceImpl.R_OSGi_INTERNAL,
 							Boolean.TRUE);
 					handlerReg.setProperties(properties);
 				}
 			}
 		}
 
 		System.out.println();
 		System.out.println("NEW REMOTE TOPIC SPACE "
 				+ java.util.Arrays.asList(theTopics));
 	}
 
 	/**
 	 * get the services of the remote channel endpoint.
 	 * 
 	 * @return the services.
 	 */
 	String[] getRemoteServices() {
 		return remoteServices;
 	}
 
 	/**
 	 * get the remote topics that the remote channel endpoint is interested in.
 	 * 
 	 * @return the topics.
 	 */
 	String[] getRemoteTopics() {
 		return remoteTopics;
 	}
 
 	/**
 	 * invoke a method on the remote host. This function is used by all proxy
 	 * bundles.
 	 * 
 	 * @param service
 	 *            the service URL.
 	 * @param methodSignature
 	 *            the method signature.
 	 * @param args
 	 *            the method parameter.
 	 * @return the result of the remote method invokation.
 	 * @see ch.ethz.iks.r_osgi.Remoting#invokeMethod(java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.Object[])
 	 * @since 0.2
 	 * @category Remoting
 	 */
 	public Object invokeMethod(final String service,
 			final String methodSignature, final Object[] args) throws Throwable {
 		final InvokeMethodMessage invokeMsg = new InvokeMethodMessage(service
 				.toString(), methodSignature, args);
 
 		try {
 			// send the message and get a MethodResultMessage in return
 			final MethodResultMessage result = (MethodResultMessage) sendMessage(invokeMsg);
 			if (result.causedException()) {
 				throw result.getException();
 			}
 			return result.getResult();
 		} catch (RemoteOSGiException e) {
 			throw new RemoteOSGiException("Method invocation of "
 					+ methodSignature + " failed.", e);
 		}
 	}
 
 	/**
 	 * fetch the service from the remote peer.
 	 * 
 	 * @param service
 	 *            the service url.
 	 * @throws IOException
 	 *             in case of network errors.
 	 * @throws BundleException
 	 *             if the installation of the proxy or the migrated bundle
 	 *             fails.
 	 */
 	void fetchService(final ServiceURL service) throws IOException,
 			BundleException {
 		// build the FetchServiceMessage
 		final FetchServiceMessage fetchReq = new FetchServiceMessage(service);
 
 		// send the FetchServiceMessage and get a DeliverServiceMessage in
 		// return
 		final RemoteOSGiMessageImpl msg = sendMessage(fetchReq);
 		if (msg instanceof DeliverServiceMessage) {
 			final DeliverServiceMessage deliv = (DeliverServiceMessage) msg;
 			try {
 				final ServiceURL url = new ServiceURL(deliv.getServiceURL(), 0);
 
 				// set the REMOTE_HOST_PROPERTY
 				final Dictionary attribs = deliv.getAttributes();
 				attribs.put(RemoteOSGiServiceImpl.REMOTE_HOST, service
 						.getHost());
 
 				// remove the service PID, if set
 				attribs.remove("service.pid");
 
 				// remove the R-OSGi registration property
 				attribs.remove(RemoteOSGiService.R_OSGi_REGISTRATION);
 
 				attributes.put(url.toString(), attribs);
 
 				// generate a proxy bundle for the service
 				// TODO: redesign ProxyGenerator to be static
 				final String bundleLocation = new ProxyGenerator()
 						.generateProxyBundle(url, deliv);
 
 				// install the proxy bundle
 				final Bundle bundle = RemoteOSGiServiceImpl.context
 						.installBundle("file:" + bundleLocation);
 
 				// store the bundle for cleanup
 				proxies.add(bundle);
 
 				// start the bundle
 				bundle.start();
 			} catch (ServiceLocationException sle) {
 				throw new RemoteOSGiException("ServiceURL "
 						+ deliv.getServiceURL() + " is not valid.");
 			}
 		} else {
 			final DeliverBundleMessage delivB = (DeliverBundleMessage) msg;
 
 			Bundle bundle = RemoteOSGiServiceImpl.context.installBundle(
 					"r-osgi://" + service.toString(), new ByteArrayInputStream(
 							delivB.getBundle()));
 
 			bundle.start();
 		}
 	}
 
 	/**
 	 * send a RemoteOSGiMessage.
 	 * 
 	 * @param msg
 	 *            the message.
 	 * @return the reply message.
 	 * @throws RemoteOSGiException
 	 *             in case of network errors.
 	 */
 	RemoteOSGiMessageImpl sendMessage(final RemoteOSGiMessageImpl msg)
 			throws RemoteOSGiException {
 		Throwable lastException = null;
 		// TODO think of setting retry count by property ?
 		int retryCounter = 3;
 
 		// if this is a reply message, the xid is
 		// the same as the incoming message. Otherwise
 		// we assign the next xid.
 		if (msg.xid == 0) {
 			msg.xid = RemoteOSGiServiceImpl.nextXid();
 		}
 
 		final Integer xid = new Integer(msg.xid);
 		if (!(msg instanceof RemoteEventMessage)) {
 			synchronized (receiveQueue) {
 				receiveQueue.put(xid, WAITING);
 			}
 		}
 
 		while (retryCounter > 0) {
 			try {
 				// send the message
 				dispatchMessage(msg);
 				if (msg instanceof RemoteEventMessage) {
 					return null;
 				}
 
 				Object reply;
 				synchronized (receiveQueue) {
 					reply = receiveQueue.get(xid);
 					final long timeout = System.currentTimeMillis() + TIMEOUT;
 					while (!lostConnection && reply == WAITING
 							&& System.currentTimeMillis() < timeout) {
 						receiveQueue.wait(TIMEOUT);
 						reply = receiveQueue.get(xid);
 					}
 					receiveQueue.remove(xid);
 
 					if (lostConnection) {
 						throw new RemoteOSGiException(
 								"Method Incovation failed, lost connection");
 					}
 					if (reply == WAITING) {
 						throw new RemoteOSGiException(
 								"Method Invocation failed.");
 					} else {
 						return (RemoteOSGiMessageImpl) reply;
 					}
 				}
 			} catch (Throwable t) {
 				// TODO: remove
 				t.printStackTrace();
 
 				lastException = t;
 				recoverConnection();
 
 				// TimeOffsetMessages have to be handled differently
 				// must send a new message with a new timestamp and XID instead
 				// of sending the same message again
 				if (msg instanceof TimeOffsetMessage) {
 					((TimeOffsetMessage) msg).restamp(RemoteOSGiServiceImpl
 							.nextXid());
 				} else {
 					// send / receive reply failed
 					retryCounter--;
 					// reset connection
 				}
 			}
 		}
 		if (lastException != null) {
 			throw new RemoteOSGiException("Network error", lastException);
 		}
 		return null;
 	}
 
 	/**
 	 * get the temporal offset of a remote peer.
 	 * 
 	 * @return the TimeOffset.
 	 * @throws RemoteOSGiException
 	 *             in case of network errors.
 	 */
 	TimeOffset getOffset() throws RemoteOSGiException {
 		if (timeOffset == null) {
 			// if unknown, perform a initial offset measurement round of 4
 			// messages
 			TimeOffsetMessage timeMsg = new TimeOffsetMessage();
 			for (int i = 0; i < 4; i++) {
 				timeMsg.timestamp();
 				timeMsg = (TimeOffsetMessage) sendMessage(timeMsg);
 			}
 			timeOffset = new TimeOffset(timeMsg.getTimeSeries());
 		} else if (timeOffset.isExpired()) {
 			// if offset has expired, start a new measurement round
 			TimeOffsetMessage timeMsg = new TimeOffsetMessage();
 			for (int i = 0; i < timeOffset.seriesLength(); i += 2) {
 				timeMsg.timestamp();
 				timeMsg = (TimeOffsetMessage) sendMessage(timeMsg);
 			}
 			timeOffset.update(timeMsg.getTimeSeries());
 		}
 		return timeOffset;
 	}
 
 	/**
 	 * message handler method.
 	 * 
 	 * @param msg
 	 *            the incoming message.
 	 * @return if reply is created, null otherwise.
 	 * @throws RemoteOSGiException
 	 *             if something goes wrong.
 	 */
 	private RemoteOSGiMessage handleMessage(final RemoteOSGiMessage msg)
 			throws RemoteOSGiException {
 
 		switch (msg.getFuncID()) {
 		// requests
 		case RemoteOSGiMessageImpl.LEASE: {
 			final LeaseMessage lease = (LeaseMessage) msg;
 			updateStatements(lease);
 
 			if (lease.getXID() == 0) {
 				return null;
 			}
 
 			return lease.replyWith(RemoteOSGiServiceImpl.getServices(),
 					RemoteOSGiServiceImpl.getTopics());
 		}
 		case RemoteOSGiMessageImpl.FETCH_SERVICE: {
 			try {
 				final FetchServiceMessage fetchReq = (FetchServiceMessage) msg;
 				final ServiceURL url = new ServiceURL(fetchReq.getServiceURL(),
 						0);
 
 				final RemoteServiceRegistration reg;
 				if (!"".equals(url.getURLPath())) {
 					reg = RemoteOSGiServiceImpl.getService(url);
 				} else {
 					reg = RemoteOSGiServiceImpl.getAnyService(url);
 				}
 
 				if (reg == null) {
 					throw new IllegalStateException("Could not get "
 							+ fetchReq.getServiceURL());
 				}
 
 				if (reg instanceof ProxiedServiceRegistration) {
 					services.put(url.toString(), reg);
 					return ((ProxiedServiceRegistration) reg)
 							.getMessage(fetchReq);
 				} else if (reg instanceof BundledServiceRegistration) {
 					return new DeliverBundleMessage(fetchReq,
 							(BundledServiceRegistration) reg);
 				}
 			} catch (ServiceLocationException sle) {
 				sle.printStackTrace();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 		case RemoteOSGiMessageImpl.INVOKE_METHOD: {
 			final InvokeMethodMessage invMsg = (InvokeMethodMessage) msg;
 			try {
 				final ProxiedServiceRegistration serv = (ProxiedServiceRegistration) services
 						.get(invMsg.getServiceURL());
 				if (serv == null) {
 					System.err.println("REQUESTED " + invMsg.getServiceURL());
 					System.err.println("IN CACHE: " + services);
 					throw new IllegalStateException("Could not get "
 							+ invMsg.getServiceURL());
 				}
 
 				// get the invokation arguments and the local method
 				final Object[] arguments = invMsg.getArgs();
 				final Method method = serv.getMethod(invMsg
 						.getMethodSignature());
 
 				// invoke method
 				try {
 					final Object result = method.invoke(
 							serv.getServiceObject(), arguments);
 					return new MethodResultMessage(invMsg, result);
 				} catch (InvocationTargetException t) {
 					throw t.getTargetException();
 				}
 			} catch (Throwable t) {
 				return new MethodResultMessage(invMsg, t);
 			}
 		}
 		case RemoteOSGiMessageImpl.REMOTE_EVENT: {
 			final RemoteEventMessage eventMsg = (RemoteEventMessage) msg;
 
 			final Event event = eventMsg.getEvent(this);
 
 			System.out.println("RECEIVED REMOTE EVENT " + event);
 
 			// and deliver the event to the local framework
 			if (RemoteOSGiServiceImpl.eventAdmin != null) {
 				RemoteOSGiServiceImpl.eventAdmin.postEvent(event);
 			} else {
 				System.err.println("Could not deliver received event: " + event
 						+ ". No EventAdmin available.");
 			}
 			return null;
 		}
 		case RemoteOSGiMessageImpl.TIME_OFFSET: {
 			// add timestamp to the message and return the message to sender
 			((TimeOffsetMessage) msg).timestamp();
 			return msg;
 		}
 		default:
 			throw new RemoteOSGiException("Unimplemented message " + msg);
 		}
 	}
 
 	/**
 	 * send a message over the channel.
 	 * 
 	 * @param msg
 	 *            the message.
 	 * @throws IOException
 	 */
 	void dispatchMessage(final RemoteOSGiMessage msg) throws IOException {
 		networkChannel.sendMessage(msg);
 	}
 
 	/**
 	 * dispose the channel.
 	 */
 	public void dispose() {
 		System.out.println("DISPOSING ENDPOINT " + getID());
 		RemoteOSGiServiceImpl.unregisterChannel(this);
 		Bundle[] bundles = (Bundle[]) proxies
 				.toArray(new Bundle[proxies.size()]);
 		for (int i = 0; i < bundles.length; i++) {
 			try {
 				if (bundles[i].getState() == Bundle.ACTIVE) {
 					bundles[i].uninstall();
 				}
 			} catch (BundleException e) {
 				// don't care
 			}
 		}
 	}
 
 	/**
 	 * Try to recover a broken-down connection.
 	 * 
 	 * @return true, if the connection could be recovered.
 	 */
 	boolean recoverConnection() {
 		lostConnection = true;
 
 		try {
 			networkChannel.reconnect();
 			lostConnection = false;
 			System.out.println("Channel " + getID() + " recovery successful");
 		} catch (IOException ioe) {
 			System.err.println("Recovery failed: " + ioe.getMessage());
 			dispose();
 			return true;
 		} finally {
 			synchronized (receiveQueue) {
 				receiveQueue.notifyAll();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * process a recieved message. Called by the channel.
 	 * 
 	 * @param msg
 	 *            the received message.
 	 */
 	public void receivedMessage(final RemoteOSGiMessage msg) {
 		if (msg == null) {
 			System.out.println("RECEIVED POISONED INPUT MESSAGE");
 			System.out.println("STARTING RECOVERY ...");
 			recoverConnection();
 			return;
 		}
 		final Integer xid = new Integer(msg.getXID());
 		synchronized (receiveQueue) {
 			final Object state = receiveQueue.get(xid);
 			if (state == WAITING) {
 				receiveQueue.put(xid, msg);
 				receiveQueue.notifyAll();
 				return;
 			} else {
 				RemoteOSGiMessage reply = handleMessage(msg);
 				if (reply != null) {
 					try {
 						dispatchMessage(reply);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * get the attributes of a service. This function is used to simplify proxy
 	 * bundle generation.
 	 * 
 	 * @param serviceURL
 	 *            the serviceURL of the remote service.
 	 * @return the service attributes.
 	 * @see ch.ethz.iks.r_osgi.Remoting#getAttributes(java.lang.String)
 	 * @since 0.2
 	 * @category Remoting
 	 */
 	public Dictionary getAttributes(final String serviceURL) {
 		return (Dictionary) attributes.get(serviceURL);
 	}
 
 	/**
 	 * get the attributes for the presentation of the service. This function is
 	 * used by proxies that support ServiceUI presentations.
 	 * 
 	 * @param serviceURL
 	 *            the serviceURL of the remote service.
 	 * @return the presentation attributes.
 	 * @see ch.ethz.iks.r_osgi.Remoting#getPresentationAttributes(java.lang.String)
 	 * @since 0.4
 	 * @category Remoting
 	 */
 	public Dictionary getPresentationAttributes(final String serviceURL) {
 		final Dictionary attribs = new Hashtable();
 		try {
 			attribs.put(RemoteOSGiServiceImpl.REMOTE_HOST, new ServiceURL(
 					serviceURL, 0).getHost());
 			attribs.put(RemoteOSGiService.PRESENTATION,
 					((Dictionary) attributes.get(serviceURL))
 							.get(RemoteOSGiService.PRESENTATION));
 		} catch (ServiceLocationException sle) {
 			throw new IllegalArgumentException("ServiceURL " + serviceURL
 					+ " is invalid.");
 		}
 		return attribs;
 	}
 
 	/**
 	 * forwards events over the channel to the remote peer.
 	 * 
 	 * @author Jan S. Rellermeyer, ETH Zurich
 	 * @category EventHandler
 	 */
 	private final class EventForwarder implements EventHandler {
 
 		public void handleEvent(Event event) {
 			try {
 				sendMessage(new RemoteEventMessage(event, getID()));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }

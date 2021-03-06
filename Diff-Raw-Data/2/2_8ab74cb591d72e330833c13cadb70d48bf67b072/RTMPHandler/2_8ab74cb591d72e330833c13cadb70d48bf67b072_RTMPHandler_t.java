 package org.red5.server.net.rtmp;
 
 /*
  * RED5 Open Source Flash Server - http://www.osflash.org/red5
  * 
  * Copyright (c) 2006 by respective authors (see below). All rights reserved.
  * 
  * This library is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License as published by the Free Software 
  * Foundation; either version 2.1 of the License, or (at your option) any later 
  * version. 
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along 
  * with this library; if not, write to the Free Software Foundation, Inc., 
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 
 import static org.red5.server.api.ScopeUtils.getScopeService;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.mina.common.ByteBuffer;
 import org.red5.server.api.IContext;
 import org.red5.server.api.IGlobalScope;
 import org.red5.server.api.IScope;
 import org.red5.server.api.IScopeHandler;
 import org.red5.server.api.IServer;
 import org.red5.server.api.Red5;
 import org.red5.server.api.event.IEventDispatcher;
 import org.red5.server.api.service.IPendingServiceCall;
 import org.red5.server.api.service.IPendingServiceCallback;
 import org.red5.server.api.service.IServiceCall;
 import org.red5.server.api.so.ISharedObject;
 import org.red5.server.api.so.ISharedObjectService;
 import org.red5.server.api.stream.IClientBroadcastStream;
 import org.red5.server.api.stream.IClientStream;
 import org.red5.server.api.stream.IStreamService;
 import org.red5.server.exception.ClientRejectedException;
 import org.red5.server.exception.ScopeNotFoundException;
 import org.red5.server.messaging.IConsumer;
 import org.red5.server.messaging.OOBControlMessage;
 import org.red5.server.net.protocol.ProtocolState;
 import org.red5.server.net.rtmp.codec.RTMP;
 import org.red5.server.net.rtmp.event.BytesRead;
 import org.red5.server.net.rtmp.event.ChunkSize;
 import org.red5.server.net.rtmp.event.IRTMPEvent;
 import org.red5.server.net.rtmp.event.Invoke;
 import org.red5.server.net.rtmp.event.Notify;
 import org.red5.server.net.rtmp.event.Ping;
 import org.red5.server.net.rtmp.event.Unknown;
 import org.red5.server.net.rtmp.message.Constants;
 import org.red5.server.net.rtmp.message.Header;
 import org.red5.server.net.rtmp.message.Packet;
 import org.red5.server.net.rtmp.status.StatusCodes;
 import org.red5.server.net.rtmp.status.StatusObject;
 import org.red5.server.net.rtmp.status.StatusObjectService;
 import org.red5.server.service.Call;
 import org.red5.server.so.ISharedObjectEvent;
 import org.red5.server.so.SharedObjectEvent;
 import org.red5.server.so.SharedObjectMessage;
 import org.red5.server.so.SharedObjectService;
 import org.red5.server.stream.IBroadcastScope;
 import org.red5.server.stream.IStreamFlow;
 import org.red5.server.stream.PlaylistSubscriberStream;
 import org.red5.server.stream.StreamService;
 
 public class RTMPHandler implements IRTMPHandler, Constants, StatusCodes {
 
 	protected static Log log = LogFactory.getLog(RTMPHandler.class.getName());
 
 	protected StatusObjectService statusObjectService;
 
 	protected IServer server;
 
 	// XXX: HACK HACK HACK to support stream ids
 	private static ThreadLocal<Integer> streamLocal = new ThreadLocal<Integer>();
 
 	public void setServer(IServer server) {
 		this.server = server;
 	}
 
 	public void setStatusObjectService(StatusObjectService statusObjectService) {
 		this.statusObjectService = statusObjectService;
 	}
 
 	// XXX: HACK HACK HACK to support stream ids
 	public static int getStreamId() {
 		return streamLocal.get().intValue();
 	}
 
 	private static void setStreamId(int id) {
 		streamLocal.set(id);
 	}
 
 	public void connectionOpened(RTMPConnection conn, RTMP state) {
 		// Nothing to do here...
 	}
 	
 	public void messageReceived(RTMPConnection conn, ProtocolState state,
 			Object in) throws Exception {
 
 		IRTMPEvent message = null;
 		try {
 
 			final Packet packet = (Packet) in;
 			message = packet.getMessage();
 			final Header header = packet.getHeader();
 			final Channel channel = conn.getChannel(header.getChannelId());
 			final IClientStream stream = conn.getStreamById(header
 					.getStreamId());
 
 			if (log.isDebugEnabled()) {
 				log.debug("Message recieved");
 				log.debug("Stream Id: " + header);
 				log.debug("Channel: " + channel);
 			}
 
 			// Thread local performance ? Should we benchmark
 			Red5.setConnectionLocal(conn);
 
 			// XXX: HACK HACK HACK to support stream ids
 			RTMPHandler.setStreamId(header.getStreamId());
 
 			// Increase number of received messages
 			conn.messageReceived();
 
 			//if (message instanceof IRTMPEvent) {
 			message.setSource(conn);
 			//}
 
 			switch (header.getDataType()) {
 				case TYPE_CHUNK_SIZE:
 					onChunkSize(conn, channel, header, (ChunkSize) message);
 					break;
 
 				case TYPE_INVOKE:
 					onInvoke(conn, channel, header, (Invoke) message);
 					if(message.getHeader().getStreamId()!=0  
 							&& ((Invoke)message).getCall().getServiceName()==null
 							&& ACTION_PUBLISH.equals(((Invoke)message).getCall().getServiceMethodName())) {
 						IClientStream s = conn.getStreamById(header.getStreamId());
 						if (s != null)
 							// Only dispatch if stream really was created 
 							((IEventDispatcher) s).dispatchEvent(message);
 					}
 					break;
 
 				case TYPE_NOTIFY: // just like invoke, but does not return
 					if (((Notify) message).getData() != null && stream != null) {
 						// Stream metadata
 						((IEventDispatcher) stream).dispatchEvent(message);
 					} else {
 						onInvoke(conn, channel, header, (Notify) message);
 					}
 					break;
 				case TYPE_PING:
 					onPing(conn, channel, header, (Ping) message);
 					break;
 
 				case TYPE_BYTES_READ:
 					onStreamBytesRead(conn, channel, header,
 							(BytesRead) message);
 					break;
 
 				case TYPE_AUDIO_DATA:
 				case TYPE_VIDEO_DATA:
 					// log.info("in packet: "+source.getSize()+"
 					// ts:"+source.getTimer());
 					
 					// NOTE: If we respond to "publish" with "NetStream.Publish.BadName",
 					// the client sends a few stream packets before stopping. We need to
 					// ignore them.
 					if (stream != null)
 						((IEventDispatcher) stream).dispatchEvent(message);
 					break;
 				case TYPE_SHARED_OBJECT:
 					onSharedObject(conn, channel, header,
 							(SharedObjectMessage) message);
 					break;
 			}
 			if (message instanceof Unknown) {
 				log.info(message);
 			}
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			log.error("Exception", e);
 		}
 		if (message != null) {
 			message.release();
 		}
 	}
 
 	public void messageSent(RTMPConnection conn, Object message) {
 		if (log.isDebugEnabled()) {
 			log.debug("Message sent");
 		}
 
 		if (message instanceof ByteBuffer) {
 			return;
 		}
 
 		// Increase number of sent messages
 		conn.messageSent((Packet) message);
 
 		Packet sent = (Packet) message;
 		final byte channelId = sent.getHeader().getChannelId();
 		final IClientStream stream = conn.getStreamByChannelId(channelId);
 		// XXX we'd better use new event model for notification
 		if (stream != null && (stream instanceof PlaylistSubscriberStream)) {
 			((PlaylistSubscriberStream) stream).written(sent.getMessage());
 		}
 	}
 
 	public void connectionClosed(RTMPConnection conn, RTMP state) {
 		state.setState(RTMP.STATE_DISCONNECTED);
 		conn.close();
 	}
 
 	public void onChunkSize(RTMPConnection conn, Channel channel,
 			Header source, ChunkSize chunkSize) {
 		for (IClientStream stream : conn.getStreams()) {
 			if (stream instanceof IClientBroadcastStream) {
 				IClientBroadcastStream bs = (IClientBroadcastStream) stream;
 				IBroadcastScope scope = (IBroadcastScope) bs.getScope()
 						.getBasicScope(IBroadcastScope.TYPE,
 								bs.getPublishedName());
 				if (scope == null) {
 					continue;
 				}
 
 				OOBControlMessage setChunkSize = new OOBControlMessage();
 				setChunkSize.setTarget("ClientBroadcastStream");
 				setChunkSize.setServiceName("chunkSize");
 				if (setChunkSize.getServiceParamMap() == null) {
 					setChunkSize.setServiceParamMap(new HashMap());
 				}
 				setChunkSize.getServiceParamMap().put("chunkSize",
 						chunkSize.getSize());
 				scope.sendOOBControlMessage((IConsumer) null, setChunkSize);
 				if (log.isDebugEnabled()) {
 					log.debug("Sending chunksize " + chunkSize + " to " + bs.getProvider());
 				}
 			}
 		}
 	}
 
 	public void invokeCall(RTMPConnection conn, IServiceCall call) {
 		final IScope scope = conn.getScope();
 		if (scope.hasHandler()) {
 			final IScopeHandler handler = scope.getHandler();
 			if (log.isDebugEnabled()) {
 				log.debug("Scope: " + scope);
 				log.debug("Handler: " + handler);
 			}
 			if (!handler.serviceCall(conn, call)) {
 				// XXX: What do do here? Return an error?
 				return;
 			}
 		}
 
 		final IContext context = scope.getContext();
 		if (log.isDebugEnabled()) {
 			log.debug("Context: " + context);
 		}
 		context.getServiceInvoker().invoke(call, scope);
 	}
 
 	private void invokeCall(RTMPConnection conn, IServiceCall call,
 			Object service) {
 		final IScope scope = conn.getScope();
 		final IContext context = scope.getContext();
 		if (log.isDebugEnabled()) {
 			log.debug("Scope: " + scope);
 			log.debug("Service: " + service);
 			log.debug("Context: " + context);
 		}
 		context.getServiceInvoker().invoke(call, service);
 	}
 
 	// ------------------------------------------------------------------------------
 
 	protected String getHostname(String url) {
 		if (log.isDebugEnabled()) {
 			log.debug("url: " + url);
 		}
 		String[] parts = url.split("/");
 		if (parts.length == 2) {
 			// TODO: is this a good default hostname?
 			return "";
 		} else {
 			return parts[2];
 		}
 	}
 
 	public void onInvoke(RTMPConnection conn, Channel channel, Header source,
 			Notify invoke) {
 
 		log.debug("Invoke");
 
 		final IServiceCall call = invoke.getCall();
 		if (call.getServiceMethodName().equals("_result")
 				|| call.getServiceMethodName().equals("_error")) {
 			final IPendingServiceCall pendingCall = conn.getPendingCall(invoke
 					.getInvokeId());
 			if (pendingCall != null) {
 				// The client sent a response to a previously made call.
 				Object[] args = call.getArguments();
 				if ((args != null) && (args.length > 0)) {
 					// TODO: can a client return multiple results?
 					pendingCall.setResult(args[0]);
 				}
 
 				Set<IPendingServiceCallback> callbacks = pendingCall
 						.getCallbacks();
 				if (callbacks.isEmpty()) {
 					return;
 				}
 
 				HashSet<IPendingServiceCallback> tmp = new HashSet<IPendingServiceCallback>();
 				tmp.addAll(callbacks);
 				Iterator<IPendingServiceCallback> it = tmp.iterator();
 				while (it.hasNext()) {
 					IPendingServiceCallback callback = it.next();
 					try {
 						callback.resultReceived(pendingCall);
 					} catch (Exception e) {
 						log.error("Error while executing callback " + callback,
 								e);
 					}
 				}
 			}
 			return;
 		}
 
 		// Make sure we don't use invoke ids that are used by the client
 		synchronized (conn.invokeId) {
 			if (conn.invokeId <= invoke.getInvokeId()) {
 				conn.invokeId = invoke.getInvokeId() + 1;
 			}
 		}
 
 		boolean disconnectOnReturn = false;
 		if (call.getServiceName() == null) {
 			log.info("call: " + call);
 			final String action = call.getServiceMethodName();
 			log.info("--" + action);
 			if (!conn.isConnected()) {
 
 				if (action.equals(ACTION_CONNECT)) {
 					log.debug("connect");
 					final Map params = invoke.getConnectionParams();
 					String host = getHostname((String) params.get("tcUrl"));
 					if (host.endsWith(":1935")) {
 						// Remove default port from connection string
 						host = host.substring(0, host.length() - 5);
 					}
 					final String path = (String) params.get("app");
 					final String sessionId = null;
 					conn.setup(host, path, sessionId, params);
 					try {
 						final IGlobalScope global = server.lookupGlobal(host,
 								path);
 						if (global == null) {
 							call.setStatus(Call.STATUS_SERVICE_NOT_FOUND);
 							if (call instanceof IPendingServiceCall) {
 								StatusObject status = (StatusObject) getStatus(NC_CONNECT_REJECTED);
 								status.setDescription("No scope \""+path+"\" on this server.");
 								((IPendingServiceCall) call).setResult(status);
 							}
 							log.info("No global scope found for " + path
 									+ " on " + host);
 							disconnectOnReturn = true;
 						} else {
 							final IContext context = global.getContext();
 							IScope scope = null;
 							try {
 								scope = context.resolveScope(path);
 							} catch (ScopeNotFoundException err) {
 								call.setStatus(Call.STATUS_SERVICE_NOT_FOUND);
 								if (call instanceof IPendingServiceCall) {
 									StatusObject status = (StatusObject) getStatus(NC_CONNECT_REJECTED);
 									status.setDescription("No scope \""+path+"\" on this server.");
 									((IPendingServiceCall) call).setResult(status);
 								}
 								log.info("Scope " + path + " not found on "
 										+ host);
 								disconnectOnReturn = true;
 							}
 							if (scope != null) {
 								log.info("Connecting to: " + scope);
 								boolean okayToConnect;
 								try {
 									if (call.getArguments() != null) {
 										okayToConnect = conn.connect(scope,
 												call.getArguments());
 									} else {
 										okayToConnect = conn.connect(scope);
 									}
 									if (okayToConnect) {
 										if (log.isDebugEnabled()) {
 											log.debug("connected");
 											log.debug("client: " + conn.getClient());
 										}
 										call.setStatus(Call.STATUS_SUCCESS_RESULT);
 										if (call instanceof IPendingServiceCall) {
 											((IPendingServiceCall) call)
 													.setResult(getStatus(NC_CONNECT_SUCCESS));
 										}
 										// Measure initial roundtrip time after connecting
 										conn.getChannel((byte) 2).write(new Ping((short) 0, 0, -1));
 										conn.ping();
 									} else {
 										log.debug("connect failed");
 										call
 												.setStatus(Call.STATUS_ACCESS_DENIED);
 										if (call instanceof IPendingServiceCall) {
 											((IPendingServiceCall) call)
 													.setResult(getStatus(NC_CONNECT_REJECTED));
 										}
 										disconnectOnReturn = true;
 									}
 								} catch (ClientRejectedException rejected) {
 									log.debug("connect rejected");
 									call.setStatus(Call.STATUS_ACCESS_DENIED);
 									if (call instanceof IPendingServiceCall) {
 										StatusObject status = (StatusObject) getStatus(NC_CONNECT_REJECTED);
 										status.setApplication(rejected
 												.getReason());
 										((IPendingServiceCall) call)
 												.setResult(status);
 									}
 									disconnectOnReturn = true;
 								}
 							}
 						}
 					} catch (RuntimeException e) {
 						call.setStatus(Call.STATUS_GENERAL_EXCEPTION);
 						if (call instanceof IPendingServiceCall) {
 							((IPendingServiceCall) call)
 									.setResult(getStatus(NC_CONNECT_FAILED));
 						}
 						log.error("Error connecting", e);
 						disconnectOnReturn = true;
 					}
 				}
 			} else if (action.equals(ACTION_DISCONNECT)) {
 				conn.close();
 			} else if (action.equals(ACTION_CREATE_STREAM)
 					|| action.equals(ACTION_DELETE_STREAM)
 					|| action.equals(ACTION_PUBLISH)
 					|| action.equals(ACTION_PLAY) || action.equals(ACTION_SEEK)
 					|| action.equals(ACTION_PAUSE)
 					|| action.equals(ACTION_CLOSE_STREAM)
 					|| action.equals(ACTION_RECEIVE_VIDEO)
 					|| action.equals(ACTION_RECEIVE_AUDIO)) {
 				IStreamService streamService = (IStreamService) getScopeService(
 						conn.getScope(), IStreamService.class,
 						StreamService.class);
 				invokeCall(conn, call, streamService);
 			} else {
 				invokeCall(conn, call);
 			}
 		} else if (conn.isConnected()) {
 			// Service calls, must be connected.
 			invokeCall(conn, call);
 		} else {
 			// Warn user attemps to call service without being connected
 			log.warn("Not connected, closing connection");
 			conn.close();
 		}
 
 		if (invoke instanceof Invoke) {
 			if ((source.getStreamId() != 0)
 					&& (call.getStatus() == Call.STATUS_SUCCESS_VOID || call
 							.getStatus() == Call.STATUS_SUCCESS_NULL)) {
 				// This fixes a bug in the FP on Intel Macs.
 				log.debug("Method does not have return value, do not reply");
 				return;
 			}
 
 			boolean sendResult = true;
 			if (call instanceof IPendingServiceCall) {
 				IPendingServiceCall psc = (IPendingServiceCall) call;
 				Object result = psc.getResult();
 				if (result instanceof DeferredResult) {
 					// Remember the deferred result to be sent later
 					DeferredResult dr = (DeferredResult) result;
 					dr.setServiceCall(psc);
 					dr.setChannel(channel);
 					dr.setInvokeId(invoke.getInvokeId());
 					conn.registerDeferredResult(dr);
 					sendResult = false;
 				}
 			};
 			
 			if (sendResult) {
 				// The client expects a result for the method call.
 				Invoke reply = new Invoke();
 				reply.setCall(call);
 				reply.setInvokeId(invoke.getInvokeId());
 				log.debug("sending reply");
 				channel.write(reply);
 				if (disconnectOnReturn) {
 					conn.close();
 				}
 			}
 		}
 	}
 
 	public Object getStatus(String code) {
 		return statusObjectService.getStatusObject(code);
 	}
 
 	public void onPing(RTMPConnection conn, Channel channel, Header source,
 			Ping ping) {
 		switch (ping.getValue1()) {
 			case 3:
 				if (ping.getValue2() != 0) {
 					// The client wants to set the buffer time
 					IClientStream stream = conn.getStreamById(ping.getValue2());
 					if (stream != null && stream.getStreamFlow() != null) {
 						IStreamFlow flow = stream.getStreamFlow();
 						int buffer = ping.getValue3();
 						// TODO: pass this to application, so custom settings can be
 						// applied
 						flow.setClientTimeBuffer(buffer);
 						int minBuffer = buffer - (buffer / 4);
 						int maxBuffer = buffer + (buffer / 4);
 						if (minBuffer < 1000) {
 							minBuffer = 1000;
 						}
 						if (maxBuffer < 5000) {
 							maxBuffer = 5000;
 						}
 						flow.setMinTimeBuffer(minBuffer);
 						flow.setMaxTimeBuffer(maxBuffer);
 						log.info("Setting client buffer on stream flow: "
 								+ ping.getValue2());
 					}
 				} else {
 					// XXX: should we store the buffer time for future streams?
 					log.warn("Unhandled ping: " + ping);
 				}
 				break;
 
 			case 7:
 				// This is the response to an IConnection.ping request
 				conn.pingReceived(ping);
 				break;
 
 			default:
 				log.warn("Unhandled ping: " + ping);
 		}
 
 	}
 
 	public void onStreamBytesRead(RTMPConnection conn, Channel channel,
 			Header source, BytesRead streamBytesRead) {
 		conn.receivedBytesRead(streamBytesRead.getBytesRead());
 	}
 
 	public void onSharedObject(RTMPConnection conn, Channel channel,
 			Header source, SharedObjectMessage object) {
 		final ISharedObject so;
 		final String name = object.getName();
 		IScope scope = conn.getScope();
 		if (scope == null) {
 			// The scope already has been deleted.
 			SharedObjectMessage msg = new SharedObjectMessage(name, 0, object
 					.isPersistent());
 			msg.addEvent(new SharedObjectEvent(
 					ISharedObjectEvent.Type.CLIENT_STATUS,
 					"SharedObject.NoObjectFound", "error"));
 			conn.getChannel((byte) 3).write(msg);
 			return;
 		}
 
 		ISharedObjectService sharedObjectService = (ISharedObjectService) getScopeService(
 				scope, ISharedObjectService.class,
				SharedObjectService.class, false);
 		if (!sharedObjectService.hasSharedObject(scope, name)) {
 			if (!sharedObjectService.createSharedObject(scope, name, object
 					.isPersistent())) {
 				SharedObjectMessage msg = new SharedObjectMessage(name, 0,
 						object.isPersistent());
 				msg.addEvent(new SharedObjectEvent(
 						ISharedObjectEvent.Type.CLIENT_STATUS,
 						"SharedObject.ObjectCreationFailed", "error"));
 				conn.getChannel((byte) 3).write(msg);
 				return;
 			}
 		}
 		so = sharedObjectService.getSharedObject(scope, name);
 		so.dispatchEvent(object);
 	}
 
 }

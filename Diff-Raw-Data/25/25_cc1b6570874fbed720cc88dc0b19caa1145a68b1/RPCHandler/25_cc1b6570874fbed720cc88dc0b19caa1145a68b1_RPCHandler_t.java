 package rpc;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import network.IncomingMessage;
 import network.MessageListener;
 import network.MessageSender;
 import network.MessageServerHandler;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 public class RPCHandler {
 	final protected Map<String, ServiceRequestListener> registeredServices;
 	protected MessageSender messageSender;
 	final static protected String VERSION = "1.0";
 	final protected Gson gson = new Gson();
 	final protected Map<String, RPCWaitingRequest> waitingRequests;
 	
 	protected RPCHandler() {
 		registeredServices = new HashMap<String, ServiceRequestListener>();
 		waitingRequests = new HashMap<String, RPCWaitingRequest>();
 	}
 	
 	public static RPCHandler getUDPInstance(int port) {
 		RPCHandler rpcHandler = new RPCHandler();
 		rpcHandler.messageSender = MessageServerHandler.startUDPServer(port, rpcHandler.newListener());
 		return rpcHandler;
 	}
 	
 	public URI getLocalURI() {
 		return messageSender.getLocalURI();
 	}
 	
 	public void registerServiceListener(String serviceName, ServiceRequestListener serviceListener) {
 		if (serviceListener == null) {
 			registeredServices.remove(serviceName);
 		} else {
 			registeredServices.put(serviceName, serviceListener);
 		}
 	}
 	
 	public void sendRequest(URI recipient, String serviceName, String dataString, RPCResponseListener responseListener) {
 		String uuid = UUID.randomUUID().toString(); // Is this safe to assume absolute local uniqueness?
 		RPCMessage requestMessage = new RPCMessage(VERSION, serviceName, uuid, dataString, null);
 		waitingRequests.put(uuid, new RPCWaitingRequest(requestMessage, recipient, responseListener));
 		messageSender.sendData(recipient, gson.toJson(requestMessage));
 	}
 	
 	IncomingMessageListener newListener() {
 		return new IncomingMessageListener();
 	}
 	
 	class IncomingMessageListener implements MessageListener {
 		@Override
 		public void onMessage(final IncomingMessage message) {
 			try {
 				final RPCMessage rpcMessage = gson.fromJson(message.getDataString(), RPCMessage.class);
				if (!rpcMessage.getVersion().equals(VERSION)) {
 					// Unsupported Message Version
 					System.out.println("Unsupported Message Version");   
 				}
 				if (rpcMessage.getRequest() != null) {
 					ServiceRequestListener service = registeredServices.get(rpcMessage.getService());
 					if (service != null) {
 						service.onRequestRecieved(new RPCEvent() {
 
 							@Override
 							public void respond(String data) {
 								RPCMessage responseMessage = new RPCMessage(VERSION, rpcMessage.getService(), rpcMessage.getId(), null, data);
 								message.sendResponse(gson.toJson(responseMessage));
 							}
 
 							@Override
 							public String getDataString() {
 								return rpcMessage.getRequest();
 							}
 
 							@Override
 							public String getServiceName() {
 								return rpcMessage.getService();
 							}
 
 							@Override
 							public URI getSenderURI() {
 								return message.getSenderURI();
 							}
 							
 						});
 					}
 				} else if (rpcMessage.getResponse() != null) {
 					RPCWaitingRequest waitingRequest = waitingRequests.get(rpcMessage.getId());
 					if (waitingRequest != null && waitingRequest.getRequestRecipient().equals(message.getSenderURI()) && waitingRequest.getRequestMessage().getService().equals(rpcMessage.getService())) {
 						waitingRequests.remove(rpcMessage.getId());
 						waitingRequest.getResponseListener().onResponseReceived(new RPCEvent() {
 							@Override
 							public void respond(String data) {
 								throw new UnsupportedOperationException("Cannot respond to a request response");
 							}
 
 							@Override
 							public String getDataString() {
 								return rpcMessage.getResponse();
 							}
 
 							@Override
 							public String getServiceName() {
 								return rpcMessage.getService();
 							}
 
 							@Override
 							public URI getSenderURI() {
 								return message.getSenderURI();
 							}
 						});
 					}
 				}
 			} catch (JsonParseException e) {
 				
 			} catch (ClassCastException e) {
 				
 			}
 			
 		}
 	}
 }

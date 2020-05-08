 package rpc;
 
 public class RPCMessage {
 	protected String pbox;
 	protected String service;
 	protected String id;
 	protected String request;
 	protected String response;
 	
	protected RPCMessage() {
		
	}
	
 	/**
 	 * 
 	 * @param version
 	 * @param service
 	 * @param uuid
 	 * @param request
 	 * @param response
 	 */
 	public RPCMessage(String version, String service, String uuid, String request, String response) {
 		this.pbox = version;
 		this.service = service;
 		this.id = uuid;
 		this.request = request;
 		this.response = response;
 	}
 	
 	public String getVersion() {
 		return pbox;
 	}
 	
 	public String getService() {
 		return service;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
 	public String getRequest() {
 		return request;
 	}
 	
 	public String getResponse() {
 		return response;
 	}
 }

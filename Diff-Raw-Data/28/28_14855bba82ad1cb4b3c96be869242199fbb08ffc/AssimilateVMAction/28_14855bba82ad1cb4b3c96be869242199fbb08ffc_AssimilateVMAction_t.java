 package n3phele.service.actions;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.core.UnprocessableEntityException;
 import n3phele.service.lifecycle.ProcessLifecycle;
 import n3phele.service.lifecycle.ProcessLifecycle.WaitForSignalRequest;
 import n3phele.service.model.Account;
 import n3phele.service.model.Cloud;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.Command;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.core.CreateVirtualServerResponse;
 import n3phele.service.model.core.Credential;
 import n3phele.service.model.core.ExecutionFactoryAssimilateRequest;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.model.core.VirtualServerStatus;
 import n3phele.service.rest.impl.AccountResource;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudProcessResource;
 import n3phele.service.rest.impl.CloudResource;
 
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Unindex;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 /**
 * (C) Copyright 2010-2013. Cristina Scheibler. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
 * except in compliance with the License. 
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 */
 
 @EntitySubclass
 @XmlRootElement(name = "AssimilateVMAction")
 @XmlType(name = "AssimilateVMAction", propOrder = { "failed" })
 @Unindex
 @Cache
 public class AssimilateVMAction extends VMAction{
 	final protected static java.util.logging.Logger log = java.util.logging.Logger.getLogger(AssimilateVMAction.class.getName());
 	@XmlTransient private ActionLogger logger;
 	private boolean failed = false;	
 	
 	private String targetIP;
 	
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 		this.epoch = new Date().getTime();
 		log.info(">>>Initiating assimilateVM<<<");
 		logger.info(">>>Initiating assimilateVM<<<");	
 		
 		log.info("Process URI: "+this.getProcess());
 		logger.info("Process URI: "+this.getProcess());	
 		
 		this.targetIP = this.getContext().getValue("targetIP");
 		
 	}
 
 	public void retrieveVirtualServer(Cloud cloud, Account account) throws Exception{
 		
 		Client client = ClientFactory.create();
 
 		ClientFilter factoryAuth = new HTTPBasicAuthFilter(Credential.unencrypted(cloud.getFactoryCredential()).getAccount(), Credential.unencrypted(cloud.getFactoryCredential()).getSecret());
 		client.addFilter(factoryAuth);
 		client.setReadTimeout(90000);
 		client.setConnectTimeout(5000);
 		WebResource resource = client.resource(cloud.getFactory()+"/assimilate");
 		
 		
 		ExecutionFactoryAssimilateRequest ar = new ExecutionFactoryAssimilateRequest();
 		ar.name = this.name;
 		ar.description = "VM Association for "+this.getName()+" "+this.getUri();
 		ar.location = cloud.getLocation();
 		ar.notification =  UriBuilder.fromUri(this.getProcess()).scheme("http").path("event").build();
 		ar.idempotencyKey = this.getProcess().toString();
 		
 		Credential factoryCredential = Credential.reencrypt(account.getCredential(), Credential.unencrypted(cloud.getFactoryCredential()).getSecret());
 		ar.accessKey = factoryCredential.getAccount(); 
 		ar.encryptedSecret = factoryCredential.getSecret();
 		ar.owner = this.getProcess();
 				
 		for(TypedParameter param : Helpers.safeIterator(cloud.getInputParameters())) {
 			String name = param.getName();
 			if ("locationId".equals(name)) {
 				String value = this.context.getValue(name);
 				if (!Helpers.isBlankOrNull(value)){
 					ar.locationId = value;
 				}
 			}				
 		}
 		ar.ipaddress = targetIP;				
 				
 		try {
 			AssimilateVirtualServerResult response = assimilateVirtualServers(resource, ar);
 			
 			//IP not found on cloud
 			if(response.getStatus() == 404){
 				log.info("IP not found on factory");
 				logger.info("IP not found on factory");
 				failed = true;
 				throw new Exception("IP not found "+response.getStatus());				
 			}
 			//IP already exists on factory
 			else if(response.getStatus() == 409){
 				log.info("Conflict: Server already exists on factory database");
 				logger.info("Conflict: Server already exists on factory database");
 				failed = true;
 			}
 			//vm added from the cloud to the factory
 			else if(response.getStatus() == 201 || response.getStatus() == 202){
 				log.info("IP found on factory");
 				logger.info("IP found on factory");
 				URI[] list = response.getRefs();	
 				this.context.putValue("vmFactory", list[0].toString());		
 			}
 		} catch (Exception e) {
 			failed = true;
 			logger.error("vm assimilate FAILED with exception "+e.getMessage());
 			log.log(Level.SEVERE, "vm assimilate FAILED with exception ", e);
 			throw e;
 		} finally {
 			ClientFactory.give(client);
 		}
 	}
 		
 	
 	@Override
 	public boolean call() throws WaitForSignalRequest, Exception {
 		if(epoch != 0) {
 							
 			URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 			if(accountURI == null)
 				throw new IllegalArgumentException("Missing account");
 			Account account = AccountResource.dao.load(accountURI, this.getOwner());
 			Cloud cloud = CloudResource.dao.load(account.getCloud(), this.getOwner());
 			Client client = ClientFactory.create();
 			ClientFilter factoryAuth = new HTTPBasicAuthFilter(Credential.unencrypted(cloud.getFactoryCredential()).getAccount(), Credential.unencrypted(cloud.getFactoryCredential()).getSecret());
 			client.addFilter(factoryAuth);
 			client.setReadTimeout(90000);
 			client.setConnectTimeout(5000);		
 			
 			
 			try {
 
 				if(this.context.getValue("vmFactory")==null)retrieveVirtualServer(cloud, account);
 				if(this.context.getValue("vmFactory")!=null){
 				if(!this.context.containsKey("publicIpAddress")) {						
 						VirtualServer vs = fetchVirtualServer(client, new URI(this.context.getValue("vmFactory")));						
 						log.info("VMAction: Server status is "+vs.getStatus());
 						URI notification = UriBuilder.fromUri(this.getProcess()).scheme("http").path("event").build();
 						if(!notification.equals(vs.getNotification())) {
 							// FIXME: Update factory to accept notification put .. resource.put(notification);
 						}
 						if(vs.getStatus().equals(VirtualServerStatus.running) && vs.getOutputParameters() != null) {
 							for(NameValue p : vs.getOutputParameters()) {
 								if(p.getKey().equals("publicIpAddress")) {
 									this.context.putValue("publicIpAddress", p.getValue());
 									log.info("VMAction: publicURI is "+p.getValue());
 								} else {
 									this.context.putValue(p.getKey(), p.getValue());
 								}
 							}
 						} else if(vs.getStatus().equals(VirtualServerStatus.terminated)) {
 							logger.error("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 							log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 							throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						} 
 					}
 				
 				epoch = 0;
				ProcessLifecycle.mgr().signalParent(this.getProcess(), SignalKind.Event, this.getProcess().toString());
 				throw new ProcessLifecycle.WaitForSignalRequest();
 				}
 					
 			} catch (UniformInterfaceException e) {
 				ClientResponse response = e.getResponse();
 				if(response != null) {
 					if(response.getStatus() == 404) {
 						logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 						log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 					}
 				}
 			}  catch (ProcessLifecycle.WaitForSignalRequest wait) {
 				throw wait;
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Unexpected exception", e);
 			} finally {
 				ClientFactory.give(client);
 			}
 			return false; // keep waiting
 		} else {
			
			// * On going monitoring of the VM
			 
 				
 			URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 			if(accountURI == null)
 				throw new IllegalArgumentException("Missing account");
 			Account account = AccountResource.dao.load(accountURI, this.getOwner());
 			Cloud cloud = CloudResource.dao.load(account.getCloud(), this.getOwner());
 			Client client = ClientFactory.create();
 			ClientFilter factoryAuth = new HTTPBasicAuthFilter(Credential.unencrypted(cloud.getFactoryCredential()).getAccount(), Credential.unencrypted(cloud.getFactoryCredential()).getSecret());
 			client.addFilter(factoryAuth);
 			client.setReadTimeout(90000);
 			client.setConnectTimeout(5000);		
 			VirtualServer vs = null;
 			try {	
 				vs = fetchVirtualServer(client, new URI(this.context.getValue("vmFactory")));		
 			} catch (UniformInterfaceException e) {
 				ClientResponse response = e.getResponse();
 				if(response != null) {
 					if(response.getStatus() == 404) {
 						logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 						log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 					}
 				}
 
 				logger.info("Access error to "+this.context.getValue("vmFactory"));
 				log.log(Level.INFO, "Access error to "+this.context.getValue("vmFactory"), e);
 				throw new ProcessLifecycle.WaitForSignalRequest();
 			} catch (ClientHandlerException e) {
 				logger.info("Access error to "+this.context.getValue("vmFactory"));
 				log.log(Level.INFO, "Access error to "+this.context.getValue("vmFactory"), e);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Unexpected exception", e);
 			} finally {
 				ClientFactory.give(client);
 			}
 			
 			log.info("VM "+this.context.getValue("vmFactory")+" "+(vs==null?"disappeared":vs.getName()+":"+vs.getStatus()));
 			if(vs != null && vs.getStatus().equals(VirtualServerStatus.running) && vs.getOutputParameters() != null) {
				
				// * All is well
				 
 			} else if(vs == null || vs.getStatus().equals(VirtualServerStatus.terminated)) {
				
				 //* The VM has died
				 
 				logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 				log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 				throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 			} 
 					
 			throw new ProcessLifecycle.WaitForSignalRequest();
 		}	
		
 	}
 	@Override
 	public void cancel() {
 		killVM();
 		log.warning("Cancel");
 		
 	}
 
 	@Override
 	public void dump() {
 		killVM();
 		log.warning("Dump");
 		
 	}
 
 	@Override
 	public void signal(SignalKind kind, String assertion) {
 		log.warning("Signal");
 		
 	}
 
 	@Override
 	public String getDescription() {
 		return "Assimilates an existing VM to n3phele";
 	}
 
 	@Override
 	public Command getPrototype() {
 		Command command = new Command();
 		command.setUri(UriBuilder.fromUri(ActionResource.dao.path).path("history").path(this.getClass().getSimpleName()).build());
 		command.setName("Assimilate");
 		command.setOwner(this.getOwner());
 		command.setOwnerName(this.getOwner().toString());
 		command.setPublic(false);
 		command.setDescription("Assimilates an existing VM to n3phele");
 		command.setPreferred(true);
 		command.setVersion("1");
 		//TODO: create icon
 		//command.setIcon(URI.create("https://www.n3phele.com/icons/on"));
 		List<TypedParameter> myParameters = new ArrayList<TypedParameter>();
 		command.setExecutionParameters(myParameters);
 		
 		myParameters.add(new TypedParameter("target", "VM IP address", ParameterType.String, "", ""));
 		for(TypedParameter param : command.getExecutionParameters()) {
 			param.setDefaultValue(this.context.getValue(param.getName()));
 		}
 		return command;
 	}
 	
 	/**
 	 * @return the targetIP
 	 */
 	public String getTargetIP() {
 		return this.targetIP;
 	}
 
 	/**
 	 * @param targetIP the targetIP to set
 	 */
 	public void setTargetIP(String targetIP) {
 		this.targetIP = targetIP;
 	}
 	
 
 	/**
 	 * @return if the action has failed
 	 */
 	public boolean hasFailed() {
 		return failed;
 	}
 
 	/**
 	 * @param failed if the action has failed
 	 */
 	public void setFailed(boolean failed) {
 		this.failed = failed;
 	}
 		
 
 	public void killVM() {
 		
 		URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 		if(accountURI == null)
 			throw new IllegalArgumentException("Missing account");
 		Account account = AccountResource.dao.load(accountURI, this.getOwner());
 		Cloud cloud = CloudResource.dao.load(account.getCloud(), this.getOwner());
 		Client client = ClientFactory.create();
 		ClientFilter factoryAuth = new HTTPBasicAuthFilter(Credential.unencrypted(cloud.getFactoryCredential()).getAccount(), Credential.unencrypted(cloud.getFactoryCredential()).getSecret());
 		client.addFilter(factoryAuth);
 		client.setReadTimeout(90000);
 		client.setConnectTimeout(5000);		
 		try {			
 			int status = terminate(client, this.context.getValue("vmFactory"), true, false, false);
 			log.info("Delete status is "+status);
 			//IP not found on cloud
 			if(status == 404){
 				log.info("IP not found on factory");
 				logger.info("IP not found on factory");
 				throw new Exception("IP not found on factory");				
 			}
 			else if(status == 204){
 				log.info("VM deleted from factory");
 				logger.info("VM deleted from factory");
 			}
 			
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception terminating VM", e);
 			logger.error("Exception terminating VM "+e.getMessage());
 		} finally {
 			ClientFactory.give(client);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("AssimilateVMAction [failed=%s, targetIP=%s, toString()=%s]",
 						failed, targetIP,
 						super.toString());
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + (this.failed ? 1231 : 1237);
 		result = prime * result
 				+ ((this.logger == null) ? 0 : this.logger.hashCode());
 		result = prime * result
 				+ ((this.targetIP == null) ? 0 : this.targetIP.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AssimilateVMAction other = (AssimilateVMAction) obj;
 		if (this.failed != other.failed)
 			return false;
 		if (this.logger == null) {
 			if (other.logger != null)
 				return false;
 		} else if (!this.logger.equals(other.logger))
 			return false;
 		if (this.targetIP == null) {
 			if (other.targetIP != null)
 				return false;
 		} else if (!this.targetIP.equals(other.targetIP))
 			return false;
 		return true;
 	}
 
 	
 	protected VirtualServer fetchVirtualServer(Client client, URI uri) {
 		return client.resource(uri).get(VirtualServer.class);
 	}
 	
 	protected AssimilateVirtualServerResult assimilateVirtualServers(WebResource resource, ExecutionFactoryAssimilateRequest assimilateRequest) {
 		return new AssimilateVirtualServerResult(resource.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, assimilateRequest));
 
 	}
 	
 	protected ProcessLifecycle processLifecycle() {
 		return ProcessLifecycle.mgr();
 	}
 	
 	protected int terminate(Client client, String factory, boolean error, boolean debug, boolean dbonly) {
 		WebResource resource = client.resource(factory);
 		ClientResponse response = resource.queryParam("error", Boolean.toString(error)).queryParam("debug", Boolean.toString(debug)).queryParam("dbonly", Boolean.toString(dbonly)).delete(ClientResponse.class);
 		return response.getStatus();
 	}
 	
 	protected static class AssimilateVirtualServerResult {
 		private ClientResponse response;
 		protected AssimilateVirtualServerResult() {}
 		public AssimilateVirtualServerResult(ClientResponse response) {
 			this.response = response;
 		}
 		
 		public URI getLocation() {
 			return response.getLocation();
 		}
 		
 		public URI[] getRefs() {
 			return response.getEntity(CreateVirtualServerResponse.class).vmList;
 		}
 		
 		public int getStatus() {
 			return response.getStatus();
 		}
 		
 		public String toString() {
 			return ""+response;
 		}
 	}
 }

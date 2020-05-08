 package n3phele.service.actions;
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
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
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
 import n3phele.service.model.Action;
 import n3phele.service.model.Cloud;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.Command;
 import n3phele.service.model.Context;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.Variable;
 import n3phele.service.model.VariableType;
 import n3phele.service.model.core.CreateVirtualServerResponse;
 import n3phele.service.model.core.Credential;
 import n3phele.service.model.core.ExecutionFactoryAssimilateRequest;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.rest.impl.AccountResource;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudResource;
 
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Unindex;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 
 @EntitySubclass
 @XmlRootElement(name = "AssimilateAction")
 @XmlType(name = "AssimilateAction", propOrder = { "inProgress","failed", "targetIP" })
 @Unindex
 @Cache
 public class AssimilateAction extends Action {
 	final protected static java.util.logging.Logger log = java.util.logging.Logger.getLogger(AssimilateAction.class.getName());
 	@XmlTransient private ActionLogger logger;
 	private ArrayList<String> inProgress = new ArrayList<String>();
 	private HashMap<String, String> childMap = new HashMap<String, String>();
 	private boolean failed = false;
 	private String targetIP;
 	
 		
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 		log.info(">>>Initiating assimilate<<<");
 		logger.info(">>>Initiating assimilate<<<");
 		URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 		if(accountURI == null)
 			throw new IllegalArgumentException("Missing account");
 		Account account = AccountResource.dao.load(accountURI, this.getOwner());
 		Cloud cloud = CloudResource.dao.load(account.getCloud(), this.getOwner());
 		
 		this.targetIP = this.getContext().getValue("targetIP");
 		log.info("Target IP: "+this.targetIP);
 		logger.info("Target IP: "+this.targetIP);
 		mergeCloudDefaultsIntoContext(cloud);
 		retrieveVirtualServer(cloud, account);
 		
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
 				
 				Context childContext = new Context();
 				childContext.putAll(this.context);
 				childContext.putValue("name", name);
 				
 				CloudProcess children = processLifecycle().spawn(this.getOwner(), childContext.getValue("name"), 
 						childContext, null, this.getProcess(), "AssimilateVM");
 				URI siblingProcess = children.getUri();
 				childMap.put(list[0].toString().replace('.', '_'), siblingProcess.toString());
 				this.inProgress.add(siblingProcess.toString());
 				
 				
 				
 				try{
 				VirtualServer vs = fetchVirtualServer(client, list[0]);
 				
 				if(vs!= null){
 					double value = getValueByCDN(cloud, vs.getParameters());
 					Date date = vs.getCreated();
 					
 					setCloudProcessPrice(account,children,value, date);
 					log.info("Process created and set");
 					logger.info("Process created and set");
 					processLifecycle().init(children);
 				}
 				}catch(Exception e) {
 					failed = true;
 					log.log(Level.SEVERE, "VM fetch", e);
 					logger.info("Error: could not retrieve virtual machine");					
 				}
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
 		int myChildren = this.inProgress.size();
 		if(myChildren != 0) {			
 			throw new ProcessLifecycle.WaitForSignalRequest();
 		}
 		if(failed) {
 			throw new UnprocessableEntityException("assimilation failed");
 		}
 		return true;
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
 		boolean isChild = this.inProgress.contains(assertion);
 		switch(kind) {
 		case Adoption:
 			log.info((isChild?"Child ":"Unknown ")+assertion+" adoption");
 			URI processURI = URI.create(assertion);
 			processLifecycle().dump(processURI);
 			return;
 		case Cancel:
 		case Dump:
 			log.info((isChild?"Child ":"Unknown ")+assertion+" cancelled or dumped");
 			if(isChild) {
 				// FIXME: Not sure if the failure semantics belong here
 				// FIXME: Of if there are parameterized..
 				// FIXME: --onFailure: killAll
 				// FIXME: --onFailure: continue
 				this.inProgress.remove(assertion);
 				this.killVM();
 				failed = true;
 			}
 			break;
 		case Event:
 			if(isChild) {
 				this.inProgress.remove(assertion);
 			} else if(assertion.equals("killVM")) {
 				killVM();
 			} else {
 				Map<String,String> params = parsetoMap(URI.create(assertion));
 				String source = params.get("source");
 				URI process = null;
 				if(source != null) {
 					process = Helpers.stringToURI(childMap.get(source.replace('.', '_')));
 					params.remove("source");
 					String childParams = null;
 					for(Entry<String, String> i : params.entrySet()) {
 						try {
 							if(!i.getKey().equals("oldStatus") && !i.getKey().equals("newStatus"))
 								continue;
 							String name = URLEncoder.encode(i.getKey(), "UTF-8");
 							String value = URLEncoder.encode(i.getValue(), "UTF-8");
 							String fragment = name+"="+value;
 							if(childParams != null) {
 								childParams += "&"+fragment;
 							} else {
 								childParams = fragment;
 							}
 						} catch (UnsupportedEncodingException e) {
 							log.log(Level.SEVERE, "Refactor exception", e);
 						}
 					}
 					if(childParams != null)
 						childParams = "?"+childParams;
 					else
 						childParams = "";
 					URI childAssertion = URI.create(process.toString()+childParams);
 					ProcessLifecycle.mgr().signal(process,SignalKind.Event, childAssertion.toString());
 					log.info("Forward event "+childAssertion+" to "+process);
 				}  else {
 					log.warning("Ignoring event "+assertion);
 				}
 			}
 			return;
 		case Failed:
 			log.info((isChild?"Child ":"Unknown ")+assertion+" failed");
 			if(isChild) {
 				this.inProgress.remove(assertion);
 				this.killVM();
 				failed = true;
 			}
 			break;
 		case Ok:
 			log.info((isChild?"Child ":"Unknown ")+assertion+" ok");
 			if(isChild) {
 				this.inProgress.remove(assertion);
 			}
 			break;
 		default:
 			return;		
 		}
 		
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
 	
 	/**
 	 * @return the inProgress
 	 */
 	public ArrayList<String> getInProgress() {
 		return inProgress;
 	}
 
 
 	/**
 	 * @param inProgress the inProgress to set
 	 */
 	public void setInProgress(ArrayList<String> inProgress) {
 		this.inProgress = inProgress;
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
 			int status = terminate(client, this.context.getValue("vmFactory"), false, false, true);
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
 	
 	private Map <String,String> parsetoMap (URI uri) {
 		Map<String,String> result = new HashMap<String,String>();
 		String query = uri.getRawQuery();
 		if(query != null && !query.isEmpty()) {
 			Scanner scanner = new Scanner(uri.getRawQuery());
 		    scanner.useDelimiter("&");
 		    while (scanner.hasNext()) {
 		        String[] nameValue = scanner.next().split("=");
 		        if (nameValue.length == 0 || nameValue.length > 2)
 		            throw new IllegalArgumentException("bad parameter");
 		
 		        try {
 					String name = URLDecoder.decode(nameValue[0], "UTF-8");
 					String value = null;
 					if (nameValue.length == 2)
 					    value = URLDecoder.decode(nameValue[1], "UTF-8");
 					result.put(name, value);
 				} catch (UnsupportedEncodingException e) {
 					log.log(Level.SEVERE, "Parse exception", e);
 				}
 		    }
 		}
         return result;
     }
 
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("AssimilateAction [failed=%s, target=%s, toString()=%s]",
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
 		AssimilateAction other = (AssimilateAction) obj;
 		if (this.failed != other.failed)
 			return false;
		if (inProgress == null) {
			if (other.inProgress != null)
				return false;
		} else if (!inProgress.equals(other.inProgress))
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
 
 	private void mergeCloudDefaultsIntoContext(Cloud cloud) {
 		for(TypedParameter p : Helpers.safeIterator(cloud.getInputParameters())) {
 			if(!this.context.containsKey(p.getName())) {
 					if(!Helpers.isBlankOrNull(p.valueOf())) {
 					Variable v = new Variable();
 					v.setName(p.getName());
 					v.setValue(p.valueOf());
 					v.setType(VariableType.valueOf(p.getType().toString()));
 					this.context.put(v.getName(), v);
 					log.info("Inserting "+v);
 				}
 			} else {
 				log.info(p.getName()+" already exists ");
 			}
 		}
 	}
 	
 	private double getValueByCDN(Cloud myCloud, ArrayList<NameValue> values){
 		for(int i = 0; i < values.size(); i++){
 			if(values.get(i).getKey().equals(myCloud.getCostDriverName())){
 				double value = myCloud.getCostMap().get( values.get(i).getValue());
 				return value;			
 			}
 		}
 		return 0;
 	}
 	
 	private void setCloudProcessPrice(Account account, CloudProcess process,double value,Date date){
 		
 		processLifecycle().setCloudProcessPrice(account.getUri().toString(), process,value,date);
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

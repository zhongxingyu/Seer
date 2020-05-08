 package n3phele.service.actions;
 /**
 * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
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
 import java.util.Arrays;
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
 
 import n3phele.service.core.NotFoundException;
 import n3phele.service.core.Resource;
 import n3phele.service.core.UnprocessableEntityException;
 import n3phele.service.lifecycle.ProcessLifecycle;
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
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.model.core.VirtualServerStatus;
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
 import com.sun.jersey.core.util.Base64;
 
 /** Creates one or more VMs on a specified cloud
  * <br> Processes the following context variables
  * <br> n <i>number of vms</i>
  * <br> name <i>vm names</i>. Where n > 1 the names will be name_<i>i</i> <i>i</i>= 0 .. n-1
  * <br> account <i> URI </i> of account on which VMs are to be created
  * <br> 
  * <br> populates its context with the following:
  * <br> cloudVM a list length <i>n</> of virtual machine actions.
  * 
  * @author Nigel Cook
  *
  */
 @EntitySubclass
 @XmlRootElement(name = "CreateVMAction")
 @XmlType(name = "CreateVMAction", propOrder = { "inProgress", "failed" })
 @Unindex
 @Cache
 public class CreateVMAction extends Action {
 	final private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(CreateVMAction.class.getName()); 
 	@XmlTransient private ActionLogger logger;
 	private ArrayList<String> inProgress = new ArrayList<String>();
 	private boolean failed = false;
 	private HashMap<String, String> childMap = new HashMap<String, String>();
 	
 	/* (non-Javadoc)
 	 * @see n3phele.service.model.Action#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		StringBuilder desc = new StringBuilder("Create ");
 		int n = this.getContext().getIntegerValue("n");
 		if(n == 1) {
 			desc.append("a VM");
 		} else {
 			desc.append(n+" VMs");
 		}
 		URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 		if(accountURI != null) {
 			Account account = AccountResource.dao.load(accountURI, this.getOwner());
 			desc.append(" on ");
 			desc.append(account.getName());
 		}
 		return desc.toString();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see n3phele.service.model.Action#getPrototype()
 	 */
 	@Override
 	public Command getPrototype() {
 		Command command = new Command();
 		command.setUri(UriBuilder.fromUri(ActionResource.dao.path).path("history").path(this.getClass().getSimpleName()).build());
 		command.setName("CreateVM");
 		command.setOwner(this.getOwner());
 		command.setOwnerName(this.getOwner().toString());
 		command.setPublic(false);
 		command.setDescription("Create one or more virtual machines");
 		command.setPreferred(true);
 		command.setVersion("1");
 		command.setIcon(URI.create("https://www.n3phele.com/icons/createVM"));
 		List<TypedParameter> myParameters = new ArrayList<TypedParameter>();
 		command.setExecutionParameters(myParameters);
 		
 		myParameters.add(new TypedParameter("n", "number of vms to create", ParameterType.Long, "", this.context.getValue("n")));
 		myParameters.add(new TypedParameter("$account", "cloud account", ParameterType.String, "", this.context.getValue("account")));
 		return command;
 	}
 	
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 
 		int n = this.context.getIntegerValue("n");
 		if(n <= 0) {
 			n = 1;
 			this.context.putValue("n", 1);
 		}
 		URI accountURI = Helpers.stringToURI(this.context.getValue("account"));
 		if(accountURI == null)
 			throw new IllegalArgumentException("Missing account");
 		Account account = AccountResource.dao.load(accountURI, this.getOwner());
 		Cloud cloud = CloudResource.dao.load(account.getCloud(), this.getOwner());
 		
 		log.info("Merging cloud defaults into context");
 		mergeCloudDefaultsIntoContext(cloud);
 		createVMs(account, cloud);
 	}
 	
 	
 	@Override
 	public boolean call() throws n3phele.service.lifecycle.ProcessLifecycle.WaitForSignalRequest, Exception {
 		int myChildren = this.inProgress.size();
 		log.info("waiting for children "+myChildren);
 		if(myChildren != 0) {
 			throw new ProcessLifecycle.WaitForSignalRequest();
 		}
 		if(failed) {
 			throw new UnprocessableEntityException("creation failed");
 		}
 		return true;
 	}
 
 	@Override
 	public void cancel() {
 		this.killVM();
 
 	}
 
 	@Override
 	public void dump() {
 		this.killVM();
 
 	}
 
 	@Override
 	public void signal(SignalKind kind, String assertion) {
 		log.info("CreateVM gets Signal "+kind+":"+assertion);
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
 
 
 	public void killVM() throws NotFoundException {
 		List<String> vms = this.context.getListValue("cloudVM");
 		log.info("KillVM killing "+vms.size()+" "+vms);
 		for(String vm : vms) {
 			Action action = ActionResource.dao.load(URI.create(vm));
 			CloudProcess process = processLifecycle().dump(action.getProcess());
 			if(!process.isFinalized() &&  !inProgress.contains(process.getUri().toString()))
 				inProgress.add(process.getUri().toString());
 		}
 		
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
 		
 	private void createVMs(Account account, Cloud myCloud) throws Exception {
 		
 		log.info("Create VMAction called");
 		Client client = ClientFactory.create();
 
 		ClientFilter factoryAuth = new HTTPBasicAuthFilter(Credential.unencrypted(myCloud.getFactoryCredential()).getAccount(), Credential.unencrypted(myCloud.getFactoryCredential()).getSecret());
 		client.addFilter(factoryAuth);
 		client.setReadTimeout(90000);
 		client.setConnectTimeout(5000);
 		WebResource resource = client.resource(myCloud.getFactory());
 		
 		ExecutionFactoryCreateRequest cr = new ExecutionFactoryCreateRequest();
 		cr.name = this.name;
 		log.info("ExecutionFactoryCreateRequest name: "+this.name);
 		cr.description = "VM Creation for "+this.getName()+" "+this.getUri();
 		log.info("ExecutionFactoryCreateRequest description: "+cr.description);
 		cr.location = myCloud.getLocation();
 		log.info("ExecutionFactoryCreateRequest location: "+cr.location);		
 		String keyName = this.context.getValue("keyName");
 		log.info("Account name: "+ account.getName());
 		if(Helpers.isBlankOrNull(keyName)) {
 			this.context.putValue("keyName", "n3phele-"+account.getName());
 		}
 		cr.parameters = contextToNameValue(myCloud, this.context);
 		cr.idempotencyKey = this.getProcess().toString();
 
 		Credential factoryCredential = Credential.reencrypt(account.getCredential(), Credential.unencrypted(myCloud.getFactoryCredential()).getSecret());
 		cr.accessKey = factoryCredential.getAccount(); 
 		cr.encryptedSecret = factoryCredential.getSecret();
 		cr.owner = this.getProcess();
 		Credential agentCredential = new Credential(Resource.get("agentUser", "test"), 
 				Resource.get("agentSecret", "password")).encrypt();
 		try {
 
 			CreateVirtualServerResult response = createVirtualServers(resource, cr);
 			log.info("Factory response: "+response.getStatus() + " "+response);
 			
 			URI location = response.getLocation();
 			log.info("Response location: "+location);			
 			if(location != null && response.getStatus() == 201) {
 				URI[] refs = response.getRefs();
 				log.info("Refs length: "+refs.length);
 
 				log.info(this.name+" "+Integer.toString(refs.length)+" vm(s) creation started. Factory "+location.toString()+" initiating status "+response.getStatus());
 				log.info(this.name+" "+Arrays.asList(refs));
 				
 				if(refs.length == 1) {
 					logger.info("vm creation started.");
 					boolean forceAgentRestart = false;
 					VirtualServer vs = null;
 					try {
 						vs = fetchVirtualServer(client, refs[0]);
 						log.info("Server status is "+vs.getStatus());
 						
 						if(vs.getStatus().equals(VirtualServerStatus.running)) {
 							forceAgentRestart  = true;
 							log.info("forcing agent restart");
 						} 
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "VM fetch", e);
 					}
 					ArrayList<CloudProcess> listProcesses = createVMProcesses(refs, forceAgentRestart, myCloud.getFactoryCredential(), agentCredential);
 					double value = 0;
 					if(vs!= null)
 					value = getValueByCDN(myCloud, vs.getParameters());
 					Date date = null;
 					if(vs!= null) date = vs.getCreated();
 					
 					
 					setCloudProcessPrice(account,listProcesses,value, date);
 
 				} else {
 					ArrayList<CloudProcess> listProcesses = createVMProcesses(refs, false, myCloud.getFactoryCredential(), agentCredential);
 					ArrayList<VirtualServer> listVs = new ArrayList<VirtualServer>();
 					for(int i = 0; i < refs.length; i++){
 						try {
 							VirtualServer vs = fetchVirtualServer(client, refs[i]);
 							listVs.add(vs);
 							log.info("Server status is "+vs.getStatus());
 						} catch (Exception e) {
 							log.log(Level.SEVERE, "VM fetch", e);
 						}
 					}
 					
 					for(int i = 0; i < listProcesses.size(); i++){
 						double value = getValueByCDN(myCloud, listVs.get(i).getParameters());
 						Date date = listVs.get(i).getCreated();
 						setCloudProcessPriceUnit(account,listProcesses.get(i),value, date);
 					}
 				}
 			} else {
 				String entity = response.getEntity();
 				log.log(Level.SEVERE, this.name+" vm creation initiation FAILED with status "+response.getStatus()+" "+entity);
 				logger.error("vm creation initiation FAILED with explanation "+entity);
 				throw new UnprocessableEntityException("vm creation initiation FAILED with status "+response.getStatus());
 			}	
 		} catch (Exception e) {
 			logger.error("vm creation FAILED with exception "+e.getMessage());
 			log.log(Level.SEVERE, "vm creation FAILED with exception ", e);
 			throw e;
 		} finally {
 			ClientFactory.give(client);
 		}
 		
 	}
 	private void setCloudProcessPrice(Account account, ArrayList<CloudProcess> processList,double value,Date date){
 		for(CloudProcess process: processList){
 			processLifecycle().setCloudProcessPrice(account.getUri().toString(), process,value,date);
 		}
 	}
 	private void setCloudProcessPriceUnit(Account account, CloudProcess process,double value,Date date){
 			processLifecycle().setCloudProcessPrice(account.getUri().toString(), process,value,date);	
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
 	private ArrayList<CloudProcess> createVMProcesses(URI[] refs, boolean forceAgentRestart, Credential factoryCredential, Credential agentCredential) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		
 		ArrayList<CloudProcess> listProcesses = new ArrayList<CloudProcess>();
 		CloudProcess[] children = new CloudProcess[refs.length];
 		URI[] siblingActions = new URI[refs.length];
 		String name = this.context.getValue("name");
 		if(Helpers.isBlankOrNull(name)) {
 			name = this.getName();
 		}
 		
 		this.inProgress.clear();
 		for(int i=0; i < refs.length; i++) {
 			Context childContext = new Context();
 			childContext.putAll(this.context);
 			childContext.putValue("name", name);
 			if(forceAgentRestart) {
 				childContext.putValue("forceAgentRestart", true);
 			}
 			childContext.putValue("vmIndex", i);
 			childContext.putValue("vmFactory", refs[i]);
 			childContext.putSecretValue("factoryUser", Credential.unencrypted(factoryCredential).getAccount());
 			childContext.putSecretValue("factorySecret", Credential.unencrypted(factoryCredential).getSecret());
 			childContext.putSecretValue("agentUser", Credential.unencrypted(agentCredential).getAccount());
 			childContext.putSecretValue("agentSecret", Credential.unencrypted(agentCredential).getSecret());
 	
 			if(refs.length > 1)
 				childContext.putValue("name", name+"_"+i );
 			children[i] = processLifecycle().spawn(this.getOwner(), childContext.getValue("name"), 
 					childContext, null, this.getProcess(), "VM");
 			
 			URI siblingProcess = children[i].getUri();
 			childMap.put(refs[i].toString().replace('.', '_'), siblingProcess.toString());
 			siblingActions[i] = children[i].getAction();
 			this.inProgress.add(siblingProcess.toString());
 		}
 
 		this.context.putValue("cloudVM", siblingActions);
 
 
 		for(CloudProcess child : children) {
 			listProcesses.add(child);
 			VMAction action = (VMAction) ActionResource.dao.load(child.getAction());
 			action.getContext().putValue("cloudVM", siblingActions);
 			ActionResource.dao.update(action);
 			processLifecycle().init(child);
 		}
 		
 		return listProcesses;
 	}
 
 
 	private ArrayList<NameValue> contextToNameValue(Cloud cloud, Context context) {
 		ArrayList<NameValue> result = new ArrayList<NameValue>();
		if(this.context.containsKey("securityGroups") && this.context.containsKey("security_groups")){
			this.context.remove("security_groups");
		}
 		for(TypedParameter param : Helpers.safeIterator(cloud.getInputParameters())) {
 			String name = param.getName();
 			String contextName = name;
 			log.info("Parameter name: "+name);
 			if(name.contains("_")) {
 				String split[] = name.split("_");
 				String camelCase = split[0];
 				for(int i=1; i < split.length; i++) {
 					if(split[i].length() > 0)
 						camelCase += split[i].substring(0,1).toUpperCase()+split[i].substring(1);
 				}
 				if(!this.context.containsKey(name) && this.context.containsKey(camelCase)) {
 					log.info("Using camelCase form "+camelCase);
 					contextName = camelCase;
 				}
 			}
 
 			if(this.context.containsKey(contextName)) {
 				String value = this.context.getValue(contextName);
 				/*
 				 * FIXME: Temporary. Move to Factory
 				 */
 				if ("userData".equals(contextName)) {
 					try {
 						if (!Helpers.isBlankOrNull(value)) {
 							String encoded = new String(Base64.encode(value));
 							value = encoded;
 						}
 					} catch (Exception e) {
 
 					}
 				}
 				log.info("Parameter exists, updating value: "+value);
 				NameValue nv = new NameValue(name, value);
 				result.add(nv);
 			} else {
 				log.info("Parameter doesn't exist");
 			}
 		}
 		return result;
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
 	
 	/**
 	 * @return the failed
 	 */
 	public boolean isFailed() {
 		return failed;
 	}
 	
 	/**
 	 * @return the failed
 	 */
 	public boolean getFailed() {
 		return failed;
 	}
 
 
 	/**
 	 * @param failed the failed to set
 	 */
 	public void setFailed(boolean failed) {
 		this.failed = failed;
 	}
 	
 		/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String.format(
 				"CreateVMAction [inProgress=%s, failed=%s, toString()=%s]",
 				inProgress, failed, super.toString());
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + (failed ? 1231 : 1237);
 		result = prime * result
 				+ ((inProgress == null) ? 0 : inProgress.hashCode());
 		result = prime * result + ((logger == null) ? 0 : logger.hashCode());
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
 		CreateVMAction other = (CreateVMAction) obj;
 		if (failed != other.failed)
 			return false;
 		if (inProgress == null) {
 			if (other.inProgress != null)
 				return false;
 		} else if (!inProgress.equals(other.inProgress))
 			return false;
 		if (logger == null) {
 			if (other.logger != null)
 				return false;
 		} else if (!logger.equals(other.logger))
 			return false;
 		return true;
 	}
 
 	
 	
 	/*
 	 * Unit Testing
 	 * ============
 	 */
 
 
 	protected VirtualServer fetchVirtualServer(Client client, URI uri) {
 		return client.resource(uri).get(VirtualServer.class);
 	}
 	
 	protected CreateVirtualServerResult createVirtualServers(WebResource resource, ExecutionFactoryCreateRequest createRequest) {
 		return new CreateVirtualServerResult(resource.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, createRequest));
 
 	}
 	
 	protected ProcessLifecycle processLifecycle() {
 		return ProcessLifecycle.mgr();
 	}
 	
 	protected static class CreateVirtualServerResult {
 		private ClientResponse response;
 		protected CreateVirtualServerResult() {}
 		public CreateVirtualServerResult(ClientResponse response) {
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
 		
 		public String getEntity() {
 			return response.getEntity(String.class);
 		}
 		
 		public String toString() {
 			return ""+response;
 		}
 	}
 }

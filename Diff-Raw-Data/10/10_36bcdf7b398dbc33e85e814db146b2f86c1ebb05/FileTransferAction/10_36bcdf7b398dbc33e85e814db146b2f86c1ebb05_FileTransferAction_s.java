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
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.core.UnprocessableEntityException;
 import n3phele.service.model.Action;
 import n3phele.service.model.Command;
 import n3phele.service.model.Context;
 import n3phele.service.model.Origin;
 import n3phele.service.model.ParameterType;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.TypedParameter;
 import n3phele.service.model.core.Credential;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.Task;
 import n3phele.service.model.core.User;
 import n3phele.service.model.repository.Repository;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.RepositoryResource;
 import n3phele.service.rest.impl.UserResource;
 
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Embed;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Unindex;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.representation.Form;
 
 /** Runs a command on a cloud server 
  * 
  * @author Nigel Cook
  *
  */
 @EntitySubclass
 @XmlRootElement(name = "FileTransferAction")
 @XmlType(name = "FileTransferAction", propOrder = {})
 @Unindex
 @Cache
 public class FileTransferAction extends Action {
 	final private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(FileTransferAction.class.getName()); 
 	@XmlTransient private ActionLogger logger;
 	
 	private String target;
 	@XmlTransient
 	@Embed
 	private Credential clientCredential=null;
 	private String instance;
 	private Long epoch = 0L;
 	private Long clientUnresponsive = null;
 	public FileTransferAction() {}
 	
 	protected FileTransferAction(User owner, String name,
 			Context context) {
 		super(owner.getUri(), name, context);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see n3phele.service.model.Action#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return "File transfer";
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see n3phele.service.model.Action#getPrototype()
 	 */
 	@Override
 	public Command getPrototype() {
 		Command command = new Command();
 		command.setUri(UriBuilder.fromUri(ActionResource.dao.path).path("history").path(this.getClass().getSimpleName()).build());
 		command.setName("FileTransfer");
 		command.setOwner(this.getOwner());
 		command.setOwnerName(this.getOwner().toString());
 		command.setPublic(false);
 		command.setDescription("Transfer a file between a VM and Cloud repository");
 		command.setPreferred(true);
 		command.setVersion("1");
 		command.setIcon(URI.create("https://www.n3phele.com/icons/fileTransfer"));
 		List<TypedParameter> myParameters = new ArrayList<TypedParameter>();
 		command.setExecutionParameters(myParameters);
 		
 		myParameters.add(new TypedParameter("target", "VM action URI", ParameterType.String, "", ""));
 		myParameters.add(new TypedParameter("source", "source file", ParameterType.String, "", ""));
 		myParameters.add(new TypedParameter("destination", "destination file", ParameterType.String, "", ""));
 		for(TypedParameter param : command.getExecutionParameters()) {
 			param.setDefaultValue(this.context.getValue(param.getName()));
 		}
 		return command;
 	}
 	
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 		log.info(this.getContext().getValue("arg"));
 		
 		this.target = this.getContext().getValue("target");
 		Action vm = ActionResource.dao.load(URI.create(this.target));
 		VMAction targetVM = null;
 		if(!(vm instanceof VMAction)) {
 			logger.error(this.target+" is not a VM. Got "+vm.getClass().getSimpleName());
 			log.warning(this.target+" is not a VM. Got "+vm.getClass().getSimpleName());
 			throw new IllegalArgumentException(this.target+" is not a VM. Got "+vm.getClass().getSimpleName());
 		} else {
 			targetVM = (VMAction) vm;
 		}
 		
 		if(this.clientCredential == null) {
 			this.clientCredential = new Credential(
 					targetVM.getContext().getValue("agentUser"), 
 					targetVM.getContext().getValue("agentSecret")).encrypt();
 		}
 
 		Client client = ClientFactory.create();
 		try {
			Credential plain = this.clientCredential;
 			client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
 			client.setReadTimeout(30000);
 			client.setConnectTimeout(5000);
 			
 			URI src = this.context.getFileValue("source");
 			URI dest = this.context.getFileValue("destination");
 
 			if(epoch == 0) {
 				epoch = Calendar.getInstance().getTimeInMillis();
 			}
 
 			Form form = new Form();
 			
 			String srcRepoName = src.getScheme();
 			if("file".equals(srcRepoName)) {
 				form.add("srcRoot", ".");
 				form.add("srcKey", src.getPath());
 			} else {
 				Repository repo = RepositoryResource.dao.load(srcRepoName, UserResource.dao.load(this.getOwner()));
 				form.add("source", repo.getTarget());
 				form.add("srcRoot", repo.getRoot());
 				form.add("srcKind", repo.getKind());
 				Credential credential;
 				credential = Credential.reencrypt(repo.getCredential(), plain.getSecret());
 				form.add("srcAccount", credential.getAccount());
 				form.add("srcSecret", credential.getSecret());
 			}
 			
 
 			String destRepoName = dest.getScheme();
 			if("file".equals(destRepoName)) {
 				form.add("destRoot", ".");
 				form.add("destKey", dest.getPath());
 			} else {
 				Repository repo = RepositoryResource.dao.load(destRepoName, UserResource.dao.load(this.getOwner()));
 				form.add("destination", repo.getTarget());
 				form.add("destRoot", repo.getRoot());
 				form.add("destKind", repo.getKind());
 				Credential credential;
 				credential = Credential.reencrypt(repo.getCredential(), plain.getSecret());
 				form.add("destAccount", credential.getAccount());
 				form.add("destSecret", credential.getSecret());
 			}
 			
 			form.add("tag", this.context.getValue("fileTableId"));
 			form.add("notification", this.getContext().getValue("notification"));
 	
 			try {
 	
 				URI location = sendRequest(client, targetVM.getContext().getURIValue("agentURI"), form);
 				if(location != null) {
 					this.instance = location.toString();	
 				} 	
 			} catch (UnprocessableEntityException e) {
 				throw e;
 			} catch (Exception e) {
 				long now = Calendar.getInstance().getTimeInMillis();
 				// Give the agent a 5 minute grace period to respond
 				if((now - epoch) > 5*60*1000L) {
 					logger.error("file copy initiation FAILED with exception "+e.getMessage());
 					log.log(Level.SEVERE, this.name+" file copy initiation FAILED with exception ", e);
 					throw new UnprocessableEntityException("file copy initiation FAILED with exception "+e.getMessage());
 				} 
 			}
 		} finally {
 			ClientFactory.give(client);
 		}
 		
 	}
 
 	@Override
 	public boolean call() throws Exception {
 
 		Client client = ClientFactory.create();
 
 		Credential plain = this.clientCredential.decrypt();
 		client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
 		client.setReadTimeout(30000);
 		client.setConnectTimeout(5000);
 		try {
 	
 				Task t;
 				try {
 					t = getTask(client, this.instance);
 					clientUnresponsive = null;
 				} catch (ClientHandlerException e) {
 					if(clientUnresponsive == null) {
 						clientUnresponsive = Calendar.getInstance().getTimeInMillis();
 						return false;
 					} else {
 						long now = Calendar.getInstance().getTimeInMillis();
 						// Give the agent a 5 minute grace period to respond
 						if((now - clientUnresponsive) > 5*60*1000L) {
 							log.log(Level.SEVERE, name+" file copy monitoring "+this.instance+" failed", e);
 							logger.error("Copy monitoring failed with exception "+e.getMessage());
 							throw new UnprocessableEntityException("Copy monitoring failed with exception "+e.getMessage());
 						} 
 					}
 					return false;
 				} catch (UniformInterfaceException e) {
 					if(clientUnresponsive == null) {
 						clientUnresponsive = Calendar.getInstance().getTimeInMillis();
 					} else {
 						long now = Calendar.getInstance().getTimeInMillis();
 						// Give the agent a 5 minute grace period to respond
 						if((now - clientUnresponsive) > 5*60*1000L) {
 							log.log(Level.SEVERE, name+" file copy monitoring "+this.instance+" failed", e);
 							logger.error("Copy monitoring failed with exception "+e.getMessage());
 							throw new UnprocessableEntityException("Copy monitoring failed with exception "+e.getMessage());
 						} 
 					}
 					return false;
 				}
 				this.context.putValue("stdout", t.getStdout());
 				this.context.putValue("stderr", t.getStderr());
 				if(t.getFinished() != null) {
 					this.context.putValue("exitCode", Integer.toString(t.getExitcode()));
 					if(t.getExitcode() == 0) {
 						Calendar time = Calendar.getInstance();
 						Calendar begin = Calendar.getInstance();
 						time.setTime(t.getFinished());
 						begin.setTime(t.getStarted());
 						long interval = time.getTimeInMillis() - begin.getTimeInMillis();
 						if(t.getStdout() != null && t.getStdout().length() > 0)
 							logger.info("Stdout:"+t.getStdout());
 						if(t.getStderr() != null && t.getStderr().length() > 0)
 							logger.warning("Stderr:"+t.getStderr());
 						String durationText;
 						if(interval < 1000) {
 							durationText = Long.toString(interval)+" milliseconds"; 
 						} else {
 							durationText = Long.toString(interval/1000)+" seconds"; 
 						}
 						log.fine(this.name+" file copy completed successfully. Elapsed time "+durationText);
 						logger.info("File copy completed successfully. Elapsed time "+durationText);
 	
 						if(t.getManifest() != null) {
 							log.info("File copy manifest length "+t.getManifest().length);
 							Origin.updateOrigin(this.getProcess(), t.getManifest()); // FIXME should be the On command process
 						}
 						return true;
 					} else {
 						if(t.getStdout() != null && t.getStdout().length() > 0)
 							logger.info("Stdout:"+t.getStdout());
 						if(t.getStderr() != null && t.getStderr().length() > 0)
 							logger.warning("Stderr:"+t.getStderr());
 						logger.error("File copy "+this.instance+" failed with exit status "+t.getExitcode());
 						log.severe(name+" file copy "+this.instance+" failed with exit status "+t.getExitcode());
 						log.severe("Stdout: "+t.getStdout());
 						log.severe("Stderr: "+t.getStderr());
 	
 						throw new UnprocessableEntityException("File copy "+this.instance+" failed with exit status "+t.getExitcode());
 					}
 				} 
 			
 	
 			return false;
 	} finally {
 		ClientFactory.give(client);
 	}
 	}
 
 
 	@Override
 	public void cancel() {
 		log.warning("Cancel");
 		
 	}
 
 	@Override
 	public void dump() {
 		log.warning("Dump");
 		
 	}
 
 	@Override
 	public void signal(SignalKind kind, String assertion) {
 		log.warning("Signal "+assertion);
 	}
 	
 	
 	/*
 	 * Getters and Setters
 	 */
 	
 
 	/**
 	 * @return the target
 	 */
 	public URI getTarget() {
 		return Helpers.stringToURI(target);
 	}
 
 	/**
 	 * @param target the target to set
 	 */
 	public void setTarget(URI target) {
 		this.target = Helpers.URItoString(target);
 	}
 
 	/**
 	 * @return the instance
 	 */
 	public URI getInstance() {
 		return Helpers.stringToURI(instance);
 	}
 
 	/**
 	 * @param instance the instance to set
 	 */
 	public void setInstance(URI instance) {
 		this.instance = Helpers.URItoString(instance);
 	}
 
 	/**
 	 * @return the epoch
 	 */
 	public Long getEpoch() {
 		return epoch;
 	}
 
 	/**
 	 * @param epoch the epoch to set
 	 */
 	public void setEpoch(Long epoch) {
 		this.epoch = epoch;
 	}
 
 	/**
 	 * @return the clientUnresponsive
 	 */
 	public Long getClientUnresponsive() {
 		return clientUnresponsive;
 	}
 
 	/**
 	 * @param clientUnresponsive the clientUnresponsive to set
 	 */
 	public void setClientUnresponsive(Long clientUnresponsive) {
 		this.clientUnresponsive = clientUnresponsive;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("OnAction [logger=%s, target=%s, instance=%s, epoch=%s, clientUnresponsive=%s, toString()=%s]",
 						logger, target, instance, epoch, clientUnresponsive,
 						super.toString());
 	}
 
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime
 				* result
 				+ ((clientCredential == null) ? 0 : clientCredential.hashCode());
 		result = prime
 				* result
 				+ ((clientUnresponsive == null) ? 0 : clientUnresponsive
 						.hashCode());
 		result = prime * result + ((epoch == null) ? 0 : epoch.hashCode());
 		result = prime * result
 				+ ((instance == null) ? 0 : instance.hashCode());
 		result = prime * result + ((logger == null) ? 0 : logger.hashCode());
 		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
 		FileTransferAction other = (FileTransferAction) obj;
 		if (clientCredential == null) {
 			if (other.clientCredential != null)
 				return false;
 		} else if (!clientCredential.equals(other.clientCredential))
 			return false;
 		if (clientUnresponsive == null) {
 			if (other.clientUnresponsive != null)
 				return false;
 		} else if (!clientUnresponsive.equals(other.clientUnresponsive))
 			return false;
 		if (epoch == null) {
 			if (other.epoch != null)
 				return false;
 		} else if (!epoch.equals(other.epoch))
 			return false;
 		if (instance == null) {
 			if (other.instance != null)
 				return false;
 		} else if (!instance.equals(other.instance))
 			return false;
 		if (logger == null) {
 			if (other.logger != null)
 				return false;
 		} else if (!logger.equals(other.logger))
 			return false;
 		if (target == null) {
 			if (other.target != null)
 				return false;
 		} else if (!target.equals(other.target))
 			return false;
 		return true;
 	}
 
 	/*
 	 * Unit testing
 	 * ============
 	 */
 	protected Task getTask(Client client, String target) {
 		WebResource resource = client.resource(target);
 		return resource.get(Task.class);
 	}
 	
 	protected URI sendRequest(Client client, URI target, Form form) {
 		WebResource resource;
 		resource = client.resource(target);
 
 		ClientResponse response = resource.path("xfer").type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, form);
 
 		URI location = response.getLocation();
 		if(location != null) {
 			log.fine(this.name+" file copy epoch. Factory "+location.toString()+" initiating status "+response.getStatus());
 			logger.info("file copy epoch.");
 
 		} else {
 			log.log(Level.SEVERE, this.name+" file copy initiation FAILED with status "+response.getStatus());
 			logger.error("file copy initiation FAILED with status "+response.getStatus());
 
 			throw new UnprocessableEntityException(this.name+" file copy initiation FAILED with status "+response.getStatus());
 		}
 
 		return location;
 	}
 
 	
 }

 /**
  * @author Nigel Cook
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 package n3phele.service.model.core;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Embed;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Unindex;
 
 
 
 @XmlRootElement(name="VirtualServer")
 @XmlType(name="VirtualServer", propOrder={"created", "description", "location", "parameters", "status", "notification", 
 										"instanceId", "spotId", "outputParameters", "siblings", "idempotencyKey", "zombie"})
 @Unindex
 @Cache
 @com.googlecode.objectify.annotation.Entity
 public class VirtualServer extends Entity {
 	@com.googlecode.objectify.annotation.Id private Long id;
 	private String description;
 	private String location;
 	@Embed private ArrayList<NameValue> parametersList;
 	private VirtualServerStatus status;
 	private String notification;
 	private String instanceId;
 	private String spotId;
 	private String accessKey;
 	private String encryptedKey;
 	private ArrayList<String> siblings;
 	@Index private String idempotencyKey;
 	//TODO: check if it is the date it was created and not the date it was made into zombie
 	private Date created;
 	private boolean zombie;
 	@Embed private ArrayList<NameValue> outputParameters;
 	
 	
 	public VirtualServer() {}
 	
 
 	/**
 	 * Creates a new virtual server
 	 * @param name virtual server label
 	 * @param description textual description of the virtual server content and purpose
 	 * @param location history end point
 	 * @param parametersMap virtual server history parameters
 	 * @param notification location to post virtual machine status updates
 	 * @param EC2 InstanceId
 	 * @param EC2 Spot request Id
 	 * @param accessKey virtual server access key
 	 * @param encryptedKey virtual server encrypted key
 	 * @param owner virtual machine owner
 	 * @param idempotencyKey globally unique key for the VM
 	 */
 	public VirtualServer(String name, String description,
 		 URI location, ArrayList<NameValue> parametersList, URI notification,
 		 String instanceId, String spotId,
 		 String accessKey, String encryptedKey, URI owner, String idempotencyKey) {
 		super(name, null, owner, false);
 		this.id = null;
 		this.description = description;
 		this.location = (location==null)?null:location.toString();
 		this.parametersList = parametersList;
 		this.notification = (notification != null)? notification.toString() : null;
 		this.instanceId = instanceId;
 		this.spotId = spotId;
 		this.status = VirtualServerStatus.initializing;
 		this.accessKey = accessKey;
 		this.encryptedKey = encryptedKey;
 		this.idempotencyKey = idempotencyKey;
 	}
 	
 	/**
 	 * Creates a new virtual server
 	 * @param name virtual server label
 	 * @param description textual description of the virtual server content and purpose
 	 * @param location history end point
 	 * @param parametersMap virtual server history parameters
 	 * @param notification location to post virtual machine status updates
 	 * @param accessKey virtual server access key
 	 * @param encryptedKey virtual server encrypted key
 	 * @param owner virtual machine owner
 	 * @param idempotencyKey globally unique key for the VM
 	 */
 	public VirtualServer(String name, String description,
 		 URI location, ArrayList<NameValue> parameters, URI notification,
 		  String accessKey, String encryptedKey, URI owner, String idempotencyKey) {
 		this(name, description, location, null, notification,
 				null, null, accessKey, encryptedKey, owner, idempotencyKey);
 		this.parametersList = parameters;
 	}
 	
 
 	/**
 	 * @return the id
 	 */
 	@XmlTransient
 	public Long getId() {
 		return id;
 	}
 
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 
 	/**
 	 * @return the outputParameters
 	 */
 	public ArrayList<NameValue> getOutputParameters() {
 		return outputParameters;
 	}
 
 
 	/**
 	 * @param outputParameters the outputParameters to set
 	 */
 	public void setOutputParameters(ArrayList<NameValue> outputParameters) {
 		this.outputParameters = outputParameters;
 	}
 
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 
 	/**
 	 * @return the location
 	 */
 	public URI getLocation() {
 		return (location==null)?null:URI.create(location);
 	}
 
 
 	/**
 	 * @param location the location to set
 	 */
 	public void setLocation(URI location) {
 		this.location = (location==null)?null:location.toString();
 	}
 
 	/**
 	 * @return the parameters
 	 */
 	@XmlElement
 	public ArrayList<NameValue> getParameters() {
 		return this.parametersList;
 	}
 	/**
 	 * @param p the parameters to set
 	 */
 	public void setParameters(ArrayList<NameValue> p) {
 		this.parametersList = p;
 	}
 
 	/**
 	 * @return the parametersMap
 	 */
 	@XmlTransient
 	public HashMap<String, String> getParametersMap() {
 		return NameValue.toMap(this.parametersList);
 	}
 
 
 	/**
 	 * @param parametersMap the parametersMap to set
 	 */
 	public void setParametersMap(HashMap<String, String> parametersMap) {
 		this.parametersList = NameValue.toList(parametersMap);
 	}
 
 
 	/**
 	 * @return the status
 	 */
 	public VirtualServerStatus getStatus() {
 		return status;
 	}
 
 
	/*
 	 * @param status the status to set
 	 */
 	public void setStatus(VirtualServerStatus status) {
 		this.status = status;
 	}
 
 
 	/**
 	 * @return the notification
 	 */
 	public URI getNotification() {
 		URI result = null;
 		if(this.notification != null) {
 			result = URI.create(this.notification);
 		}
 		return result;
 	}
 
 	
 
 	/**
 	 * @return the instanceId
 	 */
 	public String getInstanceId() {
 		return instanceId;
 	}
 
 
 	/**
 	 * @param instanceId the instanceId to set
 	 */
 	public void setInstanceId(String instanceId) {
 		this.instanceId = instanceId;
 	}
 
 
 	/**
 	 * @return the spotId
 	 */
 	public String getSpotId() {
 		return spotId;
 	}
 
 
 	/**
 	 * @param spotId the spotId to set
 	 */
 	public void setSpotId(String spotId) {
 		this.spotId = spotId;
 	}
 
 
 	/**
 	 * @param notification the notification to set
 	 */
 	public void setNotification(URI notification) {
 		if(notification != null)
 			this.notification = notification.toString();
 		else
 			this.notification = null;
 	}
 
 
 
 	/**
 	 * @return the siblings
 	 */
 	public ArrayList<String> getSiblings() {
 		 return this.siblings;
 	}
 
 
 	/**
 	 * @param siblings the siblings to set
 	 */
 	public void setSiblings(ArrayList<String> siblings) {
 		this.siblings = siblings;
 	}
 
 
 	/**
 	 * @return the accessKey
 	 */
 	@XmlTransient
 	public String getAccessKey() {
 		return accessKey;
 	}
 
 
 	/**
 	 * @param accessKey the accessKey to set
 	 */
 	public void setAccessKey(String accessKey) {
 		this.accessKey = accessKey;
 	}
 
 
 	/**
 	 * @return the encryptedKey
 	 */
 	@XmlTransient
 	public String getEncryptedKey() {
 		return encryptedKey;
 	}
 
 
 	/**
 	 * @param encryptedKey the encryptedKey to set
 	 */
 	public void setEncryptedKey(String encryptedKey) {
 		this.encryptedKey = encryptedKey;
 	}
 
 
 	/**
 	 * @return the idempotencyKey
 	 */
 	public String getIdempotencyKey() {
 		return idempotencyKey;
 	}
 
 
 	/**
 	 * @param idempotencyKey the idempotencyKey to set
 	 */
 	public void setIdempotencyKey(String idempotencyKey) {
 		this.idempotencyKey = idempotencyKey;
 	}
 
 
 	/**
 	 * @return the created
 	 */
 	public Date getCreated() {
 		return created;
 	}
 
 
 	/**
 	 * @param created the created to set
 	 */
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 
 	/**
 	 * @return the zombie
 	 */
 	public boolean isZombie() {
 		return zombie;
 	}
 	
 	/**
 	 * @return the zombie
 	 */
 	public boolean getZombie() {
 		return zombie;
 	}
 
 
 	/**
 	 * @param zombie the zombie state to set
 	 */
 	public void setZombie(boolean zombie) {
 		this.zombie = zombie;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("VirtualServer [id=%s, description=%s, location=%s, parametersList=%s, status=%s, notification=%s, instanceId=%s, spotId=%s, accessKey=%s, encryptedKey=%s, siblings=%s, idempotencyKey=%s, created=%s, zombie=%s, outputParameters=%s]",
 						id, description, location, parametersList, status,
 						notification, instanceId, spotId, accessKey,
 						encryptedKey, siblings, idempotencyKey, created,
 						zombie, outputParameters);
	}}
 
 

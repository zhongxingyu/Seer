 package n3phele.service.model;
 
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
 
 import static n3phele.service.model.core.Helpers.URItoString;
 import static n3phele.service.model.core.Helpers.stringToURI;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.model.core.Entity;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.NarrativeResource;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Parent;
 import com.googlecode.objectify.annotation.Unindex;
 import com.googlecode.objectify.condition.IfTrue;
 import com.googlecode.objectify.condition.IfNotZero;
 import com.googlecode.objectify.condition.IfNotNull;
 
 @XmlRootElement(name="CloudProcess")
 @XmlType(name="CloudProcess", propOrder={"description", "state", "running", "waitTimeout", "pendingInit", "pendingCall", "pendingCancel", "pendingDump", "pendingAssertion", 
 		"dependentOn", "dependencyFor", "start", "complete", "finalized", "action", "parent", "topLevel", "narrative","costPerHour","epoch","account"})
 @Unindex
 @com.googlecode.objectify.annotation.Entity
 public class CloudProcess extends Entity {
 	final private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(CloudProcess.class.getName()); 
 	@Id protected Long id;
 	protected ActionState state = ActionState.NEWBORN;
 	protected Date running = null;
 	protected Date waitTimeout = null;
 	protected boolean pendingInit = false;
 	protected boolean pendingCall = false;
 	protected boolean pendingCancel = false;
 	protected boolean pendingDump = false;
 	protected ArrayList<String> pendingAssertion = new ArrayList<String>();
 	protected ArrayList<String> dependentOn = new ArrayList<String>();
 	protected ArrayList<String> dependencyFor = new ArrayList<String>();
 	@Index protected Date start = null;
 	@Index protected Date complete = null;
 	@Index protected boolean finalized = false;
 	protected String action = null;
 	@Index protected String parent = null;
 	@Parent Key<CloudProcess> root;
 	@Index(IfTrue.class) boolean topLevel = false;
 	@Index(IfTrue.class) boolean haveCost = false;
 	@Index(IfNotZero.class)protected float costPerHour = 0;
 	@Index(IfNotNull.class)protected Date epoch = null;
	@Index(IfNotZero.class)protected String account = null;
 
 
 	
 	
 	public CloudProcess() {}
 	/** Describes a cloud process. 
 	 * @param owner process owner
 	 * @param parent Parent to be notified on process state changes
 	 * @param taskId Reference to the action managed by the process
 	 */
 	public CloudProcess(URI owner, String name, CloudProcess parent, boolean topLevel, Action task)  {
 		super(name, null, owner, false);
 		this.action = task.getUri().toString();
 		this.topLevel = topLevel;
 		if(parent != null) {
 			this.parent = parent.getUri().toString();
 			if(parent.root == null) {
 				this.root = Key.create(parent);
 			} else {
 				this.root = parent.root;
 			}
 		} else {
 			this.parent = null;
 			this.root = null;
 		}
 	}
 	
 	/*
 	 * Getters and Settings
 	 */
 	/**
 	 * @param account the process account
 	 */
 	public void setAccount(String account){
 		this.account = account;
 	}
 	
 	/**
 	 * @return the account associated with the process
 	 */
 	public String getAccount(){
 		return this.account;
 	}
 	
 	/**
 	 * @return the epoch of the server
 	 */
 	public Date getEpoch(){
 		return epoch;
 	}
 	
 	/**
 	 * @param epoch the epoch date of the server
 	 */
 	public void setEpoch(Date epoch){
 		this.epoch = epoch;
 	}
 	
 	/**
 	 * @return the cost per hour
 	 */
 	public float getCostPerHour(){
 		return costPerHour;
 	}
 	
 	/**
 	 * @param costPerHour the cost per hour to set
 	 */
 	public void setCostPerHour(float costPerHour){
 		this.costPerHour = costPerHour;
 		this.haveCost = (costPerHour > 0);
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
 	 * @return the process state
 	 */
 	public ActionState getState() {
 		return state;
 	}
 	/**
 	 * @param state the process state to set
 	 */
 	public void setState(ActionState state) {
 		this.state = state;
 	}
 	
 	/**
 	 * @return the date process was queued to run
 	 */
 	public Date getRunning() {
 		return running;
 	}
 	/**
 	 * @param running the date process was queued to run
 	 */
 	public void setRunning(Date running) {
 		this.running = running;
 	}
 	
 	/**
 	 * @return the time the process execution delay until, unless signalled
 	 */
 	public Date getWaitTimeout() {
 		return waitTimeout;
 	}
 	/**
 	 * @param waitTimeout the time the process will delay until, unless signalled
 	 */
 	public void setWaitTimeout(Date waitTimeout) {
 		this.waitTimeout = waitTimeout;
 	}
 	/**
 	 * @return pending Init operation
 	 */
 	public boolean isPendingInit() {
 		return pendingInit;
 	}
 	/**
 	 * @return pending Init operation
 	 */
 	public boolean getPendingInit() {
 		return pendingInit;
 	}
 	/**
 	 * @param pendingInit set pending Init state
 	 */
 	public void setPendingInit(boolean pendingInit) {
 		this.pendingInit = pendingInit;
 	}
 	/**
 	 * @return pending Call operation
 	 */
 	public boolean isPendingCall() {
 		return pendingCall;
 	}
 	/**
 	 * @return pending Call operation
 	 */
 	public boolean getPendingCall() {
 		return pendingCall;
 	}
 	/**
 	 * @param pendingCall set pending Call
 	 */
 	public void setPendingCall(boolean pendingCall) {
 		this.pendingCall = pendingCall;
 	}
 	/**
 	 * @return pending Cancel operation
 	 */
 	public boolean isPendingCancel() {
 		return pendingCancel;
 	}
 	/**
 	 * @return pending Cancel operation
 	 */
 	public boolean getPendingCancel() {
 		return pendingCancel;
 	}
 	/**
 	 * @param pendingCancel set pending Cancel state
 	 */
 	public void setPendingCancel(boolean pendingCancel) {
 		this.pendingCancel = pendingCancel;
 	}
 	/**
 	 * @return pending Dump operation
 	 */
 	public boolean isPendingDump() {
 		return pendingDump;
 	}
 	/**
 	 * @return pending Dump operation
 	 */
 	public boolean getPendingDump() {
 		return pendingDump;
 	}
 	/**
 	 * @param pendingDump set the pending Dump state
 	 */
 	public void setPendingDump(boolean pendingDump) {
 		this.pendingDump = pendingDump;
 	}
 	
 	/**
 	 * @return pending Assertion(s)
 	 */
 	public List<String> getPendingAssertion() {
 		return pendingAssertion;
 	}
 	/**
 	 * @param pendingAssertion set pending Assertion list
 	 */
 	public void setPendingAssertion(List<String> pendingAssertion) {
 		this.pendingAssertion.clear();
 		this.pendingAssertion.addAll(pendingAssertion);
 	}
 	/**
 	 * @return TRUE if process has pending assertions
 	 */
 	public boolean hasPendingAssertions() {
 		return this.pendingAssertion.size() != 0;
 	}
 	
 	/**
 	 * @return TRUE if has currently pending actions
 	 */
 	public boolean hasPending() {
 		return pendingInit || pendingCall || pendingCancel || pendingDump || pendingAssertion.size() != 0;
 	}
 	/**
 	 * @return the dependentOn
 	 */
 	public ArrayList<String> getDependentOn() {
 		return dependentOn;
 	}
 	/**
 	 * @param dependentOn the dependentOn to set
 	 */
 	public void setDependentOn(ArrayList<String> dependentOn) {
 		this.dependentOn = dependentOn;
 	}
 	/**
 	 * @return the dependencyFor
 	 */
 	public ArrayList<String> getDependencyFor() {
 		return dependencyFor;
 	}
 	/**
 	 * @param dependencyFor the dependencyFor to set
 	 */
 	public void setDependencyFor(ArrayList<String> dependencyFor) {
 		this.dependencyFor = dependencyFor;
 	}
 	/**
 	 * @return the date execution started
 	 */
 	public Date getStart() {
 		return start;
 	}
 	/**
 	 * @param start the execution start date
 	 */
 	public void setStart(Date start) {
 		this.start = start;
 	}
 	/**
 	 * @return the execution complete date
 	 */
 	public Date getComplete() {
 		return complete;
 	}
 	/**
 	 * @param complete the execution complete date to set
 	 */
 	public void setComplete(Date complete) {
 		this.complete = complete;
 	}
 	/**
 	 * @return process execution finalized
 	 */
 	public boolean isFinalized() {
 		return finalized;
 	}
 	/**
 	 * @param finalized the finalized to set
 	 */
 	public void setFinalized(boolean finalized) {
 		this.finalized = finalized;
 	}
 	/**
 	 * @return the action
 	 */
 	public URI getAction() {
 		return stringToURI(action);
 	}
 	/**
 	 * @param action the action to set
 	 */
 	public void setAction(URI task) {
 		this.action = URItoString(task);
 	}
 	/**
 	 * @return the parent
 	 */
 	public URI getParent() {
 		return stringToURI(parent);
 	}
 	/**
 	 * @param parent the parent to set
 	 */
 	public void setParent(URI parent) {
 		this.parent = URItoString(parent);
 	}
 	
 	/**
 	 * @return the root
 	 */
 	@XmlTransient
 	public Key<CloudProcess> getRoot() {
 		return root;
 	}
 	/**
 	 * @param root the root to set
 	 */
 	public void setRoot(Key<CloudProcess> root) {
 		this.root = root;
 	}
 
 	/**
 	 * @return the narrative
 	 */
 	@XmlElement
 	public Collection<Narrative> getNarrative() {
 		return this.topLevel? NarrativeResource.dao.getNarratives(this.getUri()) :
 							  NarrativeResource.dao.getProcessNarratives(this.getUri());
 	}
 	/**
 	 * @param narrative the narrative to set
 	 */
 	public void setNarrative(Collection<Narrative> narrative) {
 		
 	}
 	
 	/**
 	 * @return the description
 	 */
 	@XmlElement
 	public String getDescription() {
 		return ActionResource.dao.load(this.getAction()).getDescription();
 	}
 	/**
 	 * @param description this function does nothing, but appease jersey
 	 */
 	public void setDescription(String description) {
 		
 	}
 	
 	/**
 	 * @return the topLevel
 	 */
 	public boolean isTopLevel() {
 		return topLevel;
 	}
 	
 	/**
 	 * @return the topLevel
 	 */
 	public boolean getTopLevel() {
 		return topLevel;
 	}
 	
 	/**
 	 * @param topLevel the topLevel to set
 	 */
 	public void setTopLevel(boolean topLevel) {
 		this.topLevel = topLevel;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("CloudProcess [id=%s, state=%s, running=%s, waitTimeout=%s, pendingInit=%s, pendingCall=%s, pendingCancel=%s, pendingDump=%s, pendingAssertion=%s, dependentOn=%s, dependencyFor=%s, start=%s, complete=%s, finalized=%s, action=%s, parent=%s, root=%s, topLevel=%s]",
 						id, state, running, waitTimeout, pendingInit,
 						pendingCall, pendingCancel, pendingDump,
 						pendingAssertion, dependentOn, dependencyFor, start,
 						complete, finalized, action,
 						parent, root, topLevel);
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((action == null) ? 0 : action.hashCode());
 		result = prime * result
 				+ ((complete == null) ? 0 : complete.hashCode());
 		result = prime * result
 				+ ((dependencyFor == null) ? 0 : dependencyFor.hashCode());
 		result = prime * result
 				+ ((dependentOn == null) ? 0 : dependentOn.hashCode());
 		result = prime * result + (finalized ? 1231 : 1237);
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
 		result = prime
 				* result
 				+ ((pendingAssertion == null) ? 0 : pendingAssertion.hashCode());
 		result = prime * result + (pendingCall ? 1231 : 1237);
 		result = prime * result + (pendingCancel ? 1231 : 1237);
 		result = prime * result + (pendingDump ? 1231 : 1237);
 		result = prime * result + (pendingInit ? 1231 : 1237);
 		result = prime * result + ((root == null) ? 0 : root.hashCode());
 		result = prime * result + ((running == null) ? 0 : running.hashCode());
 		result = prime * result + ((start == null) ? 0 : start.hashCode());
 		result = prime * result + ((state == null) ? 0 : state.hashCode());
 		result = prime * result + (topLevel ? 1231 : 1237);
 		result = prime * result
 				+ ((waitTimeout == null) ? 0 : waitTimeout.hashCode());
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
 		CloudProcess other = (CloudProcess) obj;
 		if (action == null) {
 			if (other.action != null)
 				return false;
 		} else if (!action.equals(other.action))
 			return false;
 		if (complete == null) {
 			if (other.complete != null)
 				return false;
 		} else if (!complete.equals(other.complete))
 			return false;
 		if (dependencyFor == null) {
 			if (other.dependencyFor != null)
 				return false;
 		} else if (!dependencyFor.equals(other.dependencyFor))
 			return false;
 		if (dependentOn == null) {
 			if (other.dependentOn != null)
 				return false;
 		} else if (!dependentOn.equals(other.dependentOn))
 			return false;
 		if (finalized != other.finalized)
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!parent.equals(other.parent))
 			return false;
 		if (pendingAssertion == null) {
 			if (other.pendingAssertion != null)
 				return false;
 		} else if (!pendingAssertion.equals(other.pendingAssertion))
 			return false;
 		if (pendingCall != other.pendingCall)
 			return false;
 		if (pendingCancel != other.pendingCancel)
 			return false;
 		if (pendingDump != other.pendingDump)
 			return false;
 		if (pendingInit != other.pendingInit)
 			return false;
 		if (root == null) {
 			if (other.root != null)
 				return false;
 		} else if (!root.equals(other.root))
 			return false;
 		if (running == null) {
 			if (other.running != null)
 				return false;
 		} else if (!running.equals(other.running))
 			return false;
 		if (start == null) {
 			if (other.start != null)
 				return false;
 		} else if (!start.equals(other.start))
 			return false;
 		if (state != other.state)
 			return false;
 		if (topLevel != other.topLevel)
 			return false;
 		if (waitTimeout == null) {
 			if (other.waitTimeout != null)
 				return false;
 		} else if (!waitTimeout.equals(other.waitTimeout))
 			return false;
 		return true;
 	}
 
 	
 
 }

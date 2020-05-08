 package org.dawb.common.ui.plot;
 
 import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 
 class ActionContainer {
 
 	private String  groupId;
 	private IAction action;
 	private IContributionManager manager;
 
 	public ActionContainer(String groupName, IAction action, IContributionManager manager) {
 		this.groupId = groupName;
 		this.action  = action;
 		this.manager = manager;
 	}
 
 	@SuppressWarnings("unused")
 	public void setAction(IAction action) {
 		this.action = action;
 	}
 
 	@SuppressWarnings("unused")
 	public void setManager(IContributionManager manager) {
 		this.manager = manager;
 	}
 
 	public String toString() {
 		return action.toString();
 	}
 
 	public void setGroupId(String groupName) {
 		this.groupId = groupName;
 	}
 
	public void insert(boolean force) {
		if (!force && isActive()) return;
 		manager.appendToGroup(groupId, action);
 	}
 	
 	public void remove() {
 		manager.remove(action.getId());
 	}
 
 	public boolean isActive() {
		IContributionItem act = manager.find(action.getId());
		if (act==null) return false;
		return true;
 	}
 
 	public boolean isId(String id) {
 		return this.action.getId()!=null && this.action.getId().equals(id);
 	}
 
 
 }

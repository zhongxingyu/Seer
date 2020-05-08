 /* SwitchedSortAgentImpl.java
 
 	Purpose:
 
 	Description:
 
 	History:
 		2012/5/10 Created by Hawk
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.Map;
 
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.impl.ClientCtrl;
 import org.zkoss.zats.mimic.impl.EventDataManager;
 import org.zkoss.zats.mimic.operation.SortAgent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.SortEvent;
 
 /**
  * For Treecol & Listheader, their sorting order's switching is pre-defined. Initially, it's unsorted (natural).
  * When clicking first time, it become ascending. Then it switches between ascending and descending.<br/>
  * natural --> ascending, descending <--> ascending
  * @author Hawk
  *
  */
 public abstract class SwitchedSortAgentImpl extends AgentDelegator<ComponentAgent> implements SortAgent{
 
 	public static final String ASCENDING = "ascending";
 	public static final String DESCENDING = "descending";
 	
 	/**
 	 * @param target 
 	 */
 	public SwitchedSortAgentImpl(ComponentAgent target) {
 		super(target);
 	}
 	
 	protected abstract String getSortDirection();
 
 	/**
 	 * Send AU data based on the component's current sorting direction.
 	 * For Treecol & Listheader, it only sends AU when desired order is different from current sorted order. 
 	 * Their sorted order's switching is pre-defined, natural --> ascending, ascending <--> descending.
 	 * Please refer Listheader.onSort().
 	 */
 	public void sort(boolean ascending) {
 		String desktopId = target.getDesktop().getId();
 		String cmd = Events.ON_SORT;
 		Component header= ((Component)target.getDelegatee());
 
 		Map<String, Object> data = null;
 		try{
 			//When desired sorting direction equals component's current direction, do *not* send AU.
 			String currentDirection = getSortDirection();
 
 			//if header is sorted, switch sorting direction between ascending and descending
 			if (currentDirection.equals(ASCENDING)){
 				if (!ascending){
 					data = EventDataManager.getInstance().build(new SortEvent(cmd, header, false));
 					((ClientCtrl)target.getClient()).postUpdate(desktopId, cmd, header.getUuid(), data, null);
 				}
 			}else if (currentDirection.equals(DESCENDING)){
 				if (ascending){
 					data = EventDataManager.getInstance().build(new SortEvent(cmd, header, true));
 					((ClientCtrl)target.getClient()).postUpdate(desktopId, cmd, header.getUuid(), data, null);
 				}
 				((ClientCtrl) getClient()).flush(desktopId);
 				return;
 			}else { //natural, not sorted yet
 				if(ascending){
 					data = EventDataManager.getInstance().build(new SortEvent(cmd, header, ascending));
 					((ClientCtrl)target.getClient()).postUpdate(desktopId, cmd, header.getUuid(), data, null);
 				}else{
 					data = EventDataManager.getInstance().build(new SortEvent(cmd, header, true));
 					((ClientCtrl)target.getClient()).postUpdate(desktopId, cmd, header.getUuid(), data, null);
 					data = EventDataManager.getInstance().build(new SortEvent(cmd, header, ascending));
 					((ClientCtrl)target.getClient()).postUpdate(desktopId, cmd, header.getUuid(), data, null);
 				}
 			}
 			((ClientCtrl) getClient()).flush(desktopId);
 		}catch(Exception e){
 			throw new AgentException(e.getMessage(), e);
 		}
 	}
 
 }

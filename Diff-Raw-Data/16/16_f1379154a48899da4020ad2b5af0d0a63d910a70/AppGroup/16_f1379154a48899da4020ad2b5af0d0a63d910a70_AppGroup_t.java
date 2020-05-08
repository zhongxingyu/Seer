 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.group;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.meerkat.services.WebApp;
 
 public class AppGroup {
 
 	private static Logger log = Logger.getLogger(AppGroup.class);
 	private String groupname;
 	private String groupDesc;
 	private List<WebApp> appGroupedList = Collections
 			.synchronizedList(new ArrayList<WebApp>());
	
 	public AppGroup(String groupName) {
 		this.groupname = groupName;
 	}
 
 	/**
 	 * getGroupName
 	 * 
 	 * @return
 	 */
 	public final String getGroupName() {
 		return groupname;
 	}
 
 	/**
 	 * setGroupDesc
 	 * 
 	 * @param desc
 	 */
 	public final void setGroupDesc(String desc) {
 		this.groupDesc = desc;
 	}
 
 	/**
 	 * getGroupDesc
 	 * 
 	 * @return
 	 */
 	public final String getGroupDesc() {
 		return groupDesc;
 	}
 
 	/**
 	 * 
 	 * @param app
 	 */
 	public final void addApp(WebApp app) {
 		if (!appGroupedList.contains(app)) {
 			appGroupedList.add(app);
 		}
 	}
 
 	/**
 	 * getGroupAppsIterator
 	 * 
 	 * @return
 	 */
 	public final Iterator<WebApp> getGroupAppsIterator() {
 		return appGroupedList.iterator();
 	}
 
 	/**
 	 * logGroupMembers
 	 */
 	public final void logGroupMembers() {
 		Iterator<WebApp> it = getGroupAppsIterator();
 		WebApp temp;
 		log.info("Members of group " + this.groupname + ": ");
 
 		while (it.hasNext()) {
 			temp = it.next();
 			log.info("- " + temp.getName());
 		}
 	}
 
 	/**
 	 * getGroupAvailabilityAvg
 	 * 
 	 * @return
 	 */
 	public final double getGroupAvailabilityAvg() {
 		Iterator<WebApp> it = getGroupAppsIterator();
 		double availabilitySUM = 0;
		int activeMemberApps = 0;
 
 		WebApp tempWebApp;
 		while (it.hasNext()) {
 			tempWebApp = it.next();
			if(tempWebApp.isActive()){
				activeMemberApps++;
				availabilitySUM += tempWebApp.getAvailability();
			}
 		}
 
		return availabilitySUM / activeMemberApps;
 
 	}
 
 }

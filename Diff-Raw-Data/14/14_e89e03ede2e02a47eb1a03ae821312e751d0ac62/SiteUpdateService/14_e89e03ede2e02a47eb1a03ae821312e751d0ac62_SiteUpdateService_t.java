 package com.cee.news.client.content;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 /**
  * Asynchronously update service. Updates the articles of sites.
  */
 @RemoteServiceRelativePath("services/gwtSiteUpdateService")
 public interface SiteUpdateService extends RemoteService {
 	/**
 	 * Utility class for simplifying access to the instance of async service.
 	 */
 	public static class Util {
 		private static SiteUpdateServiceAsync instance;
 		public static SiteUpdateServiceAsync getInstance(){
 			if (instance == null) {
 				instance = GWT.create(SiteUpdateService.class);
 			}
 			return instance;
 		}
 	}
 	
 	/**
 	 * Retrieves all sites information from a given web url
 	 * @param location The web URL
 	 * @return Site information, either in state ok and the site object embedded or in error state
 	 */
 	SiteData retrieveSiteData(String location);
 	
 	/**
 	 * Adds update task for given site to the update queue.
 	 * If the queue already contains a task for the site, no additional
 	 * task will be added.
 	 * @param site The site to update
 	 * @return Number of update tasks in the queue
 	 */
 	int addSiteToUpdateQueue(String site);
 	
 	/**
 	 * Adds update tasks for each given site to the update queue.
 	 * If the queue already contains a task for a given site, no additional
 	 * task will be added.
 	 * @param workingSetName The sites to update
 	 * @return Number of update tasks in the queue
 	 */
 	int addSitesOfWorkingSetToUpdateQueue(String workingSetName);
 	
 	/**
 	 * Removes all commands from queue
 	 */
 	void clearQueue();
 	
 	/**
 	 * @return The number of update tasks in the queue. Used for displaying 
 	 * a progress bar.
 	 */
 	int getUpdateQueueSize();
 }

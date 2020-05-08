 package org.iplantc.de.shared.services;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 /**
  * A service for interfacing with Confluence (service facade).
  * 
  * @author hariolf
  * 
  */
 public class ConfluenceServiceFacade {
     private static ConfluenceServiceFacade service;
 
    private final ConfluenceServiceAsync proxy;
 
     private ConfluenceServiceFacade() {
         final String SESSION_SERVICE = "confluence"; //$NON-NLS-1$
 
         proxy = (ConfluenceServiceAsync)GWT.create(ConfluenceService.class);
         ((ServiceDefTarget)proxy).setServiceEntryPoint(GWT.getModuleBaseURL() + SESSION_SERVICE);
     }
 
     /**
      * Retrieve service facade singleton instance.
      * 
      * @return a singleton instance of the service facade.
      */
     public static ConfluenceServiceFacade getInstance() {
         if (service == null) {
             service = new ConfluenceServiceFacade();
         }
 
         return service;
     }
 
     /**
      * Creates a new page in the iPlant wiki as a child of the "List of Applications" page.
      * 
      * @param toolName the name of the tool which is used as the page title
      * @param description a tool description
      * @param callback called after the service call finishes
      */
     public void createDocumentationPage(String toolName, String description,
             AsyncCallback<String> callback) {
         proxy.addPage(toolName, description, callback);
     }
 
     /**
      * Updates a documentation page with an average app rating.
      * 
      * @param toolName the name of the tool which is used as the page title
      * @param avgRating the new average rating score
      * @param callback called after the service call finishes
      */
    public void updateDocumentationPage(String toolName, int avgRating, AsyncCallback<Void> callback) {
         proxy.updatePage(toolName, avgRating, callback);
     }
 
     /**
      * Adds a user comment to a tool description page.
      * 
      * @param toolName the name of the tool which is also the page title
      * @param score the user's rating of the tool
      * @param username the DE user who rated the tool
      * @param comment a comment
      * @param callback called after the service call finishes
      */
     public void addComment(String toolName, int score, String username, String comment,
             AsyncCallback<String> callback) {
         proxy.addComment(toolName, score, username, comment, callback);
     }
 
     /**
      * Removes a user comment from a tool description page.
      * 
      * @param toolName the name of the tool which is also the page title
      * @param commentId the comment ID in Confluence
      * @param callback called after the service call finishes
      */
    public void removeComment(String toolName, Long commentId, AsyncCallback<Void> callback) {
         proxy.removeComment(toolName, commentId, callback);
     }
 
     /**
      * Changes an existing user comment on a tool description page.
      * 
      * @param toolName the name of the tool which is also the page title
      * @param score the user's rating of the tool
      * @param username the DE user who rated the tool
      * @param commentId the comment ID in Confluence
      * @param newComment the new comment text
      * @param callback called after the service call finishes
      */
     public void editComment(String toolName, int score, String username, long commentId,
            String newComment, AsyncCallback<Void> callback) {
         proxy.editComment(toolName, score, username, commentId, newComment, callback);
     }
 
     /**
      * Retrieves a user comment from a tool description page.
      * 
      * @param commentId the comment ID in Confluence
      * @param callback called after the service call finishes
      */
     public void getComment(long commentId, AsyncCallback<String> callback) {
         proxy.getComment(commentId, callback);
     }
 
     /**
      * 
      * Move the wiki doc page for an app to new location under list of application with new title
      * 
      * @param oldAppName
      * @param newAppName
      * @param callback called after the service call finishes
      */
     public void movePage(String oldAppName, String newAppName, AsyncCallback<String> callback) {
         proxy.movePage(oldAppName, newAppName, callback);
     }
 }

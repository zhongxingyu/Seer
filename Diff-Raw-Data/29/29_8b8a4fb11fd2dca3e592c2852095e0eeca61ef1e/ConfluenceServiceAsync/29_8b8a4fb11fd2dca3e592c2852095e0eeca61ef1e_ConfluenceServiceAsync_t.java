 package org.iplantc.de.shared.services;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * A service interface for interfacing with Confluence (asynchronous part).
  * 
  * @author hariolf
  * 
  */
 public interface ConfluenceServiceAsync {
 
     /**
      * Creates a new page in the iPlant wiki as a child of the "List of Applications" page.
      * 
      * @param toolName the name of the tool which is used as the page title
      * @param description a tool description
      * @param callback called after the service call finishes
      */
     void addPage(String toolName, String description, AsyncCallback<String> callback);
 
     /**
      * Updates a documentation page with an average app rating.
      * 
      * @param toolName the name of the tool which is used as the page title
      * @param avgRating the new average rating score
      * @param callback called after the service call finishes
      */
    void updatePage(String toolName, int avgRating, AsyncCallback<Void> callback);
 
     /**
      * Adds a user comment to a tool description page.
      * 
      * @param toolName the name of the tool which is also the page title
      * @param score the user's rating of the tool
      * @param username the DE user who rated the tool
      * @param comment a comment
      * @param callback called after the service call finishes
      */
     void addComment(String toolName, int score, String username, String comment,
             AsyncCallback<String> callback);
 
     /**
      * Removes a user comment from a tool description page.
      * 
      * @param toolName the name of the tool which is also the page title
      * @param commentId the comment ID in Confluence
      * @param callback called after the service call finishes
      */
    void removeComment(String toolName, Long commentId, AsyncCallback<Void> callback);
 
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
     void editComment(String toolName, int score, String username, Long commentId, String newComment,
            AsyncCallback<Void> callback);
 
     /**
      * Retrieves a user comment from a tool description page.
      * 
      * @param commentId the comment ID in Confluence
      * @param callback called after the service call finishes
      */
     void getComment(long commentId, AsyncCallback<String> callback);
 
     /**
      * 
      * Move the wiki doc page for an app to new location under list of application with new title
      * 
      * @param oldAppName
      * @param newAppName
      * @param callback called after the service call finishes
      */
     void movePage(String oldAppName, String newAppName, AsyncCallback<String> callback);
 }

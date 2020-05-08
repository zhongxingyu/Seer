 package com.orgsync.api;
 
 import java.util.List;
 
 import com.ning.http.client.ListenableFuture;
 import com.orgsync.api.model.Success;
 import com.orgsync.api.model.accounts.Account;
 import com.orgsync.api.model.groups.Group;
 
 /**
  * <p>
 * Access to the groups resource from the OrgSync API. This allows a client to list, update, create, and delete groups.
  * It also allows the client to get the accounts in a group, as well as add and remove accounts from the group.
  * 
  * <p>
  * See: <a href="https://api.orgsync.com/api/docs/v2/groups">https://api.orgsync.com/api/docs/v2/groups</a>
  * 
  * @author steffyj
  * 
  */
 public interface GroupsResource {
 
     /**
      * Get the list of groups for an org.
      * 
      * @param orgId
      *            the org to get the groups for
      * @return a future to the response with the list of groups
      */
     public ListenableFuture<ApiResponse<List<Group>>>
             getOrgGroups(int orgId);
 
     /**
      * Get a single group.
      * 
      * @param groupId
      *            the id of the group to retrieve
      * @return a future to the resposne with the group
      */
     public ListenableFuture<ApiResponse<Group>>
             getGroup(int groupId);
 
     /**
      * Create a group with a given name in the org.
      * 
      * @param name
      *            the name of the new group
      * @param orgId
      *            the id of the org to create the group in
      * @return a future to the resposne with the new group
      */
     public ListenableFuture<ApiResponse<Group>>
             createGroup(String name, int orgId);
 
     /**
      * Delete a group.
      * 
      * @param groupId
      *            the id of the group to delete.
      * @return a future to the response
      */
     public ListenableFuture<ApiResponse<Success>>
             deleteGroup(int groupId);
 
     /**
      * Update the name for a given group.
      * 
      * @param groupId
      *            the group to update
      * @param name
      *            the updated name
      * @return a future to the response with the new name
      */
     public ListenableFuture<ApiResponse<Group>>
             updateGroup(int groupId, String name);
 
     /**
      * List all of the accounts in a given group.
      * 
      * @param groupId
      *            the group to list the accounts for
      * @return a future to the response with the accounts
      */
     public ListenableFuture<ApiResponse<List<Account>>>
             listAccounts(int groupId);
 
     /**
      * Add the given account ids to the given group.
      * 
      * @param groupId
      *            the group to add accounts to
      * @param accountIds
      *            the ids of the accounts to add
      * @return a future to the response
      */
     public ListenableFuture<ApiResponse<Success>>
             addAccountsToGroup(int groupId, List<Integer> accountIds);
 
     /**
      * Remove the given accounts ids from the group.
      * 
      * @param groupId
      *            the group to remove accounts from
      * @param accountIds
      *            the account ids to remove
      * @return a future to the response
      */
     public ListenableFuture<ApiResponse<Success>>
             removeAccountsToGroup(int groupId, List<Integer> accountIds);
 
 }

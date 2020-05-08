 package com.vacuumhead.wesplit.application;
 
 import com.vacuumhead.wesplit.tables.Group;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pratyushverma
  * Date: 03/03/13
  * Time: 2:28 AM
  * To change this template use File | Settings | File Templates.
  */
 public interface IGroupApplicationService {
 
     public Group createGroup(String groupName, Integer accountId);
     public Group retrieveGroupByName(String groupName);
     public Group retrieveGroupById(Integer groupId);
     public Group addMembersToGroup(Integer groupId, Integer accountId);
     public Group addAdminToGroup(Integer groupId, Integer accountId);
    public boolean isAdminOfGroup(Integer groupId, Integer accountId);
 
 }

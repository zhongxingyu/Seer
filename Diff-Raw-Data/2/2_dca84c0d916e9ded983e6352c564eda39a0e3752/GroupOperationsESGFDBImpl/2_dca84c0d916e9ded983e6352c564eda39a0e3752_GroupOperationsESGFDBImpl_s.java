 package org.esgf.commonui;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.esgf.adminui.Group;
 
 import esg.common.util.ESGFProperties;
 import esg.node.security.GroupRoleDAO;
 import esg.node.security.UserInfoCredentialedDAO;
 
 public class GroupOperationsESGFDBImpl implements GroupOperationsInterface {
 
     private final static Logger LOG = Logger.getLogger(GroupOperationsESGFDBImpl.class);
     
     private GroupRoleDAO groupRoleDAO;
     private String passwd;
     private String root = "rootAdmin"; 
     public UserInfoCredentialedDAO myUserInfoDAO;
     
     public GroupOperationsESGFDBImpl() {
         try {
             ESGFProperties myESGFProperties = new ESGFProperties();
             groupRoleDAO = new GroupRoleDAO(myESGFProperties);
             this.passwd = myESGFProperties.getAdminPassword();
             
             setMyUserInfoDAO(new UserInfoCredentialedDAO(root,passwd,myESGFProperties));
         } catch(Exception e) {
             LOG.debug("Error in GroupOperationsESGFDBImpl Constructor");
         }
     }
 
     public void setMyUserInfoDAO(UserInfoCredentialedDAO myUserInfoDAO) {
         this.myUserInfoDAO = myUserInfoDAO;
     }
     
     
     /*
      * 
      */
     @Override
     public void addGroup(String groupName,String groupDescription) {
         groupRoleDAO.addGroup(groupName,groupDescription);
     }
     
     @Override
     public void editGroup(String groupId, String newGroupName, String groupDescription) {
         String oldGroupName = null;
         
         for(int i=0;i<groupRoleDAO.getGroupEntries().size();i++) {
             
             String groupCol = groupRoleDAO.getGroupEntries().get(i)[0];
             if(groupCol.equalsIgnoreCase(groupId)) {
                 if(groupCol.equalsIgnoreCase(groupId)) {
                     oldGroupName = groupRoleDAO.getGroupEntries().get(i)[1];
                 }
             }
         }
         if(oldGroupName != null) {
             groupRoleDAO.renameGroup(oldGroupName, newGroupName);
         }
     }
     
     @Override
     public void deleteGroup(String groupName) {
         groupRoleDAO.deleteGroup(groupName);
     }
     
     
     @Override
     public String getGroupIdFromGroupName(String groupName) {
         String groupId = null;
         
         for(int i=0;i<groupRoleDAO.getGroupEntries().size();i++) {
             String groupCol = groupRoleDAO.getGroupEntries().get(i)[1];
             if(groupCol.equalsIgnoreCase(groupName)) {
                 groupId = groupRoleDAO.getGroupEntries().get(i)[0];
             }
         }
         
         return groupId;
     }
     
     /*
      * 
      */
     @Override
     public List<Group> getAllGroups() {
         List<Group> groups = new ArrayList<Group>();
         //System.out.println("GroupEntries|->" + groupRoleDAO.getGroupEntries().size());
        for(int i=0;i<groupRoleDAO.getGroupEntries().size();i++) {
             String groupCol = groupRoleDAO.getGroupEntries().get(i)[1];
             if(groupCol != null) {
                 Group group = new Group();
                 String groupDescription = groupRoleDAO.getGroupEntries().get(i)[2];
                 String groupName = groupRoleDAO.getGroupEntries().get(i)[1];
                 String groupId = groupRoleDAO.getGroupEntries().get(i)[0];
                 group = new Group(groupId,groupName,groupDescription);
                 groups.add(group);
             }
         }
         return groups;
     }
     
     /*
      * 
      */
     @Override
     public Group getGroupObjectFromGroupName(String groupName) {
         
         Group group = new Group();
         
         for(int i=0;i<groupRoleDAO.getGroupEntries().size();i++) {
             String groupCol = groupRoleDAO.getGroupEntries().get(i)[1];
             if(groupCol.equalsIgnoreCase(groupName)) {
                 String groupDescription = groupRoleDAO.getGroupEntries().get(i)[2];
                 String groupId = groupRoleDAO.getGroupEntries().get(i)[1];
                 group = new Group(groupId,groupName,groupDescription);
             }
             
         }
         return group;
     }
 
     @Override
     public String getGroupNameFromGroupId(String groupId) {
         // TODO Auto-generated method stub
         return null;
     }
     
     
     
     
 }

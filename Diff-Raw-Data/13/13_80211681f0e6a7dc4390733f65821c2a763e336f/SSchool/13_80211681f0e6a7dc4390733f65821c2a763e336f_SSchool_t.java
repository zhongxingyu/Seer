 /*
  *
  * Copyright (c) Critical Links S.A., All Rights Reserved.
  * (www.critical-links.com)
  *
  * This software is the proprietary information of Critical Links S.A. Use is
  * subject to license terms.
  *
  */
 package com.edgebox.eacds;
 
 import com.edgebox.eacds.data.CDGroup;
 import com.edgebox.eacds.data.CDSchool;
 import com.edgebox.eacds.net.CDConnection;
 import com.edgebox.eacds.net.CDPostResponse;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Augusto Campos <a-campos at critical-links.com>
  */
 public class SSchool extends SBaseModule {
 
     SSchool(String serverJavaScriptInterface) {
         super(serverJavaScriptInterface);
     }
 
     /**
      * List all Schools from the Content Server
      *
      * @param groupId - Group to list schools from
      * @param recursive - recursive in sub Groups
      * @param offset- start offset
      * @param limit - limit returned records
      * @return Collection<CDSchool>
      * @throws java.lang.Exception
      * @see CDSchool
      */
     public Collection<CDSchool> listSchools(int groupId, boolean recursive, int offset, int limit) throws Exception {
 
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.listSchools");
         params.put("param1", "" + groupId);
         params.put("param2", "" + recursive);
         params.put("param3", "" + offset);
         params.put("param4", "" + limit);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
 
         Collection<CDSchool> lrt = new ArrayList<>();
         CDSchool[] ar = gson.fromJson(rt, CDSchool[].class);
         lrt.addAll(Arrays.asList(ar));
 
         return lrt;
     }
 
     /**
      * Get School info
      *
      * @param schoolId
      * @return CDSchool
      * @throws Exception
      */
     public CDSchool getSchool(int schoolId) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.getSchool");
         params.put("param1", "" + schoolId);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
        CDPostResponse pr = CDPostResponse.build(rt);
        if (pr.success) {
             String school_jason = pr.data.toString();
             return gson.fromJson( school_jason, CDSchool.class);
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
            throw new Exception(pr.message);
        }
     }
 
     /**
      * Create new School info
      *
      * @param school CDSchool
      * @throws Exception
      */
     public void createSchool(CDSchool school) throws Exception {
         this.setSchool("SSchools.createSchool", school);
     }
 
     /**
      * Update School Info
      *
      * @param school
      * @throws Exception
      */
     public void updateSchool(CDSchool school) throws Exception {
         this.setSchool("SSchools.updateSchool", school);
 
     }
 
     private void setSchool(String action, CDSchool school) throws Exception {
         if (school == null) {
             throw new Exception("Invalid school information!");
         }
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", action);
         params.put("param1", gson.toJson(school));
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
 
         CDPostResponse pr = CDPostResponse.build(rt);
         if (pr.success) {
             // new fields add to class
             String school_jason = pr.data.toString();
             CDSchool nu = gson.fromJson(school_jason, CDSchool.class);
             school.cloneData(nu);
         } else {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
 
         /**
     * Set school message
      *
      * @param schoolId id of school
      * @param schoolMessage new message text to set
      * @throws Exception
      */
     public void setSchoolMessage(int schoolId, String schoolMessage ) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.setSchoolMessage");
         params.put("param1", "" + schoolId);
         params.put("param2", "\"" + schoolMessage + "\"");
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         CDPostResponse pr = CDPostResponse.build(rt);
         if (!pr.success) {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
     
     /**
      * Remove an existing schools
      *
      * @param schoolId
      * @throws Exception
      */
     public void removeSchool(int schoolId) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.removeSchool");
         params.put("param1", "" + schoolId);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         CDPostResponse pr = CDPostResponse.build(rt);
         if (!pr.success) {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
 
     /**
      * Move school to group
      *
      * @param schoolId school id to move
      * @param groupId group id to set in the school
      * @throws Exception
      */
     public void moveSchool(int schoolId, int groupId) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.moveSchool");
         params.put("param1", "" + schoolId);
         params.put("param2", "" + groupId);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         CDPostResponse pr = CDPostResponse.build(rt);
         if (!pr.success) {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
 
     /**
      * Move group to child of parent group
      *
      * @param groupId group id to move
      * @param parentId group id to be parent
      * @throws Exception
      */
     public void moveGroupToGroup(int groupId, int parentId) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.moveGroupToGroup");
         params.put("param1", "" + groupId);
         params.put("param2", "" + parentId);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         CDPostResponse pr = CDPostResponse.build(rt);
         if (!pr.success) {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
 
     /**
      * Get group information from content server
      *
      * @param groupId group id to get
      * @param recursive get children recursively
      * @return <CDGroup>
      * @throws Exception
      */
     public CDGroup getGroup(int groupId, boolean recursive) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.getGroup");
         params.put("param1", "" + groupId);
         params.put("param2", "" + recursive);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         return gson.fromJson(rt, CDGroup.class);
 
     }
 
     /**
      * Get all subgroups for parent group
      *
      * @param parentId Groups id to get the subgroups
      * @param recursive return all children recursively
      * @return Collection<CDGroup>
      * @throws Exception
      */
     public Collection<CDGroup> getSubGroups(int parentId, boolean recursive) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.getSubGroups");
         params.put("param1", "" + parentId);
         params.put("param2", "" + recursive);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
 
         Collection<CDGroup> lrt = new ArrayList<>();
         CDGroup[] ar = gson.fromJson(rt, CDGroup[].class);
         lrt.addAll(Arrays.asList(ar));
         return lrt;
     }
 
     /**
      * Create a new group
      *
      * @param group information
      * @throws Exception
      */
     public void createGroup(CDGroup group) throws Exception {
         this.setGroup("SSchools.createGroup", group);
     }
 
     /**
      * Update a existing group
      *
      * @param group new group info (must contain the id)
      * @throws Exception
      */
     public void updateGroup(CDGroup group) throws Exception {
         this.setGroup("SSchools.updateGroup", group);
     }
 
     private void setGroup(String action, CDGroup group) throws Exception {
         if (group == null) {
             throw new Exception("Invalid group information!");
         }
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", action);
         params.put("param1", gson.toJson(group));
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
 
         CDPostResponse pr = CDPostResponse.build(rt);
         if (pr.success) {
             // new fields add to class
             String grp_jason = pr.data.toString();
             CDGroup ng = gson.fromJson(grp_jason, CDGroup.class);
             group.cloneData(ng);
         } else {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message, new Throwable(pr.errorTrace));
         }
     }
 
     /**
      * Remove a existing group
      *
      * @param groupId group id to remove
      * @throws Exception
      */
     public void removeGroup(int groupId) throws Exception {
         Map<String, String> params = new LinkedHashMap<>();
         params.put("method", "SSchools.removeGroup");
         params.put("param1", "" + groupId);
 
         String rt = CDConnection.Post(this.ServerJavaScriptInterface, params);
         CDPostResponse pr = CDPostResponse.build(rt);
         if (!pr.success) {
             Logger.getLogger(this.getClass().getName()).log(Level.FINE, pr.log());
             throw new Exception(pr.message);
         }
     }
 
 }

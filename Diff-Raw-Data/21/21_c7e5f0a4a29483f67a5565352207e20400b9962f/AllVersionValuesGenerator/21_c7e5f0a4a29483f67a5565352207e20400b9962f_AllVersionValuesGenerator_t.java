 /*
  * $Id$
  * Copyright (c) 2004
  * Jukka Lindstrm
  */
 package com.laughingpanda.jira;
 
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.commons.collections.SequencedHashMap;
 import org.apache.log4j.Category;
 import org.ofbiz.core.entity.GenericValue;
 
 import com.atlassian.configurable.ValuesGenerator;
 import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
 import com.atlassian.jira.project.ProjectManager;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
 
 /**
  * Returns all the different possible versions sorted under projects. Projects
  * are included in the map with negative id's and versions with positives.
  * 
  * @author Jukka Lindstrm
  */
 public class AllVersionValuesGenerator implements ValuesGenerator {
 
     private final Category log = Category.getInstance(AllVersionValuesGenerator.class);
 
     private VersionManager versionManager;
    private PermissionManager permissionManager;
 
     public AllVersionValuesGenerator() {
         setVersionManager(ComponentManager.getInstance().getVersionManager());
        setPermissionManager(ManagerFactory.getPermissionManager());
     }
 
     public void setVersionManager(VersionManager versionManager) {
         this.versionManager = versionManager;
     }
 
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
     }
 
     public Map getValues(Map params) {
        User u = (User) params.get("User");                
         Map<Long,String> selection = new SequencedHashMap();
         try {
            Collection<GenericValue> projects = permissionManager.getProjects(Permissions.BROWSE, u);
             for (GenericValue project : projects) {
                 selection.put(new Long(-project.getLong("id").longValue()), project.getString("name"));
                 Collection<Version> versions = versionManager.getVersions(project);
                 for (Version version : versions) {
                     selection.put(version.getId(), "- " + version.getName());
                 }
             }
         } catch (Exception e) {
             log.error(e);
         }
         return selection;
     }
 
 }

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
 import com.atlassian.jira.project.ProjectManager;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.project.version.VersionManager;
 
 /**
  * Returns all the different possible versions sorted under projects. Projects
  * are included in the map with negative id's and versions with positives.
  * 
  * @author Jukka Lindstrm
  */
 public class AllVersionValuesGenerator implements ValuesGenerator {
 
     private final Category log = Category.getInstance(AllVersionValuesGenerator.class);
 
     private VersionManager versionManager;
    private ProjectManager projectManager;
 
     public AllVersionValuesGenerator() {
         setVersionManager(ComponentManager.getInstance().getVersionManager());
        setProjectManager(ComponentManager.getInstance().getProjectManager());
     }
 
     public void setVersionManager(VersionManager versionManager) {
         this.versionManager = versionManager;
     }
 
    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
     }
 
     public Map getValues(Map params) {
         Map<Long,String> selection = new SequencedHashMap();
         try {
            Collection<GenericValue> projects = projectManager.getProjects();
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

 package cz.admin24.myachievo.web2.service;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Repository;
 
 import cz.admin24.myachievo.connector.http.dto.PhaseActivity;
 import cz.admin24.myachievo.connector.http.dto.Project;
 import cz.admin24.myachievo.connector.http.dto.ProjectPhase;
 
 @Repository
 @Scope("session")
 public class ProjectsCache implements Serializable {
     private static final long                              serialVersionUID = 1L;
     private List<Project>                                  projects         = new ArrayList<Project>();
     private Map<String, List<ProjectPhase>>                projectsPhases   = new ConcurrentHashMap<String, List<ProjectPhase>>();
     private Map<Pair<String, String>, List<PhaseActivity>> phasesActivities = new ConcurrentHashMap<Pair<String, String>, List<PhaseActivity>>();
 
     @Autowired
     private AchievoConnectorWrapper                        achievoConnector;
     @Autowired
     private WorkReportCache                                workReportCache;
 
 
     public void clean() {
         projects = new ArrayList<Project>();
         projectsPhases = new ConcurrentHashMap<String, List<ProjectPhase>>();
         phasesActivities = new ConcurrentHashMap<Pair<String, String>, List<PhaseActivity>>();
     }
 
 
     public List<Project> getProjects() {
         if (projects.isEmpty()) {
             projects = achievoConnector.getProjects();
         }
 
         List<Project> ret = new ArrayList<Project>(projects);
         List<String> order = workReportCache.getProjectNamesOrder();
         Collections.sort(ret, new BaseObjectComparator(order));
 
         return ret;
     }
 
 
     public List<ProjectPhase> getPhases(String projectId, String projectName) {
         List<ProjectPhase> phases = projectsPhases.get(projectId);
         if (CollectionUtils.isEmpty(phases)) {
             phases = achievoConnector.getPhases(projectId);
             projectsPhases.put(projectId, phases);
         }
 
         List<ProjectPhase> ret = new ArrayList<ProjectPhase>(phases);
         List<String> order = workReportCache.getPhaseNamesOrder(projectName);
         Collections.sort(ret, new BaseObjectComparator(order));
 
         return ret;
     }
 
 
     public List<PhaseActivity> getActivities(String projectId, String phaseId, String projectName, String phaseName) {
         Pair<String, String> key = ImmutablePair.of(projectId, phaseId);
         List<PhaseActivity> activities = phasesActivities.get(key);
         if (CollectionUtils.isEmpty(activities)) {
             activities = achievoConnector.getActivities(projectId, phaseId);
             phasesActivities.put(key, activities);
         }
 
         List<PhaseActivity> ret = new ArrayList<PhaseActivity>(activities);
         List<String> order = workReportCache.getActivityNamesOrder(projectName, phaseName);
         Collections.sort(ret, new BaseObjectComparator(order));
 
         return ret;
     }
 
 
     public Project getProjectByName(String project) {
         for (Project p : getProjects()) {
            if (p.getName().equals(project)) {
                 return p;
             }
         }
         return null;
     }
 
 
     public ProjectPhase getPhaseByName(String phaseName, Project project) {
         for (ProjectPhase p : getPhases(project.getId(), project.getName())) {
            if (p.getName().equals(phaseName)) {
                 return p;
             }
         }
         return null;
     }
 
 
     public PhaseActivity getActivityByName(String activity, ProjectPhase phase, Project project) {
         for (PhaseActivity a : getActivities(project.getId(), phase.getId(), project.getName(), phase.getName())) {
            if (a.getName().equals(activity)) {
                 return a;
             }
         }
         return null;
     }
 
 }

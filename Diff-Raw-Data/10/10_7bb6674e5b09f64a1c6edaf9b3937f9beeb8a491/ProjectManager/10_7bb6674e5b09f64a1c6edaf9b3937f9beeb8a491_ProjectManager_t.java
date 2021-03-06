 package com.metaweb.gridworks;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.tools.tar.TarOutputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.metaweb.gridworks.history.HistoryEntryManager;
 import com.metaweb.gridworks.model.Project;
 import com.metaweb.gridworks.preference.PreferenceStore;
 import com.metaweb.gridworks.preference.TopList;
 
 /**
  * ProjectManager is responsible for loading and saving the workspace and projects.
  *
  *
  */
 public abstract class ProjectManager {
     // last n expressions used across all projects
     static protected final int s_expressionHistoryMax = 100;
 
     protected Map<Long, ProjectMetadata> _projectsMetadata;
     protected PreferenceStore            _preferenceStore;
 
     final static Logger logger = LoggerFactory.getLogger("project_manager");
 
     /**
      *  What caches the joins between projects.
      */
     transient protected InterProjectModel _interProjectModel = new InterProjectModel();
 
     /**
      *  Flags
      */
     transient protected int _busy = 0; // heavy operations like creating or importing projects are going on
 
     /**
      *  While each project's metadata is loaded completely at start-up, each project's raw data
      *  is loaded only when the project is accessed by the user. This is because project
      *  metadata is tiny compared to raw project data. This hash map from project ID to project
      *  is more like a last accessed-last out cache.
      */
     transient protected Map<Long, Project> _projects;
 
     static public ProjectManager singleton;
 
     protected ProjectManager(){
         _projectsMetadata = new HashMap<Long, ProjectMetadata>();
         _preferenceStore = new PreferenceStore();
         _projects = new HashMap<Long, Project>();
 
         preparePreferenceStore(_preferenceStore);
     }
 
     /**
      * Registers the project in the memory of the current session
      * @param project
      * @param projectMetadata
      */
     public void registerProject(Project project, ProjectMetadata projectMetadata) {
         synchronized (this) {
             _projects.put(project.id, project);
             _projectsMetadata.put(project.id, projectMetadata);
         }
     }
  //----------Load from data store to memory----------------
 
     /**
      * Load project metadata from data storage
      * @param projectID
      * @return
      */
     public abstract boolean loadProjectMetadata(long projectID);
 
     /**
      * Loads a project from the data store into memory
      * @param id
      * @return
      */
     protected abstract Project loadProject(long id);
 
     //------------Import and Export from Gridworks archive-----------------
     /**
      * Import project from a Gridworks archive
      * @param projectID
      * @param inputStream
      * @param gziped
      * @throws IOException
      */
     public abstract void importProject(long projectID, InputStream inputStream, boolean gziped) throws IOException;
 
     /**
      * Export project to a Gridworks archive
      * @param projectId
      * @param tos
      * @throws IOException
      */
     public abstract void exportProject(long projectId, TarOutputStream tos) throws IOException;
 
 
  //------------Save to record store------------
     /**
      * Saves a project and its metadata to the data store
      * @param id
      */
     public void ensureProjectSaved(long id) {
         synchronized(this){
             ProjectMetadata metadata = this.getProjectMetadata(id);
             if (metadata != null) {
                 try {
                     saveMetadata(metadata, id);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }//FIXME what should be the behaviour if metadata is null? i.e. not found
 
             Project project = getProject(id);
             if (project != null && metadata != null && metadata.getModified().after(project.getLastSave())) {
                 try {
                     saveProject(project);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }//FIXME what should be the behaviour if project is null? i.e. not found or loaded.
             //FIXME what should happen if the metadata is found, but not the project? or vice versa?
         }
 
     }
 
     /**
      * Save project metadata to the data store
      * @param metadata
      * @param projectId
      * @throws Exception
      */
     protected abstract void saveMetadata(ProjectMetadata metadata, long projectId) throws Exception;
 
     /**
      * Save project to the data store
      * @param project
      */
     protected abstract void saveProject(Project project);
 
     /**
      * Save workspace and all projects to data store
      * @param allModified
      */
     public void save(boolean allModified) {
         if (allModified || _busy == 0) {
             saveProjects(allModified);
             saveWorkspace();
         }
     }
 
     /**
      * Saves the workspace to the data store
      */
     protected abstract void saveWorkspace();
 
     /**
      * A utility class to prioritize projects for saving, depending on how long ago
      * they have been changed but have not been saved.
      */
     static protected class SaveRecord {
         final Project project;
         final long overdue;
 
         SaveRecord(Project project, long overdue) {
             this.project = project;
             this.overdue = overdue;
         }
     }
 
     static protected final int s_projectFlushDelay = 1000 * 60 * 60; // 1 hour
     static protected final int s_quickSaveTimeout = 1000 * 30; // 30 secs
 
     /**
      * Saves all projects to the data store
      * @param allModified
      */
     protected void saveProjects(boolean allModified) {
         List<SaveRecord> records = new ArrayList<SaveRecord>();
         Date startTimeOfSave = new Date();
 
         synchronized (this) {
             for (long id : _projectsMetadata.keySet()) {
                 ProjectMetadata metadata = getProjectMetadata(id);
                 Project project = _projects.get(id); // don't call getProject() as that will load the project.
 
                 if (project != null) {
                     boolean hasUnsavedChanges =
                         metadata.getModified().getTime() > project.getLastSave().getTime();
 
                     if (hasUnsavedChanges) {
                         long msecsOverdue = startTimeOfSave.getTime() - project.getLastSave().getTime();
 
                         records.add(new SaveRecord(project, msecsOverdue));
 
                     } else if (startTimeOfSave.getTime() - project.getLastSave().getTime() > s_projectFlushDelay) {
                         /*
                          *  It's been a while since the project was last saved and it hasn't been
                          *  modified. We can safely remove it from the cache to save some memory.
                          */
                         _projects.remove(id);
                     }
                 }
             }
         }
 
         if (records.size() > 0) {
             Collections.sort(records, new Comparator<SaveRecord>() {
                 public int compare(SaveRecord o1, SaveRecord o2) {
                     if (o1.overdue < o2.overdue) {
                         return 1;
                     } else if (o1.overdue > o2.overdue) {
                         return -1;
                     } else {
                         return 0;
                     }
                 }
             });
 
             logger.info(allModified ?
                 "Saving all modified projects ..." :
                 "Saving some modified projects ..."
             );
 
             for (int i = 0;
                  i < records.size() &&
                     (allModified || (new Date().getTime() - startTimeOfSave.getTime() < s_quickSaveTimeout));
                  i++) {
 
                 try {
                     saveProject(records.get(i).project);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     //--------------Get from memory--------------
     /**
      * Gets the InterProjectModel from memory
      */
     public InterProjectModel getInterProjectModel() {
         return _interProjectModel;
     }
 
 
     /**
      * Gets the project metadata from memory
      * Requires that the metadata has already been loaded from the data store
      * @param id
      * @return
      */
     public ProjectMetadata getProjectMetadata(long id) {
         return _projectsMetadata.get(id);
     }
 
     /**
      * Gets the project metadata from memory
      * Requires that the metadata has already been loaded from the data store
      * @param name
      * @return
      */
     public ProjectMetadata getProjectMetadata(String name) {
         for (ProjectMetadata pm : _projectsMetadata.values()) {
             if (pm.getName().equals(name)) {
                 return pm;
             }
         }
         return null;
     }
 
     /**
      * Tries to find the project id when given a project name
      * Requires that all project metadata exists has been loaded to memory from the data store
      * @param name
      *     The name of the project
      * @return
      *     The id of the project, or -1 if it cannot be found
      */
     public long getProjectID(String name) {
         for (Entry<Long, ProjectMetadata> entry : _projectsMetadata.entrySet()) {
             if (entry.getValue().getName().equals(name)) {
                 return entry.getKey();
             }
         }
         return -1;
     }
 
 
     /**
      * Gets all the project Metadata currently held in memory
      * @return
      */
     public Map<Long, ProjectMetadata> getAllProjectMetadata() {
         return _projectsMetadata;
     }
 
     /**
      * Gets the required project from the data store
      * If project does not already exist in memory, it is loaded from the data store
      * @param id
      *     the id of the project
      * @return
      *     the project with the matching id, or null if it can't be found
      */
     public Project getProject(long id) {
         synchronized (this) {
             if (_projects.containsKey(id)) {
                 return _projects.get(id);
             } else {
                 Project project = loadProject(id);
 
                 _projects.put(id, project);
 
                 return project;
             }
         }
     }
 
     /**
      * Gets the preference store
      * @return
      */
     public PreferenceStore getPreferenceStore() {
         return _preferenceStore;
     }
 
     /**
      * Gets all expressions from the preference store
      * @return
      */
     public List<String> getExpressions() {
         return ((TopList) _preferenceStore.get("expressions")).getList();
     }
 
     /**
      * The history entry manager deals with changes
      * @return manager for handling history
      */
     public abstract HistoryEntryManager getHistoryEntryManager();
 
     //-------------remove project-----------
 
     /**
      * Remove the project from the data store
      * @param project
      */
     public void deleteProject(Project project) {
         deleteProject(project.id);
     }
 
     /**
      * Remove project from data store
      * @param projectID
      */
     public abstract void deleteProject(long projectID);
 
     /**
      * Removes project from memory
      * @param projectID
      */
     protected void removeProject(long projectID){
         if (_projectsMetadata.containsKey(projectID)) {
             _projectsMetadata.remove(projectID);
         }
         if (_projects.containsKey(projectID)) {
             _projects.remove(projectID);
         }
     }
 
     //--------------Miscellaneous-----------
 
     /**
      * Sets the flag for long running operations
      * @param busy
      */
     public void setBusy(boolean busy) {
         synchronized (this) {
             if (busy) {
                 _busy++;
             } else {
                 _busy--;
             }
         }
     }
 
 
 
     /**
      * Add the latest expression to the preference store
      * @param s
      */
     public void addLatestExpression(String s) {
         synchronized (this) {
             ((TopList) _preferenceStore.get("expressions")).add(s);
         }
     }
 
 
     /**
     *
     * @param ps
     */
    static protected void preparePreferenceStore(PreferenceStore ps) {
       ps.put("scripting.expressions", new TopList(s_expressionHistoryMax));
    }
 }

 package com.rallydev.intellij.wsapi.cache;
 
 import com.intellij.openapi.components.PersistentStateComponent;
 import com.intellij.openapi.components.State;
 import com.intellij.openapi.components.Storage;
 import com.intellij.util.xmlb.XmlSerializerUtil;
 import com.rallydev.intellij.wsapi.domain.Project;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 @State(
         name = "Rally Project Cache",
         storages = @Storage(file = "$APP_CONFIG$/rally_project_cache.xml")
 )
 public class ProjectCache implements PersistentStateComponent<ProjectCache> {
 
    public List<Project> projects = new LinkedList<Project>();
     public Map<String, List<Project>> projectsByWorkspace = new HashMap<String, List<Project>>();
 
     @Nullable
     @Override
     public ProjectCache getState() {
         return this;
     }
 
     @Override
     public void loadState(ProjectCache state) {
         projectsByWorkspace = new HashMap<String, List<Project>>();
         XmlSerializerUtil.copyBean(state, this);
     }
 
 }

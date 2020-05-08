 package com.atlassian.sal.bamboo.project;
 
 import org.apache.log4j.Logger;
 import com.atlassian.sal.api.project.ProjectManager;
 import com.atlassian.bamboo.build.BuildManager;
 import com.atlassian.bamboo.build.Build;
 
 import java.util.Collection;
 import java.util.ArrayList;
 
 /**
  * The BambooProjectManager manages getting information about SAL projects. In Bamboo's case a "SAL Project" will refer
  * to a Build Plan.
  */
 public class BambooProjectManager implements ProjectManager
 {
     // ------------------------------------------------------------------------------------------------------- Constants
     // ------------------------------------------------------------------------------------------------- Type Properties
     // ---------------------------------------------------------------------------------------------------- Dependencies
     private final BuildManager buildManager;
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public BambooProjectManager(BuildManager buildManager)
     {
         this.buildManager = buildManager;
     }
 
     // ----------------------------------------------------------------------------------------------- Interface Methods
     // -------------------------------------------------------------------------------------------------- Action Methods
     // -------------------------------------------------------------------------------------------------- Public Methods
     public Collection<String> getAllProjectKeys()
     {
         Collection<String> buildKeys = new ArrayList<String>();
        Collection<Build> builds = buildManager.getAllBuilds(); // will ignore permissions.
        if (builds != null)
         {
            for (Build build : builds)
            {
                buildKeys.add(build.getKey());
            }
         }
         return buildKeys;
     }
     // ------------------------------------------------------------------------------------------------- Helper Methods
     // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
 }

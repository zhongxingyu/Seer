 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import java.io.File;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.gradle.jarjar.com.google.common.collect.Maps;
 import org.jboss.forge.addon.facets.AbstractFacet;
 import org.jboss.forge.addon.gradle.parser.GradleSourceUtil;
 import org.jboss.forge.addon.gradle.projects.model.GradleModel;
 import org.jboss.forge.addon.gradle.projects.model.GradleModelLoadUtil;
 import org.jboss.forge.addon.gradle.projects.model.GradleModelMergeUtil;
 import org.jboss.forge.addon.gradle.projects.model.GradleProfile;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.resource.FileResource;
 import org.jboss.forge.addon.resource.Resource;
 import org.jboss.forge.addon.resource.ResourceFactory;
 import org.jboss.forge.addon.resource.ResourceFilter;
 import org.jboss.forge.furnace.util.Streams;
 
 /**
  * @author Adam Wyłuda
  */
 public class GradleFacetImpl extends AbstractFacet<Project> implements GradleFacet
 {
    public static final String INITIAL_BUILD_FILE_CONTENTS = "" +
             "apply plugin: 'java'\n" +
             "repositories {\n" +
             "    mavenCentral()\n" +
             "}\n";
    
    @Inject
    private GradleManager manager;
    @Inject
    private ResourceFactory resourceFactory;
 
    // Cached model
    private GradleModel model;
    private Map<String, GradleModel> profileModels;
 
    @Override
    public boolean install()
    {
       if (!this.isInstalled())
       {
          if (!getBuildScriptResource().exists())
          {
             getBuildScriptResource().createNewFile();
             getBuildScriptResource().setContents(INITIAL_BUILD_FILE_CONTENTS);
          }
          if (!getSettingsScriptResource().exists())
          {
             getSettingsScriptResource().createNewFile();
          }
       }
       return isInstalled();
    }
 
    @Override
    public boolean isInstalled()
    {
       return getBuildScriptResource().exists();
    }
 
    @Override
    public boolean executeTask(String task)
    {
       return executeTask(task, "");
    }
 
    @Override
    public boolean executeTask(String task, String profile, String... arguments)
    {
       return manager.runGradleBuild(getFaceted().getRootDirectory().getFullyQualifiedName(), task, profile, arguments);
    }
 
    @Override
    public GradleModel getModel()
    {
       if (this.model != null)
       {
          return this.model;
       }
       loadModel();
       return this.model;
    }
 
    @Override
    public void setModel(GradleModel newModel)
    {
       String oldSource = getBuildScriptResource().getContents();
       String newSource = GradleModelMergeUtil.merge(oldSource, model, newModel);
       getBuildScriptResource().setContents(newSource);
 
       // If we need to change model name then it must be done in settings.gradle
       if (!this.model.getName().equals(newModel.getName()))
       {
          String settingsScript = getSettingsScriptResource().getContents();
          // Because setting project name in model also changes the project path
          // we must take project path from old model
          settingsScript = GradleSourceUtil.setProjectName(settingsScript, this.model.getProjectPath(),
                   newModel.getName());
          getSettingsScriptResource().setContents(settingsScript);
       }
 
       // Update profiles
       for (GradleProfile profile : newModel.getProfiles())
       {
          FileResource<?> profileScriptResource = getProfileScriptResource(profile.getName());
          
          // If profile doesn't exist we must create a file for it
          if (!profileScriptResource.exists())
          {
             profileScriptResource.createNewFile();
          }
 
          // Merge new profile contents
          String oldProfileSource = profileScriptResource.getContents();
          String newProfileSource = GradleModelMergeUtil.merge(oldProfileSource, 
                   profileModels.get(profile.getName()), profile.getModel());
          if (!newProfileSource.equals(oldProfileSource))
          {
             profileScriptResource.setContents(newProfileSource);
          }
       }
 
       // Remove profile scripts if they are not apparent on the list
       for (Resource<?> resource : getFaceted().getRootDirectory().listResources(new ResourceFilter()
       {
          @Override
          public boolean accept(Resource<?> resource)
          {
             return resource.getName().endsWith(GradleSourceUtil.PROFILE_SUFFIX);
          }
       }))
       {
          boolean hasProfile = false;
          String profileName = resource.getName().substring(0, resource.getName().lastIndexOf("-"));
          for (GradleProfile profile : newModel.getProfiles())
          {
             if (profile.getName().equals(profileName))
             {
                hasProfile = true;
                break;
             }
          }
          if (!hasProfile)
          {
             resource.delete();
          }
       }
 
       this.model = null;
    }
 
    @Override
    public FileResource<?> getBuildScriptResource()
    {
       return (FileResource<?>) getFaceted().getRootDirectory().getChild("build.gradle");
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public FileResource<?> getSettingsScriptResource()
    {
       return (FileResource<?>) resourceFactory.create(FileResource.class, new File(
                getModel().getRootProjectPath(), "settings.gradle"));
    }
 
    private void loadModel()
    {
       checkIfIsForgeLibraryInstalled();
       
       String script = getBuildScriptResource().getContents();
       Map<String, String> profileScripts = getProfileScripts();
 
       manager.runGradleBuild(getFaceted().getRootDirectory().getFullyQualifiedName(),
                GradleSourceUtil.FORGE_OUTPUT_TASK, "");
 
       FileResource<?> forgeOutputfile = (FileResource<?>) getFaceted().getRootDirectory().getChild(
                GradleSourceUtil.FORGE_OUTPUT_XML);
      String forgeOutput = Streams.toString(forgeOutputfile.getResourceInputStream());
 
       forgeOutputfile.delete();
 
       GradleModel loadedModel = GradleModelLoadUtil.load(script, profileScripts, forgeOutput);
 
       // Set resources for profiles
       profileModels = Maps.newHashMap();
       for (GradleProfile profile : loadedModel.getProfiles())
       {
          profileModels.put(profile.getName(), profile.getModel());
       }
 
       this.model = loadedModel;
    }
 
    private void checkIfIsForgeLibraryInstalled()
    {
       String script = getBuildScriptResource().getContents();
       String newScript = GradleSourceUtil.checkForIncludeForgeLibraryAndInsert(script);
 
       // If Forge library is not included
       if (!script.equals(newScript))
       {
          getBuildScriptResource().setContents(newScript);
 
          FileResource<?> forgeLib = (FileResource<?>)
                   getFaceted().getRootDirectory().getChild(GradleSourceUtil.FORGE_LIBRARY);
          forgeLib.setContents(getClass().getResourceAsStream(GradleSourceUtil.FORGE_LIBRARY_RESOURCE));
       }
    }
    
    private FileResource<?> getProfileScriptResource(String name)
    {
       return (FileResource<?>) getBuildScriptResource().getParent()
                .getChild(name + GradleSourceUtil.PROFILE_SUFFIX);
    }
    
    private Map<String, String> getProfileScripts()
    {
       Map<String, String> profileScripts = Maps.newHashMap();
       
       for (Resource<?> resource : getBuildScriptResource().getParent().listResources())
       {
          FileResource<?> file = (FileResource<?>) resource;
          if (file.getName().endsWith(GradleSourceUtil.PROFILE_SUFFIX))
          {
             String profile = file.getName().substring(0, file.getName().length() - GradleSourceUtil.PROFILE_SUFFIX.length());
             profileScripts.put(profile, file.getContents());
          }
       }
       
       return profileScripts;
    }
 }

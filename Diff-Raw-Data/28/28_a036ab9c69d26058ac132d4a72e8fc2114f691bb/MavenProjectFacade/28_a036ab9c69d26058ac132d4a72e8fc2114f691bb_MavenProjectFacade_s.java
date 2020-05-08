 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.tpmp.resolver.tycho;
 
 import static com.google.common.base.Optional.fromNullable;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.project.MavenProject;
 import org.eclipse.tycho.ArtifactDescriptor;
 import org.eclipse.tycho.ArtifactKey;
 import org.eclipse.tycho.ReactorProject;
 import org.eclipse.tycho.core.TargetPlatformConfiguration;
 import org.eclipse.tycho.core.TychoConstants;
 import org.eclipse.tycho.core.TychoProject;
 import org.eclipse.tycho.core.osgitools.DefaultReactorProject;
 import org.eclipse.tycho.core.utils.TychoProjectUtils;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolutionResult.Entry;
 import org.eclipse.tycho.resolver.TychoDependencyResolver;
 
 import com.google.common.base.Optional;
 
 @Named
 public class MavenProjectFacade
 {
    @Inject
    private Map<String, TychoProject> projectTypes;
   
    @Inject
    private TychoDependencyResolver resolver;
 
    public TychoProject getTychoProject(MavenProject project)
    {
       return projectTypes.get(project.getPackaging());
    }
 
    public Map<String, MavenProject> createVidToProjectMap(MavenSession session)
    {
       final Map<String, MavenProject> vidToProjectMap = new HashMap<String, MavenProject>();
       for (MavenProject mavenProject : session.getProjects())
       {
          final TychoProject tychoProject = getTychoProject(mavenProject);
          if (tychoProject != null)
          {
             final ReactorProject reactorProject = DefaultReactorProject.adapt(mavenProject);
             final ArtifactKey artifactKey = tychoProject.getArtifactKey(reactorProject);
             final String vid = artifactKey.getId() + "_" + artifactKey.getVersion();
             vidToProjectMap.put(vid, mavenProject);
          }
       }
       return vidToProjectMap;
    }
   
    public Optional<MavenProject> getMavenProject(final Map<String, MavenProject> projectsMap,
       ArtifactDescriptor artifact)
    {
       final ArtifactKey artifactKey = artifact.getKey();
      final String vid = artifactKey.getId() + "_" + artifactKey.getVersion();
      return fromNullable(projectsMap.get(vid));
    }
 
   public Optional<MavenProject> getMavenProject(Map<String, MavenProject> projectsMap, Entry entry)
    {
      final String vid = entry.getId() + "_" + entry.getVersion();
      return fromNullable(projectsMap.get(vid));      
    }
 
    public ArtifactKey getArtifactKey(ArtifactDescriptor artifact, Optional<MavenProject> mavenProject)
    {
       if (mavenProject.isPresent())
       {
          final MavenProject mp = mavenProject.get();
          return getTychoProject(mp).getArtifactKey(DefaultReactorProject.adapt(mp));
       }
       else
       {
          return artifact.getKey();
       }
    }
 
    public File getLocation(ArtifactDescriptor artifact, Optional<MavenProject> mavenProject)
    {
       if (mavenProject.isPresent())
       {
          return mavenProject.get().getBasedir();
       }
       else
       {
          return artifact.getLocation();
       }
    }
 
    public File getLocation(Entry entry, Optional<MavenProject> mavenProject)
    {
       if (mavenProject.isPresent())
       {
          return mavenProject.get().getBasedir();
       }
       else
       {
          return entry.getLocation();
       }
    }
   
    public TargetPlatformConfiguration getTargetPlatformConfiguration(MavenSession session, MavenProject project)
    {
       setupSessionLazy(session);
       return TychoProjectUtils.getTargetPlatformConfiguration(project);
    }
 
    private void setupSessionLazy(MavenSession session)
    {
       List<MavenProject> projects = session.getProjects();
       for (MavenProject project : projects)
       {
          setupProjectLazy(session, project);
       }
    }
 
    private void setupProjectLazy(MavenSession session, MavenProject project)
    {
       final TargetPlatformConfiguration targetPlatformConfiguration = (TargetPlatformConfiguration) project
          .getContextValue(TychoConstants.CTX_TARGET_PLATFORM_CONFIGURATION);
       if (targetPlatformConfiguration == null)
       {
          // project was not set up by Tycho. Maybe running in -Dtycho.mode=maven
          resolver.setupProject(session, project, DefaultReactorProject.adapt(project));
       }
    }
 }

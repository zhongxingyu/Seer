 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.maven;
 
 import java.io.File;
 
 import javax.validation.constraints.NotNull;
 
 import org.apache.maven.project.MavenProject;
 
 public final class MavenUtils
 {
    private MavenUtils()
    {
       super();
    }
 
    public static File getOutputDir(@NotNull MavenProject project)
    {
       final String path = project.getBuild().getOutputDirectory(); // getBuild never returns null
       return getDirInProject(project, path);
    }
 
    public static File getTestOutputDir(@NotNull MavenProject project)
    {
       final String path = project.getBuild().getTestOutputDirectory(); // getBuild never returns null
       return getDirInProject(project, path);
    }
 
    private static File getDirInProject(MavenProject project, String path)
    {
       if (path != null)
       {
          final File file = new File(path);
          if (!file.isAbsolute())
          {
            return new File(getProjectFile(project), path);
          }
          return file;
       }
       return null;
    }
 
    @NotNull
   private static File getProjectFile(MavenProject project)
    {
      return project.getFile();
    }
 }

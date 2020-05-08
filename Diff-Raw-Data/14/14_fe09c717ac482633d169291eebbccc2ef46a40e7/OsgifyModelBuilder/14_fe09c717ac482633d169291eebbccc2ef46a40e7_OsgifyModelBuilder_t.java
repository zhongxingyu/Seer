 /**
  * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.maven;
 
 import static org.sourcepit.common.utils.lang.Exceptions.pipe;
 
 import java.util.Collection;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.project.DependencyResolutionException;
 import org.apache.maven.project.ProjectBuildingException;
import org.sourcepit.maven.dependency.model.ArtifactAttachmentFactory;
 import org.sourcepit.maven.dependency.model.DependencyModel;
 import org.sourcepit.maven.dependency.model.DependencyModelResolver;
import org.sourcepit.maven.dependency.model.JavaSourceAttachmentFactory;
 import org.sourcepit.osgify.core.model.context.OsgifyContext;
 import org.sourcepit.osgify.maven.impl.OsgifyStubModelCreator;
 
 @Named
 public class OsgifyModelBuilder
 {
    @Inject
    private DependencyModelResolver dependencyModelResolver;

    @Inject
    private OsgifyStubModelCreator stubModelCreator;
 
    private OsgifyContext build(Collection<Dependency> dependencies)
    {
       final DependencyModel dependencyModel;
 
      final boolean includeSource = true;

      final ArtifactAttachmentFactory attachmentFactory = includeSource ? new JavaSourceAttachmentFactory() : null;

       try
       {
         dependencyModel = dependencyModelResolver.resolve(dependencies, attachmentFactory);
       }
       catch (ProjectBuildingException e)
       {
          throw pipe(e);
       }
       catch (DependencyResolutionException e)
       {
          throw pipe(e);
       }
 
       final OsgifyContext osgifyModel = stubModelCreator.create(dependencyModel);
       // resolve sources
       // scan contents
       // derive manifests
 
       return null;
    }
 
 
    public static class Request
    {
       private boolean includeSource;
    }
 }

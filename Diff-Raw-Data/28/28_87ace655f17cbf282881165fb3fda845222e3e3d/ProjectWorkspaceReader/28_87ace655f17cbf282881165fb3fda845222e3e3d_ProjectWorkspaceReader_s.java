 package org.sonatype.aether.ant;
 
 /*******************************************************************************
  * Copyright (c) 2010-2011 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution.
  * The Eclipse Public License is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  * The Apache License v2.0 is available at
  *   http://www.apache.org/licenses/LICENSE-2.0.html
  * You may elect to redistribute this code under either of these licenses.
  *******************************************************************************/
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.model.Model;
 import org.sonatype.aether.ant.types.Pom;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.repository.WorkspaceReader;
 import org.sonatype.aether.repository.WorkspaceRepository;
 
 /**
  * Workspace reader caching available POMs and artifacts for ant builds.
  * <p/>
  * Cached are &lt;pom> elements which are defined by the 'file'-attribute, as they reference a backing pom.xml file that
  * can be used for resolution with Aether. Also cached are &lt;artifact> elements that directly define a 'pom'-attribute
  * or child. The POM may be file-based or in-memory.
  */
 public class ProjectWorkspaceReader
     implements WorkspaceReader
 {
 
     private static ProjectWorkspaceReader instance;
 
     private static Object lock = new Object();
 
     private Map<String, WeakReference<File>> poms =
         Collections.synchronizedMap( new HashMap<String, WeakReference<File>>() );
 
     public void addPom( Pom pom )
     {
         if ( pom.getFile() != null )
         {
             String coords = coords( pom.getModel( pom ) );
             poms.put( coords, new WeakReference<File>( pom.getFile() ) );
         }
     }
 
     public void addArtifact( org.sonatype.aether.ant.types.Artifact artifact )
     {
         if ( artifact.getPom() != null )
         {
             String coords = coords( artifact.getPom(), artifact.getType() );
             poms.put( coords, new WeakReference<File>( artifact.getFile() ) );
         }
     }
 
     private String coords( org.sonatype.aether.ant.types.Pom pom, String extension )
     {
         if ( pom.getFile() != null )
         {
            return coords(pom.getModel( pom ));
         }
         else
         {
             return String.format( "%s:%s:%s:%s", pom.getArtifactId(), pom.getGroupId(), extension, pom.getVersion());
         }
     }
 
     private String coords( Model pom )
     {
         return String.format( "%s:%s:pom:%s", pom.getGroupId(), pom.getArtifactId(), pom.getVersion() );
     }
 
     private String coords( Artifact artifact )
     {
         return String.format( "%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                               artifact.getBaseVersion() );
     }
 
     public WorkspaceRepository getRepository()
     {
         return new WorkspaceRepository( "ant" );
     }
 
     public File findArtifact( Artifact artifact )
     {
        if ( !artifact.getExtension().equals( "pom" ) || !poms.containsKey( coords( artifact ) ) )
        {
            return null;
        }

         WeakReference<File> weakReference = poms.get( coords( artifact ) );
        File file = weakReference.get();
        if ( file != null )
         {
            return file;
         }
         return null;
     }
 
     public List<String> findVersions( Artifact artifact )
     {
         return Collections.emptyList();
     }
 
     ProjectWorkspaceReader()
     {
     }
 
     public static ProjectWorkspaceReader getInstance()
     {
         if ( instance != null )
         {
             return instance;
         }
 
         synchronized ( lock )
         {
             if ( instance == null )
             {
                 instance = new ProjectWorkspaceReader();
             }
             return instance;
         }
     }
 
     public static void dropInstance()
     {
         instance = null;
     }
 }

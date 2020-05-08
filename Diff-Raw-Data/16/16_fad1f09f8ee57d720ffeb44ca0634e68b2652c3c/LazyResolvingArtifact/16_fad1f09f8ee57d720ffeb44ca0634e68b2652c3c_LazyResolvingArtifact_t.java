 /*
  * Copyright (C) 2010 Okidokiteam
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.ops4j.pax.repository.resolver;
 
 import org.ops4j.pax.repository.Artifact;
 import org.ops4j.pax.repository.ArtifactIdentifier;
 import org.ops4j.pax.repository.InputStreamSource;
 import org.ops4j.pax.repository.RepositoryException;
 import org.ops4j.pax.repository.RepositoryResolver;
 
 /**
  *
  */
 public class LazyResolvingArtifact implements Artifact
 {
 
     final private ArtifactIdentifier m_artifactIdentifier;
     final private RepositoryResolver m_resolver;
 
    private volatile Artifact m_artifact;
    private volatile boolean m_failedBefore = false;
 
     public LazyResolvingArtifact( RepositoryResolver resolver, ArtifactIdentifier identifier )
         throws RepositoryException
     {
         m_artifactIdentifier = identifier;
         m_resolver = resolver;
     }
 
     private void lazyResolve()
         throws RepositoryException
     {
         if( m_failedBefore )
         {
             throw new RepositoryException( "Artifact resolve has been failed before. Thus this artifact will not go any better." );
         }
 
         if( m_artifact == null )
         {
             try
             {
                 m_artifact = m_resolver.find( m_artifactIdentifier );
             } catch( RepositoryException e )
             {
                 m_failedBefore = true;
                 throw e; // TODO rethrow safe ?
             }
         }
     }
 
     public synchronized InputStreamSource getContent()
         throws RepositoryException
     {
         lazyResolve();
         return m_artifact.getContent();
     }
 
     public String getGroupId()
     {
         return m_artifactIdentifier.getGroupId();
     }
 
     public String getArtifactId()
     {
        return m_artifactIdentifier.getArtifactId();
     }
 
     public String getVersion()
     {
        return m_artifactIdentifier.getVersion();
     }
 
     public String getClassifier()
     {
        return m_artifactIdentifier.getClassifier();
     }
 
     public String getName()
     {
        return m_artifactIdentifier.getName();
     }
 }

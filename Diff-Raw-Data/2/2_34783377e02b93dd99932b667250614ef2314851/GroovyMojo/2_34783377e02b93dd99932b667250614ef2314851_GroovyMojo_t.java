 /*
  *  Copyright 2006 The Apache Software Foundation
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
package org.apache.geronimo.genesis.plugins.script;
 
 import org.apache.geronimo.plugin.MojoSupport;
 import org.apache.geronimo.plugin.ArtifactItem;
 
 import java.io.File;
 import java.io.InputStream;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyObject;
 
 /**
  * Executes a <a href="http://groovy.codehaus.org">Groovy</a> script.
  *
  * @goal groovy
  * @requiresDependencyResolution
  *
  * @version $Rev$ $Date$
  */
 public class GroovyMojo
     extends MojoSupport
 {
     /**
      * The project to create a build for.
      *
      * @parameter expression="${project}"
      * @required
      */
     private MavenProject project = null;
     
     /**
      * The code code of the script to execute.
      *
      * @parameter
      */
     private String code = null;
     
     /**
      * The URL to use as the code of the script to execute.
      *
      * @parameter
      */
     private URL codeUrl = null;
 
     /**
      * Additional artifacts to add to the scripts classpath.
      *
      * @parameter
      */
     private ArtifactItem[] classpath;
 
     //
     // MojoSupport Hooks
     //
 
     protected MavenProject getProject() {
         return project;
     }
 
     /**
      * @component
      * @required
      * @readonly
      */
     private ArtifactFactory artifactFactory = null;
 
     protected ArtifactFactory getArtifactFactory() {
         return artifactFactory;
     }
 
     /**
      * @component
      * @required
      * @readonly
      */
     private ArtifactResolver artifactResolver = null;
 
     protected ArtifactResolver getArtifactResolver() {
         return artifactResolver;
     }
 
     /**
      * @parameter expression="${localRepository}"
      * @readonly
      * @required
      */
     protected ArtifactRepository artifactRepository = null;
 
     protected ArtifactRepository getArtifactRepository() {
         return artifactRepository;
     }
 
     //
     // Mojo
     //
     
     protected void doExecute() throws Exception {
         if (code == null && codeUrl == null) {
             throw new MojoExecutionException("Need to specify code or codeUrl");
         }
         else if (code != null && codeUrl != null) {
             throw new MojoExecutionException("Can only specify code or codeUrl, not both");
         }
         
         ClassLoader parent = getClass().getClassLoader();
         List urls = new ArrayList();
 
         // Add the plugins dependencies
         List classpathFiles = project.getCompileClasspathElements();
         for (int i = 0; i < classpathFiles.size(); ++i) {
             urls.add(new File((String)classpathFiles.get(i)).toURL());
         }
 
         // Add custom dependencies
         if (classpath != null) {
             for (int i=0; i < classpath.length; i++) {
                 Artifact artifact = getArtifact(classpath[i]);
                 urls.add(artifact.getFile().toURL());
             }
         }
 
         URL[] _urls = (URL[])urls.toArray(new URL[urls.size()]);
         if (log.isDebugEnabled()) {
             for (int i=0; i < _urls.length; i++) {
                 log.debug("URL[" + i + "]: " + _urls[i]);
             }
         }
 
         URLClassLoader cl = new URLClassLoader(_urls, parent);
         GroovyClassLoader loader = new GroovyClassLoader(cl);
         
         Class groovyClass;
         
         if (code != null) {
             groovyClass = loader.parseClass(code);
         }
         else {
             log.debug("Loading source from: " + codeUrl);
             
             InputStream input = codeUrl.openConnection().getInputStream();
             groovyClass = loader.parseClass(input);
             input.close();
         }
         
         GroovyObject groovyObject = (GroovyObject)groovyClass.newInstance();
         
         // Set some properties
         groovyObject.setProperty("project", project);
         
         groovyObject.invokeMethod("run", new Object[0]);
     }
 }

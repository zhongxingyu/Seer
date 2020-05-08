 /*
  * Copyright 2011 Xebia and the original author or authors.
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
 package fr.xebia.maven.plugin.mindmap;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Properties;
 
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactCollector;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.shared.dependency.tree.DependencyNode;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.tools.generic.SortTool;
 
 /**
  * Generate a mindmap from the pom dependencies.
  *
  * @goal mindmap
  * 
  * @author Issam EL FATMI
  *
  */
 public class MindmapMojo extends AbstractMojo {
     /** @component */
     private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;
     /**
      * @component
      * @required
      * @readonly
      */
     private ArtifactMetadataSource artifactMetadataSource;
     /**
      * @component
      * @required
      * @readonly
      */
     private ArtifactCollector artifactCollector;
     /**
      * @component
      * @required
      * @readonly
      */
     private DependencyTreeBuilder treeBuilder;
     /** @parameter default-value="${localRepository}" */
     private ArtifactRepository localRepository;
     /**
      * @parameter default-value="${project}"
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${groupIdsFilteringREGEXMatch}"
      */
     private String groupIdsFilteringREGEXMatch = null;
 
    /**
     * {@inheritDoc}
     */
     public void execute() throws MojoExecutionException {
         try {
             ArtifactFilter artifactFilter = new ScopeArtifactFilter(null);
             // Build project dependency tree
             DependencyNode rootNode = treeBuilder.buildDependencyTree(project,
                            localRepository, artifactFactory, artifactMetadataSource,
                            artifactFilter, artifactCollector);
 
             generateMindMapXML(project, rootNode);
 
         } catch (DependencyTreeBuilderException e) {
             throw new MojoExecutionException("Unable to build project dependency tree.", e);
         }
 
     }
 
     private void generateMindMapXML(MavenProject mavenProject, DependencyNode rootNode) throws MojoExecutionException {
         FileWriter fw = null;
 
         try {
             Properties p = new Properties();
             p.setProperty("resource.loader", "class");
             p.setProperty("class.resource.loader.class",
                            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 
             //  first, get and initialize an engine
             VelocityEngine ve = new VelocityEngine();
             ve.init(p);
 
             //  next, get the Template
             Template freePlaneTemplate = ve.getTemplate("MindMapTemplate.vm");
 
             // create a context and add data
             VelocityContext context = new VelocityContext();
 
             context.put("artifactId", mavenProject.getArtifactId());
             context.put("sorter", new SortTool());
             context.put("rootNode", rootNode);
            context.put("date", new SimpleDateFormat("dd/MM/yy HH:mm").format(Calendar.getInstance().getTime()));
 
            context.put("groupIdsFilteringREGEXMatch", groupIdsFilteringREGEXMatch != null?groupIdsFilteringREGEXMatch:"");
 
             context.put("creationTS", Calendar.getInstance().getTimeInMillis());
 
             // now render the template
             fw = new FileWriter("./"+mavenProject.getGroupId()+"_"+mavenProject.getArtifactId()+"_"+mavenProject.getVersion()+".mm");
 
             // write the mindmap xml to disc
             freePlaneTemplate.merge(context, fw);
         } catch (Exception e) {
             throw new MojoExecutionException("Unable to generate mind map.", e);
         } finally {
             if (fw != null) {
                 try {
                     fw.close();
                 } catch (IOException e) {
                     getLog().warn("Unable to properly close stream.", e);
                 }
             }
         }
     }
 }

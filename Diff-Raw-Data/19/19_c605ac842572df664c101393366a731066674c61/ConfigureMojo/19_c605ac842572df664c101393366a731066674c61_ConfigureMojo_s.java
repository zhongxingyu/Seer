 /*
  * Copyright 2012 htfv (Aliaksei Lahachou)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.htfv.maven.plugins.buildconfigurator.plugin;
 
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 import com.github.htfv.maven.plugins.buildconfigurator.core.BuildConfigurator;
 import com.github.htfv.maven.plugins.buildconfigurator.core.ConfigureRequest;
 import com.github.htfv.maven.plugins.buildconfigurator.core.ConfigureResult;
 import com.github.htfv.maven.plugins.buildconfigurator.core.ModelInterpolator;
 import com.github.htfv.maven.plugins.buildconfigurator.model.BCDependency;
 import com.github.htfv.maven.plugins.buildconfigurator.model.BCPluginModel;
 import com.github.htfv.maven.plugins.buildconfigurator.model.BCPropertyFile;
 
 /**
  * Configures the project according to the specified configuration:
  * <ul>
  * <li>Loads properties from property files.</li>
  * <li>Adds or updates dependencies on some condition.</li>
  * <li>Adds dependency exclusions on some condition.</li>
  * <li>Unified EL is supported in property files and configuration elements.</li>
  * </ul>
  *
  * @author htfv (Aliaksei Lahachou)
  *
  * @goal configure
 * @phase validate
  */
 public class ConfigureMojo extends AbstractMojo implements BCPluginModel
 {
     /**
      * Key of the property indicating whether the mojo is called from the m2e
      * connector.
      */
     private static final String CONTEXT_CONNECTOR_REQUEST_KEY =
             "com.github.htfv.buildConfigurator.connectorRequest";
 
     /**
      * Key of the property containing properties loaded by the
      * {@link ConfigureMojo}.
      */
     private static final String CONTEXT_NEW_PROPERTIES_KEY =
             "com.github.htfv.buildConfigurator.newProperties";
 
     /**
      * Reference to the {@link BuildConfigurator} used to configure the project.
      *
      * @component
      */
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(
             justification = "Set by the Plexus container.",
             value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" })
     private BuildConfigurator buildConfigurator;
 
     /**
      * Default character encoding used to read property files. Character
      * encoding may be overridden for each loaded property file.
      */
     private String defaultPropertyFilesEncoding;
 
     /**
      * Array of dependencies which shall be merged into the project.
      *
      * @parameter
      */
     private BCDependency[] dependencies;
 
     /**
      * Flag indicating whether the plugin should be quiet when a property file
      * is not found.
      */
     private String ignoreMissingPropertyFiles;
 
     /**
      * Reference to the Maven project which shall be configured.
      *
      * @parameter default-value="${project}"
      * @readonly
      * @required
      */
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(
             justification = "Set by the Plexus container.",
             value = "UWF_UNWRITTEN_FIELD")
     private MavenProject project;
 
     /**
      * Array of property files whose properties shall be merged into the
      * project.
      *
      * @parameter
      */
     private BCPropertyFile[] propertyFiles;
 
     /**
      * Flag indicating whether build configuration should be bypassed.
      *
      * @parameter
      */
     private String skip;
 
     /**
      * Called by the m2e connector to reinterpolate plugin configurations.
      *
      * @param configurations
      *            collection of {@link Xpp3Dom} object to reinterpolate.
      * @param newProperties
      *            map supplying properties for interpolation.
      */
     public void connectorReinterpolateConfigurations(final Collection<Xpp3Dom> configurations,
             final Map<String, String> newProperties)
     {
        ModelInterpolator interpolator = new ModelInterpolator(newProperties);
 
        for (Xpp3Dom configuration : configurations)
         {
             interpolator.interpolateDOM(configuration);
         }
     }
 
     /**
      * Executes the logic of the {@code configure} goal.
      *
      * @throws MojoExecutionException
      *             if an unexpected problem occurs.
      * @throws MojoFailureException
      *             if an expected problem occurs.
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         try
         {
             //
             // Configure the project.
             //
 
            ConfigureRequest.Builder requestBuilder = new ConfigureRequest.Builder()
                     .pluginModel(this)
                     .project(project);
 
             @SuppressWarnings("unchecked")
             Map<String, Object> context = getPluginContext();
 
             if (context != null && Boolean.TRUE.equals(context.get(CONTEXT_CONNECTOR_REQUEST_KEY)))
             {
                 requestBuilder.connectorRequest();
             }
 
            ConfigureResult result = buildConfigurator.configure(requestBuilder.build());
 
             //
             // Put result into plugin context so that connector could see it.
             //
 
             if (context != null)
             {
                 context.put(CONTEXT_NEW_PROPERTIES_KEY, result.getNewProperties());
             }
         }
        catch (Exception e)
         {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     @Override
     public String getDefaultPropertyFilesEncoding()
     {
         return defaultPropertyFilesEncoding;
     }
 
     @Override
     public BCDependency[] getDependencies()
     {
         return ArrayUtils.clone(dependencies);
     }
 
     @Override
     public String getIgnoreMissingPropertyFiles()
     {
         return ignoreMissingPropertyFiles;
     }
 
     @Override
     public BCPropertyFile[] getPropertyFiles()
     {
         return ArrayUtils.clone(propertyFiles);
     }
 
     @Override
     public String getSkip()
     {
         return skip;
     }
 
     @Override
     public void setDefaultPropertyFilesEncoding(final String defaultPropertyFilesEncoding)
     {
         this.defaultPropertyFilesEncoding = defaultPropertyFilesEncoding;
     }
 
     @Override
     public void setDependencies(final BCDependency[] dependencies)
     {
         this.dependencies = ArrayUtils.clone(dependencies);
     }
 
     @Override
     public void setIgnoreMissingPropertyFiles(final String ignoreMissingPropertyFiles)
     {
         this.ignoreMissingPropertyFiles = ignoreMissingPropertyFiles;
     }
 
     @Override
     public void setPropertyFiles(final BCPropertyFile[] propertyFiles)
     {
         this.propertyFiles = ArrayUtils.clone(propertyFiles);
     }
 
     @Override
     public void setSkip(final String skip)
     {
         this.skip = skip;
     }
 }

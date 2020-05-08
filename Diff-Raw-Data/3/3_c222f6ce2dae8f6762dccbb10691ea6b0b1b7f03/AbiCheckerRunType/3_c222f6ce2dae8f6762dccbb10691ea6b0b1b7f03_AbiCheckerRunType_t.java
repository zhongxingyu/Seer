 /**
  * Copyright (c) 2012-2013 by European Organization for Nuclear Research (CERN)
  * Author: Justin Salmon <jsalmon@cern.ch>
  *
  * XRootD is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * XRootD is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with XRootD.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.cern.dss.teamcity.server;
 
 import ch.cern.dss.teamcity.common.AbiCheckerConstants;
 import jetbrains.buildServer.serverSide.ProjectManager;
 import jetbrains.buildServer.serverSide.PropertiesProcessor;
 import jetbrains.buildServer.serverSide.RunType;
 import jetbrains.buildServer.serverSide.RunTypeRegistry;
 import jetbrains.buildServer.web.openapi.PluginDescriptor;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * To build a custom TeamCity build runner, it is necessary to extend jetbrains.buildServer.serverSide.RunType and
  * register the new run type in the RunTypeRegistry.
  */
 public class AbiCheckerRunType extends RunType {
 
     private PluginDescriptor pluginDescriptor;
     private ProjectManager projectManager;
 
     /**
      * Constructor. Uses spring autowiring feature to request objects from some spring bean in the TeamCity API and
      * inject them into this constructor.
      *
      * @param runTypeRegistry  used to register this run type in the registry.
      * @param pluginDescriptor used to get the plugin resources path, i.e. path to JSP pages.
      * @param projectManager   used to find the reference build type.
      */
     public AbiCheckerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                              @NotNull final PluginDescriptor pluginDescriptor,
                              @NotNull final ProjectManager projectManager) {
         this.pluginDescriptor = pluginDescriptor;
         this.projectManager = projectManager;
         runTypeRegistry.registerRunType(this);
     }
 
     /**
      * @return the unique identifier string of this run type. Must be equivalent to the type reported by the agent-side
      *         part of the plugin.
      */
     @Override
     public String getType() {
         return AbiCheckerConstants.TYPE;
     }
 
     /**
      * @return the user-readable name of this plugin.
      */
     @Override
     public String getDisplayName() {
         return AbiCheckerConstants.DISPLAY_NAME;
     }
 
     /**
      * @return the user-readable description of this plugin.
      */
     @Override
     public String getDescription() {
         return AbiCheckerConstants.DESCRIPTION;
     }
 
     /**
      * @return a PropertiesProcessor object, used to validate the parameters given in the web UI.
      */
     @Override
     public PropertiesProcessor getRunnerPropertiesProcessor() {
         return new AbiCheckerPropertiesProcessor();
     }
 
     /**
      * @return the absolute path to the JSP file used to edit the runner parameters.
      */
     @Override
     public String getEditRunnerParamsJspFilePath() {
         return this.pluginDescriptor.getPluginResourcesPath() + "editAbiCheckerRunner.jsp";
     }
 
     /**
      * @return the absolute path to the JSP file used to view the runner parameters.
      */
     @Override
     public String getViewRunnerParamsJspFilePath() {
         return this.pluginDescriptor.getPluginResourcesPath() + "viewAbiCheckerRunner.jsp";
     }
 
     /**
      * @return the map of default parameters to the web UI form (not needed here).
      */
     @Override
     public Map<String, String> getDefaultRunnerProperties() {
         return new HashMap<String, String>();
     }
 
     /**
      * Return a string that describes the important settings for this build runner. This string will be displayed when
      * viewing the set of build steps for a particular build type.
      *
      * @param parameters the map of parameters that the user specified in the web UI.
      *
      * @return a short, readable string describing this runner's parameters.
      */
     @NotNull
     @Override
     public String describeParameters(@NotNull Map<String, String> parameters) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("Reference build type: ").append(parameters.get(AbiCheckerConstants.BUILD_TYPE_NAME)).append("\n");
         sb.append("Reference tag: ").append(parameters.get(AbiCheckerConstants.REFERENCE_TAG)).append("\n");
         sb.append("Artifact files: ").append(parameters.get(AbiCheckerConstants.ARTIFACT_FILES)).append("\n");
         sb.append("Artifact type: ").append(parameters.get(AbiCheckerConstants.ARTIFACT_TYPE)).append("\n");
         sb.append("Header files: ").append(parameters.get(AbiCheckerConstants.ARTIFACT_HEADER_FILES)).append("\n");
         sb.append("Shared library files: ")
                 .append(parameters.get(AbiCheckerConstants.ARTIFACT_LIBRARY_FILES)).append("\n");
 
         return sb.toString();
     }
 
 }

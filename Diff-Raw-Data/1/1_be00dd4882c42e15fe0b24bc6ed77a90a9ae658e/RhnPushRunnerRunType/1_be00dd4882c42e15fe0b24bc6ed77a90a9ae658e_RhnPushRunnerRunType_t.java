 package com.miltrex.tc.rhnpush;
 
 import jetbrains.buildServer.serverSide.InvalidProperty;
 import jetbrains.buildServer.serverSide.PropertiesProcessor;
 import jetbrains.buildServer.serverSide.RunType;
 import jetbrains.buildServer.serverSide.RunTypeRegistry;
 import jetbrains.buildServer.util.PropertiesUtil;
 import jetbrains.buildServer.web.openapi.PluginDescriptor;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.*;
 
 public class RhnPushRunnerRunType extends RunType {
     private PluginDescriptor pluginDescriptor;
 
     public RhnPushRunnerRunType(final RunTypeRegistry registry, final PluginDescriptor pluginDescriptor) {
         this.pluginDescriptor = pluginDescriptor;
         registry.registerRunType(this);
     }
 
     @NotNull
     @Override
     public String getType() {
         return PluginConstants.RUN_TYPE;
     }
 
     @Override
     @NotNull
     public String getDisplayName() {
         return PluginConstants.RUNNER_DISPLAY_NAME;
     }
 
     @Override
     public String getDescription() {
         return PluginConstants.RUNNER_DESCRIPTION;
     }
 
 
     @Override
     @NotNull
     public String describeParameters(@NotNull final Map<String, String> parameters)
     {
         StringBuilder sb = new StringBuilder();
         sb.append("Working Directory: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_BUILD_WORKINGDIR));
         sb.append(" \n");
         sb.append("Server: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_SERVER));
         sb.append(" \n");
         sb.append("Username: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_USERNAME));
         sb.append(" \n");
         sb.append("Channel: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_CHANNEL));
         sb.append(" \n");
         sb.append("Artifact paths: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_PARAM_SOURCE_PATH));
        sb.append(" \n");
         sb.append("Non-GPG signed: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_NOGPG) == null ? "false" : "true");
         sb.append(" \n");
         sb.append("Verbose: ");
         sb.append(parameters.get(PluginConstants.PROPERTY_VERBOSE));
         sb.append(" \n");
         return sb.toString();
     }
 
 
     @Override
     public PropertiesProcessor getRunnerPropertiesProcessor()
     {
         return new PropertiesProcessor()
         {
             public Collection<InvalidProperty> process(Map<String, String> properties)
             {
                 final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
 
                 if (PropertiesUtil.isEmptyOrNull(properties.get(PluginConstants.PROPERTY_SERVER))) {
                     result.add(new InvalidProperty(PluginConstants.PROPERTY_SERVER, "Host name can not be empty"));
                 }
 
                 if (PropertiesUtil.isEmptyOrNull(properties.get(PluginConstants.PROPERTY_USERNAME))) {
                     result.add(new InvalidProperty(PluginConstants.PROPERTY_USERNAME, "Username can not be empty"));
                 }
 
                 if (PropertiesUtil.isEmptyOrNull(properties.get(PluginConstants.PROPERTY_PASSWORD))) {
                     result.add(new InvalidProperty(PluginConstants.PROPERTY_PASSWORD, "Password can not be empty"));
                 }
 
                 if (PropertiesUtil.isEmptyOrNull(properties.get(PluginConstants.PROPERTY_CHANNEL))) {
                     result.add(new InvalidProperty(PluginConstants.PROPERTY_CHANNEL, "Channel can not be empty"));
                 }
 
                 return result;
             }
         };
     }
 
     @Override
     public String getEditRunnerParamsJspFilePath() {
         return pluginDescriptor.getPluginResourcesPath("editRunnerRunParameters.jsp");
     }
 
     @Override
     public String getViewRunnerParamsJspFilePath() {
         return pluginDescriptor.getPluginResourcesPath("viewRunnerRunParameters.jsp");
     }
 
     @Nullable
     @Override
     public Map<String, String> getDefaultRunnerProperties() {
         Map<String, String> map = new HashMap<String, String>();
         map.put(PluginConstants.PROPERTY_VERBOSE, Boolean.TRUE.toString());
         return map;
     }
 }

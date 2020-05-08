 package com.atlassian.plugins.codegen.modules.common;
 
 import com.atlassian.plugins.codegen.annotations.*;
 import com.atlassian.plugins.codegen.modules.AbstractPluginModuleCreator;
 import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
 import com.atlassian.plugins.codegen.modules.common.component.ComponentImportModuleCreator;
 import com.atlassian.plugins.codegen.modules.common.component.ComponentImportProperties;
 import com.atlassian.plugins.codegen.modules.common.component.ComponentModuleCreator;
 import com.atlassian.plugins.codegen.modules.common.component.ComponentProperties;
 
 /**
 * @since 3.7
  */
 @RefAppPluginModuleCreator
 @JiraPluginModuleCreator
 @ConfluencePluginModuleCreator
 @BambooPluginModuleCreator
 @FeCruPluginModuleCreator
 @CrowdPluginModuleCreator
 @Dependencies({
         @Dependency(groupId = "com.atlassian.upm", artifactId = "licensing-api", version = "2.0", scope = "provided"),
         @Dependency(groupId = "com.atlassian.upm", artifactId = "upm-api", version = "2.0", scope = "provided"),
         @Dependency(groupId = "com.atlassian.plugins", artifactId = "atlassian-plugins-core", version = "2.9.0", scope = "provided"),
         @Dependency(groupId = "com.atlassian.sal", artifactId = "sal-api", version = "2.6.0", scope = "provided")
 })
 public class LicensingModuleCreator extends AbstractPluginModuleCreator<LicensingProperties>
 {
     public static final String MODULE_NAME = "Atlassian License Management";
 
     public static final String PLUGIN_LICENSE_MANAGER_CLASS = "com.atlassian.upm.api.license.PluginLicenseManager";
     public static final String PLUGIN_CONTROLLER_CLASS = "com.atlassian.plugin.PluginController";
     public static final String PLUGIN_LICENSE_EVENT_REGISTRY_CLASS = "com.atlassian.upm.api.license.PluginLicenseEventRegistry";
     public static final String LICENSE_CHECKER_DESCRIPTION = "Atlassian license management module";
     public static final String LICENSE_CHECKER_CLASS_TEMPLATE = "templates/common/licensing/LicenseChecker.java.vtl";
     
     @Override
     public void createModule(PluginModuleLocation location, LicensingProperties props) throws Exception
     {
         addPluginInfoParamToPluginXml(location, "atlassian-licensing-enabled", "true");
         
         ComponentImportModuleCreator importCreator = new ComponentImportModuleCreator();
         importCreator.createModule(location, new ComponentImportProperties(PLUGIN_LICENSE_MANAGER_CLASS));
         importCreator.createModule(location, new ComponentImportProperties(PLUGIN_CONTROLLER_CLASS));
         importCreator.createModule(location, new ComponentImportProperties(PLUGIN_LICENSE_EVENT_REGISTRY_CLASS));
         
         ComponentModuleCreator componentCreator = new ComponentModuleCreator();
         ComponentProperties componentProps = new ComponentProperties(props.getFullyQualifiedClassname());
         componentProps.setGenerateClass(false);
         componentProps.setGenerateInterface(false);
         componentProps.setIncludeExamples(false);
         componentProps.setDescription(LICENSE_CHECKER_DESCRIPTION);
         componentCreator.createModule(location, componentProps);
         
         templateHelper.writeJavaClassFromTemplate(LICENSE_CHECKER_CLASS_TEMPLATE,
                                                   props.getClassname(),
                                                   location.getSourceDirectory(),
                                                   props.getPackage(),
                                                   props);
     }
 
     @Override
     public String getModuleName()
     {
         return MODULE_NAME;
     }
 }

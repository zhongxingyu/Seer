 package com.atlassian.maven.plugins.amps.codegen.prompter;
 
 import com.atlassian.maven.plugins.amps.codegen.annotations.ModuleCreatorClass;
 import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
 import com.atlassian.plugins.codegen.modules.common.LicensingModuleCreator;
 import com.atlassian.plugins.codegen.modules.common.LicensingProperties;
 import com.atlassian.plugins.codegen.util.ClassnameUtil;
 
 import org.codehaus.plexus.components.interactivity.Prompter;
 import org.codehaus.plexus.components.interactivity.PrompterException;
 
 /**
 * @since 3.6
  */
 @ModuleCreatorClass(LicensingModuleCreator.class)
 public class LicensingPrompter extends AbstractModulePrompter<LicensingProperties>
 {
 
     public LicensingPrompter(Prompter prompter)
     {
         super(prompter);
         showAdvancedPrompt = false;
         showExamplesPrompt = false;
     }
 
     @Override
     public LicensingProperties promptForBasicProperties(PluginModuleLocation moduleLocation) throws PrompterException
     {
         String className = promptJavaClassname("Enter License Checker Class name", "LicenseChecker");
         String packageName = promptJavaPackagename("Enter Package Name", getDefaultBasePackage());
 
         LicensingProperties props = new LicensingProperties(ClassnameUtil.fullyQualifiedName(packageName, className));
 
         suppressExamplesPrompt();
 
         return props;
     }
 }

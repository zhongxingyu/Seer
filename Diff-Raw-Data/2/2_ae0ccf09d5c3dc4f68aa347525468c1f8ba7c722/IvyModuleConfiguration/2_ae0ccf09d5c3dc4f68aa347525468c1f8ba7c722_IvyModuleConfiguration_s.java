 /*
  * Copyright 2011 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ivyplug.ui.configuration.module;
 
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.vfs.VirtualFile;
 import ivyplug.bundles.IvyPlugBundle;
 import ivyplug.ui.configuration.Configuration;
 import ivyplug.ui.configuration.project.IvyProjectConfiguration;
 import ivyplug.ui.configuration.project.IvyProjectConfigurationProjectComponent;
 import ivyplug.ui.messages.Message;
 import ivyplug.ui.messages.MessagesProjectComponent;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
  * @since 03.02.2011
  */
 public class IvyModuleConfiguration extends Configuration {
 
     private static final String DEFAULT_IVY_XML_FILENAME = "ivy.xml";
     private static final String DEFAULT_IVY_SETTINGS_XML_FILENAME = "ivysettings.xml";
 
     private Module module;
     private IvyProjectConfiguration projectConfiguration;
     private ModuleRootManager moduleRootManager;
     private MessagesProjectComponent messagesProjectComponent;
     private boolean useAutoDiscovery = true;
     private File ivyXMlFile;
 
     public IvyModuleConfiguration(Module module) {
         this.module = module;
         moduleRootManager = ModuleRootManager.getInstance(module);
         IvyProjectConfigurationProjectComponent configurationProjectComponent =
                 module.getProject().getComponent(IvyProjectConfigurationProjectComponent.class);
         projectConfiguration = configurationProjectComponent.getConfiguration();
     }
 
     public boolean isUseAutoDiscovery() {
         return useAutoDiscovery;
     }
 
     public void setUseAutoDiscovery(boolean useAutoDiscovery) {
         this.useAutoDiscovery = useAutoDiscovery;
     }
 
     @Nullable
     public File getIvyXMlFile() {
         if (useAutoDiscovery) {
             ivyXMlFile = locateFileInsideModule(DEFAULT_IVY_XML_FILENAME);
         }
         return ivyXMlFile;
     }
 
     public void setIvyXMlFile(File ivyXMlFile) {
         this.ivyXMlFile = ivyXMlFile;
     }
 
     @Nullable
     public File getIvySettingsXMlFile() {
         if (useAutoDiscovery) {
             setIvySettingsXMlFile(locateFileInsideModule(DEFAULT_IVY_SETTINGS_XML_FILENAME));
         }
         File result = super.getIvySettingsXMlFile();
         if (result == null) {
            setIvySettingsXMlFile(projectConfiguration.getIvySettingsXMlFile());
         }
         return result;
     }
 
     public Map<String, String> getResolvedProperties() {
         Map<String, String> result = new HashMap<String, String>(projectConfiguration.getResolvedProperties());
         result.put("module.name", module.getName());
         result.put("module.path", ivyXMlFile.getParent());
         result.put("basedir", ivyXMlFile.getParent());
         result.putAll(super.getResolvedProperties());
         return result;
     }
 
     @Override
     protected void warn(String message) {
         if (messagesProjectComponent == null)
             messagesProjectComponent = module.getProject().getComponent(MessagesProjectComponent.class);
         messagesProjectComponent.addToTab(IvyPlugBundle.message("general.message.tab"), new Message(Message.Type.WARNING, message));
     }
 
     @Nullable
     private File locateFileInsideModule(String filename) {
         File result = null;
         VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
         for (VirtualFile contentRoot : contentRoots) {
             File file = new File(contentRoot.getPath(), filename);
             if (file.exists()) {
                 result = file;
                 break;
             }
         }
         return result;
     }
 }

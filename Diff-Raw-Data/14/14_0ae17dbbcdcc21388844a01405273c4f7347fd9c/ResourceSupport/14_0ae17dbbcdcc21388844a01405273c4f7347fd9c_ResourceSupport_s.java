 /**
  *   Copyright 2010,2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.ui, is a library of UI widgets
  */
 package net.karlmartens.ui;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public final class ResourceSupport {
 
   public static ImageDescriptor imageDescriptorFromPlugin(String pluginId, Class<?> clazz, String imageFilePath) {
    ImageDescriptor d = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
    if (d != null)
      return d;

    d = ImageDescriptor.createFromFile(clazz, imageFilePath);
     if (d != null)
       return d;
 
     return ImageDescriptor.getMissingImageDescriptor();
   }
 }

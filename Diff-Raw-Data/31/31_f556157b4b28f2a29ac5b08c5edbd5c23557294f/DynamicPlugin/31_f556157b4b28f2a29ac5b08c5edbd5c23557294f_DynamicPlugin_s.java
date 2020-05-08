 package com.atlassian.plugin.impl;
 
 import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.loaders.classloading.DeploymentUnit;

import java.net.URL;
import java.io.InputStream;
 
 public interface DynamicPlugin extends Plugin
 {
 
     void setDeletable(boolean deletable);
 
 
     void setBundled(boolean bundled);
 
 }

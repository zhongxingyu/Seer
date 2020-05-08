 package com.atlassian.plugin.impl;
 
 import com.atlassian.plugin.Plugin;
 
 public interface DynamicPlugin extends Plugin
 {
 
     void setDeletable(boolean deletable);
 
 
     void setBundled(boolean bundled);
 
 }

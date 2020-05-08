 package com.atlassian.sal.fisheye.lifecycle;
 
 import com.cenqua.fisheye.AppConfig;
 import com.cenqua.fisheye.config.AdminConfig;
 import com.cenqua.fisheye.config.RootConfig;
 import com.atlassian.sal.core.lifecycle.DefaultLifecycleManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.plugin.event.PluginEventManager;

import java.util.List;
 
 public class FisheyeLifecycleManager extends DefaultLifecycleManager
 {
    public FisheyeLifecycleManager(PluginEventManager eventManager, List<LifecycleAware> listeners)
    {
        super(eventManager, listeners);
    }
 
    public boolean isApplicationSetUp()
 	{
 		// this code is copied from TotalityFilter.requresSetup() method
 		RootConfig rootConfig = AppConfig.getsConfig();
         final AdminConfig acfg = rootConfig.getAdminConfig();
         boolean requiresSetup = !acfg.haveDoneInitialSetup() || (rootConfig.getLicense() == null) || rootConfig.getLicense().isTerminated();
         return !requiresSetup;
 	}
 
 }

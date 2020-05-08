 package com.atlassian.sal.core.lifecycle;
 
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.lifecycle.LifecycleManager;
 import com.atlassian.sal.api.lifecycle.LifecycleAware;
 
 public abstract class DefaultLifecycleManager implements LifecycleManager
 {
     private boolean started = false;
     private static final Logger log = Logger.getLogger(DefaultLifecycleManager.class);
 
     public synchronized void start()
     {
        if (!started && isApplicationSetUp())
         {
             try
             {
                 notifyOnStart();
             } finally {
                 started = true;
             }
 
         }
     }
    protected void notifyOnStart()
     {
        Collection<LifecycleAware> listeners = ComponentLocator.getComponents(LifecycleAware.class);
        for (LifecycleAware entry : listeners)
         {
             try
             {
                 entry.onStart();
            } catch (RuntimeException ex)
             {
                 log.error("Unable to start component: "+ entry.getClass().getName(), ex);
             }
         }
     }
 }

 package com.atlassian.sal.core.lifecycle;
 
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.lifecycle.LifecycleManager;
 
 public abstract class DefaultLifecycleManager implements LifecycleManager
 {
     private boolean started = false;
     private static final Logger log = Logger.getLogger(DefaultLifecycleManager.class);
 
     public synchronized void start()
     {
        if (!isStarted() && isApplicationSetUp())
         {
             try
             {
                 notifyOnStart();
             } finally {
                 started = true;
             }
 
         }
     }
    
    protected boolean isStarted()
	{
		return started;
	}
    
	protected void notifyOnStart()
     {
        final Collection<LifecycleAware> listeners = ComponentLocator.getComponents(LifecycleAware.class);
        for (final LifecycleAware entry : listeners)
         {
             try
             {
                 entry.onStart();
            } catch (final RuntimeException ex)
             {
                 log.error("Unable to start component: "+ entry.getClass().getName(), ex);
             }
         }
     }
 }

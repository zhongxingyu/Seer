 package com.atlassian.sal.crowd.lifecycle;
 
 import com.atlassian.config.lifecycle.events.ApplicationStartedEvent;
 import com.atlassian.event.Event;
 import com.atlassian.event.EventListener;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.lifecycle.LifecycleManager;
 
 /**
  * Listens to ApplicationStartedEvent and notifies lifecycle manager.
  */
 public class ApplicationReadyListener implements EventListener
 {
 	@SuppressWarnings("unchecked")
 	public Class[] getHandledEventClasses()
 	{
 		return new Class[]{ApplicationStartedEvent.class};
 	}
 
 	public void handleEvent(Event event)
 	{
 		if (event instanceof ApplicationStartedEvent)
 		{
			final LifecycleManager lifecycleManager = ComponentLocator.getComponent(LifecycleManager.class);
			lifecycleManager.start();
 		}
 		
 	}
 
 }

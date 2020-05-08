 package com.laboki.eclipse.plugin.jcolon.main;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionEndedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionStartedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.LocateSemiColonErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.events.SemiColonErrorLocationEvent;
 import com.laboki.eclipse.plugin.jcolon.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.task.Task;
 
 final class ErrorLocator extends EventBusInstance {
 
 	protected static final Logger LOGGER =
 		Logger.getLogger(ErrorLocator.class.getName());
 	protected final Problem problem = new Problem();
 	protected boolean completionAssistantIsActive;
 
 	public ErrorLocator() {
 		super();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void
 	locateSemiColonErrorEventHandler(final LocateSemiColonErrorEvent event) {
 		this.locateSemiColonError();
 	}
 
 	private void
 	locateSemiColonError() {
 		new Task() {
 
 			@Override
 			public boolean
 			shouldSchedule() {
 				if (ErrorLocator.this.completionAssistantIsActive) return false;
				return EditorContext.taskDoesNotExist(EditorContext.ERROR_CHECKING_FAMILY);
 			}
 
 			@Override
 			public void
 			execute() {
 				this.findErrorLocation();
 			}
 
 			private void
 			findErrorLocation() {
 				try {
 					this.tryToFindErrorLocation();
 				}
 				catch (final Exception e) {
 					ErrorLocator.LOGGER.log(Level.WARNING, e.getMessage(), e);
 				}
 			}
 
 			private void
 			tryToFindErrorLocation() throws Exception {
 				if (this.hasMissingSemiColonError()) this.postEvent(ErrorLocator.this.problem.location());
 			}
 
 			private boolean
 			hasMissingSemiColonError() {
 				return ErrorLocator.this.problem.isMissingSemiColonError();
 			}
 
 			private void
 			postEvent(final int location) {
 				EventBus.post(new SemiColonErrorLocationEvent(location));
 			}
 		}.setRule(EditorContext.ERROR_CHECKER_RULE)
 			.setFamily(EditorContext.ERROR_CHECKING_FAMILY)
 			.setDelay(EditorContext.SHORT_DELAY)
 			.start();
 	}
 
 	@Subscribe
 	public void
 	assistSessionStartedEventHandler(final AssistSessionStartedEvent event) {
 		this.completionAssistantIsActive = true;
 	}
 
 	@Subscribe
 	public void
 	assistSessionEndedEventHandler(final AssistSessionEndedEvent event) {
 		this.completionAssistantIsActive = false;
 	}
 }

 package com.laboki.eclipse.plugin.jcolon.main;
 
 import org.eclipse.ui.IEditorPart;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionEndedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionStartedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.CheckErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.events.LocateSemiColonErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.task.AsyncTask;
 
 final class ErrorChecker extends EventBusInstance {
 
 	protected final IEditorPart editor = EditorContext.getEditor();
 	protected boolean completionAssistantIsActive;
 
 	public ErrorChecker() {
 		super();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void
 	checkErrorEventHandler(final CheckErrorEvent event) {
 		new AsyncTask() {
 
 			@Override
 			public boolean
 			shouldSchedule() {
 				if (ErrorChecker.this.completionAssistantIsActive) return false;
				return EditorContext.taskDoesNotExist(EditorContext.ERROR_CHECKING_FAMILY);
 			}
 
 			@Override
 			public void
 			execute() {
 				if (this.canPostEvent()) this.postEvent();
 			}
 
 			private boolean
 			canPostEvent() {
 				return this.isNotInEditMode() || this.hasJDTErrors();
 			}
 
 			private boolean
 			isNotInEditMode() {
 				return !EditorContext.isInEditMode(ErrorChecker.this.editor);
 			}
 
 			private boolean
 			hasJDTErrors() {
 				return EditorContext.hasJDTErrors(ErrorChecker.this.editor);
 			}
 
 			private void
 			postEvent() {
 				EventBus.post(new LocateSemiColonErrorEvent());
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

 package com.laboki.eclipse.plugin.jcolon.main;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionEndedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionStartedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.SemiColonErrorLocationEvent;
 import com.laboki.eclipse.plugin.jcolon.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.task.AsyncTask;
 
 final class Inserter extends EventBusInstance {
 
 	protected static final Logger LOGGER =
 		Logger.getLogger(Inserter.class.getName());
 	private static final String SEMICOLON = ";";
 	protected final Problem problem = new Problem();
 	protected final IEditorPart editor = EditorContext.getEditor();
 	protected final IDocument document = EditorContext.getDocument(this.editor);
 	protected boolean completionAssistantIsActive;
 
 	public Inserter() {
 		super();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void
 	semiColonErrorLocationEventHandler(final SemiColonErrorLocationEvent event) {
 		new AsyncTask() {
 
 			@Override
 			public boolean
 			shouldSchedule() {
 				if (Inserter.this.completionAssistantIsActive) return false;
				return EditorContext.taskDoesNotExist(EditorContext.ERROR_CHECKING_FAMILY);
 			}
 
 			@Override
 			public void
 			execute() {
 				this.insertSemiColon(event.getLocation());
 			}
 
 			private void
 			insertSemiColon(final int location) {
 				try {
 					this.tryToInsertSemiColon(location);
 				}
 				catch (final Exception e) {
 					Inserter.LOGGER.log(Level.WARNING, e.getMessage(), e);
 				}
 			}
 
 			private void
 			tryToInsertSemiColon(final int location) throws Exception {
 				if (this.cannotInsertSemiColon(location)) return;
 				Inserter.this.document.replace(location, 0, Inserter.SEMICOLON);
 			}
 
 			private boolean
 			cannotInsertSemiColon(final int location) throws Exception {
 				return this.semiColonIsAlreadyInserted(location)
 					|| this.locationErrorMismatch(location)
 					|| EditorContext.isInEditMode(Inserter.this.editor);
 			}
 
 			private boolean
 			semiColonIsAlreadyInserted(final int location) throws Exception {
 				if (this.isEndOfDocument(location)) return false;
 				return String.valueOf(Inserter.this.document.getChar(location))
 					.equals(Inserter.SEMICOLON);
 			}
 
 			private boolean
 			isEndOfDocument(final int location) {
 				return Inserter.this.document.getLength() == location;
 			}
 
 			private boolean
 			locationErrorMismatch(final int location) {
 				try {
 					return location != Inserter.this.problem.location();
 				}
 				catch (final Exception e) {
 					return false;
 				}
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

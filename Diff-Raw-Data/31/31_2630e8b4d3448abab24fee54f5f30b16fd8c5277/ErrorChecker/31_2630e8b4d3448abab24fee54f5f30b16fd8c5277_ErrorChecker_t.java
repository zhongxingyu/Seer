 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPropertyListener;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.DelayedTask;
 import com.laboki.eclipse.plugin.jcolon.Instance;
 import com.laboki.eclipse.plugin.jcolon.Task;
 import com.laboki.eclipse.plugin.jcolon.inserter.events.CheckSemiColonErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.inserter.events.SyncFilesEvent;
 
 final class ErrorChecker implements Instance, KeyListener, IPropertyListener {
 
 	private EventBus eventBus;
 	private static final String TASK_FAMILY_NAME = "SEMI_COLON_ERROR_CHECKER";
 	private Control buffer = EditorContext.getBuffer(EditorContext.getEditor());
 	private IEditorPart editor = EditorContext.getEditor();
 
 	public ErrorChecker(final EventBus eventBus) {
 		this.eventBus = eventBus;
 		this.eventBus.register(this);
 	}
 
 	@Override
 	public Instance begin() {
		this.checkError();
 		this.editor.addPropertyListener(this);
 		this.buffer.addKeyListener(this);
 		return this;
 	}
 
 	@Override
 	public Instance end() {
 		this.eventBus.unregister(this);
 		this.editor.removePropertyListener(this);
 		this.buffer.removeKeyListener(this);
 		this.nullifyFields();
 		return this;
 	}
 
 	private void nullifyFields() {
 		this.eventBus = null;
 		this.buffer = null;
 		this.editor = null;
 	}
 
 	@Override
 	public void keyPressed(final KeyEvent arg0) {
 		ErrorChecker.cancelFindSemiColonError();
 	}
 
 	private static void cancelFindSemiColonError() {
 		EditorContext.asyncExec(new Task("") {
 
 			@Override
 			public void execute() {
 				ErrorChecker.cancelJobs();
 			}
 		});
 	}
 
 	@Override
 	public void keyReleased(final KeyEvent arg0) {
		this.checkError();
 	}
 
 	@Override
 	public void propertyChanged(final Object arg0, final int propID) {
		this.checkError();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void syncFiles(@SuppressWarnings("unused") final SyncFilesEvent event) {
		this.checkError();
	}

	private void checkError() {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				ErrorChecker.cancelJobs();
				ErrorChecker.this.findSemiColonError();
			}
		});
	}

	private static void cancelJobs() {
		EditorContext.cancelJobsBelongingTo(ErrorChecker.TASK_FAMILY_NAME);
 	}
 
 	private void findSemiColonError() {
 		EditorContext.asyncExec(new DelayedTask(ErrorChecker.TASK_FAMILY_NAME, 1000) {
 
 			@Override
 			public void execute() {
 				ErrorChecker.this.postEvent();
 			}
 		});
 	}
 
 	private void postEvent() {
 		this.eventBus.post(new CheckSemiColonErrorEvent());
 	}
 }

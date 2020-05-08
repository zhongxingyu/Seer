 package ru.kt15.finomen.neerc.hall.desktop;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import ru.kt15.finomen.neerc.core.LocaleManager;
 import ru.kt15.finomen.neerc.core.Localized;
 import ru.kt15.finomen.neerc.hall.Task;
 
 public class TaskStateChange extends Dialog implements Localized {
 
 	protected Object result;
 	protected Shell shell;
 	private Task task;
 	private Text text;
 	private boolean assignedEnabled, inProgressEnabled, doneEnabled, failedEnabled;
 	private Task.TaskState.StateId state;
 	private LocaleManager localeManager;
 	private Group grpState;
 	private Button btnAssigned;
 	private Button btnDone;
 	private Button btnInProgress;
 	private Button btnFailed;
 	private Button btnSave;
 	private Group grpMessageoptional;
 	/**
 	 * Create the dialog.
 	 * @param localeManager 
 	 * @param parent
 	 * @param style
 	 */
 	public TaskStateChange(LocaleManager localeManager, Task task, Shell parent, int style) {
 		super(parent, style);
 		this.localeManager = localeManager;
 		this.task = task;
 		assignedEnabled = inProgressEnabled = doneEnabled = failedEnabled = false;
 		for (Task.TaskState.StateId sid : task.getPossibleStates()) {
 			switch(sid) {
 			case ASSIGNED:
 				assignedEnabled = true;
 				break;
 			case DONE:
 				doneEnabled = true;
 				break;
 			case IN_PROGRESS:
 				inProgressEnabled = true;
 				break;
 			case FAILED:
 				failedEnabled = true;
 				break;
 			case UPDATING:
 				// This state can be set only by manager
 				break;
 			}
 		} 
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
 	public Object open() {
 		createContents();
 		shell.open();
 		shell.layout();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	private void createContents() {
 		shell = new Shell(getParent(), getStyle());
 		shell.setSize(352, 146);
 		shell.setText(getText());
 		shell.setLayout(new GridLayout(2, false));
 		
 		
 		
 		grpState = new Group(shell, SWT.NONE);
 		grpState.setLayout(new FillLayout(SWT.VERTICAL));
 		
 		btnAssigned = new Button(grpState, SWT.RADIO);
 		
 		btnInProgress = new Button(grpState, SWT.RADIO);
 		
 		btnDone = new Button(grpState, SWT.RADIO);
 		
 		btnFailed = new Button(grpState, SWT.RADIO);
 				
 		grpMessageoptional = new Group(shell, SWT.NONE);
 		GridData gd_grpMessageoptional = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
 		gd_grpMessageoptional.widthHint = 244;
 		grpMessageoptional.setLayoutData(gd_grpMessageoptional);
 		grpMessageoptional.setLayout(new FillLayout(SWT.HORIZONTAL));
 			
 		text = new Text(grpMessageoptional, SWT.BORDER | SWT.MULTI);
 		new Label(shell, SWT.NONE);
 		
 		btnSave = new Button(shell, SWT.NONE);
 		
 		btnAssigned.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
				text.setEnabled(false);
 			}
 		});
 		
 		SelectionAdapter adapt = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				text.setEnabled(true);
 			}
 		};
 		
 		btnDone.addSelectionListener(adapt);
 		btnFailed.addSelectionListener(adapt);
 		btnInProgress.addSelectionListener(adapt);
 		
 		btnAssigned.setEnabled(assignedEnabled);
 		btnDone.setEnabled(doneEnabled);
 		btnInProgress.setEnabled(inProgressEnabled);
 		btnFailed.setEnabled(failedEnabled);
 		
 		state = task.getState().getId();
 		text.setText(task.getState().getMessage());
 		
 		switch(state) {
 		case ASSIGNED:
 			btnAssigned.setSelection(true);
 			break;
 		case DONE:
 			btnDone.setSelection(true);
 			break;
 		case IN_PROGRESS:
 			btnInProgress.setSelection(true);
 			break;
 		case FAILED:
 			btnFailed.setSelection(true);
 			break;
 		case UPDATING:
 			break;
 		}
		
 		btnFailed.setSelection(true);
		
 		btnSave.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (btnAssigned.getSelection()) {
					task.changeState(Task.TaskState.assigned());
 				} else if (btnInProgress.getSelection()) {
 					task.changeState(Task.TaskState.inProgress(text.getText()));
 				} else if (btnDone.getSelection()) {
 					task.changeState(Task.TaskState.done(text.getText()));
 				} else if (btnFailed.getSelection()) {
 					task.changeState(Task.TaskState.failed(text.getText()));
 				}
 				
 				shell.close();
 			}
 		});
 		
 		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,true));
 		
 		localeManager.addLocalizedObject(this);
 	}
 
 	@Override
 	public void setLocaleStrings() {
 		shell.setText(localeManager.localize("State change"));
 		grpState.setText(localeManager.localize("State"));
 		btnAssigned.setText(localeManager.localize("Assigned"));
 		btnInProgress.setText(localeManager.localize("In progress"));
 		btnDone.setText(localeManager.localize("Done"));
 		btnFailed.setText(localeManager.localize("Failed"));
 		grpMessageoptional.setText(localeManager.localize("Message (optional)"));
 		btnSave.setText(localeManager.localize("Save"));		
 	}
 	
 	@Override
 	public boolean isDisposed() {
 		return shell.isDisposed();
 	}
 }

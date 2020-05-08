 package org.codefaces.ui.widgets;
 
 import org.apache.commons.lang.StringUtils;
 import org.codefaces.ui.CodeFacesUIActivator;
 import org.codefaces.ui.Images;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 
 public class ValidatableComboViewer {
 	private final class SchedulingJob extends Job {
 		private SchedulingJob(String name) {
 			super(name);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			validateText(monitor);
 
 			return new Status(IStatus.OK, CodeFacesUIActivator.PLUGIN_ID,
 					IStatus.OK, "", null);
 		}
 	}
 
	private final class CancelListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			validateJob.cancel();
		}
	}

 	private final class SchedulingModifyListener implements ModifyListener {
 		@Override
 		public void modifyText(ModifyEvent event) {
 			validateJob.schedule(VALIDATION_INTERVAL);
 		}
 	}
 
 	private static final int VALIDATION_INTERVAL = 500;
 
 	private Control cancelComponent;
 
 	private CancelListener cancelListener;
 
 	private Composite composite;
 
 	private final Display display;
 
 	private Label errorMessageLabel;
 
 	private final IProgressMonitorInputValidator inputValidator;
 
 	private boolean isValidInput = false;
 
 	private Object selectedObject;
 
 	private Job validateJob;
 
 	private ComboViewer viewer;
 
 	public ValidatableComboViewer(Composite parent, int style,
 			IProgressMonitorInputValidator validator) {
 		display = parent.getDisplay();
 		composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.verticalSpacing = 5;
 		layout.horizontalSpacing = 0;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		composite.setFont(parent.getFont());
 
 		viewer = new ComboViewer(new CCombo(composite, style));
 		viewer.getControl().setLayoutData(
 				new GridData(GridData.GRAB_HORIZONTAL
 						| GridData.HORIZONTAL_ALIGN_FILL));
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event
 						.getSelection();
 				if (selection == null) {
 					selectedObject = null;
 				} else {
 					selectedObject = selection.getFirstElement();
 				}
 			}
 		});
 
 		errorMessageLabel = new Label(composite, SWT.NONE);
 		errorMessageLabel.setImage(Images.getImage(Images.IMG_ERRORS));
 		setErrorMessages(null);
 
 		this.inputValidator = validator;
 		if (inputValidator != null) {
 			viewer.getCCombo()
 					.addModifyListener(new SchedulingModifyListener());
 			this.validateJob = new SchedulingJob("Validating");
 			this.cancelListener = new CancelListener();
 		}
 	}
 
 	public void attachToCancelComponent(Control cancelComponent) {
 		Assert.isNotNull(inputValidator);
 		Assert.isNotNull(cancelComponent);
 		this.cancelComponent = cancelComponent;
 		this.cancelComponent.addListener(SWT.Selection, cancelListener);
 	}
 
 	public Control getControl() {
 		return composite;
 	}
 
 	public Object getSelectedObject() {
 		return selectedObject;
 	}
 
 	public ComboViewer getViewer() {
 		return viewer;
 	}
 
 	public boolean isValidInput() {
 		return isValidInput;
 	}
 
 	/**
 	 * Detach the progress monitor part from the given cancel component
 	 * 
 	 * @param cc
 	 */
 	public void removeFromCancelComponent(Control cancelComponent) {
 		Assert.isNotNull(inputValidator);
 		Assert.isTrue(this.cancelComponent == cancelComponent
 				&& this.cancelComponent != null);
 		this.cancelComponent.removeListener(SWT.Selection, cancelListener);
 		this.cancelComponent = null;
 	}
 
 	public void setErrorMessages(String msg) {
 		if (msg == null) {
 			errorMessageLabel.setToolTipText(null);
 			errorMessageLabel.setVisible(false);
 		} else {
 			errorMessageLabel.setToolTipText(msg);
 			errorMessageLabel.setVisible(true);
 		}
 	}
 
 	public void setInput(Object input) {
 		viewer.setInput(input);
 	}
 
 	public void setSelectedObject(Object selectedObject) {
 		if (selectedObject == null) {
 			viewer.setSelection(null);
 		} else {
 			viewer.setSelection(new StructuredSelection(selectedObject));
 		}
 	}
 
 	private void validateText(final IProgressMonitor monitor) {
 		display.syncExec(new Runnable() {
 			@Override
 			public void run() {
 				final String text = viewer.getCCombo().getText();
 
 				if (StringUtils.isEmpty(text)) {
 					setErrorMessages("Required field can't be empty.");
 					return;
 				}
 
 				String errorMsg = inputValidator.validate(text, monitor);
 				if (errorMsg == null) {
 					setErrorMessages(null);
 					return;
 				}
 				setErrorMessages(errorMsg);
 			}
 		});
 	}
 }

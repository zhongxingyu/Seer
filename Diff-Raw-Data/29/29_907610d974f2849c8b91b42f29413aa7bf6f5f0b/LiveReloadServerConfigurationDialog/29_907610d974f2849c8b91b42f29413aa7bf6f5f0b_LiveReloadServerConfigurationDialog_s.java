 package org.jboss.tools.livereload.ui.internal.command;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.databinding.swt.WidgetProperties;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.jboss.tools.livereload.ui.internal.configuration.LiveReloadServerConfigurationMessages;
 import org.jboss.tools.livereload.ui.internal.util.ImageRepository;
 
 public class LiveReloadServerConfigurationDialog extends MessageDialog {
 
 	private Button enableScriptInjectionButton;
 	private Button enableRemoteConnectionsButton;
 	private final LiveReloadServerConfigurationDialogModel model;
 	private final DataBindingContext dbc = new DataBindingContext();
 	
 	public LiveReloadServerConfigurationDialog(final LiveReloadServerConfigurationDialogModel model, final String dialogTitle,
 			final String dialogMessage) {
 		super(Display.getDefault().getActiveShell(), dialogTitle, ImageRepository.getInstance().getImage(
				"livereload_wiz.png"), dialogMessage, MessageDialog.CONFIRM, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
 		this.model = model;
 	}
 
 	@Override
 	protected Control createCustomArea(final Composite parent) {
 		final Composite container = new Composite(parent, SWT.NONE);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
 				.applyTo(container);
 		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(container);
 		final GridLayout layout = new GridLayout();
 		container.setLayout(layout);
		// checkbos to enable script injection
 		this.enableScriptInjectionButton = new Button(container, SWT.CHECK);
 		this.enableScriptInjectionButton.setText(LiveReloadServerConfigurationMessages.ENABLE_SCRIPT_INJECTION_LABEL);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(enableScriptInjectionButton);
 		final IObservableValue enableScriptInjectionObservable = BeanProperties.value(
 				LiveReloadServerConfigurationDialogModel.PROPERTY_SCRIPT_INJECTION_ENABLED).observe(model);
 		final IObservableValue enableScriptInjectionButtonSelection = WidgetProperties.selection().observe(enableScriptInjectionButton);
 		dbc.bindValue(enableScriptInjectionButtonSelection, enableScriptInjectionObservable);
 		// checkbox to allow remote connections
 		this.enableRemoteConnectionsButton = new Button(container, SWT.CHECK);
 		this.enableRemoteConnectionsButton.setText(LiveReloadServerConfigurationMessages.ALLOW_REMOTE_CONNECTIONS_LABEL);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(enableRemoteConnectionsButton);
 		final IObservableValue enableRemoteConnectionsObservable = BeanProperties.value(
 				LiveReloadServerConfigurationDialogModel.PROPERTY_REMOTE_CONNECTIONS_ALLOWED).observe(model);
 		final IObservableValue enableRemoteConnectionsButtonSelection = WidgetProperties.selection().observe(enableRemoteConnectionsButton);
 		dbc.bindValue(enableRemoteConnectionsButtonSelection, enableRemoteConnectionsObservable);
 		
 		return container;
 	}
 
 	
 }

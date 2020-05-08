 /**
  * Copyright (c) 2011 Gunnar Wagenknecht and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package org.eclipse.gyrex.admin.ui.http.internal;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ComboDialogField;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField.ColumnsDescription;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
 import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
 import org.eclipse.gyrex.common.identifiers.IdHelper;
 import org.eclipse.gyrex.context.IRuntimeContext;
 import org.eclipse.gyrex.context.internal.ContextActivator;
 import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
 import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
 import org.eclipse.gyrex.http.internal.HttpActivator;
 import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
 import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
 import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.StatusDialog;
 import org.eclipse.jface.fieldassist.ContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.apache.commons.lang.StringUtils;
 
 @SuppressWarnings("restriction")
 public class EditApplicationDialog extends StatusDialog {
 
 	private static final class PropertiesFieldLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 		@Override
 		public Image getColumnImage(final Object element, final int columnIndex) {
 			// no image
 			return null;
 		}
 
 		@Override
 		public String getColumnText(final Object element, final int columnIndex) {
 			if (element instanceof String[]) {
 				return ((String[]) element)[columnIndex];
 			}
 			return getText(element);
 		}
 	}
 
 	private final StringDialogField idField = new StringDialogField();
 	private final ComboDialogField providerField = new ComboDialogField(SWT.DROP_DOWN);
 	private final Map<String, String> providerItemToIdMap = new HashMap<String, String>();
 	private final StringDialogField contextPathField = new StringDialogField();
 	private final ListDialogField propertiesField;
 	{
 		propertiesField = new ListDialogField(new IListAdapter() {
 
 			@Override
 			public void customButtonPressed(final ListDialogField field, final int index) {
 				switch (index) {
 					case 0:
 						addNewProperty();
 						break;
 
 					case 1:
 						editSelectedProperty();
 						break;
 				}
 			}
 
 			@Override
 			public void doubleClicked(final ListDialogField field) {
 				editSelectedProperty();
 			}
 
 			@Override
 			public void selectionChanged(final ListDialogField field) {
 				// nothing to do
 			}
 		}, new String[] { "Add...", "Edit...", "Remove" }, new PropertiesFieldLabelProvider()) {
 			@Override
 			protected int getListStyle() {
 				return SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
 			}
 		};
 		propertiesField.setRemoveButtonIndex(2);
 		propertiesField.setTableColumns(new ColumnsDescription(new String[] { "Key", "Value" }, true));
 	}
 	private final ListDialogField mountsField;
 	{
 		mountsField = new ListDialogField(new IListAdapter() {
 
 			@Override
 			public void customButtonPressed(final ListDialogField field, final int index) {
 				switch (index) {
 					case 0:
 						addNewMount();
 						break;
 				}
 			}
 
 			@Override
 			public void doubleClicked(final ListDialogField field) {
 				editSelectedProperty();
 			}
 
 			@Override
 			public void selectionChanged(final ListDialogField field) {
 				// nothing to do
 			}
 		}, new String[] { "Add...", "Remove" }, new LabelProvider());
 		mountsField.setRemoveButtonIndex(1);
 		mountsField.setViewerComparator(new ViewerComparator(Collator.getInstance(Locale.US)));
 	}
 
 	private final ApplicationManager applicationManager;
 	private final SortedMap<String, String> applicationProperties = new TreeMap<String, String>();
 	private final ApplicationRegistration applicationRegistration;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param parent
 	 */
 	public EditApplicationDialog(final Shell parent, final ApplicationManager applicationManager, final ApplicationRegistration applicationRegistration) {
 		super(parent);
 		this.applicationManager = applicationManager;
 		this.applicationRegistration = applicationRegistration;
 		setTitle(null == applicationRegistration ? "New Application" : "Edit Application");
 		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
 	}
 
 	void addNewMount() {
 		final MountApplicationDialog dialog = new MountApplicationDialog(getParentShell());
 		if (dialog.open() == Window.OK) {
 			mountsField.addElement(dialog.getUrl().toExternalForm());
 		}
 	}
 
 	void addNewProperty() {
 		final EditPropertyDialog dialog = new EditPropertyDialog(getParentShell(), null, null);
 		if (dialog.open() == Window.OK) {
 			applicationProperties.put(dialog.getKey(), dialog.getValue());
 			refreshProperties();
 		}
 	}
 
 	@Override
 	protected Control createDialogArea(final Composite parent) {
 		final Composite composite = (Composite) super.createDialogArea(parent);
 		final GridData gd = (GridData) composite.getLayoutData();
 		gd.minimumHeight = convertVerticalDLUsToPixels(200);
 		gd.minimumWidth = convertHorizontalDLUsToPixels(400);
 
 		idField.setLabelText("Id");
 		contextPathField.setLabelText("Context");
 		providerField.setLabelText("Provider");
 
 		final IDialogFieldListener validateListener = new IDialogFieldListener() {
 			@Override
 			public void dialogFieldChanged(final DialogField field) {
 				validate();
 			}
 		};
 
 		idField.setDialogFieldListener(validateListener);
 		contextPathField.setDialogFieldListener(validateListener);
 		providerField.setDialogFieldListener(validateListener);
 
 		providerItemToIdMap.clear();
 		final TreeSet<String> providerItems = new TreeSet<String>();
 		final Collection<ApplicationProviderRegistration> providers = HttpActivator.getInstance().getProviderRegistry().getRegisteredProviders().values();
 		for (final ApplicationProviderRegistration registration : providers) {
 			final String label = HttpUiAdapterFactory.WORKBENCH_ADAPTER.getLabel(registration);
 			providerItemToIdMap.put(label, registration.getProviderId());
 			providerItems.add(label);
 		}
 		providerField.setItems(providerItems.toArray(new String[providerItems.size()]));
 
 		contextPathField.setContentProposalProcessor(new IContentProposalProvider() {
 			@Override
 			public IContentProposal[] getProposals(final String contents, final int position) {
 				final List<IContentProposal> resultList = new ArrayList<IContentProposal>();
 
 				final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));
 
 				final Collection<ContextDefinition> contexts = ContextActivator.getInstance().getContextRegistryImpl().getDefinedContexts();
 				for (final ContextDefinition contextDefinition : contexts) {
 					if ((null == patternString) || StringUtils.contains(contextDefinition.getPath().toString(), patternString)) {
 						resultList.add(new ContentProposal(contextDefinition.getPath().toString(), contextDefinition.toString()));
 					}
 				}
 
 				return resultList.toArray(new IContentProposal[resultList.size()]);
 			}
 		});
 
 		propertiesField.setLabelText("Properties");
 		mountsField.setLabelText("Mounts");
 
 		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
 		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
 		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, providerField, contextPathField, new Separator(), propertiesField, mountsField }, false);
 		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
 		LayoutUtil.setHorizontalGrabbing(providerField.getComboControl(null));
 		LayoutUtil.setHorizontalGrabbing(contextPathField.getTextControl(null));
 		LayoutUtil.setHorizontalGrabbing(propertiesField.getListControl(null));
 		LayoutUtil.setHorizontalGrabbing(mountsField.getListControl(null));
 
 		if (null != applicationRegistration) {
 			idField.setText(applicationRegistration.getApplicationId());
 			idField.setEnabled(false);
 			contextPathField.setText(applicationRegistration.getContext().getContextPath().toString());
 			contextPathField.setEnabled(false);
 			for (final Entry<String, String> e : providerItemToIdMap.entrySet()) {
 				if (e.getValue().equals(applicationRegistration.getProviderId())) {
 					providerField.selectItem(e.getKey());
 				}
 			}
 			providerField.setEnabled(false);
 
 			applicationProperties.putAll(applicationRegistration.getInitProperties());
 			mountsField.setElements(applicationManager.getMounts(applicationRegistration.getApplicationId()));
 		}
 
 		refreshProperties();
 
 		final GridLayout masterLayout = (GridLayout) composite.getLayout();
 		masterLayout.marginWidth = 5;
 		masterLayout.marginHeight = 5;
 
 		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);
 
 		return composite;
 	}
 
 	void editSelectedProperty() {
 		final List selectedElements = propertiesField.getSelectedElements();
 		if (selectedElements.isEmpty()) {
 			return;
 		}
 
 		final String[] elem = (String[]) selectedElements.get(0);
 		final EditPropertyDialog dialog = new EditPropertyDialog(getParentShell(), elem[0], elem[1]);
 		if (dialog.open() == Window.OK) {
 			applicationProperties.put(dialog.getKey(), dialog.getValue());
 			refreshProperties();
 		}
 
 	}
 
 	@Override
 	protected void okPressed() {
 		validate();
 		if (!getStatus().isOK()) {
 			return;
 		}
 
 		try {
 			// refresh properties
 			applicationProperties.clear();
 			for (final Object property : propertiesField.getElements()) {
 				applicationProperties.put(((String[]) property)[0], ((String[]) property)[1]);
 			}
 
 			// create/update application
 			if (null == applicationRegistration) {
 				final String id = idField.getText();
 				final String providerId = providerItemToIdMap.get(providerField.getText());
 				final String contextPath = contextPathField.getText();
 				final IRuntimeContext context = HttpUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(new Path(contextPath).makeAbsolute().addTrailingSeparator());
 				applicationManager.register(id, providerId, context, applicationProperties);
 			} else {
 				applicationManager.setProperties(applicationRegistration.getApplicationId(), applicationProperties);
 			}
 
 			// update mounts
 			final List applicationMounts = mountsField.getElements();
			final Collection<String> existingMounts = null != applicationRegistration ? applicationManager.getMounts(applicationRegistration.getApplicationId()) : new ArrayList<String>(1);
 			for (final String url : existingMounts) {
 				if (!applicationMounts.contains(url)) {
 					applicationManager.unmount(url);
 				}
 			}
 			for (final Object url : applicationMounts) {
 				if (!existingMounts.contains(url)) {
 					applicationManager.mount((String) url, null != applicationRegistration ? applicationRegistration.getApplicationId() : idField.getText());
 				}
 			}
 		} catch (final Exception e) {
 			ErrorDialog.openError(getParentShell(), "Error", "An error occured saving the application.", getStatus());
 			// don't abort, i.e. close window
 		}
 
 		super.okPressed();
 	}
 
 	private void refreshProperties() {
 		final List<String[]> elements = new ArrayList<String[]>(applicationProperties.size());
 		for (final Entry<String, String> entry : applicationProperties.entrySet()) {
 			elements.add(new String[] { entry.getKey(), entry.getValue() });
 		}
 		propertiesField.setElements(elements);
 	}
 
 	void setError(final String message) {
 		updateStatus(new Status(IStatus.ERROR, HttpUiActivator.SYMBOLIC_NAME, message));
 		getShell().pack(true);
 	}
 
 	void setInfo(final String message) {
 		updateStatus(new Status(IStatus.INFO, HttpUiActivator.SYMBOLIC_NAME, message));
 	}
 
 	void setWarning(final String message) {
 		updateStatus(new Status(IStatus.WARNING, HttpUiActivator.SYMBOLIC_NAME, message));
 	}
 
 	void validate() {
 		final String id = idField.getText();
 		if (StringUtils.isBlank(id)) {
 			setInfo("Please enter an id.");
 			return;
 		}
 		if (!IdHelper.isValidId(id)) {
 			setError("The entered id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
 			return;
 		}
 
 		final String providerLabel = providerField.getText();
 		if (StringUtils.isBlank(providerLabel)) {
 			setInfo("Please select a provider.");
 			return;
 		}
 
 		final String path = contextPathField.getText();
 		if (StringUtils.isBlank(path)) {
 			setInfo("Please enter a context path.");
 			return;
 		}
 		if (!Path.EMPTY.isValidPath(path)) {
 			setError("The entered context path is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_' and '/' as separator.");
 			return;
 		}
 
 		final IRuntimeContextRegistry registry = HttpUiActivator.getInstance().getService(IRuntimeContextRegistry.class);
 		final IRuntimeContext context = registry.get(new Path(path));
 		if (null == context) {
 			setError("The context is not defined!");
 			return;
 		}
 
 		updateStatus(Status.OK_STATUS);
 	}
 }

 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fumldebug.debugger.ui.launch;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
 import org.modelexecution.fumldebug.debugger.ActivityProviderRegistry;
 import org.modelexecution.fumldebug.debugger.FUMLDebuggerPlugin;
 import org.modelexecution.fumldebug.debugger.IActivityProvider;
 import org.modelexecution.fumldebug.ui.commons.FUMLUICommons;
 import org.modelexecution.fumldebug.ui.commons.provider.ActivityContentProvider;
 import org.modelexecution.fumldebug.ui.commons.provider.ActivityLabelProvider;
 
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 
 public class ActivityMainTab extends AbstractLaunchConfigurationTab {
 
 	private Text resourceText;
 	private Button browseResourceButton;
 	private Collection<Activity> activities = Collections.emptyList();
 	private TreeViewer activityList;
 	private Activity selectedActivity = null;
 
 	public void createControl(Composite parent) {
 		Font font = parent.getFont();
 		Composite comp = createContainerComposite(parent, font);
 		createVerticalSpacer(comp, 3);
 		createResourceSelectionControls(font, comp);
 		createVerticalSpacer(comp, 10);
 		createActivitySelectionControls(font, comp);
 		createVerticalSpacer(comp, 3);
 	}
 
 	private Composite createContainerComposite(Composite parent, Font font) {
 		Composite comp = new Composite(parent, SWT.NONE);
 		setControl(comp);
 		GridLayout topLayout = new GridLayout();
 		topLayout.verticalSpacing = 0;
 		topLayout.numColumns = 3;
 		comp.setLayout(topLayout);
 		comp.setFont(font);
 		return comp;
 	}
 
 	private void createResourceSelectionControls(Font font, Composite comp) {
 		createResourceLabel(font, comp);
 		createResourceTextControl(font, comp);
 		createResourceBrowseButton(comp);
 	}
 
 	private void createResourceBrowseButton(Composite comp) {
 		browseResourceButton = createPushButton(comp, "&Browse", null);
 		browseResourceButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				browseResource();
 			}
 		});
 	}
 
 	private void createResourceTextControl(Font font, Composite comp) {
 		GridData gd;
 		resourceText = new Text(comp, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		resourceText.setLayoutData(gd);
 		resourceText.setFont(font);
 		resourceText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				updateActivities();
 				updateLaunchConfigurationDialog();
 			}
 		});
 	}
 
 	private void createResourceLabel(Font font, Composite comp) {
 		Label programLabel = new Label(comp, SWT.NONE);
 		programLabel.setText("&Resource:");
 		GridData gd = new GridData(GridData.BEGINNING);
 		programLabel.setLayoutData(gd);
 		programLabel.setFont(font);
 	}
 
 	protected void browseResource() {
 		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(
 				getShell(), ResourcesPlugin.getWorkspace().getRoot(),
 				IResource.FILE);
 		dialog.setTitle("Resource");
 		dialog.setMessage("Select a resource to debug");
 		if (dialog.open() == Window.OK) {
 			Object[] files = dialog.getResult();
 			IFile file = (IFile) files[0];
 			resourceText.setText(file.getFullPath().toString());
 		}
 
 	}
 
 	private void createActivitySelectionControls(Font font, Composite comp) {
 		Group group = new Group(comp, SWT.BORDER);
 		group.setText("Select Activity");
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.heightHint = 100;
 		gd.horizontalSpan = 3;
 		group.setLayoutData(gd);
 		GridLayout groupLayout = new GridLayout();
 		group.setLayout(groupLayout);
 		activityList = new TreeViewer(group, SWT.SINGLE | SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.BORDER);
 		activityList.setSorter(new ViewerSorter());
 		activityList.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
 		activityList.setLabelProvider(new ActivityLabelProvider());
 		activityList.setContentProvider(new ActivityContentProvider(true));
 		activityList.setInput(activities.toArray());
 		activityList
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 					@Override
 					public void selectionChanged(SelectionChangedEvent event) {
 						updateSelectedActivity();
 						updateLaunchConfigurationDialog();
 					}
 				});
 	}
 
 	private void updateSelectedActivity() {
 		IStructuredSelection selection = (IStructuredSelection) activityList
 				.getSelection();
 		Object selectedElement = selection.getFirstElement();
 		if (selectedElement instanceof Activity) {
 			selectedActivity = (Activity) selectedElement;
 		} else {
 			selectedActivity = null;
 		}
 	}
 
 	private void updateActivities() {
 		ActivityProviderRegistry activityProviderRegistry = ActivityProviderRegistry
 				.getInstance();
 		IResource iResource = getResource();
 		if (exists(iResource)
 				&& activityProviderRegistry.hasActivityProvider(iResource)) {
 			IActivityProvider activityProvider = activityProviderRegistry
 					.getActivityProvider(iResource);
 			activities = activityProvider.getActivities(iResource);
 		} else {
 			activities = Collections.emptyList();
 		}
 		refreshActivityListViewer();
 	}
 
 	private boolean exists(IResource iResource) {
 		return iResource != null && iResource.exists();
 	}
 
 	private void refreshActivityListViewer() {
 		activityList.setInput(activities.toArray());
 		activityList.refresh(true);
 		selectedActivity = null;
 	}
 
 	protected IResource getResource() {
 		return ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(resourceText.getText());
 	}
 
 	@Override
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 	}
 
 	@Override
 	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (selectedActivity != null) {
 			setErrorMessage(null);
 			setMessage(null);
 			return super.isValid(launchConfig);
		} else if (!resourceText.getText().isEmpty() && activities.size() < 1) {
 			setErrorMessage("Cannot obtain activities from selected resource.");
 			return false;
		} else if (!resourceText.getText().isEmpty() && activities.size() > 0) {
 			setErrorMessage("Select an activity.");
 			return false;
		} else if (resourceText.getText().isEmpty()) {
 			setErrorMessage("Select a resource and an activity.");
 			return false;
 		}
 		return false;
 	}
 
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		String defValResourceText = ""; //$NON-NLS-1$
 		String defValActivityName = ""; //$NON-NLS-1$
 		try {
 			defValResourceText = configuration.getAttribute(
 					FUMLDebuggerPlugin.ATT_RESOURCE, (String) null);
 			defValActivityName = configuration.getAttribute(
 					FUMLDebuggerPlugin.ATT_ACTIVITY_NAME, (String) null);
 		} catch (CoreException e) {
 			setErrorMessage(e.getMessage());
 		}
 
 		resourceText.setText(defValResourceText);
 		updateActivities();
 		Activity activity = getActivityByName(defValActivityName);
 		if (activity != null) {
 			activityList.setSelection(new StructuredSelection(activity));
 		}
 	}
 
 	private Activity getActivityByName(String activityName) {
 		for (Activity activity : activities) {
 			if (activityName.equals(activity.name)) {
 				return activity;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(FUMLDebuggerPlugin.ATT_RESOURCE,
 				resourceText.getText().trim());
 		if (selectedActivity != null) {
 			configuration.setAttribute(FUMLDebuggerPlugin.ATT_ACTIVITY_NAME,
 					selectedActivity.name);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "Activity";
 	}
 
 	@Override
 	public Image getImage() {
 		return FUMLUICommons.getImage(FUMLUICommons.IMG_ACTIVITY);
 	}
 
 }

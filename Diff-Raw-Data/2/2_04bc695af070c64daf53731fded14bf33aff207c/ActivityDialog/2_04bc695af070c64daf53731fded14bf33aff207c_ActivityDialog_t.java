 /*******************************************************************************
  * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     mikael petterson - inital API and implementation
  *     magnus mrtberg - implementation and bugfixing
  *     IBM Corporation - concepts and ideas from Eclipse
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui.dialogs;
 
 import java.util.ArrayList;
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import net.sourceforge.eclipseccase.ui.CommentDialogArea;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author mikael petterson
  * 
  */
 public class ActivityDialog extends Dialog {
 
 	/** trace id */
 	private static final String TRACE_ACTIVITYDIALOG = "ActivityDialog"; //$NON-NLS-1$
 
 	private Button newButton;
 
 	private CommentDialogArea commentDialogArea;
 
 	private ClearCaseProvider provider;
 
 	private String selectedActivity = null;
 
 	private static boolean test = false;
 
 	private IResource resource;
 
 	private ComboViewer comboViewer;
 
 	private ArrayList<String> activities;
 
 	public ActivityDialog(Shell parentShell, ClearCaseProvider provider, IResource resource) {
 		super(parentShell);
 		this.setShellStyle(SWT.CLOSE);
 		this.provider = provider;
 		this.resource = resource;
 		commentDialogArea = new CommentDialogArea(this, null);
 
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("ActivityDialog.title")+" "+resource.getName());
 		Composite composite = new Composite(parent, SWT.NULL);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		composite.setLayout(layout);
 
 		Label descriptionLabel = new Label(composite, SWT.NONE);
 		descriptionLabel.setText(Messages.getString("ActivityDialog.activityDescription")); //$NON-NLS-1$
 		descriptionLabel.setLayoutData(new GridData());
 
 		Label label = new Label(composite, SWT.NONE);
 		label.setText(Messages.getString("ActivityDialog.activity")); //$NON-NLS-1$
 		label.setLayoutData(new GridData());
 		// refresh
 		String viewName = ClearCaseProvider.getViewName(resource);
 		if (viewName == "" && provider == null) {
 			activities = new ArrayList<String>();
 			activities.add("test");
 			activities.add("test2");
 			activities.add("bmn011_quick_bug_fix");
 
 		} else {
 
 			activities = provider.listMyActivities();
 		}
 		comboViewer = createComboViewer(composite, activities.toArray(new String[activities.size()]));
 
 		// if we have activity set as selected otherwise let sorter in list
 		// decide which to set.
 		if (provider != null) {
 			if(activities.contains(ClearCaseProvider.NO_ACTIVITY)){
 				comboViewer.setSelection(new StructuredSelection(activities.get(0)), true);
 			}else{
 				String activity = provider.getCurrentActivity();
 				if(activities.contains(activity)){
 					comboViewer.setSelection(new StructuredSelection(activity), true);
 				}else{
 					//select first,
 					comboViewer.setSelection(new StructuredSelection(activities.get(activities.size() - 1)), true);
 				}
 			}
 		} else {
 			// Don't set any activity.This is for testing.
 			comboViewer.setSelection(new StructuredSelection(), true);
 		}
 		comboViewer.refresh();
 
 		//
 		// // TODO: could this be cached for project.
 		// String headline = provider.getCurrentActivity();
 		// for (String activity : activities) {
 		// // if current activity is in list set it.
 		// if (activity.equalsIgnoreCase(headline)) {
 		// comboViewer.setSelection(new StructuredSelection(activity), true);
 		// comboViewer.refresh();
 		// }
 		//
 		// }
 		// } else {
 		// // Don't set any activity.
 		// comboViewer.setSelection(new StructuredSelection(activities.), true);
 		// comboViewer.refresh();
 		// }
 
 		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				if (ClearCasePlugin.DEBUG_UCM) {
 					ClearCasePlugin.trace(TRACE_ACTIVITYDIALOG, "Selected: " + selection.getFirstElement()); //$NON-NLS-1$
 				}
 				setSelectedActivity((String) (selection.getFirstElement()));
 			}
 		});
 
 		comboViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				if (ClearCasePlugin.DEBUG_UCM) {
 					ClearCasePlugin.trace(TRACE_ACTIVITYDIALOG, "Double Clicked: " + selection.getFirstElement()); //$NON-NLS-1$
 				}
 
 				setSelectedActivity((String) (selection.getFirstElement()));
 			}
 		});
 
 		addButtons(composite);
 
 		commentDialogArea.createArea(composite);
 		commentDialogArea.addPropertyChangeListener(new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty() == CommentDialogArea.OK_REQUESTED) {
 					okPressed();
 				}
 			}
 		});
 		return composite;
 
 	}
 
 	private void addButtons(Composite parent) {
 		Composite buttons = new Composite(parent, SWT.NONE);
 		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.numColumns = 2;
 		buttons.setLayout(layout);
 
 		newButton = new Button(buttons, SWT.PUSH);
 		newButton.setText(Messages.getString("ActivityDialog.newActivity")); //$NON-NLS-1$
 		GridData data = new GridData();
 		data.horizontalAlignment = GridData.FILL;
 		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
 		data.widthHint = Math.max(widthHint, newButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
 		newButton.setLayoutData(data);
 		newButton.setEnabled(true);
 		SelectionListener newListener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// Open new Dialog to add activity.
 				Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 				NewActivityDialog dlg = new NewActivityDialog(activeShell, provider, ActivityDialog.this, resource);
 				if (dlg.open() == Window.OK) {
 					String activityId = dlg.getActivityId();
 					// Get all activities including new added.
 					activities = provider.listMyActivities();
 					comboViewer.setInput(activities);
 					// Select last element i list
 					int indexLastCreated = activities.indexOf(activityId);
 					comboViewer.setSelection(new StructuredSelection(activities.get(indexLastCreated)), true);
 				} else
 					return;
 
 			}
 		};
 		newButton.addSelectionListener(newListener);
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.CANCEL_ID) {
 			// make sure no new activity is set when dialog is cancelled.
 			selectedActivity = null;
 
 		}
 
 		super.buttonPressed(buttonId);
 	}
 
 	protected ComboViewer createComboViewer(Composite composite, String[] activities) {
 		ComboViewer comboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboViewer.setLabelProvider(new ActivityListLabelProvider());
 		comboViewer.setContentProvider(new ArrayContentProvider());
 		comboViewer.setInput(activities);
 		// comboViewer.setSorter(new ViewerSorter() {
 		// @Override
 		// public int compare(Viewer viewer, Object p1, Object p2) {
 		// return ((Activity) p1).getHeadline().compareToIgnoreCase(((Activity)
 		// p2).getHeadline());
 		// }
 		//
 		// });
 
 		return comboViewer;
 	}
 
 	public String getSelectedActivity() {
 		return selectedActivity;
 	}
 
 	public void setSelectedActivity(String selectedActivity) {
 		this.selectedActivity = selectedActivity;
 	}
 
 	public boolean activityExist(String headline) {
 		for (String activity : activities) {
 			// if current activity is in list set it.
 			if (activity.equalsIgnoreCase(headline))
 				return true;
 		}
 
 		return false;
 	}
 
 	// TODO: For testing only.
 	public static void main(String[] args) {
 		Display display = Display.getCurrent();
 		Shell activeShell = new Shell(display);
 		ActivityDialog.setTest(true);
 		ActivityDialog ad = new ActivityDialog(activeShell, null, null);
 		ad.open();
 	}
 
 	public static void setTest(boolean value) {
 		test = value;
 	}
 
 	public static boolean isTest() {
 		return test;
 	}
 
 }

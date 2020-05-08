 /*******************************************************************************
  * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     mikael petterson - inital API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui.dialogs;
 
 import java.util.Map;
 
 import java.util.HashMap;
 
 import net.sourceforge.eclipseccase.Activity;
 
 import org.eclipse.core.resources.IResource;
 
 import java.util.ArrayList;
 import java.util.Date;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.CommentDialogArea;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
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
 
 	private Combo activityCombo;
 
 	private Button newButton;
 
 	private Button oKButton;
 
 	private CommentDialogArea commentDialogArea;
 
 	private ClearCaseProvider provider;
 	//<headline,Activity>
 	private HashMap<String, Activity> activities;
 
 	private static final String NO_ACTIVITY = "NONE";
 
 	private Activity selectedActivity = null;
 
 	private static boolean test = false;
 
 	private IResource resource;
 	 
 
 	public ActivityDialog(Shell parentShell, ClearCaseProvider provider, IResource resource) {
 		super(parentShell);
 		this.setShellStyle(SWT.CLOSE);
 		this.provider = provider;
 		this.resource = resource;
 		commentDialogArea = new CommentDialogArea(this, null);
 		
 
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		getShell().setText(Messages.getString("ActivityDialog.title"));
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
 
 		activityCombo = createCombo(composite);
 
 		initContent();
 
 		activityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		activityCombo.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				System.out.println("Selected index: " + activityCombo.getSelectionIndex() + ", selected item: " + activityCombo.getItem(activityCombo.getSelectionIndex()));
 				String activitySelector = activityCombo.getItem(activityCombo.getSelectionIndex());
 				setSelectedActivity(activities.get(activitySelector));
 				updateOkButtonEnablement(true);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				System.out.println("Default selected index: " + activityCombo.getSelectionIndex() + ", selected item: " + (activityCombo.getSelectionIndex() == -1 ? "<null>" : activityCombo.getItem(activityCombo.getSelectionIndex())));
 				String activitySelector = activityCombo.getItem(activityCombo.getSelectionIndex());
 				setSelectedActivity(activities.get(activitySelector));
 				updateOkButtonEnablement(true);
 			}
 		});
 
 		activityCombo.setFocus();
 
 		addButton(parent);
 		
 		//FIXME: Is code needed?? 
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
 
 	private void addButton(Composite parent) {
 		Composite buttons = new Composite(parent, SWT.NONE);
 		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		buttons.setLayout(layout);
 
 		newButton = new Button(buttons, SWT.PUSH);
 		newButton.setText(Messages.getString("ActivityDialog.newActivity")); //$NON-NLS-1$
 		GridData data = new GridData();
 		data.horizontalAlignment = GridData.FILL;
 		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
 		data.widthHint = Math.max(widthHint, newButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
 		newButton.setLayoutData(data);
 		newButton.setEnabled(true);
 		SelectionListener listener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// Open new Dialog to add activity.
 				Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 				NewActivityDialog dlg = new NewActivityDialog(activeShell, provider, ActivityDialog.this, resource);
 				if (dlg.open() == Window.CANCEL)
 					return;
 				// FIXME: mike 20110407 update list to get new activity
 				initContent();
 
 			}
 		};
 		newButton.addSelectionListener(listener);
 	}
 	
 	
 	private void updateOkButtonEnablement(boolean enabled) {
 		oKButton = getButton(IDialogConstants.OK_ID);
 		if (oKButton != null) {
 			oKButton.setEnabled(enabled);
 		}
 	}
 
 	private void initContent() {
 		
 
 		if (isTest()) {
 			activities = new HashMap<String, Activity>();
 			activities.put("test comment", new Activity("06-Jun-00.17:16:12", "test", "mike", "test comment"));
 			activities.put("another test comment", new Activity("04-Jun-00.17:10:00", "test2", "mike", "another test comment"));
 			activities.put("bmn011_quick_bug_fix", new Activity("2011-06-14T16:16:04+03:00", "bmn011_quick_bug_fix", "bmn011", "bmn011_quick_bug_fix"));
 
 		} else {
 			String viewName = ClearCaseProvider.getViewName(resource);
 			if(viewName != null){
 			activities = provider.listActivities(viewName);
 			}
 		}
 		if (activities.size() == 0) {
 			activityCombo.add(NO_ACTIVITY);
 			activityCombo.select(0);
 			updateOkButtonEnablement(false);
 		} else {
 
 			for (Map.Entry<String, Activity> entry : activities.entrySet()) {
 				activityCombo.add(entry.getValue().getHeadline());
 				activityCombo.setData(entry.getValue().getHeadline(), entry.getValue());
 			}
 			// Make last created selected.
 			if (activities != null) {
 				Activity last = getLastCreatedActvity(activities);
 				if (last != null) {
 					String headline = last.getHeadline();
 					// get index for it
 					int index = activityCombo.indexOf(headline);
 					activityCombo.select(index);
 					setSelectedActivity(last);
 				}
 
 			}
 		}
 
 	}
 
 	/**
 	 * Retrieve last created activity.
 	 * 
 	 * @param activities
 	 * @return
 	 */
 	private Activity getLastCreatedActvity(HashMap<String, Activity> activities) {
 		Activity myLast = null;
 		Date newestDate = null;
 
 		for (Activity activity : activities.values()) {
 			Date activityDate = activity.getDate();
 			if (ClearCasePlugin.DEBUG_UCM) {
 				ClearCasePlugin.trace(TRACE_ACTIVITYDIALOG, "Date: " + activityDate.getTime()); //$NON-NLS-1$
 			}
 			if (newestDate == null) {
 				newestDate = activityDate;
 			}
 			int results = newestDate.compareTo(activityDate);
 
 			if (results < 0) {
 				// newestTime is before activityTime
 				newestDate = activityDate;
 				myLast = activity;
 			}
 		}
 
 		return myLast;
 
 	}
 	
 	protected void buttonPressed(int buttonId) {
         if (buttonId == IDialogConstants.CANCEL_ID) {
         	//make sure no new activity is set when dialog is cancelled.
            selectedActivity = null;
         }
         super.buttonPressed(buttonId);
     }
 
 	/*
 	 * Utility method that creates a combo box
 	 * 
 	 * @param parent the parent for the new label
 	 * 
 	 * @return the new widget
 	 */
 	protected Combo createCombo(Composite parent) {
 		Combo combo = new Combo(parent, SWT.READ_ONLY);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
 		combo.setLayoutData(data);
 		return combo;
 	}
 
 	/**
 	 * Returns the comment.
 	 * 
 	 * @return String
 	 */
 	public String getComment() {
 		return commentDialogArea.getComment();
 	}
 
 	public Activity getSelectedActivity() {
 		return selectedActivity;
 	}
 
 	public void setSelectedActivity(Activity selectedActivity) {
 		this.selectedActivity = selectedActivity;
 	}
 
 	public boolean activityExist(String headline) {
 		for (int i = 0; i < activities.size(); i++) {
 			Activity currentActivity = activities.get(i);
 			if (null == currentActivity) {
 				return false;
 			}
 			if (currentActivity.getHeadline().equalsIgnoreCase(headline)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// TODO: For testing only.
 	public static void main(String[] args) {
 		Display display = Display.getCurrent();
 		Shell activeShell = new Shell(display);
 		ActivityDialog ad = new ActivityDialog(activeShell, null, null);
 		ad.open();
 	}
 
 	public void setTest(boolean value) {
 		test = value;
 	}
 
 	public boolean isTest() {
 		return test;
 	}
 
 }

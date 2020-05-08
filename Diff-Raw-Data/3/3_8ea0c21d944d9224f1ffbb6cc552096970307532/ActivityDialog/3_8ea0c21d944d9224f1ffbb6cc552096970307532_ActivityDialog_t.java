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
 
 	private ArrayList<Activity> activities;
 
 	private static final String NO_ACTIVITY = "NONE";
 
 	private Activity selectedActivity = null;
 
 	private boolean test = false;
 
 	public ActivityDialog(Shell parentShell, ClearCaseProvider provider) {
 		super(parentShell);
 		this.setShellStyle(SWT.CLOSE);
 		this.provider = provider;
 		commentDialogArea = new CommentDialogArea(this, null);
 		initContent();
 
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
 		if (activities.size() == 0) {
 			activityCombo.add(NO_ACTIVITY);
 			activityCombo.select(0);
 		} else {
 			for (int i = 0; i < activities.size(); i++) {
 				activityCombo.add(activities.get(i).getHeadline());
 			}
 			if (provider != null) {
 				// Select last create
 				Activity myLastCreatedAct = getLastCreatedActvity(activities);
 				int index = activities.indexOf(myLastCreatedAct);
 				activityCombo.select(index);
 			}
 		}
 
 		activityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		activityCombo.addListener(SWT.Modify, new Listener() {
 			public void handleEvent(Event e) {
 				((Combo) e.widget).getText();
 				System.out.println("Debug selected: " + ((Combo) e.widget).getText());
 				for (int i = 0; i < activities.size(); i++) {
 					Activity currentActivity = activities.get(i);
 					if (currentActivity.getHeadline().equalsIgnoreCase(((Combo) e.widget).getText())) {
 						setSelectedActivity(currentActivity);
 						updateOkButtonEnablement(true);
 					}
 				}
 
 			}
 		});
 
 		activityCombo.setFocus();
 
 		addButton(parent);
 
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
 				NewActivityDialog dlg = new NewActivityDialog(activeShell, provider);
 				if (dlg.open() == Window.CANCEL)
 					return;
 				// FIXME: mike 20110407 update list to get new activity
 				initContent();
 
 			}
 		};
 		newButton.addSelectionListener(listener);
 	}
 
 	/*
	 * Sets OK button is disabled when NO_ACTVITY is selected. To enable
	 * OK button create a new activity or select one from list. 
 	 * 
 	 * @see
 	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
 	 * .swt.widgets.Composite)
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		super.createButtonsForButtonBar(parent);
 		// Update the button enablement only after the button is created
 		oKButton = getButton(IDialogConstants.OK_ID);
 		if (activityCombo.getSelectionIndex() == 0) {
 			updateOkButtonEnablement(false);
 			return;
 		}
 
 	}
 
 	private void updateOkButtonEnablement(boolean enabled) {
 		if (oKButton != null) {
 			oKButton.setEnabled(enabled);
 		}
 	}
 
 	private void initContent() {
 		if (provider != null) {
 			activities = provider.listActivities();
 
 		} else {
 			activities = new ArrayList<Activity>();
 		}
 
 	}
 
 	/**
 	 * Retrieve last created activity.
 	 * 
 	 * @param activities
 	 * @return
 	 */
 	private Activity getLastCreatedActvity(ArrayList<Activity> activities) {
 		Activity myLast = null;
 		Date newestDate = null;
 		for (int i = 0; i < activities.size(); i++) {
 			Activity currentActivity = activities.get(i);
 			Date activityDate = currentActivity.getDate();
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
 				myLast = currentActivity;
 			}
 
 		}
 
 		return myLast;
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
 
 	public ArrayList<Activity> getActivities() {
 		return activities;
 	}
 
 	public void setActivities(ArrayList<Activity> activities) {
 		this.activities = activities;
 	}
 
 	// TODO: For testing only.
 	public static void main(String[] args) {
 		Display display = Display.getCurrent();
 		Shell activeShell = new Shell(display);
 		ActivityDialog ad = new ActivityDialog(activeShell, null);
 		ad.open();
 	}
 
 }

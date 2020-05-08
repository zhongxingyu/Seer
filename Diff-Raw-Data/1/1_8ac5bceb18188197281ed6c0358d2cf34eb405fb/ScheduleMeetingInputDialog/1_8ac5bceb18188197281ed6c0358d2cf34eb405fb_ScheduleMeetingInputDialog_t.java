 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to fill in the meeting request information
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.mail.smtp.mailVersion.internal.dialogs;
 
 import java.text.SimpleDateFormat;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.mail.smtp.SmtpPlugin;
 import org.eclipse.mylyn.reviews.r4e.mail.smtp.mailVersion.internal.MailInputValidator;
 import org.eclipse.mylyn.reviews.r4e.mail.smtp.mailVersion.internal.SMTPHostString;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.FormDialog;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ScheduleMeetingInputDialog extends FormDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field MEETING_DIALOG_TITLE.
 	 * (value is ""Meeting Information"")
 	 */
 	private static final String MEETING_DIALOG_TITLE = "Meeting Information";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_MSG.
 	 * (value is ""Enter Meeting Information"")
 	 */
 	private static final String BASIC_PARAMS_HEADER_MSG = "Enter Meeting Information";
 
 	/**
 	 * Field SIMPLE_DATE_FORMAT.
 	 * (value is ""yyyy/MM/dd"")
 	 */
 	public static final String SIMPLE_DATE_FORMAT = "yyyy-MMM-dd HH:mm";
 
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field fStartTime.
 	 */
     protected Long fStartTime = null;
     
 	/**
 	 * Field fCalendarButton.
 	 */
 	protected Button fCalendarButton = null;
 	
 	/**
 	 * Field fDuration.
 	 */
     private Integer fDuration = null;
     
 	/**
 	 * Field fLocation.
 	 */
     private String fLocation = null;
     
 	/**
 	 * Field fStartTimeInputTextField.
 	 */
     protected Text fStartTimeInputTextField = null;
     
 	/**
 	 * Field fDurationInputTextField.
 	 */
     private Text fDurationInputTextField = null;
     
 	/**
 	 * Field fLocationInputTextField.
 	 */
     private Text fLocationInputTextField = null;
     
     /**
      * The input validator, or <code>null</code> if none.
      */
     private final IInputValidator fValidator;
     
     
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
     
 	/**
 	 * Constructor for ScheduleMeetingInputDialog.
 	 * @param aParentShell Shell
 	 */
 	public ScheduleMeetingInputDialog(Shell aParentShell) {
 		super(aParentShell);
 		setBlockOnOpen(true);
 		fValidator = new MailInputValidator();
 	}
 	
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
     /**
      * Method buttonPressed.
      * @param buttonId int
      * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
      */
 	@Override
 	protected void buttonPressed(int buttonId) {
         if (buttonId == IDialogConstants.OK_ID) {
         	
         	//NOTE fStartTime is set below
         	//Validate StartTime
         	String validateResult = validateEmptyInput(fDurationInputTextField);
         	if (null != validateResult) {
         		
         		//Validation of input failed
     			final ErrorDialog dialog = new ErrorDialog(null, 
     					SMTPHostString.getString("dialog_title_error"), 
     					SMTPHostString.getString("duration_Error"),
         				new Status(IStatus.ERROR, 
         						SmtpPlugin.FPLUGIN_ID, 0, validateResult, null), IStatus.ERROR);
     			dialog.open();
     			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
     			return;
         	}
         	fDuration = Integer.valueOf(fDurationInputTextField.getText());
         	
         	//Validate Location
         	validateResult = validateEmptyInput(fLocationInputTextField);
         	if (null != validateResult) {
         		//Validation of input failed
     			final ErrorDialog dialog = new ErrorDialog(null, 
     					SMTPHostString.getString("dialog_title_error"),
     					SMTPHostString.getString("location_Error"),
         				new Status(IStatus.ERROR, 
         						SmtpPlugin.FPLUGIN_ID, 0, validateResult, null), IStatus.ERROR);
     			dialog.open();
     			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
     			return;
         	}
         	fLocation = fLocationInputTextField.getText();   	
          } else {
         	 fStartTime = null;
         	 fDuration = null;
         	 fLocation = null;
         }
 		this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
         super.buttonPressed(buttonId);
     }
     
     /**
      * Method configureShell.
      * @param shell Shell
      * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
      */
     @Override
 	protected void configureShell(Shell shell) {
         super.configureShell(shell);
         shell.setText(MEETING_DIALOG_TITLE);
     }
     
 	/**
 	 * Configures the dialog form and creates form content. Clients should
 	 * override this method.
 	 * 
 	 * @param mform
 	 *            the dialog form
 	 */
 	@Override
 	protected void createFormContent(final IManagedForm mform) {
 
 		final FormToolkit toolkit = mform.getToolkit();
 		final ScrolledForm sform = mform.getForm();
 		sform.setExpandVertical(true);
 		final Composite composite = sform.getBody();
 		final GridLayout layout = new GridLayout(1, false);
 		composite.setLayout(layout);
         
 		//Basic parameters section
         final Section basicSection = toolkit.createSection(composite, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR |
         		  ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
         final GridData basicSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
         basicSectionGridData.horizontalSpan = 4;
         basicSection.setLayoutData(basicSectionGridData);
         
         
         basicSection.setText(SMTPHostString.getString("basic_parameter"));
         basicSection.setDescription(BASIC_PARAMS_HEADER_MSG);
         basicSection.addExpansionListener(new ExpansionAdapter()
 		{
 			@Override
 			public void expansionStateChanged(ExpansionEvent e){
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
         final Composite basicSectionClient = toolkit.createComposite(basicSection);
         basicSectionClient.setLayout(layout);
         basicSection.setClient(basicSectionClient);
         
 	    //Meeting composite
         final Composite meetingComposite = toolkit.createComposite(basicSectionClient);
 	    GridData textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		textGridData.horizontalSpan = 3;
 		meetingComposite.setLayoutData(textGridData);
 		meetingComposite.setLayout(new GridLayout(4, false));
 		
 		//Meeting Start Time
 	    final Label meetingStartTimeLabel = toolkit.createLabel(meetingComposite, 
 	    		SMTPHostString.getString("start_time_label"));
 	    meetingStartTimeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 	    fStartTimeInputTextField = toolkit.createText(meetingComposite, "", SWT.BORDER);
 	    textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 	    textGridData.horizontalSpan = 2;
 	    fStartTimeInputTextField.setLayoutData(textGridData);
 	    
 	    //Calendar Button
 	    fCalendarButton = toolkit.createButton(meetingComposite, "...", SWT.NONE);
 	    fCalendarButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 	    fCalendarButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 //				final CalendarDialog dialog = new CalendarDialog(R4EUIModelController.getNavigatorView().
 //						getSite().getWorkbenchWindow().getShell(), true);
 				final CalendarDialog dialog = new CalendarDialog(getShell(), true);
 		    	final int result = dialog.open();
 		    	if (result == Window.OK) {
 		    		final SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);	
 		    		fStartTimeInputTextField.setText(dateFormat.format(dialog.getDate()));
 		    		fStartTime = Long.valueOf(dialog.getDate().getTime());
 					getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		    	}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				// No implementation needed
 			}
 		});
 	    
 		//Meeting Duration
 	    final Label meetingEndTimeLabel = toolkit.createLabel(meetingComposite, SMTPHostString.getString("duration_label") );
 	    meetingEndTimeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 	    fDurationInputTextField = toolkit.createText(meetingComposite, "", SWT.BORDER);
 	    textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 	    textGridData.horizontalSpan = 2;
 	    fDurationInputTextField.setLayoutData(textGridData);
 	    toolkit.createLabel(meetingComposite, "");  //dummy label for alignment purposes
 
 		//Meeting Location
 	    final Label meetingLocationLabel = toolkit.createLabel(meetingComposite,
 	    		SMTPHostString.getString("location_label"));
 	    meetingLocationLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 	    fLocationInputTextField = toolkit.createText(meetingComposite, "", SWT.BORDER);
 	    textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 	    textGridData.horizontalSpan = 2;
 	    fLocationInputTextField.setLayoutData(textGridData);
 	    toolkit.createLabel(meetingComposite, "");  //dummy label for alignment purposes
     }
     
 	/**
 	 * Method isResizable.
 	 * @return boolean
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
     
     /**
      * Method getStartTime
      * @return Long
      */
     public Long getStartTime() {
         return fStartTime;
     }
     
     /**
      * Method getDuration
      * @return Integer
      */
     public Integer getDuration() {
         return fDuration;
     }
     
     /**
      * Method getLocation
      * @return String
      */
     public String getLocation() {
         return fLocation;
     }
     
     /**
      * Method validateEmptyInput.
      * @param aText Text
      * @return String
      */
     private String validateEmptyInput(Text aText) {
         if (null != fValidator) {
             return fValidator.isValid(aText.getText());
         }
         return null;
     }
     
     /**
      * Method setStartTime
      * @param aSt Long
      */
     public void setStartTime(Long aSt) {
 		final SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);	
 		fStartTimeInputTextField.setText(dateFormat.format(aSt));
		fStartTime = aSt;
     }
     
     /**
      * Method setDuration
      * @param aDuration Long
      */
     public void setDuration(Integer aDuration) {
     	fDurationInputTextField.setText(aDuration.toString());
     }
     
     /**
      * Method setLocation
      * @param aLoc String
      */
     public void setLocation(String aLoc) {
     	fLocationInputTextField.setText(aLoc);
     }
 
 }

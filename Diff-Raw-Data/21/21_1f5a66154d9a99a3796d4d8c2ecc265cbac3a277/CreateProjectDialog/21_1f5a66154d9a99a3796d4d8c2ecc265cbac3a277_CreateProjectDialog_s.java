 package edu.cmu.square.client.ui.ManageSite;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.cmu.square.client.model.GwtAsquareCase;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.client.utils.SquareUtil;
 
 /**
  * A dialog used to get information from the user for categories.
  * 
  * This dialog provides the dialog and talks back as used in the
  * caller-dialog-talkback pattern used by Manage Site.
  * 
  */
 public class CreateProjectDialog extends DialogBox
 {
 	private final TextBox projectTextBox = new TextBox();
 
 	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
 	private SuggestBox userSuggestBox = new SuggestBox(oracle);
 
 	private RadioButton case1Radiobutton = new RadioButton("projectcase", "Case 1");
 	private RadioButton case3Radiobutton = new RadioButton("projectcase", "Case 3");
 	
 	private ProjectGrid caller = null;
 
 	private List<GwtProject> listOfProjects = null;
 	private List<GwtUser> listOfUsers = null;
 
 	final ManageSitePaneMessages messages = (ManageSitePaneMessages) GWT.create(ManageSitePaneMessages.class);
 
 	private Button saveButton;
 
 	/**
 	 * Creates a new dialog box with a talk back pointer to the given category
 	 * grid.
 	 * 
 	 * @param gridPointer
 	 *            The category grid to which this dialog should talk back.
 	 */
 	public CreateProjectDialog(List<GwtProject> projects, List<GwtUser> users, ProjectGrid caller)
 		{
 			super();
 
 			this.listOfProjects = projects;
 			this.listOfUsers = users;
 			this.caller = caller;
 			this.initializeDialog(new GwtProject());
 		}
 
 	public CreateProjectDialog(int projectId, String projectName, int leadRequirementId, List<GwtProject> projects, List<GwtUser> users,
 			ProjectGrid caller, int casesId)
 		{
 			super();
 
 			this.listOfProjects = projects;
 			this.listOfUsers = users;
 			this.caller = caller;
 
 			this.initializeDialog(new GwtProject(projectId, projectName, leadRequirementId, casesId));
 		}
 
 	/**
 	 * Sets up the controls in the dialog
 	 * 
 	 * @param project
 	 *            The category to be updated in this dialog.
 	 */
 	private void initializeDialog(GwtProject project)
 	{
 		
 	
 		this.setAnimationEnabled(true);
 
 		loadUserToSuggestionBox(listOfUsers);
 
 		VerticalPanel nameLayout = new VerticalPanel();
 		VerticalPanel leaderLayout = new VerticalPanel();
 		VerticalPanel defaultTermsLayout = new VerticalPanel();
 		HorizontalPanel buttonsLayout = new HorizontalPanel();
 		VerticalPanel caseLayout = new VerticalPanel();
 		HorizontalPanel caseChoice = new HorizontalPanel();
 		
 		VerticalPanel baseLayout = new VerticalPanel();
 		baseLayout.setSpacing(10);
 		baseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		baseLayout.add(nameLayout);
 		baseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		baseLayout.add(caseLayout);
 		baseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		baseLayout.add(leaderLayout);
 		baseLayout.add(defaultTermsLayout);
 		baseLayout.add(buttonsLayout);
 
 
 		nameLayout.add(new Label(messages.projectName()));
 		nameLayout.add(this.projectTextBox);
 
 		this.projectTextBox.setWidth("500px");
 		this.userSuggestBox.setWidth("500px");
 
 		leaderLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		leaderLayout.add(new Label(messages.leadRequirementsEngineer() + ":"));
 		leaderLayout.add(this.userSuggestBox);
 		
		caseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		caseLayout.add(new Label(messages.ASquareCase() + ":"));
		caseLayout.add(caseChoice);
		
		caseChoice.add(this.case1Radiobutton);
		caseChoice.add(this.case3Radiobutton);
 
 		this.case1Radiobutton.setValue(true);
 		
 		if (project.isInDatabase())// Is an update
 		{
 			this.setText(messages.updateProjectDialogBoxTitle());
 			//GwtUser newLeadRequirementEngineer = getUserById(project.getLeadRequirementEngineer().getUserId());
 			GwtUser newAcqusitionOrganizationEngineer = getUserById(project.getAcquisitionOrganizationEngineer().getUserId());
 			
 			projectTextBox.setText(project.getName());
 			//userSuggestBox.setText(getSuggestionFormat(newLeadRequirementEngineer.getFullName(), newLeadRequirementEngineer.getUserName()));
 			userSuggestBox.setText(getSuggestionFormat(newAcqusitionOrganizationEngineer.getFullName(), newAcqusitionOrganizationEngineer.getUserName()));
 		}
 		else // Is a create action
 		{
 			this.setText(messages.createProjectDialogBoxTitle());
 			defaultTermsLayout.setWidth("100%");
 			defaultTermsLayout.setSpacing(5);
 			defaultTermsLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			
 			
 		}
 
 		saveButton = new Button(messages.save(), new SaveHandler(this, project));
 
 		// Set up the buttons
 		Button cancelButton = new Button(messages.cancel(), new CancelHandler(this));
 
 		saveButton.setEnabled(false);
 		this.projectTextBox.addKeyUpHandler(new squareKeyUpHandler());
 		this.projectTextBox.addChangeHandler(new squareChangeHandler());
 		this.projectTextBox.addKeyDownHandler(new squareKeyDownHandler());
 
 		this.userSuggestBox.addKeyUpHandler(new squareKeyUpHandler());
 		this.userSuggestBox.addKeyDownHandler(new squareKeyDownHandler());
 
 		saveButton.setWidth("100px");
 		cancelButton.setWidth("100px");
 
 		buttonsLayout.setSpacing(10);
 		buttonsLayout.add(saveButton);
 		buttonsLayout.add(cancelButton);
 
 		// set the base layout
 		this.setWidget(baseLayout);
 	}
 
 	private void loadUserToSuggestionBox(final List<GwtUser> result)
 	{
 		oracle.clear();
 
 		for (GwtUser u : result)
 		{
 			String suggestion = getSuggestionFormat(u.getFullName(), u.getUserName());
 			oracle.add(suggestion);
 		}
 	}
 
 	private String getSuggestionFormat(String fullName, String userName)
 	{
 		return fullName + "<" + userName + ">";
 	}
 
 	private String getUserName()
 	{
 		String user = userSuggestBox.getText();
 		if (user.indexOf('<') == -1 || user.indexOf('>') == -1)
 		{
 			return user;
 		}
 		else
 		{
 			return user.substring(user.indexOf('<') + 1, user.indexOf('>'));
 		}
 	}
 
 	private void configureButton()
 	{
 		if (projectTextBox.getText().trim().length() == 0 || this.userSuggestBox.getText().trim().length() == 0)
 		{
 			saveButton.setEnabled(false);
 		}
 		else
 		{
 			saveButton.setEnabled(true);
 		}
 	}
 
 	private GwtUser getUserByUserName(String userName)
 	{
 		for (GwtUser user : listOfUsers)
 		{
 			if (user.getUserName().equals(userName))
 			{
 				return user;
 			}
 
 		}
 
 		return null;
 	}
 	private GwtUser getUserById(int userId)
 	{
 		for (GwtUser user : listOfUsers)
 		{
 			if (user.getUserId().intValue() == userId)
 			{
 				return user;
 			}
 
 		}
 
 		return null;
 	}
 
 	private class SaveHandler implements ClickHandler
 	{
 		private CreateProjectDialog dialog = null;
 		private GwtProject project = null;
 
 		public SaveHandler(CreateProjectDialog dialogPointer, GwtProject newProject)
 			{
 				super();
 				this.dialog = dialogPointer;
 				this.project = newProject;
 			}
 
 		public void onClick(ClickEvent event)
 		{
 			// set the new project's name
 			project.setName(SquareUtil.firstCharacterToUpperCase(projectTextBox.getText().trim()));
 
 			if(case1Radiobutton.getValue())
 				project.setCases(new GwtAsquareCase(1));
 			else if(case3Radiobutton.getValue())
 				project.setCases(new GwtAsquareCase(3));
 			
 			// Check to make sure this project name does not already exist in
 			// the DB.
 			for (GwtProject currentProject : listOfProjects)
 			{
 				if (this.project.getName().equalsIgnoreCase(currentProject.getName()) && this.project.getId().intValue() != currentProject.getId().intValue())
 				{
 					Window.alert(messages.createProjectDialogBoxAlreadyExist());
 					return;
 				}
 			}
 
 			if (getUserByUserName(getUserName()) == null)
 			{
 				Window.alert(messages.createProjectDialogBoxBadUser());
 				return;
 			}
 
 			if (project.isInDatabase())
 			{
 				caller.updateProject(project.getId(), getUserByUserName(getUserName()).getUserId(), project.getName());
 			}
 			else
 			{
 				project.setSecurity(true);
 				project.setPrivacy(false);
 				project.setLite(false);
 				project.setAcquisitionOrganizationEngineer(getUserByUserName(getUserName()));
 				caller.createProject(project);
 			}
 
 			this.dialog.hide();
 		}
 	}
 
 	private class CancelHandler implements ClickHandler
 	{
 		private CreateProjectDialog dialog = null;
 
 		public CancelHandler(CreateProjectDialog dialog)
 			{
 				super();
 				this.dialog = dialog;
 			}
 
 		public void onClick(ClickEvent event)
 		{
 			this.dialog.hide(true);
 		}
 	}
 
 	private class squareKeyUpHandler implements KeyUpHandler
 	{
 
 		public void onKeyUp(KeyUpEvent event)
 		{
 			configureButton();
 
 		}
 	}
 
 	private class squareChangeHandler implements ChangeHandler
 	{
 
 		public void onChange(ChangeEvent event)
 		{
 			configureButton();
 
 		}
 	}
 
 	private class squareKeyDownHandler implements KeyDownHandler
 	{
 
 		public void onKeyDown(KeyDownEvent event)
 		{
 			configureButton();
 
 		}
 	}
 
 }

 package com.cs1530.group4.addendum.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.cs1530.group4.addendum.shared.Achievement;
 import com.cs1530.group4.addendum.shared.Post;
 import com.cs1530.group4.addendum.shared.UserProfile;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * This represents the UI for a user's profile page. It lists their personal
  * information and their achievements.
  */
 public class Profile extends Composite
 {
 
 	/** The a static instance of the service used for RPC calls. */
 	UserServiceAsync userService = UserService.Util.getInstance();
 
 	/** The username associated with this profile */
 	String username;
 
 	/** The application's MainView. */
 	MainView main;
 
 	/** The offset of post results to fetch from. */
 	int startIndex = 0;
 
 	/** The nextPage anchor. */
 	Anchor nextPage;
 
 	/** The prevPage anchor. */
 	Anchor prevPage;
 
 	/** The profile of the user. */
 	UserProfile userProfile;
 
 	/** The main widget of this view. */
 	VerticalPanel mainPanel;
 
 	/**
 	 * View profile as public or private (only used if logged in user == user
 	 * profile).
 	 */
 	boolean viewProfileAsPrivate = true;
 
 	/**
 	 * Instantiates a new Profile.
 	 * 
 	 * @param m
 	 *            a reference to the application's {@link MainView}
 	 * @param u
 	 *            the username associated with this profile
 	 * 
 	 * @custom.accessed None
 	 * @custom.changed None
 	 * @custom.called None
 	 */
 	public Profile(MainView m, String u, boolean privateProfile)
 	{
 		main = m;
 		username = u;
 		viewProfileAsPrivate = privateProfile;
 		mainPanel = new VerticalPanel();
 		mainPanel.setStyleName("profileBackground");
 		initWidget(mainPanel);
 		
 		Button backButton = new Button("Back to Stream");
 		backButton.setStyleName("ADCButton");
 		backButton.getElement().getStyle().setProperty("marginBottom", "10px");
 		backButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				main.setContent(new Stream(main),"stream");
 			}
 		});
 		mainPanel.add(backButton);
 
 		setUserProfile(username);
 	}
 
 	private void setupTabs(VerticalPanel verticalPanel)
 	{
 		final TabPanel tabPanel = new TabPanel();
 		tabPanel.setStyleName("profileTabPanel .gwt-TabPanel");
 		verticalPanel.add(tabPanel);
 		tabPanel.setWidth("800px");
 
 		VerticalPanel aboutPanel = new VerticalPanel();
 		tabPanel.add(aboutPanel, "About", false);
 
 		if(username.equals(Cookies.getCookie("loggedIn")))
 		{
 			HorizontalPanel viewAsPanel = new HorizontalPanel();
 			viewAsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 
 			Label viewLabel = new Label("View profile as: ");
 			viewAsPanel.add(viewLabel);
 
 			final ListBox listBox = new ListBox();
 			listBox.addItem("Yourself");
 			listBox.addItem("Public");
 			if(!viewProfileAsPrivate)
 				listBox.setSelectedIndex(1);
 			listBox.addChangeHandler(new ChangeHandler()
 			{
 				@Override
 				public void onChange(ChangeEvent event)
 				{
 					if(listBox.getSelectedIndex() == 0)
 						viewProfileAsPrivate = true;
 					else
 						viewProfileAsPrivate = false;
 
 					main.setContent(new Profile(main, username, viewProfileAsPrivate), "profile-" + username);
 				}
 			});
 			viewAsPanel.add(listBox);
 
 			VerticalPanel centeringPanel = new VerticalPanel();
 			centeringPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			centeringPanel.setWidth("100%");
 			centeringPanel.add(viewAsPanel);
 			aboutPanel.add(centeringPanel);
 		}
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		horizontalPanel.setSpacing(10);
 		aboutPanel.add(horizontalPanel);
 
 		Image profileImage = new Image("/addendum/getImage?username=" + username);
 		VerticalPanel vPanel = new VerticalPanel();
 		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPanel.setSpacing(5);
 		vPanel.add(profileImage);
		if(username.equals(Cookies.getCookie("loggedIn")) && viewProfileAsPrivate)
 		{
 			Anchor changePassword = new Anchor("Change Password");
 			changePassword.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					new NewPasswordDialog(username,main,false);
 				}
 			});
 			vPanel.add(changePassword);
 		}
 		horizontalPanel.add(vPanel);
 		profileImage.setSize("256px", "256px");
 
 		ScrollPanel toGetCase = new ScrollPanel();
 		toGetCase.setSize("504px", "256px");
 		getTrophies(toGetCase,false);
 		ScrollPanel trophyCase = new ScrollPanel();
 		trophyCase.setSize("504px", "256px");
 		getTrophies(trophyCase,true);
 		
 		TabPanel trophyTabs = new TabPanel();
 		trophyTabs.setStyleName("profileTabPanel .gwt-TabPanel");
 		trophyTabs.setSize("504px","256px");
 		trophyTabs.add(trophyCase, "Earned");
 		trophyTabs.add(toGetCase, "Un-Earned");
 		trophyTabs.selectTab(0);
 		horizontalPanel.add(trophyTabs);
 
 		VerticalPanel infoPanel = new VerticalPanel();
 		aboutPanel.add(infoPanel);
 		infoPanel.setSpacing(20);
 		infoPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		infoPanel.setWidth("100%");
 		
 		HorizontalPanel namePanel = new HorizontalPanel();
 		if(userProfile.getName() != null)
 		{
 			Label nameLabel = new Label(userProfile.getName() + ",");
 			nameLabel.setStyleName("profileUserLabel");
 			namePanel.add(nameLabel);
 		}
 		Label usernameLabel = new Label(username);
 		usernameLabel.setStyleName("profileUserLabel");
 		namePanel.add(usernameLabel);
 		infoPanel.add(namePanel);
 
 		HorizontalPanel infoRow1 = new HorizontalPanel();
 		infoRow1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		infoPanel.add(infoRow1);
 
 		setupStoryPanel(infoRow1);
 		setupEducationPanel(infoRow1);
 
 		HorizontalPanel infoRow2 = new HorizontalPanel();
 		infoRow2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		infoPanel.add(infoRow2);
 
 		setupBasicInfoPanel(infoRow2);
 		setupContactInfoPanel(infoRow2);
 
 		final VerticalPanel postPanel = new VerticalPanel();
 		tabPanel.add(postPanel, "Posts", false);
 		setupPostsPanel(tabPanel, postPanel);
 
 		tabPanel.selectTab(0);
 
 		tabPanel.addSelectionHandler(new SelectionHandler<Integer>()
 		{
 			@Override
 			public void onSelection(SelectionEvent<Integer> event)
 			{
 				if(tabPanel.getTabBar().getTabHTML(event.getSelectedItem()).equals("Posts"))
 					postSearch("username:" + username, postPanel);
 			}
 		});
 	}
 
 	private void getTrophies(ScrollPanel trophyCase, final boolean earned)
 	{
 		trophyCase.clear();
 		VerticalPanel vPanel = new VerticalPanel();
 		vPanel.setWidth("100%");
 		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		Label trophyLabel = new Label("Trophy Case");
 		trophyLabel.setStyleName("profileUserLabel");
 		vPanel.add(trophyLabel);
 		vPanel.add(new HTML("<hr />"));
 		final FlowPanel flowPanel = new FlowPanel();
 		vPanel.add(flowPanel);
 		trophyCase.add(vPanel);
 
 		AsyncCallback<ArrayList<Achievement>> callback = new AsyncCallback<ArrayList<Achievement>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{}
 
 			@Override
 			public void onSuccess(ArrayList<Achievement> results)
 			{
 				for(Achievement achievement : results)
 				{
 					VerticalPanel panel = new VerticalPanel();
 					panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 					panel.setSpacing(5);
 					Image trophy = new Image("/images/trophy.png");
 					if(!earned)
 						trophy.setStyleName("unearnedTrophy");
 					trophy.setTitle(achievement.getDescriptionText());
 					panel.add(trophy);
 					Label label = new Label(achievement.getName());
 					panel.add(label);
 					panel.getElement().getStyle().setFloat(Style.Float.LEFT);
 					flowPanel.add(panel);
 				}
 			}
 		};
 		
 		if(earned)
 			userService.getAchievements(username,callback);
 		else
 			userService.getUnearnedAchievements(username, callback);
 	}
 
 	private void setupPostsPanel(TabPanel tabPanel, final VerticalPanel postPanel)
 	{
 		nextPage = new Anchor("Next 10 Posts");
 		nextPage.setStyleName("courseAnchor");
 		nextPage.setVisible(false);
 		nextPage.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				prevPage.setVisible(true);
 				startIndex += 10;
 				postSearch("username:" + username, postPanel);
 			}
 		});
 		prevPage = new Anchor("Prev 10 Posts");
 		prevPage.setStyleName("courseAnchor");
 		prevPage.setVisible(false);
 		prevPage.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				nextPage.setVisible(true);
 				startIndex -= 10;
 				if(startIndex < 10)
 					prevPage.setVisible(false);
 
 				postSearch("username:" + username, postPanel);
 			}
 		});
 
 		VerticalPanel centerPanel = new VerticalPanel();
 		postPanel.add(centerPanel);
 		centerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		HorizontalPanel nextPrevPanel = new HorizontalPanel();
 		nextPrevPanel.add(prevPage);
 		nextPrevPanel.add(nextPage);
 		centerPanel.add(nextPrevPanel);
 		centerPanel.setWidth("100%");
 	}
 
 	private void setupContactInfoPanel(HorizontalPanel infoRow2)
 	{
 		boolean privateProfile = false;
 		if(username.equals(Cookies.getCookie("loggedIn")) && viewProfileAsPrivate)
 			privateProfile = true;
 		if(!privateProfile && userProfile.getPhone() == null && userProfile.getEmail() == null && userProfile.getAddress() == null)
 			return;
 
 		final PromptedTextBox phoneBox = new PromptedTextBox("Phone number", "promptText");
 		final PromptedTextBox emailBox = new PromptedTextBox("Email address", "promptText");
 		final PromptedTextBox addressBox = new PromptedTextBox("Street address", "promptText");
 
 		VerticalPanel contactInfoPanel = new VerticalPanel();
 		contactInfoPanel.setStyleName("contactInfoPanel");
 		contactInfoPanel.setSpacing(15);
 		infoRow2.add(contactInfoPanel);
 		contactInfoPanel.setWidth("280px");
 
 		Label lblContactInfo = new Label("Contact Information");
 		lblContactInfo.setStyleName("largerText");
 		contactInfoPanel.add(lblContactInfo);
 
 		FlexTable flexTable = new FlexTable();
 		contactInfoPanel.add(flexTable);
 		int numRows = 0;
 
 		if(privateProfile)
 		{
 			if(userProfile.getPhone() != null)
 				phoneBox.setText(userProfile.getPhone());
 			Label phoneLabel = new Label("Phone Number");
 			phoneLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, phoneLabel);
 			flexTable.setWidget(numRows, 1, phoneBox);
 			numRows++;
 		}
 		else if(userProfile.getPhone() != null)
 		{
 			Label infoLabel = new Label(userProfile.getPhone());
 			Label phoneLabel = new Label("Phone Number");
 			phoneLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, phoneLabel);
 			flexTable.setWidget(numRows, 1, infoLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			if(userProfile.getEmail() != null)
 				emailBox.setText(userProfile.getEmail());
 			Label emailLabel = new Label("Email Address");
 			emailLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, emailLabel);
 			flexTable.setWidget(numRows, 1, emailBox);
 			numRows++;
 		}
 		else if(userProfile.getEmail() != null)
 		{
 			Label infoLabel = new Label(userProfile.getEmail());
 			Label emailLabel = new Label("Email Address");
 			emailLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, emailLabel);
 			flexTable.setWidget(numRows, 1, infoLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			if(userProfile.getAddress() != null)
 				addressBox.setText(userProfile.getAddress());
 			Label streetLabel = new Label("Street Address");
 			streetLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, streetLabel);
 			flexTable.setWidget(numRows, 1, addressBox);
 			numRows++;
 		}
 		else if(userProfile.getAddress() != null)
 		{
 			Label addressLabel = new Label(userProfile.getAddress());
 			Label streetLabel = new Label("Street Address");
 			streetLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, streetLabel);
 			flexTable.setWidget(numRows, 1, addressLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			final VerticalPanel buttonPanel = new VerticalPanel();
 			buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			buttonPanel.setWidth("100%");
 			Button saveButton = new Button("Save");
 			saveButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
 			buttonPanel.add(saveButton);
 			saveButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					userProfile.setPhone(phoneBox.getText());
 					userProfile.setEmail(emailBox.getText());
 					userProfile.setAddress(addressBox.getText());
 					saveUserProfile(buttonPanel);
 				}
 			});
 			contactInfoPanel.add(buttonPanel);
 		}
 	}
 
 	private void setupBasicInfoPanel(HorizontalPanel infoRow2)
 	{
 		boolean privateProfile = false;
 		if(username.equals(Cookies.getCookie("loggedIn")) && viewProfileAsPrivate)
 			privateProfile = true;
 		if(!privateProfile && userProfile.getName() == null && userProfile.getGender() == null && userProfile.getBirthday() == null)
 			return;
 
 		final PromptedTextBox nameBox = new PromptedTextBox("First and Last Name", "promptText");
 		final PromptedTextBox genderBox = new PromptedTextBox("Your gender", "promptText");
 		final PromptedTextBox birthdayBox = new PromptedTextBox("Your birthday", "promptText");
 
 		VerticalPanel basicInfoPanel = new VerticalPanel();
 		basicInfoPanel.setSpacing(15);
 		basicInfoPanel.setStyleName("basicInfoPanel");
 		infoRow2.add(basicInfoPanel);
 		basicInfoPanel.setWidth("280px");
 
 		Label lblBasicInformation = new Label("Basic Information");
 		lblBasicInformation.setStyleName("largerText");
 		basicInfoPanel.add(lblBasicInformation);
 
 		FlexTable flexTable = new FlexTable();
 		basicInfoPanel.add(flexTable);
 
 		int numRows = 0;
 
 		if(privateProfile)
 		{
 			if(userProfile.getName() != null)
 				nameBox.setText(userProfile.getName());
 			Label nameLabel = new Label("Real Name");
 			nameLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, nameLabel);
 			flexTable.setWidget(numRows, 1, nameBox);
 			numRows++;
 		}
 		else if(userProfile.getName() != null)
 		{
 			Label infoLabel = new Label(userProfile.getName());
 			Label nameLabel = new Label("Real Name");
 			nameLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, nameLabel);
 			flexTable.setWidget(numRows, 1, infoLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			if(userProfile.getGender() != null)
 				genderBox.setText(userProfile.getGender());
 			Label genderLabel = new Label("Gender");
 			genderLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, genderLabel);
 			flexTable.setWidget(numRows, 1, genderBox);
 			numRows++;
 		}
 		else if(userProfile.getGender() != null)
 		{
 			Label infoLabel = new Label(userProfile.getGender());
 			Label genderLabel = new Label("Gender");
 			genderLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, genderLabel);
 			flexTable.setWidget(numRows, 1, infoLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			if(userProfile.getBirthday() != null)
 				birthdayBox.setText(userProfile.getBirthday());
 			Label birthdayLabel = new Label("Birthday");
 			birthdayLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, birthdayLabel);
 			flexTable.setWidget(numRows, 1, birthdayBox);
 			numRows++;
 		}
 		else if(userProfile.getBirthday() != null)
 		{
 			Label infoLabel = new Label(userProfile.getBirthday());
 			Label birthdayLabel = new Label("Birthday");
 			birthdayLabel.setStyleName("bold-black");
 			flexTable.setWidget(numRows, 0, birthdayLabel);
 			flexTable.setWidget(numRows, 1, infoLabel);
 			numRows++;
 		}
 
 		if(privateProfile)
 		{
 			final VerticalPanel buttonPanel = new VerticalPanel();
 			buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			buttonPanel.setWidth("100%");
 			Button saveButton = new Button("Save");
 			saveButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
 			buttonPanel.add(saveButton);
 			saveButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					userProfile.setName(nameBox.getText());
 					userProfile.setGender(genderBox.getText());
 					userProfile.setBirthday(birthdayBox.getText());
 					saveUserProfile(buttonPanel);
 				}
 			});
 			basicInfoPanel.add(buttonPanel);
 		}
 	}
 
 	private void setupEducationPanel(HorizontalPanel infoRow1)
 	{
 		boolean privateProfile = false;
 		if(username.equals(Cookies.getCookie("loggedIn")) && viewProfileAsPrivate)
 			privateProfile = true;
 		if(!privateProfile && userProfile.getHighSchool() == null && userProfile.getCollege() == null)
 			return;
 
 		final PromptedTextBox highSchoolBox = new PromptedTextBox("Which High School did you attend?", "promptText");
 		final PromptedTextBox collegeBox = new PromptedTextBox("Which College/University are you attending?", "promptText");
 
 		VerticalPanel educationPanel = new VerticalPanel();
 		infoRow1.add(educationPanel);
 		educationPanel.setWidth("280px");
 		educationPanel.setStyleName("storyPanel");
 		educationPanel.setSpacing(15);
 
 		Label lblEducation = new Label("Education");
 		lblEducation.setStyleName("largerText");
 		educationPanel.add(lblEducation);
 
 		Label lblHighSchool = new Label("High School");
 		lblHighSchool.setStyleName("bold-black");
 		educationPanel.add(lblHighSchool);
 
 		if(privateProfile)
 		{
 			if(userProfile.getHighSchool() != null)
 				highSchoolBox.setText(userProfile.getHighSchool());
 			educationPanel.add(highSchoolBox);
 		}
 		else if(userProfile.getHighSchool() != null)
 		{
 			Label highSchoolLabel = new Label(userProfile.getHighSchool());
 			educationPanel.add(highSchoolLabel);
 		}
 		else
 			educationPanel.remove(lblHighSchool);
 
 		Label lblCollege = new Label("College/University");
 		lblCollege.setStyleName("bold-black");
 		educationPanel.add(lblCollege);
 
 		if(privateProfile)
 		{
 			if(userProfile.getCollege() != null)
 				collegeBox.setText(userProfile.getCollege());
 			educationPanel.add(collegeBox);
 		}
 		else if(userProfile.getCollege() != null)
 		{
 			Label collegeLabel = new Label(userProfile.getCollege());
 			educationPanel.add(collegeLabel);
 		}
 		else
 			educationPanel.remove(lblCollege);
 
 		if(privateProfile)
 		{
 			final VerticalPanel buttonPanel = new VerticalPanel();
 			buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			buttonPanel.setWidth("100%");
 			Button saveButton = new Button("Save");
 			saveButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
 			buttonPanel.add(saveButton);
 			saveButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					userProfile.setHighSchool(highSchoolBox.getText());
 					userProfile.setCollege(collegeBox.getText());
 					saveUserProfile(buttonPanel);
 				}
 			});
 			educationPanel.add(buttonPanel);
 		}
 	}
 
 	private void setupStoryPanel(HorizontalPanel infoRow1)
 	{
 		boolean privateProfile = false;
 		if(username.equals(Cookies.getCookie("loggedIn")) && viewProfileAsPrivate)
 			privateProfile = true;
 		if(!privateProfile && userProfile.getTagline() == null && userProfile.getIntroduction() == null && userProfile.getBraggingRights() == null)
 			return;
 
 		final PromptedTextBox taglineBox = new PromptedTextBox("A brief description of you", "promptText");
 		final PromptedTextBox introductionBox = new PromptedTextBox("Put a little about yourself here so people know they've found the correct person.", "promptText");
 		final PromptedTextBox braggingBox = new PromptedTextBox("Examples: survived high school, have 3 kids, etc.", "promptText");
 
 		VerticalPanel storyPanel = new VerticalPanel();
 		infoRow1.add(storyPanel);
 		storyPanel.setWidth("280px");
 		storyPanel.setStyleName("educationPanel");
 		storyPanel.setSpacing(15);
 
 		Label lblStory = new Label("Story");
 		lblStory.setStyleName("largerText");
 		storyPanel.add(lblStory);
 
 		Label lblTagline = new Label("Tagline");
 		lblTagline.setStyleName("bold-black");
 		storyPanel.add(lblTagline);
 
 		if(privateProfile)
 		{
 			if(userProfile.getTagline() != null)
 				taglineBox.setText(userProfile.getTagline());
 			storyPanel.add(taglineBox);
 		}
 		else if(userProfile.getTagline() != null)
 		{
 			Label taglineLabel = new Label(userProfile.getTagline());
 			storyPanel.add(taglineLabel);
 		}
 		else
 			storyPanel.remove(lblTagline);
 
 		Label lblIntroduction = new Label("Introduction");
 		lblIntroduction.setStyleName("bold-black");
 		storyPanel.add(lblIntroduction);
 
 		if(privateProfile)
 		{
 			if(userProfile.getIntroduction() != null)
 				introductionBox.setText(userProfile.getIntroduction());
 			storyPanel.add(introductionBox);
 		}
 		else if(userProfile.getIntroduction() != null)
 		{
 			Label introductionLabel = new Label(userProfile.getIntroduction());
 			storyPanel.add(introductionLabel);
 		}
 		else
 			storyPanel.remove(lblIntroduction);
 
 		Label lblBraggingRights = new Label("Bragging Rights");
 		lblBraggingRights.setStyleName("bold-black");
 		storyPanel.add(lblBraggingRights);
 
 		if(privateProfile)
 		{
 			if(userProfile.getBraggingRights() != null)
 				braggingBox.setText(userProfile.getBraggingRights());
 			storyPanel.add(braggingBox);
 		}
 		else if(userProfile.getBraggingRights() != null)
 		{
 			Label braggingRightsLabel = new Label(userProfile.getBraggingRights());
 			storyPanel.add(braggingRightsLabel);
 		}
 		else
 			storyPanel.remove(lblBraggingRights);
 
 		if(privateProfile)
 		{
 			final VerticalPanel buttonPanel = new VerticalPanel();
 			buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			buttonPanel.setWidth("100%");
 			Button saveButton = new Button("Save");
 			saveButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
 			buttonPanel.add(saveButton);
 			saveButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					userProfile.setTagline(taglineBox.getText());
 					userProfile.setIntroduction(introductionBox.getText());
 					userProfile.setBraggingRights(braggingBox.getText());
 					saveUserProfile(buttonPanel);
 				}
 			});
 			storyPanel.add(buttonPanel);
 		}
 	}
 
 	private void setUserProfile(String username)
 	{
 		AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{}
 
 			@Override
 			public void onSuccess(UserProfile result)
 			{
 				userProfile = result;
 				setupTabs(mainPanel);
 			}
 
 		};
 		userService.getUserProfile(username, callback);
 	}
 
 	/**
 	 * Searches the database for posts which match the given query. It will then
 	 * display all results in a new panel.
 	 * 
 	 * @param searchString
 	 *            the search string
 	 * @param postPanel
 	 *            the {@link VerticalPanel} to attach the posts to
 	 * 
 	 * @custom.accessed None
 	 * @custom.changed None
 	 * @custom.called {@link com.cs1530.group4.addendum.server.UserServiceImpl#postSearch(int, String, String)}
 	 */
 	public void postSearch(final String searchString, final VerticalPanel postPanel)
 	{
 		AsyncCallback<ArrayList<Post>> callback = new AsyncCallback<ArrayList<Post>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{}
 
 			@Override
 			public void onSuccess(ArrayList<Post> posts)
 			{
 				if(posts.size() == 0)
 				{
 					postPanel.clear();
 					postPanel.add(new Label("No results found for '" + searchString + "'"));
 					return;
 				}
 				postPanel.clear();
 
 				Collections.sort(posts, Post.PostScoreComparator);
 				int count = 0;
 				for(Post post : posts)
 				{
 					count++;
 					if(count == 11)
 					{
 						nextPage.setVisible(true);
 						break;
 					}
 
 					nextPage.setVisible(false);
 					postPanel.add(new UserPost(main, post));
 				}
 			}
 		};
 		userService.postSearch(startIndex, searchString, username, callback);
 	}
 
 	private void saveUserProfile(final VerticalPanel buttonPanel)
 	{
 		AsyncCallback<Void> callback = new AsyncCallback<Void>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 				Label error = new Label("Error");
 				error.setStyleName("errorlabel");
 				buttonPanel.add(error);
 				createTimer(error);
 			}
 
 			@Override
 			public void onSuccess(Void v)
 			{
 				Label saved = new Label("Saved");
 				saved.setStyleName("successLabel");
 				buttonPanel.add(saved);
 				createTimer(saved);
 			}
 
 			private void createTimer(final Label label)
 			{
 				Timer timer = new Timer()
 				{
 					public void run()
 					{
 						buttonPanel.remove(label);
 					}
 				};
 
 				timer.schedule(5000);
 			}
 		};
 
 		userService.setUserProfile(username, userProfile, callback);
 	}
 }

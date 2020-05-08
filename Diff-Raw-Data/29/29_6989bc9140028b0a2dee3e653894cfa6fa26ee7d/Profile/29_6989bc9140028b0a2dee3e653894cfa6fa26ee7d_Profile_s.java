 package com.cs1530.group4.addendum.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.cs1530.group4.addendum.shared.Post;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class Profile extends Composite
 {
 	MainView main;
 	String username, streamLevel = "all", sortMethod = "Popular";
 	UserServiceAsync userService = UserService.Util.getInstance();
 	VerticalPanel vPanel, currentTab;
 	int startIndex = 0;
 	Anchor nextPage;
 	ArrayList<String> userCourses;
 
 	public Profile(MainView m, String u)
 	{
 		main = m;
 		username = u;
 		vPanel = new VerticalPanel();
 		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		vPanel.getElement().getStyle().setProperty("marginBottom", "10px");
 
 		initWidget(vPanel);
 		
		if(username == null)
		{
			main.setContent(new Login(main),"login");
		}

 		HorizontalPanel hPanel = new HorizontalPanel();
 		vPanel.add(hPanel);
 
 		VerticalPanel userPanel = new VerticalPanel();
 		userPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		userPanel.getElement().getStyle().setProperty("marginLeft", "10px");
 		userPanel.getElement().getStyle().setProperty("marginRight", "30px");
 		hPanel.add(userPanel);
 		
 		AbsolutePanel absolutePanel = new AbsolutePanel();
 		absolutePanel.setStyleName("gwt-border");
 		absolutePanel.setSize("128px", "128px");
 		userPanel.add(absolutePanel);
 
 		Image image = new Image("/addendum/getImage?username="+username);
 		final Label changeImageLabel = new Label("Change Image");
 		changeImageLabel.setStyleName("gwt-DecoratorPanel-white");
 		changeImageLabel.setSize("128px","28px");
 		MouseOverHandler mouseOver = new MouseOverHandler()
 		{
 			@Override
 			public void onMouseOver(MouseOverEvent event)
 			{
 				changeImageLabel.setVisible(true);
 			}	
 		};
 		MouseOutHandler mouseOut = new MouseOutHandler()
 		{
 			@Override
 			public void onMouseOut(MouseOutEvent event)
 			{
 				changeImageLabel.setVisible(false);
 			}
 		};
 		ClickHandler changePictureHandler = new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				ProfilePictureUpload profilePic = new ProfilePictureUpload(username);
 				profilePic.show();
 			}	
 		};
 		image.addMouseOverHandler(mouseOver);
 		changeImageLabel.addMouseOverHandler(mouseOver);
 		image.addMouseOutHandler(mouseOut);
 		changeImageLabel.addMouseOutHandler(mouseOut);
 		image.addClickHandler(changePictureHandler);
 		changeImageLabel.addClickHandler(changePictureHandler);
 		
 		absolutePanel.add(image);
 		image.setSize("128px", "128px");
 				
 		changeImageLabel.setVisible(false);
 		absolutePanel.add(changeImageLabel,-5,100);
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		userPanel.add(horizontalPanel);
 
 		Label usernameLabel = new Label(username);
 		usernameLabel.getElement().getStyle().setProperty("marginRight", "10px");
 		horizontalPanel.add(usernameLabel);
 
 		Anchor logoutAnchor = new Anchor("Logout");
 		horizontalPanel.add(logoutAnchor);
 		logoutAnchor.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				Cookies.removeCookie("loggedIn");
 				main.setContent(new Login(main),"login");
 			}
 		});
 		usernameLabel.getElement().getStyle().setProperty("marginBottom", "30px");
 
 		VerticalPanel classPanel = new VerticalPanel();
 		classPanel.setSpacing(3);
 		userPanel.add(classPanel);
 
 		Button addRemove = new Button("Add/Remove Classes");
 		addRemove.getElement().getStyle().setProperty("marginTop", "10px");
 		addRemove.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				main.setContent(new ClassSearch(main),"classSearch");
 				//TODO: implement class removal screen
 			}
 		});
 		Anchor allAnchor = new Anchor("All Classes");
 		allAnchor.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				streamLevel = "all";
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		});
 		getClasses(classPanel, addRemove, allAnchor);
 
 		final TabPanel tabPanel = new TabPanel();
 		hPanel.add(tabPanel);
 
 		VerticalPanel popularUpdatesPanel = new VerticalPanel();
 		popularUpdatesPanel.setWidth("600px");
 		VerticalPanel newUpdatesPanel = new VerticalPanel();
 		newUpdatesPanel.setWidth("600px");
 		tabPanel.add(popularUpdatesPanel, "Popular", false);
 		tabPanel.add(newUpdatesPanel, "New", false);
 		tabPanel.getElement().getStyle().setProperty("marginRight", "10px");
 		tabPanel.addSelectionHandler(new SelectionHandler<Integer>()
 		{
 			@Override
 			public void onSelection(SelectionEvent<Integer> event)
 			{
 				sortMethod = tabPanel.getTabBar().getTabHTML(event.getSelectedItem());
 				currentTab = (VerticalPanel) tabPanel.getWidget(event.getSelectedItem());
 				if(userCourses != null)
 				{
 					ArrayList<String> streamLevels = new ArrayList<String>();
 					streamLevels.add(streamLevel);
 					if(streamLevel.equals("all"))
 						streamLevels.addAll(userCourses);
 					getPosts(currentTab, streamLevels, sortMethod);
 				}
 			}
 		});
 		popularUpdatesPanel.setSpacing(15);
 		newUpdatesPanel.setSpacing(15);
 
 		tabPanel.selectTab(0);
 
 		Button btnCreateANew = new Button("Create a new post");
 		btnCreateANew.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				NewPost editor = new NewPost(main, userCourses);
 				editor.show();
 			}
 		});
 		hPanel.add(btnCreateANew);
 
 		nextPage = new Anchor("Next 10 Posts");
 		nextPage.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				startIndex += 10;
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				if(streamLevel.equals("all"))
 					streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		});
 	}
 
 	private void getPosts(final VerticalPanel updatesPanel, ArrayList<String> streamLevels, final String sortMethod)
 	{
 		updatesPanel.clear();
 		AsyncCallback<ArrayList<Post>> callback = new AsyncCallback<ArrayList<Post>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 			}
 
 			@Override
 			public void onSuccess(ArrayList<Post> posts)
 			{
 				if(sortMethod.equals("Popular"))
 					Collections.sort(posts, Post.PostScoreComparator);
 				if(sortMethod.equals("New"))
 					Collections.sort(posts, Post.PostTimeComparator);
 
 				for(Post post : posts)
 				{
 					updatesPanel.add(new UserPost(post));
 				}
 				if(posts.size() == 10)
 				{
 					nextPage.setVisible(true);
 					vPanel.add(nextPage);
 				}
 			}
 		};
 		userService.getPosts(startIndex, streamLevels, callback);
 	}
 
 	private void getClasses(final VerticalPanel classPanel, final Button addRemove, final Anchor allAnchor)
 	{
 		AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 			}
 
 			@Override
 			public void onSuccess(ArrayList<String> courses)
 			{
 				userCourses = courses;
 				classPanel.clear();
 				classPanel.add(allAnchor);
 				for(final String course : courses)
 				{
 					Anchor courseAnchor = new Anchor(course);
 					courseAnchor.addClickHandler(new ClickHandler()
 					{
 						@Override
 						public void onClick(ClickEvent event)
 						{
 							streamLevel = course.trim();
 							ArrayList<String> streamLevels = new ArrayList<String>();
 							streamLevels.add(streamLevel);
 							getPosts(currentTab, streamLevels, sortMethod);
 						}
 					});
 					classPanel.add(courseAnchor);
 				}
 				classPanel.add(addRemove);
 				
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				if(streamLevel.equals("all"))
 					streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		};
 
 		userService.getUserCourses(username, callback);
 	}
 }

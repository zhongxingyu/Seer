 package com.cs1530.group4.addendum.client;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import com.cs1530.group4.addendum.shared.Comment;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * This represents the UI for a comment that is associated with a
  * {@link UserPost}'s {@link com.cs1530.group4.addendum.shared.Post}
  */
 public class PostComment extends Composite implements MouseOverHandler, MouseOutHandler
 {
 	/** The menu button. */
 	Image menu;
 
 	/** The plus one button */
 	Image plusOneButton;
 
 	/** The popup. */
 	CommentMenuPopup popup;
 
 	/** The a static instance of the service used for RPC calls. */
 	UserServiceAsync userService = UserService.Util.getInstance();
 
 	/**
 	 * Instantiates a new PostComment.
 	 * 
 	 * @param main
 	 *            the application's {@link MainView}
 	 * @param comment
 	 *            the {@link Comment} associated with this PostComment
 	 * @param userPost
 	 *            the {@link UserPost} associated with this PostComment
 	 * 
 	 * @custom.accessed None
 	 * @custom.changed None
 	 * @custom.called {@link #getFormattedTime(Comment)}
 	 */
 	public PostComment(final MainView main, final Comment comment, final UserPost userPost)
 	{
 		VerticalPanel rowPanel = new VerticalPanel();
 		rowPanel.setStyleName("CommentPanelbackcolor");
 		rowPanel.setWidth("100%");
 		initWidget(rowPanel);
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setWidth("100%");
 
 		addDomHandler(this, MouseOverEvent.getType());
 		addDomHandler(this, MouseOutEvent.getType());
 
 		HTML content = new HTML(comment.getContent());
 		content.setStyleName("CommentSeperator");
 
 		HorizontalPanel headerPanel = new HorizontalPanel();
 		horizontalPanel.add(headerPanel);
 
 		if(userPost.post.getUsername().equals(Cookies.getCookie("loggedIn")))
 		{
			content.getElement().getStyle().setProperty("marginLeft", "38px");
			
 			VerticalPanel acceptedPanel = new VerticalPanel();
 			acceptedPanel.setWidth("38px");
 			final Image check = new Image();
 			check.setStyleName("imageButton");
 			if(comment.isAccepted())
 			{
 				check.setUrl("/images/accepted.png");
 				check.setTitle("Click to unaccept this answer.");
 			}
 			else
 			{
 				check.setUrl("/images/not_accepted.png");
 				check.setTitle("Click to accept this comment as an answer.");
 			}
 			check.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					if(comment.isAccepted())
 					{
 						check.setUrl("/images/not_accepted.png");
 						check.setTitle("Click to accept this comment as an answer.");
 						comment.setAccepted(false);
 						acceptComment(comment, false);
 					}
 					else
 					{
 						check.setUrl("/images/accepted.png");
 						check.setTitle("Click to unaccept this answer.");
 						for(Comment c : userPost.post.getComments())
 							c.setAccepted(false);
 						comment.setAccepted(true);
 						userPost.displayComments();
 						acceptComment(comment, true);
 					}
 				}
 
 				private void acceptComment(Comment comment, boolean b)
 				{
 					AsyncCallback<Void> callback = new AsyncCallback<Void>()
 					{
 						@Override
 						public void onFailure(Throwable caught)
 						{}
 
 						@Override
 						public void onSuccess(Void result)
 						{}
 					};
 					userService.acceptComment(comment, userPost.post.getUsername(), b, callback);
 				}
 			});
 			acceptedPanel.add(check);
 			headerPanel.add(acceptedPanel);
 		}
		else if(comment.isAccepted())
		{
			content.getElement().getStyle().setProperty("marginLeft", "38px");
			
			VerticalPanel acceptedPanel = new VerticalPanel();
			acceptedPanel.setWidth("38px");
			final Image check = new Image("/images/accepted.png");
			check.setTitle("This comment was accepted by the post author as a good answer.");
			acceptedPanel.add(check);
			headerPanel.add(acceptedPanel);
		}
		
 		Image image = new Image("/addendum/getImage?username=" + comment.getUsername());
 		headerPanel.add(image);
 		image.getElement().getStyle().setProperty("marginRight", "10px");
 		image.setSize("28px", "28px");
 
 		VerticalPanel verticalPanel = new VerticalPanel();
 		headerPanel.add(verticalPanel);
 
 		Anchor usernameLabel = new Anchor(comment.getUsername());
 		usernameLabel.setStyleName("gwt-Label-bold");
 		verticalPanel.add(usernameLabel);
 
 		HorizontalPanel timePanel = new HorizontalPanel();
 		Label lblCommenttime = new Label(getFormattedTime(comment));
 		lblCommenttime.setStyleName("gwt-Label-grey");
 
 		final Label plusOnesLabel = new Label("+" + String.valueOf(comment.getPlusOnes()));
 		plusOnesLabel.setStyleName("gwt-Label-grey-bold");
 		if(comment.getPlusOnes() == 0)
 			plusOnesLabel.setVisible(false);
 
 		plusOneButton = new Image("/images/plus_one_default.png");
 		plusOneButton.setStyleName("plusOneButton");
 		plusOneButton.setVisible(false);
 		if(comment.isPlusOned())
 			plusOneButton.setUrl("/images/plus_one_checked.png");
 		plusOneButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 				{
 
 					@Override
 					public void onFailure(Throwable caught)
 					{}
 
 					@Override
 					public void onSuccess(Boolean result)
 					{
 						comment.setPlusOned(result);
 
 						if(result) //was increased
 						{
 							comment.setPlusOnes(comment.getPlusOnes() + 1);
 							plusOnesLabel.setText("+" + comment.getPlusOnes());
 							plusOnesLabel.setVisible(true);
 							plusOneButton.setUrl("/images/plus_one_checked.png");
 						}
 						else
 						{
 							comment.setPlusOnes(comment.getPlusOnes() - 1);
 							plusOnesLabel.setText("+" + comment.getPlusOnes());
 							if(comment.getPlusOnes() == 0)
 								plusOnesLabel.setVisible(false);
 							plusOneButton.setUrl("/images/plus_one_default.png");
 						}
 
 						ArrayList<Comment> comments = userPost.post.getComments();
 						for(int i = 0; i < comments.size(); i++)
 						{
 							if(comments.get(i).getCommentKey() != null && comments.get(i).getCommentKey().equals(comment.getCommentKey()))
 							{
 								comments.remove(i);
 								comments.add(i, comment);
 								break;
 							}
 						}
 
 						userPost.displayComments();
 					}
 
 				};
 				userService.plusOne(comment.getCommentKey(), Cookies.getCookie("loggedIn"), callback);
 			}
 		});
 
 		timePanel.add(lblCommenttime);
 		timePanel.add(plusOnesLabel);
 		timePanel.add(plusOneButton);
 		verticalPanel.add(timePanel);
 
 		usernameLabel.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				main.setContent(new Profile(main, comment.getUsername(), true), "profile-" + comment.getUsername());
 			}
 		});
 
 		menu = new Image("images/menu.png");
 		menu.setSize("24px", "24px");
 		menu.setVisible(false);
 		menu.getElement().getStyle().setProperty("marginRight", "5px");
 		popup = new CommentMenuPopup(main, menu, comment, userPost);
 		menu.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				if(popup.isShowing())
 					popup.hide();
 				else
 					popup.showRelativeTo(menu);
 			}
 		});
 
 		HorizontalPanel menuPanel = new HorizontalPanel();
 		menuPanel.setWidth("100%");
 		menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		menuPanel.add(menu);
 		horizontalPanel.add(menuPanel);
 
 		rowPanel.add(horizontalPanel);
 		rowPanel.add(content);
 	}
 
 	/**
 	 * This will return a {@link Date} object formatted as "h:mm a" if it is the
 	 * same day or "MMM d, yyyy" if is is not.
 	 * 
 	 * @param comment
 	 *            the {@link Comment} object to get the {@link Date} from
 	 * @return the formatted time string
 	 * 
 	 * @custom.accessed None
 	 * @custom.changed None
 	 * @custom.called None
 	 */
 	private String getFormattedTime(Comment comment)
 	{
 		String timeFormatString = "h:mm a";
 		String editFormatString = "h:mm a";
 		Date now = new Date(System.currentTimeMillis());
 		if(comment.getCommentTime().getDate() != now.getDate())
 			timeFormatString = "MMM d, yyyy";
 		if(comment.getLastEdit() != null && comment.getLastEdit().getDate() != now.getDate())
 			editFormatString = "MMM d, yyyy";
 		DateTimeFormat dtf = new DateTimeFormat(timeFormatString, new DefaultDateTimeFormatInfo())
 		{};
 		DateTimeFormat editDtf = new DateTimeFormat(editFormatString, new DefaultDateTimeFormatInfo())
 		{};
 		String timeLabel = dtf.format(comment.getCommentTime());
 		if(comment.getLastEdit() != null)
 			timeLabel += " (last edit - " + editDtf.format(comment.getLastEdit()) + ")";
 
 		return timeLabel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google
 	 * .gwt.event.dom.client.MouseOutEvent)
 	 */
 	@Override
 	public void onMouseOut(MouseOutEvent event)
 	{
 		menu.setVisible(false);
 		plusOneButton.setVisible(false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google
 	 * .gwt.event.dom.client.MouseOverEvent)
 	 */
 	@Override
 	public void onMouseOver(MouseOverEvent event)
 	{
 		menu.setVisible(true);
 		plusOneButton.setVisible(true);
 	}
 }

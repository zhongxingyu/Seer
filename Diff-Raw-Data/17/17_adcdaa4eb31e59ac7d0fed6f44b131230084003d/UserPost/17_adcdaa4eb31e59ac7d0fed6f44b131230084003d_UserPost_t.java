 package com.cs1530.group4.addendum.client;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import com.cs1530.group4.addendum.shared.Comment;
 import com.cs1530.group4.addendum.shared.Post;
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
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 // TODO: Auto-generated Javadoc
 /**
  * UserPost visually represents a post by a user of Addendum.  It contains their uploaded content as well as any comments associated with this post.
  */
 public class UserPost extends Composite implements MouseOverHandler, MouseOutHandler
 {
 	UserServiceAsync userService = UserService.Util.getInstance();
 	int upDownVotes;
 	VerticalPanel commentPanel;
 	ScrollPanel scroll;
 	Image menu;
 	MenuPopup popup = null;
 	String loggedInUser = Cookies.getCookie("loggedIn");
 	Stream stream;
 	MainView main;
 	PromptedTextBox addAComment;
 	CommentBox commentBox;
 	UserPost userPost = this;
 	Post post;
 	boolean isExpanded = false;
 
 	/**
 	 * Instantiates a new user post.
 	 *
 	 * @param m The MainView of the application
 	 * @param stream A reference to the stream class that this post is attached to.
 	 * @param p The post object that this UserPost represents.
 	 */
 	public UserPost(MainView m, final Stream stream, Post p)
 	{
 		this.stream = stream;
 		main = m;
 		post = p;
 		upDownVotes = post.getUpvotes() - post.getDownvotes();
 		HorizontalPanel border = new HorizontalPanel();
 		border.setBorderWidth(1);
 		border.setWidth("100%");
 		initWidget(border);
 
 		FlowPanel scorePanel = new FlowPanel();
 		scorePanel.getElement().getStyle().setProperty("marginLeft", "5px");
 		scorePanel.getElement().getStyle().setProperty("marginRight", "5px");
 		border.add(scorePanel);
 		border.setCellHorizontalAlignment(scorePanel, HasHorizontalAlignment.ALIGN_CENTER);
 		border.setCellWidth(scorePanel, "34px");
 
 		final Image upArrow = new Image("images/default_up.png");
 		final Image downArrow = new Image("images/default_down.png");
 		if(post.isUpvoted())
 			upArrow.setUrl("images/voted_up.png");
 		if(post.isDownvoted())
 			downArrow.setUrl("images/voted_down.png");
 		final Label scoreLabel = new Label(String.valueOf(upDownVotes));
 		upArrow.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 				{
 					@Override
 					public void onFailure(Throwable caught)
 					{
 					}
 
 					@Override
 					public void onSuccess(Boolean success)
 					{
 						if(success) //user has not upvoted before or they have downvoted before
 						{
 							if(post.isDownvoted())
 								upDownVotes++;
 							post.setDownvoted(false);
 							downArrow.setUrl("images/default_down.png");
 							if(post.isUpvoted())
 							{
 								scoreLabel.setText(String.valueOf(--upDownVotes));
 								post.setUpvoted(false);
 								upArrow.setUrl("images/default_up.png");
 							}
 							else
 							{
 								scoreLabel.setText(String.valueOf(++upDownVotes));
 								post.setUpvoted(true);
 								upArrow.setUrl("images/voted_up.png");
 							}
 						}
 						else //user has upvoted before - undo it
 						{
 							scoreLabel.setText(String.valueOf(--upDownVotes));
 							post.setUpvoted(false);
 							upArrow.setUrl("images/default_up.png");
 						}
 					}
 				};
 				userService.upvotePost(post.getPostKey(), Cookies.getCookie("loggedIn"), callback);
 			}
 		});
 		downArrow.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 				{
 					@Override
 					public void onFailure(Throwable caught)
 					{
 					}
 
 					@Override
 					public void onSuccess(Boolean success)
 					{
 						if(success)
 						{
 							if(post.isUpvoted())
 								upDownVotes--;
 							post.setUpvoted(false);
 							upArrow.setUrl("images/default_up.png");
 							if(post.isDownvoted())
 							{
 								scoreLabel.setText(String.valueOf(++upDownVotes));
 								post.setDownvoted(false);
 								downArrow.setUrl("images/default_down.png");
 							}
 							else
 							{
 								scoreLabel.setText(String.valueOf(--upDownVotes));
 								post.setDownvoted(true);
 								downArrow.setUrl("images/voted_down.png");
 							}
 						}
 						else
 						{
 							post.setDownvoted(false);
 							scoreLabel.setText(String.valueOf(++upDownVotes));
 							downArrow.setUrl("images/default_down.png");
 						}
 					}
 				};
 				userService.downvotePost(post.getPostKey(), Cookies.getCookie("loggedIn"), callback);
 			}
 		});
 		scorePanel.add(upArrow);
 		upArrow.setSize("24px", "24px");
 
 		scoreLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		scorePanel.add(scoreLabel);
 
 		scorePanel.add(downArrow);
 		downArrow.setSize("24px", "24px");
 
 		FlowPanel postPanel = new FlowPanel();
 		postPanel.setWidth("100%");
 		border.add(postPanel);
 		
 		String label = post.getStreamLevel();
 		if(label.equals("all"))
 			label = "Everyone";
 		Label lblNewLabel = new Label(label);
 		lblNewLabel.setStyleName("postSpaceTitle");
 		lblNewLabel.getElement().getStyle().setProperty("marginLeft", "10px");
 		lblNewLabel.getElement().getStyle().setProperty("marginTop", "5px");
 		postPanel.add(lblNewLabel);
 		
 		HTML html = new HTML("<hr  style=\"width:100%;\" />");
 		postPanel.add(html);
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		postPanel.add(horizontalPanel);
 		horizontalPanel.setWidth("100%");
 
 		HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
 		horizontalPanel_2.setStyleName("PostComment");
 		horizontalPanel.add(horizontalPanel_2);
 
 		Image image = new Image("/addendum/getImage?username=" + post.getUsername());
 		horizontalPanel_2.add(image);
 		image.getElement().getStyle().setProperty("marginRight", "10px");
 		image.setSize("46px", "46px");
 
 		FlowPanel verticalPanel = new FlowPanel();
 		horizontalPanel_2.add(verticalPanel);
 
 		Anchor usernameLabel = new Anchor(post.getUsername());
 		if(stream != null)
 		{
 			usernameLabel.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					stream.postSearch("username:"+post.getUsername());
 				}
 			});
 		}
 		usernameLabel.setStyleName("gwt-Label-bold");
 		verticalPanel.add(usernameLabel);
 
 		String timeFormatString = "h:mm a";
 		String editFormatString = "h:mm a";
 		Date now = new Date(System.currentTimeMillis());
 		if(post.getPostTime().getDate() != now.getDate())
 			timeFormatString = "MMM d, yyyy";
 		if(post.getLastEdit() != null && post.getLastEdit().getDate() != now.getDate())
 			editFormatString = "MMM d, yyyy";
 		DateTimeFormat dtf = new DateTimeFormat(timeFormatString, new DefaultDateTimeFormatInfo())
 		{
 		};
 		DateTimeFormat editDtf = new DateTimeFormat(editFormatString, new DefaultDateTimeFormatInfo())
 		{
 		};
 		String timeLabel = dtf.format(post.getPostTime());
 		if(post.getLastEdit() != null)
 			timeLabel += " (last edit - " + editDtf.format(post.getLastEdit()) + ")";
 		Label lblPosttime = new Label(timeLabel);
 		lblPosttime.setStyleName("gwt-Label-grey");
 		verticalPanel.add(lblPosttime);
 
 		HorizontalPanel menuPanel = new HorizontalPanel();
 		menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		horizontalPanel.add(menuPanel);
 		menuPanel.setWidth("100%");
 
 		menu = new Image("images/menu.png");
 		menu.setSize("24px", "24px");
 		menu.setVisible(false);
 		menu.getElement().getStyle().setProperty("marginRight", "5px");
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
 		popup = new MenuPopup(main,menu,post,post.getUsername().equals(loggedInUser));
 		
 		menuPanel.add(menu);
 		
 		addDomHandler(this, MouseOverEvent.getType());
 		addDomHandler(this, MouseOutEvent.getType());
 
 		HTML postContent = new HTML(post.getPostContent());
 		postContent.setStyleName("userpostcomment");
 		postPanel.add(postContent);
 
 		HTML separator = new HTML("<hr  style=\"width:100%;\" />");
 		postPanel.add(separator);
 
 		scroll = new ScrollPanel();
 		commentPanel = new VerticalPanel();
 		commentPanel.setWidth("100%");
 		if(post.getComments().size() > 0)
 			commentPanel.setStyleName("CommentPanelbackcolor");
 		
 		VerticalPanel attachmentsPanel = new VerticalPanel();
 		postPanel.add(attachmentsPanel);
 		
 		if(post.getAttachmentKeys().size() > 0)
 		{
 			Label lblAttachments = new Label("Attachments:");
 			lblAttachments.setStyleName("NewPostBackLabel");
 			attachmentsPanel.add(lblAttachments);
 			
 			for(int i=0; i<post.getAttachmentKeys().size(); i++)
 			{
 				String key = post.getAttachmentKeys().get(i);
 				String name = post.getAttachmentNames().get(i);
 				Anchor anchor = new Anchor(name, "/addendum/getImage?key="+key,"_blank");
 				attachmentsPanel.add(anchor);
 			}
 			
			postPanel.add(new HTML("<hr  style=\"width:100%;\" />"));
 		}
 		
 		scroll.add(commentPanel);
 		postPanel.add(scroll);
 		
 		if(post.getComments() != null)
 		{
 			displayComments();
 		}
 
 		FlowPanel addCommentPanel = new FlowPanel();
 		postPanel.add(addCommentPanel);
 		addCommentPanel.setWidth("100%");
 
 		addAComment = new PromptedTextBox("Add a comment...", "promptText");
 		addAComment.getElement().getStyle().setProperty("margin", "10px");
 		commentBox = new CommentBox(addAComment, post, this);
 		commentBox.setVisible(false);
 		addCommentPanel.add(commentBox);
 		commentBox.setWidth("100%");
 		addAComment.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				showCommentBox(null);
 			}
 		});
 		addCommentPanel.add(addAComment);
 	}
 	
 	/**
 	 * This method handles the logic for displaying the comments associated with this UserPost.
 	 * If there are more than 2 comments, the comments will be collapsed into an expandable structure.
 	 * Additionally, if the comments physical height is greater than 300px, they will be placed into a scrollable panel.
 	 */
 	public void displayComments()
 	{
 		commentPanel.clear();
 		
 		if(post.getComments().size() > 2)
 		{
 			final HorizontalPanel expandPanel = new HorizontalPanel();
 			final HorizontalPanel hidePanel = new HorizontalPanel();
 			final ArrayList<Comment> comments = post.getComments();
 			
 			Label expandComments = new Label(comments.size() + " comments");
 			expandComments.setStyleName("expandCloseLink");
 			Image expandImage = new Image("images/open_arrow.png");
 			expandImage.setStyleName("expandCloseLink");
 			expandPanel.add(expandComments);
 			expandPanel.add(expandImage);
 			
 			Label hideComments = new Label("Hide comments");
 			hideComments.setStyleName("expandCloseLink");
 			Image hideImage = new Image("images/close_arrow.png");
 			hideImage.setStyleName("expandCloseLink");
 			hidePanel.add(hideComments);
 			hidePanel.add(hideImage);
 			
 			ClickHandler expandHandler = new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					isExpanded = true;
 					commentPanel.clear();
 					commentPanel.add(hidePanel);
 					for(Comment comment : post.getComments())
 						commentPanel.add(new PostComment(main,comment,stream,userPost));
 					adjustCommentScroll();
 				}
 			};
 			expandComments.addClickHandler(expandHandler);
 			expandImage.addClickHandler(expandHandler);
 			
 			ClickHandler hideHandler = new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					isExpanded = false;
 					commentPanel.clear();
 					commentPanel.add(expandPanel);
 					commentPanel.add(new PostComment(main,comments.get(comments.size()-1),stream,userPost));
 					adjustCommentScroll();
 				}
 			};
 			hideComments.addClickHandler(hideHandler);
 			hideImage.addClickHandler(hideHandler);
 			
 			if(isExpanded)
 			{
 				commentPanel.add(hidePanel);
 				for(Comment comment : post.getComments())
 					commentPanel.add(new PostComment(main,comment,stream,userPost));
 			}
 			else
 			{
 				commentPanel.add(expandPanel);
 				commentPanel.add(new PostComment(main,comments.get(comments.size()-1),stream,userPost));
 			}
 		}
 		else
 		{
 			for(Comment comment : post.getComments())
 				commentPanel.add(new PostComment(main,comment,stream,userPost));
 		}
 		
 		adjustCommentScroll();
 	}
 	
 	/**
 	 * This method determines the height of the scroll panel for the comments.  If the comments total height is > 300px, the ScrollPanel will show it's scrollbar.
 	 */
 	private void adjustCommentScroll()
 	{
 		if(commentPanel.getOffsetHeight() > 300)
 			scroll.setHeight("300px");
 		else
 			scroll.setHeight("100%");
 	}
 	
 	/**
 	 * Display the comment editing box below the existing post and comments.
 	 *
 	 * @param comment The comment to be edited or null to start a new comment.
 	 */
 	public void showCommentBox(Comment comment)
 	{
 		addAComment.setVisible(false);
 		commentBox.setVisible(true);
 		commentBox.textArea.setFocus(true);
 		if(comment != null)
 		{
 			commentBox.textArea.setHTML(comment.getContent());
 			commentBox.isEdit = true;
 			commentBox.editComment = comment;
 		}
 		else
 		{
 			commentBox.textArea.setText("");
 			commentBox.isEdit = false;
 			commentBox.editComment = null;
 		}
 	}
 
 	/**
 	 * Adds the submitted comment to the list of comments for the post.
 	 *
 	 * @param comment The comment to be added.
 	 * @param isEdit A flag to say if this was an edit or a new comment.
 	 */
 	public void addSubmittedComment(Comment comment, boolean isEdit)
 	{
 		if(isEdit)
 		{
 			comment.setLastEdit(new Date(System.currentTimeMillis()));
 			for(int i=0; i<post.getComments().size(); i++)
 			{
 				String commentKey = post.getComments().get(i).getCommentKey();
 				if(commentKey != null && commentKey.equals(comment.getCommentKey()))
 				{
 					post.getComments().remove(i);
 					post.getComments().add(i, comment);
 				}
 			}
 			
 			displayComments();
 			
 			return;
 		}
 		commentPanel.add(new PostComment(main,comment,stream,userPost));
 		commentPanel.setStyleName("gwt-DecoratorPanel-newComment");
 		adjustCommentScroll();
 		post.getComments().add(comment);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
 	 */
 	@Override
 	public void onMouseOut(MouseOutEvent event)
 	{
 		menu.setVisible(false);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
 	 */
 	@Override
 	public void onMouseOver(MouseOverEvent event)
 	{
 		menu.setVisible(true);
 	}
 }

 package com.cs1530.group4.addendum.client;
 
 import com.cs1530.group4.addendum.shared.Comment;
 import com.cs1530.group4.addendum.shared.Post;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Label;
 
 public class CommentBox extends Composite
 {
 	CommentBox commentBox = this;
 	UserServiceAsync userService = UserService.Util.getInstance();
 	RichTextArea textArea;
 	Label errorLabel;
 	
 	public CommentBox(final PromptedTextBox addComment, final Post post, final UserPost userPost)
 	{
 		VerticalPanel verticalPanel = new VerticalPanel();
 		verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		verticalPanel.setStyleName("CommentBoxBackground");
 		verticalPanel.getElement().getStyle().setProperty("padding", "10px");
 		initWidget(verticalPanel);
 		verticalPanel.setSize("100%", "124px");
 				
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setStyleName("CommentBox");
 		verticalPanel.add(horizontalPanel);
 		horizontalPanel.setWidth("100%");
 		
 		Image image = new Image("/addendum/getImage?username="+Cookies.getCookie("loggedIn"));
 		image.getElement().getStyle().setProperty("marginRight", "10px");
 		horizontalPanel.add(image);
 		image.setSize("28px", "28px");
 		
 		VerticalPanel editorPanel = new VerticalPanel();
 		editorPanel.setStyleName("body");
 		textArea = new RichTextArea();
 		RichTextToolbar toolbar = new RichTextToolbar(textArea);
 		textArea.addStyleName("small");
 		textArea.setSize("95%", "75px");
 		editorPanel.add(toolbar);
 		editorPanel.add(textArea);
 		horizontalPanel.add(editorPanel);
 		editorPanel.setWidth("100%");
 		
 		errorLabel = new Label("Error: Message length must be greater than 0");
 		errorLabel.setStyleName("gwt-Label-Error");
 		errorLabel.setVisible(false);
 		verticalPanel.add(errorLabel);
 		
 		HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
 		horizontalPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		horizontalPanel_1.setSpacing(10);
 		verticalPanel.add(horizontalPanel_1);
 		horizontalPanel_1.setSize("307px", "43px");
 		verticalPanel.setCellHorizontalAlignment(horizontalPanel_1, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		final Button btnSubmit = new Button("Post Comment");
 		btnSubmit.setStyleName("ADCButton");
 		btnSubmit.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				if(textArea.getHTML().length() == 0)
 				{
 					errorLabel.setVisible(true);
 					return;
 				}
 				
 				btnSubmit.setEnabled(false);
 				
 				final Comment comment = new Comment(Cookies.getCookie("loggedIn"),textArea.getHTML());
 				AsyncCallback<Void> callback = new AsyncCallback<Void>()
 				{
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						btnSubmit.setEnabled(true);
 						errorLabel.setVisible(true);
 						errorLabel.setText("There was a problem uploading your post, please try again later.");
 					}
 					@Override
 					public void onSuccess(Void v)
 					{
 						userPost.addSubmittedComment(comment);
 						commentBox.setVisible(false);
 						addComment.setVisible(true);
 					}
 				};
 				userService.uploadComment(post.getPostKey(), comment, callback);
 			}
 		});
		horizontalPanel_1.add(btnSubmit);
 		btnSubmit.setSize("131px", "29px");
 		
 		Button btnCancel = new Button("Cancel");
 		btnCancel.setStyleName("ADCButton");
 		btnCancel.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				commentBox.setVisible(false);
 				addComment.setVisible(true);
 			}		
 		});
 		horizontalPanel_1.add(btnCancel);
 		btnCancel.setSize("131px", "29px");
 		setStyleName("CommentBoxBackground");
 	}
 }

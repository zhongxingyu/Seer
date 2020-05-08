 package com.cs1530.group4.addendum.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * A dialog that gives the user a UI to flag a post or comment for an administrator's review
  */
 public class FlagForm extends DialogBox
 {
 	/** The a static instance of the service used for RPC calls. */
 	UserServiceAsync userService = UserService.Util.getInstance();
 	
 	/** Indicates this is referring to a post. */
 	public static int POST = 0;
 	
 	/** Indicates this is referring to a comment. */
 	public static int COMMENT = 1;
 	
 	/** A reference to this FlagForm object. */
 	FlagForm form = this;
 	
 	/** The list of flaggable options. */
 	String[] options = {"Unwanted commercial content or spam","Pornography or sexually explicit material","Hate speech or graphic violence","Harassment or bullying","This account might be compromised or hacked","Other"};
 	
 	/**
 	 * Instantiates a new flag form.
 	 *
 	 * @param key the postKey or commentKey to flag
 	 * @param postOrComment one of either {@link #POST} or {@link #COMMENT}
 	 * 
 	 * @custom.accessed None
 	 * @custom.changed None
 	 * @custom.called None
 	 */
 	public FlagForm(final String key, final int postOrComment)
 	{
 		final ArrayList<RadioButton> radios = new ArrayList<RadioButton>();
 		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setStyleName("dialog-padding");
 		vPanel.add(new Label("What kind of abuse are you reporting?"));
 		for(String option : options)
 		{
 			RadioButton button = new RadioButton("options", option);
 			radios.add(button);
 			vPanel.add(button);
 		}
 		vPanel.add(new Label("Any additional remarks?"));
 		final TextBox otherBox = new TextBox();
		otherBox.setStyleName("ADCTextbox");
 		vPanel.add(otherBox);
		otherBox.setWidth("100%");
		
		VerticalPanel vertPanel = new VerticalPanel();
		vertPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.add(vertPanel);
		vertPanel.setWidth("100%");
 		HorizontalPanel buttonPanel = new HorizontalPanel();
		vertPanel.add(buttonPanel);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		Button cancelButton = new Button("Cancel");
		cancelButton.setStyleName("ADCButton");
 		buttonPanel.add(cancelButton);
 		cancelButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				form.hide();
 			}
 		});
 		Button okButton = new Button("OK");
		okButton.setStyleName("ADCButton");
 		buttonPanel.add(okButton);
 		okButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Void> callback = new AsyncCallback<Void>()
 				{
 					@Override
 					public void onFailure(Throwable caught){}
 					@Override
 					public void onSuccess(Void v){}
 				};
 				String reason = "";
 				for(RadioButton button : radios)
 				{
 					if(button.getValue())
 						reason = button.getText();
 				}
				if(reason.equals("Other") || reason.length() == 0)
 				{
 					if(otherBox.getText().length() == 0)
 					{
 						Window.alert("Please provide a reason");
 						return;
 					}
 					reason = otherBox.getText();
 				}
 				if(postOrComment == POST)
 					userService.flagPost(key, reason, true, callback);
 				else
 					userService.flagComment(key, reason, true, callback);
 			}
 		});
 		add(vPanel);
 		
 		setStyleName("ADCBasic");
 		form.setGlassEnabled(true);
 		form.center();
 	}
 }

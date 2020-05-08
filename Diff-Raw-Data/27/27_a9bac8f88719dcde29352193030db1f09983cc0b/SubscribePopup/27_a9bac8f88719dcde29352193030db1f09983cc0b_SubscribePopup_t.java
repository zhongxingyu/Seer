 package org.iskconsv.client.view;
 
 import org.iskconsv.client.resources.Resources;
 
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class SubscribePopup extends PopupPanel implements ClickHandler
 {
 	public static final SubscribePopup instance = new SubscribePopup();
 
 	private TextBox emailTextBox;
 
	private static final String GROUP_URL = "https://groups.google.com/a/iskconsv.org/group/announcements/";

 	public SubscribePopup()
 	{
 		FlowPanel container = new FlowPanel();
 		FlowPanel inputBar = new FlowPanel();
 		FlowPanel buttonBar = new FlowPanel();
 
 		Button cancelButton = new Button("Cancel", this);
 		Button continueButton = new Button("Subscribe...", this);
 		emailTextBox = new TextBox();
 
 		Label emailLabel = new Label("Email address:");
 		emailLabel.getElement().getStyle().setDisplay(Display.INLINE);
 		inputBar.add(emailLabel);
 		inputBar.add(emailTextBox);
 
 		buttonBar.add(cancelButton);
 		buttonBar.add(continueButton);
 
 		container.add(new HTML("<h2>Subscribe to Weekly Announcements</h2>"));
 		container.add(inputBar);
 		container.add(buttonBar);
 
 		buttonBar.addStyleName(Resources.INSTANCE.style().buttonBar());
 		container.getElement().getStyle().setProperty("textAlign", "center");
 
 		this.add(container);
 		this.setAnimationEnabled(true);
 		this.setGlassEnabled(true);
 		this.setAutoHideEnabled(false);
 	}

 	@Override
 	public void center()
 	{
 		super.center();
 		emailTextBox.setFocus(true);
 	}
 
 	@Override
 	public void onClick(ClickEvent event)
 	{
 		Button button = (Button) event.getSource();
 		if (button.getText().equals("Cancel"))
 			this.hide();
 		else
 		{
			Window.open(GROUP_URL + "boxsubscribe?email=" + URL.encodeComponent(emailTextBox.getText()), "_blank", null);
 		}
 	}
 }

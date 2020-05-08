 package gwtmvp.client.mvp;
 
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 public class GreetingView extends ViewImpl implements GreetingPresenter.MyView {
 	private final TextBox name;
 	private final Button sendButton;
 	private final FlowPanel panel = new FlowPanel();
 
 	public GreetingView() {
 
 		name = new TextBox();
 		panel.add(name);
 
 		sendButton = new Button("Go");
 		panel.add(sendButton);
 
 		// Add the nameField and sendButton to the RootPanel
 		// Use RootPanel.get() to get the entire body element
 		RootPanel.get("nameFieldContainer").add(name);
 		RootPanel.get("sendButtonContainer").add(sendButton);
 
 		reset();
 	}
 
 	@Override
 	public HasValue<String> getName() {
 		return name;
 	}
 
 	@Override
 	public HasClickHandlers getSend() {
 		return sendButton;
 	}
 
 	public void reset() {
 		// Focus the cursor on the name field when the app loads
 		name.setFocus(true);
 		name.selectAll();
 	}
 
 	@Override
 	public Widget asWidget() {
 		return panel;
 	}
 }

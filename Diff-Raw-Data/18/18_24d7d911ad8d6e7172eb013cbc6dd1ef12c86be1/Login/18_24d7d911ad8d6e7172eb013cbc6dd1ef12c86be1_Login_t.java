 package eu.ist.fears.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class Login implements EntryPoint{
 
     public void onModuleLoad() {
 	final Button sendButton = new Button("Login");
 	final TextBox nameField = new TextBox();
 	nameField.setText("ist153877");
 	sendButton.addStyleName("sendButton");
 	RootPanel.get("userField").add(nameField);
 	RootPanel.get("button").add(sendButton);
 	sendButton.addClickHandler(new ClickHandler() {
 	    
 	    public void onClick(ClickEvent arg0) {
		Window.open("index.html?ticket=" + nameField.getText() + "/bogusTicket", "_self", "");		
 	    }
 	});
 	nameField.setFocus(true);
 	nameField.selectAll();
     }
 }

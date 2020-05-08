 package org.vaadin.chatbox.demo;
 
 import org.vaadin.chatbox.SharedChat;
 import org.vaadin.chatbox.ChatBox;
 import org.vaadin.chatbox.client.ChatLine;
 import org.vaadin.chatbox.client.ChatUser;
 
 import com.vaadin.annotations.Push;
 import com.vaadin.annotations.Title;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 @Title("ChatBox Add-on Demo")
 @SuppressWarnings("serial")
 @Push
 public class DemoUI extends UI {
 	
 	private static SharedChat chat = new SharedChat();
 	static {
 		chat.addLine("Welcome to chat!");
 		chat.addLine("This ChatBox addon is available at http://vaadin.com/addon/chatbox");
 		chat.addListener(new SentNotifier(chat, 60*1000));
 	}
 
 	@Override
 	protected void init(VaadinRequest request) {
 		
		
 		final ChatBox chatbox = new ChatBox(chat);
 		chatbox.setShowSendButton(false);
 		chatbox.setSizeFull();
 		
 		final VerticalLayout layout = new VerticalLayout();
 		
 		HorizontalLayout hola = new HorizontalLayout();
 		final TextField tf = new TextField("Nick:");
 		Button b = new Button("Join chat");
 		b.addClickListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				ChatUser user = ChatUser.newUser(tf.getValue());
 				chatbox.setUser(user);
				chat.addLine(new ChatLine(user+" joined."));
 				chatbox.focusToInputField();
 			}
 		});
 		hola.addComponent(tf);
 		hola.addComponent(b);
 		hola.setComponentAlignment(b, Alignment.BOTTOM_LEFT);
 		hola.setMargin(true);
 		layout.addComponent(hola);
 		
 		layout.setSizeFull();
 		layout.addComponent(chatbox);
 		layout.setExpandRatio(chatbox, 1);
 		layout.setMargin(true);
 		setContent(layout);
 	}
 
 }

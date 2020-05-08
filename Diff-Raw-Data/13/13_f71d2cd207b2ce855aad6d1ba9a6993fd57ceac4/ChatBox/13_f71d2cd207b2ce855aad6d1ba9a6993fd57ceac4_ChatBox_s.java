 package org.vaadin.chatbox;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vaadin.chatbox.SharedChat.ChatListener;
 import org.vaadin.chatbox.client.ChatBoxClientRpc;
 import org.vaadin.chatbox.client.ChatBoxServerRpc;
 import org.vaadin.chatbox.client.ChatBoxState;
 import org.vaadin.chatbox.client.ChatLine;
 import org.vaadin.chatbox.client.ChatUser;
 
 import com.vaadin.annotations.StyleSheet;
 import com.vaadin.ui.UI;
 
 @StyleSheet("chatbox.css")
 @SuppressWarnings("serial")
 public class ChatBox extends com.vaadin.ui.AbstractComponent implements ChatListener {
 
 	// To process events from the client, we implement ServerRpc
 	private ChatBoxServerRpc rpc = new ChatBoxServerRpc() {
 		@Override
 		public void lineAdded(ChatBoxState.Line line) {
 			chat.addLine(ChatBoxState.Line.convert(line));
 		}
 	};
 
 	private SharedChat chat;
 
 	private UI ui;
 
	private int numFrozenLines = 0;
 
 	public ChatBox(SharedChat chat) {
 		super();
 		setWidth("200px"); // ?
 		setHeight("200px"); // ?
 		this.chat = chat;
 		registerRpc(rpc);
 	}
 	
 	public void setUser(ChatUser user) {
 		ChatBoxState.User u = new ChatBoxState.User();
 		u.id = user.getId();
 		u.name = user.getName();
 		u.style = user.getStyle();
 		getState(true).user = u;
 	}
 	
 	public void setShowSendButton(boolean show) {
 		getState(true).showSendButton = show;
 	}
 	
 	@Override
 	public void attach() {
 		super.attach();
 		this.ui = UI.getCurrent();
 		chat.addListener(this);
 	}
 	
 	@Override
 	public void detach() {
 		chat.removeListener(this);
 		super.detach();
 	}
 	
 	@Override
 	public ChatBoxState getState(boolean markAsDirty) {
 		return (ChatBoxState) super.getState(markAsDirty);
 	}
 	
 	@Override
 	public ChatBoxState getState() {
 		return (ChatBoxState) super.getState();
 	}
 
 	@Override
 	public void lineAdded(ChatLine line) {
 		ui.access(new Runnable() {
 			@Override
 			public void run() {
 				ChatBox.this.markAsDirty();
 			}
 		});
 	}
 	
 	@Override
     public void beforeClientResponse(boolean initial) {
 		super.beforeClientResponse(initial);
		List<ChatLine> lines = chat.getLinesStartingFrom(numFrozenLines);
 		if (!lines.isEmpty()) {
			numFrozenLines += lines.size();
 			ArrayList<ChatBoxState.Line> lis = new ArrayList<ChatBoxState.Line>(lines.size());
 			for (ChatLine line : lines) {
 				lis.add(ChatBoxState.Line.convert(line));
 			}
 			getRpcProxy(ChatBoxClientRpc.class).addLines(lis);
 		}
 	}
 
 	public void focusToInputField() {
 		getRpcProxy(ChatBoxClientRpc.class).focus();
 	}
 }

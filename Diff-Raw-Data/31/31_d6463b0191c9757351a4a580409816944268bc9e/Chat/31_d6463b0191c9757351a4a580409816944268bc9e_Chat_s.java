 package com.cahoots.chat;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.ui.PlatformUI;
 
 import com.cahoots.connection.websocket.CahootsSocket;
 import com.cahoots.eclipse.collab.ChatDialog;
 import com.cahoots.eclipse.swt.SwtDisplayUtils;
 import com.cahoots.events.ChatReceivedEventListener;
 import com.cahoots.json.receive.ChatReceiveMessage;
 import com.google.inject.Inject;
 
 public class Chat {
 
 	private Map<String, ChatDialog> chats = new HashMap<String, ChatDialog>();
 
 	@Inject
 	public Chat(CahootsSocket socket) {
 		socket.addChatReceivedEventListener(new ChatReceivedEventListener() {
 
 			@Override
			public void onEvent(ChatReceiveMessage msg) {
				final ChatDialog dia;
				if (!chats.containsKey(msg.getFrom())) {
					dia = new ChatDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), Arrays.asList(new String[]{msg.getFrom()}));

					chats.put(msg.getFrom(), dia);
				} else {
					dia = chats.get(msg.getFrom());
				}
				dia.receiveMessage(msg);
 				SwtDisplayUtils.async(new Runnable() {
					
 					@Override
 					public void run() {
 						dia.open();
 					}
 				});
 			}
 		});
 	}
 
 	public void startChat(String to) {
 		final ChatDialog dia;
 		if (!chats.containsKey(to)) {
 			dia = new ChatDialog(PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow().getShell(), Arrays.asList(new String[]{to}));
 			chats.put(to, dia);
 		} else {
 			dia = chats.get(to);
 		}
 		chats.put(to, dia);
 		SwtDisplayUtils.async(new Runnable() {
 			@Override
 			public void run() {
 				dia.open();
 			}
 		});
 	}
 }

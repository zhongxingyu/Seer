 package com.narrowtux.shortcuts;
 
 import org.bukkit.entity.Player;
 
 import com.narrowtux.Assistant.Assistant;
 import com.narrowtux.Assistant.AssistantAction;
 import com.narrowtux.Assistant.AssistantPage;
 
 public class ShortcutSetupAssistant extends Assistant {
 	public ShortcutAction action = null;
 	public Shortcut shortcut = null;
 	public ShortcutPlayer player;
 	
 	public ShortcutSetupAssistant(Player p) {
 		super(p);
 		setTitle("Shortcut setup");
 		player = ShortcutPlayer.get(p);
 		player.setCurrentAssistant(this);
 		action = new ShortcutAction(player);
 		addPage(new AssistantPage(this){
 			{
 				setTitle("");
 				setText("Press the shortcut.\n Enter text to chat or a command to action it.\n Type 'done' to leave.\n Type 'cancel' to cancel setup.");
 			}
 			@Override
 			public AssistantAction onPageInput(String text){
 				if(text.equals("done")){
 					if(shortcut==null)
 					{
 						sendMessage("No shortcut defined.");
 						return AssistantAction.SILENT_REPEAT;
 					}
 					if(player.getAction(shortcut)!=null){
 						sendMessage("An action for this shortcut already exists.");
 						return AssistantAction.SILENT_REPEAT;
 					}
 					return AssistantAction.FINISH;
 				} if(text.equals("cancel")){
 					return AssistantAction.CANCEL;
 				} else {
 					action.addChat(text);
					sendMessage("Added message '"+text+"'.");
 					return AssistantAction.SILENT_REPEAT;
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void onAssistantFinish(){
 		player.addAction(shortcut, action);
 		player.setCurrentAssistant(null);
 		sendMessage("Shortcut set up.");
 		ShortcutsMain.instance.save();
 	}
 	
 	@Override
 	public void onAssistantCancel(){
 		player.setCurrentAssistant(null);
 		sendMessage("Shortcut setup cancelled.");
 	}
 
 }

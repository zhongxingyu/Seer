 package net.minecraft.src;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Collections;
 
 import org.lwjgl.input.Keyboard;
 
 import net.minecraft.client.Minecraft;
 
 public class mod_MCMenu extends BaseMod {
 	private static GuiScreen currentmenu;
 	private boolean serverNotified;
 	private static KeyBinding quickMenu;
 	/*Menu key index*/
 	private int quickMenuKey = Keyboard.KEY_K;
 	
 	public mod_MCMenu()
 	{
 		ModLoader.SetInGameHook(this,
                 true,
                 true); // every tick NOT frame
         serverNotified = false;
 		//currentmenu = new MCMenuGui();
 		//RegisterKey(BaseMod basemod, KeyBinding keybinding, boolean flag)
 		quickMenu = new KeyBinding("Activate Quick menu", 'k');
 		ModLoader.RegisterKey(this, quickMenu, true); // false for non-repeat I think
 		
 	}
 	
 	@Override
 	public String Version() {
 		return "0.1a";
 	}
 	
 	@Override
 	public void KeyboardEvent(KeyBinding keybinding)
     {
 		Minecraft game = ModLoader.getMinecraftInstance();
        
 		if (game.currentScreen==null && keybinding.keyCode == 'k')
 		{
 			game.thePlayer.sendChatMessage("Pushing "+keybinding.keyDescription+" : "+keybinding.keyCode+" makes me feel funny");
 			game.thePlayer.sendChatMessage("/quick");
 		}
     }
 	
 	public void OnTickInGame(Minecraft game)
 	{
 		if( game.isMultiplayerWorld() )
 		{
 			if( !serverNotified )
 			{
 				game.thePlayer.sendChatMessage("/menu modinstalled");
 				serverNotified = true;
 			}
 		}
 		else 
 			serverNotified = false;
 		
 		if (game.currentScreen==null && Keyboard.isKeyDown(quickMenuKey))
 		{
			game.thePlayer.sendChatMessage("/qm");
 		}
 				
 		// Check text messages :|
 		try {
 			@SuppressWarnings("unchecked")
 			ArrayList<ChatLine> chatLog = (ArrayList<ChatLine>) ModLoader.getPrivateValue(GuiIngame.class, game.ingameGUI, "e"); // 'chatMessageList' obf to 'e'
 			
 			// Chats are stored in reverse order
 			Collections.reverse(chatLog);
 			
 			if( chatLog.size() > 0 )
 			{
 					String title = "Test Menu";
 					LinkedList<String> options = new LinkedList<String>();
 					int firstIndex = -1, lastIndex = -1;
 					for(int m = 0; m < chatLog.size(); m++)
 					{
 						ChatLine chat = chatLog.get(m); 
 						// 0,9,8....1 Title
 						if( chat.message.startsWith("##Menu_") ) // Titile row
 						{
 							title = chat.message.substring(7); // Title in message before
 							firstIndex = m; // also remove title
 							m++;
 							for(; m < chatLog.size() && options.size() < 10; m++)
 							{
 								chat = chatLog.get(m);
 								if( startsWithDigit(chat.message) )
 									options.add(chat.message);
 								else
 									break;
 							}
							
							if( options.size() < 1 ||  // no options
								m >= chatLog.size() || // End of messages
								!chatLog.get(m).message.startsWith("##EndMenu") ) // last option != EndMenu
 							{
 								// we havent got it all yet..
 								Collections.reverse(chatLog);
 								return;
 							}
 							lastIndex = m;
 							
 							String[] o = new String[1];
 							o = options.toArray(o);
 							currentmenu = new MCMenuGui(title, o);
 							game.displayGuiScreen(currentmenu);
 							
 							break;
 						}
 						else if( chat.message.startsWith("##Value_") )
 						{
 							firstIndex = m;
 							lastIndex = m;
 							title = chat.message.substring(8);
 							currentmenu = new MCMenuValueGui(title);
 							game.displayGuiScreen(currentmenu);
							break;
 						}
 							
 					}
 					
 					if(firstIndex >= 0 )
 						for(;lastIndex >= firstIndex; lastIndex--)
 						{
 							chatLog.remove(lastIndex);
 						}
 							
 							
 					Collections.reverse(chatLog);
 					
 			
 			}
 			
 		} catch (IllegalArgumentException e) {
 			String title = "IllegalArgumentException";
 			String[] options = new String[1];
 			options[0] = "Sad Face";
 			currentmenu = new MCMenuGui(title, options);
 			game.displayGuiScreen(currentmenu);
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			String title = "SecurityException";
 			String[] options = new String[1];
 			options[0] = "Sad Face";
 			currentmenu = new MCMenuGui(title, options);
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			String title = "NoSuchFieldException";
 			String[] options = new String[1];
 			options[0] = "Sad Face";
 			currentmenu = new MCMenuGui(title, options);
 			e.printStackTrace();
 		}
 		
 		
 		
 	}
 	
 	// I think UTF encoding is screwing up charAt stuff
 	public boolean startsWithDigit(String s)
 	{
 		return s.matches(".?[0-9].*");
 	}
 	
 	/*public boolean startsWithDigit(String s)
 	{
 		if( isDigit(s.charAt(0)) ) // No colour prefix
 			return true;
 		else if( isDigit(s.charAt(1)) ) // dunno prefix
 			return true;
 		else if( isDigit(s.charAt(2)) ) // dunno prefix
 			return true;
 		else
 			return isDigit(s.charAt(3)); // colour prefix
 		
 	}*/
 	// Helper
 	public boolean isDigit(char c)
 	{
 		if( c >= '1' && c <= '0')
 			return true;
 		return false;
 	}
 	
 	
 
 }

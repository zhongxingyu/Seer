 package com.sk83rsplace.arkane.menus;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 
 import com.sk83rsplace.arkane.GUI.Menu;
 import com.sk83rsplace.arkane.GUI.components.ButtonComponent;
 import com.sk83rsplace.arkane.GUI.components.CheckBoxComponent;
 import com.sk83rsplace.arkane.GUI.components.LabelComponent;
 import com.sk83rsplace.arkane.GUI.components.TextComponent;
 import com.sk83rsplace.arkane.GUI.components.TextInputComponent;
 import com.sk83rsplace.arkane.HTTP.HTTP;
 import com.sk83rsplace.arkane.client.Board;
 
 public class MainMenu extends Menu {
 	private TextComponent errorMessage;
 	private ArrayList<CheckBoxComponent> characterComponents = new ArrayList<CheckBoxComponent>();
 	
 	public MainMenu() {
 		errorMessage = new TextComponent("", Color.yellow, -1, 360);
 
 		addComponent(new TextComponent("Welcome " + Board.username + ",", Color.white, -1, 50));
 		addComponent(new TextComponent("Select your Character:", Color.white, -1, 66));
 		
 		getCharacters();
 		
 		addComponent(new TextComponent("Join a Server:", Color.white, -1, Board.getHeight() - 15 - 50 - 64 - 32));
 		addComponent(new TextInputComponent("Server Host", "vps.kieraan.co.uk", -1, Board.getHeight() - 15 - 50 - 64));
 		addComponent(new ButtonComponent("Create New", 15, Board.getHeight() - 15 - 50) {
 			public void onClick() {
 				Board.menuStack.pop();
 				Board.menuStack.add(new CreationMenu());
 			}
 		});
 		addComponent(new ButtonComponent("Continue", Board.getWidth() - 15 - 258, Board.getHeight() - 15 - 50));
 		addComponent(errorMessage);
 	}
 	
 	
 	private void getCharacters() {
 		Map<String, String> params = new HashMap<String, String>();
 		
 		params.put("userID", "" + (Board.userID));
 		
 		HTTP httpConnection = new HTTP();
 		String JSONResult = httpConnection.post("http://vps.kieraan.co.uk/~Josh/listCharacters.php", params);
 		
 		try {
 			JSONObject JSONParser = new JSONObject(JSONResult);
 			JSONArray characters = JSONParser.getJSONArray("characters");
 			
 			for(int index = 0; index < characters.length(); index++) {
 				final JSONObject character = characters.getJSONObject(index);
 				CheckBoxComponent component = new CheckBoxComponent((index == 0 ? true : false), true, 360, 98 + (index * 32)) {
 					public void onInitialization(GameContainer container) {
 						try {
 							addComponent(new LabelComponent(character.getString("name"), this));
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 					
 					public void onValueChange() {
 						try {
 							if(getValue()) {
 								System.out.println(character.getInt("character_id"));
 								Board.characterID = character.getInt("character_id");
 								
 								for(CheckBoxComponent c : characterComponents)
 									if(c != this)
 										c.setValue(false);
 							}
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				};
 				
 				if(index == 0)
 					Board.characterID = character.getInt("character_id");
 					
 				characterComponents.add(component);
 				addComponent(component);
 			}
			
			if(characters.length() == 0)
				addComponent(new TextComponent("You don't have any Characters!", Color.orange, -1, 98));
 		} catch (JSONException e) {
 			e.printStackTrace();
 			errorMessage.setValue("Unexpected Error!");
 		}
 	}
 }

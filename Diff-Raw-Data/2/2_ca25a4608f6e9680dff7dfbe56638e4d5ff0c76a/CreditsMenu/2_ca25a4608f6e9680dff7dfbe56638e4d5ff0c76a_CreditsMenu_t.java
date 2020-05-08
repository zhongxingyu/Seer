 package com.sk83rsplace.arkane.menus;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.util.ResourceLoader;
 
 import com.sk83rsplace.arkane.GUI.Component;
 import com.sk83rsplace.arkane.GUI.Fonts;
 import com.sk83rsplace.arkane.GUI.Menu;
 import com.sk83rsplace.arkane.GUI.components.TextComponent;
 import com.sk83rsplace.arkane.client.Board;
 
 public class CreditsMenu extends Menu {
 	private int creditsHeight = 0, creditsOffsetY = 0;
 	
 	public CreditsMenu() {
 		generateCredits();
 	}
 	
 	private void generateCredits( ){
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream("res/credits.txt"), "UTF-8"));
 			String line = br.readLine();
 			int yOffset = 16;
 			creditsHeight += 16;
 			
 			while(line != null) {
 			    if(line != null) {
 			    	String[] chunks = line.split(" ");
 			    	TrueTypeFont font = Fonts.normalPoint;
 			    	
 			    	switch(chunks[0]) {
 				    	case "LARGE_HEADER":
 				    		line = line.replace(chunks[0] + " ", "");
 				    		font = Fonts.largePoint;
 				    		break;
 				    	case "MEDIUM_HEADER":
 				    		line = line.replace(chunks[0] + " ", "");
 				    		font = Fonts.mediumPoint;
 				    		break;
 			    	}
 
 			    
 			    	addComponent(new TextComponent(line, Color.white, font, -1, yOffset));
 			    	yOffset += font.getLineHeight() + 16;
 			    	creditsHeight += font.getLineHeight() + 16;
 			    	line = br.readLine();
 			    }
 			}
 			
 		    br.close();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void update(GameContainer container) {
 		super.update(container);
 		
 		if(creditsOffsetY < creditsHeight) {
 			for(Component c : components) {
				c.set(c.getX(), (c.getY() - 1 != -1 ? c.getY() - 1 : c.getY() - 2));
 			}
 			
 			creditsOffsetY++;
 		} else {
 			Board.menuStack.pop();
 			Board.menuStack.add(new MainMenu());
 		}
 	}
 }

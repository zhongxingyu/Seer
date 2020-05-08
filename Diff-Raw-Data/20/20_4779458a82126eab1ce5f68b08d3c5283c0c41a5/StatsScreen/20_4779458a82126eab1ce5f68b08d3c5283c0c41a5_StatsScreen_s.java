 package com.punchline.NinjaSpacePirate.screens;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.NinjaSpacePirate.gameplay.Stat;
 import com.punchline.NinjaSpacePirate.gameplay.Stats;
 import com.punchline.NinjaSpacePirate.screens.MenuButton.ButtonCallback;
 import com.punchline.javalib.Game;
 import com.punchline.javalib.utils.Display;
 
 public class StatsScreen extends MenuScreen {
 	
 	@SuppressWarnings("rawtypes")
 	public StatsScreen(Game game) {
 		super(game, "STATS");
 		
 		Stats.loadStats();
 		
 		Iterator<Entry<String, Stat>> it = Stats.getRecords().entrySet().iterator();
 		
 		while (it.hasNext()) {
 			Entry<String, Stat> entry = it.next();
 			
 			MenuButton button = new MenuButton(font, entry.getKey() + ":" + entry.getValue().formatValue());
 			buttons.add(button);
 		}
 		
 		buttons.add(new MenuButton(font, ""));
 		
 		it = Stats.getStats().entrySet().iterator();
 		
 		while (it.hasNext()) {
 			Entry<String, Stat> entry = it.next();
 			
 			MenuButton button = new MenuButton(font, entry.getKey() + ":" + entry.getValue().formatValue());
 			buttons.add(button);
 		}
 		
 		buttons.add(new MenuButton(font, ""));
 		
 		MenuButton backButton = new MenuButton(font, "Back");
 		backButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				exit();
 			}
 			
 		};
 		
 		buttons.add(backButton);
 	}
 	
 	@Override
 	public void render(float delta) {
 		Vector2 position = new Vector2(Display.getPreferredWidth() / 2, 2 * Display.getPreferredHeight() / 3);
 		
 		spriteBatch.begin();
 		
 		float x = Display.getPreferredWidth() / 2;
 		float y = 7 * Display.getPreferredHeight() / 8;
 		
 		TextBounds bounds = font.getBounds(title);
 		
 		font.draw(spriteBatch, title, x - bounds.width / 2, y - bounds.height / 2);
 		
 		font.setScale(0.6f);
 		
 		for (MenuButton button : buttons) {
 			button.draw(spriteBatch, position);
 			
 			position.y -= button.getBounds().height;
 			position.y -= LINE_PADDING;
 		}
 		
 		font.setScale(1f);
 		
 		spriteBatch.end();
 	}
 	
 }

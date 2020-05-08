 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones.screens;
 
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * @author jami
  *
  */
 public class GameSetupScreen extends AbstractScreen {
 	public String LOG = "GameSetupScreen";
 	GameSetupConfig config;
 	
 	Array<Player> pList;

    boolean isHost = false;
 	
 	/**
 	 * @param game
 	 */
 	public GameSetupScreen(VOBGame game, GameSetupConfig cfg) {
 		super(game);
 		config = cfg;
 		game.createGame(cfg);
 	}
 	
 	public void setupScreen() {
 		
 		Label gameTypeLbl = new Label("Game Type: " + (config.isMulti ? "Multiplayer" : "Single Player"), getSkin());
 		
 		Label mapLbl = new Label("Map: " + config.mapName, getSkin());
 		
 		Table table = new Table(getSkin());
 		table.setFillParent(true);
 		stage.addActor(table);
 		
 		table.add(gameTypeLbl).colspan(7).center();
 		table.row();
 		table.add(mapLbl).colspan(7);
 		table.row();
 
 		
 		TextButton addTeam1AI = new TextButton("+AI", getSkin());
 		addTeam1AI.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				game.addAIPlayer(0);
 			}
 			
 		});
 		
 		TextButton addTeam2AI = new TextButton("+AI", getSkin());
 		addTeam2AI.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				game.addAIPlayer(1);
 			}
 			
 		});
 		
 		table.add(new Label("Team One", getSkin())).colspan(2);
 		if (!config.isMulti || config.isHost) {
 			table.add(addTeam1AI).right().pad(4);
 		} else {
 			table.add();
 		}
 		table.add();
 		table.add(new Label("Team Two", getSkin())).colspan(2);
 		if (!config.isMulti || config.isHost) {
 			table.add(addTeam2AI).right().pad(4);
 		} else {
 			table.add();
 		}
 		table.row();
 		
 		
 		Array<Player> team1 = new Array<Player>();
 		Array<Player> team2 = new Array<Player>();
 		
 		pList = new Array<Player>();
 		pList.addAll(game.getPlayers());
 		
 		for (Player p: pList) {
 			if (p.getTeam() == 0) team1.add(p);//team1.add(String.format("%s (%d)", p.getPlayerName(), p.getTeam()));
 			if (p.getTeam() == 1) team2.add(p);//team2.add(String.format("%s (%d)", p.getPlayerName(), p.getTeam()));
 		}
 		
 		for (int i=0; i < team1.size || i < team2.size; i++) {
 			if (i < team1.size) {
 				table.add(new Label(String.format("%s (%d)", team1.get(i).getPlayerName(), team1.get(i).getPlayerId()), getSkin())).left().colspan(2);
 				
 				if (config.isHost && team1.get(i).getPlayerId() != game.getPlayer().getPlayerId()) {
 					table.add(getRemovePlayerButton(team1.get(i))).right();
 				} else {
 					table.add();
 				}
 				
 				
 			} else {
 				table.add().colspan(3);
 			}
 			
 			table.add();
 			
 			if (i < team2.size) {
 				table.add(new Label(String.format("%s (%d)", team2.get(i).getPlayerName(), team2.get(i).getPlayerId()), getSkin())).left().colspan(2);
 				
 				if (config.isHost && team2.get(i).getPlayerId() != game.getPlayer().getPlayerId()) {
 					table.add(getRemovePlayerButton(team2.get(i))).right();
 				} else {
 					table.add();
 				}
 			} else {
 				table.add().colspan(3);
 			}
 			table.row();
 		}
 		
 
 		table.add().colspan(5);
 
 		
 		if (!config.isMulti || config.isHost) {
            isHost = true;
		    TextButton start = new TextButton("Start Game",getSkin());
 			start.addListener(new ClickListener() {
 	
 				/* (non-Javadoc)
 				 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 				 */
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					super.touchUp(event, x, y, pointer, button);
 					
 					game.sendStartGame();
 				}
 				
 			});
 		table.add(start).size(150, 50).pad(4).right().bottom().colspan(2);
 		
		} else {
            isHost = false;
        }
 		table.row();
 		
 
 		table.add().colspan(5);
 
 		
 		TextButton cancel = new TextButton("Cancel", getSkin(), "cancel");
 		cancel.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				super.touchUp(event, x, y, pointer, button);
 				
 				game.setScreen(game.getMainMenuScreen());
 				game.closeGame();
 				
 			}
 			
 		});
 		
 		table.add(cancel).size(150, 50).pad(4).right().bottom().colspan(2);
 		
 		table.row();
 	}
 	
 	private ImageButton getRemovePlayerButton(final Player p) {
 		ImageButton remove = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("data/tread32.png"))))); 
 		remove.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				Gdx.app.log(LOG, String.format("remove (%d)", p.getPlayerId()));
 				game.removePlayer(p.getPlayerId());
 			}
 			
 		});
 		
 		return remove;
 	}
 
 	/**
 	 * Implemented methods
 	 */
 	
 	@Override
 	public void show() {
 		super.show();
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		
 		setupScreen();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ahsgaming.spacetactics.screens.AbstractScreen#render(float)
 	 */
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		//Gdx.app.log(LOG, Integer.toString(game.getPlayers().size()));
 		Array<Player> players = game.getPlayers();
 		
 		synchronized (players) {
			if (!pList.equals(players) || isHost != config.isHost) {
 				stage.clear();
 				setupScreen();
 			}
 		}
 	}
 	
 	public static class GameSetupConfig {
 		public String mapName = "blank.tmx";
 		public boolean isMulti = false;
 		public boolean isHost = true;
 		public String hostName = "localhost";
 		public String playerName = "New Player";
 	}
 
 
 
 	
 
 }

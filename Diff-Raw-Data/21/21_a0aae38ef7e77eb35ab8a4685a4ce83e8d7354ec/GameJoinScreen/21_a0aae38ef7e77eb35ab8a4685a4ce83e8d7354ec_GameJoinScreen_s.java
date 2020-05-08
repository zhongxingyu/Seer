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
 package com.ahsgaming.spacetactics.screens;
 
 import com.ahsgaming.spacetactics.SpaceTacticsGame;
 import com.ahsgaming.spacetactics.screens.GameSetupScreen.GameSetupConfig;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 /**
  * @author jami
  * This screen should display a textbox for the user to enter his nickname 
  * (registration later?), a tabbed interface to select either local games
  * (autodetect these) or WAN games (grab a list from ahsgaming.com?) 
  */
 public class GameJoinScreen extends AbstractScreen {

 	Label lblNickname;
 	TextField txtNickname;
 	
 	Label lblJoinHostname;
 	TextField txtJoinHostname;
 	
 	TextButton btnConnect;
 	TextButton btnCancel;
 	
 	Label lblStatus;
 	
 	GameSetupScreen gsScreen = null;
 	
 	/**
 	 * @param game
 	 */
 	public GameJoinScreen(SpaceTacticsGame game) {
 		super(game);
 		
 	}
 	
 	public void setupScreen() {
 		Table table = new Table(getSkin());
 		table.setFillParent(true);
 		stage.addActor(table);
 		
 		lblNickname = new Label("Nickname:", getSkin());
 		table.add(lblNickname);
 		
 		txtNickname = new TextField("Newb", getSkin());
 		table.add(txtNickname);
 		
 		table.row();
 		
 		table.row();
 		
 		lblJoinHostname = new Label("Join Server:", getSkin());
 		table.add(lblJoinHostname);
 		
 		txtJoinHostname = new TextField("", getSkin());
 		table.add(txtJoinHostname);
 		
 		table.row();
 		
 		btnCancel = new TextButton("Cancel", getSkin());
 		table.add(btnCancel);
 		
 		btnConnect = new TextButton("Connect", getSkin());
 		table.add(btnConnect);
 		
 		table.row();
 		
 		
 		lblStatus = new Label("", getSkin());
 		table.add(lblStatus).colspan(2);
 		
 		
 		btnCancel.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				game.setScreen(game.getMainMenuScreen());
 			}
 		});
 		
 		btnConnect.addListener(new ClickListener() {
 
 			/* (non-Javadoc)
 			 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
 			 */
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				GameSetupConfig cfg = new GameSetupConfig();
 				cfg.hostName = txtJoinHostname.getText();
 				cfg.isHost = false;
 				cfg.isMulti = true;
 				cfg.playerName = txtNickname.getText();
 				gsScreen = game.getGameSetupScreenMP(cfg);
 				lblStatus.setText(String.format("Connecting to host %s", cfg.hostName));
 			}
 			
 		});
 		
 		
 	}
 	
 	
 	
 
 	/* (non-Javadoc)
 	 * @see com.ahsgaming.spacetactics.screens.AbstractScreen#show()
 	 */
 	@Override
 	public void show() {
 		super.show();
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ahsgaming.spacetactics.screens.AbstractScreen#resize(int, int)
 	 */
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
 		
 		if (gsScreen != null) {
 			if (game.isConnected()) {
 				game.setScreen(gsScreen);
 			} else if (!game.isConnecting()) {
 				this.lblStatus.setText(String.format("Failed to connect to host (%s)", 
 						gsScreen.config.hostName));
 				gsScreen = null;
 				game.closeGame();
 			}
 		}
 	}
 	
 	
 
 }

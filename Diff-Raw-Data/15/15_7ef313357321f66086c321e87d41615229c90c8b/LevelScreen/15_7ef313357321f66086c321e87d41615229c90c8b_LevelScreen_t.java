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
 
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.GameObject;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.TextureManager;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.map.HexMap;
 import com.ahsgaming.valleyofbones.network.Attack;
 import com.ahsgaming.valleyofbones.network.Build;
 import com.ahsgaming.valleyofbones.network.Command;
 import com.ahsgaming.valleyofbones.network.EndTurn;
 import com.ahsgaming.valleyofbones.network.Move;
 import com.ahsgaming.valleyofbones.network.Upgrade;
 import com.ahsgaming.valleyofbones.units.Prototypes;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Buttons;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 
 /**
  * @author jami
  *
  */
 public class LevelScreen extends AbstractScreen {
 	public String LOG = "LevelScreen";
 	
 	private Group grpLevel;
 	
 	private GameController gController = null;
 	
 	// input stuff
 	private Vector2 boxOrigin = null, boxFinal = null;
 	private boolean rightBtnDown = false;
 	
 	ShapeRenderer shapeRenderer;
 	
 	
 	// camera 'center' position - this will always remain within the bounds of the map
 	private Vector2 posCamera = new Vector2();
 	
 	
 	// game stuff
 	int sinceLastGameTick = 0;
 	int sinceLastNetTick = 0;
 	
 	// UX stuff
 	Group grpScorePane = new Group();
 	ObjectMap<Player, Label> mapScoreLbls = new ObjectMap<Player, Label>();
 	
 	Group grpTurnPane = new Group();
 	Label lblTurnTimer;
 	Button btnTurnDone;
 	
 	Group grpPreviews = new Group();
 	Array<Command> commandsPreviewed = new Array<Command>();
 
 	private boolean vKeyDown;
 	
 	/**
 	 * @param game
 	 */
 	public LevelScreen(VOBGame game, GameController gController) {
 		super(game);
 		this.gController = gController;
 		shapeRenderer = new ShapeRenderer();
 	}
 	
 	/**
 	 * Methods
 	 */
 	
 	private void clampCamera() {
 		HexMap map = gController.getMap();
 		
 		if (posCamera.x < 0) posCamera.x = 0;
 		if (posCamera.x > map.getWidth() * map.getTileWidth()) posCamera.x = map.getWidth() * map.getTileWidth();
 		
 		if (posCamera.y < 0) posCamera.y = 0;
 		if (posCamera.y > map.getHeight() * map.getTileHeight() * 0.75) posCamera.y = map.getHeight() * map.getTileHeight() * 0.75f;
 	}
 	
 	private void drawSelectionBox() {
 		if (!(boxOrigin == null || boxFinal == null)) {
 			shapeRenderer.begin(ShapeType.Rectangle);
 			shapeRenderer.setColor(1, 1, 1, 1);
 			Vector2 start = mapToScreenCoords(boxOrigin.x, boxOrigin.y), end = mapToScreenCoords(boxFinal.x, boxFinal.y);
 			shapeRenderer.rect(start.x, start.y, end.x - start.x, end.y - start.y);
 			shapeRenderer.end();
 		}
 	}
 	
 	private void drawUnitBoxes() {
 		if (gController.getSelectedObject() != null) {
 			GameObject obj = gController.getSelectedObject();
 			shapeRenderer.begin(ShapeType.Line);
 			shapeRenderer.setColor((obj.getOwner() != null ? obj.getOwner().getPlayerColor() : new Color(1, 1, 1, 1)));
 			Vector2 start = gController.getMap().boardToMapCoords(obj.getBoardPosition().x, obj.getBoardPosition().y);
 			start = mapToScreenCoords(start.x, start.y);
 			
 			Vector2 base = new Vector2(start.x, start.y);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth() * 0.5f, base.y, base.x, base.y + gController.getMap().getTileHeight() * 0.25f);
 			shapeRenderer.line(base.x, base.y + gController.getMap().getTileHeight() * 0.25f, base.x, base.y + gController.getMap().getTileHeight() * 0.75f);
 			shapeRenderer.line(base.x, base.y + gController.getMap().getTileHeight() * 0.75f, base.x + gController.getMap().getTileWidth() * 0.5f, base.y + gController.getMap().getTileHeight());
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth() * 0.5f, base.y + gController.getMap().getTileHeight(), base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.75f);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.75f, base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.25f);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.25f, base.x + gController.getMap().getTileWidth() * 0.5f, base.y);
 			shapeRenderer.end();
 			
 			shapeRenderer.begin(ShapeType.FilledRectangle);
 			// TODO move this elsewhere?
 			if (obj instanceof Unit) {
 				for (Command cmd: ((Unit)obj).getCommandQueue()) {
 					Vector2 point = null;
 					if (cmd instanceof Attack) {
 						Unit target = (Unit) gController.getObjById(((Attack) cmd).target);
 						if (target != null) {
 							point = target.getPosition("center");
 						}
 					} else if (cmd instanceof Move) {
 						point = ((Move)cmd).toLocation;
 					}
 					
 					if (point != null) {
 						point = mapToScreenCoords(point.x, point.y);
 						
 						shapeRenderer.setColor((obj.getOwner() != null ? obj.getOwner().getPlayerColor() : new Color(1, 1, 1, 1)));
 						shapeRenderer.filledRect(point.x - 5, point.y - 5, 10, 10);
 					}
 				}
 			} else {
 				for (Vector2 waypt: obj.getPath()) {
 					Vector2 point = mapToScreenCoords(waypt.x, waypt.y);
 					
 					shapeRenderer.setColor((obj.getOwner() != null ? obj.getOwner().getPlayerColor() : new Color(1, 1, 1, 1)));
 					shapeRenderer.filledRect(point.x - 5, point.y - 5, 10, 10);
 					
 				}
 			}
 			shapeRenderer.end();
 		}
 	}
 	
 	private void doCameraMovement(float delta) {
 		if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.getY() <= game.getMouseScrollSize()) {
 			// move 'up'
 			posCamera.y += game.getKeyScrollSpeed() * delta;
 		} else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.getY() >= stage.getHeight() - game.getMouseScrollSize()) {
 			// move 'down'
 			posCamera.y -= game.getKeyScrollSpeed() * delta;
 		}
 		
 		if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.getX() <= game.getMouseScrollSize()) {
 			// move 'left'
 			posCamera.x -= game.getKeyScrollSpeed() * delta;
 		} else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.getX() >= stage.getWidth() - game.getMouseScrollSize()) {
 			// move 'right'
 			posCamera.x += game.getKeyScrollSpeed() * delta;
 		}
 		
 		clampCamera();
 	}
 	
 	private void doProcessInput(float delta) {
 		Vector2 mapPos = screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY());
 		Vector2 boardPos = gController.getMap().mapToBoardCoords(mapPos.x, mapPos.y);
 		
 		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
 			// TODO fix this - should be able to press (not hold) a key to build
 			
 			
 			if (Gdx.input.isKeyPressed(Keys.B)){
 				// TODO more buttons for more things
 				
 				Vector2 loc = screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY());
 				
 				loc = gController.getMap().mapToBoardCoords(loc.x, loc.y);
 				
 				// TODO should get whether the square is open or not
 				
 				if (game.getPlayer().canBuild("marine-base", gController) && gController.isBoardPosEmpty(loc)) {
 					Build bld = new Build();
 					bld.owner = game.getPlayer().getPlayerId();
 					bld.turn = gController.getGameTurn();
 					bld.building = "marine-base";
 					bld.location = loc;
 					game.sendCommand(bld);
 				}
 			} else {						
 				gController.selectObjAtBoardPos(boardPos);
				if (gController.getSelectedObject() != null && gController.getSelectedObject() instanceof Unit) {
					Unit u = (Unit)gController.getSelectedObject();
					Gdx.app.log(LOG, String.format("Selected: %s (%d/%d)", u.getProtoId(), u.getCurHP(), u.getMaxHP()));
				}
 			}
 		}
 		
 		if (Gdx.input.isButtonPressed(Buttons.RIGHT)){
 			rightBtnDown = true;
 		} else {
 			if (rightBtnDown && gController.getSelectedObject() != null) {
 				// TODO issue context-dependent commands
 				Array<GameObject> objsUnderCursor = null;
 				GameObject target = null;
 				
 				GameObject obj= gController.getSelectedObject();
 				if (objsUnderCursor == null) {
 					objsUnderCursor = gController.getObjsAtPosition(screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY()));
 				
 					for (GameObject cur: objsUnderCursor) {
 						if (cur.getOwner() != game.getPlayer()) {
 							// TODO find the object with the highest 'threat'?
 							target = cur;
 						}
 					}
 				}
 				
 				if (target != null) {
 					// attack this target!
 					Attack at = new Attack();
 					at.owner = game.getPlayer().getPlayerId();
 					at.turn = gController.getGameTurn();
 					at.unit = obj.getObjId();
 					at.target = target.getObjId();
 					at.isAdd = (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT));
 					
 					//game.sendCommand(at);
 					game.sendCommand(at);
 				} else {
 					// move to this location
 					if (gController.isBoardPosEmpty(boardPos)) {
 						Move mv = new Move();
 						mv.owner = game.getPlayer().getPlayerId();
 						mv.turn = gController.getGameTurn();
 						mv.unit = obj.getObjId();
 						mv.toLocation = boardPos;
 						mv.isAdd = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
 						mv.isAttack = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT); // TODO implement real control for a-move
 						
 						game.sendCommand(mv);
 					}
 				}
 				rightBtnDown = false;
 			}
 		}
 		
 		if (!Gdx.input.isButtonPressed(Buttons.RIGHT) && !Gdx.input.isButtonPressed(Buttons.LEFT) && Gdx.input.isKeyPressed(Keys.V) && !vKeyDown) {
 			vKeyDown = true;
 		
 			GameObject obj = gController.getSelectedObject();
 			if (obj instanceof Unit) {
 				Unit u = (Unit)obj;
 				
 				if (game.getPlayer().canUpgrade(u, "attack-upgrade-1", gController)) {
 					Upgrade upg = new Upgrade();
 					upg.turn = gController.getGameTurn();
 					upg.owner = game.getPlayer().getPlayerId();
 					upg.unit = u.getObjId();
 					upg.upgrade = "attack-upgrade-1";
 					
 					game.sendCommand(upg);
 				}
 			}
 			
 		}
 		
 		
 		// TODO make a list of flags or something rather than creating fields for every key that might get pressed...
 		// TODO reset keys 'n' things
 		if (vKeyDown && !Gdx.input.isKeyPressed(Keys.V)) {
 			vKeyDown = false;
 		}
 	}
 	
 	
 	public Vector2 screenToMapCoords(float x, float y) {
 		return new Vector2(x + (posCamera.x - stage.getWidth() * 0.5f), y + (posCamera.y - stage.getHeight() * 0.5f));  
 	}
 	
 	public Vector2 mapToScreenCoords(float x, float y) {
 		return new Vector2(x - (posCamera.x - stage.getWidth() * 0.5f), y - (posCamera.y - stage.getHeight() * 0.5f));
 	}
 	
 	public void showCommandPreviews() {
 		// TODO optimize this
 		grpPreviews.clear();
 		grpPreviews.remove();
 		grpLevel.addActor(grpPreviews);
 		
 		
 		for (Command c: gController.getCommandQueue()) {
 			if (c.owner == game.getPlayer().getPlayerId()) {
 				
 				if (c instanceof Build || c instanceof Move) {
 					Image img = null;
 					Vector2 pos = new Vector2();
 					if (c instanceof Build) {
 						img = new Image(TextureManager.getTexture(Prototypes.getProto(((Build)c).building).image + ".png"));
 						pos = ((Build)c).location;
 					} else if (c instanceof Move) {
 						img = new Image(gController.getObjById(((Move)c).unit).getImage());
 						pos = ((Move)c).toLocation;
 					}
 					Vector2 sPos = gController.getMap().boardToMapCoords(pos.x, pos.y);
 					//sPos = this.mapToScreenCoords(sPos.x, sPos.y);
 					img.setPosition(sPos.x, sPos.y);
 					img.setColor(1, 1, 1, 0.5f);
 					grpPreviews.addActor(img);
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * Implemented methods
 	 */
 	
 	@Override
 	public void show() {
 		super.show();
 		Gdx.app.log(VOBGame.LOG, "LevelScreen#show");
 		
 		grpLevel = gController.getGroup();
 		
 		// TODO set the initial camera position based on the player spawn point
 		posCamera.set(gController.getSpawnPoint(game.getPlayer().getPlayerId()));
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		
 		stage.addActor(grpLevel);
 		
 		Pixmap pix = new Pixmap((int)grpLevel.getWidth(), (int)grpLevel.getHeight(), Pixmap.Format.RGBA8888);
 		pix.setColor(1, 1, 1, 1);
 		pix.drawRectangle(0, 0, pix.getWidth(), pix.getHeight());
 		Image test = new Image(new Texture(pix));
 		grpLevel.addActor(test);
 		
 		
 		// generate the score panel
 		grpScorePane = new Group();
 		mapScoreLbls = new ObjectMap<Player, Label>();
 		int y = 0;
 		if (VOBGame.DEBUG) {
 			for (Player player: gController.getPlayers()) {
 				Label lbl = new Label(player.toString(), getSkin());
 				lbl.setColor(player.getPlayerColor());
 				lbl.setY(y);
 				y += lbl.getHeight() + 5;
 				mapScoreLbls.put(player, lbl);
 				grpScorePane.addActor(lbl);
 				if (grpScorePane.getWidth() < lbl.getWidth()) grpScorePane.setWidth(lbl.getWidth());
 			}
 		}
 		grpScorePane.setPosition(stage.getWidth() - grpScorePane.getWidth() - 10, 264);
 		stage.addActor(grpScorePane);
 		
 		// generate the turn info
 		grpTurnPane = new Group();
 		lblTurnTimer = new Label(" ", new LabelStyle(getLargeFont(), new Color(1,1,1,1)));
 		
 		btnTurnDone = new TextButton("END TURN", getSkin());
 		btnTurnDone.setSize(350, 150);
 
 		lblTurnTimer.setY(btnTurnDone.getHeight());
 		
 		grpTurnPane.addActor(btnTurnDone);
 		grpTurnPane.addActor(lblTurnTimer);
 		
 		grpTurnPane.setSize(btnTurnDone.getWidth(), lblTurnTimer.getTop());
 		
 		stage.addActor(grpTurnPane);
 		
 		
 		
 		btnTurnDone.addListener(new ClickListener() {
 
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				super.clicked(event, x, y);
 				
 				EndTurn et = new EndTurn();
 				et.owner = game.getPlayer().getPlayerId();
 				et.turn = gController.getGameTurn();
 				
 				game.sendCommand(et);
 			}
 			
 		});
 	}
 	
 	public void updateScorePane() {
 		for (Player player: gController.getPlayers()) {
 			Label lbl = mapScoreLbls.get(player);
 			if (lbl != null)
 			{
 				lbl.setText(player.toString());
 				if (grpScorePane.getWidth() < lbl.getWidth()) grpScorePane.setWidth(lbl.getWidth());
 			}
 		}
 		grpScorePane.setX(stage.getWidth() - grpScorePane.getWidth() - 10);
 	}
 	
 	public void updateTurnPane() {
 		lblTurnTimer.setText(String.format("TIME LEFT %02d:%02d", (int)Math.floor(gController.getTurnTimer() / 60), (int)gController.getTurnTimer() % 60));
 		grpTurnPane.setX(stage.getWidth() - grpTurnPane.getWidth());
 	}
 	
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		// draw a debug map
 		//gController.getMap().drawDebug(new Vector2(grpLevel.getX(), grpLevel.getY()));
 		
 		// DRAW BOXES
 		drawSelectionBox();
 		drawUnitBoxes();
 		
 		gController.update(delta);
 		
 		// move the camera around
 		doCameraMovement(delta);
 		
 		// get input
 		doProcessInput(delta);
 		
 		// update level position
 		grpLevel.setPosition(-1 * posCamera.x + stage.getWidth() * 0.5f, -1 * posCamera.y + stage.getHeight() * 0.5f);
 		
 		
 				
 		updateScorePane();
 		
 		updateTurnPane();
 		
 		showCommandPreviews();
 		
 		// easy exit for debug purposes
 		if (Gdx.input.isKeyPressed(Keys.ESCAPE) && VOBGame.DEBUG) {
 			game.quitGame();
 		}
 		
 	}
 	
 	public void addFloatingLabel(String text, float x, float y) {
 		Gdx.app.log(LOG, "Floating Label!");
 		Label lbl = new Label(text, new LabelStyle(getSmallFont(), new Color(1,1,1,1)));
 		lbl.setPosition(x, y);
 		lbl.addAction(Actions.parallel(Actions.fadeOut(1f), Actions.moveBy(0, 64f, 1f)));
 		grpLevel.addActor(lbl);
 	}
 }

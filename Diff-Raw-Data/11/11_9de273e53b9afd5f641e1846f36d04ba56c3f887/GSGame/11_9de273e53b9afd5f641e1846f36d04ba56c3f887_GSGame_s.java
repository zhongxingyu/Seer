 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.java.games.input.Controller;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SavedState;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GSGame extends BasicGameState {
 
 	LifeForm player;
 	ArrayList<LifeForm> enemies = new ArrayList<LifeForm>();
 
 	Image backgroundImage;
 	Image lifeForm;
 
 	boolean upPressed, downPressed, leftPressed, rightPressed;
 	boolean actionPressed;
 
 	int maxActionPoints = 993;
 
 	HitArea hitArea = null;
 
 	
 	int points = 0;
 	int level = 1;
 	int wave = 1;
 	
 	
 	public void init(GameContainer c, StateBasedGame game)
 			throws SlickException {
 		backgroundImage = new Image("background.png");
 		lifeForm = new Image("LifeForm.png");
 
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		super.enter(container, game);
 		points = 0;
 		level = 1;
 		wave = 1;
 		
 		enemies.clear();
 		
 		player = new LifeForm();
 		
 		addEnemies(level,wave);
 		
 	}
 	private void addEnemies(int level, int wave) {
 
 		for(int i = 0; i < level; i++) {
 			enemies.add(new Enemy(null));
 		}
 	}
 	
 	public void render(GameContainer c, StateBasedGame game, Graphics g)
 			throws SlickException {
 		// background
 		g.drawImage(backgroundImage, 0, 0);
 
 		// render enemies
 		for (LifeForm lf : enemies) {
 			lf.render(g);
 		}
 		
 		// render player
 		if(null != player) {
 			player.render(g);
 		} else {
 			g.setColor(Color.white);
 			g.drawString("Game Over!", 200, 290);
 		}
 
 		if (null != hitArea) {
 			hitArea.render(g);
 		}
 
 		g.drawString(String.format("Level: %d, Wave: %d/5", level, wave), 400, 10);
 		g.drawString(String.format("Score: %d", points), 400, 30);
 		//g.drawString(String.format("Enemies: %d", enemies.size()), 100, 50);
 
 	}
 
 	public void update(GameContainer c, StateBasedGame game, int delta)
 			throws SlickException {
 
 		// update enemies
 		for (LifeForm lf : enemies) {
 			lf.update(delta);
 		}
 		
 		if(null != player) {
 			if(player.getState() == LifeForm.STATE_DEAD) {
 				hitArea = null;
 				player = null;
 				
 
 				SavedState savedState = new SavedState("evolutionSavedState");
 				try {
 					savedState.load();
 					double highscore = savedState.getNumber("highscore");
 					
 					if(points > highscore) {
 						savedState.setNumber("highscore", points);
 						savedState.save();
 					}
 					
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 		
 				
 			} else { 
 			
 				player.inputForce.x = (leftPressed ? -1 : 0)
 						+ (rightPressed ? 1 : 0);
 				player.inputForce.y = (upPressed ? -1 : 0)
 						+ (downPressed ? 1 : 0);
 				player.update(delta);
 
 				boolean hit = false;
 				for (LifeForm e : enemies) {
 					if (player.hitTest(e)) {
 						hit = true;
 					}
 				}
 				if (hit) {
 					player.die();
 				}
 			}
 		}
 
 		if (actionPressed) {
 			if (null == player) {
 				game.enterState(LD24Evolution.GAMESTATE_MENU);
 
 			} else {
 				if(player.getState() != LifeForm.STATE_DEAD && player.getState() != LifeForm.STATE_DYING) { 
 
 					if (null == hitArea) {
 						hitArea = new HitArea(player.tailLevel + 2);
 					}
 					Vector2f dropPoint = player.getDropPoint();
 					hitArea.addPoint(dropPoint);
 				}
 			}
 			actionPressed = false;
 		}
 
 		if (null != hitArea) {
 			hitArea.update(delta);
 			if (hitArea.state() == HitArea.STATE_DESTROY) {
 				hitArea = null;
 			} else if(hitArea.state() == HitArea.STATE_CLOSED) {
 
 				hitArea.highlight = false;
 				
 				for (LifeForm e : enemies) {
 					if(hitArea.hitTest(e)) {
 						e.die();
 					}
 				}
 				
 			} else {
 			
 				
 			}
 		}
 
 		int enemiesDied = 0;
 		for(LifeForm e : (ArrayList<LifeForm>)enemies.clone()) {
 			if(e.getState() == LifeForm.STATE_DEAD) {
 				enemies.remove(e);
 				enemiesDied ++;
 				points++;
 			}
 		}
 
 		if(enemies.size() == 0) {
 			wave ++;
 			if(wave > 5) {
 				level ++;
 				wave = 1;
 				//player.speedLevel++;
				player.addTailLevel(1);
 			}
 			
 			addEnemies(level, wave);
 		}
 	}
 
 	@Override
 	public void keyPressed(int key, char c) {
 		// TODO Auto-generated method stub
 		super.keyPressed(key, c);
 
 		switch (key) {
 		case Input.KEY_UP:
 			upPressed = true;
 			break;
 		case Input.KEY_DOWN:
 			downPressed = true;
 			break;
 		case Input.KEY_LEFT:
 			leftPressed = true;
 			break;
 		case Input.KEY_RIGHT:
 			rightPressed = true;
 			break;
 		case Input.KEY_SPACE:
 			actionPressed = true;
 			break;
 		}
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		// TODO Auto-generated method stub
 		super.keyReleased(key, c);
 
 		switch (key) {
 		case Input.KEY_UP:
 			upPressed = false;
 			break;
 		case Input.KEY_DOWN:
 			downPressed = false;
 			break;
 		case Input.KEY_LEFT:
 			leftPressed = false;
 			break;
 		case Input.KEY_RIGHT:
 			rightPressed = false;
 			break;
 //		case Input.KEY_U:
 //			player.addSpeedLevel(1);
 //			break;
 //		case Input.KEY_J:
 //			player.addSpeedLevel(-1);
 //			break;
 //		case Input.KEY_I:
 //			player.addAgilityLevel(1);
 //			break;
 //		case Input.KEY_K:
 //			player.addAgilityLevel(-1);
 //			break;
//		case Input.KEY_O:
//			player.addTailLevel(1);
//			break;
 //		case Input.KEY_L:
 //			player.addTailLevel(-1);
 //			break;
 //		case Input.KEY_A:
 //			enemies.add(new Enemy(null));
 //			break;
 		}
 
 	}
 	
 	@Override
 	public void controllerButtonPressed(int controller, int button) {
 		// TODO Auto-generated method stub
 		super.controllerButtonPressed(controller, button);
 
 		switch (button) {
 		case 1:
 			upPressed = true;
 			break;
 		case 2:
 			downPressed = true;
 			break;
 		case 3:
 			leftPressed = true;
 			break;
 		case 4:
 			rightPressed = true;
 			break;
 		case 12:
 			actionPressed = true;
 			break;
 		}
 
 	}
 
 	@Override
 	public void controllerButtonReleased(int controller, int button) {
 		// TODO Auto-generated method stub
 		super.controllerButtonReleased(controller, button);
 
 		switch (button) {
 		case 1:
 			upPressed = false;
 			break;
 		case 2:
 			downPressed = false;
 			break;
 		case 3:
 			leftPressed = false;
 			break;
 		case 4:
 			rightPressed = false;
 			break;
 		case 15:
 			enemies.add(new Enemy(null));
 			break;
 		}
 	}
 	
 	public int getID() {
 		// TODO Auto-generated method stub
 		return LD24Evolution.GAMESTATE_GAME;
 	}
 
 }

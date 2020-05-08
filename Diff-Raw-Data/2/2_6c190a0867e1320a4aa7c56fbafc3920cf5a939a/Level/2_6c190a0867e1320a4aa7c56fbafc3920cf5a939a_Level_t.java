 /**************************************************************
  *	file:		Level.java
  *	author:		Andrew King, Anthony Mendez, Ghislain Muberwa
  *	class:		CS499 - Game Programming
  *
  *	assignment:	Class Project
  *	date last modified:	
  *
  *	purpose: Creates players and enemies for levels
 **************************************************************/
 package edu.csupomona.kyra.state.level;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import edu.csupomona.kyra.Kyra;
 import edu.csupomona.kyra.component.ai.ZombieAI;
 import edu.csupomona.kyra.component.gun.PlayerGun;
 import edu.csupomona.kyra.component.health.EnemyHealth;
 import edu.csupomona.kyra.component.health.ItemHealth;
 import edu.csupomona.kyra.component.health.PlayerHealth;
 import edu.csupomona.kyra.component.input.Player1Input;
 import edu.csupomona.kyra.component.input.Player2Input;
 import edu.csupomona.kyra.component.physics.AntiZombiePhysics;
 import edu.csupomona.kyra.component.physics.HeartPhysics;
 import edu.csupomona.kyra.component.physics.PlayerPhysics;
 import edu.csupomona.kyra.component.physics.ZombiePhysics;
 import edu.csupomona.kyra.component.render.HeartRender;
 import edu.csupomona.kyra.component.render.LevelRender;
 import edu.csupomona.kyra.component.render.MapHealthRender;
 import edu.csupomona.kyra.component.render.ai.AntiZombieRender;
 import edu.csupomona.kyra.component.render.ai.ZombieRender;
 import edu.csupomona.kyra.component.render.player.Player1Render;
 import edu.csupomona.kyra.component.render.player.Player2Render;
 import edu.csupomona.kyra.component.sound.ZombieFx;
 import edu.csupomona.kyra.entity.Entity;
 import edu.csupomona.kyra.entity.EntityType;
 
 public abstract class Level extends BasicGameState {
 	int stateID;
 	String path;
 	TiledMap tiledMap;
 	Entity map, player1, player2, boss;
 	ArrayList<Entity> entities;
 	Vector2f p1Pos, p2Pos;
 	Image intro, pause, complete, p1Win, p2Win;
 	boolean drawIntro, levelWon;
 
 	
 	final int PLAYER_HEALTH = 10,
 		PLAYER_HEIGHT = 60,
 		PLAYER_WIDTH = 31,
 		ZOMBIE_HEALTH = 5,
 		ZOMBIE_HEIGHT = 60,
 		ZOMBIE_WIDTH = 31,
 		HEART_HEIGHT = 16,
 		HEART_WIDTH = 16,
 		CENTER_HEIGHT = 384,
 		CENTER_WIDTH = 512;
 	
 	public Level(int stateID, String path, Vector2f p1Pos, Vector2f p2Pos, boolean drawIntro) {
 		this.stateID = stateID;
 		this.path = path;
 		this.p1Pos = p1Pos;
 		this.p2Pos = p2Pos;
 		this.drawIntro = drawIntro;
 		levelWon = false;
 	}
 	
 	protected abstract void setBoss();
 	
 	protected void addZombie(Vector2f position, boolean anti) throws SlickException {
 		String name = "zombie" + entities.size();
 		Entity zombie = new Entity(name, EntityType.ZOMBIE);
 		zombie.setPosition(position);
 		zombie.addComponent(new ZombieAI("ai_"+name, player1, player2, tiledMap));
 		if(!anti) {
 			zombie.addComponent(new ZombiePhysics("physics"+name, ZOMBIE_HEIGHT, ZOMBIE_WIDTH, tiledMap));
 			zombie.addComponent(new ZombieRender("render"+name));
 		} else {
 			zombie.addComponent(new AntiZombiePhysics("physics"+name, ZOMBIE_HEIGHT, ZOMBIE_WIDTH, tiledMap));
 			zombie.addComponent(new AntiZombieRender("render"+name));
 		}
 		zombie.addComponent(new ZombieFx("fx"+name));
 		if(!Kyra.vs)
 			zombie.addComponent(new EnemyHealth("health"+name, ZOMBIE_HEALTH, player1, player2));
 		else
 			zombie.addComponent(new EnemyHealth("health"+name, ZOMBIE_HEALTH*2, player1, player2));
 		//zombie.addComponent(new HealthRender("drawHealth"+name));
 		entities.add(zombie);
 	}
 	
 	protected void addHeart(Vector2f position) throws SlickException {
 		String name = "heart" + entities.size();
 		Entity heart = new Entity(name, EntityType.HEART);
 		heart.setPosition(position);
 		heart.addComponent(new HeartPhysics("physics"+name, HEART_HEIGHT, HEART_WIDTH, tiledMap));
 		heart.addComponent(new HeartRender("render"+name));
 		heart.addComponent(new ItemHealth("item"+name));
 		entities.add(heart);
 	}
 	
 	protected void nextLevel(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		gc.getInput().clearKeyPressedRecord();
 		gc.resume();
 		player1.getSoundComponent().stopAll();
 		if (Kyra.vs)
 			player2.getSoundComponent().stopAll();
 		for (Entity entity : entities)
 			if (entity.getSoundComponent() != null)
 				entity.getSoundComponent().stopAll();
 		sbg.getCurrentState().leave(gc, sbg);
 		sbg.getState(stateID+1).init(gc, sbg);
 		sbg.getState(stateID+1).enter(gc, sbg);
 		sbg.enterState(stateID+1);
 	}
 	
 	protected void gameOver(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		gc.getInput().clearKeyPressedRecord();
 		player1.getSoundComponent().stopAll();
 		for (Iterator<Entity> iter = entities.iterator(); iter.hasNext();) {
 			Entity entity = iter.next();
 			if (entity.getSoundComponent() != null)
 				entity.getSoundComponent().stopAll();
 		}
 		gc.resume();
 		if(sbg.getCurrentStateID() == 4)
 			drawIntro = true;
 		sbg.getCurrentState().leave(gc, sbg);
 		sbg.getState(Kyra.GAMEOVERSTATE).init(gc, sbg);
 		sbg.getState(Kyra.GAMEOVERSTATE).enter(gc, sbg);
 		sbg.enterState(Kyra.GAMEOVERSTATE);
 	}
 		
 	@Override
 	public final int getID() {
 		return stateID; 
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		tiledMap = new TiledMap(path);
 		intro = new Image("img/intro.png");
 		pause = new Image("img/pause.png");
 		complete = new Image("img/complete.png"); 
 		p1Win = new Image("img/p1_win.png");
 		p2Win = new Image("img/p2_win.png");
 		
 		boss = new Entity("boss", EntityType.BOSS);
 		entities = new ArrayList<Entity>();
 		
 		player1 = new Entity("player1", EntityType.PLAYER1);
 		player1.setPosition(p1Pos);
 		player1.addComponent(new Player1Input("p1Input"));
 		player1.addComponent(new PlayerPhysics("p1Physics", PLAYER_HEIGHT, PLAYER_WIDTH, tiledMap));
 		player1.addComponent(new Player1Render("p1Sprite"));
 		player1.addComponent(new PlayerHealth("p1Health", PLAYER_HEALTH, entities));
 		player1.addComponent(new PlayerGun("p1Gun", tiledMap));
 		player1.addComponent(new ScoreComponent("p1Score"));
 		//player1.addComponent(new PositionRender("p1Pos"));
 		
 		if (Kyra.vs) {
 			player2 = new Entity("player2", EntityType.PLAYER2);
 			player2.setPosition(p2Pos);
 			player2.addComponent(new Player2Input("p2Input"));
 			player2.addComponent(new PlayerPhysics("p1Physics", PLAYER_HEIGHT, PLAYER_WIDTH, tiledMap));
 			player2.addComponent(new Player2Render("p2Sprite"));
 			player2.addComponent(new PlayerHealth("p2Health", PLAYER_HEALTH, entities));
 			player2.addComponent(new PlayerGun("p2Gun", tiledMap));
 			player2.addComponent(new ScoreComponent("p2Score"));
 		}
 		
 		map = new Entity("map", EntityType.MAP);
 		map.addComponent(new LevelRender("level", tiledMap, player1, player2));
 		map.addComponent(new MapHealthRender("playerHealth", player1, player2));
 		
 		setBoss();
 		entities.add(boss);
 		
 	}
 
 	protected boolean isInRange(Entity player1, Entity player2, Entity other) {
 		Vector2f playerPos = null;
 		if(!player1.getHealthComponent().isDead())
 			playerPos = player1.getPosition();
 		else if (Kyra.vs && !player2.getHealthComponent().isDead())
 			playerPos = player2.getPosition();
 		Vector2f otherPos = other.getPosition();
		if (otherPos != null && playerPos != null) {
 			float xDiff = Math.abs(otherPos.x-playerPos.x);
 			float yDiff = Math.abs(otherPos.y-playerPos.y);
 			return ((xDiff < CENTER_WIDTH+300) && (yDiff < CENTER_HEIGHT+300));
 		}
 		return false;
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics gr) throws SlickException {
 		if (drawIntro)
 			intro.drawCentered(CENTER_WIDTH, CENTER_HEIGHT);
 		if (gc.isPaused() && !levelWon)
 			pause.drawCentered(CENTER_WIDTH, CENTER_HEIGHT);
 		else if(gc.isPaused() && levelWon) {
 			complete.drawCentered(CENTER_WIDTH, CENTER_HEIGHT);
 			if(Kyra.vs && player1.getScoreComponent().getScore() > player2.getScoreComponent().getScore())
 				p1Win.drawCentered(CENTER_WIDTH, CENTER_HEIGHT+115);
 			else if (Kyra.vs && player1.getScoreComponent().getScore() < player2.getScoreComponent().getScore())
 				p2Win.drawCentered(CENTER_WIDTH, CENTER_HEIGHT+115);
 		} else {
 			map.render(gc, sbg, gr);
 			if(!player1.getHealthComponent().isDead())
 				player1.render(gc, sbg, gr);
 			if (Kyra.vs && !player2.getHealthComponent().isDead())
 				player2.render(gc, sbg, gr);
 			for (Entity entity : entities) {
 				if (isInRange(player1, player2, entity))
 					entity.render(gc, sbg, gr);
 			}
 		}	
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		Input input = gc.getInput();
 		
 		if(!drawIntro) {
 			if (!gc.isPaused()) { //game is playing
 				if(!player1.getHealthComponent().isDead())
 					player1.update(gc, sbg, delta);
 				if (Kyra.vs && !player2.getHealthComponent().isDead())
 					player2.update(gc, sbg, delta);
 				//Remove dead things from the game world
 				for (Iterator<Entity> iter = entities.iterator(); iter.hasNext();) {
 					Entity entity = iter.next();
 					if (isInRange(player1, player2, entity))
 						entity.update(gc, sbg, delta);
 					EntityType type = entity.getType();
 					if (type.equals(EntityType.HEART) && entity.getHealthComponent().isDead())
 						iter.remove();
 					else if (type.equals(EntityType.ZOMBIE) && entity.getHealthComponent().isDead()) {
 						entity.getSoundComponent().stopAll();
 						iter.remove();
 					}
 					else if (type.equals(EntityType.BOSS) && entity.getHealthComponent().isDead()) {
 						gc.pause();
 						levelWon = true;
 					}
 				}
 				map.update(gc, sbg, delta);
 				//Pause if pause key is pressed
 				if (input.isKeyPressed(Input.KEY_ENTER)) {
 					gc.pause();
 					input.clearKeyPressedRecord();
 				}
 			} else { //game is paused
 				// stop all sounds
 				player1.getSoundComponent().stopAll();
 				if (Kyra.vs)
 					player2.getSoundComponent().stopAll();
 				for (Entity entity : entities)
 					if (entity.getSoundComponent() != null)
 						entity.getSoundComponent().stopAll();
 				if(input.isKeyPressed(Input.KEY_ENTER)) {
 					gc.resume();
 					input.clearKeyPressedRecord();
 				}
 				if(input.isKeyPressed(Input.KEY_SPACE))
 					nextLevel(gc, sbg);
 				if(input.isKeyPressed(Input.KEY_Q)) {
 					File f = new File("save.txt");
 					try {
 						PrintWriter pw = new PrintWriter(f);
 						pw.println(Kyra.vs);
 						pw.println(sbg.getCurrentStateID());
 						pw.close();
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					}
 				}	
 			}
 			if(!Kyra.vs && player1.getHealthComponent().isDead())
 					gameOver(gc, sbg);
 			else if(Kyra.vs &&  player1.getHealthComponent().isDead() && player2.getHealthComponent().isDead())
 					gameOver(gc, sbg);
 		} else {
 			if (input.isKeyPressed(Input.KEY_SPACE)) {
 				drawIntro = false;
 				input.clearKeyPressedRecord();
 			}
 		}
 	}
 
 }

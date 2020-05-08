 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pewpew.gamestates;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.openal.SoundStore;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import pewpew.entities.Asteroid;
 import pewpew.entities.Entity;
 import pewpew.entities.Player;
 import pewpew.entities.PowerUps;
 import pewpew.entities.powerups.HealthUp;
 import pewpew.entities.powerups.InfiniteShot;
 import pewpew.entities.powerups.RandomUp;
 import pewpew.entities.powerups.ShrinkUp;
 
 /**
  * 
  * @author michael
  */
 public class StandardGame extends BasicGameState {
 	Player p;
 	ArrayList<Entity> e;
 	ArrayList<PowerUps> pow;
 	int MaxPowerups = 3;
 	int PowerUpCoolDown = 200;
 	int EnemyCoolDown = 200;
 	int MaxEnemies = 10;
 	int StarArray[];
 	boolean stars = true;
 	boolean starflicker = true;
 	String message = "hello world";
 	Color messageColor = new Color(Color.white);
	boolean InfiniteAmmo = false;
 	static boolean[] GunsAllowed = { true, true, true, true, true };
 
 	StandardGame() {
 		pow = new ArrayList<>();
 		e = new ArrayList<>();
 		p = new Player();
 		message = "old highscore here";
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		pow = new ArrayList<>();
 		e = new ArrayList<>();
 		p = new Player();
 		message = "";
 		if (stars) {
 			StarArray = new int[(int) ((Math.random() * 50) + 1) * 3];
 			for (int i = 0; i <= StarArray.length - 3; i += 3) {
 				StarArray[i] = (int) (Math.random() * gc.getWidth()); // x
 				StarArray[i + 1] = (int) (Math.random() * gc.getHeight()); // radius
 				StarArray[i + 2] = (int) (Math.random() * 10) + 1; // radius
 			}
 		}
 
 		if (InfiniteAmmo == true) {
 			p.score += 50000;
 		}
 		// e.add(new Bosteroid(p));
 		e.add(new Asteroid(-1, -1, p));
 	}
 
 	public void getMotion(Input in, GameContainer gc, StateBasedGame sbg,
 			int delta) throws SlickException {
 		if (in.isKeyDown(Input.KEY_UP)) {
 			float rotation = (float) Math.toRadians(p.getRotation());
 			p.Forward((float) (0.2f * Math.sin(rotation)),
 					(float) (-0.2f * Math.cos(rotation)), delta);
 		}
 		if (in.isKeyDown(Input.KEY_DOWN)) { // decelerate
 			float rotation = (float) Math.toRadians(p.getRotation());
 			p.Reverse((float) (0.2f * Math.sin(rotation)),
 					(float) (-0.2f * Math.cos(rotation)), delta);
 			// p.Forward(-p.xv / 35, -p.yv / 35, delta);
 			// p.xa = 0;
 			// p.ya = 0;
 		}
 		if (in.isKeyDown(Input.KEY_RIGHT)) {
 			p.Rotate(0.05f * delta / 10);
 		}
 		if (in.isKeyDown(Input.KEY_LEFT)) {
 			p.Rotate(-0.05f * delta / 10);
 		}
 
 		// gun rotations
 		if (in.isKeyDown(Input.KEY_A)) {
 			p.rotateGun(-0.03f * delta / 10);
 		}
 		if (in.isKeyDown(Input.KEY_D)) {
 			p.rotateGun(0.03f * delta / 10);
 		}
 		if (in.isKeyDown(Input.KEY_W)) {
 			p.fireBullet();
 		}
 		// toggle fixed gun
 		if (in.isKeyPressed(Input.KEY_S)) {
 			// p.BulletAngle = (float)(-Math.PI/2) +
 			// (float)Math.toRadians(p.Ship.getRotation());
 			p.toggleFixedGun();
 		}
 
 		// Gun Modes
 		if (in.isKeyPressed(Input.KEY_1) && GunsAllowed[0]) {
 			p.changeBulletType((byte) 0);
 		} else if (in.isKeyDown(Input.KEY_2) && GunsAllowed[1]) {
 			p.changeBulletType((byte) 1);
 		} else if (in.isKeyDown(Input.KEY_3) && GunsAllowed[2]) {
 			p.changeBulletType((byte) 2);
 		} else if (in.isKeyDown(Input.KEY_4) && GunsAllowed[3]) {
 			p.changeBulletType((byte) 3);
 		} else if (in.isKeyDown(Input.KEY_5) && GunsAllowed[4]) {
 			p.changeBulletType((byte) 4);
 		}
 
 		if (in.isKeyPressed(Input.KEY_E)) {
 			nextWeapon();
 		}
 		if (in.isKeyPressed(Input.KEY_Q)) {
 			prevWeapon();
 		}
 
 		if (gameOver()) {
 			if (in.isKeyPressed(Input.KEY_R)) {
 				sbg.initStatesList(gc);
 			}
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		// cull powerups
 		for (int i = 0; i <= pow.size() - 1; i++) {
 			if (pow.get(i).cull()) {
 				pow.remove(i);
 			}
 		}
 
 		// cull enemies
 		for (int i = 0; i <= e.size() - 1; i++) {
 			if (e.get(i).cull()) {
 				e.remove(i);
 			}
 		}
 
 		MainGame g = (MainGame) sbg;
 		gc.setMusicOn(g.mute);
 		gc.setSoundOn(g.mute);
 		Input in = gc.getInput();
 		g.GetUniversalOptions(in, gc);
 		p.move(null);
 		getMotion(in, gc, sbg, delta);
 		// move enemies
 		for (int i = 0; i <= e.size() - 1; i++) {
 			e.get(i).move(p);
 		}
 		// move powerups
 		for (int i = 0; i <= pow.size() - 1; i++) {
 			pow.get(i).move(p);
 		}
 		// collision check enemies
 		for (int i = 0; i <= e.size(); i++) {
 			for (int j = 0; j <= e.size(); j++) {
 				Entity en;
 				if (j == e.size()) {
 					en = p;
 				} else {
 					en = e.get(j);
 				}
 				Entity en2;
 				if (i == e.size()) {
 					en2 = p;
 				} else {
 					en2 = e.get(i);
 				}
 				if (en == en2) {
 					continue;
 				}
 
 				if (en == null || en2 == null || en.getAllChildren() == null
 						|| en2.getAllChildren() == null) {
 					continue;
 				}
 				for (Entity ent : en.getAllChildren()) {
 					for (Entity ent2 : en2.getAllChildren()) {
 						if (ent2 == null || ent == null) {
 							continue;
 						}
 						ent2.collides(ent);
 						ent.collides(ent2);
 					}
 				}
 			}
 		}
 		// collsion check powerups
 		for (int i = 0; i <= pow.size() - 1; i++) {
 			pow.get(i).collides(p);
 		}
 		SoundStore.get().poll(0);
 		// spawn enemy
 		if ((EnemyCoolDown != 0 && p.tick % (EnemyCoolDown) == 0)
 				|| e.isEmpty() && MaxEnemies > e.size()) {
 			e.add(new Asteroid(-1, -1, p));
 			if (EnemyCoolDown >= 100) {
 				EnemyCoolDown--;
 			}
 		}
 		// spawn powerups
 		if (PowerUpCoolDown != 0 && p.tick % PowerUpCoolDown == 0
 				&& MaxPowerups > pow.size()) {
 			if (Math.random() < 0.1) {
 				double rand = Math.random();
 				if (rand > ShrinkUp.SpawnProbibility) {
 					pow.add(new ShrinkUp());
 				} else if (rand > RandomUp.SpawnProbibility) {
 					pow.add(new RandomUp());
 				} else if (rand > HealthUp.SpawnProbibility) {
 					pow.add(new HealthUp());
 				} else if (rand > InfiniteShot.SpawnProbibility) {
 					pow.add(new InfiniteShot());
 				}
 
 				if (PowerUpCoolDown < 999) {
 					PowerUpCoolDown++;
 				}
 			}
 		}
 
 	}
 
 	public void renderWinConditions(GameContainer gc, StateBasedGame sbg,
 			Graphics g) {
 		int Conditions = 1;
 		// score
 		g.setColor(Color.white);
 		NumberFormat formatter = new DecimalFormat("00000000");
 		String scr = formatter.format(p.score);
 		g.drawString(scr, 25, gc.getHeight() - (25 * Conditions));
 	}
 
 	public void nextWeapon() {
 		if (p.getAmmoType() == Player.GUN_TYPES - 1) {
 			p.changeBulletType((byte) 0);
 			if (!GunsAllowed[0]) {
 				nextWeapon();
 			}
 		} else {
 			p.changeBulletType((byte) (p.getAmmoType() + 1));
 			if (!GunsAllowed[p.getAmmoType()]) {
 				nextWeapon();
 			}
 		}
 	}
 
 	public void prevWeapon() {
 		if (p.getAmmoType() == 0) {
 			p.changeBulletType((byte) (Player.GUN_TYPES - 1));
 			if (!GunsAllowed[GunsAllowed.length - 1]) {
 				prevWeapon();
 			}
 		} else {
 			p.changeBulletType((byte) (p.getAmmoType() - 1));
 			if (!GunsAllowed[p.getAmmoType()]) {
 				prevWeapon();
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		renderWinConditions(gc, sbg, g);
 		// stars
 		if (stars) {
 			for (int i = 0; i <= StarArray.length - 3; i += 3) {
 				g.setColor(new Color((255 + (int) (-Math.random() * 100)),
 						(255 + (int) (-Math.random() * 100)), 255));
 				int rand;
 				if (starflicker) {
 					rand = (int) (Math.random() * 2) - 3;
 				} else {
 					rand = 0;
 				}
 				g.fillOval(StarArray[i] + rand / 2,
 						StarArray[i + 1] + rand / 2, StarArray[i + 2] + rand,
 						StarArray[i + 2] + rand);
 			}
 		}
 
 		// messages
 		if (!message.isEmpty()) {
 			g.setColor(messageColor);
 			g.drawString(message, 0, 50);
 		}
 		if (gameOver()) {
 			g.drawString("DEAD", Entity.FORM_WIDTH / 2 - 20,
 					Entity.FORM_HEIGHT / 2);
 			g.drawString("press r to restart", Entity.FORM_WIDTH / 2 - 75,
 					Entity.FORM_HEIGHT / 2 + 20);
 			// return;
 		}
 		// health bar
 		float health = p.health;
 		if (health == 0) {
 			health++;
 		}
 
 		if (p.health < p.MAX_HEALTH / 2 && p.tick % health < health / 2) {
 			g.setColor(Color.red);
 		} else {
 			g.setColor(Color.white);
 		}
 		g.drawRect(gc.getWidth() / 2 - 251, 1, 501, 51);
 
 		if (p.damagetick > 0) {
 			g.setColor(Color.red);
 		} else {
 			g.setColor(Color.green);
 		}
 		if (p.health > 0) {
 			g.fillRect(gc.getWidth() / 2 - 250, 2, p.health * 5, 50);
 		}
 		// player and enemies with children
 		p.render(gc, g);
 		for (int i = 0; i <= e.size() - 1; i++) {
 			e.get(i).render(gc, g);
 		}
 		for (int i = 0; i <= pow.size() - 1; i++) {
 			pow.get(i).render(gc, g);
 		}
 
 		// set acceleration to zero to allow proper render of flame exhust
 		// p.xa = 0;
 		// p.ya = 0;
 
 	}
 
 	public boolean gameOver() {
 		if (p.health < 0) {
 			return true;
 		} else
 			return false;
 	}
 
 }

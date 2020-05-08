 package spaceshooters.entities;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import spaceshooters.entities.upgrades.Upgrade;
 import spaceshooters.entities.upgrades.UpgradeTypes;
 import spaceshooters.level.Level;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.sfx.SoundEngine;
 import spaceshooters.sfx.Sounds;
 import spaceshooters.states.GameOverState;
 import spaceshooters.util.save.SaveData;
 
 /**
  * Main player class.
  * 
  * @author Mat
  * 
  */
 public class Player extends Entity {
 	
 	private int fireRate = 200;
 	private int milliCount = 0;
 	private int millis = fireRate;
 	private int milliStep = millis / 10;
 	
 	public Player(Level level, int startX, int startY) throws SlickException {
 		super(level, startX, startY, EntityTypes.PLAYER);
 		texture = new SpriteSheet(Spaceshooters.getGraphicsFolder() + "sprites.png", 45, 45, 1).getSprite(0, 0);
 		width = 30;
 		height = 36;
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
 		Input input = container.getInput();
 		
 		this.collideCheck(EntityTypes.values());
 		
 		if (this.hasAutoCannons()) {
 			if (input.isKeyDown(Input.KEY_SPACE)) {
 				milliCount += delta;
 				while (milliCount > milliStep) {
 					milliCount -= milliStep;
 					millis -= milliStep;
 				}
 				if (millis < 0) {
 					SoundEngine.getInstance().play(Sounds.SHOT, 1.0f, 0.3f);
					level.spawnEntity(new Bullet(level, this.getX() + 14, this.getY() - 5, this.hasBetterBullets()));
 					millis = fireRate;
 				}
 			}
 		} else {
 			if (input.isKeyPressed(Input.KEY_SPACE)) {
 				SoundEngine.getInstance().play(Sounds.SHOT, 1.0f, 0.3f);
				level.spawnEntity(new Bullet(level, this.getX() + 14, this.getY() - 5, this.hasBetterBullets()));
 			}
 		}
 		
 		if (input.isKeyDown(Input.KEY_LEFT) && x > 0) {
 			x -= velocity * delta;
 		}
 		if (input.isKeyDown(Input.KEY_RIGHT) && x + width < container.getWidth()) {
 			x += velocity * delta;
 		}
 		if (input.isKeyDown(Input.KEY_UP) && y > 0) {
 			y -= velocity * delta;
 		}
 		if (input.isKeyDown(Input.KEY_DOWN) && y + height < container.getHeight()) {
 			y += velocity * delta;
 		}
 		
 		if (input.isKeyPressed(Input.KEY_X)) {
 			Upgrade upgrade = new Upgrade(UpgradeTypes.DAMAGE);
 			if (this.hasBetterBullets()) {
 				this.removeUpgrade(upgrade);
 			} else {
 				this.addUpgrade(upgrade);
 			}
 		}
 		
 		if (input.isKeyPressed(Input.KEY_Z)) {
 			Upgrade upgrade = new Upgrade(UpgradeTypes.AUTO_CANNONS);
 			if (this.hasAutoCannons()) {
 				this.removeUpgrade(upgrade);
 			} else {
 				this.addUpgrade(upgrade);
 			}
 		}
 	}
 	
 	@Override
 	public void render(Graphics g) {
 		texture.draw(x, y);
 	}
 	
 	@Override
 	public void onCollide(Entity entity) {
 		if (this.getScore() > this.getHighScore()) {
 			this.setHighScore(this.getScore());
			GameOverState.highscore = this.getScore();
 		}
 		GameOverState.score = this.getScore();
 		this.resetPlayerData();
 		this.setDead();
 	}
 	
 	public boolean hasAutoCannons() {
 		return SaveData.getSaveData().getBoolean("player.upgrades." + UpgradeTypes.AUTO_CANNONS.getSaveName());
 	}
 	
 	public boolean hasBetterBullets() {
 		return SaveData.getSaveData().getBoolean("player.upgrades." + UpgradeTypes.DAMAGE.getSaveName());
 	}
 	
 	public int getHighScore() {
 		return SaveData.getSaveData().getInt("player.highscore");
 	}
 	
 	public void setHighScore(int highscore) {
 		SaveData.getSaveData().setInt("player.highscore", highscore);
 	}
 	
 	public int getScore() {
 		return SaveData.getSaveData().getInt("player.score");
 	}
 	
 	public void setScore(int score) {
 		SaveData.getSaveData().setInt("player.score", score);
 	}
 	
 	@Override
 	public boolean hasUpgrade(Upgrade u) {
 		if (SaveData.getSaveData().getBoolean("player.upgrades." + u.getType().getSaveName())) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void addUpgrade(Upgrade upgrade) {
 		SaveData.getSaveData().setBoolean("player.upgrades." + upgrade.getType().getSaveName(), true);
 	}
 	
 	@Override
 	public void removeUpgrade(Upgrade upgrade) {
 		SaveData.getSaveData().setBoolean("player.upgrades." + upgrade.getType().getSaveName(), false);
 	}
 	
 	public void resetPlayerData() {
 		this.setScore(0);
 		SaveData.getSaveData().setBooleans("player.upgrades", false);
 	}
 }

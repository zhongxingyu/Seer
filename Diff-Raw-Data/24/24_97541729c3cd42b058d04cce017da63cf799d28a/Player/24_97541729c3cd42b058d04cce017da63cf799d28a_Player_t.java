 package spaceshooters.entities;
 
 import me.matterross.moneta.tag.TagCompound;
 
 import org.lwjgl.input.Keyboard;
 
 import pl.matterross.engine.base.Input;
 import spaceshooters.aurora.entity.EntityType;
 import spaceshooters.aurora.entity.IPlayer;
 import spaceshooters.aurora.level.ILevel;
 import spaceshooters.aurora.level.ILevelType;
import spaceshooters.aurora.level.LevelType;
 import spaceshooters.config.Configuration;
 import spaceshooters.entities.Bullet.BulletType;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.save.SaveData;
 import spaceshooters.sfx.Sound;
 import spaceshooters.sfx.SoundEngine;
 
 /**
  * Main player class.
  * 
  * @author Mat
  * 
  */
 public class Player extends Entity implements IPlayer {
 	
 	private static final int BULLET_DIST_Y = 10;
 	private static final int BULLET_DIST_X = 14;
 	
 	private static final String HIGHSCORES = "highscores";
 	private static final String NORMAL_SAVE = "normal";
 	private static final String SURVIVAL_SAVE = "survival";
 	private static final String PROTECTOR_SAVE = "protector";
 	
 	// Auto - fire stuff.
 	private int fireRate = 200;
 	private int milliCount = 0;
 	private int millis = fireRate;
 	private int milliStep = millis / 10;
 	
 	// Player related variables.
 	private int score;
 	private int ammo;
 	
 	public Player(ILevel level, float startX, float startY) {
 		super(level, startX, startY, EntityType.PLAYER);
 		score = 0;
 		ammo = 16;
 	}
 	
 	@Override
 	public void update(int delta) {
 		Input input = Input.getInput();
 		
 		if (this.getLevel().getLevelType() != LevelType.SURVIVAL) {
 			/* if (this.hasAutoCannons()) {
 				if (input.isKeyDown(Input.KEY_SPACE)) {
 					milliCount += delta;
 					while (milliCount > milliStep) {
 						milliCount -= milliStep;
 						millis -= milliStep;
 					}
 					if (millis < 0) {
 						SoundEngine.getInstance().play(Sound.SHOT, 1.0f, 0.3f);
 						level.spawnEntity(new Bullet(level, this.getX() + BULLET_DIST_X, this.getY() - BULLET_DIST_Y));
 						millis = fireRate;
 					}
 				}
 			} else { */
 			if (input.isKeyPressed(Keyboard.KEY_SPACE) && ammo > 0) {
 				SoundEngine.getInstance().play(Sound.SHOT, 1.0f, 0.3f);
 				level.spawnEntity(new Bullet(level, this.getX() + BULLET_DIST_X, this.getY() - BULLET_DIST_Y));
 				ammo--;
 				//	}
 			}
 			
 			// FIXME: Remove this infinite ammo hack :D
 			if (input.isKeyPressed(Keyboard.KEY_F)) {
 				ammo = Integer.MAX_VALUE;
 			}
 		}
 		
 		// FIXME: Remove me.
 		if (input.isKeyPressed(Keyboard.KEY_LCONTROL)) {
 			level.spawnEntity(new Bullet(level, this.getX() + BULLET_DIST_X, this.getY() - BULLET_DIST_Y, BulletType.EXPLOSIVE));
 		}
 		
 		if (input.isKeyDown(Keyboard.KEY_LEFT) && position.x > 0) {
 			position.x -= velocity * delta;
 		}
 		if (input.isKeyDown(Keyboard.KEY_RIGHT) && position.x + width < Spaceshooters.WIDTH) {
 			position.x += velocity * delta;
 		}
 		if (input.isKeyDown(Keyboard.KEY_UP) && position.y > 0) {
 			position.y -= velocity * delta;
 		}
 		if (input.isKeyDown(Keyboard.KEY_DOWN) && position.y + height < Spaceshooters.HEIGHT) {
 			position.y += velocity * delta;
 		}
 	}
 	
 	@Override
 	public void onCollide(Entity entity) {
 		if (entity.getEntityType() == EntityType.ASTEROID || entity.getEntityType() == EntityType.ENEMY) {
 			this.setAlive(false);
 			
 			int difficulty = Configuration.getConfiguration().getInt("difficulty");
 			
 			if (this.getScore() > this.getHighscore(this.getLevel().getLevelType(), difficulty)) {
 				this.setHighscore(this.getLevel().getLevelType(), difficulty, score);
 			}
 		}
 	}
 	
 	public int getAmmo() {
 		return ammo;
 	}
 	
 	public void setAmmo(int ammo) {
 		this.ammo = ammo;
 	}
 	
 	public void addAmmo(int toAdd) {
 		ammo += toAdd;
 	}
 	
 	@Override
 	public int getScore() {
 		return score;
 	}
 	
 	@Override
 	public void subtractScore(int toSubtract) {
 		score -= toSubtract;
 	}
 	
 	@Override
 	public void addScore(int toAdd) {
 		score += toAdd;
 	}
 	
 	@Override
 	public void setScore(int score) {
 		this.score = score;
 	}
 	
 	@Override
 	public int getHighscore(ILevelType gamemode, int difficulty) {
 		TagCompound data = SaveData.getInstance().getData();
 		TagCompound highscores = (TagCompound) data.getTag(HIGHSCORES);
 		
 		if (highscores == null) {
 			data.putTag(new TagCompound(HIGHSCORES));
 			highscores = (TagCompound) data.getTag(HIGHSCORES);
 			return 0;
 		}
 		
 		return 0;
 		
 		/*
 		try {
 			switch (gamemode) {
 			case NORMAL:
 				return (int) highscores.getTag(NORMAL_SAVE + difficulty).getValue();
 			case SURVIVAL:
 				return (int) highscores.getTag(SURVIVAL_SAVE + difficulty).getValue();
 			case PROTECTOR:
 				return (int) highscores.getTag(PROTECTOR_SAVE + difficulty).getValue();
 			default:
 				throw new UnsupportedOperationException("No such save data read handle for " + gamemode + ": " + difficulty);
 			}
 		} catch (NullPointerException e) {
 			System.out.println("Null at " + gamemode + ": " + difficulty);
 			this.setHighscore(gamemode, difficulty, 0);
 			return 0;
 		}
 		*/
 	}
 	
 	@Override
 	public void setHighscore(ILevelType gamemode, int difficulty, int newScore) {
 		TagCompound data = SaveData.getInstance().getData();
 		TagCompound highscores = (TagCompound) data.getTag(HIGHSCORES);
 		
 		if (highscores == null) {
 			data.putTag(new TagCompound(HIGHSCORES));
 			highscores = (TagCompound) data.getTag(HIGHSCORES);
 		}
 		
 		/*
 		switch (gamemode) {
 		case NORMAL:
 			highscores.putTag(new IntTag(NORMAL_SAVE + difficulty, newScore));
 			break;
 		case SURVIVAL:
 			highscores.putTag(new IntTag(SURVIVAL_SAVE + difficulty, newScore));
 			break;
 		case PROTECTOR:
 			highscores.putTag(new IntTag(PROTECTOR_SAVE + difficulty, newScore));
 			break;
 		default:
 			throw new UnsupportedOperationException("No such save data write handle for " + gamemode + ": " + difficulty);
 		}
 		
 		SaveData.getInstance().save();
 		*/
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return "entities/player.png";
 	}
 }

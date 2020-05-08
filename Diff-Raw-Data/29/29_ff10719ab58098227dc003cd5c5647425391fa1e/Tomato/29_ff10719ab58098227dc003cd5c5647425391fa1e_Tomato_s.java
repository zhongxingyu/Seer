 package tomato.entity;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import tomato.Camera;
 import tomato.Game;
 import tomato.Input;
 import tomato.PixelExplosion;
 import tomato.TickStrategy;
 import tomato.gfx.Art;
 import tomato.item.PlayerGun;
 import tomato.level.Level;
 import tomato.math.IVec2D;
 import tomato.math.Vec2DPrecise;
 import tomato.physics.WorldPhysicHandler;
 import tomato.sound.Sound;
 
 public class Tomato extends PhysicsEntity {
 
 	private double twinkTime = 0D;
 	private double nextTwink = 0D;
 	private boolean eyesOpen = true;
 
 	public Tomato(WorldPhysicHandler handler, int xc, int yc) {
 		super(handler);
 		interactsWithWorld = true;
 		bounce = 0.0;
 		x = xc;
 		y = yc;
 		life = 100;
 		h = getSprite().getHeight();
 		w = getSprite().getWidth();
 		walkspeed = 200;
 
 		addTickStrategy(new TickStrategy() {
 
 			@Override
 			public void tick(Input input, double delta) {
 				// This makes the Tomato blink.
 				twinkTime += delta;
 				if (twinkTime >= nextTwink) {
 					eyesOpen = !eyesOpen;
 					if (!eyesOpen) {
 						nextTwink = Math.random() * 0.1 + 0.1;
 					} else {
 						nextTwink = Math.random() * 2 + 1.337;
 					}
 					twinkTime = 0D;
 				}
 			}
 		});
 	}
 
 	@Override
 	public void init(Level level) {
 		super.init(level);
 		gun = new PlayerGun(this, level, Integer.MAX_VALUE, 0.06D, true);
 	}
 
 	@Override
 	public void collided(AbstractEntity with) {
 		super.collided(with);
 	}
 
 	@Override
 	public void attack(AbstractEntity from) {
 		if (getInvulnerable() == 0) {
 			life -= from.attack;
 			if (life <= 0) {
 				die();
 			}
 
 			IVec2D P = new Vec2DPrecise(x + 0.5D * w, y + 0.5 * h);
 			IVec2D F = new Vec2DPrecise(from.x + 0.5D * from.w, from.y + 0.5 * from.h);
 
 			IVec2D PF = F.substract(P);
			IVec2D unitVector = PF.getUnitVector();
 
 			xa -= walkspeed * unitVector.getX();
 
 			ya = walkspeed * unitVector.getY() * -1;
 			setInvulnerable(0.1D);
 			new PixelExplosion(level, (int) (x + 0.5 * w), (int) (y + 0.5 * h),
 					30, 0.5D, 100, new Color(0xFF0A0E));
 			Sound.hurt.play();
 		}
 	}
 
 	@Override
 	public void die() {
 		super.die();
 		new PixelExplosion(level, (int) (x + 0.5 * w), (int) (y + 0.5 * h),
 				250, 4.00D, 2000, new Color(0xFF0A0E));
 		Sound.die.play();
 	}
 
 	@Override
 	public void render(Graphics g, Camera cam) {
 
 		cam.x = (int) Math.floor(x - 0.5 * Game.GAME_WIDTH);
 		cam.y = (int) Math.floor(y - 0.2 * Game.GAME_HEIGHT);
 		if (cam.y > level.getHeight()-cam.height) {
 			cam.y = level.getHeight()-cam.height;
 		}
 		if (cam.x < 0) {
 			cam.x = 0;
 		}
 		if (cam.x + cam.width > level.getWidth()) {
 			cam.x = level.getWidth() - cam.width;
 		}
 		if (y < 0) {
 			g.drawImage(getSprite(), (int) Math.floor(x - cam.x),
 					(int) Math.floor(1 + y - cam.y), null);
 		} else {
 			g.drawImage(getSprite(), (int) Math.floor(x - cam.x),
 					(int) Math.floor(y - cam.y), null);
 		}
 		gun.render(g, cam);
 	}
 
 	
 	@Override
 	public BufferedImage getSprite() {
 			return ((faceRight) ? Art.player1 : Art.player2)[(eyesOpen) ? 0 : 1][0];
 	}
 
 	@Override
 	public int getType() {
 		return AbstractEntity.PLAYER;
 	}
 
 }

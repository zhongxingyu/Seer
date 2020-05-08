 package de.timweb.ld48.villain.util;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import de.timweb.ld48.villain.entity.Entity;
 import de.timweb.ld48.villain.game.Game;
 import de.timweb.ld48.villain.game.Level;
 import de.timweb.ld48.villain.game.Spawner;
 import de.timweb.ld48.villain.game.VillainCanvas;
 import de.timweb.ld48.villain.level.BodyLevel;
 
 public class Virus extends Entity {
 	public static final int LEVELUP = 3;
 	private static final double DEFAULT_SPEED = 0.02;
	private static final double MIN_DISTANCE = 5;
 
 	private static int level = 0;
 	private static double maxSpeed = 2;
 
 	private BufferedImage img;
 	private Vector2d direction;
 	private int size = 8;
 	private double speed = DEFAULT_SPEED;
 	private boolean isSelected;
 	private Vector2d target;
 	private int color;
 	private int health = 1000;
 	private boolean isFreezed;
 
 	public Virus(Vector2d pos, int color) {
 		super(pos);
 
 		this.color = color;
 		direction = Vector2d.randomNormalized();
 
 		setImage();
 	}
 
 	public Virus(Vector2d pos) {
 		this(pos, (int) (Math.random() * 6));
 	}
 
 	@Override
 	public void update(int delta) {
 		double act_speed = speed;
 		if (isFreezed)
 			act_speed /= 10;
 
 		checkForAttack(delta);
 
 		if (target != null) {
 			direction = target.copy().add(-pos.x, -pos.y).normalize();
 			if (target.distance(pos) < MIN_DISTANCE) {
 				target = null;
 				speed = DEFAULT_SPEED;
 			}
 		} else {
 			if (Math.random() < 0.02) {
 				direction.flipX();
 			}
 			if (Math.random() < 0.02) {
 				direction.flipY();
 			}
 
 		}
 
 		double dx = direction.x * delta * act_speed;
 		double dy = direction.y * delta * act_speed;
 
 		pos.add(dx, dy);
 
 		if (pos.x < 0 || pos.x > VillainCanvas.WIDTH) {
 			direction.flipX();
 			dx = direction.x * delta;
 			pos.add(dx, 0);
 		}
 		if (pos.y < 0 || pos.y > VillainCanvas.HEIGHT) {
 			direction.flipY();
 			dy = direction.y * delta;
 			pos.add(0, dy);
 		}
 
 	}
 
 	private void checkForAttack(int delta) {
 		List<Spawner> spawner = ((BodyLevel) Game.g.getCurrentLevel())
 				.getSpawner();
 
 		for (Spawner e : spawner) {
 			double dist = e.getPos().distance(pos);
 			if (e.isWhite() && dist < MIN_DISTANCE * 3) {
 				e.attack((delta+level)/2, (color + 1) % 6);
 				System.out.println("attack white spawner");
 			}
 		}
 	}
 
 	@Override
 	public void render(Graphics g) {
 		if (isSelected) {
 			g.drawImage(ImageLoader.selected_32, pos.x() - 16, pos.y() - 16,
 					null);
 		}
 
 		g.drawImage(img, pos.x() - size, pos.y() - size, null);
 	}
 
 	public void setSelected(boolean b) {
 		isSelected = b;
 	}
 
 	public void setTarget(Vector2d target) {
 		this.target = target;
 
 		speed = maxSpeed * DEFAULT_SPEED;
 	}
 
 	@Override
 	protected void onKilled() {
 		SoundEffect.HURT.play();
 		System.out.println("Virus killed");
 	}
 
 	public void hurt(int delta) {
 		health -= delta;
 		if (health <= 0) {
 			kill();
 		}
 	}
 
 	public static void setLevel(int level) {
 		Virus.level = level;
 
 		// if level is too high --> don't set new images
 		if (level / LEVELUP >= 6 * 3) {
 			return;
 		}
 
 		// set new Images for this Level
 		Level gamelevel = Game.g.getCurrentLevel();
 		if (gamelevel instanceof BodyLevel) {
 			List<Virus> virus = ((BodyLevel) gamelevel).getVirus();
 
 			for (Virus v : virus) {
 				v.setImage();
 			}
 
 		}
 	}
 
 	public void setImage() {
 		img = ImageLoader.getVirusImage(color, level / LEVELUP);
 
 		size = 8;
 		int level = Virus.level / LEVELUP;
 
 		if (level >= 6) {
 			size = 12;
 		}
 		if (level >= 12) {
 			size = 16;
 		}
 	}
 
 	public static int getLevel() {
 		return level;
 	}
 
 	public void freeze() {
 		isFreezed = true;
 	}
 
 	public int getColor() {
 		return color;
 	}
 
 	public static void increaseSpeed() {
 		maxSpeed += 0.2;
 	}
 
 	public static void resetSpeed(){
 		maxSpeed = 2;
 	}
 	
 	public boolean isFrozen() {
 		return isFreezed;
 	}
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pewpew.entities.guns;
 
 import java.io.IOException;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import pewpew.Util;
 import pewpew.entities.Bullet;
 import pewpew.entities.Enemy;
 import pewpew.entities.Entity;
 import pewpew.entities.Player;
 
 /**
  * 
  * @author michael
  */
 public class BulletMissle extends Bullet {
 
 	byte stage = 0; // 0 = targeting, 1 = firering, 2 = exploding
 	Primer TargetSys;
 	BulletBomb bomb;
 	int Fuse = 300;
 	Enemy Target = null;
 	public static int SuggestedCost = 100;
 	public static int SuggestedCooldown = 120;
 	public String type = "Missle";
 	float xa;
 	float ya;
 	float acell = 2;
 	public static final Image MISSLE_BARREL;
 	static {
 		Image i = null;
 		try {
 			i = new Image("Res/Ship/MissileLauncher.png");
 		} catch (SlickException ex) {
 		}
 		MISSLE_BARREL = i;
 	}
 	public static final Audio BlastOff;
 	static {
 		Audio a = null;
 		try {
 			a = AudioLoader.getAudio("OGG", ResourceLoader
 					.getResourceAsStream("Res/Sounds/BlastOff.ogg"));
 		} catch (IOException ex) {
 		}
 		BlastOff = a;
 	}
 
 	public BulletMissle(float X, float Y, float Angle) {
 		super(X, Y, Angle);
 		TargetSys = new Primer(x, x, angle, 400);
 		speed = 10;
 		shape = new Polygon(new float[] { x, y, x + 10, y, x + 10, y + 30, x,
 				y + 30 });
 		stage = 0;
 
 	}
 
 	@Override
 	public void special() {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void death(byte conditions) {
 		cullable = true;
 
 	}
 
 	@Override
 	public int getScore() {
 		int s = 0;
 		if (bomb != null) {
 			s += bomb.score;
 		}
 		s += TargetSys.score + score;
 		return s;
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) {
 		TargetSys.render(gc, g);
 		if (stage == 1) {
 			g.draw(shape);
 		} else if (stage == 2) {
 			if (bomb != null) {
 				bomb.render(gc, g);
 			}
 		}
 	}
 
 	@Override
 	public void collides(Entity... en) {
 		if (stage == 0) {
 			TargetSys.collides(en);
 			Entity t = TargetSys.Target;
 			if (t != null) {
 				stage++;
 				BlastOff.playAsSoundEffect(1.0f, 0.5f, false);
 				Target = TargetSys.Target;
 				return;
 			}
 		} else if (stage == 1) {
 			for (Entity e : en) {
 				if (shape.intersects(e.getBounds())) {
 					stage++;
 					BlastOff.stop();
 					return;
 				}
 			}
 		} else if (stage == 2) {
 			if (bomb != null) {
 				bomb.collides(en);
 				return;
 			}
 		}
 		return;
 
 	}
 
 	@Override
 	public float doDamage() {
 
 		if (stage < 2) {
 			return 0;
 		}
 		return (int) (5f * dmgDealMult);
 	}
 
 	@Override
 	public void takeDamage(float Damage) {
 		score += doDamage();
 	}
 
 	@Override
 	public void move(Entity e) {
 		if (stage == 0) {
 			TargetSys.move(e);
 			Bullet.FireSound.playAsSoundEffect(1.0f, 0.5f, false);
 		} else if (stage == 1) {
 			// find angle to travel at
 			// Target.x = Entity.FORM_WIDTH-10;
 			// Target.y = Entity.FORM_HEIGHT-10;
 			float angleBetween = (float) (Math.atan(((y - Target.GetY()))
 					/ (x - Target.GetX())));
 			xa = (float) (acell * Math.cos(angleBetween));
 			ya = (float) (acell * Math.sin(angleBetween));
 			if (x - Target.GetX() > 0) {
 				xa = -Math.abs(xa);
 			}// WHY IS THIS NECCESARY!
 			if (y - Target.GetY() < 0) {
 				ya = Math.abs(ya);
 			} else if (y - Target.GetY() > 0) {
 				ya = -Math.abs(ya);
 			}
 			float currentSpeed = (float) Math.sqrt((xv * xv) + (yv * yv));
 			if (currentSpeed < speed || (xv < 0 && xa > 0)
 					|| (xv > 0 && xa < 0)) {
 				xv += xa;
 			}
 			if (currentSpeed < speed || (yv > 0 && ya < 0)
 					|| (yv < 0 && ya > 0)) {
 				yv += ya;
 			}
 			xv = Util.doFrictionX(xv, yv, 0.1f);
 			yv = Util.doFrictionY(xv, yv, 0.1f);
 			// System.out.println(xa);
 			// System.out.println(ya);
 			x += xv;
 			y += yv;
 			float angleFacing = (float) Math.tan(ya / xa);
 			// System.out.println(angleFacing - angle);
 			shape = (Polygon) shape.transform(Transform
 					.createRotateTransform((angleFacing - angle)));
 			shape.setCenterX(x);
 			shape.setCenterY(y);
 			Fuse--;
 			if (Fuse < 0) {
 				stage++;
 			}
 			angle = angleFacing;
 		} else if (stage == 2) {
 			if (bomb == null) {
 				bomb = new BulletBomb(x, y, 0.0f);
 				bomb.FuseRemaining = 0;
 				bomb.shrinkrate = 7f;
 				bomb.growthrate = 7f;
 				bomb.c = Color.red;
 			}
 			bomb.move(e);
 		}
 	}
 
 	@Override
 	public boolean cull() {
 		if (stage == 0) {
 			return TargetSys.cull();
 		} else if (stage == 2) {
 			if (bomb == null) {
 				return false;
 			}
 			return bomb.cull();
 		}
 		return false;
 	}
 
 	@Override
 	public String getType() {
            return "Missle";
 	}
 
 	@Override
 	public Entity[] getAllChildren() {
 		Entity[] e = { this, TargetSys };
 		return e;
 	}
 
 	@Override
 	public Polygon getBounds() {
 		if (stage == 0) {
 			if (TargetSys.shape == null && TargetSys.shape.getPointCount() > 0) {
 				return shape;
 			}
 			return TargetSys.shape;
 		} else if (stage == 1) {
 			return shape;
 		} else if (stage == 2) {
 			if (bomb == null) {
 				return shape;
 			} else
 				return bomb.getBounds();
 		} else {
 			return shape;
 		}
 	}
 }
 
 class Primer extends Bullet {
 
 	static Image TargetView;
 
 	static {
 		Image i;
 		try {
 			i = new Image("Res/Bullets/Targeter.png");
 		} catch (SlickException ex) {
 			i = null;
 		}
 		TargetView = i;
 	}
 	Enemy Target;
 	float MaxRadius;
 	private float OpenAngle;
 	private float WedgeSize = 45;
 	boolean instantFind = false;
 	boolean done = false;
 	int tick;
 	boolean cullable = false;
 	float showAtAngle = 0;
 	float ShowAtRadius = 0;
 
 	/**
 	 * Used to Find Nearest entity using a line, then a cone, which widens into
 	 * a square the triangle then continues behind until the entire screen is
 	 * covered
 	 * 
 	 */
 	Primer(float X, float Y, float Angle, float radius) {
 		super(X, Y, Angle);
 		MaxRadius = radius;
 		shape = new Polygon();
 	}
 
 	@Override
 	public void special() {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void death(byte conditions) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) {
 		g.setColor(Color.red);
 		if (shape != null) {
 			// Debug, draws the target wedge
 			// g.fill(getBounds());
 		}
 		if (Target == null) {
 			// select random point inside circle that's moveable to
 			if (tick % 10 == 0) {
 				float minAngle = -OpenAngle - 90 - WedgeSize / 2
 						+ (float) Math.toDegrees(angle);
 				float MaxAngle = OpenAngle - 90 - WedgeSize / 2
 						+ (float) Math.toDegrees(angle);
 				showAtAngle = (float) (Math.random() * (MaxAngle - minAngle) + minAngle);
 				ShowAtRadius = (float) Math.random() * MaxRadius + 10f;
 			}
 			float ShowAtX = (float) (ShowAtRadius * Math.cos(Math
 					.toRadians(showAtAngle)));
 			float ShowAtY = (float) (ShowAtRadius * Math.sin(Math
 					.toRadians(showAtAngle)));
 			TargetView.draw(x + ShowAtX, y + ShowAtY);
 		} else {
 			TargetView.draw(Target.GetX(), Target.GetY());
 		}
 	}
 
 	@Override
 	public void collides(Entity... en) {
 		if (Target != null) {
 			return;
 		}
 		for (Entity e : en) {
 			if (e == this || e.cull()) {
 				continue;
 			}
 			if (shape != null) {
 				if (shape.intersects(e.getBounds())) {
 					if (e.getSuperType().equals("Enemy")) {
 						Target = (Enemy) e;
 						return;
 					}
 
 				}
 			}
 		}
 		return;
 	}
 
 	@Override
 	public float doDamage() {
 		return 0;
 	}
 
 	@Override
 	public void takeDamage(float Damage) {
 		score += 10;
 	}
 
 	@Override
 	public void move(Entity e) {
 		Player p = (Player) e;
 		float deltax = x - p.getRotatedFirePointX();
 		float deltay = y - p.getRotatedFirePointY();
 		x = p.getRotatedFirePointX();
 		y = p.getRotatedFirePointY();
 		shape.transform(Transform.createTranslateTransform(deltax, deltay));
 		// shape.setCenterY(y);
 		angle = p.getBulletAngle();
 		// growthStage =1;
 		tick++;
 
 		if (tick % 30 == 0 || instantFind) {
 			if (!done) {
 				Growth();
 			}
 		}
 
 	}
 
 	private void Growth() {
 		/**
 		 * top points expands sideways, creating a growing triangle
 		 */
 		float topX = (float) (x + (MaxRadius * Math.cos(angle)));
 		float topY = (float) (y + (MaxRadius * Math.sin(angle)));
 		float rate = (WedgeSize / 2);
 		OpenAngle += rate * 2;
 
 		float[] a = new float[(int) (((OpenAngle * 2) / rate))];
 		int j = 0;
 		int initial;
 		int length1 = ((int) (((OpenAngle) / rate))) - 2;
 		for (int i = 0; i <= length1; i += 2) {
 			a[length1 - i] = x
 					+ (float) (MaxRadius * Math
 							.cos(Math
 									.toRadians(((i) * -rate) - (WedgeSize / 2))
 									+ angle));
 			a[length1 - i + 1] = y
 					+ (float) (MaxRadius * Math
 							.sin(Math
 									.toRadians(((i) * -rate) - (WedgeSize / 2))
 									+ angle));
 			j = i;
 		}
 		initial = j;
 		for (j = initial; j <= (int) (((OpenAngle * 2) / rate)) - 2; j += 2) {
 			a[j] = x
 					+ (float) (MaxRadius * Math.cos(Math
 							.toRadians(((j - initial) * rate) - WedgeSize / 2)
 							+ angle));
 			a[j + 1] = y
 					+ (float) (MaxRadius * Math.sin(Math
 							.toRadians(((j - initial) * rate) - WedgeSize / 2)
 							+ angle));
 		}
 		shape = new Polygon(a);
 		shape.addPoint(x, y);
 		if (OpenAngle > 180) {
 			done = true;
 			shape = null;
 		}
 
 	}
 
 	@Override
 	public boolean cull() {
 		return (done && Target == null) || cullable;
 	}
 
 	@Override
 	public Entity[] getAllChildren() {
 		Entity e[] = { this };
 		return e;
 	}
        
        @Override
	public String getType() {
            return "Primer";
	}
        
 }

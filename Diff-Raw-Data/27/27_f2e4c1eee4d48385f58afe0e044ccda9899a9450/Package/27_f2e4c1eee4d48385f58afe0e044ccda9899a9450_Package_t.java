 package entity;
 
 import networking.NetworkObjectType;
 import sound.SoundEffect;
 import gfx.ResourceManager;
 import baseGame.Rendering.RGBImage;
 import baseGame.Rendering.Renderer;
 import level.BasicLevel;
 
 public class Package extends Entity {
 
	
 	Gun wep;
 	PixelHitbox hitbox;
 
 	public Package(FloatingPoint point, BasicLevel level) {
 		super(point.getX(), point.getY(), 6, 6, level);
 		fillCrate();
 		this.hitbox = new PixelHitbox();
 		init();
 	}
 
 	public void init() {
 		hitbox.addPoint(new FloatingPoint(-xr, 0));
 		hitbox.addPoint(new FloatingPoint(0, -yr));
 		hitbox.addPoint(new FloatingPoint(0, 0));
 		hitbox.addPoint(new FloatingPoint(0, yr));
 		hitbox.addPoint(new FloatingPoint(xr, 0));
 	}
 
 	public void handleTerrainIntersections() {
 		boolean up = false;
 		boolean down = false;
 		boolean left = false;
 		boolean right = false;
 
 		FloatingPoint point = hitbox.getPoint(0);
 		if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 			left = true;
 		}
 		point = hitbox.getPoint(3);
 		if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 			right = true;
 		}
 		point = hitbox.getPoint(1);
 		if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 			up = true;
 		}
 		point = hitbox.getPoint(4);
 		if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 			down = true;
 		}
 
 		if (up && down && left && right) {
 			setLocation(x - dx * 2, y - dy * 2);
 			setSpeed(-dx * 0.5, -dy * 0.5);
 		} else if (up && down && left) {
 			setLocation(x - dx, y);
 			setSpeed(-dx * 0.5, dy);
 		} else if (up && down && right) {
 			setLocation(x - dx, y);
 			setSpeed(-dx * 0.5, dy);
 		} else if (up && left && right) {
 			setLocation(x, y - dy);
 			setSpeed(dx, -dy * 0.8);
 		} else if (down && left && right) {
 			setLocation(x, y - dy);
 			setSpeed(dx * 0.6, -dy * 0.5);
 		} else if (right || left) {
 			setLocation(x - dx, y);
 			setSpeed(-dx * 0.5, dy);
 		} else if (down || up) {
 			setLocation(x, y - dy);
 			setSpeed(dx * 0.6, -dy * 0.5);
 		}
 	}
 
 	private void fillCrate() {
 		int gun = rand.nextInt(4);
 		switch (gun) {
 		case 0:
 			this.wep = Gun.ROCKETLAUNCHER;
 			break;
 		case 1:
 			this.wep = Gun.MINELAUNCHER;
 			break;
 		case 2:
 			this.wep = Gun.AIRSTRIKE;
 			break;
 		case 3:
 			this.wep = Gun.MINIGUN;
 			break;
 		}
 	}
 
 	public void handlePlayerIntersections() {
 		for (Tank player : level.getPlayers()) {
 			if (intersectsEntity(player)) {
 				player.getWeaponList().get(wep.i).addAmmo();
 				System.out.println(wep);
 				SoundEffect.RELOAD.play();
 				remove();
 			}
 		}
 	}
 
 	@Override
 	public void tick(double dt) {
 		super.tick(dt);
 		handleTerrainIntersections();
 		handlePlayerIntersections();
 	}
 
 	
 	@Override
 	public void render(Renderer renderer) {
 		// TODO Auto-generated method stub
 		RGBImage img = ResourceManager.PACKAGE;
 		renderer.DrawImage(img, -1, (int) (x - 7), (int) (y - 14), img.getWidth(), img.getHeight());
 	}
 
 	@Override
 	public void initNetworkValues() {
 		// TODO Auto-generated method stub
 		setNetworkObjectType(NetworkObjectType.PACKAGE);
 	}
 
 }

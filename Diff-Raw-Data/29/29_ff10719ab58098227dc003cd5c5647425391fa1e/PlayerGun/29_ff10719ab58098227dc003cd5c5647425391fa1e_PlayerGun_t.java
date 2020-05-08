 package tomato.item;
 
 import java.awt.Graphics;
 import java.awt.Point;
 
 import tomato.Camera;
 import tomato.Game;
 import tomato.Input;
 import tomato.entity.Bullet;
 import tomato.entity.PhysicsEntity;
 import tomato.level.Level;
 import tomato.math.IVec2D;
 import tomato.math.Vec2DPrecise;
 
 public class PlayerGun extends AbstractBulletGun {
 	private int mouseX, mouseY;
 
 	public PlayerGun(PhysicsEntity player, Level level, int ammo,
 			double reloadTime, boolean autoReload) {
 		super(player, level);
 		this.reloadTime = reloadTime;
 		this.ammo = ammo;
 		this.autoReload = autoReload;
 	}
 
 	@Override
 	public void render(Graphics g, Camera cam) {
 		// g.drawImage(((e.faceRight) ? Art.player1 : Art.player2)[1][1],
 		// (int) e.x - cam.x, (int) e.y - cam.y, null);
 	}
 
 	@Override
 	public void tick(Input input, double delta) {
 		super.tick(input, delta);
 		Point mouse = input.getCurrentMousePosition();
 		mouseX = mouse.x;
 		mouseY = mouse.y;
 	}
 
 	@Override
 	public void shoot() {
 		super.shoot();
 		Camera cam = Camera.getInstance();
 		IVec2D v_player = new Vec2DPrecise(e.x + (e.w / 2), e.y + (e.h / 2));
 		IVec2D v_mouse = new Vec2DPrecise((mouseX / Game.getScreenScale()) + cam.x
 				- Bullet.BULLET_DIAMETER / 2, (mouseY / Game.getScreenScale())
 				+ cam.y - Bullet.BULLET_DIAMETER / 2);
 		IVec2D v_player_mouse = v_mouse.substract(v_player);
		IVec2D unitvector = v_player_mouse.getNormalized();
 		shoot(unitvector);
 	}
 
 	@Override
 	public void shoot(IVec2D unitvector) {
 		super.shoot(unitvector);
 	}
 
 	@Override
 	public int getAmmo() {
 		return ammo;
 	}
 
 }

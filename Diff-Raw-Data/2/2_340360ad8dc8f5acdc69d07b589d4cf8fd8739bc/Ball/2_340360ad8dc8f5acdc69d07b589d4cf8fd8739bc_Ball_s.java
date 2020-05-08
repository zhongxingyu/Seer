 package game;
 
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import menu.Commons;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 
 import util.Graphics;
 import assets.Textures;
 
 public class Ball {
 	private static final int STICKY_TIME = 4000;
 	private static final float SPEED_UP_DELTA = 0.02f;
 	private float x;
 	private float y;
 	private float r;
 	private float hitR;
 	private float dx;
 	private float dy;
 	private boolean sticky;
 	private long stickyTimer;
 	private float angle;
 	private float speedFactor;
 	private boolean hasBouncedInCurrentFrame;
 
 	public Ball(float x, float y, float r, float dx, float dy) {
 		this.x = x;
 		this.y = y;
 		this.r = r;
 		this.hitR = 0.5f * r;
 		this.dx = dx;
 		this.dy = dy;
 		this.sticky = true;
 		this.stickyTimer = 0l;
 		this.angle = 0.0f;
 		this.speedFactor = 1.0f;
 		this.hasBouncedInCurrentFrame = false;
 	}
 
 	public void render() {
 		glLoadIdentity();
 		glTranslatef(x, y, 0.0f);
 		glRotatef(angle, 0.0f, 0.0f, 1.0f);
 		Graphics.drawQuad(-0.5f * r, -0.5f * r, r, r, Textures.get().getBall(),
 				true);
 		glLoadIdentity();
 	}
 
 	public void update(float delta, Paddle paddle) {
 		if (sticky) {
 			updateStickyBall(delta, paddle);
 		} else {
 			updateNormalBall(delta, paddle);
 		}
 	}
 
 	private void updateStickyBall(float delta, Paddle paddle) {
 		x = paddle.getX() + paddle.getWidth() / 2.0f;
 		y = paddle.getY() + paddle.getBounceHeight() + 0.5f * r;
 		stickyTimer += delta;
 		if (Mouse.isButtonDown(0) && !Commons.get().isKeyPressed()
 				|| stickyTimer > STICKY_TIME) {
 			Commons.get().setKeyPressed(true);
 			sticky = false;
 		}
 	}
 
 	private void updateNormalBall(float delta, Paddle paddle) {
 		float newX = x + dx * delta / 300.0f;
 		float newY = y + dy * delta / 300.0f * speedFactor;
 
 		if (!hasBouncedInCurrentFrame) {
 			if (y - 0.5f * hitR <= paddle.getY() + paddle.getHeight()
 					&& y > paddle.getY() && x > paddle.getX()
 					&& x < paddle.getX() + paddle.getWidth()) {
 				dx = (x - (paddle.getX() + 0.5f * paddle.getWidth())) * 3.0f;
				dy = -dy;
 				paddle.bounce((x - paddle.getX()) / paddle.getWidth());
 				speedFactor += SPEED_UP_DELTA;
 				newX = x + dx * delta / 300.0f;
 				newY = y + dy * delta / 300.0f * speedFactor;
 				hasBouncedInCurrentFrame = true;
 			} else {
 				if (newX - 0.5f * hitR < 0
 						|| newX + 0.5f * hitR > Display.getWidth()) {
 					dx = -dx;
 					newX = x + dx * delta / 300.0f;
 					hasBouncedInCurrentFrame = true;
 				}
 				if (newY + 0.5f * hitR > Display.getHeight() - Hud.height) {
 					dy = -dy;
 					newY = y + dy * delta / 300.0f * speedFactor;
 					hasBouncedInCurrentFrame = true;
 				}
 			}
 		}
 		x = newX;
 		y = newY;
 
 		angle -= 0.1f * dx;
 	}
 
 	public void bounce(Block block) {
 		if (!hasBouncedInCurrentFrame) {
 			float blockCenterX = block.getX() + 0.5f * Block.getWidth();
 			float blockCenterY = block.getY() - 0.5f * Block.getHeight();
 			dx = x - blockCenterX > 0 ? Math.abs(dx) : -Math.abs(dx);
 			dy = y - blockCenterY > 0 ? Math.abs(dy) : -Math.abs(dy);
 			hasBouncedInCurrentFrame = true;
 		}
 	}
 
 	public boolean isAlive() {
 		return y + r >= 0;
 	}
 
 	public boolean isSticky() {
 		return sticky;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public float getHitR() {
 		return hitR;
 	}
 
 	public void setHasBouncedInCurrentFrame(boolean hasBounced) {
 		hasBouncedInCurrentFrame = hasBounced;
 	}
 
 }

 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.opengl.Texture;
 
 public abstract class TankEntity extends Entity {
 	
 	protected Texture gun;
 	protected Texture body;
 	// angle 0 is direct left
 	protected float gunAngle;
 	protected int bodyAngle = 0;
	protected int deltaAng = 5;
 	protected long firingInterval = 100;	// ms
 	protected long lastFire;
 	private int bulletIndex = 0;
 	private Bullet[]	bullets;
 	private ArrayList<Bullet> entities = new ArrayList<Bullet>();
 	private ArrayList<Bullet> removeList = new ArrayList<Bullet>();
 
 	public TankEntity() {
 		entities.clear();
 		bullets = new Bullet[20];
 		for (int i = 0; i < bullets.length; i++) {
 			bullets[i] = new Bullet(this.game);
 		}
 	}
 	
 	public void move(long delta,float setAng){
 		float nx = x + (delta * dx) / 10;
 		float ny = y + (delta * dy) / 10;
 		
 		if(dx != 0 || dy != 0){
 			/** add anima here */
 			
 			if(bodyAngle > setAng){
 				if(bodyAngle-setAng <= 180)
 					bodyAngle -= deltaAng;
 				else if(bodyAngle-setAng > 180)
 					bodyAngle += deltaAng;
 			}
 			if(bodyAngle < setAng){
 				if(setAng - bodyAngle <= 180)
 					bodyAngle += deltaAng;
 				else if(setAng - bodyAngle > 180)
 					bodyAngle -= deltaAng;
 			}
 			
 			if(bodyAngle == 360)
 				bodyAngle = 0;
 			if(bodyAngle == -deltaAng)
 				bodyAngle = 360-deltaAng;
 		}
 		xPreMove = x;
 		yPreMove = y;
 		if (validLocation(nx, ny)) {
 			x = (int)nx;
 			y = (int)ny;
 		} else if(validLocation(x, ny)){
 			y = (int)ny;
 		} else if(validLocation(nx, y)){
 			x = (int)nx;
 		}
 		
 		/*for ( Bullet entity : entities ) {
 			entity.move(delta);
 			if(this.game.map.blocked((int)entity.x/game.map.TILE_SIZE, (int)entity.y/game.map.TILE_SIZE)){
 				removeList.add(entity);
 				entity.setDX(entity.getMoveSpeed());
 				entity.setDY(entity.getMoveSpeed());
 			}
 		}*/
 	}
 	
 	public void moveBack() {
 		x = xPreMove;
 		y = yPreMove;
 	}
 	
 	public void Fire() {
 		/*if (System.currentTimeMillis() - lastFire < firingInterval) {
 			return;
 		}
 
 		System.out.println(bulletIndex+"\n");
 		lastFire = System.currentTimeMillis();
 		Bullet bullet = bullets[bulletIndex ++ % bullets.length];
 		bullet.reinitialize((float)(x-Math.cos(0.0174532925*gunAngle)*width/1.5), (float)(y-Math.sin(0.0174532925*gunAngle)*height/1.5),(float)-Math.cos(0.0174532925*gunAngle)*bullet.dx, (float)-Math.sin(0.0174532925*gunAngle)*bullet.dy);
 		entities.add(bullet);*/
 
 		//soundManager.playEffect(SOUND_SHOT);
 	}
 	
 	public void setGunAngle(float gunAngle) {
 		this.gunAngle = gunAngle;
 	}
 	
 	public void setBodyAngle(int bodyAngle) {
 		this.bodyAngle = bodyAngle;
 	}
 	
 	public void draw() {
 		glPushMatrix();
 		glTranslatef(x, y, 0);
         glRotatef(bodyAngle, 0f, 0f, 1f);
         glTranslatef(-x, -y, 0);
         
         body.bind();
         super.draw();
     	
 		glTranslatef(x, y, 0);
         glRotatef(gunAngle - bodyAngle, 0f, 0f, 1f);
         glTranslatef(-x, -y, 0);
         
        halfSize += 8;
         gun.bind();
         super.draw();
     	glPopMatrix();
    	halfSize -= 8;
         
         for ( Entity entity : entities ) {
         	entity.draw();
 		}
         entities.removeAll(removeList);
 		removeList.clear();
 	}
 	
 	public abstract void collidedWith(Entity other);
 }

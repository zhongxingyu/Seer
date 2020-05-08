 package com.sirolf2009.syringe.world.entity;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.sirolf2009.syringe.client.models.AABB;
 import com.sirolf2009.syringe.client.renderers.IEntityRenderer;
 import com.sirolf2009.syringe.world.World;
 
 public abstract class Entity {
 
 	private AABB AABB;
 
 	private World world;
 	private float posX;
 	private float posY;
 	private float posZ;
 	private float velX;
 	private float velY;
 	private float velZ;
 
 	private IEntityRenderer renderer;
 
 	public Entity(World world, IEntityRenderer renderer) {
 		this.world = world;
 		setRenderer(renderer);
 		AABB = getRenderer().getModel().AABB;
 	}
 
 	public void update(long delta) {
 		posX += velX;
 		posY += velY;
 		posZ += velZ;
 		AABB.setLocation(posX, posY, posZ);
 		if(AABB.intersects(world.ground)) {
 			velX *= .9;
			velY = 0;
 			velZ *= .9;
			System.out.println("clip");
 		} else {
 			velY -= .005;
 			System.out.println("noclip");
 		}
 		Entity colliding = checkColliding();
 		if((colliding = checkColliding()) != null) {
 			Vector3f bounce = getDistanceTo(colliding.getAABB());
 			velX -= bounce.x/100;
 			velY -= bounce.y/100;
 			velZ -= bounce.z/100;
 		}
 		velX *= 0.5;
 		velY *= 0.5;
 		velZ *= 0.5;
 	}
 
 	public Vector3f getDistanceTo(AABB AABB) {
 		float xAxis = Math.abs(getAABB().center.x - AABB.center.x); //distance between centers
 		float yAxis = Math.abs(getAABB().center.y - AABB.center.y); //distance between centers
 		float zAxis = Math.abs(getAABB().center.z - AABB.center.z); //distance between centers
 
 	    float cw = getAABB().width/2 + AABB.width/2; //combined width
 	    float ch = getAABB().height/2 + AABB.height/2; //combined width
 	    float cd = getAABB().depth/2 + AABB.depth/2; //combined width
 
 	    //early exit
 	    if(xAxis > cw) return null;
 	    if(yAxis > ch) return null;
 	    if(zAxis > cd) return null;
 	    
 		float ox = Math.abs(xAxis - cw); //overlap on x
 		float oy = Math.abs(yAxis - ch); //overlap on y
 		float oz = Math.abs(zAxis - cd); //overlap on z
 
 		//direction
 		Vector3f deltaDir = Vector3f.sub(AABB.center, getAABB().center, null); //subtract other.position from this.position
 		deltaDir.normalise();
 
 		return new Vector3f(deltaDir.x * ox, deltaDir.y * oy, deltaDir.z * oz);
 		
 		/*Vector3f distance = new Vector3f();
 		distance.x = getAABB().center.x - AABB.center.x;
 		distance.y = getAABB().center.y - AABB.center.y;
 		distance.z = getAABB().center.z - AABB.center.z;
 		distance.normalise();
 		return distance;*/
 	}
 
 	private Entity checkColliding() {
 		for(Entity entity : getWorld().entities) {
 			if(entity == this) {
 				continue;
 			}
 			if(AABB.intersects(entity.getAABB())) {
 				return entity;
 			}
 		}
 		return null;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public void setWorld(World world) {
 		this.world = world;
 	}
 
 	public float getPosX() {
 		return posX;
 	}
 
 	public void setPosX(float posX) {
 		this.posX = posX;
 	}
 
 	public float getPosY() {
 		return posY;
 	}
 
 	public void setPosY(float posY) {
 		this.posY = posY;
 	}
 
 	public float getPosZ() {
 		return posZ;
 	}
 
 	public void setPosZ(float posZ) {
 		this.posZ = posZ;
 	}
 
 	public float getVelX() {
 		return velX;
 	}
 
 	public void setVelX(float velX) {
 		this.velX = velX;
 	}
 
 	public float getVelY() {
 		return velY;
 	}
 
 	public void setVelY(float velY) {
 		this.velY = velY;
 	}
 
 	public float getVelZ() {
 		return velZ;
 	}
 
 	public void setVelZ(float velZ) {
 		this.velZ = velZ;
 	}
 
 	public IEntityRenderer getRenderer() {
 		return renderer;
 	}
 
 	public void setRenderer(IEntityRenderer renderer) {
 		this.renderer = renderer;
 	}
 
 	public AABB getAABB() {
 		return AABB;
 	}
 
 	public void setAABB(AABB AABB) {
 		this.AABB = AABB;
 	}
 }

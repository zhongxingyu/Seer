 package weapons;
 
 import gameObjects.GameObject;
 import gameObjects.GameObjectData;
 
 import com.golden.gamedev.object.SpriteGroup;
 
 public class CreationProjectile extends Projectile{
 	GameObject myCreated;
 	SpriteGroup myCreatedSpriteGroup;
 
 	CreationProjectile(int speed, String imgPath, SpriteGroup g, GameObject created, SpriteGroup sg) {
 		super(speed, imgPath, g);
 		myCreated = created;
 		myCreatedSpriteGroup = sg;
 		
 	}
 
 	@Override
 	public void actionOnCollision(GameObject hit) {
		myCreated.setLocation(myX, myY);
 		myCreatedSpriteGroup.add(myCreated);
 		removeProjectile();
 		
 	}
 
 	@Override
 	public void createProjectile(double x, double y) {
 		CreationProjectile createdProjectile = new CreationProjectile(mySpeed, myImgPath, myGroup, myCreated, myCreatedSpriteGroup);
 		createdProjectile.setPosition(x,y);
 		myGroup.add(createdProjectile);
 	}
 
 	@Override
 	public GameObject makeGameObject(GameObjectData god) {
 		String path = god.getImgPath();
 		Projectile returning = new CreationProjectile(mySpeed, path, myGroup, myCreated, myCreatedSpriteGroup);
 		double x = god.getX();
 		double y = god.getY();
 		returning.setPosition(x, y);
 		return returning;
 	}
 
 }

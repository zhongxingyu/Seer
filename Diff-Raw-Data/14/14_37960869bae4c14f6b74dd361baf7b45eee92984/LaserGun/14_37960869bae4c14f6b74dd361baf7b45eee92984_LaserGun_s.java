 package bmc.game.gameobjects;
 
 import java.util.List;
 
 import bmc.game.SpriteLocations;
 import android.graphics.Canvas;
 
 public class LaserGun extends GameObject {
 	protected float rotation;
 	protected Player player;
 	protected long lastShot = 0;
 	protected List<GameObject> gameObjects;
 	Sprite[] sprites;
 	
 	public LaserGun(Sprite[] sprites, float x, float y, Player player, List<GameObject> gameObjects, int spriteLocation)
 	{
 		super(sprites, spriteLocation);
 		mVelocityX = 0;
 		mVelocityY = 0;
		float playerX = player.getRect().centerX();
		float playerY = player.getRect().centerY();
		double angle = Math.atan( (playerY-y) / (playerX-x) );
		if( playerX - x < 0 )
			angle += Math.PI;
		rotation = (float)(angle * 180 / Math.PI);
		setX(x - mWidth/2f);
		setY(y - mHeight/2f);
 		this.player = player;
 		this.gameObjects = gameObjects;
 		this.sprites = sprites;
 	}
 	
 	public LaserGun(Sprite[] sprites, float x, float y, Player player, List<GameObject> gameObjects)
 	{
 		this(sprites, x, y, player, gameObjects, SpriteLocations.Laser.getLocation());
 	}
 	
 	public void doDraw(Canvas canvas)
 	{
 		mSprites[index].doDraw(canvas, mDestination, rotation);
 	}
 	
 	public void animate(long elapsedTime)
 	{
 		lastShot += elapsedTime;
 		if(lastShot > 3000)
 		{
 			synchronized (gameObjects)
 			{
 				gameObjects.add(new Laser(sprites, getRect().centerX(), getRect().centerY(), player.getRect().centerX(), player.getRect().centerY()));
 				lastShot = 0;
 			}
 		}
 		float x = getX();
 		float y = getY();
 		float playerX = player.getRect().centerX();
 		float playerY = player.getRect().centerY();
 		double angle = Math.atan( (playerY-y) / (playerX-x) );
 		if( playerX - x < 0 )
 			angle += Math.PI;
 		rotation = (float)(angle * 180 / Math.PI);
 		mSprites[index].animate(elapsedTime);
 	}
 
 	public void addVelocityX(double vel)
 	{
 		return;
 	}
 	
 	public void addVelocityY(double vel)
 	{
 		return;
 	}
 	
 	public void setPlayer(Player player)
     {
         this.player = player;
     }
     
     public void setList(List<GameObject> objects)
     {
         this.gameObjects = objects;
     }
 }

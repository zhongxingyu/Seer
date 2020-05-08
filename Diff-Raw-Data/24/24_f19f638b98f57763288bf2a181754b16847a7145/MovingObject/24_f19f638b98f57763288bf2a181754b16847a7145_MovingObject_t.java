 import java.awt.geom.Point2D;
 
 import GameEngine.Game.GameDrawer;
 import GameEngine.GameObject;
 
 
 public abstract class MovingObject extends MyGameObject {
 
 	float direction;
     
     Point2D.Float oldPosition;
 	
 	public MovingObject(float arg0, float arg1, SurvivalGame game) {
 		super(arg0, arg1, game);
         oldPosition = getPosition();
 	}
 	   
 	public int getGridX()
 	{
 		return (int) (getPosition().x / SurvivalGame.widthTile);
 	}
 	
 	public int getGridY()
 	{
 		return (int) (getPosition().y / SurvivalGame.heightTile);
 	}
     
     
     public void setDirection(float direction)
     {
         while (direction < 0.0) direction += 360.0;
         while (direction >= 360.0) direction -= 360.0;
         
         this.direction = direction;
         
         // Set the rotation to be the same as the direction value
         rotation = direction;
     }
     
     public void revertPosition ()
     {
     	this.setPosition(oldPosition);
     }
     
     public float getDirection()
     {
 		return direction;
 	}
 
 
 	public void moveInDirection(float direction, float speed)
 	{
 		oldPosition = this.getPosition();
 		setDirection(direction);
 		if (game.canMoveInDirection(this))
 			incrementPosition((float)Math.sin(Math.toRadians(direction))*speed, -(float)Math.cos(Math.toRadians(direction))*speed);
 		
 	}
 	
	
 	public abstract float[] getColor();
 	
 
 }

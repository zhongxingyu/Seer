 package pt.up.fe.pt.lpoo.bombermen.entities;
 
 import pt.up.fe.pt.lpoo.bombermen.Constants;
 import pt.up.fe.pt.lpoo.bombermen.Entity;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Wall extends Entity
 {
     public Wall(int guid, int hitPoints)
     {
         super(Entity.TYPE_WALL, guid);
 
         _hitPoints = hitPoints;
 
         BoundRect.setWidth(Constants.WALL_BOUNDING_WIDTH);
         BoundRect.setHeight(Constants.WALL_BOUNDING_HEIGHT);
     }
 
     private int _hitPoints;
     public static TextureRegion Regions[][];
 
     public boolean IsUndestroyable()
     {
         return _hitPoints != -1;
     }
 
     @Override
     public void draw(SpriteBatch batch, float parentAlpha)
     {
         Color color = getColor();
         batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
         batch.draw(Regions[0][IsUndestroyable() ? 1 : 0], getX(), getY(), getOriginX(), getOriginY(),
                 getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
     }
 }

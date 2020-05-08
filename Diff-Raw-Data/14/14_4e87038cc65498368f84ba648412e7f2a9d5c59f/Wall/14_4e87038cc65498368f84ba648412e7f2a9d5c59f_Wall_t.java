 package pt.up.fe.pt.lpoo.bombermen.entities;
 
 import pt.up.fe.pt.lpoo.bombermen.Constants;
 import pt.up.fe.pt.lpoo.bombermen.Entity;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
 
 public class Wall extends Entity
 {
    private static final float offsetWidth = (Constants.WALL_WIDTH - Constants.WALL_BOUNDING_WIDTH) / 2.f;
    private static final float offsetHeight = (Constants.WALL_HEIGHT - Constants.WALL_BOUNDING_HEIGHT) / 2.f;
    
    @Override
    public Rectangle GetBoundingRectangle()
    {
        Rectangle rect = super.GetBoundingRectangle();
        rect.x += offsetWidth;
        rect.y += offsetHeight;
        return rect;
    }

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

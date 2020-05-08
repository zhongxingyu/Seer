 package pt.up.fe.pt.lpoo.bombermen;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class PowerUp extends Entity
 {
    public PowerUp(int guid, int type)
     {
         super(Entity.TYPE_POWER_UP, guid);
 
         _type = type;
         _stateTime = 0;
     }
 
     private int _type;
     private float _stateTime;
     public static TextureRegion Regions[][];
    public static Animation Animation = null;
 
     @Override
     public void OnCollision(Entity other)
     {
     }
 
     @Override
     public void OnDestroy()
     {
     }
 
     @Override
     public void OnExplode(Explosion e)
     {
     }
 
     @Override
     public void draw(SpriteBatch batch, float parentAlpha)
     {
         Color color = getColor();
         batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(Animation.getKeyFrame(_stateTime, true), getX(), getY(), getOriginX(), getOriginY(),
                 getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
     }
 }

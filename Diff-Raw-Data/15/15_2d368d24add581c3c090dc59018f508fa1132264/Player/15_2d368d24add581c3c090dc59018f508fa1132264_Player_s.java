 package pt.up.fe.pt.lpoo.bombermen.entities;
 
 import pt.up.fe.pt.lpoo.bombermen.Bombermen2;
 import pt.up.fe.pt.lpoo.bombermen.Constants;
 import pt.up.fe.pt.lpoo.bombermen.Entity;
 import pt.up.fe.pt.lpoo.utils.Direction;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.Array;
 
 public class Player extends Entity
 {
     
     private static final float offsetWidth = (Constants.PLAYER_WIDTH - Constants.PLAYER_BOUNDING_WIDTH) / 2.f;
     private static final float offsetHeight = (Constants.PLAYER_HEIGHT - Constants.PLAYER_BOUNDING_HEIGHT) / 2.f;
 
     @Override
     public Rectangle GetBoundingRectangle()
     {
         Rectangle rect = super.GetBoundingRectangle();
         rect.x += offsetWidth;
         rect.y += offsetHeight;
         return rect;
     }
 
     public static Animation[] Animations = { null, null, null, null };
 
     private static BitmapFont font = new BitmapFont();
 
     private int _moveAnimationTimer = 0;
 
     public void SetMoving(int dir, boolean val)
     {
         _moving[dir] = val;
     }
     
     private boolean[] _moving = { false, false, false, false };
 
     @Override
     public void act(float delta)
     {
         _stateTime += delta;
         super.act(delta);
 
         delta *= 1000;
 
         Array<Entity> entities = Bombermen2._game.GetEntities();
 
         for (int i = 0; i < _moving.length; ++i)
         {
             if (!_moving[i]) continue;
 
             boolean moved = true;
 
             float x = getX();
             float y = getY();
 
             switch (i)
             {
                 case Direction.NORTH:
                     setY(getY() + Constants.INIT_PLAYER_SPEED * delta);
                     break;
                 case Direction.SOUTH:
                     setY(getY() - Constants.INIT_PLAYER_SPEED * delta);
                     break;
                 case Direction.EAST:
                     setX(getX() + Constants.INIT_PLAYER_SPEED * delta);
                     break;
                 case Direction.WEST:
                     setX(getX() - Constants.INIT_PLAYER_SPEED * delta);
                     break;
                 default:
                     moved = false;
             }
 
             if (moved)
             {
                 for (Entity e : entities)
                 {
                     if (e.IsBomb() && e.ToBomb().JustCreated() && e.ToBomb().GetCreatorGuid() == GetGuid()) if (Collides(e))
                         break;
                     else
                         e.ToBomb().SetJustCreated(false);
                     
                     if (!e.IsPlayer() && Collides(e))
                     {
                         moved = false;
                         setX(x);
                         setY(y);
                         break;
                     }
 
                 }
             }
         }
 
     }
 
     private float _stateTime;
 
     @Override
     public void setX(float x)
     {
         if (getX() < x)
             _direction = Direction.EAST;
         else if (getX() > x) _direction = Direction.WEST;
         super.setX(x);
         _moveAnimationTimer += 2;
     }
 
     @Override
     public void setY(float y)
     {
         if (getY() < y)
             _direction = Direction.NORTH;
         else if (getY() > y) _direction = Direction.SOUTH;
         super.setY(y);
         _moveAnimationTimer += 2;
     }
 
     public Player(int guid, String name)
     {
         super(Entity.TYPE_PLAYER, guid);
         
         _direction = Direction.SOUTH;
         setName(name);
         
         BoundRect.setWidth(Constants.PLAYER_BOUNDING_WIDTH);
         BoundRect.setHeight(Constants.PLAYER_BOUNDING_HEIGHT);
     }
 
     private int _direction;
     public static TextureRegion Regions[][];
 
     @Override
     public void draw(SpriteBatch batch, float parentAlpha)
     {
         Color color = getColor();
         batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
 
        batch.draw(Animations[_direction].getKeyFrame((_moveAnimationTimer > 0 ? _stateTime : 0), true), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        _moveAnimationTimer -= _moveAnimationTimer > 0 ? 1 : 0;
 
         font.draw(batch, getName(), getX() - Constants.PLAYER_WIDTH / 2f, getY() + Constants.PLAYER_HEIGHT * 3f / 2f);
 
     }
 }

 package pt.up.fe.pt.lpoo.bombermen;
 
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_SPAWN;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public abstract class Entity
 {
     public static final int TYPE_PLAYER = 0;
     public static final int TYPE_BOMB = 1;
     public static final int TYPE_EXPLOSION = 2;
     public static final int TYPE_WALL = 3;
     public static final int TYPE_POWER_UP = 4;
 
     private int _type;
     private int _guid;
     private Rectangle _rect;
 
     Entity(int type, int guid, Vector2 pos)
     {
         _type = type;
         _guid = guid;
         Vector2 size = this.GetSize();
         _rect = new Rectangle(pos.x, pos.y, size.x, size.y);
     }
 
    public boolean Overlaps(Rectangle r)
    {
        return _rect.overlaps(r);
    }

     public boolean Collides(Entity e)
     {
        return Overlaps(e._rect);
     }
 
     public abstract void OnCollision(Entity e);
 
     public abstract Vector2 GetSize();
 
     public int GetType()
     {
         return _type;
     }
 
     public int GetGuid()
     {
         return _guid;
     }
 
     public void SetX(float x)
     {
         _rect.x = x;
     }
 
     public void SetY(float y)
     {
         _rect.y = y;
     }
 
     public float GetX()
     {
         return _rect.x;
     }
 
     public float GetY()
     {
         return _rect.y;
     }
 
     public void SetPosition(float x, float y)
     {
         _rect.x = x;
         _rect.y = y;
     }
 
     public Vector2 GetPosition()
     {
         return new Vector2(_rect.x, _rect.y);
     }
 
     public boolean IsPlayer()
     {
         return _type == TYPE_PLAYER;
     }
 
     public boolean IsBomb()
     {
         return _type == TYPE_BOMB;
     }
 
     public boolean IsExplosion()
     {
         return _type == TYPE_EXPLOSION;
     }
 
     public boolean IsWall()
     {
         return _type == TYPE_WALL;
     }
 
     public boolean IsPowerUp()
     {
         return _type == TYPE_POWER_UP;
     }
 
     public Player ToPlayer()
     {
         return IsPlayer() ? (Player) this : null;
     }
 
     public Bomb ToBomb()
     {
         return IsBomb() ? (Bomb) this : null;
     }
 
     public Explosion ToExplosion()
     {
         return IsExplosion() ? (Explosion) this : null;
     }
 
     public Wall ToWall()
     {
         return IsWall() ? (Wall) this : null;
     }
 
     public PowerUp ToPowerUp()
     {
         return IsPowerUp() ? (PowerUp) this : null;
     }
 
     public abstract SMSG_SPAWN GetSpawnMessage();
 }

 package pt.up.fe.pt.lpoo.bombermen;
 
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_DESTROY;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_SPAWN;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_SPAWN_BOMB;
 import pt.up.fe.pt.lpoo.utils.Direction;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Bomb extends Entity
 {
     private static CollisionHandler<Bomb> cHandler = new CollisionHandler<Bomb>()
     {};
 
     public interface ExplodeHandler
     {
         void OnExplode();
     }
 
     private int _explosionRadius;
     private int _bombTimer; // ms
     private int _radius[] = { 0, 0, 0, 0 };
     private boolean _justCreated = true;
     private int _creatorGuid;
     private ExplodeHandler _explodeHandler = null;
 
     public int GetCreatorGuid()
     {
         return _creatorGuid;
     }
 
     public void SetJustCreated(boolean val)
     {
         _justCreated = false;
     }
 
     public boolean JustCreated()
     {
         return _justCreated;
     }
 
     Bomb(int guid, int creatorGuid, Vector2 pos, int explosionRadius, BombermenServer sv)
     {
         super(TYPE_BOMB, guid, pos, sv);
 
         _creatorGuid = creatorGuid;
 
         _explosionRadius = explosionRadius;
         _bombTimer = 0;
     }
 
     public int GetExplosionRadius()
     {
         return _explosionRadius;
     }
 
     @Override
     public SMSG_SPAWN GetSpawnMessage()
     {
         return new SMSG_SPAWN_BOMB(GetGuid(), GetX(), GetY());
     }
 
     private static final Vector2 size = new Vector2(Constants.BOMB_WIDTH, Constants.BOMB_HEIGHT);
 
     @Override
     public Vector2 GetSize()
     {
         return size;
     }
 
     @Override
     public void OnCollision(Entity e)
     {
         cHandler.OnCollision(this, e);
     }
 
     public void TriggerExplosion()
     {
         _bombTimer += Constants.BOMB_TIMER;
     }
 
     public void AddOnExplodeHandler(ExplodeHandler eh)
     {
         _explodeHandler = eh;
     }
 
     @Override
     public void Update(int diff)
     {
         _bombTimer += diff;
 
         if (_bombTimer > Constants.BOMB_TIMER)
         {
             // destroy this bomb
             _server.SendAll(new SMSG_DESTROY(GetGuid()));
             _server.RemoveEntityNextUpdate(GetGuid());
 
             CalculateRadiuses();
 
             Vector2 pos = new Vector2(GetX() - (Constants.WALL_WIDTH  - Constants.BOMB_WIDTH),
                     GetY() - (Constants.WALL_HEIGHT - Constants.BOMB_HEIGHT));
 
             int id = _server.IncLastId();
             Explosion exCenter = new Explosion(id, pos, Direction.NONE, false, _server);
             _server.CreateEntityNextUpdate(exCenter);
             _server.SendAll(exCenter.GetSpawnMessage());
 
             for (int d : Direction.Directions)
             {
                 for (int i = 1; i <= _radius[d]; ++i)
                 {
                     id = _server.IncLastId();
 
                     pos.x = Direction.ApplyMovementX(GetX() - (Constants.WALL_WIDTH  - Constants.BOMB_WIDTH),  d, (int)(i * Constants.EXPLOSION_WIDTH));
                     pos.y = Direction.ApplyMovementY(GetY() - (Constants.WALL_HEIGHT - Constants.BOMB_HEIGHT), d, (int)(i * Constants.EXPLOSION_HEIGHT));
 
                     Explosion ex = new Explosion(id, pos, d, i == _radius[d], _server);
                     _server.CreateEntityNextUpdate(ex);
                     _server.SendAll(ex.GetSpawnMessage());
                 }
             }
 
             if (_explodeHandler != null)
                 _explodeHandler.OnExplode();
         }
     }
 
     public void CalculateRadiuses()
     {
         boolean[] draw = { true, true, true, true };
 
        Rectangle r = new Rectangle(0, 0, Constants.EXPLOSION_WIDTH, Constants.EXPLOSION_HEIGHT);
 
         for (int i = 1; i <= _explosionRadius; ++i)
         {
             for (int d : Direction.Directions)
             {
                r.x = Direction.ApplyMovementX(GetX() - (Constants.WALL_WIDTH  - Constants.BOMB_WIDTH),  d, (int)(i * Constants.EXPLOSION_WIDTH));
                r.y = Direction.ApplyMovementY(GetY() - (Constants.WALL_HEIGHT - Constants.BOMB_HEIGHT), d, (int)(i * Constants.EXPLOSION_HEIGHT));

                 if (draw[d])
                 {
                     _radius[d]++;
                     for (Entity e : _server.GetEntities().values())
                     {
                         if (e.IsWall() && e.Collides(r))
                         {
                             Wall w = e.ToWall();
                             if (w.IsUndestroyable())
                                 _radius[d]--;
                             draw[d] = false;
                         }
                     }
                 }
             }
         }
     }
 }

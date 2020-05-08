 package com.fisherevans.lrk.states.adventure;
 
 import com.fisherevans.lrk.LRK;
 import com.fisherevans.lrk.rpg.RPGEntityGenerator;
 import com.fisherevans.lrk.states.adventure.entities.AdventureEntity;
 import com.fisherevans.lrk.states.adventure.entities.DumbBlob;
 import com.fisherevans.lrk.states.adventure.entities.PlayerEntity;
 import com.fisherevans.lrk.states.adventure.entities.Wall;
 import com.fisherevans.lrk.states.adventure.lights.PlayerLight;
 import com.fisherevans.lrk.tools.JBox2DUtils;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.tiled.TiledMap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: immortal
  * Date: 8/18/13
  * Time: 8:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EntityManager
 {
     private AdventureState _state;
 
     private ArrayList<AdventureEntity> _entities, _entitiesToDelete;
     private AdventureEntity _camera;
     private PlayerEntity _player;
 
     private EntityCompareY _entityCompareY;
 
     private int _entitiesMapIndex;
 
     public EntityManager(AdventureState state)
     {
         _state = state;
 
         _entityCompareY = new EntityCompareY();
 
         _entities = new ArrayList<>();
         _entitiesToDelete = new ArrayList<>();
 
         _player = new PlayerEntity(0, 0, state.getWorld(), _state);
         _entities.add(_player);
         _camera = _player;
 
         _entitiesMapIndex = _state.getTiledMap().getLayerIndex("entities");
     }
 
     public void processTile(int x, int y, TiledMap tiledMap)
     {
         processEntitiesTile(x, y, tiledMap.getLocalTileId(x, y, _entitiesMapIndex));
     }
 
     private void processEntitiesTile(int x, int y, int id)
     {
         if(id < 0)
             return;
 
         LRK.log("Checking id " + id);
 
         boolean noMatch = false;
         switch(id)
         {
             case 0: // PLAYER
                 LRK.log("Setting player position " + x + ", " + y);
                 _player.getBody().setTransform(new Vec2(x, y), _player.getBody().getAngle());
                 break;
             case 1: // DUMB BLOB
                 _entities.add(new DumbBlob(RPGEntityGenerator.getBlob(), x, y, _state.getWorld(), _state));
                 break;
             default:
                 noMatch = true;
         }
 
         if(noMatch)
             LRK.log("Failed to load the entities tile: " + id);
     }
 
     public void update(float delta)
     {
         // Remove to-be-deleted entities
         flushRemoveEntityQueue();
 
         // Update the entities
         for(AdventureEntity entity:_entities)
             entity.update(delta);
 
         // Sort the entities by their Y value
         Collections.sort(_entities, _entityCompareY);
     }
 
     public void flushRemoveEntityQueue()
     {
         if(_entitiesToDelete.size() > 0)
         {
             for(AdventureEntity entity:_entitiesToDelete)
             {
                 _entities.remove(entity);
                 entity.destroy();
                 _state.getWorld().destroyBody(entity.getBody());
             }
             _entitiesToDelete.clear();
         }
     }
 
     public void removeEntity(AdventureEntity entity)
     {
         _entitiesToDelete.add(entity);
     }
 
     public ArrayList<AdventureEntity> getEntities()
     {
         return _entities;
     }
 
     public AdventureEntity getCamera()
     {
         return _camera;
     }
 
     public PlayerEntity getPlayer()
     {
         return _player;
     }
 
     public class EntityCompareY implements Comparator<AdventureEntity>
     {
         @Override
         public int compare(AdventureEntity ent1, AdventureEntity ent2)
         {
             if(ent1.getY() > ent2.getY())
                 return 1;
             else if(ent1.getY() < ent2.getY())
                 return -1;
             else
                 return 0;
         }
     }
 }

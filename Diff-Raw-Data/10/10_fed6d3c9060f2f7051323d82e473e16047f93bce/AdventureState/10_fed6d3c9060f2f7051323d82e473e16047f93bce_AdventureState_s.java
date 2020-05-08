 package com.fisherevans.lrk.states.adventure;
 
 import com.fisherevans.lrk.Resources;
 import com.fisherevans.lrk.launcher.Game;
 import com.fisherevans.lrk.LRK;
 import com.fisherevans.lrk.StateLibrary;
 import com.fisherevans.lrk.managers.DisplayManager;
 import com.fisherevans.lrk.managers.InputManager;
 import com.fisherevans.lrk.managers.MusicManager;
 import com.fisherevans.lrk.states.adventure.lights.LightManager;
 import com.fisherevans.lrk.states.adventure.ui.AdventureDebugUI;
 import com.fisherevans.lrk.states.character.CharacterState;
 import com.fisherevans.lrk.states.GFX;
 import com.fisherevans.lrk.states.LRKState;
 import com.fisherevans.lrk.states.adventure.entities.AdventureEntity;
 import com.fisherevans.lrk.states.adventure.ui.PlayerStats;
 import com.fisherevans.lrk.states.transitions.SimpleFadeTransition;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.*;
 import org.newdawn.slick.tiled.TiledMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Fisher evans
  * Date: 5/12/13
  * Time: 5:28 PM
  */
 public class AdventureState extends LRKState
 {
 
     public static final float
         TILE_SIZE = 32f,
             DEFAULT_RENDER_DISTANCE = 7.5f,
             MAX_RENDER_DISTANCE = 11f;
 
     public static float TILES_WIDE, TILES_HIGH;
 
     private TiledMap _tiledMap;
     private World _world;
 
     private EntityManager _entityManager;
     private CollisionManager _collisionManager;
     private LightManager _lightManager;
 
     private EffectManager _effectManager;
 
     private SpriteManager _backgroundSpriteManager;
     private SpriteManager _foregroundSpriteManager;
 
     private Vec2 _aimShift = new Vec2(0, 0);
 
     private String _levelName = "small_test";
 
     public AdventureState(String levelName) throws SlickException
     {
         super(StateLibrary.getTempID());
         addUIComponent(new PlayerStats(this));
         addUIComponent(new AdventureDebugUI(this));
         setGrabMouse(true);
 
         _levelName = levelName;
     }
 
     @Override
     public void init() throws SlickException
     {
         resize();
 
         MusicManager.play("calm_wip");
         Game.lrk.getPlayer().reset();
 
         //_lightManager = new LightManager(this, new Color(0.7f, 0.7f, 0.85f));
         //Light playerLight = new PlayerLight(_playerEntity, _lightManager);
         //_lightManager.addLight(playerLight);
         //_lightManager.setCameraLight(playerLight);
 
         try // load the map
         {
             _tiledMap = new TiledMap("res/maps/template.tmx");
         }
         catch(Exception e)
         {
             e.printStackTrace();
             LRK.log("FAILED TO LOAD MAP");
             System.exit(111);
         }
 
         _world = new World(new Vec2(0, 0f), true);
         _world.setAllowSleep(true);
 
         _entityManager = new EntityManager(this);
         _collisionManager = new CollisionManager(this);
         _lightManager = new LightManager(this, new Color(0.25f, 0.25f, 0.35f));
 
         _effectManager = new EffectManager(this);
 
         _backgroundSpriteManager = new SpriteManager(this);
         _foregroundSpriteManager = new SpriteManager(this);
 
         for(int y = 0;y < _tiledMap.getHeight();y++)
         {
             for (int x = 0; x < _tiledMap.getWidth(); x++) // loop through each tile in the map
             {
                 _collisionManager.processTile(x, y, _tiledMap);
                 _entityManager.processTile(x, y, _tiledMap);
                 _lightManager.processTile(x, y, _tiledMap);
             }
         }
     }
 
     @Override
     public void enter() throws SlickException
     {
         InputManager.setQueryControllerMovement(false);
         resize();
     }
 
     @Override
     public void exit() throws SlickException
     {
         InputManager.setQueryControllerMovement(true);
     }
 
     @Override
     public void render(Graphics gfx) throws SlickException
     {
         // WHEN DRAWING ENTITIES, SHIFT EACH BY X AND Y SHIFT
         // SUBTRACT THE CAMERA POS AND ADD HALF THE DISPLAY AND SUBTRACT THE AIM SHIFT
         float xShift = (TILES_WIDE/2f - _entityManager.getCamera().getX())*TILE_SIZE - _aimShift.x;
         float yShift = (TILES_HIGH/2f - _entityManager.getCamera().getY())*TILE_SIZE - _aimShift.y;
 
         // WHAT TILE TO START DRAWING AT
         int startX = (int)(_entityManager.getCamera().getX()+_aimShift.x/TILE_SIZE) - (int)TILES_WIDE/2 - 1;
         int startY = (int)(_entityManager.getCamera().getY()+_aimShift.y/TILE_SIZE) - (int)TILES_HIGH/2 - 1;
 
         // CALC THE RENDER SIZE FOR THE VIGNETTE
         int vignetteSize = (int) (getRenderDistance() *TILE_SIZE*2);
 
         // PRE-CALC THE DRAW COORID'S FOR THE ENTITIES
         for(AdventureEntity ent: _entityManager.getEntities()) // FOR EACH ENTITY
         {
             if(inRenderArea(ent)) // IF THEIR IN RENDER DISTANCE
                 ent.getDrawPosition().set(getDrawPosition(ent.getBody().getPosition(), xShift, yShift)); // DRAW THEM WITH THE X AND Y SHIFTS
             else
                 ent.getDrawPosition().set(new Vec2(-1000, -1000));
         }
 
         float clipX = xShift + _entityManager.getCamera().getX()*TILE_SIZE-vignetteSize/2f + 1;
         float clipY = yShift + _entityManager.getCamera().getY()*TILE_SIZE-vignetteSize/2f + 1;
         float clipSize = vignetteSize - 2;
 
         // CLIP THE DRAWING AROUND THE RENDER DISTANCE
         GFX.clip(clipX, clipY, clipSize, clipSize, DisplayManager.getBackgroundScale());
 
         // DRAW THE BACKGROUND LAYER
         drawMapLayer(xShift, yShift, startX, startY, getLayerIds("background"));
 
         // DRAW THE BACKGROUND SPRITES
         _backgroundSpriteManager.render(gfx, xShift, yShift);
 
         // DRAW THE ENTITIES
         for(AdventureEntity ent: _entityManager.getEntities())
             if(inRenderArea(ent))
                 ent.render(gfx);
 
         // DRAW THE OBJECTS LAYER
         drawMapLayer(xShift, yShift, startX, startY, getLayerIds("objects"));
 
         // DRAW THE FOREGROUND LAYER
         drawMapLayer(xShift, yShift, startX, startY, getLayerIds("foreground"));
 
         // UN-CLIP THE GRAPHICS ELEMENT
         GFX.unClip();
 
         // LIGHTING SYSTEM
         _lightManager.render(gfx, xShift, yShift);
 
         // CLIP THE DRAWING AROUND THE RENDER DISTANCE
         GFX.clip(clipX, clipY, clipSize, clipSize, DisplayManager.getBackgroundScale());
 
         // DRAW THE ENTITIES IDENTIFIERS (NAME, HEALTHBAR, ETC)
         for(AdventureEntity ent: _entityManager.getEntities())
             if(inRenderArea(ent))
                 ent.renderIdentifiers(gfx);
 
         // DRAW THE FOREGROUND SPRITES
         _foregroundSpriteManager.render(gfx, xShift, yShift);
 
         // DRAW THE VIGNETTE
         _lightManager.renderVignette(gfx, xShift, yShift, vignetteSize);
 
         // UN-CLIP THE GRAPHICS ELEMENT
         GFX.unClip();
     }
 
     /**
      * get the int ids of the given string layers
      * @param layers string layers to lookup
      * @return the array of int containing the layers in the same order
      */
     private int[] getLayerIds(String... layers)
     {
         int[] layerIds = new int[layers.length];
 
         for(int layerNumber = 0;layerNumber < layers.length;layerNumber++)
             layerIds[layerNumber] = _tiledMap.getLayerIndex(layers[layerNumber]);
 
         return layerIds;
     }
 
     /**
      * Draws a tiled map layer on the primary graphics element
      * @param xShift the sub tile x shift to move the whole layer by
      * @param yShift the sub tile x shift to move the whole layer by
      * @param startX the x index of the tile to begin drawing at
      * @param startY the y index of the tile to begin drawing at
      * @param layerIds the layers to draw
      */
     private void drawMapLayer(float xShift, float yShift, int startX, int startY, int[] layerIds)
     {
         // NORMALIZE SHIFT TO AVOID TEARING
         xShift = GFX.filterDrawPosition(xShift, DisplayManager.getBackgroundScale());
         yShift = GFX.filterDrawPosition(yShift, DisplayManager.getBackgroundScale());
 
         Image tile;
         for(int y = startY;y <= startY+TILES_WIDE+1;y++)
         {
             for(int x = startX;x <= startX+TILES_WIDE+1;x++) // FOR EACH TIME ON THE SCREEN
             {
                 for(Integer layerId:layerIds) // FOR EACH LAYER
                 {
                     tile = _tiledMap.getTileImage(x, y, layerId); // GET THE TILE FOR THAT LAYER | v- IF IT'S NOT NULL AND IT'S IN THE RENDER DISTANCE
                     if(tile != null)
                         GFX.drawImageCentered(x*TILE_SIZE + xShift, y*TILE_SIZE + yShift, tile); // DRAW THE TILE WITH THE X AND Y SHIFT
                 }
             }
         }
     }
 
     @Override
     public void update(float delta) throws SlickException
     {
         // AIM SHIFT BASED ON MOUSE
         _aimShift.set(InputManager.getMouseXOrigin(), InputManager.getMouseYOrigin());
         _aimShift.mulLocal(0.2f/DisplayManager.getBackgroundScale()); // scale it for moving the viewport
 
         // EFFECT QUEUE
         if(_effectManager.getQueueSize() > 0)
         {
             _effectManager.update(delta);
             for(AdventureEntity entity: _entityManager.getEntities())
                 _effectManager.processEntity(entity);
             _effectManager.clearComplete();
         }
 
         _entityManager.update(delta);
         _collisionManager.update(delta);
 
         _backgroundSpriteManager.update(delta);
         _foregroundSpriteManager.update(delta);
 
         _lightManager.update(delta);
 
         // Update the physics world
         _world.step(delta, 5, 5);
     }
 
     @Override
     public void destroy() throws SlickException
     {
     }
 
     @Override
     public void resize()
     {
         TILES_WIDE = DisplayManager.getBackgroundWidth()/TILE_SIZE;
         TILES_HIGH = DisplayManager.getBackgroundHeight()/TILE_SIZE;
 
         if(_lightManager != null)
             _lightManager.resize();
     }
 
     @Override
     public void keyMenu()
     {
         try
         {
             StateLibrary.setActiveState(new SimpleFadeTransition(StateLibrary.getTempID(), StateLibrary.getActiveState(), StateLibrary.getState("options"), 0.5f));
         }
         catch (SlickException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Override
     public void keyBack()
     {
         try
         {
             StateLibrary.setActiveState(new CharacterState(this));
         } catch (SlickException e)
         {
             LRK.log("Failed to change states - code 207");
             e.printStackTrace();
         }
     }
 
     @Override
     public void mouseEvent(MouseInputType type, float x, float y)
     {
         if(type == MouseInputType.LeftPressed)
             _entityManager.getPlayer().leftMousePress(x, y);
         if(type == MouseInputType.RightPressed)
             _entityManager.getPlayer().rightMousePress(x, y);
     }
 
     public EffectManager getEffectManager()
     {
         return _effectManager;
     }
 
     public float getRenderDistance()
     {
         float distance = DEFAULT_RENDER_DISTANCE + Game.lrk.getPlayer().getLightStength();
         return distance > MAX_RENDER_DISTANCE ? MAX_RENDER_DISTANCE : distance;
     }
 
     public boolean inRenderArea(AdventureEntity entity)
     {
         return inRenderArea(entity, 0);
     }
 
     public boolean inRenderArea(AdventureEntity entity, float renderPadding)
     {
         return inRenderArea(entity.getBody().getPosition(), renderPadding);
     }
 
     public boolean inRenderArea(Vec2 position, float renderPadding)
     {
         return Math.abs(position.x- _entityManager.getCamera().getX())-renderPadding <= getRenderDistance()
                 && Math.abs(position.y- _entityManager.getCamera().getY())-renderPadding <= getRenderDistance();
     }
 
     public SpriteManager getBackgroundSpriteManager()
     {
         return _backgroundSpriteManager;
     }
 
     public SpriteManager getForegroundSpriteManager()
     {
         return _foregroundSpriteManager;
     }
 
     public static Vec2 getDrawPosition(Vec2 worldPos, float xShift, float yShift)
     {
         return new Vec2(xShift + worldPos.x * TILE_SIZE, yShift + worldPos.y * TILE_SIZE);
     }
 
     public LightManager getLightManager()
     {
         return _lightManager;
     }
 
     public EntityManager getEntityManager()
     {
         return _entityManager;
     }
 
     public TiledMap getTiledMap()
     {
         return _tiledMap;
     }
 
     public World getWorld()
     {
         return _world;
     }
 
     public CollisionManager getCollisionManager()
     {
         return _collisionManager;
     }
 }

 package com.game.rania.model;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.FrameBuffer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.Config;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.LocationController;
 import com.game.rania.model.element.Font;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.utils.DrawUtils;
 
 public class Radar extends Object
 {
 
   private static final int   bigCoeff      = 4;
 
   private Text               textCoord     = null, textPlanet = null;
   private Text               textUsers     = null;
   private Vector2            posObject     = new Vector2();
   private Vector2            scaleObject   = new Vector2();
   private Color              colorObject   = new Color();
   private Player             player        = null;
   private float              size;
   private TextureRegion      sensorRegion, objRegion;
   private LocationController locController = Controllers.locController;
 
   public Radar(Player player, float x, float y)
   {
     super(RegionID.RADAR, x, y);
     this.player = player;
     sensorRegion = RaniaGame.mView.getTextureRegion(RegionID.RADAR_SENSOR);
     objRegion = RaniaGame.mView.getTextureRegion(RegionID.RADAR_OBJECT);
     initFrameBuffer();
     touchObject = true;
   }
 
   private float   speedSensor = 150.0f;
   private float   deltaSensor = 0.0f;
   private float   alpha       = 0.0f;
   private boolean smallMode   = true;
 
   @Override
   public boolean touchUp(float x, float y)
   {
     if (!visible)
       return false;
 
     smallMode = !smallMode;
     if (smallMode)
     {
       RaniaGame.mController.addProcessor(Controllers.locController.getPlayerController());
       position.set(savePosition);
       scale.div(bigCoeff);
       width /= bigCoeff;
       height /= bigCoeff;
       projMatrix.setToOrtho2D(0, 0, width, height);
       frameBuffer = smallFrameBuffer;
       regionBuffer = smallRegionBuffer;
     }
     else
     {
       RaniaGame.mController.removeProcessor(Controllers.locController.getPlayerController());
       player.stop();
       savePosition.set(position);
       position.set(0, 0);
       scale.scl(bigCoeff);
       width *= bigCoeff;
       height *= bigCoeff;
       projMatrix.setToOrtho2D(0, 0, width, height);
       frameBuffer = bigFrameBuffer;
       regionBuffer = bigRegionBuffer;
     }
     return true;
   }
 
   @Override
   public void update(float deltaTime)
   {
     deltaSensor += deltaTime * speedSensor;
     float widthRadar = getWidth();
     if (deltaSensor > widthRadar)
       deltaSensor -= widthRadar;
     textCoord.content = String.format("%d %d", locController.getOutputX(player.position.x), locController.getOutputY(player.position.y));
     textUsers.content = String.valueOf(locController.getUsers().size());
   }
 
   private FrameBuffer smallFrameBuffer = null, bigFrameBuffer = null, frameBuffer = null;
   private TextureRegion smallRegionBuffer = null, bigRegionBuffer = null, regionBuffer = null;
   private SpriteBatch   spriteBuffer      = null;
   private ShapeRenderer shapeBuffer       = null;
   private Vector2       savePosition      = new Vector2(0, 0);
   private float         width, height;
   Matrix4               projMatrix        = new Matrix4();
 
   private void initFrameBuffer()
   {
     width = region.getRegionWidth();
     height = region.getRegionHeight();
 
     frameBuffer = smallFrameBuffer = new FrameBuffer(Format.RGBA4444, region.getRegionWidth(), region.getRegionHeight(), false);
     regionBuffer = smallRegionBuffer = new TextureRegion(smallFrameBuffer.getColorBufferTexture());
     smallRegionBuffer.flip(false, true);
 
     bigFrameBuffer = new FrameBuffer(Format.RGBA4444, region.getRegionWidth() * bigCoeff, region.getRegionHeight() * bigCoeff, false);
     bigRegionBuffer = new TextureRegion(bigFrameBuffer.getColorBufferTexture());
     bigRegionBuffer.flip(false, true);
 
     spriteBuffer = new SpriteBatch();
     shapeBuffer = new ShapeRenderer();
     projMatrix.setToOrtho2D(0, 0, width, height);
 
     textPlanet = new Text("", Font.getFont("data/fonts/Postmodern One.ttf", 20), color, 0, 0);
     textCoord = new Text("", Font.getFont("data/fonts/Postmodern One.ttf", 20), color, width * 0.5f, 20);
     textUsers = new Text("", Font.getFont("data/fonts/Postmodern One.ttf", 20), color, width * 0.5f, height - 20);
   }
 
   @Override
   public boolean draw(SpriteBatch sprite, ShapeRenderer shape)
   {
     if (!visible || player == null || region == null)
       return false;
 
     sprite.end();
 
     frameBuffer.begin();
     spriteBuffer.setProjectionMatrix(projMatrix);
     shapeBuffer.setProjectionMatrix(projMatrix);
 
     Gdx.gl.glClearColor(0, 0, 0, 0);
     Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
     if (smallMode)
     {
       size = player.radar.item.radius;
       spriteBuffer.begin();
       spriteBuffer.setColor(color);
       drawRegion(spriteBuffer, region, width * 0.5f, height * 0.5f, angle, scale.x, scale.y);
       spriteBuffer.end();
 
       if (locController.getStar() != null)
       {
         posObject.set(locController.getStar().position);
         posObject.sub(player.position);
         posObject.scl(width / size, height / size);
         posObject.add(width * 0.5f, height * 0.5f);
 
         // orbits
         shapeBuffer.begin(ShapeType.Line);
         shapeBuffer.setColor(1, 1, 1, 0.75f);
         for (Planet planet : locController.getPlanets())
         {
           DrawUtils.drawDottedCircle(shapeBuffer, posObject.x, posObject.y, planet.orbit * width / size, 4.0f);
         }
         shapeBuffer.end();
       }
 
       spriteBuffer.begin();
       if (objRegion != null)
       {
         if (locController.getStar() != null)
         {
           colorObject.set(1, 1, 1, 1);
           drawRadarObject(locController.getStar(), 0, size);
         }
 
         for (Planet planet : locController.getPlanets())
         {
           colorObject.set(planet.domain.color);
           drawRadarObject(planet, 0, size);
         }
 
         for (User user : locController.getUsers())
         {
           colorObject.set(user.domain.color);
           drawRadarObject(user, 0.035f, size);
         }
       }
 
       if (Config.radarNoiseOn && sensorRegion != null)
       {
         spriteBuffer.setColor(color);
         drawRegion(spriteBuffer, sensorRegion, deltaSensor, height * 0.5f, angle, 1, height * 0.98f / sensorRegion.getRegionHeight());
       }
 
       textCoord.draw(spriteBuffer, shape);
       textUsers.draw(spriteBuffer, shape);
       spriteBuffer.end();
     }
     else
     {
       size = player.radar.item.big_radius;
       spriteBuffer.begin();
       spriteBuffer.setColor(color);
       drawRegion(spriteBuffer, region, width * 0.5f, height * 0.5f, angle, scale.x, scale.y);
 
       if (objRegion != null)
       {
         colorObject.set(1, 1, 1, 1);
         for (Location location : locController.getLocations())
         {
           drawRadarObject(location.x, location.y, 0, 0.04f, 0.04f, size);
 
           posObject.set(location.x, location.y);
           posObject.sub(player.position);
           posObject.scl(width / size, height / size);
           posObject.add(width * 0.5f, height * 0.5f);
           textPlanet.content = location.starName;
           textPlanet.draw(spriteBuffer, posObject.x, posObject.y + 25);
           textPlanet.content = "(" + locController.getOutputX(location.x) + ", " + locController.getOutputY(location.y) + ")";
           textPlanet.draw(spriteBuffer, posObject.x, posObject.y - 25);
         }
       }
       spriteBuffer.end();
     }
     frameBuffer.end();
 
     sprite.begin();
    colorObject.set(1, 1, 1, 1);
     drawRegion(sprite, regionBuffer, position.x, position.y, 0, 1, 1);
 
     return true;
   }
 
   protected void drawRadarObject(Object object, float fixedSize, float gridSize)
   {
     if (fixedSize == 0)
     {
       drawRadarObject(object.position.x,
                       object.position.y,
                       object.angle,
                       object.region.getRegionWidth() * object.scale.x / gridSize,
                       object.region.getRegionHeight() * object.scale.y / gridSize,
                       gridSize);
     }
     else
     {
       drawRadarObject(object.position.x,
                       object.position.y,
                       object.angle,
                       fixedSize,
                       fixedSize,
                       gridSize);
     }
   }
 
   protected void drawRadarObject(float posX, float posY, float angle, float w, float h, float gridSize)
   {
     posObject.set(posX, posY);
     posObject.sub(player.position);
     posObject.scl(width / gridSize, height / gridSize);
     posObject.add(width * 0.5f, height * 0.5f);
 
     scaleObject.set(w, h);
     scaleObject.scl(width / objRegion.getRegionWidth(),
                     height / objRegion.getRegionHeight());
 
     if (Config.radarNoiseOn)
     {
       alpha = (deltaSensor - posObject.x) / width;
       if (alpha < 0)
         alpha += 1.0f;
       colorObject.a = 1.0f - 0.5f * alpha;
     }
 
     spriteBuffer.setColor(colorObject);
     drawRegion(spriteBuffer, objRegion, posObject, angle, scaleObject);
   }
 }

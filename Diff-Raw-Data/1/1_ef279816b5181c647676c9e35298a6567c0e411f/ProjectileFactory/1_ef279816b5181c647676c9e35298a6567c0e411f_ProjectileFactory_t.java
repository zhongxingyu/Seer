 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package game.factories;
 
 import game.CacheTool;
 import game.components.graphics.AnimatedSheet;
 import game.components.graphics.TexturedQuad;
 import game.components.graphics.animations.Continuous;
 import game.components.interfaces.IRenderComponent;
 import game.components.misc.EffectsOnDeath;
 import game.components.misc.Life;
 import game.components.misc.OutOfBounds;
 import game.components.misc.ProjectileDamage;
 import game.components.misc.RangeLimiter;
 import game.components.physics.Gravity;
 import game.components.physics.Movement;
 import game.components.physics.MovingProjectileCollision;
 import game.components.physics.StaticCollision;
 import game.entities.Entity;
 import game.entities.IEntity;
 import game.triggers.IEffect;
 import game.triggers.effects.AOEDamageEffect;
 import game.triggers.effects.RemoveEntityEffect;
 import game.triggers.effects.spawn.SpawnAnimationEffect;
 import game.types.Orientation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import loader.data.json.types.ProjectileData;
 import loader.parser.ParserException;
 import main.Locator;
 import math.ExMath;
 import math.Rectangle;
 
 import org.newdawn.slick.Image;
 
 import util.SpriteSheet;
 
 public class ProjectileFactory {
   private final Rectangle rect;
   private final Orientation orientation;
   private final int launchAngle, spread;
   private final ProjectileData data;
   private final Image img;
   private final SpriteSheet sprite, explosion;
 
   /**
    * Construct a new projectile factory for a given projectile.
    * @param bounds boundary rectangle, the projectiles should die when leaving
    *               this area
    * @param data the projectile data
    * @throws IOException when fetching images from the drive fails
    * @throws ParserException when fetching images from the drive fails
    */
   public ProjectileFactory(Rectangle bounds, int launchAngle, int spread,
       Orientation orientation, ProjectileData data)
       throws IOException, ParserException {
     assert data != null;
 
     this.rect = bounds;
     this.launchAngle = launchAngle;
     this.spread = spread;
     this.orientation = orientation;
     this.data = data;
 
     if (data.texture != null) {
       img = CacheTool.getImage(Locator.getCache(), data.texture);
       sprite = null;
     } else if (data.sprite != null) {
       img = null;
       sprite = CacheTool.getSpriteSheet(Locator.getCache(), data.sprite);
     } else {
       img = null;
       sprite = null;
     }
 
     explosion = (data.aoe == null)
       ? null
       : CacheTool.getSpriteSheet(Locator.getCache(), data.aoe.explosionSprite);
   }
 
   /**
    * Make a new projectile.
    * @param source the source of the projectile (who fired it), can be null
    * @param x the x coordinate of where it starts
    * @param y the y coordinate of where it starts
    * @return a new projectile
    */
   public Entity makeProjectile(IEntity source, float x, float y) {
     // FIXME: This method is horribly messy
 
     Entity p = new Entity(x, y, data.hitbox.width, data.hitbox.height);
 
     p.addLogicComponent(new Life(p, data.targets));
     p.addLogicComponent(new RangeLimiter(p, data.duration, data.range));
 
     p.addLogicComponent(new ProjectileDamage(p, source, data.damage));
     p.addLogicComponent(new OutOfBounds(p, rect));
 
     int angle = getRotation();
 
     // If these conditions are met, the projectile is moving
     if (data.speed != 0 || data.gravity) {
       Movement mov = getMovement(p, angle);
       p.addLogicComponent(mov);
 
       if (data.gravity)
         p.addLogicComponent(new Gravity(mov));
 
       if (data.collides)
         p.addLogicComponent(new MovingProjectileCollision(p, mov));
     } else {
       if (data.collides) {
         p.addLogicComponent(new StaticCollision(p));
       }
     }
 
     IRenderComponent render = getRender(angle);
     if (render != null) {
       p.addRenderComponent(render);
     }
 
     ArrayList<IEffect> effectsOnDeath = new ArrayList<>();
     effectsOnDeath.add(new RemoveEntityEffect(p));
 
     if (data.aoe != null) {
       setupExplosion(source, p, effectsOnDeath);
     }
 
     p.addLogicComponent(new EffectsOnDeath(p, effectsOnDeath));
 
     return p;
   }
 
   private Movement getMovement(Entity p, int angle) {
     float rad = ExMath.degToRad(angle);
 
     float dx = (float) Math.cos(rad) * data.speed;
     float dy = (float) Math.sin(rad) * data.speed;
 
     if (orientation == Orientation.LEFT) {
       dx = -dx;
     }
 
     return new Movement(p, dx, dy);
   }
 
   private int getRotation() {
     if (spread > 0) {
       return launchAngle +
           Locator.getRandom().nextInt(-spread, spread) - spread / 2;
     } else {
       return launchAngle;
     }
   }
 
   private void setupExplosion(IEntity source, Entity p, List<IEffect> effectsOnDeath) {
     AnimatedSheet explosionAnim = new AnimatedSheet(
         data.aoe.explosionSprite.framerate,
         data.aoe.explosionSprite.offset.x, data.aoe.explosionSprite.offset.y,
         Orientation.RIGHT, 0, explosion);
 
     effectsOnDeath.add(new AOEDamageEffect(
       source, p.body, data.aoe.radius, data.aoe.damage));
     effectsOnDeath.add(new SpawnAnimationEffect(p, explosionAnim, null));
   }
 
   private IRenderComponent getRender(int angle) {
     if (data.texture != null) {
       return new TexturedQuad(img, orientation, angle);
     } else if (data.sprite != null) {
       AnimatedSheet sheet = new AnimatedSheet(data.sprite.framerate,
         data.sprite.offset.x, data.sprite.offset.y,
         orientation, angle, sprite);
       sheet.setAnimator(new Continuous(sheet.getTileCount()));
 
       return sheet;
     } else {
       return null;
     }
   }
 }

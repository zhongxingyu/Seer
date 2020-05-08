 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.entities;
 
 import game.CacheTool;
 import game.actions.AOEDamage;
import game.actions.RemoveEntity;
 import game.actions.SpawnDeathAnim;
 import game.components.basic.Life;
 import game.components.graphics.DummyAnimation;
 import game.components.graphics.RSheet;
 import game.components.graphics.TexturedQuad;
 import game.components.graphics.animations.Idle;
 import game.components.interfaces.ICompAnim;
 import game.components.misc.ActionOnDeath;
 import game.components.misc.Damage;
 import game.components.misc.RangeLimiter;
 import game.components.physics.Gravity;
 import game.components.physics.Movement;
 import game.components.physics.ProjectileCollision;
 import game.entities.groups.EntityType;
 
 import java.io.IOException;
 
 import loader.data.json.ProjectilesData.ProjectileData;
 import loader.parser.ParserException;
 import main.Locator;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 
 public class ProjectileTemplate {
   private final ProjectileData data;
   private final Image          img;
   private final SpriteSheet    explosion;
 
   public ProjectileTemplate(final ProjectileData data)
     throws IOException, ParserException {
     this.data = data;
 
     if (data.texture != null) {
       img = CacheTool.getImage(Locator.getCache(), data.texture);
     } else if (data.sprite != null) {
       img = CacheTool.getSpriteSheet(Locator.getCache(), data.sprite);
     } else {
       img = null;
     }
 
     if (data.aoe != null) {
       explosion = CacheTool.getSpriteSheet(Locator.getCache(), data.aoe.explosionSprite);
     } else {
       explosion = null;
     }
   }
 
   public IEntity makeProjectile(final float x, final float y, final float rot) {
     ICompAnim anim = null;
     if (img == null) {
       anim = new DummyAnimation();
     } else if (data.texture != null) {
       anim = new TexturedQuad(img);
     } else if (data.sprite != null) {
       anim = new RSheet(data.sprite.framerate,
                         data.sprite.offset.x,
                         data.sprite.offset.y,
                         (SpriteSheet) img,
                         new Idle());
     }
 
     final Entity e = new Entity(x, y, data.hitbox.width, data.hitbox.height,
                                 EntityType.PROJECTILE);
 
     e.addLogicComponent(new Movement((float) Math.cos(rot) * data.speed,
                                      (float) Math.sin(rot) * data.speed));
     e.addLogicComponent(new RangeLimiter(data.duration, data.range));
     e.addLogicComponent(new Life(data.targets));
     e.addLogicComponent(new Damage(data.damage));
     e.addLogicComponent(new ProjectileCollision());
    e.addLogicComponent(new ActionOnDeath(new RemoveEntity(e)));
 
     if (data.gravity) {
       e.addLogicComponent(new Gravity());
     }
 
     e.addRenderComponent(anim);
 
     if (data.aoe != null) {
       final RSheet explosionAnim = new RSheet(data.aoe.explosionSprite.framerate,
                                               data.aoe.explosionSprite.offset.x,
                                               data.aoe.explosionSprite.offset.y,
                                               explosion, new Idle());
 
       e.addLogicComponent(new ActionOnDeath(
           new AOEDamage(e.getBody(), data.aoe.radius, data.aoe.damage)));
       e.addLogicComponent(new ActionOnDeath(
           new SpawnDeathAnim(e.getBody(),
                              explosionAnim.getTileWidth(),
                              explosionAnim.getTileHeight(),
                              explosionAnim)));
     }
 
     return e;
   }
 }

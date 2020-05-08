 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.factories;
 
 import game.CacheTool;
 import game.components.graphics.DummyAnimation;
 import game.components.graphics.RSheet;
 import game.components.graphics.TexturedQuad;
 import game.components.graphics.animations.Continuous;
 import game.components.interfaces.IAnimatedComponent;
 import game.components.misc.EffectsOnDeath;
 import game.components.misc.Life;
 import game.components.misc.ProjectileDamage;
 import game.components.misc.RangeLimiter;
 import game.components.physics.Gravity;
 import game.components.physics.Movement;
 import game.components.physics.MovingProjectileCollision;
 import game.components.physics.StaticCollision;
 import game.entities.Entity;
 import game.entities.EntityType;
 import game.entities.IEntity;
 import game.triggers.effects.AOEDamage;
 import game.triggers.effects.RemoveEntity;
 import game.triggers.effects.SpawnAnimationEffect;
 
 import java.io.IOException;
 
 import loader.data.json.ProjectilesData.ProjectileData;
 import loader.parser.ParserException;
 import main.Locator;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 
 public class ProjectileFactory {
   private final ProjectileData data;
   private final Image img;
   private final SpriteSheet sprite, explosion;
 
   public ProjectileFactory(ProjectileData data)
       throws IOException, ParserException {
     assert data != null;
 
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
 
     if (data.aoe != null) {
       explosion = CacheTool.getSpriteSheet(Locator.getCache(),
                                            data.aoe.explosionSprite);
     } else {
       explosion = null;
     }
   }
 
   public IEntity makeProjectile(IEntity source, float x, float y, float rot) {
     IAnimatedComponent anim = makeAnimation();
 
     Entity e = new Entity(x, y, data.hitbox.width, data.hitbox.height,
                           EntityType.PROJECTILE);
 
     Life life = new Life(e, data.targets);
 
     RangeLimiter range = new RangeLimiter(e, data.duration, data.range);
 
     e.addLogicComponent(life);
     e.addLogicComponent(range);
 
    // If these conditions are met, the
     if (data.speed != 0 || data.gravity) {
       Movement mov = new Movement(e, (float) Math.cos(rot) * data.speed,
                                      (float) Math.sin(rot) * data.speed);
       e.addLogicComponent(mov);
      e.addLogicComponent(new Gravity(mov));
 
       if (data.collides) {
        e.addLogicComponent(new ProjectileDamage(e, source, data.damage));
         e.addLogicComponent(new MovingProjectileCollision(e, mov));
       }
     } else {
       if (data.collides) {
        e.addLogicComponent(new ProjectileDamage(e, source, data.damage));
         e.addLogicComponent(new StaticCollision(e));
       }
     }
 
     EffectsOnDeath effects = new EffectsOnDeath(e);
     effects.add(new RemoveEntity(e));
     e.addLogicComponent(effects);
 
     e.addRenderComponent(anim);
 
     if (data.aoe != null) {
       RSheet explosionAnim = new RSheet(data.aoe.explosionSprite.framerate,
                                         data.aoe.explosionSprite.offset.x,
                                         data.aoe.explosionSprite.offset.y,
                                         explosion);
 
       effects.add(new AOEDamage(source, e.getBody(), data.aoe.radius, data.aoe.damage));
       effects.add(new SpawnAnimationEffect(e, explosionAnim, null));
     }
 
     return e;
   }
 
   private IAnimatedComponent makeAnimation() {
     if (data.texture != null) {
       return new TexturedQuad(img);
     } else if (data.sprite != null) {
       RSheet sheet = new RSheet(
         data.sprite.framerate,
         data.sprite.offset.x,
         data.sprite.offset.y,
         sprite
       );
       sheet.setAnimator(new Continuous(sheet.getTileCount()));
       return sheet;
     } else {
       return new DummyAnimation();
     }
   }
 }

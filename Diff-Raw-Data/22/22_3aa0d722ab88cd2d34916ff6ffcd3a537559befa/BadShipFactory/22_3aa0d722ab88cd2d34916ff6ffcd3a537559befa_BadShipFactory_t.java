 package audioProject.entities;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import audioProject.AudioProject;
 
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entity.sprite.AbstractSprite.AbstractSpriteAdapter;
 import toritools.math.Vector2;
 import toritools.pathing.interpolator.HermiteKeyFrameInterpolator;
 import toritools.render.ColorUtils;
 import toritools.scripting.EntityScript.EntityScriptAdapter;
 
 public class BadShipFactory {
 
 	public static Entity makePathedEnemy(final HermiteKeyFrameInterpolator path, final float aggressionRatio) {
 
 		final Entity entity = new Entity();
 		entity.setDim(new Vector2(40, 40));
 		entity.setType("enemy");
 
 		entity.addScript(new EntityScriptAdapter() {
 
 			float allTime;
 
 			@Override
 			public void onSpawn(Entity self, Level level) {
 				allTime = 0;
 				self.setPos(path.getPositionDeltaAtTime(0));
 				self.getVariableCase().setVar("health", "100");
 			}
 
 			@Override
 			public void onUpdate(Entity self, float time, Level level) {
 				allTime += time * Math.abs(AudioProject.controller.getFeel());
 				if (allTime > path.getEndTime()) {
 					level.despawnEntity(self);
 				} else if (self.getVariableCase().getFloat("health") <= 0) {
 					level.despawnEntity(self);
 					level.spawnEntity(new Explosion(self.getPos(), Color.RED, self.getDim().x, 30));
 				} else {
 					self.setPos(path.getPositionDeltaAtTime(allTime));
					if (AudioProject.controller.isBeat()) {
 						float scalar = .1f + Math.abs(AudioProject.controller.getFeel()) * .1f;
 						level.spawnEntity(new BadBullet(self.getPos(), new Vector2(-1, 0).scale(scalar)));
 						level.spawnEntity(new BadBullet(self.getPos(), new Vector2(-1, .7f).scale(scalar)));
 						level.spawnEntity(new BadBullet(self.getPos(), new Vector2(-1, -.7f).scale(scalar)));
 					}
 				}
 			}
 		});
 		
 		final Color color = ColorUtils.blend(new Color(255, 0, 128), Color.RED, Math.random());
 		
 		entity.setSprite(new AbstractSpriteAdapter() {
 			@Override
 			public void draw(Graphics g, Entity self, Vector2 position, Vector2 dimension) {
 				g.setColor(color);
 				g.fillOval((int) position.x, (int) position.y, (int) dimension.x, (int) dimension.y);
 			}
 		});
 
 		return entity;
 	}
 
 }

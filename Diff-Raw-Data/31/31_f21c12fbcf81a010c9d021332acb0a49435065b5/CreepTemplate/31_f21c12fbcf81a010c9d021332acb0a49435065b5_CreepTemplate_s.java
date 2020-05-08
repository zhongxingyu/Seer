 package com.gemserk.games.newtod.templates;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.World;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gemserk.commons.artemis.components.PhysicsComponent;
 import com.gemserk.commons.artemis.components.ScriptComponent;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
 import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
 import com.gemserk.commons.gdx.GlobalTime;
 import com.gemserk.commons.gdx.box2d.BodyBuilder;
 import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
 import com.gemserk.games.newtod.Collisions;
 import com.gemserk.games.newtod.Tags;
 import com.gemserk.games.newtod.path.Path;
 import com.gemserk.games.newtod.path.Path.PathTraversal;
 import com.gemserk.games.newtod.systems.components.CreepDataComponent;
 
 public class CreepTemplate extends EntityTemplateImpl{
 
 	ComponentMapper<CreepDataComponent> creepDataMapper;
 	ComponentMapper<SpatialComponent> spatialMapper;
 	
 	BodyBuilder bodyBuilder;
 	World world;
 
 
 	@Override
 	public void apply(Entity entity) {
 		Path path = parameters.get("path");
 		final Vector2 pathEnd = path.getEndPosition();
 		final Float speed = parameters.get("speed");
 		
 		Float startDistanceInPath = parameters.get("startDistanceInPath",0f);
 		Integer hitpoints = parameters.get("hitpoints");
 		
 		PathTraversal pathTraversal = path.getTraversal();
 		
 		pathTraversal.advance(startDistanceInPath);
 		
 		CreepDataComponent creepData = new CreepDataComponent(speed, pathTraversal,hitpoints);
 		
 		entity.addComponent(creepData);
 		
 		ScriptComponent scriptComponent = new ScriptComponent(new ScriptJavaImpl() {
 			@Override
 			public void update(World world, Entity e) {
 				CreepDataComponent creepDataComponent  = creepDataMapper.get(e);
 				PathTraversal pathTraversal = creepDataComponent.pathTraversal;
 				pathTraversal.advance(speed * GlobalTime.getDelta());
 
 				Vector2 position = pathTraversal.getPosition();
 				if (position.dst(pathEnd) < 0.001f) {
 					pathTraversal.reset();
 				}
 				
 				SpatialComponent spatialComponent = spatialMapper.get(e);
 				spatialComponent.setPosition(pathTraversal.getPosition());
 			}
 		});
 		
 		
 		
 		entity.addComponent(scriptComponent);
 		
 		Body body = bodyBuilder.fixture(//
				bodyBuilder.fixtureDefBuilder().circleShape(0.05f)).type(BodyType.StaticBody).userData(entity).build();
 		
 		entity.addComponent(new PhysicsComponent(body));
 		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 0.10f, 0.10f)));
 		world.getGroupManager().set(Tags.CREEPS, entity);
 	}
 	
 	/**
 	 * speed
 	 * pathtraversal
 	 * hitpoints
 	 */
 }

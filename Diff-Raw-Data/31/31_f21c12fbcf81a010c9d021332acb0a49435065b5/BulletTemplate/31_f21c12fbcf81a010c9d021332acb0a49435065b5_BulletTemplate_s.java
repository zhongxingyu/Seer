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
 import com.gemserk.commons.gdx.box2d.BodyBuilder;
 import com.gemserk.commons.gdx.box2d.Contacts;
 import com.gemserk.commons.gdx.box2d.Contacts.Contact;
 import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
 import com.gemserk.games.newtod.Collisions;
 import com.gemserk.games.newtod.systems.components.CreepDataComponent;
 
 public class BulletTemplate extends EntityTemplateImpl {
 
 	ComponentMapper<CreepDataComponent> creepDataMapper;
 	ComponentMapper<PhysicsComponent> physicsMapper;
 	ComponentMapper<SpatialComponent> spatialMapper;
 
 	BodyBuilder bodyBuilder;
 	World world;
 
 	@Override
 	public void apply(Entity entity) {
 
 		Vector2 position = parameters.get("position");
 		Vector2 direction = parameters.get("direction");
 		Float speed = parameters.get("speed");
 		
 		ScriptComponent scriptComponent = new ScriptComponent(new ScriptJavaImpl() {
 			@Override
 			public void update(World world, Entity e) {
 				PhysicsComponent physicsComponent = physicsMapper.get(e);
 				Contacts contacts = physicsComponent.getContact();
 				for (int i = 0; i < contacts.getContactCount(); i++) {
 					Contact contact = contacts.getContact(i);
 					Entity creep = (Entity) contact.getOtherFixture().getBody().getUserData();
					CreepDataComponent creepData = creepDataMapper.get(e);
 					if(creepData==null)
 						continue;
 					
 					creepData.hitpoints-=1;
 					System.out.println("Hit detected");
 					if(creepData.hitpoints==0){
 						creep.delete();
 					}
 					e.delete();
 					break;
 				}
 			}
 		});
 		
 		
 		
 		entity.addComponent(scriptComponent);
 		
 		Body body = bodyBuilder.fixture(//
				bodyBuilder.fixtureDefBuilder().circleShape(0.02f)).type(BodyType.DynamicBody).bullet().userData(entity).build();
 		
 		body.setTransform(position.x, position.y, 0);
 		body.setLinearVelocity(direction.x * speed, direction.y * speed);
 		
 		entity.addComponent(new PhysicsComponent(body));
 		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 0.04f, 0.04f)));
 		
 		
 		
 	}
 }

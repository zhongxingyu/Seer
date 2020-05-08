 package com.gemserk.games.archervsworld.artemis.systems;
 
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.math.Vector2;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.gdx.input.LibgdxPointer;
 import com.gemserk.componentsengine.properties.AbstractProperty;
 import com.gemserk.componentsengine.utils.AngleUtils;
 import com.gemserk.games.archervsworld.artemis.components.BowComponent;
 import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
 import com.gemserk.games.archervsworld.artemis.entities.Groups;
 import com.gemserk.resources.Resource;
 import com.gemserk.resources.ResourceManager;
 
 public class UpdateBowSystem extends EntitySystem {
 	
 	private LibgdxPointer pointer;
 
 	private ArcherVsWorldEntityFactory entityFactory;
 	
 	ResourceManager<String> resourceManager;
 	
 	public void setResourceManager(ResourceManager<String> resourceManager) {
 		this.resourceManager = resourceManager;
 	}
 	
 	public UpdateBowSystem(LibgdxPointer pointer, ArcherVsWorldEntityFactory entityFactory) {
 		super(BowComponent.class);
 		this.entityFactory = entityFactory;
 		this.pointer = pointer;
 	}
 	
 	@Override
 	protected void begin() {
 		pointer.update();
 	}
 	
 	AngleUtils angleUtils = new AngleUtils();
 	
 	Vector2 direction = new Vector2();
 	
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		
 		entities = world.getGroupManager().getEntities(Groups.Bow);
 		
 		if (pointer.touched) {
 			
 			// update bow direction
 			
 			for (int i = 0; i < entities.size(); i++) {
 				Entity entity = entities.get(i);
 				final SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
 				Vector2 direction = pointer.getPressedPosition().cpy().sub(pointer.getPosition());
 				
 				final BowComponent bowComponent = entity.getComponent(BowComponent.class);
 				
 				float angle = direction.angle();
 				
 				int minFireAngle = -70;
 				int maxFireAngle = 80;
 				
 				Vector2 p0 = pointer.getPressedPosition();
 				Vector2 p1 = pointer.getPosition();
 				
 				// the power multiplier
 				float multiplier = 3f;
 				
 				Vector2 mul = p1.cpy().sub(p0).mul(-1f).mul(multiplier);
 				
 				float power = truncate(mul.len(), bowComponent.getMinPower(), bowComponent.getMaxPower());
 				
 				bowComponent.setPower(power);
 				
 				if (bowComponent.getArrow() == null) {
 				
 					// TODO: add it as a child using scene graph component so transformations will be handled automatically
 					
 					Entity arrow = entityFactory.createArrow(new AbstractProperty<Vector2>(){
 						
 						Vector2 position= new Vector2();
 						
 						Vector2 diff = new Vector2();
 						
 						@Override
 						public Vector2 get() {
 							position.set(spatialComponent.getPositionProperty().get());
 							
 							diff.set(1f,0f);
 							diff.rotate(spatialComponent.getAngle());
 							diff.mul(bowComponent.getPower() * 0.005f);
 							
 							position.sub(diff);
 							
 							return position;
 						}
 						
					}, spatialComponent.getAngleProperty());
 					bowComponent.setArrow(arrow);
 					
 				}
 				
 				if ((angleUtils.minimumDifference(angle, minFireAngle) < 0) && (angleUtils.minimumDifference(angle, maxFireAngle) > 0)) {
 					spatialComponent.setAngle(angle);
 				}
 				
 			}
 			
 		}
 		
 		if (pointer.wasReleased) {
 			
 			for (int i = 0; i < entities.size(); i++) {
 				
 				Entity entity = entities.get(i);
 				BowComponent bowComponent = entity.getComponent(BowComponent.class);
 				
 				if (bowComponent.getArrow() == null)
 					continue;
 
 				float power = bowComponent.getPower();
 				
 				Entity arrow = bowComponent.getArrow();
 				SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);
 
 				direction.set(1f,0f);
 				direction.rotate(arrowSpatialComponent.getAngle());
 				
 				entityFactory.createPhysicsArrow(arrowSpatialComponent.getPosition(), direction, power);
 				
 				world.deleteEntity(arrow);
 				bowComponent.setArrow(null);
 				
 				Resource<Sound> sound = resourceManager.get("BowSound");
 				sound.get().play(1f);
 				
 			}
 			
 		}
 		
 	}
 
 	public float truncate(float a, float min, float max) {
 		if (a < min)
 			a = min;
 		if (a > max)
 			a = max;
 		return a;
 	}
 	
 	@Override
 	public void initialize() {
 
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 }

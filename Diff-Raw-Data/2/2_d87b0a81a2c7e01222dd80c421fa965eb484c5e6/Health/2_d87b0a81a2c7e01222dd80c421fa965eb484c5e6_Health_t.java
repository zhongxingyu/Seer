 package com.punchline.javalib.entities.components.generic;
 
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.entities.components.abstracted.Stat;
 
 /**
  * A component describing an Entity's health. When health becomes 0, the Entity will be deleted.
  * @author MadcowD
  * @created Jul 23, 2013
  */
 public class Health extends Stat {
 	
 	/**
 	 * Callback for when an Entity dies.
 	 * @author Nathaniel
 	 * @created Jul 24, 2013
 	 */
 	public interface HealthEventCallback {
 		
 		/**
 		 * Called when the owner of this component runs out of health.
 		 * @param owner The owner of this component.
 		 * @param world The world that Entity owner belongs to.
 		 */
 		public void invoke(Entity owner, EntityWorld world);
 		
 	}
 	
 	private Entity owner;
 	private EntityWorld world;
 	
 	/**
 	 * Whether a health bar should be rendered above this component's owner.
 	 * The health bar will only render if the EntityWorld contains a HealthRenderSystem.
 	 */
 	public boolean render = false;
 	
 	/**
 	 * Constructs a Health component.
 	 * @param owner The Entity that owns this component.
 	 * @param world The EntityWorld that owns Entity owner.
 	 * @param max The max amount of health.
 	 */
 	public Health(Entity owner, EntityWorld world, float max) {
 		super(max);
 		
 		this.owner = owner;
 		this.world = world;
 	}
 	
 	@Override
 	public void drain(double amount) {
		if (isEmpty()) return; //Already dead, don't drain.
		
 		super.drain(amount);
 		
 		if (isEmpty()) { //Dead
 			
 			if (onDeath != null)
 				onDeath.invoke(owner, world);
 			
 			owner.delete();
 			
 			return;
 		}
 		
 		if (amount > 0) { //Damaged
 			
 			if (onDamage != null)
 				onDamage.invoke(owner, world);
 			
 		}
 	}
 
 	@Override
 	public void fill(double amount) {
 		super.fill(amount);
 		
 		if (amount > 0) { //Healed
 			
 			if (onHeal != null)
 				onHeal.invoke(owner, world);
 			
 		}
 	}
 
 	/**
 	 * Callback for when the Entity that owns this component dies.
 	 */
 	public HealthEventCallback onDeath;
 
 	/**
 	 * Callback for when the Entity that owns this component takes damage.
 	 */
 	public HealthEventCallback onDamage;
 	
 	/**
 	 * Callback for when the Entity that owns this component is healed.
 	 */
 	public HealthEventCallback onHeal;
 	
 	@Override
 	public void onAdd(ComponentManager container) { }
 
 	@Override
 	public void onRemove(ComponentManager container) { }
 	
 }

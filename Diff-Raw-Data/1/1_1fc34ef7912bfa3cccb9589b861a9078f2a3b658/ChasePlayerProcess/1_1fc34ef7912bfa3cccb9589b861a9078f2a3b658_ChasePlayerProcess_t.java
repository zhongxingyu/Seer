 package com.punchline.NinjaSpacePirate.gameplay.entities.processes;
 
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.NinjaSpacePirate.gameplay.entities.systems.PlayerControlSystem;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.entities.components.generic.Health;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.physical.Velocity;
 import com.punchline.javalib.entities.events.EventCallback;
 import com.punchline.javalib.entities.processes.ProcessState;
 import com.punchline.javalib.utils.LogManager;
 
 /**
  * A process that makes an enemy chase the player.
  * @author Natman64
  *
  */
 public class ChasePlayerProcess extends MovementProcess {
 	private class SuccessCallback implements EventCallback {
 		
 		private ChasePlayerProcess process;
 		public SuccessCallback(ChasePlayerProcess process){
 			this.process = process;
 		}
 		
 		@Override
 		public void invoke(Entity e, Object... args) {
 			process.endAll = true;
 		}
 		
 	}
 	
 	private static final float SPEED_ADVANTAGE_MODIFIER = 1.75f;
 	
 	private Entity chaser;
 	private Entity player;
 	
 	/** Flag that triggers the ending of all ChasePlayerProcesses. */
 	public boolean endAll = false;
 	
 	public ChasePlayerProcess(EntityWorld world, Entity chaser, Entity player) {
 		super(world, chaser);
 		
 		this.player = player;
 		this.chaser = chaser;
 		
 		if(chaser.getType().equals("Tile"))
 			LogManager.error("WeirdPool", "A tile was found instead of a NPC");
 		
 		endOnDeath(chaser);
 		Health playerHealth = player.getComponent(Health.class);
 		
 		playerHealth.onDeath.addCallback(this, new SuccessCallback(this));
 	}
 	
 	@Override
 	public void update(EntityWorld world, float deltaTime) {
 		if (endAll) {
 			world.getProcessManager().endAll(getClass(), ProcessState.SUCCEEDED);
 			
 			return;
 		}
 		
 		if (chaser == null || player == null) return;
 		
 		if (!chaser.hasComponent(getClass())) {
 			chaser.addComponent(this);
 		}
 		
 		Transform chaserTransform = chaser.getComponent(Transform.class);
 		Transform playerTransform = player.getComponent(Transform.class);
 		
 		if (chaserTransform == null || playerTransform == null) return;
 		
 		Vector2 position = chaserTransform.getPosition();
 		Vector2 destination = playerTransform.getPosition();
 		
 		Vector2 velocity = destination.cpy().sub(position);
 		velocity.nor();
 		velocity.scl(PlayerControlSystem.movementSpeed * SPEED_ADVANTAGE_MODIFIER); //speed relative the player's regular speed
 		
 		Velocity v = chaser.getComponent(Velocity.class);
 		v.setLinearVelocity(velocity);
 	}
 
 	@Override
 	public void onEnd(EntityWorld world, ProcessState endState) {
 		if (endState != ProcessState.SUCCEEDED && endState != ProcessState.FAILED) return;
 		
 		Velocity v = chaser.getComponent(Velocity.class);
 		
 		if (v == null) return;
 		
 		v.setLinearVelocity(new Vector2());
 		
 		//remove callbacks from the entities.
 		chaser.onDeleted.removeCallback(this);
 		player.onDeleted.removeCallback(this);
 		
 		Health chaserHealth = chaser.getComponent(Health.class);
 		Health playerHealth = player.getComponent(Health.class);
 		
 		if (chaserHealth == null || playerHealth == null) return;
 		
 		chaserHealth.onDeath.removeCallback(this);
 		playerHealth.onDeath.removeCallback(this);
 		
 		chaser.removeComponent(this);
 	}
 
 	@Override
 	public void onAdd(ComponentManager container) {
 		
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		
 	}
 
 }

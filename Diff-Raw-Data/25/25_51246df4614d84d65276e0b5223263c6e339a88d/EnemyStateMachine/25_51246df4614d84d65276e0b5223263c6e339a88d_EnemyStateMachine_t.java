 package fart.dungeoncrawler.npc.states;
 
 import java.awt.Rectangle;
 import java.util.HashMap;
 import java.util.Random;
 
 import Utils.Vector2;
 
 import fart.dungeoncrawler.GameObject;
 import fart.dungeoncrawler.IUpdateable;
 import fart.dungeoncrawler.actor.BaseEnemy;
 import fart.dungeoncrawler.actor.NewPlayer;
 import fart.dungeoncrawler.enums.AttackType;
 import fart.dungeoncrawler.enums.DynamicObjectState;
 
 public class EnemyStateMachine implements IUpdateable {
 	private HashMap<DynamicObjectState, NPCState> states;
 	private NPCState curState;
 	private BaseEnemy owner;
 	private NewPlayer player;
 	private Random random;
 	
 	/**
 	 * Updates and manages all enemyStates. All AI and logic for enemies is updated here.
 	 * @param owner The enemy this machine belongs to.
 	 * @param player The Player. 
 	 */
 	public EnemyStateMachine(BaseEnemy owner, NewPlayer player) {
 		this.owner = owner;
 		this.player = player;
 		
 		random = new Random();
 		
 		initStates();
 	}
 	
 	/**
 	 * Gets the Player.
 	 * @return The Player. 
 	 */
 	public GameObject getPlayer() {
 		return player;
 	}
 	
 	/**
 	 * Creates and initializes all states and puts them in the state-hashmap.
 	 */
 	private void initStates() {
 		states = new HashMap<DynamicObjectState, NPCState>();
 		
 		states.put(DynamicObjectState.Idle, new IdleState(this, owner));
 		states.put(DynamicObjectState.Walking, new WalkingState(this, owner));
 		states.put(DynamicObjectState.Alerted, new AlertState(this, owner));
 		states.put(DynamicObjectState.Chasing, new ChasingState(this, owner));
 		states.put(DynamicObjectState.Terminated, new TerminatedState(this, owner));
 		states.put(DynamicObjectState.Fleeing, new FleeingState(this, owner));
 		states.put(DynamicObjectState.Hit, new HitState(this, owner));
 		states.put(DynamicObjectState.Attacking, new AttackingState(this, owner));
 		
 		curState = states.get(DynamicObjectState.Idle);
 	}
 	
 	@Override
 	public void update(float elapsed) {
 		if(owner.getHealth().isDead())
 			setState(DynamicObjectState.Terminated);
 		DynamicObjectState doState = owner.getState();
 		
 		//Check if the current state needs to be switched
 		//Idle - we check if player is in range or randomly start walking
 		if(doState == DynamicObjectState.Idle) {
 			if(checkPlayerInAggroRange())
 				return;
 			
 			double d = random.nextDouble();
 			if(d < 0.0075)
 				setState(DynamicObjectState.Walking);
 		}
 		//Walking - we check if player is in range
 		if(doState == DynamicObjectState.Walking) {
 			if(checkPlayerInAggroRange())
 				return;
 		}
 		//Alerted - player is in range, so we start heading towards him. If our 
 		//health is below a certain threshold we start fleeing
 		if(doState == DynamicObjectState.Alerted) {
 			//Vector2 dirToPlayer = player.getPosition().sub(owner.getPosition());
 			//float distanceToPlayer = dirToPlayer.length();
 			
 			if(owner.getHealth().lowerThan(0.15f)) {
 				//HP is lower than 15%, we start to flee
 				setState(DynamicObjectState.Fleeing);
 				((FleeingState)curState).setThreat(player);
 			} else if(checkPlayerInAttackRange()) {
 				//TODO: Check if enemy is facing the player...
 				double d = random.nextDouble();
 				if(d < 0.02) {
 					owner.setAttackType(AttackType.melee);
 					setState(DynamicObjectState.Attacking);
 					System.out.println("Attack!");
 				}
 			}
 			else {
				if(owner.getSimpleSpell() != null)
				{
					if(!owner.getSimpleSpell().isOnCooldown()) {
						owner.setAttackType(AttackType.spell);
						owner.setAggroRange(1000);
						setState(DynamicObjectState.Attacking);
						System.out.println("Spell!");
					}
 				}
 			}
 		}
 		if(doState == DynamicObjectState.Chasing) {
 			if(owner.getHealth().lowerThan(0.15f)) {
 				//HP is lower than 15%, we start to flee
 				setState(DynamicObjectState.Fleeing);
 				((FleeingState)curState).setThreat(player);
 			} else if(checkPlayerInAttackRange()) {
 				//TODO: Check if enemy is facing the player...
 				double d = random.nextDouble();
 				if(d < 0.02) {
 					setState(DynamicObjectState.Attacking);
 					System.out.println("Attack!");
 				}
 			}
 		}
 		//Attacking
 		if(doState == DynamicObjectState.Attacking) {
 			
 		}
 		//Is Hit
 		if(doState == DynamicObjectState.Hit) {
 			
 		}
 		//Flees
 		if(doState == DynamicObjectState.Fleeing) {
 			
 		}
 		if(doState == DynamicObjectState.Terminated) {
 			return;
 		}
 		
 		curState.update(elapsed);
 	}
 	
 	/**
 	 * Sets a new state. Exit() is called for the current state, the new state is set and activate()d. 
 	 * @param state The new state to set. 
 	 */
 	public void setState(DynamicObjectState state) {
 		if(state == DynamicObjectState.Terminated) {
 			owner.getCollision().removeDynamicObject(owner);
 		}
 		
 		if(!states.containsKey(state))
 			return;
 		
 		if(curState.getDOState() == state)
 			return;
 		
 		curState.exit();
 		curState = states.get(state);
 		curState.activate();
 		
 		if(state == DynamicObjectState.Chasing)
 			((ChasingState)curState).setGoal(player);
 		
 		owner.setState(state);
 		owner.setCurrentAnimation(state);
 	}
 	
 	/**
 	 * Return the current DynamicObjectState.
 	 * @return The current state. 
 	 */
 	public DynamicObjectState getState() {
 		return curState.getDOState();
 	}
 	
 	/**
 	 * Checks if the player is in aggroRange, to change to alertstate. 
 	 * Function is not in use. Use checkPlayerInAggroRange() instead. 
 	 * @return If the player is in range.
 	 */ 
 	@Deprecated
 	public boolean isPlayerInAggroRange() {
 		Vector2 dirToPlayer = player.getPosition().sub(owner.getPosition());
 		float distanceToPlayer = dirToPlayer.length();
 		
 		return distanceToPlayer < owner.getAggroRange();
 	}
 	
 	/**
 	 * Checks if the player is in aggroRange. If so, the state is switched to alerted.
 	 * @return Is the Player is in aggroRange. 
 	 */
 	public boolean checkPlayerInAggroRange() {
 		Vector2 dirToPlayer = player.getPosition().sub(owner.getPosition());
 		float distanceToPlayer = dirToPlayer.length();
 		
 		if(distanceToPlayer < owner.getAggroRange()) {
 			setState(DynamicObjectState.Alerted);
 			((AlertState)curState).setGoal(player);
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Checks if the player is in attackRange. 
 	 * @return If the player is in range. 
 	 */
 	public boolean checkPlayerInAttackRange() {
 		Rectangle collisionRect = new Rectangle(owner.getCollisionRect());
 		int range = owner.getAttackRange();
 		
 		collisionRect.x -= range;
 		collisionRect.y -= range;
 		collisionRect.width += (range * 2);
 		collisionRect.height += (range * 2);
 		
 		return player.getCollisionRect().intersects(collisionRect);
 	}
 }

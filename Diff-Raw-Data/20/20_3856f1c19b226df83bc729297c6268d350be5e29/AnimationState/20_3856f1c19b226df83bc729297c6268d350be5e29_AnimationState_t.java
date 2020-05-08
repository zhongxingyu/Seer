 /**
  * 
  */
 package anim;
 
 import java.util.EnumSet;
 
 import org.newdawn.slick.Animation;
 
 import util.Direction;
 import config.Config;
 import entities.Entity;
 import entities.Player;
 
 /**
  * An enumeration of all of the animations in the game, as well as their transitions
  * @author Mullings
  *
  */
 public enum AnimationState {
 	STATIC {
 		
 	},
 	
 	BASIC { 
 		
 	}, 
 	
 	// passive animations
 	IDLE {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			if (!entity.isTouching(Direction.DOWN)) {
 				if (entity.getPhysicsBody().getLinearVelocity().y >= Config.VEL_EPSILON) {
 					return AnimationState.FALL;
 				} else {
 					return AnimationState.RISE;
 				}
 			} 
 			
 			if (!entity.isStill()) {
 				return AnimationState.RUN;
 			} 
 			return this;
 		}
 	}, 
 	RUN {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			if (!entity.isTouching(Direction.DOWN)) {
 				if (entity.getPhysicsBody().getLinearVelocity().y >= Config.VEL_EPSILON) {
 					return AnimationState.FALL;
 				} else {
 					return AnimationState.RISE;
 				}
 			} 
 			if (entity.isStill()) {
 				return AnimationState.IDLE;
 			} 
 			
 			return this;
 		}
 	}, 
 
 	RISE {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			if (entity.getPhysicsBody().getLinearVelocity().y >= Config.VEL_EPSILON) {
 				return AnimationState.SOMERSAULT;
			}
			if (entity.isTouching(Direction.DOWN)){
				return AnimationState.IDLE;
			}
 			return this;
 		}
 
 	}, 
 	FALL {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			if (entity.isTouching(Direction.DOWN)) {
 				return AnimationState.IDLE;
 			}
 			if (entity.getPhysicsBody().getLinearVelocity().y < -Config.VEL_EPSILON) {
 				return AnimationState.RISE;
 			}
 			return this;
 		}
 
 	}, 
 	
 	// player specific
 	
 	JUMP(true) {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			return AnimationState.RISE;
 		}
 	},
 	
 	HOOKING {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			if (!((Player) entity).getHookshot().isShooting()) {
 				if (!entity.isTouching(Direction.DOWN)) {
 					if (entity.getPhysicsBody().getLinearVelocity().y < Config.VEL_EPSILON) {
 						return AnimationState.RISE;
 					} else {
 						return AnimationState.FALL;
 					}
 				} else {
 					return AnimationState.IDLE;
 				}
 			}
 			return this;
 		}
 
 	}, SOMERSAULT(true) {
 		@Override
 		public AnimationState getNextState(Entity entity) {
 			return AnimationState.FALL;
 		}
 	};
 	
 	
 	private EnumSet<AnimationState> prohibited;
 	private boolean isTransition = false;
 	
 	private AnimationState() {
 	}
 	
 	private AnimationState(boolean isTransition) {
 		this.isTransition = isTransition;
 	}
 	
 	public AnimationState getNextState(Entity entity) {
 		return this;
 	}
 	
 	// method for transitions
 	public AnimationState getNextState(Entity entity, Animation animation) {
 		if (isTransition() && animation.isStopped()) {
 			return getNextState(entity);
 		}
 		return this;
 	}
 	
 	public boolean isTransition() {
 		return isTransition;
 	}
 	
 	private void prohibitTransitions(AnimationState... prohibitedTransitions) {
 		prohibited = EnumSet.noneOf(AnimationState.class);
 		for (AnimationState t : prohibitedTransitions) {
 			prohibited.add(t);
 		}
 	}
 	
 	
 	public boolean canChangeTo(AnimationState state) {
 		return !prohibited.contains(state);
 	}
 	
 	static {
 		STATIC.prohibitTransitions();
 		BASIC.prohibitTransitions();
 		IDLE.prohibitTransitions();
 		RUN.prohibitTransitions();
 		JUMP.prohibitTransitions();
 		RISE.prohibitTransitions();
 		FALL.prohibitTransitions();
 		HOOKING.prohibitTransitions();
 		SOMERSAULT.prohibitTransitions();
 	}
 }

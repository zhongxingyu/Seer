 /**
  * 
  */
 package anim;
 
 import java.util.EnumMap;
 
 import org.newdawn.slick.Animation;
 
 import util.Direction;
 import entities.Entity;
 
 
 /**
  * A state machine for handling animation transitions
  * @author Mullings
  *
  */
 public class AnimStateMachine {
 	private EnumMap<AnimationState, Animation> animMap;
 	protected AnimationState currentState;

 	public AnimStateMachine() {
 		animMap = new EnumMap<AnimationState, Animation>(AnimationState.class);
 	}
 	
 	public void addAnimation(AnimationState type, Animation animation) {
 		animMap.put(type, animation);
 	}
 	
 	public void update(Entity entity) {
		if (currentState == null) return;
 		Animation animation = animMap.get(currentState);
 		if (currentState.isTransition()) {
 
 			if (animation.isStopped()) {
 				animation.restart();
 				play(currentState.getNextState(entity));
 			} else {
 				play(currentState);
 			}
 		} else {
 
 			play(currentState.getNextState(entity));
 		}
 	}
 	
 	
 	public boolean play(AnimationState state) {
 		if (currentState == null || currentState.canChangeTo(state)) {
 			currentState = state;
 			return true;
 		}
 		return false;
 	}
 
 	public AnimationState getCurrentState() {
 		return currentState;
 	}
 
 	public Animation getCurrentAnimation() {
 		if (currentState != null) {
 			return animMap.get(currentState);
 		}
 		return null;
 	}
 	
 	public void setFrames(AnimationState as, int numFrames, int durPerFrame) {
 		for (int i = 0; i < numFrames; i++) {
 			Animation a = animMap.get(as);
 			a.setDuration(i, durPerFrame);
 			animMap.put(as, a);
 		}
 		for (int i = numFrames; i < animMap.get(as).getFrameCount(); i++) {
 			Animation a = animMap.get(as);
 			a.setDuration(i, 0);
 			animMap.put(as, a);
 		}
 	}
 	
 }

 package edu.brown.cs32.goingrogue.game;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.graphics.Animation;
 
 /** Caches animations according to the creature and action that initiated them.
  * This ensures that an animation can be played continuously for a creature until the action that
  * animation was initiated by stops happening or is overridden
  * 
  * @author Dominic Adams
  * @author Ben Weedon
  * @author John Adler
  * @author Ken Lin
  * @version 1.0 4/13
  */
 public class AnimationCache {
 
 	
 	//Stores an animation and the action that it is attached to
 	static class AnimationData {
 		Action action;
 		List<Animation> animations;
 		int playTime;
 		
 		AnimationData(Action act, List<Animation> anim) {
 			action=act;
 			animations=anim;
 			playTime=0;
 			for(Animation a: animations) {
 				if(a.getDuration()>playTime) playTime=a.getDuration();
 			}
 		}
 		
 		public String toString() {
 			return ""+animations;
 		}
 	}
 	
	ConcurrentHashMap<Creature, AnimationData> cache;
 	
 	public AnimationCache() {
		cache=new ConcurrentHashMap<>();
 	}
 	
 	/** Adds a creature and its animation data to the cache
 	 * 
 	 * @return true if the creature was already in the cache, else false
 	 */
 	public boolean add(Creature creature, Action action, List<Animation> anim) {
 		AnimationData data=cache.put(creature, new AnimationData(action, anim));
 		
 		return data!=null;
 	}
 	
 	/** Returns the animation currently associated with the specified creature
 	 */
 	public List<Animation> get(Creature creature) {
 		if(cache.get(creature)==null) return null;
 		
 		return cache.get(creature).animations;
 	}
 	
 	/** Returns the animation currently associated with the specified creature
 	 */
 	public Action getAction(Creature creature) {
 		if(cache.get(creature)==null) return null;
 		return cache.get(creature).action;
 	}
 	
 	/** Updates the entire cache of animations by the given delta value
 	 * Removes a piece of animation data if its exceeded its play time
 	 * NOTE: If animations are updated anywhere else, this method will NOT delete them as soon as they finish
 	 */
 	public void update(int delta) {
 		
 		for(Map.Entry<Creature, AnimationData> dataPair: cache.entrySet()) {
 			AnimationData data=dataPair.getValue();
 			data.playTime-=delta;
 
 			if(data.playTime<=0) cache.remove(dataPair.getKey());
 			
 			for(Animation a: data.animations) {
 				
 				Countdown c=new Countdown(2000, data);
 				c.start();
 				a.updateWithReset(delta);
 				c.end();
 				
 				if(a.isFinished()) System.out.println("Anim in cache is finished -- should not happen");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	static class Countdown extends Thread {
 		int timeout;
 		Object o;
 		AnimationData data;
 		boolean shouldThrow;
 		
 		public Countdown(int to, AnimationData d) {
 			timeout=to;
 			o=new Object();
 			data=d;
 			shouldThrow=true;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				synchronized(o) {
 					o.wait(timeout);
 				}
 			} catch(InterruptedException e){}
 			
 			if(shouldThrow) throw new RuntimeException(""+data);
 		}
 		
 		public void end() {
 			shouldThrow=false;
 		}
 	}
 }

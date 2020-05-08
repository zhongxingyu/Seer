 package com.eagerlogic.cubee.client.properties;
 
 import com.eagerlogic.cubee.client.EventQueue;

import java.util.ArrayList;
 import java.util.LinkedList;
import java.util.List;
 
 /**
  *
  * @author dipacs
  */
 public abstract class AAnimator {
 	
	private static final List<AAnimator> animators = new ArrayList<AAnimator>();
 	private static final Runnable ANIMATOR_TASK = new Runnable() {
 
 		@Override
 		public void run() {
 			AAnimator.animate();
 		}
 	};
 	
 	private static void animate() {
		for (int i = animators.size() -1; i >= 0; i--) {
			if (animators.size() <= i) {
				continue;
			}
			AAnimator animator = animators.get(i);
 			try {
 				animator.onAnimate();
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 		
 		if (animators.size() > 0) {
 			EventQueue.getInstance().invokeLater(ANIMATOR_TASK);
 		}
 	}
 	
 	private boolean started;
 	
 	public void start() {
 		if (started) {
			return;
 		}
 		
 		animators.add(this);
 		if (animators.size() == 1) {
 			EventQueue.getInstance().invokeLater(ANIMATOR_TASK);
 		}
 		started = true;
 	}
 	
 	public void stop() {
 		if (!started) {
			return;
 		}
		
 		started = false;
		
 		animators.remove(this);
 	}
 
 	public final boolean isStarted() {
 		return started;
 	}
 	
 	public final boolean isStopped() {
 		return !started;
 	}
 	
 	protected abstract void onAnimate();
 
 }

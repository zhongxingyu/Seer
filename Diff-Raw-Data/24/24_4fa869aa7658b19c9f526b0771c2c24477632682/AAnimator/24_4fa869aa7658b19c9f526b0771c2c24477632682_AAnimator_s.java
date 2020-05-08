 package com.eagerlogic.cubee.client.properties;
 
 import com.eagerlogic.cubee.client.EventQueue;
 import java.util.LinkedList;
 
 /**
  *
  * @author dipacs
  */
 public abstract class AAnimator {
 	
	private static final LinkedList<AAnimator> animators = new LinkedList<AAnimator>();
 	private static final Runnable ANIMATOR_TASK = new Runnable() {
 
 		@Override
 		public void run() {
 			AAnimator.animate();
 		}
 	};
 	
 	private static void animate() {
		for (AAnimator animator : animators) {
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
			throw new IllegalStateException("This animator is already started.");
 		}
 		
 		animators.add(this);
 		if (animators.size() == 1) {
 			EventQueue.getInstance().invokeLater(ANIMATOR_TASK);
 		}
 		started = true;
 	}
 	
 	public void stop() {
 		if (!started) {
			throw new IllegalStateException("This animator is already stopped.");
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

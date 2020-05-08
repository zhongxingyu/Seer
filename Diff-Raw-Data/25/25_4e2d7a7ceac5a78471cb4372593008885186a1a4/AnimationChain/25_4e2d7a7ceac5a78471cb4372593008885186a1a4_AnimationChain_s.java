 package document_object;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class AnimationChain<T> implements Runnable {
 	
 	protected ArrayList<AnimationStep<T>> stack;
 	protected Timer timer = null;
 	protected AnimationChain<T> child = null;
 	protected final int fps = 60;
 	protected Runnable finalizer;
 	
 	public AnimationChain() {
 		this.stack = new ArrayList<AnimationStep<T>>();
 	}
 	
 	public AnimationChain<T> queue( T start, T end, int duration, Lambda<T> handler ) {
 		this.stack.add( new AnimationStep<T>( start, end, duration, handler ) );
 		return this;
 	}
 	
 	public AnimationChain<T> then( T start, T end, int duration, Lambda<T> handler ) {
 		if ( this.child == null ) {
 			this.child = new AnimationChain<T>();
 			child.queue( start, end, duration, handler );
 		} else {
 			child.then(start, end, duration, handler);
 		}
 		return this;
 	}
 	
 	public AnimationChain<T> ensure( Runnable handler ) {
		this.finalizer = handler;
 		return this;
 	}
 	
 	public void run() {
 		
 		int outstanding = 0;
 		int fps_step = Math.round( 1000 / fps );
 		if ( this.timer == null ) {
 			this.timer = new Timer();
 		}
 		
 		for ( int i = 0; i < this.stack.size(); ++i ) {
 			final AnimationStep<T> animation_step = this.stack.get( i );
 			if ( animation_step.getStart() == animation_step.getEnd() ) {
 				continue;
 			}
 			T step;
 			
 			if ( animation_step.getStart() instanceof Integer ) {
 				Integer calculated_step = (int)Math.round(
 						( (float)( (Integer)animation_step.getEnd() - (Integer)animation_step.getStart() ) 
 								/ ( (float)animation_step.getDuration()
 										* (float)fps / 1000.0 ) ) );
 				if ( calculated_step == 0 ) {
 					continue;
 				}
 				step = (T)calculated_step;
 				int current = (Integer)animation_step.getStart();
 				
 				while ( Math.abs( current - (Integer)animation_step.getEnd() ) >= Math.abs( (Integer)step ) ) {
 					current += (Integer)step;
 					final int final_current = current;
 					outstanding += fps_step;
 					this.timer.schedule( new TimerTask() {
 						public void run() {
 							( (Lambda<Integer>)animation_step.getHandler() ).run( final_current );
 						}
 					}, outstanding );
 				}
 				this.timer.schedule( new TimerTask() {
 					public void run() {
 						( (Lambda<Integer>)animation_step.getHandler() ).run( (Integer)animation_step.getEnd() );
 					}
 				}, outstanding + fps_step );
 			} else {
 				Float calculated_step = (float)(( (Float)animation_step.getEnd() - (Float)animation_step.getStart() )
 						/ ( (float)animation_step.getDuration() * (float)fps / 1000.0 ));
 				step = (T)calculated_step;
 				float current = (Float)animation_step.getStart();
 				while ( Math.abs( current - (Float)animation_step.getEnd() ) >= Math.abs( (Float)step ) ) {
 					current += (Float)step;
 					final float final_current = current;
 					outstanding += fps_step;
 					this.timer.schedule( new TimerTask() {
 						public void run() {
 							( (Lambda<Float>)animation_step.getHandler() ).run( final_current );
 						}
 					}, outstanding );
 				}
 				this.timer.schedule( new TimerTask() {
 					public void run() {
 						( (Lambda<Float>)animation_step.getHandler() ).run( (Float)animation_step.getEnd() );
 					}
 				}, outstanding + fps_step );
 			}
 		}
 		int sub_step = 2;
 		if ( this.finalizer != null ) {
 			this.timer.schedule( new TimerTask() {
 				public void run() {
 					finalizer.run();
 				}
 			}, outstanding + sub_step * fps_step );
 			++sub_step;
 		}
 		if ( this.child != null ) {
 			this.timer.schedule( new TimerTask() {
 				public void run() {
 					child.run();
 				}
 			}, outstanding + sub_step * fps_step );
 		}
 	}
 	
 	public void cancel() {
 		if ( this.timer != null )
 			this.timer.cancel();
 		this.stack.clear();
 		this.timer = new Timer();
 	}
 }

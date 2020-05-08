 package guri.screenmodel;
 
 import java.util.TimerTask;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * An Canvas which update the context in each given period.
  */
 abstract public class AnimationUnitCanvas extends UnitCanvas {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8666745955018271170L;
 
 	private final ScheduledThreadPoolExecutor executor;
 
	private ScheduledFuture repaintFuture;
 
 	/**
 	 * to store the repainting should be stopped. no effect when the repainting
 	 * is alway stopped.
 	 */
 	private State state = State.IDLE;
 
 	protected long time = -1;
 
 	private int ms;
 
 	/**
 	 * Create a Canvas intance
 	 * 
 	 * @param sprite
 	 *            the sprite to be paint
 	 * @param ms
 	 *            the time length between each display repaint in ms
 	 */
 	public AnimationUnitCanvas(Unit sprite, int ms,
 			ScheduledThreadPoolExecutor executor) {
 		super(sprite);
 		this.ms = ms;
 		this.executor = executor;
 	}
 
 	/**
 	 * create a thread to repeat repaint it.
 	 */
 	public synchronized void start() {
 		if (state == State.RUNNING)
 			return;
 		state = State.RUNNING;
 		repaintFuture = executor.scheduleAtFixedRate(new RepaintTimer(), 0, ms,
 				TimeUnit.MILLISECONDS);
 	}
 
 	/**
 	 * stop repainting.
 	 */
 	public synchronized void stop() {
 		if (state == State.IDLE)
 			return;
 		state = State.IDLE;
 		repaintFuture.cancel(false);
 	}
 
 	abstract protected void anime();
 
 	private class RepaintTimer extends TimerTask {
 
 		@Override
 		public void run() {
 			time = System.currentTimeMillis();
 			anime();
 			repaint();
 		}
 
 	}
 
 	enum State {
 		IDLE, RUNNING
 	}
 
 }

 package com.evervoid.client.graphics.geometry;
 
 import com.evervoid.client.graphics.EverNode;
 
 public abstract class AnimatedTransform extends Transform
 {
 	public static enum DurationMode
 	{
 		CONTINUOUS, FIXED, REPETITIVE;
 	}
 
 	private Runnable aCallback = null;
 	private float aDuration = 0f;
 	protected DurationMode aDurationMode = DurationMode.FIXED;
 	protected float aProgress = 0f;
 	protected Smoothing aSmoothing = Smoothing.SINE;
 	protected float aSpeed = 1f;
 	private boolean aStarted = false;
 
 	public AnimatedTransform(final EverNode node)
 	{
 		super(node);
 	}
 
 	@Override
 	public void delete()
 	{
 		stop();
 		super.delete();
 	}
 
 	protected void deregister()
 	{
 		if (!aDurationMode.equals(DurationMode.REPETITIVE)) {
 			TransformManager.unregister(this);
 		}
 	}
 
 	public AnimatedTransform done(final boolean resetProgress, final boolean runCallback)
 	{
 		final Runnable oldCallback = aCallback;
 		aCallback = null;
 		aStarted = aDurationMode.equals(DurationMode.REPETITIVE);
 		if (resetProgress || aDurationMode.equals(DurationMode.REPETITIVE)) {
 			aProgress = 0;
 			getReady();
 		}
 		if (runCallback && oldCallback != null) {
 			oldCallback.run();
 		}
 		setNotifyOnChange(!aStarted);
 		return this;
 	}
 
 	public boolean frame(final float tpf)
 	{
 		if (!aStarted) {
 			return false;
 		}
 		aProgress = Math.min(1, aProgress + tpf / aDuration);
 		final float prog = aSmoothing.smooth(aProgress);
 		step(prog, 1 - prog);
 		if (aProgress >= 1) {
 			stop();
 		}
 		return true;
 	}
 
 	public float getDuration()
 	{
 		return aDuration;
 	}
 
 	abstract protected void getReady();
 
 	public boolean isInProgress()
 	{
 		return aStarted;
 	}
 
 	protected void register()
 	{
 		TransformManager.register(this);
 	}
 
 	public abstract void reset();
 
 	protected void setBackContinuous(final double measure)
 	{
 		setBackContinuous((float) measure);
 	}
 
 	protected void setBackContinuous(final float measure)
 	{
 		if (!aDurationMode.equals(DurationMode.CONTINUOUS)) {
 			return;
 		}
 		setDuration(measure / aSpeed);
 		start(true);
 	}
 
 	public AnimatedTransform setDuration(final double d)
 	{
 		return setDuration((float) d);
 	}
 
 	public AnimatedTransform setDuration(final float duration)
 	{
 		aDuration = duration;
 		return this;
 	}
 
 	public AnimatedTransform setDurationMode(final DurationMode durationMode)
 	{
 		aDurationMode = durationMode;
 		if (aDurationMode.equals(DurationMode.CONTINUOUS)) {
 			setSmoothing(Smoothing.LINEAR);
 		}
 		return this;
 	}
 
 	public AnimatedTransform setSmoothing(final Smoothing smoothing)
 	{
 		aSmoothing = smoothing;
 		return this;
 	}
 
 	public AnimatedTransform setSpeed(final float speed)
 	{
 		aSpeed = speed;
 		return this;
 	}
 
 	public AnimatedTransform start()
 	{
 		return start(null, true, true);
 	}
 
 	public AnimatedTransform start(final boolean resetProgress)
 	{
 		return start(null, resetProgress, true);
 	}
 
 	public AnimatedTransform start(final Runnable callback)
 	{
 		return start(callback, true, true);
 	}
 
 	public AnimatedTransform start(final Runnable callback, final boolean resetProgress, final boolean runCallback)
 	{
 		done(resetProgress, runCallback);
 		aCallback = callback;
 		getReady();
 		aStarted = true;
 		register();
 		setNotifyOnChange(false);
 		return this;
 	}
 
 	abstract protected void step(float progress, float antiProgress);
 
 	public AnimatedTransform stop()
 	{
 		if (!isInProgress()) {
 			return this;
 		}
 		aStarted = false;
 		done(true, true);
 		deregister();
 		return this;
 	}
 
 	@Override
 	public String toString()
 	{
 		String active = "(off)";
 		if (aStarted) {
 			active = " @ " + (aProgress * 100) + "%";
 		}
		return "Animated" + super.toString() + active;
 	}
 }

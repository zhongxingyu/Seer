 package com.harcourtprogramming.markov;
 
 /**
  * TODO: Documentation Here
  * @param <S> 
  * @author Benedict
  */
 public final class ImmutableMarkovTransition<S> implements IMarkovTransition<S>
 {
 	private float weighting;
 	private S destinationState;
 
 	/**
 	 * TODO: Documentation Here
 	 * @return
 	 */
 	ImmutableMarkovTransition(float weighting, S destinationState)
 	{
		if (destinationState == null) throw new IllegalArgumentException("Destition State must be non-null");
		if (weighting <= 0) throw new IllegalArgumentException("Weight must be a postive value");
 		this.weighting = weighting;
 		this.destinationState = destinationState;
 	}
 
 	/**
 	 * TODO: Documentation Here
 	 * @return
 	 */
 	@Override
 	public float getWeighting()
 	{
 		return weighting;
 	}
 	
 	/**
 	 * TODO: Documentation Here
 	 * @return
 	 */
 	@Override
 	public S getDestinationState()
 	{
 		return destinationState;
 	}
 }

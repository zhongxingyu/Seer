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

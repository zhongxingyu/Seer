 package com.harcourtprogramming.markov;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @param <S> 
  * @author Benedict
  */
 public final class MarkovBuilder<S> implements IMarkovProcess<S>
 {
 	private MarkovProcess<S> proc;
 	private boolean sealed;
 
 	public MarkovBuilder()
 	{
 		sealed = false;
 		proc = new MarkovProcess<S>();
 	}
 
 	public void seal()
 	{
 		sealed = true;
 	}
 
 	public MarkovProcess<S> getProcess()
 	{
 		if (this.sealed) return proc;
 		return proc.clone();
 	}
 
 	public void addRateTransition(S from, S to, float rate)
 	{
 		throw new UnsupportedOperationException("Not yet implemented.");
 	}
 
 	public void addWeightTransition(S from, S to, float delta)
 	{
 		throw new UnsupportedOperationException("Not yet implemented.");
 	}
 
 	public void modifyTransitionWeighting(S from, S to, float delta)
 	{
 		throw new UnsupportedOperationException("Not yet implemented.");
 	}
 
 	@Override
 	public boolean transistionExists(S from, S to)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public Set<S> getStates()
 	{
 		return proc.getStates();
 	}
 
 	@Override
 	public Set<IMarkovTransition<S>> getTransitions(S fromState)
 	{
 		return proc.getTransitions(fromState);
 	}
 
 	@Override
 	public Map<S, Float> getProbabilityDensityFunction(S fromState)
 	{
 		return proc.getProbabilityDensityFunction(fromState);
 	}
 
 	@Override
 	public Map<S, Float> getCumulativeDensityFuntion(S fromState)
 	{
 		return proc.getCumulativeDensityFuntion(fromState);
 	}
 
 }

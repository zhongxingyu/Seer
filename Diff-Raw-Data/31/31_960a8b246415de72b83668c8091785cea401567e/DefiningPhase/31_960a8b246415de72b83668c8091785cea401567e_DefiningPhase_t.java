 package com.ace.moab.services;
 
 /**
  * Encapsulates all phase-transition logic that applies during the Defining phase.
  */
 public class DefiningPhase extends Phase {
 
     // This phase doesn't have any member variables, so there's no point
     // in creating a new object every time we want the phase. Just use
     // the singleton.
     static final DefiningPhase Instance = new DefiningPhase();
 
     public Phase getNextPhase(Lifecycle lifecycle, Transition transition) throws InvalidTransitionException {
 		switch (transition) {
 			case Submit:
            case Onboard:
 				return new AnalyzingPhase(transition);
 			default:
                 break;
 		}
         return invalid(lifecycle, transition);
     }
 
 	public boolean isStable() {
 		return true;
 	}
 }

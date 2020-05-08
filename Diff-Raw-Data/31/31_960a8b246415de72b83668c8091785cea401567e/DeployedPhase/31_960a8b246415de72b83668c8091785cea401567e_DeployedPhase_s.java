 package com.ace.moab.services;
 
 /**
  * Encapsulates all phase-transition logic that applies during the Deployed phase.
  */
 public class DeployedPhase extends Phase {
 
     // This phase doesn't have any member variables, so there's no point
     // in creating a new object every time we want the phase. Just use
     // the singleton.
     static final DeployedPhase Instance = new DeployedPhase();
 
     public Phase getNextPhase(Lifecycle lifecycle, Transition transition) throws InvalidTransitionException {
 		switch (transition) {
 			case Pause:
 			case DetectDamage:
 				return SuspendedPhase.Instance;
 			case Expire:
 			case Terminate:
 				return CleaningPhase.Instance;
 			case AutoMigrate:
			case ManualMigrate:
 			case Modify:
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

 package com.ace.moab.services;
 
 /**
  * Encapsulates all phase-transition logic that applies during the Analyzing phase.
  */
 public class AnalyzingPhase extends Phase {
 
     /**
      * Which transition caused us to enter the Analyzing phase? This is important because
      * it influences where we go after analyzing.
      */
     public final Transition fromTransition;
 
     public AnalyzingPhase(Transition fromTransition) {
         this.fromTransition = fromTransition;
     }
 
 	public Phase getNextPhase(Lifecycle lifecycle, Transition transition) throws InvalidTransitionException {
 		switch (transition) {
 			case Accept:
                 switch (fromTransition) {
                     case Submit:
                     case Modify:
                    case Migrate:
                     case AutoMigrate:
                    case Resume:
                         return DeployingPhase.Instance;
                     default:
                         throw new InvalidTransitionException(String.format(
                                 "Services in the %s phase cannot accept a \"%s\" transition after \"%s\".",
                                 name, transition.toString(), fromTransition.toString()));
                 }
             case Adopt:
                 if (fromTransition == Transition.Onboard) {
                     return DeployedPhase.Instance;
                 }
                 break;
 			case Reject:
                if (fromTransition == Transition.Submit || fromTransition == Transition.Onboard) {
                    return FailedPhase.Instance;
                }
                break;
            case Terminate:
                 if (fromTransition == Transition.Submit) {
 				    return FailedPhase.Instance;
                 } else {
                     return CleaningPhase.Instance;
                 }
             case RejectModify:
                 if (fromTransition == Transition.Modify) {
                     return DeployedPhase.Instance;
                 }
                 break;
             case RejectResume:
                 if (fromTransition == Transition.Resume) {
                     return SuspendedPhase.Instance;
                 }
                 break;
 			default:
                 break;
 		}
         return invalid(lifecycle, transition);
     }
 
 	@Override
 	public boolean isStable() {
 		return false;
 	}
 }

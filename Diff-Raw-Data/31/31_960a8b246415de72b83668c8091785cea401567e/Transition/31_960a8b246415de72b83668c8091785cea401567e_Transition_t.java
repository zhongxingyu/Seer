 package com.ace.moab.services;
 
 /**
  * Enumerates all the signals that a Service can respond to, which may cause a transition to a new phase of
  * the service's lifecycle.
  */
 public enum Transition {
     /**
      * A request to create a new service has been submitted. Leads to {@link AnalyzingPhase}.
      */
 	Submit,
     /**
      * {@link AnalyzingPhase} is complete, and scheduler has figured out how to place all
      * service components. Leads to {@link DeployingPhase}.
      */
 	Accept,
     /**
      * After {@link #Onboard}, {@link AnalyzingPhase} is complete. Leads to {@link DeployedPhase}.
      */
     Adopt,
     /**
      * {@link AnalyzingPhase} ended with failure because there is a fundamental problem with
      * the request. No resources have been allocated. Leads to {@link FailedPhase}.
      */
 	Reject,
     /**
      * A manual request to remove history for a service in {@link TerminatedPhase} has been
      * received. Leads to {@link DeletedPhase}.
      */
 	Purge,
     /**
      * An automatic request to remove history for a service in {@link TerminatedPhase} has been
      * received (e.g., because we're reached our retention limit). Leads to {@link DeletedPhase}.
      */
     AutoPurge,
     /**
      * A request has been received to permanently release all resources associated with a service.
      * Can be received in most phases. Leads to {@link CleaningPhase} if resources were allocated,
      * or to {@link FailedPhase} if service has never reached {@link DeployingPhase}.
      */
 	Terminate,
     /**
      * If a service is in {@link BlockedPhase} after {@link CleaningPhase}, and user specifies
      * that cleaning should be abandoned, this transition leads to {@link TerminatedPhase}.
      */
     Abandon,
     /**
      * All resources relating to the service are released. Leads to {@link TerminatedPhase}.
      */
     Clean,
     /**
      * A request has been received for a service in the {@link BlockedPhase} to restart the
      * deployment process. Leads to {@link DeployingPhase}.
      */
 	Unblock,
     /**
      * A service in the {@link DeployingPhase} has experienced a setback that will require
      * manual intervention to fix. Leads to {@link BlockedPhase}.
      */
 	Block,
     /**
      * All work necessary to deploy a service in {@link DeployingPhase} is complete and successful.
      * Leads to {@link DeployedPhase}.
      */
     Deploy,
     /**
      * A service created outside normal workflow needs to be adopted or grandfathered into the system.
      * Leads to {@link AnalyzingPhase} -- but analysis is sparse. Basically we just do some sanity
      * checks, like verifying that we don't already have the service in the system and the service
      * definition meets minimal standards (e.g., has a valid name), and then we trigger the
      * {@link #Adopt} transition (or, if we find a problem), the {@link #Reject} transition.
      */
     Onboard,
     /**
      * A request to modify a {@link DeployedPhase} service has been analyzed. {@link AnalyzingPhase}
      * has concluded that the modification must be disallowed. Leads back to {@link DeployedPhase}.
      */
 	RejectModify,
     /**
      * A request to modify a {@link DeployedPhase} service has been analyzed. {@link AnalyzingPhase}
      * has concluded that the modification must be disallowed. Leads back to {@link DeployedPhase}.
      */
     RejectResume,
     /**
      * The system has asked for a service to be migrated. Leads to {@link AnalyzingPhase}.
      */
     AutoMigrate,
     /**
      * A user has asked for a service to be migrated. Leads to {@link AnalyzingPhase}.
      */
    Migrate,
     /**
      * A user has asked for a service to be modified (adding or removing resources, changing affinities,
      * or similar). Leads to {@link AnalyzingPhase}.
      */
     Modify,
     /**
      * A {@link DeployedPhase} service has reached the end of its lifetime and needs to be retired.
      * Leads to {@link CleaningPhase}.
      */
     Expire,
     /**
      * A user has asked for a {@link DeployedPhase} service to be put in a state of temporary
      * suspension. Leads to {@link SuspendedPhase}.
      */
 	Pause,
     /**
      * The system has detected that a {@link DeployedPhase} service is either not running (e.g., due
      * to a blue screen), or must not be allowed to continue running (e.g., due to a security breach).
      * Leads to {@link SuspendedPhase}.
      */
 	DetectDamage,
     /**
      * A user wishes to modify a service in {@link FailedPhase}, so that it can be resubmitted. Leads
      * to {@link DefiningPhase}.
      */
     Tweak,
     /**
      * A user wishes to reactivate a service in the {@link SuspendedPhase}. Leads to {@link DeployedPhase}.
      */
 	Resume,
 }

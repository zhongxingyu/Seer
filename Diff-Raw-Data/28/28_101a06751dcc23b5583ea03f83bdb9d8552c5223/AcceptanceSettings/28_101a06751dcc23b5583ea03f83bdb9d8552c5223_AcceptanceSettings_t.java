 package com.atlassian.jira.ext.commitacceptance.server.action;
 
 /**
 * Wraps the commit acceptance settings for one project
 * or globally.
  *
  * @author <a href="mailto:ferenc.kiss@midori.hu">Ferenc Kiss</a>
  * @version $Id$
  */
 public class AcceptanceSettings {
 	/**
	 * If <code>true</code> the global settings override
	 * the project-specific settings.
 	 */
 	private boolean useGlobalRules;
     /**
      * If <code>true</code> the commit message must contain
      * at least valid issue key.
      */
     private boolean mustHaveIssue;
 
     /**
      * If <code>true</code>, all the issues must be assigned to
      * the commiter.
      */
     private boolean mustBeAssignedToCommiter;
 
     /**
      * If <code>true</code>, all the issues must be unresolved.
      */
     private boolean mustBeUnresolved;
 
 
     public boolean getUseGlobalRules() {
 		return useGlobalRules;
 	}
 
 	public void setUseGlobalRules(boolean useGlobalRules) {
 		this.useGlobalRules = useGlobalRules;
 	}
 
 	public boolean isMustHaveIssue() {
         return mustHaveIssue;
     }
 
     public void setMustHaveIssue(boolean mustHaveIssue) {
         this.mustHaveIssue = mustHaveIssue;
     }
 
     public boolean isMustBeUnresolved() {
         return mustBeUnresolved;
     }
 
     public void setMustBeUnresolved(boolean mustBeUnresolved) {
         this.mustBeUnresolved = mustBeUnresolved;
     }
 
     public boolean isMustBeAssignedToCommiter() {
         return mustBeAssignedToCommiter;
     }
 
     public void setMustBeAssignedToCommiter(boolean mustBeAssignedToCommiter) {
         this.mustBeAssignedToCommiter = mustBeAssignedToCommiter;
     }
 }

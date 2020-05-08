 /**
 * $Id: OwnerStepFilter.java,v 1.2 2004/12/28 16:10:09 jed Exp $
  * Created by jed on Dec 28, 2004
  */
 package org.infoglue.cms.util.workflow;
 
 import org.infoglue.cms.security.InfoGluePrincipal;
 import com.opensymphony.workflow.spi.Step;
 
 /**
  * Filters steps according to owner.  If a step has no owner, it is assumed that anyone can view it.
  * An administrator can view all steps regardless of owner.
 * @author jed
 * @version $Revision: 1.2 $ $Date: 2004/12/28 16:10:09 $
  */
 public class OwnerStepFilter implements StepFilter
 {
 	private final InfoGluePrincipal userPrincipal;
 
 	/**
 	 * Constructs an owner step filter with the given user principal
 	 * @param userPrincipal an InfoGluePrincipal representing a user
 	 */
 	public OwnerStepFilter(InfoGluePrincipal userPrincipal)
 	{
 		this.userPrincipal = userPrincipal;
 	}
 
 	/**
 	 * Indicates whether the step is allowed
 	 * @param step a workflow step.
 	 * @return true if the step is owned by the current user, the current user is an administrator, or if the owner
 	 * of the step is null; otherwise returns false.
 	 */
 	public boolean isAllowed(Step step)
 	{
 		return step.getOwner() == null
 				|| step.getOwner().equalsIgnoreCase(userPrincipal.getName())
 				|| userPrincipal.getIsAdministrator();
 	}
 }

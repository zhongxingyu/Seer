 /**
 * $Id: AllStepFilter.java,v 1.2 2004/12/28 16:10:09 jed Exp $
  * Created by jed on Dec 28, 2004
  */
 package org.infoglue.cms.util.workflow;
 
 import com.opensymphony.workflow.spi.Step;
 
 /**
  * No-op filter that allows all steps
 * @author jed
 * @version $Revision: 1.2 $ $Date: 2004/12/28 16:10:09 $
  */
 public class AllStepFilter implements StepFilter
 {
 	/**
 	 * No-op implementation that allows all steps to pass
 	 * @param step a workflow step
 	 * @return true
 	 */
 	public boolean isAllowed(Step step)
 	{
 		return true;
 	}
 }

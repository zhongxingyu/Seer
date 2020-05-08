 /**
 * $Id: AllStepFilter.java,v 1.3 2004/12/28 16:54:33 jed Exp $
  * Created by jed on Dec 28, 2004
  */
 package org.infoglue.cms.util.workflow;
 
 import com.opensymphony.workflow.spi.Step;
 
 /**
  * No-op filter that allows all steps
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.3 $ $Date: 2004/12/28 16:54:33 $
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

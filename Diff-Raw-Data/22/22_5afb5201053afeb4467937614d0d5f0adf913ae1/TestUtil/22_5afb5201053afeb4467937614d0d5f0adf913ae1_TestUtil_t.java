 package org.jboss.tools.jst.jsp.test;
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager;
import org.jboss.tools.test.util.JobUtils;
 
public class TestUtil {
 
	public static void waitForValidation(IProject project) throws CoreException{
		JobUtils.waitForIdle();
		for (int i = 0; i < 50; i++) {
			if(ValidatorManager.getStatus().equals(ValidatorManager.SLEEPING)) {
				break;
			}
			JobUtils.delay(100);
			JobUtils.waitForIdle();
		}
	}
}

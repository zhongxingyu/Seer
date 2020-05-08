 
 package eai.msejdf.jbpm.actions;
 
 import org.jbpm.graph.exe.ExecutionContext;
 import org.jbpm.graph.node.DecisionHandler;
 import org.apache.log4j.Logger;
 
 import eai.msejdf.esb.Company;
import eai.msejdf.utils.SOAMessageConstants;
 
 public class CheckStockVariationDecisionHandler implements DecisionHandler  {
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger logger = Logger.getLogger(CheckStockVariationDecisionHandler.class);
 
 	public static final String APPROVE_NOTIFICATION = "Request Approval";
 	public static final String NO_ACTION_REQUIRED = "No Action Required";
 	public static final String NOTIFY_USER = "Notify User";
 	
 	private static final float VARIATION_THRESHOLD_APPROVAL_REQURIED_LOW_INCL = (float)0.5; /* lower threshold (inclusive) */
 	private static final float VARIATION_THRESHOLD_APPROVAL_REQURIED_HIGH_INCL = (float)1; /* upper threshold (inclusive) */
 	
 	private static final long serialVersionUID = 1L;
 
 	public String decide(ExecutionContext executionContext) throws Exception 
 	{
 		if (logger.isDebugEnabled())
 		{
 			logger.debug("decide - start"); //$NON-NLS-1$
 		}
 		
		Company company = (Company) executionContext.getContextInstance().getVariable(SOAMessageConstants.JBPM_MSG_BODY);
 		String transition = null;
 		
 		//Extract variation from the message
 		float variation = Math.abs(company.getVariation().floatValue());
 		
 		if (variation < CheckStockVariationDecisionHandler.VARIATION_THRESHOLD_APPROVAL_REQURIED_LOW_INCL)
 		{
 			/* No action required */ 
 			transition = CheckStockVariationDecisionHandler.NO_ACTION_REQUIRED;
 		}
 		else if (variation <= CheckStockVariationDecisionHandler.VARIATION_THRESHOLD_APPROVAL_REQURIED_HIGH_INCL)
 		{
 			/* User approval required */
 			transition = CheckStockVariationDecisionHandler.APPROVE_NOTIFICATION;
 		}
 		else
 		{
 			/* Automatic notification */
 			transition = CheckStockVariationDecisionHandler.NOTIFY_USER;
 		}
 		
 		if (logger.isInfoEnabled())
 		{
 			logger.info("Transition = " + transition); //$NON-NLS-1$
 		}		
 		
 		if (logger.isDebugEnabled())
 		{
 			logger.debug("decide - end"); //$NON-NLS-1$
 		}
 		
 		return transition;
 	}
 }

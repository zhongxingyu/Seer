 
 package eai.msejdf.jbpm.actions;
 
 import org.jbpm.graph.exe.ExecutionContext;
 import org.jbpm.graph.node.DecisionHandler;
 import org.apache.log4j.Logger;
 
 import eai.msejdf.esb.Company;
 
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
 		
//		String message = executionContext.getContextInstance().getVariable("msgBody").toString();
//		
//		//TODO: Extract variation from the message (or variable carrying it) and remove this
//		String variationStr = message;
//		
//		float variation = Math.abs(Float.parseFloat(variationStr.replace("%", "")));

		Company company = (Company) executionContext.getContextInstance().getVariable("msgBody");
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

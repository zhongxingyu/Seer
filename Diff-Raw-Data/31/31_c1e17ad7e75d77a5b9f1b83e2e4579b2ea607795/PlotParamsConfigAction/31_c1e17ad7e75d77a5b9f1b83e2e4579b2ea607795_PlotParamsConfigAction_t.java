 /*
 
 $Source: /share/content/gforge/webcgh/webgenome/src/org/rti/webcgh/webui/plot/PlotParamsConfigAction.java,v $
$Revision: 1.5 $
$Date: 2006-05-26 17:39:37 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the National 
 Cancer Institute, and so to the extent government employees are co-authors, any 
 rights in such works shall be subject to Title 17 of the United States Code, 
 section 105.
 
 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL 
 CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */
 
 
 package org.rti.webcgh.webui.plot;
 
 
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.Set;
 
 import org.apache.log4j.Logger;
import org.apache.struts.Globals;
 import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionForm;
 import org.rti.webcgh.array.ShoppingCart;
 import org.rti.webcgh.array.persistent.PersistentDomainObjectMgr;
 import org.rti.webcgh.webui.util.AttributeManager;
 
 
 /**
  * Struts action class for retrieving plotting parameters
  * for presentation
  */
 public class PlotParamsConfigAction extends Action {
 	
 	// ===============================
 	//       Constants
 	// ===============================
 	
 	private static final String DEF_PLOT_TYPE = "scatter";
 	
     
     private PersistentDomainObjectMgr persistentDomainObjectMgr = null;
     
 	
     /**
      * @param persistentDomainObjectMgr The persistentDomainObjectMgr to set.
      */
     public void setPersistentDomainObjectMgr(
             PersistentDomainObjectMgr persistentDomainObjectMgr) {
         this.persistentDomainObjectMgr = persistentDomainObjectMgr;
     }
     
     
     private static Logger LOGGER = Logger.getLogger(PlotParamsConfigAction.class.getName());
     
 	/**
 	 * Performs action of retrieving plotting parameters
  	 * for presentation
 	 *
 	 * @param mapping Routing information for downstream actions
 	 * @param form Data from calling form
 	 * @param request Servlet request object
 	 * @param response Servlet response object
 	 * @return Identification of downstream action as configured in the
 	 * struts-config.xml file
 	 * @throws Exception
 	 */
 	public ActionForward execute
 	(
 		ActionMapping mapping, ActionForm form, HttpServletRequest request,
 		HttpServletResponse response
 	) throws Exception {
 		LOGGER.info("Starting action 'Starting PlotParamsConfigAction'");
 		
 		PlotParamsForm pform = (PlotParamsForm)form;
 		
 		String plotType = pform.getPlotType();
 		
 //		String plotType = request.getParameter("plotType");
 //		if (plotType == null || plotType.length() < 1) {
 //			plotType = DEF_PLOT_TYPE;
 //			pform.setPlotType(DEF_PLOT_TYPE);
 //		}
 		
 		
 		// Get list of available quantitation types and set form quantitation type
 		ShoppingCart cart = AttributeManager.getShoppingCart(request);
 		Set qTypes = cart.quantitationTypes();
 		// set as attribute in session
		request.setAttribute("quantitationTypes", qTypes);
 		
 		
 		
 		// determine if the user has configured parameters yet or not
 		boolean configurationIsDone = false;
 		String paramsConfigured = pform.getParamsConfigured();
 		if (paramsConfigured != null && paramsConfigured.equals("true")) {
 			configurationIsDone = true;
 		}
 		
 		
 		String calledFromPlotParams = request.getParameter("calledFromPlotParams");
 		
		// See if there are errors on validate
		ActionErrors ae = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
		
 		// if the user has supplied parameters that validate, forward to the
 		// page that will execute the plot.  if not, send user to appropriate
 		// plot parameter config page
 		ActionForward forward = null;
 		
 		/*if (configurationIsDone) {
 			
 			// the user has successfully configured parameters, so reset
 			// the form-bean attribute to be ready for the next time
 			pform.setParamsConfigured("");
 			
 			forward = mapping.findForward("readyToPlot");
 		}
 		else if ("scatter".equals(plotType))
 			forward = mapping.findForward("scatterPlotConfig");			
 		else if ("ideogram".equals(plotType))
 			forward = mapping.findForward("ideogramPlotConfig");
 		else
 			forward = mapping.findForward("scatterPlotConfig");
 		*/
 		
		// if no errors and it's called from params jsp
		if (ae == null && calledFromPlotParams != null) {
 			
 			// the user has successfully configured parameters, so reset
 			// the form-bean attribute to be ready for the next time
 			pform.setParamsConfigured("");
 			
 			forward = mapping.findForward("readyToPlot");
 		}
 		else if ("scatter".equals(plotType))
 			forward = mapping.findForward("scatterPlotConfig");			
 		else if ("ideogram".equals(plotType))
 			forward = mapping.findForward("ideogramPlotConfig");
 		else
 			forward = mapping.findForward("scatterPlotConfig");
 
 		
 		LOGGER.info("Completed action 'PlotParamsConfigAction'");
 		
 		return forward;
 	}
     
 }

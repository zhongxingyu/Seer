 /*
$Revision: 1.3 $
$Date: 2006-10-28 04:07:57 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
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
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webcgh.webui.struts.cart;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.rti.webcgh.webui.struts.BaseAction;
 
 /**
  * Routes a request to one of several pages
  * that enable the user to input plotting
  * parameters.  Routing is based on selected
  * plot type.
  * @author dhall
  *
  */
 public final class RouteToPlotParametersPageAction extends BaseAction {
 
 	
 	/**
      * Execute action.
      * @param mapping Routing information for downstream actions
      * @param form Form data
      * @param request Servlet request object
      * @param response Servlet response object
      * @return Identification of downstream action as configured in the
      * struts-config.xml file
      * @throws Exception All exceptions thrown by classes in
      * the method are passed up to a registered exception
      * handler configured in the struts-config.xml file
      */
     public ActionForward execute(
         final ActionMapping mapping, final ActionForm form,
         final HttpServletRequest request,
         final HttpServletResponse response
     ) throws Exception {
     	
     	// Recover type of plot from form
     	PlotParametersForm pForm = (PlotParametersForm) form;
     	String plotType = pForm.getPlotType();
     	
    	// If new plot, reset form plot name
    	if (request.getParameter("id") == null) {
    		pForm.setName("");
    	}
    	
     	// Determine forward
     	ActionForward forward = null;
     	if ("scatter".equals(plotType))	 {
     		forward = mapping.findForward("scatter");
     	} else if ("ideogram".equals(plotType)) {
     		forward = mapping.findForward("ideogram");
     	}
     	
     	return forward;
     }
 }

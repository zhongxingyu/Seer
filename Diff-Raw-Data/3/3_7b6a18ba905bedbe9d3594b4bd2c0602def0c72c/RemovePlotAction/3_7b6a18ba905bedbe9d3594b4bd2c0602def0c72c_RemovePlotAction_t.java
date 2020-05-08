 /*
 $Revision: 1.7 $
 $Date: 2008-02-22 18:24:44 $
 
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
 
 package org.rti.webgenome.webui.struts.cart;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.rti.webgenome.domain.Plot;
 import org.rti.webgenome.domain.ShoppingCart;
 import org.rti.webgenome.webui.struts.BaseAction;
 import org.rti.webgenome.webui.util.PageContext;
 
 /**
  * Action to remove plot from shopping cart.
  * @author dhall
  *
  */
 public final class RemovePlotAction extends BaseAction {
 
 
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
     	
     	// Get shopping cart
     	ShoppingCart cart = this.getShoppingCart(request);
     	
     	// Get ID of plot to remove
     	long id = Long.parseLong(request.getParameter("id"));
     	
     	// If there are references to plot, deny request to delete
     	if (this.getDbService().isPlotReferenced(id)) {
     		return mapping.findForward("referenced");
     	}
     	
     	// Retrieve plot
     	Plot plot = cart.getPlot(id);
     	
     	// Remove plot
     	cart.removePlot(id);
     	if (PageContext.standAloneMode(request)) {
     		this.getDbService().updateShoppingCart(cart);
     	}
     	
     	// Get rid of image files
     	for (String fileName : plot.getAllImageFileNames()) {
     		this.getImageFileManager().deleteImageFile(fileName);
     	}
     	
     	// Get rid of plot interactivity files
     	this.getDataFileManager().deleteDataFiles(plot);
     	
    	// remove from db
    	this.dbService.deletePlot(plot);
    	
     	// TODO: Stand-alone specific actions
     	
         return mapping.findForward("success");
     }
 }

 /*
$Revision: 1.25 $
$Date: 2006-11-29 03:14:06 $
 
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
 
 package org.rti.webcgh.webui.struts.client;
 
 import java.io.File;
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.rti.webcgh.core.InvalidClientQueryParametersException;
 import org.rti.webcgh.core.PlotType;
 import org.rti.webcgh.domain.BioAssay;
 import org.rti.webcgh.domain.Experiment;
 import org.rti.webcgh.domain.GenomeInterval;
 import org.rti.webcgh.domain.Organism;
import org.rti.webcgh.domain.QuantitationType;
 import org.rti.webcgh.domain.ShoppingCart;
 import org.rti.webcgh.graphics.util.ColorChooser;
 import org.rti.webcgh.service.client.ClientDataService;
 import org.rti.webcgh.service.dao.OrganismDao;
 import org.rti.webcgh.service.io.ImageFileManager;
 import org.rti.webcgh.service.util.IdGenerator;
 import org.rti.webcgh.units.BpUnits;
 import org.rti.webcgh.webui.struts.BaseAction;
 import org.rti.webcgh.webui.struts.cart.PlotParametersForm;
 import org.rti.webcgh.webui.struts.cart.SelectedExperimentsForm;
 import org.rti.webcgh.webui.util.ClientQueryParser;
 import org.rti.webcgh.webui.util.PageContext;
 import org.rti.webcgh.webui.util.SessionMode;
 import org.rti.webgenome.client.BioAssayDataConstraints;
 
 /**
  * Action that is invoked when a user is initially directed
  * to webGenome from a client application.
  */
 public final class ClientPlotAction extends BaseAction {
     
 	/** Logger. */
 	private static final Logger LOGGER =
 		Logger.getLogger(ClientPlotAction.class);
 	
 	
 	/** Client data service. This property should be injected. */
     private ClientDataService clientDataService = null;
     
     /** Experiment ID generator. */
     private IdGenerator experimentIdGenerator = null;
     
     /** Bioassay ID generator. */
     private IdGenerator bioAssayIdGenerator = null;
     
     /** Image file manager. */
     private ImageFileManager imageFileManager = null;
     
     /** Organism data access object. */
     private OrganismDao organismDao = null;
 
 
     /**
      * Set image file manager.
      * @param imageFileManager Image file manager.
      */
     public void setImageFileManager(
     		final ImageFileManager imageFileManager) {
 		this.imageFileManager = imageFileManager;
 	}
 
 
     /**
      * Set organism data access object.
      * @param organismDao Organism data access object
      */
 	public void setOrganismDao(final OrganismDao organismDao) {
 		this.organismDao = organismDao;
 	}
 
 
 	/**
      * Set the client data service.
      * @param clientDataService Client data service
      */
     public void setClientDataService(
     		final ClientDataService clientDataService) {
 		this.clientDataService = clientDataService;
 	}
 
 
     /**
      * Set bioassay ID generator.
      * @param bioAssayIdGenerator ID generator
      */
 	public void setBioAssayIdGenerator(
 			final IdGenerator bioAssayIdGenerator) {
 		this.bioAssayIdGenerator = bioAssayIdGenerator;
 	}
 
 
 	/**
 	 * Set experiment ID generator.
 	 * @param experimentIdGenerator ID generator
 	 */
 	public void setExperimentIdGenerator(
 			final IdGenerator experimentIdGenerator) {
 		this.experimentIdGenerator = experimentIdGenerator;
 	}
 
 
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
 	public ActionForward execute(final ActionMapping mapping,
 			final ActionForm form, final HttpServletRequest request, 
     		final HttpServletResponse response) throws Exception {
 		LOGGER.debug("Starting ClientPlotAction");
 		
 		// Cache client ID in session
 		String clientID = request.getParameter("clientID");
 		if (clientID == null) {
 			throw new InvalidClientQueryParametersException(
 					"Missing 'clientID' parameter");
 		}
 		PageContext.setClientId(request, clientID);
 		
 		// Construct parameters for obtaining data through
 		// the client data service
         String[] experimentIds = ClientQueryParser.getExperimentIds(request);
         BioAssayDataConstraints[] constraints =
         	ClientQueryParser.getBioAssayDataConstraints(request);
         
        // Get quantitation type
        String qType = request.getParameter("qType");
        if (qType == null) {
        	throw new InvalidClientQueryParametersException(
        			"Missing 'qType' parameter");
        }
        QuantitationType quantitationType =
        	QuantitationType.getQuantitationType(qType);
        if (quantitationType == null) {
        	throw new InvalidClientQueryParametersException(
			"Unrecognized quantitation type");
        }
        
         // Retrieve data from client
         Collection<Experiment> experiments = 
         	this.clientDataService.getClientData(constraints,
         			experimentIds, clientID);
         
         // TODO: In the future the organism should come from the
         // client query string
         
         // Give each experiment a unique ID and default
         // organism.  Give each bioassay a color
         ColorChooser colorChooser = PageContext.getColorChooser(
         		request, true);
         Organism org = this.organismDao.loadDefault();
         for (Experiment exp : experiments) {
         	Long expId = this.experimentIdGenerator.nextId();
         	exp.setId(expId);
         	exp.setOrganism(org);
         	for (BioAssay ba : exp.getBioAssays()) {
         		ba.setColor(colorChooser.nextColor());
         		ba.setId(this.bioAssayIdGenerator.nextId());
         	}
         }
         
         // Put data in shopping cart
         ShoppingCart cart = PageContext.getShoppingCart(request, true);
         cart.add(experiments);
         
         // TODO: Make this cleaner.
         // Initialize image file manager
         if (!this.imageFileManager.isInitialized()) {
 	        String absPlotPath = this.getServlet().
 	        	getServletContext().getRealPath("/plots");
 	        File imageDir = new File(absPlotPath);
 	        this.imageFileManager.init(imageDir);
         }
         
         
         // Set image file manager property of shopping cart
         // so that image files will be deleted when the users
         // session ends
         cart.setImageFileManager(this.imageFileManager);
         
         // Set session mode
         PageContext.setSessionMode(request, SessionMode.CLIENT);
         
         // Initialize plot parameters form
         PlotParametersForm pForm = (PlotParametersForm) form;
         pForm.init();
         pForm.setGenomeIntervals(GenomeInterval.encode(constraints));
         pForm.setUnits(BpUnits.BP.getName());
         pForm.setPlotType(PlotType.SCATTER.getName());
         
         // Set selected experiments form
         SelectedExperimentsForm sef =
         	PageContext.getSelectedExperimentsForm(request, true);
         sef.setSelectedExperimentIds(experiments);
         
 		return mapping.findForward("success");
 	}
 }

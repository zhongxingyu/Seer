 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.dto.critieria.AllGenesCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.ArrayPlatformCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.CloneOrProbeIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.Constants;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.FoldChangeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneOntologyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.InstitutionCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.PathwayCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.ArrayPlatformDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 import gov.nih.nci.rembrandt.cache.PresentationTierCache;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ChromosomeHelper;
 import gov.nih.nci.rembrandt.web.helper.ReportGeneratorHelper;
 import gov.nih.nci.rembrandt.web.struts.form.GeneExpressionForm;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.LookupDispatchAction;
 
 
 
 public class GeneExpressionAction extends LookupDispatchAction {
     private static Logger logger = Logger.getLogger(GeneExpressionAction.class);
 	private PresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
     private UserCredentials credentials;
 	/**
      * Method setup
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     
     //Setup the gene Expression form from menu page
     public ActionForward setup(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
 		GeneExpressionForm geneExpressionForm = (GeneExpressionForm) form;
 		//Since Chromosomes is a static variable there is no need to set it twice.
 		//It is only a lookup option collection
 		if(geneExpressionForm.getChromosomes()==null||geneExpressionForm.getChromosomes().isEmpty()) {
 			//set the chromsomes list in the form 
 			logger.debug("Setup the chromosome values for the form");
 			geneExpressionForm.setChromosomes(ChromosomeHelper.getInstance().getChromosomes());
 		}
 		return mapping.findForward("backToGeneExp");
     }
     
    //if multiUse button clicked (with styles de-activated) forward back to page
     public ActionForward multiUse(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
 		
 		return mapping.findForward("backToGeneExp");
     }
     
     
     
     /**
      * Method submitAllGenes
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     
     //If this is an All Genes submit
     public ActionForward submitAllGenes(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
         
         request.getSession().setAttribute("currentPage", "0");
 		request.getSession().removeAttribute("currentPage2");
 		GeneExpressionForm geneExpressionForm = (GeneExpressionForm) form;
          
 		geneExpressionForm.setGeneGroup("");
 		geneExpressionForm.setRegulationStatus("up");
 		geneExpressionForm.setFoldChangeValueUp(RembrandtConstants.ALL_GENES_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueUDUp(RembrandtConstants.ALL_GENES_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueDown(RembrandtConstants.ALL_GENES_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueUDDown(RembrandtConstants.ALL_GENES_GENE_EXP_REGULATION);
 		logger.debug("set former gene values to null");
 		logger.debug("This is an All Genes Gene Expression Submital");
 		return mapping.findForward("showAllGenes");
     }
     
     /**
      * Method submitStandard
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     
     //If this is a standard submit
     public ActionForward submitStandard(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
         
         request.getSession().setAttribute("currentPage", "0");
 		request.getSession().removeAttribute("currentPage2");
 		
 		GeneExpressionForm geneExpressionForm = (GeneExpressionForm) form;
 		// set form back to standard state and clear default value
 		geneExpressionForm.setRegulationStatus("");
 		geneExpressionForm.setFoldChangeValueUp(RembrandtConstants.STANDARD_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueUDUp(RembrandtConstants.STANDARD_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueDown(RembrandtConstants.STANDARD_GENE_EXP_REGULATION);
 		geneExpressionForm.setFoldChangeValueUDDown(RembrandtConstants.STANDARD_GENE_EXP_REGULATION);
 		
 		logger.debug("This is an Standard Gene Expression Submital");
 		return mapping.findForward("backToGeneExp");
     }
     
     /**
      * Method submittal
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     //If this is a Submittal do the following	
 	public ActionForward submittal(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
         
 		request.getSession().setAttribute("currentPage", "0");
 		request.getSession().removeAttribute("currentPage2");
 		String sessionId = request.getSession().getId();
 		GeneExpressionForm geneExpressionForm = (GeneExpressionForm) form;
  
 	 /*The following 15 lines of code/logic will eventually need to be moved/re-organized. All Genes queries should have their own actions, forms, etc. For
 	  * now, in order to properly validate an all genes query and STILL be able to forward back to
 	  * the appropriate query page (tile order), this logic has been placed in the action itself, so
 	  * that there can be proper forwarding when errors are generated from an all genes submission.
 	  * This logic checks if the query is an all genes query and then if the fold change value is
 	  * less than 4 with a corresponding regulation status uplon submission. If it is less than 4,
 	  * an error is created and sent with the request to the forward.
 	  */
 		   if (geneExpressionForm.getIsAllGenes()){
 		      try{
 		        int intFoldChangeValueUp = Integer.parseInt(geneExpressionForm.getFoldChangeValueUp());
 		        int intFoldChangeValueDown = Integer.parseInt(geneExpressionForm.getFoldChangeValueDown());
 		        int intFoldChangeValueUDUp = Integer.parseInt(geneExpressionForm.getFoldChangeValueUDUp());
 		        int intFoldChangeValueUDDown = Integer.parseInt(geneExpressionForm.getFoldChangeValueUDDown());
 			        if((intFoldChangeValueUp < 4 && geneExpressionForm.getRegulationStatus().equalsIgnoreCase("up")) || 
 			                (intFoldChangeValueDown < 4 && geneExpressionForm.getRegulationStatus().equalsIgnoreCase("down")) ||
 			                (intFoldChangeValueUDUp < 4 && geneExpressionForm.getRegulationStatus().equalsIgnoreCase("updown")) || 
 			                (intFoldChangeValueUDDown < 4 && geneExpressionForm.getRegulationStatus().equalsIgnoreCase("updown"))){
 			            ActionErrors errors = new ActionErrors();
 			            errors.add("regulationStatusAllGenes", new ActionError(
 						"gov.nih.nci.nautilus.ui.struts.form.regulationStatus.allGenes.error")); 
 			            this.saveErrors(request, errors);
 					    return mapping.findForward("showAllGenes"); 
 			        }
 		      }catch (NumberFormatException ex) {
 		           ActionErrors errors = new ActionErrors();
 		            errors.add("regulationStatusAllGenes", new ActionError(
 					"gov.nih.nci.nautilus.ui.struts.form.regulationStatus.allGenes.error")); 
 		            this.saveErrors(request, errors);
 				    return mapping.findForward("showAllGenes");    
 	  		    }
 		    }
 		   //All Genes validation ENDS HERE 
 		   
 		
 		// Create Query Objects
 		GeneExpressionQuery geneExpQuery = createGeneExpressionQuery(geneExpressionForm);
 	    
         //Check user credentials and constrain query by Institutions
         if(request.getSession().getAttribute(RembrandtConstants.USER_CREDENTIALS)!=null){
             credentials = (UserCredentials) request.getSession().getAttribute(RembrandtConstants.USER_CREDENTIALS);
             InstitutionCriteria institutionCriteria = new InstitutionCriteria();
             institutionCriteria.setInstitutions(credentials.getInstitutes());
             geneExpQuery.setInstitutionCriteria(institutionCriteria);
         }
         logger.debug("This is a Gene Expression Submital");
 	   
 		if (!geneExpQuery.isEmpty()) {
 			SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
             queryBag.putQuery(geneExpQuery, geneExpressionForm);
             presentationTierCache.putSessionQueryBag(sessionId, queryBag);
             
      	} else {
 			ActionErrors errors = new ActionErrors();
 			ActionError error = new ActionError("gov.nih.nci.nautilus.ui.struts.form.query.geneexp.error");
 			errors.add(ActionErrors.GLOBAL_ERROR, error);
 			this.saveErrors(request, errors);
 		    return mapping.findForward("backToGeneExp"); 
 		}
 		
         
 		return mapping.findForward("advanceSearchMenu");
 	}
 
 	/**
 	 * This action is called when the user has selected the preview
 	 * button on the GeneExpression build query page.  It takes
 	 * the current values that the user has input and creates a 
 	 * GeneExpressionQuery.  It then creates a CompoundQuery with
 	 * the query and gives it a temp name. It then calls the 
 	 * ReportGeneratorHelper which will construct a report and drop
 	 * it in the sessionCache for later retrieval for rendering in a jsp.
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward preview(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 
 		request.getSession().setAttribute("currentPage", "0");
 		request.getSession().removeAttribute("currentPage2");
 		GeneExpressionForm geneExpressionForm = (GeneExpressionForm) form;
 		logger.debug("This is a Gene Expression Preview");
 		// Create Query Objects
 		GeneExpressionQuery geneExpQuery = createGeneExpressionQuery(geneExpressionForm);
 	    request.setAttribute("previewForm",geneExpressionForm.cloneMe());
         logger.debug("This is a Preview Report");
 	    CompoundQuery compoundQuery = new CompoundQuery(geneExpQuery);
 	    compoundQuery.setQueryName(RembrandtConstants.PREVIEW_RESULTS);
         logger.debug("Setting query name to:"+compoundQuery.getQueryName());
 	    compoundQuery.setAssociatedView(ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
         logger.debug("Associated View for the Preview:"+compoundQuery.getAssociatedView().getClass());
 	    //Save the sessionId that this preview query is associated with
         compoundQuery.setSessionId(request.getSession().getId());
         //Generate the reportXML for the preview.  It will be stored in the session
 	    //cache for later retrieval
         ReportGeneratorHelper reportHelper = new ReportGeneratorHelper(compoundQuery, new HashMap());
         return mapping.findForward("previewReport");
 	}
 	
 	public ActionForward getCytobands(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 			GeneExpressionForm geForm = (GeneExpressionForm)form;
 			//This is the static list of chromosomes that is fetched the first time it is needed
 			List chromosomes = geForm.getChromosomes();
 			//IMPORTANT! geForm.chromosomeNumber is NOT the chromosome number.  It is the index
 			//into the static chromosomes list where the chromosome can be found.
 			if(!"".equals(geForm.getChromosomeNumber())) {
 				ChromosomeBean bean = (ChromosomeBean)chromosomes.get(Integer.parseInt(geForm.getChromosomeNumber()));
 				geForm.setCytobands(bean.getCytobands());
 			}
 			
 			return mapping.findForward("backToGeneExp");
 	}
 			
 			
 	
 	
 	private GeneExpressionQuery createGeneExpressionQuery(GeneExpressionForm geneExpressionForm){
 	    GeneExpressionQuery geneExpQuery = (GeneExpressionQuery)QueryManager.createQuery(QueryType.GENE_EXPR_QUERY_TYPE);
 	    geneExpQuery.setQueryName(geneExpressionForm.getQueryName());
 		// Change this code later to get view type directly from Form !!
 	    String thisView = geneExpressionForm.getResultView();
 		if (thisView.equalsIgnoreCase("sample")) {
 			geneExpQuery.setAssociatedView(ViewFactory
 					.newView(ViewType.CLINICAL_VIEW));
 		} else if (thisView.equalsIgnoreCase("gene")) {
 			geneExpQuery.setAssociatedView(ViewFactory
 					.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
 		}
 		// Set gene criteria
 		GeneIDCriteria geneIDCrit = geneExpressionForm.getGeneIDCriteria();
 		if (!geneIDCrit.isEmpty())
 			geneExpQuery.setGeneIDCrit(geneIDCrit);
 		SampleCriteria sampleIDCrit = geneExpressionForm.getSampleCriteria();
 		if (!sampleIDCrit.isEmpty())
 			geneExpQuery.setSampleIDCrit(sampleIDCrit);
 		AllGenesCriteria allGenesCrit = geneExpressionForm.getAllGenesCriteria();
 		if (!allGenesCrit.isEmpty())
 		    geneExpQuery.setAllGenesCrit(allGenesCrit);
		FoldChangeCriteria foldChangeCrit = geneExpressionForm.getFoldChangeCriteria();
 		if (!foldChangeCrit.isEmpty())
 			geneExpQuery.setFoldChgCrit(foldChangeCrit);
 		RegionCriteria regionCrit = geneExpressionForm.getRegionCriteria();
 		if (!regionCrit.isEmpty())
 			geneExpQuery.setRegionCrit(regionCrit);
 		DiseaseOrGradeCriteria diseaseOrGradeCriteria = geneExpressionForm
 				.getDiseaseOrGradeCriteria();
 		if (!diseaseOrGradeCriteria.isEmpty())
 			geneExpQuery.setDiseaseOrGradeCrit(diseaseOrGradeCriteria);
 		CloneOrProbeIDCriteria cloneOrProbeIDCriteria = geneExpressionForm
 				.getCloneOrProbeIDCriteria();
 		if (!cloneOrProbeIDCriteria.isEmpty())
 			geneExpQuery.setCloneOrProbeIDCrit(cloneOrProbeIDCriteria);
 		GeneOntologyCriteria geneOntologyCriteria = geneExpressionForm
 				.getGeneOntologyCriteria();
 		if (!geneOntologyCriteria.isEmpty())
 			geneExpQuery.setGeneOntologyCrit(geneOntologyCriteria);
 		PathwayCriteria pathwayCriteria = geneExpressionForm
 				.getPathwayCriteria();
 		if (!pathwayCriteria.isEmpty())
 			geneExpQuery.setPathwayCrit(pathwayCriteria);
 		ArrayPlatformCriteria arrayPlatformCriteria = geneExpressionForm
 				.getArrayPlatformCriteria();
 		if (!arrayPlatformCriteria.isEmpty()) {
 			geneExpQuery.setArrayPlatformCrit(arrayPlatformCriteria);
 		}else {
 			/*
 			 * This logic is required for an all genes query.  There
 			 * must be an ArrayPlatformDE specified for the all gene's
 			 * query, and there was not one being created.  This is 
 			 * probably a hack as we may later allow the user to select
 			 * from the a list of platforms, and all could be the default.
 			 * --Dave
 			 */
 			arrayPlatformCriteria = new ArrayPlatformCriteria();
 			arrayPlatformCriteria.setPlatform(new ArrayPlatformDE(Constants.ALL_PLATFROM));
 			geneExpQuery.setArrayPlatformCrit(arrayPlatformCriteria);
 		}
         
 	    return geneExpQuery;
 	}
 	
 	protected Map getKeyMethodMap() {
 		 
        HashMap<String,String> map = new HashMap<String,String>();
        //Gene Expression Query Button using gene expression setup method
        map.put("GeneExpressionAction.setupButton", "setup");
        
        //Submit Query Button using gene expression submittal method
        map.put("buttons_tile.submittalButton", "submittal");
        
        //Preview Query Button using gene expression preview method
        map.put("buttons_tile.previewButton", "preview");
        
        //Submit All Genes Button using gene expression submitAllGenes method
        map.put("buttons_tile.submitAllGenes", "submitAllGenes");
        
        //Submit Standard Button using gene expression submitStandard method
        map.put("buttons_tile.submitStandard", "submitStandard");
        
        //Submit to get the cytobands of the selected chromosome
        map.put("GeneExpressionAction.getCytobands", "getCytobands");
        
        //Submit nothing if multiuse button entered if css turned off
        map.put("buttons_tile.multiUseButton", "multiUse");
        
        return map;
        
        }
 }

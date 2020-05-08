 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.critieria.AllGenesCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AlleleFrequencyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AnalysisTypeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AssayPlatformCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.CloneOrProbeIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.Constants;
 import gov.nih.nci.caintegrator.dto.critieria.CopyNumberCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SegmentMeanCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SNPCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.AssayPlatformDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.caintegrator.dto.de.SNPIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.enumeration.AnalysisType;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ComparativeGenomicQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.service.findings.RembrandtAsynchronousFindingManagerImpl;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ChromosomeHelper;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.helper.ListConvertor;
 import gov.nih.nci.rembrandt.web.helper.ReportGeneratorHelper;
 import gov.nih.nci.rembrandt.web.struts.form.ComparativeGenomicForm;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.LookupDispatchAction;
 
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ComparativeGenomicAction extends LookupDispatchAction {
     private static Logger logger = Logger.getLogger(ComparativeGenomicAction.class);
     private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
    //if multiUse button clicked (with styles de-activated) forward back to page
     public ActionForward multiUse(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
 		
		return mapping.findForward("backToCGH");
     }
     
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
     
     //Setup the comparativeGenomicForm from menu page
     public ActionForward setup(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
 		ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
 		//Since Chromosomes is a static variable there is no need to set it twice.
 		//It is only a lookup option collection
 		if(comparativeGenomicForm.getChromosomes()==null||comparativeGenomicForm.getChromosomes().isEmpty()) {
 			//set the chromsomes list in the form 
 			logger.debug("Setup the chromosome values for the form");
 			comparativeGenomicForm.setChromosomes(ChromosomeHelper.getInstance().getChromosomes());
 		}
         GroupRetriever groupRetriever = new GroupRetriever();
         comparativeGenomicForm.setSavedSampleList(groupRetriever.getClinicalGroupsCollectionNoPath(request.getSession()));
 		return mapping.findForward("backToCGH");
     }
     
     /*This method is needed for the apparant problem with LookupDispatchAction class in Struts
      *  (non-Javadoc)
      * Doesn't appear that the developer can make a call to any of the methods in this
      * class via a url path (e.g. myaction.do?method=setup). The work around is not specifying
      * a method, in which case struts will call the following "unspecified" method call below.
      * In this case the desired effect is to reset the form(setup) with the prefilled dropdowns...
      * so ALL this method does is call the setup method.
      */
     /**TODO change the action to a DispatchAction for more flexibility in the future.
      * -KR
      */
     public ActionForward unspecified(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
         this.setup(mapping,form,request,response);
         
         return mapping.findForward("backToCGH");
     }
     
     public ActionForward getCytobands(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 			ComparativeGenomicForm cgForm = (ComparativeGenomicForm)form;
 			//This is the static list of chromosomes that is fetched the first time it is needed
 			List chromosomes = cgForm.getChromosomes();
 			//IMPORTANT! geForm.chromosomeNumber is NOT the chromosome number.  It is the index
 			//into the static chromosomes list where the chromosome can be found.
 			if(!"".equals(cgForm.getChromosomeNumber())) {
 				ChromosomeBean bean = (ChromosomeBean)chromosomes.get(Integer.parseInt(cgForm.getChromosomeNumber()));
 				cgForm.setCytobands(bean.getCytobands());
 			}
 			
 			return mapping.findForward("backToCGH");
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
 		
 		ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
         //set all Genes query and give copyNumber default value
 		comparativeGenomicForm.setSampleType("PairedTissue");
 		comparativeGenomicForm.setGeneGroup("");
 		comparativeGenomicForm.setCopyNumberView("calculatedCN");
 		comparativeGenomicForm.setGeneRegionView("geneView");
 		comparativeGenomicForm.setCopyNumber("amplified");
 		comparativeGenomicForm.setCnAmplified(RembrandtConstants.ALL_GENES_COPY_NUMBER_REGULATION);
 		comparativeGenomicForm.setCnADAmplified(RembrandtConstants.ALL_GENES_COPY_NUMBER_REGULATION);
 		comparativeGenomicForm.setCnADDeleted("1");
 		comparativeGenomicForm.setCnDeleted("1");
 
 		comparativeGenomicForm.setSegmentMean("");
 		comparativeGenomicForm.setSmAmplified("");
 		comparativeGenomicForm.setSmDeleted("");
          
 		logger.debug("This is an All Genes cgh Submital");
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
 		
 		ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
 		////set standard query and clear copyNumber default value
 		comparativeGenomicForm.setCopyNumber("");
 		comparativeGenomicForm.setCnAmplified("");
 		comparativeGenomicForm.setCnADAmplified("");
 		comparativeGenomicForm.setCnADDeleted("");
 		comparativeGenomicForm.setCnDeleted("");
 
 		comparativeGenomicForm.setSegmentMean("");
 		comparativeGenomicForm.setSmAmplified("");
 		comparativeGenomicForm.setSmDeleted("");
 		
 		logger.debug("This is an Standard cgh Submital");
 		return mapping.findForward("backToCGH");
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
         ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
         
         /*The following 15 lines of code/logic will eventually need to be moved/re-organized. All Genes queries should have their own actions, forms, etc. For
    	  * now, in order to properly validate an all genes query and STILL be able to forward back to
    	  * the appropriate query page (tile order), this logic has been placed in the action itself, so
    	  * that there can be proper forwarding when errors are generated from an all genes submission.
    	  * This logic checks if the query is an all genes query and then if the copy number value is
    	  * less than 10 for amplified and greater than 1 for deletion. If it is less than 10 for amp and greater than 1 for deleted,
    	  * an error is created and sent with the request to the forward.
    	  * BEGINS HERE!!
    	  */
    		   if (comparativeGenomicForm.getIsAllGenes()){
    		       try{
    		        int intCnAmplified = Integer.parseInt(comparativeGenomicForm.getCnAmplified());
    		        float floatCnDeleted = Float.parseFloat(comparativeGenomicForm.getCnDeleted());
    		        int intCnADAmplified = Integer.parseInt(comparativeGenomicForm.getCnADAmplified());
    		        float floatCnADDeleted = Float.parseFloat(comparativeGenomicForm.getCnADDeleted());
    			        if((intCnAmplified < 10 && comparativeGenomicForm.getCopyNumber().equalsIgnoreCase("amplified")) || 
    			             (intCnADAmplified < 10 && comparativeGenomicForm.getCopyNumber().equalsIgnoreCase("ampdel"))){
    			            ActionErrors errors = new ActionErrors();
 			            errors.add("copyNumberAllGenesAmp", new ActionError(
 						"gov.nih.nci.nautilus.ui.struts.form.copyNumberAmp.allGenes.error")); 
 			            this.saveErrors(request, errors);
 					    return mapping.findForward("showAllGenes"); 
    			        }
    			        if((floatCnDeleted > 1 && comparativeGenomicForm.getCopyNumber().equalsIgnoreCase("deleted")) ||
    			             (floatCnADDeleted > 1 && comparativeGenomicForm.getCopyNumber().equalsIgnoreCase("ampdel"))){
    			            ActionErrors errors = new ActionErrors();
    			            errors.add("copyNumberAllGenesDel", new ActionError(
    						"gov.nih.nci.nautilus.ui.struts.form.copyNumberDel.allGenes.error")); 
    			            this.saveErrors(request, errors);
    					    return mapping.findForward("showAllGenes"); 
    			        }
    		     } catch (NumberFormatException ex) {
    		           ActionErrors errors = new ActionErrors();
 		            errors.add("copyNumberAllGenesDel", new ActionError(
 					"gov.nih.nci.nautilus.ui.struts.form.copyNumberDel.allGenes.error")); 
 		            this.saveErrors(request, errors);
 				    return mapping.findForward("showAllGenes");    
   		    }
         }
    		  //All Genes validation ENDS HERE  
    		
         
         logger.debug("This is a Comparative Genomic Submittal");
         //Create Query Objects
         ComparativeGenomicQuery cghQuery = createCGHQuery(comparativeGenomicForm, request.getSession());
         
         //Check user credentials and constrain query by Institutions
         if(cghQuery != null){
         	cghQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(request.getSession()));
             }
         
         //This is required as struts resets the form.  It is later added back to the request
         request.setAttribute("previewForm", comparativeGenomicForm.cloneMe());
        
         
             try {
                 //Store the query in the SessionQueryBag
                 if (!cghQuery.isEmpty()) {
                     SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
                     queryBag.putQuery(cghQuery, comparativeGenomicForm);
                     presentationTierCache.putSessionQueryBag(sessionId, queryBag);
                 } else {
                     ActionErrors errors = new ActionErrors();
                     ActionError error =  new ActionError(
                     		"gov.nih.nci.nautilus.ui.struts.form.query.cgh.error");
                     errors.add(ActionErrors.GLOBAL_ERROR,error);
                     this.saveErrors(request, errors);
                     return mapping.findForward("backToCGH");
                 }
             }
             catch (Exception e) {
                logger.error(e);
             }
             return mapping.findForward("advanceSearchMenu");
         }
   
     
     /**
      * Method preview
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
     public ActionForward preview(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
         request.getSession().setAttribute("currentPage", "0");
         request.getSession().removeAttribute("currentPage2");
         ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
         logger.debug("This is a Comparative Genomic Preview");
         //Create Query Objects
         ComparativeGenomicQuery cghQuery = createCGHQuery(comparativeGenomicForm,request.getSession());
         if(cghQuery != null){
         	cghQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(request.getSession()));
             }
         //This is required as struts resets the form.  It is later added back to the request
         request.setAttribute("previewForm", comparativeGenomicForm.cloneMe());
         CompoundQuery compoundQuery = new CompoundQuery(cghQuery);
         compoundQuery.setQueryName(RembrandtConstants.PREVIEW_RESULTS);
         logger.debug("Setting query name to:"+compoundQuery.getQueryName());
         compoundQuery.setAssociatedView(cghQuery.getAssociatedView());
         logger.debug("Associated View for the Preview:"+compoundQuery.getAssociatedView().getClass());
 	    //Save the sessionId that this preview query is associated with
         compoundQuery.setSessionId(request.getSession().getId());
         //Generate the reportXML for the preview.  It will be stored in the session
 	    //cache for later retrieval
         //ReportGeneratorHelper reportHelper = new ReportGeneratorHelper(compoundQuery, new HashMap());
         //return mapping.findForward("previewReport");
         
         RembrandtAsynchronousFindingManagerImpl asynchronousFindingManagerImpl = new RembrandtAsynchronousFindingManagerImpl();
         try {
 			asynchronousFindingManagerImpl.submitQuery(request.getSession(), compoundQuery);
 		} catch (FindingsQueryException e) {
 			logger.error(e.getMessage());
 		}
 		//wait for few seconds for the jobs to get added to the cache
 		Thread.sleep(1000);
         return mapping.findForward("viewResults");
     }
             
     
     private ComparativeGenomicQuery createCGHQuery(ComparativeGenomicForm comparativeGenomicForm, HttpSession session){
         UserListBeanHelper helper = new UserListBeanHelper(session); 
         //Create Query Objects
         ComparativeGenomicQuery cghQuery = (ComparativeGenomicQuery) QueryManager
                 .createQuery(QueryType.CGH_QUERY_TYPE);
         cghQuery.setQueryName(comparativeGenomicForm.getQueryName());
         String thisView = comparativeGenomicForm.getResultView();
         // Change this code later to get view type directly from Form !!
         if (thisView.equalsIgnoreCase("sample")) {
             cghQuery.setAssociatedView(ViewFactory
                     .newView(ViewType.CLINICAL_VIEW));
         } else if (thisView.equalsIgnoreCase("gene")) {
             cghQuery.setAssociatedView(ViewFactory
                     .newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
         }
         //Get InsitutionAccess
         Collection<InstitutionDE> accessInstitutions = InsitutionAccessHelper.getInsititutionCollection(session);
         
         // set Disease criteria
         DiseaseOrGradeCriteria diseaseOrGradeCriteria = comparativeGenomicForm
                 .getDiseaseOrGradeCriteria();
         if (!diseaseOrGradeCriteria.isEmpty()) {
             cghQuery.setDiseaseOrGradeCrit(diseaseOrGradeCriteria);
         }
        
         // Set gene criteria
         GeneIDCriteria geneIDCrit = comparativeGenomicForm.getGeneIDCriteria();
         
         //if(geneIDCrit.isEmpty() && comparativeGenomicForm.getGeneOption().equalsIgnoreCase("geneList")){
         if(geneIDCrit.isEmpty() && (comparativeGenomicForm.getGeneOption() != null && comparativeGenomicForm.getGeneOption().equalsIgnoreCase("geneList"))){
 			Collection<GeneIdentifierDE> genes = null;
 			UserList geneList = helper.getUserList(comparativeGenomicForm.getGeneFile());
 			if(geneList!=null){
 				try {	
 					//assumes geneList.getListSubType!=null && geneList.getListSubType().get(0) !=null
 					genes = ListConvertor.convertToGeneIdentifierDE(geneList.getList(), geneList.getListSubType()); //StrategyHelper.convertToSampleIDDEs(geneList.getList());
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(!genes.isEmpty()){
 					geneIDCrit.setGeneIdentifiers(genes);
 				}
 			}
 		}
 		
 		if (!geneIDCrit.isEmpty())	{
 			cghQuery.setGeneIDCrit(geneIDCrit);
 		}
         
         // Set all genes criteria
         AllGenesCriteria allGenesCrit = comparativeGenomicForm.getAllGenesCriteria();
 		if (!allGenesCrit.isEmpty())
 		    cghQuery.setAllGenesCrit(allGenesCrit);
         
         // Set sample Criteria
         SampleCriteria sampleIDCrit = comparativeGenomicForm.getSampleCriteria();
         Collection<SampleIDDE> sampleIds = null;
         if(sampleIDCrit.getSampleIDs()== null && comparativeGenomicForm.getSampleGroup()!=null && comparativeGenomicForm.getSampleGroup().equalsIgnoreCase("Upload")){
            UserList sampleList = helper.getUserList(comparativeGenomicForm.getSampleFile());
            if(sampleList!=null){
                try {
             	    Set<String>list = new HashSet<String>(sampleList.getList());
       				//get the samples associated with these specimens
       				List<String> samples = LookupManager.getSpecimenNames(list, accessInstitutions);
       				//Add back any samples that were just sampleIds to start with
       				if(samples != null){
       					list.addAll(samples);
       				}
       				sampleIds = ListConvertor.convertToSampleIDDEs(list);
                } catch (OperationNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(!sampleIds.isEmpty()){
                    sampleIDCrit.setSampleIDs(sampleIds);
                    sampleIDCrit.setSampleFile(comparativeGenomicForm.getSampleFile());
                    sampleIDCrit.setSampleGroup(comparativeGenomicForm.getSampleGroup());
                }
            }
        }
         
         
         if ( !sampleIDCrit.isEmpty() && comparativeGenomicForm.getSampleGroup()!=null && comparativeGenomicForm.getSampleGroup().equalsIgnoreCase("Specify")){
             sampleIDCrit.setSampleGroup(comparativeGenomicForm.getSampleGroup());
         }
         
         
 		if (!sampleIDCrit.isEmpty())
 		    cghQuery.setSampleIDCrit(sampleIDCrit);
 
         // set copy number or segmentMean criteria
 		if ( comparativeGenomicForm.getCopyNumberView().equals("calculatedCN") ) {
 	        CopyNumberCriteria CopyNumberCrit = comparativeGenomicForm.getCopyNumberCriteria();
 		    if (!CopyNumberCrit.isEmpty()) {
 		    	CopyNumberCrit.setCopyNumber(comparativeGenomicForm.getCopyNumber());
 		        cghQuery.setCopyNumberCrit(CopyNumberCrit);
 		    }
 		} else {
 	        SegmentMeanCriteria segmentMeanCrit = comparativeGenomicForm.getSegmentMeanCriteria();
 	        if (!segmentMeanCrit.isEmpty()) {
 	        	segmentMeanCrit.setSegmentMean(comparativeGenomicForm.getSegmentMean());
 	            cghQuery.setSegmentMeanCriteria(segmentMeanCrit);
 	        }
 		}
 		
         // set region criteria
         RegionCriteria regionCrit = comparativeGenomicForm.getRegionCriteria();
         if (!regionCrit.isEmpty()) {
         	regionCrit.setRegion(comparativeGenomicForm.getRegion());
             cghQuery.setRegionCrit(regionCrit);
         }
 
         // set clone/probe criteria
         CloneOrProbeIDCriteria cloneOrProbeIDCriteria = comparativeGenomicForm
                 .getCloneOrProbeIDCriteria();
         if (!cloneOrProbeIDCriteria.isEmpty()) {
             cghQuery.setCloneOrProbeIDCrit(cloneOrProbeIDCriteria);
         }
 
         // set snp criteria
         SNPCriteria snpCrit = comparativeGenomicForm.getSNPCriteria();
         if(snpCrit.isEmpty() && comparativeGenomicForm.getSnpId().equalsIgnoreCase("snpList")){
 			Collection<SNPIdentifierDE> snps = null;
 			UserList sList = helper.getUserList(comparativeGenomicForm.getSnpListFile());
 			if(sList!=null){
 				try {	
 					//assumes list.getListSubType!=null && list.getListSubType().get(0) !=null
 					snps = ListConvertor.convertToSNPIdentifierDE(sList.getList(), sList.getListSubType());
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(!snps.isEmpty()){
 					snpCrit.setSNPIdentifiers(snps);
 				}
 			}
 		}
         if (!snpCrit.isEmpty()) {
         	snpCrit.setSnpId(comparativeGenomicForm.getSnpId());
             cghQuery.setSNPCrit(snpCrit);
         }
      // set AnalysisType criteria
         String analysisType = comparativeGenomicForm.getAnalysisType();
         if (analysisType!= null) {
         	AnalysisTypeCriteria analysisTypeCriteri = new AnalysisTypeCriteria(AnalysisType.valueOf(analysisType));
             cghQuery.setAnalysisTypeCriteria(analysisTypeCriteri);
         }
 
         // set allele criteria
         AlleleFrequencyCriteria alleleFrequencyCriteria = comparativeGenomicForm
                 .getAlleleFrequencyCriteria();
         if (!alleleFrequencyCriteria.isEmpty()) {
             cghQuery.setAlleleFrequencyCrit(alleleFrequencyCriteria);
         }
 
         AssayPlatformCriteria assayPlatformCriteria = comparativeGenomicForm
                 .getAssayPlatformCriteria();
         if (!assayPlatformCriteria.isEmpty()) {
             cghQuery.setAssayPlatformCrit(assayPlatformCriteria);
         }
 	    else {
 			/*
 			 * This logic is required for an all genes query.  There
 			 * must be an AssayPlatformDE specified for the all gene's
 			 * query, and there was not one being created.  This is 
 			 * probably a hack as we may later allow the user to select
 			 * from the a list of platforms, and all could be the default.
 			 * --Dave
 			 */
 	    	assayPlatformCriteria = new AssayPlatformCriteria();
 	    	assayPlatformCriteria.setAssayPlatformDE(new AssayPlatformDE(Constants.AFFY_100K_SNP_ARRAY));
 	    	cghQuery.setAssayPlatformCrit(assayPlatformCriteria);
 		}
         
         
         
         return cghQuery;
     }
     
     protected Map getKeyMethodMap() {
 		 
       HashMap<String,String> map = new HashMap<String,String>();
       //Comparative Genomic Query Button using comparative genomic setup method
       map.put("ComparativeGenomicAction.setupButton", "setup");
       
       //Submit Query Button using comparative genomic submittal method
       map.put("buttons_tile.submittalButton", "submittal");
       
       //Preview Query Button using comparative genomic preview method
       map.put("buttons_tile.previewButton", "preview");
       
       //Submit All Genes Button using cgh submitAllGenes method
       map.put("buttons_tile.submitAllGenes", "submitAllGenes");
       
       //Submit Standard Button using cgh expression submitStandard method
       map.put("buttons_tile.submitStandard", "submitStandard");
       
       //Submit to get the cytobands of the selected chromosome
       map.put("ComparativeGenomicAction.getCytobands", "getCytobands");
       
       //Submit nothing if multiuse button entered if css turned off
       map.put("buttons_tile.multiUseButton", "multiUse");
       
       return map;
       
       }
     
     
 }

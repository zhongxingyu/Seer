 /*L
  * Copyright (c) 2006 SAIC, SAIC-F.
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/rembrandt/LICENSE.txt for details.
  */
 
 package gov.nih.nci.rembrandt.web.struts2.action;
 
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
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ChromosomeHelper;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.helper.ListConvertor;
 import gov.nih.nci.rembrandt.web.struts2.form.ComparativeGenomicForm;
 import gov.nih.nci.rembrandt.web.struts2.form.UIFormValidator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.ServletRequestAware;
 import org.apache.struts2.interceptor.SessionAware;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 
 
 
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
 
 public class ComparativeGenomicAction extends ActionSupport implements SessionAware, ServletRequestAware, Preparable { 
 
     private static Logger logger = Logger.getLogger(ComparativeGenomicAction.class);
     private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
     
     HttpServletRequest servletRequest;
     ComparativeGenomicForm form;
     
     
     Map<String, Object> sessionMap;
        
     
     @Override
 	public void prepare() throws Exception {
 		
     	if (form == null) {
 			form = new ComparativeGenomicForm();
 			form.reset(this.servletRequest);
 		}
 		
 	}
 
 	//if multiUse button clicked (with styles de-activated) forward back to page
 //    public String multiUse()
 //	throws Exception {
 //        //saveToken(request);
 //
 //    	return "backToCGH";
 //    }
     
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
     public String setup() {
 	
     	String sID = servletRequest.getHeader("Referer");
     	
     	// prevents Referer Header injection
     	if ( sID != null && sID != "" && !sID.contains("rembrandt")) {
     		return "failure";
     	}
 
 		//comparativeGenomicForm = comparativeGenomicFormInSession;
 		//this.geneExpressionForm = geneExpressionFormInSession;
 		
     	//Since Chromosomes is a static variable there is no need to set it twice.
 		//It is only a lookup option collection
 		if(form.getChromosomes()==null||form.getChromosomes().isEmpty()) {
 			//set the chromsomes list in the form 
 			logger.debug("Setup the chromosome values for the form");
 			form.setChromosomes(ChromosomeHelper.getInstance().getChromosomes());
 		}
         GroupRetriever groupRetriever = new GroupRetriever();
         form.setSavedSampleList(groupRetriever.getClinicalGroupsCollectionNoPath(servletRequest.getSession()));
         
         //saveToken(request);
         //sessionMap.put("comparativeGenomicForm", comparativeGenomicForm);
 		return "backToCGH";
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
     public String unspecified()
     throws Exception {
         
     	this.setup();
         //saveToken(request);
 
         return "backToCGH";
         
     }
     
     public String getCytobands()
     		throws Exception {
 
     	ComparativeGenomicForm cgForm = (ComparativeGenomicForm)form;
     	cgForm.validateForCytobands();
     	
     	//This is the static list of chromosomes that is fetched the first time it is needed
     	List chromosomes = cgForm.getChromosomes();
     	//IMPORTANT! geForm.chromosomeNumber is NOT the chromosome number.  It is the index
     	//into the static chromosomes list where the chromosome can be found.
     	if(!"".equals(cgForm.getChromosomeNumber())) {
     		ChromosomeBean bean = (ChromosomeBean)chromosomes.get(Integer.parseInt(cgForm.getChromosomeNumber()));
     		cgForm.setCytobands(bean.getCytobands());
     	}
 
     	//toggle the Gene / Region views correctly
     	cgForm.setSelectedView("regionView");
     	cgForm.setGeneRegionViewChange();
     	
     	return "backToCGH";
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
     public String submitAllGenes()
 	throws Exception {
         
     	//Shan: whatch impact
     	//if (!isTokenValid(request)) {
 		//	return mapping.findForward("failure");
 		//}
         
         this.servletRequest.getSession().setAttribute("currentPage", "0");
 		this.servletRequest.getSession().removeAttribute("currentPage2");
 		
 		//ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) comparitiveGenomicForm;
 		                       
         //set all Genes query and give copyNumber default value
 		this.form.setSampleType("PairedTissue");
 		form.setGeneGroup("");
 		form.setCopyNumberView("calculatedCN");
 		form.setGeneRegionView("geneView");
 		form.setCopyNumber("amplified");
 		form.setCnAmplified(RembrandtConstants.ALL_GENES_COPY_NUMBER_REGULATION);
 		form.setCnADAmplified(RembrandtConstants.ALL_GENES_COPY_NUMBER_REGULATION);
 		form.setCnADDeleted("1");
 		form.setCnDeleted("1");
 
 		form.setSegmentMean("");
 		form.setSmAmplified("");
 		form.setSmDeleted("");
          
 		logger.debug("This is an All Genes cgh Submital");
        
 		//resetToken(request);
 
 		return "showAllGenes";
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
     public String submitStandard()
 	throws Exception {
         this.servletRequest.getSession().setAttribute("currentPage", "0");
 		this.servletRequest.getSession().removeAttribute("currentPage2");
 		
 		//ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
 		////set standard query and clear copyNumber default value
 		form.setCopyNumber("");
 		form.setCnAmplified("");
 		form.setCnADAmplified("");
 		form.setCnADDeleted("");
 		form.setCnDeleted("");
 
 		form.setSegmentMean("");
 		form.setSmAmplified("");
 		form.setSmDeleted("");
 		
 		logger.debug("This is an Standard cgh Submital");
 
 		return "backToCGH";
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
     public String submittal()
             throws Exception {
        
     	if (validateForSubmitOrPreview() != 0)
     		return "backToCGH";
    	
    	setDataFormDetails();   
         
         this.servletRequest.getSession().setAttribute("currentPage", "0");
         this.servletRequest.getSession().removeAttribute("currentPage2");
         String sessionId = this.servletRequest.getSession().getId();
         
         //ComparativeGenomicForm comparativeGenomicForm = (ComparativeGenomicForm) form;
         
         /*The following 15 lines of code/logic will eventually need to be moved/re-organized. All Genes queries should have their own actions, forms, etc. For
    	  * now, in order to properly validate an all genes query and STILL be able to forward back to
    	  * the appropriate query page (tile order), this logic has been placed in the action itself, so
    	  * that there can be proper forwarding when errors are generated from an all genes submission.
    	  * This logic checks if the query is an all genes query and then if the copy number value is
    	  * less than 10 for amplified and greater than 1 for deletion. If it is less than 10 for amp and greater than 1 for deleted,
    	  * an error is created and sent with the request to the forward.
    	  * BEGINS HERE!!
    	  */
    		   if (form.getIsAllGenes()){
    		       try{
    		        int intCnAmplified = Integer.parseInt(form.getCnAmplified());
    		        float floatCnDeleted = Float.parseFloat(form.getCnDeleted());
    		        int intCnADAmplified = Integer.parseInt(form.getCnADAmplified());
    		        float floatCnADDeleted = Float.parseFloat(form.getCnADDeleted());
    			        if((intCnAmplified < 10 && form.getCopyNumber().equalsIgnoreCase("amplified")) || 
    			             (intCnADAmplified < 10 && form.getCopyNumber().equalsIgnoreCase("ampdel"))){
    			            
    			        	//ActionErrors errors = new ActionErrors();
 			            //errors.add("copyNumberAllGenesAmp", new ActionError(
 						//"gov.nih.nci.nautilus.ui.struts.form.copyNumberAmp.allGenes.error")); 
 			            //this.saveErrors(request, errors);
 					    String error = ApplicationContext.getLabelProperties().getProperty(
 					    		"gov.nih.nci.nautilus.ui.struts.form.copyNumberAmp.allGenes.error");
 			            addFieldError("copyNumberAllGenesAmp", error);
 			            return "showAllGenes"; 
    			        }
    			        if((floatCnDeleted > 1 && form.getCopyNumber().equalsIgnoreCase("deleted")) ||
    			             (floatCnADDeleted > 1 && form.getCopyNumber().equalsIgnoreCase("ampdel"))) {
    			            
    			            String error = ApplicationContext.getLabelProperties().getProperty(
 					    		"gov.nih.nci.nautilus.ui.struts.form.copyNumberDel.allGenes.error");
    			            addFieldError("copyNumberAllGenesDel", error);
    					    return "showAllGenes"; 
    			        }
    		     } catch (NumberFormatException ex) {
 		            
 		            String error = ApplicationContext.getLabelProperties().getProperty(
 				    		"gov.nih.nci.nautilus.ui.struts.form.copyNumberDel.allGenes.error");
 		            addFieldError("copyNumberAllGenesDel", error);
 				    return "showAllGenes";    
   		    }
         }
    		  //All Genes validation ENDS HERE  
    		
         
         logger.debug("This is a Comparative Genomic Submittal");
         //Create Query Objects
         ComparativeGenomicQuery cghQuery = createCGHQuery(form, this.servletRequest.getSession());
         
         //Check user credentials and constrain query by Institutions
         if(cghQuery != null){
         	cghQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(this.servletRequest.getSession()));
             }
         
         //This is required as struts resets the form.  It is later added back to the request
         this.servletRequest.setAttribute("previewForm", form.cloneMe());
        
         
             try {
                 //Store the query in the SessionQueryBag
                 if (!cghQuery.isEmpty()) {
                     SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
                     queryBag.putQuery(cghQuery, form);
                     presentationTierCache.putSessionQueryBag(sessionId, queryBag);
                 } else {                    
                     addActionError(ApplicationContext.getLabelProperties().getProperty(
 				    		"gov.nih.nci.nautilus.ui.struts.form.query.cgh.error"));
                     return "backToCGH";
                 }
             }
             catch (Exception e) {
                logger.error(e);
             }
            
             
             //resetToken(request);
 
             return "advanceSearchMenu";
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
     public String preview()
             throws Exception {
         
    	if (validateForSubmitOrPreview() != 0)
    		return "backToCGH";
    	
     	setDataFormDetails();
     	
         this.servletRequest.getSession().setAttribute("currentPage", "0");
         this.servletRequest.getSession().removeAttribute("currentPage2");
         
         logger.debug("This is a Comparative Genomic Preview");
         
         ComparativeGenomicQuery cghQuery = createCGHQuery(form,this.servletRequest.getSession());
         if(cghQuery != null)
         	cghQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(this.servletRequest.getSession()));
             
         //This is required as struts resets the form.  It is later added back to the request
         
         //Shan: don't know what's this for. To delet...
         //this.servletRequest.setAttribute("previewForm", form.cloneMe());
         
         CompoundQuery compoundQuery = new CompoundQuery(cghQuery);
         compoundQuery.setQueryName(RembrandtConstants.PREVIEW_RESULTS);
         logger.debug("Setting query name to:"+compoundQuery.getQueryName());
         
         compoundQuery.setAssociatedView(cghQuery.getAssociatedView());
         logger.debug("Associated View for the Preview:"+compoundQuery.getAssociatedView().getClass());
 	    
         //Save the sessionId that this preview query is associated with
         compoundQuery.setSessionId(this.servletRequest.getSession().getId());
                 
         RembrandtAsynchronousFindingManagerImpl asynchronousFindingManagerImpl = new RembrandtAsynchronousFindingManagerImpl();
         try {
 			asynchronousFindingManagerImpl.submitQuery(this.servletRequest.getSession(), compoundQuery);
 		} catch (FindingsQueryException e) {
 			logger.error(e.getMessage());
 		}
 		
         //wait for few seconds for the jobs to get added to the cache
 		Thread.sleep(1000);
         
 		//resetToken(request);
 
         return "showResults";
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
         
         
         if (comparativeGenomicForm.getSampleGroup()!=null && comparativeGenomicForm.getSampleGroup().equalsIgnoreCase("Specify")){
         	comparativeGenomicForm.setSampleList(comparativeGenomicForm.getSampleList());
      	    sampleIDCrit = comparativeGenomicForm.getSampleCriteria();
             sampleIDCrit.setSampleGroup(comparativeGenomicForm.getSampleGroup());
         }
         
         
 		if (sampleIDCrit != null && !sampleIDCrit.isEmpty())
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
 
 	@Override
 	public void setServletRequest(HttpServletRequest arg0) {
 		// TODO Auto-generated method stub
 		servletRequest = arg0;
 		
 	}
 
 //	public ComparativeGenomicForm getComparativeGenomicForm() {
 //		return comparativeGenomicForm;
 //	}
 //
 //	public void setComparativeGenomicForm(
 //			ComparativeGenomicForm comparativeGenomicForm) {
 //		this.comparativeGenomicForm = comparativeGenomicForm;
 //	}
 
 	public HttpServletRequest getServletRequest() {
 		return servletRequest;
 	}
 
 
 	@Override
 	public void setSession(Map<String, Object> arg0) {
 		// TODO Auto-generated method stub
 		sessionMap = arg0;
 	}
 	
 	protected void setDataFormDetails() {
 		this.form.setGeneListDetails();
 		this.form.setGeneOptionDetails();
 		
 		this.form.setCytobandRegionStartDetails();
 		this.form.setCytobandRegionEndDetails();
 		this.form.setCloneListSpecifyDetails();
 		this.form.setBasePairStartDetails();
 		this.form.setBasePairEndDetails();
 		
 		this.form.setCnAmplifiedDetails();
 		this.form.setSmAmplifiedDetails();
 		this.form.setCloneListFileDetails();
 		this.form.setSnpListSpecifyDetails();
 		this.form.setCnADAmplifiedDetails();
 		this.form.setCnADDeletedDetails();
 		this.form.setCnUnchangeToDetails();
 		this.form.setSmUnchangeToDetails();
 		this.form.setCnDeletedDetails();
 		this.form.setSmDeletedDetails();
 		this.form.setCnUnchangeFromDetails();
 		this.form.setSmUnchangeFromDetails();
 		
 	}
 
 	public ComparativeGenomicForm getForm() {
 		return form;
 	}
 
 	public void setForm(ComparativeGenomicForm form) {
 		this.form = form;
 	}
 	
 	/**
 	 * Validate input data for "preview" or "submit" action
 	 * 
 	 * 4 items to check on:
 	 * 
 	 * 1). Query Name
 	 * 2). Chromosome data: chromosome numnber, cytoband start and end, base pair start and end
 	 * 3). Copy number or segment mean
 	 * 4). CGH Query
 	 * 
 	 * @return 0 if validated. -1 if validation failed
 	 */
 	public int validateForSubmitOrPreview() {
     	
 		if (validateQueryName() == 0) {
 			if (validateChromosomeInputData() == 0)
 				if (validateCopyNumberorSementMean() == 0)
 					return validateCGHQuery();
		} 
 		
		return -1; //error
 	}	
     
 	protected int validateChromosomeInputData() {
     	List<String> errors = new ArrayList<String>();
     	errors = UIFormValidator.validateChromosomalRegion(this.form.getChromosomeNumber(), 
     			this.form.getRegion(), this.form.getCytobandRegionStart(), 
     			this.form.getBasePairStart(), this.form.getBasePairEnd(), errors);
     	if (errors.size() > 0) {
     		for (String error : errors) {
     			addFieldError("chromosome", error);
     		}
     		return -1;
     	}	
     	
     	return 0;
 	}
 	
 	protected int validateQueryName() {
 		
 		String error = UIFormValidator.validateQueryName(this.form.getQueryName());
     	if (error != null) {
     		addFieldError("queryName", error);
     		return -1;
     	}
     	
     	return 0;
 	}
 	
 	protected int validateCopyNumberorSementMean() {
 
     	// validate copy number or segment mean
     	int errorCode = 0;
     	String error = null;
     	if ( this.form.getCopyNumberView().equals("calculatedCN") ) {
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"ampdel",this.form.getCnADAmplified());
     		if (error != null) {
     			addFieldError("cnADAmplified", error);
     			errorCode = -1; 
     		}
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"ampdel",this.form.getCnADDeleted());
     		if (error != null) 
     			addFieldError("cnADDeleted", error);
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"amplified",this.form.getCnAmplified());
     		if (error != null) {
     			addFieldError("cnAmplified", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"deleted",this.form.getCnDeleted());
     		if (error != null)  {
     			addFieldError("cnDeleted", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"unchange",this.form.getCnUnchangeFrom());
     		if (error != null) {
     			addFieldError("cnUnchangeFrom", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateCopyNo(this.form.getCopyNumber(),"unchange",this.form.getCnUnchangeTo());
     		if (error != null) {
     			addFieldError("cnUnchangeTo", error);
     			errorCode = -1;
     		}
     		
     	} else { // validate segment mean
     		error = UIFormValidator.validateSegmentMean(this.form.getSegmentMean(),"amplified", this.form.getSmAmplified());
     		if (error != null) {
     			addFieldError("smAmplified", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateSegmentMean(this.form.getSegmentMean(),"deleted",this.form.getSmDeleted());
     		if (error != null) {
     			addFieldError("smDeleted", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateSegmentMean(this.form.getSegmentMean(),"unchange",this.form.getSmUnchangeFrom());
     		if (error != null) {
     			addFieldError("smUnchangeFrom", error);
     			errorCode = -1;
     		}
     		
     		error = UIFormValidator.validateSegmentMean(this.form.getSegmentMean(),"unchange",this.form.getSmUnchangeTo());
     		if (error != null) {
     			addFieldError("smUnchangeTo", error);
     			errorCode = -1;
     		}
     	}
     	
     	return errorCode;
 	}
 
 	protected int validateCGHQuery() {	
 		String queryName = this.form.getQueryName();
 		String geneOption = this.form.getGeneOption();
 		String geneList = this.form.getGeneList().trim();
 		String chromosome = this.form.getChromosomeNumber().trim();
 		
 		// Validate minimum criteria's for CGH Query
 		if (queryName != null && queryName.length() >= 1 && 
 				(geneOption != null && geneOption.equalsIgnoreCase("standard"))) {
			if ((geneList == null || geneList.length() == 0)
					&& (chromosome == null || (chromosome.length() > 0 && chromosome.equals("-1")))) {
 				String msg = ApplicationContext.getLabelProperties().getProperty("gov.nih.nci.nautilus.ui.struts.form.cgh.minimum.error");
 				addActionError(msg);
 				return -1;
 			}
 		}
 
 		return 0;
 	}
 
 }
     

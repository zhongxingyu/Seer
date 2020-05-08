 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.critieria.AgeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.ChemoAgentCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GenderCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.KarnofskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.LanskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.MRIClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.NeuroExamClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OccurrenceCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudyChemoAgentCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudyRadiationTherapyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudySurgeryOutcomeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudySurgeryTitleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.PriorSurgeryTitleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RaceCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RadiationTherapyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurgeryOutcomeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurvivalCriteria;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.caintegrator.studyQueryService.FindingsManager;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.service.findings.RembrandtAsynchronousFindingManagerImpl;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ChromosomeHelper;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.helper.ListConvertor;
 import gov.nih.nci.rembrandt.web.helper.ReportGeneratorHelper;
 import gov.nih.nci.rembrandt.web.struts.form.ClinicalDataForm;
 import gov.nih.nci.rembrandt.web.struts.form.GeneExpressionForm;
 
 
 import java.util.ArrayList;
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
 
 public class ClinicalDataAction extends LookupDispatchAction {
     private static Logger logger = Logger.getLogger(ClinicalDataAction.class);
     private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
    //if multiUse button clicked (with styles de-activated) forward back to page
     public ActionForward multiUse(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
 		
 		return mapping.findForward("backToClinical");
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
     
     public ActionForward setup(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
         ClinicalDataForm clinicalDataForm = (ClinicalDataForm) form;
         GroupRetriever groupRetriever = new GroupRetriever();
         clinicalDataForm.setSavedSampleList(groupRetriever.getClinicalGroupsCollectionNoPath(request.getSession()));
         return mapping.findForward("backToClinical");
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
         
         return mapping.findForward("backToClinical");
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
     public ActionForward submittal(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         request.getSession().setAttribute("currentPage", "0");
         request.getSession().removeAttribute("currentPage2");
         String sessionId = request.getSession().getId();
         ClinicalDataForm clinicalDataForm = (ClinicalDataForm) form;
         logger.debug("This is a Clinical Data Submittal");
         
         //Create Query Objects
         ClinicalDataQuery clinicalDataQuery = createClinicalDataQuery(clinicalDataForm, request.getSession());
         
         //Check user credentials and constrain query by Institutions
         if(clinicalDataQuery != null){
             clinicalDataQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(request.getSession()));
             }        
         
         if (!clinicalDataQuery.isEmpty()) {
         	SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
             queryBag.putQuery(clinicalDataQuery, clinicalDataForm);
             presentationTierCache.putSessionQueryBag(sessionId, queryBag);
         }else{
             ActionErrors errors = new ActionErrors();
             ActionError error = new ActionError("gov.nih.nci.nautilus.ui.struts.form.query.cgh.error");
             errors.add(ActionErrors.GLOBAL_ERROR,error);
             this.saveErrors(request, errors);
             return mapping.findForward("backToClinical");
         }
         return mapping.findForward("advanceSearchMenu");
     }
 
     
     /**
      * 
      * This method is called when preview button is clicked on the clincial query page
      */
     public ActionForward preview(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         request.getSession().setAttribute("currentPage", "0");
         request.getSession().removeAttribute("currentPage2");
                 
         ClinicalDataForm clinicalDataForm = (ClinicalDataForm) form;
         
         logger.debug("This is a Clinical Data Preview");
         //Create Query Objects
         ClinicalDataQuery clinicalDataQuery = createClinicalDataQuery(clinicalDataForm, request.getSession());
         if(clinicalDataQuery != null){
             clinicalDataQuery.setInstitutionCriteria(InsitutionAccessHelper.getInsititutionCriteria(request.getSession()));
             }
 
         request.setAttribute("previewForm",clinicalDataForm.cloneMe());
         CompoundQuery compoundQuery = new CompoundQuery(clinicalDataQuery);
         compoundQuery.setQueryName(RembrandtConstants.PREVIEW_RESULTS);
         logger.debug("Setting query name to:"+compoundQuery.getQueryName());
         compoundQuery.setAssociatedView(ViewFactory
                 .newView(ViewType.CLINICAL_VIEW));
         logger.debug("Associated View for the Preview:"+compoundQuery.getAssociatedView().getClass());
 	    //Save the sessionId that this preview query is associated with
         compoundQuery.setSessionId(request.getSession().getId());
         //Generate the reportXML for the preview.  It will be stored in the session
 	    //cache for later retrieval
         //ReportGeneratorHelper reportHelper = new ReportGeneratorHelper(compoundQuery,new HashMap());
         //return mapping.findForward("previewReport");
         RembrandtAsynchronousFindingManagerImpl asynchronousFindingManagerImpl = new RembrandtAsynchronousFindingManagerImpl();
         try {
 			asynchronousFindingManagerImpl.submitQuery(request.getSession(), compoundQuery);
 		} catch (FindingsQueryException e) {
 			logger.error(e.getMessage());
 		}
 		//wait for few seconds for the jobs to get added to the cache
 		Thread.sleep(2000);
         return mapping.findForward("viewResults");
 	}
        
     /**
      * 
      * private method used to create clincial queries
      */
     private ClinicalDataQuery createClinicalDataQuery(ClinicalDataForm clinicalDataForm, HttpSession session){
         UserListBeanHelper helper = new UserListBeanHelper(session); 
         String thisView = clinicalDataForm.getResultView();
         // Create Query Objects
         ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager
                 .createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
         clinicalDataQuery.setQueryName(clinicalDataForm.getQueryName());
 
         // Change this code later to get view type directly from Form !!
         if (thisView.equalsIgnoreCase("sample")) {
             clinicalDataQuery.setAssociatedView(ViewFactory
                     .newView(ViewType.CLINICAL_VIEW));
           //clinicalDataQuery.setAssociatedView(ViewFactory.newView(ViewType.SAMPLE_VIEW_TYPE));
         } else if (thisView.equalsIgnoreCase("gene")) {
             clinicalDataQuery.setAssociatedView(ViewFactory
                     .newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
             //clinicalDataQuery.setAssociatedView(ViewFactory.newView(ViewType.GENE_VIEW_TYPE));
         }
         
         // Set sample Criteria
         SampleCriteria sampleIDCrit = clinicalDataForm.getSampleCriteria();
         Set<SampleIDDE> sampleIds = new HashSet<SampleIDDE>();
         if(sampleIDCrit.isEmpty() && clinicalDataForm.getSampleGroup().equalsIgnoreCase("Upload")){
            UserList sampleList = helper.getUserList(clinicalDataForm.getSampleFile());
            if(sampleList!=null){
                try {
             	List<String> specimenNames = sampleList.getList();
    				//get the samples associated with these specimens
    				List<String> samples = LookupManager.getSampleIDs(specimenNames);
    				//Add back any samples that were just sampleIds to start with
    				if(samples != null){
    					specimenNames.addAll(samples);
    				}
    				sampleIds.addAll(ListConvertor.convertToSampleIDDEs(specimenNames));
                } catch (OperationNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(!sampleIds.isEmpty()){
                    sampleIDCrit.setSampleFile(clinicalDataForm.getSampleFile());
                    sampleIDCrit.setSampleGroup(clinicalDataForm.getSampleGroup());
                    sampleIDCrit.setSampleIDs(sampleIds);
                }
            }
        
        }
        if ( !sampleIDCrit.isEmpty() && clinicalDataForm.getSampleGroup()!=null && clinicalDataForm.getSampleGroup().equalsIgnoreCase("Specify")){
             sampleIDCrit.setSampleGroup(clinicalDataForm.getSampleGroup());
         }
         
		if (!sampleIDCrit.isEmpty())
 		    clinicalDataQuery.setSampleIDCrit(sampleIDCrit);
 
         // Set disease criteria
         DiseaseOrGradeCriteria diseaseOrGradeCrit = clinicalDataForm
                 .getDiseaseOrGradeCriteria();      
         if (!diseaseOrGradeCrit.isEmpty()) {
             clinicalDataQuery.setDiseaseOrGradeCrit(diseaseOrGradeCrit);
         }
 
         // Set Occurrence criteria
         OccurrenceCriteria occurrenceCrit = clinicalDataForm
                 .getOccurrenceCriteria();
         if (!occurrenceCrit.isEmpty()) {
             clinicalDataQuery.setOccurrenceCrit(occurrenceCrit);
         }
 
         // Set RadiationTherapy criteria
         RadiationTherapyCriteria radiationTherapyCrit = clinicalDataForm
                 .getRadiationTherapyCriteria();
         if (!radiationTherapyCrit.isEmpty()) {
             clinicalDataQuery.setRadiationTherapyCrit(radiationTherapyCrit);
         }
 
         //Set ChemoAgent Criteria
         ChemoAgentCriteria chemoAgentCrit = clinicalDataForm
                 .getChemoAgentCriteria();
         if (!chemoAgentCrit.isEmpty()) {
             clinicalDataQuery.setChemoAgentCrit(chemoAgentCrit);
         }
 
 
         // Set SurgeryType Criteria
         SurgeryOutcomeCriteria surgeryOutcomeCrit = clinicalDataForm.getSurgeryOutcomeCriteria();
         if (!surgeryOutcomeCrit.isEmpty()) {
             clinicalDataQuery.setSurgeryOutcomeCrit(surgeryOutcomeCrit);
         }
 
 
         // Set SurgeryTitle Criteria
         PriorSurgeryTitleCriteria priroSurgeryTitleCrit = clinicalDataForm.getPriorSurgeryTitleCriteria();
         if (!priroSurgeryTitleCrit.isEmpty()) {
             clinicalDataQuery.setPriorSurgeryTitleCrit(priroSurgeryTitleCrit);
         }
         
         
         //Set onStudyChemoAgentCrit Criteria
         OnStudyChemoAgentCriteria onStudyChemoAgentCrit = clinicalDataForm
                 .getOnStudyChemoAgentCriteria();
         if (!onStudyChemoAgentCrit.isEmpty()) {
             clinicalDataQuery.setOnStudyChemoAgentCriteria(onStudyChemoAgentCrit);
         }
 
         
         // Set OnStudyRadiationTherapy criteria
         OnStudyRadiationTherapyCriteria onStudyRadiationTherapyCrit = clinicalDataForm
                 .getOnStudyRadiationTherapyCriteria();
         if (!onStudyRadiationTherapyCrit.isEmpty()) {
             clinicalDataQuery.setOnStudyRadiationTherapyCriteria(onStudyRadiationTherapyCrit);
         }
         
         
         // Set OnStudySurgeryType Criteria
         OnStudySurgeryOutcomeCriteria onStudySurgeryOutcomeCrit = clinicalDataForm.getOnStudySurgeryOutcomeCriteria();
         if (!onStudySurgeryOutcomeCrit.isEmpty()) {
             clinicalDataQuery.setOnStudySurgeryOutcomeCriteria(onStudySurgeryOutcomeCrit);
         }
         
         // Set OnStudySurgeryTitle Criteria
         OnStudySurgeryTitleCriteria onStudySurgeryTitleCrit = clinicalDataForm.getOnStudySurgeryTitleCriteria();
         if (!onStudySurgeryTitleCrit.isEmpty()) {
             clinicalDataQuery.setOnStudySurgeryTitleCriteria(onStudySurgeryTitleCrit);
         }
         // Set Survival Criteria
         SurvivalCriteria survivalCrit = clinicalDataForm.getSurvivalCriteria();
         if (!survivalCrit.isEmpty()) {
             clinicalDataQuery.setSurvivalCrit(survivalCrit);
         }
         
        
 
         // Set Age Criteria
         AgeCriteria ageCrit = clinicalDataForm.getAgeCriteria();
         if (!ageCrit.isEmpty()) {
             clinicalDataQuery.setAgeCrit(ageCrit);
         }
 
         // Set gender Criteria
         GenderCriteria genderCrit = clinicalDataForm.getGenderCriteria();
         if (!genderCrit.isEmpty()) {
             clinicalDataQuery.setGenderCrit(genderCrit);
         }
         
         // Set race Criteria
         RaceCriteria raceCrit = clinicalDataForm.getRaceCriteria();
         if (!raceCrit.isEmpty()) {
             clinicalDataQuery.setRaceCrit(raceCrit);
         }
         
        // set KarnofskyClinicalEvalCriteria 
     	KarnofskyClinicalEvalCriteria karnofskyCriteria = clinicalDataForm.getKarnofskyCriteria();
     	 if (!karnofskyCriteria.isEmpty()) {
              clinicalDataQuery.setKarnofskyCriteria(karnofskyCriteria);
          }
     	
     	 // set lanskyCriteria 
     	 LanskyClinicalEvalCriteria lanskyCriteria = clinicalDataForm.getLanskyCriteria();
      	 if (!lanskyCriteria.isEmpty()) {
               clinicalDataQuery.setLanskyCriteria(lanskyCriteria);
           }
 
      	 // set lanskyCriteria 
     	 MRIClinicalEvalCriteria mriCriteria = clinicalDataForm.getMriCriteria();
      	 if (!mriCriteria.isEmpty()) {
               clinicalDataQuery.setMriCriteria(mriCriteria);
           }
      	 
      	 // set NeuroExamClinicalEvalCriteria 
      	 NeuroExamClinicalEvalCriteria neuroExamCriteria = clinicalDataForm.getNeuroExamCriteria();
      	 if (!neuroExamCriteria.isEmpty()) {
               clinicalDataQuery.setNeuroExamCriteria(neuroExamCriteria);
           }
      	 
         return clinicalDataQuery;
     }
     
     protected Map getKeyMethodMap() {
 		 
      HashMap<String,String> map = new HashMap<String,String>();
      
      
      
      //Submit Query Button using clinical submittal method
      map.put("buttons_tile.submittalButton", "submittal");
      
      //Preview Query Button using clinical preview method
      map.put("buttons_tile.previewButton", "preview");
      
      //setup clinical query
      map.put("ClinicalDataAction.setupButton", "setup");
      
      //Submit nothing if multiuse button entered if css turned off
      map.put("buttons_tile.multiUseButton", "multiUse");
      
      return map;
      
      }
 
 
 
 
 
 
 }

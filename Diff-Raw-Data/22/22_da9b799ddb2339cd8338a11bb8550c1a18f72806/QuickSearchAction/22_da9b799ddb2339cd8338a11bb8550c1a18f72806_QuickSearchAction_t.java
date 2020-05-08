 package gov.nih.nci.rembrandt.web.struts.action;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.critieria.InstitutionCriteria;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SNPIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.enumeration.GeneExpressionDataSetType;
 import gov.nih.nci.caintegrator.ui.graphing.data.kaplanmeier.KaplanMeierDataController;
 import gov.nih.nci.caintegrator.ui.graphing.data.kaplanmeier.KaplanMeierSampleInfo;
 import gov.nih.nci.caintegrator.ui.graphing.data.kaplanmeier.KaplanMeierStoredData;
 import gov.nih.nci.caintegrator.util.CaIntegratorConstants;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.queryservice.resultset.kaplanMeierPlot.KMPlotManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.kaplanMeierPlot.KaplanMeierPlotContainer;
import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.helper.KMDataSetHelper;
 import gov.nih.nci.rembrandt.web.helper.ListConvertor;
 import gov.nih.nci.rembrandt.web.helper.SampleBasedQueriesRetriever;
 import gov.nih.nci.rembrandt.web.struts.form.KMDataSetForm;
 import gov.nih.nci.rembrandt.web.struts.form.QuickSearchForm;
 import gov.nih.nci.rembrandt.web.struts.form.UIFormValidator;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 
 
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
 
 public class QuickSearchAction extends DispatchAction {
 	private static Logger logger = Logger.getLogger(QuickSearchAction.class);
 	private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
 	/**
 	 * Method execute
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
 	private ActionForward doGeneExpPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		/*
 		GeneExpressionGraphGenerator generator = new GeneExpressionGraphGenerator(
 				form);
 		if (generator.getErrors().size() > 0) {
 			this.saveErrors(request, generator.getErrors());
 			return mapping.findForward("badgraph");
 		}
 		generator.setRequestAttributes(request);
 		generator.setSessionAttributes(request.getSession(true));
 		return mapping.findForward("histogram");
 		*/
 		
 		//		old school
 		//GeneExpressionGraphGenerator generator = new GeneExpressionGraphGenerator(form);
 		/*
 		if (generator.getErrors().size() > 0) {
 			this.saveErrors(request, generator.getErrors());
 			return mapping.findForward("badgraph");
 		}
 		*/
 		
 		//we will handle the error elsewhere, so no need to findForward("badgraph")
 		QuickSearchForm qsForm = (QuickSearchForm) form;
 		//need this to pass the geneSymbol to the JSP
		//request.setAttribute("geneSymbol", qsForm.getGeneSymbol());
		
		request.setAttribute("geneSymbol", MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, qsForm.getGeneSymbol()));
 		
         //generator.setRequestAttributes(request);
         //generator.setSessionAttributes(request.getSession(true));
         return mapping.findForward("histogram");
 		
 	}
 
 	/***************************************************************************
 	 * This method gathers the input parameters from the quickSearchForm and
 	 * creates and forwards the request to the KMPlot jsp.
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public ActionForward doKMPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		String sessionId = request.getSession().getId();
 		KMDataSetForm kmForm = (KMDataSetForm) form;
 		InstitutionCriteria institutionCriteria = InsitutionAccessHelper.getInsititutionCriteria(request.getSession());
 		ActionErrors errors = new ActionErrors();
		String quickSearchVariableName = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("quickSearchName")));
 		if (quickSearchVariableName != null) {
 			quickSearchVariableName = quickSearchVariableName.toUpperCase();
 		}
        
		String quickSearchType = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("quickSearchType")));
 		if (quickSearchType == null) {
 			quickSearchType = RembrandtConstants.GENE_SYMBOL;
 		}
 		double upFold = kmForm.getUpFold();
 		double downFold = kmForm.getDownFold();
 		//TEMP COMENTED OUT
		String kmplotType = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("plotType")));
		String qsGroupName = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("quickSearchGroupName")));
		String qsGroupNameCompare = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("quickSearchGroupNameCompare")));
 		//String kmplotType = CaIntegratorConstants.SAMPLE_KMPLOT;
 
        	kmForm.setPlotType(kmplotType);
 		KaplanMeierPlotContainer kmResultsContainer = null;
 		KaplanMeierSampleInfo[] kmSampleInfos = {new KaplanMeierSampleInfo(0,0,0)};
 		
 		//		see if we are constraining by a group of samples
 		List<SampleIDDE> constrainSamples = null;
 		// ^ first time thru we arent constraining, so pass this as null for now - may need to implement later if new reqts
 		
 		UserListBeanHelper helper = new UserListBeanHelper(request.getSession());
 		
		String baselineGroup = request.getParameter("baselineGroup")!=null ? MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String)request.getParameter("baselineGroup"))) : "ALL GLIOMA";
 
 		UserList constrainSamplesUl = helper.getUserList(baselineGroup);
 		try	{
 			List<String>specimenNames = constrainSamplesUl.getList();
 			constrainSamples = convertToSpecimenList(specimenNames);
 		}
 		catch(Exception e){
 			logger.warn("GROUP " + qsGroupName + " NOT FOUND"+ e.getMessage());
 		}
 		
 		
 		if (kmplotType.equals(CaIntegratorConstants.GENE_EXP_KMPLOT)) {			
             kmResultsContainer = performKMGeneExpressionQuery(constrainSamples, quickSearchVariableName, GeneExpressionDataSetType.GeneExpressionDataSet, institutionCriteria);
            	if(kmResultsContainer!=null) {
            		List reporters = kmResultsContainer.getAssociatedGEReportersSortedByMeanIntensity();
            		if(reporters != null  && reporters.size() > 0){
            			String highestIntensityReporter = (String) reporters.get(0);
            			if (highestIntensityReporter.contains(CaIntegratorConstants.HIGHEST_GEOMETRIC_MEAN_INTENSITY)){
 						int pos = highestIntensityReporter.indexOf(CaIntegratorConstants.HIGHEST_GEOMETRIC_MEAN_INTENSITY);
 						highestIntensityReporter = highestIntensityReporter.substring(0,pos);
 					} else if (reporters.size() == 1 && highestIntensityReporter.contains(CaIntegratorConstants.LOWEST_GEOMETRIC_MEAN_INTENSITY)) {
 						int pos = highestIntensityReporter.indexOf(CaIntegratorConstants.LOWEST_GEOMETRIC_MEAN_INTENSITY);
 						highestIntensityReporter = highestIntensityReporter.substring(0,pos);
 					}
     				kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(highestIntensityReporter);
            		}
 				if(kmResultsContainer.getGeneSymbol()!= null){
 					kmForm.setGeneOrCytoband(kmResultsContainer.getGeneSymbol().getValue().toString());
 				}
 			}
 			kmForm.setUpOrAmplified("Up Regulated");
 			kmForm.setDownOrDeleted("Down Regulated");			
 			kmForm.setPlotVisible(true);
 		} else if (kmplotType.equals(CaIntegratorConstants.COPY_NUMBER_KMPLOT)) {
 			kmForm.setUpOrAmplified("Amplified");
 			kmForm.setDownOrDeleted("Deleted");
 			if(quickSearchType.equals(RembrandtConstants.GENE_SYMBOL)){
 			   kmResultsContainer = performKMCopyNumberQuery(constrainSamples, quickSearchVariableName, quickSearchType, institutionCriteria);
 			   if(kmResultsContainer != null  && kmResultsContainer.getCytobandDE()!= null){
 				   kmSampleInfos = kmResultsContainer.getMedianKMPlotSamples();
 				   String cytobandGeneSymbol = kmResultsContainer.getCytobandDE().getValue().toString();
 				   kmForm.setGeneOrCytoband(quickSearchVariableName+"("+cytobandGeneSymbol+")");
 				   kmForm.setPlotVisible(true); 
 			   }
 			 }else if(quickSearchType.equals(RembrandtConstants.SNP_PROBESET_ID)){
 				 kmResultsContainer = performKMCopyNumberQuery(constrainSamples, quickSearchVariableName, quickSearchType, institutionCriteria);
 				 if(kmResultsContainer != null){
 					 kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(quickSearchVariableName);
 					 kmForm.setGeneOrCytoband(quickSearchVariableName); 
 					 kmForm.setPlotVisible(true); 
 				 }
 			 }
 
 		}else if (kmplotType.equals(CaIntegratorConstants.SAMPLE_KMPLOT)) {
 			KaplanMeierSampleInfo[] restofKMSampleInfos = null;
 			//dont need the clin query, just get the list now
 			List<SampleIDDE> sampleList = null;
 			helper = new UserListBeanHelper(request.getSession());
 			UserList ul = helper.getUserList(qsGroupName);
 			try	{
 				List<String> specimenNames = ul.getList();
 				//get the samples associated with these specimens
 				List<String> sampleIds = LookupManager.getSampleIDs(specimenNames);
 				//Add back any samples that were just sampleIds to start with
 				if (sampleIds != null){
 					specimenNames.addAll(sampleIds);
 				}
 				sampleList = new ArrayList<SampleIDDE>();
 				sampleList.addAll(ListConvertor.convertToSampleIDDEs(specimenNames));
 			}
 			catch(Exception e){
 				System.out.println("GROUP " + qsGroupName + " NOT FOUND");
 			}
 			
 			//ClinicalDataQuery clinicalDataQuery = getSelectedQuery(sessionId,"TARGET_LIST");
 			//List<SampleIDDE> sampleList = new ArrayList<SampleIDDE>();
 			//sampleList.addAll(clinicalDataQuery.getSampleIDCrit().getSampleIDs());
 
 
 			   kmResultsContainer = performKMClinicalQuery(sampleList, institutionCriteria);
 			   if(kmResultsContainer != null  ){
 				    kmSampleInfos = kmResultsContainer.getMeanKMPlotSamples();
 			   }
 			   
 			   //HERE IS WHERE WE WILL PASS THE 2ND comparison group, or ALL
 			   //the second list name, qsGroupNameCompare can be the following:
 			   // "" == rest of samples
 			   // none == no second group, only do the first by itself
 			   // "rest of the gliomas" == get only the diseased samples, so set csamples to ALL_GLIOMA and do the exclusion
 			   
 			   List<SampleIDDE> csampleList = null;
 			   final String ROTG = "Rest of the Gliomas";
 			   if(qsGroupNameCompare!=null && qsGroupNameCompare.length()>0 && !qsGroupNameCompare.equalsIgnoreCase("none")){
 				   String cgroup = qsGroupNameCompare.equalsIgnoreCase(ROTG) ? "ALL GLIOMA" : qsGroupNameCompare; 
 				   UserList cul = helper.getUserList(cgroup);
 				   if(cul!=null){
 					   csampleList = new ArrayList<SampleIDDE>();
 					   List<String> csamList = cul.getList();
 						//get the samples associated with these specimens
 					   List<String> sampleIds = LookupManager.getSampleIDs(csamList);
 						//Add back any samples that were just sampleIds to start with
 					   if(sampleIds != null){
 					    csamList.addAll(sampleIds);
 					   }
 						csampleList.addAll(ListConvertor.convertToSampleIDDEs(csamList));
 					   
 					   //
 				   }
 				   else	{
 					   csampleList = null;
 				   }
 			   }
 			   else	{
 				   //we are comparing a group with nothing, so assume its a "restOfSamples"
 				   //pass an empty list
 				   csampleList = null;
 			   }
 			   
 			   //here - remove the temporary list we created, if this is a clinical2km
 			   if(qsGroupName.equals("SamplesFromClinicalReport"))	{
 				   helper.removeList(qsGroupName);
 			   }
 			   
 			   //perform a query to get back all samples
 			   //here we want to call getRestOfSummaryKMPLotSamples() only if we are comparing first group
 			   //with "Rest of Samples", else just get them all (no exclusion)
 			   // if qsGroupNameCompare == null, then we are expecting to do the "restOfSamples" bit
 			   if(!qsGroupNameCompare.equalsIgnoreCase("none"))	{
 				   KaplanMeierPlotContainer allSampleKMResultsContainer = performKMClinicalQuery(csampleList,institutionCriteria);
 				   if(allSampleKMResultsContainer != null  ){
 					   if(qsGroupNameCompare!=null && (qsGroupNameCompare.equals("")|| qsGroupNameCompare.equals(ROTG)))	{
 						   //this is "vs restOfSamples" or restOfTheGliomas (aka ROTG)
 						   restofKMSampleInfos = allSampleKMResultsContainer.getRestOfSummaryKMPlotSamples(sampleList);
 					   }
 					   else	{
 						   //this is NOT a "vs restOfSamples", so dont do the exclusion
 						   restofKMSampleInfos = allSampleKMResultsContainer.getMeanKMPlotSamples();
 					   }
 				   }
 			   }
 			   else	{
 				   restofKMSampleInfos = new KaplanMeierSampleInfo[0];
 			   }
 			   
 			   //SHOULD PASS THE NAMES HERE IF WE WANT THEM IN THE LEGEND
 			   if(qsGroupNameCompare!=null && qsGroupNameCompare.equals(""))	{
 				   qsGroupNameCompare  = "Rest of Samples";
 			   }
 					KaplanMeierDataController dataGenerator = new KaplanMeierDataController( kmSampleInfos, restofKMSampleInfos, kmplotType, qsGroupName, qsGroupNameCompare);
 					KaplanMeierStoredData storedData = dataGenerator.getStoredData();
 					storedData.setId("KAPLAN");
 					kmForm.setStoredData(storedData);
 					kmForm.setSelectedDataset("KAPLAN");
 					presentationTierCache.addNonPersistableToSessionCache(request.getSession().getId(),"MyKaplainMeierContainer",kmResultsContainer);
 					presentationTierCache.addSessionGraphingData(request.getSession().getId(), storedData);
 					kmForm.setUpOrAmplified("Up Regulated");
 					kmForm.setDownOrDeleted("Down Regulated");	
 					kmForm.setGeneOrCytoband("   ");
 					kmForm.setReporters(new ArrayList());
 					kmForm.setDownFold(1.0);
 					kmForm.setUpFold(100.0);
 					kmForm.setSelectedReporter(CaIntegratorConstants.GRAPH_BLANK);
 					kmForm.setPlotVisible(true);
 		 }
 
 		if(kmplotType.equals(CaIntegratorConstants.COPY_NUMBER_KMPLOT) || kmplotType.equals(CaIntegratorConstants.GENE_EXP_KMPLOT)){			
 			if(kmResultsContainer != null && kmResultsContainer.getAssociatedReporters().size() == 0 ){
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						"gov.nih.nci.nautilus.ui.struts.form.quicksearch.noRecord",
 						quickSearchType, quickSearchVariableName));
 				this.saveErrors(request, errors);
 			}
 			else{
 				KaplanMeierDataController dataGenerator = new KaplanMeierDataController(upFold, downFold, quickSearchVariableName, kmSampleInfos, kmplotType, baselineGroup);
 				KaplanMeierStoredData storedData = dataGenerator.getStoredData();
 				storedData.setId("KAPLAN");
 				kmForm.setStoredData(storedData);
 				kmForm.setSelectedDataset("KAPLAN");
 				presentationTierCache.addNonPersistableToSessionCache(request.getSession().getId(),"MyKaplainMeierContainer",kmResultsContainer);
 				presentationTierCache.addSessionGraphingData(request.getSession().getId(), storedData);
 				if(kmplotType.equals(CaIntegratorConstants.COPY_NUMBER_KMPLOT)){
 		            kmForm = KMDataSetHelper.populateReporters(kmResultsContainer.getAssociatedSNPReportersSortedByPosition(), kmplotType, kmForm);
 				}else if (kmplotType.equals(CaIntegratorConstants.GENE_EXP_KMPLOT)){	
 		            kmForm = KMDataSetHelper.populateReporters(kmResultsContainer.getAssociatedGEReportersSortedByMeanIntensity(), kmplotType, kmForm);
 				}
 			}
 		}
 		/**
 		 * Select the mapping to follow
 		 */
 		
 		if (errors.isEmpty()) {
 			return mapping.findForward("kmplot");
 		} else {
 			return mapping.findForward("badgraph");
 		}
 	}
 
 	public ActionForward redrawKMPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		InstitutionCriteria institutionCriteria = InsitutionAccessHelper.getInsititutionCriteria(request.getSession());
 
 		KMDataSetForm kmForm = (KMDataSetForm) form;
 		KaplanMeierSampleInfo[] kmSampleInfos = null;
 		
 		String baselineGroup = request.getParameter("baselineGroup")!=null ? (String)request.getParameter("baselineGroup") : "ALL GLIOMA";
 
 		//		see if we are constraining by a group of samples
 		String cGroupName = "ALL GLIOMA"; //get from the Form
 		
 		cGroupName = baselineGroup;
 		
 		List<SampleIDDE> sampleList = null;
 		UserListBeanHelper helper = new UserListBeanHelper(request.getSession());
 		UserList ul = helper.getUserList(cGroupName);
 		try	{
 			List<String> samList = ul.getList();			
 			sampleList = convertToSpecimenList(samList);
 		}
 		catch(Exception e){
 			System.out.println("GROUP " + cGroupName + " NOT FOUND");
 		}
 		
 
 		// kmForm.setReporters(populateReporters());
 		String kmplotType = kmForm.getPlotType();
 		double upRegulation = kmForm.getUpFold();
 		double downRegulation = kmForm.getDownFold();
         String algorithm = kmForm.getReporterSelection(); 
         KaplanMeierPlotContainer kmResultsContainer = null;
         if(algorithm.equals(RembrandtConstants.REPORTER_SELECTION_UNI)){
             kmResultsContainer = performKMGeneExpressionQuery(sampleList, kmForm.getGeneOrCytoband(), GeneExpressionDataSetType.UnifiedGeneExpressionDataSet, institutionCriteria);
 
             //if (kmForm.getSelectedReporter().equals(CaIntegratorConstants.GRAPH_MEAN)  || kmForm.getSelectedReporter().equals(CaIntegratorConstants.GRAPH_MEDIAN)){
             //	kmForm.setSelectedReporter(CaIntegratorConstants.GRAPH_BLANK);
             //}
         }
         else{
         	//this is cached....if we are switching the sample groups, then dont get it from cache
             kmResultsContainer = getKmResultsContainer(request.getSession().getId());
         
           //  String quickSearchVariableName = kmForm.getGeneOrCytoband();
         	//try and see which type of KM plot this WAS, since this is not preserved by the KMDataSetForm
           //  String quickSearchType = ""; //kmResultsContainer.getCytobandDE() == null ? RembrandtConstants.GENE_SYMBOL : RembrandtConstants.SNP_PROBESET_ID;
          //   quickSearchType = RembrandtConstants.SNP_PROBESET_ID;
          //   kmResultsContainer = performKMCopyNumberQuery(sampleList, quickSearchVariableName, quickSearchType, institutionCriteria);
 
         }
 		if (kmResultsContainer != null	&& kmForm.getSelectedReporter() != null){
 			List reporterList = kmResultsContainer.getAssociatedReporters();
 			String reporter = kmForm.getSelectedReporter();
 			//remove extra formatting
 			int pos = 0;
 			 if (reporter.contains(CaIntegratorConstants.HIGHEST_GEOMETRIC_MEAN_INTENSITY)){
 				pos = reporter.indexOf(CaIntegratorConstants.HIGHEST_GEOMETRIC_MEAN_INTENSITY);
 				reporter = reporter.substring(0,pos).trim();
 			} else if (reporter.contains(CaIntegratorConstants.LOWEST_GEOMETRIC_MEAN_INTENSITY)){
 				pos = reporter.indexOf(CaIntegratorConstants.LOWEST_GEOMETRIC_MEAN_INTENSITY);
 				reporter = reporter.substring(0,pos).trim();
 			} 
 			 
 			if ((reporter.trim().length() > 0  ) &&
 					((reporterList.contains(reporter)||
 							reporter.equals(CaIntegratorConstants.GRAPH_MEAN)
 							|| reporter.equals(CaIntegratorConstants.GRAPH_MEDIAN)))) {    
 				kmForm.setPlotVisible(true);
 			} else { // empty graph
 				KaplanMeierSampleInfo[] km = {new KaplanMeierSampleInfo(0, 0, 0)};
 				kmSampleInfos = km;
 				kmForm.setPlotVisible(false);
 			}
 				if (kmplotType.equals(CaIntegratorConstants.GENE_EXP_KMPLOT)) {
 					kmForm = KMDataSetHelper.populateReporters(kmResultsContainer.getAssociatedGEReportersSortedByMeanIntensity(), kmplotType, kmForm);
 					if (reporter.equals(
 							CaIntegratorConstants.GRAPH_MEAN)) {
 						kmSampleInfos = kmResultsContainer.getMeanKMPlotSamples();
 					} else if (reporter.equals(
 							CaIntegratorConstants.GRAPH_MEDIAN)) {
 						kmSampleInfos = kmResultsContainer.getMedianKMPlotSamples();
 					} else if (!reporter.equals(CaIntegratorConstants.GRAPH_BLANK)){
 						kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(reporter);
 					}
 				} else if (kmplotType.equals(CaIntegratorConstants.COPY_NUMBER_KMPLOT)) {
 					kmForm = KMDataSetHelper.populateReporters(kmResultsContainer.getAssociatedSNPReportersSortedByPosition(), kmplotType, kmForm);
 					if (reporter.equals(
 							CaIntegratorConstants.GRAPH_MEAN)) {
 						kmSampleInfos = kmResultsContainer.getMeanKMPlotSamples();
 					} else if (reporter.equals(
 							CaIntegratorConstants.GRAPH_MEDIAN)) {
 						kmSampleInfos = kmResultsContainer.getMedianKMPlotSamples();
 					} else if (!reporter.equals(CaIntegratorConstants.GRAPH_BLANK)){
 						kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(reporter);
 					}
 					else{
 						kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(reporter);
 					}
 				}
 
 			//System.out.println("\n***********************************************************");
 			//for (int i=0; i < selectedPlots.length; i++ ) {
 			//	System.out.println(selectedPlots[i]);
 			//}
 			//System.out.println("\n***********************************************************");
 				
 			//KaplanMeierDataController dataGenerator = new KaplanMeierDataController(upRegulation, downRegulation, kmForm.getGeneOrCytoband(), kmSampleInfos, kmplotType, baselineGroup);
 			String[] selectedPlots = kmForm.getSelectedItems();
 			KaplanMeierDataController dataGenerator = new KaplanMeierDataController(upRegulation, downRegulation, kmForm.getGeneOrCytoband(), kmSampleInfos, kmplotType, baselineGroup, selectedPlots);
 			KaplanMeierStoredData storedData = dataGenerator.getStoredData();
 			storedData.setId("KAPLAN");
 			kmForm.setStoredData(storedData);
 			kmForm.setSelectedDataset("KAPLAN");
 			presentationTierCache.addSessionGraphingData(request.getSession().getId(), storedData);
 			kmForm.setDownFold(downRegulation);
 			kmForm.setUpFold(upRegulation);
 			return mapping.findForward("kmplot");
 		}
 		return mapping.findForward("badgraph");
 	}
 	
 	private KaplanMeierPlotContainer getKmResultsContainer(String sessionId) {
 		return (KaplanMeierPlotContainer)presentationTierCache.getNonPersistableObjectFromSessionCache(sessionId,"MyKaplainMeierContainer");
 	}
 
 	public ActionForward quickSearch(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		QuickSearchForm qsForm = (QuickSearchForm) form;
 		ActionErrors errors = new ActionErrors();
 		if (!qsForm.getPlot().equals(CaIntegratorConstants.SAMPLE_KMPLOT) && qsForm.getQuickSearchType() != null
 				&& qsForm.getQuickSearchType().equals(
 						RembrandtConstants.GENE_SYMBOL)) {
 			errors = UIFormValidator.validateGeneSymbol(qsForm, errors);
 		}
 		if (errors.isEmpty()) {
 			String chartType = qsForm.getPlot();
 
 			if (chartType.equalsIgnoreCase("kapMaiPlotGE")) {
 				logger.debug("user requested geneExp kapMai w/ genesymbol");
 				request.setAttribute("quickSearchName", qsForm.getQuickSearchName());
 				request.setAttribute("quickSearchType", qsForm.getQuickSearchType());
 				request.setAttribute("plotType",CaIntegratorConstants.GENE_EXP_KMPLOT);
 				return mapping.findForward("kmplot");
 			}
 			if (chartType.equalsIgnoreCase("kapMaiPlotCN")) {
 				logger.debug("user rquested SNP kapMaiPlotCN");
 				request.setAttribute("quickSearchType", qsForm.getQuickSearchType());
 				request.setAttribute("quickSearchName", qsForm.getQuickSearchName());
 				request.setAttribute("plotType",CaIntegratorConstants.COPY_NUMBER_KMPLOT);
 				return mapping.findForward("kmplot");
 			}
 			if (chartType.equalsIgnoreCase(CaIntegratorConstants.SAMPLE_KMPLOT)) {
 				logger.debug("user rquested SNP kapMaiPlotCN");
 				request.setAttribute("quickSearchType", qsForm.getQuickSearchType());
 				request.setAttribute("quickSearchName", qsForm.getQuickSearchName());
 				request.setAttribute("quickSearchGroupName", qsForm.getGroupName());
 				request.setAttribute("quickSearchGroupNameCompare", qsForm.getGroupNameCompare());
 				request.setAttribute("plotType",CaIntegratorConstants.SAMPLE_KMPLOT);
 				return mapping.findForward("kmplot");
 			}
 			else if (chartType.equalsIgnoreCase("geneExpPlot")) {
 				try {
 					logger.debug("user has requested geneExpPlot");
 					return doGeneExpPlot(mapping, qsForm, request, response);
 				} catch (Exception e) {
 					logger.error("Gene Expression Plot Flopped");
 					logger.error(e);
 					return mapping.findForward("error");
 				}
 			}
 
 		}
 		this.saveErrors(request, errors);
 		return mapping.findForward("mismatch");
 	}
 
 	/**
 	 * @return Returns the kmResultsContainer.
 	 * @throws Exception
 	 */
 	private KaplanMeierPlotContainer performKMGeneExpressionQuery(List<SampleIDDE> samples,
 			String geneSymbol,GeneExpressionDataSetType geneExpressionDataSetType, InstitutionCriteria institutionCriteria) throws Exception {
 		KMPlotManager kmPlotManager = new KMPlotManager();
 		KaplanMeierPlotContainer kaplanMeierPlotContainer = null;
             
 		switch(geneExpressionDataSetType){
 		case GeneExpressionDataSet:
 		default:
 			
             kaplanMeierPlotContainer = (KaplanMeierPlotContainer) kmPlotManager
 			.performKMGeneExpressionQuery(samples, geneSymbol,institutionCriteria);
 			break;
 		case UnifiedGeneExpressionDataSet:
 			kaplanMeierPlotContainer = (KaplanMeierPlotContainer) kmPlotManager
 			.performUnifiedKMGeneExpressionQuery(samples, geneSymbol, institutionCriteria);
 			break;
 		}
 		return kaplanMeierPlotContainer;
 	}
 
 	/**
 	 * @return Returns the kmResultsContainer.
 	 * @throws Exception
 	 */
 	private KaplanMeierPlotContainer performKMCopyNumberQuery(List<SampleIDDE> samples, String name,
 			String type,InstitutionCriteria institutionCriteria) throws Exception {
 		
 		// THIS	IS EXPECTING A "TYPE" FROM QUICKSEARCHFORM, NOT KMDATASETFORM
 		// CaIntegratorConstants.COPY_NUMBER_KMPLOT vs RembrandtConstants.SNP_PROBESET_ID
 		// CaIntegratorConstants.GENE_EXP_KMPLOT vs RembrandtConstants.GENE_SYMBOL
 		
 		KMPlotManager kmPlotManager = new KMPlotManager();
 		KaplanMeierPlotContainer kmResultsContainer = null;
 		
 		if (type.equals(RembrandtConstants.GENE_SYMBOL)) {
 			GeneIdentifierDE.GeneSymbol genesymbolDE = new GeneIdentifierDE.GeneSymbol(name);
 			
 			//get list of samples to hand off
 			
 			kmResultsContainer = (KaplanMeierPlotContainer) kmPlotManager
 					.performKMCopyNumberQuery(samples, genesymbolDE, institutionCriteria);
 
 		}
 		/**
 		 * TODO:FOr 1.0 if(type.equals(RembrandtConstants.CYTOBAND)){ CytobandDE
 		 * cytobandDE = new CytobandDE(name); this.kmResultsContainer =
 		 * kmPlotManager.performKMCopyNumberQuery(cytobandDE); }
 		 */
 		if (type.equals(RembrandtConstants.SNP_PROBESET_ID)) {
 			SNPIdentifierDE.SNPProbeSet snpDE = new SNPIdentifierDE.SNPProbeSet(
 					name);
 			kmResultsContainer = (KaplanMeierPlotContainer) kmPlotManager
 					.performKMCopyNumberQuery(samples, snpDE, institutionCriteria);
 
 		}
 		return kmResultsContainer;
 	}
 	/**
 	 * @return Returns the kmResultsContainer.
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unused")
 	private KaplanMeierPlotContainer performKMClinicalQuery(
 			List<SampleIDDE> sampleList, InstitutionCriteria institutionCriteria) throws Exception {
 		KMPlotManager kmPlotManager = new KMPlotManager();
 		KaplanMeierPlotContainer kaplanMeierPlotContainer = null;
             
 		kaplanMeierPlotContainer = (KaplanMeierPlotContainer) kmPlotManager
 			.performKMSampleQuery(sampleList, institutionCriteria);			
 		
 		return kaplanMeierPlotContainer;
 	}
 	public ActionForward redrawKaplanMeierGeneExpressionPlot(
 			ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		/*
 		 * This will need to be changes to reflect the new distinction between
 		 * copy number and gene expression km plots
 		 */
 		return mapping.findForward("kmplot");
 	}
 	private ClinicalDataQuery getSelectedQuery(String sessionId, String queryName){
 	       SampleBasedQueriesRetriever sampleBasedQueriesRetriever = new SampleBasedQueriesRetriever();
 	       ClinicalDataQuery clinicalDataQuery = sampleBasedQueriesRetriever.getQuery(sessionId, queryName);
         return clinicalDataQuery;       
 	}
 	private List<SampleIDDE> convertToSpecimenList(List<String> samples){
 		List<SampleIDDE> specimenList = new ArrayList<SampleIDDE>();
 		Set<String>specimenNames = new HashSet<String>(samples);
 		List<String> specimens = LookupManager.getSpecimenNames(specimenNames);
 		try {
 			if(specimens != null){
 				specimenNames.addAll(specimens);
 				//Remove Blood Type Specimens for KM plot
 				List<String> bloodSamples = new ArrayList<String>();
 				for(String specimenName:specimenNames){
 					if(specimenName.endsWith("_B")){
 						bloodSamples.add(specimenName);
 					}
 				}
 				specimenNames.removeAll(bloodSamples);			
 				specimenList.addAll(ListConvertor.convertToSampleIDDEs(specimenNames));			
 			}
 		} catch (OperationNotSupportedException e) {
 			logger.error(e.getMessage());
 		}
 		return specimenList;
 	}
 
 }

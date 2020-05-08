 /*
  * Created on Nov 19, 2004
  */ 
 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.application.cache.BusinessCacheManager;
 import gov.nih.nci.caintegrator.dto.query.OperatorType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ComparativeGenomicQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.service.findings.RembrandtTaskResult;
 import gov.nih.nci.rembrandt.util.IGVHelper;
 import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ReportGeneratorHelper;
 import gov.nih.nci.rembrandt.web.helper.WebGenomeHelper;
 import gov.nih.nci.rembrandt.web.struts.form.ClinicalDataForm;
 import gov.nih.nci.rembrandt.web.struts.form.ComparativeGenomicForm;
 import gov.nih.nci.rembrandt.web.struts.form.GeneExpressionForm;
 import gov.nih.nci.rembrandt.web.struts.form.ReportGeneratorForm;
 import gov.nih.nci.rembrandt.web.xml.CopyNumberIGVReport;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
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
 
 public class ReportGeneratorAction extends DispatchAction {
 
     private static Logger logger = Logger.getLogger(ReportGeneratorAction.class);
     private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
     
     public ActionForward compundReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		return mapping.findForward("generateReport");
 	}
     /**
      * This action method should be called when it is desired to actually render
      * a report to a jsp.  It will grab the desired report XML to display from the cache
      * and store it in the request so that it can be rendered.  
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward runGeneViewReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
     	String sID = request.getHeader("Referer");
     	
     	// prevents Referer Header injection
     	if ( sID != null && sID != "" && !sID.contains("rembrandt")) {
     		return (mapping.findForward("failure"));
     	}
     	
     	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
     	String sessionId = request.getSession().getId();
     	
     	// cleanup data - To prevent cross-site scripting
     	if( rgForm.getFilter_value1() != null && !rgForm.getFilter_value1().equals(""))
     		rgForm.setFilter_value1(MoreStringUtils.cleanJavascript( rgForm.getFilter_value1()));
     	if( rgForm.getFilter_value2() != null && !rgForm.getFilter_value2().equals(""))
     		rgForm.setFilter_value2(MoreStringUtils.cleanJavascript( rgForm.getFilter_value2()));
     	if( rgForm.getFilter_value3() != null && !rgForm.getFilter_value3().equals(""))
     		rgForm.setFilter_value3(MoreStringUtils.cleanJavascript( rgForm.getFilter_value3()));
     	if( rgForm.getFilter_value4() != null && !rgForm.getFilter_value4().equals(""))
     		rgForm.setFilter_value4(MoreStringUtils.cleanJavascript( rgForm.getFilter_value4()));
     	if( rgForm.getFilter_value5() != null && !rgForm.getFilter_value5().equals(""))
     		rgForm.setFilter_value5(MoreStringUtils.cleanJavascript( rgForm.getFilter_value5()));
     	if( rgForm.getFilter_value6() != null && !rgForm.getFilter_value6().equals(""))
     		rgForm.setFilter_value6(MoreStringUtils.cleanJavascript( rgForm.getFilter_value6()));
     			
     	//get the specified report bean from the cache using the query name as the key
     	ReportBean reportBean = presentationTierCache.getReportBean(sessionId,rgForm.getQueryName());
     	String isIGV = "false";
     	if(request.getParameter("igv")!=null)
     		isIGV = (String) request.getParameter("igv");
     	/*
     	 * check to see if this is a filter submission.  If it is then
     	 * we are going to need to generate XML most likely.  WE should probably
     	 * differentiate what are XML generation filter options and what are
     	 * XSLT filter options.  But for now they are all contained in the 
     	 * filterParams HashMap...  This could be clearer as it will definatley
     	 * cause some confusion for maintenance.
     	 * 
     	 * --Dave 
     	 */
     	Map filterParams = rgForm.getFilterParams();
     	/*
     	 * If there is a filter_type specified, we know that the UI
     	 * wants to perform a filter.  So we need to annotate the 
     	 * queryName to show that it is filter report.
     	 */
     	if(filterParams!=null&&filterParams.containsKey("filter_type")) {
     		//get the old resultant
     		Resultant resultant = reportBean.getResultant();
     		//get the old query
     		CompoundQuery cQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		//Mark this as a filter report
     		String queryName = cQuery.getQueryName();
     		//don't mark it again as a filter report if it is already a filter 
     		//report at present this will cause the old result in cache
     		//to be overwritten...
     		if(queryName.indexOf("filter report")<0) {
     			queryName = queryName + RembrandtConstants.FILTER_REPORT_SUFFIX;
     		}
     		//change the name of the associated query
     		cQuery.setQueryName(queryName);
             if(isIGV!= null && isIGV.equals("true")){
             	cQuery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_IGV ));
            }
     		//Create a new bean to store the new resultant, name, param combination
     		ReportBean newReportBean = new ReportBean();
     		//set the retrieval key for the ReportBean
     		newReportBean.setResultantCacheKey(queryName);
 //    		set the modified query in to the resultant and the report bean
     		resultant.setAssociatedQuery(cQuery);
     		newReportBean.setAssociatedQuery(cQuery);
     		//add the resultant to the new bean
     		newReportBean.setResultant(resultant);
     		//put the new bean in cache
     		BusinessCacheManager.getInstance().addToSessionCache(sessionId, queryName,newReportBean );
     		/*
     		 *  Generate new XML for the old resultant under the new QueryName.
     		 *	The filter param maps is necesary because it contains data that
     		 *  will be necesary to generate the correct XML for the filter
     		 *  specified...  This does beg the question, Why are we 
     		 *  regenerating the XML to apply a filter when we could do that in
     		 *  XSL.  Need to take a look at that in subsequent releases.
     		 *  
     		 *  --Dave
     		 *   
     		 */
     		
     		ReportGeneratorHelper generatorHelper = null;
     		try {
     		   generatorHelper = new ReportGeneratorHelper(cQuery,filterParams); 
 	    	}catch(Exception e) {
 				logger.error("Unable to create the ReportBean");
 				logger.error(e);
 			
 			  
 			}
     		//get the final constructed report bean
     		reportBean = generatorHelper.getReportBean();
     	}
     	//Do we have a ReportBean... we have to have a ReportBean
     	if(reportBean!=null) {
 	    	//Check to see if there is an XSLT specified
     		if("".equals(rgForm.getXsltFileName())||rgForm.getXsltFileName()==null) {
 	    		//If no filters specified then use the default XSLT
 	    		request.setAttribute(RembrandtConstants.XSLT_FILE_NAME,RembrandtConstants.DEFAULT_XSLT_FILENAME);
 	    	}else {
 	    		//Apply any XSL filters defined in the form
 	    		request.setAttribute(RembrandtConstants.XSLT_FILE_NAME,rgForm.getXsltFileName());
 	    	}
     		/*
     		 * decide whether the XSL should allow an "Show All Values" button.
     		 * At this time the AllGenedQuery has just too many values to return,
     		 * especially for a CopyNumber AllGEnes query 
     		 */
     		if(reportBean.isAllGenesQuery()) {
     			rgForm.setAllowShowAllValues("false");
     		}
     		
     		/*
     		 *	put the textual description of the compound query into the report. 
     		 *
     		 * 	this is a complete hack and should be revisited as soon as we re-do the 
     		 *  toString() for each query - note the ugly HTML that does not belong here.
     		 *  also, due to the stupidity of XSL, we need to replace certain chars:
     		 *  such as <,  >, and & before sending it over.  we are then relying on 
     		 *  trusty old javascript to convert back into HTML for presentation
     		 * 
     		 *  -RCL
     		 */
     		CompoundQuery compoundQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		StringBuffer sb = new StringBuffer();
     		if(compoundQuery != null) {			
     			String theQuery  =  compoundQuery.toString();
     	 		sb.append("<br><a name=\'queryInfo\'></a>Query: "+theQuery);
     	 		sb.append("<table>");
     	 		sb.append("<tr>");
     	 		Query[] queries = compoundQuery.getAssociatiedQueries();
     	 		for(int i = 0; i<queries.length; i++){
     	 			sb.append("<td>");
     	 			sb.append(queries[i]);
     	 			sb.append("</td>");
     	 		}
     	 		sb.append("</tr>");
     	 		sb.append("</table>");
     		}
     		
     		String noHTMLString = sb.toString();
     		//noHTMLString = noHTMLString.replaceAll("\\<.*?\\>","");
     		noHTMLString = noHTMLString.replaceAll("<", "{");
     		noHTMLString = noHTMLString.replaceAll(">", "}");
     		noHTMLString = noHTMLString.replaceAll("&nbsp;", " ");
     		rgForm.setQueryDetails(noHTMLString);
     		
 	    	//add the Filter Parameters from the form to the forwarding request
 	    	request.setAttribute(RembrandtConstants.FILTER_PARAM_MAP, rgForm.getFilterParams());
 	    	//put the report xml in the request
 	    	request.setAttribute(RembrandtConstants.REPORT_XML, reportBean.getReportXML());
     	}else {
     		//Throw an exception because you should never call this action method
     		//unless you have already generated the report and stored it in the cache
     		logger.error( new IllegalStateException("Missing ReportBean for: "+rgForm.getQueryName()));
     	}
     	//Go to the geneViewReport.jsp to render the report
     	return mapping.findForward("runGeneViewReport");
     }
     /**
      * This action method should be called when it is desired to actually render
      * a Copy Number IGV report to a jsp.  It will grab the desired report XML to display from the cache
      * and store it in the request so that it can be rendered.  
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward runIGVReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
     	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
     	String sessionId = request.getSession().getId();
     	//get the specified report bean from the cache using the query name as the key
     	ReportBean reportBean = presentationTierCache.getReportBean(sessionId,rgForm.getQueryName());
     	/*
     	 * check to see if this is a filter submission.  If it is then
     	 * we are going to need to generate XML most likely.  WE should probably
     	 * differentiate what are XML generation filter options and what are
     	 * XSLT filter options.  But for now they are all contained in the 
     	 * filterParams HashMap...  This could be clearer as it will definatley
     	 * cause some confusion for maintenance.
     	 * 
     	 * --Dave 
     	 */
     	Map filterParams = rgForm.getFilterParams();
     	/*
     	 * If there is a filter_type specified, we know that the UI
     	 * wants to perform a filter.  So we need to annotate the 
     	 * queryName to show that it is filter report.
     	 */
     	if(filterParams!=null&&filterParams.containsKey("filter_type")) {
     		//get the old resultant
     		Resultant resultant = reportBean.getResultant();
     		//get the old query
     		CompoundQuery cQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		//Mark this as a filter report
     		String queryName = cQuery.getQueryName();
     		//don't mark it again as a filter report if it is already a filter 
     		//report at present this will cause the old result in cache
     		//to be overwritten...
     		if(queryName.indexOf("filter report")<0) {
     			queryName = queryName + RembrandtConstants.FILTER_REPORT_SUFFIX;
     		}
     		//change the name of the associated query
     		cQuery.setQueryName(queryName);
     		//Create a new bean to store the new resultant, name, param combination
     		ReportBean newReportBean = new ReportBean();
     		//set the retrieval key for the ReportBean
     		newReportBean.setResultantCacheKey(queryName);
 //    		set the modified query in to the resultant and the report bean
     		resultant.setAssociatedQuery(cQuery);
     		newReportBean.setAssociatedQuery(cQuery);
     		//add the resultant to the new bean
     		newReportBean.setResultant(resultant);
     		//put the new bean in cache
     		BusinessCacheManager.getInstance().addToSessionCache(sessionId, queryName,newReportBean );
     		/*
     		 *  Generate new XML for the old resultant under the new QueryName.
     		 *	The filter param maps is necesary because it contains data that
     		 *  will be necesary to generate the correct XML for the filter
     		 *  specified...  This does beg the question, Why are we 
     		 *  regenerating the XML to apply a filter when we could do that in
     		 *  XSL.  Need to take a look at that in subsequent releases.
     		 *  
     		 *  --Dave
     		 *   
     		 */
     		
     		ReportGeneratorHelper generatorHelper = null;
     		try {
     			generatorHelper = new ReportGeneratorHelper(cQuery,filterParams);
     		}
 	    	catch(Exception e) {
 				logger.error("Unable to create the ReportBean");
 				logger.error(e);
 			}
     		//get the final constructed report bean
     		reportBean = generatorHelper.getReportBean();
     	}
     	//Do we have a ReportBean... we have to have a ReportBean
     	if(reportBean!=null) {
 
     		CompoundQuery compoundQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		StringBuffer sb = new StringBuffer();
     		if(compoundQuery != null) {			
     			String theQuery  =  compoundQuery.toString();
     	 		sb.append("<br><a name=\'queryInfo\'></a>Query: "+theQuery);
     	 		sb.append("<table>");
     	 		sb.append("<tr>");
     	 		Query[] queries = compoundQuery.getAssociatiedQueries();
     	 		for(int i = 0; i<queries.length; i++){
     	 			sb.append("<td>");
     	 			sb.append(queries[i]);
     	 			sb.append("</td>");
     	 		}
     	 		sb.append("</tr>");
     	 		sb.append("</table>");
     		}
     		
     		String noHTMLString = sb.toString();
     		//noHTMLString = noHTMLString.replaceAll("\\<.*?\\>","");
     		noHTMLString = noHTMLString.replaceAll("<", "{");
     		noHTMLString = noHTMLString.replaceAll(">", "}");
     		noHTMLString = noHTMLString.replaceAll("&nbsp;", " ");
     		rgForm.setQueryDetails(noHTMLString);
     		
 	    	//add the Filter Parameters from the form to the forwarding request
 	    	request.setAttribute(RembrandtConstants.FILTER_PARAM_MAP, rgForm.getFilterParams());
 	    	//put the report xml in the request
 	    	CopyNumberIGVReport copyNumberIGVReport = new CopyNumberIGVReport();
 	    	StringBuffer stringBuffer = copyNumberIGVReport.getIGVReport(reportBean.getResultant());
 	    	String locus = "chr"+copyNumberIGVReport.getChr()+":"+copyNumberIGVReport.getStartLoc()+"-"+copyNumberIGVReport.getEndLoc(); //chromosome:start-end
 	     	String url = request.getRequestURL().toString();
 	    	url = url.substring(0, url.lastIndexOf("/")+1);
 	    	IGVHelper igvHelper = new IGVHelper(sessionId, locus,  url);
 	    	//fileDownload.do?method=brbFileDownload&fileId=
 	    	//url = url+"igvfileDownload.do?method=igvFileDownload&igv=";
 	    	String cnFileName = igvHelper.getIgvCopyNumberFileName();
 	    	igvHelper.writeStringtoFile(stringBuffer.toString(), cnFileName);
 	    	String igvURL = igvHelper.getIgvJNPL();
 	    	if(igvURL != null){
 	    		request.setAttribute(RembrandtConstants.REPORT_IGV, igvURL);
 	    	}
     	}else {
     		//Throw an exception because you should never call this action method
     		//unless you have already generated the report and stored it in the cache
     		logger.error( new IllegalStateException("Missing ReportBean for: "+rgForm.getQueryName()));
     	}
     	//Go to the geneViewReport.jsp to render the report
     	return mapping.findForward("runIGVReport");
     }
     /**
      * This action method should be called when it is desired to actually render
      * a report to a jsp.  It will grab the desired report XML to display from the cache
      * and store it in the request so that it can be rendered.  
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward runGeneViewReportFromCache(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	throws Exception {
     	String sID = request.getHeader("Referer");
     	
     	// prevents Referer Header injection
     	if ( sID != null && sID != "" && !sID.contains("rembrandt")) {
     		return (mapping.findForward("failure"));
     	}
     	
     	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
     	String sessionId = request.getSession().getId();
 		String taskId = request.getParameter("taskId");
 		
 		// cleanup data - To prevent cross-site scripting
     	if( rgForm.getFilter_value1() != null && !rgForm.getFilter_value1().equals(""))
     		rgForm.setFilter_value1(MoreStringUtils.cleanJavascript( rgForm.getFilter_value1()));
     	if( rgForm.getFilter_value2() != null && !rgForm.getFilter_value2().equals(""))
     		rgForm.setFilter_value2(MoreStringUtils.cleanJavascript( rgForm.getFilter_value2()));
     	if( rgForm.getFilter_value3() != null && !rgForm.getFilter_value3().equals(""))
     		rgForm.setFilter_value3(MoreStringUtils.cleanJavascript( rgForm.getFilter_value3()));
     	if( rgForm.getFilter_value4() != null && !rgForm.getFilter_value4().equals(""))
     		rgForm.setFilter_value4(MoreStringUtils.cleanJavascript( rgForm.getFilter_value4()));
     	if( rgForm.getFilter_value5() != null && !rgForm.getFilter_value5().equals(""))
     		rgForm.setFilter_value5(MoreStringUtils.cleanJavascript( rgForm.getFilter_value5()));
     	if( rgForm.getFilter_value6() != null && !rgForm.getFilter_value6().equals(""))
     		rgForm.setFilter_value6(MoreStringUtils.cleanJavascript( rgForm.getFilter_value6()));
 		
     	//get the specified report bean from the cache using the query name as the key
     	RembrandtTaskResult taskResult = (RembrandtTaskResult) presentationTierCache.getTaskResult(sessionId, taskId);
     	String reportBeanCacheKey = taskResult.getReportBeanCacheKey();
     	ReportBean reportBean = (ReportBean) presentationTierCache.getReportBean(sessionId, reportBeanCacheKey);
     	/*
     	 * check to see if this is a filter submission.  If it is then
     	 * we are going to need to generate XML most likely.  WE should probably
     	 * differentiate what are XML generation filter options and what are
     	 * XSLT filter options.  But for now they are all contained in the 
     	 * filterParams HashMap...  This could be clearer as it will definatley
     	 * cause some confusion for maintenance.
     	 * 
     	 * --Dave 
     	 */
     	Map filterParams = rgForm.getFilterParams();
     	/*
     	 * If there is a filter_type specified, we know that the UI
     	 * wants to perform a filter.  So we need to annotate the 
     	 * queryName to show that it is filter report.
     	 */
     	if(filterParams!=null&&filterParams.containsKey("filter_type")) {
     		//get the old resultant
     		Resultant resultant = reportBean.getResultant();
     		//get the old query
     		CompoundQuery cQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		//Mark this as a filter report
     		String queryName = cQuery.getQueryName();
     		//don't mark it again as a filter report if it is already a filter 
     		//report at present this will cause the old result in cache
     		//to be overwritten...
     		if(queryName.indexOf("filter report")<0) {
     			queryName = queryName + RembrandtConstants.FILTER_REPORT_SUFFIX;
     		}
     		//change the name of the associated query
     		cQuery.setQueryName(queryName);
     		
     		//Create a new bean to store the new resultant, name, param combination
     		ReportBean newReportBean = new ReportBean();
     		//set the retrieval key for the ReportBean
     		newReportBean.setResultantCacheKey(queryName);
 //    		set the modified query in to the resultant and the report bean
     		resultant.setAssociatedQuery(cQuery);
     		newReportBean.setAssociatedQuery(cQuery);
     		//add the resultant to the new bean
     		newReportBean.setResultant(resultant);
     		//put the new bean in cache
     		BusinessCacheManager.getInstance().addToSessionCache(sessionId, queryName,newReportBean );
     		/*
     		 *  Generate new XML for the old resultant under the new QueryName.
     		 *	The filter param maps is necesary because it contains data that
     		 *  will be necesary to generate the correct XML for the filter
     		 *  specified...  This does beg the question, Why are we 
     		 *  regenerating the XML to apply a filter when we could do that in
     		 *  XSL.  Need to take a look at that in subsequent releases.
     		 *  
     		 *  --Dave
     		 *   
     		 */
     		
     		ReportGeneratorHelper generatorHelper = null;
     		try {
     			generatorHelper = new ReportGeneratorHelper(cQuery,filterParams); 
 	    	}catch(Exception e) {
 				logger.error("Unable to create the ReportBean");
 				logger.error(e);
 			
 			  
 			}
     		//get the final constructed report bean
     		reportBean = generatorHelper.getReportBean();
     	}
     	//Do we have a ReportBean... we have to have a ReportBean
     	if(reportBean!=null) {
 	    	//Check to see if there is an XSLT specified
     		if("".equals(rgForm.getXsltFileName())||rgForm.getXsltFileName()==null) {
 	    		//If no filters specified then use the default XSLT
 	    		request.setAttribute(RembrandtConstants.XSLT_FILE_NAME,RembrandtConstants.DEFAULT_XSLT_FILENAME);
 	    	}else {
 	    		//Apply any XSL filters defined in the form
 	    		request.setAttribute(RembrandtConstants.XSLT_FILE_NAME,rgForm.getXsltFileName());
 	    	}
     		/*
     		 * decide whether the XSL should allow an "Show All Values" button.
     		 * At this time the AllGenedQuery has just too many values to return,
     		 * especially for a CopyNumber AllGEnes query 
     		 */
     		if(reportBean.isAllGenesQuery()) {
     			rgForm.setAllowShowAllValues("false");
     		}
     		
     		/*
     		 *	put the textual description of the compound query into the report. 
     		 *
     		 * 	this is a complete hack and should be revisited as soon as we re-do the 
     		 *  toString() for each query - note the ugly HTML that does not belong here.
     		 *  also, due to the stupidity of XSL, we need to replace certain chars:
     		 *  such as <,  >, and & before sending it over.  we are then relying on 
     		 *  trusty old javascript to convert back into HTML for presentation
     		 * 
     		 *  -RCL
     		 */
     		CompoundQuery compoundQuery = ((CompoundQuery)(reportBean.getAssociatedQuery()));
     		StringBuffer sb = new StringBuffer();
     		if(compoundQuery != null) {			
     			String theQuery  =  compoundQuery.toString();
     	 		sb.append("<br><a name=\'queryInfo\'></a>Query: "+theQuery);
     	 		sb.append("<table>");
     	 		sb.append("<tr>");
     	 		Query[] queries = compoundQuery.getAssociatiedQueries();
     	 		for(int i = 0; i<queries.length; i++){
     	 			sb.append("<td>");
     	 			sb.append(queries[i]);
     	 			sb.append("</td>");
     	 		}
     	 		sb.append("</tr>");
     	 		sb.append("</table>");
     		}
     		
     		String noHTMLString = sb.toString();
     		//noHTMLString = noHTMLString.replaceAll("\\<.*?\\>","");
     		noHTMLString = noHTMLString.replaceAll("<", "{");
     		noHTMLString = noHTMLString.replaceAll(">", "}");
     		noHTMLString = noHTMLString.replaceAll("&nbsp;", " ");
     		rgForm.setQueryDetails(noHTMLString);
     		
 	    	//add the Filter Parameters from the form to the forwarding request
 	    	request.setAttribute(RembrandtConstants.FILTER_PARAM_MAP, rgForm.getFilterParams());
 	    	//put the report xml in the request
 	    	request.setAttribute(RembrandtConstants.REPORT_XML, reportBean.getReportXML());
     	}else {
     		//Throw an exception because you should never call this action method
     		//unless you have already generated the report and stored it in the cache
     		logger.error( new IllegalStateException("Missing ReportBean for: "+rgForm.getQueryName()));
     	}
     	//Go to the geneViewReport.jsp to render the report
     	return mapping.findForward("runGeneViewReport");
     }
 	/**
 	 * Makes the necessary calls to run a compound query, then forwards the 
 	 * request to the report rendering mechanism.
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward runCompoundQueryReport(
 		ActionMapping mapping,
 		ActionForm form,
 		HttpServletRequest request,
 		HttpServletResponse response)
 		throws Exception {
 		//Get the sessionId
 		String sessionId = request.getSession().getId();
         ActionForward thisForward = null;
         ActionErrors errors = new ActionErrors();
 		SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
 
 		String queryName = request.getParameter("queryName");
 		String typeOfView = request.getParameter("typeOfView");
 		
 		CompoundQuery cQuery = (CompoundQuery)queryBag.getCompoundQuery(queryName);
 		if(cQuery == null)	{
 			//perhaps this was a filtered report, see if that exists
 			//remove the suffix and see if the cquery is found
 			queryName = queryName.substring(0, queryName.indexOf(RembrandtConstants.FILTER_REPORT_SUFFIX));
 			cQuery = (CompoundQuery)queryBag.getCompoundQuery(queryName);
 		}
 		if (cQuery != null) {
 			// Get the viewType array from session 
 			//ViewType [] availableViewTypes = (ViewType []) request.getSession().getAttribute(RembrandtConstants.VALID_QUERY_TYPES_KEY);
 			//if (availableViewTypes == null){
 			ViewType [] availableViewTypes = cQuery.getValidViews();
 				//request.getSession().setAttribute(RembrandtConstants.VALID_QUERY_TYPES_KEY, availableViewTypes);
 			//}
 			ViewType selectView = availableViewTypes[Integer.parseInt(typeOfView)];
 			cQuery.setAssociatedView(ViewFactory.newView(selectView));
            	//ReportGeneratorHelper will execute the query if necesary, or will
 			//retrieve from cache.  It will then generate the XML for the report
 			//and store in a reportBean in the cache for later retrieval
             
 			ReportGeneratorHelper rgHelper = null;
 			try {
 			 rgHelper = new ReportGeneratorHelper(cQuery, new HashMap());
 			}
 	    	catch(Exception e) {
 				logger.error("Unable to create the ReportBean");
 				logger.error(e);
 			}
 			ReportBean reportBean = rgHelper.getReportBean();
 			request.setAttribute("queryName", reportBean.getResultantCacheKey());
 			//Send to the appropriate view as per selection!!
 			thisForward = new ActionForward();
 
 			thisForward.setPath("/runReport.do?method=runGeneViewReport&resultSetName="+ reportBean.getResultantCacheKey());
 		}else {
 			logger.error("SessionQueryBag has no Compound queries to execute");
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("gov.nih.nci.nautilus.ui.struts.action.executequery.querycoll.no.error"));
 			this.saveErrors(request, errors);
 			thisForward = mapping.findForward("failure");
 		}
 	    return thisForward;
 	 }
 	
     /**
      * This action is used to generate a preview report.  Because the current
      * preview is in fact a popup from the build query page this forward
      * actually returns back to the input action.  This action method is currently
      * called by all 3 /preview* action mappings and is necesary to allow for validation
      * before we actually execute a query and display the results.  The previous
      * actions will have already created, and executed, the preview query.
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
 	public ActionForward previewReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
         String goBack=null;	
         if(form instanceof GeneExpressionForm) {
             request.setAttribute("geneexpressionForm", request.getAttribute("previewForm"));
             goBack = "backToGeneExp";
         }else if(form instanceof ClinicalDataForm) {
             request.setAttribute("clinicaldataForm", request.getAttribute("previewForm"));
             goBack = "backToClinical";
         }else if(form instanceof ComparativeGenomicForm) {
             request.setAttribute("comparitivegenomicForm", request.getAttribute("previewForm"));
             goBack = "backToCGH";
         }
         //We obviously have passed validation...
         //So now go back to the submitting page and run
         //java script to spawn the report window.
         request.removeAttribute("previewForm");
         request.setAttribute("preview", new String("yes"));
         request.setAttribute("queryName",RembrandtConstants.PREVIEW_RESULTS);
         logger.debug("back: " + goBack);
         return mapping.findForward(goBack);
 	}
 	public ActionForward submitSamples(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		ActionForward thisForward = null;
 		ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 		//Used to get the old resultant from cache
 		String queryName = rgForm.getQueryName();
 		//This is what the user wants to name the new resultSet
 		String prb_queryName = rgForm.getPrbQueryName();
 		//get this list of sample ids
 		String[] sampleIds = rgForm.getSamples();
 		String sessionId = request.getSession().getId();
 		
 		//get the old 
 		CompoundQuery cquery = presentationTierCache.getQuery(sessionId, queryName );
 		if(cquery!=null) {
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 			cquery.setQueryName(prb_queryName);
 			//This will generate the report and store it in the cache
 			ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false );
 			//store the name of the query in the form so that we can later pull it out of cache
 			ReportBean reportBean = rgHelper.getReportBean();
 			rgForm.setQueryName(reportBean.getResultantCacheKey());
 			
 			HashMap<String,String> fpm = rgForm.getFilterParams();
 			String msg = ResourceBundle.getBundle(RembrandtConstants.APPLICATION_RESOURCES, Locale.US).getString("add_samples_msg");
 			fpm.put("statusMsg", msg);
 			rgForm.setFilterParams(fpm);
 
        	}
 		//now send everything that we have done to the actual method that will render the report
 		return runGeneViewReport(mapping, rgForm, request, response);
 	}
 public ActionForward submitSpecimens(ActionMapping mapping, ActionForm form,
 		HttpServletRequest request, HttpServletResponse response)
 		throws Exception {
 	ActionForward thisForward = null;
 	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 	//Used to get the old resultant from cache
 	String queryName = rgForm.getQueryName();
 	//This is what the user wants to name the new resultSet
 	String prb_queryName = rgForm.getPrbQueryName();
 	//actually get the list of specimen names
 	String[] specimenNames = rgForm.getSamples();
 	//get the samples associated with these specimens
 	List<String> sampleIds = LookupManager.getSampleIDs(Arrays.asList(specimenNames));
 	String sessionId = request.getSession().getId();
 	
 	//get the old 
 	CompoundQuery cquery = presentationTierCache.getQuery(sessionId, queryName );
 	if(cquery!=null) {
 		cquery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 		cquery.setQueryName(prb_queryName);
 		//This will generate the report and store it in the cache
 		ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cquery, (String[])sampleIds.toArray(new String[sampleIds.size()]), false );
 		//store the name of the query in the form so that we can later pull it out of cache
 		ReportBean reportBean = rgHelper.getReportBean();
 		rgForm.setQueryName(reportBean.getResultantCacheKey());
 		
 		HashMap<String,String> fpm = rgForm.getFilterParams();
 		String msg = ResourceBundle.getBundle(RembrandtConstants.APPLICATION_RESOURCES, Locale.US).getString("add_samples_msg");
 		fpm.put("statusMsg", msg);
 		rgForm.setFilterParams(fpm);
 
    	}
 	//now send everything that we have done to the actual method that will render the report
 	return runGeneViewReport(mapping, rgForm, request, response);
 }
 
 public ActionForward exportToExcelForGeneView(ActionMapping mapping, ActionForm form,
 		HttpServletRequest request, HttpServletResponse response)
 		throws Exception {
 	ActionForward thisForward = null;
 	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 	//Used to get the old resultant from cache
 	String queryName = rgForm.getQueryName();
 	//This is what the user wants to name the new resultSet
 	String prb_queryName = rgForm.getPrbQueryName();
 	String sessionId = request.getSession().getId();
 	
 	String reportType = request.getParameter( "reportType" );
 	String[] sampleIds = null;
 	
 	if( reportType.equals( "Gene Expression Sample" ) || reportType.equals( "Copy Number" ) ){
 		sampleIds = (String[])request.getSession().getAttribute("tmp_excel_export");
 	}
 	else {
 		List list = (List)request.getSession().getAttribute("clinical_tmpSampleList");
 		sampleIds = (String[])list.toArray(new String[list.size()]);
 	}
 	
 	//get the old 
 	CompoundQuery cquery = presentationTierCache.getQuery(sessionId, queryName );
 	
 	if(cquery!=null) {
 		if( reportType.equals( "Gene Expression Sample" ) )
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
 		else if ( reportType.equals( "Gene Copy Number" ) )	
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_GENE_SAMPLE_VIEW ));
 		else if ( reportType.equals( "Copy Number" ) )	
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_SEGMENT_VIEW ));
 		//else if ( reportType.equals( "Copy Number-IGV" ) )	
 		//	cquery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_IGV ));
 		else
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 		
 		cquery.setQueryName(prb_queryName);
 		
 		//This will generate the report and store it in the cache
 		//ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false );
 		ReportGeneratorHelper rgHelper = null;	
 			rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false);
 
 		/*
 		if (!reportType.equals("Gene Expression Sample") && !reportType.equals("Copy Number")) {
 			rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false, true );
 		} else {
 		}
 		*/
 		//store the name of the query in the form so that we can later pull it out of cache
 		ReportBean reportBean = rgHelper.getReportBean();
 		rgForm.setQueryName(reportBean.getResultantCacheKey());
    	}
 	//now send everything that we have done to the actual method that will render the report
 	return runGeneViewReport(mapping, rgForm, request, response);
 }
 public ActionForward exportToIGV(ActionMapping mapping, ActionForm form,
 		HttpServletRequest request, HttpServletResponse response)
 		throws Exception {
 	ActionForward thisForward = null;
 	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 	//Used to get the old resultant from cache
 	String queryName = rgForm.getQueryName();
 	//This is what the user wants to name the new resultSet
 	String prb_queryName = rgForm.getPrbQueryName();
 	String sessionId = request.getSession().getId();
 	
 	String reportType = request.getParameter( "reportType" );
 	String[] sampleIds = null;
 	
 	if( reportType.equals( "Copy Number" ) ){
 		sampleIds = (String[])request.getSession().getAttribute("tmp_excel_export");
 	}
 	
 	
 	//get the old 
 	CompoundQuery cquery = presentationTierCache.getQuery(sessionId, queryName );
 	
 	if(cquery!=null) {
 		if ( reportType.equals( "Gene Copy Number" ) )	
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_GENE_SAMPLE_VIEW ));
 		else if ( reportType.equals( "Copy Number" ) )	
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.COPYNUMBER_SEGMENT_VIEW ));
 		
 		cquery.setQueryName(prb_queryName);
 		
 		//This will generate the report and store it in the cache
 		//ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false );
 		ReportGeneratorHelper rgHelper = null;	
 			rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false);
 
 		/*
 		if (!reportType.equals("Gene Expression Sample") && !reportType.equals("Copy Number")) {
 			rgHelper = new ReportGeneratorHelper(cquery, sampleIds, false, true );
 		} else {
 		}
 		*/
 		//store the name of the query in the form so that we can later pull it out of cache
 		ReportBean reportBean = rgHelper.getReportBean();
 		rgForm.setQueryName(reportBean.getResultantCacheKey());
    	}
 	//now send everything that we have done to the actual method that will render the report
 	return runGeneViewReport(mapping, rgForm, request, response);
 }
 public ActionForward switchViews(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		ActionForward thisForward = null;
 		ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 		//Used to get the old resultant from cache
 		String queryName = rgForm.getQueryName();
 		String sessionId = request.getSession().getId();
 		
 		String[] sampleIds = rgForm.getSamples();
 		
 		//get the old 
 		CompoundQuery cquery = presentationTierCache.getQuery(sessionId, queryName );
 		if(cquery!=null) {
 			//A clone has to be used in order to avoid view confusion
 			CompoundQuery clonedQuery = (CompoundQuery)cquery.clone();
 			String reportView = (String)rgForm.getReportView();
 			if(reportView != null)	{
 				if(reportView.equals("G"))
 					clonedQuery.setAssociatedView(ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
 				else if(reportView.equals("C"))
 					clonedQuery.setAssociatedView(clonedQuery.getAssociatedView());
 				else
 					clonedQuery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 			}
 			else	{
 				//clinical by default since thats universal for all query types
 				clonedQuery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));				
 			}
 			//This will generate the report and store it in the cache
 			ReportGeneratorHelper rgHelper = null;
 			try {
				if(sampleIds.length == 0 || (sampleIds[0] != null && sampleIds[0].equals("")) )	{
 				    rgHelper = new ReportGeneratorHelper(clonedQuery, rgForm.getFilterParams() );
 				}
 				else	{  
 					//to fix the query name conflict issue, a new boolean value
 					//is added to ReportGeneratorHelper constructor to find out if a new query
 					//name for the query is needed. 
 				    rgHelper = new ReportGeneratorHelper(clonedQuery, sampleIds, true);
 				}
 			}catch(Exception e) {
 				logger.error("Unable to create the ReportBean");
 				logger.error(e);
 			}
 			
 			
 			//store the name of the query in the form so that we can later pull it out of cache
 			ReportBean reportBean = rgHelper.getReportBean();
 			rgForm.setQueryName(reportBean.getResultantCacheKey());
        	}
 		//now send everything that we have done to the actual method that will render the report
 		return runGeneViewReport(mapping, rgForm, request, response);
 	}
 	
 	public ActionForward runShowAllValuesQuery(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		ActionForward thisForward = null;
 		ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 		String queryName = rgForm.getQueryName();
 		String sessionId = request.getSession().getId();
 		ReportBean reportBean = presentationTierCache.getReportBean(sessionId, queryName);
 		if(reportBean!=null) {
 			//This will generate get a resultant and store it in the cache
 			/*
 			 * Dropped the sessionId into the reportBean associated query, because it was
 			 * getting missed some how
 			 * @todo must find this
 			 */
 			((CompoundQuery)(reportBean.getAssociatedQuery())).setSessionId(sessionId);
 			
 			//put a flag in the filterParams Map so we know its a showAllValues
 			HashMap<String,String> fpm = rgForm.getFilterParams();
 			fpm.put("showAllValues", "true");
 			rgForm.setFilterParams(fpm);
 			ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(reportBean, rgForm.getFilterParams());
 			//store the name of the query in the form so that we can later pull it out of cache
 			reportBean = rgHelper.getReportBean();
 			if(reportBean!= null){
 				rgForm.setQueryName(reportBean.getResultantCacheKey());
 			}
        	}
 		//now send everything that we have done to the actual method that will render the report
 		return runGeneViewReport(mapping, rgForm, request, response);
 	}
 	
 	public ActionForward runFilterCopyNumber(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		ActionForward thisForward = null;
 		ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
 		String queryName = rgForm.getQueryName();
 		String sessionId = request.getSession().getId();
 		ReportBean reportBean = presentationTierCache.getReportBean(sessionId, queryName);
 		//CompoundQuery cquery = BusinessCacheManager.getInstance().getQuery(sessionId, queryName );
 		
 		Resultant resultant = reportBean.getResultant();
 		//set the defaults in case we dont get what we need from the form
 		Integer nocalls = new Integer("0");
 		Integer percent = new Integer("0");
 		OperatorType operator = OperatorType.OR;
 		String filter_element = "copy_number";
 		
 		//get the data we need from the form
 		if(rgForm.getFilter_value5()!=null)
 		    nocalls = Integer.valueOf((String) rgForm.getFilter_value5());
 		if(rgForm.getFilter_value6()!=null)
 		    percent = Integer.valueOf((String) rgForm.getFilter_value6());
 		//reusing filter_value4 for this
 		if(rgForm.getFilter_value4()!=null && ((String)rgForm.getFilter_value4()).equalsIgnoreCase("and"))
 		    operator = OperatorType.AND;
 		/*
 		 * filter_element should never be anything except "copy_number" for this operation, 
 		 * which is the default value above, and the hidden param in the html form
 		 * thus, this next part is actually redundant, but will include it in case we need to 
 		 * change the filter_element on the fly for some reason in the future
 		 * 	-RCL
 		 */ 	
 		if(rgForm.getFilter_element()!=null)
 		    filter_element = (String) rgForm.getFilter_element();
 		
 		Map<String, Object> filterParams = new HashMap<String, Object>();		
 		//put all params from the form in the filterparams map
 		filterParams.put( "filter_value4", operator );
 		filterParams.put( "filter_value5", nocalls );
 		filterParams.put( "filter_value6", percent );
 		filterParams.put( "filter_element", filter_element );
 		
 		if(reportBean!=null) {
 			//This will generate get a resultant and store it in the cache
 		    ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(reportBean, filterParams);
 			//store the name of the query in the form so that we can later pull it out of cache
 		    //reportBean is now our new bean (a.k.a. rb, with newly populated XML, ect)
 			reportBean = rgHelper.getReportBean();
 			//add the new name so we know its a copy number filter
 			rgForm.setQueryName(reportBean.getResultantCacheKey());
 		     
        	}
 		//now send everything that we have done to the actual method that will render the report
 		return runGeneViewReport(mapping, rgForm, request, response);
 	}
 
 
     public ActionForward webGenomeRequest(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
             String sessionID = request.getSession().getId();
           //String hostURL = PropertyLoader.loadProperties(RembrandtConstants.WEB_GENOMEAPP_PROPERTIES).
             //getProperty("webGenome.hostURL");
            //actually get the list of specimen names
 	       String[] specimanIds = (String[])request.getSession().getAttribute("tmp_web_genome");
 	       String queryName =(String)request.getSession().getAttribute("tmp_web_genome_qname");
            // build WebGenome request URL
            ReportBean report = presentationTierCache.getReportBean(sessionID, queryName);
 	       List<String> specimenNames = Arrays.asList(specimanIds);
            String hostURL = System.getProperty("webGenome.url");
            String webGenomeURL = WebGenomeHelper.buildURL(report, sessionID, hostURL, specimenNames);
            //request.getSession().removeAttribute("tmp_web_genome");
 	       //request.getSession().removeAttribute("tmp_web_genome_qname");
             logger.debug("Sending Plotting request to WebGenome Application:  URL: " + webGenomeURL);
             ActionForward f2 = new ActionForward("webGenome", webGenomeURL, true, false);
             return f2;
     }
 
     public ActionForward webGenomeRequestTest(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
     		return webGenomeRequest( mapping,  form, request,  response);
     }
 }

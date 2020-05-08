 /*
  * Created on Nov 19, 2004
  */ 
 package gov.nih.nci.nautilus.ui.struts.action;
 
 import gov.nih.nci.nautilus.cache.CacheManagerDelegate;
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.query.CompoundQuery;
 import gov.nih.nci.nautilus.query.OperatorType;
 import gov.nih.nci.nautilus.resultset.Resultant;
 import gov.nih.nci.nautilus.resultset.ResultsetManager;
 import gov.nih.nci.nautilus.ui.bean.ReportBean;
 import gov.nih.nci.nautilus.ui.helper.ReportGeneratorHelper;
 import gov.nih.nci.nautilus.ui.struts.form.ClinicalDataForm;
 import gov.nih.nci.nautilus.ui.struts.form.ComparativeGenomicForm;
 import gov.nih.nci.nautilus.ui.struts.form.GeneExpressionForm;
 import gov.nih.nci.nautilus.ui.struts.form.ReportGeneratorForm;
 import gov.nih.nci.nautilus.view.ViewFactory;
 import gov.nih.nci.nautilus.view.ViewType;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 
 public class ReportGeneratorAction extends DispatchAction {
 
     private Logger logger = Logger.getLogger(ReportGeneratorAction.class);
 	
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
     	ReportGeneratorForm rgForm = (ReportGeneratorForm)form;
     	String sessionId = request.getSession().getId();
     	//get the specified report bean from the cache using the query name as the key
     	ReportBean reportBean = CacheManagerDelegate.getInstance().getReportBean(sessionId,rgForm.getQueryName());
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
     		CompoundQuery cQuery = ((CompoundQuery)(reportBean.getResultant().getAssociatedQuery()));
     		//Mark this as a filter report
     		String queryName = cQuery.getQueryName();
     		//don't mark it again as a filter report if it is already a filter 
     		//report at present this will cause the old result in cache
     		//to be overwritten...
     		if(queryName.indexOf("filter report")<0) {
     			queryName = queryName+" filter report";
     		}
     		//change the name of the associated query
     		cQuery.setQueryName(queryName);
     		//set the modified query in to the resultant
     		resultant.setAssociatedQuery(cQuery);
     		//Create a new bean to store the new resultant, name, param combination
     		ReportBean newReportBean = new ReportBean();
     		//set the retrieval key for the ReportBean
     		newReportBean.setResultantCacheKey(queryName);
     		//add the resultant to the new bean
     		newReportBean.setResultant(resultant);
     		//put the new bean in cache
     		CacheManagerDelegate.getInstance().addToSessionCache(sessionId, queryName,newReportBean );
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
     		
     		ReportGeneratorHelper generatorHelper = new ReportGeneratorHelper(cQuery,filterParams); 
     		//get the final constructed report bean
     		reportBean = generatorHelper.getReportBean();
     	}
     	//Do we have a ReportBean... we have to have a ReportBean
     	if(reportBean!=null) {
 	    	//Check to see if there is an XSLT specified
     		if("".equals(rgForm.getXsltFileName())||rgForm.getXsltFileName()==null) {
 	    		//If no filters specified then use the default XSLT
 	    		request.setAttribute(NautilusConstants.XSLT_FILE_NAME,NautilusConstants.DEFAULT_XSLT_FILENAME);
 	    	}else {
 	    		//Apply any filters defined in the form
 	    		request.setAttribute(NautilusConstants.XSLT_FILE_NAME,rgForm.getXsltFileName());
 	    	}
 	    	//add the Filter Parameters from the form to the forwarding request
 	    	request.setAttribute(NautilusConstants.FILTER_PARAM_MAP, rgForm.getFilterParams());
 	    	//put the report xml in the request
 	    	request.setAttribute(NautilusConstants.REPORT_XML, reportBean.getReportXML());
     	}else {
     		//Throw an exception because you should never call this action method
     		//unless you have already generated the report and stored it in the cache
     		logger.error( new IllegalStateException("Can not find the desired report in cache"));
     	}
     	//Go to the geneViewReport.jsp to render the report
     	return mapping.findForward("runGeneViewReport");
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
         request.setAttribute("queryName",NautilusConstants.PREVIEW_RESULTS);
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
 		CompoundQuery cquery = CacheManagerDelegate.getInstance().getQuery(sessionId, queryName );
 		if(cquery!=null) {
 			cquery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 			cquery.setQueryName(prb_queryName);
 			//This will generate the report and store it in the cache
 			ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cquery, sampleIds );
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
 		ReportBean reportBean = CacheManagerDelegate.getInstance().getReportBean(sessionId, queryName);
 		if(reportBean!=null) {
 			//This will generate get a resultant and store it in the cache
 			ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(reportBean, rgForm.getFilterParams());
 			//store the name of the query in the form so that we can later pull it out of cache
 			reportBean = rgHelper.getReportBean();
 			rgForm.setQueryName(reportBean.getResultantCacheKey());
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
 		ReportBean reportBean = CacheManagerDelegate.getInstance().getReportBean(sessionId, queryName);
 		//CompoundQuery cquery = CacheManagerDelegate.getInstance().getQuery(sessionId, queryName );
 		
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
 		 * filter_type should never be anything except "copy_number" for this operation, 
 		 * which is the default value above, and the hidden param in the html form
 		 * thus, this next part is actually redundant, but will include it in case we need to 
 		 * change the filter_type on the fly for some reason in the future
 		 * 	-RCL
 		 */ 	
 		if(rgForm.getFilter_element()!=null)
 		    filter_element = (String) rgForm.getFilter_element();
 		
 		Map filterParams = new HashMap();		
 		//put all params from the form in the filterparams map
 		filterParams.put( "filter_value4", operator );
 		filterParams.put( "filter_value5", nocalls );
 		filterParams.put( "filter_value6", percent );
		filterParams.put( "filter_element", filter_element );
 		
 		/*
 		//hold our samples for exclusion
 		String[] sampleIds = null;
 		excludedSamples = ResultsetManager.getSampleIdsforCopyNumberFilter(resultant, nocalls, percent, operator);
 		if(excludedSamples!=null)
 		    sampleIds = (String[]) excludedSamples.toArray(new String[0]);
 		 */
 		
 		//ReportBean rb = new ReportBean();
 		//our newly populated map
 		//rb.setFilterParams(filterParams);
 		//the name from the old bean, plus the additional string
 		//rb.setResultantCacheKey(reportBean.getResultantCacheKey() + " Copy Number Filter");
 		//put the resultant from the old bean into the new bean
 		//rb.setResultant(resultant);
 		
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
 	
 }
